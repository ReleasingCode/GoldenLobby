package com.releasingcode.goldenlobby;


import com.releasingcode.goldenlobby.configuracion.CustomConfiguration;
import com.releasingcode.goldenlobby.connections.ServerInfo;
import com.releasingcode.goldenlobby.connections.ServerManager;
import com.releasingcode.goldenlobby.database.Database;
import com.releasingcode.goldenlobby.database.DatabaseConfig;
import com.releasingcode.goldenlobby.database.RedisManager;
import com.releasingcode.goldenlobby.database.pubsub.onRedisMessage;
import com.releasingcode.goldenlobby.extendido.nms.ParticleEffect;
import com.releasingcode.goldenlobby.languages.LanguageFile;
import com.releasingcode.goldenlobby.listeners.BasicCancelledEvents;
import com.releasingcode.goldenlobby.listeners.DoubleJump;
import com.releasingcode.goldenlobby.listeners.OnJoin;
import com.releasingcode.goldenlobby.loader.LobbyMCPlugin;
import com.releasingcode.goldenlobby.managers.VaultAPI;
import com.releasingcode.goldenlobby.modulos.bossbar.BossBarPlugin;
import com.releasingcode.goldenlobby.modulos.cooldown.CooldownPlugin;
import com.releasingcode.goldenlobby.modulos.fly.FlyPlugin;
import com.releasingcode.goldenlobby.modulos.inventarios.InventarioPlugin;
import com.releasingcode.goldenlobby.modulos.npcserver.NPCServerPlugin;
import com.releasingcode.goldenlobby.modulos.onjoinitems.OnJoinItemsPlugin;
import com.releasingcode.goldenlobby.modulos.playerhider.PlayerHidePlugin;
import com.releasingcode.goldenlobby.modulos.scoreboard.ScoreboardPlugin;
import com.releasingcode.goldenlobby.modulos.scoreboard.manager.SidebarScoreboard;
import com.releasingcode.goldenlobby.modulos.setspawn.SpawnPointPlugin;
import com.releasingcode.goldenlobby.modulos.spawn.SpawnPlugin;
import com.releasingcode.goldenlobby.modulos.warps.WarpsPlugin;
import com.releasingcode.goldenlobby.modulos.welcomemessage.WelcomeMessage;
import com.releasingcode.goldenlobby.npc.NPCLib;
import com.releasingcode.goldenlobby.npc.hologram.Hologram;
import com.releasingcode.goldenlobby.npc.internal.MinecraftVersion;
import org.bukkit.configuration.file.FileConfiguration;

import java.net.InetSocketAddress;


public class GoldenLobby extends LobbyMCPlugin {
    private static GoldenLobby plugin;
    public CustomConfiguration globalConfig;
    private DatabaseConfig dbConfig;
    private Database database;
    private NPCLib npcLib;
    private ServerManager serverManager;
    private boolean mysqlEnable, skinExternal;
    private boolean placeHolderAPI;
    private RedisManager redisManager;
    private boolean isNewVersion;
    private VaultAPI vaultAPI;
    private LanguageFile lang;

    public static GoldenLobby getInstance() {
        return plugin;
    }

    public static String getVersion() {
        return plugin.getServer().getClass().getPackage().getName().split("\\.")[3];
    }

    public static MinecraftVersion getMinecraftVersion() {
        return MinecraftVersion.valueOf(getVersion().toUpperCase());
    }


    public boolean isPlaceHolderAPI() {
        return placeHolderAPI;
    }

    public void cargarRecursos() {
        new Hologram.HologramListener();
        npcLib = new NPCLib(this);
    }

    public boolean isMysqlEnable() {
        return mysqlEnable;
    }


    public void serversConnections() {
        if (serverManager != null) serverManager.stop();
        globalConfig = new CustomConfiguration("config", GoldenLobby.getInstance());
        FileConfiguration config = globalConfig.getConfig();
        for (String key : config.getConfigurationSection("Connections").getKeys(false)) {
            String host = config.getString("Connections." + key + ".host");
            String[] addresFully = host.split(":");
            if (addresFully.length > 1) {
                String Address = addresFully[0];
                int port = Utils.tryParseInt(addresFully[1]) ? Integer.parseInt(addresFully[1]) : 25565;
                if (port < 1) {
                    port = 25565;
                }
                ServerInfo info = new ServerInfo(new InetSocketAddress(Address, port));
                info.setName(key);
                ServerManager.addServer(key, info);
            }
        }

        skinExternal = globalConfig.getConfig().getString("SkinFetcher", "internal").equals("external");

        serverManager = new ServerManager(config.getLong("syncDelay", 2000));
        serverManager.async();
        ServerManager.clearVar();
        if (config.isConfigurationSection("Vars")) {
            for (String key : config.getConfigurationSection("Vars").getKeys(false)) {
                String formula = config.getString("Vars." + key + ".formula", "");
                String singular = config.getString("Vars." + key + ".singular", "");
                String plural = config.getString("Vars." + key + ".plural", "");
                ServerManager.ServerManagerVars var = new ServerManager.ServerManagerVars(key);
                var.setFormula(formula);
                var.setSingular(singular);
                var.setPlural(plural);
                ServerManager.addVarForServer(key, var);
            }
        }
    }

