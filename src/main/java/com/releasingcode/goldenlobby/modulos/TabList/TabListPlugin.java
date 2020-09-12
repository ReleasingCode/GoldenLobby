package com.releasingcode.goldenlobby.modulos.TabList;

import com.releasingcode.goldenlobby.GoldenLobby;
import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.configuracion.CustomConfiguration;
import com.releasingcode.goldenlobby.extendido.nms.ITablList;
import com.releasingcode.goldenlobby.loader.LobbyComponente;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.scheduler.BukkitRunnable;

import java.lang.reflect.InvocationTargetException;
import java.util.concurrent.atomic.AtomicInteger;

public class TabListPlugin extends LobbyComponente implements Listener {
    private Class<?> tabListNms;
    private CustomConfiguration tablistConfig;
    private TabList tabList;
    public static TabListPlugin instance;

    public TabListPlugin getInstance() {
        return instance;
    }

    @Override
    protected void onEnable() {
        Utils.log("&cTabList", " - Loading module TabList");
        loadTablist();
        new TabListCommand(this).register();
        getPlugin().getServer().getPluginManager().registerEvents(this, getPlugin());
        runnable();

    }

    protected void loadTablist() {
        if (!setupVersionProtocol()) {
            Utils.log("&cTabList", "This version of Minecraft is not supported! ");
            return;
        }

        tablistConfig = new CustomConfiguration("tablist", getPlugin());
        tabList = new TabList(this);
        Utils.log("&cTabList", "Cargada la config");

    }

    @Override
    public void onDisable() {
        Utils.log("&Disabling TabList");
    }

    public CustomConfiguration getTablistConfig() {
        return tablistConfig;
    }

    private boolean setupVersionProtocol() {
        try {
            tabListNms = Class.forName(
                    "com.releasingcode.goldenlobby.extendido.nms." + GoldenLobby.getVersion() + ".TabList_" + GoldenLobby.getVersion());
        } catch (Exception ignored) {
        }
        return tabListNms != null;
    }

    public void runnable() {
        if (getTabList() != null) {
            AtomicInteger integer = new AtomicInteger(0);
            AtomicInteger j = new AtomicInteger(0);
            new BukkitRunnable() {
                public void run() {
                    if (integer.get() >= tabList.getHeader().size()) {
                        integer.set(0);

                    }
                    if (j.get() >= tabList.getFooter().size()) {
                        j.set(0);
                    }

                    for (Player p : Bukkit.getOnlinePlayers()) {
                        sendTabList(p, tabList.getHeader().get(integer.get()), tabList.getFooter().get(j.get()));
                    }

                    integer.set(integer.get() + 1);
                    j.set(j.get() + 1);

                }
            }.runTaskTimer(getPlugin(), 30, tabList.getInterval());
        }
    }

    public void sendTabList(Player player, String header, String footer) {
        ITablList tab = getTabList();
        if (tab != null) {
            tab.tabList(player, header, footer);
        }
    }

    public ITablList getTabList() {
        try {
            return (ITablList) tabListNms.getConstructors()[0].newInstance();
        } catch (IllegalAccessException | InvocationTargetException | InstantiationException e) {
            e.printStackTrace();
        }
        return null;
    }

    public void reload() {
        tablistConfig = null;
        tabList = null;

        tablistConfig = new CustomConfiguration("tablist", getPlugin());
        tabList = new TabList(this);
        Utils.log("&cTabList", "Config Reloaded ");


    }

}
