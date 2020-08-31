package com.releasingcode.goldenlobby.modulos.inventarios;

import com.releasingcode.goldenlobby.LobbyMC;
import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.call.CallBack;
import com.releasingcode.goldenlobby.configuracion.CustomConfiguration;
import com.releasingcode.goldenlobby.connections.ServerInfo;
import com.releasingcode.goldenlobby.loader.LobbyComponente;
import org.apache.commons.codec.binary.Base64;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import com.releasingcode.goldenlobby.database.pubsub.SubChannel;
import com.releasingcode.goldenlobby.database.pubsub.onRedisMessage;
import com.releasingcode.goldenlobby.extendido.nms.IMenu;
import com.releasingcode.goldenlobby.modulos.inventarios.comandos.InventarioCommand;
import com.releasingcode.goldenlobby.modulos.inventarios.comandos.MainCommand;
import com.releasingcode.goldenlobby.modulos.inventarios.db.InventoriesDB;
import com.releasingcode.goldenlobby.modulos.inventarios.db.InventoryFetch;
import com.releasingcode.goldenlobby.modulos.inventarios.db.redis.OnRedisMessageInv;
import com.releasingcode.goldenlobby.modulos.inventarios.listener.ItemInventory;
import com.releasingcode.goldenlobby.modulos.inventarios.listener.ItemMenuListener;
import com.releasingcode.goldenlobby.modulos.inventarios.manager.Inventario;
import com.releasingcode.goldenlobby.modulos.inventarios.manager.ItemSelector;
import com.releasingcode.goldenlobby.modulos.inventarios.manager.ItemSlot;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;

import static org.bukkit.Bukkit.getServer;

public class InventarioPlugin extends LobbyComponente {
    private static InventarioPlugin instance;
    private ArrayList<CustomConfiguration> inventariosConfigs;
    private ArrayList<InventarioCommand> comandos;
    private InventoriesDB dbManager;
    private Class<?> IMenu;
    private boolean iamsender;

    public static InventarioPlugin getInstance() {
        return instance;
    }

    @Override
    protected void onEnable() {
        instance = this;
        iamsender = false;
        Utils.log(" - Cargando modulo Inventarios");
        inventariosConfigs = new ArrayList<>();
        comandos = new ArrayList<>();
        try {
            IMenu = Class.forName(
                    "us.minecub.lobbymc.extendido.nms." + LobbyMC.getVersion() + ".IMenu_" + LobbyMC.getVersion());
        } catch (Exception ignored) {
        }
        new MainCommand(this).register();
        getServer().getPluginManager().registerEvents(new ItemInventory(), getPlugin());
        dbManager = new InventoriesDB(this);
        checkInventories();
        inventoriesLoadFromDisk();
        loadInventories();
        ItemMenuListener.getInstance().register(LobbyMC.getInstance());
        onRedisMessage.registerUpdater(SubChannel.SYNC_INVENTORY, new OnRedisMessageInv());
    }

    public IMenu NMSMenu() {
        if (IMenu == null) {
            Utils.log("IMenu: &cdesabilitado [version incompatible]: " + Utils.getNMSVersion());
            return null;
        }
        try {

            return (IMenu) this.IMenu.getConstructors()[0].newInstance();
        } catch (InstantiationException | IllegalAccessException | InvocationTargetException e) {
            e.printStackTrace();
        }
        return null;
    }

    public boolean isIamsender() {
        return iamsender;
    }

    public void setIamSender(boolean iamsender) {
        this.iamsender = iamsender;
    }

