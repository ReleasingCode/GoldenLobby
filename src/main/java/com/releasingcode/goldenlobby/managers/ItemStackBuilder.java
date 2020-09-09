package com.releasingcode.goldenlobby.managers;

import com.releasingcode.goldenlobby.Utils;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.enchantments.EnchantmentTarget;
import org.bukkit.enchantments.EnchantmentWrapper;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class ItemStackBuilder {
    private final ItemStack item;
    private final List<String> lore;
    private final Map<Enchantment, Integer> enchantments = new HashMap<>();
    private ItemMeta meta;
    private SkullMeta skullMeta;
    private String name64;

    public ItemStackBuilder(final ItemStack item) {
        this.item = item;
        if (item.getType() == Material.SKULL_ITEM) {
            skullMeta = (SkullMeta) item.getItemMeta();
            this.lore = ((this.skullMeta != null && this.skullMeta.hasLore()) ? this.skullMeta
                    .getLore() : new ArrayList<>());
        } else {
            this.meta = item.getItemMeta();
            this.lore = ((this.meta != null && this.meta.hasLore()) ? this.meta.getLore() : new ArrayList<>());
        }
    }

    public ItemStackBuilder(final Material material) {
        this(new ItemStack(material));
    }

    public ItemStackBuilder(final Material material, int amount, short data) {
        this(new ItemStack(material, amount, data));
    }

    public ItemStackBuilder(String builder) {
        String[] separator = builder.split(":");
        Material m = Material.STONE;
        int amount = 1;
        short data = 0;
        name64 = null;
        if (separator.length == 1) {
            Material material = Material.getMaterial(separator[0].toUpperCase());
            if (material != null) {
                m = material;
            }
        } else if (separator.length == 2) {
            Material material = Material.getMaterial(separator[0].toUpperCase());
            if (material != null) {
                m = material;
            }
            amount = Utils.tryParseInt(separator[1]) ? Integer.parseInt(separator[1]) : 1;
        } else if (separator.length == 3) {
            if (separator[0].toLowerCase().equals("skull_owner")) {
                m = Material.SKULL_ITEM;
                amount = Utils.tryParseInt(separator[1]) ? Integer.parseInt(separator[1]) : 1;
                data = 3;
                name64 = separator[2];
            } else {
                Material material = Material.getMaterial(separator[0].toUpperCase());
                if (material != null) {
                    m = material;
                }
                amount = Utils.tryParseInt(separator[1]) ? Integer.parseInt(separator[1]) : 1;
                data = Utils.tryParseShort(separator[2]) ? Short.parseShort(separator[2]) : 0;
            }
        }
        this.item = new ItemStack(m, amount, data);
        if (name64 != null) {
            skullMeta = (SkullMeta) item.getItemMeta();
            this.lore = ((this.skullMeta != null && this.skullMeta.hasLore()) ? this.skullMeta
                    .getLore() : new ArrayList<>());
        } else {
            this.meta = item.getItemMeta();
            this.lore = ((this.meta != null && this.meta.hasLore()) ? this.meta.getLore() : new ArrayList<>());
        }

    }

    public static boolean equalsItemMaterial(ItemStack stack, ItemStack selectorStack) {
        if (stack != null && selectorStack != null) {
            if (stack.getType().equals(selectorStack.getType())) {
                return stack.getData() == selectorStack.getData();
            }
        }
        return false;
    }

    public static boolean equalsItem(ItemStack stack, String displayName) {
        if (stack != null && displayName != null) {
            ItemMeta meta = stack.getItemMeta();
            if (meta != null) {
                String displayStack = meta.getDisplayName();
                return displayName.contains(displayStack);
            }
        }
        return false;
    }

    public static boolean equalsItem(ItemStack stack, ItemStack selectorStack) {
        if (stack != null && selectorStack != null) {
            if (stack.getType().equals(selectorStack.getType())) {
                ItemMeta meta = stack.getItemMeta();
                ItemMeta selectorMeta = selectorStack.getItemMeta();
                if (meta != null && selectorMeta != null) {
                    String displayStack = meta.getDisplayName();
                    String displaySelector = selectorMeta.getDisplayName();
                    return displaySelector != null && displayStack.contains(displaySelector);
                }
            }
        }
        return false;
    }

    public ItemStackBuilder setType(final Material type) {
        this.item.setType(type);
        return this;
    }


    public ItemStackBuilder setName(String displayName) {
        if (displayName != null) {
            if (this.meta != null) {
                this.meta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
            }
            if (skullMeta != null) {
                this.skullMeta.setDisplayName(ChatColor.translateAlternateColorCodes('&', displayName));
            }
        }
        return this;
    }

    public ItemStackBuilder addLore(final List<String> array) {
        if (this.meta != null || this.skullMeta != null) {
            for (int length = array.size(), i = 0; i < length; ++i) {
                this.lore.add(ChatColor.translateAlternateColorCodes('&', array.get(i)));
            }
        }
        return this;
    }

    public ItemStackBuilder addLore(final String... array) {
        if (this.meta != null || this.skullMeta != null) {
            for (int length = array.length, i = 0; i < length; ++i) {
                this.lore.add(ChatColor.translateAlternateColorCodes('&', array[i]));
            }
        }
        return this;
    }

    public ItemStackBuilder addEnchantment(final Enchantment enchantment, final int n) {
        enchantments.put(enchantment, n);
        return this;
    }

    public ItemStackBuilder addFlag(ItemFlag... flags) {
        if (skullMeta != null) {
            this.skullMeta.addItemFlags(flags);
        }
        if (meta != null) {
            this.meta.addItemFlags(flags);
        }
        return this;
    }

    public ItemStackBuilder setDurability(final int n) {
        this.item.setDurability((short) n);
        return this;
    }

    public ItemStackBuilder setAmount(final int amount) {
        this.item.setAmount(amount);
        return this;
    }


    public ItemStackBuilder replaceLore(final String s, final String s2) {
        for (int i = 0; i < this.lore.size(); ++i) {
            if (this.lore.get(i).contains(s)) {
                this.lore.remove(i);
                this.lore.add(i, s2);
                break;
            }
        }
        return this;
    }

    public ItemStack build() {
        if (!this.lore.isEmpty()) {
            if (this.meta != null) {
                this.meta.setLore(this.lore);
            } else if (this.skullMeta != null) {
                this.skullMeta.setLore(this.lore);
            }
            this.lore.clear();
        }
        if (meta != null) {
            this.item.setItemMeta(this.meta);
        }
        if (skullMeta != null) {
            if (!name64.trim().equals("0")) {
                SkinGameProfile owner = new SkinGameProfile(this.name64);
                owner.applySkull(this.skullMeta);
            }
            this.item.setItemMeta(this.skullMeta);
        }
        if (this.item.getType() != Material.AIR) {
            this.enchantments.forEach(this.item::addEnchantment);
        }
        return this.item;
    }

    public static class GlowEnchantment extends EnchantmentWrapper {
        public GlowEnchantment() {
            super(120);
        }

        public boolean canEnchantItem(ItemStack item) {
            return true;
        }

        public boolean conflictsWith(Enchantment other) {
            return false;
        }

        public EnchantmentTarget getItemTarget() {
            return null;
        }

        public int getMaxLevel() {
            return 10;
        }

        public String getName() {
            return "Glow";
        }

        public int getStartLevel() {
            return 1;
        }
    }
}