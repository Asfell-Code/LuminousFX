package asfell.luminousfx.client;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.LinkedHashMap;
import java.util.Map;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import net.fabricmc.loader.api.FabricLoader;

public final class LuminousFXConfig {
    private static final Gson GSON = new GsonBuilder().setPrettyPrinting().create();
    private static final Path FILE = FabricLoader.getInstance().getConfigDir().resolve("luminousfx.json");
    private static final Map<String, Boolean> modules = new LinkedHashMap<>();
    private static final Map<String, Integer> integers = new LinkedHashMap<>();
    private static final Map<String, Double> decimals = new LinkedHashMap<>();

    private LuminousFXConfig() {
    }

    public static void load() {
        if (!Files.exists(FILE)) {
            save();
            return;
        }
        try {
            StoredConfig stored = GSON.fromJson(Files.readString(FILE), StoredConfig.class);
            if (stored != null) {
                if (stored.modules != null) {
                    stored.modules.forEach((name, enabled) -> {
                        if (name != null && enabled != null) {
                            modules.put(name, enabled);
                        }
                    });
                }
                if (stored.integers != null) {
                    stored.integers.forEach((name, value) -> {
                        if (name != null && value != null) {
                            integers.put(name, value);
                        }
                    });
                }
                if (stored.decimals != null) {
                    stored.decimals.forEach((name, value) -> {
                        if (name != null && value != null) {
                            decimals.put(name, value);
                        }
                    });
                }
            }
        } catch (IOException | RuntimeException ignored) {
            save();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(new StoredConfig(modules, integers, decimals)));
        } catch (IOException ignored) {
        }
    }

    public static boolean getBoolean(String key, boolean defaultValue) {
        return modules.getOrDefault(key, defaultValue);
    }

    public static boolean isEnabled(String key) {
        return getBoolean(key, false);
    }

    public static boolean isEnabled(String key, boolean defaultValue) {
        return getBoolean(key, defaultValue);
    }

    public static void setBoolean(String key, boolean value) {
        modules.put(key, value);
        save();
    }

    public static int getInt(String key, int defaultValue) {
        return integers.getOrDefault(key, defaultValue);
    }

    public static double getDouble(String key, double defaultValue) {
        return decimals.getOrDefault(key, defaultValue);
    }

    public static void setDouble(String key, double value) {
        decimals.put(key, value);
        save();
    }

    public static void setDoubleWithoutSaving(String key, double value) {
        decimals.put(key, value);
    }

    public static void setInt(String key, int value) {
        integers.put(key, value);
        save();
    }

    public static void setIntWithoutSaving(String key, int value) {
        integers.put(key, value);
    }

    private record StoredConfig(Map<String, Boolean> modules, Map<String, Integer> integers,
            Map<String, Double> decimals) {
    }
}