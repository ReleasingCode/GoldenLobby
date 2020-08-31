package com.releasingcode.goldenlobby.modulos.repartidor.npc;


import org.bukkit.Location;

public class NPCRepartidor {

    public NPCRepartidor() {
        super();
    }

    public static void spawnNPC(final Location loc) {
       /* NPC npc = LobbyMC.getInstance().getNpcLib().createNPC("deliveryman", UUID.fromString("e401550b-f3d7-4e18-b361-5d343c59b207"), "deliveryman");
        npc.setLocation(loc);
        npc.setUpdateHologramAutomatic(false);
        npc.create();
        SkinGameProfile.loadGameProfile("DeliveryMan", new SkinGameProfile.Callback() {
            @Override
            public void call(Skin skinData) {
                npc.destroyForUpdate();
                npc.setSkin(skinData);
                npc.setText(Arrays.asList(" ", " ", " "));
                npc.setReady(true);
                NPCManager.addFullyLoaded(npc);
            }

            @Override
            public void failed() {
                npc.setText(Arrays.asList(" ", " ", " "));
                npc.setReady(true);
                NPCManager.addFullyLoaded(npc);
            }
        });*/
    }
}
