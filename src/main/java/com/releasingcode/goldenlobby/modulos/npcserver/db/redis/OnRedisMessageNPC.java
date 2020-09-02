package com.releasingcode.goldenlobby.modulos.npcserver.db.redis;

import com.releasingcode.goldenlobby.database.pubsub.IRedisSub;
import com.releasingcode.goldenlobby.database.pubsub.SubChannel;
import com.releasingcode.goldenlobby.modulos.npcserver.NPCServerPlugin;

public class OnRedisMessageNPC implements IRedisSub {
    @Override
    public void onIMessage(byte[] messageByte) {
        String returnBack = new String(messageByte);
        if (NPCServerPlugin.getInstance().isIamsender()) { // yo fue el remitente
            //recibí el mensaje y estoy reiniciando el estado de remitente
            NPCServerPlugin.getInstance().setIamSender(false); // solo es activado cuando ejecuta el comando sync
            return;
        }
        SubChannel.SubOperation operation = SubChannel.SubOperation.from(returnBack);
        if (operation != null) {
            System.out.println("Reloading NPC from Database: " + operation.lower());
            NPCServerPlugin.getInstance().reloadNPC(() -> System.out.println("the plug-in has been reloaded"),
                    operation);
        }
    }
}
