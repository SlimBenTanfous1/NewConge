package tn.bfpme.services;

import tn.bfpme.interfaces.IUtilisateur;

import tn.bfpme.models.*;

import tn.bfpme.utils.MyDataBase;
import tn.bfpme.utils.SessionManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

public class ServiceUtilisateur implements IUtilisateur {

    private Connection cnx;

    public ServiceUtilisateur() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    @Override
    public List<User> afficherusers() {
        List<User> userList = new ArrayList<>();
        String sql = "SELECT * FROM user";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                User user = new User();
                user.setIdUser(rs.getInt("ID_User"));
                user.setNom(rs.getString("Nom"));
                user.setPrenom(rs.getString("Prenom"));
                user.setEmail(rs.getString("Email"));
                user.setMdp(rs.getString("MDP"));
                user.setImage(rs.getString("Image"));

                Date creationDate = rs.getDate("Creation_Date");
                if (creationDate != null) {
                    user.setCreationDate(creationDate.toLocalDate());
                }

                userList.add(user);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return userList;
    }


    @Override
    public UserConge TriType() {
        return null;
    }

    @Override
    public UserConge TriNom() {
        return null;
    }

    @Override
    public UserConge TriPrenom() {
        List<User> users = new ArrayList<>();
        List<Conge> conges = new ArrayList<>();
        String query = "WITH RECURSIVE Subordinates AS (" +
                "SELECT ID_User, Nom, Prenom, Email, Image, ID_Departement " +
                "FROM user " +
                "WHERE ID_Manager = ? " +
                "UNION " +
                "SELECT u.ID_User, u.Nom, u.Prenom, u.Email, u.Image, u.ID_Departement " +
                "FROM user u " +
                "INNER JOIN Subordinates s ON s.ID_User = u.ID_Manager) " +
                "SELECT * FROM Subordinates ORDER BY Prenom";
        try {
            PreparedStatement st = cnx.prepareStatement(query);
            st.setInt(1, SessionManager.getInstance().getUser().getIdUser());
            ResultSet rs = st.executeQuery();
            while (rs.next()) {
                User user = new User(
                        rs.getInt("ID_User"),
                        rs.getString("Nom"),
                        rs.getString("Prenom"),
                        rs.getString("Email"),
                        rs.getString("MDP"),
                        rs.getString("Image"),
                        rs.getInt("ID_Departement"),
                        rs.getInt("ID_Manager"),
                        rs.getDate("Creation_Date").toLocalDate()
                );
                users.add(user);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return new UserConge(users, conges);
    }

    public UserConge TriDateDebut() {
        List<User> users = new ArrayList<>();
        List<Conge> conges = new ArrayList<>();
        String query = "WITH RECURSIVE Subordinates AS (" +
                "SELECT ID_User, Nom, Prenom, Email, Image, ID_Departement " +
                "FROM user " +
                "WHERE ID_User = ? " +
                "UNION ALL " +
                "SELECT u.ID_User, u.Nom, u.Prenom, u.Email, u.Image, u.ID_Departement " +
                "FROM user u " +
                "INNER JOIN Subordinates s ON u.ID_Manager = s.ID_User " +
                ") " +
                "SELECT user.ID_User, user.Nom, user.Prenom, user.Email, user.Image, user.ID_Departement, " +
                "conge.ID_Conge, conge.TypeConge, conge.Statut, conge.DateFin, conge.DateDebut, conge.description, conge.file " +
                "FROM user " +
                "JOIN conge ON user.ID_User = conge.ID_User " +
                "WHERE user.ID_User IN (SELECT ID_User FROM Subordinates WHERE ID_User != ?) AND conge.Statut = ? ORDER BY conge.DateDebut";
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
            }
            PreparedStatement ps = cnx.prepareStatement(query);
            int currentUserId = SessionManager.getInstance().getUser().getIdUser();
            ps.setInt(1, currentUserId);
            ps.setInt(2, currentUserId);
            ps.setString(3, String.valueOf(Statut.En_Attente));
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                User user = new User();
                user.setIdUser(rs.getInt("ID_User"));
                user.setNom(rs.getString("Nom"));
                user.setPrenom(rs.getString("Prenom"));
                user.setEmail(rs.getString("Email"));
                user.setImage(rs.getString("Image"));
                user.setIdDepartement(rs.getInt("ID_Departement"));
                if (!users.contains(user)) {
                    users.add(user);
                }
                Conge conge = new Conge();
                conge.setIdConge(rs.getInt("ID_Conge"));
                conge.setDateDebut(rs.getDate("DateDebut").toLocalDate());
                conge.setDateFin(rs.getDate("DateFin").toLocalDate());
                //conge.setTypeConge(TypeConge.valueOf(rs.getString("TypeConge")));
                conge.setStatut(Statut.valueOf(rs.getString("Statut")));
                conge.setDescription(rs.getString("description"));
                conge.setFile(rs.getString("file"));
                conge.setIdUser(rs.getInt("ID_User"));
                conges.add(conge);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return new UserConge(users, conges);
    }

    @Override
    public UserConge TriDateFin() {
        return null;
    }

    @Override
    public UserConge AfficherApprove() {
        return null;
    }

    @Override
    public UserConge AfficherReject() {
        return null;
    }

    @Override
    public List<User> RechrecheRH(String recherche) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT u.ID_User, u.Nom, u.Prenom, u.Email, u.Image, u.ID_Departement, tc.ID_TypeConge, tc.Designation, tc.Pas, tc.PeriodeJ, tc.PeriodeM, tc.PeriodeA, tc.File " +
                "FROM `user` u " +
                "LEFT JOIN `user_typeconge` utc ON u.ID_User = utc.ID_User " +
                "LEFT JOIN `typeconge` tc ON utc.ID_TypeConge = tc.ID_TypeConge " +
                "WHERE u.`ID_User` LIKE ? " +
                "AND (u.`Nom` LIKE ? " +
                "OR u.`Prenom` LIKE ? " +
                "OR u.`Email` LIKE ? )";
        try (PreparedStatement ste = cnx.prepareStatement(sql)) {
            String searchPattern = "%" + recherche + "%";
            ste.setString(1, "%" + SessionManager.getInstance().getUser().getIdUser() + "%");
            ste.setString(2, searchPattern);
            ste.setString(3, searchPattern);
            ste.setString(4, searchPattern);
            ResultSet rs = ste.executeQuery();
            while (rs.next()) {
                User user = new User();
                user.setIdUser(rs.getInt("ID_User"));
                user.setNom(rs.getString("Nom"));
                user.setPrenom(rs.getString("Prenom"));
                user.setEmail(rs.getString("Email"));
                user.setImage(rs.getString("Image"));
                user.setIdDepartement(rs.getInt("ID_Departement"));

                TypeConge typeConge = new TypeConge();
                // typeConge.setIdTypeConge(rs.getInt("ID_TypeConge"), totalSolde);
                typeConge.setDesignation(rs.getString("Designation"));
                typeConge.setPas(rs.getDouble("Pas"));
                typeConge.setPeriodeJ(rs.getInt("PeriodeJ"));
                typeConge.setPeriodeM(rs.getInt("PeriodeM"));
                typeConge.setPeriodeA(rs.getInt("PeriodeA"));
                typeConge.setFile(rs.getBoolean("File"));

                user.addTypeConge(typeConge);

                if (!users.contains(user)) {
                    users.add(user);
                }
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return users;
    }

    public User getChef() {
        User chef = null;
        Connection cnx = MyDataBase.getInstance().getCnx(); // Assuming MyDataBase is your connection manager

        // Get the current user's department ID and role ID
        int currentDeptId = SessionManager.getInstance().getUser().getIdDepartement();
        int currentRoleId = SessionManager.getInstance().getUserRole().getIdRole();

        // Find parent roles of the current user's role
        List<Integer> parentRoleIds = ServiceRole.getParentRoleIds(currentRoleId);
        if (parentRoleIds.isEmpty()) {
            return null;
        }

        // Create SQL query to find a user with one of the parent roles in the same department
        String query = "SELECT u.ID_User, u.Nom, u.Prenom, u.Email, u.Image, u.ID_Departement " +
                "FROM user u " +
                "JOIN user_role ur ON u.ID_User = ur.ID_User " +
                "WHERE u.ID_Departement = ? AND ur.ID_Role IN (" +
                parentRoleIds.stream().map(String::valueOf).collect(Collectors.joining(",")) +
                ") LIMIT 1";

        try {
            PreparedStatement ps = cnx.prepareStatement(query);
            ps.setInt(1, currentDeptId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                chef = new User();
                chef.setIdUser(rs.getInt("ID_User"));
                chef.setNom(rs.getString("Nom"));
                chef.setPrenom(rs.getString("Prenom"));
                chef.setEmail(rs.getString("Email"));
                chef.setImage(rs.getString("Image"));
                chef.setIdDepartement(rs.getInt("ID_Departement"));

                // Retrieve the TypeConge associated with the chef
                List<TypeConge> typeConges = getTypeCongesByUserId(chef.getIdUser());
                for (TypeConge typeConge : typeConges) {
                    chef.addTypeConge(typeConge);
                }
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return chef;
    }

    private List<TypeConge> getTypeCongesByUserId(int userId) {
        List<TypeConge> typeConges = new ArrayList<>();
        String query = "SELECT tc.* FROM typeconge tc " +
                "JOIN user_typeconge utc ON tc.ID_TypeConge = utc.ID_TypeConge " +
                "WHERE utc.ID_User = ?";

        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement ps = cnx.prepareStatement(query)) {
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                TypeConge typeConge = new TypeConge();
                //typeConge.setIdTypeConge(rs.getInt("ID_TypeConge"), totalSolde);
                typeConge.setDesignation(rs.getString("Type"));
                typeConge.setPas(rs.getDouble("Pas"));
                typeConge.setPeriodeJ(rs.getInt("PeriodeJ"));
                typeConge.setPeriodeM(rs.getInt("PeriodeM"));
                typeConge.setPeriodeA(rs.getInt("PeriodeA"));
                typeConge.setFile(rs.getBoolean("File"));
                typeConges.add(typeConge);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return typeConges;
    }

    public int getManagerIdByUserId2(int userId) {
        String query = "SELECT ID_Manager FROM user WHERE ID_User = ?";
        int managerId = 0;
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
            }
            PreparedStatement ps = cnx.prepareStatement(query);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                managerId = rs.getInt("ID_Manager");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return managerId;
    }

    public int getManagerIdByUserId(int userId) {
        int managerId = Integer.parseInt(null);
        String query = "SELECT ID_Manager FROM user WHERE ID_User = ?";

        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
            }
            PreparedStatement ps = cnx.prepareStatement(query);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                managerId = rs.getInt("ID_Manager");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return managerId;
    }


    public List<User> getUsersByDepartment(String departement) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT user.ID_User, user.Nom, user.Prenom, user.Email, user.Image, user.ID_Departement " +
                "FROM user " +
                "WHERE user.ID_Departement = ?";

        try {
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setString(1, departement);
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                User user = new User();
                user.setIdUser(rs.getInt("ID_User"));
                user.setNom(rs.getString("Nom"));
                user.setPrenom(rs.getString("Prenom"));
                user.setEmail(rs.getString("Email"));
                user.setImage(rs.getString("Image"));
                user.setIdDepartement(rs.getInt("ID_Departement"));

                // Retrieve the TypeConge associated with the user
                List<TypeConge> typeConges = getTypeCongesByUserId(user.getIdUser());
                for (TypeConge typeConge : typeConges) {
                    user.addTypeConge(typeConge);
                }

                users.add(user);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }

        return users;
    }

    @Override
    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        String query = "SELECT * FROM user";
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
            }
            PreparedStatement stm = cnx.prepareStatement(query);
            ResultSet rs = stm.executeQuery();
            while (rs.next()) {
                int idUser = rs.getInt("ID_User");
                String nom = rs.getString("Nom");
                String prenom = rs.getString("Prenom");
                String email = rs.getString("Email");
                String mdp = rs.getString("MDP");
                String image = rs.getString("Image");
                LocalDate creationDate = null;
                Date date = rs.getDate("Creation_Date");
                if (date != null) {
                    creationDate = date.toLocalDate();
                }
                int idDepartement = rs.getInt("ID_Departement");
                int idManager = rs.getInt("ID_Manager");
                int idSolde = rs.getInt("idSolde");

                User user = new User(idUser, nom, prenom, email, mdp, image, creationDate, idDepartement, idManager, idSolde);
                userList.add(user);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return userList;
    }

