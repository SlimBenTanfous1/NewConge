package tn.bfpme.models;

import java.time.LocalDate;
import java.util.*;

public class User {
    private int idUser;
    private String nom;
    private String prenom;
    private String email;
    private String mdp;
    private String image;
    private LocalDate creationDate;
    private int idManager;
    private int idDepartement;
    private int idRole;
    private int ID_UserSolde; // Add this field
    private Map<Integer, Double> soldeMap = new HashMap<>();
    private Map<Integer, String> typeCongeMap = new HashMap<>();
    private String departementNom; // New field for department name
    private String roleNom; // New field for role name
    private String managerName; // New field for manager name
    private double TotalSolde;
    private List<TypeConge> typeConges; // New field for TypeConge objects

    public User() {
        this.typeConges = new ArrayList<>();
    }

    public User(int idUser, String nom, String prenom, String email, String mdp, String image, LocalDate creationDate, int idManager, int idDepartement, int idRole, int ID_UserSolde, String departementNom, String roleNom, String managerName) {
        this.idUser = idUser;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.mdp = mdp;
        this.image = image;
        this.creationDate = creationDate;
        this.idManager = idManager;
        this.idDepartement = idDepartement;
        this.idRole = idRole;
        this.ID_UserSolde = ID_UserSolde;
        this.departementNom = departementNom;
        this.roleNom = roleNom;
        this.managerName = managerName;
    }
    public User(int idUser, String nom, String prenom, String email, String mdp, String image, LocalDate creationDate, int idManager, int idDepartement, int idRole) {
        this.idUser = idUser;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.mdp = mdp;
        this.image = image;
        this.creationDate = creationDate != null ? creationDate : LocalDate.now();
        this.idManager = idManager;
        this.idDepartement = idDepartement;
        this.idRole = idRole;
    }

    public User(int idUser, String nom, String prenom, String email, String mdp, String image, TypeConge idUser1, int idDepartement, int idRole) {

    }

    public User(int idUser, String nom, String prenom, String email, String mdp, String image, int idDepartement, int idManager, LocalDate creationDate) {
        this.idUser = idUser;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.mdp = mdp;
        this.image = image;
        this.idDepartement = idDepartement;
        this.idManager = idManager;
        this.creationDate = creationDate != null ? creationDate : LocalDate.now();
    }

    public User(int i, String sansManager, String s, String s1, String s2, String s3, int i1, int i2, int i3, int i4, int i5, int i6) {
    }

    public User(int idUser, String nom, String prenom, String email, String mdp, String image) { //Const modif
        this.idUser = idUser;
        this.nom = nom;
        this.prenom = prenom;
        this.email = email;
        this.mdp = mdp;
        this.image = image;
    }

    public User(int idUser, String root, String prenom, String email, String mdp, String image, int i, int idManager, int idDepartement, int idRole, int idUserSolde, int i1, int i2, int i3) {
    }

    public int getID_UserSolde() {
        return ID_UserSolde;
    }

    public void setID_UserSolde(int ID_UserSolde) {
        this.ID_UserSolde = ID_UserSolde;
    }

    public int getIdUser() {
        return idUser;
    }

    public void setIdUser(int idUser) {
        this.idUser = idUser;
    }

    public String getNom() {
        return nom;
    }

    public void setNom(String nom) {
        this.nom = nom;
    }

    public String getPrenom() {
        return prenom;
    }

    public void setPrenom(String prenom) {
        this.prenom = prenom;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getMdp() {
        return mdp;
    }

    public void setMdp(String mdp) {
        this.mdp = mdp;
    }

    public String getImage() {
        return image;
    }

    public void setImage(String image) {
        this.image = image;
    }


    public int getIdManager() {
        return idManager;
    }

    public void setIdManager(int idManager) {
        this.idManager = idManager;
    }

    public int getIdDepartement() {
        return idDepartement;
    }

    public void setIdDepartement(int idDepartement) {
        this.idDepartement = idDepartement;
    }

    public int getIdRole() {
        return idRole;
    }

    public void setIdRole(int idRole) {
        this.idRole = idRole;
    }

    public String getDepartementNom() {
        return departementNom;
    }

    public void setDepartementNom(String departementNom) {
        this.departementNom = departementNom;
    }

    public String getRoleNom() {
        return roleNom;
    }

    public void setRoleNom(String roleNom) {
        this.roleNom = roleNom;
    }

    public String getManagerName() {
        return managerName == null || managerName.isEmpty() ? "Il n'y a pas de manager" : managerName;
    }

    public void setManagerName(String managerName) {
        this.managerName = managerName;
    }

    public LocalDate getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(LocalDate creationDate) {
        this.creationDate = creationDate;
    }
    public void setSoldeByType(int typeCongeId, double totalSolde, String typeConge) {
        this.soldeMap.put(typeCongeId, totalSolde);
        this.typeCongeMap.put(typeCongeId, typeConge);
    }

    public double getSoldeByType(int typeCongeId) {
        return this.soldeMap.getOrDefault(typeCongeId, 0.0);
    }

    public String getTypeConge(int typeCongeId) {
        return this.typeCongeMap.get(typeCongeId);
    }

    @Override
    public String toString() {
        return "User{" +
                "idUser=" + idUser +
                ", nom='" + nom + '\'' +
                ", prenom='" + prenom + '\'' +
                ", email='" + email + '\'' +
                ", mdp='" + mdp + '\'' +
                ", image='" + image + '\'' +
                ", creationDate=" + creationDate +
                ", idManager=" + idManager +
                ", idDepartement=" + idDepartement +
                ", idRole=" + idRole +
                ", ID_UserSolde=" + ID_UserSolde +
                ", departementNom='" + departementNom + '\'' +
                ", roleNom='" + roleNom + '\'' +
                ", managerName='" + managerName + '\'' +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return idUser == user.idUser;
    }

    @Override
    public int hashCode() {
        return Objects.hash(idUser);
    }

    public void addTypeConge(TypeConge typeConge) {
        if (this.typeConges == null) {
            this.typeConges = new ArrayList<>();
        }
        this.typeConges.add(typeConge);
    }

    // Getter for typeConges
    public List<TypeConge> getTypeConges() {
        return typeConges;
    }
}
