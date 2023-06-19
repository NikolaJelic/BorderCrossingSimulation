package com.nikola.bordercrossingsimulator.models.terminal;

import java.util.concurrent.atomic.AtomicBoolean;

public abstract class Terminal {
    private final TerminalCategory terminalCategory;
    private final AtomicBoolean occupied;
    private final AtomicBoolean status;

    protected int terminalId;

    public Terminal(TerminalCategory terminalCategory, int terminalId){
        this.occupied = new AtomicBoolean(false);
        this.status = new AtomicBoolean(true);
        this.terminalCategory = terminalCategory;
        this.terminalId = terminalId;
    }
    public synchronized boolean isOccupied() {
        return occupied.get();
    }

    public synchronized void setOccupied(boolean occupied) {
        this.occupied.set(occupied);
    }

    public synchronized TerminalCategory getTerminalCategory() {
        return terminalCategory;
    }

    public void setStatus(boolean status){this.status.set(status); }
    public synchronized boolean getStatus(){ return status.get();}

    public int getTerminalId(){return  terminalId;}


}
