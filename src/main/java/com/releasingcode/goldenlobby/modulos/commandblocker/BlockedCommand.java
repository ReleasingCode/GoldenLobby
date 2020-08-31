package com.releasingcode.goldenlobby.modulos.commandblocker;

public class BlockedCommand {
    private final String messageWhenExecuted;
    private final String permission;

    public BlockedCommand(String messageWhenExecuted, String permission) {
        this.messageWhenExecuted = messageWhenExecuted;
        this.permission = permission;
    }

    public String getMessageWhenExecuted() {
        return messageWhenExecuted;
    }

    public String getPermission() {
        return permission;
    }

}
