package com.releasingcode.goldenlobby.modulos.TabList;

import org.bukkit.configuration.file.FileConfiguration;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;

public class TabList {
    FileConfiguration configuration;
    ArrayList<String> header;
    ArrayList<String> footer;
    public int interval;
    public boolean enable;

    public TabList(TabListPlugin instace) {
        configuration = instace.getTablistConfig().getConfig();
        header = configuration.isSet("header") ? new ArrayList<>(
                Arrays.asList(configuration.getString("header").split("\\n"))) : new ArrayList<>();
        footer = configuration.isSet("footer") ? new ArrayList<>(
                Arrays.asList(configuration.getString("footer").split("\\n"))) : new ArrayList<>();
        enable = configuration.getBoolean("enable");
        interval = configuration.getInt("animation.interval");
    }

    public ArrayList<String> getHeader() {
        return enable ? header : new ArrayList<>(Collections.singletonList("None"));
    }

    public ArrayList<String> getFooter() {
        return enable ? footer : new ArrayList<>(Collections.singletonList("None"));
    }

    public int getInterval() {
        if (interval > 0)
            return interval;
        return 20;
    }
}
