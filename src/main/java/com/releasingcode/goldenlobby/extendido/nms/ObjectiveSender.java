package com.releasingcode.goldenlobby.extendido.nms;


import com.releasingcode.goldenlobby.exception.IncompatibleMinecraftVersionException;
import es.minecub.core.apis.reflection.Reflection;
import org.apache.commons.lang.Validate;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

public class ObjectiveSender {
    private final static Map<UUID, Object> playersConnections = new ConcurrentHashMap<>();
    private final static Map<UUID, String> sentObjectives = new HashMap<>();

    // The action field of the scoreboard objective packet.
    private final static int PACKET_SCOREBOARD_OBJECTIVE_ACTION_CREATE = 0;
    private final static int PACKET_SCOREBOARD_OBJECTIVE_ACTION_DELETE = 1;
    private final static int PACKET_SCOREBOARD_OBJECTIVE_ACTION_UPDATE = 2;

    // The location field of the objective display packet.
    // For the curious ones: 0 = list ; 1 = sidebar ; 2 = below name.
    private final static int PACKET_DISPLAY_OBJECTIVE_SIDEBAR_LOCATION = 1;

    // The NMS classes & enum values needed to send the packets.
    private final static Class<?> packetPlayOutScoreboardObjectiveClass;
    private final static Class<?> packetPlayOutScoreboardDisplayObjectiveClass;
    private final static Class<?> packetPlayOutScoreboardScoreClass;

    private static Object enumScoreboardHealthDisplay_INTEGER = null;
    private static Object enumScoreboardAction_CHANGE = null;
    private static Object enumScoreboardAction_REMOVE = null;

    static {
        try {
            packetPlayOutScoreboardObjectiveClass = Reflection
                    .getMinecraftClassByName("PacketPlayOutScoreboardObjective");
            packetPlayOutScoreboardDisplayObjectiveClass = Reflection
                    .getMinecraftClassByName("PacketPlayOutScoreboardDisplayObjective");
            packetPlayOutScoreboardScoreClass = Reflection.getMinecraftClassByName("PacketPlayOutScoreboardScore");

            // IScoreboardCriteria.EnumScoreboardHealthDisplay.INTEGER value

            Class<?> enumScoreboardHealthDisplay;
            try {
                enumScoreboardHealthDisplay = Reflection
                        .getMinecraftClassByName("IScoreboardCriteria$EnumScoreboardHealthDisplay");
            } catch (ClassNotFoundException e) {
                enumScoreboardHealthDisplay = Reflection.getMinecraftClassByName("EnumScoreboardHealthDisplay");
            }

            for (Object enumConstant : enumScoreboardHealthDisplay.getEnumConstants()) {
                if (((Enum<?>) enumConstant).name().equals("INTEGER")) {
                    enumScoreboardHealthDisplay_INTEGER = enumConstant;
                    break;
                }
            }

            if (enumScoreboardHealthDisplay_INTEGER == null)
                throw new ClassNotFoundException(
                        "Unable to retrieve the INTEGER value of the IScoreboardCriteria$EnumScoreboardHealthDisplay enum");

            // PacketPlayOutScoreboardScore.EnumScoreboardAction values

            Class<?> enumScoreboardAction;
            try {
                enumScoreboardAction = Reflection
                        .getMinecraftClassByName("PacketPlayOutScoreboardScore$EnumScoreboardAction");
            } catch (ClassNotFoundException e) {
                enumScoreboardAction = Reflection.getMinecraftClassByName("EnumScoreboardAction");
            }

            for (Object enumConstant : enumScoreboardAction.getEnumConstants()) {
                switch (((Enum<?>) enumConstant).name()) {
                    case "CHANGE":
                        enumScoreboardAction_CHANGE = enumConstant;
                        break;

                    case "REMOVE":
                        enumScoreboardAction_REMOVE = enumConstant;
                        break;
                }
            }

            if (enumScoreboardAction_CHANGE == null)
                throw new ClassNotFoundException(
                        "Unable to retrieve the CHANGE value of the PacketPlayOutScoreboardScore$EnumScoreboardAction enum");
            if (enumScoreboardAction_REMOVE == null)
                throw new ClassNotFoundException(
                        "Unable to retrieve the REMOVE value of the PacketPlayOutScoreboardScore$EnumScoreboardAction enum");
        } catch (ClassNotFoundException e) {
            throw new IncompatibleMinecraftVersionException(
                    "Unable to get the required classes to send scoreboard packets", e);
        }
    }

