package com.releasingcode.goldenlobby.database;

import com.releasingcode.goldenlobby.Utils;
import redis.clients.jedis.BinaryJedisPubSub;
import redis.clients.jedis.Jedis;
import redis.clients.jedis.JedisPool;
import redis.clients.jedis.JedisPoolConfig;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.ScheduledFuture;
import java.util.concurrent.TimeUnit;

public class RedisManager {
    private final String host;
    private final String password;
    private final JedisPoolConfig config;
    int port;
    int timeout;
    ScheduledExecutorService executor;
    ScheduledFuture<?> schedule;
    private JedisPool jedisEditor;
    private Jedis jedisSub;

    public RedisManager(String host, String password, int port) {
        executor = Executors.newSingleThreadScheduledExecutor();
        this.host = host;
        this.password = password;
        this.port = port;
        timeout = 6000;
        //Para Editores (Publish)
        config = new JedisPoolConfig();
        config.setMaxTotal(10);
        config.setMaxWaitMillis(6000);
        config.setBlockWhenExhausted(true);
        stablishConnectionJedisPub();
        //Para subscriptor (Sub)
        stablishConnectionJedisSub();
    }

    public void stablishConnectionJedisPub() {
        jedisEditor = new JedisPool(config, host, port, timeout, password);
    }

    public void stablishConnectionJedisSub() {
        jedisSub = new Jedis(host, port, timeout);
        jedisSub.auth(password);
    }

    public void registerChangesListener(BinaryJedisPubSub sub, byte[]... bytes) {
        if (!isPubConnected()) {
            return;
        }
        if (schedule != null) {
            schedule.cancel(true);
        }
        schedule = executor.scheduleAtFixedRate(() -> {
            Utils.log("&eInicializando Conexion Redis [Sub]");
            try {
                if (jedisSub == null) {
                    stablishConnectionJedisSub();
                }
                Utils.log("&aConexion establecida [Redis][Sub]");
                jedisSub.subscribe(sub, bytes);
            } catch (Exception e) {
                //error de conexión intentando nuevamente en 5 segundos
                jedisSub = null;
                Utils.log("&cConexion de Jedis Perdida [Sub], reintentando conexion en 5 segundos");
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException interruptedException) {
                    interruptedException.printStackTrace();
                }
            }

        }, 0, 5, TimeUnit.SECONDS);
    }

    public Jedis getJedisSub() {
        return jedisSub;
    }

    public boolean isEditorConnected() {
        try {
            return getJedisEditor() != null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void pub(byte[] channel, byte[] data) {
        try (Jedis jedis = getJedisEditor()) {
            jedis.publish(channel, data);
        }
    }

    public Jedis getJedisEditor() {
        return jedisEditor.getResource();
    }

    public boolean isPubConnected() {
        try {
            return getJedisSub() != null;
        } catch (Exception e) {
            e.printStackTrace();
        }
        return false;
    }

    public void createSubs() {

    }
}