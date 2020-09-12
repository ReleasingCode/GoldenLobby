package com.releasingcode.goldenlobby.modulos.npcserver.comandos;

import com.releasingcode.goldenlobby.BaseCommand;
import com.releasingcode.goldenlobby.GoldenLobby;
import com.releasingcode.goldenlobby.Utils;
import com.releasingcode.goldenlobby.call.CallBack;
import com.releasingcode.goldenlobby.database.pubsub.SubChannel;
import com.releasingcode.goldenlobby.languages.Lang;
import com.releasingcode.goldenlobby.managers.LobbyPlayer;
import com.releasingcode.goldenlobby.managers.LobbyPlayerMap;
import com.releasingcode.goldenlobby.managers.MessageSuggest;
import com.releasingcode.goldenlobby.managers.SkinGameProfile;
import com.releasingcode.goldenlobby.modulos.npcserver.NPCServerPlugin;
import com.releasingcode.goldenlobby.modulos.npcserver.object.LobbyPlayerBuilder;
import com.releasingcode.goldenlobby.npc.api.NPC;
import com.releasingcode.goldenlobby.npc.api.skin.Skin;
import com.releasingcode.goldenlobby.npc.api.skin.SkinFetcher;
import com.releasingcode.goldenlobby.npc.api.state.NPCMode;
import com.releasingcode.goldenlobby.npc.api.state.NPCPosition;
import com.releasingcode.goldenlobby.npc.internal.NPCManager;
import org.bukkit.Bukkit;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.Random;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;

public class NpcServerCommand extends BaseCommand {
    private final GoldenLobby lobbyMC;
    NPCServerPlugin plugin;

    public NpcServerCommand(NPCServerPlugin plugin, String command, String usage, String description) {
        super(command, usage, description);
        this.plugin = plugin;
        this.lobbyMC = GoldenLobby.getInstance();
    }

    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        // es un console

        if (args.length == 0) {
            if (!sender.hasPermission("goldenlobby.npcserver.create")) {
                sender.sendMessage(Lang.NO_PERMISSION.toString());
                return true;
            }
            sender.sendMessage(Utils.chatColor(Lang.COMMAND_ENABLE.toString()));
            sender.sendMessage(Utils.chatColor("&e - reload"));
            sender.sendMessage(Utils.chatColor("&e - sync [purge]"));
            return true;
        }