    public static void send(SidebarObjective objective) {
        Validate.notNull(objective, "The objective cannot be null");

        for (UUID receiver : objective.getReceivers()) {
            try {
                send(receiver, objective);
            } catch (RuntimeException ignored) {
            } // Caught, so the packets are not sent for this player only.
        }
    }

    public static void updateDisplayName(SidebarObjective objective) {
        Validate.notNull(objective, "The objective cannot be null");

        for (UUID receiver : objective.getReceivers()) {
            try {
                String currentPlayerObjective = sentObjectives.get(receiver);
                if (objective.getName().equals(currentPlayerObjective)) {
                    updateObjectiveDisplayName(getPlayerConnection(receiver), objective);
                } else {
                    send(receiver, objective);
                }
            } catch (RuntimeException ignored) {
            } // Caught, so the packets are not sent for this player only.
        }
    }

    public static void updateLine(SidebarObjective objective, String oldLine, String newLine, int score) {
        for (UUID receiver : objective.getReceivers()) {
            try {
                String currentPlayerObjective = sentObjectives.get(receiver);

                if (objective.getName().equals(currentPlayerObjective)) {
                    Object connection = getPlayerConnection(receiver);

                    sendScore(connection, objective, newLine, score);
                    deleteScore(connection, objective, oldLine);
                } else {
                    send(receiver, objective);
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    public static void clear(UUID player) {
        String sentObjectiveName = sentObjectives.get(player);

        if (sentObjectiveName != null)
            destroyObjective(getPlayerConnection(player), sentObjectiveName);
    }

    public static void clearForAll() {
        for (Map.Entry<UUID, String> sentObjective : sentObjectives.entrySet()) {
            try {
                Object connection = getPlayerConnection(sentObjective.getKey());
                if (connection != null)
                    destroyObjective(connection, sentObjective.getValue());
            } catch (RuntimeException ignored) {
            }
        }
    }

    private static void send(UUID receiver, SidebarObjective objective) {
        final String oldObjective = sentObjectives.get(receiver);
        final Object connection = getPlayerConnection(receiver);

        createObjective(connection, objective);
        sendScores(connection, objective);

        // The objective is displayed when the scores are sent, so all the lines
        // are displayed
        // instantaneously, even with bad connections.
        setObjectiveDisplay(connection, objective);

        sentObjectives.put(receiver, objective.getName());

        if (oldObjective != null) {
            destroyObjective(connection, oldObjective);
        }
    }

    private static void createObjective(Object connection, SidebarObjective objective) {
        sendScoreboardObjectivePacket(connection, objective.getName(), objective.getDisplayName(),
                                      PACKET_SCOREBOARD_OBJECTIVE_ACTION_CREATE);
    }

    private static void updateObjectiveDisplayName(Object connection, SidebarObjective objective) {
        sendScoreboardObjectivePacket(connection, objective.getName(), objective.getDisplayName(),
                                      PACKET_SCOREBOARD_OBJECTIVE_ACTION_UPDATE);
    }

    private static void setObjectiveDisplay(Object connection, SidebarObjective objective) {
        sendScoreboardDisplayObjectivePacket(connection, objective.getName(),
                                             PACKET_DISPLAY_OBJECTIVE_SIDEBAR_LOCATION);
    }

    private static void sendScores(Object connection, SidebarObjective objective) {
        for (Map.Entry<String, Integer> score : objective.getScores().entrySet()) {
            sendScore(connection, objective, score.getKey(), score.getValue());
        }
    }

    private static void sendScore(Object connection, SidebarObjective objective, String score, Integer value) {
        sendScoreboardScorePacket(connection, objective.getName(), score, value, enumScoreboardAction_CHANGE);
    }

    private static void deleteScore(Object connection, SidebarObjective objective, String score) {
        sendScoreboardScorePacket(connection, objective.getName(), score, 0, enumScoreboardAction_REMOVE);
    }

    private static void destroyObjective(Object connection, String objectiveName) {
        sendScoreboardObjectivePacket(connection, objectiveName, "", PACKET_SCOREBOARD_OBJECTIVE_ACTION_DELETE);
    }

    private static void sendScoreboardObjectivePacket(Object connection, String objectiveName,
                                                      String objectiveDisplayName, int action) {
        try {
            Object packet = Reflection.instantiate(packetPlayOutScoreboardObjectiveClass);

            Reflection.setFieldValue(packet, "a", objectiveName); // Objective
            // name
            Reflection.setFieldValue(packet, "b", objectiveDisplayName); // Display
            // name
            Reflection.setFieldValue(packet, "c", enumScoreboardHealthDisplay_INTEGER); // Display
            // mode
            // (integer
            // or
            // hearts)
            Reflection.setFieldValue(packet, "d", action); // Action (0 =
            // create; 1 =
            // delete; 2 =
            // update)
            NMSNetwork.sendPacket(connection, packet);
        } catch (NoSuchMethodException | IllegalAccessException | InstantiationException | NoSuchFieldException e) {
            throw new IncompatibleMinecraftVersionException("Cannot send PacketPlayOutScoreboardObjective", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("An exception was caught while sending a PacketPlayOutScoreboardObjective",
                                       e.getCause());
        }
    }

    private static void sendScoreboardDisplayObjectivePacket(Object connection, String objectiveName, int location) {
        try {
            Object packet = Reflection.instantiate(packetPlayOutScoreboardDisplayObjectiveClass);

            Reflection.setFieldValue(packet, "a", location); // Objective
            // location (0 =
            // list ; 1 =
            // sidebar ; 2 =
            // below name)
            Reflection.setFieldValue(packet, "b", objectiveName); // Objective
            // name

            NMSNetwork.sendPacket(connection, packet);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | NoSuchFieldException e) {
            throw new IncompatibleMinecraftVersionException("Cannot send PacketPlayOutScoreboardDisplayObjective", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException(
                    "An exception was caught while sending a PacketPlayOutScoreboardDisplayObjective", e.getCause());
        }
    }

    private static void sendScoreboardScorePacket(Object connection, String objectiveName, String scoreName,
                                                  int scoreValue, Object action) {
        try {
            Object packet = Reflection.instantiate(packetPlayOutScoreboardScoreClass);

            Reflection.setFieldValue(packet, "a", scoreName); // Score name
            Reflection.setFieldValue(packet, "b", objectiveName); // Objective
            // name this
            // score
            // belongs
            // to
            Reflection.setFieldValue(packet, "c", scoreValue); // Score value
            Reflection.setFieldValue(packet, "d", action); // Action (enum
            // member - CHANGE
            // or REMOVE)

            NMSNetwork.sendPacket(connection, packet);
        } catch (NoSuchMethodException | InstantiationException | IllegalAccessException | NoSuchFieldException e) {
            throw new IncompatibleMinecraftVersionException("Cannot send PacketPlayOutScoreboardScore", e);
        } catch (InvocationTargetException e) {
            throw new RuntimeException("An exception was caught while sending a PacketPlayOutScoreboardScore",
                                       e.getCause());
        }
    }

    private static Object getPlayerConnection(UUID id) {
        if (playersConnections.containsKey(id))
            return playersConnections.get(id);

        try {
            final Player player = Bukkit.getPlayer(id);
            if (player == null)
                return null;
            Object connection = NMSNetwork.getPlayerConnection(player);
            if (connection != null) {
                playersConnections.put(id, connection);
                return connection;
            } else {
                throw new RuntimeException("Unable to retrieve a player's connection (UUID: " + id + ")");
            }
        } catch (InvocationTargetException e) {
            throw new RuntimeException("Unable to retrieve a player's connection (UUID: " + id + ")", e);
        }
    }

    public static void handleLogin(UUID id) {
        playersConnections.remove(id);
        sentObjectives.remove(id);
    }
}
