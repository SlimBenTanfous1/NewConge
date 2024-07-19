package tn.bfpme.services;

import tn.bfpme.interfaces.IConge;
import tn.bfpme.models.*;
import tn.bfpme.models.Statut;
import tn.bfpme.utils.MyDataBase;
import tn.bfpme.utils.SessionManager;
import java.sql.Connection;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceConge implements IConge<Conge> {
    private static Connection cnx;


    public ServiceConge() {
        cnx = MyDataBase.getInstance().getCnx();
    }

    public static List<Conge> getCongesByUserId(int idUser) {
        List<Conge> conges = new ArrayList<>();
        String sql = "SELECT * FROM conge WHERE ID_User = ?";
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
            }
            PreparedStatement stm = cnx.prepareStatement(sql);
            stm.setInt(1, idUser);
            ResultSet rs = stm.executeQuery();
            while (rs.next()) {
                Conge conge = new Conge();
                conge.setIdConge(rs.getInt("ID_Conge"));
                conge.setDateDebut(rs.getDate("DateDebut").toLocalDate());
                conge.setDateFin(rs.getDate("DateFin").toLocalDate());
                conge.setTypeConge(rs.getInt("TypeConge")); // Assuming TypeConge is int, you might need to adjust this
                conge.setStatut(Statut.valueOf(rs.getString("Statut"))); // Assuming Statut is an enum
                conge.setFile(rs.getString("file"));
                conge.setDescription(rs.getString("description"));
                conge.setIdUser(rs.getInt("ID_User"));
                conge.setMessage(rs.getString("Message"));
                conges.add(conge);
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
        return conges;
    }


    public String getCongeTypeName(int idType) {
        String query = "SELECT Designation FROM typeconge WHERE ID_TypeConge = ?";
        String typeName = null;
        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement pstmt = cnx.prepareStatement(query)) {

            pstmt.setInt(1, idType);
            try (ResultSet rs = pstmt.executeQuery()) {
                if (rs.next()) {
                    typeName = rs.getString("Designation");
                }
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return typeName;
    }
    public void AddConge(Conge conge) {
        String qry = "INSERT INTO `conge`(`DateDebut`, `DateFin`, `TypeConge`, `Statut`, `file`, `description`, `ID_User`) VALUES (?,?,?,?,?,?,?)";
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
            }
            PreparedStatement stm = cnx.prepareStatement(qry);
            stm.setDate(1, Date.valueOf(conge.getDateDebut()));
            stm.setDate(2, Date.valueOf(conge.getDateFin()));
            stm.setInt(3, conge.getIdTypeConge());
            stm.setString(4, String.valueOf(conge.getStatut()));
            stm.setString(5, conge.getFile());
            stm.setString(6, conge.getDescription());
            stm.setInt(7, conge.getIdUser());
            stm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }


    @Override
    public List<Conge> afficher() {
        List<Conge> conges = new ArrayList<>();
        String sql = "SELECT c.*, t.Designation AS TypeDesignation, t.ID_TypeConge AS TypeCongeID " +
                "FROM conge c " +
                "LEFT JOIN typeconge t ON c.TypeConge = t.ID_TypeConge " +
                "WHERE c.ID_User = ?";
        cnx = MyDataBase.getInstance().getCnx();
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
            }
            PreparedStatement ste = cnx.prepareStatement(sql);
            ste.setInt(1, SessionManager.getInstance().getUser().getIdUser());
            ResultSet rs = ste.executeQuery();
            while (rs.next()) {
                Conge conge = new Conge();
                conge.setIdConge(rs.getInt("ID_Conge"));
                conge.setDateDebut(rs.getDate("DateDebut").toLocalDate());
                conge.setDateFin(rs.getDate("DateFin").toLocalDate());
                conge.setDesignation(rs.getString("TypeDesignation"));
                TypeConge typeConge = new TypeConge(rs.getInt("TypeCongeID"), rs.getString("TypeDesignation"));
                conge.setTypeConge(typeConge.getIdTypeConge());
                conge.setStatut(Statut.valueOf(rs.getString("Statut")));
                conge.setIdUser(rs.getInt("ID_User"));
                conge.setFile(rs.getString("file"));
                conge.setDescription(rs.getString("description"));
                conge.setMessage(rs.getString("Message"));
                conges.add(conge);
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return conges;
    }

    @Override
    public void Add(Conge conge) {
        String qry = "INSERT INTO `conge`(`DateDebut`, `DateFin`, `TypeConge`, `Statut`, `ID_User`, `file`, `description`) VALUES (?,?,?,?,?,?,?)";
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
            }
            PreparedStatement stm = cnx.prepareStatement(qry);
            stm.setDate(1, Date.valueOf(conge.getDateDebut()));
            stm.setDate(2, Date.valueOf(conge.getDateFin()));
            stm.setString(3, String.valueOf(conge.getTypeConge()));
            stm.setString(4, String.valueOf(conge.getStatut()));
            stm.setInt(5, conge.getIdUser());
            stm.setString(6, conge.getFile());
            stm.setString(7, conge.getDescription());
            stm.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }
    private void updateBalance(int userId, int typeCongeId, double newBalance) {
        String query = "UPDATE user_Solde SET TotalSolde = ? WHERE ID_User = ? AND ID_TypeConge = ?";
        try (Connection conn = MyDataBase.getInstance().getCnx();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setDouble(1, newBalance);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, typeCongeId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    @Override
    public void updateConge(Conge conge) {
        cnx = MyDataBase.getInstance().getCnx();
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
            }
            String qry = "UPDATE `conge` SET `DateDebut`=?, `DateFin`=?, `TypeConge`=?, `Statut`=?, `ID_User`=?, `file`=?, `description`=? WHERE `ID_Conge`=?";
            PreparedStatement stm = cnx.prepareStatement(qry);
            stm.setDate(1, java.sql.Date.valueOf(conge.getDateDebut()));
            stm.setDate(2, java.sql.Date.valueOf(conge.getDateFin()));
            stm.setString(3, conge.getTypeConge().toString());
            stm.setString(4, conge.getStatut().toString());
            stm.setInt(5, conge.getIdUser());
            stm.setString(6, conge.getFile());
            stm.setString(7, conge.getDescription());
            stm.setInt(8, conge.getIdConge());
            stm.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public void updateStatutConge(int id, Statut statut) {
        try {
            String qry = "UPDATE `conge` SET `Statut`=? WHERE `ID_Conge`=?";
            cnx = MyDataBase.getInstance().getCnx();
            PreparedStatement stm = cnx.prepareStatement(qry);
            stm.setString(1, String.valueOf(statut));
            stm.setInt(2, id);
            stm.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }



    @Override
    public void deleteConge(Conge conge) {
        try {
            String qry = "DELETE FROM `conge` WHERE `ID_Conge`=?";
            PreparedStatement stm = cnx.prepareStatement(qry);
            stm.setInt(1, conge.getIdConge());
            stm.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public void deleteCongeByID(int id) {
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
            }
            String qry = "DELETE FROM `conge` WHERE `ID_Conge`=?";
            PreparedStatement stm = cnx.prepareStatement(qry);
            stm.setInt(1, id);
            stm.executeUpdate();
            System.out.println("Suppression Effectuée");
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    @Override
    public List<Conge> TriparStatut() {
        List<Conge> conges = new ArrayList<>();
        String sql = "SELECT `ID_Conge`, `DateDebut`, `DateFin`, `TypeConge`, `Statut`, `ID_User`, `file`, `description` FROM `conge` WHERE `ID_User` LIKE '%" + SessionManager.getInstance().getUser().getIdUser() + "%' ORDER BY `Statut`";
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
            }
            Statement ste = cnx.createStatement();
            ResultSet rs = ste.executeQuery(sql);
            while (rs.next()) {
                Conge conge = new Conge();
                conge.setIdConge(rs.getInt("ID_Conge"));
                conge.setDateDebut(rs.getDate("DateDebut").toLocalDate());
                conge.setDateFin(rs.getDate("DateFin").toLocalDate());
                conge.setTypeConge(TypeConge.valueOf(rs.getString("TypeConge")));
                conge.setStatut(Statut.valueOf(rs.getString("Statut")));
                conge.setIdUser(rs.getInt("ID_User"));
                conge.setFile(rs.getString("file"));
                conge.setDescription(rs.getString("description"));
                conges.add(conge);
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return conges;
    }

    @Override
    public List<Conge> TriparType() {
        List<Conge> conges = new ArrayList<>();
        String sql = "SELECT `ID_Conge`, `DateDebut`, `DateFin`, `TypeConge`, `Statut`, `ID_User`, `file`, `description` FROM `conge` WHERE `ID_User` LIKE '%" + SessionManager.getInstance().getUser().getIdUser() + "%' ORDER BY `TypeConge`";
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
            }
            Statement ste = cnx.createStatement();
            ResultSet rs = ste.executeQuery(sql);
            while (rs.next()) {
                Conge conge = new Conge();
                conge.setIdConge(rs.getInt("ID_Conge"));
                conge.setDateDebut(rs.getDate("DateDebut").toLocalDate());
                conge.setDateFin(rs.getDate("DateFin").toLocalDate());
                conge.setTypeConge(TypeConge.valueOf(rs.getString("TypeConge")));
                conge.setStatut(Statut.valueOf(rs.getString("Statut")));
                conge.setIdUser(rs.getInt("ID_User"));
                conge.setFile(rs.getString("file"));
                conge.setDescription(rs.getString("description"));
                conges.add(conge);
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return conges;
    }

    @Override
    public List<Conge> TriparDateD() {
        List<Conge> conges = new ArrayList<>();
        String sql = "SELECT `ID_Conge`, `DateDebut`, `DateFin`, `TypeConge`, `Statut`, `ID_User`, `file`, `description` FROM `conge` WHERE `ID_User` LIKE '%" + SessionManager.getInstance().getUser().getIdUser() + "%'ORDER BY `DateDebut`";
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
            }
            Statement ste = cnx.createStatement();
            ResultSet rs = ste.executeQuery(sql);
            while (rs.next()) {
                Conge conge = new Conge();
                conge.setIdConge(rs.getInt("ID_Conge"));
                conge.setDateDebut(rs.getDate("DateDebut").toLocalDate());
                conge.setDateFin(rs.getDate("DateFin").toLocalDate());
                conge.setTypeConge(TypeConge.valueOf(rs.getString("TypeConge")));
                conge.setStatut(Statut.valueOf(rs.getString("Statut")));
                conge.setIdUser(rs.getInt("ID_User"));
                conge.setFile(rs.getString("file"));
                conge.setDescription(rs.getString("description"));
                conges.add(conge);
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return conges;
    }

    @Override
    public List<Conge> TriparDateF() {
        List<Conge> conges = new ArrayList<>();
        String sql = "SELECT `ID_Conge`, `DateDebut`, `DateFin`, `TypeConge`, `Statut`, `ID_User`, `file`, `description` FROM `conge` WHERE `ID_User` LIKE '%" + SessionManager.getInstance().getUser().getIdUser() + "%' ORDER BY `DateFin`";
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
            }
            Statement ste = cnx.createStatement();
            ResultSet rs = ste.executeQuery(sql);
            while (rs.next()) {
                Conge conge = new Conge();
                conge.setIdConge(rs.getInt("ID_Conge"));
                conge.setDateDebut(rs.getDate("DateDebut").toLocalDate());
                conge.setDateFin(rs.getDate("DateFin").toLocalDate());
                conge.setTypeConge(TypeConge.valueOf(rs.getString("TypeConge")));
                conge.setStatut(Statut.valueOf(rs.getString("Statut")));
                conge.setIdUser(rs.getInt("ID_User"));
                conge.setFile(rs.getString("file"));
                conge.setDescription(rs.getString("description"));
                conges.add(conge);
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return conges;
    }

    @Override
    public List<Conge> TriparDesc() {
        List<Conge> conges = new ArrayList<>();
        String sql = "SELECT `ID_Conge`, `DateDebut`, `DateFin`, `TypeConge`, `Statut`, `ID_User`, `file`, `description` FROM `conge` WHERE `ID_User` LIKE '%" + SessionManager.getInstance().getUser().getIdUser() + "%' ORDER BY `description`";
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
            }
            Statement ste = cnx.createStatement();
            ResultSet rs = ste.executeQuery(sql);
            while (rs.next()) {
                Conge conge = new Conge();
                conge.setIdConge(rs.getInt("ID_Conge"));
                conge.setDateDebut(rs.getDate("DateDebut").toLocalDate());
                conge.setDateFin(rs.getDate("DateFin").toLocalDate());
                conge.setTypeConge(TypeConge.valueOf(rs.getString("TypeConge")));
                conge.setStatut(Statut.valueOf(rs.getString("Statut")));
                conge.setIdUser(rs.getInt("ID_User"));
                conge.setFile(rs.getString("file"));
                conge.setDescription(rs.getString("description"));
                conges.add(conge);
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return conges;
    }

    @Override
    public List<Conge> Rechreche(String recherche) {
        List<Conge> conges = new ArrayList<>();
        String sql = "SELECT `ID_Conge`, `DateDebut`, `DateFin`, `TypeConge`, `Statut`, `ID_User`, `file`, `description` " +
                "FROM `conge` " +
                "WHERE `ID_User` LIKE ? " +
                "AND (`TypeConge` LIKE ? " +
                "OR `Statut` LIKE ? " +
                "OR `DateDebut` LIKE ? " +
                "OR `DateFin` LIKE ? " +
                "OR `description` LIKE ?)";


        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
            }
            PreparedStatement ste = cnx.prepareStatement(sql);
            String searchPattern = "%" + recherche + "%";
            ste.setString(1, "%" + SessionManager.getInstance().getUser().getIdUser() + "%");
            ste.setString(2, searchPattern);
            ste.setString(3, searchPattern);
            ste.setString(4, searchPattern);
            ste.setString(5, searchPattern);
            ste.setString(6, searchPattern);
            ResultSet rs = ste.executeQuery();
            while (rs.next()) {
                Conge conge = new Conge();
                conge.setIdConge(rs.getInt("ID_Conge"));
                conge.setDateDebut(rs.getDate("DateDebut").toLocalDate());
                conge.setDateFin(rs.getDate("DateFin").toLocalDate());
                conge.setTypeConge(TypeConge.valueOf(rs.getString("TypeConge")));
                conge.setStatut(Statut.valueOf(rs.getString("Statut")));
                conge.setIdUser(rs.getInt("ID_User"));
                conge.setFile(rs.getString("file"));
                conge.setDescription(rs.getString("description"));
                conges.add(conge);
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return conges;
    }

    @Override
    public void updateUserSolde(int userId, int typeCongeId, int congeDays) {
        String query = "UPDATE user_solde " +
                "SET TotalSolde = TotalSolde - ? " +
                "WHERE ID_User = ? AND ID_TypeConge = ?";
        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement ps = cnx.prepareStatement(query)) {
            System.out.println("Executing query: " + query);
            System.out.println("Parameters: congeDays = " + congeDays + ", userId = " + userId + ", typeCongeId = " + typeCongeId);

            ps.setInt(1, congeDays);
            ps.setInt(2, userId);
            ps.setInt(3, typeCongeId);
            int rowsUpdated = ps.executeUpdate();

            System.out.println("Rows updated: " + rowsUpdated);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void NewMessage(String message, int idUser, int idConge) {
        cnx = MyDataBase.getInstance().getCnx();

        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
            }
            String qry = "UPDATE `conge` SET `Message`=? WHERE `ID_User`=? AND `ID_Conge`=?";
            PreparedStatement stm = cnx.prepareStatement(qry);
            stm.setString(1, message);
            stm.setInt(2, idUser);
            stm.setInt(3, idConge);
            stm.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public String AfficherMessage(int id) {
        String Message = "";
        String sql = "SELECT `Message` FROM `conge` WHERE `ID_Conge`=? AND `Message` IS NOT NULL AND `Message` <> '' ";
        try {
            PreparedStatement stm = cnx.prepareStatement(sql);
            stm.setInt(1, id);
            ResultSet rs = stm.executeQuery(sql);
            while (rs.next()) {
                Message = rs.getString("Message");
            }
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
        return Message;
    }

    // New methods for leave request handling
    public int getSupervisor(int userId) throws SQLException {
        String sql = "SELECT ID_Manager FROM user WHERE ID_User = ?";
        try (PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, userId);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("ID_Manager");
            } else {
                throw new SQLException("Manager not found for user ID: " + userId);
            }
        }
    }

    public void handleLeaveRequest(Conge conge) throws SQLException {
        Add(conge);
        int supervisorId = getSupervisor(conge.getIdUser());
        ServiceNotification notificationService = new ServiceNotification();
        notificationService.NewNotification(supervisorId, "New leave request", 0, "You have a new leave request to review.");
    }


}
