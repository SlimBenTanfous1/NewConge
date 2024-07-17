package tn.bfpme.controllers.RHC;

import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.Pane;
import javafx.stage.Popup;
import javafx.stage.Stage;
import javafx.stage.Window;
import tn.bfpme.models.Departement;
import tn.bfpme.models.Role;
import tn.bfpme.models.RoleHierarchie;
import tn.bfpme.models.User;
import tn.bfpme.services.ServiceDepartement;
import tn.bfpme.services.ServiceRole;
import tn.bfpme.services.ServiceUtilisateur;
import tn.bfpme.utils.SessionManager;
import tn.bfpme.utils.StageManager;

import java.io.IOException;
import java.util.List;

public class RHController {
    @FXML
    private Pane DepartementPane, RolesPane, UtilisateursPane, HierarchyPane;
    @FXML
    private ListView<Departement> departementListView;
    @FXML
    private TextField deptNameField, deptDescriptionField;
    @FXML
    private ComboBox<Departement> parentDeptComboBox;
    @FXML
    private ComboBox<Departement> DepHComboBox,ComboFilderDepHie;
    @FXML
    private ListView<Role> roleListView;
    @FXML
    private TextField roleNameField, roleDescriptionField;
    @FXML
    private ComboBox<Role> parentRoleComboBox;
    @FXML
    private ListView<User> userListView;
    @FXML
    private TextField User_field;
    @FXML
    private ComboBox<Departement> departmentComboBox;
    @FXML
    private ComboBox<Role> roleComboBox;
    @FXML
    private Button settingsButton;
    @FXML
    public Button NotifBtn;
    @FXML
    private ComboBox<Role> RoleHComboBox;
    @FXML
    private ListView<RoleHierarchie> roleHListView;

    private FilteredList<User> filteredData;

    @FXML
    private Pane PaneCont;
    private ServiceDepartement depService;
    private ServiceRole roleService;
    private ServiceUtilisateur userService;
    private Popup settingsPopup;
    private Popup notifPopup;

    private User selectedUser; // Store the selected user here

