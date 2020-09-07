package com.releasingcode.goldenlobby.modulos.npcserver;

import com.releasingcode.goldenlobby.GoldenLobby;
import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.call.CallBack;
import com.releasingcode.goldenlobby.configuracion.CustomConfiguration;
import com.releasingcode.goldenlobby.database.pubsub.SubChannel;
import com.releasingcode.goldenlobby.database.pubsub.onRedisMessage;
import com.releasingcode.goldenlobby.loader.LobbyComponente;
import com.releasingcode.goldenlobby.managers.SkinGameProfile;
import com.releasingcode.goldenlobby.modulos.npcserver.comandos.NpcServerCommand;
import com.releasingcode.goldenlobby.modulos.npcserver.db.NPCFetch;
import com.releasingcode.goldenlobby.modulos.npcserver.db.history.HistoryStatsDB;
import com.releasingcode.goldenlobby.modulos.npcserver.db.mysql.NPCDB;
import com.releasingcode.goldenlobby.modulos.npcserver.db.redis.OnRedisMessageNPC;
import com.releasingcode.goldenlobby.modulos.npcserver.history.HistoryManager;
import com.releasingcode.goldenlobby.modulos.npcserver.listener.NPCListener;
import com.releasingcode.goldenlobby.npc.api.NPC;
import com.releasingcode.goldenlobby.npc.api.skin.Skin;
import com.releasingcode.goldenlobby.npc.api.skin.SkinFetcher;
import com.releasingcode.goldenlobby.npc.api.state.NPCMode;
import com.releasingcode.goldenlobby.npc.api.state.NPCPosition;
import com.releasingcode.goldenlobby.npc.api.state.NPCSlot;
import com.releasingcode.goldenlobby.npc.internal.NPCManager;
import com.releasingcode.goldenlobby.serializer.Serializer;
import com.releasingcode.goldenlobby.serializer.Serializers;
import org.apache.commons.codec.binary.Base64;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.inventory.ItemStack;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class NPCServerPlugin extends LobbyComponente {
    private static final HashMap<String, CustomConfiguration> npcsConfig
            = new HashMap<>();
    private static NPCServerPlugin instance;
    private NPCDB npcdb;
    private HistoryStatsDB historyStatsDB;
    private boolean iamsender;
    private HistoryManager historyManager;

    public static NPCServerPlugin getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        instance = this;
        iamsender = false;
        npcsConfig.clear();
        Utils.log(" - Loading module NPCServer");
        npcdb = new NPCDB(this);
        historyStatsDB = new HistoryStatsDB(this);
        new NPCListener(instance);
        new NpcServerCommand(this, "mcnpc", "mcnpc", "Create, edit NPC to teleport to other servers")
                .register();
        completeLoadNPC(SubChannel.SubOperation.GET_FROM_LOCAL);
        onRedisMessage.registerUpdater(SubChannel.SYNC_NPC, new OnRedisMessageNPC());
    }

    public boolean isIamsender() {
        return iamsender;
    }

    public void completeLoadNPC(SubChannel.SubOperation operation) {
        if (operation == SubChannel.SubOperation.GET_FROM_LOCAL) {
            loadFileNPC();
            historyManager = new HistoryManager();
            GoldenLobby.getInstance().getNpcLib().startTaskNPC();
            return;
        }
        if (GoldenLobby.getInstance().isMysqlEnable()) {
            boolean purge = operation == SubChannel.SubOperation.PURGE_AND_GET_FROM_DB;
            syncDbStart(purge);
        }
    }

    public void setIamSender(boolean iamsender) {
        this.iamsender = iamsender;
    }

    public NPCDB getNpcdb() {
        return npcdb;
    }


    public void syncDbStart(boolean purge) {
        getPlugin().getServer().getScheduler()
                .runTaskAsynchronously(getPlugin(), () -> getNpcdb().fetchNPCs(callback -> {
                    if (callback.isEmpty()) {
                        Utils.log("");
                        return;
                    }
                    Utils.log("No NPC's were found in the database: " + callback.size());
                    callFetchingUpdating(callback, purge);
                    historyManager = new HistoryManager();
                    GoldenLobby.getInstance().getNpcLib().startTaskNPC();
                }));
    }

    public HistoryManager getHistoryManager() {
        return historyManager;
    }

    public void callFetchingUpdating(ArrayList<NPCFetch> callback, boolean purge) {
        for (NPCFetch FETCH : callback) {
            boolean isInLocal = false;
            for (NPC npc : NPCManager.getAllNPCs()) {
                if (!npc.getName().toLowerCase().equals(FETCH.getName().toLowerCase())) {
                    continue;
                }
                isInLocal = true;
                //ACTUALIZAR
                npc.destroy();
                Utils.log("Updating the configuration: " + npc.getName() + "");
                byte[] bytesconfig = org.apache.commons.codec.binary.Base64.decodeBase64(FETCH.getBase64());
                String text = new String(bytesconfig, StandardCharsets.UTF_8);
                deleteConfigNPCForUpdate(npc.getName());
                CustomConfiguration uconfig = new CustomConfiguration(npc.getName().toLowerCase(),
                        "/npc/" + FETCH.getDir() + "/", getPlugin());
                uconfig.updateFile(text);
                npcsConfig.put(npc.getName().toLowerCase(), uconfig);
                loadNPC(uconfig);
                Utils.log("Configuration: " + npc.getName() + " Updated!");
            }
                    /*
                        Crear el archivo localmente
                     */
            if (!isInLocal) {
                File folderInventory = new File(getPlugin().getDataFolder(), "/npc/");
                if (!folderInventory.isDirectory()) {
                    folderInventory.mkdirs();
                }
                if (folderInventory.isDirectory()) {
                    byte[] bytesconfig = Base64.decodeBase64(FETCH.getBase64());
                    String text = new String(bytesconfig, StandardCharsets.UTF_8);
                    File npcConfig = new File(getPlugin().getDataFolder(),
                            "/npc/" + FETCH.getDir() + "/" + FETCH.getName() + ".yml");
                    if (!npcConfig.getAbsoluteFile().isDirectory()) {
                        File folderParent = npcConfig.getAbsoluteFile().getParentFile();
                        folderParent.mkdirs();
                    }
                    try {
                        FileWriter fw = new FileWriter(npcConfig, false);
                        fw.write(text);
                        fw.close();
                    } catch (Exception e) {
                        Utils.log("Could not synchronize a file " + FETCH
                                .getName() + " , a writing error occurred: " + e.getMessage());
                    }
                    CustomConfiguration cConfig = new CustomConfiguration(npcConfig, getPlugin());
                    npcsConfig.put(cConfig.getFile().getName().replace(".yml", ""), cConfig);
                    loadNPC(cConfig);
                    Utils.log("Generating new NPC from database: " + FETCH.getName());
                }
            }
        }
        if (purge) {
            purgefiles();
        }
    }

    public boolean hasFile(Collection<CustomConfiguration> rootFilesConfiguration, File file) {
        for (CustomConfiguration inList :
                rootFilesConfiguration) {
            if (inList.getFile().getName().toLowerCase().replace(".yml", "")
                    .equals(file.getName().toLowerCase().replace(".yml", ""))) {
                return true;
            }
        }
        return false;
    }

    public void purgefiles() {
        File folder = new File(getPlugin().getDataFolder(), "/npc/");
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    //String directorios
                    if (file.isDirectory()) {
                        if (file.getName().toLowerCase().equals("staff") || file.getName().toLowerCase()
                                .equals("command") || file.getName().toLowerCase().equals("history")) {
                            File[] configFile = file.listFiles();
                            if (configFile != null) {
                                for (File config : configFile) {
                                    if (hasFile(npcsConfig.values(), config)) {
                                        continue;
                                    }
                                    config.delete();
                                    NPC npc = NPCManager.getNPC(config.getName().replace(".yml", ""));
                                    if (npc != null) {
                                        npc.destroy();
                                    }
                                }
                            }
                        }
                    }
                }
            }

        }
    }

    @Override
    public void onDisable() {
        npcsConfig.clear();
        GoldenLobby.getInstance().getNpcLib().cancel();
        NPCManager.clearNpcs();
    }

    public void reloadNPC(NPC npc, CallBack.SingleCallBack callBack, SubChannel.SubOperation operation) {
        getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            if (operation != SubChannel.SubOperation.GET_FROM_LOCAL) {
                callBack.onError();
                return;
            }
            if (npc != null) {
                CustomConfiguration configuration = npc.getConfiguration();
                npc.destroy();
                configuration.reloadConfig();
                NPC npcLoaded = loadNPC(configuration);
                if (npcLoaded != null) {
                    if (!npcLoaded.getNPCMode().equals(NPCMode.HISTORY)) {
                        callBack.onSuccess();
                        return;
                    }
                    historyManager.loadHistory(npcLoaded);
                    callBack.onSuccess();
                } else {
                    callBack.onError();
                }
                return;
            }
            callBack.onError();
        });
    }

    public void reloadNPC(CallBack.SingleCallBack callBack, SubChannel.SubOperation operation) {
        getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            try {
                GoldenLobby.getInstance().getNpcLib().cancel();
                GoldenLobby.getInstance().stopServerConnections();
                npcsConfig.clear();
                NPCManager.clearFullyLoaded();
                ArrayList<String> NpcNames = NPCManager.getAllNPCs().stream().map(NPC::getName)
                        .collect(Collectors.toCollection(ArrayList::new));
                for (String npcName : NpcNames) { //evitar concurrencia de lista
                    NPC npc = NPCManager.getNPC(npcName);
                    if (npc != null) {
                        npc.destroy();
                    }
                }
                NPCManager.clearNpcs();
                getPlugin().getServer().getScheduler().runTaskLater(getPlugin(), () -> {
                    completeLoadNPC(operation);
                    callBack.onSuccess();
                }, 2 * 20);

            } catch (Exception e) {
                e.printStackTrace();
                callBack.onError();
            }
        });
    }

    public void sendSync(SubChannel.SubOperation operation) {
        getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            setIamSender(true);
            GoldenLobby.getInstance().getRedisManager().pub(
                    SubChannel.SYNC_NPC.tobyte(), operation.tobyte()
            );
        });
    }

    public void loadFileNPC() {
        File folder = new File(getPlugin().getDataFolder(), "/npc/");
        if (folder.isDirectory()) {
            File[] files = folder.listFiles();
            if (files != null) {
                for (File file : files) {
                    //String directorios
                    if (file.isDirectory()) {
                        if (file.getName().toLowerCase().equals("staff")
                                || file.getName().toLowerCase().equals("command")
                                || file.getName().toLowerCase().equals("history")) {
                            File[] configFile = file.listFiles();
                            if (configFile != null) {
                                for (File config : configFile) {
                                    if (config.getName().toLowerCase().endsWith(".yml")) {
                                        if (!npcsConfig
                                                .containsKey(config.getName().toLowerCase().replace(".yml", ""))) {
                                            npcsConfig.put(config.getName().toLowerCase().replace(".yml", ""),
                                                    new CustomConfiguration(config, getPlugin()));
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        loadFromConfiguration();
    }

    public void loadFromConfiguration() {
        for (CustomConfiguration config :
                npcsConfig.values()) {
            loadNPC(config);
        }
    }

    public NPC loadNPC(CustomConfiguration fileConfiguration) {
        String name = fileConfiguration.getFile().getName().replace(".yml", "");
        FileConfiguration configuration = fileConfiguration.getConfig();
        String worldName = configuration.getString("Location.World", null);
        double X = configuration.getDouble("Location.X", 0);
        double Y = configuration.getDouble("Location.Y", -50);
        double Z = configuration.getDouble("Location.Z", 0);
        float yaw = (float) configuration.getDouble("Location.Yaw", 0);
        float pitch = (float) configuration.getDouble("Location.Pitch", 0);
        if (worldName == null) {
            return null;
        }
        World world = Bukkit.getWorld(worldName);
        if (world == null) {
            return null;
        }
        String uuid = configuration.getString("uuid", null);
        if (uuid == null) {
            return null;
        }
        UUID uid = null;
        try {
            uid = UUID.fromString(uuid);
        } catch (Exception ignored) {

        }
        if (uid == null) {
            return null;
        }
        Serializer<ItemStack> serializer = Serializers.getSerializer(ItemStack.class);
        Location loc = new Location(world, X, Y, Z, yaw, pitch);
        List<String> holograma = configuration.getString("Hologram", null) == null ? new ArrayList<>() : Utils
                .coloredList(new ArrayList<>(Arrays.asList(configuration.getString("Hologram").split("\\n"))));
        List<String> comandos = configuration.getString("Commands", null) == null ? new ArrayList<>() : Utils
                .coloredList(new ArrayList<>(Arrays.asList(configuration.getString("Commands", "").split("\\n"))));
        List<String> Rewardcomandos = configuration
                .getString("RewardCommands", null) == null ? new ArrayList<>() : Utils.coloredList(
                new ArrayList<>(Arrays.asList(configuration.getString("RewardCommands", "").split("\\n"))));
        NPCMode mode = NPCMode.from(configuration.getString("NPCMode", "server"));
        boolean isLookAtPlayer = configuration.getBoolean("lookAtPlayer", false);
        String skinValue = configuration.getString("Skin", null);
        String nameNPC = configuration.getString("npcName", null);
        String cooldownValidator = configuration.getString("cooldownValidator", null);
        NPCPosition positionStatus = NPCPosition.from(configuration.getString("position", "NORMAL"));
        NPC npc = GoldenLobby.getInstance().getNpcLib().createNPC(name, uid, nameNPC);
        if (serializer != null) {
            ItemStack helmet = serializer.deserialize(configuration.getString("equipment.helmet"));
            ItemStack chestplate = serializer.deserialize(configuration.getString("equipment.chestplate"));
            ItemStack leggings = serializer.deserialize(configuration.getString("equipment.leggings"));
            ItemStack boots = serializer.deserialize(configuration.getString("equipment.boots"));
            ItemStack mainhand = serializer.deserialize(configuration.getString("equipment.mainhand"));
            ItemStack offhand = serializer.deserialize(configuration.getString("equipment.offhand"));
            npc.setHashItem(NPCSlot.HELMET, helmet);
            npc.setHashItem(NPCSlot.CHESTPLATE, chestplate);
            npc.setHashItem(NPCSlot.LEGGINGS, leggings);
            npc.setHashItem(NPCSlot.BOOTS, boots);
            npc.setHashItem(NPCSlot.MAINHAND, mainhand);
            npc.setHashItem(NPCSlot.OFFHAND, offhand);
        }
        npc.setLocation(loc);
        npc.setPositionMemory(positionStatus);
        npc.setRewardCommands(Rewardcomandos);
        npc.setLookAtPlayer(isLookAtPlayer);
        npc.setMode(mode);
        npc.setConfigurationFile(fileConfiguration);
        npc.setCooldownValidator(cooldownValidator);
        npc.setCommand(comandos);
        npc.create();
        if (skinValue != null) {
            if (GoldenLobby.getInstance().isSkinExternal()) {
                SkinFetcher.fetchSkinFromIdAsync(skinValue, new SkinFetcher.Callback() {
                    @Override
                    public void call(Skin skinData) {
                        npc.destroyForUpdate();
                        npc.setSkin(skinData);
                        npc.setText(holograma);
                        npc.setReady(true);
                        NPCManager.addFullyLoaded(npc);
                    }

                    @Override
                    public void failed() {
                        npc.setText(holograma);
                        npc.setReady(true);
                        NPCManager.addFullyLoaded(npc);
                    }
                });
            } else {
                SkinGameProfile.loadGameProfile(skinValue, new SkinGameProfile.Callback() {
                    @Override
                    public void call(Skin skinData) {
                        npc.destroyForUpdate();
                        npc.setSkin(skinData);
                        npc.setText(holograma);
                        npc.setReady(true);
                        NPCManager.addFullyLoaded(npc);
                    }

                    @Override
                    public void failed() {
                        npc.setText(holograma);
                        npc.setReady(true);
                        NPCManager.addFullyLoaded(npc);
                    }
                });
            }
        } else {
            npc.setText(holograma);
            npc.setReady(true);
            NPCManager.addFullyLoaded(npc);
        }
        return npc;
    }

    public void npcDelete(String name, String dir, CallBack.SingleCallBack callBack) {
        npcsConfig.remove(name.toLowerCase());
        try {
            if (checkDirectory()) {
                File file = new File(getPlugin().getDataFolder(), "/npc/" + dir + "/" + name + ".yml");
                if (file.delete()) {
                    callBack.onSuccess();
                } else {
                    callBack.onError();
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            callBack.onError();
        }
    }

    public void deleteConfigNPC(String name, String dir, CallBack.SingleCallBack callBack) {
        if (!GoldenLobby.getInstance().isMysqlEnable()) {
            getPlugin().getServer().getScheduler()
                    .runTaskAsynchronously(getPlugin(), () -> npcDelete(name, dir, callBack));
            return;
        }
        getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(),
                () -> getNpcdb().removeConfiguration(name.toLowerCase(), new CallBack.ReturnCallBack<Integer>() {
                    @Override
                    public void onSuccess(Integer callback) {
                        npcDelete(name, dir, callBack);
                    }

                    @Override
                    public void onError(Integer callback) {
                        npcDelete(name, dir, callBack);
                    }
                }));
    }

    public void deleteConfigNPCForUpdate(String fileName) {
        File commandFile = new File(getPlugin().getDataFolder(), "/npc/command/" + fileName + ".yml");
        Utils.deleteIfExist(commandFile);
        File staffFile = new File(getPlugin().getDataFolder(), "/npc/staff/" + fileName + ".yml");
        Utils.deleteIfExist(staffFile);
        File history = new File(getPlugin().getDataFolder(), "/npc/history/" + fileName + ".yml");
        Utils.deleteIfExist(history);
    }

    public void createConfigNPC(NPC npc, String player, CallBack.SingleCallBack callBack) {
        if (checkDirectory()) {
            Serializer<ItemStack> serializer = Serializers.getSerializer(ItemStack.class);
            CustomConfiguration previus = npc.getConfiguration();
            CustomConfiguration configuration;
            String directory = npc.getNPCMode().getDirectory() + "/";
            if (previus != null) {
                previus.reloadConfig();
                deleteConfigNPCForUpdate(npc.getName().toLowerCase());
                previus.changeLocation("/npc/" + directory);
                configuration = previus;
            } else {
                deleteConfigNPCForUpdate(npc.getName().toLowerCase());
                configuration =
                        new CustomConfiguration(npc.getName().toLowerCase(), "/npc/" + directory, getPlugin());
            }

            //cargar previus configuration
            npcsConfig.put(npc.getName().toLowerCase(), configuration);
            Location location = npc.getLocation();
            configuration.set("uuid", npc.getUniqueId().toString());
            configuration.set("Location.World", location.getWorld().getName());
            configuration.set("Location.X", location.getX());
            configuration.set("Location.Y", location.getY());
            configuration.set("Location.Z", location.getZ());
            configuration.set("Location.Yaw", location.getYaw());
            configuration.set("Location.Pitch", location.getPitch());
            configuration.set("Hologram", Utils.toStringList(npc.getText()));
            configuration.set("NPCMode", npc.getNPCMode().name().toLowerCase());
            configuration.set("lookAtPlayer", npc.isLookAtPlayer());
            if (npc.getCooldownValidator() == null || !npc.getCooldownValidator().trim().isEmpty()) {
                configuration.set("cooldownValidator", npc.getCooldownValidator());
            }
            configuration.set("position", npc.getPositionMemory().name().toLowerCase());
            if (!npc.getCommand().isEmpty()) {
                configuration.set("Commands", Utils.toStringList(npc.getCommand()));
            }
            if (!npc.getRewardCommands().isEmpty()) {
                configuration.set("RewardCommands", Utils.toStringList(npc.getRewardCommands()));
            }
            if (serializer != null) {
                configuration.set("equipment.helmet", serializer.serialize(npc.getItem(NPCSlot.HELMET)));
                configuration.set("equipment.chestplate", serializer.serialize(npc.getItem(NPCSlot.CHESTPLATE)));
                configuration.set("equipment.leggings", serializer.serialize(npc.getItem(NPCSlot.LEGGINGS)));
                configuration.set("equipment.boots", serializer.serialize(npc.getItem(NPCSlot.BOOTS)));
                configuration.set("equipment.mainhand", serializer.serialize(npc.getItem(NPCSlot.MAINHAND)));
                configuration.set("equipment.offhand", serializer.serialize(npc.getItem(NPCSlot.OFFHAND)));
            }
            configuration.set("Skin", npc.getValueSkin());
            npc.setConfigurationFile(configuration);
            if (GoldenLobby.getInstance().isMysqlEnable()) {
                getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), () -> {
                    try {
                        getNpcdb().createConfiguration(player,
                                configuration, directory.replace("/", "")
                                , callBack);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });
            } else {
                callBack.onSuccess();
            }
        }
    }

    public boolean checkDirectory() {
        File file = new File(getPlugin().getDataFolder(), "/npc/");
        if (!file.isDirectory()) {
            return file.mkdir();
        }
        return true;
    }


    public CustomConfiguration getConfigNPC(String name) {
        return npcsConfig.getOrDefault(name.toLowerCase().replace(".yml", ""), null);
    }

    public HistoryStatsDB getHistoryDB() {
        return historyStatsDB;
    }
}
