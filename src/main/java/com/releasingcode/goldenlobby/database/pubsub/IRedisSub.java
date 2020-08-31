package com.releasingcode.goldenlobby.database.pubsub;

public interface IRedisSub {
    void onIMessage(byte[] messageByte);
}
