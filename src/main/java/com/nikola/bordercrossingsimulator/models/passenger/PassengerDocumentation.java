package com.nikola.bordercrossingsimulator.models.passenger;

import java.io.Serializable;
import java.util.Random;

public class PassengerDocumentation implements Serializable {
    private final boolean valid;

    public PassengerDocumentation() {
        this.valid = new Random().nextDouble() >= 0.03;
    }

    public boolean isValid(){ return valid;}
}
