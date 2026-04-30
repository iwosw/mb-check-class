package com.talhanation.bannermod.client.military.gui.war;

import com.talhanation.bannermod.util.GameProfileUtils;
import com.talhanation.bannermod.war.client.WarClientState;
import com.talhanation.bannermod.war.registry.PoliticalEntityRecord;
import com.talhanation.bannermod.war.registry.PoliticalEntityStatus;
import com.talhanation.bannermod.war.runtime.WarDeclarationRecord;
import com.talhanation.bannermod.war.runtime.WarGoalType;
import com.talhanation.bannermod.war.runtime.WarState;
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
        super(text("gui.bannermod.states.info.title"));
        this.parent = parent;
        this.entityId = entityId;
    }

    @Override
    protected void init() {
        super.init();
        this.guiLeft = (this.width - W) / 2;
        this.guiTop = (this.height - H) / 2;

        addRenderableWidget(Button.builder(text("gui.bannermod.common.back"), btn -> onClose())
                .bounds(guiLeft + W - 80 - 8, guiTop + H - 24, 80, 18).build());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xFF101010);
        graphics.fill(guiLeft, guiTop, guiLeft + W, guiTop + H, 0xC0101010);
        graphics.renderOutline(guiLeft, guiTop, W, H, 0xFFFFFFFF);

        PoliticalEntityRecord entity = WarClientState.entityById(entityId);
        if (entity == null) {
            graphics.drawCenteredString(font, text("gui.bannermod.states.info.not_found"), guiLeft + W / 2, guiTop + 12, 0xFFFFAA55);
            super.render(graphics, mouseX, mouseY, partialTick);
            return;
        }

        int x = guiLeft + 8;
        int y = guiTop + 8;
        graphics.drawString(font, displayName(entity), x, y, 0xFFFFFF, false);
        graphics.drawString(font, text("gui.bannermod.states.info.status_badge", localizedStatus(entity.status())).getString(), x, y + 12, statusColor(entity.status()), false);

        y += 28;
        graphics.drawString(font, text("gui.bannermod.states.detail.leader", playerName(entity.leaderUuid())), x, y, 0xFFFFFF, false);
        y += 12;
        graphics.drawString(font, text("gui.bannermod.states.detail.co_leaders", coLeaderSummary(entity)), x, y, 0xFFFFFF, false);
        y += 12;
        graphics.drawString(font, text("gui.bannermod.states.detail.capital", entity.capitalPos() == null ? text("gui.bannermod.common.none").getString() : entity.capitalPos().toShortString()),
                x, y, 0xFFFFFF, false);
        y += 12;
        graphics.drawString(font, text("gui.bannermod.states.detail.color", entity.color().isBlank() ? text("gui.bannermod.common.none").getString() : entity.color()), x, y, 0xFFFFFF, false);
        y += 12;
        graphics.drawString(font, text("gui.bannermod.states.detail.region", entity.homeRegion().isBlank() ? text("gui.bannermod.common.none").getString() : entity.homeRegion()),
                x, y, 0xFFFFFF, false);
        y += 16;

        graphics.drawString(font, text("gui.bannermod.states.info.charter"), x, y, 0xAAFFAA, false);
        y += 11;
        y = drawWrapped(graphics, entity.charter().isBlank() ? text("gui.bannermod.states.info.empty_text").getString() : entity.charter(), x, y, W - 16);
        y += 6;
        graphics.drawString(font, text("gui.bannermod.states.info.ideology"), x, y, 0xAAAAFF, false);
        y += 11;
        y = drawWrapped(graphics, entity.ideology().isBlank() ? text("gui.bannermod.states.info.empty_text").getString() : entity.ideology(), x, y, W - 16);

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
        graphics.drawString(font, text("gui.bannermod.states.info.active_wars", involved.size()), x, y, 0xFFFF7777, false);
        for (int i = 0; i < involved.size() && i < 4; i++) {
            WarDeclarationRecord war = involved.get(i);
            String line = text("gui.bannermod.states.info.war_line", localizedWarState(war.state()), localizedWarGoal(war.goalType())).getString();
            graphics.drawString(font, font.plainSubstrByWidth(line, W - 16), x, y + 11 + i * 11, 0xFFFFFF, false);
        }
    }

    private static Component text(String key, Object... args) {
        return Component.translatable(key, args);
    }

    private static Component localizedStatus(PoliticalEntityStatus status) {
        return switch (status) {
            case SETTLEMENT -> text("gui.bannermod.states.status.settlement");
            case STATE -> text("gui.bannermod.states.status.state");
            case VASSAL -> text("gui.bannermod.states.status.vassal");
            case PEACEFUL -> text("gui.bannermod.states.status.peaceful");
        };
    }

    private static Component localizedWarState(WarState state) {
        return switch (state) {
            case DECLARED -> text("gui.bannermod.war_list.state.declared");
            case ACTIVE -> text("gui.bannermod.war_list.state.active");
            case IN_SIEGE_WINDOW -> text("gui.bannermod.war_list.state.in_siege_window");
            case RESOLVED -> text("gui.bannermod.war_list.state.resolved");
            case CANCELLED -> text("gui.bannermod.war_list.state.cancelled");
        };
    }

    private static Component localizedWarGoal(WarGoalType goalType) {
        return switch (goalType) {
            case TRIBUTE -> text("gui.bannermod.war_list.goal.tribute");
            case OCCUPATION -> text("gui.bannermod.war_list.goal.occupation");
            case ANNEX_LIMITED_CHUNKS -> text("gui.bannermod.war_list.goal.annex_limited_chunks");
            case VASSALIZATION -> text("gui.bannermod.war_list.goal.vassalization");
            case REGIME_CHANGE -> text("gui.bannermod.war_list.goal.regime_change");
            case WHITE_PEACE -> text("gui.bannermod.war_list.goal.white_peace");
        };
    }

    private static String displayName(PoliticalEntityRecord entity) {
        return entity.name().isBlank() ? text("gui.bannermod.states.unnamed").getString() : entity.name();
    }

    private static String playerName(@Nullable UUID id) {
        if (id == null) {
            return text("gui.bannermod.common.unknown").getString();
        }
        String name = GameProfileUtils.getPlayerName(id);
        return name == null || name.isBlank() ? text("gui.bannermod.common.unknown").getString() : name;
    }

    private static String coLeaderSummary(PoliticalEntityRecord entity) {
        if (entity.coLeaderUuids().isEmpty()) {
            return text("gui.bannermod.common.none").getString();
        }
        List<String> names = new ArrayList<>();
        for (int i = 0; i < Math.min(3, entity.coLeaderUuids().size()); i++) {
            names.add(playerName(entity.coLeaderUuids().get(i)));
        }
        String suffix = entity.coLeaderUuids().size() > names.size() ? " +" + (entity.coLeaderUuids().size() - names.size()) : "";
        return String.join(", ", names) + suffix;
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
