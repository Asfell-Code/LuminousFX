package asfell.luminousfx.client;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public class LuminousFXClient implements ClientModInitializer {
	private static KeyMapping clickGuiKey;

	@Override
	public void onInitializeClient() {
		LuminousFXConfig.load();
		CrystalPvPOverlay.register();
		clickGuiKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.luminousfx.clickgui", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT,
				"key.categories.luminousfx"));
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (client.player != null && LuminousFXConfig.isEnabled("Night Vision")) {
				client.player.addEffect(new MobEffectInstance(MobEffects.NIGHT_VISION, 300, 0, false, false, false));
			}
			while (clickGuiKey.consumeClick()) {
				if (client.screen == null) {
					client.setScreen(new ClickGuiScreen());
				}
			}
		});
	}
}