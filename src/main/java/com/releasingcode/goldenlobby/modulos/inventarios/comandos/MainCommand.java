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
        super("mcinventories", "/mcinventories", "Administrador de inventario");
        this.plugin = plugin;
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (sender instanceof Player) {
            Player p = ((Player) sender).getPlayer();
            LobbyPlayer lobbyPlayer = LobbyPlayerMap.getJugador(p);
            if (!p.hasPermission("lobbymc.inventories.admin")) {
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
                    lobbyPlayer.sendMessage("&aRecargando la configuración de inventarios");
                    plugin.reloadInventories(new CallBack.SingleCallBack() {
                        @Override
                        public void onSuccess() {
                            for (Player p : Bukkit.getOnlinePlayers()) {
                                Inventario.clearItemsSelectorToPlayer(p);
                                Inventario.setSelectorToPlayer(p);
                            }
                            lobbyPlayer.sendMessage("&aSe ha recargado la configuración de inventarios");
                            lobbyPlayer.sendMessage(" &fInventarios cargados: &a" + Inventario.getInventories().size());
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
                                    "No existe un archivo de inventario con ese nombre: " + archivoInventario);
                        }
                    } else {
                        lobbyPlayer.sendMessage("&cComando: &e/mcinventories open [archivo]");
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
                            sender.sendMessage("§cNo existe un inventario con ese nombre");
                        }
                        return true;
                    }
                    fetchDB(sender);
                    sendSync(sender);
                }
                return true;
            }
            lobbyPlayer.sendMessage("&6&lAdministrador de inventarios");
            lobbyPlayer.sendMessage("");
            lobbyPlayer.sendMessage(" &f/mcinventories reload &a- Recargar configuración");
            lobbyPlayer.sendMessage(" &f/mcinventories open [archivo-inventario] &a- Abrir un menú");
            sender.sendMessage(" §f/mcinventories sync [archivo] §a- Sincronizar un inventario");
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
                        sender.sendMessage(Utils.chatColor("&aSe ha recargado la configuración de inventarios"));
                        sender.sendMessage(Utils.chatColor(" &fInventarios cargados: &a" + Inventario.getInventories().size()));
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
                        sender.sendMessage("§cNo existe un inventario con ese nombre");
                    }
                    return true;
                }
                fetchDB(sender);
                sendSync(sender);
            }
        }
        sender.sendMessage("§6§lAdministrador de inventarios");
        sender.sendMessage("");
        sender.sendMessage(" §f/mcinventories reload §a- Recargar configuración");
        sender.sendMessage(" §f/mcinventories sync [archivo] §a- Sincronizar un inventario");
        return true;
    }

    public void sendSync(CommandSender sender) {
        plugin.sendSync(SubChannel.SubOperation.GET_FROM_DB, new CallBack.SingleCallBack() {
            @Override
            public void onSuccess() {
                sender.sendMessage("§aSe ha sincronizado los servidores a la base de datos de inventarios ");
            }

            @Override
            public void onError() {
                sender.sendMessage("§cError al sincronizar los servidores con los inventarios ");
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
                                "§aSe ha sincronizado a la base de datos el inventario: " + archivoInventario);
                    }

                    @Override
                    public void onError() {
                        sender.sendMessage("§cError al sincronizar con Redis el inventario: " + archivoInventario);
                    }
                });
            }

            @Override
            public void onError() {
                // siempre es -1
                sender.sendMessage("§cNo se pudo sincronizar a la base de datos el inventario: §a" + archivoInventario);
            }
        });
    }

    public void fetchDB(CommandSender sender) {
        sender.sendMessage("§aObteniendo datos registrados en la base de datos");
        InventarioPlugin.getInstance().reloadInventories(() -> {
            Utils.log("Se ha recargado el modulo de inventarios");
            sender.sendMessage("§aSe ha recargado el modulo de inventarios");
        }, true);
    }
}
