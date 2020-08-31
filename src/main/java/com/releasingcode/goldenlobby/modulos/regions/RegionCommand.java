package com.releasingcode.goldenlobby.modulos.regions;

import com.releasingcode.goldenlobby.BaseCommand;
import com.releasingcode.goldenlobby.LobbyMC;
import com.releasingcode.goldenlobby.Utils;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.metadata.FixedMetadataValue;
import com.releasingcode.goldenlobby.modulos.regions.cuboid.Cuboid;
import com.releasingcode.goldenlobby.modulos.regions.cuboid.StageOfCreation;

import java.util.EnumMap;

public class RegionCommand extends BaseCommand {
    private static final String[] HELP_COMMAND = Utils.colorizeArray(new String[]{
            " ",
            "&aUso correcto del comando /region:",
            "&7/region create [lobby/pvp] - entrar en el modo para crear una región.",
            "&7/region remove [lobby/pvp] - remover una región.",
            "&7/region list - listar regiones.",
            "&7/region exit - salir del modo de creación de regiones.",
            " "
    });

    private final RegionPlugin plugin;

    public RegionCommand(RegionPlugin plugin) {
        super("region", "/region", "Establecer y quitar regiones.");
        this.plugin = plugin;
    }

    @Override public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if (!(sender instanceof Player) || !sender.hasPermission("lobbymc.regions")) return false;

        Player player = (Player) sender;

        if (args.length == 0) {
            player.sendMessage(HELP_COMMAND);
            return false;
        }

        if (args[0].equalsIgnoreCase("create") && hasValidArgs(player, args)) {
            createRegion(player, args[1]);
        }

        if (args[0].equalsIgnoreCase("exit")) {
            exitRegion(player);
        }

        if (args[0].equalsIgnoreCase("list")) {
            listRegions(player);
        }

        if (args[0].equalsIgnoreCase("remove")) {
            removeRegion(player, args);
        }

        return false;
    }

    private void removeRegion(Player player, String[] args) {
        if (args.length > 1) {
            String regionName = args[1];
            EnumMap<StageOfCreation.Regions, Cuboid> regions = plugin.getCuboidManager().getRegions();
            if (regions.containsKey(StageOfCreation.Regions.valueOf(regionName.toUpperCase()))) {
                plugin.getCuboidManager().removeRegion(StageOfCreation.Regions.valueOf(regionName.toUpperCase()));
                plugin.getConfiguration().getConfig().set("regiones." + regionName.toLowerCase(), null);
                player.sendMessage(ChatColor.GREEN + "Has removido la región " + regionName + " correctamente.");
                return;
            }
            player.sendMessage(ChatColor.RED + "¡La región " + regionName + " no existe!");
        } else {
            player.sendMessage(HELP_COMMAND);
        }
    }

    private void listRegions(Player player) {
        EnumMap<StageOfCreation.Regions, Cuboid> regions = plugin.getCuboidManager().getRegions();

        if (regions.isEmpty()) {
            player.sendMessage(ChatColor.GREEN + "No hay regiones establecidas.");
            return;
        }

        player.sendMessage(Utils.chatColor("&7&m-----&r&6REGIONES&7&m-----"));
        regions.forEach((regiones, cuboid) -> {
            player.sendMessage(Utils.chatColor("&6R: &7" + regiones.name() + " &6/ Coordenadas: "));
            player.sendMessage(Utils.chatColor("   &9PUNTO UNO&6: x: &7" + cuboid.getPoint1().getX() + " &6y: &7" + cuboid.getPoint1().getY() + " &6z: &7" + cuboid.getPoint1().getZ()));
            player.sendMessage(Utils.chatColor("   &9PUNTO DOS&6: x: &7" + cuboid.getPoint2().getX() + " &6y: &7" + cuboid.getPoint2().getY() + " &6z: &7" + cuboid.getPoint2().getZ()));
            player.sendMessage(Utils.chatColor("   &6* Cantidad de bloques: &7" + cuboid.getTotalBlockSize()));
        });
        player.sendMessage(" ");
    }

    private boolean hasValidArgs(Player player, String[] args) {
        if (args.length < 2) {
            player.sendMessage(HELP_COMMAND);
            return false;
        }

        if (args[1].equalsIgnoreCase("lobby") || args[1].equalsIgnoreCase("pvp")) {
            if (plugin.getCuboidManager().getRegions().containsKey(StageOfCreation.Regions.valueOf(args[1].toUpperCase()))) {
                player.sendMessage(ChatColor.RED + "¡Esa región ya está creada!");
                return false;
            }
            return true;
        }

        player.sendMessage(HELP_COMMAND);
        return false;
    }

    private void createRegion(Player player, String regionName) {
        if (player.hasMetadata("creatingRegion")) {
            player.sendMessage(ChatColor.RED + "¡Ya estás en el modo de creación de región! Desactívalo con /region exit");
        } else {
            player.setMetadata("creatingRegion", new FixedMetadataValue(LobbyMC.getInstance(), new StageOfCreation(StageOfCreation.Regions.valueOf(regionName.toUpperCase()))));
            player.sendMessage(ChatColor.GREEN + "Has entrado al modo de creación de regiones. Debes cliquear dos bloques, el primero marcará el inicio de la región, y el segundo su fin.");
        }
    }

    private void exitRegion(Player player) {
        if (player.hasMetadata("creatingRegion")) {
            player.sendMessage(ChatColor.GREEN + "Has salido del modo de creación de región correctamente.");
            player.removeMetadata("creatingRegion", LobbyMC.getInstance());
        } else {
            player.sendMessage(ChatColor.RED + "¡No estás en el modo de creación de región!");
        }
    }

}
