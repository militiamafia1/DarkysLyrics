package dev.darky.lyrics;

import net.fabricmc.loader.api.FabricLoader;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/** Loads user-provided LRC timestamped lyrics; no lyrics are bundled. */
public final class LrcLyrics {
    private static final Pattern TIMESTAMP = Pattern.compile("\\[(\\d{1,2}):(\\d{2})(?:[.:](\\d{1,3}))?]\\s*(.*)");
    private static final Path DIRECTORY = FabricLoader.getInstance().getConfigDir().resolve("darkys-lyrics");
    private static final Path FILE = DIRECTORY.resolve("lyrics.lrc");
    private static final List<Line> LINES = new ArrayList<>();
    private static long startedAtMs = -1L;

    private LrcLyrics() {}

    public static Path filePath() { return FILE; }

    public static void reload() {
        LINES.clear();
        try {
            Files.createDirectories(DIRECTORY);
            if (!Files.exists(FILE)) {
                Files.writeString(FILE, "[00:00.00]Put your own timestamped lyrics here\n[00:05.00]Example: next line at five seconds\n", StandardCharsets.UTF_8);
            }
            for (String raw : Files.readAllLines(FILE, StandardCharsets.UTF_8)) {
                Matcher matcher = TIMESTAMP.matcher(raw.trim());
                if (!matcher.matches()) continue;
                int minutes = Integer.parseInt(matcher.group(1));
                int seconds = Integer.parseInt(matcher.group(2));
                String fraction = matcher.group(3);
                int millis = 0;
                if (fraction != null) {
                    millis = fraction.length() == 1 ? Integer.parseInt(fraction) * 100
                            : fraction.length() == 2 ? Integer.parseInt(fraction) * 10
                            : Integer.parseInt(fraction.substring(0, 3));
                }
                LINES.add(new Line((minutes * 60_000L) + (seconds * 1_000L) + millis, matcher.group(4)));
            }
            LINES.sort(Comparator.comparingLong(Line::timeMs));
        } catch (IOException | NumberFormatException ignored) {
            // A missing or malformed user file should not crash Minecraft.
        }
    }

    public static void start() { startedAtMs = System.currentTimeMillis(); }
    public static boolean isStarted() { return startedAtMs >= 0; }
    public static long elapsedMs() { return isStarted() ? Math.max(0, System.currentTimeMillis() - startedAtMs) : 0; }
    public static List<Line> lines() { return List.copyOf(LINES); }

    public static int currentIndex() {
        if (LINES.isEmpty() || !isStarted()) return -1;
        MediaBridgeClient.Track track = MediaBridgeClient.current();
        long elapsed = track.playing() ? track.positionMs() : elapsedMs();
        int found = -1;
        for (int i = 0; i < LINES.size(); i++) {
            if (LINES.get(i).timeMs() <= elapsed) found = i;
            else break;
        }
        return found;
    }

    public record Line(long timeMs, String text) {}
}
