package com.nikola.bordercrossingsimulator.models.terminal;

public class PoliceTerminal extends Terminal{
    private static int terminalCount = 1;
    public PoliceTerminal(TerminalCategory terminalCategory) {
        super(terminalCategory, terminalCount++);
    }
}
