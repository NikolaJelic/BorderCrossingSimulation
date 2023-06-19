package com.nikola.bordercrossingsimulator.models.passenger;

import com.nikola.bordercrossingsimulator.models.transportable.Baggage;

import java.util.Random;

public class Passenger {
    private static int count = 0;
    private final int passengerId;
    private boolean hasBaggage = false;
    private Baggage baggage = null;

    private boolean isDriver = false;
    private final PassengerDocumentation documentation;
    public Passenger(boolean isDriver) {
        if (new Random().nextDouble() <= 0.7) {
            hasBaggage = true;
            baggage = new Baggage();
        }
        this.isDriver = isDriver;
        this.documentation = new PassengerDocumentation();
        passengerId = count++;

    }
    public int getPassengerId(){return passengerId;}
    public boolean isDocumentationValid(){ return documentation.isValid();}

    public boolean getIsDriver(){return isDriver;}

    public boolean checkBaggage() {
        if (hasBaggage) {
            return baggage.canPassInspection();
        } else {
            return true;
        }
    }

}
