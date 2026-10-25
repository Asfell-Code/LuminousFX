package asfell.luminousfx.client;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;

public class LuminousFXClient implements ClientModInitializer {
	private static KeyMapping clickGuiKey;
	private static KeyMapping nightVisionToggleKey;

	@Override
	public void onInitializeClient() {
		LuminousFXConfig.load();
		GlintResourcePack.initialize();
		LuminousFXModules.register();
		clickGuiKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.luminousfx.clickgui", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT,
				"key.categories.luminousfx"));
		nightVisionToggleKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.luminousfx.night_vision_toggle", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_N,
				"key.categories.luminousfx"));
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (clickGuiKey.consumeClick()) {
				if (client.screen == null) {
					client.setScreen(new ClickGuiScreen());
				}
			}
			while (nightVisionToggleKey.consumeClick()) {
				boolean enabled = LuminousFXConfig.getBoolean("night_vision", false);
				LuminousFXConfig.setBoolean("night_vision", !enabled);
			}
		});
	}
}