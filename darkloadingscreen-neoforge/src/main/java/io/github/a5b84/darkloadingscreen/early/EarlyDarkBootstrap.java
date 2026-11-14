package io.github.a5b84.darkloadingscreen.early;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.ByteBuffer;
import java.nio.ByteOrder;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Locale;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.CRC32;
import java.util.zip.Deflater;
import java.util.zip.DeflaterOutputStream;
import java.util.zip.InflaterInputStream;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforgespi.earlywindow.GraphicsBootstrapper;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * Early window bootstrapper for NeoForge (fml-loadingscreen / earlydisplay).
 * Runs before DisplayWindow.initialize(), reads config/dark_loading_screen-client.toml,
 * enables dark mode, and writes a custom theme + progress bar / logo textures into config/fml/
 * so the NeoForge early loading screen uses the exact colors from Dark Loading Screen's config.
 */
public final class EarlyDarkBootstrap implements GraphicsBootstrapper {
    private static final Logger LOGGER = LogManager.getLogger("DarkLoadingScreen");

    public static final int DEFAULT_BG = 0x14181c;
    public static final int DEFAULT_BAR = 0xe22837;
    public static final int DEFAULT_BAR_BG = 0x14181c;
    public static final int DEFAULT_BORDER = 0x303336;
    public static final int DEFAULT_LOGO = 0xffffff;

    private static final String PROGRESS_BAR_BG_FILE = "dls_progress_bar_bg.png";
    private static final String PROGRESS_BAR_FG_FILE = "dls_progress_bar_fg.png";
    private static final String MOJANG_LOGO_FILE = "dls_mojang_logo.png";

    private static final Pattern TOML_KV_PATTERN = Pattern.compile(
            "^\\s*([A-Za-z0-9_]+)\\s*=\\s*\"([0-9a-fA-F]{6})\"\\s*(?:#.*)?$"
    );
    private static final Pattern JSON_KV_PATTERN = Pattern.compile(
            "\"([A-Za-z0-9_]+)\"\\s*:\\s*\"#?([0-9a-fA-F]{6})\""
    );

    @Override
    public String name() {
        return "dark_loading_screen";
    }

    @Override
    public void bootstrap(String[] args) {
        System.setProperty("fml.earlyWindowDarkMode", "true");
        try {
            Path configDir = FMLPaths.CONFIGDIR.get();
            if (configDir == null) {
                return;
            }
            int[] colors = readOrInitConfigColors(configDir);
            syncEarlyTheme(configDir, colors[0], colors[1], colors[2], colors[3], colors[4]);
            LOGGER.info(
                    "[Dark Loading Screen] Applied early loading screen theme (bg=#{}, bar=#{}, barBg=#{}, border=#{}, logo=#{})",
                    hex6(colors[0]), hex6(colors[1]), hex6(colors[2]), hex6(colors[3]), hex6(colors[4])
            );
        } catch (Throwable t) {
            LOGGER.warn("[Dark Loading Screen] Failed to apply custom early loading screen theme", t);
        }
    }

    public static void syncWithConfig(int bg, int bar, int barBg, int border, int logo) {
        try {
            Path configDir = FMLPaths.CONFIGDIR.get();
            if (configDir != null) {
                syncEarlyTheme(configDir, bg, bar, barBg, border, logo);
            }
        } catch (Throwable t) {
            LOGGER.warn("[Dark Loading Screen] Failed to sync early loading screen theme on config reload", t);
        }
    }

