package com.talhanation.bannermod.client.civilian.gui;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.items.civilian.SettlementSurveyorToolItem;
import com.talhanation.bannermod.network.messages.civilian.MessageSetSurveyorMode;
import com.talhanation.bannermod.network.messages.civilian.MessageSetSurveyorRole;
import com.talhanation.bannermod.network.messages.civilian.MessageValidateSurveyorSession;
import com.talhanation.bannermod.settlement.building.BuildingDefinition;
import com.talhanation.bannermod.settlement.building.BuildingDefinitionRegistry;
import com.talhanation.bannermod.settlement.building.BuildingType;
import com.talhanation.bannermod.settlement.building.ZoneRole;
import com.talhanation.bannermod.settlement.building.ZoneSelection;
import com.talhanation.bannermod.settlement.validation.SurveyorMode;
import com.talhanation.bannermod.settlement.validation.SurveyorSessionCodec;
import com.talhanation.bannermod.settlement.validation.ValidationSession;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FormattedCharSequence;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

public class SettlementSurveyorScreen extends Screen {
    private static final BuildingDefinitionRegistry DEFINITIONS = new BuildingDefinitionRegistry();
    private static final int PANEL_MAX_WIDTH = 308;
    private static final int PANEL_HEIGHT = 244;
    private static final int PANEL_MARGIN = 10;
    private static final int CONTENT_PADDING = 10;
    private static final int SECTION_SPACING = 8;
    private static final int LINE_HEIGHT = 12;
    private static final int TITLE_COLOR = 0xFFFFFFFF;
    private static final int TEXT_COLOR = 0xFFF1F1F1;
    private static final int MUTED_TEXT_COLOR = 0xFFD4D4D4;
    private static final int COMPLETE_COLOR = 0xFF86D36B;
    private static final int PENDING_COLOR = 0xFFE3C16F;
    private static final int PANEL_COLOR = 0xFF121212;
    private static final int PANEL_INNER_COLOR = 0xFF1D1D1D;
    private static final int PANEL_OUTLINE_COLOR = 0xFF6F6F6F;

    private final InteractionHand hand;
    private Button modeButton;
    private Button roleButton;
    private Button validateButton;

    public SettlementSurveyorScreen(InteractionHand hand) {
        super(Component.translatable("bannermod.surveyor.screen.title"));
        this.hand = hand;
    }

    public static void open(InteractionHand hand) {
        Minecraft.getInstance().setScreen(new SettlementSurveyorScreen(hand));
    }

    @Override
    protected void init() {
        super.init();
        int left = panelLeft();
        int top = panelTop();
        int contentWidth = contentWidth();
        int actionButtonWidth = (contentWidth - SECTION_SPACING) / 2;
        int actionY = top + PANEL_HEIGHT - 30;

        this.modeButton = this.addRenderableWidget(new ExtendedButton(left + CONTENT_PADDING, top + 24, contentWidth, 20,
                Component.empty(), button -> cycleMode()));
        this.modeButton.setTooltip(Tooltip.create(Component.translatable("bannermod.surveyor.screen.mode.tooltip")));

        this.roleButton = this.addRenderableWidget(new ExtendedButton(left + CONTENT_PADDING, top + 48, contentWidth, 20,
                Component.empty(), button -> cycleRole()));
        this.roleButton.setTooltip(Tooltip.create(Component.translatable("bannermod.surveyor.screen.role.tooltip")));

        this.validateButton = this.addRenderableWidget(new ExtendedButton(left + CONTENT_PADDING, actionY, actionButtonWidth, 20,
                Component.translatable("bannermod.surveyor.screen.validate"), button -> validate()));
        this.addRenderableWidget(new ExtendedButton(left + CONTENT_PADDING + actionButtonWidth + SECTION_SPACING, actionY, actionButtonWidth, 20,
                Component.translatable("gui.bannermod.common.close"), button -> onClose()));
        syncButtons();
    }

    @Override
    public void tick() {
        super.tick();
        syncButtons();
        if (!(currentStack().getItem() instanceof SettlementSurveyorToolItem)) {
            this.onClose();
        }
    }

