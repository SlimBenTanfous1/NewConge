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

    public List<TypeConge> searchTypeConge(String searchText) {
            List<TypeConge> typeConges = new ArrayList<>();
            String query = "SELECT ID_TypeConge, Designation, Pas, PeriodeJ, PeriodeM, PeriodeA, File FROM typeconge WHERE LOWER(Designation) LIKE ?";
            try (Connection connection = MyDataBase.getInstance().getCnx();
                 PreparedStatement preparedStatement = connection.prepareStatement(query)) {
                String searchPattern = "%" + searchText.toLowerCase() + "%";
                preparedStatement.setString(1, searchPattern);
                ResultSet resultSet = preparedStatement.executeQuery();
                while (resultSet.next()) {
                    int idTypeConge = resultSet.getInt("ID_TypeConge");
                    String designation = resultSet.getString("Designation");
                    double pas = resultSet.getDouble("Pas");
                    int periodeJ = resultSet.getInt("PeriodeJ");
                    int periodeM = resultSet.getInt("PeriodeM");
                    int periodeA = resultSet.getInt("PeriodeA");
                    boolean file = resultSet.getBoolean("File");

                    TypeConge typeConge = new TypeConge(idTypeConge, designation, pas, periodeJ, periodeM, periodeA, file);
                    typeConges.add(typeConge);
                }
            } catch (SQLException e) {
                e.printStackTrace();
            }
            return typeConges;
        }

    public int AddTypeConge(String designation, double pas, int periodeJ, int periodeM, int periodeA, boolean file) {
        String query = "INSERT INTO typeconge (Designation, Pas, PeriodeJ, PeriodeM, PeriodeA, File) VALUES (?, ?, ?, ?, ?, ?)";
        try {
            Connection connection = MyDataBase.getInstance().getCnx();
            PreparedStatement pstmt = connection.prepareStatement(query, Statement.RETURN_GENERATED_KEYS);
            pstmt.setString(1, designation);
            pstmt.setDouble(2, pas);
            pstmt.setInt(3, periodeJ);
            pstmt.setInt(4, periodeM);
            pstmt.setInt(5, periodeA);
            pstmt.setBoolean(6, file);
            pstmt.executeUpdate();
            ResultSet rs = pstmt.getGeneratedKeys();
            if (rs.next()) {
                return rs.getInt(1); // Return the generated ID
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return -1;
    }

    public double getPasBySoldeId(int idSolde) {
        String query = "SELECT Pas FROM typeconge WHERE ID_TypeConge = ?";
        try (Connection conn = MyDataBase.getInstance().getCnx();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idSolde);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getDouble("Pas");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }


    public int getSoldeCongeIdByDesignation(String designation) {
        String query = "SELECT ID_TypeConge FROM typeconge WHERE Designation = ?";
        try (Connection conn = MyDataBase.getInstance().getCnx();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setString(1, designation);
            ResultSet rs = pstmt.executeQuery();
            if (rs.next()) {
                return rs.getInt("ID_TypeConge");
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return 0;
    }
    public void updateTypeConge(int idSolde, String designation, double pas, int periodeJ, int periodeM, int periodeA, boolean fileRequired) {
        String query = "UPDATE typeconge SET Designation = ?, Pas = ?, PeriodeJ = ?, PeriodeM = ?, PeriodeA = ?, File = ? WHERE ID_TypeConge = ?";
        try (Connection conn = MyDataBase.getInstance().getCnx();
             PreparedStatement stmt = conn.prepareStatement(query)) {
            stmt.setString(1, designation);
            stmt.setDouble(2, pas);
            stmt.setInt(3, periodeJ);
            stmt.setInt(4, periodeM);
            stmt.setInt(5, periodeA);
            stmt.setBoolean(6, fileRequired);
            stmt.setInt(7, idSolde);
            stmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }


    public void deleteTypeConge(int idSolde) {
        String query = "DELETE FROM typeconge WHERE ID_TypeConge = ?";
        try (Connection conn = MyDataBase.getInstance().getCnx();
             PreparedStatement pstmt = conn.prepareStatement(query)) {
            pstmt.setInt(1, idSolde);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean existsByDesignation(String designation) {
        String query = "SELECT COUNT(*) FROM typeconge WHERE Designation = ?";
        try (Connection connection = MyDataBase.getInstance().getCnx();
             PreparedStatement preparedStatement = connection.prepareStatement(query)) {
            preparedStatement.setString(1, designation);
            ResultSet resultSet = preparedStatement.executeQuery();
            if (resultSet.next()) {
                return resultSet.getInt(1) > 0;
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return false;
    }


}
