package com.talhanation.bannermod.client.military.gui.war;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.network.messages.war.MessageCancelAllyInvite;
import com.talhanation.bannermod.network.messages.war.MessageInviteAlly;
import com.talhanation.bannermod.network.messages.war.MessageRespondAllyInvite;
import com.talhanation.bannermod.war.client.WarClientState;
import com.talhanation.bannermod.war.registry.PoliticalEntityAuthority;
import com.talhanation.bannermod.war.registry.PoliticalEntityRecord;
import com.talhanation.bannermod.war.runtime.WarAllyInviteRecord;
import com.talhanation.bannermod.war.runtime.WarDeclarationRecord;
import com.talhanation.bannermod.war.runtime.WarSide;
import com.talhanation.bannermod.war.runtime.WarState;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * War Room → Allies subscreen for one selected war.
 *
 * <p>Lists current allies of each side and pending invites. The local player gets
 * Accept/Decline on invites where they lead the invited entity, Cancel on invites
 * they issued, and an "Invite ally" button if they lead one of the war's main
 * sides and the war is still pre-active.</p>
 */
public class WarAlliesScreen extends Screen {
    private static final int W = 380;
    private static final int H = 252;
    private static final int ROW_H = 16;
    private static final int LIST_VISIBLE = 9;
    private static final int LEATHER = 0xFF6F4728;
    private static final int LEATHER_DARK = 0xFF3E2515;
    private static final int PAGE_BG = 0xFFF3E2B6;
    private static final int PAGE_SHADE = 0xFF7A5A33;
    private static final int GOLD = 0xFFE0B45C;
    private static final int INK = 0xFF2D2418;
    private static final int INK_MUTED = 0xFF6C5B45;
    private static final int WAX = 0xFFD8B56C;

    private final Screen parent;
    private final UUID warId;
    private int guiLeft;
    private int guiTop;
    private int scrollOffset = 0;
    private List<Row> rows = List.of();

    private Button inviteAttackerBtn;
    private Button inviteDefenderBtn;
    private Button refreshBtn;
    private Button closeBtn;

    public WarAlliesScreen(@Nullable Screen parent, UUID warId) {
        super(Component.translatable("gui.bannermod.war_allies.title"));
        this.parent = parent;
        this.warId = warId;
    }

    @Override
    protected void init() {
        super.init();
        this.guiLeft = (this.width - W) / 2;
        this.guiTop = (this.height - H) / 2;

        rebuildRows();

        int btnY = guiTop + H - 28;
        inviteAttackerBtn = Button.builder(Component.translatable("gui.bannermod.war_allies.invite_attacker"),
                btn -> openInvitePicker(WarSide.ATTACKER))
                .bounds(guiLeft + 8, btnY, 110, 18).build();
        inviteDefenderBtn = Button.builder(Component.translatable("gui.bannermod.war_allies.invite_defender"),
                btn -> openInvitePicker(WarSide.DEFENDER))
                .bounds(guiLeft + 122, btnY, 110, 18).build();
        refreshBtn = Button.builder(Component.translatable("gui.bannermod.common.refresh"), btn -> rebuildRows())
                .bounds(guiLeft + 240, btnY, 60, 18).build();
        closeBtn = Button.builder(Component.translatable("gui.bannermod.common.back"), btn -> onClose())
                .bounds(guiLeft + 304, btnY, 68, 18).build();

        addRenderableWidget(inviteAttackerBtn);
        addRenderableWidget(inviteDefenderBtn);
        addRenderableWidget(refreshBtn);
        addRenderableWidget(closeBtn);

        updateInviteButtons();
    }

    private void rebuildRows() {
        WarDeclarationRecord war = currentWar();
        List<Row> rebuilt = new ArrayList<>();
        if (war == null) {
            this.rows = rebuilt;
            return;
        }
        for (UUID allyId : war.attackerAllyIds()) {
            rebuilt.add(Row.ally(WarSide.ATTACKER, allyId));
        }
        for (UUID allyId : war.defenderAllyIds()) {
            rebuilt.add(Row.ally(WarSide.DEFENDER, allyId));
        }
        for (WarAllyInviteRecord invite : WarClientState.allyInvitesForWar(warId)) {
            rebuilt.add(Row.invite(invite));
        }
        this.rows = rebuilt;
        scrollOffset = 0;
        updateInviteButtons();
    }

