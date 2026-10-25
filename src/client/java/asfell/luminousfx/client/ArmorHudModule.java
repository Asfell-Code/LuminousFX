package asfell.luminousfx.client;

import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;

public final class ArmorHudModule implements LuminousFXModule {
    private static final String ID = "armor_hud";
    private static final String VERTICAL_KEY = ID + ".vertical";
    private static final String X_OFFSET_KEY = ID + ".x_offset";
    private static final String Y_OFFSET_KEY = ID + ".y_offset";
    private static final String POINTS_KEY = ID + ".show_durability_points";
    private static final String VISIBLE_KEY = ID + ".visible";
    private static final String WARNING_KEY = ID + ".show_warnings";
    private static final ResourceLocation SLOT_BACKGROUND = ResourceLocation.parse(
            "minecraft:textures/gui/sprites/hud/hotbar_offhand_left.png");
    private static final int BOX_SIZE = 20;
    private static final int SPACING = 2;
    private static final int X_OFFSET = -210;
    private static final int Y_OFFSET = -20;
    private static final float WARNING_THRESHOLD = 0.25F;

    @Override
    public String id() {
        return ID;
    }

    @Override
    public String translationKey() {
        return "module.luminousfx.armor_hud";
    }

    @Override
    public boolean defaultEnabled() {
        return true;
    }

    @Override
    public void register() {
        HudRenderCallback.EVENT.register((graphics, ignored) -> render(graphics));
    }

