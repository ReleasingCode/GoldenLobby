package com.releasingcode.goldenlobby.modulos.inventarios.comandos;

import com.releasingcode.goldenlobby.BaseCommand;
import com.releasingcode.goldenlobby.GoldenLobby;
import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.call.CallBack;
import com.releasingcode.goldenlobby.configuracion.CustomConfiguration;
import com.releasingcode.goldenlobby.database.pubsub.SubChannel;
import com.releasingcode.goldenlobby.managers.LobbyPlayer;
import com.releasingcode.goldenlobby.managers.LobbyPlayerMap;
import com.releasingcode.goldenlobby.modulos.inventarios.InventarioPlugin;
import com.releasingcode.goldenlobby.modulos.inventarios.builder.ItemMenuHolder;
import com.releasingcode.goldenlobby.modulos.inventarios.manager.Inventario;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

public class MainCommand extends BaseCommand {
    InventarioPlugin plugin;

    public MainCommand(InventarioPlugin plugin) {
        super("mcinventories", "/mcinventories", "Inventory Manager");
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = ((Player) sender).getPlayer();
            LobbyPlayer lobbyPlayer = LobbyPlayerMap.getJugador(p);
            if (!p.hasPermission("goldenlobby.inventories.admin")) {
                return true;
            }
            if (args.length > 0) {
                if (args[0].toLowerCase().equals("reload")) {
                    for (Player op : Bukkit.getOnlinePlayers()) {
                        if (op.getOpenInventory() != null) {
                            if (op.getOpenInventory().getTopInventory() != null) {
                                if (op.getOpenInventory().getTopInventory().getHolder()
                                        instanceof ItemMenuHolder) {
                                    op.closeInventory();
                                }
                            }
                        }
                    }
                    lobbyPlayer.sendMessage("&aReloading the inventory configuration");
                    plugin.reloadInventories(new CallBack.SingleCallBack() {
                        @Override
                        public void onSuccess() {
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                Inventario.clearItemsSelectorToPlayer(p);
                                Inventario.setSelectorToPlayer(p);
                            }
                            lobbyPlayer.sendMessage("&aInventory configuration has been reloaded");
                            lobbyPlayer.sendMessage(" &fLoaded inventories: &a" + Inventario.getInventories().size());
                        }

                        @Override
                        public void onError() {

                        }
                    }, false);
                    return true;
                }
                if (args[0].equals("open")) {
                    if (args.length > 1) {
                        String archivoInventario = args[1];
                        Inventario inv = Inventario.getInventoryByName(archivoInventario);
                        if (inv != null) {
                            if (p.getOpenInventory() != null) {
                                p.closeInventory();
                            }
                            inv.openInventory(p);
                        } else {
                            lobbyPlayer.sendMessage(
                                    "There is no inventory file by that name.: " + archivoInventario);
                        }
                    } else {
                        lobbyPlayer.sendMessage("&cCommand: &e/mcinventories open [archivo]");
                    }
                    return true;
                }
                if (args[0].toLowerCase().equals("sync")) {
                    if (!GoldenLobby.getInstance().isMysqlEnable()) {
                        return true;
                    }
                    if (args.length > 1) {
                        String archivoInventario = args[1];
                        Inventario inv = Inventario.getInventoryByName(archivoInventario);
                        if (inv != null) {
                            sendSyncRebaseConfig(sender, p.getName(), inv.getCustomConfiguration(), archivoInventario);
                        } else {
                            sender.sendMessage("§cThere is no inventory under that name");
                        }
                        return true;
                    }
                    fetchDB(sender);
                    sendSync(sender);
                }
                return true;
            }
            lobbyPlayer.sendMessage("&6&lInventory Manager");
            lobbyPlayer.sendMessage("");
            lobbyPlayer.sendMessage(" &f/mcinventories reload &a- Reload Configuration");
            lobbyPlayer.sendMessage(" &f/mcinventories open [archivo-inventario] &a- Open a menu");
            sender.sendMessage(" §f/mcinventories sync [archivo] §a- Synchronize an inventory");
            return true;
        }
        if (args.length > 0) {
/*
              Recargar configuración
             */
            if (args[0].toLowerCase().equals("reload")) {
                for (Player op : Bukkit.getOnlinePlayers()) {
                    if (op.getOpenInventory() != null) {
                        if (op.getOpenInventory().getTopInventory() != null) {
                            if (op.getOpenInventory().getTopInventory().getHolder()
                                    instanceof ItemMenuHolder) {
                                op.closeInventory();
                            }
                        }
                    }
                }
                plugin.reloadInventories(new CallBack.SingleCallBack() {
                    @Override
                    public void onSuccess() {
                        for (Player p : Bukkit.getOnlinePlayers()) {
                            Inventario.clearItemsSelectorToPlayer(p);
                            Inventario.setSelectorToPlayer(p);
                        }
                        sender.sendMessage(Utils.chatColor("&aInventory configuration has been reloaded"));
                        sender.sendMessage(Utils.chatColor(" &fLoaded inventories: &a" + Inventario.getInventories().size()));
                    }

                    @Override
                    public void onError() {

                    }
                }, false);
                return true;
            }
            if (args[0].toLowerCase().equals("sync")) {
                if (args.length > 1) {
                    String archivoInventario = args[1];
                    Inventario inv = Inventario.getInventoryByName(archivoInventario);
                    if (inv != null) {
                        sendSyncRebaseConfig(sender, "console", inv.getCustomConfiguration(), archivoInventario);
                    } else {
                        sender.sendMessage("§cThere is no inventory under that name");
                    }
                    return true;
                }
                fetchDB(sender);
                sendSync(sender);
            }
        }
        sender.sendMessage("§6§lInventory Manager");
        sender.sendMessage("");
        sender.sendMessage(" §f/mcinventories reload §a- Reload Configuration");
        sender.sendMessage(" §f/mcinventories sync [archivo] §a- Synchronize an inventory");
        return true;
    }

    public void sendSync(CommandSender sender) {
        plugin.sendSync(SubChannel.SubOperation.GET_FROM_DB, new CallBack.SingleCallBack() {
            @Override
            public void onSuccess() {
                sender.sendMessage("§aServers have been synchronized with the inventory database ");
            }

            @Override
            public void onError() {
                sender.sendMessage("§cError when synchronizing servers with inventories ");
            }
        });
    }

    public void sendSyncRebaseConfig(CommandSender sender, String by, CustomConfiguration customConfiguration, String archivoInventario) {
        plugin.syncConfig(sender, by, customConfiguration, new CallBack.SingleCallBack() {
            @Override
            public void onSuccess() {
                plugin.sendSync(SubChannel.SubOperation.GET_FROM_DB, new CallBack.SingleCallBack() {
                    @Override
                    public void onSuccess() {
                        sender.sendMessage(
                                "§aInventory has been synchronized to the database: " + archivoInventario);
                    }

                    @Override
                    public void onError() {
                        sender.sendMessage("§cError synchronizing inventory with Redis: " + archivoInventario);
                    }
                });
            }

            @Override
            public void onError() {
                // siempre es -1
                sender.sendMessage("§cInventory could not be synchronized to the database: §a" + archivoInventario);
            }
        });
    }

    public void fetchDB(CommandSender sender) {
        sender.sendMessage("§aObtaining data recorded in the database");
        InventarioPlugin.getInstance().reloadInventories(() -> {
            Utils.log("Inventory module has been reloaded");
            sender.sendMessage("§aInventory module has been reloaded");
        }, true);
    }
}
