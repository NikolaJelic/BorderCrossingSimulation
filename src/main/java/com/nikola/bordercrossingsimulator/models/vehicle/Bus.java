package com.nikola.bordercrossingsimulator.models.vehicle;

import com.nikola.bordercrossingsimulator.Main;
import com.nikola.bordercrossingsimulator.controllers.SimulationController;
import com.nikola.bordercrossingsimulator.models.passenger.Passenger;
import com.nikola.bordercrossingsimulator.models.terminal.TerminalCategory;

import java.nio.file.Path;
import java.util.logging.Level;

public class Bus extends Vehicle {
    public Bus(Path bannedPassengersLogPath, Path vehiclesBinaryLogPath, SimulationController simulationController) {
        super(52, 100, TerminalCategory.PASSENGER, bannedPassengersLogPath, vehiclesBinaryLogPath, simulationController);
    }

    @Override
    public boolean inspectCustoms() {
        boolean ret = true;
        try {
            for (Passenger passenger : passengers) {
                if (!passenger.checkBaggage()) {
                    passengers.remove(passenger);
                    sleep(100);
                    rejectedPassengers.add(passenger);
                    if (passenger.getIsDriver()) {
                        travelState = TravelState.REJECTED;
                        ret = false;
                    }
                }
            }
        } catch (Exception e) {
            Main.logger.log(Level.WARNING, e.fillInStackTrace().toString());
        }
        return ret;
    }

    @Override
    public String typeToString() {
        return "Bus";
    }
}
