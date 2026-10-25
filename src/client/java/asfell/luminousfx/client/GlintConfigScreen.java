package asfell.luminousfx.client;

import java.util.Locale;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.gui.screens.inventory.InventoryScreen;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.decoration.ArmorStand;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

/** Palette picker and resource-pack based enchant glint customizer. */
public final class GlintConfigScreen extends Screen {
    private static final int UI_WIDTH = 960;
    private static final int UI_HEIGHT = 540;
    private static final String[] PALETTES = {"light", "lime", "no", "orange", "red", "yellow"};
    private static final int[] SWATCHES = {0xFF63B5FF, 0xFF9DFF45, 0xFF202020,
            0xFFFF963D, 0xFFFF4355, 0xFFFFE34F};
    private int selectedPalette;
    private double brightness;
    private double speed;
    private double density;
    private boolean draggingBrightness;
    private boolean draggingSpeed;
    private boolean draggingDensity;
    private String status = "";
    private ArmorStand previewStand;

    public GlintConfigScreen() {
        super(Component.literal("LuminousFX — enchant glint"));
        this.selectedPalette = Math.max(0, Math.min(PALETTES.length - 1,
                LuminousFXConfig.getInt("glint.palette", 0)));
        this.brightness = GlintSettings.brightness();
        this.speed = GlintSettings.speed();
        this.density = GlintSettings.density();
    }

    @Override
    protected void init() {
        previewStand = minecraft.level == null ? null : EntityType.ARMOR_STAND.create(minecraft.level);
        if (previewStand != null) {
            previewStand.setInvisible(false);
            previewStand.setNoGravity(true);
            previewStand.setNoBasePlate(true);
            previewStand.setShowArms(true);
            previewStand.setItemSlot(net.minecraft.world.entity.EquipmentSlot.HEAD, enchanted(Items.NETHERITE_HELMET.getDefaultInstance()));
            previewStand.setItemSlot(net.minecraft.world.entity.EquipmentSlot.CHEST, enchanted(Items.NETHERITE_CHESTPLATE.getDefaultInstance()));
            previewStand.setItemSlot(net.minecraft.world.entity.EquipmentSlot.LEGS, enchanted(Items.NETHERITE_LEGGINGS.getDefaultInstance()));
            previewStand.setItemSlot(net.minecraft.world.entity.EquipmentSlot.FEET, enchanted(Items.NETHERITE_BOOTS.getDefaultInstance()));
            previewStand.setItemSlot(net.minecraft.world.entity.EquipmentSlot.MAINHAND, enchanted(Items.NETHERITE_SWORD.getDefaultInstance()));
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xC9000000);
        float scale = Math.min(width / (float) UI_WIDTH, height / (float) UI_HEIGHT);
        int left = Math.round((width - UI_WIDTH * scale) / 2.0F);
        int top = Math.round((height - UI_HEIGHT * scale) / 2.0F);
        graphics.pose().pushPose();
        graphics.pose().translate(left, top, 0);
        graphics.pose().scale(scale, scale, 1.0F);
        drawInterface(graphics, mouseX, mouseY);
        graphics.pose().popPose();
    }

