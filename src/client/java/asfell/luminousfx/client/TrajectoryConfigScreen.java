package asfell.luminousfx.client;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

public final class TrajectoryConfigScreen extends Screen {
    private static final int PANEL_WIDTH = 720;
    private static final int PANEL_HEIGHT = 560;
    private final Screen parent;
    private String editingColorKey;
    private String colorInput = "";
    private boolean colorSelected;
    private String hoveredControl;

    public TrajectoryConfigScreen(Screen parent) {
        super(Component.literal("Настройки траектории"));
        this.parent = parent;
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xB8000000);
        float scale = uiScale();
        int virtualWidth = Math.max(PANEL_WIDTH, Math.round(width / scale));
        int virtualHeight = Math.max(PANEL_HEIGHT, Math.round(height / scale));
        int left = (virtualWidth - PANEL_WIDTH) / 2;
        int top = (virtualHeight - PANEL_HEIGHT) / 2;
        int virtualMouseX = Math.round((float) mouseX / scale);
        int virtualMouseY = Math.round((float) mouseY / scale);
        hoveredControl = controlAt(virtualMouseX, virtualMouseY, left, top);
        graphics.pose().pushPose();
        graphics.pose().scale(scale, scale, 1.0F);
        graphics.fill(left, top, left + PANEL_WIDTH, top + PANEL_HEIGHT, 0xFF151A22);
        graphics.fill(left, top, left + PANEL_WIDTH, top + 54, 0xFF202A36);
        graphics.drawString(font, "ТРАЕКТОРИЯ / НАСТРОЙКИ", left + 24, top + 20, 0xFFF4F7FB, false);
        graphics.drawString(font, "Щёлкните ПКМ по карточке модуля, чтобы вернуться", left + 24, top + 36, 0xFF8D9AAA, false);

        int x = left + 24;
        int y = top + 78;
        graphics.drawString(font, "ОТОБРАЖЕНИЕ", x, y, 0xFF65D4FF, false);
        drawSwitch(graphics, x, y + 18, "Моя траектория", "trajectory.show_self", true);
        drawSwitch(graphics, x, y + 46, "Другие игроки", "trajectory.show_others", true);
        drawSwitch(graphics, x, y + 74, "Мобы", "trajectory.show_mobs", false);

        int right = left + 365;
        graphics.drawString(font, "ЛИНИЯ", right, y, 0xFF65D4FF, false);
        drawValue(graphics, right, y + 20, "Стиль", styleName(), "style");
        drawValue(graphics, right, y + 48, "Толщина", LuminousFXConfig.getInt("trajectory.line_width", 2) + " пикс.", "width");
        drawValue(graphics, right, y + 76, "Цвет линии", hex(LuminousFXConfig.getInt("trajectory.line_color", 0x55D6FF)), "line_color");
        drawValue(graphics, right, y + 104, "Прозрачность", LuminousFXConfig.getInt("trajectory.line_alpha", 220) + "%", "line_alpha");

        int lower = top + 220;
        graphics.drawString(font, "МЕТКА ПОПАДАНИЯ", x, lower, 0xFFFFCA55, false);
        drawSwitch(graphics, x, lower + 18, "Показывать", "trajectory.marker", true);
        drawValue(graphics, x, lower + 46, "Форма", shapeName(), "shape");
        drawValue(graphics, x, lower + 74, "Размер", LuminousFXConfig.getInt("trajectory.marker_size", 4) + " пикс.", "size");
        drawValue(graphics, x, lower + 102, "Цвет метки", hex(LuminousFXConfig.getInt("trajectory.marker_color", 0xFFCA55)), "marker_color");
        drawValue(graphics, x, lower + 130, "Прозрачность метки", LuminousFXConfig.getInt("trajectory.marker_alpha", 255) + "%", "marker_alpha");

