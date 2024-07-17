package tn.bfpme.controllers.RHC;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.control.Alert;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import tn.bfpme.models.Role;
import tn.bfpme.services.ServiceRole;

import java.net.URL;
import java.util.List;
import java.util.ResourceBundle;

public class paneRoleController implements Initializable {

    @FXML
    private TextField roleDescriptionField;
    @FXML
    private ListView<Role> roleListView;
    @FXML
    private TextField roleNameField;

    private final ServiceRole roleService = new ServiceRole();
    private RHController RHC;
    private paneUserController PaneUser;

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadRoles();
        roleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                roleNameField.setText(newValue.getNom());
                roleDescriptionField.setText(newValue.getDescription());
            }
        });
    }

    @FXML
    private void handleAddRole() {
        String name = roleNameField.getText();
        String description = roleDescriptionField.getText();
        roleService.addRole(name, description);
        loadRoles();
        //loadRoleHierarchie();
    }

    @FXML
    private void handleEditRole() {
        Role selectedRole = roleListView.getSelectionModel().getSelectedItem();
        if (selectedRole != null) {
            String name = roleNameField.getText();
            String description = roleDescriptionField.getText();
            roleService.updateRole(selectedRole.getIdRole(), name, description);
            loadRoles();
            //loadRoleHierarchie();
        }
    }

    @FXML
    private void handleDeleteRole() {
        Role selectedRole = roleListView.getSelectionModel().getSelectedItem();
        if (selectedRole != null) {
            roleService.deleteRole(selectedRole.getIdRole());
            loadRoles();
            //loadRoleHierarchie();
        }
    }

    void loadRoles() {
        try {
            List<Role> roleList = roleService.getAllRoles();
            Role noParentRole = new Role(0, "Pas de rôle", "");
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
            /*parentRoleComboBox.setItems(roles);
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
            });*/
        } catch (Exception e) {
            showError("Failed to load roles: " + e.getMessage());
        }
    }

    protected void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
