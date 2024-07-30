package tn.bfpme.services;

import java.sql.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import tn.bfpme.utils.MyDataBase;
import tn.bfpme.models.*;

public class ServiceUserSolde {
    private static Connection cnx;
    public ServiceUserSolde() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }
    public void updateUserSolde(UserSolde userSolde) {
        String query = "UPDATE user_solde SET TotalSolde = ? WHERE ID_UserSolde = ?";
        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement stm = cnx.prepareStatement(query)) {
            stm.setDouble(1, userSolde.getTotalSolde());
            stm.setInt(2, userSolde.getUD_UserSolde());
            stm.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addUserSolde(int userId, int typeCongeId, double totalSolde) {
        Connection cnx = MyDataBase.getInstance().getCnx();
        String query = "INSERT INTO user_solde(ID_User, ID_TypeConge, TotalSolde) VALUES (?,?,?)";
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
            }
            PreparedStatement pstmt = cnx.prepareStatement(query);
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
        String sql = "SELECT ID_UserSolde, ID_User, ID_TypeConge, TotalSolde FROM user_solde WHERE ID_User = ?";
        Connection cnx = MyDataBase.getInstance().getCnx();
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
            }
            PreparedStatement stm = cnx.prepareStatement(sql);
            stm.setInt(1, userId);
            ResultSet rs = stm.executeQuery();
            while (rs.next()) {
                UserSolde userSolde = new UserSolde();
                userSolde.setUD_UserSolde(rs.getInt("ID_UserSolde"));
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

    public void incrementMonthlyLeaveBalances() {
        List<UserSolde> allUserSoldes = getAllUserSoldes();
        Map<Integer, Double> typeCongeLimits = getTypeCongeLimit();
        Map<Integer, Double> typeCongePas = getTypeCongePas();

        for (UserSolde userSolde : allUserSoldes) {
            int typeCongeId = userSolde.getID_TypeConge();
            double currentSolde = userSolde.getTotalSolde();

            double pas = typeCongePas.getOrDefault(typeCongeId, 0.0);
            double limit = typeCongeLimits.getOrDefault(typeCongeId, Double.MAX_VALUE);
            double newSolde = currentSolde + pas;

            if (newSolde > limit) {
                newSolde = limit;
            }

            userSolde.setTotalSolde(newSolde);
            updateUserSolde(userSolde); // Ensure this method accepts UserSolde object
        }
    }

    public Map<Integer, Double> getTypeCongeLimit() {
        Map<Integer, Double> typeCongeLimits = new HashMap<>();
        String query = "SELECT ID_TypeConge, `Limit` FROM typeconge";  // Use backticks for reserved keywords
        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement stm = cnx.prepareStatement(query);
             ResultSet rs = stm.executeQuery()) {
            while (rs.next()) {
                typeCongeLimits.put(rs.getInt("ID_TypeConge"), rs.getDouble("Limit"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return typeCongeLimits;
    }

    private Map<Integer, Double> getTypeCongePas() {
        Map<Integer, Double> pasMap = new HashMap<>();
        String query = "SELECT ID_TypeConge, Pas FROM typeconge";
        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement stm = cnx.prepareStatement(query);
             ResultSet rs = stm.executeQuery()) {
            while (rs.next()) {
                pasMap.put(rs.getInt("ID_TypeConge"), rs.getDouble("Pas"));
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return pasMap;
    }

    public List<UserSolde> getAllUserSoldes() {
        List<UserSolde> soldeList = new ArrayList<>();
        String query = "SELECT ID_UserSolde, ID_User, ID_TypeConge, TotalSolde FROM user_solde";
        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement stm = cnx.prepareStatement(query);
             ResultSet rs = stm.executeQuery()) {
            while (rs.next()) {
                UserSolde userSolde = new UserSolde(
                        rs.getInt("ID_UserSolde"),
                        rs.getInt("ID_User"),
                        rs.getInt("ID_TypeConge"),
                        rs.getDouble("TotalSolde")
                        //""  // Designation is not present in the table structure
                );
                soldeList.add(userSolde);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return soldeList;
    }
}
