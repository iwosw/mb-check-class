package com.talhanation.bannermod.client.military.gui;

import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

public final class MilitaryGuiStyle {
    public static final int TEXT = 0xF4D8A1;
    public static final int TEXT_DARK = 0x3B2A17;
    public static final int TEXT_MUTED = 0xB8A17A;
    public static final int TEXT_GOOD = 0x79B15A;
    public static final int TEXT_WARN = 0xD9A441;
    public static final int TEXT_DENIED = 0xD06A4B;

    private static final int FRAME_DARK = 0xF0201810;
    private static final int FRAME_WOOD = 0xF05A4025;
    private static final int PARCHMENT = 0xE8D7B98C;
    private static final int PARCHMENT_LIGHT = 0xF0E6D8B8;
    private static final int IRON_DARK = 0xD8140E09;
    private static final int OUTLINE_GOLD = 0xFFE0B86A;
    private static final int OUTLINE_WOOD = 0xFF8A6A3A;

    private MilitaryGuiStyle() {
    }

    public static void parchmentPanel(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, FRAME_DARK);
        graphics.fill(x + 2, y + 2, x + width - 2, y + height - 2, FRAME_WOOD);
        graphics.fill(x + 4, y + 4, x + width - 4, y + height - 4, PARCHMENT);
        graphics.renderOutline(x, y, width, height, OUTLINE_WOOD);
        graphics.renderOutline(x + 3, y + 3, width - 6, height - 6, 0x80301810);
    }

    public static void insetPanel(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, IRON_DARK);
        graphics.renderOutline(x, y, width, height, OUTLINE_WOOD);
        graphics.renderOutline(x + 1, y + 1, width - 2, height - 2, 0x66301810);
    }

    public static void parchmentInset(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, PARCHMENT_LIGHT);
        graphics.renderOutline(x, y, width, height, 0xAA8A6A3A);
    }

    public static void titleStrip(GuiGraphics graphics, int x, int y, int width, int height) {
        graphics.fill(x, y, x + width, y + height, 0xD05A4025);
        graphics.renderOutline(x, y, width, height, OUTLINE_GOLD);
    }

    public static void drawCenteredTitle(GuiGraphics graphics, Font font, Component title, int x, int y, int width) {
        graphics.drawCenteredString(font, title, x + width / 2, y, TEXT);
    }

    public static void drawBadge(GuiGraphics graphics, Font font, Component text, int x, int y, int width, int color) {
        graphics.fill(x, y, x + width, y + 12, 0xAA201810);
        graphics.renderOutline(x, y, width, 12, color | 0xFF000000);
        graphics.drawString(font, font.plainSubstrByWidth(text.getString(), width - 8), x + 4, y + 2, color, false);
    }

    public static void commandButton(GuiGraphics graphics, Font font, int mouseX, int mouseY,
                                     int x, int y, int width, int height, Component label,
                                     boolean enabled, boolean selected) {
        boolean hovered = mouseX >= x && mouseX < x + width && mouseY >= y && mouseY < y + height;
        int bg = selected ? 0xD05A4025 : (hovered && enabled ? 0xCC4B3928 : 0xB82A2119);
        int outline = selected || (hovered && enabled) ? OUTLINE_GOLD : OUTLINE_WOOD;
        int textColor = enabled ? TEXT : TEXT_MUTED;
        if (!enabled && !selected) {
            bg = 0x90201810;
            outline = 0x805C4A36;
        }
        graphics.fill(x, y, x + width, y + height, bg);
        graphics.renderOutline(x, y, width, height, outline);
        String clamped = clampLabel(font, label.getString(), Math.max(0, width - 6));
        graphics.drawCenteredString(font, clamped, x + width / 2, y + (height - 8) / 2, textColor);
    }

    /**
     * Clamps a raw string to fit within {@code maxWidth} pixels using the given font.
     * Adds an ellipsis when truncation occurs. Safe for null / empty inputs.
     */
    public static String clampLabel(Font font, String text, int maxWidth) {
        if (text == null || text.isEmpty() || maxWidth <= 0) {
            return text == null ? "" : text;
        }
        if (font.width(text) <= maxWidth) {
            return text;
        }
        String ellipsis = "...";
        int budget = Math.max(0, maxWidth - font.width(ellipsis));
        String head = font.plainSubstrByWidth(text, budget);
        return head + ellipsis;
    }

    /**
     * Component-friendly overload: returns a clamped {@link Component} that preserves
     * the original component when it already fits, and falls back to a literal when truncated.
     */
    public static Component clampLabel(Font font, Component label, int maxWidth) {
        if (label == null) {
            return Component.literal("");
        }
        String raw = label.getString();
        if (font.width(raw) <= maxWidth) {
            return label;
        }
        return Component.literal(clampLabel(font, raw, maxWidth));
    }
}
