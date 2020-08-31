package com.releasingcode.goldenlobby;

import org.bukkit.Bukkit;
import org.bukkit.command.*;

import java.lang.reflect.Field;
import java.util.List;
import java.util.Map;

public abstract class BaseCommand implements CommandExecutor {

    protected static CommandMap cmap;
    protected final String command;
    protected final String description;
    protected final List<String> alias;
    protected final String usage;
    protected final String permMessage;
    protected ReflectCommand baseCommand;

    public BaseCommand(String command) {
        this(command, null, null, null, null);
    }

    public BaseCommand(String command, String usage) {
        this(command, usage, null, null, null);
    }

    public BaseCommand(String command, String usage, String description) {
        this(command, usage, description, null, null);
    }

    public BaseCommand(String command, String usage, String description, String permissionMessage) {
        this(command, usage, description, permissionMessage, null);
    }

    public BaseCommand(String command, String usage, String description, List<String> aliases) {
        this(command, usage, description, null, aliases);
    }

    public BaseCommand(String command, String usage, String description, String permissionMessage,
                       List<String> aliases) {
        this.command = command.toLowerCase();
        this.usage = usage;
        this.description = description;
        this.permMessage = permissionMessage;
        this.alias = aliases;
    }

    public void unregister() {
        if (baseCommand != null) {
            Map<String, Command> knows = knowsCommand();
            if (knows != null) {
                knows.remove(this.command);
            }
            baseCommand.unregister(getCommandMap());
            baseCommand.setExecutor(null);
        }
    }

    public void register() {
        ReflectCommand cmd = new ReflectCommand(this.command);
        if (this.alias != null)
            cmd.setAliases(this.alias);
        if (this.description != null)
            cmd.setDescription(this.description);
        if (this.usage != null)
            cmd.setUsage(this.usage);
        if (this.permMessage != null)
            cmd.setPermissionMessage(this.permMessage);
        getCommandMap().register(this.command, cmd);
        baseCommand = cmd;
        cmd.setExecutor(this);
    }

    @SuppressWarnings("unchecked") final Map<String, Command> knowsCommand() {
        try {
            final Field f = SimpleCommandMap.class.getDeclaredField("knownCommands");
            f.setAccessible(true);
            return (Map<String, Command>) f.get(getCommandMap());
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    final CommandMap getCommandMap() {
        if (cmap == null) {
            try {
                final Field f = Bukkit.getServer().getClass().getDeclaredField("commandMap");
                f.setAccessible(true);
                cmap = (CommandMap) f.get(Bukkit.getServer());
                return getCommandMap();
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            return cmap;
        }
        return getCommandMap();
    }

    public abstract boolean onCommand(CommandSender sender, Command cmd, String label, String[] args);

    public List<String> onTabComplete(CommandSender sender, Command cmd, String label, String[] args) {
        return null;
    }

    private static final class ReflectCommand extends Command {
        private BaseCommand exe = null;

        protected ReflectCommand(String command) {
            super(command);
        }

        public void setExecutor(BaseCommand exe) {
            this.exe = exe;
        }

        @Override
        public boolean execute(CommandSender sender, String commandLabel, String[] args) {
            if (exe != null) {
                return exe.onCommand(sender, this, commandLabel, args);
            }
            return false;
        }

        @Override
        public List<String> tabComplete(CommandSender sender, String alais, String[] args) {
            if (exe != null) {
                return exe.onTabComplete(sender, this, alais, args);
            }
            return null;
        }
    }
}