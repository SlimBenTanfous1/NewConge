package tn.bfpme.utils;

import javafx.scene.image.Image;
import javafx.stage.Stage;

import java.util.*;

public class StageManager {
    private static final Set<Stage> stages = Collections.synchronizedSet(new HashSet<>());
    private static final Map<String, Stage> stageMap = Collections.synchronizedMap(new HashMap<>());
    private static final String ICON_PATH = "/assets/imgs/logo_bfpme.png";
    private static final Image ICON_IMAGE = new Image(StageManager.class.getResourceAsStream(ICON_PATH));

    public static void addStage(Stage stage) {
        stages.add(stage);
    }

    public static void addStage(String name, Stage stage) {
        stage.getIcons().add(ICON_IMAGE);
        stageMap.put(name, stage);
        stages.add(stage);
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
}
