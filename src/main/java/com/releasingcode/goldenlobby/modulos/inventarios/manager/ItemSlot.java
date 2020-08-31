package com.releasingcode.goldenlobby.modulos.inventarios.manager;

import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.connections.ServerInfo;
import org.bukkit.inventory.ItemFlag;

import java.util.*;

public class ItemSlot implements Cloneable {
    private String slot;
    private long ticksFrameDelay;
    //private HashMap<Integer, String> materials;
    private final ItemSlotStatusInputs realMaterial;
    private HashMap<Integer, String> names;
    private final ItemSlotStatusInputs lores;
    private ArrayList<String> commands;
    private int position;
    private String lastMaterial;
    private ItemFlag[] flags;
    private boolean glow;
    private int namesMaxValue;
    private final int loresMaxValue;
    private String serverName;

    public ItemSlot() {
        glow = false;
        slot = "1,1";
        ticksFrameDelay = 0;
        names = new HashMap<>();
        lores = new ItemSlotStatusInputs();
        realMaterial = new ItemSlotStatusInputs();
        commands = new ArrayList<>();
        lastMaterial = null;
        namesMaxValue = 0;
        serverName = null;
        loresMaxValue = 0;
        flags = null;
    }

    public int getStaticPosition(int inv) {
        return Math.min(position, inv);
    }

    public String getLastMaterial() {
        return lastMaterial;
    }

    public void setLastMaterial(String lastMaterial) {
        this.lastMaterial = lastMaterial;
    }

    public void setSlot(String slot) {
        this.slot = slot;
        position = getPosition();
    }

    public String getServerName() {
        return serverName;
    }

    public void setServerName(String serverName) {
        this.serverName = serverName;
    }

    public ServerInfo.Estados getStatus() {
        if (serverName != null && !serverName.trim().isEmpty()) {
            return TranslateServer.getStatus(serverName);
        }
        return ServerInfo.Estados.NOT_FOUND;
    }

    public void setGlow(boolean glow) {
        this.glow = glow;
    }

    public boolean isGlowing() {
        return glow;
    }

    public int getPosition() {
        String[] slot = this.slot.split(",");
        int x = Utils.tryParseInt(slot[0].trim()) ? Integer.parseInt(slot[0].trim()) : 0, y = Utils
                .tryParseInt(slot[1].trim()) ? Integer.parseInt(slot[1].trim()) : 0;
        x = Math.max(x, 1);
        y = Math.max(y, 1);
        return ((x - 1) + ((y - 1) * 9));
    }

    public boolean hasNames() {
        return names.size() > 0;
    }

    public ArrayList<String> getCommands() {
        return commands;
    }

    public int keyHightest(HashMap<Integer, String> getMax) {
        Comparator<Integer> comparaValue = Comparator.comparingInt(o -> o);
        return Collections.max(getMax.keySet(), comparaValue) + 1;
    }

    public boolean hasLore() {
        return lores.hasInput();
    }

    public void addFlags(String addFlags) {
        if (addFlags != null) {
            String[] flags = addFlags.split("\\n");
            ArrayList<ItemFlag> flggis = new ArrayList<>();
            for (String preFlag : flags) {
                preFlag = preFlag.toUpperCase();
                try {
                    ItemFlag flagged = ItemFlag.valueOf(preFlag);
                    flggis.add(flagged);
                } catch (Exception ignored) {
                }
            }
            this.flags = flggis.toArray(new ItemFlag[0]);
        }
    }

    public long getTicksFrameDelay() {
        return ticksFrameDelay;
    }

    public void setCommand(ArrayList<String> comandosConfig) {
        this.commands = comandosConfig;
    }

    public void setDelayItemUpdate(int ticksFrameDelay) {
        this.ticksFrameDelay = ticksFrameDelay;
    }

    public Object clone() throws CloneNotSupportedException {
        return super.clone();
    }

    public int getLoresMaxValue() {
        return loresMaxValue;
    }

    public int getNamesMaxValue() {
        return namesMaxValue;
    }

    public ItemSlotInput getItemSlotMaterials() {
        if (!realMaterial.getInputOfServerOn().getInputs().isEmpty() || !realMaterial.getInputOfServerOff().getInputs().isEmpty()) {
            return getStatus() == ServerInfo.Estados.ON ?
                    realMaterial.getInputOfServerOn() : (getStatus() == ServerInfo.Estados.OFF ?
                    realMaterial.getInputOfServerOff() : realMaterial.getInputsDefault()
            );
        }
        return realMaterial.getInputsDefault();
    }

