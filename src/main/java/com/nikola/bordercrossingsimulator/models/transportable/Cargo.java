package com.nikola.bordercrossingsimulator.models.transportable;

import java.util.Random;

public class Cargo implements Transportable {
    private final CustomsDocumentation customsDocumentation;

    public Cargo() {
        this.customsDocumentation = (new Random().nextBoolean()) ? null : new CustomsDocumentation();
    }

    @Override
    public boolean canPassInspection() {
        if (customsDocumentation != null) {
            return customsDocumentation.isValidDocumentation();
        } else {
            return true;
        }
    }
}
