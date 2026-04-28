package com.talhanation.bannermod.client.military.gui.overlay;

import com.talhanation.bannermod.war.config.WarServerConfig;
import com.talhanation.bannermod.war.runtime.BattleWindowClock;
import com.talhanation.bannermod.war.runtime.BattleWindowSchedule;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;

import java.time.Duration;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Locale;

/**
 * Top-right HUD chip showing the current state of the warfare-RP battle window.
 *
 * <p>Reads {@link WarServerConfig} (Forge auto-syncs SERVER configs to the client on
 * connection) and turns the schedule into a {@link BattleWindowClock.Phase}. Renders only
 * once per frame, after the hotbar overlay, so it composes with vanilla HUD without flicker.
 * Hidden when regulated PvP is disabled, when GUI is hidden ({@code F1}), or when the
 * F3 debug overlay is active.</p>
 */
public class BattleWindowHud {
    private static final int PADDING_X = 6;
    private static final int PADDING_Y = 4;
    private static final int MARGIN_TOP = 6;
    private static final int MARGIN_RIGHT = 6;
    private static final long SCHEDULE_CACHE_MS = 5_000L;

    private static final int COLOR_OPEN = 0xFF55FF55;
    private static final int COLOR_CLOSED_SOON = 0xFFFFFF55;
    private static final int COLOR_CLOSED = 0xFFAAAAAA;
    private static final int COLOR_BG = 0x80000000;

    private long cachedScheduleAtMs = 0L;
    private List<String> cachedScheduleRaw;
    private BattleWindowSchedule cachedSchedule;

    @SubscribeEvent
    public void onRender(RenderGuiLayerEvent.Post event) {
        if (!event.getName().equals(VanillaGuiLayers.HOTBAR)) return;
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (mc.options.hideGui) return;
        if (mc.getDebugOverlay().showDebugScreen()) return;
        if (!regulatedPvpEnabled()) return;

        BattleWindowSchedule schedule = schedule();
        if (schedule == null) return;

        BattleWindowClock.Phase phase = BattleWindowClock.compute(
                schedule, ZonedDateTime.now(ZoneId.systemDefault()));

        renderPhase(event.getGuiGraphics(), mc, phase);
    }

    private void renderPhase(GuiGraphics graphics, Minecraft mc, BattleWindowClock.Phase phase) {
        Font font = mc.font;
        String label = label(phase);
        int color = colorFor(phase);

        int textWidth = font.width(label);
        int boxWidth = textWidth + PADDING_X * 2;
        int boxHeight = font.lineHeight + PADDING_Y * 2;
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int x = screenWidth - MARGIN_RIGHT - boxWidth;
        int y = MARGIN_TOP;

        graphics.fill(x, y, x + boxWidth, y + boxHeight, COLOR_BG);
        graphics.drawString(font, label, x + PADDING_X, y + PADDING_Y, color, true);
    }

    private static String label(BattleWindowClock.Phase phase) {
        if (phase instanceof BattleWindowClock.Phase.Open open) {
            return "Battle window OPEN · " + formatDuration(open.untilClose());
        }
        BattleWindowClock.Phase.Closed closed = (BattleWindowClock.Phase.Closed) phase;
        if (closed.nextWindow() == null) {
            return "No battle windows scheduled";
        }
        String day = closed.nextWindow().dayOfWeek().name().substring(0, 3);
        String time = closed.nextWindow().startsAt().toString();
        return "Next battle: " + day + " " + time + " · in " + formatDuration(closed.untilOpen());
    }

    private static int colorFor(BattleWindowClock.Phase phase) {
        if (phase instanceof BattleWindowClock.Phase.Open) {
            return COLOR_OPEN;
        }
        Duration until = phase.timeUntilTransition();
        if (until.toMinutes() < 5) {
            return COLOR_CLOSED_SOON;
        }
        return COLOR_CLOSED;
    }

    private static String formatDuration(Duration duration) {
        long seconds = Math.max(0, duration.getSeconds());
        long days = seconds / 86_400L;
        long hours = (seconds % 86_400L) / 3600L;
        long minutes = (seconds % 3600L) / 60L;
        long secs = seconds % 60L;
        if (days > 0) return String.format(Locale.ROOT, "%dd %dh", days, hours);
        if (hours > 0) return String.format(Locale.ROOT, "%dh %dm", hours, minutes);
        if (minutes > 0) return String.format(Locale.ROOT, "%d:%02d", minutes, secs);
        return String.format(Locale.ROOT, "0:%02d", secs);
    }

    private static boolean regulatedPvpEnabled() {
        try {
            return WarServerConfig.RegulatedPvpEnabled.get();
        } catch (Exception ignored) {
            return false;
        }
    }

    private BattleWindowSchedule schedule() {
        long now = System.currentTimeMillis();
        if (cachedSchedule != null && (now - cachedScheduleAtMs) < SCHEDULE_CACHE_MS) {
            return cachedSchedule;
        }
        List<String> raw;
        try {
            raw = WarServerConfig.BattleWindows.get();
        } catch (Exception ignored) {
            return null;
        }
        if (cachedSchedule != null && cachedScheduleRaw != null && cachedScheduleRaw.equals(raw)) {
            cachedScheduleAtMs = now;
            return cachedSchedule;
        }
        BattleWindowSchedule resolved;
        try {
            resolved = WarServerConfig.resolveSchedule();
        } catch (Exception ignored) {
            return null;
        }
        cachedSchedule = resolved;
        cachedScheduleRaw = List.copyOf(raw);
        cachedScheduleAtMs = now;
        return cachedSchedule;
    }
}
