package tn.bfpme.utils;

import javafx.scene.control.TreeView;
import javafx.scene.control.TreeCell;
import javafx.scene.control.TreeItem;

import javafx.scene.paint.Color;
import tn.bfpme.models.User;

public class ColoredTreeCell extends TreeCell<User> {

    @Override
    protected void updateItem(User item, boolean empty) {
        super.updateItem(item, empty);

        if (empty || item == null) {
            setText(null);
            setGraphic(null);
            setStyle("");
        } else {
            setText(item.getNom() + " " + item.getPrenom());

            int level = getTreeItemLevel(getTreeItem());
            Color color = getColorForLevel(level);
            setStyle("-fx-background-color: " + toHexString(color) + ";");
        }
    }

    private int getTreeItemLevel(TreeItem<User> treeItem) {
        int level = 0;
        while ((treeItem = treeItem.getParent()) != null) {
            level++;
        }
        return level;
    }

    private Color getColorForLevel(int level) {
        switch (level) {
            case 0:
                return Color.LIGHTBLUE;
            case 1:
                return Color.LIGHTGREEN;
            case 2:
                return Color.LIGHTYELLOW;
            default:
                return Color.LIGHTCORAL;
        }
    }

    private String toHexString(Color color) {
        int r = (int) (255 * color.getRed());
        int g = (int) (255 * color.getGreen());
        int b = (int) (255 * color.getBlue());
        return String.format("#%02X%02X%02X", r, g, b);
    }
}

