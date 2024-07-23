package tn.bfpme.controllers.RHC;

import javafx.application.Platform;
import javafx.beans.value.ChangeListener;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.fxml.Initializable;
import javafx.scene.control.*;
import javafx.scene.control.cell.TreeItemPropertyValueFactory;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Pane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.lang.ObjectUtils;

import org.apache.poi.ss.usermodel.Cell;
import tn.bfpme.models.*;
import tn.bfpme.services.*;
import tn.bfpme.utils.MyDataBase;
import tn.bfpme.utils.SessionManager;

import java.io.*;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import java.io.FileOutputStream;
import java.net.URL;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.Provider;
import java.sql.*;
import java.time.LocalDate;
import java.util.*;
import java.util.stream.Collectors;

public class paneUserController implements Initializable {
    @FXML
    private TextField Depart_field;
    @FXML
    private TreeTableView<Role> roleTable;
    @FXML
    private TreeTableColumn<Role, Integer> idRoleColumn;
    @FXML
    private TreeTableColumn<Role, String> nomRoleColumn;
    @FXML
    private TreeTableColumn<Role, String> DescRoleColumn;
    @FXML
    private TreeTableColumn<Role, Integer> RoleParColumn;
    @FXML
    private TreeTableColumn<Role, String> RoleFilsColumn;

    @FXML
    private TreeTableView<Departement> deptTable;
    @FXML
    private TreeTableColumn<Departement, Integer> idDapartementColumn;
    @FXML
    private TreeTableColumn<Departement, String> nomDeptColumn;
    @FXML
    private TreeTableColumn<Departement, String> DescriptionDeptColumn;
    @FXML
    private TreeTableColumn<Departement, Integer> DeptparColumn;

    @FXML
    private TreeTableView<User> userTable;
    @FXML
    private TreeTableColumn<User, Integer> idUserColumn;
    @FXML
    private TreeTableColumn<User, String> prenomUserColumn;
    @FXML
    private TreeTableColumn<User, String> nomUserColumn;
    @FXML
    private TreeTableColumn<User, String> departUserColumn;
    @FXML
    private TreeTableColumn<User, String> roleUserColumn;
    @FXML
    private TreeTableColumn<User, String> managerUserColumn;

    @FXML
    private ComboBox<String> RoleComboFilter;

    @FXML
    private TextField Role_field;
    @FXML
    private Button ExporterExcelButton;
    @FXML
    public ListView<User> userListView;
    @FXML
    public TextField User_field;
    @FXML
    public TextField ID_A;
    @FXML
    public TextField MDP_A;
    @FXML
    public ImageView PDPimageHolder;
    @FXML
    public TextField Prenom_A;

    @FXML
    private TextField searchFieldDept;
    @FXML
    private TextField searchFieldRole;
    @FXML
    private TextField searchFieldUser;
    @FXML
    private TextField RechercheBarUser;
    @FXML
    private ComboBox<String> hierarCombo;
    @FXML
    private ListView<Role> roleListView;
    @FXML
    private TextField RoleSearchBar;
    @FXML
    private TextField soldeField1, soldeField2;
    @FXML
    private Button modifier_user, saveButton;
    @FXML
    private Pane paneSoldeUsers;

    @FXML
    public GridPane UserContainers;
    @FXML
    private Label affectationlabel;
    @FXML
    public Pane UtilisateursPane;
    @FXML
    public ListView<Departement> departListView;
    @FXML
    public TextField email_A;
    @FXML
    public TextField image_A;
    @FXML
    public Label infolabel;
    @FXML
    public TextField nom_A;
    @FXML
    private Pane DepartPane1;
    @FXML
    private Pane RolePane1;
    @FXML
    private Pane UserPane1;

    @FXML
    private Button removeFilterButton, adduserbtn;

    @FXML
    private Tab TabAffectationid;


    public User selectedUser;
    public FilteredList<User> filteredData;
    public FilteredList<Departement> filteredDepartments;
    public FilteredList<Role> filteredRoles;
    ServiceUtilisateur UserS = new ServiceUtilisateur();
    Connection cnx = MyDataBase.getInstance().getCnx();

    private final ServiceDepartement depService = new ServiceDepartement();
    private final ServiceUtilisateur userService = new ServiceUtilisateur();
    private final ServiceUserSolde serviceUserSolde = new ServiceUserSolde();
    private final ServiceTypeConge serviceTypeConge = new ServiceTypeConge();
    private final ServiceRole roleService = new ServiceRole();
    private ObservableList<User> users;

    private ChangeListener<User> userSelectionListener = (observable, oldValue, newValue) -> {
        if (newValue != null) {
            handleUserSelection(newValue);
        }
    };


    ObservableList<String> HierarchieList = FXCollections.observableArrayList("Utilisateurs", "Départements");

