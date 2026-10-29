package dev.darky.lyrics;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import java.util.List;

public final class LyricsScreen extends Screen {
    private int selectedTab = 0;

    private static final int BG = 0xF20A0D18;
    private static final int PANEL = 0xFF11182B;
    private static final int PANEL_ALT = 0xFF171F38;
    private static final int BLUE = 0xFF75B8FF;
    private static final int PURPLE = 0xFF9A7BFF;
    private static final int TEXT = 0xFFF5F6FF;
    private static final int MUTED = 0xFFAAB4CB;
    private static final int GOLD = 0xFFFFB34F;

    public LyricsScreen() { super(Component.literal("Darky's Lyrics")); }

    @Override
    protected void init() {
        int panelWidth = Math.min(520, this.width - 36);
        int left = (this.width - panelWidth) / 2;
        int top = Math.max(20, (this.height - 320) / 2);
        addRenderableWidget(Button.builder(Component.literal("PLAYER"), b -> selectedTab = 0)
                .bounds(left + 18, top + 62, 88, 20).build());
        addRenderableWidget(Button.builder(Component.literal("LYRICS"), b -> selectedTab = 1)
                .bounds(left + 110, top + 62, 88, 20).build());
        addRenderableWidget(Button.builder(Component.literal("VISUALS"), b -> selectedTab = 2)
                .bounds(left + 202, top + 62, 88, 20).build());
        addRenderableWidget(Button.builder(Component.literal("START TIMER"), b -> LrcLyrics.start())
                .bounds(left + panelWidth - 198, top + 62, 92, 20).build());
        addRenderableWidget(Button.builder(Component.literal("RELOAD LRC"), b -> LrcLyrics.reload())
                .bounds(left + panelWidth - 102, top + 62, 84, 20).build());
        addRenderableWidget(Button.builder(Component.literal("Done"), b -> onClose())
                .bounds(left + panelWidth - 88, top + 286, 70, 20).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        int panelWidth = Math.min(520, this.width - 36);
        int panelHeight = 320;
        int left = (this.width - panelWidth) / 2;
        int top = (this.height - panelHeight) / 2;
        graphics.fill(0, 0, width, height, BG);
        graphics.fill(left, top, left + panelWidth, top + panelHeight, PANEL);
        graphics.fill(left, top, left + panelWidth, top + 3, BLUE);
        graphics.fill(left + 14, top + 16, left + 42, top + 44, GOLD);
        graphics.drawCenteredString(font, Component.literal("♫"), left + 28, top + 21, 0xFF101522);
        graphics.drawString(font, Component.literal("DARKY'S LYRICS"), left + 52, top + 17, TEXT);
        graphics.drawString(font, Component.literal("MUSIC OVERLAY  •  FABRIC 1.21.11"), left + 52, top + 33, MUTED);
        graphics.fill(left + 14, top + 52, left + panelWidth - 14, top + 53, 0xFF29324D);
        super.render(graphics, mouseX, mouseY, partialTick);

        int contentTop = top + 96;
        if (selectedTab == 0) {
            drawCard(graphics, left + 18, contentTop, panelWidth - 36, 104);
            graphics.drawString(font, Component.literal("NOW PLAYING"), left + 30, contentTop + 10, BLUE);
            MediaBridgeClient.Track track = MediaBridgeClient.current();
            String title = track.displayTitle();
            if (title.length() > 45) title = title.substring(0, 42) + "...";
            String artist = track.displayArtist();
            if (artist.length() > 48) artist = artist.substring(0, 45) + "...";
            graphics.drawString(font, Component.literal(title), left + 30, contentTop + 31, TEXT);
            graphics.drawString(font, Component.literal(artist), left + 30, contentTop + 51, MUTED);
            graphics.drawString(font, Component.literal(track.playing() ? "Windows media detected • lyrics follow playback position" : "Start the optional Windows Media Bridge for auto-detection"), left + 30, contentTop + 71, GOLD);
        } else if (selectedTab == 1) {
            drawCard(graphics, left + 18, contentTop, panelWidth - 36, 172);
            graphics.drawCenteredString(font, Component.literal("LIVE LYRICS  •  LOCAL LRC"), left + panelWidth / 2, contentTop + 10, BLUE);
            List<LrcLyrics.Line> lines = LrcLyrics.lines();
            int current = LrcLyrics.currentIndex();
            if (lines.isEmpty()) {
                graphics.drawCenteredString(font, Component.literal("No timestamped lines found."), left + panelWidth / 2, contentTop + 55, TEXT);
            } else {
                int first = Math.max(0, (current < 0 ? 0 : current) - 2);
                for (int row = 0; row < 5 && first + row < lines.size(); row++) {
                    int idx = first + row;
                    int color = idx == current ? BLUE : (idx > current ? MUTED : 0xFF64708B);
                    String line = lines.get(idx).text();
                    if (line.length() > 54) line = line.substring(0, 51) + "...";
                    graphics.drawCenteredString(font, Component.literal(line), left + panelWidth / 2, contentTop + 37 + row * 22, color);
                }
            }
            MediaBridgeClient.Track track = MediaBridgeClient.current();
            String status = track.playing() ? "Media position: " + formatTime(track.positionMs()) : (LrcLyrics.isStarted() ? "Manual timer: " + formatTime(LrcLyrics.elapsedMs()) : "Waiting for media bridge or START TIMER");
            graphics.drawCenteredString(font, Component.literal(status), left + panelWidth / 2, contentTop + 148, GOLD);
        } else {
            drawCard(graphics, left + 18, contentTop, panelWidth - 36, 150);
            graphics.drawString(font, Component.literal("LOCAL VISUALS"), left + 30, contentTop + 12, BLUE);
            graphics.drawString(font, Component.literal("Theme: Midnight / blue / violet"), left + 30, contentTop + 36, TEXT);
            graphics.drawString(font, Component.literal("HUD layout: compact"), left + 30, contentTop + 56, TEXT);
            graphics.drawString(font, Component.literal("Client-side only; no gameplay data is sent."), left + 30, contentTop + 76, MUTED);
            graphics.drawString(font, Component.literal("Media metadata stays on your PC via localhost."), left + 30, contentTop + 100, GOLD);
        }
        graphics.drawString(font, Component.literal("Lyrics file: config/darkys-lyrics/lyrics.lrc"), left + 20, top + panelHeight - 18, MUTED);
    }

    private static String formatTime(long millis) {
        long seconds = millis / 1000;
        return String.format("%02d:%02d", seconds / 60, seconds % 60);
    }

    private void drawCard(GuiGraphics graphics, int x, int y, int w, int h) {
        graphics.fill(x, y, x + w, y + h, PANEL_ALT);
        graphics.fill(x, y, x + 2, y + h, PURPLE);
    }

    @Override public boolean isPauseScreen() { return false; }
}
