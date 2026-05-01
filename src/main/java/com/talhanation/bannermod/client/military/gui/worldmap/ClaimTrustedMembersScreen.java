package com.talhanation.bannermod.client.military.gui.worldmap;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.client.military.ClientManager;
import com.talhanation.bannermod.client.military.gui.RecruitsScreenBase;
import com.talhanation.bannermod.client.military.gui.player.PlayersList;
import com.talhanation.bannermod.client.military.gui.player.SelectPlayerScreen;
import com.talhanation.bannermod.client.military.gui.widgets.SelectedPlayerWidget;
import com.talhanation.bannermod.network.messages.military.MessageUpdateClaim;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.persistence.military.RecruitsPlayerInfo;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ClaimTrustedMembersScreen extends RecruitsScreenBase {
    private static final Component TITLE = Component.translatable("gui.bannermod.claim.members.title");
    private static final Component BUTTON_ADD = Component.translatable("gui.bannermod.claim.members.add");
    private static final Component BUTTON_SAVE = Component.translatable("gui.recruits.button.save");
    private static final Component BUTTON_BACK = Component.translatable("gui.recruits.button.back");
    private static final Component BUTTON_PREV = Component.literal("<");
    private static final Component BUTTON_NEXT = Component.literal(">");

    private final ClaimEditScreen parent;
    private final RecruitsClaim claim;
    private final Player player;
    private final List<RecruitsPlayerInfo> trustedMembers = new ArrayList<>();

    private SelectedPlayerWidget selectedPlayerWidget;
    private Button prevButton;
    private Button nextButton;
    private Button addButton;
    private Button saveButton;
    private Button backButton;
    private boolean dirty;
    private int selectedIndex;
    private int x;
    private int y;

    public ClaimTrustedMembersScreen(ClaimEditScreen parent, RecruitsClaim claim, Player player) {
        super(TITLE, 1, 1);
        this.parent = parent;
        this.claim = claim;
        this.player = player;
        for (RecruitsPlayerInfo trustedMember : claim.getTrustedPlayers()) {
            if (trustedMember != null && trustedMember.getUUID() != null) {
                this.trustedMembers.add(copyInfo(trustedMember));
            }
        }
        this.selectedIndex = this.trustedMembers.isEmpty() ? -1 : 0;
    }

    @Override
    protected void init() {
        this.x = this.width / 2;
        this.y = this.height / 2;
        setWidgets();
    }

    private void setWidgets() {
        clearWidgets();
        int rowY = y - 18;

        this.selectedPlayerWidget = new SelectedPlayerWidget(font, x - 70, rowY, 140, 20, Component.literal("x"), this::removeSelectedMember);
        RecruitsPlayerInfo selected = currentSelected();
        if (selected != null) {
            this.selectedPlayerWidget.setPlayer(selected.getUUID(), displayName(selected));
            this.selectedPlayerWidget.setButtonVisible(true);
            this.selectedPlayerWidget.setButtonActive(true);
        } else {
            this.selectedPlayerWidget.setPlayer(null, null);
            this.selectedPlayerWidget.setButtonVisible(false);
            this.selectedPlayerWidget.setButtonActive(false);
        }
        addRenderableWidget(this.selectedPlayerWidget);

        this.prevButton = addRenderableWidget(new ExtendedButton(x - 95, rowY, 20, 20, BUTTON_PREV, button -> cycleSelection(-1)));
        this.nextButton = addRenderableWidget(new ExtendedButton(x + 75, rowY, 20, 20, BUTTON_NEXT, button -> cycleSelection(1)));
        this.addButton = addRenderableWidget(new ExtendedButton(x - 70, y + 12, 140, 20, BUTTON_ADD, button -> openAddPlayerScreen()));
        this.saveButton = addRenderableWidget(new ExtendedButton(x - 70, y + 74, 65, 20, BUTTON_SAVE, button -> saveAndReturn()));
        this.backButton = addRenderableWidget(new ExtendedButton(x + 5, y + 74, 65, 20, BUTTON_BACK, button -> onClose()));

        updateButtons();
    }

    private void openAddPlayerScreen() {
        minecraft.setScreen(new SelectPlayerScreen(this, player, SelectPlayerScreen.TITLE, SelectPlayerScreen.BUTTON_SELECT, SelectPlayerScreen.BUTTON_SELECT_TOOLTIP, false, PlayersList.FilterType.ANY_TEAM,
                playerInfo -> {
                    addTrustedMember(playerInfo);
                    minecraft.setScreen(this);
                }
        ));
    }

    private void addTrustedMember(@Nullable RecruitsPlayerInfo playerInfo) {
        if (playerInfo == null || playerInfo.getUUID() == null) {
            return;
        }
        if (this.claim.getPlayerInfo() != null && playerInfo.getUUID().equals(this.claim.getPlayerInfo().getUUID())) {
            if (this.player != null) {
                this.player.displayClientMessage(Component.translatable("gui.bannermod.claim.members.owner_denied"), false);
            }
            return;
        }
        if (containsTrustedMember(playerInfo.getUUID())) {
            if (this.player != null) {
                this.player.displayClientMessage(Component.translatable("gui.bannermod.claim.members.duplicate", displayName(playerInfo)), false);
            }
            return;
        }
        this.trustedMembers.add(copyInfo(playerInfo));
        this.selectedIndex = this.trustedMembers.size() - 1;
        this.dirty = true;
        setWidgets();
    }

    private void removeSelectedMember() {
        if (currentSelected() == null) {
            return;
        }
        this.trustedMembers.remove(this.selectedIndex);
        if (this.trustedMembers.isEmpty()) {
            this.selectedIndex = -1;
        } else {
            this.selectedIndex = Math.min(this.selectedIndex, this.trustedMembers.size() - 1);
        }
        this.dirty = true;
        setWidgets();
    }

    private void cycleSelection(int delta) {
        if (this.trustedMembers.isEmpty()) {
            return;
        }
        int size = this.trustedMembers.size();
        this.selectedIndex = Math.floorMod(this.selectedIndex + delta, size);
        setWidgets();
    }

    private void saveAndReturn() {
        this.claim.setTrustedPlayers(this.trustedMembers);
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageUpdateClaim(this.claim));
        ClientManager.markClaimsStale();
        this.dirty = false;
        this.minecraft.setScreen(this.parent);
    }

    private void updateButtons() {
        boolean hasSelection = currentSelected() != null;
        this.prevButton.active = this.trustedMembers.size() > 1;
        this.nextButton.active = this.trustedMembers.size() > 1;
        this.saveButton.active = this.dirty;
        this.saveButton.setTooltip(this.dirty ? null : net.minecraft.client.gui.components.Tooltip.create(Component.translatable("gui.bannermod.claim.members.save_disabled")));
        this.prevButton.visible = hasSelection;
        this.nextButton.visible = hasSelection;
    }

    @Nullable
    private RecruitsPlayerInfo currentSelected() {
        if (this.selectedIndex < 0 || this.selectedIndex >= this.trustedMembers.size()) {
            return null;
        }
        return this.trustedMembers.get(this.selectedIndex);
    }

    private boolean containsTrustedMember(UUID playerUuid) {
        for (RecruitsPlayerInfo trustedMember : this.trustedMembers) {
            if (trustedMember != null && playerUuid.equals(trustedMember.getUUID())) {
                return true;
            }
        }
        return false;
    }

    private RecruitsPlayerInfo copyInfo(RecruitsPlayerInfo playerInfo) {
        RecruitsPlayerInfo copy = new RecruitsPlayerInfo(playerInfo.getUUID(), playerInfo.getName());
        copy.setOnline(playerInfo.isOnline());
        return copy;
    }

    private String displayName(RecruitsPlayerInfo playerInfo) {
        return playerInfo.getName() == null || playerInfo.getName().isBlank()
                ? playerInfo.getUUID().toString()
                : playerInfo.getName();
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.renderBackground(guiGraphics, mouseX, mouseY, delta);
        guiGraphics.fill(x - 102, y - 82, x + 102, y + 106, 0xFF1E120A);
        guiGraphics.fill(x - 100, y - 80, x + 100, y + 104, 0xE0B08B55);
        guiGraphics.fill(x - 94, y - 50, x + 94, y + 2, 0xCC140E09);
        guiGraphics.fill(x - 94, y + 40, x + 94, y + 68, 0xCC140E09);
        guiGraphics.renderOutline(x - 100, y - 80, 200, 184, 0xFF8A683F);
        guiGraphics.renderOutline(x - 94, y - 50, 188, 52, 0xFF8A683F);
        guiGraphics.renderOutline(x - 94, y + 40, 188, 28, 0xFF8A683F);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.renderForeground(guiGraphics, mouseX, mouseY, delta);
        guiGraphics.drawCenteredString(font, this.title, x, y - 70, 0xFFF4D8A1);
        guiGraphics.drawString(font, Component.translatable("gui.bannermod.claim.members.subtitle"), x - 90, y - 58, 0xFFF4D8A1, false);
        guiGraphics.drawString(font, Component.translatable("gui.bannermod.claim.members.summary", this.trustedMembers.size()), x - 90, y - 42, 0xFFE8D9BF, false);

        if (currentSelected() == null) {
            guiGraphics.drawCenteredString(font, Component.translatable("gui.bannermod.claim.members.empty"), x, y - 8, 0xFFE8D9BF);
        } else {
            guiGraphics.drawCenteredString(font,
                    Component.translatable("gui.bannermod.claim.members.index", this.selectedIndex + 1, this.trustedMembers.size()),
                    x, y + 48, 0xFFE8D9BF);
        }

        guiGraphics.drawString(font, Component.translatable("gui.bannermod.claim.members.hint.access"), x - 90, y + 18, 0xFFE8D9BF, false);
        guiGraphics.drawString(font, Component.translatable("gui.bannermod.claim.members.hint.no_politics"), x - 90, y + 30, 0xFFE8D9BF, false);
    }

    @Override
    public void onClose() {
        this.minecraft.setScreen(this.parent);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
