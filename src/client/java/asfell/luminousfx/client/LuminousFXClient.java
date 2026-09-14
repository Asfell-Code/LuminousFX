package asfell.luminousfx.client;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.minecraft.client.KeyMapping;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;

public final class LuminousFXClient implements ClientModInitializer {
	private static final int NIGHT_VISION_DURATION = 300;
	private static KeyMapping nightVisionKey;
	private static boolean nightVisionEnabled = true;

	@Override
	public void onInitializeClient() {
		nightVisionKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.luminousfx.night_vision", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT,
				"key.categories.luminousfx"));
		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			while (nightVisionKey.consumeClick()) {
				nightVisionEnabled = !nightVisionEnabled;
			}
			if (client.player != null && nightVisionEnabled) {
				client.player.addEffect(new MobEffectInstance(
						MobEffects.NIGHT_VISION, NIGHT_VISION_DURATION, 0, false, false, false));
			}
		});
	}
}