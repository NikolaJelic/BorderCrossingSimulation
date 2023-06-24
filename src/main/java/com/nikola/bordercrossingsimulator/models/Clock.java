package com.nikola.bordercrossingsimulator.models;

import com.nikola.bordercrossingsimulator.Main;
import com.nikola.bordercrossingsimulator.controllers.SimulationController;
import javafx.application.Platform;

import java.util.logging.Level;

public class Clock extends Thread {
    private static final Object lock = new Object();
    private static boolean isRunning = true;
    private final SimulationController simulationController;
    private long elapsedTime = 0;

    public Clock(SimulationController simulationController) {
        this.simulationController = simulationController;
        isRunning = true;
    }

    public static void changeState() {
        synchronized (lock) {
            isRunning = !isRunning;
        }
    }

    @Override
    public void run() {
        while (!Simulation.isFinished()) {
            try {
                sleep(1000);
            } catch (InterruptedException e) {
                Main.logger.log(Level.WARNING, e.getMessage());
            }
            if (isRunning) {
                ++elapsedTime;
                Platform.runLater(() -> simulationController.updateTime(elapsedTime));

            }
        }
    }

}
