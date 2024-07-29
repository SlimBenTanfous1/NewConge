package tn.bfpme.services;

import tn.bfpme.models.Departement;
import tn.bfpme.utils.MyDataBase;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class ServiceDepartement {
    private static Connection cnx = MyDataBase.getInstance().getCnx();

    public static Departement getDepartmentById(int idDepartement) {
        Departement departement = null;
        String sql = "SELECT * FROM departement WHERE ID_Departement = ?";
        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setInt(1, idDepartement);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                departement = new Departement(
                        rs.getInt("ID_Departement"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getInt("Parent_Dept"),
                        rs.getInt("level")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return departement;
    }

    public static Departement getDepartmentByName(String name) {
        Departement departement = null;
        String sql = "SELECT * FROM departement WHERE nom = ?";
        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement stmt = cnx.prepareStatement(sql)) {
            stmt.setString(1, name);
            ResultSet rs = stmt.executeQuery();
            if (rs.next()) {
                departement = new Departement(
                        rs.getInt("ID_Departement"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getInt("Parent_Dept"),
                        rs.getInt("level")
                );
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return departement;
    }

    public static Departement getParentDepartment(int idDepartement) {
        Departement departement = getDepartmentById(idDepartement);
        if (departement != null && departement.getParentDept() != 0) {
            return getDepartmentById(departement.getParentDept());
        }
        return null;
    }

    public List<Departement> getAllDepartments() {
        List<Departement> departments = new ArrayList<>();
        String query = "SELECT d.*, dp.nom AS parentDeptName FROM departement d " +
                "LEFT JOIN departement dp ON d.Parent_Dept = dp.ID_Departement";
        try (Connection cnx = MyDataBase.getInstance().getCnx();
             Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Departement dept = new Departement(
                        rs.getInt("ID_Departement"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getInt("Parent_Dept"),
                        rs.getInt("level")
                );
                dept.setParentDeptName(rs.getString("parentDeptName"));
                departments.add(dept);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return departments;
    }

    public List<Departement> getAllDepartmentParents() {
        List<Departement> departments = new ArrayList<>();
        String query = "SELECT * FROM `departement` WHERE `Parent_Dept` IS NULL";
        try (Connection cnx = MyDataBase.getInstance().getCnx();
             Statement stmt = cnx.createStatement();
             ResultSet rs = stmt.executeQuery(query)) {
            while (rs.next()) {
                Departement dept = new Departement(
                        rs.getInt("ID_Departement"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getInt("Parent_Dept"),
                        rs.getInt("level")
                );
                //dept.setParentDeptName(rs.getString("parentDeptName"));
                departments.add(dept);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return departments;
    }

    public List<Departement> getDepItsParent(int id) {
        List<Departement> departments = new ArrayList<>();
        String query = "SELECT * FROM `departement` WHERE `Parent_Dept` = ?";
        try {
            Connection cnx = MyDataBase.getInstance().getCnx();
            PreparedStatement pts = cnx.prepareStatement(query);
            pts.setInt(1, id);
            ResultSet rs = pts.executeQuery();
            while (rs.next()) {
                Departement dept = new Departement(
                        rs.getInt("ID_Departement"),
                        rs.getString("nom"),
                        rs.getString("description"),
                        rs.getInt("Parent_Dept"),
                        rs.getInt("level")
                );
                //dept.setParentDeptName(rs.getString("parentDeptName"));
                departments.add(dept);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return departments;
    }

    public void addDepartement(String name, String description, Integer parentDeptId,int level) {
        String query = "INSERT INTO departement (nom, description, Parent_Dept, level) VALUES (?, ?, ?,?)";
        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.setInt(3, parentDeptId);
            pstmt.setInt(4, level);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void addDepartement2(String name, String description) {
        String query = "INSERT INTO departement (nom, description,level) VALUES (?, ?, 1)";
        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void updateDepartment(int id, String name, String description, Integer parentDeptId) {
        String query = "UPDATE departement SET nom = ?, description = ?, Parent_Dept = ? WHERE ID_Departement = ?";
        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setString(1, name);
            pstmt.setString(2, description);
            if (parentDeptId != null) {
                pstmt.setInt(3, parentDeptId);
            } else {
                pstmt.setNull(3, Types.INTEGER);
            }
            pstmt.setInt(4, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteDepartment(int id) {
        String query = "DELETE FROM departement WHERE ID_Departement = ?";
        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement pstmt = cnx.prepareStatement(query)) {
            pstmt.setInt(1, id);
            pstmt.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    // Get department by ID
    private void ensureConnection() throws SQLException {
        if (cnx == null || cnx.isClosed()) {
            cnx = MyDataBase.getInstance().getCnx();
        }
    }

    public Departement getDepartementById(int departmentId) throws SQLException {
        ensureConnection();
        String query = "SELECT * FROM departement WHERE ID_Departement = ?";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setInt(1, departmentId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Departement departement = new Departement();
                departement.setIdDepartement(resultSet.getInt("ID_Departement"));
                departement.setNom(resultSet.getString("nom"));
                departement.setDescription(resultSet.getString("description"));
                departement.setParentDept(resultSet.getInt("Parent_Dept"));
                departement.setLevel(resultSet.getInt("Level"));
                return departement;
            } else {
                System.out.println("No department found for department ID: " + departmentId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving department by ID: " + e.getMessage(), e);
        }
        return null;
    }

    private Departement extractDepartementFromResultSet(ResultSet resultSet) throws SQLException {
        Departement departement = new Departement();
        departement.setIdDepartement(resultSet.getInt("ID_Departement"));
        departement.setNom(resultSet.getString("nom"));
        departement.setDescription(resultSet.getString("description"));
        departement.setParentDept(resultSet.getInt("Parent_Dept"));
        departement.setLevel(resultSet.getInt("Level"));
        return departement;
    }
    public Departement getDepartmentByUserId2(int userId) throws SQLException {
        ensureConnection();
        System.out.println("Retrieving department for user ID: " + userId);
        String query = "SELECT d.* FROM departement d " +
                "JOIN user u ON d.ID_Departement = u.ID_Departement " +
                "WHERE u.ID_User = ?";
        try (PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setInt(1, userId);
            ResultSet resultSet = statement.executeQuery();
            if (resultSet.next()) {
                Departement departement = new Departement();
                departement.setIdDepartement(resultSet.getInt("ID_Departement"));
                departement.setNom(resultSet.getString("nom"));
                departement.setDescription(resultSet.getString("description"));
                departement.setParentDept(resultSet.getInt("Parent_Dept"));
                departement.setLevel(resultSet.getInt("Level"));
                System.out.println("Retrieved department: " + departement.getNom() + " for user ID: " + userId);
                return departement;
            } else {
                System.out.println("No department found for user ID: " + userId);
            }
        } catch (SQLException e) {
            throw new RuntimeException("Error retrieving department by user ID: " + e.getMessage(), e);
        }
        return null;
    }



}
