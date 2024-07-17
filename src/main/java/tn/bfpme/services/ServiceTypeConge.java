package tn.bfpme.services;

import tn.bfpme.models.*;

import tn.bfpme.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceTypeConge {
    private Connection cnx;

    public ServiceTypeConge(Connection cnx) {
        this.cnx = cnx;
    }

    public ServiceTypeConge() {
        this.cnx = MyDataBase.getInstance().getCnx();
    }

    public List<TypeConge> getAllTypeConge() {
        List<TypeConge> typeconges = new ArrayList<>();
        String query = "SELECT * FROM `TypeConge`";
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
            }
            Statement stmt = cnx.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                TypeConge type = new TypeConge(
                        rs.getInt("ID_TypeConge"),
                        rs.getString("Designation"),
                        rs.getDouble("Pas"),
                        rs.getInt("PeriodeJ"),
                        rs.getInt("PeriodeM"),
                        rs.getInt("PeriodeA"),
                        rs.getBoolean("File")
                );
                typeconges.add(type);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return typeconges;
    }

    public void AddTypeConge(TypeConge typeConge) {
        String query = "INSERT INTO `typeconge`(`Designation`, `Pas`, `PeriodeJ`, `PeriodeM`, `PeriodeA`, `File`) VALUES (?,?,?,?,?,?)";
        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setString(1, typeConge.getDesignation());
            pstmt.setDouble(2, typeConge.getPas());
            pstmt.setInt(3, typeConge.getPeriodeJ());
            pstmt.setInt(4, typeConge.getPeriodeM());
            pstmt.setInt(5, typeConge.getPeriodeA());
            pstmt.setBoolean(5, typeConge.isFile());
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void UpdateTypeConge(TypeConge typeConge) {
        try {
            String qry = "UPDATE `typeconge` SET `Designation`=?,`Pas`=?,`PeriodeJ`=?,`PeriodeM`=?,`PeriodeA`=?,`File`=? WHERE `ID_TypeConge`=?";
            PreparedStatement stm = cnx.prepareStatement(qry);
            stm.setString(1, typeConge.getDesignation());
            stm.setDouble(2, typeConge.getPas());
            stm.setInt(3, typeConge.getPeriodeJ());
            stm.setInt(4, typeConge.getPeriodeM());
            stm.setInt(5, typeConge.getPeriodeM());
            stm.setBoolean(6, typeConge.isFile());
            stm.setInt(7, typeConge.getIdTypeConge());
            stm.executeUpdate();
        } catch (Exception ex) {
            System.out.println(ex.getMessage());
        }
    }

    public void DeleteTypeCongeById(int idTypeConge) {
        try {
            String qry = "DELETE FROM `typeconge` WHERE `ID_TypeConge`=?";
            PreparedStatement smt = cnx.prepareStatement(qry);
            smt.setInt(1, idTypeConge);
            smt.executeUpdate();
        } catch (SQLException ex) {
            System.out.println(ex.getMessage());
        }
    }

}
