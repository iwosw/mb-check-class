package com.talhanation.bannermod.client.military.gui;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.client.military.gui.group.RecruitsGroupListScreen;
import com.talhanation.bannermod.client.military.gui.player.PlayersList;
import com.talhanation.bannermod.client.military.gui.player.SelectPlayerScreen;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.network.messages.military.MessageAssignRecruitToPlayer;
import com.talhanation.bannermod.network.messages.military.MessageDisband;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;

public class RecruitMoreScreen extends RecruitsScreenBase {

    private static final Component TITLE = Component.translatable("gui.recruits.more_screen.title");
    private Player player;
    private AbstractRecruitEntity recruit;
    private static final MutableComponent DISBAND = Component.translatable("gui.recruits.inv.text.disband");
    private static final MutableComponent TOOLTIP_DISBAND = Component.translatable("gui.recruits.inv.tooltip.disband");
    public static final MutableComponent TOOLTIP_KEEP_TEAM = Component.translatable("gui.recruits.inv.tooltip.keepTeam");
    public static final MutableComponent TOOLTIP_ASSIGN_GROUP_TO_PLAYER = Component.translatable("gui.recruits.inv.tooltip.assignGroupToPlayer");
    private static final MutableComponent ASSIGN_TO_PLAYER = Component.translatable("gui.recruits.team.assignNewOwner");
    private static final MutableComponent GROUP_SETTINGS = Component.translatable("gui.recruits.groups.settings");
    private static final MutableComponent RENAME = Component.translatable("gui.recruits.inv.rename");
    private static final MutableComponent STATUS_TEXT = Component.translatable("gui.recruits.more.status");

    private static final int BUTTON_W = 130;

    public RecruitMoreScreen(Screen parent, AbstractRecruitEntity recruit, Player player) {
        super(TITLE, 195,160);
        this.player = player;
        this.recruit = recruit;
    }

    @Override
    protected void init() {
        super.init();

        setButtons();
    }

    private void setButtons(){
        clearWidgets();

        Button buttonRename = new ExtendedButton(guiLeft + 32, guiTop + 25, BUTTON_W, 20,
                MilitaryGuiStyle.clampLabel(font, RENAME, BUTTON_W - 6),
                btn -> {
                    if(recruit != null) {
                        minecraft.setScreen(new RenameRecruitScreen(this, recruit));
                    }
                }
        );
        addRenderableWidget(buttonRename);

        Button buttonDisband = new ExtendedButton(guiLeft + 32, guiTop + 45, BUTTON_W, 20,
                MilitaryGuiStyle.clampLabel(font, DISBAND, BUTTON_W - 6),
                btn -> {
                    if(this.recruit != null) {
                        if(this.recruit.getTeam() != null) {
                            minecraft.setScreen(new ConfirmScreen(DISBAND, TOOLTIP_KEEP_TEAM,
                                    () -> BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageDisband(this.recruit.getUUID(), true)),
                                    () -> BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageDisband(this.recruit.getUUID(), false)),
                                    () -> minecraft.setScreen(RecruitMoreScreen.this)
                            ));
                        }
                        else
                            BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageDisband(this.recruit.getUUID(), false));
                    }
                }
        );
        buttonDisband.setTooltip(Tooltip.create(TOOLTIP_DISBAND));
        addRenderableWidget(buttonDisband);

        Button giveToPlayer = new ExtendedButton(guiLeft + 32, guiTop + 65, BUTTON_W, 20,
                MilitaryGuiStyle.clampLabel(font, ASSIGN_TO_PLAYER, BUTTON_W - 6),
            btn -> {
                if(recruit != null) {
                    minecraft.setScreen(new SelectPlayerScreen(this, player, ASSIGN_TO_PLAYER, ASSIGN_TO_PLAYER, TOOLTIP_ASSIGN_GROUP_TO_PLAYER, false, PlayersList.FilterType.NONE,
                        (playerInfo) -> {
                            BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageAssignRecruitToPlayer(this.recruit.getUUID(), playerInfo.getUUID()));
                            onClose();
                        })
                    );
                }
            }
        );
        addRenderableWidget(giveToPlayer);

        Button buttonGroupSettings = new ExtendedButton(guiLeft + 32, guiTop + 105, BUTTON_W, 20,
                MilitaryGuiStyle.clampLabel(font, GROUP_SETTINGS, BUTTON_W - 6),
                btn -> {
                    minecraft.setScreen(new RecruitsGroupListScreen(player));
                }
        );
        addRenderableWidget(buttonGroupSettings);
    }


    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        MilitaryGuiStyle.parchmentPanel(guiGraphics, guiLeft, guiTop, xSize, ySize);
        MilitaryGuiStyle.titleStrip(guiGraphics, guiLeft + 6, guiTop + 4, xSize - 12, 14);
        MilitaryGuiStyle.parchmentInset(guiGraphics, guiLeft + 18, guiTop + 22, xSize - 36, 110);
        MilitaryGuiStyle.insetPanel(guiGraphics, guiLeft + 14, guiTop + 136, xSize - 28, 16);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        Component clampedTitle = MilitaryGuiStyle.clampLabel(font, TITLE, xSize - 20);
        MilitaryGuiStyle.drawCenteredTitle(guiGraphics, font, clampedTitle, guiLeft, guiTop + 7, xSize);
        Component statusClamped = MilitaryGuiStyle.clampLabel(font, STATUS_TEXT, xSize - 36);
        guiGraphics.drawString(font, statusClamped, guiLeft + 18, guiTop + 140, MilitaryGuiStyle.TEXT, false);
    }

}
