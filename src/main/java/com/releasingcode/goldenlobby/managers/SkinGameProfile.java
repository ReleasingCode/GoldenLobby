package com.releasingcode.goldenlobby.managers;

import com.google.common.base.Predicate;
import com.google.common.io.ByteStreams;
import com.google.gson.JsonArray;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import org.bukkit.Bukkit;
import org.bukkit.inventory.meta.SkullMeta;
import com.releasingcode.goldenlobby.npc.api.skin.Skin;

import java.io.IOException;
import java.io.InputStream;
import java.io.UnsupportedEncodingException;
import java.lang.reflect.Field;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.Map;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ThreadLocalRandom;

public class SkinGameProfile {
    private static final Field skullProfile;
    private static final Method fillProfile;
    private static final String USERNAME_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";
    private static final Map<String, GameProfile> cachedProfiles = new ConcurrentHashMap<>();

    static {
        Field field = null;
        Method method = null, method1 = null;
        try {
            String pkg = Bukkit.getServer().getClass().getPackage().getName();
            field = Class.forName(pkg + ".inventory.CraftMetaSkull").getDeclaredField("profile");
            field.setAccessible(true);
            pkg = "net.minecraft.server" + pkg.substring(pkg.lastIndexOf('.'));
            Class<?> clazz = Class.forName(pkg + ".TileEntitySkull");
            for (Method m : clazz.getDeclaredMethods()) {
                if (Modifier.isStatic(m.getModifiers())) {
                    Class<?>[] params = m.getParameterTypes();
                    if (params.length > 0 && params[0] == GameProfile.class) {
                        method = m;
                        break;
                    }
                }
            }
            method1 = Class.forName(pkg + ".GameProfileSerializer").getDeclaredMethod(
                    "serialize", Class.forName(pkg + ".NBTTagCompound"), field.getType());
        } catch (Exception ex) {
            // Not supported
        }
        skullProfile = field;
        fillProfile = method;
    }

    private final String name;

    public SkinGameProfile(String name) {
        this.name = name;
    }

    private static void load(final String mapName, String name, Callback callback) {
        final GameProfile profile = new GameProfile(null, name);
        cachedProfiles.put(mapName.toLowerCase(), profile);
        try {
            if (fillProfile.getParameterTypes().length == 2) { // Spigot
                fillProfile.invoke(null, profile, (Predicate<GameProfile>) profile1 -> {
                    if (profile1 != null) {
                        cachedProfiles.put(mapName.toLowerCase(), profile1);
                        try {
                            callback.call(new Skin(profile1, name));
                        } catch (Exception e) {
                            callback.failed();
                        }
                    }
                    return false;
                });
            } else if (fillProfile.getParameterTypes().length == 1) { // Bukkit
                cachedProfiles.put(mapName.toLowerCase(), (GameProfile) fillProfile.invoke(null, profile));
                try {
                    callback.call(new Skin(profile, name));
                } catch (Exception e) {
                    callback.failed();
                }
            }
        } catch (Exception ex) {
            // No support ;c
        }
    }

    public static void loadGameProfile(String name, Callback callback) {
        GameProfile gameProfile = cachedProfiles.getOrDefault(name, null);
        if (gameProfile != null) {
            try {
                callback.call(new Skin(gameProfile, name));
            } catch (Exception e) {
                callback.failed();
            }
        } else {
            loadProfile(name, callback);
        }
    }

    private static void loadProfile(final String name, Callback callback) {
        int length = name.length();
        if (length <= 36) { // uuid/name
            final GameProfile profile;
            if (length <= 16) { // name :O
                load(name, name, callback);
                return;
            } else if (length == 32) { // non-hyphen uuid
                profile = new GameProfile(UUID.fromString(name.replaceFirst("(\\w{8})(\\w{4})(\\w{4})(\\w{4})(\\w{12})", "$1-$2-$3-$4-$5")), null);
            } else if (length == 36) { // hyphen uuid
                profile = new GameProfile(UUID.fromString(name), null);
            } else { // idk D:
                cachedProfiles.put(name.toLowerCase().toLowerCase(), new GameProfile(UUID.randomUUID(), null));
                callback.failed();
                return;
            }
            cachedProfiles.put(name.toLowerCase().toLowerCase(), profile);
            // Load skin from UUID asynchronous:
            Thread thread = new Thread(() -> {
                UUID uuid = profile.getId();
                try {
                    URLConnection connection = new URL("https://api.mojang.com/user/profiles/"
                                                               + uuid.toString().replace("-", "") + "/names").openConnection();
                    InputStream in = connection.getInputStream();
                    byte[] data = new byte[in.available()];
                    ByteStreams.readFully(in, data);
                    JsonArray array = (JsonArray) new JsonParser().parse(new String(data, StandardCharsets.UTF_8));
                    String currentName = array.get(array.size() - 1).getAsJsonObject().get("name").getAsString();
                    load(name, currentName, callback);
                } catch (IOException ex) {
                    callback.failed();
                    // Cannot connect/no such player
                }
            });
            thread.setDaemon(true);
            thread.start();
        } else { // json?
            String base64;
            try {
                Base64.getDecoder().decode(name);
                base64 = name;
            } catch (IllegalArgumentException ex) {
                try {
                    base64 = "{textures:{SKIN:{url:\"" + URLEncoder.encode(name, "UTF-8") + "\"}}}";
                } catch (UnsupportedEncodingException ex2) {
                    cachedProfiles.put(name.toLowerCase().toLowerCase(), new GameProfile(UUID.randomUUID(), null));
                    callback.failed();
                    return;
                }
            }
            try {
                char[] chars = new char[16];
                Random random = ThreadLocalRandom.current();
                for (int i = 0; i < 16; ++i) {
                    chars[i] = USERNAME_CHARS.charAt(random.nextInt(36));
                }
                GameProfile profile = new GameProfile(UUID.randomUUID(), new String(chars));
                Property p = new Property("textures", base64);
                profile.getProperties().put("textures", p);
                cachedProfiles.put(name.toLowerCase(), profile);
                try {
                    callback.call(new Skin(profile, name));
                } catch (Exception e) {
                    callback.failed();
                }
            } catch (Exception ex) {
                cachedProfiles.put(name.toLowerCase(), new GameProfile(UUID.randomUUID(), null));
                callback.failed();
            }
        }
    }

    public GameProfile getGameProfile() {
        return cachedProfiles.getOrDefault(name.toLowerCase(), null);
    }

    public void applySkull(SkullMeta meta) {
        GameProfile profile = cachedProfiles.get(name.toLowerCase());
        if (profile != null) {
            if (profile.getName() != null) {
                try {
                    skullProfile.set(meta, profile);
                } catch (Exception ex) {
                    // No support ;c
                }
            }
        } else {
            loadProfile(name, skinData -> {
            });
            meta.setOwner(name);
        }
    }

    public interface Callback {

        void call(Skin skinData);

        default void failed() {
        }
    }
}
