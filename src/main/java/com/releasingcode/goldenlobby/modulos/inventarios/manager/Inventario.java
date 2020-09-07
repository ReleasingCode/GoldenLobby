package com.releasingcode.goldenlobby.modulos.inventarios.manager;

import com.releasingcode.goldenlobby.configuracion.CustomConfiguration;
import com.releasingcode.goldenlobby.extendido.nms.IMenu;
import com.releasingcode.goldenlobby.managers.ItemStackBuilder;
import com.releasingcode.goldenlobby.managers.LobbyPlayer;
import com.releasingcode.goldenlobby.managers.LobbyPlayerMap;
import com.releasingcode.goldenlobby.managers.indexing.LobbyPlayerIndexing;
import com.releasingcode.goldenlobby.modulos.inventarios.InventarioPlugin;
import com.releasingcode.goldenlobby.modulos.inventarios.builder.ItemMenuHolder;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public class Inventario implements Cloneable {
    private static final List<ItemSelector> itemsOpenInventory = new ArrayList<>();
    private static final Map<String, Inventario> inventario = new HashMap<>();
    private final Map<UUID, LobbyPlayer> openInventories = new ConcurrentHashMap<>();
    private final List<ItemSlot> itemSlots;
    private final CustomConfiguration customConfiguration;
    private final ScheduledExecutorService thread;
    private final IMenu NMSMenu;
    private String Comando;
    private List<String> Titulo;
    private int row;
    private long titleTicks;
    private int Items_Inventory_Updater = 5;
    private ItemSelector itemSelector;
    private ScheduledFuture<?> scheduledFuture;

    public Inventario(CustomConfiguration customConfiguration) {
        thread = Executors.newScheduledThreadPool(1);
        this.customConfiguration = customConfiguration;
        itemSelector = new ItemSelector(this);
        itemSlots = new ArrayList<>();
        NMSMenu = InventarioPlugin.getInstance().NMSMenu();
    }

    public int getItems_Inventory_Updater() {
        return Items_Inventory_Updater;
    }

    public void setItems_Inventory_Updater(int items_Inventory_Updater) {
        Items_Inventory_Updater = items_Inventory_Updater;
    }

    public static void addInventario(String file, Inventario inv) {
        inventario.put(file.toLowerCase(), inv);
    }

    public static Inventario getInventoryByName(String name) {
        return inventario.getOrDefault(name.toLowerCase(), null);
    }

    public static void clearInventories() {
        inventario.clear();
    }

    public static boolean isEqualsTo(ItemStack stack, ItemStack hand) {
        if (stack != null) {
            ItemMeta meta = stack.getItemMeta();
            if (meta != null) {
                String displayName = meta.getDisplayName();
                if (displayName != null) {
                    if (hand.getType() == stack.getType()) {
                        String displayNameSelector = hand.getItemMeta() != null
                                ? (hand.getItemMeta().getDisplayName() != null
                                ? hand.getItemMeta().getDisplayName() : "") : "";
                        return displayName.equals(displayNameSelector);
                    }

                }
            }
        }
        return false;
    }

    public static ItemSelector getItemByItemStack(ItemStack stack) {
        if (stack != null) {
            ItemMeta meta = stack.getItemMeta();
            if (meta != null) {
                String displayName = meta.getDisplayName();
                if (displayName != null) {
                    for (ItemSelector selectors : itemsOpenInventory) {
                        ItemStack selectorStack = selectors.getItem();
                        if (selectorStack.getType() == stack.getType()) {
                            String displayNameSelector = selectorStack.getItemMeta() != null ? selectorStack
                                    .getItemMeta().getDisplayName() != null ? selectorStack.getItemMeta()
                                    .getDisplayName() : "" : "";
                            if (displayName.equals(displayNameSelector)) {
                                if (selectors.hasActive()) {
                                    return selectors;
                                }
                            }
                        }
                    }
                }
            }
        }
        return null;
    }

    public static void clear() {
        for (Inventario inv : getInventories()) {
            inv.stopTask();
        }
        itemsOpenInventory.clear();
        clearInventories();
    }

    public static void clearItemsSelectorToPlayer(Player p) {
        for (ItemStack stack : p.getInventory().getContents()) {
            if (ItemSelector.hasItemSelector(stack)) {
                stack.setType(Material.AIR);
                p.updateInventory();
            }
        }
    }

    public static void setSelectorToPlayer(Player p) {
        for (ItemSelector selector : getItemsSelector()) {
            if (selector.getInventario() != null && selector.getItem() != null && selector.hasActive()) {
                PlayerInventory inventory = p.getInventory();
                ItemStack perHead = new ItemStackBuilder(
                        selector.getBuilderString().replace("{player}", p.getName()))
                        .setName(selector.getName())
                        .addLore(selector.getLore())
                        .build();
                inventory.setItem(selector.getSlot(), perHead);
            }
        }
    }

    /*
    Cuando se hace un reload no se va a eliminar nada
    simplemente se tiene que volver a limpiar la lista
    y agregar nuevamente los elementos :)
     */
    public static List<ItemSelector> getItemsSelector() {
        return itemsOpenInventory;
    }

    public static ArrayList<Inventario> getInventories() {
        return new ArrayList<>(inventario.values());
    }

    public CustomConfiguration getCustomConfiguration() {
        return customConfiguration;
    }

    public void setTitleTicks(long ticks) {
        this.titleTicks = ticks;
    }

    public void stopTask() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
            if (thread != null) {
                thread.shutdown();
            }
        }
    }

    public void makeItemSelector() {
        ItemSelector selector = itemSelector.build();
        itemsOpenInventory.add(selector);
    }

    public ItemSelector getItemSelector() {
        return itemSelector;
    }

    public void setItemSelector(ItemSelector itemSelector) {
        this.itemSelector = itemSelector;
    }

    /*
    Actualización de inventario [Cambiar directamente por jugador]
    @Inventory Update
     */
    public void itemUpdater() {
        if (scheduledFuture != null) {
            scheduledFuture.cancel(true);
        }
        List<Long> minorTicksFrame = getItemSlots().stream().map(ItemSlot::getTicksFrameDelay).collect(Collectors.toCollection(ArrayList::new));
        List<Long> disableChecker = minorTicksFrame.stream().filter(number -> number == 0).collect(Collectors.toList());
        long min = Collections.min(minorTicksFrame);
        if (disableChecker.size() == getItemSlots().size()) {
            min = -1; //disable
        } else {
            if (min < 1) {
                min = 1;
            }
        }
        setItems_Inventory_Updater((int) min);
        if (min > 0) {
            scheduledFuture =
                    thread.scheduleAtFixedRate(() -> {
                        for (LobbyPlayer lp : LobbyPlayerMap.getPlayers()) {
                            try {
                                ItemMenu menu = lp.getInventoryManager().getMenu();
                                if (menu != null) {
                                    menu.tick();
                                }
                                updatePlayers(lp);
                            } catch (Exception e) {
                            }
                        }
                    }, 0, 50, TimeUnit.MILLISECONDS);
        }
    }

    public List<ItemSlot> getItemSlots() {
        return itemSlots;
    }

    public IMenu getNMSMenu() {
        return NMSMenu;
    }

    public void updatePlayers(LobbyPlayer lp) {
        Player player = lp.getPlayer();
        if (player != null) {
            if (player.getOpenInventory() != null) {
                if (NMSMenu != null && titleTicks > 0) {
                    if (player.getOpenInventory().getTopInventory() != null) {
                        if (player.getOpenInventory().getTopInventory().getHolder() != null) {
                            if (player.getOpenInventory().getTopInventory().getHolder() instanceof ItemMenuHolder) {
                                List<String> titulo = getTitulo();
                                LobbyPlayerIndexing indexing = lp.getInventoryManager().getIndexing();
                                int ticking = indexing.getAndIncrementIndexAtExtra("title-ticking", titleTicks + 1);
                                if (ticking >= titleTicks) {
                                    int index = indexing.getAndIncrementIndexAtExtra("title", titulo.size());
                                    String title = new TranslateServer(titulo.get(index)).text();
                                    NMSMenu.setTextInventory(player, title);
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    public void openInventory(Player player) {
        LobbyPlayer lp = LobbyPlayerMap.getJugador(player);
        if (lp != null) {
            lp.getInventoryManager().resetIndexing();
            String beforeUpdate = new TranslateServer(Titulo.size() > 0 ? Titulo.get(0) : "").text();
            ItemMenu menu = new ItemMenu(lp, beforeUpdate, ItemMenu.Size.row(getRow()), this);
            lp.getInventoryManager().setInventario(menu);
            lp.getInventoryManager().getMenu().open();
        }
    }

    public void updateInventory(Player player) {
        // menu.updateInventoryPerPlayer(player);
        LobbyPlayer lpplayer = LobbyPlayerMap.getJugador(player);
        if (lpplayer != null) {
            openInventories.put(player.getUniqueId(), lpplayer);
        }
    }

    public int getRow() {
        return row;
    }

    public void setRow(int row) {
        this.row = row;
    }

    public Map<UUID, LobbyPlayer> getPlayers() {
        return openInventories;
    }

    public String getComando() {
        return Comando;
    }

    public void setComando(String comando) {
        Comando = comando;
    }

    public List<String> getTitulo() {
        return Titulo != null ? Titulo : new ArrayList<>();
    }

    public void setTitulo(List<String> titulo) {
        Titulo = titulo;
    }

    public void removePlayer(UUID player) {
        openInventories.computeIfPresent(player, (UUID, lobbyPlayer) -> {
            lobbyPlayer.getInventoryManager().resetIndexing();
            return null;
        });
    }

    public void addItems(ItemSlot item) {
        try {
            ItemSlot i = (ItemSlot) item.clone();
            itemSlots.add(i);
        } catch (CloneNotSupportedException e) {
            e.printStackTrace();
        }
    }

    public void itemsSync() {
        itemUpdater();
    }

}