    public void setMaterials(ServerInfo.Estados estados, LinkedHashMap<Integer, String> materiales) {
        // this.materials = materiales;
        //this.materialesMaxValue = keyHightest(this.materials);
        setInputsPerStatus(estados, this.realMaterial, materiales);
    }

    public ItemSlotInput getItemSlotLore() {
        if (!lores.getInputOfServerOn().getInputs().isEmpty() || !lores.getInputOfServerOff().getInputs().isEmpty()) {
            return getStatus() == ServerInfo.Estados.ON ?
                    lores.getInputOfServerOn() : (getStatus() == ServerInfo.Estados.OFF ?
                    lores.getInputOfServerOff() : lores.getInputsDefault()
            );
        }
        return lores.getInputsDefault();
    }

    public HashMap<Integer, String> getLores() {
        return getItemSlotLore().getInputs();
    }

    public void setInputsPerStatus(ServerInfo.Estados estados, ItemSlotStatusInputs base, LinkedHashMap<Integer, String> inputs) {
        switch (estados) {
            case ON: {
                base.getInputOfServerOn().setInput(inputs);
                base.getInputOfServerOn().setInputMaxValue(keyHightest(inputs));
                break;
            }
            case OFF: {
                base.getInputOfServerOff().setInput(inputs);
                base.getInputOfServerOff().setInputMaxValue(keyHightest(inputs));
                break;
            }
            default: {
                base.getInputsDefault().setInput(inputs);
                base.getInputsDefault().setInputMaxValue(keyHightest(inputs));
            }
        }
    }

    public void setLores(ServerInfo.Estados estados, LinkedHashMap<Integer, String> lores) {
        setInputsPerStatus(estados, this.lores, lores);
    }

    public HashMap<Integer, String> getNames() {
        return names;
    }

    public void setNames(HashMap<Integer, String> names) {
        this.names = names;
        this.namesMaxValue = keyHightest(this.names);
    }

    public ItemFlag[] getFlags() {
        return flags;
    }

    public boolean hasServerName() {
        return getServerName() != null && !getServerName().trim().isEmpty();
    }

    /**
     * Esta clase administra
     * Las Entradas del Inventario
     * ya sea server_off, o server_on dentro de la configuración
     */
    public static class ItemSlotInput {
        private HashMap<Integer, String> inputs;
        private int inputsMaxValue;
        private int workIndex;

        ItemSlotInput() {
            inputsMaxValue = 0;
            workIndex = -1;
            inputs = new HashMap<>();
        }

        public int getInputMaxValue() {
            return inputsMaxValue;
        }

        public void setInputMaxValue(int loresMaxValue) {
            this.inputsMaxValue = loresMaxValue;
        }

        public HashMap<Integer, String> getInputs() {
            return inputs;
        }

        public void setInput(HashMap<Integer, String> input) {
            this.inputs = input;
            workIndex = input.keySet().iterator().next();
        }

        public boolean hasInput() {
            return !inputs.isEmpty();
        }

        public void setLastWorkIndex(int index) {
            this.workIndex = index;
        }

        public int getWorkIndex() {
            return workIndex;
        }
    }

    public static class ItemSlotStatusInputs {
        private final ItemSlotInput serverOnInputs; // entradas de server encendido
        private final ItemSlotInput serverOffInputs; // entradas de servidor apagado
        private final ItemSlotInput defaultInputs; // entradas por defecto

        public ItemSlotStatusInputs() {
            this.serverOnInputs = new ItemSlotInput();
            this.serverOffInputs = new ItemSlotInput();
            this.defaultInputs = new ItemSlotInput();
        }

        public ItemSlotInput getInputsDefault() {
            return defaultInputs;
        }

        public ItemSlotInput getInputOfServerOff() {
            return serverOffInputs;
        }

        public ItemSlotInput getInputOfServerOn() {
            return serverOnInputs;
        }

        public boolean hasInput() {
            return defaultInputs.hasInput() || serverOffInputs.hasInput() || serverOnInputs.hasInput();
        }
    }
}
