package asfell.luminousfx.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class NightVisionConfigScreen extends Screen {
    private static final int PANEL_WIDTH = 380;
    private static final int PANEL_HEIGHT = 230;
    private final Screen parent;
    private double gammaValue;
    private boolean draggingSlider;

    public NightVisionConfigScreen(Screen parent) {
        super(Component.translatable("screen.luminousfx.night_vision.title"));
        this.parent = parent;
        this.gammaValue = NightVisionModule.getConfiguredGamma();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xB8000000);
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xFF151A22);
        graphics.fill(left, top, left + PANEL_WIDTH, top + 42, 0xFF202A36);
        graphics.drawString(font, Component.translatable("screen.luminousfx.night_vision.title"),
                left + 22, top + 16, 0xFFF4F7FB, false);

        boolean enabled = LuminousFXConfig.getBoolean("night_vision", false);
        drawButton(graphics, left + 24, top + 58, PANEL_WIDTH - 48, 26,
                Component.translatable(enabled ? "screen.luminousfx.night_vision.enabled"
                        : "screen.luminousfx.night_vision.disabled").getString(), enabled);

        graphics.drawString(font, Component.translatable("screen.luminousfx.night_vision.gamma",
            Math.round(gammaValue * 100)), left + 24, top + 101, 0xFFF0F3F8, false);
        int sliderLeft = left + 30;
        int sliderRight = left + PANEL_WIDTH - 30;
        int sliderY = top + 126;
        graphics.fill(sliderLeft, sliderY, sliderRight, sliderY + 6, 0xFF424A57);
        int knobX = sliderLeft + (int) ((sliderRight - sliderLeft) * (gammaValue - 1.0) / 14.0);
        graphics.fill(sliderLeft, sliderY, knobX, sliderY + 6, 0xFF4D9DFF);
        graphics.fill(knobX - 5, sliderY - 4, knobX + 5, sliderY + 10, 0xFFF5F8FC);
        graphics.drawString(font, "100%", sliderLeft, sliderY + 16, 0xFF9DA6B5, false);
        String maximum = "1500%";
        graphics.drawString(font, maximum, sliderRight - font.width(maximum), sliderY + 16, 0xFF9DA6B5, false);

        drawButton(graphics, left + 24, top + 184, 150, 24,
                Component.translatable("screen.luminousfx.night_vision.reset").getString(), false);
        drawButton(graphics, left + PANEL_WIDTH - 174, top + 184, 150, 24,
                Component.translatable("gui.done").getString(), false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) {
            return super.mouseClicked(mouseX, mouseY, button);
        }
        int left = (width - PANEL_WIDTH) / 2;
        int top = (height - PANEL_HEIGHT) / 2;
        if (inside(mouseX, mouseY, left + 24, top + 58, PANEL_WIDTH - 48, 26)) {
            boolean enabled = LuminousFXConfig.getBoolean("night_vision", false);
            LuminousFXConfig.setBoolean("night_vision", !enabled);
            return true;
        }
        if (inside(mouseX, mouseY, left + 25, top + 118, PANEL_WIDTH - 50, 20)) {
            draggingSlider = true;
            updateSlider(mouseX, left);
            return true;
        }
        if (inside(mouseX, mouseY, left + 24, top + 184, 150, 24)) {
            gammaValue = 10.0;
            NightVisionModule.setConfiguredGamma(gammaValue);
            return true;
        }
        if (inside(mouseX, mouseY, left + PANEL_WIDTH - 174, top + 184, 150, 24)) {
            onClose();
            return true;
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (draggingSlider && button == 0) {
            updateSlider(mouseX, (width - PANEL_WIDTH) / 2);
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        if (draggingSlider) {
            draggingSlider = false;
            LuminousFXConfig.save();
            return true;
        }
        return super.mouseReleased(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    @Override
    public void onClose() {
        draggingSlider = false;
        NightVisionModule.setConfiguredGamma(gammaValue);
        minecraft.setScreen(parent);
    }

    private void updateSlider(double mouseX, int left) {
        int sliderLeft = left + 30;
        int sliderWidth = PANEL_WIDTH - 60;
        double ratio = Math.max(0.0, Math.min(1.0, (mouseX - sliderLeft) / sliderWidth));
        gammaValue = 1.0 + Math.round(ratio * 140.0) / 10.0;
        NightVisionModule.previewConfiguredGamma(gammaValue);
    }

    private boolean inside(double mouseX, double mouseY, int x, int y, int width, int height) {
        return mouseX >= x && mouseX <= x + width && mouseY >= y && mouseY <= y + height;
    }

    private void drawButton(GuiGraphics graphics, int x, int y, int width, int height,
            String label, boolean active) {
        graphics.fill(x, y, x + width, y + height, active ? 0xFF347FD5 : 0xFF2B3443);
        int textX = x + (width - font.width(label)) / 2;
        int textY = y + (height - font.lineHeight) / 2;
        graphics.drawString(font, label, textX, textY, 0xFFF0F3F8, false);
    }
}