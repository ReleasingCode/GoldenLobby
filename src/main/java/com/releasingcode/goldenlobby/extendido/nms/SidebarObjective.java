package com.releasingcode.goldenlobby.extendido.nms;

import org.apache.commons.lang.Validate;

import java.util.*;

public class SidebarObjective {
    public static final int MAX_LENGTH_OBJECTIVE_NAME = 16;
    public static final int MAX_LENGTH_OBJECTIVE_DISPLAY_NAME = 32;
    public static final int MAX_LENGTH_SCORE_NAME = 40;

    private final String name;
    private final Map<String, Integer> scores = new HashMap<>();
    private final Set<UUID> receivers = new HashSet<>();
    private String displayName;

    public SidebarObjective(String name, String displayName) {
        if (name == null)
            name = Math.abs(UUID.randomUUID().getLeastSignificantBits()) + "";
        this.name = name.substring(0, Math.min(MAX_LENGTH_OBJECTIVE_NAME, name.length()));
        setDisplayName(displayName);
    }

    public SidebarObjective(String displayName) {
        this(null, displayName);
    }

    public SidebarObjective() {
        this(null);
    }

    public boolean setScore(String name, Integer score) {
        Validate.notNull(name, "The score name cannot be null!");
        Validate.notNull(score, "The score cannot be null!");
        if (name.length() > MAX_LENGTH_SCORE_NAME)
            name = name.substring(0, MAX_LENGTH_SCORE_NAME);

        return scores.put(name, score) != null;
    }

    public boolean removeScore(String name) {
        Validate.notNull(name, "The score name cannot be null!");
        return scores.remove(name) != null;
    }

    public void addReceiver(UUID id) {
        receivers.add(id);
    }

    public void removeReceiver(UUID id) {
        receivers.remove(id);
    }

    public String getName() {
        return name;
    }

    public String getDisplayName() {
        return displayName;
    }

    public void setDisplayName(String displayName) {
        this.displayName = displayName != null
                ? displayName.substring(0, Math.min(MAX_LENGTH_OBJECTIVE_DISPLAY_NAME, displayName.length())) : "";
    }

    public Map<String, Integer> getScores() {
        return scores;
    }

    public Set<UUID> getReceivers() {
        return receivers;
    }
}
