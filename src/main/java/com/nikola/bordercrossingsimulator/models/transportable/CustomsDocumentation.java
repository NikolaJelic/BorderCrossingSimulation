package com.nikola.bordercrossingsimulator.models.transportable;

import java.util.Random;

public class CustomsDocumentation {
    private final double declaredWeight;
    private final double realWeight;

    public CustomsDocumentation() {
        Random random = new Random();
        this.declaredWeight = random.nextDouble(35000);
        if (random.nextDouble() <= 0.2) {
            this.realWeight = random.nextDouble(declaredWeight * 0.3) + declaredWeight;
        } else {
            this.realWeight = random.nextDouble(declaredWeight);
        }
    }

    public boolean isValidDocumentation() {
        return realWeight <= declaredWeight;
    }
}
