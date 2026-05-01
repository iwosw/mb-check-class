package com.talhanation.bannermod.client.civilian.gui;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.items.civilian.SettlementSurveyorToolItem;
import com.talhanation.bannermod.network.messages.civilian.MessageModifySurveyorSession;
import com.talhanation.bannermod.network.messages.civilian.MessageSetSurveyorMode;
import com.talhanation.bannermod.network.messages.civilian.MessageSetSurveyorRole;
import com.talhanation.bannermod.network.messages.civilian.MessageValidateSurveyorSession;
import com.talhanation.bannermod.settlement.building.ZoneRole;
import com.talhanation.bannermod.settlement.building.ZoneSelection;
import com.talhanation.bannermod.settlement.validation.SurveyorModeGuidance;
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
import java.util.Locale;

public class SettlementSurveyorScreen extends Screen {
    private static final int PANEL_MAX_WIDTH = 308;
    private static final int PANEL_HEIGHT = 308;
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
    private Button actionsButton;
    private Button closeButton;
    private Button cancelCornerButton;
    private Button clearRoleButton;
    private Button resetMarksButton;
    private boolean actionsMenuOpen;

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
        int actionButtonWidth = (contentWidth - SECTION_SPACING * 2) / 3;
        int actionY = top + PANEL_HEIGHT - 30;

        this.modeButton = this.addRenderableWidget(new ExtendedButton(left + CONTENT_PADDING, top + 24, contentWidth, 20,
                Component.empty(), button -> cycleMode()));
        this.modeButton.setTooltip(Tooltip.create(Component.translatable("bannermod.surveyor.screen.mode.tooltip")));

        this.roleButton = this.addRenderableWidget(new ExtendedButton(left + CONTENT_PADDING, top + 48, contentWidth, 20,
                Component.empty(), button -> cycleRole()));
        this.roleButton.setTooltip(Tooltip.create(Component.translatable("bannermod.surveyor.screen.role.tooltip")));

        this.validateButton = this.addRenderableWidget(new ExtendedButton(left + CONTENT_PADDING, actionY, actionButtonWidth, 20,
                Component.translatable("bannermod.surveyor.screen.validate"), button -> validate()));
        this.actionsButton = this.addRenderableWidget(new ExtendedButton(left + CONTENT_PADDING + actionButtonWidth + SECTION_SPACING, actionY, actionButtonWidth, 20,
                Component.translatable("bannermod.surveyor.screen.actions"), button -> toggleActionsMenu()));
        this.closeButton = this.addRenderableWidget(new ExtendedButton(left + CONTENT_PADDING + (actionButtonWidth + SECTION_SPACING) * 2, actionY, actionButtonWidth, 20,
                Component.translatable("gui.bannermod.common.close"), button -> onClose()));

