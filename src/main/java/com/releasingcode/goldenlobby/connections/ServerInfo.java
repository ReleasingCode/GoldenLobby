package com.releasingcode.goldenlobby.connections;

import com.google.common.base.Charsets;
import com.releasingcode.goldenlobby.Utils;

import java.io.ByteArrayOutputStream;
import java.net.InetSocketAddress;

public class ServerInfo {
    private static final int PROTOCOL_VERSION = 735, PROTOCOL_VERSION_LENGTH = Utils.varIntLength(PROTOCOL_VERSION);
    private final InetSocketAddress socketAddress;
    String name;
    String host;
    int online;
    int maxplayers;
    String motd;
    private boolean available;
    private byte[] handshake;

    public ServerInfo(InetSocketAddress socketAddress) {
        this.socketAddress = socketAddress;
        available = false;
        handshake();
    }

    public void handshake() {
        byte[] addressBytes = socketAddress.getHostString().getBytes(Charsets.UTF_8);
        ByteArrayOutputStream bytes = new ByteArrayOutputStream();
        int port = socketAddress.getPort();
        // Handshake
        Utils.writeVarInt(bytes,
                4 + PROTOCOL_VERSION_LENGTH + Utils.varIntLength(addressBytes.length) + addressBytes.length);
        bytes.write(0);
        Utils.writeVarInt(bytes, PROTOCOL_VERSION);
        Utils.writeVarInt(bytes, addressBytes.length);
        bytes.write(addressBytes, 0, addressBytes.length);
        bytes.write((port >> 8) & 0xFF);
        bytes.write(port & 0xFF);
        bytes.write(1); // Next protocol state: Status
        bytes.write(new byte[]{1, 0}, 0, 2); // Status Request
        handshake = bytes.toByteArray();
    }

    public boolean isAvailable() {
        return available;
    }

    public void setAvailable(boolean available) {
        this.available = available;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public byte[] getHandshake() {
        return handshake;
    }

    public InetSocketAddress getSocketAddress() {
        return socketAddress;
    }

    public int getMaxplayers() {
        return maxplayers;
    }

    public void setMaxplayers(int maxplayers) {
        this.maxplayers = maxplayers;
    }

    public int getOnline() {
        return online;
    }

    public void setOnline(int online) {
        this.online = online;
    }

    public String getHost() {
        return host;
    }

    public void setHost(String host) {
        this.host = host;
    }

    public String getMotd() {
        return motd;
    }

    public void setMotd(String motd) {
        this.motd = motd;
    }

    public enum Estados {
        OFF, ON, NOT_FOUND
    }
}