    private static int[] readOrInitConfigColors(Path configDir) {
        int bg = DEFAULT_BG;
        int bar = DEFAULT_BAR;
        int barBg = DEFAULT_BAR_BG;
        int border = DEFAULT_BORDER;
        int logo = DEFAULT_LOGO;

        Path tomlFile = configDir.resolve("dark_loading_screen-client.toml");
        Path legacyJsonFile = configDir.resolve("dark-loading-screen.json");

        if (Files.isRegularFile(tomlFile)) {
            try {
                List<String> lines = Files.readAllLines(tomlFile, StandardCharsets.UTF_8);
                for (String line : lines) {
                    Matcher m = TOML_KV_PATTERN.matcher(line);
                    if (m.matches()) {
                        String key = m.group(1);
                        int val = Integer.parseInt(m.group(2), 16) & 0xFFFFFF;
                        switch (key) {
                            case "background" -> bg = val;
                            case "bar" -> bar = val;
                            case "barBackground" -> barBg = val;
                            case "border" -> border = val;
                            case "logo" -> logo = val;
                        }
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("[Dark Loading Screen] Failed to read {}", tomlFile, e);
            }
        } else if (Files.isRegularFile(legacyJsonFile)) {
            try {
                String json = Files.readString(legacyJsonFile, StandardCharsets.UTF_8);
                Matcher m = JSON_KV_PATTERN.matcher(json);
                while (m.find()) {
                    String key = m.group(1);
                    int val = Integer.parseInt(m.group(2), 16) & 0xFFFFFF;
                    switch (key) {
                        case "background", "bg" -> bg = val;
                        case "bar" -> bar = val;
                        case "barBackground", "barBg" -> barBg = val;
                        case "border" -> border = val;
                        case "logo" -> logo = val;
                    }
                }
            } catch (Exception e) {
                LOGGER.warn("[Dark Loading Screen] Failed to read legacy config {}", legacyJsonFile, e);
            }
        }

        return new int[]{bg, bar, barBg, border, logo};
    }

    public static void syncEarlyTheme(Path configDir, int bg, int bar, int barBg, int border, int logo) throws IOException {
        Path fmlDir = configDir.resolve("fml");
        Files.createDirectories(fmlDir);

        writeProgressBarBgPng(fmlDir.resolve(PROGRESS_BAR_BG_FILE), border, barBg);
        writeProgressBarFgPng(fmlDir.resolve(PROGRESS_BAR_FG_FILE), bar);

        boolean customLogoReady = false;
        if ((logo & 0xFFFFFF) != 0xFFFFFF) {
            customLogoReady = tryWriteTintedMojangLogoPng(fmlDir.resolve(MOJANG_LOGO_FILE), bg, logo);
        }

        String darkThemeJson = buildThemeJson("builtin:darkmode", bg, logo, customLogoReady);
        String defaultThemeJson = buildThemeJson("builtin:default", bg, logo, customLogoReady);

        Files.writeString(fmlDir.resolve("theme-darkmode.json"), darkThemeJson, StandardCharsets.UTF_8);
        Files.writeString(fmlDir.resolve("theme-default.json"), defaultThemeJson, StandardCharsets.UTF_8);
    }

    private static String buildThemeJson(String parentTheme, int bg, int logo, boolean customLogoReady) {
        String bgHex = "#" + hex6(bg);
        String textHex = "#" + hex6(logo);

        StringBuilder sb = new StringBuilder(1024);
        sb.append("{\n");
        sb.append("  \"version\": 1,\n");
        sb.append("  \"extends\": \"").append(parentTheme).append("\",\n");
        sb.append("  \"colorScheme\": {\n");
        sb.append("    \"screenBackground\": \"").append(bgHex).append("\",\n");
        sb.append("    \"text\": \"").append(textHex).append("\",\n");
        sb.append("    \"memoryLowColor\": \"#ffffff\",\n");
        sb.append("    \"memoryHighColor\": \"#ffffff\"\n");
        sb.append("  },\n");
        sb.append("  \"sprites\": {\n");
        sb.append("    \"progressBarBackground\": {\n");
        sb.append("      \"resource\": \"").append(PROGRESS_BAR_BG_FILE).append("\",\n");
        sb.append("      \"scaling\": {\n");
        sb.append("        \"type\": \"nine_slice\",\n");
        sb.append("        \"width\": 40,\n");
        sb.append("        \"height\": 20,\n");
        sb.append("        \"left\": 2,\n");
        sb.append("        \"top\": 2,\n");
        sb.append("        \"right\": 2,\n");
        sb.append("        \"bottom\": 2,\n");
        sb.append("        \"stretchHorizontalFill\": true,\n");
        sb.append("        \"stretchVerticalFill\": true,\n");
        sb.append("        \"linearScaling\": false\n");
        sb.append("      }\n");
        sb.append("    },\n");
        sb.append("    \"progressBarForeground\": {\n");
        sb.append("      \"resource\": \"").append(PROGRESS_BAR_FG_FILE).append("\",\n");
        sb.append("      \"scaling\": {\n");
        sb.append("        \"type\": \"nine_slice\",\n");
        sb.append("        \"width\": 40,\n");
        sb.append("        \"height\": 20,\n");
        sb.append("        \"left\": 4,\n");
        sb.append("        \"top\": 4,\n");
        sb.append("        \"right\": 4,\n");
        sb.append("        \"bottom\": 4,\n");
        sb.append("        \"stretchHorizontalFill\": true,\n");
        sb.append("        \"stretchVerticalFill\": true,\n");
        sb.append("        \"linearScaling\": false\n");
        sb.append("      }\n");
        sb.append("    },\n");
        sb.append("    \"progressBarIndeterminate\": {\n");
        sb.append("      \"resource\": \"").append(PROGRESS_BAR_FG_FILE).append("\",\n");
        sb.append("      \"scaling\": {\n");
        sb.append("        \"type\": \"nine_slice\",\n");
        sb.append("        \"width\": 40,\n");
        sb.append("        \"height\": 20,\n");
        sb.append("        \"left\": 4,\n");
        sb.append("        \"top\": 4,\n");
        sb.append("        \"right\": 4,\n");
        sb.append("        \"bottom\": 4,\n");
        sb.append("        \"stretchHorizontalFill\": true,\n");
        sb.append("        \"stretchVerticalFill\": true,\n");
        sb.append("        \"linearScaling\": false\n");
        sb.append("      }\n");
        sb.append("    }\n");
        if (customLogoReady) {
            sb.append("  },\n");
            sb.append("  \"loadingScreen\": {\n");
            sb.append("    \"mojangLogo\": {\n");
            sb.append("      \"visible\": false\n");
            sb.append("    },\n");
            sb.append("    \"decoration\": {\n");
            sb.append("      \"dls_logo\": {\n");
            sb.append("        \"type\": \"image\",\n");
            sb.append("        \"visible\": true,\n");
            sb.append("        \"centerHorizontally\": true,\n");
            sb.append("        \"top\": 96,\n");
            sb.append("        \"texture\": {\n");
            sb.append("          \"resource\": \"").append(MOJANG_LOGO_FILE).append("\",\n");
            sb.append("          \"scaling\": {\n");
            sb.append("            \"type\": \"stretch\",\n");
            sb.append("            \"width\": 512,\n");
            sb.append("            \"height\": 128,\n");
            sb.append("            \"linearScaling\": true\n");
            sb.append("          }\n");
            sb.append("        }\n");
            sb.append("      }\n");
            sb.append("    }\n");
            sb.append("  }\n");
        } else {
            sb.append("  }\n");
        }
        sb.append("}\n");
        return sb.toString();
    }

    private static void writeProgressBarBgPng(Path path, int borderRgb, int barBgRgb) throws IOException {
        int width = 40;
        int height = 20;
        int[] pixels = new int[width * height];
        int borderArgb = 0xFF000000 | (borderRgb & 0xFFFFFF);
        int bgArgb = 0xFF000000 | (barBgRgb & 0xFFFFFF);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boolean isBorder = (x == 0 || x == width - 1 || y == 0 || y == height - 1);
                pixels[y * width + x] = isBorder ? borderArgb : bgArgb;
            }
        }
        writeRgbaPng(path, width, height, pixels);
    }

