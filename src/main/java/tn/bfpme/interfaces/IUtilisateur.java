package tn.bfpme.interfaces;

import tn.bfpme.models.User;
import tn.bfpme.models.UserConge;

import java.sql.SQLException;
import java.util.List;

public interface IUtilisateur {

   // UserConge afficherusers();
    List<User> afficherusers();

        // UserConge Rechercher(String search);
    UserConge TriType();
    UserConge TriNom();
    UserConge TriPrenom();
    UserConge TriDateDebut();
    UserConge TriDateFin();
    UserConge AfficherApprove();
    UserConge AfficherReject();

    List<User> RechrecheRH(String recherche);

    // Similar corrections for AfficherApprove, AfficherReject, TriType, TriNom, TriPrenom, TriDateDebut, TriDateFin
    User getChef();

    List<User> getUsersByDepartment(String departement);

    List<User> getAllUsers();
    void updateUser(User user);

    List<User> getAllUsersInfo();

    // Adjust the method signature to match the changes in how solde is handled
    void addUser(String nom, String prenom, String email, String mdp, String image, int idDepartement, int idRole);

    void deleteUser(int idUser);

    //void assignUserToDepartmentAndRole(int idUser, int idDepartement, int idRole) throws SQLException;

    void assignRoleToUser(int userId, int roleId);

    void updateUserRoleAndDepartment(int userId, int roleId, int departmentId) throws SQLException;

    void updateUserRole(int userId, int roleId);

    void updateUserDepartment(int userId, int departmentId);

    User getUserById(int userId);

    void Add(User user);

    void Update(User user);

    List<User> Show();

    void Delete(User user) throws SQLException;

    void DeleteByID(int id);

    List<User> SortDepart();

    List<User> SortRole();

    List<User> searchUsers(String query);

    List<User> search(String query);

    List<User> ShowUnder();
}
