package com.releasingcode.goldenlobby.modulos.scoreboard.db.redis;

import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.call.CallBack;
import com.releasingcode.goldenlobby.database.pubsub.IRedisSub;
import com.releasingcode.goldenlobby.database.pubsub.SubChannel;
import com.releasingcode.goldenlobby.modulos.inventarios.InventarioPlugin;
import com.releasingcode.goldenlobby.modulos.scoreboard.ScoreboardPlugin;

public class OnRedisMessageScoreboard implements IRedisSub {
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
            Utils.log("Sincronizando Scoreboard desde Base de datos: " + operation.lower());
            ScoreboardPlugin.getInstance().fetchFromDatabase(new CallBack.SingleCallBack() {
                @Override
                public void onSuccess() {
                    Utils.log("Sincronizacion de scoreboard completada");
                }
            });
        }
    }
}
