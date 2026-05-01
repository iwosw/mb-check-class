package com.talhanation.bannermod.client.military.gui.war;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.network.messages.war.MessageDeclareWar;
import com.talhanation.bannermod.war.client.WarClientState;
import com.talhanation.bannermod.war.registry.PoliticalEntityAuthority;
import com.talhanation.bannermod.war.registry.PoliticalEntityRecord;
import com.talhanation.bannermod.war.runtime.WarGoalType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WarDeclareScreen extends Screen {
    private static final int W = 300;
    private static final int H = 150;

    private final Screen parent;
    private final List<PoliticalEntityRecord> attackers = new ArrayList<>();
    private final List<PoliticalEntityRecord> defenders = new ArrayList<>();
    private int guiLeft;
    private int guiTop;
    private int attackerIndex;
    private int defenderIndex;
    private int goalIndex;
    private EditBox casusBelliBox;
    private Button attackerButton;
    private Button defenderButton;
    private Button goalButton;
    private Button declareButton;

    public WarDeclareScreen(Screen parent) {
        super(Component.translatable("gui.bannermod.war_list.declare"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        this.guiLeft = (this.width - W) / 2;
        this.guiTop = (this.height - H) / 2;
        rebuildChoices();

        this.attackerButton = Button.builder(Component.literal(""), btn -> cycleAttacker())
                .bounds(guiLeft + 10, guiTop + 28, 135, 18).build();
        this.defenderButton = Button.builder(Component.literal(""), btn -> cycleDefender())
                .bounds(guiLeft + 155, guiTop + 28, 135, 18).build();
        this.goalButton = Button.builder(Component.literal(""), btn -> cycleGoal())
                .bounds(guiLeft + 10, guiTop + 58, 135, 18).build();
        this.casusBelliBox = new EditBox(this.font, guiLeft + 155, guiTop + 58, 135, 18, Component.translatable("gui.bannermod.war_declare.casus"));
        this.casusBelliBox.setMaxLength(96);
        addRenderableWidget(this.attackerButton);
        addRenderableWidget(this.defenderButton);
        addRenderableWidget(this.goalButton);
        addRenderableWidget(this.casusBelliBox);

        this.declareButton = Button.builder(Component.translatable("gui.bannermod.war_list.declare"), btn -> declareWar())
                .bounds(guiLeft + 10, guiTop + H - 28, 80, 18).build();
        addRenderableWidget(this.declareButton);
        addRenderableWidget(Button.builder(Component.translatable("gui.bannermod.common.cancel"), btn -> onClose())
                .bounds(guiLeft + W - 90, guiTop + H - 28, 80, 18).build());
        updateButtons();
    }

    private void rebuildChoices() {
        this.attackers.clear();
        this.defenders.clear();
        Player player = Minecraft.getInstance().player;
        UUID playerId = player == null ? null : player.getUUID();
        for (PoliticalEntityRecord entity : WarClientState.entities()) {
            if (playerId != null && PoliticalEntityAuthority.canAct(playerId, false, entity)) {
                this.attackers.add(entity);
            }
        }
        this.attackerIndex = Math.min(this.attackerIndex, Math.max(0, this.attackers.size() - 1));
        rebuildDefenders();
    }

    private void rebuildDefenders() {
        this.defenders.clear();
        PoliticalEntityRecord attacker = selectedAttacker();
        for (PoliticalEntityRecord entity : WarClientState.entities()) {
            if (attacker == null || !entity.id().equals(attacker.id())) {
                this.defenders.add(entity);
            }
        }
        this.defenderIndex = Math.min(this.defenderIndex, Math.max(0, this.defenders.size() - 1));
    }

    private void cycleAttacker() {
        if (this.attackers.isEmpty()) return;
        this.attackerIndex = (this.attackerIndex + 1) % this.attackers.size();
        rebuildDefenders();
        updateButtons();
    }

    private void cycleDefender() {
        if (this.defenders.isEmpty()) return;
        this.defenderIndex = (this.defenderIndex + 1) % this.defenders.size();
        updateButtons();
    }

    private void cycleGoal() {
        this.goalIndex = (this.goalIndex + 1) % WarGoalType.values().length;
        updateButtons();
    }

    private void updateButtons() {
        PoliticalEntityRecord attacker = selectedAttacker();
        PoliticalEntityRecord defender = selectedDefender();
        this.attackerButton.setMessage(Component.translatable("gui.bannermod.war_declare.attacker", displayName(attacker)));
        this.defenderButton.setMessage(Component.translatable("gui.bannermod.war_declare.defender", displayName(defender)));
        this.goalButton.setMessage(Component.translatable("gui.bannermod.war_declare.goal", localizedGoal(selectedGoal())));
        this.attackerButton.active = this.attackers.size() > 1;
        this.defenderButton.active = this.defenders.size() > 1;
        this.declareButton.active = attacker != null && defender != null;
        this.declareButton.setTooltip(this.declareButton.active ? null : Tooltip.create(declareDenial()));
    }

    private void declareWar() {
        PoliticalEntityRecord attacker = selectedAttacker();
        PoliticalEntityRecord defender = selectedDefender();
        if (attacker == null || defender == null) {
            return;
        }
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageDeclareWar(
                attacker.id(), defender.id(), selectedGoal(), this.casusBelliBox.getValue().trim()));
        this.minecraft.setScreen(this.parent);
    }

    private PoliticalEntityRecord selectedAttacker() {
        return this.attackers.isEmpty() ? null : this.attackers.get(this.attackerIndex);
    }

    private PoliticalEntityRecord selectedDefender() {
        return this.defenders.isEmpty() ? null : this.defenders.get(this.defenderIndex);
    }

    private WarGoalType selectedGoal() {
        WarGoalType[] values = WarGoalType.values();
        return values[Math.floorMod(this.goalIndex, values.length)];
    }

    private String displayName(PoliticalEntityRecord entity) {
        if (entity == null) {
            return Component.translatable("gui.bannermod.common.none").getString();
        }
        return entity.name() == null || entity.name().isBlank()
                ? Component.translatable("gui.bannermod.states.unnamed").getString()
                : entity.name();
    }

    private Component localizedGoal(WarGoalType goalType) {
        return switch (goalType) {
            case TRIBUTE -> Component.translatable("gui.bannermod.war_list.goal.tribute");
            case OCCUPATION -> Component.translatable("gui.bannermod.war_list.goal.occupation");
            case ANNEX_LIMITED_CHUNKS -> Component.translatable("gui.bannermod.war_list.goal.annex_limited_chunks");
            case VASSALIZATION -> Component.translatable("gui.bannermod.war_list.goal.vassalization");
            case REGIME_CHANGE -> Component.translatable("gui.bannermod.war_list.goal.regime_change");
            case WHITE_PEACE -> Component.translatable("gui.bannermod.war_list.goal.white_peace");
        };
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0xFF101010);
        graphics.fill(guiLeft, guiTop, guiLeft + W, guiTop + H, 0xD0101010);
        graphics.renderOutline(guiLeft, guiTop, W, H, 0xFFFFFFFF);
        graphics.drawCenteredString(font, this.title.getString(), guiLeft + W / 2, guiTop + 8, 0xFFFFFF);
        graphics.drawString(font, Component.translatable("gui.bannermod.war_declare.hint"), guiLeft + 10, guiTop + 18, 0xCCCCCC, false);
        graphics.drawString(font, Component.translatable("gui.bannermod.war_declare.casus"), guiLeft + 155, guiTop + 48, 0xAAAAAA, false);
        if (this.attackers.isEmpty()) {
            graphics.drawString(font, font.plainSubstrByWidth(declareDenial().getString(), W - 20), guiLeft + 10, guiTop + 86, 0xFFFF8888, false);
        }
        renderActionFeedback(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private Component declareDenial() {
        Player player = Minecraft.getInstance().player;
        UUID actor = player == null ? null : player.getUUID();
        if (attackers.isEmpty()) {
            if (!WarClientState.entities().isEmpty()) {
                return PoliticalEntityAuthority.denialReason(actor, false, WarClientState.entities().get(0));
            }
            return Component.translatable("gui.bannermod.war_list.tooltip.no_declarer");
        }
        if (defenders.isEmpty()) {
            return Component.translatable("gui.bannermod.war.denial.no_defender");
        }
        return Component.translatable("gui.bannermod.war_list.tooltip.no_declarer");
    }

    private void renderActionFeedback(GuiGraphics graphics) {
        Component feedback = WarClientState.lastActionFeedback();
        if (feedback == null || feedback.getString().isBlank()) return;
        graphics.drawString(font, font.plainSubstrByWidth(feedback.getString(), W - 20),
                guiLeft + 10, guiTop + H - 44, 0xFFFFDD88, false);
    }

    @Override
    public void onClose() {
        if (this.parent != null) {
            this.minecraft.setScreen(this.parent);
        } else {
            super.onClose();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
