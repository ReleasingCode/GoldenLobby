package com.releasingcode.goldenlobby.languages;

import com.releasingcode.goldenlobby.GoldenLobby;
import org.bukkit.ChatColor;

public enum Lang {
    //NPC
    TALKING_TO("talking-to", "Talking to "),
    NPC_HAS_BEEN_RELOADED("npc-reloaded", "The NPC has been recharged:"),
    NO_EXIST_NPC_WHILE_RELOAD("no-exist-npc-while-reload", "An error occurred while reloading the configuration and NPC's"),
    YOU_CANNOT_SYNC_NCP_READY("you-cannot-sync-npcready", "You can't synchronize while there's no NPC ready"),
    RELOADING_CONFIG_NPC("reloading-config-ncp", "Reloading configuration and NPC's, please wait..."),
    ERROR_OCURRED_WHILE_RELOADING_CONFIG("error-ocurred-while-reloding-config", "An error occurred while reloading the configuration and NPC's"),
    CANNOT_RELOAD_WHILE_NPC_READY("you-cannot-reload-while-npc-ready", "You cannot reload while there is no NPC ready, This occurs when a person is editing an NPC"),
    TYPE_A_VALID_NCP("type-a-vaild-ncp", "Write a valid NPC type"),
    WRITE_NAME_NCP_IDENTIFIER("write-name-npc-identifier", "You must write the name of the NPC's identifier"),
    ENTER_IDENTIFIER_NAME_ANOTHERNPC("enter-identifier-name-anotherncp", "Enter an identifier name that another NPC does not have"),
    YOU_HAVE_CREATED_NPC("you-have-created-ncp", "&a [*] &aYou have created the NPC &f "),
    EDITING_MODE_HAS_BEEN_ACTIVED("", "&e - The editing mode has been activated for this npc "),
    YOU_CANNOT_EXECUTE_COMMAND_WHILE_EDIT("you-cannot-executed-command", "&cYou cannot execute this command while in edit mode"),
    WRITE_NAMENCP_TO_TELEPORT("write-namencp-to-teleport", "&aWrite a name of the NPC you want to teleport to"),
    TP_TO_NCP("tp-to-ncp", "&aTeleporting to the npc: "),
    NO_NCP_FOUND_WHIT_NAME("ncp-no-found", "&cNo NPC found with that name "),
    YOU_MUST_EDIT_MODE("you-most-edit-mode", "&cYou must be in edit mode to execute this command "),
    LIST_OF_COMMANDS_FROM("list-of-commands-from", "List of Commands from "),
    NPC_BOUNTY_COMMANDS("npc-bounty-commands", "&c No bounty commands yet this npc"),
    REAWARD_COMMAND_LIST("reward-command-list", "&c Reward Command List "),
    LIST_OF_NPC("list-of-npc", "Lista de NPC's"),
    ALREADY_EDITING_NPC("already-editing-npc", "&cYou are already editing an NPC, to exit use /"),
    ALREADY_EXECUTED_THIS_COMMAND("", "&c You have already executed this command and have not yet selected an NPC, please select it to continue editing"),
    SELECT_NPC_START_EDITING("select-npc-start-editing", "&c Select an NPC to start editing"),
    THIS_NPC_CANNOT_EDITED("this-npc-cannot-edited", "&c This npc cannot be edited"),
    //            ("","You do not have permission"),
    //            ("","You do not have permission"),
    //            ("","You do not have permission"),
    //            ("","You do not have permission"),
    //            ("","You do not have permission"),
    //            ("","You do not have permission"),
    //            ("","You do not have permission"),
    //            ("","You do not have permission"),
    //            ("","You do not have permission"),
    //            ("","You do not have permission"),
    //            ("","You do not have permission"),
    //            ("","You do not have permission"),
    //            ("","You do not have permission"),


    //scoreboard
    THIS_NCP_NO_COMMANDS("this-ncp-no-commands", "&cThis npc has no commands yet"),
    COMMAND_ARE_MISSING("command-are-missing", "&aArguments for this command are missing:"),
    RELOADING_SCOREBOARD("realoading-scoreboard", "&aReloading scoreboard"),
    UPDATING_SCOREBOARD("updating-scoreboard", "&a Updating the Scoreboard in the database"),
    UPDATE_COMPLETED("update-completed", "&aUpdate and synchronization completed"),
    DATA_HAS_BEEN_UPDATED("data-has-been-updated", "&aData has been updated but not synchronized"),
    ERROR_WHILE_UPDATING_SCOREBOARD("error-uptating-scoredboard", "&aError while updating the scoreboard in the database"),
    SYNC_SERVERS_DB("sync-servers-db", "&a Synchronizing the servers with the database"),
    DATA_HASBEEN_UPDATED_BUT_NO_SYNC_REDIS("data-hasbeen-updated-but-no-sync-redis", "Error while synchronizing, data has been updated but not synchronized with redis "),
    ERROR_WHILE_REQUESTING("error-while-requesting", "&cError while requesting scoreboard from database "),

