package com.releasingcode.goldenlobby.modulos.cooldown.command;

import com.releasingcode.goldenlobby.BaseCommand;
import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.call.CallBack;
import com.releasingcode.goldenlobby.managers.FutureTime;
import com.releasingcode.goldenlobby.managers.LobbyPlayer;
import com.releasingcode.goldenlobby.managers.LobbyPlayerMap;
import com.releasingcode.goldenlobby.managers.MessageSuggest;
import com.releasingcode.goldenlobby.managers.cooldown.LobbyCooldown;
import com.releasingcode.goldenlobby.modulos.cooldown.CooldownPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import com.releasingcode.goldenlobby.modulos.cooldown.object.CooldownSystem;

public class CooldownCommand extends BaseCommand {
    private final CooldownPlugin plugin;

    public CooldownCommand(CooldownPlugin plugin, String command) {
        super(command);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            if (sender.hasPermission("lobbymc.cooldown.admin")) {
                Player player = ((Player) sender).getPlayer();
                LobbyPlayer lobbyPlayer = LobbyPlayerMap.getJugador(player);
                if (lobbyPlayer == null) {
                    player.sendMessage(Utils.chatColor("&cNo hemos cargado aún tus estadisticas"));
                    return true;
                }
                if (lobbyPlayer.getLobbyCooldown().hasEditing()) {
                    LobbyCooldown editing = lobbyPlayer.getLobbyCooldown();
                    if (args.length == 0) {
                        lobbyPlayer.sendMessage("");
                        lobbyPlayer.sendMessage("&e Cooldown - Minecub - System");
                        lobbyPlayer.sendMessage("&6 Estás en modo edición y los comandos disponibles son");
                        lobbyPlayer.sendMessage("");
                        lobbyPlayer.sendMessageWithSuggest(
                                new MessageSuggest(" - /mccooldown set [cooldown] ", "/mccooldown set ", "Establece el tiempo al cooldown actual"),
                                new MessageSuggest(" - /mccooldown setstatus [active/disable/finished]", "/mccooldown setstatus ", "Establece el estado actual al cooldown")

                        );
                        return true;
                    }
                    switch (args[0].toLowerCase()) {
                        case "set": {
                            if (args.length == 1) {
                                lobbyPlayer.sendMessageWithSuggest(
                                        new MessageSuggest("Necesitas especificar el tiempo del cooldown /mccooldown setcooldown hh:mm:ss o ", "/mccoldown create ", "Crea un cooldown"));
                                return true;
                            }
                            String cooldown = args[1].toLowerCase();
                            FutureTime FutTime = FutureTime.parseByString(cooldown);
                            if (!FutTime.isValid()) {
                                lobbyPlayer.sendMessage("&cNo es un tiempo válido:&e " + cooldown);
                                return true;
                            }
                            editing.getCooldownEditing().setCooldownString(cooldown.toLowerCase());
                            lobbyPlayer.sendMessage("&aSe ha establecido un cooldown de: ");
                            lobbyPlayer.sendMessage("&e" + FutTime.toString());
                            return true;
                        }
                        case "delete": {
                            lobbyPlayer.setEditingCooldown(null);
                            CooldownSystem.removeCooldown(editing.getCooldownEditing().getName());
                            plugin.getCooldownDB().deleteCooldown(editing.getCooldownEditing().getName(), callback -> {
                            });
                            lobbyPlayer.sendMessage("&aHas eliminado el cooldown: &e" + editing.getCooldownEditing().getName());
                            lobbyPlayer.sendMessage("&eSaliste del modo edición");
                            return true;
                        }

                        case "cancel": {
                            lobbyPlayer.setEditingCooldown(null);
                            lobbyPlayer.sendMessage("&cAcción de edición cancelada");
                            lobbyPlayer.sendMessage("&eSaliste del modo edición");
                            return true;
                        }
                        case "info": {
                            FutureTime futureTime = editing.getCooldownEditing().getFutureTime();
                            lobbyPlayer.sendMessage("&aEl cooldown actual de &6" + editing.getCooldownEditing().getName() + "&a es:");
                            lobbyPlayer.sendMessage("&e" + futureTime);
                            lobbyPlayer.sendMessage("&e Tiempo restante" + editing.getCooldownEditing().getRemaing());
                            return true;
                        }
                        case "save": {
                            lobbyPlayer.sendMessage("&6Guardando cooldown...");
                            String name = editing.getCooldownEditing().getName();
                            lobbyPlayer.setEditingCooldown(null);
                            plugin.getCooldownDB().createOrUpdate(editing.getCooldownEditing(), new CallBack.SingleCallBack() {
                                @Override
                                public void onSuccess() {
                                    lobbyPlayer.setEditingCooldown(null);
                                    lobbyPlayer.sendMessage("&aHas guardado con exito el cooldown: &e" + name);
                                    lobbyPlayer.sendMessage("&eSaliste del modo edición");
                                }

                                @Override
                                public void onError() {
                                    lobbyPlayer.setEditingCooldown(name);
                                    lobbyPlayer.sendMessage("&cNo se ha podido guardar el cooldown en la base de datos");
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
                            new MessageSuggest(" - /mccooldown list [active/disable/finished]", "/mccooldown list ", "Muestra la lista de cooldown activos/finalizados/desabilitados"),
                            new MessageSuggest(" - /mccooldown edit [cooldownName]", "/mccooldown edit ", "Edita un cooldown existente"),
                            new MessageSuggest(" - /mccooldown delete [cooldownName]", "/mccooldown delete ", "Elimina un cooldown existente")

                    );
                    return true;
                }
                switch (args[0].toLowerCase()) {
                    case "create": {
                        if (args.length == 1) {
                            lobbyPlayer.sendMessageWithSuggest(
                                    new MessageSuggest("&cNecesitas especificar el nombre del cooldown /mccooldown create [cooldownName]", "/mccooldown create ", "Crea un cooldown"));
                            return true;
                        }
                        String name = args[1];
                        CooldownSystem system = CooldownSystem.getCooldown(name);
                        if (system != null) {
                            lobbyPlayer.sendMessage("&cYa existe un cooldown con este nombre.");
                            return true;
                        }
                        CooldownSystem.addCooldown(name, new CooldownSystem(name.toLowerCase()));
                        lobbyPlayer.setEditingCooldown(name.toLowerCase());
                        lobbyPlayer.sendMessage("&aHas creado un nuevo cooldown");
                        lobbyPlayer.sendMessage("&eEstás en modo edición del cooldown: " + name.toLowerCase());
                        lobbyPlayer.sendMessage("");
                        lobbyPlayer.sendMessage("&6 Estás en modo edición y los comandos disponibles son");
                        lobbyPlayer.sendMessage("");
                        lobbyPlayer.sendMessageWithSuggest(
                                new MessageSuggest(" - /mccooldown set [cooldown] ", "/mccooldown set ", "Establece el tiempo al cooldown actual"),
                                new MessageSuggest(" - /mccooldown setstatus [active/disable/finished]", "/mccooldown setstatus ", "Establece el estado actual al cooldown")

                        );
                        return true;
                    }
                    case "edit": {
                        if (args.length == 1) {
                            lobbyPlayer.sendMessageWithSuggest(
                                    new MessageSuggest("Necesitas especificar el nombre del cooldown /mccooldown edit [cooldownName]", "/mccooldown edit ", "Crea un cooldown"));
                            return true;
                        }
                        String name = args[1];
                        CooldownSystem system = CooldownSystem.getCooldown(name);
                        if (system == null) {
                            lobbyPlayer.sendMessage("&cNo existe un cooldown con este nombre");
                            return true;
                        }
                        lobbyPlayer.setEditingCooldown(name.toLowerCase());
                        lobbyPlayer.sendMessage("&eHas puesto en modo edición del cooldown: " + name.toLowerCase());
                        return true;
                    }
                    case "start": {
                        if (args.length == 1) {
                            lobbyPlayer.sendMessageWithSuggest(
                                    new MessageSuggest("Necesitas especificar el nombre del cooldown /mccooldown start [cooldownName]", "/mccooldown start ", "Crea un cooldown"));
                            return true;
                        }
                        String name = args[1];
                        CooldownSystem system = CooldownSystem.getCooldown(name);
                        if (system == null) {
                            lobbyPlayer.sendMessage("&cNo existe un cooldown con este nombre");
                            return true;
                        }
                        lobbyPlayer.sendMessage("&6Iniciando cooldown..");
                        system.prepareFinishAt(); // establece el tiempo de finalización
                        system.prepareStarted();
                        system.setStatus(CooldownSystem.CooldownStatus.ENABLED);
                        plugin.getCooldownDB().createOrUpdate(system, new CallBack.SingleCallBack() {
                            @Override
                            public void onSuccess() {
                                lobbyPlayer.setEditingCooldown(null);
                                lobbyPlayer.sendMessage("&aSe ha iniciado el cooldown: &e" + name);
                            }

                            @Override
                            public void onError() {
                                lobbyPlayer.setEditingCooldown(name);
                                lobbyPlayer.sendMessage("&cNo se ha podido iniciar el cooldown");
                            }
                        });
                        return true;
                    }
                    case "stop": {
                        if (args.length == 1) {
                            lobbyPlayer.sendMessageWithSuggest(
                                    new MessageSuggest("Necesitas especificar el nombre del cooldown /mccooldown start [cooldownName]", "/mccooldown start ", "Crea un cooldown"));
                            return true;
                        }
                        String name = args[1];
                        CooldownSystem system = CooldownSystem.getCooldown(name);
                        if (system == null) {
                            lobbyPlayer.sendMessage("&cNo existe un cooldown con este nombre");
                            return true;
                        }
                        lobbyPlayer.setEditingCooldown(name.toLowerCase());
                        lobbyPlayer.sendMessage("&eEl cooldown " + name.toLowerCase() + " ha iniciado");
                    }
                    case "list": {
                        return true;
                    }
                    case "reload": {
                        plugin.reloadSettings();
                        lobbyPlayer.sendMessage("&aHas recargado la configuración del cooldown");
                        return true;
                    }
                }
            } else {
                sender.sendMessage(Utils.chatColor("&cNo tienes permisos para ejecutar este comando"));
            }
        }
        return true;
    }
}
