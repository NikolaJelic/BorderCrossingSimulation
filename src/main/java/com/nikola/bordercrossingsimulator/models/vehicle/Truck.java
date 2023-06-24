package com.nikola.bordercrossingsimulator.models.vehicle;

import com.nikola.bordercrossingsimulator.Main;
import com.nikola.bordercrossingsimulator.controllers.SimulationController;
import com.nikola.bordercrossingsimulator.models.terminal.TerminalCategory;
import com.nikola.bordercrossingsimulator.models.transportable.Cargo;

import java.nio.file.Path;
import java.util.logging.Level;

public class Truck extends Vehicle {
    private final Cargo cargo;

    public Truck(Path bannedPassengersLogPath, Path vehiclesBinaryLogPath, SimulationController simulationController) {
        super(3, 500, TerminalCategory.CARGO, bannedPassengersLogPath, vehiclesBinaryLogPath, simulationController);
        cargo = new Cargo();
    }

    @Override
    protected boolean inspectCustoms() {
        try {
            sleep(2000);
        } catch (InterruptedException e) {
            Main.logger.log(Level.WARNING, e.getMessage());
        }
        if (cargo.canPassInspection()) {
            return true;
        } else {
            rejectedPassengers.add(passengers.get(0));
            return false;
        }

    }

    @Override
    public String typeToString() {
        return "Truck";
    }
}