    //setspawn
    YOU_HAVE_STABLISHED_POINT("you-have-stablished-point", "&aYou have established the point of appearance "),
    ERROR_EXCUTING_THIS_COMMAND("error-executing this command", "An error occurred while executing this command. ERRx05  "),
    THERE_IS_ALREADY_AWARP_NAME("already-warp-witch-name", "&cThere is already a warp with this name "),
    ALREADY_WARP_WITCH_NAME("already-warp-witchname", "&cThere is already a warp with this name "),
    YOU_HAVE_CREATED_WARP("you-have-created-warp", "&aYou have created the warp: &e"),
    YOU_SPECIFY_WARP_NAME("you-specify-warp-name", "&cYou must specify a warp name "),
    THERE_NO_WARP_THISNAME("there-warp-this-name", "&cThere is no warp with this name "),
    WARP_HAS_BEEN_ERASED("warp-erased", "&cThe warp has been erased: &e"),
    WARP_RELOADED("warp-reloaded", "&aWarps have been reloaded"),
    NO_WARPS_AVAILABLE("no-warps-available", "&cNo warps available"),
    CLICK_TP_WARP("click-tp-warp", "&aClick to teleport to warp &e"),
    SPECIFY_WARP_NAME("specify-warp-name", "&cYou must specify a warp name "),

    //welcome message
    WELCOME_MESSAGE_SETTING("welcome-message_setting", "&aWelcome message settings restarted"),


    NO_LOAD_Statistics("no_load_statistics", "Unable to upload your statistics, please let an administrator know"),
    MODE_BUILDING_ACTIVE("mode-building-active", "You are in edit mode, end edit mode to click an NPC"),
    NPC_NOT_READY_CLICKED("npc-not-ready", "This npc is not ready to be clicked"),
    NPC_NOT_READYSTAFF("npc-not-readystaff", "This staff npc is not ready yet, it needs at least one reward command"),
    ERROR_INTERACTION_REGISTER("error-interaction-register", "An error occurred while recording your interaction, contact an administrator"),

    //nopermiso
    NO_PERMISSION("no-permission", "You do not have permission"),
    //GamemodeCommand
    YOU_CANNOT_RESTART_STATISTICS("write-gamemode", "You cannot reset statistics from an offline user's game"),
    YOU_HAS_RESET_HISTORY("you-has-reset-history", "You have reset the history statistics for: "),

    AN_ERROR_REMOVE_HISTORY("an-error-remove-history", "An error has occurred when trying to remove history statistics for"),
    SPECIFY_NAME_RESTART_HISTORY_STATISTICS("specify-name-restart-history-stats", "You must specify the name of the player for whom you will restart the history statistics"),
    SYNC_NPC("synchronizing-ncp", "Synchronizing the npcs from the console"),


    SUCCESSFULLY_RELOADED("successfully-reloaded", "Successfully reloaded"),


    WRITTE_GAMEMODE("write-gamemode", "Please write the game mode"),
    COMMAND_ENABLE("command-enable-are ", "The available commands are"),
    GAMEMODE_CHANGED("gamemode-changed", "Your game mode has been changed"),
    GAMEMODE_CHANGED_ADVENTURE("gamemode-changed-adventure", "Your game mode has changed to adventure"),
    GAMEMODE_CHANGED_CREATIVE("gamemode-changed-creative", "Your game mode has changed to creative"),
    GAMEMODE_CHANGED_SPECTACTOR("gamemode-changed-spectactor", "Your game mode has changed to spectator"),
    GAMEMODE_CHANGED_NOTEXIST("gamemode-changed-notexist", "The game mode you are writing does not exist");


    private final String path;
    private final String def;

    Lang(String path, String start) {
        this.path = path;
        this.def = start;
    }

    public String toString() {
        LanguageFile languageFile = GoldenLobby.getInstance().getLang();
        if (languageFile != null) {
            return ChatColor.translateAlternateColorCodes('&', languageFile.getConfig().getConfig().getString(this.path, this.def));
        }
        return ChatColor.translateAlternateColorCodes('&', this.def);

    }

    public String getPath() {
        return path;
    }

    public String getDef() {
        return def;
    }
}
