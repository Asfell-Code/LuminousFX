package asfell.luminousfx.client;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;
import net.fabricmc.fabric.api.client.rendering.v1.HudRenderCallback;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.boss.enderdragon.EndCrystal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.Vec3;

public final class CrystalPvPOverlay {
    private static final float CRYSTAL_RADIUS = 12.0F;
    private static final int BLUE = 0xFF54B9FF;
    private static final int RED = 0xFFFF4F66;
    private static final int GREEN = 0xFF59E391;
    private static final int AMBER = 0xFFFFC857;

    private CrystalPvPOverlay() {
    }

    public static void register() {
        WorldRenderEvents.AFTER_ENTITIES.register(CrystalPvPOverlay::renderWorld);
        HudRenderCallback.EVENT.register((graphics, ignored) -> renderHud(graphics));
    }

    private static void renderWorld(WorldRenderContext context) {
        Minecraft minecraft = Minecraft.getInstance();
        PoseStack pose = context.matrixStack();
        if (minecraft.level == null || minecraft.player == null || pose == null) {
            return;
        }
        var level = minecraft.level;
        var player = minecraft.player;
        Vec3 camera = context.camera().getPosition();
        if (minecraft.getEntityRenderDispatcher().shouldRenderHitBoxes()) {
            for (Entity entity : level.entitiesForRendering()) {
                if (entity != player) {
                    drawBox(pose, entity.getBoundingBox().move(-camera.x, -camera.y, -camera.z), LuminousFXConfig.getHitboxColor());
                }
            }
        }
        for (Entity entity : level.entitiesForRendering()) {
            if (!(entity instanceof EndCrystal crystal)) {
                continue;
            }
            Vec3 position = crystal.position().subtract(camera);
            if (LuminousFXConfig.isEnabled("Blast Radius")) {
                drawSphere(pose, position, CRYSTAL_RADIUS, 0.45F, BLUE);
            }
            if (LuminousFXConfig.isEnabled("Crystal Timer")) {
                drawRing(pose, position, 1.4F, crystal.tickCount / 20.0F, AMBER);
            }
            if (LuminousFXConfig.isEnabled("Crystal Damage Viz")) {
                drawLine(pose, position, Vec3.ZERO, damageColor(crystal, player.position()));
            }
        }
    }

    private static void renderHud(GuiGraphics graphics) {
        Minecraft minecraft = Minecraft.getInstance();
        if (minecraft.player == null || minecraft.level == null) {
            return;
        }
        if (LuminousFXConfig.isEnabled("Armor Durability")) {
            drawArmorHud(graphics, minecraft);
        }
        if (LuminousFXConfig.isEnabled("Hit Marker")) {
            drawCrosshair(graphics, minecraft);
        }
    }

    private static void drawArmorHud(GuiGraphics graphics, Minecraft minecraft) {
        int screenWidth = minecraft.getWindow().getGuiScaledWidth();
        int screenHeight = minecraft.getWindow().getGuiScaledHeight();
        int left = screenWidth / 2 - 91 - 44;
        int top = screenHeight - 22 - 4 * 18;
        ItemStack[] armor = new ItemStack[4];
        int index = 0;
        for (ItemStack stack : minecraft.player.getArmorSlots()) {
            if (index < armor.length) {
                armor[index++] = stack;
            }
        }
        for (int slot = 0; slot < armor.length; slot++) {
            ItemStack stack = armor[armor.length - 1 - slot] == null ? ItemStack.EMPTY : armor[armor.length - 1 - slot];
            int y = top + slot * 18;
            int percent = durabilityPercent(stack);
            if (!stack.isEmpty()) {
                graphics.renderItem(stack, left, y);
                graphics.fill(left + 18, y + 14, left + 48, y + 17, 0xFF303640);
                graphics.fill(left + 18, y + 14, left + 18 + Math.max(1, percent * 30 / 100), y + 17,
                        LuminousFXConfig.getArmorBarColor());
            }
        }
    }

