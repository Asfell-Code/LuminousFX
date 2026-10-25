package asfell.luminousfx.client;

/** Client-side parameters used to generate the enchant-glint resource pack. */
public final class GlintSettings {
    private static final String PREFIX = "glint.";
    public static final double DEFAULT_BRIGHTNESS = 1.0;
    public static final double DEFAULT_SPEED = 1.0;
    public static final double DEFAULT_DENSITY = 1.0;

    private GlintSettings() { }

    public static double brightness() {
        return clamp(LuminousFXConfig.getDouble(PREFIX + "brightness", DEFAULT_BRIGHTNESS), 0.1, 4.0);
    }

    public static double speed() {
        return clamp(LuminousFXConfig.getDouble(PREFIX + "speed", DEFAULT_SPEED), 0.0, 5.0);
    }

    public static double density() {
        return clamp(LuminousFXConfig.getDouble(PREFIX + "density", DEFAULT_DENSITY), 0.25, 4.0);
    }

    public static void setBrightness(double value) {
        LuminousFXConfig.setDouble(PREFIX + "brightness", clamp(value, 0.1, 4.0));
    }

    public static void setSpeed(double value) {
        LuminousFXConfig.setDouble(PREFIX + "speed", clamp(value, 0.0, 5.0));
    }

    public static void setDensity(double value) {
        LuminousFXConfig.setDouble(PREFIX + "density", clamp(value, 0.25, 4.0));
    }

    public static void reset() {
        LuminousFXConfig.setDouble(PREFIX + "brightness", DEFAULT_BRIGHTNESS);
        LuminousFXConfig.setDouble(PREFIX + "speed", DEFAULT_SPEED);
        LuminousFXConfig.setDouble(PREFIX + "density", DEFAULT_DENSITY);
        LuminousFXConfig.setInt("glint.palette", 0);
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }
}