    @Override
    public void updateUser(User user) {
        String query = "UPDATE user SET Nom=?, Prenom=?, Email=?, MDP=?, Image=?, ID_Departement=?, ID_Manager=? WHERE ID_User=?";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setString(1, user.getNom());
            pst.setString(2, user.getPrenom());
            pst.setString(3, user.getEmail());
            pst.setString(4, user.getMdp());
            pst.setString(5, user.getImage());
            pst.setInt(6, user.getIdDepartement());
            pst.setInt(7, user.getIdManager());
            pst.setInt(8, user.getIdUser());
            pst.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public List<User> getAllUsersInfo() {
        return getAllUsers();
    }

    @Override
    public void addUser(String nom, String prenom, String email, String mdp, String image, int idDepartement, int idRole) {
        String query = "INSERT INTO user (Nom, Prenom, Email, MDP, Image, ID_Departement, ID_Manager, Creation_Date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setString(1, nom);
            pst.setString(2, prenom);
            pst.setString(3, email);
            pst.setString(4, mdp);
            pst.setString(5, image);
            pst.setInt(6, idDepartement);
            pst.setInt(7, idRole);
            pst.setDate(8, Date.valueOf(LocalDate.now()));
            pst.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void deleteUser(int idUser) {
        String query = "DELETE FROM user WHERE ID_User=?";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setInt(1, idUser);
            pst.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    public void assignRoleToUser(int userId, int roleId) {
        String sqlUserRole = "INSERT INTO user_role (ID_User, ID_Role) VALUES (?, ?)";
        String sqlTypeConge = "INSERT INTO user_typeconge (ID_User, ID_TypeConge) SELECT ?, ID_TypeConge FROM typeconge";

        try (Connection cnx = MyDataBase.getInstance().getCnx()) {
            // Start a transaction
            cnx.setAutoCommit(false);

            // Insert into user_role table
            try (PreparedStatement stmUserRole = cnx.prepareStatement(sqlUserRole)) {
                stmUserRole.setInt(1, userId);
                stmUserRole.setInt(2, roleId);
                stmUserRole.executeUpdate();
            }

            // Assign default TypeConge to user
            try (PreparedStatement stmTypeConge = cnx.prepareStatement(sqlTypeConge)) {
                stmTypeConge.setInt(1, userId);
                stmTypeConge.executeUpdate();
            }

            // Commit the transaction
            cnx.commit();
        } catch (SQLException e) {
            e.printStackTrace();
            try {
                if (cnx != null) {
                    cnx.rollback();
                }
            } catch (SQLException rollbackEx) {
                rollbackEx.printStackTrace();
            }
        }
    }

    @Override
    public void updateUserRoleAndDepartment(int userId, int roleId, int departmentId) throws SQLException {
        String updateDepartmentQuery = "UPDATE user SET ID_Departement = ? WHERE ID_User = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(updateDepartmentQuery)) {
            stmt.setInt(1, departmentId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }

        // Check if the user already has a role
        String checkRoleQuery = "SELECT COUNT(*) FROM user_role WHERE ID_User = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(checkRoleQuery)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                // Update the user's role
                String updateRoleQuery = "UPDATE user_role SET ID_Role = ? WHERE ID_User = ?";
                try (PreparedStatement roleStmt = cnx.prepareStatement(updateRoleQuery)) {
                    roleStmt.setInt(1, roleId);
                    roleStmt.setInt(2, userId);
                    roleStmt.executeUpdate();
                }
            } else {
                // Insert a new role for the user
                String insertRoleQuery = "INSERT INTO user_role (ID_User, ID_Role) VALUES (?, ?)";
                try (PreparedStatement roleStmt = cnx.prepareStatement(insertRoleQuery)) {
                    roleStmt.setInt(1, userId);
                    roleStmt.setInt(2, roleId);
                    roleStmt.executeUpdate();
                }
            }
        }
    }

    @Override
    public void updateUserRole(int userId, int roleId) {
        String query = "UPDATE user_role SET ID_Role=? WHERE ID_User=?";
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
            }
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setInt(1, roleId);
            pst.setInt(2, userId);
            pst.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void updateUserDepartment(int userId, int departmentId) {
        String query = "UPDATE user SET ID_Departement=? WHERE ID_User=?";
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
            }
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setInt(1, departmentId);
            pst.setInt(2, userId);
            pst.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public User getUserById(int userId) {
        String query = "SELECT * FROM user WHERE ID_User=?";
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
            }
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setInt(1, userId);
            ResultSet rs = pst.executeQuery();
            if (rs.next()) {
                return new User(
                        rs.getInt("ID_User"),
                        rs.getString("Nom"),
                        rs.getString("Prenom"),
                        rs.getString("Email"),
                        rs.getString("MDP"),
                        rs.getString("Image"),
                        rs.getInt("ID_Departement"),
                        rs.getInt("ID_Manager"),
                        rs.getDate("Creation_Date").toLocalDate()
                );
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    @Override
    public void Add(User user) {
        String query = "INSERT INTO user (Nom, Prenom, Email, MDP, Image, ID_Departement, ID_Manager, Creation_Date) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setString(1, user.getNom());
            pst.setString(2, user.getPrenom());
            pst.setString(3, user.getEmail());
            pst.setString(4, user.getMdp());
            pst.setString(5, user.getImage());
            pst.setInt(6, user.getIdDepartement());
            pst.setInt(7, user.getIdManager());
            pst.setDate(8, Date.valueOf(user.getCreationDate()));
            pst.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void Update(User user) {
        String query = "UPDATE user SET Nom=?, Prenom=?, Email=?, MDP=?, Image=?, ID_Departement=?, ID_Manager=? WHERE ID_User=?";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setString(1, user.getNom());
            pst.setString(2, user.getPrenom());
            pst.setString(3, user.getEmail());
            pst.setString(4, user.getMdp());
            pst.setString(5, user.getImage());
            pst.setInt(6, user.getIdDepartement());
            pst.setInt(7, user.getIdManager());
            pst.setInt(8, user.getIdUser());
            pst.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public List<User> Show() {
        return getAllUsers();
    }

    @Override
    public void Delete(User user) throws SQLException {
        String query = "DELETE FROM user WHERE ID_User=?";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setInt(1, user.getIdUser());
            pst.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void DeleteByID(int id) {
        String query = "DELETE FROM user WHERE ID_User=?";
        try {
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setInt(1, id);
            pst.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public List<User> SortDepart() {
        return null;
    }

    @Override
    public List<User> SortRole() {
        return null;
    }

    @Override
    public List<User> searchUsers(String query) {
        return null;
    }

    @Override
    public List<User> search(String query) {
        return null;
    }

    @Override
    public List<User> ShowUnder() {
        return null;
    }

    public int getLastInsertedUserId() throws SQLException {
        String query = "SELECT LAST_INSERT_ID()";
        try (Connection cnx = MyDataBase.getInstance().getCnx();
             Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            if (rs.next()) {
                return rs.getInt(1);
            }
        }
        throw new SQLException("Failed to retrieve last inserted user ID");
    }

    public void checkRoleDepartmentUniqueness(int idUser, int idRole, int idDepartement) {

    }

    public void setUserManager(int idUser) {
    }

    public void setManagerForUser(int userId, int managerId) {
        String query = "UPDATE user SET ID_Manager = ? WHERE ID_User = ?";
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx(); // Re-establish the connection if necessary
            }
            PreparedStatement pstmt = cnx.prepareStatement(query);
            pstmt.setInt(1, managerId);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}