    private static void drawCrosshair(GuiGraphics graphics, Minecraft minecraft) {
        int centerX = minecraft.getWindow().getGuiScaledWidth() / 2;
        int centerY = minecraft.getWindow().getGuiScaledHeight() / 2;
        int color = minecraft.player.hurtTime > 0 ? RED : 0xA0FFFFFF;
        graphics.fill(centerX - 7, centerY - 1, centerX - 2, centerY + 1, color);
        graphics.fill(centerX + 2, centerY - 1, centerX + 7, centerY + 1, color);
        graphics.fill(centerX - 1, centerY - 7, centerX + 1, centerY - 2, color);
        graphics.fill(centerX - 1, centerY + 2, centerX + 1, centerY + 7, color);
    }

    private static int durabilityPercent(ItemStack stack) {
        if (stack.isEmpty() || !stack.isDamageableItem()) {
            return 0;
        }
        return Math.max(0, (stack.getMaxDamage() - stack.getDamageValue()) * 100 / stack.getMaxDamage());
    }

    private static void drawSphere(PoseStack pose, Vec3 center, float radius, float alpha, int color) {
        for (int ring = 0; ring < 3; ring++) {
            Vec3 previous = spherePoint(center, radius, ring, 0.0F);
            for (int segment = 1; segment <= 32; segment++) {
                Vec3 next = spherePoint(center, radius, ring, segment * 360.0F / 32.0F);
                drawLine(pose, previous, next, (color & 0x00FFFFFF) | ((int) (alpha * 255) << 24));
                previous = next;
            }
        }
    }

    private static Vec3 spherePoint(Vec3 center, float radius, int ring, float degrees) {
        double angle = Math.toRadians(degrees);
        return switch (ring) {
            case 0 -> center.add(Math.cos(angle) * radius, 0, Math.sin(angle) * radius);
            case 1 -> center.add(0, Math.cos(angle) * radius, Math.sin(angle) * radius);
            default -> center.add(Math.cos(angle) * radius, Math.sin(angle) * radius, 0);
        };
    }

    private static void drawRing(PoseStack pose, Vec3 center, float radius, float progress, int color) {
        Vec3 previous = center.add(radius, 0, 0);
        for (int index = 1; index <= 24; index++) {
            float angle = Math.min(index / 24.0F, 1.0F) * ((progress % 1.0F) * 360.0F);
            Vec3 next = center.add(Math.cos(Math.toRadians(angle)) * radius, 0.05, Math.sin(Math.toRadians(angle)) * radius);
            drawLine(pose, previous, next, color);
            previous = next;
        }
    }

    private static int damageColor(EndCrystal crystal, Vec3 target) {
        double distance = crystal.position().distanceTo(target);
        return distance < 4.0 ? RED : distance < 8.0 ? AMBER : GREEN;
    }

    private static void drawLine(PoseStack pose, Vec3 from, Vec3 to, int color) {
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(2.0F);
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.LINES, DefaultVertexFormat.POSITION_COLOR);
        buffer.addVertex(pose.last().pose(), (float) from.x, (float) from.y, (float) from.z).setColor(color);
        buffer.addVertex(pose.last().pose(), (float) to.x, (float) to.y, (float) to.z).setColor(color);
        BufferUploader.drawWithShader(buffer.buildOrThrow());
    }

    private static void drawBox(PoseStack pose, net.minecraft.world.phys.AABB box, int color) {
        double minX = box.minX;
        double minY = box.minY;
        double minZ = box.minZ;
        double maxX = box.maxX;
        double maxY = box.maxY;
        double maxZ = box.maxZ;
        Vec3[] corners = {
                new Vec3(minX, minY, minZ), new Vec3(maxX, minY, minZ),
                new Vec3(maxX, minY, maxZ), new Vec3(minX, minY, maxZ),
                new Vec3(minX, maxY, minZ), new Vec3(maxX, maxY, minZ),
                new Vec3(maxX, maxY, maxZ), new Vec3(minX, maxY, maxZ)
        };
        int[][] edges = {{0, 1}, {1, 2}, {2, 3}, {3, 0}, {4, 5}, {5, 6}, {6, 7}, {7, 4},
                {0, 4}, {1, 5}, {2, 6}, {3, 7}};
        for (int[] edge : edges) {
            drawLine(pose, corners[edge[0]], corners[edge[1]], color);
        }
    }
}