    private void updateInviteButtons() {
        WarDeclarationRecord war = currentWar();
        boolean preActive = war != null && war.state() == WarState.DECLARED;
        UUID local = localPlayerUuid();
        if (inviteAttackerBtn != null) {
            inviteAttackerBtn.active = preActive && war != null
                    && isLeaderOf(war.attackerPoliticalEntityId(), local);
            inviteAttackerBtn.setTooltip(inviteAttackerBtn.active ? null : Tooltip.create(inviteDenial(war, preActive, war == null ? null : war.attackerPoliticalEntityId())));
        }
        if (inviteDefenderBtn != null) {
            inviteDefenderBtn.active = preActive && war != null
                    && isLeaderOf(war.defenderPoliticalEntityId(), local);
            inviteDefenderBtn.setTooltip(inviteDefenderBtn.active ? null : Tooltip.create(inviteDenial(war, preActive, war == null ? null : war.defenderPoliticalEntityId())));
        }
    }

    private Component inviteDenial(@Nullable WarDeclarationRecord war, boolean preActive, @Nullable UUID sideId) {
        if (war == null) return Component.translatable("gui.bannermod.war.denial.war_not_found");
        if (!preActive) return Component.translatable("gui.bannermod.war.ally_denial.war_not_pre_active");
        PoliticalEntityRecord side = WarClientState.entityById(sideId);
        UUID local = localPlayerUuid();
        return PoliticalEntityAuthority.denialReason(local, false, side);
    }

