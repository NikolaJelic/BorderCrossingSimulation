package com.nikola.bordercrossingsimulator.models.vehicle;

import com.nikola.bordercrossingsimulator.Main;
import com.nikola.bordercrossingsimulator.controllers.SimulationController;
import com.nikola.bordercrossingsimulator.models.passenger.Passenger;
import com.nikola.bordercrossingsimulator.models.terminal.TerminalCategory;
import com.nikola.bordercrossingsimulator.models.transportable.Cargo;

import java.nio.file.Path;
import java.util.ArrayList;
import java.util.logging.Level;

public class Truck extends Vehicle{
    private final Cargo cargo;

    public Truck(Path bannedPassengersLogPath, SimulationController simulationController) {
        super(3, 500, TerminalCategory.CARGO, bannedPassengersLogPath, simulationController);
        cargo = new Cargo();
    }

    @Override
    protected boolean inspectCustoms() {
        try {
            sleep(2000);
        } catch (InterruptedException e) {
            Main.logger.log(Level.WARNING, e.getMessage());
        }
        if(cargo.canPassInspection()){
            return true;
        }else{
            ArrayList<Passenger> out = new ArrayList<>();
            out.add(passengers.get(0));
            logCrossingReport(out);
            return false;
        }

    }

    @Override
    protected String typeToString() {
        return "Truck";
    }
}
