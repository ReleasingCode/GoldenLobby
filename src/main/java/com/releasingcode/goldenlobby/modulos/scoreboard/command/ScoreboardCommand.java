package com.releasingcode.goldenlobby.modulos.scoreboard.command;

import com.releasingcode.goldenlobby.BaseCommand;
import com.releasingcode.goldenlobby.GoldenLobby;
import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.call.CallBack;
import com.releasingcode.goldenlobby.database.pubsub.SubChannel;
import com.releasingcode.goldenlobby.extendido.scoreboard.Sidebar;
import com.releasingcode.goldenlobby.modulos.scoreboard.ScoreboardPlugin;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;

import java.io.IOException;

public class ScoreboardCommand extends BaseCommand {
    private final ScoreboardPlugin plugin;

    public ScoreboardCommand(ScoreboardPlugin plugin, String command, String usage, String description) {
        super(command, usage, description);
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!sender.hasPermission("lobbymc.scoreboard.admin")) {
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(Utils.chatColor("&a Faltan argumentos para esté comando: "));
            sender.sendMessage(Utils.chatColor("&e  - /mcscoreboard reload"));
            sender.sendMessage(Utils.chatColor("&e  - /mcscoreboard sync [rebase]"));
            return true;
        }
        if (args[0].equals("reload")) {
            sender.sendMessage(Utils.chatColor("&a Recargando scoreboard"));
            plugin.getScoreboardManager().clear();
            Sidebar.exit();
            plugin.loadScoreboard();
            return true;
        }
        if (args[0].equals("sync")) {
            if (!GoldenLobby.getInstance().isMysqlEnable()) {
                return true;
            }
            if (args.length > 1) {
                if (args[1].equals("rebase")) {
                    sender.sendMessage(Utils.chatColor("&a Actualizando el Scoreboard en la base de datos"));
                    try {
                        plugin.getScoreboardDB().createConfiguration(sender.getName(), plugin.getScoreboardConfig(), new CallBack.SingleCallBack() {
                            @Override
                            public void onSuccess() {
                                plugin.sendSync(SubChannel.SubOperation.PURGE_AND_GET_FROM_DB, new CallBack.SingleCallBack() {
                                    @Override
                                    public void onSuccess() {
                                        sender.sendMessage(Utils.chatColor("&aActualización y sincronización completada"));
                                    }

                                    @Override
                                    public void onError() {
                                        sender.sendMessage(Utils.chatColor("&cLos datos han sido actualizados pero no sincronizados"));
                                    }
                                });
                            }

                            @Override
                            public void onError() {
                                sender.sendMessage(Utils.chatColor("&aError mientras se actualizaba el scoreboard en la base de datos"));
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
                return true;
            }
            sender.sendMessage(Utils.chatColor(("&a Sincronizando los servidores con la base de datos")));
            plugin.fetchFromDatabase(new CallBack.SingleCallBack() {
                @Override
                public void onSuccess() {
                    plugin.sendSync(SubChannel.SubOperation.GET_FROM_DB, new CallBack.SingleCallBack() {
                        @Override
                        public void onSuccess() {
                            sender.sendMessage(Utils.chatColor("&aActualización y sincronización completada"));
                        }

                        @Override
                        public void onError() {
                            sender.sendMessage(Utils.chatColor("&cError mientras se sincronizaba, se ha actualizado los datos pero no se han sincronizado con redis"));
                        }
                    });
                }

                @Override
                public void onError() {
                    sender.sendMessage(Utils.chatColor("&cError mientras se solicitaba el scoreboard a la base de datos"));
                    sender.sendMessage(Utils.chatColor("&7 - No hay datos"));
                    sender.sendMessage(Utils.chatColor("&7 - No se pudo establecer una conexión"));
                }
            });

            return true;
        }
        return false;
    }

}
