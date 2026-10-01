package asfell.luminousfx.client;

public final class NightVisionModule implements LuminousFXModule {
    private static final String ID = "night_vision";
    private static final String GAMMA_KEY = "night_vision.gamma_value";
    private static final String LEGACY_GAMMA_KEY = "night_vision.gamma_tenths";
    private static final double DEFAULT_GAMMA = 10.0;
    private static final double MIN_GAMMA = 1.0;
    private static final double MAX_GAMMA = 15.0;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String translationKey() {
        return "module.luminousfx.night_vision";
    }

    @Override
    public boolean defaultEnabled() {
        return false;
    }

    @Override
    public void register() {
    }

    public static boolean isEnabled() {
        return LuminousFXConfig.getBoolean(ID, false);
    }

    public static double getConfiguredGamma() {
        double legacyDefault = LuminousFXConfig.getInt(LEGACY_GAMMA_KEY, 100) / 10.0;
        double gamma = LuminousFXConfig.getDouble(GAMMA_KEY, legacyDefault == 10.0
                ? DEFAULT_GAMMA : legacyDefault);
        return Math.max(MIN_GAMMA, Math.min(MAX_GAMMA, gamma));
    }

    public static void setConfiguredGamma(double gamma) {
        LuminousFXConfig.setDouble(GAMMA_KEY, Math.max(MIN_GAMMA, Math.min(MAX_GAMMA, gamma)));
    }

    public static void previewConfiguredGamma(double gamma) {
        LuminousFXConfig.setDoubleWithoutSaving(GAMMA_KEY,
                Math.max(MIN_GAMMA, Math.min(MAX_GAMMA, gamma)));
    }
}