    public void initialize() {
        depService = new ServiceDepartement();
        roleService = new ServiceRole();
        userService = new ServiceUtilisateur();

        loadDepartments();
        loadRoles();
        loadUsers();
        loadRoleHierarchie();

        departementListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                deptNameField.setText(newValue.getNom());
                deptDescriptionField.setText(newValue.getDescription());
                parentDeptComboBox.getSelectionModel().select(newValue.getParentDept() != 0 ? depService.getDepartmentById(newValue.getParentDept()) : null);
            }
        });

        roleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                roleNameField.setText(newValue.getNom());
                roleDescriptionField.setText(newValue.getDescription());
                Role parentRole = roleService.getRoleParents(newValue.getIdRole());
                if (parentRole != null) {
                    parentRoleComboBox.getSelectionModel().select(parentRole);
                } else {
                    parentRoleComboBox.getSelectionModel().clearSelection();
                }
            }
        });

        userListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> {
                if (newValue != null) {
                    handleUserSelection(newValue);
                }
            });
        });

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
                    Departement departement = depService.getDepartmentById(user.getIdDepartement());
                    Role role = roleService.getRoleById(user.getIdRole());
                    setText(user.getPrenom() + " " + user.getNom() + " _ " + user.getEmail() + " _ " + (role != null ? role.getNom() : "N/A") + " _ " + (departement != null ? departement.getNom() : "N/A"));
                }
            }
        });

        User_field.textProperty().addListener((observable, oldValue, newValue) -> {
            filteredData.setPredicate(user -> {
                if (newValue == null || newValue.isEmpty()) {
                    return true;
                }
                String lowerCaseFilter = newValue.toLowerCase();
                return user.getNom().toLowerCase().contains(lowerCaseFilter) || user.getPrenom().toLowerCase().contains(lowerCaseFilter) || user.getEmail().toLowerCase().contains(lowerCaseFilter);
            });
        });
        settingsPopup = new Popup();
        settingsPopup.setAutoHide(true);
        try {
            Parent settingsContent = FXMLLoader.load(getClass().getResource("/Settings.fxml"));
            settingsPopup.getContent().add(settingsContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
        notifPopup = new Popup();
        notifPopup.setAutoHide(true);
        try {
            Parent notifContent = FXMLLoader.load(getClass().getResource("/paneNotif.fxml"));
            notifPopup.getContent().add(notifContent);
        } catch (IOException e) {
            e.printStackTrace();
        }
        userListView.setCellFactory(param -> new ListCell<User>() {
            @Override
            protected void updateItem(User user, boolean empty) {
                super.updateItem(user, empty);
                if (empty || user == null) {
                    setText(null);
                } else {
                    Departement departement = depService.getDepartmentById(user.getIdDepartement());
                    Role role = roleService.getRoleById(user.getIdRole());
                    setText(user.getPrenom() + " " + user.getNom() + " _ " + user.getEmail() + " _ " + (role != null ? role.getNom() : "N/A") + " _ " + (departement != null ? departement.getNom() : "N/A"));
                }
            }
        });


    }

    private void loadDepartments() {
        try {
            List<Departement> departmentList = depService.getAllDepartments();
            Departement noParentDept = new Departement(0, "", "", 0);
            departmentList.add(0, noParentDept);

            ObservableList<Departement> departments = FXCollections.observableArrayList(departmentList);
            departementListView.setItems(departments);
            departementListView.setCellFactory(param -> new ListCell<Departement>() {
                @Override
                protected void updateItem(Departement item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null || item.getNom() == null) {
                        setText(null);
                    } else {
                        setText(item.getNom());
                    }
                }
            });
            parentDeptComboBox.setItems(departments);
            parentDeptComboBox.setCellFactory(param -> new ListCell<Departement>() {
                @Override
                protected void updateItem(Departement item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null || item.getNom() == null) {
                        setText(null);
                    } else {
                        setText(item.getNom());
                    }
                }
            });
            parentDeptComboBox.setButtonCell(new ListCell<Departement>() {
                @Override
                protected void updateItem(Departement item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null || item.getNom() == null) {
                        setText(null);
                    } else {
                        setText(item.getNom());
                    }
                }
            });
            departmentComboBox.setItems(departments);
            departmentComboBox.setCellFactory(param -> new ListCell<Departement>() {
                @Override
                protected void updateItem(Departement item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null || item.getNom() == null) {
                        setText(null);
                    } else {
                        setText(item.getNom());
                    }
                }
            });
            departmentComboBox.setButtonCell(new ListCell<Departement>() {
                @Override
                protected void updateItem(Departement item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null || item.getNom() == null) {
                        setText(null);
                    } else {
                        setText(item.getNom());
                    }
                }
            });
            DepHComboBox.setItems(departments);
            DepHComboBox.setCellFactory(param -> new ListCell<Departement>() {
                @Override
                protected void updateItem(Departement item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null || item.getNom() == null) {
                        setText(null);
                    } else {
                        setText(item.getNom());
                    }
                }
            });
            DepHComboBox.setButtonCell(new ListCell<Departement>() {
                @Override
                protected void updateItem(Departement item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null || item.getNom() == null) {
                        setText(null);
                    } else {
                        setText(item.getNom());
                    }
                }
            });
        } catch (Exception e) {
            showError("Failed to load departments: " + e.getMessage());
        }
    }

    private void loadRoles() {
        try {
            List<Role> roleList = roleService.getAllRoles();
            Role noParentRole = new Role(0, "", "");
            roleList.add(0, noParentRole);
            ObservableList<Role> roles = FXCollections.observableArrayList(roleList);
            roleListView.setItems(roles);
            roleListView.setCellFactory(param -> new ListCell<Role>() {
                @Override
                protected void updateItem(Role item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null || item.getNom() == null) {
                        setText(null);
                    } else {
                        setText(item.getNom());
                    }
                }
            });
            parentRoleComboBox.setItems(roles);
            parentRoleComboBox.setCellFactory(param -> new ListCell<Role>() {
                @Override
                protected void updateItem(Role item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null || item.getNom() == null) {
                        setText(null);
                    } else {
                        setText(item.getNom());
                    }
                }
            });
            parentRoleComboBox.setButtonCell(new ListCell<Role>() {
                @Override
                protected void updateItem(Role item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null || item.getNom() == null) {
                        setText(null);
                    } else {
                        setText(item.getNom());
                    }
                }
            });
            RoleHComboBox.setItems(roles);
            RoleHComboBox.setCellFactory(param -> new ListCell<Role>() {
                @Override
                protected void updateItem(Role item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null || item.getNom() == null) {
                        setText(null);
                    } else {
                        setText(item.getNom());
                    }
                }
            });
            RoleHComboBox.setButtonCell(new ListCell<Role>() {
                @Override
                protected void updateItem(Role item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null || item.getNom() == null) {
                        setText(null);
                    } else {
                        setText(item.getNom());
                    }
                }
            });
            roleComboBox.setItems(roles);
            roleComboBox.setCellFactory(param -> new ListCell<Role>() {
                @Override
                protected void updateItem(Role item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null || item.getNom() == null) {
                        setText(null);
                    } else {
                        setText(item.getNom());
                    }
                }
            });
            roleComboBox.setButtonCell(new ListCell<Role>() {
                @Override
                protected void updateItem(Role item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null || item.getNom() == null) {
                        setText(null);
                    } else {
                        setText(item.getNom());
                    }
                }
            });
        } catch (Exception e) {
            showError("Failed to load roles: " + e.getMessage());
        }
    }

    private void loadUsers() {
        try {
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
                        Departement departement = depService.getDepartmentById(user.getIdDepartement());
                        Role role = roleService.getRoleById(user.getIdRole());
                        setText(user.getPrenom() + " " + user.getNom() + " _ " + user.getEmail() + " _ " + (role != null ? role.getNom() : "N/A") + " _ " + (departement != null ? departement.getNom() : "N/A"));
                    }
                }
            });
        } catch (Exception e) {
            showError("Failed to load users: " + e.getMessage());
        }
    }

    private void loadRoleHierarchie() {
        try {
            ObservableList<RoleHierarchie> roleHierarchies = FXCollections.observableArrayList(roleService.getAllRoleHierarchies());
            roleHListView.setItems(roleHierarchies);
        } catch (Exception e) {
            showError("Failed to load role hierarchies: " + e.getMessage());
        }
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    public Integer getSelectedUserId() {
        return selectedUser != null ? selectedUser.getIdUser() : null;
    }

    private void loadRoleHeierarchie() {
        ObservableList<RoleHierarchie> roleHierarchies = FXCollections.observableArrayList(roleService.getAllRoleHierarchies());
        roleHListView.setItems(roleHierarchies);
    }

    @FXML
    private void handleAddDepartment() {
        String name = deptNameField.getText();
        String description = deptDescriptionField.getText();
        Departement parent = parentDeptComboBox.getSelectionModel().getSelectedItem();

        if (parent.getNom().isEmpty()) {
            depService.addDepartement2(name, description);
        } else {
            depService.addDepartement(name, description, parent.getIdDepartement() != 0 ? parent.getIdDepartement() : 0);
        }
        loadDepartments();
    }

    @FXML
    private void handleEditDepartment() {
        Departement selectedDept = departementListView.getSelectionModel().getSelectedItem();
        if (selectedDept != null) {
            String name = deptNameField.getText();
            String description = deptDescriptionField.getText();
            Departement parent = parentDeptComboBox.getSelectionModel().getSelectedItem();
            depService.updateDepartment(selectedDept.getIdDepartement(), name, description, parent != null ? parent.getIdDepartement() : null);
            loadDepartments();
        }
    }

    @FXML
    private void handleDeleteDepartment() {
        Departement selectedDept = departementListView.getSelectionModel().getSelectedItem();
        if (selectedDept != null) {
            depService.deleteDepartment(selectedDept.getIdDepartement());
            loadDepartments();
        }
    }

    @FXML
    private void handleAddRole() {
        String name = roleNameField.getText();
        String description = roleDescriptionField.getText();
        roleService.addRole(name, description);
        loadRoles();
        loadRoleHierarchie();
    }

    @FXML
    private void handleEditRole() {
        Role selectedRole = roleListView.getSelectionModel().getSelectedItem();
        if (selectedRole != null) {
            String name = roleNameField.getText();
            String description = roleDescriptionField.getText();
            roleService.updateRole(selectedRole.getIdRole(), name, description);
            loadRoles();
            loadRoleHierarchie();
        }
    }

    @FXML
    private void handleDeleteRole() {
        Role selectedRole = roleListView.getSelectionModel().getSelectedItem();
        if (selectedRole != null) {
            roleService.deleteRole(selectedRole.getIdRole());
            loadRoles();
            loadRoleHierarchie();
        }
    }

    @FXML
    void handleAddRoleToH(ActionEvent event) {
        Role parent = parentRoleComboBox.getSelectionModel().getSelectedItem();
        Role child = RoleHComboBox.getSelectionModel().getSelectedItem();
        roleService.addRoleHierarchy(parent, child);
        loadRoleHierarchie();
    }

    @FXML
    void handleDeleteRoleH(ActionEvent event) {
        Role selectedRole = roleListView.getSelectionModel().getSelectedItem();
        if (selectedRole != null) {
            roleService.deleteRoleHierarchy(selectedRole.getIdRole());
            loadRoleHierarchie();
        }
    }

    @FXML
    void handleEditRoleH(ActionEvent event) {
        RoleHierarchie selectedRole = roleHListView.getSelectionModel().getSelectedItem();
        Role parent = parentRoleComboBox.getSelectionModel().getSelectedItem();
        Role child = RoleHComboBox.getSelectionModel().getSelectedItem();
        int id = selectedRole.getIdRoleH();
        if (selectedRole != null) {
            roleService.updateRoleHierarchy(id, parent, child);
            loadRoleHierarchie();
        }
    }

    @FXML
    private void handleEditUser() {
        // Debug statement to check if a user is selected
        System.out.println("Selected User in handleEditUser: " + selectedUser);

        if (selectedUser != null) {
            Role selectedRole = roleComboBox.getSelectionModel().getSelectedItem();
            Departement selectedDepartement = departmentComboBox.getSelectionModel().getSelectedItem();

            // Debug statements to check selected role and department
            System.out.println("Selected Role: " + selectedRole);
            System.out.println("Selected Department: " + selectedDepartement);

            boolean isUpdated = false;

            try {
                if (selectedRole != null && selectedDepartement != null) {
                    userService.updateUserRoleAndDepartment(selectedUser.getIdUser(), selectedRole.getIdRole(), selectedDepartement.getIdDepartement());
                    isUpdated = true;
                } else if (selectedRole != null) {
                    userService.updateUserRole(selectedUser.getIdUser(), selectedRole.getIdRole());
                    isUpdated = true;
                } else if (selectedDepartement != null) {
                    userService.updateUserDepartment(selectedUser.getIdUser(), selectedDepartement.getIdDepartement());
                    isUpdated = true;
                }

                if (isUpdated) {
                    loadUsers();
                    highlightSelectedUser(selectedUser);
                } else {
                    showError("Please select a role and/or department to assign.");
                }
            } catch (Exception e) {
                // Log any exception that occurs during the update process
                e.printStackTrace();
                showError("An error occurred while updating the user: " + e.getMessage());
            }
        } else {
            showError("Please select a user to edit.");
        }
    }

    private void handleUserSelection(User newValue) {
        selectedUser = newValue;
        try {
            User_field.setText(newValue.getPrenom() + " " + newValue.getNom());

            Departement departement = depService.getDepartmentById(newValue.getIdDepartement());
            Role role = roleService.getRoleById(newValue.getIdRole());

            if (departement != null) {
                departmentComboBox.getSelectionModel().select(departement);
            } else {
                departmentComboBox.getSelectionModel().clearSelection();
            }

            if (role != null) {
                roleComboBox.getSelectionModel().select(role);
            } else {
                roleComboBox.getSelectionModel().clearSelection();
            }

            // Debugging to check the selected user
            System.out.println("Selected User in Listener: " + newValue);

        } catch (Exception e) {
            e.printStackTrace(); // Log the exception to the console
            showError("An error occurred while selecting the user: " + e.getMessage());
        }
    }

    public void highlightSelectedUser(User user) {
        Platform.runLater(() -> {
            userListView.getItems().forEach(u -> {
                if (u.equals(user)) {
                    userListView.getSelectionModel().select(u);
                    userListView.scrollTo(u);
                }
            });
        });
    }

    @FXML
    private void handleAssignUser() {
        Integer userId = getSelectedUserId();
        Role selectedRole = roleComboBox.getSelectionModel().getSelectedItem();

        if (userId != null && selectedRole != null) {
            int roleId = selectedRole.getIdRole();
            userService.assignRoleToUser(userId, roleId);
            loadUsers();
            highlightSelectedUser(userService.getUserById(userId));
        } else {
            showError("Please select a user and a role to assign.");
        }
    }

    @FXML
    private void showDepartementPane() {
        DepartementPane.setVisible(true);
        RolesPane.setVisible(false);
        UtilisateursPane.setVisible(false);
        HierarchyPane.setVisible(false);

    }

    @FXML
    private void showRolesPane() {
        DepartementPane.setVisible(false);
        RolesPane.setVisible(true);
        UtilisateursPane.setVisible(false);
        HierarchyPane.setVisible(false);
    }

    @FXML
    void showHierarchyPane(ActionEvent event) {
        DepartementPane.setVisible(false);
        RolesPane.setVisible(false);
        UtilisateursPane.setVisible(false);
        HierarchyPane.setVisible(true);
    }

    @FXML
    private void showUtilisateursPane() {
        DepartementPane.setVisible(false);
        RolesPane.setVisible(false);
        UtilisateursPane.setVisible(true);
        HierarchyPane.setVisible(false);

    }

    @FXML
    void ListeDesDemandes(ActionEvent event) {
        navigateToScene(event, "/DemandeDepListe.fxml", "Liste des demandes - " + SessionManager.getInstance().getUserDepartmentName());
    }

    @FXML
    public void goto_profil(ActionEvent actionEvent) {
        navigateToScene(actionEvent, "/profile.fxml", "Mon profil");
    }

    @FXML
    public void Demander(ActionEvent actionEvent) {
        navigateToScene(actionEvent, "/DemandeConge.fxml", "Demande congé");
    }

    @FXML
    public void Historique(ActionEvent actionEvent) {
        navigateToScene(actionEvent, "/HistoriqueConge.fxml", "Historique congé");
    }

    @FXML
    void settings_button(ActionEvent event) {
        if (settingsPopup.isShowing()) {
            settingsPopup.hide();
        } else {
            Window window = ((Node) event.getSource()).getScene().getWindow();
            double x = window.getX() + settingsButton.localToScene(0, 0).getX() + settingsButton.getScene().getX() - 150;
            double y = window.getY() + settingsButton.localToScene(0, 0).getY() + settingsButton.getScene().getY() + settingsButton.getHeight();
            settingsPopup.show(window, x, y);
        }
    }

    @FXML
    void OpenNotifPane(ActionEvent event) {
        if (notifPopup.isShowing()) {
            notifPopup.hide();
        } else {
            Window window = ((Node) event.getSource()).getScene().getWindow();
            double x = window.getX() + NotifBtn.localToScene(0, 0).getX() + NotifBtn.getScene().getX() - 250;
            double y = window.getY() + NotifBtn.localToScene(0, 0).getY() + NotifBtn.getScene().getY() + NotifBtn.getHeight();
            notifPopup.show(window, x, y);
        }
    }

    private void navigateToScene(ActionEvent actionEvent, String fxmlFile, String title) {
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource(fxmlFile));
            Parent root = loader.load();
            Stage stage = (Stage) ((Node) actionEvent.getSource()).getScene().getWindow();
            Scene scene = new Scene(root);
            stage.setScene(scene);
            stage.setTitle(title);
            stage.show();
            StageManager.addStage(title, stage);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @FXML
    public void User_Recherche(ActionEvent actionEvent) {
        String searchText = User_field.getText().trim();
        for (User user : userService.getAllUsers()) {
            if ((user.getNom() + " " + user.getPrenom()).equalsIgnoreCase(searchText) || user.getEmail().equalsIgnoreCase(searchText) || ((user.getPrenom() + " " + user.getNom()).equalsIgnoreCase(searchText))) {
                User_field.setText(user.getEmail());
                break;
            }
        }

    }

    public Pane getPaneCont() {
        return PaneCont;
    }
}