    private void openInvitePicker(WarSide side) {
        WarDeclarationRecord war = currentWar();
        if (war == null) return;
        Minecraft.getInstance().setScreen(new WarAllyInvitePickerScreen(this, war, side));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        graphics.fill(0, 0, width, height, 0x66000000);
        graphics.fill(guiLeft + 4, guiTop + 5, guiLeft + W + 4, guiTop + H + 5, 0x55000000);
        graphics.fill(guiLeft, guiTop, guiLeft + W, guiTop + H, LEATHER_DARK);
        graphics.fill(guiLeft + 2, guiTop + 2, guiLeft + W - 2, guiTop + H - 2, LEATHER);
        renderParchmentPanel(graphics, guiLeft + 8, guiTop + 20, W - 16, LIST_VISIBLE * ROW_H + 20);
        renderParchmentPanel(graphics, guiLeft + 8, guiTop + H - 62, W - 16, 42);

        WarDeclarationRecord war = currentWar();
        String header = war == null
                ? Component.translatable("gui.bannermod.war_allies.not_found").getString()
                : Component.translatable("gui.bannermod.war_allies.header",
                entityName(war.attackerPoliticalEntityId()),
                entityName(war.defenderPoliticalEntityId()),
                localizedWarState(war.state())).getString();
        graphics.drawCenteredString(font, header, guiLeft + W / 2, guiTop + 8, GOLD);
        graphics.drawString(font, font.plainSubstrByWidth(Component.translatable("gui.bannermod.war_allies.hint").getString(), W - 32), guiLeft + 12, guiTop + 24, INK_MUTED, false);

        renderList(graphics, mouseX, mouseY);
        renderVisibleStatus(graphics);
        renderActionFeedback(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderActionFeedback(GuiGraphics graphics) {
        Component feedback = WarClientState.lastActionFeedback();
        if (feedback == null || feedback.getString().isBlank()) return;
        graphics.drawString(font, font.plainSubstrByWidth(feedback.getString(), W - 24),
                guiLeft + 12, guiTop + H - 34, WAX, false);
    }

    private void renderVisibleStatus(GuiGraphics graphics) {
        Component status = visibleStatus();
        if (status == null || status.getString().isBlank()) {
            return;
        }
        graphics.drawString(font, font.plainSubstrByWidth(status.getString(), W - 24),
                guiLeft + 12, guiTop + H - 48, INK, false);
    }

    private void renderList(GuiGraphics graphics, int mouseX, int mouseY) {
        int listX = guiLeft + 8;
        int listY = guiTop + 24;
        int listW = W - 16;
        int listH = LIST_VISIBLE * ROW_H;
        graphics.drawString(font, Component.translatable("gui.bannermod.war_allies.ledger"), listX, listY - 12, INK, false);
        graphics.fill(listX, listY, listX + listW, listY + listH, 0x22FFFFFF);
        graphics.renderOutline(listX, listY, listW, listH, PAGE_SHADE);

        if (rows.isEmpty()) {
            graphics.drawCenteredString(font, Component.translatable("gui.bannermod.war_allies.empty"),
                    listX + listW / 2, listY + listH / 2 - 4, 0xAAAAAA);
            return;
        }

        int rendered = Math.min(LIST_VISIBLE, Math.max(0, rows.size() - scrollOffset));
        for (int i = 0; i < rendered; i++) {
            Row row = rows.get(scrollOffset + i);
            int rowY = listY + i * ROW_H;
            graphics.drawString(font,
                    font.plainSubstrByWidth(row.label(this), listW - 8),
                    listX + 4, rowY + 4, row.color(), false);
        }
    }

    private Component visibleStatus() {
        if (!WarClientState.hasSnapshot()) {
            return Component.translatable("gui.bannermod.war_list.waiting_sync");
        }
        WarDeclarationRecord war = currentWar();
        if (war == null) {
            return Component.translatable("gui.bannermod.war_allies.not_found");
        }
        if (war.state() != WarState.DECLARED) {
            return Component.translatable("gui.bannermod.war.ally_denial.war_not_pre_active");
        }
        UUID local = localPlayerUuid();
        for (Row row : rows) {
            WarAllyInviteRecord invite = row.invite();
            if (invite != null && isLeaderOf(invite.inviteePoliticalEntityId(), local)) {
                return Component.translatable("gui.bannermod.war_allies.status.accept_decline");
            }
            if (invite != null && isLeaderOf(war.mainSideEntityId(invite.side()), local)) {
                return Component.translatable("gui.bannermod.war_allies.status.cancel_pending");
            }
        }
        if (inviteAttackerBtn != null && inviteAttackerBtn.active) {
            return Component.translatable("gui.bannermod.war_allies.status.open_picker", localizedWarSide(WarSide.ATTACKER));
        }
        if (inviteDefenderBtn != null && inviteDefenderBtn.active) {
            return Component.translatable("gui.bannermod.war_allies.status.open_picker", localizedWarSide(WarSide.DEFENDER));
        }
        return inviteDenial(war, true, war.attackerPoliticalEntityId());
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
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0 || button == 1) {
            int listX = guiLeft + 8;
            int listY = guiTop + 24;
            int listW = W - 16;
            int listH = LIST_VISIBLE * ROW_H;
            if (mouseX >= listX && mouseX < listX + listW
                    && mouseY >= listY && mouseY < listY + listH) {
                int idx = scrollOffset + (int) ((mouseY - listY) / ROW_H);
                if (idx >= 0 && idx < rows.size()) {
                    Row row = rows.get(idx);
                    WarAllyInviteRecord invite = row.invite();
                    if (invite != null) {
                        if (button == 1) {
                            return declineIfAddressedToMe(invite);
                        }
                        triggerInviteAction(invite);
                        return true;
                    }
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    private void triggerInviteAction(WarAllyInviteRecord invite) {
        UUID local = localPlayerUuid();
        if (isLeaderOf(invite.inviteePoliticalEntityId(), local)) {
            BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageRespondAllyInvite(invite.id(), true));
            rebuildRows();
            return;
        }
        UUID sideEntityId = currentWar() == null ? null : currentWar().mainSideEntityId(invite.side());
        if (isLeaderOf(sideEntityId, local)) {
            BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageCancelAllyInvite(invite.id()));
            rebuildRows();
        }
    }

    private boolean declineIfAddressedToMe(WarAllyInviteRecord invite) {
        if (!isLeaderOf(invite.inviteePoliticalEntityId(), localPlayerUuid())) {
            return false;
        }
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageRespondAllyInvite(invite.id(), false));
        rebuildRows();
        return true;
    }

    @Override
    public boolean keyPressed(int key, int scan, int mods) {
        if (key == 261 /* DELETE */ || key == 259 /* BACKSPACE */) {
            // shortcut: decline the topmost invite addressed to me
            UUID local = localPlayerUuid();
            for (Row row : rows) {
                WarAllyInviteRecord invite = row.invite();
                if (invite != null && isLeaderOf(invite.inviteePoliticalEntityId(), local)) {
                    BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageRespondAllyInvite(invite.id(), false));
                    rebuildRows();
                    return true;
                }
            }
        }
        return super.keyPressed(key, scan, mods);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double delta) {
        int max = Math.max(0, rows.size() - LIST_VISIBLE);
        scrollOffset = Math.max(0, Math.min(max, scrollOffset - (int) Math.signum(delta)));
        return true;
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

    @Nullable
    private WarDeclarationRecord currentWar() {
        for (WarDeclarationRecord war : WarClientState.wars()) {
            if (war.id().equals(warId)) return war;
        }
        return null;
    }

    private String entityName(@Nullable UUID id) {
        if (id == null) return Component.translatable("gui.bannermod.common.unknown").getString();
        PoliticalEntityRecord entity = WarClientState.entityById(id);
        if (entity == null) return Component.translatable("gui.bannermod.common.unknown").getString();
        if (entity.name().isBlank()) return Component.translatable("gui.bannermod.states.unnamed").getString();
        return entity.name();
    }

    private static Component localizedWarState(WarState state) {
        return switch (state) {
            case DECLARED -> Component.translatable("gui.bannermod.war_list.state.declared");
            case ACTIVE -> Component.translatable("gui.bannermod.war_list.state.active");
            case IN_SIEGE_WINDOW -> Component.translatable("gui.bannermod.war_list.state.in_siege_window");
            case RESOLVED -> Component.translatable("gui.bannermod.war_list.state.resolved");
            case CANCELLED -> Component.translatable("gui.bannermod.war_list.state.cancelled");
        };
    }

    private static Component localizedWarSide(WarSide side) {
        return switch (side) {
            case ATTACKER -> Component.translatable("gui.bannermod.war.side.attacker");
            case DEFENDER -> Component.translatable("gui.bannermod.war.side.defender");
        };
    }

    @Nullable
    private static UUID localPlayerUuid() {
        Player player = Minecraft.getInstance().player;
        return player == null ? null : player.getUUID();
    }

    private static boolean isLeaderOf(@Nullable UUID entityId, @Nullable UUID playerUuid) {
        if (entityId == null || playerUuid == null) return false;
        PoliticalEntityRecord entity = WarClientState.entityById(entityId);
        if (entity == null) return false;
        return PoliticalEntityAuthority.canAct(playerUuid, false, entity);
    }

    /** Either a confirmed ally row or a pending invite row. */
    private record Row(WarSide side, @Nullable UUID allyEntityId, @Nullable WarAllyInviteRecord invite) {
        static Row ally(WarSide side, UUID id) {
            return new Row(side, id, null);
        }

        static Row invite(WarAllyInviteRecord record) {
            return new Row(record.side(), null, record);
        }

        String label(WarAlliesScreen screen) {
            if (invite != null) {
                UUID local = WarAlliesScreen.localPlayerUuid();
                String name = screen.entityName(invite.inviteePoliticalEntityId());
                String tag;
                if (WarAlliesScreen.isLeaderOf(invite.inviteePoliticalEntityId(), local)) {
                    tag = Component.translatable("gui.bannermod.war_allies.row.invite.accept_decline").getString();
                } else if (WarAlliesScreen.isLeaderOf(
                        screen.currentWar() == null
                                ? null
                                : screen.currentWar().mainSideEntityId(side),
                        local)) {
                    tag = Component.translatable("gui.bannermod.war_allies.row.invite.cancel").getString();
                } else {
                    tag = "";
                }
                return Component.translatable("gui.bannermod.war_allies.row.invite", localizedWarSide(side), name).getString() + tag;
            }
            return Component.translatable("gui.bannermod.war_allies.row.ally", localizedWarSide(side), screen.entityName(allyEntityId)).getString();
        }

        int color() {
            if (invite != null) return 0xFFFFD24A;
            return side == WarSide.ATTACKER ? 0xFFFF8888 : 0xFF99CCFF;
        }
    }
}
