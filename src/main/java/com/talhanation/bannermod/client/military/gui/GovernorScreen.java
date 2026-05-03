package com.talhanation.bannermod.client.military.gui;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.client.military.gui.MilitaryGuiStyle;
import com.talhanation.bannermod.client.settlement.BannerModSettlementClientMirror;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.governance.BannerModGovernorPolicy;
import com.talhanation.bannermod.inventory.military.GovernorContainer;
import com.talhanation.bannermod.network.messages.military.MessageOpenGovernorScreen;
import com.talhanation.bannermod.network.messages.military.MessageUpdateGovernorPolicy;
import com.talhanation.bannermod.shared.settlement.BannerModSettlementClientSnapshotContract.Envelope;
import de.maxhenkel.corelib.inventory.ScreenBase;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;

import java.util.Arrays;
import java.util.List;
import java.util.UUID;

public class GovernorScreen extends ScreenBase<GovernorContainer> {
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/gui/professions/blank_gui.png");
    private static final BannerModSettlementClientMirror SETTLEMENT_MIRROR = new BannerModSettlementClientMirror();

    private final Player player;
    private final AbstractRecruitEntity recruit;
    private final List<Button> policyButtons = new java.util.ArrayList<>();

    public GovernorScreen(GovernorContainer container, Inventory playerInventory, Component title) {
        super(TEXTURE, container, playerInventory, title);
        this.imageWidth = 320;
        this.imageHeight = 188;
        this.player = container.getPlayerEntity();
        this.recruit = container.getRecruit();
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;
        SETTLEMENT_MIRROR.beginGovernorRequest(this.recruit.getUUID(), this.player.level().getGameTime());
        addPolicyButtons(BannerModGovernorPolicy.GARRISON_PRIORITY, 120);
        addPolicyButtons(BannerModGovernorPolicy.FORTIFICATION_PRIORITY, 144);
        addPolicyButtons(BannerModGovernorPolicy.TAX_PRESSURE, 168);
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageOpenGovernorScreen(this.recruit.getUUID(), false));
    }

    private void addPolicyButtons(BannerModGovernorPolicy policy, int yOffset) {
        Button minus = new ExtendedButton(this.leftPos + 150, this.topPos + yOffset, 16, 16, Component.literal("-"), button -> stepPolicy(policy, -1));
        Button plus = new ExtendedButton(this.leftPos + 170, this.topPos + yOffset, 16, 16, Component.literal("+"), button -> stepPolicy(policy, 1));
        minus.setTooltip(Tooltip.create(text("gui.bannermod.governor.action.disabled.loading")));
        plus.setTooltip(Tooltip.create(text("gui.bannermod.governor.action.disabled.loading")));
        this.policyButtons.add(minus);
        this.policyButtons.add(plus);
        addRenderableWidget(minus);
        addRenderableWidget(plus);
    }

    private void stepPolicy(BannerModGovernorPolicy policy, int delta) {
        BannerModSettlementClientMirror.GovernorView state = SETTLEMENT_MIRROR.governorView(this.recruit.getUUID());
        if (!state.canUpdatePolicy()) {
            return;
        }
        int currentValue = switch (policy) {
            case GARRISON_PRIORITY -> state.garrisonPriority();
            case FORTIFICATION_PRIORITY -> state.fortificationPriority();
            case TAX_PRESSURE -> state.taxPressure();
        };
        SETTLEMENT_MIRROR.markGovernorStale(this.recruit.getUUID(), this.player.level().getGameTime());
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageUpdateGovernorPolicy(this.recruit.getUUID(), policy, policy.clamp(currentValue + delta)));
    }

    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        MilitaryGuiStyle.parchmentPanel(guiGraphics, leftPos, topPos, imageWidth, imageHeight);
        MilitaryGuiStyle.parchmentInset(guiGraphics, leftPos + 8, topPos + 8, 186, 107);
        MilitaryGuiStyle.parchmentInset(guiGraphics, leftPos + 8, topPos + 118, 186, imageHeight - 126);
        MilitaryGuiStyle.insetPanel(guiGraphics, leftPos + 202, topPos + 22, imageWidth - 210, imageHeight - 30);
    }

    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        BannerModSettlementClientMirror.GovernorView state = SETTLEMENT_MIRROR.governorView(this.recruit.getUUID());
        updatePolicyButtons(state);
        int x = leftPos + 10;
        int y = topPos + 10;
        guiGraphics.drawString(font, text("gui.bannermod.governor.title", recruit.getName().getString()), x, y, MilitaryGuiStyle.TEXT_DARK, false);
        y += 12;
        guiGraphics.drawString(font, text("gui.bannermod.governor.mirror_state", text(state.stateKey()).getString()), x, y, state.stateColor(), false);
        guiGraphics.renderOutline(x - 3, y - 2, 120, 11, state.stateColor());
        y += 12;
        guiGraphics.drawString(font, text("gui.bannermod.governor.settlement", text(state.settlementKey()).getString()), x, y, MilitaryGuiStyle.TEXT_DARK, false);
        y += 12;
        guiGraphics.drawString(font, text("gui.bannermod.governor.citizens", state.citizenCount()), x, y, MilitaryGuiStyle.TEXT_DARK, false);
        y += 12;
        guiGraphics.drawString(font, text("gui.bannermod.governor.tax_obligation", state.taxesCollected(), state.taxesDue(), text(taxObligationStateKey(state)).getString()), x, y, taxObligationColor(state), false);
        y += 12;
        guiGraphics.drawString(font, text(taxObligationConsequenceKey(state)), x, y, taxObligationColor(state), false);
        y += 12;
        guiGraphics.drawString(font, text("gui.bannermod.governor.treasury", state.treasuryBalance(), state.lastTreasuryNet()), x, y, MilitaryGuiStyle.TEXT_DARK, false);
        y += 12;
        guiGraphics.drawString(font, text("gui.bannermod.governor.projected", state.projectedTreasuryBalance()), x, y, MilitaryGuiStyle.TEXT_DARK, false);
        y += 12;
        guiGraphics.drawString(font, text("gui.bannermod.governor.heartbeat", state.lastHeartbeatTick()), x, y, MilitaryGuiStyle.TEXT_DARK, false);
        y += 12;
        guiGraphics.drawString(font, text("gui.bannermod.governor.incidents"), x, y, MilitaryGuiStyle.TEXT_DARK, false);
        y += 12;
        int visibleIncidents = Math.min(1, state.incidents().size());
        for (int i = 0; i < visibleIncidents; i++) {
            guiGraphics.drawString(font, text("gui.bannermod.governor.bullet", governorValue(state.incidents().get(i))), x, y, MilitaryGuiStyle.TEXT_DARK, false);
            y += 10;
        }
        if (state.incidents().isEmpty()) {
            guiGraphics.drawString(font, text("gui.bannermod.governor.none_row"), x, y, MilitaryGuiStyle.TEXT_DARK, false);
            y += 10;
        } else if (state.incidents().size() > visibleIncidents) {
            guiGraphics.drawString(font, text("gui.bannermod.governor.incidents_more", state.incidents().size() - visibleIncidents), x, y, MilitaryGuiStyle.TEXT_DARK, false);
            y += 10;
        }
        guiGraphics.drawString(font, text("gui.bannermod.governor.action_reason", text(state.actionReasonKey()).getString()), x, y, state.canUpdatePolicy() ? MilitaryGuiStyle.TEXT_GOOD : MilitaryGuiStyle.TEXT_DENIED, false);
        guiGraphics.drawString(font, text("gui.bannermod.governor.policy.garrison", governorPolicyValue(state.garrisonPriority())), leftPos + 10, topPos + 124, MilitaryGuiStyle.TEXT_DARK, false);
        guiGraphics.drawString(font, text("gui.bannermod.governor.policy.fortification", governorPolicyValue(state.fortificationPriority())), leftPos + 10, topPos + 148, MilitaryGuiStyle.TEXT_DARK, false);
        guiGraphics.drawString(font, text("gui.bannermod.governor.policy.tax", governorPolicyValue(state.taxPressure())), leftPos + 10, topPos + 172, MilitaryGuiStyle.TEXT_DARK, false);

        int logisticsX = leftPos + 210;
        int logisticsY = topPos + 10;
        guiGraphics.drawString(font, text("gui.bannermod.governor.logistics"), logisticsX, logisticsY, MilitaryGuiStyle.TEXT, false);
        logisticsY += 14;
        for (String line : state.logisticsLines()) {
            guiGraphics.drawString(font, shorten(lineComponent(line), 26), logisticsX, logisticsY, MilitaryGuiStyle.TEXT, false);
            logisticsY += 11;
        }
        logisticsY += 4;
        guiGraphics.drawString(font, text("gui.bannermod.governor.advice"), logisticsX, logisticsY, MilitaryGuiStyle.TEXT, false);
        logisticsY += 12;
        guiGraphics.drawString(font, shorten(text("gui.bannermod.governor.advice.garrison", governorValue(state.garrisonRecommendation())), 26), logisticsX, logisticsY, MilitaryGuiStyle.TEXT, false);
        logisticsY += 11;
        guiGraphics.drawString(font, shorten(text("gui.bannermod.governor.advice.fort", governorValue(state.fortificationRecommendation())), 26), logisticsX, logisticsY, MilitaryGuiStyle.TEXT, false);
        logisticsY += 11;
        for (int i = 0; i < Math.min(3, state.recommendations().size()); i++) {
            guiGraphics.drawString(font, shorten(text("gui.bannermod.governor.bullet", governorValue(state.recommendations().get(i))), 26), logisticsX, logisticsY, MilitaryGuiStyle.TEXT, false);
            logisticsY += 10;
        }
    }

    public static void applyUpdate(UUID recruitId, Envelope envelope) {
        SETTLEMENT_MIRROR.applyGovernorUpdate(recruitId, envelope);
    }

    private static Component shorten(Component component, int maxLength) {
        return Component.literal(shorten(component.getString(), maxLength));
    }

    private static String shorten(String value, int maxLength) {
        if (value == null || value.length() <= maxLength) {
            return value == null ? "" : value;
        }
        return value.substring(0, Math.max(0, maxLength - 3)) + "...";
    }

    private static Component text(String key, Object... args) {
        return Component.translatable(key, args);
    }

    private static Component lineComponent(String encodedLine) {
        String[] parts = encodedLine.split(" ");
        if (parts.length == 0) {
            return Component.empty();
        }
        Object[] args = Arrays.copyOfRange(parts, 1, parts.length);
        for (int i = 0; i < args.length; i++) {
            if (args[i] instanceof String value) {
                args[i] = governorValue(value);
            }
        }
        return Component.translatable(parts[0], args);
    }

    private static Component governorPolicyValue(int value) {
        return governorValue(BannerModGovernorPolicy.GARRISON_PRIORITY.valueLabel(value));
    }

    private static Component governorValue(String value) {
        if (value == null || value.isBlank()) {
            return text("gui.bannermod.common.none");
        }
        if (value.startsWith("gui.bannermod.")) {
            return text(value);
        }
        String key = switch (value) {
            case "low" -> "gui.bannermod.governor.value.low";
            case "balanced" -> "gui.bannermod.governor.value.balanced";
            case "high" -> "gui.bannermod.governor.value.high";
            case "hold_course" -> "gui.bannermod.governor.token.hold_course";
            case "increase_garrison" -> "gui.bannermod.governor.token.increase_garrison";
            case "strengthen_fortifications" -> "gui.bannermod.governor.token.strengthen_fortifications";
            case "relieve_supply_pressure" -> "gui.bannermod.governor.token.relieve_supply_pressure";
            case "hostile_claim" -> "gui.bannermod.governor.token.hostile_claim";
            case "degraded_settlement" -> "gui.bannermod.governor.token.degraded_settlement";
            case "unclaimed_settlement" -> "gui.bannermod.governor.token.unclaimed_settlement";
            case "under_siege" -> "gui.bannermod.governor.token.under_siege";
            case "worker_shortage" -> "gui.bannermod.governor.token.worker_shortage";
            case "supply_blocked" -> "gui.bannermod.governor.token.supply_blocked";
            case "recruit_upkeep_blocked" -> "gui.bannermod.governor.token.recruit_upkeep_blocked";
            case "local_outpost" -> "gui.bannermod.governor.token.local_outpost";
            case "water_gate" -> "gui.bannermod.governor.token.water_gate";
            case "junction_market" -> "gui.bannermod.governor.token.junction_market";
            case "chokepoint_fort" -> "gui.bannermod.governor.token.chokepoint_fort";
            case "surplus_hub" -> "gui.bannermod.governor.token.surplus_hub";
            default -> null;
        };
        if (key != null) {
            return text(key);
        }
        ResourceLocation id = value.contains(":") ? ResourceLocation.tryParse(value) : ResourceLocation.tryParse("minecraft:" + value);
        if (id != null && BuiltInRegistries.ITEM.containsKey(id)) {
            return BuiltInRegistries.ITEM.get(id).getDescription();
        }
        return Component.literal(value.replace('_', ' '));
    }

    private void updatePolicyButtons(BannerModSettlementClientMirror.GovernorView state) {
        for (Button button : this.policyButtons) {
            button.active = state.canUpdatePolicy();
            button.setTooltip(state.canUpdatePolicy() ? Tooltip.create(text("gui.bannermod.governor.action.enabled")) : Tooltip.create(text(state.actionReasonKey())));
        }
    }

    private static String taxObligationStateKey(BannerModSettlementClientMirror.GovernorView state) {
        if (state.taxesDue() <= 0) {
            return "gui.bannermod.governor.tax_state.none";
        }
        if (state.taxesCollected() >= state.taxesDue()) {
            return "gui.bannermod.governor.tax_state.satisfied";
        }
        return "gui.bannermod.governor.tax_state.unpaid";
    }

    private static String taxObligationConsequenceKey(BannerModSettlementClientMirror.GovernorView state) {
        if (state.taxesDue() <= 0) {
            return "gui.bannermod.governor.tax_consequence.none";
        }
        if (state.taxesCollected() >= state.taxesDue()) {
            return "gui.bannermod.governor.tax_consequence.satisfied";
        }
        return "gui.bannermod.governor.tax_consequence.unpaid";
    }

    private static int taxObligationColor(BannerModSettlementClientMirror.GovernorView state) {
        if (state.taxesDue() <= 0) {
            return MilitaryGuiStyle.TEXT_MUTED;
        }
        if (state.taxesCollected() >= state.taxesDue()) {
            return MilitaryGuiStyle.TEXT_GOOD;
        }
        return MilitaryGuiStyle.TEXT_DENIED;
    }
}
