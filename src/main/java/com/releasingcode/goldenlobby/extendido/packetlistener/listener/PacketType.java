package com.releasingcode.goldenlobby.extendido.packetlistener.listener;

public enum PacketType
{
    PacketHandshakingInSetProtocol,

    PacketLoginInStart,
    PacketLoginInEncryptionBegin,

    PacketLoginOutDisconnect,
    PacketLoginOutEncryptionBegin,
    PacketLoginOutSuccess,
    PacketLoginOutSetCompression,

    PacketPlayInAbilities,
    PacketPlayInArmAnimation,
    PacketPlayInBlockDig,
    PacketPlayInBlockPlace,
    PacketPlayInChat,
    PacketPlayInClientCommand,
    PacketPlayInCloseWindow,
    PacketPlayInCustomPayload,
    PacketPlayInEnchantItem,
    PacketPlayInEntityAction,
    PacketPlayInFlying,
    PacketPlayInLook,
    PacketPlayInPosition,
    PacketPlayInPositionLook,
    PacketPlayInHeldItemSlot,
    PacketPlayInKeepAlive,
    PacketPlayInResourcePackStatus,
    PacketPlayInSetCreativeSlot,
    PacketPlayInSettings,
    PacketPlayInSpectate,
    PacketPlayInSteerVehicle,
    PacketPlayInTabComplete,
    PacketPlayInTransaction,
    PacketPlayInUpdateSign,
    PacketPlayInUseEntity,
    PacketPlayInWindowClick,

    PacketPlayOutAbilities,
    PacketPlayOutAnimation,
    PacketPlayOutAttachEntity,
    PacketPlayOutBed,
    PacketPlayOutBlockAction,
    PacketPlayOutBlockBreakAnimation,
    PacketPlayOutBlockChange,
    PacketPlayOutCamera,
    PacketPlayOutChat,
    PacketPlayOutCloseWindow,
    PacketPlayOutCollect,
    PacketPlayOutCombatEvent,
    PacketPlayOutCustomPayload,
    PacketPlayOutEntity,
    PacketPlayOutEntityLook,
    PacketPlayOutRelEntityMove,
    PacketPlayOutRelEntityMoveLook,
    PacketPlayOutEntityDestroy,
    PacketPlayOutEntityEffect,
    PacketPlayOutEntityEquipment,
    PacketPlayOutEntityHeadRotation,
    PacketPlayOutEntityMetadata,
    PacketPlayOutEntityStatus,
    PacketPlayOutEntityTeleport,
    PacketPlayOutEntityVelocity,
    PacketPlayOutExperience,
    PacketPlayOutExplosion,
    PacketPlayOutGameStateChange,
    PacketPlayOutHeldItemSlot,
    PacketPlayOutKeepAlive,
    PacketPlayOutKickDisconnect,
    PacketPlayOutLogin,
    PacketPlayOutMap,
    PacketPlayOutMapChunk,
    PacketPlayOutMapChunkBulk,
    PacketPlayOutMultiBlockChange,
    PacketPlayOutNamedEntitySpawn,
    PacketPlayOutNamedSoundEffect,
    PacketPlayOutOpenSignEditor,
    PacketPlayOutOpenWindow,
    PacketPlayOutPlayerInfo,
    PacketPlayOutPlayerListHeaderFooter,
    PacketPlayOutPosition,
    PacketPlayOutRemoveEntityEffect,
    PacketPlayOutResourcePackSend,
    PacketPlayOutRespawn,
    PacketPlayOutScoreboardDisplayObjective,
    PacketPlayOutScoreboardObjective,
    PacketPlayOutScoreboardScore,
    PacketPlayOutScoreboardTeam,
    PacketPlayOutServerDifficulty,
    PacketPlayOutSetCompression,
    PacketPlayOutSetSlot,
    PacketPlayOutSpawnEntity,
    PacketPlayOutSpawnEntityExperienceOrb,
    PacketPlayOutSpawnEntityLiving,
    PacketPlayOutSpawnEntityPainting,
    PacketPlayOutSpawnEntityWeather,
    PacketPlayOutSpawnPosition,
    PacketPlayOutStatistic,
    PacketPlayOutTabComplete,
    PacketPlayOutTileEntityData,
    PacketPlayOutTitle,
    PacketPlayOutTransaction,
    PacketPlayOutUpdateAttributes,
    PacketPlayOutUpdateEntityNBT,
    PacketPlayOutUpdateHealth,
    PacketPlayOutUpdateSign,
    PacketPlayOutUpdateTime,
    PacketPlayOutWindowData,
    PacketPlayOutWindowItems,
    PacketPlayOutWorldBorder,
    PacketPlayOutWorldEvent,
    PacketPlayOutWorldParticles,

    PacketStatusInPing,
    PacketStatusInStart,

    PacketStatusOutPong,
    PacketStatusOutServerInfo;

    public boolean equalsClient ()
    {
        return name().startsWith("PacketPlayIn") ||
                name().startsWith("PacketStatusIn") ||
                name().startsWith("PacketLoginIn") ||
                name().startsWith("PacketHandshakingIn");
    }

    public boolean equalsServer ()
    {
        return name().startsWith("PacketPlayOut") ||
                name().startsWith("PacketStatusOut") ||
                name().startsWith("PacketLoginOut");
    }

}