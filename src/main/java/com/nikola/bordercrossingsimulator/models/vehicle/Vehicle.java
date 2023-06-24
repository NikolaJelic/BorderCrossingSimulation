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
import java.util.List;
import java.util.Random;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.Semaphore;
import java.util.logging.Level;

import static com.nikola.bordercrossingsimulator.models.Simulation.endOfLane;


public abstract class Vehicle extends Thread implements Serializable {
    public static final Semaphore loggingSemaphore = new Semaphore(1);
    private static final Semaphore binaryLoggingSemaphore = new Semaphore(1);

    protected final ArrayList<Passenger> rejectedPassengers = new ArrayList<>();

    private static int vehicleCounter = 1;
    private final int vehicleId;
    private final int policeWaitDuration; //in milliseconds
    private final TerminalCategory vehicleCategory;
    protected TravelState travelState = TravelState.IN_LANE;
    private final Path bannedPassengersLogPath;
    private final Path bannedUsersBinaryLog;
    private final int maxCapacity;
    protected final CopyOnWriteArrayList<Passenger> passengers = new CopyOnWriteArrayList<>();
    protected StringBuilder crossingLog = new StringBuilder();
    private final SimulationController simulationController;
    private int position;

    private boolean isRunning = true;


    public Vehicle(int maxCapacity, int policeWaitDuration, TerminalCategory vehicleCategory, Path bannedPassengersLogPath, Path bannedUsersBinaryLog,SimulationController simulationController) {
        this.vehicleId = vehicleCounter++;
        this.maxCapacity = maxCapacity;
        this.bannedPassengersLogPath = bannedPassengersLogPath;
        this.bannedUsersBinaryLog = bannedUsersBinaryLog;
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
            awaitCondition();
            if(travelState == TravelState.IN_LANE && position < endOfLane){
                tryMove();
            }
          //  System.out.println(typeToString() + vehicleId + ": " + travelState + " Position: " + position);
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
            Platform.runLater(()->simulationController.updatePoliceTerminal(this, terminal));

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
            logCrossingReport(rejectedPassengers, terminal, true);
            Platform.runLater(()-> simulationController.resetPoliceTerminal(terminal));
            Platform.runLater(()-> simulationController.updateLog(String.valueOf(crossingLog)));
            terminal.setOccupied(false);



        }
    }



    private void passCustomsTerminal() {
        Terminal terminal = Simulation.tryGetCustomsTerminal(this);
        crossingLog = new StringBuilder();
        if(terminal != null){
            Platform.runLater(()->simulationController.updateCustomsTerminal(this, terminal));
            travelState = TravelState.UNDER_INSPECTION;
            crossingLog.append(typeToString()).append(" ").append(vehicleId).append(" on customs terminal [").append(terminal.getTerminalId()).append("] ");
            if(inspectCustoms()){
                crossingLog.append("passed customs inspection with passengers: ");
                passengers.forEach((passenger) ->  crossingLog.append(" ").append(passenger.getPassengerId()).append(" "));
                crossingLog.append('\n');
                travelState = TravelState.FINISHED;
                Simulation.removeFromTerminalQueue(this);
                Simulation.addToPassed(this);

            }else{
                crossingLog.append("failed customs inspection.\n");
                travelState = TravelState.REJECTED;
                Simulation.removeFromTerminalQueue(this);

                Simulation.addToRejected(this);
            }
            logCrossingReport(rejectedPassengers, terminal, false);
            Platform.runLater(()-> simulationController.resetCustomsTerminal(terminal));
            Platform.runLater(()-> simulationController.updateLog(String.valueOf(crossingLog)));
            terminal.setOccupied(false);



        }
    }
    protected abstract boolean inspectCustoms();
    private boolean inspectPolice() {
        boolean ret = true;
        for(Passenger passenger : passengers){
            if(!passenger.isDocumentationValid()){
                passengers.remove(passenger);
                if(passenger.getIsDriver()){
                   travelState = TravelState.REJECTED;
                   ret =  false;
                }
                rejectedPassengers.add(passenger);
            }
            try {
                sleep(policeWaitDuration);
            } catch (InterruptedException e) {
                Main.logger.log(Level.WARNING, e.getMessage());
            }
        }
        logPassengersToBinaryFile(rejectedPassengers);
        return ret;
    }

    protected void logPassengersToBinaryFile(ArrayList<Passenger> passengers){
        try{
            binaryLoggingSemaphore.acquire();
            List<Passenger> serializedPassengers = new ArrayList<>();

            try {
                File inFile = new File(bannedUsersBinaryLog.toUri());
                if(inFile.exists()) {
                    FileInputStream fileIn = new FileInputStream(inFile);
                    ObjectInputStream in = new ObjectInputStream(fileIn);

                    serializedPassengers = (List<Passenger>) in.readObject();
                    in.close();
                    fileIn.close();
                }
                serializedPassengers.addAll(passengers);


                FileOutputStream fileOut = new FileOutputStream(new File(bannedUsersBinaryLog.toUri()), false);
                ObjectOutputStream out = new ObjectOutputStream(fileOut);

                out.writeObject(serializedPassengers);

                out.close();
                fileOut.close();

            } catch (Exception e) {
                // Handle exceptions appropriately
                e.printStackTrace();
            }
        }catch (Exception e){
            Main.logger.log(Level.WARNING, e.getMessage());
        }finally {
            binaryLoggingSemaphore.release();
        }
    }
    protected void logCrossingReport(ArrayList<Passenger> rejectedPassengers, Terminal terminal, boolean isPoliceTerminal) {
        try{
            loggingSemaphore.acquire();
            File file = new File(bannedPassengersLogPath.toUri());
            try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, true))){
                StringBuilder out = new StringBuilder();
                if(!rejectedPassengers.isEmpty()){
                    out.append(typeToString()).append("[").append(vehicleId).append("]");
                    out.append((travelState == TravelState.REJECTED) ? " rejected " :  " passed ") ;
                    out.append("at ").append((isPoliceTerminal)? "Police" : "Customs").append(" Terminal[").append(terminal.getTerminalId()).append("]");

                    out.append(" With removed passengers: ");
                    rejectedPassengers.forEach((passenger -> out.append(passenger.getPassengerId()).append(' ')));
                    out.append('\n');
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
        finally {
            loggingSemaphore.release();
        }
    }

    public abstract String typeToString();



    private void tryMove(){
        Simulation.tryMove(this);
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
