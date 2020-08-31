package com.releasingcode.goldenlobby.modulos.repartidor.utils;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Calendar;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class Utils {
    public Utils() {
        super();
    }

    public static ItemStack createItem(final Material mat, final String name) {
        return createItem(mat, name, 1);
    }

    public static ItemStack createItem(final Material mat, final String name, final int amount) {
        return createItem(mat, name, amount, null);
    }

    public static ItemStack createItem(final Material mat, final String name, final int amount, final short subID) {
        return createItem(mat, name, amount, subID, null);
    }

    public static ItemStack createItem(final Material mat, final String name, final int amount, final List<String> lore) {
        return createItem(mat, name, amount, (short) 0, lore);
    }

    public static ItemStack createItem(final Material mat, final String name, final int amount, final short subID, final List<String> lore) {
        final ItemStack i = new ItemStack(mat, amount, subID);
        final ItemMeta m = i.getItemMeta();
        if (name != null) {
            m.setDisplayName(name);
        }
        if (lore != null) {
            m.setLore(lore);
        }
        i.setItemMeta(m);
        return i;
    }

    public static String formatDateDiff(final long millis) {
        final StringBuilder s = new StringBuilder();
        final int days = (int) TimeUnit.MILLISECONDS.toDays(millis);
        final long hours = TimeUnit.MILLISECONDS.toHours(millis) - days * 24;
        final long minutes = TimeUnit.MILLISECONDS.toMinutes(millis) - TimeUnit.MILLISECONDS.toHours(millis) * 60L;
        final long seconds = TimeUnit.MILLISECONDS.toSeconds(millis) - TimeUnit.MILLISECONDS.toMinutes(millis) * 60L;
        if (days > 0) {
            s.append(((days < 10) ? "0" : "") + days + ":");
        }
        if (hours > 0L) {
            s.append(((hours < 10L) ? "0" : "") + hours + ":");
        }
        s.append(((minutes < 10L) ? "0" : "") + minutes + ":");
        s.append(((seconds < 10L) ? "0" : "") + seconds);
        return (s.length() == 0) ? "00:01" : s.toString();
    }

    public static String getMonth() {
        final Calendar calendar = Calendar.getInstance();
        final int month = calendar.get(2);
        final String[] names = {"Enero", "Febrero", "Marzo", "Abril", "Mayo", "Junio", "Julio", "Agosto", "Septiembre", "Octubre", "Noviembre", "Diciembre"};
        return names[month];
    }
}
