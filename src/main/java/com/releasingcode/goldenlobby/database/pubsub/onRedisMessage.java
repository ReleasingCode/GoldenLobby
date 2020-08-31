package com.releasingcode.goldenlobby.database.pubsub;

import redis.clients.jedis.BinaryJedisPubSub;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.stream.Collectors;

public class onRedisMessage extends BinaryJedisPubSub {
    private static final LinkedHashMap<String, IRedisSub> register = new LinkedHashMap<>();

    public static void registerUpdater(SubChannel subChannel, IRedisSub then) {
        register.put(subChannel.lower(), then);
    }

    public static byte[][] getChannels() {
        ArrayList<byte[]> on = register.keySet().stream().map(String::getBytes).collect(Collectors.toCollection(ArrayList::new));
        byte[][] bytes = new byte[on.size()][];
        bytes = on.toArray(bytes);
        return bytes;
    }

    @Override
    public void onMessage(byte[] channelByte, byte[] messageByte) {
        String channel = new String(channelByte);
        if (register.containsKey(channel.toLowerCase())) {
            register.get(channel.toLowerCase()).onIMessage(messageByte);
        }
    }

    @Override
    public void onSubscribe(byte[] channel, int subscribedChannels) {
        super.onSubscribe(channel, subscribedChannels);
    }
}