    @Override
    public void initialize(URL location, ResourceBundle resources) {
        loadUsers();
        loadUsers1();
        loadUsers3();
        loadDepartments1();
        setupSearch();
        setupSearch1();
        TabAffectationid.setOnSelectionChanged(event -> {
            if (TabAffectationid.isSelected()) {
                resetAffectationTab();
            }
        });

        loadRolesIntoComboBox();
        setupRemoveFilterButton();
        setupRoleSearchBar();
        if (SessionManager.getInstance().getUserRoleName().equals("AdminIT")) {
            adduserbtn.setDisable(true);
        }

        setupRoleComboBoxListener();
        loadDeparts3();
        loadRole1s();
        loadRoles3();
        hierarCombo.setValue("Selectioner type");
        hierarCombo.setItems(HierarchieList);

        // Add the listener to the userListView
        userListView.getSelectionModel().selectedItemProperty().addListener(userSelectionListener);
        roleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                handleRoleSelection(newValue);
            }
        });

        // Initially hide solde fields
        hideSoldeFields();
    }

    private void hideSoldeFields() {
    }



    @FXML
    void SelecHierar(ActionEvent event) {
        if (hierarCombo.getValue().equals("Utilisateurs")) {
            UserPane1.setVisible(true);
            RolePane1.setVisible(false);
            DepartPane1.setVisible(false);
        }
        if (hierarCombo.getValue().equals("Départements")) {
            UserPane1.setVisible(false);
            DepartPane1.setVisible(true);
            RolePane1.setVisible(false);
        }
        /*if (hierarCombo.getValue().equals("Roles")) {
            UserPane1.setVisible(false);
            DepartPane1.setVisible(false);
            RolePane1.setVisible(true);
        }*/
    }

    private void loadUsers() {
        UserContainers.getChildren().clear();
        List<User> userList = userService.getAllUsers();
        users = FXCollections.observableArrayList(userList);
        filteredData = new FilteredList<>(users, p -> true);

        int column = 0;
        int row = 0;
        try {
            for (User user : filteredData) {
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("/RH_User_Card.fxml"));
                Pane userBox = fxmlLoader.load();
                CardUserRHController cardController = fxmlLoader.getController();
                Departement department = depService.getDepartmentById(user.getIdDepartement());
                Role role = roleService.getRoleByUserId(user.getIdUser());
                userBox.prefWidthProperty().bind(UserContainers.widthProperty());
                String departmentName = department != null ? department.getNom() : "N/A";
                String roleName = role != null ? role.getNom() : "N/A";
                cardController.setData(user, roleName, departmentName);
                if (column == 1) {
                    column = 0;
                    row++;
                }
                UserContainers.add(userBox, column++, row);
                GridPane.setMargin(userBox, new javafx.geometry.Insets(10));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void handleUserSelection(User selectedUser) {
        this.selectedUser = selectedUser;
        if (selectedUser != null) {
            User_field.setText(selectedUser.getPrenom() + " " + selectedUser.getNom());

            // Fetch and set the department
            Departement department = depService.getDepartmentById(selectedUser.getIdDepartement());
            if (department != null) {
                Depart_field.setText(department.getNom());
            } else {
                Depart_field.setText("No department");
            }

            // Fetch and set the role
            Role role = roleService.getRoleByUserId(selectedUser.getIdUser());
            if (role != null) {
                Role_field.setText(role.getNom());
            } else {
                Role_field.setText("No role");
            }
        }
    }
    private void handleRoleSelection(Role selectedRole) {
        try {
            List<Departement> relatedDepartments = getRelatedDepartments(selectedRole.getIdRole());
            ObservableList<Departement> observableList = FXCollections.observableArrayList(relatedDepartments);
            departListView.setItems(observableList);
        } catch (SQLException e) {
            e.printStackTrace();
            showError("Une erreur s'est produite lors de la récupération des départements : " + e.getMessage());
        }
    }


    private void loadUsers1() {
        List<User> userList = userService.getAllUsers();
        ObservableList<User> users = FXCollections.observableArrayList(userList);
        filteredData = new FilteredList<>(users, p -> true);
        userListView.setItems(filteredData);
        userListView.setCellFactory(param -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                } else {
                    setText(user.getPrenom() + " " + user.getNom());
                }
            }
        });
        userListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                handleUserSelection(newValue);
                // Filter the list to show only the selected user
                filteredData.setPredicate(user -> user.equals(newValue));
            } else {
                // Reset the list to show all users when the selection is cleared
                filteredData.setPredicate(user -> true);
            }
        });
    }

    private void loadDepartments1() {
        List<Departement> departmentList = depService.getAllDepartments();
        ObservableList<Departement> departments = FXCollections.observableArrayList(departmentList);
        filteredDepartments = new FilteredList<>(departments, p -> true);
        departListView.setItems(filteredDepartments);
        departListView.setCellFactory(param -> new ListCell<Departement>() {
            @Override
            protected void updateItem(Departement item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNom());
                }
            }
        });

        departListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                if (newValue != null) {
                    Depart_field.setText(newValue.getNom());
                } else {
                    // Clear selection if newValue is null
                    Depart_field.clear();
                    departListView.getSelectionModel().clearSelection();
                }
            });
        });
    }

    private void loadRole1s() {
        List<Role> roleList = roleService.getAllRoles();
        ObservableList<Role> roles = FXCollections.observableArrayList(roleList);
        filteredRoles = new FilteredList<>(roles, p -> true);
        roleListView.setItems(filteredRoles);
        roleListView.setCellFactory(param -> new ListCell<Role>() {
            @Override
            protected void updateItem(Role item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                } else {
                    setText(item.getNom());
                }
            }
        });

        roleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                if (newValue != null) {
                    Role_field.setText(newValue.getNom());
                } else {
                    Role_field.setText("");
                }
            });
        });
    }

    private void loadUsers3() {
        try {
            List<User> userList = userService.getAllUsers();
            Map<Integer, User> userMap = userList.stream().collect(Collectors.toMap(User::getIdUser, user -> user));

            // Update the manager name, department, and role for each user
            for (User user : userList) {
                User manager = userMap.get(user.getIdManager());
                if (manager != null) {
                    user.setManagerName(manager.getNom());
                } else {
                    user.setManagerName("Il n'y a pas de manager");
                }

                Departement department = userService.getDepartmentByUserId(user.getIdUser());
                if (department != null) {
                    user.setDepartementNom(department.getNom());
                } else {
                    user.setDepartementNom("sans département");
                }

                Role role = userService.getRoleByUserId(user.getIdUser());
                if (role != null) {
                    user.setRoleNom(role.getNom());
                } else {
                    user.setRoleNom("sans rôle");
                }

                // Debugging: Print user details to verify
                System.out.println("User: " + user.getNom() + ", Manager: " + user.getManagerName() + ", Department: " + user.getDepartementNom() + ", Role: " + user.getRoleNom());
            }

            ObservableList<User> users = FXCollections.observableArrayList(userList);

            TreeItem<User> root = new TreeItem<>(new User(0, "", "", "", "", "", null, 0, 0, 0));
            root.setExpanded(true);

            Map<Integer, TreeItem<User>> treeItemMap = new HashMap<>();
            treeItemMap.put(0, root);

            // Create tree items for all users
            for (User user : users) {
                TreeItem<User> item = new TreeItem<>(user);
                treeItemMap.put(user.getIdUser(), item);
            }

            // Attach children to their respective parents
            for (User user : users) {
                TreeItem<User> item = treeItemMap.get(user.getIdUser());
                TreeItem<User> parentItem = treeItemMap.getOrDefault(user.getIdManager(), root);

                if (parentItem != null) {
                    parentItem.getChildren().add(item);
                }
            }

            userTable.setRoot(null);
            userTable.setRoot(root);
            userTable.setShowRoot(false);

            idUserColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("idUser"));
            prenomUserColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("prenom"));
            nomUserColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("nom"));
            roleUserColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("roleNom"));
            departUserColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("departementNom"));
            managerUserColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("managerName"));
        } catch (Exception e) {
            e.printStackTrace();
        }
    }



    private void loadRoles3() {
        List<Role> roleList = roleService.getAllRoles();
        ObservableList<Role> roles = FXCollections.observableArrayList(roleList);

        TreeItem<Role> root = new TreeItem<>(new Role(0, "Sans role parent", "", 0)); // Adjust constructor as necessary
        root.setExpanded(true);
        System.out.println("Root created.");

        Map<Integer, TreeItem<Role>> roleMap = new HashMap<>();
        roleMap.put(0, root);

        for (Role role : roles) {
            System.out.println("Processing role: " + role);
            TreeItem<Role> item = new TreeItem<>(role);
            roleMap.put(role.getIdRole(), item);

            TreeItem<Role> parentItem = roleMap.getOrDefault(role.getRoleParent(), root);
            parentItem.getChildren().add(item);
            System.out.println("Added role to parent: " + role.getRoleParent());
        }

        // Update roles with their parent and child names
        for (Role role : roles) {
            TreeItem<Role> item = roleMap.get(role.getIdRole());
            TreeItem<Role> parentItem = roleMap.get(role.getRoleParent());
            if (parentItem != null && parentItem.getValue() != null) {
                role.setParentRoleName(parentItem.getValue().getNom());
            }
            for (TreeItem<Role> childItem : item.getChildren()) {
                role.setChildRoleName(childItem.getValue().getNom());
            }
        }

        roleTable.setRoot(root);
        roleTable.setShowRoot(false);
        System.out.println("Roles loaded into table.");

        idRoleColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("idRole"));
        nomRoleColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("nom"));
        DescRoleColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("description"));
        RoleParColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("parentRoleName"));
        RoleFilsColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("childRoleName"));
    }

    private void loadDeparts3() {
        System.out.println("Loading departments...");
        List<Departement> departmentList = depService.getAllDepartments();
        System.out.println("Departments: " + departmentList);
        ObservableList<Departement> departments = FXCollections.observableArrayList(departmentList);

        TreeItem<Departement> root = new TreeItem<>(new Departement(0, "Sans dep.Parent", "", 0));
        root.setExpanded(true);
        System.out.println("Root created.");

        Map<Integer, TreeItem<Departement>> departMap = new HashMap<>();
        departMap.put(0, root);

        for (Departement departement : departments) {
            System.out.println("Processing department: " + departement);
            TreeItem<Departement> item = new TreeItem<>(departement);
            departMap.put(departement.getIdDepartement(), item);

            TreeItem<Departement> parentItem = departMap.getOrDefault(departement.getParentDept(), root);
            parentItem.getChildren().add(item);
            System.out.println("Added department to parent: " + departement.getParentDept());
        }

        // Update departments with their parent names
        for (Departement departement : departments) {
            TreeItem<Departement> item = departMap.get(departement.getIdDepartement());
            TreeItem<Departement> parentItem = departMap.get(departement.getParentDept());
            if (parentItem != null && parentItem.getValue() != null) {
                departement.setParentDeptName(parentItem.getValue().getNom());
            }
        }

        deptTable.setRoot(root);
        deptTable.setShowRoot(false);
        System.out.println("Departments loaded into table.");

        idDapartementColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("idDepartement"));
        nomDeptColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("nom"));
        DescriptionDeptColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("description"));
        DeptparColumn.setCellValueFactory(new TreeItemPropertyValueFactory<>("parentDeptName"));
    }


    private boolean isCurrentUser(int userId, String email) {
        User user = UserS.getUserById(userId);
        return user != null && user.getEmail().equals(email);
    }


    @FXML
    public void User_Recherche(KeyEvent event) {
        String searchText = User_field.getText().trim();
        filteredData.setPredicate(user -> {
            if (searchText == null || searchText.isEmpty()) {
                return true;
            }
            String lowerCaseFilter = searchText.toLowerCase();
            return user.getNom().toLowerCase().contains(lowerCaseFilter) ||
                    user.getPrenom().toLowerCase().contains(lowerCaseFilter) ||
                    user.getEmail().toLowerCase().contains(lowerCaseFilter);
        });
    }

    @FXML
    void Depart_Recherche(KeyEvent event) {
        String searchText = Depart_field.getText().trim();
        filteredDepartments.setPredicate(departement -> {
            if (searchText == null || searchText.isEmpty()) {
                return true;
            }
            String lowerCaseFilter = searchText.toLowerCase();
            return (departement.getNom() != null && departement.getNom().toLowerCase().contains(lowerCaseFilter)) ||
                    (departement.getDescription() != null && departement.getDescription().toLowerCase().contains(lowerCaseFilter));
        });
    }

    @FXML
    void Role_Recherche(KeyEvent event) {
        String searchText = Role_field.getText().trim();
        filteredRoles.setPredicate(role -> {
            if (searchText == null || searchText.isEmpty()) {
                return true;
            }
            String lowerCaseFilter = searchText.toLowerCase();
            return role.getNom().toLowerCase().contains(lowerCaseFilter) ||
                    role.getDescription().toLowerCase().contains(lowerCaseFilter);
        });
    }


    @FXML
    void rechercheUser1(ActionEvent event) {
        String searchText = searchFieldUser.getText().trim();
        filteredData.setPredicate(user -> {
            if (searchText == null || searchText.isEmpty()) {
                return true;
            }
            String lowerCaseFilter = searchText.toLowerCase();
            return user.getNom().toLowerCase().contains(lowerCaseFilter) ||
                    user.getPrenom().toLowerCase().contains(lowerCaseFilter) ||
                    user.getEmail().toLowerCase().contains(lowerCaseFilter);
        });
    }

    @FXML
    void rechercheDept1(ActionEvent event) {
        String searchText = searchFieldDept.getText().trim();
        filteredDepartments.setPredicate(departement -> {
            if (searchText == null || searchText.isEmpty()) {
                return true;
            }
            String lowerCaseFilter = searchText.toLowerCase();
            return (departement.getNom() != null && departement.getNom().toLowerCase().contains(lowerCaseFilter)) ||
                    (departement.getDescription() != null && departement.getDescription().toLowerCase().contains(lowerCaseFilter));
        });
    }

    @FXML
    void rechercheRole1(ActionEvent event) {
        String searchText = searchFieldRole.getText().trim();
        filteredRoles.setPredicate(role -> {
            if (searchText == null || searchText.isEmpty()) {
                return true;
            }
            String lowerCaseFilter = searchText.toLowerCase();
            return role.getNom().toLowerCase().contains(lowerCaseFilter) ||
                    role.getDescription().toLowerCase().contains(lowerCaseFilter);
        });
    }



    private List<UserSolde> getSoldeCongeByUserId(int userId) {
        List<UserSolde> soldeCongeList = new ArrayList<>();
        String query = "SELECT us.*, tc.Designation FROM user_solde us " +
                "JOIN typeconge tc ON us.ID_TypeConge = tc.ID_TypeConge " +
                "WHERE us.ID_User = ?";
        try {
            if (cnx == null || cnx.isClosed()) {
                cnx = MyDataBase.getInstance().getCnx();
            }
            PreparedStatement stm = cnx.prepareStatement(query);
            stm.setInt(1, userId);
            ResultSet rs = stm.executeQuery();
            while (rs.next()) {
                UserSolde userSolde = new UserSolde(
                        rs.getInt("ID_UserSolde"),
                        rs.getInt("ID_User"),
                        rs.getInt("ID_TypeConge"),
                        rs.getDouble("TotalSolde"),
                        rs.getString("Designation")
                );
                soldeCongeList.add(userSolde);
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return soldeCongeList;
    }

    @FXML
    private void handleEditUser() {
        if (selectedUser != null) {
            Departement selectedDepartement = departListView.getSelectionModel().getSelectedItem();
            Role selectedRole = roleListView.getSelectionModel().getSelectedItem();
            boolean isUpdated = false;
            try {
                if (selectedRole != null && selectedDepartement != null) {
                    System.out.println("Updating role and department for user: " + selectedUser);
                    userService.checkRoleDepartmentUniqueness(selectedUser.getIdUser(), selectedRole.getIdRole(), selectedDepartement.getIdDepartement());
                    userService.updateUserRoleAndDepartment(selectedUser.getIdUser(), selectedRole.getIdRole(), selectedDepartement.getIdDepartement());
                    isUpdated = true;
                } else if (selectedRole != null) {
                    System.out.println("Updating role for user: " + selectedUser);
                    userService.checkRoleDepartmentUniqueness(selectedUser.getIdUser(), selectedRole.getIdRole(), selectedUser.getIdDepartement());
                    userService.updateUserRoleAndDepartment(selectedUser.getIdUser(), selectedRole.getIdRole(), selectedUser.getIdDepartement());
                    isUpdated = true;
                } else if (selectedDepartement != null) {
                    System.out.println("Updating department for user: " + selectedUser);
                    userService.checkRoleDepartmentUniqueness(selectedUser.getIdUser(), selectedUser.getIdRole(), selectedDepartement.getIdDepartement());
                    userService.updateUserRoleAndDepartment(selectedUser.getIdUser(), selectedUser.getIdRole(), selectedDepartement.getIdDepartement());
                    isUpdated = true;
                }
                if (isUpdated) {
                    loadUsers();
                    affectationlabel.setText("Modification effectuée");
                    resetAffectationTab(); // Reset the tab after editing
                } else {
                    showError("Veuillez sélectionner un rôle et/ou un département à attribuer.");
                }
            } catch (SQLException e) {
                if (e.getMessage().contains("Invalid role-department relationship")) {
                    showError("La relation rôle-département n'est pas valide.");
                } else {
                    e.printStackTrace();
                    showError("Une erreur s'est produite lors de la mise à jour de l'utilisateur : " + e.getMessage());
                }
            } catch (Exception e) {
                e.printStackTrace();
                showError("Une erreur s'est produite : " + e.getMessage());
            }
        } else {
            showError("Veuillez sélectionner un utilisateur à modifier.");
        }
        loadUsers3();
    }



    @FXML
    private void handleRemoveUserAssignment() {
        Integer userId = getSelectedUserId();

        if (userId != null) {
            try {
                Departement selectedDepartement = departListView.getSelectionModel().getSelectedItem();
                Role selectedRole = roleListView.getSelectionModel().getSelectedItem();

                // Update the user's department and/or role
                if (selectedRole != null && selectedDepartement != null) {
                    userService.removeUserRoleAndDepartment(userId);
                    affectationlabel.setText("Rôle et département supprimés.");
                } else if (selectedRole != null) {
                    userService.removeUserRole(userId);
                    affectationlabel.setText("Rôle supprimé.");
                } else if (selectedDepartement != null) {
                    userService.removeUserDepartment(userId);
                    affectationlabel.setText("Département supprimé.");
                } else {
                    // If no role and department selected, remove both
                    userService.removeUserRoleAndDepartment(userId);
                    affectationlabel.setText("Rôle et département supprimés.");
                }

                loadUsers();
                resetAffectationTab(); // Reset the tab after deletion
            } catch (SQLException e) {
                showError("Une erreur s'est produite lors de la suppression de l'affectation de l'utilisateur : " + e.getMessage());
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
                showError("Une erreur s'est produite : " + e.getMessage());
            }
        } else {
            showError("Veuillez sélectionner un utilisateur pour supprimer l'affectation.");
        }
        loadUsers3();
    }




    public Integer getSelectedUserId() {
        return selectedUser != null ? selectedUser.getIdUser() : null;
    }

    private void highlightSelectedUser(User user) {
        Platform.runLater(() -> {
            userListView.getItems().forEach(u -> {
                if (u.equals(user)) {
                    userListView.getSelectionModel().select(u);
                    userListView.scrollTo(u);
                }
            });
            System.out.println("Highlighted User: " + user);
        });
    }

    protected void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    @FXML
    void ajouter_user(ActionEvent actionEvent) {
        String nom = nom_A.getText();
        String prenom = Prenom_A.getText();
        String email = email_A.getText();
        String mdp = MDP_A.getText();
        String image = image_A.getText();

        /*if (email.matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@(bfpme\\.tn|gmail\\.com)$")) {*/
            try {
                if (!emailExists(email)) {
                    User newUser = new User(0, nom, prenom, email, mdp, image, LocalDate.now());
                    UserS.AddUser_RH(newUser);
                    int newUserId = UserS.getLastInsertedUserId();
                    List<TypeConge> typeConges = serviceTypeConge.getAllTypeConge();
                    for (TypeConge typeConge : typeConges) {
                        serviceUserSolde.addUserSolde(newUserId, typeConge.getIdTypeConge(), 0.0);
                    }
                    infolabel.setText("Ajout Effectué");
                } else {
                    infolabel.setText("Email déjà existe");
                }
            } catch (SQLException e) {
                throw new RuntimeException(e);
            }
       /* } else {
            infolabel.setText("Email est invalide");
        }*/
    }

    @FXML
    void modifier_user(ActionEvent event) {
        String Nom = nom_A.getText();
        String Prenom = Prenom_A.getText();
        String Email = email_A.getText();
        String Mdp = MDP_A.getText();
        String Image = image_A.getText();
        /*int solde_annuel = parseIntOrZero(S_Ann.getText());
        int solde_maladie = parseIntOrZero(S_mal.getText());
        int solde_exceptionnel = parseIntOrZero(S_exc.getText());
        int solde_maternite = parseIntOrZero(S_mat.getText());*/
        if (Email.matches("^[_A-Za-z0-9-\\+]+(\\.[_A-Za-z0-9-]+)*@(bfpme\\.tn|gmail\\.com)$")) {
            int IdUser = Integer.parseInt(ID_A.getText());
            try {
                if (!emailExistss(Email, IdUser) || isCurrentUser(IdUser, Email)) {
                    User user = new User(IdUser, Nom, Prenom, Email, Mdp, Image);
                    UserS.Update(user);
                    infolabel.setText("Modification Effectuée");
                } else {
                    infolabel.setText("Email déjà existe");
                }
            } catch (SQLException e) {
                infolabel.setText("Erreur de base de données: " + e.getMessage());
                e.printStackTrace();
            }
        } else {
            infolabel.setText("Email est invalide");
        }
    }

    @FXML
    void supprimer_user(ActionEvent event) {
        try {
            int userId = Integer.parseInt(ID_A.getText());

            User user = UserS.getUserById(userId);
            if (user != null) {
                UserS.DeleteByID(user.getIdUser());
                infolabel.setText("Suppression Effectuée");
                System.out.println("User deleted: " + user);
                loadUsers();
            } else {
                infolabel.setText("Utilisateur non trouvé");
            }
        } catch (NumberFormatException e) {
            infolabel.setText("L'ID de l'utilisateur doit être un nombre.");
        }
    }

    @FXML
    void upload_image(ActionEvent event) {
        String imagePath = null;
        FileChooser fileChooser = new FileChooser();
        fileChooser.setTitle("Choose Image File");
        fileChooser.getExtensionFilters().addAll(new FileChooser.ExtensionFilter("Image Files", "*.png", "*.jpg", "*.gif"));
        Stage stage = (Stage) nom_A.getScene().getWindow();
        File selectedFile = fileChooser.showOpenDialog(stage);
        if (selectedFile != null) {
            try {
                Path destinationFolder = Paths.get("src/main/resources/assets/imgs");
                if (!Files.exists(destinationFolder)) {
                    Files.createDirectories(destinationFolder);
                }
                String fileName = UUID.randomUUID().toString() + "_" + selectedFile.getName();
                Path destinationPath = destinationFolder.resolve(fileName);
                Files.copy(selectedFile.toPath(), destinationPath, StandardCopyOption.REPLACE_EXISTING);
                imagePath = destinationPath.toString();
                System.out.println("Image uploaded successfully: " + imagePath);
                image_A.setText(fileName);
                if (imagePath != null) {
                    try {
                        File file = new File(imagePath);
                        FileInputStream inputStream = new FileInputStream(file);
                        Image image = new Image(inputStream);
                        PDPimageHolder.setImage(image);
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private boolean emailExists(String email) throws SQLException {
        String query = "SELECT * FROM `user` WHERE Email=?";
        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    private boolean emailExistss(String email, int excludeUserId) throws SQLException {
        String query = "SELECT * FROM `user` WHERE Email=?";
        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement statement = cnx.prepareStatement(query)) {
            statement.setString(1, email);
            try (ResultSet resultSet = statement.executeQuery()) {
                return resultSet.next();
            }
        }
    }

    /*private TypeConge getDefaultSolde() {
        String query = "SELECT SoldeAnn, SoldeMal, SoldeExc, SoldeMat FROM soldeconge LIMIT 1";
        try (Connection cnx = MyDataBase.getInstance().getCnx();
             PreparedStatement stm = cnx.prepareStatement(query);
             ResultSet rs = stm.executeQuery()) {
            if (rs.next()) {
                return new TypeConge(
                        rs.getDouble("SoldeAnn"),
                        rs.getDouble("SoldeMal"),
                        rs.getDouble("SoldeExc"),
                        rs.getDouble("SoldeMat")
                );
            } else {
                throw new SQLException("No default solde values found in soldeconge table.");
            }
        } catch (SQLException e) {
            e.printStackTrace();
            return new TypeConge(0, 0, 0, 0);
        }
    }*/

    @FXML
    public void RechercheBarUser(ActionEvent actionEvent) {
        String searchText = RechercheBarUser.getText().trim();
        filteredData.setPredicate(user -> {
            if (searchText == null || searchText.isEmpty()) {
                return true;
            }
            String lowerCaseFilter = searchText.toLowerCase();
            return user.getNom().toLowerCase().contains(lowerCaseFilter) ||
                    user.getPrenom().toLowerCase().contains(lowerCaseFilter) ||
                    user.getEmail().toLowerCase().contains(lowerCaseFilter);
        });
    }

    private void setupSearch() {
        RechercheBarUser.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return user.getNom().toLowerCase().contains(lowerCaseFilter) ||
                        user.getPrenom().toLowerCase().contains(lowerCaseFilter) ||
                        user.getEmail().toLowerCase().contains(lowerCaseFilter);
            });
            refreshUserContainers();
        });
    }

    private void setupSearch1() {
        User_field.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return user.getNom().toLowerCase().contains(lowerCaseFilter) ||
                        user.getPrenom().toLowerCase().contains(lowerCaseFilter) ||
                        user.getEmail().toLowerCase().contains(lowerCaseFilter);
            });
            refreshUserContainers();
        });
    }


    private void refreshUserContainers() {
        UserContainers.getChildren().clear();
        int column = 0;
        int row = 0;
        try {
            for (User user : filteredData) {
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("/RH_User_Card.fxml"));
                Pane userBox = fxmlLoader.load();
                CardUserRHController cardController = fxmlLoader.getController();
                Departement department = depService.getDepartmentById(user.getIdDepartement());
                Role role = new ServiceRole().getRoleByUserId(user.getIdUser());
                String departmentName = department != null ? department.getNom() : "N/A";
                String roleName = role != null ? role.getNom() : "N/A";
                cardController.setData(user, roleName, departmentName);
                if (column == 1) {
                    column = 0;
                    row++;
                }
                UserContainers.add(userBox, column++, row);
                GridPane.setMargin(userBox, new javafx.geometry.Insets(10));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private void loadRolesIntoComboBox() {
        List<Role> roles = roleService.getAllRoles();
        ObservableList<String> roleNames = FXCollections.observableArrayList();
        for (Role role : roles) {
            roleNames.add(role.getNom());
        }
        RoleComboFilter.setItems(roleNames);
        resetRoleComboBoxItems();

    }

    @FXML
    public void filterByRoleCB(ActionEvent actionEvent) {
        String selectedRole = RoleComboFilter.getValue();
        if (selectedRole != null) {
            filteredData.setPredicate(user -> {
                Role userRole = roleService.getRoleById(user.getIdRole());
                return userRole != null && userRole.getNom().equals(selectedRole);
            });
            refreshUserContainers();
        }
    }

    private void setupRoleComboBoxListener() {
        RoleComboFilter.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                Role userRole = roleService.getRoleByUserId(user.getIdUser());
                return userRole != null && userRole.getNom().equals(newValue);
            });
            loadFilteredUsers(); // Call method to refresh the displayed users
        });
    }

    private void loadFilteredUsers() {
        UserContainers.getChildren().clear();
        int column = 0;
        int row = 0;
        try {
            for (User user : filteredData) {
                FXMLLoader fxmlLoader = new FXMLLoader();
                fxmlLoader.setLocation(getClass().getResource("/RH_User_Card.fxml"));
                Pane userBox = fxmlLoader.load();
                CardUserRHController cardController = fxmlLoader.getController();
                Departement department = depService.getDepartmentById(user.getIdDepartement());
                Role role = roleService.getRoleByUserId(user.getIdUser());
                String departmentName = department != null ? department.getNom() : "N/A";
                String roleName = role != null ? role.getNom() : "N/A";
                cardController.setData(user, roleName, departmentName);
                if (column == 1) {
                    column = 0;
                    row++;
                }
                UserContainers.add(userBox, column++, row);
                GridPane.setMargin(userBox, new javafx.geometry.Insets(10));
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    void removeFilters(ActionEvent event) {

    }

    private void setupRemoveFilterButton() {
        removeFilterButton.setOnAction(event -> {
            RoleComboFilter.getSelectionModel().clearSelection();
            filteredData.setPredicate(user -> true);
            loadFilteredUsers();
        });
    }

    private void setupRoleSearchBar() {
        RoleSearchBar.textProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue == null || newValue.isEmpty()) {
                RoleComboFilter.hide();
                resetRoleComboBoxItems();
                return;
            }

            RoleComboFilter.show();

            ObservableList<String> filteredRoles = FXCollections.observableArrayList();
            for (String role : RoleComboFilter.getItems()) {
                if (role.toLowerCase().contains(newValue.toLowerCase())) {
                    filteredRoles.add(role);
                }
            }

            RoleComboFilter.setItems(filteredRoles);
            if (!filteredRoles.isEmpty()) {
                RoleComboFilter.show();
            }
        });
    }

    private void resetRoleComboBoxItems() {
        List<Role> roles = roleService.getAllRoles();
        ObservableList<String> roleNames = FXCollections.observableArrayList();
        for (Role role : roles) {
            roleNames.add(role.getNom());
        }
        RoleComboFilter.setItems(roleNames);
    }

    public void TabGestion(Event event) {
    }


    public void clearuserselection(ActionEvent actionEvent) {
        Depart_field.setText("");
        User_field.setText("");
        Role_field.setText("");
        User_field.clear();
        userListView.getSelectionModel().clearSelection();
        Role_field.clear();
        roleListView.getSelectionModel().clearSelection();
        Depart_field.clear();
        departListView.getSelectionModel().clearSelection();
        filteredData.setPredicate(user -> true);
        filteredDepartments.setPredicate(departement -> true);
        filteredRoles.setPredicate(role -> true);

        // Refresh the list views
        userListView.setItems(filteredData);
        departListView.setItems(filteredDepartments);
        roleListView.setItems(filteredRoles);
    }

    public void clearroleselection(ActionEvent actionEvent) {
        Role_field.setText("");
        Role_field.clear();
        roleListView.getSelectionModel().clearSelection();
        filteredRoles.setPredicate(role -> true);
        roleListView.setItems(filteredRoles);
    }

    public void cleardepartselection(ActionEvent actionEvent) {
        Depart_field.setText("");
        Depart_field.clear();
        departListView.getSelectionModel().clearSelection();
        filteredDepartments.setPredicate(departement -> true);
        departListView.setItems(filteredDepartments);

    }
    @FXML
    private void TabAffectation(Event event) {
        if (TabAffectationid.isSelected()) {
            resetAffectationTab();
        }
    }

    private void resetAffectationTab() {
        affectationlabel.setText("");
        // Clear selection and fields
        User_field.clear();
        Depart_field.clear();
        Role_field.clear();
        userListView.getSelectionModel().clearSelection();
        departListView.getSelectionModel().clearSelection();
        roleListView.getSelectionModel().clearSelection();

        // Reset the filters to show all items
        filteredData.setPredicate(user -> true);
        filteredDepartments.setPredicate(departement -> true);
        filteredRoles.setPredicate(role -> true);

        // Refresh the list views
        userListView.setItems(filteredData);
        departListView.setItems(filteredDepartments);
        roleListView.setItems(filteredRoles);
    }


    @FXML
    public void ExporterExcel(ActionEvent actionEvent) {

    }
    private List<Departement> getRelatedDepartments(int roleId) throws SQLException {
        String query = "SELECT d.* FROM departement d JOIN role_departement rd ON d.ID_Departement = rd.ID_Departement WHERE rd.ID_Role = ?";
        List<Departement> departments = new ArrayList<>();
        try  {
            if (cnx == null || cnx.isClosed()) {
            cnx = MyDataBase.getInstance().getCnx();
        }

            PreparedStatement stmt = cnx.prepareStatement(query);
            stmt.setInt(1, roleId);
            ResultSet rs = stmt.executeQuery();
            while (rs.next()) {
                int id = rs.getInt("ID_Departement");
                String name = rs.getString("nom");
                String description = rs.getString("description");
                int parentDeptId = rs.getInt("Parent_Dept");
                int level = rs.getInt("Level");
                Departement dept = new Departement(id, name, description, parentDeptId, level);
                departments.add(dept);
            }
        } catch (SQLException e) {
            throw new RuntimeException(e);
        }
        return departments;
    }
}