    private static void writeProgressBarFgPng(Path path, int barRgb) throws IOException {
        int width = 40;
        int height = 20;
        int[] pixels = new int[width * height];
        int barArgb = 0xFF000000 | (barRgb & 0xFFFFFF);

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                boolean isMargin = (x < 2 || x >= width - 2 || y < 2 || y >= height - 2);
                pixels[y * width + x] = isMargin ? 0x00000000 : barArgb;
            }
        }
        writeRgbaPng(path, width, height, pixels);
    }

    private static boolean tryWriteTintedMojangLogoPng(Path targetPath, int bgRgb, int logoRgb) {
        try {
            InputStream in = null;
            ClassLoader ctxLoader = Thread.currentThread().getContextClassLoader();
            if (ctxLoader != null) {
                in = ctxLoader.getResourceAsStream("assets/minecraft/textures/gui/title/mojangstudios.png");
            }
            if (in == null) {
                in = ClassLoader.getSystemResourceAsStream("assets/minecraft/textures/gui/title/mojangstudios.png");
            }
            if (in == null) {
                return false;
            }
            byte[] rawBytes;
            try (InputStream autoClose = in) {
                rawBytes = autoClose.readAllBytes();
            }
            DecodedPng src = decodePng(rawBytes);
            if (src == null || src.width <= 0 || src.height < 2) {
                return false;
            }

            int halfH = src.height / 2;
            int outW = src.width * 2;
            int outH = halfH;
            int[] outPixels = new int[outW * outH];

            int bgR = (bgRgb >> 16) & 0xFF;
            int bgG = (bgRgb >> 8) & 0xFF;
            int bgB = bgRgb & 0xFF;

            int logoR = (logoRgb >> 16) & 0xFF;
            int logoG = (logoRgb >> 8) & 0xFF;
            int logoB = logoRgb & 0xFF;

            for (int y = 0; y < halfH; y++) {
                for (int x = 0; x < src.width; x++) {
                    outPixels[y * outW + x] = tintLogoPixel(src.argb[y * src.width + x], bgR, bgG, bgB, logoR, logoG, logoB);
                    outPixels[y * outW + (src.width + x)] = tintLogoPixel(src.argb[(y + halfH) * src.width + x], bgR, bgG, bgB, logoR, logoG, logoB);
                }
            }

            writeRgbaPng(targetPath, outW, outH, outPixels);
            return true;
        } catch (Throwable t) {
            LOGGER.debug("[Dark Loading Screen] Could not pre-tint mojangstudios.png for early screen", t);
            return false;
        }
    }

    private static int tintLogoPixel(int srcArgb, int bgR, int bgG, int bgB, int logoR, int logoG, int logoB) {
        int a = (srcArgb >>> 24) & 0xFF;
        int r = (srcArgb >> 16) & 0xFF;
        int g = (srcArgb >> 8) & 0xFF;
        int b = srcArgb & 0xFF;

        // Vanilla mojangstudios.png has white letters (R=255, G=255, B=255) on either a black/red background or alpha.
        // Using the green/blue channel intensity combined with alpha extracts the clean anti-aliased glyph mask.
        float mask;
        if (a < 255) {
            mask = (a / 255.0f) * (Math.min(g, b) / 255.0f);
        } else {
            mask = Math.min(g, b) / 255.0f;
        }
        int outAlpha = Math.clamp(Math.round(mask * 255.0f), 0, 255);
        if (outAlpha == 0) {
            return 0x00000000;
        }
        return (outAlpha << 24) | (logoR << 16) | (logoG << 8) | logoB;
    }

    private record DecodedPng(int width, int height, int[] argb) {}

    private static DecodedPng decodePng(byte[] pngBytes) throws IOException {
        ByteBuffer buf = ByteBuffer.wrap(pngBytes).order(ByteOrder.BIG_ENDIAN);
        if (buf.remaining() < 8 || buf.getLong() != 0x89504E470D0A1A0AL) {
            return null;
        }
        int width = 0;
        int height = 0;
        int bitDepth = 0;
        int colorType = 0;
        byte[] palette = null;
        byte[] trns = null;
        ByteArrayOutputStream idatStream = new ByteArrayOutputStream();

        while (buf.remaining() >= 8) {
            int len = buf.getInt();
            int type = buf.getInt();
            if (len < 0 || buf.remaining() < len + 4) {
                break;
            }
            byte[] chunkData = new byte[len];
            buf.get(chunkData);
            buf.getInt(); // skip CRC

            if (type == 0x49484452) { // IHDR
                ByteBuffer ihdr = ByteBuffer.wrap(chunkData).order(ByteOrder.BIG_ENDIAN);
                width = ihdr.getInt();
                height = ihdr.getInt();
                bitDepth = ihdr.get() & 0xFF;
                colorType = ihdr.get() & 0xFF;
            } else if (type == 0x504C5445) { // PLTE
                palette = chunkData;
            } else if (type == 0x74524E53) { // tRNS
                trns = chunkData;
            } else if (type == 0x49444154) { // IDAT
                idatStream.write(chunkData);
            } else if (type == 0x49454E44) { // IEND
                break;
            }
        }

        if (width <= 0 || height <= 0 || bitDepth != 8) {
            return null;
        }

        int bytesPerPixel = switch (colorType) {
            case 0 -> 1; // Grayscale
            case 2 -> 3; // RGB
            case 3 -> 1; // Indexed
            case 4 -> 2; // Grayscale + Alpha
            case 6 -> 4; // RGBA
            default -> -1;
        };
        if (bytesPerPixel <= 0) {
            return null;
        }

        int stride = width * bytesPerPixel;
        byte[] rawScanlines = new byte[(stride + 1) * height];
        try (InflaterInputStream inflater = new InflaterInputStream(new ByteArrayInputStream(idatStream.toByteArray()))) {
            int offset = 0;
            while (offset < rawScanlines.length) {
                int r = inflater.read(rawScanlines, offset, rawScanlines.length - offset);
                if (r < 0) {
                    break;
                }
                offset += r;
            }
        }

        byte[] prevRow = new byte[stride];
        byte[] currRow = new byte[stride];
        int[] argb = new int[width * height];

        for (int y = 0; y < height; y++) {
            int rowStart = y * (stride + 1);
            int filter = rawScanlines[rowStart] & 0xFF;
            for (int i = 0; i < stride; i++) {
                int raw = rawScanlines[rowStart + 1 + i] & 0xFF;
                int a = (i >= bytesPerPixel) ? (currRow[i - bytesPerPixel] & 0xFF) : 0;
                int b = prevRow[i] & 0xFF;
                int c = (i >= bytesPerPixel) ? (prevRow[i - bytesPerPixel] & 0xFF) : 0;
                int recon = switch (filter) {
                    case 0 -> raw;
                    case 1 -> (raw + a) & 0xFF;
                    case 2 -> (raw + b) & 0xFF;
                    case 3 -> (raw + ((a + b) >>> 1)) & 0xFF;
                    case 4 -> (raw + paethPredictor(a, b, c)) & 0xFF;
                    default -> raw;
                };
                currRow[i] = (byte) recon;
            }

            for (int x = 0; x < width; x++) {
                int pxOffset = x * bytesPerPixel;
                int r, g, b, a;
                switch (colorType) {
                    case 0 -> {
                        r = g = b = currRow[pxOffset] & 0xFF;
                        a = 255;
                    }
                    case 2 -> {
                        r = currRow[pxOffset] & 0xFF;
                        g = currRow[pxOffset + 1] & 0xFF;
                        b = currRow[pxOffset + 2] & 0xFF;
                        a = 255;
                    }
                    case 3 -> {
                        int idx = currRow[pxOffset] & 0xFF;
                        if (palette != null && idx * 3 + 2 < palette.length) {
                            r = palette[idx * 3] & 0xFF;
                            g = palette[idx * 3 + 1] & 0xFF;
                            b = palette[idx * 3 + 2] & 0xFF;
                        } else {
                            r = g = b = 255;
                        }
                        a = (trns != null && idx < trns.length) ? (trns[idx] & 0xFF) : 255;
                    }
                    case 4 -> {
                        r = g = b = currRow[pxOffset] & 0xFF;
                        a = currRow[pxOffset + 1] & 0xFF;
                    }
                    case 6 -> {
                        r = currRow[pxOffset] & 0xFF;
                        g = currRow[pxOffset + 1] & 0xFF;
                        b = currRow[pxOffset + 2] & 0xFF;
                        a = currRow[pxOffset + 3] & 0xFF;
                    }
                    default -> {
                        r = g = b = a = 255;
                    }
                }
                argb[y * width + x] = (a << 24) | (r << 16) | (g << 8) | b;
            }

            System.arraycopy(currRow, 0, prevRow, 0, stride);
        }

        return new DecodedPng(width, height, argb);
    }

    private static int paethPredictor(int a, int b, int c) {
        int p = a + b - c;
        int pa = Math.abs(p - a);
        int pb = Math.abs(p - b);
        int pc = Math.abs(p - c);
        if (pa <= pb && pa <= pc) {
            return a;
        } else if (pb <= pc) {
            return b;
        } else {
            return c;
        }
    }

    public static void writeRgbaPng(Path path, int width, int height, int[] argbPixels) throws IOException {
        ByteArrayOutputStream pngOut = new ByteArrayOutputStream(1024);
        DataOutputStream dos = new DataOutputStream(pngOut);

        // PNG signature: 89 50 4E 47 0D 0A 1A 0A
        dos.writeLong(0x89504E470D0A1A0AL);

        // IHDR chunk (13 bytes)
        ByteArrayOutputStream ihdrData = new ByteArrayOutputStream(13);
        DataOutputStream ihdrDos = new DataOutputStream(ihdrData);
        ihdrDos.writeInt(width);
        ihdrDos.writeInt(height);
        ihdrDos.writeByte(8); // bit depth = 8
        ihdrDos.writeByte(6); // color type = 6 (RGBA)
        ihdrDos.writeByte(0); // compression = 0
        ihdrDos.writeByte(0); // filter = 0
        ihdrDos.writeByte(0); // interlace = 0
        writeChunk(dos, "IHDR", ihdrData.toByteArray());

        // IDAT chunk
        ByteArrayOutputStream rawScanlines = new ByteArrayOutputStream(height * (1 + width * 4));
        for (int y = 0; y < height; y++) {
            rawScanlines.write(0); // filter type 0 (None)
            int rowOffset = y * width;
            for (int x = 0; x < width; x++) {
                int argb = argbPixels[rowOffset + x];
                rawScanlines.write((argb >> 16) & 0xFF); // R
                rawScanlines.write((argb >> 8) & 0xFF);  // G
                rawScanlines.write(argb & 0xFF);         // B
                rawScanlines.write((argb >>> 24) & 0xFF); // A
            }
        }

        ByteArrayOutputStream compressedIdat = new ByteArrayOutputStream();
        Deflater deflater = new Deflater(Deflater.DEFAULT_COMPRESSION);
        try (DeflaterOutputStream deflaterOut = new DeflaterOutputStream(compressedIdat, deflater)) {
            rawScanlines.writeTo(deflaterOut);
        } finally {
            deflater.end();
        }
        writeChunk(dos, "IDAT", compressedIdat.toByteArray());

        // IEND chunk
        writeChunk(dos, "IEND", new byte[0]);

        dos.flush();
        Files.write(path, pngOut.toByteArray());
    }

    private static void writeChunk(DataOutputStream dos, String type, byte[] data) throws IOException {
        byte[] typeBytes = type.getBytes(StandardCharsets.US_ASCII);
        dos.writeInt(data.length);
        dos.write(typeBytes);
        dos.write(data);

        CRC32 crc = new CRC32();
        crc.update(typeBytes);
        crc.update(data);
        dos.writeInt((int) crc.getValue());
    }

    private static String hex6(int rgb) {
        return String.format(Locale.ROOT, "%06x", rgb & 0xFFFFFF);
    }
}
