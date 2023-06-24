package com.nikola.bordercrossingsimulator.controllers;

import com.nikola.bordercrossingsimulator.models.vehicle.Vehicle;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.paint.Color;

import java.util.ArrayList;
import java.util.Collections;

public class OtherVehiclesController {
    public ListView<Label> vehicleList;

    public void updateList(ArrayList<Vehicle> vehicles) {
        if(vehicleList != null){
            vehicleList.getItems().clear();
        }
        vehicles.forEach(vehicle -> {
            Label l = new Label(vehicle.typeToString() + "[" + vehicle.getVehicleId() + "]");
            Color textColor = switch (vehicle.typeToString()) {
                case "Car" -> Color.RED;
                case "Bus" -> Color.BLUE;
                case "Truck" -> Color.GREEN;
                default -> throw new IllegalStateException("Unexpected value: " + vehicle.typeToString());
            };
            l.setTextFill(textColor);
            if (vehicleList != null) {
                vehicleList.getItems().add(l);
            }
        });
    }

}
