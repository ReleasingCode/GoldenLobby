package com.releasingcode.goldenlobby.database.pubsub;

import java.io.*;

public class NetworkStream {
    public static byte[] toByte(Object object) throws IOException {
        ByteArrayOutputStream byteOut = new ByteArrayOutputStream();
        ObjectOutputStream out = new ObjectOutputStream(byteOut);
        out.writeObject(object);
        return byteOut.toByteArray();
    }

    public static Object fromByte(byte[] bytes) throws IOException, ClassNotFoundException {
        ByteArrayInputStream byteIn = new ByteArrayInputStream(bytes);
        ObjectInputStream in = new ObjectInputStream(byteIn);
        return in.readObject();
    }
}
