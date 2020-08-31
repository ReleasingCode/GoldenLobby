package com.releasingcode.goldenlobby.modulos.inventarios.manager;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;
import com.releasingcode.goldenlobby.LobbyMC;
import com.releasingcode.goldenlobby.connections.ServerInfo;
import com.releasingcode.goldenlobby.connections.ServerManager;
import com.releasingcode.goldenlobby.managers.ItemStackBuilder;
import com.releasingcode.goldenlobby.managers.LobbyPlayer;
import com.releasingcode.goldenlobby.managers.indexing.LobbyPlayerIndexing;
import com.releasingcode.goldenlobby.npc.internal.MinecraftVersion;
import es.minecub.core.utils.StringUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.InventoryView;
import com.releasingcode.goldenlobby.modulos.inventarios.builder.ItemClickEvent;
import com.releasingcode.goldenlobby.modulos.inventarios.builder.ItemMenuHolder;
import com.releasingcode.goldenlobby.modulos.inventarios.builder.MenuItem;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class ItemMenu {
    private final LobbyPlayer lobbyPlayer;
    private final ArrayList<ItemSlot> slots;
    private final Inventario inventario;
    private final HashMap<Integer, ClickItem> clickEvents;
    private String name;
    private Size size;
    private MenuItem[] items;
    private Inventory bukkitInventory;


    public ItemMenu(LobbyPlayer player, String name, Size size, Inventario inventario) {
        this.lobbyPlayer = player;
        this.name = name;
        this.size = size;
        this.items = new MenuItem[size.getSize()];
        this.inventario = inventario;
        this.slots = new ArrayList<>(inventario.getItemSlots());
        clickEvents = new HashMap<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;

    }

    public Size getSize() {
        return size;
    }

    public void tick() {
        slots.forEach(slot -> updateItemSlot(slot, false));
        LobbyPlayerIndexing indexing = lobbyPlayer.getInventoryManager().getIndexing();
        int tick = indexing.getAndIncrementIndexAtExtra("sendPacket", 22);
        if (tick >= 21) {
            update();
        }
    }


    public void updateItemSlot(ItemSlot itemSlot, boolean forceUpdate) {
        Player player = this.lobbyPlayer.getPlayer();
        if (player == null) {
            return;
        }
        if (player.getOpenInventory() != null) {
            if (player.getOpenInventory().getTopInventory() != null) {
                if (player.getOpenInventory().getTopInventory().getHolder() instanceof ItemMenuHolder) {
                    LobbyPlayerIndexing indexing = lobbyPlayer.getInventoryManager().getIndexing();
                    ServerInfo.Estados estado = itemSlot.getStatus();
                    int tick = indexing.getAndIncrementIndexAtExtra("tick="
                            + itemSlot.getStaticPosition(size.size), (itemSlot.getTicksFrameDelay() + 1));
                    if (forceUpdate || itemSlot.getTicksFrameDelay() > 0 && tick >= itemSlot.getTicksFrameDelay()) {
                        ItemSlot.ItemSlotInput materialsInputs = itemSlot.getItemSlotMaterials();
                        int index = indexing.getAndIncrementIndexAtInt(itemSlot.
                                        getStaticPosition(size.size),
                                materialsInputs.getInputMaxValue());
                        String material = null;
                        if (materialsInputs.getInputs().containsKey(index)) {
                            material = materialsInputs.getInputs().get(index);
                            material = material.replace("{player}", player.getName());
                            itemSlot.setLastMaterial(material);
                        }
                        ItemStackBuilder lastBuilder = new ItemStackBuilder(material == null ?
                                itemSlot.getLastMaterial() != null ?
                                        itemSlot.getLastMaterial() : "stone:1:0" : material);
                        if (itemSlot.hasNames()) {
                            int indexNames = indexing.getAndIncrementIndexAtExtra("names" + itemSlot.getPosition(),
                                    itemSlot.getNamesMaxValue());
                            if (itemSlot.getNames().containsKey(indexNames)) {
                                String name = itemSlot.getNames().get(indexNames);
                                name = new TranslateServer(name).text();
                                lastBuilder.setName(name);
                            }
                        }
                        if (itemSlot.isGlowing()) {
                            if (LobbyMC.getMinecraftVersion().isAboveOrEqual(MinecraftVersion.V1_14_R1)) {

                            } else {
                                lastBuilder.addEnchantment(new ItemStackBuilder.GlowEnchantment(), 1);
                            }
                        }
                        if (itemSlot.getFlags() != null && itemSlot.getFlags().length > 0) {
                            lastBuilder.addFlag(itemSlot.getFlags());
                        }
                        if (itemSlot.hasLore()) {

                            ItemSlot.ItemSlotInput ItemLoreSlot = itemSlot.getItemSlotLore();
                            int indexLores = indexing
                                    .getAndIncrementIndexAtExtra("lores-" + estado.name().toLowerCase() +
                                            itemSlot.getStaticPosition(size.size), ItemLoreSlot.getInputMaxValue());
                            if (ItemLoreSlot.getInputs().containsKey(indexLores)) {
                                String lore;
                                lore = ItemLoreSlot.getInputs().get(indexLores);
                                ItemLoreSlot.setLastWorkIndex(indexLores);
                                ArrayList<String> atLore;
                                if (itemSlot.hasServerName()) {
                                    lore = TranslateServer.TranslateServer(itemSlot.getServerName(), lore);
                                } else {
                                    lore = new TranslateServer(lore).text();
                                }
                                lore = ServerManager.translateVar(lore, null);
                                atLore = new ArrayList<>(Arrays.asList(lore.split("\\n")));
                                atLore = loreUpdater(itemSlot.getServerName(), atLore);
                                lastBuilder.addLore(atLore);
                            } else {
                                String lore = ItemLoreSlot.getInputs().get(ItemLoreSlot.getWorkIndex());
                                if (itemSlot.hasServerName()) {
                                    lore = TranslateServer.TranslateServer(itemSlot.getServerName(), lore);
                                    lore = ServerManager.translateVar(lore, null);
                                } else {
                                    lore = ItemLoreSlot.getInputs().get(indexLores);
                                    lore = new TranslateServer(lore).text();
                                }
                                lore = ServerManager.translateVar(lore, null);
                                ArrayList<String> atLore = new ArrayList<>(Arrays.asList(lore.split("\\n")));
                                atLore = loreUpdater(itemSlot.getServerName(), atLore);
                                lastBuilder.addLore(atLore);
                            }
                        }
                        ClickItem click = clickEvents.containsKey(itemSlot.getPosition())
                                ? clickEvents.get(itemSlot.getPosition())
                                : clickEvents.computeIfAbsent(itemSlot.getPosition(),
                                (key) -> new ClickItem(itemSlot, lastBuilder.build()));
                        click.setIcon(lastBuilder.build());
                        setItem(itemSlot.getStaticPosition(size.size), click);
                    }
                }
            }
        }
    }

    public ArrayList<String> loreUpdater(String serverName, ArrayList<String> preLore) {
        LinkedList<String> updateLore = new LinkedList<>(preLore);
        if (serverName != null && !serverName.trim().isEmpty()) {
            for (String text : updateLore) {
                if (text.trim().contains("{automaticmotd}")) {
                    updateLore.remove(text);
                    ServerInfo managersv = ServerManager.getServerManager(serverName);
                    if (managersv != null) {
                        try {
                            updateLore.addAll(getServerDataMotd(managersv));
                        } catch (Exception e) {
                        }
                        return new ArrayList<>(updateLore);
                    }
                }
            }
            return new ArrayList<>(updateLore);
        }

        Pattern motd = Pattern.compile("\\{(.*)_automaticmotd}");
        Matcher match_motd;
        for (String text : updateLore) {
            match_motd = motd.matcher(text.trim());
            if (match_motd.find()) {
                updateLore.remove(text);
                ServerInfo managersv = ServerManager.getServerManager(match_motd.group(1));
                if (managersv != null) {
                    try {
                        updateLore.addAll(getServerDataMotd(managersv));
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    return new ArrayList<>(updateLore);
                }
            }
        }
        return new ArrayList<>(updateLore);
    }

    public boolean checkServer(String[] beforeLore) {
        int index = 0;
        for (String text : beforeLore) {
            if (text.trim().contains("motd")) {
                return true;
            }
        }
        return false;
    }

    public List<String> getServerDataMotd(ServerInfo server) throws IllegalStateException {
        if (server.getName().trim().toLowerCase().startsWith("dn-")) {
            List<String> list = new ArrayList<>(Arrays.asList(
                    "",
                    "${lobby.games.server.players}[" + server.getOnline() + ", " + server.getMaxplayers() + "]"));

            if (StringUtils.isValidJson(server.getMotd())) {
                if (StringUtils.parseJson(server.getMotd()).getAsJsonObject() == null) {
                    list.add(ChatColor.RED + "¡Sin información!");
                    return list;
                }

                JsonObject obj = StringUtils.parseJson(server.getMotd()).getAsJsonObject();
                String map = obj.get("map").getAsString();

                if (map.equals("unknown")) { // El mapa todavía no se ha decidido
                    list.add(1, "${lobby.games.server.map.unknown}");
                } else {
                    list.add(1, "${lobby.games.server.map}[" + map + "]");
                }

                String state = obj.get("state").getAsString();

                if (state.equals("WAITING")) {
                    int playersLeft = obj.get("playersLeft").getAsInt();

                    list.add(
                            "${lobby.games.server.status}${lobby.games.server.dn.status.playersleft." + (playersLeft == 1 ? "singular" : "plural") + "}[" + playersLeft + "]");
                } else if (state.equalsIgnoreCase("STARTING")) {
                    list.add("${lobby.games.server.status}${lobby.games.server.status.starting}");
                } else {
                    list.add("${lobby.games.server.dn.phase}[" + obj.get("phase").getAsString() + "]");
                    list.add("");

                    for (JsonElement el : obj.get("teams").getAsJsonArray()) {
                        JsonObject teamObj = el.getAsJsonObject();

                        int health = teamObj.get("health").getAsInt();

                        if (health != 0) {
                            list.add(teamObj.get("color").getAsString() + "${anni.scoreboard.team.nexus}["
                                    + teamObj.get("translation").getAsString() + "]" + ChatColor.RESET + health);
                        }
                    }
                }

            } else {
                String[] data = server.getMotd() != null ? server.getMotd().split(";") : null;
                if (data == null) {
                    return new ArrayList<>();
                }
                if (data.length > 2) {
                    list.add("${lobby.games.server.dn.phase}[" + data[1] + "]");
                    list.add("");

                    int redNexus = Integer.parseInt(data[2]);

                    if (redNexus > 0) {
                        list.add("${lobby.games.server.dn.nexus.red}[" + redNexus + "]");
                    }

                    int blueNexus = Integer.parseInt(data[3]);

                    if (blueNexus > 0) {
                        list.add("${lobby.games.server.dn.nexus.blue}[" + blueNexus + "]");
                    }

                    int yellowNexus = Integer.parseInt(data[4]);

                    if (yellowNexus > 0) {
                        list.add("${lobby.games.server.dn.nexus.yellow}[" + yellowNexus + "]");
                    }

                    int greenNexus = Integer.parseInt(data[5]);

                    if (greenNexus > 0) {
                        list.add("${lobby.games.server.dn.nexus.green}[" + greenNexus + "]");
                    }
                } else if (data[1].contains("Comenzando")) {
                    list.add("${lobby.games.server.status}${lobby.games.server.status.starting}");
                } else {
                    list.add("${lobby.games.server.status}${lobby.games.server.dn.status.playersleft."
                            + (data[1].equals("1") ? "singular" : "plural") + "}[" + data[1] + "]");
                }

                if (server.getName().startsWith("DN-")) { // Si es DN le ponemos el nombre del mapa. Si es MiniDN, no
                    list.add(1, "${lobby.games.server.map}[" + data[0] + "]");
                }

            }
            list.add("");
            list.add("${lobby.games.server.join}");

            return list;
        } else {
            return Arrays.asList(
                    "",
                    "${lobby.games.server.players}[" + server.getOnline() + ", " + server.getMaxplayers() + "]",
                    "${lobby.games.server.status}" + ChatColor.stripColor(server.getMotd()),
                    "",
                    "${lobby.games.server.join}");
        }
    }

    public void setItem(int position, MenuItem menuItem) {
        items[position] = menuItem;
    }

    public void update() {
        Player player = lobbyPlayer.getPlayer();
        if (player == null) {
            return;
        }
        InventoryView inventory = player.getOpenInventory();
        if (inventory != null
                && (inventory.getTopInventory()).getHolder()
                instanceof ItemMenuHolder && ((ItemMenuHolder) inventory.getTopInventory().getHolder()).getMenu()
                .equals(this)) {
            this.apply(inventory.getTopInventory(), player);
            inventario.getNMSMenu().setItemContents(player, items);
        }
    }


    private void apply(Inventory inventory, Player player) {
        int i = 0;
        while (i < this.items.length) {
            if (this.items[i] != null) {
                inventory.setItem(i, this.items[i].getIcon());
            }
            ++i;
        }
    }

    public void open() {
        Player player = lobbyPlayer.getPlayer();
        if (player == null) {
            return;
        }
        bukkitInventory = Bukkit.createInventory(
                new ItemMenuHolder(this, Bukkit.createInventory(player,
                        size.getSize()), inventario),
                size.getSize(), ChatColor.translateAlternateColorCodes('&', name));
        player.openInventory(bukkitInventory);
        slots.forEach(S -> updateItemSlot(S, true));
        update();
    }

    public Inventory getBukkitInventory() {
        return bukkitInventory;
    }


    public void onInventoryClick(InventoryClickEvent event) {
        int slot = event.getRawSlot();
        if (slot >= 0 && slot < size.getSize() && items[slot] != null) {
            Player player = (Player) event.getWhoClicked();
            ItemClickEvent itemClickEvent = new ItemClickEvent(player, event.getCurrentItem(), event.getClick());
            items[slot].onItemClick(itemClickEvent);
            if (itemClickEvent.willUpdate()) {
                update();
            } else {
                //player.updateInventory();
                if (itemClickEvent.willClose() || itemClickEvent.willGoBack()) {
                    Bukkit.getScheduler().scheduleSyncDelayedTask(LobbyMC.getInstance(), player::closeInventory, 0);
                }
                /*if (itemClickEvent.willGoBack() && hasParent()) {
                    final String playerName = player.getName();
                    Bukkit.getScheduler().scheduleSyncDelayedTask(LobbyMC.getInstance(), () -> {
                        Player p = Bukkit.getPlayerExact(playerName);
                        if (p != null) {
                            parent.open(p);
                        }
                    }, 3);
                }*/
            }

        }

    }

    public void destroy() {
        name = null;
        size = null;
        items = null;
    }

    public Inventario getInventario() {
        return inventario;
    }

    public enum Size {
        ONE_LINE(9), TWO_LINE(18), THREE_LINE(27), FOUR_LINE(36), FIVE_LINE(45), SIX_LINE(54);

        private final int size;

        Size(int size) {
            this.size = size;
        }

        public static Size row(int row) {
            if (row == 1) {
                return ONE_LINE;
            } else if (row == 2) {
                return TWO_LINE;
            } else if (row == 3) {
                return THREE_LINE;
            } else if (row == 4) {
                return FOUR_LINE;
            } else if (row == 5) {
                return FIVE_LINE;
            } else {
                return SIX_LINE;
            }
        }

        public static Size fit(int slots) {
            if (slots < 10) {
                return ONE_LINE;
            } else if (slots < 19) {
                return TWO_LINE;
            } else if (slots < 28) {
                return THREE_LINE;
            } else if (slots < 37) {
                return FOUR_LINE;
            } else if (slots < 46) {
                return FIVE_LINE;
            } else {
                return SIX_LINE;
            }
        }

        public int getSize() {
            return size;
        }
    }
}