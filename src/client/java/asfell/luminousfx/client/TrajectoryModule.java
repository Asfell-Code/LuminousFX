package asfell.luminousfx.client;

import java.util.ArrayList;
import java.util.List;

import com.mojang.blaze3d.systems.RenderSystem;
import com.mojang.blaze3d.vertex.BufferBuilder;
import com.mojang.blaze3d.vertex.BufferUploader;
import com.mojang.blaze3d.vertex.DefaultVertexFormat;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.Tesselator;
import com.mojang.blaze3d.vertex.VertexFormat;

import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderContext;
import net.fabricmc.fabric.api.client.rendering.v1.WorldRenderEvents;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public final class TrajectoryModule implements LuminousFXModule {
    private static final String ID = "trajectory";
    private static final String SELF = ID + ".show_self";
    private static final String OTHERS = ID + ".show_others";
    private static final String MOBS = ID + ".show_mobs";
    private static final String STEPS = ID + ".steps";
    private static final String STYLE = ID + ".line_style";
    private static final String WIDTH = ID + ".line_width";
    private static final String LINE_COLOR = ID + ".line_color";
    private static final String LINE_ALPHA = ID + ".line_alpha";
    private static final String MARKER = ID + ".marker";
    private static final String MARKER_COLOR = ID + ".marker_color";
    private static final String MARKER_ALPHA = ID + ".marker_alpha";
    private static final String MARKER_SIZE = ID + ".marker_size";
    private static final String MARKER_SHAPE = ID + ".marker_shape";
    private static final String BOW = ID + ".bow";
    private static final String CROSSBOW = ID + ".crossbow";
    private static final String PEARL = ID + ".ender_pearl";
    private static final String SNOWBALLS = ID + ".snowballs";
    private static final String POTIONS = ID + ".potions";
    private static final String TRIDENT = ID + ".trident";

    private volatile List<Track> tracks = List.of();

    @Override public String id() { return ID; }
    @Override public String translationKey() { return "module.luminousfx.trajectory"; }
    @Override public boolean defaultEnabled() { return true; }

    @Override
    public void register() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> {
            if (!LuminousFXConfig.isEnabled(ID, defaultEnabled()) || client.level == null || client.player == null) {
                tracks = List.of();
            }
        });
        WorldRenderEvents.LAST.register(this::renderWorld);
    }

    private void renderWorld(WorldRenderContext context) {
        Minecraft client = Minecraft.getInstance();
        if (!LuminousFXConfig.isEnabled(ID, defaultEnabled()) || client.level == null || client.player == null) {
            tracks = List.of();
            return;
        }
        tracks = calculateTracks(client.level, client.player);
        PoseStack pose = context.matrixStack();
        Vec3 camera = context.camera().getPosition();
        pose.pushPose();
        pose.translate(-camera.x, -camera.y, -camera.z);
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        RenderSystem.disableDepthTest();
        RenderSystem.setShader(GameRenderer::getPositionColorShader);
        RenderSystem.lineWidth(clamp(LuminousFXConfig.getInt(WIDTH, 2), 1, 6));
        for (Track track : tracks) drawTrack(pose, track);
        RenderSystem.lineWidth(1.0F);
        RenderSystem.enableDepthTest();
        RenderSystem.disableBlend();
        pose.popPose();
    }

    private List<Track> calculateTracks(Level level, LivingEntity player) {
        List<Track> result = new ArrayList<>();
        if (LuminousFXConfig.getBoolean(SELF, true)) addTrack(result, level, player, true);
        if (LuminousFXConfig.getBoolean(OTHERS, true)) {
            for (LivingEntity entity : level.players()) {
                if (entity != player && entity.isAlive()) addTrack(result, level, entity, false);
            }
        }
        if (LuminousFXConfig.getBoolean(MOBS, false)) {
            for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class,
                    player.getBoundingBox().inflate(48.0D), entity -> entity != player && entity.isAlive())) {
                addTrack(result, level, entity, false);
            }
        }
        return List.copyOf(result);
    }

    private void addTrack(List<Track> result, Level level, LivingEntity owner, boolean local) {
        ItemStack held = heldProjectile(owner);
        Projectile projectile = projectileFor(held);
        if (projectile == null || !LuminousFXConfig.getBoolean(projectile.key(), true)) return;
        double speed = projectile.speed();
        if (held.is(Items.BOW)) {
            double charge = Math.min(1.0D, owner.getTicksUsingItem() / 20.0D);
            charge = (charge * charge + charge * 2.0D) / 3.0D;
            if (charge < 0.1D) return;
            speed *= charge;
        }
        Vec3 view = owner.getViewVector(1.0F);
        Vec3 velocity = view.scale(speed);
        Vec3 right = view.cross(new Vec3(0.0D, 1.0D, 0.0D));
        if (right.lengthSqr() < 1.0E-6D) right = new Vec3(1.0D, 0.0D, 0.0D);
        right = right.normalize();
        boolean mainHand = projectileFor(owner.getMainHandItem()) != null;
        double handSide = mainHand ? 1.0D : -1.0D;
        Vec3 position = owner.getEyePosition()
            .add(right.scale(handSide * 0.32D))
            .add(0.0D, -0.16D, 0.0D)
            .add(view.scale(0.35D));
        List<Vec3> points = new ArrayList<>();
        points.add(position);
        Impact impact = null;
        int steps = clamp(LuminousFXConfig.getInt(STEPS, 80), 12, 180);
        for (int step = 0; step < steps; step++) {
            Vec3 next = position.add(velocity);
            BlockHitResult hit = level.clip(new ClipContext(position, next, ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE, owner));
            if (hit.getType() != HitResult.Type.MISS) {
                impact = new Impact(hit.getLocation(), Vec3.atLowerCornerOf(hit.getDirection().getNormal()));
                break;
            }
            Impact entityImpact = entityImpact(level, owner, position, next);
            if (entityImpact != null) {
                impact = entityImpact;
                break;
            }
            points.add(next);
            position = next;
            velocity = velocity.scale(projectile.drag()).add(0.0D, -projectile.gravity(), 0.0D);
        }
        result.add(new Track(points, impact, local));
    }

    private Impact entityImpact(Level level, LivingEntity owner, Vec3 start, Vec3 end) {
        Impact closest = null;
        double closestDistance = Double.MAX_VALUE;
        AABB search = new AABB(start, end).inflate(1.0D);
        for (LivingEntity entity : level.getEntitiesOfClass(LivingEntity.class, search,
                candidate -> candidate != owner && candidate.isAlive())) {
            var hit = entity.getBoundingBox().inflate(0.15D).clip(start, end);
            if (hit.isPresent() && start.distanceToSqr(hit.get()) < closestDistance) {
                Vec3 point = hit.get();
                closest = new Impact(point, faceNormal(entity.getBoundingBox(), point));
                closestDistance = start.distanceToSqr(point);
            }
        }
        return closest;
    }

    private void drawTrack(PoseStack pose, Track track) {
        if (track.points().size() < 2) return;
        int style = clamp(LuminousFXConfig.getInt(STYLE, 0), 0, 2);
        int baseColor = LuminousFXConfig.getInt(LINE_COLOR, 0x55D6FF);
        int alpha = clamp(LuminousFXConfig.getInt(LINE_ALPHA, 220), 0, 255);
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        for (int index = 0; index < track.points().size(); index++) {
            if (style == 1 && (index / 3) % 2 == 1) continue;
            Vec3 point = track.points().get(index);
            Vec3 previous = index == 0 ? point : track.points().get(index - 1);
            int color = style == 2 ? gradientColor(baseColor, index, track.points().size()) : baseColor;
            buffer.addVertex(pose.last().pose(), (float) previous.x, (float) previous.y, (float) previous.z)
                .setColor(red(color), green(color), blue(color), alpha);
            buffer.addVertex(pose.last().pose(), (float) point.x, (float) point.y, (float) point.z)
                    .setColor(red(color), green(color), blue(color), alpha);
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        if (track.impact() != null && LuminousFXConfig.getBoolean(MARKER, true)) drawMarker(pose, track.impact());
    }

    private void drawMarker(PoseStack pose, Impact impact) {
        Vec3 normal = impact.normal().normalize();
        Vec3 point = impact.point().add(normal.scale(0.003D));
        Vec3 tangent = Math.abs(normal.y) < 0.9D ? new Vec3(0.0D, 1.0D, 0.0D).cross(normal).normalize()
                : new Vec3(1.0D, 0.0D, 0.0D).cross(normal).normalize();
        Vec3 bitangent = normal.cross(tangent).normalize();
        int color = LuminousFXConfig.getInt(MARKER_COLOR, 0xFFCA55);
        int alpha = clamp(LuminousFXConfig.getInt(MARKER_ALPHA, 255), 0, 255);
        float size = clamp(LuminousFXConfig.getInt(MARKER_SIZE, 4), 1, 16) / 10.0F;
        int shape = clamp(LuminousFXConfig.getInt(MARKER_SHAPE, 0), 0, 3);
        float outerSize = size * 1.8F;
        int outerColor = 0xFFFFFF;
        int outerAlpha = Math.max(alpha, 220);
        drawRing(pose, point, tangent, bitangent, outerSize, outerColor, outerAlpha, 24);
        BufferBuilder buffer = Tesselator.getInstance().begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        if (shape == 0 || shape == 1) {
            for (int i = 0; i <= 16; i++) {
                double angle = Math.PI * 2.0D * i / 16.0D;
                addMarkerVertex(buffer, pose, point.add(tangent.scale(Math.cos(angle) * size))
                    .add(bitangent.scale(Math.sin(angle) * size)), color, alpha);
            }
        } else {
            markerVertex(buffer, pose, point, tangent, bitangent, -size, -size, color, alpha);
            markerVertex(buffer, pose, point, tangent, bitangent, size, size, color, alpha);
            if (shape == 3) {
                markerVertex(buffer, pose, point, tangent, bitangent, -size, size, color, alpha);
                markerVertex(buffer, pose, point, tangent, bitangent, size, -size, color, alpha);
            }
        }
        BufferUploader.drawWithShader(buffer.buildOrThrow());
        BufferBuilder crosshair = Tesselator.getInstance().begin(VertexFormat.Mode.DEBUG_LINES, DefaultVertexFormat.POSITION_COLOR);
        markerVertex(crosshair, pose, point, tangent, bitangent, -outerSize, 0.0D, outerColor, outerAlpha);
        markerVertex(crosshair, pose, point, tangent, bitangent, outerSize, 0.0D, outerColor, outerAlpha);
        markerVertex(crosshair, pose, point, tangent, bitangent, 0.0D, -outerSize, outerColor, outerAlpha);
        markerVertex(crosshair, pose, point, tangent, bitangent, 0.0D, outerSize, outerColor, outerAlpha);
        BufferUploader.drawWithShader(crosshair.buildOrThrow());
    }

    private void drawRing(PoseStack pose, Vec3 point, Vec3 tangent, Vec3 bitangent, float size, int color, int alpha, int segments) {
        BufferBuilder ring = Tesselator.getInstance().begin(VertexFormat.Mode.DEBUG_LINE_STRIP, DefaultVertexFormat.POSITION_COLOR);
        for (int i = 0; i <= segments; i++) {
            double angle = Math.PI * 2.0D * i / segments;
                addMarkerVertex(ring, pose, point.add(tangent.scale(Math.cos(angle) * size))
                    .add(bitangent.scale(Math.sin(angle) * size)), color, alpha);
        }
        BufferUploader.drawWithShader(ring.buildOrThrow());
    }

    private void markerVertex(BufferBuilder buffer, PoseStack pose, Vec3 point, Vec3 tangent, Vec3 bitangent,
            double tangentOffset, double bitangentOffset, int color, int alpha) {
        addMarkerVertex(buffer, pose, point.add(tangent.scale(tangentOffset)).add(bitangent.scale(bitangentOffset)), color, alpha);
    }

    private void addMarkerVertex(BufferBuilder buffer, PoseStack pose, Vec3 point, int color, int alpha) {
        buffer.addVertex(pose.last().pose(), (float) point.x, (float) point.y, (float) point.z)
                .setColor(red(color), green(color), blue(color), alpha);
    }

    private Vec3 faceNormal(AABB box, Vec3 point) {
        double x = Math.min(point.x - box.minX, box.maxX - point.x);
        double y = Math.min(point.y - box.minY, box.maxY - point.y);
        double z = Math.min(point.z - box.minZ, box.maxZ - point.z);
        if (x <= y && x <= z) return new Vec3(point.x < box.getCenter().x ? -1.0D : 1.0D, 0.0D, 0.0D);
        if (y <= z) return new Vec3(0.0D, point.y < box.getCenter().y ? -1.0D : 1.0D, 0.0D);
        return new Vec3(0.0D, 0.0D, point.z < box.getCenter().z ? -1.0D : 1.0D);
    }

    private ItemStack heldProjectile(LivingEntity entity) {
        if (projectileFor(entity.getMainHandItem()) != null) return entity.getMainHandItem();
        if (projectileFor(entity.getOffhandItem()) != null) return entity.getOffhandItem();
        return ItemStack.EMPTY;
    }

    private Projectile projectileFor(ItemStack stack) {
        if (stack.is(Items.BOW)) return new Projectile(BOW, 3.0D, 0.05D, 0.99D);
        if (stack.is(Items.CROSSBOW)) return new Projectile(CROSSBOW, 3.15D, 0.05D, 0.99D);
        if (stack.is(Items.ENDER_PEARL)) return new Projectile(PEARL, 1.5D, 0.03D, 0.99D);
        if (stack.is(Items.SNOWBALL) || stack.is(Items.EGG)) return new Projectile(SNOWBALLS, 1.5D, 0.03D, 0.99D);
        if (stack.is(Items.SPLASH_POTION) || stack.is(Items.LINGERING_POTION)) return new Projectile(POTIONS, 0.5D, 0.05D, 0.99D);
        if (stack.is(Items.TRIDENT)) return new Projectile(TRIDENT, 2.5D, 0.05D, 0.99D);
        return null;
    }

    private int gradientColor(int color, int index, int size) {
        float factor = size <= 1 ? 0.0F : (float) index / (size - 1);
        return ((int) (red(color) * (1.0F - factor) + 255.0F * factor) << 16)
                | ((int) (green(color) * (1.0F - factor) + 100.0F * factor) << 8)
                | (int) (blue(color) * (1.0F - factor) + 120.0F * factor);
    }

    private int red(int color) { return color >> 16 & 255; }
    private int green(int color) { return color >> 8 & 255; }
    private int blue(int color) { return color & 255; }
    private int clamp(int value, int min, int max) { return Math.max(min, Math.min(max, value)); }

    private record Projectile(String key, double speed, double gravity, double drag) {}
    private record Impact(Vec3 point, Vec3 normal) {}
    private record Track(List<Vec3> points, Impact impact, boolean local) {}
}