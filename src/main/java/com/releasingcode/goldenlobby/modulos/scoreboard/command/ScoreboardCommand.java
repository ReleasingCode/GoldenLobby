package com.releasingcode.goldenlobby.modulos.scoreboard.command;

import com.releasingcode.goldenlobby.BaseCommand;
import com.releasingcode.goldenlobby.GoldenLobby;
import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.call.CallBack;
import com.releasingcode.goldenlobby.database.pubsub.SubChannel;
import com.releasingcode.goldenlobby.extendido.scoreboard.Sidebar;
import com.releasingcode.goldenlobby.languages.Lang;
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
        if (!sender.hasPermission("goldenlobby.scoreboard.admin")) {
            return true;
        }
        if (args.length == 0) {
            sender.sendMessage(Utils.chatColor(Lang.COMMAND_ARE_MISSING.toString()));
            sender.sendMessage(Utils.chatColor("&e  - /mcscoreboard reload"));
            sender.sendMessage(Utils.chatColor("&e  - /mcscoreboard sync [rebase]"));
            return true;
        }
        if (args[0].equals("reload")) {
            sender.sendMessage(Utils.chatColor(Lang.RELOADING_SCOREBOARD.toString()));
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
                    sender.sendMessage(Utils.chatColor(Lang.UPDATING_SCOREBOARD.toString()));
                    try {
                        plugin.getScoreboardDB().createConfiguration(sender.getName(), plugin.getScoreboardConfig(), new CallBack.SingleCallBack() {
                            @Override
                            public void onSuccess() {
                                plugin.sendSync(SubChannel.SubOperation.PURGE_AND_GET_FROM_DB, new CallBack.SingleCallBack() {
                                    @Override
                                    public void onSuccess() {
                                        sender.sendMessage(Utils.chatColor(Lang.UPDATE_COMPLETED.toString()));
                                    }

                                    @Override
                                    public void onError() {
                                        sender.sendMessage(Utils.chatColor(Lang.DATA_HAS_BEEN_UPDATED.toString()));
                                    }
                                });
                            }

                            @Override
                            public void onError() {
                                sender.sendMessage(Utils.chatColor(Lang.ERROR_WHILE_UPDATING_SCOREBOARD.toString()));
                            }
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return true;
                }
                return true;
            }
            sender.sendMessage(Utils.chatColor((Lang.SYNC_SERVERS_DB.toString())));
            plugin.fetchFromDatabase(new CallBack.SingleCallBack() {
                @Override
                public void onSuccess() {
                    plugin.sendSync(SubChannel.SubOperation.GET_FROM_DB, new CallBack.SingleCallBack() {
                        @Override
                        public void onSuccess() {
                            sender.sendMessage(Utils.chatColor(Lang.UPDATE_COMPLETED.toString()));
                        }

                        @Override
                        public void onError() {
                            sender.sendMessage(Utils.chatColor(Lang.DATA_HASBEEN_UPDATED_BUT_NO_SYNC_REDIS.toString()));
                        }
                    });
                }

                @Override
                public void onError() {
                    sender.sendMessage(Utils.chatColor(Lang.ERROR_WHILE_REQUESTING.toString()));
                    sender.sendMessage(Utils.chatColor("&7 - No data available"));
                    sender.sendMessage(Utils.chatColor("&7 - No connection could be established"));
                }
            });

            return true;
        }
        return false;
    }

}
