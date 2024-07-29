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

    public ServiceSubordinateManager(ServiceRole roleService, ServiceDepartement departementService) {
        this.roleService = roleService;
        this.departementService = departementService;
    }

    private void ensureConnection() throws SQLException {
        if (cnx == null || cnx.isClosed()) {
            cnx = MyDataBase.getInstance().getCnx();
        }
    }

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

    private Integer findManagerByHierarchy(int userId, int roleId, int departmentId) throws SQLException {
        ensureConnection();
        Role userRole = getRoleByUserId2(userId);
        Departement userDept = getDepartmentByUserId2(userId);
        if (userRole == null || userDept == null) {
            return null;
        }
        if ("Employe".equals(userRole.getNom())) {
            String query = "SELECT u.ID_User FROM user_role ur JOIN user u ON ur.ID_User = u.ID_User " +
                    "JOIN role r ON ur.ID_Role = r.ID_Role " +
                    "WHERE u.ID_Departement = ? AND r.nom != 'Employe' " +
                    "ORDER BY r.Level DESC LIMIT 1";
            try (PreparedStatement statement = cnx.prepareStatement(query)) {
                statement.setInt(1, departmentId);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    int managerId = resultSet.getInt("ID_User");
                    return managerId;
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error finding manager: " + e.getMessage(), e);
            }
        } else {
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
                    return managerId;
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error finding manager: " + e.getMessage(), e);
            }
            query = "WITH RECURSIVE dept_hierarchy AS (" +
                    "SELECT ID_Departement, Parent_Dept FROM departement WHERE ID_Departement = ? " +
                    "UNION ALL " +
                    "SELECT d.ID_Departement, d.Parent_Dept FROM departement d " +
                    "JOIN dept_hierarchy dh ON dh.Parent_Dept = d.ID_Departement " +
                    ") " +
                    "SELECT u.ID_User FROM user_role ur " +
                    "JOIN user u ON ur.ID_User = u.ID_User " +
                    "JOIN role r ON ur.ID_Role = r.ID_Role " +
                    "JOIN dept_hierarchy dh ON u.ID_Departement = dh.ID_Departement " +
                    "WHERE r.Level < (SELECT Level FROM role WHERE ID_Role = ?) " +
                    "ORDER BY r.Level DESC, dh.ID_Departement ASC LIMIT 1";
            try (PreparedStatement statement = cnx.prepareStatement(query)) {
                statement.setInt(1, departmentId);
                statement.setInt(2, roleId);
                ResultSet resultSet = statement.executeQuery();
                if (resultSet.next()) {
                    int managerId = resultSet.getInt("ID_User");
                    return managerId;
                }
            } catch (SQLException e) {
                throw new RuntimeException("Error finding manager: " + e.getMessage(), e);
            }
        }
        String query = "SELECT u.ID_User FROM user_role ur " +
                "JOIN user u ON ur.ID_User = u.ID_User " +
                "WHERE ur.ID_Role = (SELECT ID_Role FROM role WHERE nom = 'DG')";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                int managerId = resultSet.getInt("ID_User");
                return managerId;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error finding manager: " + e.getMessage(), e);
        }
        return null;
    }

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

    public void updateUserManager(int userId, Integer managerId) throws SQLException {
        String query = "UPDATE user SET ID_Manager = ? WHERE ID_User = ?";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            if (managerId == null) {
                statement.setNull(1, java.sql.Types.INTEGER);
            } else {
                statement.setInt(1, managerId);
            }
            statement.setInt(2, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user manager: " + e.getMessage(), e);
        }
    }

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
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user department: " + e.getMessage(), e);
        }
    }

    public void removeRoleAndDepartment(int userId) throws SQLException {
        updateUserRole(userId, null);
        updateUserDepartment(userId, null);
        updateUserManager(userId, null);

        String query = "SELECT ID_User FROM user WHERE ID_Manager = ?";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int subordinateId = resultSet.getInt("ID_User");
                updateUserManager(subordinateId, null);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error removing role and department: " + e.getMessage(), e);
        }
    }

    private void reassignSubordinatesToNewManager(int newManagerId, int roleId, int departmentId) throws SQLException {
        ensureConnection();
        String levelQuery = "SELECT r.Level AS roleLevel, d.Level AS deptLevel " +
                "FROM role r, departement d " +
                "WHERE r.ID_Role = ? AND d.ID_Departement = ?";
        int newManagerRoleLevel = -1;
        int newManagerDeptLevel = -1;
        try (PreparedStatement levelStatement = cnx.prepareStatement(levelQuery)) {
            levelStatement.setInt(1, roleId);
            levelStatement.setInt(2, departmentId);
            ResultSet levelResultSet = levelStatement.executeQuery();
            if (levelResultSet.next()) {
                newManagerRoleLevel = levelResultSet.getInt("roleLevel");
                newManagerDeptLevel = levelResultSet.getInt("deptLevel");
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error getting new manager role and department level: " + e.getMessage(), e);
        }
        if (newManagerRoleLevel == -1 || newManagerDeptLevel == -1) {
            throw new RuntimeException("Could not determine role or department level for the new manager.");
        }
        String query = "SELECT u.ID_User " +
                "FROM user u " +
                "JOIN user_role ur ON u.ID_User = ur.ID_User " +
                "JOIN role r ON ur.ID_Role = r.ID_Role " +
                "JOIN departement d ON u.ID_Departement = d.ID_Departement " +
                "WHERE r.Level = ? AND d.Level = ?";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setInt(1, newManagerRoleLevel + 1);
            statement.setInt(2, newManagerDeptLevel + 1);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                int subordinateId = resultSet.getInt("ID_User");
                updateUserManager(subordinateId, newManagerId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error reassigning subordinates: " + e.getMessage(), e);
        }
    }


    public void reassignManagersForAllUsers() throws SQLException {
        List<User> allUsers = getAllUsers();
        for (User user : allUsers) {
            if (user.getIdManager() == 0) {
                int managerId = findManager(user.getIdUser(), user.getIdRole(), user.getIdDepartement());
                if (managerId != user.getIdUser()) {
                    updateUserManager(user.getIdUser(), managerId);
                }
            }
        }
    }

    public void reassignUsersWithoutManager(int newManagerId) throws SQLException {
        ensureConnection();

        User newManager = getUserById(newManagerId);
        if (newManager == null) {
            return;
        }
        Role newManagerRole = getRoleByUserId2(newManager.getIdUser());
        Departement newManagerDept = getDepartmentByUserId2(newManager.getIdUser());

        List<User> usersWithoutManager = getUsersWithoutManager();
        for (User user : usersWithoutManager) {
            Role userRole = getRoleByUserId2(user.getIdUser());
            Departement userDept = getDepartmentByUserId2(user.getIdUser());

            if (userRole != null && userDept != null) {
                if (userRole.getLevel() < newManagerRole.getLevel() &&
                        userDept.getLevel() > newManagerDept.getLevel() &&
                        userDept.getParentDept() == newManager.getIdDepartement()) {
                    updateUserManager(user.getIdUser(), newManagerId);
                }
            }
        }
    }

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

        Role userRole = getRoleByUserId2(userId); // Correctly retrieve role by user ID
        Departement userDept = getDepartmentByUserId2(userId); // Correctly retrieve department by user ID

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

            // Automatically set other users with lower level under the new assigned user
            reassignSubordinatesToNewManager(userId, roleId, departmentId);
        }
    }


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

    private Integer findManager(int userId, int roleId, int departmentId) throws SQLException {
        Role userRole = getRoleByUserId2(userId);
        Departement userDept = getDepartmentByUserId2(userId);
        if (userRole != null && "DG".equals(userRole.getNom())) {
            return null;
        }
        if (userDept != null && "Direction Générale".equals(userDept.getNom())) {
            return null;
        }
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
                return departement;
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving department by user ID: " + e.getMessage(), e);
        }
        return null;
    }

    public Role getRoleByUserId2(int userId) throws SQLException {
        ensureConnection();
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
                return role;
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

    public void reassignEmployeesToNewManager(int oldManagerId, int newManagerId) throws SQLException {
        String query = "UPDATE user SET ID_Manager = ? WHERE ID_Manager = ?";
        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setInt(1, newManagerId);
            statement.setInt(2, oldManagerId);
            statement.executeUpdate();
        }
    }

}
