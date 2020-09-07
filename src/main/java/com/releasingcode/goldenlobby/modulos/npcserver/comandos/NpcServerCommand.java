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
                            lobbyPlayer.sendMessage("&aEstás editando actualmente",
                                    Lang.COMMAND_ENABLE.toString(),
                                    "  &7 - /mcnpc addline (texto) &eAdd a line of text about the NPC",
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
                            lobbyPlayer.sendMessage("&aEstás editando el NPC: &e" + name);
                            builder.setEditing(npc.getName());
                            builder.setCommands(builder.getNpc().getCommand());
                            builder.setRewardCommands(builder.getNpc().getRewardCommands());
                            builder.setHologram(builder.getNpc().getText());
                        } else {
                            lobbyPlayer.sendMessage("&cNo existe el NPC que mencionaste");
                        }
                        return true;
                    }
                    case "status": {
                        if (!builder.isEditing()) {
                            lobbyPlayer.sendMessage("&cNo estás editando ningun NPC");
                            return true;
                        }
                        if (args.length == 1) {
                            lobbyPlayer.sendMessage(" &c[*] Debes escribir el estado al que quieres poner al npc");
                            lobbyPlayer.sendMessage("  &7/mcnpc status normal/sit/corpse");
                            return true;
                        }
                        String nameStatus = args[1];
                        NPCPosition position = NPCPosition.from(nameStatus);
                        if (position == null) {
                            lobbyPlayer.sendMessage("&cNo existe este estado de npc");
                            lobbyPlayer.sendMessage("&a - Normal");
                            lobbyPlayer.sendMessage("&a - Sit");
                            lobbyPlayer.sendMessage("&a - Corpse");
                            return true;
                        }
                        if (position == builder.getNpc().getPositionMemory()) {
                            lobbyPlayer.sendMessage("&aEl npc ya se encuentra en este modo");
                            return true;
                        }

                        builder.getNpc().setPositionStatus(position);

                        lobbyPlayer.sendMessage("&aHas establecido la posición del npc a:&e " + position.name().toLowerCase());
                        return true;
                    }
                    case "skin": {
                        if (!builder.isEditing()) {
                            lobbyPlayer.sendMessage("&cNo estás editando ningun NPC");
                            return true;
                        }
                        if (args.length == 1) {
                            lobbyPlayer.sendMessage(" &c[*] Debes escribir el Usuario o UUID de un Jugador");
                            lobbyPlayer.sendMessage("  &7/mcnpc skin Username/UUID");
                            return true;
                        }
                        String name = args[1];
                        builder.getNpc().setReady(false);
                        lobbyPlayer.sendMessage("&6Estableciendo skin al NPC, por favor espere...");
                        if (GoldenLobby.getInstance().isSkinExternal()) {
                            SkinFetcher.fetchSkinFromIdAsync(name, new SkinFetcher.Callback() {
                                @Override
                                public void call(Skin skinData) {
                                    builder.getNpc().setSkin(skinData);
                                    builder.getNpc().destroyForUpdate();
                                    builder.getNpc().setReady(true);
                                    lobbyPlayer.sendMessage("&aHas establecido la Skin al NPC");
                                }

                                @Override
                                public void failed() {
                                    lobbyPlayer.sendMessage("&cHa ocurrido un error al hacer fetch del Skin", "" +
                                                    "&ePosibles problemas:",
                                            " &7- No existe el Nombre, UUID en Mojang",
                                            " &7- No se pudo establecer conexión con la api externa");
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
                                    lobbyPlayer.sendMessage("&aHas establecido la Skin al NPC");
                                }

                                @Override
                                public void failed() {
                                    lobbyPlayer.sendMessage("&cHa ocurrido un error al hacer fetch del Skin", "" +
                                                    "&ePosibles problemas:",
                                            " &7- No existe el Nombre, UUID en Mojang",
                                            " &7- No se pudo establecer conexión con Mojang (Limite excedido)");
                                    builder.getNpc().setReady(true);
                                }
                            });
                        }
                        return true;
                    }
                    case "addline": {
                        if (!builder.isEditing()) {
                            lobbyPlayer.sendMessage("&cNo estás editando ningun NPC");
                            return true;
                        }
                        if (args.length == 1) {
                            lobbyPlayer.sendMessage(" &c[*] Debes escribir texto en el argumento");
                            lobbyPlayer.sendMessage("  &7/mcnpc addline (texto)");
                            return true;
                        }
                        String linea = Utils.concatArgs(args, 1);
                        builder.addLine(linea);
                        lobbyPlayer.sendMessage("&aHas agregado una nueva linea al NPC: " + linea);
                        builder.getNpc().setText(builder.getHologram());
                        return true;
                    }
                    case "removeline": {
                        if (!builder.isEditing()) {
                            lobbyPlayer.sendMessage("&cNo estás editando ningun NPC");
                            return true;
                        }
                        if (args.length == 1) {
                            lobbyPlayer.sendMessage(" &c[*] Debes escribir la linea que deseas remover");
                            lobbyPlayer.sendMessage("  &7/mcnpc removeline linea");
                            return true;
                        }
                        String linea = args[1];
                        if (builder.removeLine(linea)) {
                            lobbyPlayer.sendMessage("&aHas eliminado la linea: " + linea);
                            builder.getNpc().setText(builder.getHologram());
                        } else {
                            lobbyPlayer.sendMessage("&cNo se ha podido eliminar la linea: " + linea);
                        }
                        return true;
                    }
                    case "move": {
                        if (!builder.isEditing()) {
                            lobbyPlayer.sendMessage("&cNo estás editando ningun NPC");
                            return true;
                        }
                        builder.getNpc().setReady(false);
                        lobbyPlayer.sendMessage(
                                "&aHas movido el NPC &e" + builder.getNpc().getName() + "&a a tu localización");
                        builder.getNpc().destroyForUpdate();
                        builder.getNpc().setLocation(player.getLocation());
                        builder.getNpc().create();
                        builder.getNpc().setReady(true);
                        return true;
                    }
                    case "delete": {
                        if (!builder.isEditing()) {
                            lobbyPlayer.sendMessage("&cNo estás editando ningun NPC");
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
                                        lobbyPlayer.sendMessage("&cSe ha removido el NPC!");
                                        lobbyPlayer.sendMessage("&aHas salido del modo edición del NPC");
                                    }

                                    @Override
                                    public void onError() {
                                        builder.getNpc().destroy();
                                        builder.setEditing(null);
                                        lobbyPlayer.sendMessage("&cNo se ha podido eliminar el NPC!");
                                        lobbyPlayer.sendMessage("&aHas salido del modo edición del NPC");
                                    }
                                });

                        return true;
                    }
                    case "mode": {
                        if (!builder.isEditing()) {
                            lobbyPlayer.sendMessage("&cNo estás editando ningun NPC");
                            return true;
                        }
                        if (args.length == 1) {
                            lobbyPlayer.sendMessage(" &c[*] Debes especificar el modo del NPC: ");
                            lobbyPlayer.sendMessage("  &7- COMMAND (Para ejecutar comandos)");
                            lobbyPlayer.sendMessage("  &7- STAFF (Busca y encuentra el Staff)");
                            return true;
                        }
                        NPCMode modo = NPCMode.from(args[1]);
                        if (modo == null) {
                            lobbyPlayer.sendMessage(" &c[*] Debes especificar el modo del NPC válido:");
                            lobbyPlayer.sendMessage("  &7- COMMAND (Para ejecutar comandos)");
                            lobbyPlayer.sendMessage("  &7- STAFF (Busca y encuentra el Staff)");
                            return true;
                        }
                        builder.getNpc().setMode(modo);
                        lobbyPlayer.sendMessage(
                                "&aHas cambiado el modo del NPC a: &e" + builder.getNpc().getNPCMode().name());
                        return true;
                    }
                    case "addcommand": {
                        if (!builder.isEditing()) {
                            lobbyPlayer.sendMessage("&cNo estás editando ningun NPC");
                            return true;
                        }
                        if (args.length == 1) {
                            lobbyPlayer.sendMessage(" &c[*] Debes escribir el comando en el argumento");
                            lobbyPlayer.sendMessage(" &6 - server:servidorBungee &7(Envia a un servidor)");
                            lobbyPlayer.sendMessage(" &6 - msg:Texto a mostrar &7(Muestra un mensaje al jugador)");
                            lobbyPlayer.sendMessage(
                                    " &6 - consola:eco give {player} 100 &7(Ejecuta un comando desde consola)");
                            lobbyPlayer.sendMessage(
                                    " &6 - player:open inventory &7(Ejecuta el comando desde el jugador)");
                            lobbyPlayer.sendMessage("  &7/mcnpc addcommand [tipo]:[extra]");
                            return true;
                        }
                        String linea = Utils.concatArgs(args, 1);
                        builder.addCommand(linea);
                        lobbyPlayer.sendMessage("&aHas agregado un comando al NPC: " + linea);
                        builder.getNpc().setCommand(builder.getCommands());
                        return true;
                    }
                    case "addrewardcommand": {
                        if (!builder.isEditing()) {
                            lobbyPlayer.sendMessage("&cNo estás editando ningun NPC");
                            return true;
                        }
                        if (!builder.getNpc().getNPCMode().equals(NPCMode.STAFF)) {
                            lobbyPlayer.sendMessage("&cNo puedes utilizar este comando en modo distinto a STAFF");
                            return true;
                        }
                        if (args.length == 1) {
                            lobbyPlayer.sendMessage(" &c[*] Debes escribir el comando en el argumento");
                            lobbyPlayer.sendMessage(" &6 - server:servidorBungee &7(Envia a un servidor)");
                            lobbyPlayer.sendMessage(" &6 - msg:Texto a mostrar &7(Muestra un mensaje al jugador)");
                            lobbyPlayer.sendMessage(
                                    " &6 - consola:eco give {player} 100 &7(Ejecuta un comando desde consola)");
                            lobbyPlayer.sendMessage(
                                    " &6 - player:open inventory &7(Ejecuta el comando desde el jugador)");
                            lobbyPlayer.sendMessage("  &7/mcnpc addcommand [tipo]:[extra]");
                            return true;
                        }
                        String linea = Utils.concatArgs(args, 1);
                        builder.addRewardCommands(linea);
                        lobbyPlayer.sendMessage("&aHas agregado un comando de recompensa al NPC: " + linea);
                        builder.getNpc().setRewardCommands(builder.getRewardCommands());
                        return true;
                    }
                    case "removecommand": {
                        if (!builder.isEditing()) {
                            lobbyPlayer.sendMessage("&cNo estás editando ningun NPC");
                            return true;
                        }
                        if (args.length == 1) {
                            lobbyPlayer
                                    .sendMessage(" &c[*] Debes escribir la linea del comando que deseas remover");
                            lobbyPlayer.sendMessage("  &7/mcnpc removecommand linea");
                            return true;
                        }
                        String linea = args[1];
                        if (builder.removeCommand(linea)) {
                            lobbyPlayer.sendMessage("&aHas eliminado el comando en la linea: " + linea);
                            builder.getNpc().setCommand(builder.getCommands());
                        } else {
                            lobbyPlayer.sendMessage("&cNo se ha podido eliminar el comando en la linea: " + linea);
                        }
                        return true;
                    }
                    case "removerewardcommand": {
                        if (!builder.isEditing()) {
                            lobbyPlayer.sendMessage("&cNo estás editando ningun NPC");
                            return true;
                        }
                        if (!builder.getNpc().getNPCMode().equals(NPCMode.STAFF)) {
                            lobbyPlayer.sendMessage("&cNo puedes utilizar este comando en modo distinto a STAFF");
                            return true;
                        }
                        if (args.length == 1) {
                            lobbyPlayer
                                    .sendMessage(" &c[*] Debes escribir la linea del comando que deseas remover");
                            lobbyPlayer.sendMessage("  &7/mcnpc removecommand linea");
                            return true;
                        }
                        String linea = args[1];
                        if (builder.removeRewardCommands(linea)) {
                            lobbyPlayer
                                    .sendMessage("&aHas eliminado el comando de recompensa en la linea: " + linea);
                            builder.getNpc().setCommand(builder.getCommands());
                        } else {
                            lobbyPlayer.sendMessage(
                                    "&cNo se ha podido eliminar el comando de recompensa en la linea: " + linea);
                        }
                        return true;
                    }
                    case "done": {
                        if (!builder.isEditing()) {
                            lobbyPlayer.sendMessage("&cNo estás editando ningun NPC");
                            return true;
                        }
                        if (!builder.getNpc().isReady()) {
                            lobbyPlayer.sendMessage(
                                    "&cNo puede guardar un npc mientras está realizando una operación");
                            return true;
                        }
                        lobbyPlayer.sendMessage(
                                "&e+ Guardando configuración " + builder.getNpc().getName() + " espere...");
                        plugin.createConfigNPC(builder.getNpc(), player.getName(), new CallBack.SingleCallBack() {
                            @Override
                            public void onSuccess() {
                                builder.getNpc().setEditing(false);
                                lobbyPlayer.sendMessage(
                                        "&e+ Se han guardado los datos del NPC: &6" + builder.getNpc().getName());
                                lobbyPlayer.sendMessage("&aHas salido del modo edición del NPC");
                                builder.setEditing(null);
                            }

                            @Override
                            public void onError() {
                                lobbyPlayer.sendMessage(
                                        "&cHa ocurrido un error mientras se guardaba la configuración");
                                lobbyPlayer.sendMessage("&aHas salido del modo edición del NPC");
                                builder.setEditing(null);
                            }
                        });
                        return true;
                    }
                    case "sync": {
                        if (builder.isEditing()) {
                            lobbyPlayer.sendMessage("&cNo puedes recargar los NPC si estás en modo Edición de NPC");
                            return true;
                        }
                        if (args.length == 1) {
                            lobbyPlayer.sendMessage("&a¡Sincronizando servidores!");
                            plugin.reloadNPC(new CallBack.SingleCallBack() {
                                @Override
                                public void onSuccess() {
                                    plugin.sendSync(SubChannel.SubOperation.GET_FROM_DB);
                                    sender.sendMessage(Utils.chatColor("&aSincronización completada!"));
                                }

                                @Override
                                public void onError() {
                                    sender.sendMessage(Utils.chatColor(
                                            "&cHa ocurrido un error mientras se recargaba la configuración y los NPC's"));
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
                            lobbyPlayer.sendMessage("&cNecesitas editar un npc para ejecutar este comando");
                            return true;
                        }
                        NPC npc = builder.getNpc();
                        if (args.length > 1) {
                            String argumento = args[1];
                            if (argumento.toLowerCase().equals("true")) {
                                lobbyPlayer.sendMessage("&aHas habilitado mirar al jugador, npc: " + npc.getName());
                                npc.setLookAtPlayer(true);
                            } else {
                                lobbyPlayer
                                        .sendMessage("&cHas desabilitado mirar al jugador, npc: " + npc.getName());
                                npc.setLookAtPlayer(false);
                            }
                            return true;
                        }
                        lobbyPlayer.sendMessage("Faltan argumentos: /mcnpc lookatplayer [true|false]");
                        return true;
                    }
                    case "reset": {
                        if (args.length > 1) {
                            String nombre = args[1];
                            Player playerArg = Bukkit.getPlayer(nombre);
                            if (playerArg == null) {
                                lobbyPlayer.sendMessage(
                                        "&cNo puedes reiniciar estadisticas desde el jugado de un usuario fuera de linea");
                                return true;
                            }
                            LobbyPlayer player1 = LobbyPlayerMap.getJugador(playerArg);
                            if (player1 != null) {
                                plugin.getHistoryDB().resetPlayer(player1,
                                        new CallBack.SingleCallBack() {
                                            @Override
                                            public void onSuccess() {
                                                lobbyPlayer.sendMessage(
                                                        "&aHas reseteado las estadisticas de historia para: " + nombre);
                                            }

                                            @Override
                                            public void onError() {
                                                lobbyPlayer.sendMessage(
                                                        "&cHa ocurrido un error al intentar eliminar estadisticas de historia para: " + nombre + " [DB]");
                                            }
                                        });
                                return true;
                            }
                            lobbyPlayer.sendMessage(
                                    "&cHa ocurrido un error al intentar eliminar estadisticas de historia para: " + nombre);
                            return true;
                        }
                        lobbyPlayer.sendMessage(
                                "&cDebes especificar el nombre del jugador al que reiniciarás la estadistica de historia");
                        return true;
                    }

                    default: {
                        if (builder.isEditing()) {
                            sendHelpEditing(lobbyPlayer);
                            return true;
                        }
                        lobbyPlayer.sendMessage("&cNo has especificado una opción",
                                " &e- create (ModeNPC) (nombreId)",
                                " &e- edit [nombreId]", "");
                        return true;
                    }
                }
            }
            lobbyPlayer.sendMessage(Lang.NO_PERMISSION.toString());
        }

        return true;
    }

    public void rebaseSync(CommandSender sender) {
        sender.sendMessage(Utils.chatColor("&6Resubiendo los npcs de este servidor a la base de datos"));
        plugin.getNpcdb().rebaseConfiguration(sender.getName(), new CallBack.SingleCallBack() {
            @Override
            public void onSuccess() {
                sender.sendMessage(Utils.chatColor("&aLos npcs han sido resubidos"));
                plugin.sendSync(SubChannel.SubOperation.PURGE_AND_GET_FROM_DB);
            }

            @Override
            public void onError() {
                sender.sendMessage(Utils.chatColor("&cError al resubir los npcs"));
            }
        });
    }

    public void purgeSync(CommandSender sender) {
        sender.sendMessage(Utils.chatColor("&aEliminando npcs no registrados en la base de datos"));
        plugin.purgefiles();
        sender.sendMessage(Utils.chatColor("&aSincronizando con los demas servidores"));
        plugin.sendSync(SubChannel.SubOperation.PURGE_AND_GET_FROM_DB);
        sender.sendMessage(Utils.chatColor("&aSincronización ejecutada"));
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
                        "  &7 - /mcnpc addline (texto)",
                        "/mcnpc addline ",
                        "&eAgrega una linea de texto sobre el NPC"),
                new MessageSuggest(
                        "  &7 - /mcnpc removeline (linea)",
                        "/mcnpc removeline ",
                        " &eElimina una linea de texto del NPC"),
                new MessageSuggest(
                        "  &7 - /mcnpc skin (Username/UUID)",
                        "/mcnpc skin ",
                        "&eEstablecer una skin al NPC"),
                new MessageSuggest(
                        "  &7 - /mcnpc addcommand (Username/UUID)",
                        "/mcnpc addcommand ",
                        "&eAgregar un comando al NPC"),
                new MessageSuggest(
                        "  &7 - /mcnpc removecommand (linea)",
                        "/mcnpc removecommand ",
                        "&eRemueve un comando al NPC en linea especifica"),
                new MessageSuggest(
                        "  &7 - /mcnpc addrewardcommand (comando)",
                        "/mcnpc addrewardcommand ",
                        "&eAgrega un comando de recompensa al npc"),
                new MessageSuggest(
                        "  &7 - /mcnpc removerewardcommand (linea)",
                        "/mcnpc removerewardcommand ",
                        "&eRemueve un comando de recompensa al npc"),
                new MessageSuggest(
                        "  &7 - /mcnpc commands",
                        "/mcnpc commands ",
                        "&eMuestra la lista de comandos del npc agregados"),
                new MessageSuggest(
                        "  &7 - /mcnpc rewardcommands",
                        "/mcnpc rewardcommands ",
                        "&eMuestra la lista de comandos de recompensa del npc agregados"),

                new MessageSuggest(
                        "  &7 - /mcnpc move",
                        "/mcnpc move",
                        "&eMueves al NPC a tu posición actual"),
                new MessageSuggest(
                        "  &7 - /mcnpc done",
                        "/mcnpc done",
                        "&eGuarda las propiedades del npc y cierra el modo edición")

        );
        lobbyPlayer.sendMessage("");
    }
}
