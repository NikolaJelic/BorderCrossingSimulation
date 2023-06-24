package com.nikola.bordercrossingsimulator.models.transportable;

import java.io.Serializable;

public interface Transportable extends Serializable {
    public abstract boolean canPassInspection();
}
