package tn.bfpme.controllers.RHC;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.VBox;
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
    @FXML
    private VBox roleParentVBox;

    private final ServiceRole roleService = new ServiceRole();

    @Override
    public void initialize(URL url, ResourceBundle resourceBundle) {
        loadRoles();
        roleListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue != null) {
                roleNameField.setText(newValue.getNom());
                roleDescriptionField.setText(newValue.getDescription());
                populateParentRolesComboBoxes(newValue.getIdRole());
            }
        });
    }

    private void populateParentRolesComboBoxes(int roleId) {
        roleParentVBox.getChildren().clear(); // Clear any existing ComboBoxes
        List<Role> parentRoles = roleService.getParentRoles2(roleId);
        ObservableList<Role> allRoles = FXCollections.observableArrayList(roleService.getAllRoles());

        for (Role parentRole : parentRoles) {
            ComboBox<Role> comboBox = new ComboBox<>(allRoles);
            comboBox.setValue(parentRole);
            comboBox.setPrefHeight(31);
            comboBox.setPrefWidth(281);
            comboBox.setCellFactory(param -> new ListCell<Role>() {
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
            comboBox.setButtonCell(new ListCell<Role>() {
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
            roleParentVBox.getChildren().add(comboBox);
        }
    }

    @FXML
    private void handleAddRole() {
        String name = roleNameField.getText();
        String description = roleDescriptionField.getText();
        roleService.addRole(name, description);
        loadRoles();
    }

    @FXML
    private void handleEditRole() {
        Role selectedRole = roleListView.getSelectionModel().getSelectedItem();
        if (selectedRole != null) {
            String name = roleNameField.getText();
            String description = roleDescriptionField.getText();
            roleService.updateRole(selectedRole.getIdRole(), name, description);
            loadRoles();
        }
    }

    @FXML
    private void handleDeleteRole() {
        Role selectedRole = roleListView.getSelectionModel().getSelectedItem();
        if (selectedRole != null) {
            roleService.deleteRole(selectedRole.getIdRole());
            loadRoles();
        }
    }

    @FXML
    private void addRole() {
        ComboBox<Role> comboBox = new ComboBox<>(FXCollections.observableArrayList(roleService.getAllRoles()));
        comboBox.setPrefHeight(31);
        comboBox.setPrefWidth(281);
        comboBox.setCellFactory(param -> new ListCell<Role>() {
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
        comboBox.setButtonCell(new ListCell<Role>() {
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
        roleParentVBox.getChildren().add(comboBox);
    }
    @FXML
    private void handleAddRoleHierarchy() {
        Role selectedRole = roleListView.getSelectionModel().getSelectedItem();
        if (selectedRole != null) {
            for (Node node : roleParentVBox.getChildren()) {
                if (node instanceof ComboBox) {
                    ComboBox<Role> comboBox = (ComboBox<Role>) node;
                    Role parentRole = comboBox.getValue();
                    if (parentRole != null) {
                        roleService.addRoleHierarchy(parentRole.getIdRole(), selectedRole.getIdRole());
                    }
                }
            }
        }
    }
    @FXML
    private void handleRemoveRoleHierarchy() {
        Role selectedRole = roleListView.getSelectionModel().getSelectedItem();
        if (selectedRole != null) {
            for (Node node : roleParentVBox.getChildren()) {
                if (node instanceof ComboBox) {
                    ComboBox<Role> comboBox = (ComboBox<Role>) node;
                    Role parentRole = comboBox.getValue();
                    if (parentRole != null) {
                        roleService.removeRoleHierarchy(parentRole.getIdRole(), selectedRole.getIdRole());
                    }
                }
            }
        }
    }


    @FXML
    private void removeRole() {
        if (!roleParentVBox.getChildren().isEmpty()) {
            roleParentVBox.getChildren().remove(roleParentVBox.getChildren().size() - 1);
        }
    }

    void loadRoles() {
        try {
            List<Role> roleList = roleService.getAllRoles();
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
