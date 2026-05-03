package com.talhanation.bannermod.client.military.gui;

import com.talhanation.bannermod.entity.military.MessengerEntity;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;

public class MessengerMainScreen extends RecruitsScreenBase {

    private static final Component BUTTON_MESSAGES = Component.translatable("gui.recruits.messenger.tab.messages");
    private static final Component TITLE = Component.translatable("gui.recruits.messenger.main_title");
    private static final Component SUBTITLE = Component.translatable("gui.recruits.messenger.main_subtitle");
    private static final Component NEXT_STEP = Component.translatable("gui.recruits.messenger.main_next_step");

    private final Player player;
    private final MessengerEntity messenger;

    public MessengerMainScreen(MessengerEntity messenger, Player player) {
        super(TITLE, 195,160);
        this.messenger = messenger;
        this.player = player;
    }

    @Override
    protected void init() {
        super.init();
        setButtons();
    }

    private void setButtons() {
        clearWidgets();

        int btnWidth = 128;
        int btnX = guiLeft + (xSize - btnWidth) / 2;

        Button messagesButton = new ExtendedButton(btnX, guiTop + 30, btnWidth, 20,
                MilitaryGuiStyle.clampLabel(font, BUTTON_MESSAGES, btnWidth - 6),
                button -> minecraft.setScreen(new MessengerScreen(messenger, player))
        );
        addRenderableWidget(messagesButton);

    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        MilitaryGuiStyle.parchmentPanel(guiGraphics, guiLeft, guiTop, xSize, ySize);
        MilitaryGuiStyle.titleStrip(guiGraphics, guiLeft + 8, guiTop + 4, xSize - 16, 14);
        MilitaryGuiStyle.parchmentInset(guiGraphics, guiLeft + 16, guiTop + 22, xSize - 32, 48);
        MilitaryGuiStyle.insetPanel(guiGraphics, guiLeft + 26, guiTop + 92, xSize - 52, 28);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        Component clampedTitle = MilitaryGuiStyle.clampLabel(font, TITLE, xSize - 20);
        MilitaryGuiStyle.drawCenteredTitle(guiGraphics, font, clampedTitle, guiLeft, guiTop + 7, xSize);
        Component clampedSubtitle = MilitaryGuiStyle.clampLabel(font, SUBTITLE, xSize - 36);
        guiGraphics.drawCenteredString(font, clampedSubtitle, guiLeft + xSize / 2, guiTop + 30, MilitaryGuiStyle.TEXT_DARK);
        Component clampedNext = MilitaryGuiStyle.clampLabel(font, NEXT_STEP, xSize - 60);
        guiGraphics.drawString(font, clampedNext, guiLeft + 30, guiTop + 101, MilitaryGuiStyle.TEXT, false);
    }
}
