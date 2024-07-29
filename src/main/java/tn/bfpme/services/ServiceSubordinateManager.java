package tn.bfpme.services;

import tn.bfpme.models.Departement;
import tn.bfpme.models.Role;
import tn.bfpme.models.User;
import tn.bfpme.utils.MyDataBase;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ServiceSubordinateManager {
    private ServiceRole roleService;
    private ServiceDepartement departementService;

    private static Connection cnx = MyDataBase.getInstance().getCnx();

    // Constructor
    public ServiceSubordinateManager(ServiceRole roleService, ServiceDepartement departementService) {
        this.roleService = roleService;
        this.departementService = departementService;
    }

    private void ensureConnection() throws SQLException {
        if (cnx == null || cnx.isClosed()) {
            cnx = MyDataBase.getInstance().getCnx();
        }
    }

    // Update user's role and department and assign the appropriate manager
    public User getUserById(int userId) throws SQLException {
        ensureConnection();
        String query = "SELECT u.*, d.nom AS departementNom, r.nom AS roleNom, r.ID_Role, r.Level AS roleLevel, d.ID_Departement, d.Level AS deptLevel, d.Parent_Dept " +
                "FROM user u " +
                "LEFT JOIN departement d ON u.ID_Departement = d.ID_Departement " +
                "LEFT JOIN user_role ur ON u.ID_User = ur.ID_User " +
                "LEFT JOIN role r ON ur.ID_Role = r.ID_Role " +
                "WHERE u.ID_User = ?";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                User user = extractUserFromResultSet(resultSet);
                user.setDepartementNom(resultSet.getString("departementNom"));
                user.setRoleNom(resultSet.getString("roleNom"));
                user.setIdRole(resultSet.getInt("ID_Role"));
                user.setIdDepartement(resultSet.getInt("ID_Departement"));
                return user;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving user by ID: " + e.getMessage(), e);
        }
        return null;
    }

    private User extractUserFromResultSet(ResultSet resultSet) throws SQLException {
        User user = new User();
        user.setIdUser(resultSet.getInt("ID_User"));
        user.setNom(resultSet.getString("nom"));
        user.setPrenom(resultSet.getString("prenom"));
        user.setEmail(resultSet.getString("email"));
        user.setMdp(resultSet.getString("MDP"));
        user.setImage(resultSet.getString("image"));
        user.setCreationDate(resultSet.getDate("Creation_Date") != null ? resultSet.getDate("Creation_Date").toLocalDate() : null);
        user.setIdManager(resultSet.getInt("ID_Manager"));
        user.setIdDepartement(resultSet.getInt("ID_Departement"));
        user.setID_UserSolde(resultSet.getInt("idSolde"));
        user.setDepartementNom(resultSet.getString("departementNom"));
        user.setRoleNom(resultSet.getString("roleNom"));
        return user;
    }

    // Find manager by hierarchy
    private Integer findManagerByHierarchy(int userId, int roleId, int departmentId) throws SQLException {
        ensureConnection();
        System.out.println("Finding manager by hierarchy for user ID: " + userId);

        Role userRole = getRoleByUserId2(userId);
        Departement userDept = getDepartmentByUserId2(userId);

        if (userRole == null || userDept == null) {
            System.out.println("Invalid role or department for user ID: " + userId);
            return null;
        }

        System.out.println("User Role: " + userRole.getNom());
        System.out.println("User Department: " + userDept.getNom());

        if ("Employe".equals(userRole.getNom())) {
            // Special case for Employe role
            String query = "SELECT u.ID_User FROM user_role ur JOIN user u ON ur.ID_User = u.ID_User " +
                    "JOIN role r ON ur.ID_Role = r.ID_Role " +
                    "WHERE u.ID_Departement = ? AND r.nom != 'Employe' " +
                    "ORDER BY r.Level DESC LIMIT 1";
            try (PreparedStatement statement = cnx.prepareStatement(query)) {
                statement.setInt(1, departmentId);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    int managerId = resultSet.getInt("ID_User");
                    System.out.println("Found manager in the same department with different role: " + managerId);
                    return managerId;
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error finding manager: " + e.getMessage(), e);
            }
        } else {
            // General case for other roles
            while (true) {
                String query = "SELECT u.ID_User FROM user_role ur JOIN user u ON ur.ID_User = u.ID_User " +
                        "JOIN role r ON ur.ID_Role = r.ID_Role " +
                        "WHERE u.ID_Departement = ? AND r.Level < (SELECT Level FROM role WHERE ID_Role = ?) " +
                        "ORDER BY r.Level DESC LIMIT 1";
                try (PreparedStatement statement = cnx.prepareStatement(query)) {
                    statement.setInt(1, departmentId);
                    statement.setInt(2, roleId);
                    ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        int managerId = resultSet.getInt("ID_User");
                        if (managerId != userId) {
                            System.out.println("Found manager in the same department with higher level: " + managerId);
                            return managerId;
                        }
                    }
                } catch (SQLException e) {
                    throw new RuntimeException("Error finding manager: " + e.getMessage(), e);
                }

                // Move up the hierarchy
                String parentDeptQuery = "SELECT Parent_Dept FROM departement WHERE ID_Departement = ?";
                try (PreparedStatement statement = cnx.prepareStatement(parentDeptQuery)) {
                    statement.setInt(1, departmentId);
                    ResultSet resultSet = statement.executeQuery();
                    if (resultSet.next()) {
                        departmentId = resultSet.getInt("Parent_Dept");
                        if (departmentId == 0) {
                            break; // No more parent departments, exit loop
                        }
                    } else {
                        break; // No parent department found, exit loop
                    }
                } catch (SQLException e) {
                    throw new RuntimeException("Error finding parent department: " + e.getMessage(), e);
                }
            }
        }

        // Default to DG if no other manager is found
        String query = "SELECT u.ID_User FROM user_role ur " +
                "JOIN user u ON ur.ID_User = u.ID_User " +
                "WHERE ur.ID_Role = (SELECT ID_Role FROM role WHERE nom = 'DG')";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int managerId = resultSet.getInt("ID_User");
                System.out.println("Defaulting to DG manager: " + managerId);
                return managerId != userId ? managerId : null;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding manager: " + e.getMessage(), e);
        }

        System.out.println("No manager found for user ID: " + userId);
        return null;
    }

    // Update subordinates' managers if a new manager is assigned to the same role and department
    private void updateSubordinateManagers(int managerId, int departmentId) throws SQLException {
        String query = "SELECT u.ID_User FROM user u WHERE u.ID_Departement = ? AND u.ID_Manager IS NULL";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setInt(1, departmentId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int subordinateId = resultSet.getInt("ID_User");
                updateUserManager(subordinateId, managerId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error updating subordinate managers: " + e.getMessage(), e);
        }
    }

    // Method to update user manager
    public void updateUserManager(int userId, Integer managerId) throws SQLException {
        // Ensure DG's manager is always null
        Role userRole = getRoleByUserId2(userId);
        if (userRole != null && "DG".equals(userRole.getNom())) {
            managerId = null;
        }

        String query = "UPDATE user SET ID_Manager = ? WHERE ID_User = ?";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            if (managerId == null) {
                statement.setNull(1, java.sql.Types.INTEGER);
                System.out.println("Manager ID is null for user ID: " + userId + ". Setting manager to null.");
            } else {
                statement.setInt(1, managerId);
                System.out.println("Setting manager ID: " + managerId + " for user ID: " + userId);
            }
            statement.setInt(2, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user manager: " + e.getMessage(), e);
        }
    }



    // Method to update user role
    public void updateUserRole(int userId, Integer roleId) throws SQLException {
        ensureConnection();
        if (roleId != null) {
            String query = "INSERT INTO user_role (ID_User, ID_Role) VALUES (?, ?) " +
                    "ON DUPLICATE KEY UPDATE ID_Role = VALUES(ID_Role)";
            try (PreparedStatement statement = cnx.prepareStatement(query)) {
                statement.setInt(1, userId);
                statement.setInt(2, roleId);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error updating user role: " + e.getMessage(), e);
            }
        } else {
            String query = "DELETE FROM user_role WHERE ID_User = ?";
            try (PreparedStatement statement = cnx.prepareStatement(query)) {
                statement.setInt(1, userId);
                statement.executeUpdate();
            } catch (SQLException e) {
                throw new RuntimeException("Error deleting user role: " + e.getMessage(), e);
            }
        }
    }

    public void updateUserDepartment(int userId, Integer departmentId) throws SQLException {
        ensureConnection();
        String query = "UPDATE user SET ID_Departement = ? WHERE ID_User = ?";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            if (departmentId != null) {
                statement.setInt(1, departmentId);
            } else {
                statement.setNull(1, java.sql.Types.INTEGER);
            }
            statement.setInt(2, userId);
            statement.executeUpdate();
            System.out.println("Updated department for user ID: " + userId + " to department ID: " + departmentId);
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user department: " + e.getMessage(), e);
        }
    }

    // Method to remove user's role, department, and manager, and set their subordinates' managers to null
    public void removeRoleAndDepartment(int userId) throws SQLException {
        System.out.println("Removing role, department, and manager for user ID: " + userId);

        // Remove user's role
        updateUserRole(userId, null);
        System.out.println("Role removed for user ID: " + userId);

        // Remove user's department
        updateUserDepartment(userId, null);
        System.out.println("Department removed for user ID: " + userId);

        // Remove user's manager
        updateUserManager(userId, null);
        System.out.println("Manager removed for user ID: " + userId);

        // Find subordinates and set their managers to null
        String query = "SELECT ID_User FROM user WHERE ID_Manager = ?";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int subordinateId = resultSet.getInt("ID_User");
                updateUserManager(subordinateId, null);
                System.out.println("Set manager to null for subordinate ID: " + subordinateId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error removing role and department: " + e.getMessage(), e);
        }
    }

    // Method to reassign users without a manager
    public void reassignUsersWithoutManager(int newManagerId) throws SQLException {
        ensureConnection();

        // Fetch the new manager's role and department details
        User newManager = getUserById(newManagerId);
        if (newManager == null) {
            System.out.println("Invalid role or department for new manager ID: " + newManagerId);
            return;
        }
        Role newManagerRole = roleService.getRoleByUserId2(newManager.getIdRole());
        Departement newManagerDept = departementService.getDepartmentByUserId2(newManager.getIdDepartement());

        System.out.println("Reassigning users without a manager to new manager ID: " + newManagerId);

        // Fetch users without a manager
        List<User> usersWithoutManager = getUsersWithoutManager();
        for (User user : usersWithoutManager) {
            Role userRole = roleService.getRoleByUserId2(user.getIdRole());
            Departement userDept = departementService.getDepartmentByUserId2(user.getIdDepartement());

            if (userRole != null && userDept != null) {
                System.out.println("Checking criteria for user ID: " + user.getIdUser());
                System.out.println("User Role Level: " + userRole.getLevel());
                System.out.println("New Manager Role Level: " + newManagerRole.getLevel());
                System.out.println("User Dept Level: " + userDept.getLevel());
                System.out.println("New Manager Dept Level: " + newManagerDept.getLevel());
                System.out.println("User Dept Parent: " + userDept.getParentDept());
                System.out.println("New Manager Dept ID: " + newManager.getIdDepartement());

                if (userRole.getLevel() < newManagerRole.getLevel() &&
                        userDept.getLevel() > newManagerDept.getLevel() &&
                        userDept.getParentDept() == newManager.getIdDepartement()) {
                    updateUserManager(user.getIdUser(), newManagerId);
                    System.out.println("Reassigned user ID: " + user.getIdUser() + " to manager ID: " + newManagerId);
                } else {
                    System.out.println("Skipping reassignment for user ID: " + user.getIdUser() + " due to not meeting criteria.");
                }
            } else {
                System.out.println("Skipping reassignment for user ID: " + user.getIdUser() + " due to invalid role or department.");
            }
        }
    }

    // Update the assignRoleAndDepartment method to call the reassignUsersWithoutManager method
    public void assignRoleAndDepartment(int userId, int roleId, int departmentId) throws SQLException {
        ensureConnection();
        System.out.println("Assigning role and department for user ID: " + userId);

        // Check for duplicate role and department
        if (isDuplicateRoleAndDepartment(userId, roleId, departmentId)) {
            System.out.println("Un autre utilisateur a déjà ce rôle et ce département.");
            throw new RuntimeException("Erreur: Un autre utilisateur a déjà ce rôle et ce département.");
        }

        // Update user's role and department
        updateUserRole(userId, roleId);
        updateUserDepartment(userId, departmentId);

        Role userRole = roleService.getRoleByUserId2(roleId);
        Departement userDept = departementService.getDepartmentByUserId2(departmentId);

        // Special handling for DG
        if (userRole != null && "DG".equals(userRole.getNom())) {
            System.out.println("User is assigned as DG.");
            updateUserManager(userId, null); // DG should have no manager
            // Reassign all directors to report to DG
            reassignDirectorsToDG(userId);
        } else {
            // Find the appropriate manager
            Integer managerId = findManager(userId, roleId, departmentId);
            if (managerId != null && !managerId.equals(userId)) {
                // Update user's manager
                updateUserManager(userId, managerId);
            } else {
                // If no manager found or if the user is DG, set manager to null
                updateUserManager(userId, null);
            }

            // Update subordinates' managers if a new manager is assigned to the same role and department
            updateSubordinateManagers(userId, departmentId);

            // Reassign users without a manager
            reassignUsersWithoutManager(userId);
        }
    }

    // Reassign all directors to report to DG
    private void reassignDirectorsToDG(int dgUserId) throws SQLException {
        String query = "SELECT u.ID_User FROM user u " +
                "JOIN user_role ur ON u.ID_User = ur.ID_User " +
                "JOIN role r ON ur.ID_Role = r.ID_Role " +
                "WHERE r.nom = 'Directeur'";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int directorId = resultSet.getInt("ID_User");
                updateUserManager(directorId, dgUserId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reassigning directors to DG: " + e.getMessage(), e);
        }
    }

    // Refined findManager method for completeness
    private Integer findManager(int userId, int roleId, int departmentId) throws SQLException {
        Role userRole = roleService.getRoleByUserId2(roleId);
        Departement userDept = departementService.getDepartmentByUserId2(departmentId);

        // Debug statements
        System.out.println("Finding manager for user ID: " + userId + ", role ID: " + roleId + ", department ID: " + departmentId);
        System.out.println("User Role: " + (userRole != null ? userRole.getNom() : "null"));
        System.out.println("User Department: " + (userDept != null ? userDept.getNom() : "null"));

        // Check if user is DG or belongs to Direction Générale
        if (userRole != null && "DG".equals(userRole.getNom())) {
            System.out.println("User role is DG for user ID: " + userId);
            return null; // DG role should not have a manager
        }

        if (userDept != null && "Direction Générale".equals(userDept.getNom())) {
            System.out.println("User belongs to Direction Générale for user ID: " + userId);
            return null; // Direction Générale should not have a manager
        }

        // Find the manager based on role and department hierarchy
        return findManagerByHierarchy(userId, roleId, departmentId);
    }


    public List<User> getAllUsers() throws SQLException {
        ensureConnection();
        List<User> users = new ArrayList<>();
        String query = "SELECT u.*, d.nom AS departementNom, r.nom AS roleNom " +
                "FROM user u " +
                "LEFT JOIN departement d ON u.ID_Departement = d.ID_Departement " +
                "LEFT JOIN user_role ur ON u.ID_User = ur.ID_User " +
                "LEFT JOIN role r ON ur.ID_Role = r.ID_Role";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                User user = extractUserFromResultSet(resultSet);
                user.setDepartementNom(resultSet.getString("departementNom"));
                user.setRoleNom(resultSet.getString("roleNom"));
                users.add(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving all users: " + e.getMessage(), e);
        }
        return users;
    }

    // Method to get users without a manager
    public List<User> getUsersWithoutManager() throws SQLException {
        ensureConnection();
        List<User> users = new ArrayList<>();
        String query = "SELECT u.*, d.nom AS departementNom, r.nom AS roleNom " +
                "FROM user u " +
                "LEFT JOIN departement d ON u.ID_Departement = d.ID_Departement " +
                "LEFT JOIN user_role ur ON u.ID_User = ur.ID_User " +
                "LEFT JOIN role r ON ur.ID_Role = r.ID_Role " +
                "WHERE u.ID_Manager IS NULL";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                User user = extractUserFromResultSet(resultSet);
                user.setDepartementNom(resultSet.getString("departementNom"));
                user.setRoleNom(resultSet.getString("roleNom"));
                users.add(user);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving users without a manager: " + e.getMessage(), e);
        }
        return users;
    }

    public Departement getDepartmentByUserId2(int userId) throws SQLException {
        ensureConnection();
        System.out.println("Retrieving department for user ID: " + userId);
        String query = "SELECT d.* FROM departement d " +
                "JOIN user u ON d.ID_Departement = u.ID_Departement " +
                "WHERE u.ID_User = ?";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Departement departement = new Departement();
                departement.setIdDepartement(resultSet.getInt("ID_Departement"));
                departement.setNom(resultSet.getString("nom"));
                departement.setDescription(resultSet.getString("description"));
                departement.setParentDept(resultSet.getInt("Parent_Dept"));
                departement.setLevel(resultSet.getInt("Level"));
                System.out.println("Retrieved department: " + departement.getNom() + " for user ID: " + userId);
                return departement;
            } else {
                System.out.println("No department found for user ID: " + userId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving department by user ID: " + e.getMessage(), e);
        }
        return null;
    }

    public Role getRoleByUserId2(int userId) throws SQLException {
        ensureConnection();
        System.out.println("Retrieving role for user ID: " + userId);
        String query = "SELECT r.* FROM role r " +
                "JOIN user_role ur ON r.ID_Role = ur.ID_Role " +
                "WHERE ur.ID_User = ?";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Role role = new Role();
                role.setIdRole(resultSet.getInt("ID_Role"));
                role.setNom(resultSet.getString("nom"));
                role.setDescription(resultSet.getString("description"));
                role.setLevel(resultSet.getInt("Level"));
                System.out.println("Retrieved role: " + role.getNom() + " for user ID: " + userId);
                return role;
            } else {
                System.out.println("No role found for user ID: " + userId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving role by user ID: " + e.getMessage(), e);
        }
        return null;
    }
    private boolean isDuplicateRoleAndDepartment(int userId, int roleId, int departmentId) throws SQLException {
        String query = "SELECT COUNT(*) AS count FROM user u " +
                "JOIN user_role ur ON u.ID_User = ur.ID_User " +
                "WHERE ur.ID_Role = ? AND u.ID_Departement = ? AND u.ID_User != ?";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setInt(1, roleId);
            statement.setInt(2, departmentId);
            statement.setInt(3, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next() && resultSet.getInt("count") > 0) {
                return true;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error checking for duplicate role and department: " + e.getMessage(), e);
        }
        return false;
    }
}
