package com.nikola.bordercrossingsimulator.models.terminal;

public class CustomsTerminal extends Terminal{
    private static int terminalCount = 1;
    public CustomsTerminal(TerminalCategory terminalCategory) {
        super(terminalCategory, terminalCount++);
    }
}
