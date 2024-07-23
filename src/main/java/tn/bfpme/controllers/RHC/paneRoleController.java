package tn.bfpme.controllers.RHC;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.Initializable;
import javafx.geometry.Insets;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.VBox;
import tn.bfpme.models.Role;
import tn.bfpme.services.ServiceRole;

import java.net.URL;
import java.util.*;

public class paneRoleController implements Initializable {
    @FXML
    private TextArea roleDescriptionField;
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
    
    @FXML
    private void handleAddRole() {
        String name = roleNameField.getText();
        String description = roleDescriptionField.getText();
        if (roleNameField.getText().isEmpty() || roleDescriptionField.getText().isEmpty()) {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Champs requis non remplis", "Veuillez remplir toutes les informations nécessaires.");
            return;
        }
        Role newRole = roleService.addRole2(name, description);
        if (newRole != null) {
            for (Node node : roleParentVBox.getChildren()) {
                if (node instanceof HBox) {
                    HBox hBox = (HBox) node;
                    for (Node childNode : hBox.getChildren()) {
                        if (childNode instanceof ComboBox) {
                            ComboBox<Role> comboBox = (ComboBox<Role>) childNode;
                            Role parentRole = comboBox.getValue();
                            if (parentRole != null) {
                                roleService.addRoleHierarchy(parentRole.getIdRole(), newRole.getIdRole());
                            }
                        }
                    }
                }
            }
            showSuccess("Message de succès", "Le rôle a été ajouté avec succès.");
            loadRoles();
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Ajout du rôle échoué", "Le rôle n'a pas pu être ajouté.");
        }
    }

    @FXML
    private void handleEditRole() {
        Role selectedRole = roleListView.getSelectionModel().getSelectedItem();
        if (selectedRole != null) {
            String name = roleNameField.getText();
            String description = roleDescriptionField.getText();
            if (roleNameField.getText().isEmpty() || roleDescriptionField.getText().isEmpty()) {
                showAlert(Alert.AlertType.ERROR, "Erreur", "Champs requis non remplis", "Veuillez remplir toutes les informations nécessaires.");
                return;
            }
            roleService.updateRole(selectedRole.getIdRole(), name, description);
            List<Role> currentParents = roleService.getRoleParents2(selectedRole.getIdRole());
            Set<Role> newParentsSet = new HashSet<>();
            for (Node node : roleParentVBox.getChildren()) {
                if (node instanceof HBox) {
                    HBox hBox = (HBox) node;
                    for (Node childNode : hBox.getChildren()) {
                        if (childNode instanceof ComboBox) {
                            ComboBox<Role> comboBox = (ComboBox<Role>) childNode;
                            Role parentRole = comboBox.getValue();
                            if (parentRole != null) {
                                newParentsSet.add(parentRole);
                            }
                        }
                    }
                }
            }
            List<Role> newParents = new ArrayList<>(newParentsSet);
            for (Role currentParent : currentParents) {
                if (!newParents.contains(currentParent)) {
                    roleService.removeRoleHierarchy(currentParent.getIdRole(), selectedRole.getIdRole());
                }
            }
            for (Role newParent : newParents) {
                if (!currentParents.contains(newParent)) {
                    roleService.addRoleHierarchy(newParent.getIdRole(), selectedRole.getIdRole());
                }
            }
            showSuccess("Message de succès", "Le rôle a été modifié avec succès");
        } else {
            showAlert(Alert.AlertType.ERROR, "Erreur", "Aucun rôle sélectionné", "Veuillez sélectionner un rôle à modifier.");
        }
        loadRoles();
    }

    @FXML
    private void handleDeleteRole() {
        Role selectedRole = roleListView.getSelectionModel().getSelectedItem();
        if (selectedRole != null) {
            roleService.deleteRole(selectedRole.getIdRole());
            loadRoles();
            showSuccess("Message de success", "Le role à été supprimer avec success");
        }
    }

    @FXML
    private void addRole() {
        String roleNameToExclude = roleNameField.getText();
        Role selectedRole = roleListView.getSelectionModel().getSelectedItem();
        List<Role> allRoles = roleService.getAllRoles();
        List<Role> filteredRoles = new ArrayList<>();
        for (Role role : allRoles) {
            if (selectedRole != null && role.getIdRole() == selectedRole.getIdRole()) {
                continue;
            }
            if (role.getNom().equals(roleNameToExclude)) {
                continue;
            }
            filteredRoles.add(role);
        }
        ComboBox<Role> comboBox = new ComboBox<>(FXCollections.observableArrayList(filteredRoles));
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
        Button removeButton = new Button("-");
        removeButton.setPrefHeight(25);
        removeButton.setPrefWidth(25);
        removeButton.getStyleClass().addAll("btn-primary", "FontSize-12", "RobotoRegular");
        removeButton.setOnAction(e -> roleParentVBox.getChildren().remove(comboBox.getParent()));
        HBox hBox = new HBox(comboBox, removeButton);
        hBox.setSpacing(5);
        roleParentVBox.getChildren().add(hBox);
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

    private void populateParentRolesComboBoxes(int roleId) {
        roleParentVBox.getChildren().clear();
        List<Role> parentRoles = roleService.getParentRoles2(roleId);
        List<Role> allRoles = roleService.getAllRoles();
        allRoles.removeIf(role -> role.getIdRole() == roleId);
        ObservableList<Role> allRolesObservable = FXCollections.observableArrayList(allRoles);
        for (Role parentRole : parentRoles) {
            ComboBox<Role> comboBox = new ComboBox<>(allRolesObservable);
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
            HBox hBox = new HBox(comboBox);
            hBox.setSpacing(5);
            Button removeButton = new Button("-");
            removeButton.setPrefHeight(25);
            HBox.setMargin(removeButton, new Insets(2, 0, 0, 0)); // Top margin of 2
            removeButton.setPrefWidth(25);
            removeButton.getStyleClass().addAll("btn-primary", "FontSize-12", "RobotoRegular");
            removeButton.setOnAction(e -> roleParentVBox.getChildren().remove(hBox));
            hBox.getChildren().add(removeButton);
            roleParentVBox.getChildren().add(hBox);
        }
    }

    private void showAlert(Alert.AlertType alertType, String title, String header, String content) {
        Alert alert = new Alert(alertType);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        alert.showAndWait();
    }

    protected void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("Error");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    protected void showSuccess(String message, String titre) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle(titre);
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }
}
