package com.releasingcode.goldenlobby.managers.cooldown;

import com.releasingcode.goldenlobby.modulos.cooldown.object.CooldownSystem;

public class LobbyCooldown {
    private final String editing;

    public LobbyCooldown(String editing) {
        this.editing = editing;
    }

    public CooldownSystem getCooldownEditing() {
        if (editing == null || editing.trim().isEmpty()) {
            return null;
        }
        return CooldownSystem.getCooldown(this.editing);
    }

    public String getEditing() {
        return editing;
    }

    public boolean hasEditing() {
        return editing != null;
    }
}
