package asfell.luminousfx.client;

import java.util.List;

public final class LuminousFXModules {
    private static final List<LuminousFXModule> MODULES = List.of(
            new NightVisionModule(),
            new ArmorHudModule(),
            new TrajectoryModule());

    private LuminousFXModules() {
    }

    public static void register() {
        MODULES.forEach(module -> module.register());
    }

    public static List<LuminousFXModule> all() {
        return MODULES;
    }
}