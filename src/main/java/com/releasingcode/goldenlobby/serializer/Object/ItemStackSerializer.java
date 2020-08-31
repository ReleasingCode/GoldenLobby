package com.releasingcode.goldenlobby.serializer.Object;

import com.releasingcode.goldenlobby.LobbyMC;
import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.managers.Potion1_9;
import com.releasingcode.goldenlobby.serializer.Serializer;
import com.releasingcode.goldenlobby.serializer.Serializers;
import org.bukkit.*;
import org.bukkit.block.BlockState;
import org.bukkit.block.Chest;
import org.bukkit.block.Furnace;
import org.bukkit.block.Hopper;
import org.bukkit.block.banner.Pattern;
import org.bukkit.enchantments.Enchantment;
import org.bukkit.inventory.ItemFlag;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.*;
import org.bukkit.potion.Potion;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionType;

import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

public class ItemStackSerializer
        implements Serializer<ItemStack> {
    @Override
    public String serialize(final ItemStack itemStack) {
        try {
            final HashMap<String, String> hashMap = new HashMap<>();
            hashMap.put("Material", String.valueOf(itemStack.getType().getId()));
            hashMap.put("Amount", String.valueOf(itemStack.getAmount()));
            hashMap.put("Data", String.valueOf(itemStack.getDurability()));
            String string = "";
            for (final Enchantment enchantment : itemStack.getEnchantments().keySet()) {
                string = string + ":" + enchantment.getId() + "," + itemStack.getEnchantmentLevel(enchantment);
            }
            final String replaceFirst = string.replaceFirst(":", "");
            if (!replaceFirst.equals("")) {
                hashMap.put("EN", replaceFirst);
            }
            if (itemStack.hasItemMeta()) {
                final ItemMeta itemMeta = itemStack.getItemMeta();
                hashMap.put("Flags", Serializers.listToString(itemStack.getItemMeta().getItemFlags(), ":"));
                hashMap.put("Unbr", String.valueOf(itemMeta.spigot().isUnbreakable()));
                if (itemMeta.hasDisplayName()) {
                    hashMap.put("Name", Serializers.stringToByteString(itemStack.getItemMeta().getDisplayName().replace("��", "&")));
                }
                if (itemMeta.hasLore()) {
                    hashMap.put("Lore", Serializers.listToBytes(this.translateColor(false, itemStack.getItemMeta().getLore()), ":"));
                }
                if (itemMeta instanceof SkullMeta) {
                    final SkullMeta skullMeta = (SkullMeta) itemStack.getItemMeta();
                    if (skullMeta.hasOwner()) {
                        hashMap.put("Owner", skullMeta.getOwner());
                    }
                }
                if (itemMeta instanceof BannerMeta) {
                    final BannerMeta bannerMeta = (BannerMeta) itemStack.getItemMeta();
                    hashMap.put("BaseColor", bannerMeta.getBaseColor().toString());
                    hashMap.put("Patterns", Serializers.superSerializer(bannerMeta.getPatterns(), Pattern.class, ":"));
                }
                if (itemMeta instanceof BookMeta) {
                    final BookMeta bookMeta = (BookMeta) itemStack.getItemMeta();
                    if (bookMeta.hasAuthor()) {
                        hashMap.put("Author", bookMeta.getAuthor());
                    }
                    if (bookMeta.hasPages()) {
                        hashMap.put("Pages", Serializers.listToString(bookMeta.getPages(), ":"));
                    }
                    if (bookMeta.hasTitle()) {
                        hashMap.put("Title", bookMeta.getTitle());
                    }
                }
                if (itemMeta instanceof FireworkMeta) {
                    final FireworkMeta fireworkMeta = (FireworkMeta) itemStack.getItemMeta();
                    if (fireworkMeta.hasEffects()) {
                        hashMap.put("Effects", Serializers.superSerializer(fireworkMeta.getEffects(), FireworkEffect.class, ":"));
                    }
                    hashMap.put("Power", String.valueOf(fireworkMeta.getPower()));
                }
                if (itemMeta instanceof LeatherArmorMeta) {
                    hashMap.put("Color", Objects.requireNonNull(Serializers.getSerializer(Color.class)).serialize(((LeatherArmorMeta) itemStack.getItemMeta()).getColor()));
                }
                if (itemMeta instanceof EnchantmentStorageMeta) {
                    final EnchantmentStorageMeta enchantmentStorageMeta = (EnchantmentStorageMeta) itemStack.getItemMeta();
                    StringBuilder string2 = new StringBuilder();
                    for (final Enchantment enchantment2 : enchantmentStorageMeta.getEnchants().keySet()) {
                        string2.append(":").append(enchantment2.getId()).append(",").append(itemStack.getEnchantmentLevel(enchantment2));
                    }
                    hashMap.put("ENM", string2.toString().replaceFirst(":", ""));
                }
                if (itemMeta instanceof BlockStateMeta) {
                    final BlockState blockState = ((BlockStateMeta) itemStack.getItemMeta()).getBlockState();
                    if (blockState instanceof Chest) {
                        hashMap.put("CItems", Serializers.listToBytes(Serializers.serializeList(Arrays.asList(((Chest) blockState).getInventory().getContents()), ItemStack.class), ":"));
                    }
                    if (blockState instanceof Hopper) {
                        hashMap.put("HItems", Serializers.listToBytes(Serializers.serializeList(Arrays.asList(((Hopper) blockState).getInventory().getContents()), ItemStack.class), ":"));
                    }
                    if (blockState instanceof Furnace) {
                        hashMap.put("FItems", Serializers.listToBytes(Serializers.serializeList(Arrays.asList(((Furnace) blockState).getInventory().getContents()), ItemStack.class), ":"));
                    }
                }
            }
            if (LobbyMC.getInstance().isNewVersion()) {
                if (itemStack.getType().equals(Material.POTION) || itemStack.getType().equals(Material.SPLASH_POTION) || itemStack.getType().equals(Material.LINGERING_POTION) || itemStack.getType().equals(Material.TIPPED_ARROW)) {
                    final Potion1_9 fromItemStack = Potion1_9.fromItemStack(itemStack);
                    final PotionMeta potionMeta = (PotionMeta) itemStack.getItemMeta();
                    hashMap.put("PType", fromItemStack.getIdType().toString());
                    if (fromItemStack.getType() != null) {
                        hashMap.put("Type", fromItemStack.getType().toString().replace("_", "-"));
                    } else {
                        hashMap.put("Type", null);
                    }
                    hashMap.put("Long", String.valueOf(fromItemStack.isLong()));
                    hashMap.put("St", String.valueOf(fromItemStack.isStrong()));
                    hashMap.put("PE", Serializers.superSerializer(potionMeta.getCustomEffects(), PotionEffect.class, ":"));
                }
            } else if (itemStack.getType().equals(Material.POTION)) {
                final Potion fromItemStack2 = Potion.fromItemStack(itemStack);
                final PotionMeta potionMeta2 = (PotionMeta) itemStack.getItemMeta();
                hashMap.put("PType", String.valueOf(fromItemStack2.isSplash()));
                if (fromItemStack2.getType() != null) {
                    hashMap.put("Type", fromItemStack2.getType().toString().replace("_", "-"));
                } else {
                    hashMap.put("Type", null);
                }
                hashMap.put("Long", String.valueOf(fromItemStack2.hasExtendedDuration()));
                hashMap.put("St", String.valueOf(fromItemStack2.getLevel()));
                hashMap.put("PE", Serializers.superSerializer(potionMeta2.getCustomEffects(), PotionEffect.class, ":"));
            }
            StringBuilder string3 = new StringBuilder();
            for (final String s : hashMap.keySet()) {
                string3.append("!!").append(s).append("--").append(hashMap.get(s));
            }
            return "-V3-" + string3.toString().replaceFirst("!!", "");
        } catch (Exception ex) {
            return this.serialize(new ItemStack(Material.AIR));
        }
    }

    @Override
    public ItemStack deserialize(final String s) {
        try {
            if (!s.startsWith("SPECIAL:")) {
                final HashMap<String, String> hashMap = new HashMap<>();
                if (s.startsWith("-V3-")) {
                    for (final String s2 : s.replaceFirst("-V3-", "").split("!!")) {
                        if (s2.split("--").length == 2) {
                            hashMap.put(s2.split("--")[0], s2.split("--")[1]);
                        }
                    }
                } else {
                    for (final String s3 : new String(Base64.getDecoder().decode(s), StandardCharsets.UTF_8).split(";")) {
                        if (s3.split("\"").length == 2) {
                            hashMap.put(s3.split("\"")[0], s3.split("\"")[1]);
                        }
                    }
                }
                ItemStack itemStack;
                if (hashMap.containsKey("Type")) {
                    if (LobbyMC.getInstance().isNewVersion()) {
                        itemStack = new Potion1_9(Potion1_9.PotionType.fromString(hashMap.get("Type").replace("-", "_")), Boolean.valueOf(hashMap.get("St")), Boolean.valueOf(hashMap.get("Long")), Potion1_9.PotionIdType.fromString(hashMap.get("PType"))).toItemStack(Integer.parseInt(hashMap.get("Amount")));
                        final PotionMeta itemMeta = (PotionMeta) itemStack.getItemMeta();
                        if (hashMap.get("PE") != null && !hashMap.get("PE").isEmpty()) {
                            final String[] split3 = hashMap.get("PE").split(":");
                            for (int length3 = split3.length, k = 0; k < length3; ++k) {
                                itemMeta.addCustomEffect(Serializers.getSerializer(PotionEffect.class).deserialize(split3[k]), true);
                            }
                        }
                        itemStack.setItemMeta(itemMeta);
                    } else {
                        final Potion potion =
                                new Potion(PotionType.valueOf(hashMap.get("Type").replace("-", "_")),
                                           Utils.tryParseInt(hashMap.get("St")) ?
                                                   Integer.parseInt(hashMap.get("St")) :
                                                   (Boolean.parseBoolean(hashMap.get("St")) ? 1 : 0));
                        if (Boolean.parseBoolean(hashMap.get("PType")) || hashMap.get("PType").equalsIgnoreCase("splash")) {
                            potion.splash();
                        }
                        if (Boolean.parseBoolean(hashMap.get("Long"))) {
                            potion.extend();
                        }
                        itemStack = potion.toItemStack(Integer.parseInt(hashMap.get("Amount")));
                        final PotionMeta itemMeta2 = (PotionMeta) itemStack.getItemMeta();
                        if (hashMap.get("PE") != null && !hashMap.get("PE").isEmpty()) {
                            final String[] split4 = hashMap.get("PE").split(":");
                            for (int length4 = split4.length, l = 0; l < length4; ++l) {
                                itemMeta2.addCustomEffect(Objects.requireNonNull(Serializers.getSerializer(PotionEffect.class)).deserialize(split4[l]), true);
                            }
                        }
                        itemStack.setItemMeta(itemMeta2);
                    }
                } else {
                    itemStack = new ItemStack(Material.getMaterial(Integer.parseInt(hashMap.get("Material"))), Integer.parseInt(hashMap.get("Amount")), Short.parseShort(hashMap.get("Data")));
                }
                if (hashMap.containsKey("EN")) {
                    for (final String s4 : hashMap.get("EN").split(":")) {
                        itemStack.addUnsafeEnchantment(Enchantment.getById(Integer.parseInt(s4.split(",")[0])), Integer.valueOf(s4.split(",")[1]));
                    }
                }
                final ItemMeta itemMeta3 = itemStack.getItemMeta();
                if (hashMap.containsKey("Name")) {
                    itemMeta3.setDisplayName(ChatColor.translateAlternateColorCodes('&', Serializers.byteStringToString(hashMap.get("Name"))));
                }
                if (hashMap.containsKey("Unbr")) {
                    itemMeta3.spigot().setUnbreakable(Boolean.parseBoolean(hashMap.get("Unbr")));
                }
                if (hashMap.containsKey("Lore")) {
                    final List<byte[]> bytesToList = Serializers.bytesToList(hashMap.get("Lore"), ":");
                    final ArrayList<String> lore = new ArrayList<String>();
                    for (byte[] bytes : bytesToList) {
                        lore.add(ChatColor.translateAlternateColorCodes('&', new String(bytes)));
                    }
                    itemMeta3.setLore(lore);
                }
                if (hashMap.containsKey("Owner")) {
                    ((SkullMeta) itemMeta3).setOwner(hashMap.get("Owner"));
                }
                if (hashMap.containsKey("BaseColor")) {
                    ((BannerMeta) itemMeta3).setBaseColor(DyeColor.valueOf(hashMap.get("BaseColor")));
                }
                if (hashMap.containsKey("Patterns")) {
                    ((BannerMeta) itemMeta3).setPatterns(Serializers.superDeserializer(hashMap.get("Patterns"), Pattern.class, ":"));
                }
                if (hashMap.containsKey("Author")) {
                    ((BookMeta) itemMeta3).setAuthor(hashMap.get("Author"));
                }
                if (hashMap.containsKey("Pages")) {
                    ((BookMeta) itemMeta3).setPages(Serializers.stringToList(hashMap.get("Pages"), ":"));
                }
                if (hashMap.containsKey("Title")) {
                    ((BookMeta) itemMeta3).setTitle(hashMap.get("Title"));
                }
                if (hashMap.containsKey("Power")) {
                    ((FireworkMeta) itemMeta3).setPower(Integer.parseInt(hashMap.get("Power")));
                }
                if (hashMap.containsKey("Color")) {
                    ((LeatherArmorMeta) itemMeta3).setColor(Objects.requireNonNull(Serializers.getSerializer(Color.class)).deserialize(hashMap.get("Color")));
                }
                if (hashMap.containsKey("PotionEffects")) {
                    for (PotionEffect potionEffect : Serializers.superDeserializer(hashMap.get("PotionEffects"), PotionEffect.class, ":")) {
                        ((PotionMeta) itemMeta3).addCustomEffect(potionEffect, true);
                    }
                }
                if (hashMap.containsKey("Flags")) {
                    for (ItemFlag itemFlag : Serializers.superDeserializer(hashMap.get("Flags"), ItemFlag.class, ":")) {
                        itemMeta3.addItemFlags(itemFlag);
                    }
                }
                if (hashMap.containsKey("CItems")) {
                    final Chest blockState = (Chest) ((BlockStateMeta) itemMeta3).getBlockState();
                    final List<ItemStack> superByteDeserializer = Serializers.superByteDeserializer(hashMap.get("CItems"), ItemStack.class, ":");
                    final ItemStack[] contents = new ItemStack[superByteDeserializer.size()];
                    for (int n2 = 0; n2 < superByteDeserializer.size(); ++n2) {
                        contents[n2] = superByteDeserializer.get(n2);
                    }
                    blockState.getInventory().setContents(contents);
                    ((BlockStateMeta) itemMeta3).setBlockState(blockState);
                }
                if (hashMap.containsKey("HItems")) {
                    final Hopper blockState2 = (Hopper) ((BlockStateMeta) itemMeta3).getBlockState();
                    final List<ItemStack> superByteDeserializer2 = Serializers.superByteDeserializer(hashMap.get("HItems"), ItemStack.class, ":");
                    final ItemStack[] contents2 = new ItemStack[superByteDeserializer2.size()];
                    for (int n3 = 0; n3 < superByteDeserializer2.size(); ++n3) {
                        contents2[n3] = superByteDeserializer2.get(n3);
                    }
                    blockState2.getInventory().setContents(contents2);
                    ((BlockStateMeta) itemMeta3).setBlockState(blockState2);
                }
                if (hashMap.containsKey("FItems")) {
                    final Furnace blockState3 = (Furnace) ((BlockStateMeta) itemMeta3).getBlockState();
                    final List<ItemStack> superByteDeserializer3 = Serializers.superByteDeserializer(hashMap.get("FItems"), ItemStack.class, ":");
                    final ItemStack[] contents3 = new ItemStack[superByteDeserializer3.size()];
                    for (int n4 = 0; n4 < superByteDeserializer3.size(); ++n4) {
                        contents3[n4] = superByteDeserializer3.get(n4);
                    }
                    blockState3.getInventory().setContents(contents3);
                    ((BlockStateMeta) itemMeta3).setBlockState(blockState3);
                }
                itemStack.setItemMeta(itemMeta3);
                return itemStack;
            }
        } catch (Exception ex) {
        }
        return new ItemStack(Material.AIR);
    }

    private List<String> translateColor(final boolean b, final List<String> list) {
        return list.stream().map(s -> b ? ChatColor.translateAlternateColorCodes('&', s) : s.replace("§", "&")).collect(Collectors.toList());
    }
}

