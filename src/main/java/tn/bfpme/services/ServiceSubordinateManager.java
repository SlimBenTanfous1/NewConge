package tn.bfpme.services;

import tn.bfpme.models.Departement;
import tn.bfpme.models.Role;
import tn.bfpme.models.User;
import tn.bfpme.utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.*;
public class ServiceSubordinateManager {
private ServiceUtilisateur userService;
private ServiceRole roleService;
private ServiceDepartement departementService;

// Constructor
public ServiceSubordinateManager(ServiceUtilisateur userService, ServiceRole roleService, ServiceDepartement departementService) {
    this.userService = userService;
    this.roleService = roleService;
    this.departementService = departementService;
}

    // Get all users
    public List<User> getAllUsers() throws SQLException {
        return userService.getAllUsers();
    }

    // Get department by user ID
    public Departement getDepartmentByUserId(int userId) throws SQLException {
        return userService.getDepartmentByUserId(userId);
    }

    // Get role by user ID
    public Role getRoleByUserId(int userId) throws SQLException {
        return userService.getRoleByUserId(userId);
    }

    // Assign role and department to a user and find a manager
    public void assignRoleAndDepartment(int userId, int roleId, int departementId) throws SQLException {
        // Update user's role and department
        userService.updateUserRoleAndDepartment(userId, roleId, departementId);

        // Find and assign manager
        int managerId = findManager(userId, roleId, departementId);
        userService.updateUserManager(userId, managerId);
    }

    // Find manager for a user based on hierarchy rules
    public int findManager(int userId, int roleId, int departementId) throws SQLException {
        Role userRole = roleService.getRoleById(roleId);
        Departement userDept = departementService.getDepartementById(departementId);

        if (userRole == null) {
            System.out.println("User role is null for user ID: " + userId);
            return 0;
        }

        if (userDept == null) {
            System.out.println("User department is null for user ID: " + userId);
            return 0;
        }

        // Step 1: Check for the highest level user in the parent department
        while (userDept != null) {
            List<User> potentialManagers = userService.getUsersByDepartementId(userDept.getParentDept());

            for (User potentialManager : potentialManagers) {
                Role managerRole = roleService.getRoleById(potentialManager.getIdRole());
                if (managerRole != null) {
                    if (managerRole.getLevel() == userRole.getLevel() - 1) {
                        return potentialManager.getIdUser();
                    }
                } else {
                    System.out.println("Manager role is null for user: " + potentialManager.getNom());
                }
            }

            // Move up to the next parent department
            Integer parentDeptId = userDept.getParentDept();
            if (parentDeptId == null) {
                break;
            }
            userDept = departementService.getDepartementById(parentDeptId);
        }

        // Step 2: Check for the closest level to the user
        List<User> potentialManagers = userService.getUsersByDepartementId(userDept.getParentDept());
        for (User potentialManager : potentialManagers) {
            Role managerRole = roleService.getRoleById(potentialManager.getIdRole());
            if (managerRole != null) {
                if (managerRole.getLevel() < userRole.getLevel()) {
                    return potentialManager.getIdUser();
                }
            } else {
                System.out.println("Manager role is null for user: " + potentialManager.getNom());
            }
        }

        // Step 3: Default to DG if no manager is found
        User dg = userService.getUserByRole("DG");
        return dg != null ? dg.getIdUser() : 0;
    }

    // Remove role and department assignment for a user
    public void removeUserAssignment(int userId) throws SQLException {
        // Remove role and department
        userService.updateUserRoleAndDepartment(userId, 0, 0);

        // Set user's manager to null
        userService.updateUserManager(userId, 0);

        // Update subordinates
        List<User> subordinates = userService.getSubordinates(userId);
        for (User subordinate : subordinates) {
            userService.updateUserManager(subordinate.getIdUser(), 0);
        }
    }

    // Reassign subordinates to a new manager
    public void reassignSubordinates(int oldUserId, int newUserId) throws SQLException {
        List<User> subordinates = userService.getSubordinates(oldUserId);

        for (User subordinate : subordinates) {
            userService.updateUserManager(subordinate.getIdUser(), newUserId);
        }
    }
}