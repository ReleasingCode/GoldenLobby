package com.releasingcode.goldenlobby.modulos.tragacubos.objects.tragacubos;

import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Entity;
import org.bukkit.entity.ItemFrame;
import org.bukkit.inventory.ItemStack;

public class MarcoTragaperra {
    private final Location location;
    private final BlockFace faceClicked;
    private final ItemFrame frame;

    public MarcoTragaperra(Location location, BlockFace faceClicked) {
        this.location = location;
        this.faceClicked = faceClicked;
        frame = spawnFrame();
    }

    public Location getLocation() {
        return location;
    }

    public ItemStack getItem() {
        return frame != null ? frame.getItem() : null;
    }

    public void setItem(ItemStack item) {
        if (frame != null) {
            frame.setItem(item);
        }
    }

    public BlockFace getFaceClicked() {
        return faceClicked;
    }


    public boolean isEqualsLocationItemFrame(ItemFrame entity, Location base) {
        Block b = entity.getLocation().getBlock().getRelative(entity.getFacing().getOppositeFace());
        return b.getLocation().getBlockX() == base.getBlockX()
                && b.getLocation().getBlockY() == base.getBlockY()
                && b.getLocation().getBlockZ() == base.getBlockZ();
    }

    private ItemFrame spawnFrame() {
        if (location != null) {
            for (Entity entity : location.getWorld().getNearbyEntities(location, 1, 1, 1)) {
                if (entity instanceof ItemFrame) {
                    if (isEqualsLocationItemFrame((ItemFrame) entity, location)) {
                        return (ItemFrame) entity;
                    }
                }
            }
            Block blockAt = location.getWorld().getBlockAt(location);
            Block relative = blockAt.getRelative(faceClicked);
            if (relative != null && relative.getLocation() != null) {
                try {
                    return location.getWorld().spawn(relative.getLocation(), ItemFrame.class);
                } catch (Exception e) {
                }
            }

        }
        return null;
    }

    public void kill() {
        if (frame != null) {
            frame.remove();
        }
    }

}
