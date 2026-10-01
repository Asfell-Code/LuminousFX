package asfell.luminousfx.client;

public interface LuminousFXModule {
    String id();

    String translationKey();

    boolean defaultEnabled();

    void register();
}