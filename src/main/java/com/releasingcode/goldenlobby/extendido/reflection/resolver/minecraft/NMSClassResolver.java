package com.releasingcode.goldenlobby.extendido.reflection.resolver.minecraft;


import com.releasingcode.goldenlobby.extendido.reflection.minecraft.Minecraft;
import com.releasingcode.goldenlobby.extendido.reflection.resolver.ClassResolver;

public class NMSClassResolver extends ClassResolver {

    @Override
    public Class resolve(String... names) throws ClassNotFoundException {
        for (int i = 0; i < names.length; i++) {
            if (!names[i].startsWith("net.minecraft.server")) {
                names[i] = "net.minecraft.server." + Minecraft.getVersion() + names[i];
            }
        }
        return super.resolve(names);
    }
}
