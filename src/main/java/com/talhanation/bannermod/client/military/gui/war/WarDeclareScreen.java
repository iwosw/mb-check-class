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
        super(Component.literal("Declare War"));
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
        this.casusBelliBox = new EditBox(this.font, guiLeft + 155, guiTop + 58, 135, 18, Component.literal("Casus belli"));
        this.casusBelliBox.setMaxLength(96);
        addRenderableWidget(this.attackerButton);
        addRenderableWidget(this.defenderButton);
        addRenderableWidget(this.goalButton);
        addRenderableWidget(this.casusBelliBox);

        this.declareButton = Button.builder(Component.literal("Declare"), btn -> declareWar())
                .bounds(guiLeft + 10, guiTop + H - 28, 80, 18).build();
        addRenderableWidget(this.declareButton);
        addRenderableWidget(Button.builder(Component.literal("Cancel"), btn -> onClose())
                .bounds(guiLeft + W - 90, guiTop + H - 28, 80, 18).build());
        updateButtons();
    }

    private void rebuildChoices() {
        this.attackers.clear();
        this.defenders.clear();
        Player player = Minecraft.getInstance().player;
        UUID playerId = player == null ? null : player.getUUID();
        for (PoliticalEntityRecord entity : WarClientState.entities()) {
            if (playerId != null && PoliticalEntityAuthority.canAct(playerId, false, entity) && entity.status().canDeclareOffensiveWar()) {
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
        this.attackerButton.setMessage(Component.literal("Attacker: " + displayName(attacker)));
        this.defenderButton.setMessage(Component.literal("Defender: " + displayName(defender)));
        this.goalButton.setMessage(Component.literal("Goal: " + selectedGoal().name()));
        this.attackerButton.active = this.attackers.size() > 1;
        this.defenderButton.active = this.defenders.size() > 1;
        this.declareButton.active = attacker != null && defender != null;
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
            return "none";
        }
        return entity.name() == null || entity.name().isBlank() ? shortId(entity.id()) : entity.name();
    }

    private String shortId(UUID id) {
        String value = id.toString();
        return value.substring(0, Math.min(8, value.length()));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.fill(guiLeft, guiTop, guiLeft + W, guiTop + H, 0xD0101010);
        graphics.renderOutline(guiLeft, guiTop, W, H, 0xFFFFFFFF);
        graphics.drawCenteredString(font, this.title.getString(), guiLeft + W / 2, guiTop + 8, 0xFFFFFF);
        graphics.drawString(font, "Pick sides, goal, and optional casus belli.", guiLeft + 10, guiTop + 18, 0xCCCCCC, false);
        graphics.drawString(font, "Casus belli", guiLeft + 155, guiTop + 48, 0xAAAAAA, false);
        if (this.attackers.isEmpty()) {
            graphics.drawString(font, "No leader-controlled state can declare war.", guiLeft + 10, guiTop + 86, 0xFFFF8888, false);
        }
        super.render(graphics, mouseX, mouseY, partialTick);
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
