package com.releasingcode.goldenlobby.modulos.regions.tienda;

import com.google.common.base.Preconditions;
import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.managers.ItemStackBuilder;
import org.bukkit.Material;
import org.bukkit.configuration.ConfigurationSection;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class ItemStackConfigParser {
    private final Map<Integer, ShopItem> items = new HashMap<>();
    private final FileConfiguration fileConfiguration;
    private int invSize;
    private String invName;
    private String command;

    public ItemStackConfigParser(FileConfiguration fileConfiguration) {
        this.fileConfiguration = fileConfiguration;
        if (!fileConfiguration.getBoolean("shop-enabled")) return;
        invSize = fileConfiguration.getInt("inventory-size");
        invName = fileConfiguration.getString("nombre-tienda");
        command = fileConfiguration.getString("comando");
        Preconditions.checkArgument(invSize % 9 == 0, "¡El tamaño del inventario debe ser múltiplo de 9!");
        parse();
    }

    private void parse() {
        Set<String> items = fileConfiguration.getConfigurationSection("inventory-contents").getKeys(false);
        items.forEach(item -> {
            ConfigurationSection configurationSection = fileConfiguration.getConfigurationSection("inventory-contents." + item);
            int slot = Integer.parseInt(item);
            int price = configurationSection.getInt("price");
            int amount = configurationSection.getInt("amount");
            Material material = Material.valueOf(configurationSection.getString("material"));
            String name = configurationSection.getString("name");
            List<String> lore = (List<String>) configurationSection.getList("lore");
            List<String> enchants = ((List<String>) configurationSection.getList("enchants"));

            ItemStackBuilder itemStackBuilder = new ItemStackBuilder(material)
                    .setName(Utils.chatColor(name))
                    .addLore(Utils.colorizeArray(lore.toArray(new String[0])))
                    .setAmount(amount);

            enchants.forEach(enchant -> itemStackBuilder.addEnchantment(Enchantment.getByName(enchant.split(":")[0]), Integer.parseInt(enchant.split(":")[1])));

            this.items.put(slot, new ShopItem(itemStackBuilder.build(), price));
        });
        if (!items.isEmpty()) Utils.log("¡Cargados " + items.size() + " items de la tienda!");
    }

    public int getInvSize() {
        return invSize;
    }

    public Map<Integer, ShopItem> getItems() {
        return items;
    }

    public boolean itemExistsAtSlot(int slot) {
        return items.containsKey(slot);
    }

    public String getInvName() {
        return invName;
    }

    public String getCommand() {
        return command;
    }

    public static class ShopItem {
        private final ItemStack item;
        private final int price;

        public ShopItem(ItemStack item, int price) {
            this.item = item;
            this.price = price;
        }

        public int getPrice() {
            return price;
        }

        public ItemStack getItem() {
            return item;
        }
    }

    public boolean isAShopItem(ItemStack item) {
        return items.values().stream()
                .anyMatch(shopItem -> ItemStackBuilder.equalsItem(shopItem.item, item));
    }

}
