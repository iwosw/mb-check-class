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
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
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
        int left = this.width / 2 - 120;
        int top = this.height / 2 - 90;

        this.modeButton = this.addRenderableWidget(new ExtendedButton(left + 10, top + 24, 220, 20,
                Component.empty(), button -> cycleMode()));
        this.modeButton.setTooltip(Tooltip.create(Component.translatable("bannermod.surveyor.screen.mode.tooltip")));

        this.roleButton = this.addRenderableWidget(new ExtendedButton(left + 10, top + 48, 220, 20,
                Component.empty(), button -> cycleRole()));
        this.roleButton.setTooltip(Tooltip.create(Component.translatable("bannermod.surveyor.screen.role.tooltip")));

        this.validateButton = this.addRenderableWidget(new ExtendedButton(left + 10, top + 154, 106, 20,
                Component.translatable("bannermod.surveyor.screen.validate"), button -> validate()));
        this.addRenderableWidget(new ExtendedButton(left + 124, top + 154, 106, 20,
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
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        int left = this.width / 2 - 120;
        int top = this.height / 2 - 90;
        graphics.fill(left, top, left + 240, top + 182, 0xC0101010);
        graphics.fill(left + 1, top + 1, left + 239, top + 181, 0xD0302418);

        ItemStack stack = currentStack();
        ValidationSession session = SurveyorSessionCodec.read(stack);
        SurveyorMode mode = session == null ? SurveyorMode.BOOTSTRAP_FORT : session.mode();
        BlockPos pendingCorner = SettlementSurveyorToolItem.pendingCorner(stack);

        graphics.drawString(this.font, this.title, left + 10, top + 8, 0xFFF4E4B0, false);
        graphics.drawString(this.font, Component.translatable("bannermod.surveyor.screen.capture_hint"), left + 10, top + 74, 0xFFB8B8B8, false);
        graphics.drawString(this.font, Component.translatable("bannermod.surveyor.screen.anchor",
                session == null || session.anchorPos().equals(BlockPos.ZERO) ? "-" : session.anchorPos().toShortString()), left + 10, top + 88, 0xFFD8D8D8, false);
        graphics.drawString(this.font, Component.translatable("bannermod.surveyor.screen.pending",
                pendingCorner == null ? "-" : pendingCorner.toShortString()), left + 10, top + 100, 0xFFD8D8D8, false);

        drawRequirements(graphics, left + 10, top + 116, mode, session);
        drawZones(graphics, left + 125, top + 74, session);
        drawChecklist(graphics, left + 125, top + 126);

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void drawRequirements(GuiGraphics graphics, int x, int y, SurveyorMode mode, @Nullable ValidationSession session) {
        graphics.drawString(this.font, Component.translatable("bannermod.surveyor.screen.requirements"), x, y, 0xFFF4E4B0, false);
        List<ZoneRole> requiredRoles = requiredRoles(mode);
        if (requiredRoles.isEmpty()) {
            graphics.drawString(this.font, Component.translatable("bannermod.surveyor.screen.requirements.none"), x, y + 12, 0xFFB8B8B8, false);
            return;
        }
        int lineY = y + 12;
        for (ZoneRole role : requiredRoles) {
            boolean complete = hasRole(session, role);
            Component line = Component.literal(complete ? "[x] " : "[ ] ")
                    .append(SettlementSurveyorToolItem.roleLabel(role));
            graphics.drawString(this.font, line, x, lineY, complete ? 0xFF86D36B : 0xFFE3C16F, false);
            lineY += 12;
        }
    }

    private void drawZones(GuiGraphics graphics, int x, int y, @Nullable ValidationSession session) {
        graphics.drawString(this.font, Component.translatable("bannermod.surveyor.screen.zones"), x, y, 0xFFF4E4B0, false);
        if (session == null || session.selections().isEmpty()) {
            graphics.drawString(this.font, Component.translatable("bannermod.surveyor.screen.zones.none"), x, y + 12, 0xFFB8B8B8, false);
            return;
        }
        int lineY = y + 12;
        int shown = 0;
        for (ZoneSelection selection : session.selections()) {
            if (shown == 4) {
                break;
            }
            Component line = SettlementSurveyorToolItem.roleLabel(selection.role())
                    .copy().withStyle(ChatFormatting.GRAY)
                    .append(Component.literal(" " + selection.min().toShortString()));
            graphics.drawString(this.font, line, x, lineY, 0xFFD8D8D8, false);
            lineY += 12;
            shown++;
        }
        if (session.selections().size() > shown) {
            graphics.drawString(this.font, Component.translatable("bannermod.surveyor.screen.zones.more", session.selections().size() - shown), x, lineY, 0xFFB8B8B8, false);
        }
    }

    private void drawChecklist(GuiGraphics graphics, int x, int y) {
        graphics.drawString(this.font, Component.translatable("bannermod.surveyor.screen.checklist"), x, y, 0xFFF4E4B0, false);
        graphics.drawString(this.font, Component.translatable("bannermod.surveyor.screen.checklist.1"), x, y + 12, 0xFFD8D8D8, false);
        graphics.drawString(this.font, Component.translatable("bannermod.surveyor.screen.checklist.2"), x, y + 24, 0xFFD8D8D8, false);
        graphics.drawString(this.font, Component.translatable("bannermod.surveyor.screen.checklist.3"), x, y + 36, 0xFFD8D8D8, false);
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

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