        switch (args[0]) {
            case "reset": {
                if (args.length > 1) {
                    String nombre = args[1];
                    Player playerArg = Bukkit.getPlayer(nombre);
                    if (playerArg == null) {
                        sender.sendMessage(Utils.chatColor(
                                Lang.YOU_CANNOT_RESTART_STATISTICS.toString()));
                        return true;
                    }
                    LobbyPlayer player1 = LobbyPlayerMap.getJugador(playerArg);
                    if (player1 != null) {
                        plugin.getHistoryDB().resetPlayer(player1,
                                new CallBack.SingleCallBack() {
                                    @Override
                                    public void onSuccess() {
                                        sender.sendMessage(Utils.chatColor(
                                                Lang.YOU_HAS_RESET_HISTORY.toString() + nombre));
                                    }

                                    @Override
                                    public void onError() {
                                        sender.sendMessage(Utils.chatColor(
                                                Lang.AN_ERROR_REMOVE_HISTORY.toString() + nombre + " [DB]"));
                                    }
                                });
                        return true;
                    }
                    sender.sendMessage(Utils.chatColor(
                            Lang.AN_ERROR_REMOVE_HISTORY.toString() + nombre));
                    return true;
                }
                sender.sendMessage(Utils.chatColor(
                        Lang.SPECIFY_NAME_RESTART_HISTORY_STATISTICS.toString()));
                return true;
            }
            case "sync": {
                if (!sender.hasPermission("goldenlobby.npcserver.create")) {
                    sender.sendMessage(Utils.chatColor(Lang.NO_PERMISSION.toString()));
                    return true;
                }
                if (isReadyNPCs()) {
                    sender.sendMessage(Utils.chatColor(Lang.SYNC_NPC.toString()));
                    if (args.length == 1) {
                        sender.sendMessage(Utils.chatColor("&a¡Synchronizing servers!"));
                        plugin.reloadNPC(new CallBack.SingleCallBack() {
                            @Override
                            public void onSuccess() {
                                plugin.sendSync(SubChannel.SubOperation.GET_FROM_DB);
                                sender.sendMessage(Utils.chatColor("&aSynchronization completed!"));
                            }

                            @Override
                            public void onError() {
                                sender.sendMessage(Utils.chatColor(
                                        "&cAn error occurred while reloading the configuration and NPC's"));
                            }
                        }, SubChannel.SubOperation.GET_FROM_DB);
                        return true;
                    }
                    switch (args[1]) {
                        case "purge": {
                            purgeSync(sender);
                            return true;
                        }
                        case "rebase": {
                            rebaseSync(sender);
                            return true;
                        }
                    }
                } else {
                    sender.sendMessage(Utils.chatColor(Lang.YOU_CANNOT_SYNC_NCP_READY.toString()));
                    sender.sendMessage(Utils.chatColor("&cThis occurs when a person is editing an NPC"));
                }
                return true;
            }
            case "reload": {
                if (!sender.hasPermission("goldenlobby.npcserver.create")) {
                    sender.sendMessage(Utils.chatColor(Lang.NO_PERMISSION.toString()));
                    return true;
                }
                if (isReadyNPCs()) {
                    if (args.length > 1) {
                        String npcNombre = args[1];
                        NPC npc = NPCManager.getNPC(npcNombre);
                        if (npc == null) {
                            sender
                                    .sendMessage(Utils.chatColor(Lang.NO_EXIST_NPC_WHILE_RELOAD.toString()));
                            return true;
                        }
                        sender.sendMessage(Utils.chatColor("&eReloading NPC: " + npcNombre));
                        plugin.reloadNPC(npc, () -> {
                            sender.sendMessage(Utils.chatColor(Lang.NPC_HAS_BEEN_RELOADED.toString() + npcNombre));
                        }, SubChannel.SubOperation.GET_FROM_LOCAL);
                        return true;
                    }
                    sender.sendMessage(Utils.chatColor(Lang.RELOADING_CONFIG_NPC.toString()));
                    plugin.reloadNPC(new CallBack.SingleCallBack() {
                        @Override
                        public void onSuccess() {
                            sender.sendMessage(Utils.chatColor(Lang.SUCCESSFULLY_RELOADED.toString()));
                        }

                        @Override
                        public void onError() {
                            sender.sendMessage(Utils.chatColor(
                                    Lang.ERROR_OCURRED_WHILE_RELOADING_CONFIG.toString()));
                        }
                    }, SubChannel.SubOperation.GET_FROM_LOCAL);
                    return true;

                } else {
                    sender.sendMessage(Utils.chatColor(Lang.YOU_CANNOT_SYNC_NCP_READY.toString()));
                }
                return true;
            }
        }
        if (sender instanceof Player) {
            Player player = ((Player) sender).getPlayer();
            LobbyPlayer lobbyPlayer = LobbyPlayerMap.getJugador(player);
            LobbyPlayerBuilder builder = lobbyPlayer.getNpcBuilder();
            if (player.hasPermission("goldenlobby.npcserver.create")) {
                //mcnpc create den
                // builder
                // - mcnpc addline &a&l¡ACTUALIZACIÓN! (x++)
                // - mcnpc editline [linea] -> por favor escriba en el chat la linea
                // - mcnpc removeline [linea] -> linea eliminada (de arriba hacia abajo)
                // - mcnpc targetserver [nombre] (BungeeCord)
                // - mcnpc done
                switch (args[0]) {
                    case "create": {
                        if (builder.isEditing()) {
                            lobbyPlayer.sendMessage(Lang.YOU_CURRENTLY_EDITING.toString(),
                                    Lang.COMMAND_ENABLE.toString(),
                                    "  &7 - /mcnpc addline (text) &eAdd a line of text about the NPC",
                                    "  &7 - /mcnpc removeline (linea) &eRemove a line of text from the NPC",
                                    "  &7 - /mcnpc done &eClose NPC edit mode", "");
                            return true;
                        }
                        if (args.length == 1) {
                            lobbyPlayer.sendMessage(" &c[*] You must write the NPC mode");
                            lobbyPlayer.sendMessage("  &7/mcnpc create (ModoNPC) NombreId");
                            return true;
                        }
                        String modo = args[1];
                        if (NPCMode.from(modo) == null) {
                            lobbyPlayer.sendMessage(" &c[*] " + Lang.TYPE_A_VALID_NCP.toString() + ":",
                                    "  &e- COMMAND",
                                    "  &e- HISTORY",
                                    "  &e- STAFF");
                            return true;
                        }
                        if (args.length == 2) {
                            lobbyPlayer.sendMessage(" &c[*] " + Lang.WRITE_NAME_NCP_IDENTIFIER.toString());
                            lobbyPlayer.sendMessage("  &7/mcnpc create NombreId");
                            return true;
                        }
                        String name = args[2];
                        if (NPCManager.alreadyNPC(name)) {
                            lobbyPlayer
                                    .sendMessage(" &c[*] " + Lang.ENTER_IDENTIFIER_NAME_ANOTHERNPC.toString());
                            return true;
                        }
                        builder.setEditing(name);
                        UUID uuid = new UUID(new Random().nextLong(), 0);
                        NPC npc = GoldenLobby.getInstance().getNpcLib().createNPC(name, uuid, name);
                        npc.setEditing(true);
                        npc.setLocation(player.getLocation());
                        npc.setMode(NPCMode.from(modo));
                        npc.create();
                        npc.setReady(true);
                        NPCManager.addFullyLoaded(npc);
                        lobbyPlayer.sendMessage("",
                                Lang.YOU_HAVE_CREATED_NPC.toString() + name,
                                Lang.EDITING_MODE_HAS_BEEN_ACTIVED.toString(), "");
                        sendHelpEditing(lobbyPlayer);
                        return true;
                    }
                    case "tp": {
                        if (builder.isEditing()) {
                            lobbyPlayer.sendMessage(
                                    Lang.YOU_CANNOT_EXECUTE_COMMAND_WHILE_EDIT.toString());
                            return true;
                        }
                        if (args.length == 1) {
                            lobbyPlayer.sendMessage(Lang.WRITE_NAMENCP_TO_TELEPORT.toString());
                            return true;
                        }
                        String name = args[1];
                        NPC npc = NPCManager.getNPC(name);
                        if (npc != null) {
                            lobbyPlayer.sendMessage(Lang.TP_TO_NCP.toString() + npc.getName());
                            player.teleport(npc.getLocation());
                        } else {
                            lobbyPlayer.sendMessage(Lang.NO_NCP_FOUND_WHIT_NAME.toString());
                        }
                        return true;
                    }
                    case "commands": {
                        if (!builder.isEditing()) {
                            lobbyPlayer.sendMessage(Lang.YOU_MUST_EDIT_MODE.toString());
                            return true;
                        }
                        if (builder.getNpc().getCommand().isEmpty()) {
                            lobbyPlayer.sendMessage(Lang.THIS_NCP_NO_COMMANDS.toString());
                            return true;
                        }
                        lobbyPlayer.sendMessage(Lang.LIST_OF_COMMANDS_FROM.toString() + builder.getNpc().getName());
                        AtomicInteger i = new AtomicInteger();
                        builder.getNpc().getCommand().forEach(Comando -> {
                            lobbyPlayer.sendMessage((i.getAndIncrement()) + ".- &e" + command);
                        });
                        return true;
                    }
                    case "rewardcommands": {
                        if (!builder.isEditing()) {
                            lobbyPlayer.sendMessage(Lang.YOU_MUST_EDIT_MODE.toString());
                            return true;
                        }
                        if (builder.getNpc().getRewardCommands().isEmpty()) {
                            lobbyPlayer.sendMessage(Lang.NPC_BOUNTY_COMMANDS.toString());
                            return true;
                        }
                        lobbyPlayer.sendMessage(Lang.REAWARD_COMMAND_LIST.toString() + builder.getNpc().getName());
                        AtomicInteger i = new AtomicInteger();
                        builder.getNpc().getRewardCommands().forEach(Comando -> {
                            lobbyPlayer.sendMessage((i.getAndIncrement()) + ".- &e" + command);
                        });
                        return true;
                    }
                    case "list": {
                        if (builder.isEditing()) {
                            lobbyPlayer.sendMessage(
                                    Lang.YOU_CANNOT_EXECUTE_COMMAND_WHILE_EDIT.toString());
                            return true;
                        }
                        lobbyPlayer.sendMessage(Lang.LIST_OF_NPC.toString());
                        for (NPC npc : NPCManager.getAllNPCs()) {
                            try {
                                lobbyPlayer.sendMessage(
                                        " - &a" + npc.getName() + " &7[" + npc.getWorld().getName() + "] - [" + npc
                                                .getNPCMode().toString() + "&7]");
                            } catch (Exception ignored) {

                            }
                        }
                        return true;
                    }
                    case "edit": {
                        if (builder.isEditing()) {
                            lobbyPlayer.sendMessage(
                                    Lang.ALREADY_EDITING_NPC.toString() + command + " done");
                            return true;
                        }
                        if (builder.isSelecting()) {
                            lobbyPlayer.sendMessage(
                                    Lang.ALREADY_EXECUTED_THIS_COMMAND.toString());
                            return true;
                        }
                        if (args.length == 1) {
                            lobbyPlayer.sendMessage(Lang.SELECT_NPC_START_EDITING.toString());
                            builder.setSelectingNpc(true);
                            return true;
                        }
                        String name = args[1];
                        if (name.toLowerCase().equals("deliveryman")) {
                            lobbyPlayer.sendMessage(Lang.THIS_NPC_CANNOT_EDITED.toString());
                            return true;
                        }
                        NPC npc = NPCManager.getNPC(name);
                        if (npc != null) {

                            npc.setEditing(true);
                            lobbyPlayer.sendMessage(Lang.YOU_ARE_EDITING_NPC.toString() + name);
                            builder.setEditing(npc.getName());
                            builder.setCommands(builder.getNpc().getCommand());
                            builder.setRewardCommands(builder.getNpc().getRewardCommands());
                            builder.setHologram(builder.getNpc().getText());

                        } else {
                            lobbyPlayer.sendMessage(Lang.NPC_MENTIONED_NOT_EXIST.toString());
                        }
                        return true;
                    }
                    case "status": {
                        if (!builder.isEditing()) {
                            lobbyPlayer.sendMessage(Lang.YOU_NOT_EDITING_ANY_NPC.toString());
                            return true;
                        }
                        if (args.length == 1) {
                            lobbyPlayer.sendMessage(Lang.YOU_WRITE_STATE_PUT_NPC.toString());
                            lobbyPlayer.sendMessage("  &7/mcnpc status normal/sit/corpse");
                            return true;
                        }
                        String nameStatus = args[1];
                        NPCPosition position = NPCPosition.from(nameStatus);
                        if (position == null) {
                            lobbyPlayer.sendMessage(Lang.THERE_SUCH_STATE_OF_NPC.toString());
                            lobbyPlayer.sendMessage("&a - Normal");
                            lobbyPlayer.sendMessage("&a - Sit");
                            lobbyPlayer.sendMessage("&a - Corpse");
                            return true;
                        }
                        if (position == builder.getNpc().getPositionMemory()) {
                            lobbyPlayer.sendMessage(Lang.NPC_IS_READY_IN_THIS_MODE.toString());
                            return true;
                        }

                        builder.getNpc().setPositionStatus(position);

                        lobbyPlayer.sendMessage(Lang.ESTABLISHED_POSITION_OF_NPC.toString() + position.name().toLowerCase());
                        return true;
                    }
                    case "skin": {
                        if (!builder.isEditing()) {
                            lobbyPlayer.sendMessage(Lang.YOU_NOT_EDITING_ANY_NPC.toString());
                            return true;
                        }
                        if (args.length == 1) {
                            lobbyPlayer.sendMessage(Lang.ENTER_PLAYERS_USER_OR_UUID.toString());
                            lobbyPlayer.sendMessage("  &7/mcnpc skin Username/UUID");
                            return true;
                        }
                        String name = args[1];
                        builder.getNpc().setReady(false);
                        lobbyPlayer.sendMessage(Lang.ESTABLISHING_SKIN_NPC.toString());
                        if (GoldenLobby.getInstance().isSkinExternal()) {
                            SkinFetcher.fetchSkinFromIdAsync(name, new SkinFetcher.Callback() {
                                @Override
                                public void call(Skin skinData) {
                                    builder.getNpc().setSkin(skinData);
                                    builder.getNpc().destroyForUpdate();
                                    builder.getNpc().setReady(true);
                                    lobbyPlayer.sendMessage(Lang.YOU_ESTABLISHED_SKIN_NPC.toString());
                                }

                                @Override
                                public void failed() {
                                    lobbyPlayer.sendMessage(Lang.ERROR_OCURRED_WHILE_FETCHING_SKING.toString(), "" +
                                                    "&ePossible problems:",
                                            "&7- There is no Name, UUID in Mojang",
                                            "&7- Could not establish connection with external api");
                                    builder.getNpc().setReady(true);
                                }
                            });
                        } else {
                            SkinGameProfile.loadGameProfile(name, new SkinGameProfile.Callback() {
                                @Override
                                public void call(Skin skinData) {
                                    builder.getNpc().setSkin(skinData);
                                    builder.getNpc().destroyForUpdate();
                                    builder.getNpc().setReady(true);
                                    lobbyPlayer.sendMessage(Lang.YOU_ESTABLISHED_SKIN_NPC.toString());
                                }

                                @Override
                                public void failed() {
                                    lobbyPlayer.sendMessage(Lang.ERROR_OCURRED_WHILE_FETCHING_SKING.toString(), "" +
                                                    "&ePossible problems:",
                                            "&7- There is no Name, UUID in Mojang",
                                            "&7- Could not establish connection with external api");
                                    builder.getNpc().setReady(true);
                                }
                            });
                        }
                        return true;
                    }
                    case "addline": {
                        if (!builder.isEditing()) {
                            lobbyPlayer.sendMessage(Lang.YOU_NOT_EDITING_ANY_NPC.toString());
                            return true;
                        }
                        if (args.length == 1) {
                            lobbyPlayer.sendMessage(Lang.YOU_MUST_WRITE_TEXT_ARGUMENTS.toString());
                            lobbyPlayer.sendMessage("  &7/mcnpc addline (texto)");
                            return true;
                        }
                        String linea = Utils.concatArgs(args, 1);
                        builder.addLine(linea);
                        lobbyPlayer.sendMessage(Lang.YOU_ADDED_NEW_LINE.toString() + linea);
                        builder.getNpc().setText(builder.getHologram());
                        return true;
                    }

                    case "removeline": {
                        if (!builder.isEditing()) {
                            lobbyPlayer.sendMessage(Lang.YOU_NOT_EDITING_ANY_NPC.toString());
                            return true;
                        }
                        if (args.length == 1) {
                            lobbyPlayer.sendMessage(Lang.YOU_MUST_WRITE_LINEA_TO_REMOVE.toString());
                            lobbyPlayer.sendMessage("  &7/mcnpc removeline linea");
                            return true;
                        }
                        String linea = args[1];
                        if (builder.removeLine(linea)) {
                            lobbyPlayer.sendMessage(Lang.YOU_REMOVE_LINE.toString() + linea);
                            builder.getNpc().setText(builder.getHologram());
                        } else {
                            lobbyPlayer.sendMessage(Lang.LINEA_COULD_NOT_REMOVE.toString() + linea);
                        }
                        return true;
                    }
                    case "move": {
                        if (!builder.isEditing()) {
                            lobbyPlayer.sendMessage(Lang.YOU_NOT_EDITING_ANY_NPC.toString());
                            return true;
                        }
                        builder.getNpc().setReady(false);
                        lobbyPlayer.sendMessage(
                                Lang.YOU_HAVE_REMOVED_NPC.toString() + builder.getNpc().getName() + Lang.YOU_LOCATION.toString());
                        builder.getNpc().destroyForUpdate();
                        builder.getNpc().setLocation(player.getLocation());
                        builder.getNpc().create();
                        builder.getNpc().setReady(true);
                        return true;
                    }
                    case "delete": {
                        if (!builder.isEditing()) {
                            lobbyPlayer.sendMessage(Lang.YOU_NOT_EDITING_ANY_NPC.toString());
                            return true;
                        }
                        NPC npc = builder.getNpc();
                        npc.setReady(false);
                        String dir = npc.getNPCMode().getDirectory();
                        plugin.deleteConfigNPC(builder.getNpc().getName().toLowerCase(), dir,
                                new CallBack.SingleCallBack() {
                                    @Override
                                    public void onSuccess() {
                                        builder.getNpc().destroy();
                                        builder.setEditing(null);
                                        lobbyPlayer.sendMessage(Lang.NPC_HAS_BEEN_REMOVED.toString());
                                        lobbyPlayer.sendMessage(Lang.YOU_LEFT_NPC_EDITING_MODE.toString());
                                    }

                                    @Override
                                    public void onError() {
                                        builder.getNpc().destroy();
                                        builder.setEditing(null);
                                        lobbyPlayer.sendMessage(Lang.NPC_COULD_NOT_REMOVED.toString());
                                        lobbyPlayer.sendMessage(Lang.YOU_LEFT_NPC_EDITING_MODE.toString());
                                    }
                                });

                        return true;
                    }
                    case "mode": {
                        if (!builder.isEditing()) {
                            lobbyPlayer.sendMessage(Lang.YOU_NOT_EDITING_ANY_NPC.toString());
                            return true;
                        }
                        if (args.length == 1) {
                            lobbyPlayer.sendMessage(Lang.YOU_SPECIFY_MODE_NPC.toString());
                            lobbyPlayer.sendMessage("  &7- COMMAND (To execute commands.)");
                            lobbyPlayer.sendMessage("  &7- STAFF (Search and Find Staff)");
                            return true;
                        }
                        NPCMode modo = NPCMode.from(args[1]);
                        if (modo == null) {
                            lobbyPlayer.sendMessage(Lang.YOU_SPECIFY_VALID_NPC.toString());
                            lobbyPlayer.sendMessage("  &7- COMMAND (To execute commands.)");
                            lobbyPlayer.sendMessage("  &7- STAFF (Search and Find Staff)");
                            return true;
                        }
                        builder.getNpc().setMode(modo);
                        lobbyPlayer.sendMessage(
                                Lang.YOU_HAVE_SPECIFY_CHANGED_MODE.toString() + builder.getNpc().getNPCMode().name());
                        return true;
                    }
                    case "addcommand": {
                        if (!builder.isEditing()) {
                            lobbyPlayer.sendMessage(Lang.YOU_NOT_EDITING_ANY_NPC.toString());
                            return true;
                        }
                        if (args.length == 1) {
                            lobbyPlayer.sendMessage(" &c[*] You must type the command in the argument");
                            lobbyPlayer.sendMessage(" &6 - server:servidorBungee &7(Send to a server)");
                            lobbyPlayer.sendMessage(" &6 - msg:Texto a mostrar &7(Displays a message to the player)");
                            lobbyPlayer.sendMessage(
                                    " &6 - consola:eco give {player} 100 &7(Execute a command from the console)");
                            lobbyPlayer.sendMessage(
                                    " &6 - player:open inventory &7(Execute the command from the player)");
                            lobbyPlayer.sendMessage("  &7/mcnpc addcommand [tipo]:[extra]");
                            return true;
                        }
                        String linea = Utils.concatArgs(args, 1);
                        builder.addCommand(linea);
                        lobbyPlayer.sendMessage(Lang.YOU_HAVE_ADDED_COMMAND_NPC.toString() + linea);
                        builder.getNpc().setCommand(builder.getCommands());
                        return true;
                    }

                    case "addrewardcommand": {
                        if (!builder.isEditing()) {
                            lobbyPlayer.sendMessage(Lang.YOU_NOT_EDITING_ANY_NPC.toString());
                            return true;
                        }

                        if (!builder.getNpc().getNPCMode().equals(NPCMode.STAFF)) {
                            lobbyPlayer.sendMessage(Lang.YOU_CANNOT_USE_OTHER_STAFF.toString());
                            return true;
                        }

                        if (args.length == 1) {
                            lobbyPlayer.sendMessage(" &c[*] You must type the command in the argument");
                            lobbyPlayer.sendMessage(" &6 - server:servidorBungee &7(Send to a server)");
                            lobbyPlayer.sendMessage(" &6 - msg:Texto a mostrar &7(Displays a message to the player)");
                            lobbyPlayer.sendMessage(
                                    " &6 - consola:eco give {player} 100 &7(Execute a command from the console)");
                            lobbyPlayer.sendMessage(
                                    " &6 - player:open inventory &7(Execute the command from the player)");
                            lobbyPlayer.sendMessage("  &7/mcnpc addcommand [type]:[extra]");
                            return true;
                        }

                        String linea = Utils.concatArgs(args, 1);
                        builder.addRewardCommands(linea);
                        lobbyPlayer.sendMessage(Lang.YOU_HAVE_REWARD_COMMAND.toString() + linea);
                        builder.getNpc().setRewardCommands(builder.getRewardCommands());
                        return true;
                    }
                    case "removecommand": {
                        if (!builder.isEditing()) {
                            lobbyPlayer.sendMessage(Lang.YOU_NOT_EDITING_ANY_NPC.toString());
                            return true;
                        }
                        if (args.length == 1) {
                            lobbyPlayer
                                    .sendMessage(Lang.YOU_MUST_WANT_REMOVE.toString());
                            lobbyPlayer.sendMessage("  &7/mcnpc removecommand line");
                            return true;
                        }
                        String linea = args[1];
                        if (builder.removeCommand(linea)) {
                            lobbyPlayer.sendMessage("&aYou have deleted the command in the: " + linea);
                            builder.getNpc().setCommand(builder.getCommands());
                        } else {
                            lobbyPlayer.sendMessage("&cCould not remove the command in the line: " + linea);
                        }
                        return true;
                    }
                    case "removerewardcommand": {
                        if (!builder.isEditing()) {
                            lobbyPlayer.sendMessage(Lang.YOU_NOT_EDITING_ANY_NPC.toString());
                            return true;
                        }
                        if (!builder.getNpc().getNPCMode().equals(NPCMode.STAFF)) {
                            lobbyPlayer.sendMessage(Lang.YOU_CANNOT_USE_OTHER_STAFF.toString());
                            return true;
                        }
                        if (args.length == 1) {
                            lobbyPlayer
                                    .sendMessage(" &c[*] You must type the command line you want to remove");
                            lobbyPlayer.sendMessage("  &7/mcnpc removecommand line");
                            return true;
                        }
                        String linea = args[1];
                        if (builder.removeRewardCommands(linea)) {
                            lobbyPlayer
                                    .sendMessage("&aYou have removed the reward command in the: " + linea);
                            builder.getNpc().setCommand(builder.getCommands());
                        } else {
                            lobbyPlayer.sendMessage(
                                    "&cCould not remove the reward command in the line: " + linea);
                        }
                        return true;
                    }
                    case "done": {
                        if (!builder.isEditing()) {
                            lobbyPlayer.sendMessage(Lang.YOU_NOT_EDITING_ANY_NPC.toString());
                            return true;
                        }
                        if (!builder.getNpc().isReady()) {
                            lobbyPlayer.sendMessage(
                                    "&cYou cannot save an npc while you are performing an operation");
                            return true;
                        }
                        lobbyPlayer.sendMessage(
                                "&e+ Saving configuration " + builder.getNpc().getName() + " wait...");
                        plugin.createConfigNPC(builder.getNpc(), player.getName(), new CallBack.SingleCallBack() {
                            @Override
                            public void onSuccess() {
                                builder.getNpc().setEditing(false);
                                lobbyPlayer.sendMessage(
                                        "&e+ NPC data has been saved: &6" + builder.getNpc().getName());
                                lobbyPlayer.sendMessage("&aYou have left the NPC editing mode");
                                builder.setEditing(null);
                            }

                            @Override
                            public void onError() {
                                lobbyPlayer.sendMessage(
                                        "&cAn error occurred while saving the configuration");
                                lobbyPlayer.sendMessage("&aYou have left the NPC editing mode");
                                builder.setEditing(null);
                            }
                        });
                        return true;
                    }
                    case "sync": {
                        if (builder.isEditing()) {
                            lobbyPlayer.sendMessage("&cYou cannot reload NPCs if you are in NPC Edit mode");
                            return true;
                        }
                        if (args.length == 1) {
                            lobbyPlayer.sendMessage("&a¡Synchronizing servers!");
                            plugin.reloadNPC(new CallBack.SingleCallBack() {
                                @Override
                                public void onSuccess() {
                                    plugin.sendSync(SubChannel.SubOperation.GET_FROM_DB);
                                    sender.sendMessage(Utils.chatColor("&aSynchronization completed!"));
                                }

                                @Override
                                public void onError() {
                                    sender.sendMessage(Utils.chatColor(
                                            "&cAn error occurred while reloading the configuration and NPC's"));
                                }
                            }, SubChannel.SubOperation.GET_FROM_DB);

                            return true;
                        }
                        switch (args[1]) {
                            case "purge": {
                                purgeSync(sender);
                                return true;
                            }
                            case "rebase": {
                                rebaseSync(sender);
                                return true;
                            }
                        }
                        return true;
                    }
                    case "lookatplayer": {
                        if (!builder.isEditing()) {
                            lobbyPlayer.sendMessage("&cYou need to edit an npc to execute this command");
                            return true;
                        }
                        NPC npc = builder.getNpc();
                        if (args.length > 1) {
                            String argumento = args[1];
                            if (argumento.toLowerCase().equals("true")) {
                                lobbyPlayer.sendMessage("&aYou have enabled look at the player, npc: " + npc.getName());
                                npc.setLookAtPlayer(true);
                            } else {
                                lobbyPlayer
                                        .sendMessage("&cYou have disabled looking at the player, npc: " + npc.getName());
                                npc.setLookAtPlayer(false);
                            }
                            return true;
                        }
                        lobbyPlayer.sendMessage("Arguments are missing: /mcnpc lookatplayer [true|false]");
                        return true;
                    }
                    case "reset": {
                        if (args.length > 1) {
                            String nombre = args[1];
                            Player playerArg = Bukkit.getPlayer(nombre);
                            if (playerArg == null) {
                                lobbyPlayer.sendMessage(
                                        "&cYou cannot reset statistics from an offline user's game");
                                return true;
                            }
                            LobbyPlayer player1 = LobbyPlayerMap.getJugador(playerArg);
                            if (player1 != null) {
                                plugin.getHistoryDB().resetPlayer(player1,
                                        new CallBack.SingleCallBack() {
                                            @Override
                                            public void onSuccess() {
                                                lobbyPlayer.sendMessage(
                                                        "&aYou have reset the history statistics for: " + nombre);
                                            }

                                            @Override
                                            public void onError() {
                                                lobbyPlayer.sendMessage(
                                                        "&cAn error has occurred when trying to remove history statistics for: " + nombre + " [DB]");
                                            }
                                        });
                                return true;
                            }
                            lobbyPlayer.sendMessage(
                                    "&cAn error has occurred when trying to remove history statistics for: " + nombre);
                            return true;
                        }
                        lobbyPlayer.sendMessage(
                                "&cYou must specify the name of the player for whom you will restart the history statistics");
                        return true;
                    }

                    default: {
                        if (builder.isEditing()) {
                            sendHelpEditing(lobbyPlayer);
                            return true;
                        }
                        lobbyPlayer.sendMessage("&cYou have not specified an option",
                                " &e- create (ModeNPC) (nameId)",
                                " &e- edit [nameId]", "");
                        return true;
                    }
                }
            }
            lobbyPlayer.sendMessage(Lang.NO_PERMISSION.toString());
        }

