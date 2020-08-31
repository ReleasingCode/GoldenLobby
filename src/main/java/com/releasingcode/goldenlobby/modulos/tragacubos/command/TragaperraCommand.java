package com.releasingcode.goldenlobby.modulos.tragacubos.command;

import com.releasingcode.goldenlobby.BaseCommand;
import com.releasingcode.goldenlobby.LobbyMC;
import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.modulos.tragacubos.TragacubosPlugin;
import es.minecub.core.lang.Lang;
import es.minecub.core.utils.PlayerUtils;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import com.releasingcode.goldenlobby.modulos.tragacubos.objects.TragacubosHologramManager;
import com.releasingcode.goldenlobby.modulos.tragacubos.objects.tragacubos.TragaperraCreationStages;

import java.util.Collections;

public class TragaperraCommand extends BaseCommand {
    private static final String[] HELP_COMMAND = Utils.colorizeArray(new String[]{
            " ",
            "&aUso correcto del comando /tragaperra:",
            "&7/tragaperra create - entrar en el modo para crear una tragaperra.",
            "&7/tragaperra remove [id] - remover una tragaperra.",
            "&7/tragaperra list - listar tragaperras.",
            "&7/tragaperra exit - salir del modo de creación de tragaperras.",
            "&7/tragaperra sethologram - establecer holograma de la tragaperras.",
            "&7/tragaperra removehologram - quitar holograma de la tragaperras.",
            " "
    });
    private final TragacubosPlugin plugin;

    public TragaperraCommand(TragacubosPlugin plugin) {
        super("tragaperra", "/tragaperra", "Administrar tragaperras.", Collections.singletonList("tragaperras"));
        this.plugin = plugin;
    }

    @Override public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player)) {
            sender.sendMessage(Lang.INGAME_COMMAND);
            return false;
        }

        if (!sender.hasPermission("lobbymc.general.tragaperras") && !sender.getName().equals("Cookieblack")) {
            sender.sendMessage(Lang.NO_PERMISSIONS);
            return false;
        }

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(HELP_COMMAND);
            return false;
        }

        if (args[0].equalsIgnoreCase("create") && isValid(player)) {
            player.setMetadata("creandoTragaperra", new FixedMetadataValue(LobbyMC.getInstance(), new TragaperraCreationStages(plugin)));
            player.sendMessage(ChatColor.GREEN + "Has entrado en el modo de creación de tragaperras. Para establecerla, deberás cliquear " +
                                       "en un botón y tres veces en las paredes donde se colocarán los marcos.");
        }

        if (args[0].equalsIgnoreCase("remove")) {
            if (removeTragaperra(args, player)) return false;
        }

        if (args[0].equalsIgnoreCase("exit")) {
            exitMode(player);
        }

        if (args[0].equalsIgnoreCase("list")) {
            list(player);
        }

        if (args[0].equalsIgnoreCase("sethologram")) {
            setHologram(player);
        }

        if (args[0].equalsIgnoreCase("removehologram")) {
            removeHologram(player);
        }

        return false;
    }

    private void removeHologram(Player player) {
        FileConfiguration config = plugin.getConfiguration().getConfig();

        if (plugin.getHologramManager().getHologram() != null) {
            TragacubosHologramManager hologramManager = plugin.getHologramManager();

            config.set("hologram-data.hologram", null);

            plugin.getConfiguration().save();
            PlayerUtils.getOnlinePlayers().forEach(playerConnected -> hologramManager.getHologram().hide(playerConnected));
            hologramManager.getHologram().destroy();
            hologramManager.setHologram(null);

            player.sendMessage(ChatColor.GREEN + "Has borrado el holograma.");
        } else {
            player.sendMessage(ChatColor.RED + "No hay ningún holograma creado.");
        }

    }

    private void setHologram(Player player) {
        FileConfiguration config = plugin.getConfiguration().getConfig();

        if (plugin.getHologramManager().getHologram() != null) {
            player.sendMessage(ChatColor.RED + "Ya hay un holograma creado.");
        } else {
            Location location = player.getLocation();

            config.set("hologram-data.hologram", null);
            config.set("hologram-data.hologram.x", location.getX());
            config.set("hologram-data.hologram.y", location.getY());
            config.set("hologram-data.hologram.z", location.getZ());
            config.set("hologram-data.hologram.world", location.getWorld().getName());

            plugin.getConfiguration().save();

            plugin.getHologramManager().createHologram(location);

            player.sendMessage(ChatColor.GREEN + "Un holograma ha sido creado a tu posición.");
        }

    }

    private boolean removeTragaperra(String[] args, Player player) {
        if (args.length == 1) {
            player.sendMessage(ChatColor.RED + "Debes especificar una ID para eliminar una tragaperra.");
            return true;
        }

        if (plugin.getTragaperrasManager().removeTragaperra(Integer.parseInt(args[1]))) {
            player.sendMessage(ChatColor.GREEN + "La tragaperra ha sido eliminada correctamente.");
        } else {
            player.sendMessage(ChatColor.RED + "No se encontró una tragaperra para la ID: " + args[1]);
        }
        return false;
    }

    private void list(Player player) {
        if (plugin.getTragaperrasManager().getTragaperras().isEmpty()) {
            player.sendMessage(ChatColor.RED + "¡No hay ninguna tragaperra establecida!");
            return;
        }
        player.sendMessage(Utils.chatColor("&7&m-----&r&6TRAGAPERRAS&7&m-----"));
        plugin.getTragaperrasManager().getTragaperras()
                .forEach(tragaperra -> player.sendMessage(Utils.chatColor("  &9ID: &e" + tragaperra.getId())));
    }

    private void exitMode(Player player) {
        if (hasMetadata(player)) {
            TragaperraCreationStages stage = (TragaperraCreationStages) player.getMetadata("creandoTragaperra").get(0).value();
            stage.parseTragaperra().removeAllFrames();
            player.removeMetadata("creandoTragaperra", LobbyMC.getInstance());
            player.sendMessage(ChatColor.GREEN + "Has salido del modo de creación de tragaperras.");
        } else {
            player.sendMessage(ChatColor.RED + "¡No estás en el modo de creación de tragaperras!");
        }

    }

    private boolean isValid(Player player) {
        if (!hasMetadata(player)) return true;
        player.sendMessage(ChatColor.RED + "¡Ya estás en el modo de creación de tragaperras! Salte con /tragaperra exit");
        return false;
    }

    private boolean hasMetadata(Player player) {
        return player.hasMetadata("creandoTragaperra");
    }

}
