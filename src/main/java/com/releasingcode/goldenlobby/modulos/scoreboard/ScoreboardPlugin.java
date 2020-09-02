package com.releasingcode.goldenlobby.modulos.scoreboard;

import com.releasingcode.goldenlobby.GoldenLobby;
import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.call.CallBack;
import com.releasingcode.goldenlobby.configuracion.CustomConfiguration;
import com.releasingcode.goldenlobby.database.pubsub.SubChannel;
import com.releasingcode.goldenlobby.database.pubsub.onRedisMessage;
import com.releasingcode.goldenlobby.extendido.scoreboard.Sidebar;
import com.releasingcode.goldenlobby.loader.LobbyComponente;
import com.releasingcode.goldenlobby.modulos.scoreboard.command.ScoreboardCommand;
import com.releasingcode.goldenlobby.modulos.scoreboard.db.ScoreboardDB;
import com.releasingcode.goldenlobby.modulos.scoreboard.db.ScoreboardFetch;
import com.releasingcode.goldenlobby.modulos.scoreboard.db.redis.OnRedisMessageScoreboard;
import com.releasingcode.goldenlobby.modulos.scoreboard.listener.onJoin;
import com.releasingcode.goldenlobby.modulos.scoreboard.manager.ScoreboardManager;
import org.apache.commons.codec.binary.Base64;

import java.nio.charset.StandardCharsets;

public class ScoreboardPlugin extends LobbyComponente {
    private static ScoreboardPlugin instance;
    private CustomConfiguration scoreboardConfig;
    private ScoreboardManager scoreboardManager;
    private ScoreboardDB scoreboardDB;
    private boolean iamsender;
    private NameTag nameTag;

    public static ScoreboardPlugin getInstance() {
        return instance;
    }

    @Override
    protected void onEnable() {
        instance = this;
        scoreboardDB = new ScoreboardDB(this);

        loadScoreboard();
        Utils.log(" - Loading module Scoreboard");
        new onJoin(this);
        new ScoreboardCommand(this, "mcscoreboard", "/mcscoreboard", "Reload Scoreboard").register();
        onRedisMessage.registerUpdater(SubChannel.SYNC_SCOREBOARD, new OnRedisMessageScoreboard());
    }

    public void loadScoreboard() {
        scoreboardConfig = new CustomConfiguration("scoreboard", getPlugin());
        scoreboardManager = new ScoreboardManager(this);
        nameTag = new NameTag(this);
        nameTag.start();
    }

    public void reloadScoreboard() {
        if (nameTag != null) {
            nameTag.cancel();
        }
        getScoreboardManager().clear();
        Sidebar.exit();
        loadScoreboard();
    }

    public ScoreboardDB getScoreboardDB() {
        return scoreboardDB;
    }

    public boolean isIamsender() {
        return iamsender;
    }

    public void setIamSender(boolean iamsender) {
        this.iamsender = iamsender;
    }

    public void sendSync(SubChannel.SubOperation operation, CallBack.SingleCallBack callBack) {
        getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            try {
                setIamSender(true);
                GoldenLobby.getInstance().getRedisManager().pub(
                        SubChannel.SYNC_SCOREBOARD.tobyte(), operation.tobyte()
                );
                callBack.onSuccess();
            } catch (Exception e) {
                setIamSender(false);
                callBack.onError();
            }
        });
    }

    public void fetchFromDatabase(CallBack.SingleCallBack callBack) {
        getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), ()
                -> getScoreboardDB().fetchScoreboard(new CallBack.ReturnCallBack<ScoreboardFetch>() {
            @Override
            public void onSuccess(ScoreboardFetch callback) {
                if (callback == null) {
                    Utils.log("&cNo data to synchronize");
                    callBack.onError();
                    return;
                }
                //ACTUALIZAR
                Utils.log("Updating Scoreboard...");
                byte[] bytesconfig = Base64.decodeBase64(callback.getBase64());
                String text = new String(bytesconfig, StandardCharsets.UTF_8);
                scoreboardConfig.updateFile(text);
                getPlugin().getServer().getScheduler().runTask(getPlugin(), () -> {
                    reloadScoreboard();
                    callBack.onSuccess();
                });
            }

            @Override
            public void onError(ScoreboardFetch callback) {
                callBack.onError();
            }
        }));
    }

    public ScoreboardManager getScoreboardManager() {
        return scoreboardManager;
    }

    public CustomConfiguration getScoreboardConfig() {
        return scoreboardConfig;
    }

    @Override
    protected void onDisable() {
        Utils.log("&cScoreboard module disabled");
    }
}
