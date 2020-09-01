package com.releasingcode.goldenlobby.extendido.packetlistener.listener;

public interface PacketExecutor
{
	void call(PacketListener listen, PacketEvent event) throws Exception;
}