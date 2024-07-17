package tn.bfpme.services;

import tn.bfpme.interfaces.IConge;
import tn.bfpme.models.*;
import tn.bfpme.models.Statut;
import tn.bfpme.utils.MyDataBase;
import tn.bfpme.utils.SessionManager;


import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceConge implements IConge<Conge> {
    private Connection cnx;

    public ServiceConge(Connection cnx) {
        this.cnx = cnx;
    }

    public ServiceConge() {
        this.cnx = MyDataBase.getInstance().getCnx();
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
        String sql = "SELECT * FROM conge WHERE `ID_User` = ?";
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
                try {
                    conge.setTypeConge(TypeConge.valueOf(rs.getString("TypeConge")));
                } catch (IllegalArgumentException e) {
                    System.out.println("Unknown TypeConge value: " + rs.getString("TypeConge"));
                    continue;
                }
                try {
                    conge.setStatut(Statut.valueOf(rs.getString("Statut")));
                } catch (IllegalArgumentException e) {
                    System.out.println("Unknown Statut value: " + rs.getString("Statut"));
                    continue;
                }
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
    public void updateSoldeAnnuel(int id, int solde) {
        return;
    }

    @Override
    public void updateSoldeAnnuel(int id, double solde) {
        String query = "UPDATE user SET Solde_Annuel = ? WHERE ID_User = ?";
        try (PreparedStatement stm = cnx.prepareStatement(query)) {
            stm.setDouble(1, solde);
            stm.setInt(2, id);
            stm.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateSoldeMaladie(int id, int solde) {

    }

    @Override
    public void updateSoldeMaladie(int id, double solde) {
        try {
            String qry = "UPDATE `user` SET `Solde_Maladie`=? WHERE `ID_User`=?";
            PreparedStatement stm = cnx.prepareStatement(qry);
            stm.setDouble(1, solde);
            stm.setInt(2, id);
            stm.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void updateSoldeExceptionnel(int id, int solde) {

    }

    @Override
    public void updateSoldeExceptionnel(int id, double solde) {
        try {
            String qry = "UPDATE `user` SET `Solde_Exceptionnel`=? WHERE `ID_User`=?";
            PreparedStatement stm = cnx.prepareStatement(qry);
            stm.setDouble(1, solde);
            stm.setInt(2, id);
            stm.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void updateSoldeMaternité(int id, int solde) {

    }

    @Override
    public void updateSoldeMaternité(int id, double solde) {
        try {
            String qry = "UPDATE `user` SET `Solde_Maternité`=? WHERE `ID_User`=?";
            PreparedStatement stm = cnx.prepareStatement(qry);
            stm.setDouble(1, solde);
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
