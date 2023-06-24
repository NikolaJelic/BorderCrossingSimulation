package com.nikola.bordercrossingsimulator;

import com.nikola.bordercrossingsimulator.controllers.SimulationController;
import com.nikola.bordercrossingsimulator.models.Simulation;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.logging.FileHandler;
import java.util.logging.Handler;
import java.util.logging.Logger;

public class Main extends Application {

    public static final String LOG_PATH = "data" + File.separator + "logs" + File.separator + "simulation.log";
    public static final Logger logger;
    public static final Handler handler;

    static {
        try {
            handler = new FileHandler(LOG_PATH);
            logger = Logger.getLogger(Main.class.getName());
            logger.addHandler(handler);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

    }

    public static void main(String[] args) {
        launch();
    }

    @Override
    public void start(Stage primaryStage) throws Exception {


        FXMLLoader fxmlLoader = new FXMLLoader(Main.class.getResource(File.separator +  "com" +File.separator + "nikola"+File.separator +"bordercrossingsimulator"+File.separator +"simulation.fxml"));
        Parent root = fxmlLoader.load();
        primaryStage.setTitle("Border Crossing");
        primaryStage.setScene(new Scene(root, 800, 600));
        SimulationController simulationController = fxmlLoader.getController();
        Simulation simulationRunner = new Simulation(simulationController);
        simulationController.setSimulationRunner(simulationRunner);
        primaryStage.setResizable(false);
        primaryStage.show();
        primaryStage.setOnCloseRequest(e -> {
            System.exit(0);
        });
    }
}