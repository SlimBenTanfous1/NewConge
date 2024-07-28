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

    public Departement getDepartmentByUserId(int userId) throws SQLException {
        return userService.getDepartmentByUserId(userId);
    }

    public Role getRoleByUserId(int userId) throws SQLException {
        return userService.getRoleByUserId(userId);
    }

    public void assignRoleAndDepartment(int userId, int roleId, int departmentId) throws SQLException {
        ServiceUtilisateur userService = new ServiceUtilisateur();

        if (!isUserExists(userId)) {
            throw new RuntimeException("User does not exist: " + userId);
        }

        Integer managerId = findManager(userId, roleId, departmentId);

        // Debug statements
        System.out.println("Assigning role and department for user ID: " + userId);
        System.out.println("Role ID: " + roleId);
        System.out.println("Department ID: " + departmentId);
        System.out.println("Manager ID: " + (managerId != null ? managerId : "null"));

        userService.updateUserManager(userId, managerId);
        userService.updateUserDepartment(userId, departmentId);
        userService.removeUserRole(userId);
        userService.addUserRole(userId, roleId);
    }






    private Integer findManager(int userId, int roleId, int departmentId) throws SQLException {
        Role userRole = roleService.getRoleById(roleId);
        Departement userDept = departementService.getDepartementById(departmentId);

        // Debug statements
        System.out.println("Finding manager for user ID: " + userId + ", role ID: " + roleId + ", department ID: " + departmentId);
        System.out.println("User Role: " + userRole.getNom());
        System.out.println("User Department: " + userDept.getNom());

        // Check if user is DG or belongs to Direction Générale
        if ("DG".equals(userRole.getNom())) {
            System.out.println("User role is DG for user ID: " + userId);
            return null; // DG role should not have a manager
        }

        if ("Direction Générale".equals(userDept.getNom())) {
            System.out.println("User belongs to Direction Générale for user ID: " + userId);
            return null; // Direction Générale should not have a manager
        }

        // Find the manager based on the department hierarchy
        String query = "SELECT ID_Manager FROM departement WHERE ID_Departement = ?";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setInt(1, departmentId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Integer managerId = resultSet.getInt("ID_Manager");
                if (managerId != null && managerId != 0) {
                    System.out.println("Found manager ID: " + managerId + " for user ID: " + userId);
                    return managerId;
                } else {
                    System.out.println("No manager found in department hierarchy for user ID: " + userId);
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding manager: " + e.getMessage(), e);
        }

        // Find the manager based on the role hierarchy
        query = "SELECT parent_role_id FROM rolehierarchie WHERE role_id = ?";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setInt(1, roleId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Integer parentRoleId = resultSet.getInt("parent_role_id");
                if (parentRoleId != null && parentRoleId != 0) {
                    System.out.println("Found parent role ID: " + parentRoleId + " for user ID: " + userId);
                    // Find a user with the parent role in the same department
                    query = "SELECT ID_User FROM user_role ur JOIN user u ON ur.ID_User = u.ID_User WHERE ur.ID_Role = ? AND u.ID_Departement = ?";
                    try (PreparedStatement innerStatement = cnx.prepareStatement(query)) {
                        innerStatement.setInt(1, parentRoleId);
                        innerStatement.setInt(2, departmentId);
                        ResultSet innerResultSet = innerStatement.executeQuery();
                        if (innerResultSet.next()) {
                            Integer parentId = innerResultSet.getInt("ID_User");
                            System.out.println("Found manager ID: " + parentId + " based on role hierarchy for user ID: " + userId);
                            return parentId;
                        } else {
                            System.out.println("No manager found in role hierarchy for user ID: " + userId);
                        }
                    }
                }
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding manager based on role hierarchy: " + e.getMessage(), e);
        }

        // If no manager found, return null
        System.out.println("No manager found for user ID: " + userId);
        return null;
    }

    public void removeUserAssignment(int userId) throws SQLException {
        try {
            ensureConnection();
            if (userService.isUserExists(userId)) {
                String deleteRoleQuery = "DELETE FROM user_role WHERE ID_User = ?";
                try (PreparedStatement deleteRoleStatement = cnx.prepareStatement(deleteRoleQuery)) {
                    if (cnx == null || cnx.isClosed()) {
                        cnx = MyDataBase.getInstance().getCnx();
                    }
                    deleteRoleStatement.setInt(1, userId);
                    deleteRoleStatement.executeUpdate();
                }

                String updateDepartmentQuery = "UPDATE user SET ID_Departement = NULL WHERE ID_User = ?";
                try (PreparedStatement updateDepartmentStatement = cnx.prepareStatement(updateDepartmentQuery)) {
                    updateDepartmentStatement.setInt(1, userId);
                    updateDepartmentStatement.executeUpdate();
                }

                String updateManagerQuery = "UPDATE user SET ID_Manager = NULL WHERE ID_User = ?";
                try (PreparedStatement updateManagerStatement = cnx.prepareStatement(updateManagerQuery)) {
                    updateManagerStatement.setInt(1, userId);
                    updateManagerStatement.executeUpdate();
                }

                userService.updateSubordinatesManager(userId);
            } else {
                throw new RuntimeException("User does not exist: " + userId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error removing user assignment: " + e.getMessage(), e);
        }
    }

    private void reassignSubordinatesToNewManager(int userId, int roleId, int departementId) throws SQLException {
        List<User> subordinates = userService.getUsersWithoutManager();
        for (User subordinate : subordinates) {
            if (subordinate.getIdDepartement() == departementId && subordinate.getIdRole() == roleId) {
                userService.updateUserManager(subordinate.getIdUser(), userId);
            }
        }
    }

    private void reassignManagersForAllUsers() throws SQLException {
        List<User> allUsers = userService.getAllUsers();
        for (User user : allUsers) {
            if (user.getIdManager() == 0) {
                int managerId = findManager(user.getIdUser(), user.getIdRole(), user.getIdDepartement());
                if (managerId != user.getIdUser()) {
                    userService.updateUserManager(user.getIdUser(), managerId);
                }
            }
        }
    }
    public boolean isUserExists(int userId) throws SQLException {
        ensureConnection();
        String query = "SELECT 1 FROM user WHERE ID_User = ?";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            throw new RuntimeException("Error checking if user exists: " + e.getMessage(), e);
        }
    }
}
