package com.nikola.bordercrossingsimulator.models.vehicle;

import com.nikola.bordercrossingsimulator.Main;
import com.nikola.bordercrossingsimulator.controllers.SimulationController;
import com.nikola.bordercrossingsimulator.models.Simulation;
import com.nikola.bordercrossingsimulator.models.passenger.Passenger;
import com.nikola.bordercrossingsimulator.models.terminal.Terminal;
import com.nikola.bordercrossingsimulator.models.terminal.TerminalCategory;
import javafx.application.Platform;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;

import static com.nikola.bordercrossingsimulator.models.Simulation.endOfLane;


public abstract class Vehicle extends Thread implements Serializable {
    private static final Semaphore loggingSemaphore = new Semaphore(1);
    private static final Semaphore policeSemaphore = new Semaphore(3);
    private static final Semaphore customsSemaphore = new Semaphore(2);

    private static int vehicleCounter = 1;
    private final int vehicleId;
    private final int policeWaitDuration; //in milliseconds
    private final TerminalCategory vehicleCategory;
    protected TravelState travelState = TravelState.IN_LANE;
    private final Path bannedPassengersLogPath;
    private final int maxCapacity;
    protected final CopyOnWriteArrayList<Passenger> passengers = new CopyOnWriteArrayList<>();
    protected StringBuilder crossingLog = new StringBuilder();
    private final SimulationController simulationController;
    private int position;

    private boolean isRunning = true;


    public Vehicle(int maxCapacity, int policeWaitDuration, TerminalCategory vehicleCategory, Path bannedPassengersLogPath, SimulationController simulationController) {
        this.vehicleId = vehicleCounter++;
        this.maxCapacity = maxCapacity;
        this.bannedPassengersLogPath = bannedPassengersLogPath;
        this.policeWaitDuration = policeWaitDuration;
        this.vehicleCategory = vehicleCategory;
        int passengerCount = new Random().nextInt(1, maxCapacity);
        for (int i = 0; i < passengerCount; ++i) {
            passengers.add(new Passenger(i == 0));
        }
        this.simulationController = simulationController;
    }

    public int getVehicleId(){return vehicleId;}
    public TravelState getTravelState(){return travelState;}

    public synchronized int getPosition(){ return  position;}
    public synchronized void setPosition(int position){ this.position = position;}

    @Override
    public void run(){
        while(travelState != TravelState.FINISHED && travelState != TravelState.REJECTED && !Simulation.isFinished()){
       //     awaitCondition();
            if(travelState == TravelState.IN_LANE){
                tryMove();
            }
            System.out.println(typeToString() + vehicleId + ": " + travelState + " Position: " + position);
            if(position == endOfLane && !Simulation.isInTerminalQueue(this)){
                travelState = TravelState.WAITING_FOR_POLICE;
                Simulation.addToTerminalQueue(this);


            }
            if(travelState == TravelState.WAITING_FOR_POLICE && Simulation.isInTerminalQueue(this)){
                passPoliceTerminal();
            }
            if(travelState == TravelState.WAITING_FOR_CUSTOMS && Simulation.isInTerminalQueue(this)){
                passCustomsTerminal();
            }
        }
    }

    private void passPoliceTerminal() {
        Terminal terminal = Simulation.tryGetPoliceTerminal(this);
        crossingLog = new StringBuilder();

        if(terminal != null){
            travelState = TravelState.UNDER_INSPECTION;
            Simulation.removeFromLane(this);

            crossingLog.append(typeToString()).append(" ").append(vehicleId).append(" on police terminal [").append(terminal.getTerminalId()).append("] ");
            if(inspectPolice()){
                crossingLog.append("passed police inspection with passengers: ");
                passengers.forEach((passenger) ->  crossingLog.append(" ").append(passenger.getPassengerId()).append(" "));
                crossingLog.append('\n');
                travelState = TravelState.WAITING_FOR_CUSTOMS;
            }else{
                crossingLog.append("failed police inspection.\n");
                travelState = TravelState.REJECTED;
                Simulation.addToRejected(this);
            }
            terminal.setOccupied(false);
            System.out.println(crossingLog);

        }
    }



    private void passCustomsTerminal() {
        Terminal terminal = Simulation.tryGetCustomsTerminal(this);
        crossingLog = new StringBuilder();
        if(terminal != null){
            travelState = TravelState.UNDER_INSPECTION;
            crossingLog.append(typeToString()).append(" ").append(vehicleId).append(" on customs terminal [").append(terminal.getTerminalId()).append("] ");
            if(inspectCustoms()){
                crossingLog.append("passed customs inspection with passengers: ");
                passengers.forEach((passenger) ->  crossingLog.append(" ").append(passenger.getPassengerId()).append(" "));
                crossingLog.append('\n');
                travelState = TravelState.FINISHED;
                Simulation.addToPassed(this);

            }else{
                crossingLog.append("failed customs inspection.\n");
                travelState = TravelState.REJECTED;

                Simulation.addToRejected(this);
            }
            terminal.setOccupied(false);

            System.out.println(crossingLog);

        }
    }
    protected abstract boolean inspectCustoms();
    private boolean inspectPolice() {
        ArrayList<Passenger> rejectedPassengers = new ArrayList<>();
        for(Passenger passenger : passengers){
            if(!passenger.isDocumentationValid()){
                passengers.remove(passenger);
                if(passenger.getIsDriver()){
                   travelState = TravelState.REJECTED;
                    rejectedPassengers.add(passenger);
                   return false;
                }
                rejectedPassengers.add(passenger);
            }
            try {
                sleep(policeWaitDuration);
            } catch (InterruptedException e) {
                Main.logger.log(Level.WARNING, e.getMessage());
            }
        }
        logCrossingReport(rejectedPassengers);
        return true;
    }

    protected void logCrossingReport(ArrayList<Passenger> rejectedPassengers) {
        try{
            loggingSemaphore.acquire();
            File file = new File(bannedPassengersLogPath.toUri());
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))){
                StringBuilder out = new StringBuilder();
                if(!rejectedPassengers.isEmpty()){
                    out.append(typeToString()).append(", ").append(vehicleId).append(", rejected [").append(travelState == TravelState.REJECTED).append("]");

                    out.append(" With rejected passengers: ");
                    rejectedPassengers.forEach((passenger -> out.append(passenger.getPassengerId()).append(' ')));

                writer.write(out.toString());
                writer.flush();
                writer.close();
                }
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }catch (Exception e){
            Main.logger.log(Level.WARNING, e.getMessage());
        }
    }

    protected abstract String typeToString();



    private void tryMove(){
        Simulation.tryMove(this);
     //   Platform.runLater(() ->simulationController.updateLane(this));
    }
    private void awaitCondition() {
        synchronized (passengers){
            if(!isRunning){
                try {
                    passengers.wait();
                } catch (InterruptedException e) {
                    Main.logger.log(Level.WARNING, e.getMessage());
                }
            }
        }
    }

    public void changeState(){
        synchronized (passengers){
            isRunning= !isRunning;
            if(isRunning){
                passengers.notifyAll();
            }
        }
    }


    public TerminalCategory getVehicleCategory() {
        return vehicleCategory;
    }
}
