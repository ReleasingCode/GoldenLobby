package com.releasingcode.goldenlobby.modulos.repartidor.votifier;

import es.minecub.core.sync.actions.defaults.PerformCommandAction;

public class Command {
    private final String server;
    private final String syntax;

    public Command(final String server, final String syntax) {
        super();
        this.server = server;
        this.syntax = syntax;
    }

    public void performCommand(final String player) {
        new PerformCommandAction(this.server, this.syntax.replace("%player%", player), "Votaciones", false).send();
    }
}
