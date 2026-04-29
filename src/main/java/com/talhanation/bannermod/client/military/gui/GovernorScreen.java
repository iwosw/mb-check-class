package com.talhanation.bannermod.client.military.gui;

import com.talhanation.bannermod.bootstrap.BannerModMain;
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
        guiGraphics.fill(leftPos, topPos, leftPos + imageWidth, topPos + imageHeight, 0xE0C6B98D);
        guiGraphics.fill(leftPos + 3, topPos + 3, leftPos + imageWidth - 3, topPos + imageHeight - 3, 0xF0E6D8B8);
        guiGraphics.fill(leftPos + 202, topPos + 22, leftPos + imageWidth - 8, topPos + imageHeight - 8, 0x503B2F20);
    }

    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        BannerModSettlementClientMirror.GovernorView state = SETTLEMENT_MIRROR.governorView(this.recruit.getUUID());
        updatePolicyButtons(state);
        int x = leftPos + 10;
        int y = topPos + 10;
        guiGraphics.drawString(font, text("gui.bannermod.governor.title", recruit.getName().getString()), x, y, 4210752, false);
        y += 12;
        guiGraphics.drawString(font, text("gui.bannermod.governor.mirror_state", text(state.stateKey()).getString()), x, y, state.stateColor(), false);
        guiGraphics.renderOutline(x - 3, y - 2, 120, 11, state.stateColor());
        y += 12;
        guiGraphics.drawString(font, text("gui.bannermod.governor.settlement", text(state.settlementKey()).getString()), x, y, 4210752, false);
        y += 12;
        guiGraphics.drawString(font, text("gui.bannermod.governor.citizens", state.citizenCount()), x, y, 4210752, false);
        y += 12;
        guiGraphics.drawString(font, text("gui.bannermod.governor.tax_obligation", state.taxesCollected(), state.taxesDue(), text(taxObligationStateKey(state)).getString()), x, y, taxObligationColor(state), false);
        y += 12;
        guiGraphics.drawString(font, text(taxObligationConsequenceKey(state)), x, y, taxObligationColor(state), false);
        y += 12;
        guiGraphics.drawString(font, text("gui.bannermod.governor.treasury", state.treasuryBalance(), state.lastTreasuryNet()), x, y, 4210752, false);
        y += 12;
        guiGraphics.drawString(font, text("gui.bannermod.governor.projected", state.projectedTreasuryBalance()), x, y, 4210752, false);
        y += 12;
        guiGraphics.drawString(font, text("gui.bannermod.governor.heartbeat", state.lastHeartbeatTick()), x, y, 4210752, false);
        y += 12;
        guiGraphics.drawString(font, text("gui.bannermod.governor.incidents"), x, y, 4210752, false);
        y += 12;
        int visibleIncidents = Math.min(1, state.incidents().size());
        for (int i = 0; i < visibleIncidents; i++) {
            guiGraphics.drawString(font, Component.literal("- " + readableToken(state.incidents().get(i))), x, y, 4210752, false);
            y += 10;
        }
        if (state.incidents().isEmpty()) {
            guiGraphics.drawString(font, text("gui.bannermod.governor.none_row"), x, y, 4210752, false);
            y += 10;
        } else if (state.incidents().size() > visibleIncidents) {
            guiGraphics.drawString(font, text("gui.bannermod.governor.incidents_more", state.incidents().size() - visibleIncidents), x, y, 4210752, false);
            y += 10;
        }
        guiGraphics.drawString(font, text("gui.bannermod.governor.action_reason", text(state.actionReasonKey()).getString()), x, y, state.canUpdatePolicy() ? 0x2E5D32 : 0x8A1F11, false);
        guiGraphics.drawString(font, text("gui.bannermod.governor.policy.garrison", BannerModGovernorPolicy.GARRISON_PRIORITY.valueLabel(state.garrisonPriority())), leftPos + 10, topPos + 124, 4210752, false);
        guiGraphics.drawString(font, text("gui.bannermod.governor.policy.fortification", BannerModGovernorPolicy.FORTIFICATION_PRIORITY.valueLabel(state.fortificationPriority())), leftPos + 10, topPos + 148, 4210752, false);
        guiGraphics.drawString(font, text("gui.bannermod.governor.policy.tax", BannerModGovernorPolicy.TAX_PRESSURE.valueLabel(state.taxPressure())), leftPos + 10, topPos + 172, 4210752, false);

        int logisticsX = leftPos + 210;
        int logisticsY = topPos + 10;
        guiGraphics.drawString(font, text("gui.bannermod.governor.logistics"), logisticsX, logisticsY, 4210752, false);
        logisticsY += 14;
        for (String line : state.logisticsLines()) {
            guiGraphics.drawString(font, shorten(lineComponent(line), 26), logisticsX, logisticsY, 4210752, false);
            logisticsY += 11;
        }
        logisticsY += 4;
        guiGraphics.drawString(font, text("gui.bannermod.governor.advice"), logisticsX, logisticsY, 4210752, false);
        logisticsY += 12;
        guiGraphics.drawString(font, shorten(text("gui.bannermod.governor.advice.garrison", readableToken(state.garrisonRecommendation())), 26), logisticsX, logisticsY, 4210752, false);
        logisticsY += 11;
        guiGraphics.drawString(font, shorten(text("gui.bannermod.governor.advice.fort", readableToken(state.fortificationRecommendation())), 26), logisticsX, logisticsY, 4210752, false);
        logisticsY += 11;
        for (int i = 0; i < Math.min(3, state.recommendations().size()); i++) {
            guiGraphics.drawString(font, shorten(Component.literal("- " + readableToken(state.recommendations().get(i))), 26), logisticsX, logisticsY, 4210752, false);
            logisticsY += 10;
        }
    }

    public static void applyUpdate(UUID recruitId, Envelope envelope) {
        SETTLEMENT_MIRROR.applyGovernorUpdate(recruitId, envelope);
    }

    private static String readableToken(String token) {
        return token == null || token.isBlank() ? "none" : token.replace('_', ' ');
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
            if (args[i] instanceof String value && value.startsWith("gui.bannermod.")) {
                args[i] = Component.translatable(value);
            }
        }
        return Component.translatable(parts[0], args);
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
            return 0x555555;
        }
        if (state.taxesCollected() >= state.taxesDue()) {
            return 0x2E7D32;
        }
        return 0x8A1F11;
    }
}
