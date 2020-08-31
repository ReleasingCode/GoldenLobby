package com.releasingcode.goldenlobby.npc.api.skin;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.authlib.properties.Property;
import com.releasingcode.goldenlobby.call.CallBack;
import org.bukkit.Bukkit;

import java.io.UnsupportedEncodingException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadLocalRandom;

public class SkinFetcher {

    private static final String MINETOOLSAPIUUID = "https://api.minetools.eu/uuid/";
    private static final String MINETOOLSAPISKIN = "https://api.minetools.eu/profile/";
    private static final Map<String, String> cachedUUIDs = new ConcurrentHashMap<>();
    private static final Map<String, GameProfile> cachedProfiles = new ConcurrentHashMap<>();
    private static final Map<String, Skin> cachedProfilesSkinData = new ConcurrentHashMap<>();
    private static final ExecutorService EXECUTOR = Executors.newSingleThreadExecutor();
    private static final String USERNAME_CHARS = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static void fetchSkinFromIdAsync(final String name, Callback callback) {
        if (name.length() <= 36) { // nombre o UUID
            if (name.length() <= 16) { // name
                if (cachedUUIDs.containsKey(name.toLowerCase())) {
                    String uuid = cachedUUIDs.get(name.toLowerCase());
                    fetchSkin(uuid, callback);
                    return;
                }
                fetchUUID(name, new CallBack.ReturnCallBack<String>() {
                    @Override
                    public void onSuccess(String uuid) {
                        cachedUUIDs.put(name.toLowerCase(), uuid);
                        fetchSkin(uuid, callback);
                    }

                    @Override
                    public void onError(String err) {
                        callback.failed();
                    }
                });
            } else { // UUID
                fetchSkin(name.replace("-", ""), callback);
            }
        } else { //base64
            if (cachedProfiles.containsKey(name.toLowerCase())) {
                GameProfile profileCached = cachedProfiles.get(name.toLowerCase());
                try {
                    callback.call(new Skin(profileCached, name));
                } catch (Exception e) {
                    callback.failed();
                }
                return;
            }
            String base64;
            try {
                Base64.getDecoder().decode(name);
                base64 = name;
            } catch (IllegalArgumentException ex) {
                try {
                    base64 = "{textures:{SKIN:{url:\"" + URLEncoder.encode(name, "UTF-8") + "\"}}}";
                } catch (UnsupportedEncodingException ex2) {
                    cachedProfiles.put(name.toLowerCase(), new GameProfile(UUID.randomUUID(), null));
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

    public static void fetchSkin(String uuid, Callback callback) {
        if (cachedProfiles.containsKey(uuid.toLowerCase())) {
            Skin data = cachedProfilesSkinData.get(uuid.toLowerCase());
            callback.call(data);
            return;
        }
        EXECUTOR.execute(() -> {
            try {
                StringBuilder builder = new StringBuilder();
                HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(MINETOOLSAPISKIN + uuid).openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();
                Scanner scanner = new Scanner(httpURLConnection.getInputStream());
                while (scanner.hasNextLine()) {
                    builder.append(scanner.nextLine());
                }
                scanner.close();
                httpURLConnection.disconnect();
                JsonObject jsonObject = (JsonObject) new JsonParser().parse(builder.toString());
                JsonArray textures = jsonObject.get("raw").getAsJsonObject().get("properties").getAsJsonArray();
                String status = jsonObject.get("raw").getAsJsonObject().get("status").getAsString();
                if (!status.toLowerCase().trim().equals("ok")) {
                    callback.failed();
                    return;
                }
                for (int i = 0; i < textures.size(); i++) {
                    JsonObject object = textures.get(i).getAsJsonObject();
                    String name = object.get("name").getAsString();
                    String value = object.get("value").getAsString();
                    String signature = object.get("signature").getAsString();
                    if (name.toLowerCase().equals("textures")) {
                        Skin skin = new Skin(value, signature, uuid);
                        cachedProfilesSkinData.put(uuid.toLowerCase(), skin);
                        callback.call(skin);
                        return;
                    }
                }
            } catch (Exception exception) {
                Bukkit.getLogger().severe("No se pudo retornar la skin!: " + uuid + ", error: " + exception.getMessage());
                exception.printStackTrace();
                callback.failed();
            }
        });
    }

    public static void fetchUUID(String name, CallBack.ReturnCallBack<String> callback) {
        EXECUTOR.execute(() -> {
            try {
                StringBuilder builder = new StringBuilder();
                HttpURLConnection httpURLConnection = (HttpURLConnection) new URL(MINETOOLSAPIUUID + name).openConnection();
                httpURLConnection.setRequestMethod("GET");
                httpURLConnection.setDoOutput(true);
                httpURLConnection.setDoInput(true);
                httpURLConnection.connect();
                Scanner scanner = new Scanner(httpURLConnection.getInputStream());
                while (scanner.hasNextLine()) {
                    builder.append(scanner.nextLine());
                }
                scanner.close();
                httpURLConnection.disconnect();
                JsonObject jsonObject = (JsonObject) new JsonParser().parse(builder.toString());
                String status = jsonObject.get("status").getAsString();
                if (!status.toLowerCase().trim().equals("ok")) {
                    callback.onError(null);
                    return;
                }
                String value = jsonObject.get("id").getAsString();
                callback.onSuccess(value);
            } catch (Exception exception) {
                Bukkit.getLogger().severe("No se pudo retornar la UUID!: " + name + ", error: " + exception.getMessage());
                callback.onError(null);
            }
        });
    }

    public interface Callback {
        void call(Skin skinData);

        default void failed() {
        }
    }
}
