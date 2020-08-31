package com.releasingcode.goldenlobby.modulos.repartidor.items;

import java.util.List;

public class CommandItem extends Item {
    public CommandItem(final String name, final String dName, final String permission, final List<String> lore, final int slot, final int time, final int coins) {
        super(name, dName, permission, lore, slot, time, coins, ItemType.COMMAND);
    }
}
