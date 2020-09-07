package com.releasingcode.goldenlobby.modulos.cooldown.command;

import com.releasingcode.goldenlobby.BaseCommand;
import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.call.CallBack;
import com.releasingcode.goldenlobby.languages.Lang;
import com.releasingcode.goldenlobby.managers.FutureTime;
import com.releasingcode.goldenlobby.managers.LobbyPlayer;
import com.releasingcode.goldenlobby.managers.LobbyPlayerMap;
import com.releasingcode.goldenlobby.managers.MessageSuggest;
import com.releasingcode.goldenlobby.managers.cooldown.LobbyCooldown;
import com.releasingcode.goldenlobby.modulos.cooldown.CooldownPlugin;
import com.releasingcode.goldenlobby.modulos.cooldown.object.CooldownSystem;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class CooldownCommand extends BaseCommand {
    private final CooldownPlugin plugin;

    public CooldownCommand(CooldownPlugin plugin, String command) {
        super(command);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            if (sender.hasPermission("goldenlobby.cooldown.admin")) {
                Player player = ((Player) sender).getPlayer();
                LobbyPlayer lobbyPlayer = LobbyPlayerMap.getJugador(player);
                if (lobbyPlayer == null) {
                    player.sendMessage(Utils.chatColor("&cWe have not yet uploaded your statistics"));
                    return true;
                }
                if (lobbyPlayer.getLobbyCooldown().hasEditing()) {
                    LobbyCooldown editing = lobbyPlayer.getLobbyCooldown();
                    if (args.length == 0) {
                        lobbyPlayer.sendMessage("");
                        lobbyPlayer.sendMessage("&e Cooldown - GoldenLobby - System");
                        lobbyPlayer.sendMessage("&6 You are in edit mode and the available commands are");
                        lobbyPlayer.sendMessage("");
                        lobbyPlayer.sendMessageWithSuggest(
                                new MessageSuggest(" - /mccooldown set [cooldown] ", "/mccooldown set ", "Sets the time to the current cooldown"),
                                new MessageSuggest(" - /mccooldown setstatus [active/disable/finished]", "/mccooldown setstatus ", "Sets the current status to cooldown")

                        );
                        return true;
                    }
                    switch (args[0].toLowerCase()) {
                        case "set": {
                            if (args.length == 1) {
                                lobbyPlayer.sendMessageWithSuggest(
                                        new MessageSuggest("You need to specify the time of the cooldown /mccooldown setcooldown hh:mm:ss o ", "/mccoldown create ", "Create a cooldown"));
                                return true;
                            }
                            String cooldown = args[1].toLowerCase();
                            FutureTime FutTime = FutureTime.parseByString(cooldown);
                            if (!FutTime.isValid()) {
                                lobbyPlayer.sendMessage("&cNot a valid time:&e " + cooldown);
                                return true;
                            }
                            editing.getCooldownEditing().setCooldownString(cooldown.toLowerCase());
                            lobbyPlayer.sendMessage("&aA cooldown has been established:: ");
                            lobbyPlayer.sendMessage("&e" + FutTime.toString());
                            return true;
                        }
                        case "delete": {
                            lobbyPlayer.setEditingCooldown(null);
                            CooldownSystem.removeCooldown(editing.getCooldownEditing().getName());
                            plugin.getCooldownDB().deleteCooldown(editing.getCooldownEditing().getName(), callback -> {
                            });
                            lobbyPlayer.sendMessage("&aYou have eliminated the cooldown: &e" + editing.getCooldownEditing().getName());
                            lobbyPlayer.sendMessage("&eYou left the editing mode");
                            return true;
                        }

                        case "cancel": {
                            lobbyPlayer.setEditingCooldown(null);
                            lobbyPlayer.sendMessage("&cCanceled editing action");
                            lobbyPlayer.sendMessage("&eYou left the editing mode");
                            return true;
                        }
                        case "info": {
                            FutureTime futureTime = editing.getCooldownEditing().getFutureTime();
                            lobbyPlayer.sendMessage("&aThe current cooldown of &6" + editing.getCooldownEditing().getName() + "&a is:");
                            lobbyPlayer.sendMessage("&e" + futureTime);
                            lobbyPlayer.sendMessage("&e Remaining time" + editing.getCooldownEditing().getRemaing());
                            return true;
                        }
                        case "save": {
                            lobbyPlayer.sendMessage("&6Saving cooldown...");
                            String name = editing.getCooldownEditing().getName();
                            lobbyPlayer.setEditingCooldown(null);
                            plugin.getCooldownDB().createOrUpdate(editing.getCooldownEditing(), new CallBack.SingleCallBack() {
                                @Override
                                public void onSuccess() {
                                    lobbyPlayer.setEditingCooldown(null);
                                    lobbyPlayer.sendMessage("&aYou have successfully saved the cooldown : &e" + name);
                                    lobbyPlayer.sendMessage("&eYou left the editing mode");
                                }

                                @Override
                                public void onError() {
                                    lobbyPlayer.setEditingCooldown(name);
                                    lobbyPlayer.sendMessage("&cCouldn't save the cooldown to the database");
                                }
                            });
                            return true;
                        }
                    }
                    return true;
                }
                if (args.length == 0) {
                    lobbyPlayer.sendMessage("&e Cooldown - Minecub - System");
                    lobbyPlayer.sendMessage("");
                    lobbyPlayer.sendMessageWithSuggest(
                            new MessageSuggest(" - /mccooldown create [cooldownName] ", "/mccooldown create ", "Crea un cooldown"),
                            new MessageSuggest(" - /mccooldown list [active/disable/finished]", "/mccooldown list ", "Displays the list of active/finalized/disabled cooldowns"),
                            new MessageSuggest(" - /mccooldown edit [cooldownName]", "/mccooldown edit ", "Edit an existing cooldown"),
                            new MessageSuggest(" - /mccooldown delete [cooldownName]", "/mccooldown delete ", "Removes an existing cooldown")

                    );
                    return true;
                }
                switch (args[0].toLowerCase()) {
                    case "create": {
                        if (args.length == 1) {
                            lobbyPlayer.sendMessageWithSuggest(
                                    new MessageSuggest("&cYou need to specify the name of the cooldown /mccooldown create [cooldownName]", "/mccooldown create ", "Crea un cooldown"));
                            return true;
                        }
                        String name = args[1];
                        CooldownSystem system = CooldownSystem.getCooldown(name);
                        if (system != null) {
                            lobbyPlayer.sendMessage("&cThere is already a cooldown with this name.");
                            return true;
                        }
                        CooldownSystem.addCooldown(name, new CooldownSystem(name.toLowerCase()));
                        lobbyPlayer.setEditingCooldown(name.toLowerCase());
                        lobbyPlayer.sendMessage("&aYou have created a new cooldown");
                        lobbyPlayer.sendMessage("&eYou are in cooldown editing mode: " + name.toLowerCase());
                        lobbyPlayer.sendMessage("");
                        lobbyPlayer.sendMessage("&6 You are in edit mode and the available commands are");
                        lobbyPlayer.sendMessage("");
                        lobbyPlayer.sendMessageWithSuggest(
                                new MessageSuggest(" - /mccooldown set [cooldown] ", "/mccooldown set ", "Sets the time to the current cooldown"),
                                new MessageSuggest(" - /mccooldown setstatus [active/disable/finished]", "/mccooldown setstatus ", "Sets the current status to cooldown")

                        );
                        return true;
                    }
                    case "edit": {
                        if (args.length == 1) {
                            lobbyPlayer.sendMessageWithSuggest(
                                    new MessageSuggest("You need to specify the name of the cooldown /mccooldown edit [cooldownName]", "/mccooldown edit ", "Create a cooldown"));
                            return true;
                        }
                        String name = args[1];
                        CooldownSystem system = CooldownSystem.getCooldown(name);
                        if (system == null) {
                            lobbyPlayer.sendMessage("&cThere is no cooldown with this name");
                            return true;
                        }
                        lobbyPlayer.setEditingCooldown(name.toLowerCase());
                        lobbyPlayer.sendMessage("&eYou have set the cooldown edition mode: " + name.toLowerCase());
                        return true;
                    }
                    case "start": {
                        if (args.length == 1) {
                            lobbyPlayer.sendMessageWithSuggest(
                                    new MessageSuggest("You need to specify the name of the cooldown /mccooldown start [cooldownName]", "/mccooldown start ", "Create a cooldown"));
                            return true;
                        }
                        String name = args[1];
                        CooldownSystem system = CooldownSystem.getCooldown(name);
                        if (system == null) {
                            lobbyPlayer.sendMessage("&cThere is no cooldown with this name");
                            return true;
                        }
                        lobbyPlayer.sendMessage("&6Starting cooldown..");
                        system.prepareFinishAt(); // establece el tiempo de finalización
                        system.prepareStarted();
                        system.setStatus(CooldownSystem.CooldownStatus.ENABLED);
                        plugin.getCooldownDB().createOrUpdate(system, new CallBack.SingleCallBack() {
                            @Override
                            public void onSuccess() {
                                lobbyPlayer.setEditingCooldown(null);
                                lobbyPlayer.sendMessage("&aThe cooldown has started: &e" + name);
                            }

                            @Override
                            public void onError() {
                                lobbyPlayer.setEditingCooldown(name);
                                lobbyPlayer.sendMessage("&cCouldn't start the cooldown");
                            }
                        });
                        return true;
                    }
                    case "stop": {
                        if (args.length == 1) {
                            lobbyPlayer.sendMessageWithSuggest(
                                    new MessageSuggest("You need to specify the name of the cooldown /mccooldown start [cooldownName]", "/mccooldown start ", "Create a cooldown"));
                            return true;
                        }
                        String name = args[1];
                        CooldownSystem system = CooldownSystem.getCooldown(name);
                        if (system == null) {
                            lobbyPlayer.sendMessage("&cThere is no cooldown with this name");
                            return true;
                        }
                        lobbyPlayer.setEditingCooldown(name.toLowerCase());
                        lobbyPlayer.sendMessage("&eThe cooldown " + name.toLowerCase() + " has started");
                    }
                    case "list": {
                        return true;
                    }
                    case "reload": {
                        plugin.reloadSettings();
                        lobbyPlayer.sendMessage("&aYou have reloaded the cooldown configuration");
                        return true;
                    }
                }
            } else {
                sender.sendMessage(Utils.chatColor(Lang.NO_PERMISSION.toString()));
            }
        }
        return true;
    }
}
