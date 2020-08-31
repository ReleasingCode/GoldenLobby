package com.releasingcode.goldenlobby.modulos.regions.cuboid;

import org.bukkit.Location;

public class StageOfCreation {
    private final Regions region;
    private Stages stage = Stages.FIRST_POINT;
    private Location firstLoc;
    private Location secondLoc;

    public StageOfCreation(Regions region) {
        this.region = region;
    }

    public Stages getStage() {
        return stage;
    }

    public Location getFirstLoc() {
        return firstLoc;
    }

    public StageOfCreation setFirstLoc(Location firstLoc) {
        this.firstLoc = firstLoc;
        this.stage = Stages.SECOND_POINT;
        return this;
    }

    public Location getSecondLoc() {
        return secondLoc;
    }

    public void setSecondLoc(Location secondLoc) {
        this.secondLoc = secondLoc;
    }

    public Regions getRegion() {
        return region;
    }

    public enum Stages {
        FIRST_POINT, SECOND_POINT
    }

    public enum Regions {
        LOBBY, PVP
    }

}