        int menuLeft = actionMenuLeft();
        int menuTop = actionMenuTop();
        int menuWidth = actionMenuWidth();
        this.cancelCornerButton = this.addRenderableWidget(new ExtendedButton(menuLeft, menuTop, menuWidth, 20,
                Component.translatable("bannermod.surveyor.screen.action.cancel_corner"),
                button -> performAction(MessageModifySurveyorSession.Action.CANCEL_PENDING_CORNER)));
        this.clearRoleButton = this.addRenderableWidget(new ExtendedButton(menuLeft, menuTop + 22, menuWidth, 20,
                Component.translatable("bannermod.surveyor.screen.action.clear_role"),
                button -> performAction(MessageModifySurveyorSession.Action.CLEAR_CURRENT_ROLE)));
        this.resetMarksButton = this.addRenderableWidget(new ExtendedButton(menuLeft, menuTop + 44, menuWidth, 20,
                Component.translatable("bannermod.surveyor.screen.action.reset_all"),
                button -> performAction(MessageModifySurveyorSession.Action.RESET_ALL_MARKS)));
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
        ValidationSession session = SurveyorSessionCodec.read(stack);
        SurveyorMode mode = session == null ? SurveyorMode.BOOTSTRAP_FORT : session.mode();
        List<ZoneRole> roles = SurveyorModeGuidance.requiredRoles(mode);
        if (roles.size() <= 1) {
            return;
        }
        ZoneRole current = SettlementSurveyorToolItem.selectedRole(stack);
        int currentIndex = Math.max(0, roles.indexOf(current));
        ZoneRole next = roles.get((currentIndex + 1) % roles.size());
        SettlementSurveyorToolItem.setSelectedRole(stack, next);
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageSetSurveyorRole(this.hand, next));
        syncButtons();
    }

    private void validate() {
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageValidateSurveyorSession(this.hand));
    }

    private void toggleActionsMenu() {
        this.actionsMenuOpen = !this.actionsMenuOpen;
        syncButtons();
    }

    private void performAction(MessageModifySurveyorSession.Action action) {
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageModifySurveyorSession(this.hand, action));
        this.actionsMenuOpen = false;
        syncButtons();
    }

    private void syncButtons() {
        ItemStack stack = currentStack();
        ValidationSession session = SurveyorSessionCodec.read(stack);
        SurveyorMode mode = session == null ? SurveyorMode.BOOTSTRAP_FORT : session.mode();
        if (this.modeButton != null) {
            this.modeButton.setMessage(Component.translatable("bannermod.surveyor.screen.mode", SettlementSurveyorToolItem.modeLabel(mode)));
            this.modeButton.setTooltip(Tooltip.create(modeHint(mode)));
        }
        if (this.roleButton != null) {
            ZoneRole selectedRole = SettlementSurveyorToolItem.selectedRole(stack);
            List<ZoneRole> requiredRoles = SurveyorModeGuidance.requiredRoles(mode);
            this.roleButton.setMessage(Component.translatable("bannermod.surveyor.screen.role", SettlementSurveyorToolItem.roleLabel(selectedRole)));
            this.roleButton.active = requiredRoles.size() > 1;
            this.roleButton.setTooltip(requiredRoles.isEmpty()
                    ? Tooltip.create(Component.translatable("bannermod.surveyor.screen.role.tooltip.inspect"))
                    : requiredRoles.size() == 1
                    ? Tooltip.create(Component.translatable("bannermod.surveyor.screen.role.tooltip.fixed", SettlementSurveyorToolItem.roleLabel(requiredRoles.get(0)), roleHint(requiredRoles.get(0))))
                    : Tooltip.create(Component.translatable("bannermod.surveyor.screen.role.tooltip.cycle", SettlementSurveyorToolItem.roleLabel(selectedRole))));
        }
        if (this.validateButton != null) {
            boolean hasAnchor = session != null && !session.anchorPos().equals(BlockPos.ZERO);
            boolean ready = validationReady(mode, session);
            this.validateButton.active = ready || mode == SurveyorMode.INSPECT_EXISTING && hasAnchor;
            ZoneRole nextMissingRole = SurveyorModeGuidance.nextMissingRole(mode, session);
            this.validateButton.setTooltip(!hasAnchor
                    ? Tooltip.create(Component.translatable("bannermod.surveyor.screen.validate.need_anchor"))
                    : this.validateButton.active
                    ? Tooltip.create(Component.translatable("bannermod.surveyor.screen.validate.tooltip"))
                    : nextMissingRole != null
                    ? Tooltip.create(Component.translatable("bannermod.surveyor.screen.validate.need_role", SettlementSurveyorToolItem.roleLabel(nextMissingRole), roleHint(nextMissingRole)))
                    : Tooltip.create(Component.translatable("bannermod.surveyor.screen.validate.need_roles")));
        }
        if (this.actionsButton != null) {
            this.actionsButton.setTooltip(Tooltip.create(Component.translatable("bannermod.surveyor.screen.actions.tooltip")));
        }
        if (this.cancelCornerButton != null) {
            boolean hasPendingCorner = SettlementSurveyorToolItem.hasPendingCorner(stack);
            this.cancelCornerButton.visible = this.actionsMenuOpen;
            this.cancelCornerButton.active = hasPendingCorner;
            this.cancelCornerButton.setTooltip(Tooltip.create(Component.translatable(
                    hasPendingCorner ? "bannermod.surveyor.screen.action.cancel_corner.tooltip" : "bannermod.surveyor.screen.action.cancel_corner.disabled")));
        }
        if (this.clearRoleButton != null) {
            boolean hasRoleZone = SettlementSurveyorToolItem.hasZoneForSelectedRole(stack);
            this.clearRoleButton.visible = this.actionsMenuOpen;
            this.clearRoleButton.active = hasRoleZone;
            this.clearRoleButton.setTooltip(Tooltip.create(Component.translatable(
                    hasRoleZone ? "bannermod.surveyor.screen.action.clear_role.tooltip" : "bannermod.surveyor.screen.action.clear_role.disabled",
                    SettlementSurveyorToolItem.roleLabel(SettlementSurveyorToolItem.selectedRole(stack)))));
        }
        if (this.resetMarksButton != null) {
            boolean hasAnyMarks = SettlementSurveyorToolItem.hasAnyMarks(stack);
            this.resetMarksButton.visible = this.actionsMenuOpen;
            this.resetMarksButton.active = hasAnyMarks;
            this.resetMarksButton.setTooltip(Tooltip.create(Component.translatable(
                    hasAnyMarks ? "bannermod.surveyor.screen.action.reset_all.tooltip" : "bannermod.surveyor.screen.action.reset_all.disabled")));
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
        if (this.actionsMenuOpen) {
            graphics.fill(actionMenuLeft() - 4, actionMenuTop() - 4, actionMenuLeft() + actionMenuWidth() + 4, actionMenuTop() + 68, PANEL_COLOR);
            graphics.fill(actionMenuLeft() - 3, actionMenuTop() - 3, actionMenuLeft() + actionMenuWidth() + 3, actionMenuTop() + 67, PANEL_INNER_COLOR);
            graphics.renderOutline(actionMenuLeft() - 4, actionMenuTop() - 4, actionMenuWidth() + 8, 72, PANEL_OUTLINE_COLOR);
        }

        ItemStack stack = currentStack();
        ValidationSession session = SurveyorSessionCodec.read(stack);
        SurveyorMode mode = session == null ? SurveyorMode.BOOTSTRAP_FORT : session.mode();
        ZoneRole selectedRole = SettlementSurveyorToolItem.selectedRole(stack);
        BlockPos pendingCorner = SettlementSurveyorToolItem.pendingCorner(stack);

        graphics.drawString(this.font, this.title, contentX, top + 8, TITLE_COLOR, true);

        int infoY = drawWrapped(graphics, Component.translatable("bannermod.surveyor.screen.capture_hint"), contentX, top + 74, contentWidth, MUTED_TEXT_COLOR);
        infoY = drawWrapped(graphics, Component.translatable("bannermod.surveyor.screen.current_role", SettlementSurveyorToolItem.roleLabel(selectedRole), roleHint(selectedRole)), contentX, infoY + 4, contentWidth, TEXT_COLOR);
        infoY = drawWrapped(graphics, currentStep(mode, session), contentX, infoY + 4, contentWidth, PENDING_COLOR);
        graphics.drawString(this.font, Component.translatable("bannermod.surveyor.screen.anchor",
                session == null || session.anchorPos().equals(BlockPos.ZERO) ? "-" : session.anchorPos().toShortString()), contentX, infoY + 4, TEXT_COLOR, true);
        int sectionY = infoY + 20;
        if (pendingCorner != null) {
            graphics.drawString(this.font, Component.translatable("bannermod.surveyor.screen.pending", pendingCorner.toShortString()), contentX, infoY + 16, TEXT_COLOR, true);
            sectionY += 12;
        }

        if (panelWidth >= 280) {
            int columnWidth = (contentWidth - SECTION_SPACING) / 2;
            int reqBottom = drawRequirements(graphics, contentX, sectionY, columnWidth, mode, session);
            int zonesBottom = drawZones(graphics, contentX + columnWidth + SECTION_SPACING, sectionY, columnWidth, session);
            sectionY = Math.max(reqBottom, zonesBottom) + SECTION_SPACING;
        } else {
            sectionY = drawRequirements(graphics, contentX, sectionY, contentWidth, mode, session) + SECTION_SPACING;
            sectionY = drawZones(graphics, contentX, sectionY, contentWidth, session) + SECTION_SPACING;
        }

        drawChecklist(graphics, contentX, sectionY, contentWidth, mode, session);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.actionsMenuOpen && !insideActionMenu(mouseX, mouseY) && (this.actionsButton == null || !this.actionsButton.isMouseOver(mouseX, mouseY))) {
            this.actionsMenuOpen = false;
            syncButtons();
        }
        return super.mouseClicked(mouseX, mouseY, button);
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
                    .append(Component.literal(": " + sizeX(selection) + "x" + sizeY(selection) + "x" + sizeZ(selection) + " @ " + selection.min().toShortString()));
            lineY = drawWrapped(graphics, line, x, lineY, width, TEXT_COLOR);
            shown++;
        }
        if (session.selections().size() > shown) {
            lineY = drawWrapped(graphics, Component.translatable("bannermod.surveyor.screen.zones.more", session.selections().size() - shown), x, lineY, width, MUTED_TEXT_COLOR);
        }
        return lineY;
    }

    private int drawChecklist(GuiGraphics graphics, int x, int y, int width, SurveyorMode mode, @Nullable ValidationSession session) {
        graphics.drawString(this.font, Component.translatable("bannermod.surveyor.screen.checklist"), x, y, TITLE_COLOR, true);
        int lineY = y + LINE_HEIGHT;
        boolean hasAnchor = session != null && !session.anchorPos().equals(BlockPos.ZERO);
        lineY = drawWrapped(graphics, checklistLine(hasAnchor, Component.translatable("bannermod.surveyor.screen.check.anchor")), x, lineY, width, hasAnchor ? COMPLETE_COLOR : PENDING_COLOR);
        for (ZoneRole role : requiredRoles(mode)) {
            boolean complete = hasRole(session, role);
            lineY = drawWrapped(graphics, checklistLine(complete, SettlementSurveyorToolItem.roleLabel(role)), x, lineY, width, complete ? COMPLETE_COLOR : PENDING_COLOR);
        }
        boolean ready = validationReady(mode, session);
        return drawWrapped(graphics, checklistLine(ready, Component.translatable("bannermod.surveyor.screen.check.ready")), x, lineY, width, ready ? COMPLETE_COLOR : PENDING_COLOR);
    }

    private boolean validationReady(SurveyorMode mode, @Nullable ValidationSession session) {
        if (session == null || session.anchorPos().equals(BlockPos.ZERO)) {
            return false;
        }
        return requiredRoles(mode).stream().allMatch(role -> hasRole(session, role));
    }

    private Component checklistLine(boolean complete, Component label) {
        return Component.literal(complete ? "[x] " : "[ ] ").append(label);
    }

    private List<ZoneRole> requiredRoles(SurveyorMode mode) {
        List<ZoneRole> roles = new ArrayList<>(SurveyorModeGuidance.requiredRoles(mode));
        roles.sort(Comparator.comparingInt(Enum::ordinal));
        return roles;
    }

    private Component modeHint(SurveyorMode mode) {
        return Component.translatable("bannermod.surveyor.mode_hint." + (mode == null ? SurveyorMode.BOOTSTRAP_FORT : mode).name().toLowerCase(Locale.ROOT));
    }

    private Component roleHint(ZoneRole role) {
        return Component.translatable("bannermod.surveyor.role_hint." + (role == null ? ZoneRole.INTERIOR : role).name().toLowerCase(Locale.ROOT));
    }

    private Component currentStep(SurveyorMode mode, @Nullable ValidationSession session) {
        if (session == null || session.anchorPos().equals(BlockPos.ZERO)) {
            return Component.translatable("bannermod.surveyor.screen.next.anchor");
        }
        ZoneRole nextMissingRole = SurveyorModeGuidance.nextMissingRole(mode, session);
        if (nextMissingRole != null) {
            return Component.translatable("bannermod.surveyor.screen.next.role", SettlementSurveyorToolItem.roleLabel(nextMissingRole), roleHint(nextMissingRole));
        }
        return Component.translatable("bannermod.surveyor.screen.next.validate");
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

    private int sizeX(ZoneSelection selection) {
        return Math.abs(selection.max().getX() - selection.min().getX()) + 1;
    }

    private int sizeY(ZoneSelection selection) {
        return Math.abs(selection.max().getY() - selection.min().getY()) + 1;
    }

    private int sizeZ(ZoneSelection selection) {
        return Math.abs(selection.max().getZ() - selection.min().getZ()) + 1;
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

    private int actionMenuWidth() {
        return Math.min(172, contentWidth());
    }

    private int actionMenuLeft() {
        int actionButtonWidth = (contentWidth() - SECTION_SPACING * 2) / 3;
        int actionsLeft = panelLeft() + CONTENT_PADDING + actionButtonWidth + SECTION_SPACING;
        return actionsLeft + Math.max(0, (actionButtonWidth - actionMenuWidth()) / 2);
    }

    private int actionMenuTop() {
        int actionY = panelTop() + PANEL_HEIGHT - 30;
        return actionY - 70;
    }

    private boolean insideActionMenu(double mouseX, double mouseY) {
        return mouseX >= actionMenuLeft() - 4
                && mouseX <= actionMenuLeft() + actionMenuWidth() + 4
                && mouseY >= actionMenuTop() - 4
                && mouseY <= actionMenuTop() + 68;
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
