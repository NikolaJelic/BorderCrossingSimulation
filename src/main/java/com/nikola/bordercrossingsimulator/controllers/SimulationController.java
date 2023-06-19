package com.nikola.bordercrossingsimulator.controllers;


import com.nikola.bordercrossingsimulator.models.Simulation;
import com.nikola.bordercrossingsimulator.models.vehicle.Vehicle;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.text.Text;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

public class SimulationController {
    @FXML
    public Label elapsedTime;
    @FXML
    public Button showAllButton;
    @FXML
    public ListView<Label> visibleLane;
    @FXML
    public Text crossingLog;
    @FXML
    public Button pauseButton;
    @FXML
    public Label policeFirstLabel;
    @FXML
    public Label policeSecondLabel;
    @FXML
    public Label policeThirdLabel;
    @FXML
    public Label customsFirstLabel;
    @FXML
    public Label customsSecondLabel;
    @FXML
    public Button crossedButton;
    @FXML
    public Button rejectedButton;
    private Simulation simulation;


    public void showAllVehicles(ActionEvent actionEvent) {
    }

    public void updateGui() {
        final CopyOnWriteArrayList<Vehicle> lane = Simulation.getLane();
        int size = lane.size();
        int startIndex = Math.max(0, size - 5);
        int endIndex = Math.min(size, startIndex + 5);
        List<Vehicle> vLane = new ArrayList<>(lane.subList(startIndex, endIndex));
        visibleLane.getItems().clear();

        for (Vehicle vehicle : vLane) {
            visibleLane.getItems().add(new Label(String.valueOf( vehicle.getVehicleId())));
        }
    }
    public void updateTime(long time) {
        elapsedTime.setText(String.valueOf(time));
    }


    public void switchState(ActionEvent actionEvent) {
        simulation.changeRunState();
    }

    public void showCrossed(ActionEvent actionEvent) {

    }

    public void showRejected(ActionEvent actionEvent) {

    }

    public void setSimulationRunner(Simulation simulation) {
        try {
            this.simulation = simulation;
            new Thread(() -> this.simulation.start()).start();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }
}
