package com.talhanation.bannermod.client.military.gui;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.client.military.gui.component.RecruitsMultiLineEditBox;
import com.talhanation.bannermod.entity.military.MessengerEntity;
import com.talhanation.bannermod.network.messages.military.MessageAnswerMessenger;
import com.talhanation.bannermod.persistence.military.RecruitsPlayerInfo;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;

public class MessengerAnswerScreen extends RecruitsScreenBase {

    private final Player player;
    private final MessengerEntity messenger;
    private RecruitsMultiLineEditBox textFieldMessage;

    private final String message;

    private final RecruitsPlayerInfo playerInfo;
    private static final MutableComponent BUTTON_OK = Component.translatable("gui.recruits.inv.text.ok_messenger");
    private static final Component TITLE = Component.translatable("gui.recruits.messenger.answer_title");
    private static final Component LABEL_FROM = Component.translatable("gui.recruits.messenger.answer_from");
    private static final Component LABEL_TO = Component.translatable("gui.recruits.messenger.answer_to");
    private static final Component LABEL_REPLY = Component.translatable("gui.recruits.messenger.answer_reply");
    private Button okButton;
    private boolean acknowledged;
    public MessengerAnswerScreen(MessengerEntity messenger, Player player, String message, RecruitsPlayerInfo playerInfo) {
        super(TITLE, 197,250);
        this.player = player;
        this.messenger = messenger;
        this.message = message;
        this.playerInfo = playerInfo;
    }

    @Override
    protected void init() {
        super.init();
        this.textFieldMessage = new RecruitsMultiLineEditBox(font, guiLeft + 3, guiTop + ySize - 215, 186, 165, Component.empty(), Component.empty());
        this.textFieldMessage.setValue(message);
        this.textFieldMessage.setEnableEditing(false);

        addRenderableWidget(textFieldMessage);

        setOKButton();
    }
    public void tick() {
        super.tick();
    }

    public boolean mouseClicked(double p_100753_, double p_100754_, int p_100755_) {
        return super.mouseClicked(p_100753_, p_100754_, p_100755_);
    }

    private void setOKButton() {
        this.okButton = addRenderableWidget(new ExtendedButton(guiLeft + 33, guiTop + ySize - 50, 128, 20, BUTTON_OK,
                button -> {
                    BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageAnswerMessenger(messenger.getUUID()));
                    this.acknowledged = true;
                    this.okButton.active = false;
                }
        ));
    }

    @Override
    public void onClose() {
        super.onClose();

    }


    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        MilitaryGuiStyle.parchmentPanel(guiGraphics, guiLeft, guiTop, xSize, ySize);
        MilitaryGuiStyle.titleStrip(guiGraphics, guiLeft + 8, guiTop + 8, xSize - 16, 22);
        MilitaryGuiStyle.parchmentInset(guiGraphics, guiLeft + 8, guiTop + 36, xSize - 16, 175);
        MilitaryGuiStyle.insetPanel(guiGraphics, guiLeft + 24, guiTop + ySize - 80, xSize - 48, 18);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        String targetPlayer = playerInfo.getName();
        String owner = this.messenger.getOwnerName();
        int rawtime = this.messenger.getWaitingTime();
        int time = rawtime / 20;
        Component timeLine = time <= 100
                ? Component.translatable("gui.recruits.messenger.answer_time_seconds", time)
                : Component.translatable("gui.recruits.messenger.answer_time_minutes", time / 60);

        Component clampedTitle = MilitaryGuiStyle.clampLabel(font, TITLE, xSize - 24);
        MilitaryGuiStyle.drawCenteredTitle(guiGraphics, font, clampedTitle, guiLeft, guiTop + 14, xSize);
        guiGraphics.drawString(font, LABEL_FROM, guiLeft + 14, guiTop + 44, MilitaryGuiStyle.TEXT_DARK, false);
        guiGraphics.drawString(font, LABEL_TO, guiLeft + 14,  guiTop + 56, MilitaryGuiStyle.TEXT_DARK, false);
        String clampedOwner = MilitaryGuiStyle.clampLabel(font, owner, xSize - 60);
        String clampedTarget = MilitaryGuiStyle.clampLabel(font, targetPlayer, xSize - 60);
        guiGraphics.drawString(font, clampedOwner, guiLeft + 48,  guiTop + 44, MilitaryGuiStyle.TEXT_DARK, false);
        guiGraphics.drawString(font, clampedTarget, guiLeft + 48,  guiTop + 56, MilitaryGuiStyle.TEXT_DARK, false);
        Component clampedTime = MilitaryGuiStyle.clampLabel(font, timeLine, xSize - 28);
        guiGraphics.drawString(font, clampedTime, guiLeft + 14, guiTop + 68, MilitaryGuiStyle.TEXT_MUTED, false);
        guiGraphics.drawString(font, LABEL_REPLY, guiLeft + 14, guiTop + 86, MilitaryGuiStyle.TEXT_DARK, false);
        Component statusLine = acknowledged
                ? Component.translatable("gui.recruits.messenger.answer_status.accepted")
                : Component.translatable("gui.recruits.messenger.answer_status.ready");
        Component clampedStatus = MilitaryGuiStyle.clampLabel(font, statusLine, xSize - 60);
        guiGraphics.drawString(font, clampedStatus,
                guiLeft + 30,
                guiTop + ySize - 75,
                acknowledged ? MilitaryGuiStyle.TEXT_GOOD : MilitaryGuiStyle.TEXT_MUTED,
                false);

        if(!messenger.getMainHandItem().isEmpty()){
            guiGraphics.renderFakeItem(messenger.getMainHandItem(), guiLeft + 120, guiTop + ySize - 48);
            guiGraphics.renderItemDecorations(font, messenger.getMainHandItem(),guiLeft + 120, guiTop + ySize - 48);
        }
    }
}