        return true;
    }

    public void rebaseSync(CommandSender sender) {
        sender.sendMessage(Utils.chatColor("&6Uploading the npcs of this server to the database"));
        plugin.getNpcdb().rebaseConfiguration(sender.getName(), new CallBack.SingleCallBack() {
            @Override
            public void onSuccess() {
                sender.sendMessage(Utils.chatColor("&aThe npcs have been resubmitted"));
                plugin.sendSync(SubChannel.SubOperation.PURGE_AND_GET_FROM_DB);
            }

            @Override
            public void onError() {
                sender.sendMessage(Utils.chatColor("&cError when uploading the npcs"));
            }
        });
    }

    public void purgeSync(CommandSender sender) {
        sender.sendMessage(Utils.chatColor("&aRemoving npcs not registered in the database"));
        plugin.purgefiles();
        sender.sendMessage(Utils.chatColor("&aSynchronizing with other servers"));
        plugin.sendSync(SubChannel.SubOperation.PURGE_AND_GET_FROM_DB);
        sender.sendMessage(Utils.chatColor("&aSynchronization executed"));
    }

    public boolean isReadyNPCs() {
        boolean ready = true;
        for (NPC npc : NPCManager.getAllNPCs()) {
            if (!npc.isReady()) {
                ready = false;
                break;
            }
        }
        return ready;
    }

    public void sendHelpEditing(LobbyPlayer lobbyPlayer) {
        lobbyPlayer.sendMessage("",
                Lang.COMMAND_ENABLE.toString()
        );
        lobbyPlayer.sendMessageWithSuggest(
                new MessageSuggest(
                        "  &7 - /mcnpc addline (text)",
                        "/mcnpc addline ",
                        "&eAdd a line of text about the NPC"),
                new MessageSuggest(
                        "  &7 - /mcnpc removeline (line)",
                        "/mcnpc removeline ",
                        " &eRemove a line of text from the NPC"),
                new MessageSuggest(
                        "  &7 - /mcnpc skin (Username/UUID)",
                        "/mcnpc skin ",
                        "&eEstablish a skin to the NPC"),
                new MessageSuggest(
                        "  &7 - /mcnpc addcommand (Username/UUID)",
                        "/mcnpc addcommand ",
                        "&eAdding a command to the NPC"),
                new MessageSuggest(
                        "  &7 - /mcnpc removecommand (line)",
                        "/mcnpc removecommand ",
                        "&eRemove a command to the specific NPC in line"),
                new MessageSuggest(
                        "  &7 - /mcnpc addrewardcommand (command)",
                        "/mcnpc addrewardcommand ",
                        "&eAdd a reward command to the npc"),
                new MessageSuggest(
                        "  &7 - /mcnpc removerewardcommand (line)",
                        "/mcnpc removerewardcommand ",
                        "&eRemove a reward command to the npc"),
                new MessageSuggest(
                        "  &7 - /mcnpc commands",
                        "/mcnpc commands ",
                        "&eDisplays the list of added npc commands"),
                new MessageSuggest(
                        "  &7 - /mcnpc rewardcommands",
                        "/mcnpc rewardcommands ",
                        "&eDisplays the list of added npc reward commands"),

                new MessageSuggest(
                        "  &7 - /mcnpc move",
                        "/mcnpc move",
                        "&eYou move the NPC to your current position"),
                new MessageSuggest(
                        "  &7 - /mcnpc done",
                        "/mcnpc done",
                        "&eSave the npc properties and close the edit mode")

        );
        lobbyPlayer.sendMessage("");
    }
}
