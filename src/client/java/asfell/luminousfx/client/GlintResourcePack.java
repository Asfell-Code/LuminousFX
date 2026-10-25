package asfell.luminousfx.client;

import java.awt.GraphicsEnvironment;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;

import javax.imageio.ImageIO;
import javax.swing.JFileChooser;

import com.mojang.blaze3d.platform.NativeImage;

import asfell.luminousfx.LuminousFX;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.resources.ResourceLocation;

/** Replaces vanilla enchant-glint textures with in-memory textures; no resource reload is needed. */
public final class GlintResourcePack {
    private static final Path SOURCE_CONFIG = FabricLoader.getInstance().getConfigDir()
            .resolve("luminousfx-glint-source.txt");
    private static final Map<ResourceLocation, DynamicTexture> TEXTURES = new HashMap<>();
    private static final String[] TEXTURE_FILES = {"enchanted_glint_item.png", "enchanted_glint_entity.png"};
    private static final ResourceLocation[] TEXTURE_IDS = {
            ResourceLocation.withDefaultNamespace("textures/misc/enchanted_glint_item.png"),
            ResourceLocation.withDefaultNamespace("textures/misc/enchanted_glint_entity.png")
    };

    private static Path sourceFolder = loadSourceFolder();
    private static BufferedImage[][] frames;
    private static BufferedImage[] sourceImages;
    private static String cachedPalette;
    private static Path cachedSourceFolder;
    private static double animationSpeed;
    private static long lastUploadedFrame = Long.MIN_VALUE;
    private static boolean initialPaletteApplied;

    private GlintResourcePack() { }

    public static void initialize() {
        ClientTickEvents.END_CLIENT_TICK.register(client -> tick());
    }

    public static String sourceFolderLabel() {
        return sourceFolder == null || sourceFolder.getFileName() == null
                ? "не выбрана" : sourceFolder.getFileName().toString();
    }

    public static void chooseSourceFolder() {
        if (GraphicsEnvironment.isHeadless()) return;
        JFileChooser chooser = new JFileChooser(sourceFolder == null
                ? defaultSourceFolder().toFile() : sourceFolder.toFile());
        chooser.setDialogTitle("Выберите каталог палитр glint");
        chooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
        if (chooser.showOpenDialog(null) != JFileChooser.APPROVE_OPTION) return;

        sourceFolder = chooser.getSelectedFile().toPath();
        sourceImages = null;
        cachedPalette = null;
        cachedSourceFolder = null;
        try {
            Files.createDirectories(SOURCE_CONFIG.getParent());
            Files.writeString(SOURCE_CONFIG, sourceFolder.toString());
        } catch (IOException ignored) {
            // Keep the selected directory for this game session even if persisting the path fails.
        }
    }

    public static void applyLive(String palette, double brightness, double speed, double density) throws IOException {
        if (sourceFolder == null) {
            throw new IOException("не найдена папка с палитрами light, lime, no, orange, red, yellow");
        }
        if (sourceImages == null || !palette.equals(cachedPalette) || !sourceFolder.equals(cachedSourceFolder)) {
            Path paletteFolder = sourceFolder.resolve(palette);
            BufferedImage[] loadedImages = new BufferedImage[TEXTURE_FILES.length];
            for (int i = 0; i < TEXTURE_FILES.length; i++) {
                Path file = paletteFolder.resolve(TEXTURE_FILES[i]);
                if (!Files.isRegularFile(file)) {
                    throw new IOException("нет файла " + palette + "/" + TEXTURE_FILES[i]);
                }
                loadedImages[i] = ImageIO.read(file.toFile());
                if (loadedImages[i] == null) throw new IOException("не удалось прочитать " + TEXTURE_FILES[i]);
            }
            sourceImages = loadedImages;
            cachedPalette = palette;
            cachedSourceFolder = sourceFolder;
        }
        BufferedImage[] sources = sourceImages;
        int frameCount = speed < 0.05 ? 1 : 4;
        BufferedImage[][] nextFrames = new BufferedImage[sources.length][frameCount];
        for (int texture = 0; texture < sources.length; texture++) {
            for (int frame = 0; frame < frameCount; frame++) {
                nextFrames[texture][frame] = createFrame(sources[texture], frame, brightness, density);
            }
        }

        frames = nextFrames;
        animationSpeed = Math.max(0.0, speed);
        lastUploadedFrame = Long.MIN_VALUE;
        uploadCurrentFrame(0);
    }

