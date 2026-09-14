package asfell.luminousfx.client;

import org.lwjgl.glfw.GLFW;

import com.mojang.blaze3d.platform.InputConstants;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.item.ItemStack;

public final class LuminousFXClient implements ClientModInitializer {
	private static final int NIGHT_VISION_DURATION = 300;
	private static final int ARMOR_HUD_SHIFT_LEFT = 12;
	private static KeyMapping nightVisionKey;
	private static boolean nightVisionEnabled = true;

	@Override
	public void onInitializeClient() {
		nightVisionKey = KeyBindingHelper.registerKeyBinding(new KeyMapping(
				"key.luminousfx.night_vision", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_RIGHT_SHIFT,
				"key.categories.luminousfx"));
		HudRenderCallback.EVENT.register((graphics, ignored) -> renderArmorHud(graphics));
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

	private static void renderArmorHud(GuiGraphics graphics) {
		Minecraft minecraft = Minecraft.getInstance();
		if (minecraft.player == null || minecraft.level == null) {
			return;
		}

		int left = minecraft.getWindow().getGuiScaledWidth() / 2 - 91 - 44 - ARMOR_HUD_SHIFT_LEFT;
		int top = minecraft.getWindow().getGuiScaledHeight() - 22 - 4 * 18;
		ItemStack[] armor = new ItemStack[4];
		int index = 0;
		for (ItemStack stack : minecraft.player.getArmorSlots()) {
			if (index < armor.length) {
				armor[index++] = stack;
			}
		}

		for (int slot = 0; slot < armor.length; slot++) {
			ItemStack stack = armor[armor.length - 1 - slot];
			if (stack == null || stack.isEmpty()) {
				continue;
			}

			int y = top + slot * 18;
			int percent = durabilityPercent(stack);
			graphics.renderItem(stack, left, y);
			graphics.fill(left + 18, y + 14, left + 48, y + 17, 0xFF303640);
			graphics.fill(left + 18, y + 14, left + 18 + Math.max(1, percent * 30 / 100), y + 17,
					0xFF59E391);
		}
	}

	private static int durabilityPercent(ItemStack stack) {
		if (!stack.isDamageableItem()) {
			return 0;
		}
		return Math.max(0, (stack.getMaxDamage() - stack.getDamageValue()) * 100 / stack.getMaxDamage());
	}
}