    public void fetchFromDatabase(CallBack.SingleCallBack callBack) {
        getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(),
                () -> getDbManager().fetchInventories(new CallBack.ReturnCallBack<ArrayList<InventoryFetch>>() {
                    @Override
                    public void onSuccess(ArrayList<InventoryFetch> callback) {
                        if (callback.isEmpty()) {
                            return;
                        }
                        for (InventoryFetch FETCH : callback) {
                            boolean isInLocal = false;
                            for (Inventario inv : Inventario.getInventories()) {
                                String name = inv.getCustomConfiguration().getFile().getName().replace(".yml", "");
                                if (!name.toLowerCase().equals(FETCH.getName())) {
                                    continue;
                                }
                                isInLocal = true;
                                //ACTUALIZAR
                                Utils.log("Configuracion para: " + name + ", actualizando...");
                                byte[] bytesconfig = Base64.decodeBase64(FETCH.getBase64());
                                String text = new String(bytesconfig, StandardCharsets.UTF_8);
                                inv.getCustomConfiguration().updateFile(text);
                                loadInventory(inv.getCustomConfiguration());
                                Utils.log("Configuracion: " + name + " actualizada!");
                            }
                    /*
                        Crear el archivo localmente
                     */
                            if (!isInLocal) {
                                File folderInventory = new File(getPlugin().getDataFolder(), "/inventarios/");
                                if (folderInventory.isDirectory()) {
                                    byte[] bytesconfig = Base64.decodeBase64(FETCH.getBase64());
                                    String text = new String(bytesconfig, StandardCharsets.UTF_8);
                                    File inventario = new File(getPlugin().getDataFolder(),
                                            "/inventarios/" + FETCH.getName() + ".yml");
                                    try {
                                        FileWriter fw = new FileWriter(inventario, false);
                                        fw.write(text);
                                        fw.close();
                                    } catch (Exception e) {
                                        Utils.log("No se pudo sincronizar un archivo " + FETCH
                                                .getName() + " , ocurrio un error de escritura: " + e.getMessage());
                                    }
                                    CustomConfiguration inv = new CustomConfiguration(inventario, getPlugin());
                                    inventariosConfigs.add(inv);
                                    loadInventory(inv);
                                    Utils.log("Generando inventario desde base de datos: " + FETCH.getName());
                                }
                            }
                        }
                        callBack.onSuccess();
                    }

                    @Override
                    public void onError(ArrayList<InventoryFetch> callback) {
                        callBack.onError();
                    }
                }));
    }

    public void sendSync(SubChannel.SubOperation operation, CallBack.SingleCallBack callBack) {
        getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            try {
                setIamSender(true);
                LobbyMC.getInstance().getRedisManager().pub(SubChannel.SYNC_INVENTORY.tobyte(), operation.tobyte());
                callBack.onSuccess();
            } catch (Exception e) {
                setIamSender(false);
                callBack.onError();
            }
        });
    }

    public InventoriesDB getDbManager() {
        return dbManager;
    }

    @Override
    protected void onDisable() {
        Utils.log(" - Desabilitando modulo de Inventarios");
        this.inventariosConfigs.clear();
        LobbyMC.getInstance().getServerManager().clear();
        Inventario.clear();
        for (InventarioCommand commandos : comandos) {
            commandos.unregister();
        }
    }


    public void syncConfig(CommandSender sender, String who, CustomConfiguration customConfiguration, CallBack.SingleCallBack callBack) {
        getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            sender.sendMessage("§aSincronizando inventarios, por favor espere!");
            try {
                getDbManager().createConfiguration(who, customConfiguration, callBack);
            } catch (IOException e) {
                sender.sendMessage("§4Error mientras se sincronizaba la configuración: " + customConfiguration.getFile()
                        .getName());
                callBack.onError();
                e.printStackTrace();
            }
        });
    }


    public void reloadInventories(CallBack.SingleCallBack callBack, boolean fetchdb) {
        getPlugin().getServer().getScheduler().runTaskAsynchronously(getPlugin(), () -> {
            this.inventariosConfigs.clear();
            LobbyMC.getInstance().getServerManager().clear();
            Inventario.clear();
            LobbyMC.getInstance().serversConnections();
            if (!fetchdb) {
                checkInventories();
                inventoriesLoadFromDisk();
                for (InventarioCommand commandos : comandos) {
                    commandos.unregister();
                }
                loadInventories();
            } else {
                fetchFromDatabase(new CallBack.SingleCallBack() {
                    @Override
                    public void onSuccess() {
                        callBack.onSuccess();
                    }

                    @Override
                    public void onError() {
                        callBack.onError();
                    }
                });
                checkInventories();
            }
            callBack.onSuccess();
        });

    }

    @SuppressWarnings("unchecked")
    public void loadInventory(CustomConfiguration customConfiguration) {
        FileConfiguration config = customConfiguration.getConfig();
        Inventario inventario = new Inventario(customConfiguration);
        String comando = config.getString("Comando", null);
        List<String> titulos = new ArrayList<>();
        long titleticks = config.getLong("TituloActualizacion", 0);
        int filas = config.getInt("Filas", 1);
        if (config.isConfigurationSection("TituloMenu")) {
            ConfigurationSection tituloMenu = config.getConfigurationSection("TituloMenu");
            for (String keys : tituloMenu.getKeys(true)) {
                String value = tituloMenu.getString(keys);
                titulos.add(value);
            }
        }
        inventario.setTitleTicks(titleticks);
        inventario.setTitulo(titulos);
        inventario.setRow(filas);
        if (comando != null && !comando.trim().isEmpty()) {
            inventario.setComando(comando);
            InventarioCommand invcommand = new InventarioCommand(inventario);
            invcommand.register();
            comandos.add(invcommand);
        }
            /*
             Init Selector Item
             Aqui se establece datos desde la config al objecto del item que permitirá abrir el inventario
             */
        String itemBuild = config.getString("Item-Selector", "stone:1:0");
        String name = config.getString("Item-Selector-Name", "");
        int slot = config.getInt("Item-Selector-Slot", -1);
        String[] lore = config.getString("Item-Selector-Lore", "").split("\\n");
        ItemSelector selector = inventario.getItemSelector();
        selector.setBuilderString(itemBuild);
        selector.setLore(lore);
        selector.setSlot(slot);
        selector.setName(name);
        inventario.makeItemSelector();
        if (config.isList("Items")) {
            List<?> items = config.getList("Items");
            for (Object item : items) {
                LinkedHashMap<String, Object> reading = (LinkedHashMap<String, Object>) item;
                String slotXY = reading.get("Slot").toString().trim().replaceAll("\\s{2,}", " ");
                ItemSlot atItem = new ItemSlot();
                atItem.setSlot(slotXY);
                int delayUpdateItem = Utils.tryParseInt(reading.getOrDefault("ItemUpdateDelay", 0).toString()) ?
                        Integer.parseInt(reading.getOrDefault("ItemUpdateDelay", 0).toString()) : 0;
                atItem.setDelayItemUpdate(delayUpdateItem);
                ArrayList<Object[]> retriveEvaluationMaterial = evaluateInputs(reading, "Material");
                for (Object[] neccesary : retriveEvaluationMaterial) {
                    atItem.setMaterials((ServerInfo.Estados) neccesary[0], (LinkedHashMap<Integer, String>) neccesary[1]);
                }
                LinkedHashMap<Integer, String> namesConfig = new LinkedHashMap<>();
                if (reading.get("Name") instanceof LinkedHashMap) {
                    namesConfig = (LinkedHashMap<Integer, String>) reading.get("Name");
                } else if (reading.get("Name") instanceof String) {
                    namesConfig.put(0, (String) reading.get("Name"));
                } else {
                    ArrayList<String> namesFetch = (ArrayList<String>) reading.get("Name");
                    for (int i = 0; i < namesFetch.size(); i++) {
                        namesConfig.put(i, namesFetch.get(i));
                    }
                }
                atItem.setNames(namesConfig);
                ArrayList<Object[]> retriveEvaluationLore = evaluateInputs(reading, "Lore");
                for (Object[] neccesary : retriveEvaluationLore) {
                    atItem.setLores((ServerInfo.Estados) neccesary[0], (LinkedHashMap<Integer, String>) neccesary[1]);
                }

                boolean glow = (boolean) reading.getOrDefault("glow", false);
                atItem.setGlow(glow);
                String addFlags = (String) reading.getOrDefault("AddFlags", null);
                atItem.addFlags(addFlags);
                //atItem.setLores(loreConfig);
                String serverName = (String) reading.get("Server");
                atItem.setServerName(serverName);
                ArrayList<String> comandosConfig = new ArrayList<>();
                if (reading.get("Command") instanceof List) {
                    comandosConfig = (ArrayList<String>) reading.get("Command");
                } else {
                    comandosConfig.add((String) reading.get("Command"));
                }
                atItem.setCommand(comandosConfig);
                inventario.addItems(atItem);
            }
        }
        inventario.itemsSync();
        Inventario.addInventario(customConfiguration.getFile().getName().replace(".yml", ""), inventario);
    }

    @SuppressWarnings("unchecked")
    public ArrayList<Object[]> evaluateInputs(LinkedHashMap<String, Object> reading, String evaluateTo) {
        ArrayList<Object[]> result = new ArrayList<>();
        if (reading.get(evaluateTo) instanceof LinkedHashMap) {
            LinkedHashMap<Object, String> loreByKeysChecker = (LinkedHashMap<Object, String>) reading.get(evaluateTo);
            LinkedHashMap<Integer, String> loreByKeysInteger;
            LinkedHashMap<String, Object> loreByKeysString;
            Object Typekeys = getTypeKeys(loreByKeysChecker);
            if (Typekeys instanceof Integer) {
                        /*
                        Obtiene Lore por defecto siempre será así
                         */
                loreByKeysInteger = (LinkedHashMap<Integer, String>) reading.get(evaluateTo);
                result.add(new Object[]{ServerInfo.Estados.NOT_FOUND, loreByKeysInteger});
            } else {
                        /*
                            Aquí retornamos keys
                              - server_on
                              - server_off
                                (que pueden contener multiples lores)
                              ejm:
                              server_on:
                                0: |-
                                  &aHola
                                1: |-
                                  &eLore 2
                             */
                loreByKeysString = (LinkedHashMap<String, Object>) reading.get(evaluateTo);
                if (loreByKeysString.containsKey("server_on")) {
                    if (loreByKeysString.get("server_on") instanceof LinkedHashMap) {
                        LinkedHashMap<Integer, String> multipleAnimation = (LinkedHashMap<Integer, String>) loreByKeysString
                                .get("server_on");
                        result.add(new Object[]{ServerInfo.Estados.ON, multipleAnimation});
                    } else {
                        LinkedHashMap<Integer, String> singleText = new LinkedHashMap<>();
                        singleText.put(0, (String) loreByKeysString.get("server_on"));
                        result.add(new Object[]{ServerInfo.Estados.ON, singleText});
                    }
                }
                if (loreByKeysString.containsKey("server_off")) {
                    if (loreByKeysString.get("server_off") instanceof LinkedHashMap) {
                        LinkedHashMap<Integer, String> multipleAnimation = (LinkedHashMap<Integer, String>) loreByKeysString
                                .get("server_off");
                        result.add(new Object[]{ServerInfo.Estados.OFF, multipleAnimation});
                    } else {
                        LinkedHashMap<Integer, String> singleText = new LinkedHashMap<>();
                        singleText.put(0, (String) loreByKeysString.get("server_off"));
                        result.add(new Object[]{ServerInfo.Estados.OFF
                                , singleText});
                    }
                }
            }
        } else {
                    /*
                    Carga el modelo por defecto sin (server_off)(server_on)
                     */
            LinkedHashMap<Integer, String> loreConfig = new LinkedHashMap<>();
            loreConfig.put(0, (String) reading.get(evaluateTo));
            result.add(new Object[]{ServerInfo.Estados.NOT_FOUND, loreConfig});
        }

        return result;
    }

    public Object getTypeKeys(LinkedHashMap<Object, String> datas) {
        for (Object keys : datas.keySet()) {
            if (keys instanceof Integer) {
                return 0;
            }
        }
        return "";
    }

    public void loadInventories() {
        for (CustomConfiguration customConfiguration : inventariosConfigs) {
            loadInventory(customConfiguration);
        }
    }

    public void inventoriesLoadFromDisk() {
        File inventarios = new File(getPlugin().getDataFolder(), "/inventarios/");
        if (inventarios.isDirectory()) {
            File[] files = inventarios.listFiles();
            if (files != null) {
                if (files.length > 0) {
                    for (File f : files) {
                        if (f.getName().toLowerCase().endsWith(".yml")) {
                            //agrego el inventario a la lista de configuración
                            this.inventariosConfigs.add(new CustomConfiguration(f, getPlugin()));
                        }
                    }
                }
            }
        }
    }

    public void checkInventories() {
        /*
        Esto comprueba si la ruta /plugins/LobbyMC/inventarios
        existe, si no existe crea la ruta con el archivo
        serverselector.yml por defefecto que está en el
        resource de este complemento
         */
        String serverSelectorFile = "serverselector.yml";
        File folderInventory = new File(getPlugin().getDataFolder(), "/inventarios/");
        if (!folderInventory.isDirectory()) {
            if (folderInventory.mkdirs()) {
                File serverselector = new File(getPlugin().getDataFolder(), "/inventarios/" + serverSelectorFile);
                CustomConfiguration.copyDefaultConfig(serverselector, "inventarios/" + serverSelectorFile, getPlugin());
            }
        }
    }
}
