package tn.bfpme.utils;

import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.*;

public class StageManager {
    private static final Set<Stage> stages = Collections.synchronizedSet(new HashSet<>());
    private static final Map<String, Stage> stageMap = Collections.synchronizedMap(new HashMap<>());
    private static final String ICON_PATH = "/assets/imgs/logo_bfpme.png";
    private static final Image ICON_IMAGE = new Image(StageManager.class.getResourceAsStream(ICON_PATH));
    private static double lastStageWidth = 1340; // Default width
    private static double lastStageHeight = 830; // Default height

    public static void addStage(Stage stage) {
        setStageSize(stage);
        stages.add(stage);
        addSizeListeners(stage);
        updateLastStageSize(stage);
    }

    public static void addStage(String name, Stage stage) {
        setStageSize(stage);
        stage.getIcons().add(ICON_IMAGE);
        stageMap.put(name, stage);
        stages.add(stage);
        addSizeListeners(stage);
        updateLastStageSize(stage);
    }

    public static void removeStage(Stage stage) {
        stages.remove(stage);
        stageMap.values().remove(stage);
    }

    public static void removeStage(String name) {
        Stage stage = stageMap.remove(name);
        if (stage != null) {
            stages.remove(stage);
            stage.close();
        }
    }

    public static Stage getStage(String name) {
        return stageMap.get(name);
    }

    public static void closeAllStages() {
        for (Stage stage : new HashSet<>(stages)) {
            if (stage != null) {
                stage.close();
            }
        }
        stages.clear();
        stageMap.clear();
    }
    private static void setStageSize(Stage stage) {
        stage.setWidth(lastStageWidth);
        stage.setHeight(lastStageHeight);
    }

    private static void updateLastStageSize(Stage stage) {
        lastStageWidth = stage.getWidth();
        lastStageHeight = stage.getHeight();
    }

    private static void addSizeListeners(Stage stage) {
        stage.widthProperty().addListener((obs, oldVal, newVal) -> {
            lastStageWidth = newVal.doubleValue();
        });

        stage.heightProperty().addListener((obs, oldVal, newVal) -> {
            lastStageHeight = newVal.doubleValue();
        });
    }

}
