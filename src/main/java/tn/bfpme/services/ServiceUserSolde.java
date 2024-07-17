package tn.bfpme.services;

import java.sql.*;
import tn.bfpme.utils.MyDataBase;
import tn.bfpme.models.*;

public class ServiceUserSolde {
    private static Connection cnx;
    public ServiceUserSolde(Connection cnx) {
        this.cnx = cnx;
    }
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


}
