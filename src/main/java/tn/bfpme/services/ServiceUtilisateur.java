package tn.bfpme.services;

import tn.bfpme.interfaces.IUtilisateur;

import tn.bfpme.models.*;

import tn.bfpme.utils.MyDataBase;
import tn.bfpme.utils.SessionManager;

import java.sql.*;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
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
    public List<User> search(String query) {
        return null;
    }

    @Override
    public List<User> ShowUnder() {
        List<User> users = new ArrayList<>();
        int currentUserId = SessionManager.getInstance().getUser().getIdUser();
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
            ps.setInt(2, currentUserId);
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
        System.out.println(users);
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


    // Method to get all subordinate departments
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
    public void removeUserRoleAndDepartment(int userId) throws SQLException {
        if (cnx == null || cnx.isClosed()) {
            cnx = MyDataBase.getInstance().getCnx();
        }

        User currentUser = getUserById(userId);
        if (currentUser == null) {
            throw new SQLException("User not found.");
        }

        // Update subordinates before removing the user from the hierarchy
        updateSubordinatesForUser(userId);

        // Remove the user's role and department
        String removeRoleQuery = "DELETE FROM user_role WHERE ID_User = ?";
        String removeDepartmentQuery = "UPDATE user SET ID_Departement = NULL, ID_Manager = NULL WHERE ID_User = ?";

        try (PreparedStatement stmtRole = cnx.prepareStatement(removeRoleQuery);
             PreparedStatement stmtDept = cnx.prepareStatement(removeDepartmentQuery)) {
            stmtRole.setInt(1, userId);
            stmtRole.executeUpdate();

            stmtDept.setInt(1, userId);
            stmtDept.executeUpdate();
        }
    }

    public void updateUserRoleAndDepartment(int userId, int roleId, int departmentId) throws SQLException {
        if (cnx == null || cnx.isClosed()) {
            cnx = MyDataBase.getInstance().getCnx();
        }

        System.out.println("Updating user with UserID: " + userId + ", RoleID: " + roleId + ", DepartmentID: " + departmentId);

        if (!isRoleDepartmentValid(roleId, departmentId)) {
            throw new SQLException("Invalid role-department relationship for RoleID: " + roleId + " and DepartmentID: " + departmentId);
        }

        String updateDepartmentQuery = "UPDATE user SET ID_Departement = ? WHERE ID_User = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(updateDepartmentQuery)) {
            stmt.setInt(1, departmentId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            System.out.println("Updated department for user ID: " + userId);
        }

        String checkRoleQuery = "SELECT COUNT(*) FROM user_role WHERE ID_User = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(checkRoleQuery)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                String updateRoleQuery = "UPDATE user_role SET ID_Role = ? WHERE ID_User = ?";
                try (PreparedStatement roleStmt = cnx.prepareStatement(updateRoleQuery)) {
                    roleStmt.setInt(1, roleId);
                    roleStmt.setInt(2, userId);
                    roleStmt.executeUpdate();
                    System.out.println("Updated role for user ID: " + userId);
                }
            } else {
                String insertRoleQuery = "INSERT INTO user_role (ID_User, ID_Role) VALUES (?, ?)";
                try (PreparedStatement roleStmt = cnx.prepareStatement(insertRoleQuery)) {
                    roleStmt.setInt(1, userId);
                    roleStmt.setInt(2, roleId);
                    roleStmt.executeUpdate();
                    System.out.println("Inserted role for user ID: " + userId);
                }
            }
        }

        // Set the manager for the user
        setManagerForUser(userId, departmentId, roleId);

        // Update subordinates for the user
        reassignOrphanedSubordinates(departmentId, roleId, userId);
    }

    public User getUserById(int userId) throws SQLException {
        String query = "SELECT * FROM user WHERE ID_User = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return new User(
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
            }
        }
        return null;
    }

    public boolean isRoleDepartmentValid(int roleId, int departmentId) throws SQLException {
        // Assuming the role ID for "Employe" is 5
        final int EMPLOYE_ROLE_ID = 5;

        if (roleId == EMPLOYE_ROLE_ID) {
            // Allow "Employe" role to have any department
            return true;
        }

        String query = "SELECT COUNT(*) FROM role_departement WHERE ID_Role = ? AND ID_Departement = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, roleId);
            stmt.setInt(2, departmentId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            }
        }
        return false;
    }


    private void setManagerForUser(int userId, int departmentId, int roleId) throws SQLException {
        User user = getUserById(userId);
        user.setIdDepartement(departmentId);
        user.setIdRole(roleId);

        System.out.println("Setting manager for user: " + user);

        List<Integer> parentRoleIds = getParentRoleIds(roleId);
        Integer managerId = findManagerInDepartment(userId, departmentId, parentRoleIds);
        if (managerId == null) {
            managerId = findManagerInDepartmentHierarchy(userId, departmentId, parentRoleIds);
        }
        if (managerId == null) {
            managerId = getPDGId();
        }

        String updateManagerQuery = "UPDATE user SET ID_Manager = ? WHERE ID_User = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(updateManagerQuery)) {
            if (managerId != null) {
                stmt.setInt(1, managerId);
            } else {
                stmt.setNull(1, java.sql.Types.INTEGER);
            }
            stmt.setInt(2, userId);
            stmt.executeUpdate();
            System.out.println("Manager set for user " + userId + ": " + managerId);
        }
    }

    private void reassignSubordinates(int departmentId, int roleId) throws SQLException {
        String query = "SELECT ID_User FROM user WHERE ID_Departement = ? AND ID_Manager IS NULL";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, departmentId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int userId = rs.getInt("ID_User");
                System.out.println("Reassigning subordinate: " + userId);
                setManagerForUser(userId, departmentId, roleId);
            }
        }
    }

    private void updateSubordinatesForUser(int userId) throws SQLException {
        List<User> subordinates = getSubordinates(userId);
        for (User subordinate : subordinates) {
            String updateManagerQuery = "UPDATE user SET ID_Manager = NULL WHERE ID_User = ?";
            try (PreparedStatement stmt = cnx.prepareStatement(updateManagerQuery)) {
                stmt.setInt(1, subordinate.getIdUser());
                stmt.executeUpdate();
                System.out.println("Set manager to null for subordinate: " + subordinate.getIdUser());
            }
        }
    }


    private List<User> getSubordinates(int userId) throws SQLException {
        List<User> subordinates = new ArrayList<>();
        String query = "SELECT u.ID_User, u.ID_Departement, ur.ID_Role FROM user u " +
                "JOIN user_role ur ON u.ID_User = ur.ID_User " +
                "WHERE u.ID_Manager = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                User user = new User(
                        rs.getInt("ID_User"),
                        rs.getInt("ID_Departement"),
                        rs.getInt("ID_Role")
                );
                subordinates.add(user);
            }
        }
        return subordinates;
    }

    public User findManagerForUser(User user) throws SQLException {
        // Get all parent roles for the current user's role
        List<Integer> parentRoleIds = getParentRoleIds(user.getIdRole());
        System.out.println("Parent roles for user " + user.getIdUser() + ": " + parentRoleIds);

        // Try to find a manager in the same department first
        System.out.println("Looking for manager in the same department for user " + user.getIdUser() + "...");
        Integer managerId = findManagerInDepartment(user.getIdUser(), user.getIdDepartement(), parentRoleIds);
        if (managerId != null) {
            System.out.println("Manager found in the same department: " + managerId);
            return getUserById(managerId);
        }

        // If no manager is found in the same department, search in parent departments
        System.out.println("Looking for manager in parent departments for user " + user.getIdUser() + "...");
        managerId = findManagerInDepartmentHierarchy(user.getIdUser(), user.getIdDepartement(), parentRoleIds);
        if (managerId != null) {
            System.out.println("Manager found in parent department: " + managerId);
            return getUserById(managerId);
        }

        // If no manager is found, return the PDG as the last resort
        System.out.println("Looking for PDG for user " + user.getIdUser() + "...");
        managerId = getPDGId();
        if (managerId != null) {
            System.out.println("PDG found: " + managerId);
            return getUserById(managerId);
        }

        System.out.println("No manager found for user " + user.getIdUser());
        return null;
    }


    private Integer findManagerInDepartment(int userId, int departmentId, List<Integer> parentRoleIds) throws SQLException {
        for (int parentRoleId : parentRoleIds) {
            String query = "SELECT u.ID_User FROM user u JOIN user_role ur ON u.ID_User = ur.ID_User WHERE u.ID_Departement = ? AND ur.ID_Role = ? AND u.ID_User != ? LIMIT 1";
            try (PreparedStatement stmt = cnx.prepareStatement(query)) {
                stmt.setInt(1, departmentId);
                stmt.setInt(2, parentRoleId);
                stmt.setInt(3, userId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    return rs.getInt("ID_User");
                }
            }
        }
        return null;
    }


    private Integer findManagerInDepartmentHierarchy(int userId, int deptId, List<Integer> parentRoleIds) throws SQLException {
        Set<Integer> visitedDepts = new HashSet<>();
        while (deptId != 0 && !visitedDepts.contains(deptId)) {
            visitedDepts.add(deptId);
            Integer managerId = findManagerInDepartment(userId, deptId, parentRoleIds);
            if (managerId != null) {
                return managerId;
            }

            String parentDeptQuery = "SELECT Parent_Dept FROM departement WHERE ID_Departement = ?";
            try (PreparedStatement stmt = cnx.prepareStatement(parentDeptQuery)) {
                stmt.setInt(1, deptId);
                ResultSet rs = stmt.executeQuery();
                if (rs.next()) {
                    deptId = rs.getInt("Parent_Dept");
                } else {
                    break;
                }
            }
        }
        return null;
    }


    private Integer getPDGId() throws SQLException {
        String query = "SELECT u.ID_User FROM user u JOIN user_role ur ON u.ID_User = ur.ID_User WHERE ur.ID_Role = (SELECT r.ID_Role FROM role r WHERE r.nom = 'PDG') LIMIT 1";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("ID_User");
            }
        }
        return null;
    }

    private List<Integer> getParentRoleIds(int roleId) throws SQLException {
        String query = "SELECT ID_RoleP FROM rolehierarchie WHERE ID_RoleC = ?";
        List<Integer> parentRoleIds = new ArrayList<>();
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, roleId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                parentRoleIds.add(rs.getInt("ID_RoleP"));
            }
        }
        return parentRoleIds;
    }

    private void reassignOrphanedSubordinates(int departmentId, int roleId, int newManagerId) throws SQLException {
        List<User> orphanedUsers = getOrphanedUsersInDepartment(departmentId);

        for (User orphan : orphanedUsers) {
            // Only update if the orphaned user's role hierarchy is under the new manager's role
            if (isChildRole(orphan.getIdRole(), roleId)) {
                setManagerForUser(orphan.getIdUser(), departmentId, roleId, newManagerId);
            }
        }
    }

    private List<User> getOrphanedUsersInDepartment(int departmentId) throws SQLException {
        List<User> orphanedUsers = new ArrayList<>();
        String query = "SELECT u.ID_User, u.ID_Departement, ur.ID_Role FROM user u " +
                "JOIN user_role ur ON u.ID_User = ur.ID_User " +
                "WHERE u.ID_Departement = ? AND u.ID_Manager IS NULL";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, departmentId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                User user = new User(
                        rs.getInt("ID_User"),
                        rs.getInt("ID_Departement"),
                        rs.getInt("ID_Role")
                );
                orphanedUsers.add(user);
            }
        }
        return orphanedUsers;
    }

    private boolean isChildRole(int childRoleId, int parentRoleId) throws SQLException {
        String query = "SELECT COUNT(*) FROM rolehierarchie WHERE ID_RoleC = ? AND ID_RoleP = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(query)) {
            stmt.setInt(1, childRoleId);
            stmt.setInt(2, parentRoleId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next() && rs.getInt(1) > 0) {
                return true;
            }
        }
        return false;
    }

    private void setManagerForUser(int userId, int departmentId, int roleId, int managerId) throws SQLException {
        String updateManagerQuery = "UPDATE user SET ID_Manager = ? WHERE ID_User = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(updateManagerQuery)) {
            stmt.setInt(1, managerId);
            stmt.setInt(2, userId);
            stmt.executeUpdate();
        }
    }


}


