package com.releasingcode.goldenlobby.modulos.inventarios.db.redis;

import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.database.pubsub.IRedisSub;
import com.releasingcode.goldenlobby.database.pubsub.SubChannel;
import com.releasingcode.goldenlobby.modulos.inventarios.InventarioPlugin;

public class OnRedisMessageInv implements IRedisSub {
    @Override
    public void onIMessage(byte[] messageByte) {
        // al recibir sincronización del inventario
        String returnBack = new String(messageByte);
        if (InventarioPlugin.getInstance().isIamsender()) { // yo fue el remitente
            //recibí el mensaje y estoy reiniciando el estado de remitente
            InventarioPlugin.getInstance().setIamSender(false); // solo es activado cuando ejecuta el comando sync
            return;
        }
        SubChannel.SubOperation operation = SubChannel.SubOperation.from(returnBack);
        if (operation != null) {
            System.out.println("Synchronizing Inventory from Database: " + operation.lower());
            InventarioPlugin.getInstance().reloadInventories(() -> Utils.log("Inventory module has been reloaded"), true);
        }
    }
}
