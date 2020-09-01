package com.releasingcode.goldenlobby.extendido.packetlistener;

import com.releasingcode.goldenlobby.extendido.packetlistener.channel.ChannelAbstract;
import com.releasingcode.goldenlobby.extendido.reflection.resolver.ClassResolver;
import com.releasingcode.goldenlobby.extendido.reflection.resolver.ConstructorResolver;
import org.bukkit.entity.Player;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

public class ChannelInjector {

    private static final ClassResolver CLASS_RESOLVER = new ClassResolver();

    private ChannelAbstract channel;

    public boolean inject(IPacketListener iPacketListener) {
        List<Exception> exceptions = new ArrayList<>();
        try {
            Class.forName("io.netty.channel.Channel");
            channel = newChannelInstance(iPacketListener, "com.releasingcode.goldenlobby.extendido.packetlistener.channel.INCChannel");
            System.out.println("[PacketListenerAPI] Using INChannel");
            return true;
        } catch (Exception e1) {
            exceptions.add(e1);
        }
        for (Exception e : exceptions) {
            e.printStackTrace();
        }
        return false;
    }

    protected ChannelAbstract newChannelInstance(IPacketListener iPacketListener, String clazzName) throws ClassNotFoundException, NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        return (ChannelAbstract) new ConstructorResolver(CLASS_RESOLVER.resolve(clazzName)).resolve(new Class[]{IPacketListener.class}).newInstance(iPacketListener);
    }

    public void addChannel(Player p) {
        this.channel.addChannel(p);
    }

    public void removeChannel(Player p) {
        this.channel.removeChannel(p);
    }

    public void addServerChannel() {
        this.channel.addServerChannel();
    }

}