    private void render(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null
                || minecraft.options.hideGui
                || !LuminousFXConfig.getBoolean(VISIBLE_KEY, true)
                || !LuminousFXConfig.isEnabled(ID, defaultEnabled())) {
            return;
        }
        drawArmorHud(graphics, minecraft);
    }

    private void drawArmorHud(GuiGraphics graphics, Minecraft minecraft) {
        var player = minecraft.player;
        if (player == null) {
            return;
        }
        var window = minecraft.getWindow();
        if (window == null) {
            return;
        }
        int screenWidth = window.getGuiScaledWidth();
        int screenHeight = window.getGuiScaledHeight();
        boolean vertical = LuminousFXConfig.getBoolean(VERTICAL_KEY, false);
        boolean showDurabilityPoints = LuminousFXConfig.getBoolean(POINTS_KEY, false);
        int slotStride = BOX_SIZE + SPACING;
        int xOffset = screenWidth / 2 + LuminousFXConfig.getInt(X_OFFSET_KEY, X_OFFSET);
        int yOffset = screenHeight + LuminousFXConfig.getInt(Y_OFFSET_KEY, Y_OFFSET);
        ItemStack[] armor = new ItemStack[4];
        int index = 0;
        for (ItemStack stack : player.getArmorSlots()) {
            armor[index++] = stack;
        }
        for (int slot = 0; slot < armor.length; slot++) {
            ItemStack stack = armor[slot];
            if (stack.isEmpty()) {
                continue;
            }
            int drawX = slotX(vertical, xOffset, slot, armor.length, slotStride);
            int drawY = slotY(vertical, yOffset, BOX_SIZE, slot, slotStride);
            drawSlotBackground(graphics, drawX, drawY);
            graphics.renderItem(stack, drawX + (BOX_SIZE - 16) / 2, drawY + (BOX_SIZE - 16) / 2);
            if (LuminousFXConfig.getBoolean(WARNING_KEY, true) && isDurabilityLow(stack)) {
                drawWarning(graphics, drawX, drawY);
            }
            if (showDurabilityPoints) {
                drawDurabilityPoints(graphics, drawX, drawY, stack);
            } else {
                graphics.pose().pushPose();
                graphics.pose().translate(0.0F, 0.0F, 200.0F);
                drawDurabilityBar(graphics, drawX, drawY + BOX_SIZE - 4, stack);
                graphics.pose().popPose();
            }
        }
    }

    private int slotX(boolean vertical, int xOffset, int slot, int count, int slotStride) {
        return vertical ? xOffset : xOffset + (count - 1 - slot) * slotStride;
    }

    private int slotY(boolean vertical, int yOffset, int boxSize, int slot, int slotStride) {
        return vertical ? yOffset - boxSize - slot * slotStride : yOffset;
    }

    private void drawSlotBackground(GuiGraphics graphics, int x, int y) {
        graphics.blit(SLOT_BACKGROUND, x - 2, y - 2, 0.0F, 0.0F, 23, 24, 29, 24);
    }

    private void drawDurabilityBar(GuiGraphics graphics, int x, int y, ItemStack stack) {
        int maxDamage = stack.getMaxDamage();
        int damage = stack.getDamageValue();
        if (maxDamage <= 0 || damage <= 0) {
            return;
        }
        int barWidth = 13;
        int barX = x + BOX_SIZE / 2 - barWidth / 2;
        float ratio = durabilityRatio(maxDamage, damage);
        int remainingWidth = Math.max(0, Math.min(barWidth, Math.round(ratio * barWidth)));
        graphics.fill(barX, y, barX + barWidth, y + 2, 0xFF000000);
        if (remainingWidth > 0) {
            graphics.fill(barX, y, barX + remainingWidth, y + 1, durabilityColor(ratio));
        }
    }

    private void drawDurabilityPoints(GuiGraphics graphics, int x, int y, ItemStack stack) {
        int maxDamage = stack.getMaxDamage();
        if (maxDamage <= 0) {
            return;
        }
        String display = String.valueOf(maxDamage - stack.getDamageValue());
        int textWidth = Minecraft.getInstance().font.width(display);
        graphics.drawString(Minecraft.getInstance().font, display, x + BOX_SIZE - 2 - textWidth, y + 2,
                0xFF202020, true);
    }

    private boolean isDurabilityLow(ItemStack stack) {
        return stack.getMaxDamage() > 0 && stack.getDamageValue() > 0
                && durabilityRatio(stack.getMaxDamage(), stack.getDamageValue()) < WARNING_THRESHOLD;
    }

    private void drawWarning(GuiGraphics graphics, int x, int y) {
        int bobbingOffset = (int) (Math.sin(System.currentTimeMillis() / 200.0D) * 2.0D);
        graphics.drawString(Minecraft.getInstance().font, Component.literal("!"), x - 1, y - 4 + bobbingOffset,
                0xFFFF5555, true);
    }

    private float durabilityRatio(int maxDamage, int damage) {
        if (maxDamage <= 0) {
            return 0.0F;
        }
        return Math.max(0.0F, Math.min(1.0F, (float) (maxDamage - damage) / maxDamage));
    }

    private int durabilityColor(float ratio) {
        return hsvToArgb(Math.max(0.0F, Math.min(1.0F, ratio)) * 120.0F, 1.0F, 1.0F);
    }

    private int hsvToArgb(float hue, float saturation, float value) {
        float normalizedHue = (hue % 360.0F + 360.0F) % 360.0F;
        float sector = normalizedHue / 60.0F;
        int index = (int) sector % 6;
        float fraction = sector - (int) sector;
        float p = value * (1.0F - saturation);
        float q = value * (1.0F - fraction * saturation);
        float t = value * (1.0F - (1.0F - fraction) * saturation);
        float red = 0.0F;
        float green = 0.0F;
        float blue = 0.0F;
        switch (index) {
            case 0 -> { red = value; green = t; blue = p; }
            case 1 -> { red = q; green = value; blue = p; }
            case 2 -> { red = p; green = value; blue = t; }
            case 3 -> { red = p; green = q; blue = value; }
            case 4 -> { red = t; green = p; blue = value; }
            case 5 -> { red = value; green = p; blue = q; }
            default -> { }
        }
        return 0xFF000000 | Math.round(red * 255.0F) << 16
                | Math.round(green * 255.0F) << 8 | Math.round(blue * 255.0F);
    }
}