    private void cycleMode() {
        Player player = minecraft == null ? null : minecraft.player;
        ItemStack stack = currentStack();
        if (player == null || !(stack.getItem() instanceof SettlementSurveyorToolItem)) {
            return;
        }
        ValidationSession session = SettlementSurveyorToolItem.getOrCreateSession(player, stack);
        SurveyorMode[] modes = SurveyorMode.values();
        SurveyorMode next = modes[(session.mode().ordinal() + 1) % modes.length];
        SettlementSurveyorToolItem.setMode(player, stack, next);
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageSetSurveyorMode(this.hand, next));
        syncButtons();
    }

    private void cycleRole() {
        ItemStack stack = currentStack();
        if (!(stack.getItem() instanceof SettlementSurveyorToolItem)) {
            return;
        }
        ZoneRole[] roles = ZoneRole.values();
        ZoneRole current = SettlementSurveyorToolItem.selectedRole(stack);
        ZoneRole next = roles[(current.ordinal() + 1) % roles.length];
        SettlementSurveyorToolItem.setSelectedRole(stack, next);
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageSetSurveyorRole(this.hand, next));
        syncButtons();
    }

    private void validate() {
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageValidateSurveyorSession(this.hand));
    }

    private void syncButtons() {
        ItemStack stack = currentStack();
        ValidationSession session = SurveyorSessionCodec.read(stack);
        SurveyorMode mode = session == null ? SurveyorMode.BOOTSTRAP_FORT : session.mode();
        if (this.modeButton != null) {
            this.modeButton.setMessage(Component.translatable("bannermod.surveyor.screen.mode", SettlementSurveyorToolItem.modeLabel(mode)));
        }
        if (this.roleButton != null) {
            this.roleButton.setMessage(Component.translatable("bannermod.surveyor.screen.role", SettlementSurveyorToolItem.roleLabel(SettlementSurveyorToolItem.selectedRole(stack))));
        }
        if (this.validateButton != null) {
            boolean hasAnchor = session != null && !session.anchorPos().equals(BlockPos.ZERO);
            this.validateButton.active = hasAnchor;
            this.validateButton.setTooltip(hasAnchor
                    ? Tooltip.create(Component.translatable("bannermod.surveyor.screen.validate.tooltip"))
                    : Tooltip.create(Component.translatable("bannermod.surveyor.screen.validate.need_anchor")));
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        int panelWidth = panelWidth();
        int left = panelLeft();
        int top = panelTop();
        int contentX = left + CONTENT_PADDING;
        int contentWidth = contentWidth();
        graphics.fill(0, 0, this.width, this.height, 0x78000000);
        graphics.fill(left, top, left + panelWidth, top + PANEL_HEIGHT, PANEL_COLOR);
        graphics.fill(left + 1, top + 1, left + panelWidth - 1, top + PANEL_HEIGHT - 1, PANEL_INNER_COLOR);
        graphics.renderOutline(left, top, panelWidth, PANEL_HEIGHT, PANEL_OUTLINE_COLOR);

        ItemStack stack = currentStack();
        ValidationSession session = SurveyorSessionCodec.read(stack);
        SurveyorMode mode = session == null ? SurveyorMode.BOOTSTRAP_FORT : session.mode();
        BlockPos pendingCorner = SettlementSurveyorToolItem.pendingCorner(stack);

        graphics.drawString(this.font, this.title, contentX, top + 8, TITLE_COLOR, true);

        int infoY = drawWrapped(graphics, Component.translatable("bannermod.surveyor.screen.capture_hint"), contentX, top + 74, contentWidth, MUTED_TEXT_COLOR);
        graphics.drawString(this.font, Component.translatable("bannermod.surveyor.screen.anchor",
                session == null || session.anchorPos().equals(BlockPos.ZERO) ? "-" : session.anchorPos().toShortString()), contentX, infoY + 4, TEXT_COLOR, true);
        graphics.drawString(this.font, Component.translatable("bannermod.surveyor.screen.pending",
                pendingCorner == null ? "-" : pendingCorner.toShortString()), contentX, infoY + 16, TEXT_COLOR, true);

        int sectionY = infoY + 32;
        if (panelWidth >= 280) {
            int columnWidth = (contentWidth - SECTION_SPACING) / 2;
            int reqBottom = drawRequirements(graphics, contentX, sectionY, columnWidth, mode, session);
            int zonesBottom = drawZones(graphics, contentX + columnWidth + SECTION_SPACING, sectionY, columnWidth, session);
            sectionY = Math.max(reqBottom, zonesBottom) + SECTION_SPACING;
        } else {
            sectionY = drawRequirements(graphics, contentX, sectionY, contentWidth, mode, session) + SECTION_SPACING;
            sectionY = drawZones(graphics, contentX, sectionY, contentWidth, session) + SECTION_SPACING;
        }

        drawChecklist(graphics, contentX, sectionY, contentWidth);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    private int drawRequirements(GuiGraphics graphics, int x, int y, int width, SurveyorMode mode, @Nullable ValidationSession session) {
        graphics.drawString(this.font, Component.translatable("bannermod.surveyor.screen.requirements"), x, y, TITLE_COLOR, true);
        List<ZoneRole> requiredRoles = requiredRoles(mode);
        if (requiredRoles.isEmpty()) {
            return drawWrapped(graphics, Component.translatable("bannermod.surveyor.screen.requirements.none"), x, y + LINE_HEIGHT, width, MUTED_TEXT_COLOR);
        }
        int lineY = y + LINE_HEIGHT;
        for (ZoneRole role : requiredRoles) {
            boolean complete = hasRole(session, role);
            Component line = Component.literal(complete ? "[x] " : "[ ] ")
                    .append(SettlementSurveyorToolItem.roleLabel(role));
            lineY = drawWrapped(graphics, line, x, lineY, width, complete ? COMPLETE_COLOR : PENDING_COLOR);
        }
        return lineY;
    }

    private int drawZones(GuiGraphics graphics, int x, int y, int width, @Nullable ValidationSession session) {
        graphics.drawString(this.font, Component.translatable("bannermod.surveyor.screen.zones"), x, y, TITLE_COLOR, true);
        if (session == null || session.selections().isEmpty()) {
            return drawWrapped(graphics, Component.translatable("bannermod.surveyor.screen.zones.none"), x, y + LINE_HEIGHT, width, MUTED_TEXT_COLOR);
        }
        int lineY = y + LINE_HEIGHT;
        int shown = 0;
        for (ZoneSelection selection : session.selections()) {
            if (shown == 4) {
                break;
            }
            Component line = SettlementSurveyorToolItem.roleLabel(selection.role())
                    .copy()
                    .append(Component.literal(": " + selection.min().toShortString()));
            lineY = drawWrapped(graphics, line, x, lineY, width, TEXT_COLOR);
            shown++;
        }
        if (session.selections().size() > shown) {
            lineY = drawWrapped(graphics, Component.translatable("bannermod.surveyor.screen.zones.more", session.selections().size() - shown), x, lineY, width, MUTED_TEXT_COLOR);
        }
        return lineY;
    }

    private int drawChecklist(GuiGraphics graphics, int x, int y, int width) {
        graphics.drawString(this.font, Component.translatable("bannermod.surveyor.screen.checklist"), x, y, TITLE_COLOR, true);
        int lineY = y + LINE_HEIGHT;
        lineY = drawWrapped(graphics, Component.translatable("bannermod.surveyor.screen.checklist.1"), x, lineY, width, TEXT_COLOR);
        lineY = drawWrapped(graphics, Component.translatable("bannermod.surveyor.screen.checklist.2"), x, lineY, width, TEXT_COLOR);
        return drawWrapped(graphics, Component.translatable("bannermod.surveyor.screen.checklist.3"), x, lineY, width, TEXT_COLOR);
    }

    private List<ZoneRole> requiredRoles(SurveyorMode mode) {
        BuildingType type = switch (mode) {
            case BOOTSTRAP_FORT -> BuildingType.STARTER_FORT;
            case HOUSE -> BuildingType.HOUSE;
            case FARM -> BuildingType.FARM;
            case MINE -> BuildingType.MINE;
            case LUMBER_CAMP -> BuildingType.LUMBER_CAMP;
            case SMITHY -> BuildingType.SMITHY;
            case STORAGE -> BuildingType.STORAGE;
            case ARCHITECT_BUILDER -> BuildingType.ARCHITECT_WORKSHOP;
            case INSPECT_EXISTING -> null;
        };
        if (type == null) {
            return List.of();
        }
        BuildingDefinition definition = DEFINITIONS.get(type).orElse(null);
        if (definition == null) {
            return List.of();
        }
        List<ZoneRole> roles = new ArrayList<>(definition.requiredZones());
        roles.sort(Comparator.comparingInt(Enum::ordinal));
        return roles;
    }

    private boolean hasRole(@Nullable ValidationSession session, ZoneRole role) {
        if (session == null) {
            return false;
        }
        for (ZoneSelection selection : session.selections()) {
            if (selection.role() == role) {
                return true;
            }
        }
        return false;
    }

    private ItemStack currentStack() {
        Player player = minecraft == null ? null : minecraft.player;
        return player == null ? ItemStack.EMPTY : player.getItemInHand(this.hand);
    }

    private int drawWrapped(GuiGraphics graphics, Component text, int x, int y, int width, int color) {
        List<FormattedCharSequence> lines = this.font.split(text, width);
        for (FormattedCharSequence line : lines) {
            graphics.drawString(this.font, line, x, y, color, true);
            y += LINE_HEIGHT;
        }
        return y;
    }

    private int panelWidth() {
        return Math.min(PANEL_MAX_WIDTH, this.width - PANEL_MARGIN * 2);
    }

    private int panelLeft() {
        return (this.width - panelWidth()) / 2;
    }

    private int panelTop() {
        return Math.max(PANEL_MARGIN, (this.height - PANEL_HEIGHT) / 2);
    }

    private int contentWidth() {
        return panelWidth() - CONTENT_PADDING * 2;
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
