package com.nikola.bordercrossingsimulator.models;

import com.nikola.bordercrossingsimulator.Main;
import com.nikola.bordercrossingsimulator.controllers.SimulationController;
import com.nikola.bordercrossingsimulator.models.terminal.*;
import com.nikola.bordercrossingsimulator.models.vehicle.Vehicle;
import com.nikola.bordercrossingsimulator.models.vehicle.Car;
import com.nikola.bordercrossingsimulator.models.vehicle.Truck;
import com.nikola.bordercrossingsimulator.models.vehicle.Bus;
import javafx.application.Platform;


import java.io.File;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Collections;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.logging.Level;

import static java.lang.Thread.sleep;

public class Simulation {
    public static  int endOfLane = 49;

    private final static CopyOnWriteArrayList<Terminal> policeTerminals = new CopyOnWriteArrayList<>();
    private final static CopyOnWriteArrayList<Terminal> customsTerminals = new CopyOnWriteArrayList<>();
    private final static CopyOnWriteArrayList<Vehicle> terminalQueue = new CopyOnWriteArrayList<>();
    private final static ArrayList<Vehicle> rejected = new ArrayList<>();
    private final static ArrayList<Vehicle> passed = new ArrayList<>();
    private static final AtomicBoolean finished = new AtomicBoolean(false);
    private static final CopyOnWriteArrayList<Vehicle> lane = new CopyOnWriteArrayList<>();
    private static final AtomicBoolean runState = new AtomicBoolean(true);
    private final Path vehiclesTextLogPath;
    private final Path vehiclesBinaryLogPath;

    private final SimulationController simulationController;
    private final TerminalStatusWatcher terminalStatusWatcher;
    private Clock clock;

    public Simulation(SimulationController simulationController) {
        this.simulationController = simulationController;
        vehiclesTextLogPath = Path.of("data" + File.separator + "vehicle_text_log" + System.nanoTime() + ".txt");
        vehiclesBinaryLogPath = Path.of("data" + File.separator + "vehicle_binary_log" + System.nanoTime() + ".txt");

        initTerminals();
        initVehicles();
        ArrayList<Terminal> terminals = new ArrayList<>();
        terminals.addAll(policeTerminals);
        terminals.addAll(customsTerminals);
        Path terminalStateFilePath = Path.of("data" + File.separator + "terminals.txt");

        this.terminalStatusWatcher = new TerminalStatusWatcher(terminalStateFilePath, terminals);

    }

    public Path getVehiclesTextLogPath(){return vehiclesTextLogPath;}
    public static boolean isFinished() {
        return finished.get();
    }

    public static void tryMove(Vehicle vehicle) {
        synchronized (lane) {
            int currentPosition = vehicle.getPosition();
            if (currentPosition + 1 < lane.size() && lane.get(currentPosition + 1) == null && currentPosition >= 0) {
                lane.set(currentPosition, null);
                lane.set(++currentPosition, vehicle);
                vehicle.setPosition(currentPosition);

            }
        }
    }

    public static boolean isInTerminalQueue(Vehicle vehicle) {
        return terminalQueue.contains(vehicle);
    }

    public static void addToTerminalQueue(Vehicle vehicle) {
        terminalQueue.add(vehicle);
    }

    public static void removeFromLane(Vehicle vehicle) {
        synchronized (lane) {
            lane.remove(vehicle);
            --endOfLane;
        }
    }

    /**
     * Try to find an empty appropriate terminal and return it while setting the occupy flag to true.
     * Returns null if no appropriate terminal is found
     * Terminal occupied flag needs to be reset in the caller of this method
     *
     * @param vehicle
     * @return terminal or null
     */

    public static synchronized Terminal tryGetPoliceTerminal(Vehicle vehicle) {
        for (Terminal terminal : policeTerminals) {
            if (terminal.getStatus() && !terminal.isOccupied() && terminal.getTerminalCategory() == vehicle.getVehicleCategory()) {
                terminal.setOccupied(true);
                return terminal;
            }
        }
        return null;
    }

    public static synchronized CopyOnWriteArrayList<Vehicle> getLane(){return lane;}
    public static void addToRejected(Vehicle vehicle) {
        synchronized (rejected) {
            rejected.add(vehicle);
            removeFromTerminalQueue(vehicle);

        }
    }

    public static void addToPassed(Vehicle vehicle) {
        synchronized (passed) {
            passed.add(vehicle);
            removeFromTerminalQueue(vehicle);
        }
    }

    public static void removeFromTerminalQueue(Vehicle vehicle) {
        terminalQueue.remove(vehicle);
    }

    public static Terminal tryGetCustomsTerminal(Vehicle vehicle) {
        for (Terminal terminal : customsTerminals) {
            if (terminal.getStatus() && !terminal.isOccupied() && terminal.getTerminalCategory() == vehicle.getVehicleCategory()) {
                terminal.setOccupied(true);
                return terminal;
            }
        }
        return null;
    }

    public  void changeRunState() {
        Clock.changeState();
        synchronized (lane) {
         lane.forEach(Vehicle::changeState);
        }
    }



    public void start() {
        terminalStatusWatcher.start();

        synchronized (lane) {
            //Platform.runLater(() ->simulationController.setupLane(lane));

            for (Vehicle vehicle : lane) {
                vehicle.start();
            }
        }
        clock = new Clock(this.simulationController);

        clock.start();
        System.out.println(lane.size());

        run();
    }

    public void run() {

        while (!finished.get()) {
          Platform.runLater(simulationController::updateGui);

            try {
                sleep(100);
            } catch (InterruptedException e) {
                Main.logger.log(Level.WARNING, e.getMessage());
            }
            finished.set(finishedMoving());
        }
    }

    private boolean finishedMoving() {
        boolean laneFinish, terminalsFinish;
        synchronized (lane) {
            laneFinish = lane.isEmpty();
        }
        synchronized (terminalQueue) {
            terminalsFinish = terminalQueue.isEmpty();
        }
        return laneFinish && terminalsFinish;
    }

    private void initTerminals() {
        policeTerminals.add(new PoliceTerminal(TerminalCategory.PASSENGER));
        policeTerminals.add(new PoliceTerminal(TerminalCategory.PASSENGER));
        policeTerminals.add(new PoliceTerminal(TerminalCategory.CARGO));

        customsTerminals.add(new CustomsTerminal(TerminalCategory.PASSENGER));
        customsTerminals.add(new CustomsTerminal(TerminalCategory.CARGO));
    }

    private void initVehicles() {

        for (int i = 0; i < 35; ++i) {
            lane.add(new Car(vehiclesTextLogPath, vehiclesBinaryLogPath,  simulationController));
        }
        for (int i = 0; i < 10; ++i) {
            lane.add(new Truck(vehiclesTextLogPath, vehiclesBinaryLogPath, simulationController));
        }
        for (int i = 0; i < 5; ++i) {
            lane.add(new Bus(vehiclesTextLogPath, vehiclesBinaryLogPath, simulationController));
        }
        Collections.shuffle(lane);
        for (int i = 0; i < 50; ++i) {
            lane.get(i).setPosition(i);
        }
    }
}
