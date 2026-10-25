package asfell.luminousfx.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class ArmorHudConfigScreen extends Screen {
    private static final int PANEL_WIDTH = 300;
    private static final int PANEL_HEIGHT = 286;
    private static final int BUTTON_WIDTH = 244;
    private static final int BUTTON_HEIGHT = 20;
    private static final int LEFT = 28;
    private static final int X_BUTTON_Y = 158;
    private static final int Y_BUTTON_Y = 206;
    private static final int BUTTON_GAP = 8;
    private static final String VERTICAL_KEY = "armor_hud.vertical";
    private static final String X_OFFSET_KEY = "armor_hud.x_offset";
    private static final String Y_OFFSET_KEY = "armor_hud.y_offset";
    private static final String POINTS_KEY = "armor_hud.show_durability_points";
    private static final String VISIBLE_KEY = "armor_hud.visible";
    private static final String WARNING_KEY = "armor_hud.show_warnings";
    private final Screen parent;

    public ArmorHudConfigScreen(Screen parent) {
        super(Component.literal("Armor HUD Settings"));
        this.parent = parent;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xA8000000);
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xFF14171D);
        graphics.drawString(font, "ARMOR HUD", left + LEFT, top + 18, 0xFFF3F6FB, false);

        int buttonY = top + 38;
        drawButton(graphics, left + LEFT, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT,
            "HUD: " + onOff(VISIBLE_KEY, true));
        buttonY += 24;
        drawButton(graphics, left + LEFT, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT,
            "Warnings: " + onOff(WARNING_KEY, true));
        buttonY += 24;
        drawButton(graphics, left + LEFT, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT,
                LuminousFXConfig.getBoolean(VERTICAL_KEY, false)
                        ? "Orientation: Vertical" : "Orientation: Horizontal");
        buttonY += 24;
        drawButton(graphics, left + LEFT, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT,
                LuminousFXConfig.getBoolean(POINTS_KEY, false)
                        ? "Durability: Points" : "Durability: Bar");

        graphics.drawString(font, "X: " + LuminousFXConfig.getInt(X_OFFSET_KEY, -210), left + LEFT, top + 142,
                0xFFF0F3F8, false);
        drawButton(graphics, left + LEFT, top + X_BUTTON_Y, 118, BUTTON_HEIGHT, "X - 10");
        drawButton(graphics, left + LEFT + 118 + BUTTON_GAP, top + X_BUTTON_Y,
            118, BUTTON_HEIGHT, "X + 10");

        graphics.drawString(font, "Y: " + LuminousFXConfig.getInt(Y_OFFSET_KEY, -20), left + LEFT, top + 190,
                0xFFF0F3F8, false);
        drawButton(graphics, left + LEFT, top + Y_BUTTON_Y, 118, BUTTON_HEIGHT, "Y - 10");
        drawButton(graphics, left + LEFT + 118 + BUTTON_GAP, top + Y_BUTTON_Y,
            118, BUTTON_HEIGHT, "Y + 10");

        drawButton(graphics, left + LEFT, top + 242, 118, BUTTON_HEIGHT, "Reset");
        drawButton(graphics, left + LEFT + 126, top + 242, 118, BUTTON_HEIGHT, "Done");
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 && button != 1) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        if (inside(mouseX, mouseY, left + LEFT, top + 38, BUTTON_WIDTH, BUTTON_HEIGHT)) {
            LuminousFXConfig.setBoolean(VISIBLE_KEY,
                    !LuminousFXConfig.getBoolean(VISIBLE_KEY, true));
            return true;
        }
        if (inside(mouseX, mouseY, left + LEFT, top + 62, BUTTON_WIDTH, BUTTON_HEIGHT)) {
            LuminousFXConfig.setBoolean(WARNING_KEY,
                    !LuminousFXConfig.getBoolean(WARNING_KEY, true));
            return true;
        }
        if (inside(mouseX, mouseY, left + LEFT, top + 86, BUTTON_WIDTH, BUTTON_HEIGHT)) {
            LuminousFXConfig.setBoolean(VERTICAL_KEY, !LuminousFXConfig.getBoolean(VERTICAL_KEY, false));
            return true;
        }
        if (inside(mouseX, mouseY, left + LEFT, top + 110, BUTTON_WIDTH, BUTTON_HEIGHT)) {
            LuminousFXConfig.setBoolean(POINTS_KEY, !LuminousFXConfig.getBoolean(POINTS_KEY, false));
            return true;
        }
        if (inside(mouseX, mouseY, left + LEFT, top + X_BUTTON_Y, 118, BUTTON_HEIGHT)) {
            changeInt(X_OFFSET_KEY, button == 1 ? 10 : -10, -300, 300);
            return true;
        }
        if (inside(mouseX, mouseY, left + LEFT + 118 + BUTTON_GAP, top + X_BUTTON_Y,
                118, BUTTON_HEIGHT)) {
            changeInt(X_OFFSET_KEY, button == 1 ? -10 : 10, -300, 300);
            return true;
        }
        if (inside(mouseX, mouseY, left + LEFT, top + Y_BUTTON_Y, 118, BUTTON_HEIGHT)) {
            changeInt(Y_OFFSET_KEY, button == 1 ? 10 : -10, -300, 100);
            return true;
        }
        if (inside(mouseX, mouseY, left + LEFT + 118 + BUTTON_GAP, top + Y_BUTTON_Y,
                118, BUTTON_HEIGHT)) {
            changeInt(Y_OFFSET_KEY, button == 1 ? -10 : 10, -300, 100);
            return true;
        }
        if (inside(mouseX, mouseY, left + LEFT, top + 242, 118, BUTTON_HEIGHT)) {
            LuminousFXConfig.setBoolean("armor_hud", true);
            LuminousFXConfig.setBoolean(VISIBLE_KEY, true);
            LuminousFXConfig.setBoolean(WARNING_KEY, true);
            LuminousFXConfig.setBoolean(VERTICAL_KEY, false);
            LuminousFXConfig.setBoolean(POINTS_KEY, false);
            LuminousFXConfig.setInt(X_OFFSET_KEY, -210);
            LuminousFXConfig.setInt(Y_OFFSET_KEY, -20);
            return true;
        }
        if (inside(mouseX, mouseY, left + LEFT + 126, top + 242, 118, BUTTON_HEIGHT)) {
            onClose();
            return true;
        }
        return true;
    }

    @Override
    public void onClose() {
        minecraft.setScreen(parent);
    }

    private void changeInt(String key, int amount, int min, int max) {
        int current = LuminousFXConfig.getInt(key, 0);
        LuminousFXConfig.setInt(key, Math.max(min, Math.min(max, current + amount)));
    }

    private String onOff(String key, boolean defaultValue) {
        return LuminousFXConfig.getBoolean(key, defaultValue) ? "ON" : "OFF";
    }

    private boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private void drawButton(GuiGraphics graphics, int x, int y, int width, int height, String label) {
        graphics.fill(x, y, x + width, y + height, 0xFF2B3443);
        int textWidth = font.width(label);
        int textX = x + (width - textWidth) / 2;
        int textY = y + (height - font.lineHeight) / 2;
        graphics.drawString(font, label, textX, textY, 0xFFF0F3F8, false);
    }
}
