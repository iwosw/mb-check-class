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
    private static final int H = 166;
    private static final int LEATHER = 0xFF6F4728;
    private static final int LEATHER_DARK = 0xFF3E2515;
    private static final int PAGE_BG = 0xFFF3E2B6;
    private static final int PAGE_SHADE = 0xFF7A5A33;
    private static final int GOLD = 0xFFE0B45C;
    private static final int INK = 0xFF2D2418;
    private static final int INK_MUTED = 0xFF6C5B45;
    private static final int WAX = 0xFFD8B56C;

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
                .bounds(guiLeft + 16, guiTop + 42, 128, 20).build();
        this.defenderButton = Button.builder(Component.literal(""), btn -> cycleDefender())
                .bounds(guiLeft + 156, guiTop + 42, 128, 20).build();
        this.goalButton = Button.builder(Component.literal(""), btn -> cycleGoal())
                .bounds(guiLeft + 16, guiTop + 76, 128, 20).build();
        this.casusBelliBox = new EditBox(this.font, guiLeft + 156, guiTop + 76, 128, 20, Component.translatable("gui.bannermod.war_declare.casus"));
        this.casusBelliBox.setMaxLength(96);
        this.casusBelliBox.setTextColor(INK);
        this.casusBelliBox.setTextColorUneditable(INK_MUTED);
        this.casusBelliBox.setBordered(true);
        this.casusBelliBox.setHint(Component.translatable("gui.bannermod.war_declare.casus_belli.hint"));
        addRenderableWidget(this.attackerButton);
        addRenderableWidget(this.defenderButton);
        addRenderableWidget(this.goalButton);
        addRenderableWidget(this.casusBelliBox);

        this.declareButton = Button.builder(Component.translatable("gui.bannermod.war_list.declare"), btn -> declareWar())
                .bounds(guiLeft + 16, guiTop + H - 32, 92, 20).build();
        addRenderableWidget(this.declareButton);
        addRenderableWidget(Button.builder(Component.translatable("gui.bannermod.common.cancel"), btn -> onClose())
                .bounds(guiLeft + W - 108, guiTop + H - 32, 92, 20).build());
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
        // Cycle buttons go inert when there's only one valid option to cycle through;
        // surface the gating reason rather than leaving the player to guess.
        this.attackerButton.setTooltip(this.attackerButton.active ? null
                : Tooltip.create(Component.translatable("gui.bannermod.war_declare.attacker.only_one")));
        this.defenderButton.setTooltip(this.defenderButton.active ? null
                : Tooltip.create(Component.translatable("gui.bannermod.war_declare.defender.only_one")));
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
        graphics.fill(0, 0, width, height, 0x66000000);
        graphics.fill(guiLeft + 4, guiTop + 5, guiLeft + W + 4, guiTop + H + 5, 0x55000000);
        graphics.fill(guiLeft, guiTop, guiLeft + W, guiTop + H, LEATHER_DARK);
        graphics.fill(guiLeft + 2, guiTop + 2, guiLeft + W - 2, guiTop + H - 2, LEATHER);
        renderParchmentPanel(graphics, guiLeft + 10, guiTop + 18, W - 20, H - 28);
        graphics.drawCenteredString(font, this.title.getString(), guiLeft + W / 2, guiTop + 8, GOLD);
        graphics.drawString(font, font.plainSubstrByWidth(Component.translatable("gui.bannermod.war_declare.hint").getString(), W - 36), guiLeft + 16, guiTop + 28, INK_MUTED, false);
        graphics.drawString(font, Component.translatable("gui.bannermod.war_declare.casus"), guiLeft + 156, guiTop + 64, INK_MUTED, false);
        renderStatus(graphics);
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
        graphics.drawString(font, font.plainSubstrByWidth(feedback.getString(), W - 32),
                guiLeft + 16, guiTop + H - 50, WAX, false);
    }

    private void renderStatus(GuiGraphics graphics) {
        Component status = visibleStatus();
        int color = status == null || status.equals(declareDenial()) ? 0xFF8A3128 : INK;
        if (status != null && !status.getString().isBlank()) {
            graphics.drawString(font, font.plainSubstrByWidth(status.getString(), W - 32),
                    guiLeft + 16, guiTop + 110, color, false);
        }
    }

    private Component visibleStatus() {
        if (!WarClientState.hasSnapshot()) {
            return Component.translatable("gui.bannermod.war_list.waiting_sync");
        }
        PoliticalEntityRecord attacker = selectedAttacker();
        PoliticalEntityRecord defender = selectedDefender();
        if (attacker == null || defender == null) {
            return declareDenial();
        }
        return Component.translatable("gui.bannermod.war_declare.ready");
    }

    private void renderParchmentPanel(GuiGraphics graphics, int x, int y, int w, int h) {
        graphics.fill(x, y, x + w, y + h, PAGE_BG);
        graphics.fill(x, y, x + w, y + 2, 0x88FFF1BE);
        graphics.fill(x, y + h - 2, x + w, y + h, PAGE_SHADE);
        graphics.fill(x, y, x + 2, y + h, 0x66FFF1BE);
        graphics.fill(x + w - 2, y, x + w, y + h, 0x66B88245);
        graphics.renderOutline(x, y, w, h, PAGE_SHADE);
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
