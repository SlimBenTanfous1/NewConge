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

    private static Connection cnx = MyDataBase.getInstance().getCnx();

    // Constructor
    public ServiceSubordinateManager(ServiceUtilisateur userService, ServiceRole roleService, ServiceDepartement departementService) {
        this.userService = userService;
        this.roleService = roleService;
        this.departementService = departementService;
    }

    private void ensureConnection() throws SQLException {
        if (cnx == null || cnx.isClosed()) {
            cnx = MyDataBase.getInstance().getCnx();
        }
    }

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
        // Ensure connection
        ensureConnection();

        // Update user's department
        String updateDepartmentQuery = "UPDATE user SET ID_Departement = ? WHERE ID_User = ?";
        try (PreparedStatement statement = cnx.prepareStatement(updateDepartmentQuery)) {
            statement.setInt(1, departementId);
            statement.setInt(2, userId);
            statement.executeUpdate();
        }

        // Remove old roles
        String deleteRoleQuery = "DELETE FROM user_role WHERE ID_User = ?";
        try (PreparedStatement statement = cnx.prepareStatement(deleteRoleQuery)) {
            statement.setInt(1, userId);
            statement.executeUpdate();
        }

        // Assign new role
        String insertRoleQuery = "INSERT INTO user_role (ID_User, ID_Role) VALUES (?, ?)";
        try (PreparedStatement statement = cnx.prepareStatement(insertRoleQuery)) {
            statement.setInt(1, userId);
            statement.setInt(2, roleId);
            statement.executeUpdate();
        }

        // Handle special cases where manager should be null
        Role userRole = roleService.getRoleById(roleId);
        Departement userDept = departementService.getDepartementById(departementId);
        if (userRole != null && ("DG".equals(userRole.getNom()) || (userDept != null && "Direction Générale".equals(userDept.getNom())))) {
            userService.updateUserManager(userId, null);
        } else {
            // Find and assign manager
            int managerId = findManager(userId, roleId, departementId);
            userService.updateUserManager(userId, managerId);
        }
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

        // Handle special case for DG role
        if ("DG".equals(userRole.getNom())) {
            System.out.println("User role is DG for user ID: " + userId);
            return 0; // DG has no manager
        }

        // Handle special case for Direction Générale department
        if ("Direction Générale".equals(userDept.getNom())) {
            System.out.println("User department is Direction Générale for user ID: " + userId);
            return 0; // Direction Générale has no parent department
        }

        // Step 1: Check for the highest level user in the parent department
        while (userDept != null) {
            System.out.println("Checking department: " + userDept.getNom());
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
                System.out.println("Parent department is null for department: " + userDept.getNom());
                break;
            }
            userDept = departementService.getDepartementById(parentDeptId);
            if (userDept == null) {
                System.out.println("Department not found for parent department ID: " + parentDeptId);
                break;
            }
        }

        // Step 2: Check for the closest level to the user
        if (userDept != null) {
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
        }

        // Step 3: Default to DG if no manager is found
        User dg = userService.getUserByRole("DG");
        return dg != null ? dg.getIdUser() : 0;
    }

    // Remove role and department assignment for a user
    public void removeUserAssignment(int userId) throws SQLException {
        try {
            // Remove role assignments from user_role table
            userService.removeUserRole(userId);

            // Set department to NULL in user table
            userService.updateUserDepartment(userId, null);

            // Set manager to NULL in user table
            userService.updateUserManager(userId, null);

            // Reassign subordinates' managers to NULL
            userService.updateSubordinatesManager(userId);
        } catch (SQLException e) {
            throw new RuntimeException("Error removing user assignment: " + e.getMessage(), e);
        }
    }

    // Reassign subordinates to a new manager
    private void reassignSubordinatesToNewManager(int userId, int roleId, int departementId) throws SQLException {
        List<User> subordinates = userService.getUsersWithoutManager();
        for (User subordinate : subordinates) {
            if (subordinate.getIdDepartement() == departementId) {
                userService.updateUserManager(subordinate.getIdUser(), userId);
            }
        }
    }
}