    public boolean isSkinExternal() {
        return skinExternal;
    }

    public NPCLib getNpcLib() {
        return npcLib;
    }

    public DatabaseConfig getDbConfig() {
        return this.dbConfig;
    }

    @Override
    public void onDisable() {
        npcLib.cancel();
        Utils.log("Desabilitando complemento");
    }

    public Database getDB() {
        return database;
    }

    public ServerManager getServerManager() {
        return serverManager;
    }

    public void stopServerConnections() {
        if (serverManager != null) {
            serverManager.stop();
        }
    }

    public RedisManager getRedisManager() {
        return redisManager;
    }

    public boolean isNewVersion() {
        return isNewVersion;
    }

    @Override
    public void onEnable() {

        mysqlEnable = false;
        skinExternal = false;
        plugin = this;
        isNewVersion = MinecraftVersion.getNMSVersion().isAboveOrEqual(MinecraftVersion.V1_9_R1);
        if (!ParticleEffect.ParticlePacket.PacketInstantiationException.isFunctional) {
            ParticleEffect.ParticlePacket.PacketInstantiationException.fixFunction();
        }
        placeHolderAPI = getServer().getPluginManager().getPlugin("PlaceholderAPI") != null;
        vaultAPI = new VaultAPI(this);
        if (vaultAPI.setupEconomy()) {
            Utils.log("Hook with Vault [Economy] [OK]");
        }
        if (vaultAPI.setupPermissions()) {
            Utils.log("Hook with Vault [Permission] [OK]");
        }
        Utils.log("Iniciando complemento");
        CustomConfiguration dbConfigFile = new CustomConfiguration("database", this);
        this.mysqlEnable = dbConfigFile.getConfig().getBoolean("MySQL.Enable", false);
        // Para cargar un componente se debe hacer de la siguiente manera
        // llamar al metodo cargarComponente y la clase que contiene una herencia de LobbyComponente
        lang = new LanguageFile(plugin, "language");
        lang.setup();
        this.getServer().getPluginManager().registerEvents(new OnJoin(), this);
        this.getServer().getPluginManager().registerEvents(new DoubleJump(), this);
        this.getServer().getPluginManager().registerEvents(new BasicCancelledEvents(), this);
        cargarRecursos();
        serversConnections();
        getServer().getMessenger().registerOutgoingPluginChannel(this, "BungeeCord");
        /*
          Carga Componentes del Plugin
         */
        cargarComponente(InventarioPlugin.class); //modulo de inventarios
        cargarComponente(NPCServerPlugin.class);
        cargarComponente(SidebarScoreboard.class);
        cargarComponente(ScoreboardPlugin.class);
        cargarComponente(SpawnPointPlugin.class);
        cargarComponente(SpawnPlugin.class);
        cargarComponente(WelcomeMessage.class);
        cargarComponente(FlyPlugin.class);
        cargarComponente(PlayerHidePlugin.class);
        cargarComponente(WarpsPlugin.class);
        cargarComponente(CooldownPlugin.class);
        cargarComponente(BossBarPlugin.class);
        cargarComponente(OnJoinItemsPlugin.class);

        if (mysqlEnable && dbConfigFile.getConfig() != null) {
            this.dbConfig = new DatabaseConfig(dbConfigFile.getConfig());
            database = new Database(this);
        }

        Utils.log("Conectando redis");
        String hostRedis = dbConfigFile.getConfig().getString("Redis.Host");
        String passwordRedis = dbConfigFile.getConfig().getString("Redis.PassWord");
        int portRedis = dbConfigFile.getConfig().getInt("Redis.Port");
        redisManager = new RedisManager(hostRedis, passwordRedis, portRedis);
        if (redisManager.isEditorConnected() && redisManager.isPubConnected()) {
            Utils.log("  Editor Client [" + (redisManager.isEditorConnected() ? "conectado" : "desconectado") + "]");
            Utils.log("  Subscriptor Client [" + (redisManager.isPubConnected() ? "conectado" : "desconectado") + "]");
            redisManager.registerChangesListener(new onRedisMessage(),
                    onRedisMessage.getChannels());
        }
    }

    public VaultAPI getVaultAPI() {
        return vaultAPI;
    }

    public LanguageFile getLang() {
        return lang;
    }

}