    private void drawInterface(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.fill(0, 0, UI_WIDTH, UI_HEIGHT, 0xFF10151D);
        border(graphics, 0, 0, UI_WIDTH, UI_HEIGHT, 0xFF435366);
        graphics.fill(1, 1, UI_WIDTH - 1, 48, 0xFF1B2531);
        graphics.drawString(font, "LUMINOUSFX  /  ENCHANT GLINT", 24, 18, 0xFFF2F6FC, false);
        graphics.drawString(font, "ТЕКСТУРНАЯ ПАЛИТРА · ПРЕДПРОСМОТР · ПАРАМЕТРЫ", 630, 20, 0xFF9EADBE, false);

        // Three fixed columns remain centered and are scaled down to fit small GUI sizes.
        panel(graphics, 18, 64, 236, 426, "ПАЛИТРА", 0xFF16202B);
        panel(graphics, 270, 64, 420, 426, "ПРЕДПРОСМОТР", 0xFF141C26);
        panel(graphics, 706, 64, 236, 426, "ПАРАМЕТРЫ", 0xFF16202B);

        for (int i = 0; i < PALETTES.length; i++) {
            int y = 114 + i * 52;
            boolean selected = selectedPalette == i;
            graphics.fill(34, y, 238, y + 42, selected ? 0xFF29394B : 0xFF1D2732);
            border(graphics, 34, y, 204, 42, selected ? 0xFF63B5FF : 0xFF354453);
            graphics.fill(46, y + 10, 68, y + 32, SWATCHES[i]);
            graphics.drawString(font, PALETTES[i].toUpperCase(Locale.ROOT), 80, y + 16,
                    selected ? 0xFFFFFFFF : 0xFFCBD5E0, false);
            if (selected) graphics.drawString(font, "✓", 216, y + 16, 0xFF83C7FF, false);
        }
        graphics.drawString(font, "Источник: " + GlintResourcePack.sourceFolderLabel(), 34, 438,
                0xFF8798AA, false);
        graphics.drawString(font, "Ожидаются пары PNG в палитрах", 34, 456,
                0xFF748496, false);

        // Armor-stand entity and enchanted inventory items form the live preview.
        graphics.fill(292, 96, 668, 458, 0xFF101720);
        if (previewStand != null) {
            Point virtualMouse = point(mouseX, mouseY);
            InventoryScreen.renderEntityInInventoryFollowsMouse(graphics, 390, 120, 575, 438,
                    112, 0.0F, virtualMouse.x, virtualMouse.y, previewStand);
        } else {
            drawFallbackPreview(graphics);
        }
        graphics.drawString(font, "Зачарованная незеритовая броня", 306, 442, 0xFFB9C8D8, false);

        graphics.drawString(font, "ЯРКОСТЬ", 728, 116, 0xFFE1E8F0, false);
        graphics.drawString(font, String.format(Locale.ROOT, "%.0f%%", brightness * 100), 877, 116,
                0xFF8BCBFF, false);
        drawSlider(graphics, 730, 146, 158, (brightness - 0.1) / 3.9);
        graphics.drawString(font, "10%", 730, 160, 0xFF8695A5, false);
        graphics.drawString(font, "400%", 856, 160, 0xFF8695A5, false);

        graphics.drawString(font, "СКОРОСТЬ", 728, 194, 0xFFE1E8F0, false);
        graphics.drawString(font, String.format(Locale.ROOT, "%.1f×", speed), 877, 194,
                0xFF8BCBFF, false);
        drawSlider(graphics, 730, 224, 158, speed / 5.0);
        graphics.drawString(font, "0×", 730, 238, 0xFF8695A5, false);
        graphics.drawString(font, "5×", 868, 238, 0xFF8695A5, false);

        graphics.drawString(font, "ПЛОТНОСТЬ УЗОРА", 728, 272, 0xFFE1E8F0, false);
        graphics.drawString(font, String.format(Locale.ROOT, "%.2f×", density), 866, 272,
            0xFF8BCBFF, false);
        drawSlider(graphics, 730, 302, 158, (density - 0.25) / 3.75);
        graphics.drawString(font, "0.25×", 730, 316, 0xFF8695A5, false);
        graphics.drawString(font, "4×", 868, 316, 0xFF8695A5, false);
        graphics.drawString(font, "Яркость и плотность меняют текстуру", 728, 358, 0xFF94A5B7, false);
        graphics.drawString(font, "без перезагрузки ресурсов", 728, 376, 0xFF94A5B7, false);

        graphics.fill(18, 492, 942, 493, 0xFF2C3947);
        button(graphics, 22, 498, 180, 30, "ВЫБРАТЬ ПАПКУ", false);
        button(graphics, 490, 498, 180, 30, "СБРОСИТЬ НАСТРОЙКИ", false);
        button(graphics, 720, 498, 218, 30, "ГОТОВО", true);
        graphics.drawString(font, status, 220, 509, status.startsWith("Ошибка") ? 0xFFFF7E86 : 0xFF91CFA7, false);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button != 0) return super.mouseClicked(mouseX, mouseY, button);
        Point p = point(mouseX, mouseY);
        for (int i = 0; i < PALETTES.length; i++) {
            int y = 114 + i * 52;
            if (inside(p, 34, y, 204, 42)) {
                selectedPalette = i;
                refreshPreviewTexture();
                return true;
            }
        }
        if (inside(p, 730, 136, 158, 24)) {
            draggingBrightness = true;
            updateSlider(p.x, 730, 158, 0.1, 4.0, true);
            refreshPreviewTexture();
            return true;
        }
        if (inside(p, 730, 214, 158, 24)) {
            draggingSpeed = true;
            updateSlider(p.x, 730, 158, 0.0, 5.0, false);
            refreshPreviewTexture();
            return true;
        }
        if (inside(p, 730, 292, 158, 24)) {
            draggingDensity = true;
            updateDensity(p.x);
            refreshPreviewTexture();
            return true;
        }
        if (inside(p, 22, 498, 180, 30)) {
            GlintResourcePack.chooseSourceFolder();
            status = "Папка с текстурами: " + GlintResourcePack.sourceFolderLabel();
            refreshPreviewTexture();
            return true;
        }
        if (inside(p, 490, 498, 180, 30)) {
            resetDefaults();
            return true;
        }
        if (inside(p, 720, 498, 218, 30)) {
            applyAndSave();
            minecraft.setScreen(null);
            return true;
        }
        return true;
    }

    @Override
    public boolean mouseDragged(double mouseX, double mouseY, int button, double dragX, double dragY) {
        if (button == 0 && (draggingBrightness || draggingSpeed || draggingDensity)) {
            Point p = point(mouseX, mouseY);
            if (draggingBrightness) updateSlider(p.x, 730, 158, 0.1, 4.0, true);
            if (draggingSpeed) updateSlider(p.x, 730, 158, 0.0, 5.0, false);
            if (draggingDensity) updateDensity(p.x);
            refreshPreviewTexture();
            return true;
        }
        return super.mouseDragged(mouseX, mouseY, button, dragX, dragY);
    }

    @Override
    public boolean mouseReleased(double mouseX, double mouseY, int button) {
        draggingBrightness = false;
        draggingSpeed = false;
        draggingDensity = false;
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
        minecraft.setScreen(null);
    }

    private void applyAndSave() {
        GlintSettings.setBrightness(brightness);
        GlintSettings.setSpeed(speed);
        GlintSettings.setDensity(density);
        LuminousFXConfig.setInt("glint.palette", selectedPalette);
        try {
            GlintResourcePack.applyLive(PALETTES[selectedPalette], brightness, speed, density);
            status = "Сохранено и применено";
        } catch (java.io.IOException exception) {
            status = "Ошибка: " + (exception.getMessage() == null ? "текстуры недоступны" : exception.getMessage());
        }
    }

    private void updateSlider(int x, int left, int width, double min, double max, boolean isBrightness) {
        double ratio = Math.max(0.0, Math.min(1.0, (x - left) / (double) width));
        double value = min + ratio * (max - min);
        if (isBrightness) brightness = value;
        else speed = value;
    }

    private void refreshPreviewTexture() {
        try {
            GlintResourcePack.applyLive(PALETTES[selectedPalette], brightness, speed, density);
            status = "Предпросмотр обновлён";
        } catch (java.io.IOException exception) {
            status = "Ошибка: " + (exception.getMessage() == null ? "текстуры недоступны" : exception.getMessage());
        }
    }

    private void resetDefaults() {
        GlintSettings.reset();
        selectedPalette = 0;
        brightness = GlintSettings.DEFAULT_BRIGHTNESS;
        speed = GlintSettings.DEFAULT_SPEED;
        density = GlintSettings.DEFAULT_DENSITY;
        refreshPreviewTexture();
        status = "Настройки сброшены";
    }

    private void updateDensity(int x) {
        double ratio = Math.max(0.0, Math.min(1.0, (x - 730) / 158.0));
        density = 0.25 + ratio * 3.75;
    }

    private void drawFallbackPreview(GuiGraphics graphics) {
        // Keep the preview populated in menus where Minecraft has not created a ClientLevel yet.
        graphics.fill(460, 150, 500, 194, 0xFF323C48);
        graphics.fill(448, 196, 512, 276, 0xFF252F3A);
        graphics.fill(451, 278, 475, 353, 0xFF323C48);
        graphics.fill(485, 278, 509, 353, 0xFF323C48);
        graphics.fill(422, 208, 446, 274, 0xFF323C48);
        graphics.fill(514, 208, 538, 274, 0xFF323C48);
        int tint = SWATCHES[selectedPalette];
        graphics.fill(456, 157, 504, 191, tint);
        graphics.fill(444, 197, 516, 272, tint);
        graphics.fill(449, 279, 477, 350, tint);
        graphics.fill(483, 279, 511, 350, tint);
        graphics.renderItem(enchanted(Items.NETHERITE_HELMET.getDefaultInstance()), 310, 150);
        graphics.renderItem(enchanted(Items.NETHERITE_CHESTPLATE.getDefaultInstance()), 310, 200);
        graphics.renderItem(enchanted(Items.NETHERITE_LEGGINGS.getDefaultInstance()), 310, 250);
        graphics.renderItem(enchanted(Items.NETHERITE_BOOTS.getDefaultInstance()), 310, 300);
        graphics.renderItem(enchanted(Items.NETHERITE_SWORD.getDefaultInstance()), 628, 232);
        graphics.drawCenteredString(font, "Зачарованный манекен", 480, 378, 0xFFBAC6D3);
    }

    private void drawSlider(GuiGraphics graphics, int x, int y, int width, double ratio) {
        graphics.fill(x, y, x + width, y + 7, 0xFF364452);
        int knob = x + (int) (width * Math.max(0.0, Math.min(1.0, ratio)));
        graphics.fill(x, y, knob, y + 7, 0xFF54A9F5);
        graphics.fill(knob - 5, y - 4, knob + 5, y + 11, 0xFFEAF3FC);
    }

    private void panel(GuiGraphics g, int x, int y, int w, int h, String title, int color) {
        g.fill(x, y, x + w, y + h, color);
        border(g, x, y, w, h, 0xFF354555);
        g.drawString(font, title, x + 16, y + 17, 0xFF93A7BA, false);
        g.fill(x + 16, y + 38, x + w - 16, y + 39, 0xFF2B3947);
    }

    private void button(GuiGraphics g, int x, int y, int w, int h, String label, boolean primary) {
        g.fill(x, y, x + w, y + h, primary ? 0xFF347FC2 : 0xFF202B37);
        border(g, x, y, w, h, primary ? 0xFF68B8FA : 0xFF425365);
        g.drawCenteredString(font, label, x + w / 2, y + 12, 0xFFF2F6FA);
    }

    private void border(GuiGraphics g, int x, int y, int w, int h, int color) {
        g.fill(x, y, x + w, y + 1, color);
        g.fill(x, y + h - 1, x + w, y + h, color);
        g.fill(x, y, x + 1, y + h, color);
        g.fill(x + w - 1, y, x + w, y + h, color);
    }

    private ItemStack enchanted(ItemStack stack) {
        stack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        return stack;
    }

    private Point point(double mouseX, double mouseY) {
        float scale = Math.min(width / (float) UI_WIDTH, height / (float) UI_HEIGHT);
        return new Point(Math.round((float) ((mouseX - (width - UI_WIDTH * scale) / 2.0F) / scale)),
                Math.round((float) ((mouseY - (height - UI_HEIGHT * scale) / 2.0F) / scale)));
    }

    private boolean inside(Point p, int x, int y, int w, int h) {
        return p.x >= x && p.x <= x + w && p.y >= y && p.y <= y + h;
    }

    private record Point(int x, int y) { }
}