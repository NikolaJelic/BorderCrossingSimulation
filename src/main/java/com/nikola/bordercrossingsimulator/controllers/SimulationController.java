package com.nikola.bordercrossingsimulator.controllers;


import com.nikola.bordercrossingsimulator.Main;
import com.nikola.bordercrossingsimulator.models.Simulation;
import com.nikola.bordercrossingsimulator.models.terminal.Terminal;
import com.nikola.bordercrossingsimulator.models.vehicle.Vehicle;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.TextField;
import javafx.scene.paint.Color;
import javafx.scene.text.Text;
import javafx.stage.Stage;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.logging.Level;

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
    public Button finishedButton;
    public TextField searchInputField;
    public Text vehicleInfo;
    OtherVehiclesController otherVehiclesController = new OtherVehiclesController();
    ProblemVehiclesController problemVehiclesController;
    private Simulation simulation;

    public synchronized void updateLog(String log) {
        String previous = crossingLog.getText();
        String add = previous + '\n' + log;
        crossingLog.setText(add);
    }


    public void showAllVehicles(ActionEvent actionEvent) {
        try {

            FXMLLoader loader = new FXMLLoader(getClass().getResource(File.separator +  "com" +File.separator + "nikola"+File.separator +"bordercrossingsimulator"+File.separator +"otherVehicles.fxml"));
            loader.setController(otherVehiclesController);
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Other vehicles");
            stage.setScene(new Scene(root, 600, 150));
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            Main.logger.log(Level.WARNING, e.getMessage());
        }
    }

    public void updateGui() {
        final CopyOnWriteArrayList<Vehicle> lane = Simulation.getLane();
        int size = lane.size();
        int startIndex = Math.max(0, size - 5);
        int endIndex = Math.min(size, startIndex + 5);
        List<Vehicle> vLane = new ArrayList<>(lane.subList(startIndex, endIndex));
        visibleLane.getItems().clear();
        ArrayList<Vehicle> others = new ArrayList<>(lane.subList(0, startIndex));
        Platform.runLater(() -> otherVehiclesController.updateList(others));
        vLane.forEach(vehicle -> {
            Label l = new Label(vehicle.typeToString() + "[" + vehicle.getVehicleId() + "]");
            Color textColor = switch (vehicle.typeToString()) {
                case "Car" -> Color.RED;
                case "Bus" -> Color.BLUE;
                case "Truck" -> Color.GREEN;
                default -> throw new IllegalStateException("Unexpected value: " + vehicle.typeToString());
            };
            l.setTextFill(textColor);
            visibleLane.getItems().add(l);
        });

    }

    public void updateTime(long time) {
        elapsedTime.setText(String.valueOf(time));
    }


    public void switchState(ActionEvent actionEvent) {
        simulation.changeRunState();
    }


    public void setSimulationRunner(Simulation simulation) {
        try {
            this.simulation = simulation;
            new Thread(() -> this.simulation.start()).start();
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
    }

    public synchronized void updatePoliceTerminal(Vehicle vehicle, Terminal terminal) {
        switch (terminal.getTerminalId()) {
            case 1 -> policeFirstLabel.setText((vehicle.typeToString() + "[" + vehicle.getVehicleId() + "]"));
            case 2 -> policeSecondLabel.setText((vehicle.typeToString() + "[" + vehicle.getVehicleId() + "]"));
            case 3 -> policeThirdLabel.setText((vehicle.typeToString() + "[" + vehicle.getVehicleId() + "]"));
        }
    }

    public synchronized void resetPoliceTerminal(Terminal terminal) {
        switch (terminal.getTerminalId()) {
            case 1 -> policeFirstLabel.setText("");
            case 2 -> policeSecondLabel.setText("");
            case 3 -> policeThirdLabel.setText("");
        }
    }

    public synchronized void updateCustomsTerminal(Vehicle vehicle, Terminal terminal) {
        switch (terminal.getTerminalId()) {
            case 1 -> customsFirstLabel.setText((vehicle.typeToString() + "[" + vehicle.getVehicleId() + "]"));
            case 2 -> customsSecondLabel.setText((vehicle.typeToString() + "[" + vehicle.getVehicleId() + "]"));
        }
    }

    public synchronized void resetCustomsTerminal(Terminal terminal) {
        switch (terminal.getTerminalId()) {
            case 1 -> customsFirstLabel.setText("");
            case 2 -> customsSecondLabel.setText("");
        }
    }

    public void showFinished(ActionEvent actionEvent) {
        try {
            problemVehiclesController = new ProblemVehiclesController(simulation.getVehiclesTextLogPath());
            FXMLLoader loader = new FXMLLoader(getClass().getResource( File.separator +  "com" +File.separator + "nikola"+File.separator +"bordercrossingsimulator"+File.separator +"problemVehicles.fxml"));
            loader.setController(problemVehiclesController);
            Parent root = loader.load();
            Stage stage = new Stage();
            stage.setTitle("Problem vehicles");
            stage.setScene(new Scene(root, 600, 400));
            stage.setResizable(false);
            stage.show();
        } catch (Exception e) {
            Main.logger.log(Level.WARNING, e.getMessage());
        }
    }

    public void searchByID(ActionEvent actionEvent) {
        String input = searchInputField.getText();
        if (!input.isEmpty()) {
            try {
                int vehicleId = Integer.parseInt(input);
                ArrayList<Vehicle> allVehicles = Simulation.getAllVehicles();
                Optional<Vehicle> targetVehicle = allVehicles.stream().filter(vehicle -> vehicle.getVehicleId() == vehicleId).findFirst();
                targetVehicle.ifPresent(vehicle -> vehicleInfo.setText(vehicle.toString()));
            } catch (Exception e) {
                Main.logger.log(Level.WARNING, e.getMessage());
            }
        }
    }
}
