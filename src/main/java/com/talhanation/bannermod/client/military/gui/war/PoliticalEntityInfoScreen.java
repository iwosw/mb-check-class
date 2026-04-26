package com.talhanation.bannermod.client.military.gui.war;

import com.talhanation.bannermod.war.client.WarClientState;
import com.talhanation.bannermod.war.registry.PoliticalEntityRecord;
import com.talhanation.bannermod.war.registry.PoliticalEntityStatus;
import com.talhanation.bannermod.war.runtime.WarDeclarationRecord;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Read-only political entity info: name, status, leader, co-leaders, capital, charter, ideology,
 * home region, and the wars this entity participates in. Sourced from {@link WarClientState}.
 *
 * <p>Open via {@link WarListScreen} "Attacker info" / "Defender info" buttons. Long-form text
 * fields (charter, ideology) are word-wrapped to the panel width.</p>
 */
public class PoliticalEntityInfoScreen extends Screen {
    private static final int W = 320;
    private static final int H = 240;

    private final Screen parent;
    private final UUID entityId;

    private int guiLeft;
    private int guiTop;
    private int scrollOffset = 0;

    public PoliticalEntityInfoScreen(@Nullable Screen parent, UUID entityId) {
        super(Component.literal("State Info"));
        this.parent = parent;
        this.entityId = entityId;
    }

    @Override
    protected void init() {
        super.init();
        this.guiLeft = (this.width - W) / 2;
        this.guiTop = (this.height - H) / 2;

        addRenderableWidget(Button.builder(Component.literal("Back"), btn -> onClose())
                .bounds(guiLeft + W - 80 - 8, guiTop + H - 24, 80, 18).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.fill(guiLeft, guiTop, guiLeft + W, guiTop + H, 0xC0101010);
        graphics.renderOutline(guiLeft, guiTop, W, H, 0xFFFFFFFF);

        PoliticalEntityRecord entity = WarClientState.entityById(entityId);
        if (entity == null) {
            graphics.drawCenteredString(font, "Entity not found", guiLeft + W / 2, guiTop + 12, 0xFFFFAA55);
            graphics.drawCenteredString(font, shortId(entityId), guiLeft + W / 2, guiTop + 28, 0xAAAAAA);
            super.render(graphics, mouseX, mouseY, partialTick);
            return;
        }

        int x = guiLeft + 8;
        int y = guiTop + 8;
        graphics.drawString(font, entity.name().isBlank() ? shortId(entityId) : entity.name(), x, y, 0xFFFFFF, false);
        graphics.drawString(font, "[" + entity.status().name() + "]", x, y + 12, statusColor(entity.status()), false);

        y += 28;
        graphics.drawString(font, "Leader: " + shortId(entity.leaderUuid()), x, y, 0xFFFFFF, false);
        y += 12;
        graphics.drawString(font, "Co-leaders: " + entity.coLeaderUuids().size(), x, y, 0xFFFFFF, false);
        y += 12;
        graphics.drawString(font, "Capital: " + (entity.capitalPos() == null ? "(none)" : entity.capitalPos().toShortString()),
                x, y, 0xFFFFFF, false);
        y += 12;
        graphics.drawString(font, "Color: " + (entity.color().isBlank() ? "(none)" : entity.color()), x, y, 0xFFFFFF, false);
        y += 12;
        graphics.drawString(font, "Home region: " + (entity.homeRegion().isBlank() ? "(none)" : entity.homeRegion()),
                x, y, 0xFFFFFF, false);
        y += 16;

        graphics.drawString(font, "Charter:", x, y, 0xAAFFAA, false);
        y += 11;
        y = drawWrapped(graphics, entity.charter().isBlank() ? "(empty)" : entity.charter(), x, y, W - 16);
        y += 6;
        graphics.drawString(font, "Ideology:", x, y, 0xAAAAFF, false);
        y += 11;
        y = drawWrapped(graphics, entity.ideology().isBlank() ? "(empty)" : entity.ideology(), x, y, W - 16);

        renderInvolvedWarsFooter(graphics, entity);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderInvolvedWarsFooter(GuiGraphics graphics, PoliticalEntityRecord entity) {
        List<WarDeclarationRecord> involved = new ArrayList<>();
        for (WarDeclarationRecord war : WarClientState.wars()) {
            if (war.involves(entity.id())) involved.add(war);
        }
        int x = guiLeft + 8;
        int y = guiTop + H - 24 - involved.size() * 11 - 14;
        graphics.drawString(font, "Active wars: " + involved.size(), x, y, 0xFFFF7777, false);
        for (int i = 0; i < involved.size() && i < 4; i++) {
            WarDeclarationRecord war = involved.get(i);
            String line = "  [" + war.state().name() + "] " + war.goalType().name() + " · id=" + shortId(war.id());
            graphics.drawString(font, font.plainSubstrByWidth(line, W - 16), x, y + 11 + i * 11, 0xFFFFFF, false);
        }
    }

    private int drawWrapped(GuiGraphics graphics, String text, int x, int y, int maxWidth) {
        List<FormattedCharSequence> lines = font.split(Component.literal(text), maxWidth);
        for (int i = 0; i < lines.size() && i < 4; i++) {
            graphics.drawString(font, lines.get(i), x, y + i * 11, 0xFFFFFF, false);
        }
        return y + Math.min(lines.size(), 4) * 11;
    }

    private static int statusColor(PoliticalEntityStatus status) {
        return switch (status) {
            case STATE -> 0xFF55FF55;
            case VASSAL -> 0xFFFFFF55;
            case PEACEFUL -> 0xFF55AAFF;
            case SETTLEMENT -> 0xFFAAAAAA;
        };
    }

    private static String shortId(UUID id) {
        if (id == null) return "?";
        String s = id.toString();
        return s.length() > 8 ? s.substring(0, 8) : s;
    }

    @Override
    public void onClose() {
        if (parent != null) {
            this.minecraft.setScreen(parent);
        } else {
            super.onClose();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
