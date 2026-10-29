package dev.darky.lyrics;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/** Reads metadata from the optional, local-only Windows Media Bridge. */
public final class MediaBridgeClient {
    private static final URI ENDPOINT = URI.create("http://127.0.0.1:38471/nowplaying");
    private static final HttpClient HTTP = HttpClient.newBuilder()
            .connectTimeout(Duration.ofMillis(500)).build();
    private static final ScheduledExecutorService POLLER = Executors.newSingleThreadScheduledExecutor(r -> {
        Thread t = new Thread(r, "DarkysLyrics-MediaBridge");
        t.setDaemon(true);
        return t;
    });
    private static volatile Track current = new Track(false, "", "", "", 0, "bridge-not-running");

    private MediaBridgeClient() {}

    public static void start() {
        POLLER.scheduleWithFixedDelay(MediaBridgeClient::poll, 0, 1, TimeUnit.SECONDS);
    }

    public static Track current() { return current; }

    private static void poll() {
        try {
            HttpRequest request = HttpRequest.newBuilder(ENDPOINT).timeout(Duration.ofMillis(900)).GET().build();
            HttpResponse<String> response = HTTP.send(request, HttpResponse.BodyHandlers.ofString());
            if (response.statusCode() != 200) return;
            JsonObject json = JsonParser.parseString(response.body()).getAsJsonObject();
            current = new Track(
                    json.has("playing") && json.get("playing").getAsBoolean(),
                    value(json, "title"), value(json, "artist"), value(json, "album"),
                    json.has("positionMs") ? Math.max(0, json.get("positionMs").getAsLong()) : 0,
                    value(json, "status"));
        } catch (Exception ignored) {
            current = new Track(false, "", "", "", 0, "bridge-not-running");
        }
    }

    private static String value(JsonObject json, String key) {
        return json.has(key) && !json.get(key).isJsonNull() ? json.get(key).getAsString() : "";
    }

    public record Track(boolean playing, String title, String artist, String album, long positionMs, String status) {
        public String displayTitle() { return title.isBlank() ? "No media detected" : title; }
        public String displayArtist() { return artist.isBlank() ? "Open a music app" : artist; }
    }
}
