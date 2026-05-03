package com.talhanation.bannermod.client.military.gui.war;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.client.military.gui.MilitaryGuiStyle;
import com.talhanation.bannermod.client.military.gui.widgets.DropDownMenu;
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

    private final Screen parent;
    private final List<PoliticalEntityRecord> attackers = new ArrayList<>();
    private final List<PoliticalEntityRecord> defenders = new ArrayList<>();
    private int guiLeft;
    private int guiTop;
    private int attackerIndex;
    private int defenderIndex;
    private int goalIndex;
    private EditBox casusBelliBox;
    private DropDownMenu<PoliticalEntityRecord> attackerDropdown;
    private DropDownMenu<PoliticalEntityRecord> defenderDropdown;
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

        // Dropdown shows the candidate list at a glance; cycle button required N
        // clicks to find the right entry. Server-authoritative — onSelect just
        // updates the local index, declareWar() still posts MessageDeclareWar.
        this.attackerDropdown = new DropDownMenu<>(
                selectedAttacker(), guiLeft + 16, guiTop + 50, 128, 20,
                this.attackers, this::displayName,
                entity -> {
                    int idx = this.attackers.indexOf(entity);
                    if (idx >= 0) {
                        this.attackerIndex = idx;
                        rebuildDefenders();
                        rebuildDefenderDropdown();
                        updateButtons();
                    }
                });
        this.defenderDropdown = new DropDownMenu<>(
                selectedDefender(), guiLeft + 156, guiTop + 50, 128, 20,
                this.defenders, this::displayName,
                entity -> {
                    int idx = this.defenders.indexOf(entity);
                    if (idx >= 0) {
                        this.defenderIndex = idx;
                        updateButtons();
                    }
                });
        this.goalButton = Button.builder(Component.literal(""), btn -> cycleGoal())
                .bounds(guiLeft + 16, guiTop + 76, 128, 20).build();
        this.casusBelliBox = new EditBox(this.font, guiLeft + 156, guiTop + 76, 128, 20, Component.translatable("gui.bannermod.war_declare.casus"));
        this.casusBelliBox.setMaxLength(96);
        this.casusBelliBox.setTextColor(MilitaryGuiStyle.TEXT_DARK);
        this.casusBelliBox.setTextColorUneditable(MilitaryGuiStyle.TEXT_MUTED);
        this.casusBelliBox.setBordered(true);
        this.casusBelliBox.setHint(Component.translatable("gui.bannermod.war_declare.casus_belli.hint"));
        addRenderableWidget(this.goalButton);
        addRenderableWidget(this.casusBelliBox);

        this.declareButton = Button.builder(Component.translatable("gui.bannermod.war_list.declare"), btn -> declareWar())
                .bounds(guiLeft + 16, guiTop + H - 32, 92, 20).build();
        addRenderableWidget(this.declareButton);
        addRenderableWidget(Button.builder(Component.translatable("gui.bannermod.common.cancel"), btn -> onClose())
                .bounds(guiLeft + W - 108, guiTop + H - 32, 92, 20).build());
        // Dropdowns added LAST so the open option list renders above the goal button
        // and casus belli box that sit immediately below them.
        addRenderableWidget(this.attackerDropdown);
        addRenderableWidget(this.defenderDropdown);
        updateButtons();
    }

    private void rebuildDefenderDropdown() {
        if (this.defenderDropdown == null) return;
        // Defenders depend on attacker selection; recreate the dropdown so the
        // option list stays in sync.
        removeWidget(this.defenderDropdown);
        this.defenderDropdown = new DropDownMenu<>(
                selectedDefender(), guiLeft + 156, guiTop + 50, 128, 20,
                this.defenders, this::displayName,
                entity -> {
                    int idx = this.defenders.indexOf(entity);
                    if (idx >= 0) {
                        this.defenderIndex = idx;
                        updateButtons();
                    }
                });
        addRenderableWidget(this.defenderDropdown);
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

    private void cycleGoal() {
        this.goalIndex = (this.goalIndex + 1) % WarGoalType.values().length;
        updateButtons();
    }

    private void updateButtons() {
        PoliticalEntityRecord attacker = selectedAttacker();
        PoliticalEntityRecord defender = selectedDefender();
        this.goalButton.setMessage(Component.translatable("gui.bannermod.war_declare.goal", localizedGoal(selectedGoal())));
        // Dropdowns clamp to read-only display when only one (or zero) options are
        // available — matches the previous active=size>1 semantics on the cycle button.
        // The choose-side labels next to the dropdown explain that the side is fixed.
        this.attackerDropdown.active = this.attackers.size() > 1;
        this.defenderDropdown.active = this.defenders.size() > 1;
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
        MilitaryGuiStyle.parchmentPanel(graphics, guiLeft, guiTop, W, H);
        MilitaryGuiStyle.parchmentInset(graphics, guiLeft + 10, guiTop + 18, W - 20, H - 28);
        MilitaryGuiStyle.drawCenteredTitle(graphics, font, this.title, guiLeft, guiTop + 8, W);
        graphics.drawString(font, font.plainSubstrByWidth(Component.translatable("gui.bannermod.war_declare.hint").getString(), W - 36), guiLeft + 16, guiTop + 28, MilitaryGuiStyle.TEXT_MUTED, false);
        graphics.drawString(font, Component.translatable("gui.bannermod.war_declare.attacker.choose"), guiLeft + 16, guiTop + 40, MilitaryGuiStyle.TEXT_MUTED, false);
        graphics.drawString(font, Component.translatable("gui.bannermod.war_declare.defender.choose"), guiLeft + 156, guiTop + 40, MilitaryGuiStyle.TEXT_MUTED, false);
        graphics.drawString(font, Component.translatable("gui.bannermod.war_declare.casus"), guiLeft + 156, guiTop + 64, MilitaryGuiStyle.TEXT_MUTED, false);
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
                guiLeft + 16, guiTop + H - 50, MilitaryGuiStyle.TEXT_WARN, false);
    }

    private void renderStatus(GuiGraphics graphics) {
        Component status = visibleStatus();
        int color = status == null || status.equals(declareDenial()) ? MilitaryGuiStyle.TEXT_DENIED : MilitaryGuiStyle.TEXT_DARK;
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

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Drive dropdowns explicitly — DropDownMenu.onClick is intentionally inert,
        // option clicks must route through onMouseClick.
        if (this.attackerDropdown != null && this.attackerDropdown.active && this.attackerDropdown.isMouseOver(mouseX, mouseY)) {
            this.attackerDropdown.onMouseClick(mouseX, mouseY);
            return true;
        }
        if (this.defenderDropdown != null && this.defenderDropdown.active && this.defenderDropdown.isMouseOver(mouseX, mouseY)) {
            this.defenderDropdown.onMouseClick(mouseX, mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (this.attackerDropdown != null) this.attackerDropdown.onMouseMove(mouseX, mouseY);
        if (this.defenderDropdown != null) this.defenderDropdown.onMouseMove(mouseX, mouseY);
        super.mouseMoved(mouseX, mouseY);
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
