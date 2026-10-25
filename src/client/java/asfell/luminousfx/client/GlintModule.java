package asfell.luminousfx.client;

public final class GlintModule implements LuminousFXModule {
    @Override
    public String id() {
        return "glint";
    }

    @Override
    public String translationKey() {
        return "module.luminousfx.glint";
    }

    @Override
    public boolean defaultEnabled() {
        return true;
    }

    @Override
    public void register() {
        // Glint customization is applied by generating a vanilla resource pack from the selected PNGs.
    }
}