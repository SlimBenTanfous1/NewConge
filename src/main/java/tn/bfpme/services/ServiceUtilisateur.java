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
    public List<User> show() throws SQLException {
        List<User> users = new ArrayList<>();
        String qry = "SELECT * FROM user";

        try (PreparedStatement pstm = cnx.prepareStatement(qry);
             ResultSet rs = pstm.executeQuery()) {
            while (rs.next()) {
                User user = new User();
                user.setIdUser(rs.getInt("ID"));
                user.setNom(rs.getString("Nom"));
                user.setPrenom(rs.getString("Prenom"));
                user.setEmail(rs.getString("Email"));
                user.setRoleNom(rs.getString("Role"));
                user.setDepartementNom(rs.getString("Departement"));
                user.setManagerName(rs.getString("Manager")); // Fetch and set the contrat_id
                // user.add(user);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return users;
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

    public UserConge AfficherEnAttente() {
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
                "conge.ID_Conge, conge.TypeConge, conge.Statut, conge.DateFin, conge.DateDebut, conge.description, conge.file, typeconge.Designation, typeconge.Pas, typeconge.Periode, typeconge.File " +
                "FROM user " +
                "JOIN conge ON user.ID_User = conge.ID_User " +
                "JOIN typeconge ON conge.TypeConge = typeconge.ID_TypeConge " +
                "WHERE user.ID_User IN (SELECT ID_User FROM Subordinates WHERE ID_User != ?) AND conge.Statut = ?";

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
                // Add idSolde field handling if necessary
                if (!users.contains(user)) {
                    users.add(user);
                }

                Conge conge = new Conge();
                conge.setIdConge(rs.getInt("ID_Conge"));
                conge.setDateDebut(rs.getDate("DateDebut").toLocalDate());
                conge.setDateFin(rs.getDate("DateFin").toLocalDate());
                conge.setStatut(Statut.valueOf(rs.getString("Statut")));
                conge.setDescription(rs.getString("description"));
                conge.setFile(rs.getString("file"));
                conge.setIdUser(rs.getInt("ID_User"));
                conge.setDesignation(rs.getString("Designation"));
                TypeConge typeConge = new TypeConge();
                typeConge.setIdTypeConge(rs.getInt("TypeConge"));
                typeConge.setDesignation(rs.getString("Designation"));
                typeConge.setPas(rs.getDouble("Pas"));
                typeConge.setPeriode(rs.getString("Periode"));
                typeConge.setFile(rs.getBoolean("File"));
                conge.setTypeConge2(typeConge); // Set the TypeConge object
                conges.add(conge);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return new UserConge(users, conges);
    }

    @Override
    public UserConge TriType() {
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
                "WHERE user.ID_User IN (SELECT ID_User FROM Subordinates WHERE ID_User != ?) AND conge.Statut = ? ORDER BY conge.TypeConge";
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
    public UserConge TriNom() {
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
                "WHERE user.ID_User IN (SELECT ID_User FROM Subordinates WHERE ID_User != ?) AND conge.Statut = ? ORDER BY user.Nom ";
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
                "WHERE user.ID_User IN (SELECT ID_User FROM Subordinates WHERE ID_User != ?) AND conge.Statut = ? ORDER BY conge.DateFin";
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
    public UserConge AfficherApprove() {
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
                "conge.ID_Conge, conge.TypeConge, conge.Statut, conge.DateFin, conge.DateDebut, conge.description, conge.file, typeconge.Designation, typeconge.Pas, typeconge.Periode, typeconge.File " +
                "FROM user " +
                "JOIN conge ON user.ID_User = conge.ID_User " +
                "JOIN typeconge ON conge.TypeConge = typeconge.ID_TypeConge " +
                "WHERE user.ID_User IN (SELECT ID_User FROM Subordinates WHERE ID_User != ?) AND conge.Statut = ?";

        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
            }
            PreparedStatement ps = cnx.prepareStatement(query);
            int currentUserId = SessionManager.getInstance().getUser().getIdUser();
            ps.setInt(1, currentUserId);
            ps.setInt(2, currentUserId);
            ps.setString(3, String.valueOf(Statut.Approuvé));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                User user = new User();
                user.setIdUser(rs.getInt("ID_User"));
                user.setNom(rs.getString("Nom"));
                user.setPrenom(rs.getString("Prenom"));
                user.setEmail(rs.getString("Email"));
                user.setImage(rs.getString("Image"));
                user.setIdDepartement(rs.getInt("ID_Departement"));
                // Add idSolde field handling if necessary
                if (!users.contains(user)) {
                    users.add(user);
                }

                Conge conge = new Conge();
                conge.setIdConge(rs.getInt("ID_Conge"));
                conge.setDateDebut(rs.getDate("DateDebut").toLocalDate());
                conge.setDateFin(rs.getDate("DateFin").toLocalDate());
                conge.setStatut(Statut.valueOf(rs.getString("Statut")));
                conge.setDescription(rs.getString("description"));
                conge.setFile(rs.getString("file"));
                conge.setIdUser(rs.getInt("ID_User"));
                conge.setDesignation(rs.getString("Designation"));
                TypeConge typeConge = new TypeConge();
                typeConge.setIdTypeConge(rs.getInt("TypeConge"));
                typeConge.setDesignation(rs.getString("Designation"));
                typeConge.setPas(rs.getDouble("Pas"));
                typeConge.setPeriode(rs.getString("Periode"));
                typeConge.setFile(rs.getBoolean("File"));
                conge.setTypeConge2(typeConge); // Set the TypeConge object
                conges.add(conge);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return new UserConge(users, conges);
    }

    @Override
    public UserConge AfficherReject() {
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
                "conge.ID_Conge, conge.TypeConge, conge.Statut, conge.DateFin, conge.DateDebut, conge.description, conge.file, typeconge.Designation, typeconge.Pas, typeconge.Periode, typeconge.File " +
                "FROM user " +
                "JOIN conge ON user.ID_User = conge.ID_User " +
                "JOIN typeconge ON conge.TypeConge = typeconge.ID_TypeConge " +
                "WHERE user.ID_User IN (SELECT ID_User FROM Subordinates WHERE ID_User != ?) AND conge.Statut = ?";
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
            }
            PreparedStatement ps = cnx.prepareStatement(query);
            int currentUserId = SessionManager.getInstance().getUser().getIdUser();
            ps.setInt(1, currentUserId);
            ps.setInt(2, currentUserId);
            ps.setString(3, String.valueOf(Statut.Rejeté));
            ResultSet rs = ps.executeQuery();

            while (rs.next()) {
                User user = new User();
                user.setIdUser(rs.getInt("ID_User"));
                user.setNom(rs.getString("Nom"));
                user.setPrenom(rs.getString("Prenom"));
                user.setEmail(rs.getString("Email"));
                user.setImage(rs.getString("Image"));
                user.setIdDepartement(rs.getInt("ID_Departement"));
                // Add idSolde field handling if necessary
                if (!users.contains(user)) {
                    users.add(user);
                }

                Conge conge = new Conge();
                conge.setIdConge(rs.getInt("ID_Conge"));
                conge.setDateDebut(rs.getDate("DateDebut").toLocalDate());
                conge.setDateFin(rs.getDate("DateFin").toLocalDate());
                conge.setStatut(Statut.valueOf(rs.getString("Statut")));
                conge.setDescription(rs.getString("description"));
                conge.setFile(rs.getString("file"));
                conge.setIdUser(rs.getInt("ID_User"));
                conge.setDesignation(rs.getString("Designation"));
                TypeConge typeConge = new TypeConge();
                typeConge.setIdTypeConge(rs.getInt("TypeConge"));
                typeConge.setDesignation(rs.getString("Designation"));
                typeConge.setPas(rs.getDouble("Pas"));
                typeConge.setPeriode(rs.getString("Periode"));
                typeConge.setFile(rs.getBoolean("File"));
                conge.setTypeConge2(typeConge); // Set the TypeConge object
                conges.add(conge);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return new UserConge(users, conges);
    }

    @Override
    public List<User> RechrecheRH(String recherche) {
        List<User> users = new ArrayList<>();
        String sql = "SELECT u.ID_User, u.Nom, u.Prenom, u.Email, u.Image, u.ID_Departement, tc.ID_TypeConge, tc.Designation, tc.Pas, tc.Periode, tc.File " +
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
                typeConge.setPeriode(rs.getString("Periode"));
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
                typeConge.setPeriode(rs.getString("Periode"));
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
        int managerId = 0;
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
    public void Add(User user) {
        String query = "INSERT INTO user (`Nom`, `Prenom`, `Email`, `MDP`, `Image`, `ID_Departement`, `ID_Manager`, `Creation_Date`) VALUES (?, ?, ?, ?, ?, ?, ?, ?)";
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
            }
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

    public void AddUser_RH(User user) {
        String query = "INSERT INTO user (`Nom`, `Prenom`, `Email`, `MDP`, `Image`,`Creation_Date`) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
            }
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setString(1, user.getNom());
            pst.setString(2, user.getPrenom());
            pst.setString(3, user.getEmail());
            pst.setString(4, user.getMdp());
            pst.setString(5, user.getImage());
            pst.setDate(6, Date.valueOf(user.getCreationDate()));
            pst.executeUpdate();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    @Override
    public void Update(User user) {
        String query = "UPDATE user SET Nom=?, Prenom=?, Email=?, MDP=?, Image=? WHERE ID_User=?";
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
            }
            PreparedStatement pst = cnx.prepareStatement(query);
            pst.setString(1, user.getNom());
            pst.setString(2, user.getPrenom());
            pst.setString(3, user.getEmail());
            pst.setString(4, user.getMdp());
            pst.setString(5, user.getImage());
            pst.setInt(6, user.getIdUser());
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
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
            }
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
        List<User> users = new ArrayList<>();
        int currentUserId = SessionManager.getInstance().getUser().getIdUser(); // Assuming SessionManager manages the current user's session
        String sql = "WITH RECURSIVE Subordinates AS (" +
                "SELECT u.ID_User, u.Nom, u.Prenom, u.Email, u.MDP, u.Image, u.ID_Departement, u.ID_Manager, ur.ID_Role " +
                "FROM user u " +
                "LEFT JOIN user_role ur ON u.ID_User = ur.ID_User " +
                "WHERE u.ID_Manager = ? " +
                "UNION ALL " +
                "SELECT u.ID_User, u.Nom, u.Prenom, u.Email, u.MDP, u.Image, u.ID_Departement, u.ID_Manager, ur.ID_Role " +
                "FROM user u " +
                "INNER JOIN Subordinates s ON s.ID_User = u.ID_Manager " +
                "LEFT JOIN user_role ur ON u.ID_User = ur.ID_User" +
                ") " +
                "SELECT s.ID_User, s.Nom, s.Prenom, s.Email, s.MDP, s.Image, s.ID_Departement, s.ID_Manager, s.ID_Role, d.nom AS DepartementNom, r.nom AS RoleNom " +
                "FROM Subordinates s " +
                "LEFT JOIN departement d ON s.ID_Departement = d.ID_Departement " +
                "LEFT JOIN role r ON s.ID_Role = r.ID_Role " +
                "WHERE s.ID_User != ?";

        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
            }
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, currentUserId);
            ps.setInt(2, currentUserId); // Exclude current user
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                User user = new User();
                user.setIdUser(rs.getInt("ID_User"));
                user.setNom(rs.getString("Nom"));
                user.setPrenom(rs.getString("Prenom"));
                user.setEmail(rs.getString("Email"));
                user.setMdp(rs.getString("MDP"));
                user.setImage(rs.getString("Image"));
                user.setIdDepartement(rs.getInt("ID_Departement"));
                user.setIdManager(rs.getInt("ID_Manager"));
                user.setIdRole(rs.getInt("ID_Role"));
                user.setDepartementNom(rs.getString("DepartementNom"));
                user.setRoleNom(rs.getString("RoleNom"));
                users.add(user);
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return users;
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

    public void checkRoleDepartmentUniqueness(int userId, int roleId, int departmentId) throws SQLException {
        if (cnx == null || cnx.isClosed()) {
            cnx = MyDataBase.getInstance().getCnx();
        }

        String query = "SELECT COUNT(*) FROM user " +
                "JOIN user_role ON user.ID_User = user_role.ID_User " +
                "WHERE user_role.ID_Role = ? AND user.ID_Departement = ? AND user.ID_User != ?";

        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setInt(1, roleId);
            statement.setInt(2, departmentId);
            statement.setInt(3, userId);
            ResultSet rs = statement.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                throw new SQLException("Un autre utilisateur a déjà le même rôle et département.");
            }
        }
    }


    public void setManagerForUser(int userId, int managerId) {
        String query = "UPDATE user SET ID_Manager = ? WHERE ID_User = ?";
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
            }
            PreparedStatement pstmt = cnx.prepareStatement(query);
            pstmt.setInt(1, managerId);
            pstmt.setInt(2, userId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<User> RechercheUnder(String input) {
        List<User> users = new ArrayList<>();
        int currentUserId = SessionManager.getInstance().getUser().getIdUser(); // Assuming SessionManager manages the current user's session
        String sql = "WITH RECURSIVE Subordinates AS (" +
                "SELECT u.ID_User, u.Nom, u.Prenom, u.Email, u.MDP, u.Image, u.ID_Departement, u.ID_Manager, ur.ID_Role " +
                "FROM user u " +
                "LEFT JOIN user_role ur ON u.ID_User = ur.ID_User " +
                "WHERE u.ID_Manager = ? " +
                "UNION ALL " +
                "SELECT u.ID_User, u.Nom, u.Prenom, u.Email, u.MDP, u.Image, u.ID_Departement, u.ID_Manager, ur.ID_Role " +
                "FROM user u " +
                "INNER JOIN Subordinates s ON s.ID_User = u.ID_Manager " +
                "LEFT JOIN user_role ur ON u.ID_User = ur.ID_User" +
                ") " +
                "SELECT s.ID_User, s.Nom, s.Prenom, s.Email, s.MDP, s.Image, s.ID_Departement, s.ID_Manager, s.ID_Role, d.nom AS DepartementNom, r.nom AS RoleNom " +
                "FROM Subordinates s " +
                "LEFT JOIN departement d ON s.ID_Departement = d.ID_Departement " +
                "LEFT JOIN role r ON s.ID_Role = r.ID_Role " +
                "WHERE s.ID_User != ? " +
                "AND (s.Nom LIKE ? OR s.Prenom LIKE ? OR s.Email LIKE ?)";

        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
            }
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, currentUserId);
            ps.setInt(2, currentUserId); // Exclude current user
            String searchInput = "%" + input + "%";
            ps.setString(3, searchInput);
            ps.setString(4, searchInput);
            ps.setString(5, searchInput);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                User user = new User();
                user.setIdUser(rs.getInt("ID_User"));
                user.setNom(rs.getString("Nom"));
                user.setPrenom(rs.getString("Prenom"));
                user.setEmail(rs.getString("Email"));
                user.setMdp(rs.getString("MDP"));
                user.setImage(rs.getString("Image"));
                user.setIdDepartement(rs.getInt("ID_Departement"));
                user.setIdManager(rs.getInt("ID_Manager"));
                user.setIdRole(rs.getInt("ID_Role"));
                user.setDepartementNom(rs.getString("DepartementNom"));
                user.setRoleNom(rs.getString("RoleNom"));
                users.add(user);
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return users;
    }

    public String getRoleNameByUserID(int userId) {
        String query = "SELECT r.nom AS RoleName " +
                "FROM user u " +
                "JOIN user_role ur ON u.ID_User = ur.ID_User " +
                "JOIN role r ON ur.ID_Role = r.ID_Role " +
                "WHERE u.ID_User = ?";
        String RoleName = "";
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
            }
            PreparedStatement ps = cnx.prepareStatement(query);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                RoleName = rs.getString("RoleName");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return RoleName;
    }

    public String getDepNameByUserID(int userId) {
        String query = "SELECT d.nom AS DepartmentName " +
                "FROM user u " +
                "JOIN departement d ON u.ID_Departement = d.ID_Departement " +
                "WHERE u.ID_User = ?";
        String DepName = "";
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
            }
            PreparedStatement ps = cnx.prepareStatement(query);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();

            if (rs.next()) {
                DepName = rs.getString("DepartmentName");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return DepName;
    }

    private int getEmployeeRoleId() throws SQLException {
        String query = "SELECT ID_Role FROM role WHERE nom = 'Employee' LIMIT 1";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("ID_Role");
            }
        }
        return -1; // Or handle appropriately if 'Employee' role doesn't exist
    }




    private Integer findManagerByRoleAndDepartment(int roleId, int departmentId) throws SQLException {
        // Define the query to find the suitable manager based on role and department hierarchy
        String query = "SELECT u.ID_User " +
                "FROM user u " +
                "JOIN user_role ur ON u.ID_User = ur.ID_User " +
                "JOIN departement d ON u.ID_Departement = d.ID_Departement " +
                "WHERE ur.ID_Role = (SELECT rh.ID_RoleP FROM rolehierarchie rh WHERE rh.ID_RoleC = ? LIMIT 1) " +
                "AND d.ID_Departement = (SELECT d.Parent_Dept FROM departement d WHERE d.ID_Departement = ? LIMIT 1) " +
                "LIMIT 1";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, roleId);
            stmt.setInt(2, departmentId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("ID_User");
            }
        }
        return null; // Return null if no suitable manager is found
    }

    private List<User> getUsersInDepartment(int departmentId) throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT u.*, ur.ID_Role FROM user u JOIN user_role ur ON u.ID_User = ur.ID_User WHERE u.ID_Departement = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, departmentId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                users.add(new User(
                        rs.getInt("ID_User"),
                        rs.getString("Nom"),
                        rs.getString("Prenom"),
                        rs.getString("Email"),
                        rs.getString("MDP"),
                        rs.getString("Image"),
                        rs.getDate("Creation_Date") != null ? rs.getDate("Creation_Date").toLocalDate() : null,
                        rs.getInt("ID_Departement"),
                        rs.getInt("ID_Manager"),
                        rs.getInt("ID_Role")));
            }
        }
        return users;
    }

    private List<Integer> getSubDepartmentIds(int departmentId) throws SQLException {
        List<Integer> subDepartmentIds = new ArrayList<>();
        String query = "SELECT ID_Departement FROM departement WHERE Parent_Dept = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, departmentId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                subDepartmentIds.add(rs.getInt("ID_Departement"));
                // Recursively get sub-departments
                subDepartmentIds.addAll(getSubDepartmentIds(rs.getInt("ID_Departement")));
            }
        }
        return subDepartmentIds;
    }

    private int getRoleIdByName(String roleName) throws SQLException {
        String query = "SELECT ID_Role FROM role WHERE nom = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setString(1, roleName);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("ID_Role");
            }
        }
        return -1; // Return -1 if the role is not found
    }

    public List<User> getAllUsers() {
        List<User> userList = new ArrayList<>();
        String sql = "SELECT * FROM user";
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
            }
            PreparedStatement ps = cnx.prepareStatement(sql);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                User user = new User(
                        rs.getInt("ID_User"),
                        rs.getString("Nom"),
                        rs.getString("Prenom"),
                        rs.getString("Email"),
                        rs.getString("MDP"),
                        rs.getString("Image"),
                        rs.getDate("Creation_Date") != null ? rs.getDate("Creation_Date").toLocalDate() : null,
                        rs.getInt("ID_Departement"),
                        rs.getInt("ID_Manager"),
                        rs.getInt("idSolde")
                );
                userList.add(user);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return userList;
    }

    public Departement getDepartmentByUserId(int userId) {
        String sql = "SELECT d.* FROM departement d JOIN user u ON d.ID_Departement = u.ID_Departement WHERE u.ID_User = ?";
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
            }
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Departement(rs.getInt("ID_Departement"), rs.getString("nom"), rs.getString("description"), rs.getInt("Parent_Dept"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    public Role getRoleByUserId(int userId) {
        String sql = "SELECT r.* FROM role r JOIN user_role ur ON r.ID_Role = ur.ID_Role WHERE ur.ID_User = ?";
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
            }
            PreparedStatement ps = cnx.prepareStatement(sql);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            if (rs.next()) {
                return new Role(rs.getInt("ID_Role"), rs.getString("nom"), rs.getString("description"));
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return null;
    }

    //------------------------------- Hierarchie ---------------------------//*
    private void ensureConnection() throws SQLException {
        if (cnx == null || cnx.isClosed()) {
            cnx = MyDataBase.getInstance().getCnx();
        }
    }

    public void updateSubordinatesManager(int managerId) throws SQLException {
        String query = "UPDATE user SET ID_Manager = NULL WHERE ID_Manager = ?";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setInt(1, managerId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating subordinates' manager: " + e.getMessage(), e);
        }
    }

    public User getUserById(int userId) throws SQLException {
        String query = "SELECT * FROM user WHERE ID_User = ?";
        try {
            ensureConnection();
            PreparedStatement statement = cnx.prepareStatement(query);
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return extractUserFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving user by ID: " + e.getMessage(), e);
        }
        return null;
    }

    public List<User> getUsersByDepartementId(int departementId) throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM user WHERE ID_Departement = ?";
        try {
            ensureConnection();
            PreparedStatement statement = cnx.prepareStatement(query);
            statement.setInt(1, departementId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                users.add(extractUserFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving users by department ID: " + e.getMessage(), e);
        }
        return users;
    }

    public User getUserByRole(String roleName) throws SQLException {
        String query = "SELECT u.* FROM user u JOIN user_role ur ON u.ID_User = ur.ID_User JOIN role r ON ur.ID_Role = r.ID_Role WHERE r.nom = ?";
        try {
            ensureConnection();
            PreparedStatement statement = cnx.prepareStatement(query);
            statement.setString(1, roleName);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                return extractUserFromResultSet(resultSet);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving user by role name: " + e.getMessage(), e);
        }
        return null;
    }

    public void updateUserManager(int userId, int managerId) throws SQLException {
        ensureConnection();
        String query = "UPDATE user SET ID_Manager = ? WHERE ID_User = ?";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setInt(1, managerId);
            statement.setInt(2, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user manager: " + e.getMessage(), e);
        }
    }

    public List<User> getSubordinates(int managerId) throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM user WHERE ID_Manager = ?";
        try {
            ensureConnection();
            PreparedStatement statement = cnx.prepareStatement(query);
            statement.setInt(1, managerId);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                users.add(extractUserFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving subordinates by manager ID: " + e.getMessage(), e);
        }
        return users;
    }

    public List<User> getUsersWithoutManager() throws SQLException {
        List<User> users = new ArrayList<>();
        String query = "SELECT * FROM user WHERE ID_Manager IS NULL";
        try {
            ensureConnection();
            PreparedStatement statement = cnx.prepareStatement(query);
            ResultSet resultSet = statement.executeQuery();
            while (resultSet.next()) {
                users.add(extractUserFromResultSet(resultSet));
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving users without a manager: " + e.getMessage(), e);
        }
        return users;
    }

    public void updateUserDepartment(int userId, Integer departmentId) throws SQLException {
        ensureConnection();
        String query = "UPDATE user SET ID_Departement = ? WHERE ID_User = ?";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            if (departmentId == null) {
                statement.setNull(1, java.sql.Types.INTEGER);
            } else {
                statement.setInt(1, departmentId);
            }
            statement.setInt(2, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error updating user department: " + e.getMessage(), e);
        }
    }

    public void addUserRole(int userId, int roleId) throws SQLException {
        ensureConnection();
        String query = "INSERT INTO user_role (ID_User, ID_Role) VALUES (?, ?)";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setInt(1, userId);
            statement.setInt(2, roleId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error adding user role: " + e.getMessage(), e);
        }
    }

    public void removeUserRole(int userId) throws SQLException {
        ensureConnection();
        String query = "DELETE FROM user_role WHERE ID_User = ?";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setInt(1, userId);
            statement.executeUpdate();
        } catch (SQLException e) {
            throw new RuntimeException("Error removing user role: " + e.getMessage(), e);
        }
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
        return user;
    }
}
