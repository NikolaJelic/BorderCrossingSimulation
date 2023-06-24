package com.nikola.bordercrossingsimulator.models.vehicle;

import com.nikola.bordercrossingsimulator.Main;
import com.nikola.bordercrossingsimulator.controllers.SimulationController;
import com.nikola.bordercrossingsimulator.models.terminal.TerminalCategory;

import java.nio.file.Path;
import java.util.logging.Level;

public class Car extends Vehicle {
    public Car(Path bannedPassengersLogPath, Path vehiclesBinaryLogPath, SimulationController simulationController) {
        super(5, 500, TerminalCategory.PASSENGER, bannedPassengersLogPath, vehiclesBinaryLogPath, simulationController);
    }

    @Override
    public boolean inspectCustoms() {
        try {
            sleep(2000);
        } catch (Exception e) {
            Main.logger.log(Level.WARNING, e.fillInStackTrace().toString());
        }
        return true;
    }


    @Override
    public String typeToString() {
        return "Car";
    }
}
