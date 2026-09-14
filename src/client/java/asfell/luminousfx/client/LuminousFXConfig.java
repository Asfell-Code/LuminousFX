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
    private static int armorBarColor = 0xFF59E391;
    private static int hitboxColor = 0xFF54B9FF;

    static {
        modules.put("Crystal Damage Viz", true);
        modules.put("Crystal Timer", true);
        modules.put("Blast Radius", true);
        modules.put("Armor Durability", true);
        modules.put("Hit Marker", true);
                modules.put("Night Vision", false);
     }

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
                        if (modules.containsKey(name) && enabled != null) {
                            modules.put(name, enabled);
                        }
                    });
                }
                if (stored.armorBarColor != null) {
                    armorBarColor = stored.armorBarColor;
                }
                if (stored.hitboxColor != null) {
                    hitboxColor = stored.hitboxColor;
                }
            }
        } catch (IOException | RuntimeException ignored) {
            save();
        }
    }

    public static void save() {
        try {
            Files.createDirectories(FILE.getParent());
            Files.writeString(FILE, GSON.toJson(new StoredConfig(modules, armorBarColor, hitboxColor)));
        } catch (IOException ignored) {
        }
    }

    public static boolean isEnabled(String name) {
        return modules.getOrDefault(name, false);
    }

    public static int getArmorBarColor() {
        return armorBarColor;
    }

    public static int getHitboxColor() {
        return hitboxColor;
    }

    public static void cycleArmorBarColor() {
        armorBarColor = nextColor(armorBarColor);
        save();
    }

    public static void cycleHitboxColor() {
        hitboxColor = nextColor(hitboxColor);
        save();
    }

    private static int nextColor(int color) {
        return switch (color) {
            case 0xFF59E391 -> 0xFF54B9FF;
            case 0xFF54B9FF -> 0xFFFFC857;
            case 0xFFFFC857 -> 0xFFFF4F66;
            default -> 0xFF59E391;
        };
    }

    public static void setEnabled(String name, boolean enabled) {
        if (modules.containsKey(name)) {
            modules.put(name, enabled);
            save();
        }
    }

    private record StoredConfig(Map<String, Boolean> modules, Integer armorBarColor, Integer hitboxColor) {
    }
}