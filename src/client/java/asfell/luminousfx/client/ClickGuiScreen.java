package asfell.luminousfx.client;

import java.util.List;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public class ClickGuiScreen extends Screen {
    private static final int UI_WIDTH = 900;
    private static final int UI_HEIGHT = 700;
    private static final int PANEL_WIDTH = 760;
    private static final int PANEL_HEIGHT = 620;
    private static final int SIDEBAR_WIDTH = 190;
    private static final int CARD_HEIGHT = 62;
    private static final int CARD_GAP = 12;
    private static final String[] CATEGORIES = {"category.luminousfx.hud"};

    private int selectedCategory;

    public ClickGuiScreen() {
        super(Component.literal("LuminousFX ClickGUI"));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        float scale = Math.min(this.width / (float) UI_WIDTH, this.height / (float) UI_HEIGHT);
        int offsetX = (int) ((this.width - UI_WIDTH * scale) / 2.0F);
        int offsetY = (int) ((this.height - UI_HEIGHT * scale) / 2.0F);
        graphics.fill(0, 0, this.width, this.height, 0xA8000000);
        graphics.pose().pushPose();
        graphics.pose().translate(offsetX, offsetY, 0.0F);
        graphics.pose().scale(scale, scale, 1.0F);
        renderUi(graphics);
        graphics.pose().popPose();
    }

    private void renderUi(GuiGraphics graphics) {
        int left = (UI_WIDTH - PANEL_WIDTH) / 2;
        int top = (UI_HEIGHT - PANEL_HEIGHT) / 2;
        fillRounded(graphics, left, top, PANEL_WIDTH, PANEL_HEIGHT, 14, 0xFF14171D);
        fillRounded(graphics, left, top, SIDEBAR_WIDTH, PANEL_HEIGHT, 14, 0xFF1B1F27);
        graphics.fill(left + SIDEBAR_WIDTH - 14, top, left + SIDEBAR_WIDTH, top + PANEL_HEIGHT, 0xFF1B1F27);
        graphics.drawString(this.font, "LUMINOUSFX", left + 28, top + 30, 0xFFF3F6FB, false);
        graphics.drawString(this.font, Component.translatable("screen.luminousfx.subtitle"), left + 28, top + 47, 0xFF778192, false);
        for (int index = 0; index < CATEGORIES.length; index++) {
            int itemTop = top + 92 + index * 54;
            boolean active = selectedCategory == index;
            if (active) {
                fillRounded(graphics, left + 14, itemTop - 10, SIDEBAR_WIDTH - 28, 40, 8, 0xFF2B3443);
                graphics.fill(left + 14, itemTop - 2, left + 17, itemTop + 22, 0xFF4D9DFF);
            }
            drawCategoryIcon(graphics, left + 31, itemTop + 2, index, active ? 0xFF63AAFF : 0xFF7C8798);
            graphics.drawString(this.font, Component.translatable(CATEGORIES[index]), left + 54, itemTop + 1,
                    active ? 0xFFFFFFFF : 0xFF9DA6B5, false);
        }
        int contentLeft = left + SIDEBAR_WIDTH + 30;
        graphics.drawString(this.font, Component.translatable(CATEGORIES[selectedCategory]), contentLeft, top + 31, 0xFFF3F6FB, false);
        graphics.drawString(this.font, Component.translatable("screen.luminousfx.modules"), contentLeft, top + 50, 0xFF778192, false);
        List<LuminousFXModule> modules = LuminousFXModules.all();
        for (int index = 0; index < modules.size(); index++) {
            int cardLeft = contentLeft + index % 2 * 268;
            int cardTop = top + 78 + index / 2 * (CARD_HEIGHT + CARD_GAP);
            LuminousFXModule module = modules.get(index);
            fillRounded(graphics, cardLeft, cardTop, 254, CARD_HEIGHT, 9, 0xFF202631);
            graphics.drawString(this.font, Component.translatable(module.translationKey()), cardLeft + 17, cardTop + 19, 0xFFF0F3F8, false);
            if (module.id().equals("armor_hud") || module.id().equals("night_vision")
                    || module.id().equals("trajectory") || module.id().equals("glint")) {
                graphics.drawString(this.font, Component.literal("⚙"), cardLeft + 166, cardTop + 19, 0xFFB8C2D1, false);
            }
            if (module.id().equals("glint")) {
                fillRounded(graphics, cardLeft + 191, cardTop + 18, 53, 24, 7, 0xFF347FC2);
                graphics.drawCenteredString(font, "SET", cardLeft + 217, cardTop + 26, 0xFFF5F8FC);
            } else {
                drawToggle(graphics, cardLeft + 198, cardTop + 18,
                        LuminousFXConfig.getBoolean(module.id(), module.defaultEnabled()));
            }
        }
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0 && button != 1) return super.mouseClicked(mouseX, mouseY, button);
        float scale = Math.min(this.width / (float) UI_WIDTH, this.height / (float) UI_HEIGHT);
        int offsetX = (int) ((this.width - UI_WIDTH * scale) / 2.0F);
        int offsetY = (int) ((this.height - UI_HEIGHT * scale) / 2.0F);
        int x = Math.round((float) (mouseX - offsetX) / scale);
        int y = Math.round((float) (mouseY - offsetY) / scale);
        int left = (UI_WIDTH - PANEL_WIDTH) / 2;
        int top = (UI_HEIGHT - PANEL_HEIGHT) / 2;
        for (int index = 0; index < CATEGORIES.length; index++) {
            int itemTop = top + 82 + index * 54;
            if (x >= left + 14 && x <= left + SIDEBAR_WIDTH - 14 && y >= itemTop && y <= itemTop + 40) {
                selectedCategory = index;
                return true;
            }
        }
        int contentLeft = left + SIDEBAR_WIDTH + 30;
        List<LuminousFXModule> modules = LuminousFXModules.all();
        for (int index = 0; index < modules.size(); index++) {
            int cardLeft = contentLeft + index % 2 * 268;
            int cardTop = top + 78 + index / 2 * (CARD_HEIGHT + CARD_GAP);
            if (x < cardLeft || x > cardLeft + 254 || y < cardTop || y > cardTop + CARD_HEIGHT) continue;
            LuminousFXModule module = modules.get(index);
            if (button == 1) {
                Screen configScreen = switch (module.id()) {
                    case "glint" -> new GlintConfigScreen();
                    case "night_vision" -> new NightVisionConfigScreen(this);
                    case "trajectory" -> new TrajectoryConfigScreen(this);
                    case "armor_hud" -> new ArmorHudConfigScreen(this);
                    default -> null;
                };
                if (configScreen != null) minecraft.setScreen(configScreen);
                return true;
            }
            if (module.id().equals("night_vision")) {
                boolean enabled = LuminousFXConfig.getBoolean(module.id(), module.defaultEnabled());
                LuminousFXConfig.setBoolean(module.id(), !enabled);
                return true;
            }
            if ((module.id().equals("armor_hud") || module.id().equals("trajectory")
                    ) && (x >= cardLeft + 154 && x <= cardLeft + 192)) {
                Screen configScreen = switch (module.id()) {
                    case "trajectory" -> new TrajectoryConfigScreen(this);
                    default -> new ArmorHudConfigScreen(this);
                };
                minecraft.setScreen(configScreen);
                return true;
            }
            if (module.id().equals("glint") && x >= cardLeft + 191 && x <= cardLeft + 244) {
                minecraft.setScreen(new GlintConfigScreen());
                return true;
            }
            boolean enabled = LuminousFXConfig.getBoolean(module.id(), module.defaultEnabled());
            LuminousFXConfig.setBoolean(module.id(), !enabled);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (keyCode == 256) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void drawToggle(GuiGraphics graphics, int x, int y, boolean enabled) {
        fillRounded(graphics, x, y, 39, 24, 12, enabled ? 0xFF3E8FEF : 0xFF424A57);
        graphics.fill(x + (enabled ? 22 : 4), y + 4, x + (enabled ? 35 : 17), y + 17, 0xFFF5F8FC);
    }

    private void drawCategoryIcon(GuiGraphics graphics, int x, int y, int category, int color) {
        switch (category) {
            case 0 -> {
                graphics.fill(x - 7, y - 7, x + 7, y + 7, color);
                graphics.fill(x - 3, y - 3, x + 3, y + 3, 0xFF1B1F27);
            }
            case 1 -> {
                graphics.fill(x - 7, y - 2, x + 7, y + 2, color);
                graphics.fill(x - 2, y - 7, x + 2, y + 7, color);
            }
            default -> {
                graphics.fill(x - 7, y - 7, x + 7, y - 4, color);
                graphics.fill(x - 7, y - 1, x + 7, y + 2, color);
                graphics.fill(x - 7, y + 5, x + 7, y + 8, color);
            }
        }
    }

    private void fillRounded(GuiGraphics graphics, int x, int y, int width, int height, int radius, int color) {
        graphics.fill(x + radius, y, x + width - radius, y + height, color);
        graphics.fill(x, y + radius, x + width, y + height - radius, color);
        graphics.fill(x + 2, y + 2, x + radius, y + radius, color);
        graphics.fill(x + width - radius, y + 2, x + width - 2, y + radius, color);
        graphics.fill(x + 2, y + height - radius, x + radius, y + height - 2, color);
        graphics.fill(x + width - radius, y + height - radius, x + width - 2, y + height - 2, color);
    }
}