        graphics.drawString(font, "ФИЛЬТР ПРЕДМЕТОВ", right, lower, 0xFF65D4FF, false);
        drawSwitch(graphics, right, lower + 18, "Лук", "trajectory.bow", true);
        drawSwitch(graphics, right, lower + 46, "Арбалет", "trajectory.crossbow", true);
        drawSwitch(graphics, right, lower + 74, "Эндер-жемчуг", "trajectory.ender_pearl", true);
        drawSwitch(graphics, right, lower + 102, "Снежки / яйца", "trajectory.snowballs", true);
        drawSwitch(graphics, right, lower + 130, "Зелья", "trajectory.potions", true);
        drawSwitch(graphics, right, lower + 158, "Трезубец", "trajectory.trident", true);
        drawButton(graphics, left + 24, top + 450, 150, 24, "Сбросить", "reset");
        graphics.drawString(font, "ESC  закрыть", left + 24, top + PANEL_HEIGHT - 22, 0xFF8D9AAA, false);
        if (editingColorKey != null) {
            graphics.fill(left + 24, top + PANEL_HEIGHT - 58, left + 250, top + PANEL_HEIGHT - 34, 0xFF273444);
            graphics.fill(left + 32, top + PANEL_HEIGHT - 53, left + 32 + font.width("#" + colorInput),
                    top + PANEL_HEIGHT - 41, colorSelected ? 0xFF3D75B8 : 0xFF344558);
            graphics.drawString(font, "#" + colorInput, left + 32, top + PANEL_HEIGHT - 51, 0xFFFFFFFF, false);
            graphics.drawString(font, "ENTER — применить", left + 145, top + PANEL_HEIGHT - 51, 0xFFB8C7D8, false);
        }
        graphics.pose().popPose();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        float scale = uiScale();
        int virtualWidth = Math.max(PANEL_WIDTH, Math.round(width / scale));
        int virtualHeight = Math.max(PANEL_HEIGHT, Math.round(height / scale));
        int left = (virtualWidth - PANEL_WIDTH) / 2;
        int top = (virtualHeight - PANEL_HEIGHT) / 2;
        int x = Math.round((float) mouseX / scale);
        int y = Math.round((float) mouseY / scale);
        if (editingColorKey != null && !isColorField(x, y, left, top)) commitColorEdit();
        if (button != 0) return true;
        String control = controlAt(x, y, left, top);
        if (control == null) return true;
        switch (control) {
            case "self" -> flip("trajectory.show_self", true);
            case "others" -> flip("trajectory.show_others", true);
            case "mobs" -> flip("trajectory.show_mobs", false);
            case "style" -> cycle("trajectory.line_style", 3);
            case "width" -> step("trajectory.line_width", 1, 6);
            case "line_color" -> beginColorEdit("trajectory.line_color");
            case "line_alpha" -> stepAlpha("trajectory.line_alpha");
            case "marker" -> flip("trajectory.marker", true);
            case "shape" -> cycle("trajectory.marker_shape", 4);
            case "size" -> step("trajectory.marker_size", 1, 16);
            case "marker_color" -> beginColorEdit("trajectory.marker_color");
            case "marker_alpha" -> stepAlpha("trajectory.marker_alpha");
            case "bow" -> flip("trajectory.bow", true);
            case "crossbow" -> flip("trajectory.crossbow", true);
            case "pearl" -> flip("trajectory.ender_pearl", true);
            case "snowballs" -> flip("trajectory.snowballs", true);
            case "potions" -> flip("trajectory.potions", true);
            case "trident" -> flip("trajectory.trident", true);
            case "reset" -> resetDefaults();
            default -> { }
        }
        return true;
    }

    @Override
    public boolean charTyped(char codePoint, int modifiers) {
        if (editingColorKey != null && (isHex(codePoint) || codePoint == '#')) {
            if (colorSelected) {
                colorInput = "";
                colorSelected = false;
            }
            if (codePoint == '#') return true;
            if (colorInput.length() >= 6) return true;
            colorInput += Character.toUpperCase(codePoint);
            previewColor();
            return true;
        }
        return super.charTyped(codePoint, modifiers);
    }

    @Override
    public boolean keyPressed(int keyCode, int scanCode, int modifiers) {
        if (editingColorKey != null) {
            if (keyCode == 259 && colorSelected) {
                colorInput = "";
                colorSelected = false;
                previewColor();
                return true;
            }
            if (keyCode == 259 && !colorInput.isEmpty()) {
                colorInput = colorInput.substring(0, colorInput.length() - 1);
                previewColor();
                return true;
            }
            if (keyCode == 257) {
                commitColorEdit();
                return true;
            }
            if (keyCode == 256) {
                commitColorEdit();
                onClose();
                return true;
            }
        }
        if (keyCode == 256) {
            onClose();
            return true;
        }
        return super.keyPressed(keyCode, scanCode, modifiers);
    }

    private void beginColorEdit(String key) {
        editingColorKey = key;
        colorInput = hex(LuminousFXConfig.getInt(key, key.endsWith("line_color") ? 0x55D6FF : 0xFFCA55)).substring(1);
        colorSelected = true;
    }

    private boolean isHex(char value) {
        return value >= '0' && value <= '9' || value >= 'A' && value <= 'F' || value >= 'a' && value <= 'f';
    }

    private boolean isColorField(int x, int y, int left, int top) {
        return inside(x, y, left + 365, top + 144, 300, 30)
                || inside(x, y, left + 24, top + 312, 300, 30);
    }

    private String controlAt(int x, int y, int left, int top) {
        if (inside(x, y, left, top + 88, 300, 28)) return "self";
        if (inside(x, y, left, top + 116, 300, 28)) return "others";
        if (inside(x, y, left, top + 144, 300, 28)) return "mobs";
        if (inside(x, y, left + 341, top + 88, 355, 28)) return "style";
        if (inside(x, y, left + 341, top + 116, 355, 28)) return "width";
        if (inside(x, y, left + 341, top + 144, 355, 28)) return "line_color";
        if (inside(x, y, left + 341, top + 172, 355, 28)) return "line_alpha";
        if (inside(x, y, left, top + 230, 300, 28)) return "marker";
        if (inside(x, y, left, top + 258, 300, 28)) return "shape";
        if (inside(x, y, left, top + 286, 300, 28)) return "size";
        if (inside(x, y, left, top + 314, 300, 28)) return "marker_color";
        if (inside(x, y, left, top + 342, 300, 28)) return "marker_alpha";
        if (inside(x, y, left + 341, top + 230, 355, 28)) return "bow";
        if (inside(x, y, left + 341, top + 258, 355, 28)) return "crossbow";
        if (inside(x, y, left + 341, top + 286, 355, 28)) return "pearl";
        if (inside(x, y, left + 341, top + 314, 355, 28)) return "snowballs";
        if (inside(x, y, left + 341, top + 342, 355, 28)) return "potions";
        if (inside(x, y, left + 341, top + 370, 355, 28)) return "trident";
        if (inside(x, y, left + 24, top + 450, 150, 24)) return "reset";
        return null;
    }

    private boolean inside(int x, int y, int left, int top, int width, int height) {
        return x >= left && x <= left + width && y >= top && y <= top + height;
    }

    private void previewColor() {
        if (editingColorKey != null) {
            LuminousFXConfig.setInt(editingColorKey, colorInput.isEmpty() ? 0 : parseColor(colorInput));
        }
    }

    private void commitColorEdit() {
        if (editingColorKey == null) return;
        previewColor();
        editingColorKey = null;
        colorInput = "";
        colorSelected = false;
    }

    private int parseColor(String value) {
        String normalized = (value + "000000").substring(0, 6);
        return Integer.parseInt(normalized, 16);
    }

    private float uiScale() {
        return Math.max(0.1F, Math.min(1.0F, Math.min((width - 24.0F) / PANEL_WIDTH, (height - 24.0F) / PANEL_HEIGHT)));
    }
    private void flip(String key, boolean defaultValue) { LuminousFXConfig.setBoolean(key, !LuminousFXConfig.getBoolean(key, defaultValue)); }
    private void step(String key, int min, int max) { LuminousFXConfig.setInt(key, LuminousFXConfig.getInt(key, min) >= max ? min : LuminousFXConfig.getInt(key, min) + 1); }
    private void stepAlpha(String key) { LuminousFXConfig.setInt(key, LuminousFXConfig.getInt(key, 255) < 30 ? 255 : LuminousFXConfig.getInt(key, 255) - 15); }
    private void cycle(String key, int count) { LuminousFXConfig.setInt(key, (LuminousFXConfig.getInt(key, 0) + 1) % count); }

    private void resetDefaults() {
        LuminousFXConfig.setBoolean("trajectory", true);
        LuminousFXConfig.setBoolean("trajectory.show_self", true);
        LuminousFXConfig.setBoolean("trajectory.show_others", true);
        LuminousFXConfig.setBoolean("trajectory.show_mobs", false);
        LuminousFXConfig.setInt("trajectory.steps", 80);
        LuminousFXConfig.setInt("trajectory.line_style", 0);
        LuminousFXConfig.setInt("trajectory.line_width", 2);
        LuminousFXConfig.setInt("trajectory.line_color", 0x55D6FF);
        LuminousFXConfig.setInt("trajectory.line_alpha", 220);
        LuminousFXConfig.setBoolean("trajectory.marker", true);
        LuminousFXConfig.setInt("trajectory.marker_shape", 0);
        LuminousFXConfig.setInt("trajectory.marker_size", 4);
        LuminousFXConfig.setInt("trajectory.marker_color", 0xFFCA55);
        LuminousFXConfig.setInt("trajectory.marker_alpha", 255);
        LuminousFXConfig.setBoolean("trajectory.bow", true);
        LuminousFXConfig.setBoolean("trajectory.crossbow", true);
        LuminousFXConfig.setBoolean("trajectory.ender_pearl", true);
        LuminousFXConfig.setBoolean("trajectory.snowballs", true);
        LuminousFXConfig.setBoolean("trajectory.potions", true);
        LuminousFXConfig.setBoolean("trajectory.trident", true);
        editingColorKey = null;
        colorInput = "";
        colorSelected = false;
    }

    private void drawSwitch(GuiGraphics g, int x, int y, String label, String key, boolean defaultValue) {
        boolean enabled = LuminousFXConfig.getBoolean(key, defaultValue);
        drawHover(g, x, y - 2, 300, 28, switch (key) {
            case "trajectory.show_self" -> "self";
            case "trajectory.show_others" -> "others";
            case "trajectory.show_mobs" -> "mobs";
            case "trajectory.marker" -> "marker";
            case "trajectory.bow" -> "bow";
            case "trajectory.crossbow" -> "crossbow";
            case "trajectory.ender_pearl" -> "pearl";
            case "trajectory.snowballs" -> "snowballs";
            case "trajectory.potions" -> "potions";
            case "trajectory.trident" -> "trident";
            default -> key;
        });
        g.drawString(font, label, x, y + 5, 0xFFE7ECF3, false);
        g.fill(x + 240, y, x + 274, y + 18, enabled ? 0xFF328FD1 : 0xFF3B4653);
        g.fill(x + (enabled ? 258 : 244), y + 4, x + (enabled ? 269 : 255), y + 14, 0xFFF6F8FB);
    }

    private void drawValue(GuiGraphics g, int x, int y, String label, String value, String control) {
        drawHover(g, x - 24, y - 2, 355, 28, control);
        g.drawString(font, label, x, y + 5, 0xFFE7ECF3, false);
        g.drawString(font, value + "  >", x + 180, y + 5, 0xFF9DDFFF, false);
    }

    private void drawButton(GuiGraphics g, int x, int y, int width, int height, String label, String control) {
        drawHover(g, x, y, width, height, control);
        g.fill(x, y, x + width, y + height, 0xFF273444);
        g.drawString(font, label, x + 12, y + 8, 0xFFE7ECF3, false);
    }

    private void drawHover(GuiGraphics g, int x, int y, int width, int height, String control) {
        if (control.equals(hoveredControl) || (control.equals("ender_pearl") && hoveredControl.equals("pearl"))) {
            g.fill(x, y, x + width, y + height, 0x263D75B8);
        }
    }

    private String styleName() { return switch (LuminousFXConfig.getInt("trajectory.line_style", 0)) { case 1 -> "Пунктир"; case 2 -> "Градиент"; default -> "Сплошная"; }; }
    private String shapeName() { return switch (LuminousFXConfig.getInt("trajectory.marker_shape", 0)) { case 1 -> "Сфера"; case 2 -> "Кольцо"; case 3 -> "Крест"; default -> "Точка"; }; }
    private String hex(int color) { return String.format("#%06X", color & 0xFFFFFF); }

    @Override
    public void onClose() { minecraft.setScreen(parent); }
}