package com.nikola.bordercrossingsimulator.controllers;

import com.nikola.bordercrossingsimulator.Main;
import com.nikola.bordercrossingsimulator.models.vehicle.Vehicle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.stream.Stream;

public class ProblemVehiclesController {
    private final Path logPath;
    @FXML
    public Button rejectedButton;
    @FXML
    public Button passedButton;
    @FXML
    public ListView<Label> detailsList;

    public ProblemVehiclesController(Path logPath) {
        this.logPath = logPath;
    }


    public void showRejectedVehicles(ActionEvent actionEvent) {
        detailsList.getItems().clear();

        ArrayList<String> vehicleInfo = readLogFile();
        vehicleInfo.forEach((log) -> {
            if(log.contains("rejected")) {
                detailsList.getItems().add(new Label(log));
            }
        });
    }

    public void showPassedWithProblems(ActionEvent actionEvent) {
        detailsList.getItems().clear();

        ArrayList<String> vehicleInfo = readLogFile();
        vehicleInfo.forEach((log) -> {
            if(!log.contains("rejected")) {
                detailsList.getItems().add(new Label(log));
            }
        });
    }

    private ArrayList<String> readLogFile() {
        ArrayList<String> ret = new ArrayList<>();
        Map<String, StringBuilder> lineMap = new HashMap<>();
        try {
            Vehicle.loggingSemaphore.acquire();
            try (Stream<String> lines = Files.lines(logPath)) {
                lines.forEach((line) -> {
                    String[] split = line.split(" ", 2);
                    if (!lineMap.containsKey(split[0])) {
                        lineMap.put(split[0], new StringBuilder(line));
                    } else {
                        StringBuilder existingLine = lineMap.get(split[0]);
                        existingLine.append(" ").append(split[1]);
                    }
                });
            }
        } catch (Exception e) {
            Main.logger.log(Level.WARNING, e.getMessage());
        }finally {
            Vehicle.loggingSemaphore.release();
        }
        lineMap.forEach((key, value) -> {
            ret.add(value.toString());
        });
        return ret;
    }


}
