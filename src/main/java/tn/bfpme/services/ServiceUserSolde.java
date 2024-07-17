package tn.bfpme.services;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

import tn.bfpme.utils.MyDataBase;
import tn.bfpme.models.*;

public class ServiceUserSolde {
    private static Connection cnx;
    /*public ServiceUserSolde(Connection cnx) {
        this.cnx = cnx;
    }*/
    public ServiceUserSolde() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }



    public void AddUserWithSoldes(int userId) {
        String fetchTypesQuery = "SELECT ID_TypeConge FROM typeconge";
        String insertQuery = "INSERT INTO user_solde(ID_User, ID_TypeConge, TotalSolde) VALUES (?,?,?)";
        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement fetchStmt = cnx.prepareStatement(fetchTypesQuery);
             PreparedStatement insertStmt = cnx.prepareStatement(insertQuery);
             ResultSet rs = fetchStmt.executeQuery()) {
            while (rs.next()) {
                int typeCongeId = rs.getInt("ID_TypeConge");
                insertStmt.setInt(1, userId);
                insertStmt.setInt(2, typeCongeId);
                insertStmt.setDouble(3, 0.0); // Assuming initial TotalSolde is 0, change if needed
                insertStmt.executeUpdate();
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void updateUserSolde(int userId, int typeCongeId, double newTotalSolde) {
        String query = "UPDATE user_solde SET TotalSolde = ? WHERE ID_User = ? AND ID_TypeConge = ?";
        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setDouble(1, newTotalSolde);
            pstmt.setInt(2, userId);
            pstmt.setInt(3, typeCongeId);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
    public void addUserSolde(int userId, int typeCongeId, double totalSolde) {
        String query = "INSERT INTO user_solde(ID_User, ID_TypeConge, TotalSolde) VALUES (?,?,?)";
        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, userId);
            pstmt.setInt(2, typeCongeId);
            pstmt.setDouble(3, totalSolde);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public List<UserSolde> getUserSoldes(int userId) {
        List<UserSolde> userSoldes = new ArrayList<>();
        String query = "SELECT * FROM user_solde WHERE ID_User = ?";

        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
            }
            PreparedStatement ps = cnx.prepareStatement(query);
            ps.setInt(1, userId);
            ResultSet rs = ps.executeQuery();
            while (rs.next()) {
                UserSolde userSolde = new UserSolde();
                userSolde.setUD_UserSolde(rs.getInt("UD_UserSolde"));
                userSolde.setID_User(rs.getInt("ID_User"));
                userSolde.setID_TypeConge(rs.getInt("ID_TypeConge"));
                userSolde.setTotalSolde(rs.getDouble("TotalSolde"));
                userSoldes.add(userSolde);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        return userSoldes;
    }
}