    public static void tick() {
        if (!initialPaletteApplied) {
            initialPaletteApplied = true;
            String[] palettes = {"light", "lime", "no", "orange", "red", "yellow"};
            int selected = Math.max(0, Math.min(palettes.length - 1,
                    LuminousFXConfig.getInt("glint.palette", 0)));
            try {
                // Texture creation uses OpenGL, so defer it until the client render context is active.
                applyLive(palettes[selected], GlintSettings.brightness(), GlintSettings.speed(),
                    GlintSettings.density());
            } catch (IOException exception) {
                LuminousFX.LOGGER.warn("Could not load the initial glint palette", exception);
            }
        }
        if (frames == null || frames.length == 0 || animationSpeed <= 0.0) return;
        long framePeriodNanos = Math.max(1L, (long) (100_000_000.0 / animationSpeed));
        long frameNumber = System.nanoTime() / framePeriodNanos;
        if (frameNumber != lastUploadedFrame) uploadCurrentFrame(frameNumber);
    }

    private static void uploadCurrentFrame(long frameNumber) {
        if (frames == null) return;
        int count = frames[0].length;
        int frame = count == 1 ? 0 : (int) Math.floorMod(frameNumber, count);
        Minecraft minecraft = Minecraft.getInstance();
        for (int i = 0; i < TEXTURE_IDS.length; i++) {
            try {
                NativeImage pixels = toNativeImage(frames[i][frame]);
                DynamicTexture texture = TEXTURES.get(TEXTURE_IDS[i]);
                if (texture == null) {
                    texture = new DynamicTexture(pixels);
                    TEXTURES.put(TEXTURE_IDS[i], texture);
                    minecraft.getTextureManager().register(TEXTURE_IDS[i], texture);
                } else {
                    texture.setPixels(pixels);
                    texture.upload();
                }
            } catch (IOException exception) {
                throw new IllegalStateException("Unable to update enchant glint texture", exception);
            }
        }
        lastUploadedFrame = frameNumber;
    }

    private static BufferedImage createFrame(BufferedImage source, int frame, double brightness, double density) {
        int width = source.getWidth();
        int height = source.getHeight();
        int offsetX = frame * Math.max(1, width / 32);
        int offsetY = frame * Math.max(1, height / 32);
        BufferedImage result = new BufferedImage(width, height, BufferedImage.TYPE_INT_ARGB);
        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int sourceX = (int) Math.floor(x * density + offsetX) % width;
                int sourceY = (int) Math.floor(y * density + offsetY) % height;
                int argb = source.getRGB(sourceX, sourceY);
                int alpha = argb >>> 24;
                int red = brighten(argb >>> 16 & 0xFF, brightness);
                int green = brighten(argb >>> 8 & 0xFF, brightness);
                int blue = brighten(argb & 0xFF, brightness);
                result.setRGB(x, y, alpha << 24 | red << 16 | green << 8 | blue);
            }
        }
        return result;
    }

    private static NativeImage toNativeImage(BufferedImage image) throws IOException {
        try (ByteArrayOutputStream encoded = new ByteArrayOutputStream()) {
            if (!ImageIO.write(image, "png", encoded)) throw new IOException("PNG encoding unavailable");
            return NativeImage.read(new ByteArrayInputStream(encoded.toByteArray()));
        }
    }

    private static int brighten(int channel, double multiplier) {
        return (int) Math.max(0, Math.min(255, Math.round(channel * multiplier)));
    }

    private static Path loadSourceFolder() {
        try {
            if (Files.isRegularFile(SOURCE_CONFIG)) {
                Path configured = Path.of(Files.readString(SOURCE_CONFIG).trim());
                if (Files.isDirectory(configured)) return configured;
            }
        } catch (IOException | RuntimeException ignored) { }
        Path fallback = defaultSourceFolder();
        return Files.isDirectory(fallback) ? fallback : null;
    }

    private static Path defaultSourceFolder() {
        return Path.of(System.getProperty("user.home"), "Desktop", "Новая папка");
    }
}