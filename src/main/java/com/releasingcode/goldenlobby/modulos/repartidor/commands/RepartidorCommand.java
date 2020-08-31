package com.releasingcode.goldenlobby.modulos.repartidor.commands;


import com.releasingcode.goldenlobby.BaseCommand;
import com.releasingcode.goldenlobby.LobbyMC;
import com.releasingcode.goldenlobby.modulos.repartidor.RepartidorCorePlugin;
import es.minecub.core.minecubos.MinecubosAPI;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Entity;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import com.releasingcode.goldenlobby.modulos.repartidor.items.Item;
import com.releasingcode.goldenlobby.modulos.repartidor.items.ItemType;
import com.releasingcode.goldenlobby.modulos.repartidor.items.VoteItem;
import com.releasingcode.goldenlobby.modulos.repartidor.manager.RepartidorManager;
import com.releasingcode.goldenlobby.modulos.repartidor.playerdata.MinecubPlayer;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RepartidorCommand extends BaseCommand {
    List<String> tempUUIDs = new ArrayList<>();

    public RepartidorCommand(String command) {
        super(command);
    }


    public boolean onCommand(CommandSender sender, Command cmd, String label, final String[] args) {
        Player p;
        if (!(sender instanceof Player)) {
            if (args.length != 3 && args.length != 4) {
                sender.sendMessage(
                        ChatColor.RED + "Este comando no está pensado para ser ejecutado por un ser humano... pero si insistes, te contaré el secreto de su uso: /tdms vote <usuario> <servicio>.");
            } else {
                if (args[0].equals("vote")) {
                    if (args.length == 4) {
                        if (this.tempUUIDs.contains(args[3])) {
                            return true;
                        }

                        this.tempUUIDs.add(args[3]);
                        Bukkit.getScheduler()
                                .runTaskLater(LobbyMC.getInstance(), new Runnable() {
                                    public void run() {
                                        RepartidorCommand.this.tempUUIDs.remove(args[3]);
                                    }
                                }, 2400L);
                    } else {
                        sender.sendMessage(
                                ChatColor.RED + "Veo que has decidido no hacerme caso... En ese caso, ejecutaré el comando.");
                    }

                    p = Bukkit.getPlayerExact(args[1]);
                    for (Item item : RepartidorManager.getItems().values()) {
                        if (item.getType() == ItemType.VOTEPAGE && ((VoteItem) item).getServiceName().equals(args[2])) {
                            if (p != null && p.isOnline()) {
                                p.sendMessage(
                                        ChatColor.GOLD + "¡Gracias por votarnos!" + ChatColor.GREEN + " Como recompensa " + "te hemos dado " + ChatColor.AQUA + item
                                                .getCoins() + ChatColor.GREEN + " minecubos.");
                                MinecubPlayer mp = RepartidorCorePlugin.getPlayer(p);
                                if (mp == null) {
                                    return true;
                                }
                                Bukkit.getScheduler().runTaskAsynchronously(LobbyMC.getInstance(), () -> {
                                    MinecubosAPI.giveMinecubos(p, item.getCoins(), true);
                                });
                                long time = (new Date()).getTime();
                                mp.getLongs().put(item.getName(), time);
                                RepartidorManager.updateLong(p.getName(), item.getName(), time);
                            } else {
                                long time = (new Date()).getTime();
                                RepartidorManager.updateLong(args[1], item.getName(), time);
                            }

                            return true;
                        }
                    }
                } else if (args[0].equals("reconnect")) {
                    RepartidorManager.openConnection();
                }

            }
            return true;
        }

        p = (Player) sender;
        if (args.length != 1) {
            if (!sender.hasPermission("core.admin")) {
                return true;
            }
            p.sendMessage("/repartidor setnpc");
            p.sendMessage("/repartidor resetitems");
        } else {
            if (args[0].equalsIgnoreCase("setnpc")) {
                if (!sender.hasPermission("core.admin")) {
                    return true;
                }
                RepartidorCorePlugin.getInstance().getConfig().set("TDM",
                        p.getWorld().getName() + ", " + p.getLocation().getX() + ", " + p.getLocation()
                                .getY() + ", " + p.getLocation().getZ() + ", " + p.getLocation().getYaw() + ", " + p
                                .getLocation().getPitch());
                RepartidorCorePlugin.getInstance().saveConfig();
                p.sendMessage("NPC set.");
            } else if (args[0].equalsIgnoreCase("resetitems")) {
                if (!sender.hasPermission("core.admin")) {
                    return true;
                }
                for (Entity ent : Bukkit.getWorlds().get(0).getEntities()) {
                    if (ent.getType() == EntityType.DROPPED_ITEM) {
                        ent.remove();
                    }
                }
                p.sendMessage("Items borrados.");
            } else if (args[0].equalsIgnoreCase("open")) {
                MinecubPlayer minecubPlayer = RepartidorCorePlugin.getPlayer(p);
                p.openInventory(minecubPlayer.getInventory());
            }

        }
        return true;

    }
}
