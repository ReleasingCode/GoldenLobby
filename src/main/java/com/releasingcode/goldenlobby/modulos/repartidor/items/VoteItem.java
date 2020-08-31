package com.releasingcode.goldenlobby.modulos.repartidor.items;

import java.util.Arrays;

public class VoteItem extends Item {
    private final String link;
    private final String serviceName;

    public VoteItem(final String name, final String dName, final String permission, final int slot, final int time, final int coins, final String link, final String serviceName, final String publicServiceName) {
        super(name, dName, permission,
                Arrays.asList("${lobby.deliveryman.vote.lore}[" + publicServiceName + ", " + coins + "]"), slot, time,
                coins, ItemType.VOTEPAGE);
        this.serviceName = serviceName;
        this.link = link;
    }

    public String getLink() {
        return this.link;
    }

    public String getServiceName() {
        return this.serviceName;
    }
}
