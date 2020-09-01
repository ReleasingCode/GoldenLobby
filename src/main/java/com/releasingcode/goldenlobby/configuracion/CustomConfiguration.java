package com.releasingcode.goldenlobby.configuracion;

import com.releasingcode.goldenlobby.GoldenLobby;
import com.releasingcode.goldenlobby.Utils;
import org.bukkit.configuration.InvalidConfigurationException;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.Plugin;

import java.io.*;
import java.nio.charset.StandardCharsets;

public class CustomConfiguration {
    private final Plugin plugin;
    private final String name;
    private FileConfiguration config;
    private File file;

    /*
        Instanciar esta clase por cada archivo
        por ejemplo
        CustomConfiguracion archivo = new CustomConfiguration("archivo", instanciaPlugin);
     */
    public CustomConfiguration(File file, Plugin main) {
        this.plugin = main;
        this.file = file;
        this.name = file.getName();
        if (!file.getAbsoluteFile().getParentFile().isDirectory()) {
            File folderParent = file.getAbsoluteFile().getParentFile();
            folderParent.mkdirs();
        }
        try {
            config = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        } catch (FileNotFoundException e) {
            e.printStackTrace();
            Utils.log("&c  - Error al leer el archivo " + file.getName() + "");
        }
    }

    public CustomConfiguration(CustomConfiguration previus, String resourceName, String child, Plugin main) {
        this.plugin = main;
        name = !resourceName.endsWith(".dat") ? resourceName + ".yml" : resourceName;
        File fileRoot = new File(main.getDataFolder(), "/");
        if (!fileRoot.isDirectory()) {
            fileRoot.mkdir();
        }
        File childPath = new File(main.getDataFolder(), child);
        if (!childPath.isDirectory()) {
            childPath.mkdir();
        }
        this.file = new File(main.getDataFolder(), child + name);
        if (!file.exists()) {
            if (main.getClass().getClassLoader().getResourceAsStream(name) == null) {
                try {
                    if (file.createNewFile()) {
                        Utils.log("&c  - Creando archivo " + name + "");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Utils.log("&c  - Error al crear " + name + "");
                }
                try {
                    config = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(previus.getFile()), StandardCharsets.UTF_8));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Utils.log("&c  - Error al leer el archivo " + name + "");
                }
            } else {
                InputStream in = main.getResource(name);
                try {
                    OutputStream out = new FileOutputStream(previus.getFile());
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.close();
                    in.close();
                    Utils.log("&c  - Creando archivo " + name + "");
                    try {
                        config = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(previus.getFile()), StandardCharsets.UTF_8));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Utils.log("&c  - Error al leer el archivo " + file.getName() + "");
                    }
                } catch (Exception e) {
                    Utils.log("&c  - Error al crear el archivo " + name + "");
                }

            }
        } else {
            try {
                config = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(previus.getFile()), StandardCharsets.UTF_8));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Utils.log("&c  - Error al leer el archivo " + name + "");
            }
        }
    }

    public static boolean deleteFile(String child, String name) {
        File file = new File(GoldenLobby.getInstance().getDataFolder(), child + "/" + name);
        return file.delete();
    }

    public static File[] getFilesPath(String path) {
        File file = new File(GoldenLobby.getInstance().getDataFolder(), path);
        if (!file.isDirectory()) {
            file.mkdir();
        }
        return file.listFiles();
    }

    public CustomConfiguration(String resourceName, String child, Plugin main) {
        this.plugin = main;
        name = !resourceName.endsWith(".dat") ? resourceName + ".yml" : resourceName;
        File fileRoot = new File(main.getDataFolder(), "/");
        if (!fileRoot.isDirectory()) {
            fileRoot.mkdir();
        }
        File childPath = new File(main.getDataFolder(), child);
        if (!childPath.isDirectory()) {
            childPath.mkdir();
        }
        this.file = new File(main.getDataFolder(), child + name);
        if (!file.exists()) {
            if (main.getClass().getClassLoader().getResourceAsStream(name) == null) {
                try {
                    if (file.createNewFile()) {
                        Utils.log("&c  - Creando archivo " + name + "");
                    }
                } catch (IOException e) {
                    e.printStackTrace();
                    Utils.log("&c  - Error al crear " + name + "");
                }
                try {
                    config = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
                } catch (FileNotFoundException e) {
                    e.printStackTrace();
                    Utils.log("&c  - Error al leer el archivo " + name + "");
                }
            } else {
                InputStream in = main.getResource(name);
                try {
                    OutputStream out = new FileOutputStream(file);
                    byte[] buf = new byte[1024];
                    int len;
                    while ((len = in.read(buf)) > 0) {
                        out.write(buf, 0, len);
                    }
                    out.close();
                    in.close();
                    Utils.log("&c  - Creando archivo " + name + "");
                    try {
                        config = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
                    } catch (FileNotFoundException e) {
                        e.printStackTrace();
                        Utils.log("&c  - Error al leer el archivo " + file.getName() + "");
                    }
                } catch (Exception e) {
                    Utils.log("&c  - Error al crear el archivo " + name + "");
                }

            }
        } else {
            try {
                config = YamlConfiguration.loadConfiguration(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
            } catch (FileNotFoundException e) {
                e.printStackTrace();
                Utils.log("&c  - Error al leer el archivo " + name + "");
            }
        }
    }

    public CustomConfiguration(String resourceName, Plugin main) {
        this(resourceName, "/", main);
    }

    public static void copyDefaultConfig(File file, String resourceName, Plugin plugin) {

        InputStream in = plugin.getResource(resourceName);
        try {
            OutputStream out = new FileOutputStream(file);
            byte[] buf = new byte[1024];
            int len;
            while ((len = in.read(buf)) > 0) {
                out.write(buf, 0, len);
            }
            out.close();
            in.close();
            Utils.log("&c  - Creando archivo " + resourceName + "");
        } catch (Exception e) {
            Utils.log("&c  - Error al crear el archivo " + resourceName + "");
        }
    }

    public void save() {
        try {
            config.save(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void updateFile(String text) {
        try {
            FileWriter fw = new FileWriter(getFile(), false);
            fw.write(text);
            fw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public void addDefault(String path, Object value) {
        if (!config.isSet(path)) {
            config.set(path, value);
            save();
        }
    }

    public void set(String path, Object value) {
        config.set(path, value);
        save();
    }

    public FileConfiguration getConfig() {
        return config;
    }

    public void reloadConfig() {
        try {
            this.config.load(new InputStreamReader(new FileInputStream(file), StandardCharsets.UTF_8));
        } catch (IOException | InvalidConfigurationException e) {
            e.printStackTrace();
        }
    }

    public void changeLocation(String child) {
        File fileRoot = new File(plugin.getDataFolder(), "/");
        if (!fileRoot.isDirectory()) {
            fileRoot.mkdir();
        }
        File childPath = new File(plugin.getDataFolder(), child);
        if (!childPath.isDirectory()) {
            childPath.mkdir();
        }
        this.file = new File(plugin.getDataFolder(), child + name);
    }

    public File getFile() {
        return file;
    }


}
