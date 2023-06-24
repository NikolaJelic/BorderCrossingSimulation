package com.nikola.bordercrossingsimulator.models.transportable;

import java.util.Random;

public class Baggage implements Transportable {
    @Override
    public boolean canPassInspection() {
        return new Random().nextDouble() > 0.1;
    }
}
