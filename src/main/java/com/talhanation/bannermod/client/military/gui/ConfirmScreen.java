package com.talhanation.bannermod.client.military.gui;

import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.FormattedCharSequence;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;

import java.util.List;

public class ConfirmScreen extends RecruitsScreenBase {

    private final Runnable yesAction;
    private final Runnable noAction;
    private final Runnable backAction;
    private final Component text;

    private static final MutableComponent BUTTON_YES = Component.translatable("gui.recruits.button.Yes");
    private static final MutableComponent BUTTON_NO = Component.translatable("gui.recruits.button.No");
    private static final MutableComponent BUTTON_BACK = Component.translatable("gui.recruits.button.back");
    private static final MutableComponent STATUS_BINARY = Component.translatable("gui.recruits.confirm.status.binary");
    private static final MutableComponent STATUS_TRINARY = Component.translatable("gui.recruits.confirm.status.trinary");


    public ConfirmScreen(Component title, Component text, Runnable yesAction, Runnable noAction) {
        this(title, text, yesAction, noAction, null);
    }
    public ConfirmScreen(Component title, Component text, Runnable yesAction, Runnable noAction, Runnable backAction) {
        super(title, 246,84);
        this.yesAction = yesAction;
        this.noAction = noAction;
        this.backAction = backAction;
        this.text = text;
    }

    @Override
    protected void init() {
        super.init();
        int buttonWidth = 75;
        int buttonHeight = 20;


        this.addRenderableWidget(new ExtendedButton(guiLeft + 7, guiTop + ySize - 27, buttonWidth, buttonHeight, BUTTON_YES, button -> {
            this.yesAction.run();
            onClose();
        }));

        if(backAction != null){
            this.addRenderableWidget(new ExtendedButton(guiLeft + 7 + 75 + 2, guiTop + ySize - 27, buttonWidth, buttonHeight, BUTTON_NO, button -> {
                this.noAction.run();
                onClose();
            }));

            this.addRenderableWidget(new ExtendedButton(guiLeft + 7 + 150 + 4, guiTop + ySize - 27, buttonWidth, buttonHeight, BUTTON_BACK, button -> {
                this.backAction.run();

            }));
        }
        else {
            this.addRenderableWidget(new ExtendedButton(guiLeft + 7 + 150 + 4, guiTop + ySize - 27, buttonWidth, buttonHeight, BUTTON_NO, button -> {
                this.noAction.run();
                onClose();
            }));
        }

    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        MilitaryGuiStyle.parchmentPanel(guiGraphics, guiLeft, guiTop, xSize, ySize);
        MilitaryGuiStyle.titleStrip(guiGraphics, guiLeft + 6, guiTop + 4, xSize - 12, 14);
        MilitaryGuiStyle.parchmentInset(guiGraphics, guiLeft + 8, guiTop + 22, xSize - 16, ySize - 56);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        Component clampedTitle = MilitaryGuiStyle.clampLabel(font, title, xSize - 20);
        MilitaryGuiStyle.drawCenteredTitle(guiGraphics, font, clampedTitle, guiLeft, guiTop + 7, xSize);
        int maxWidth = xSize - 20; // xSize minus Padding


        List<FormattedCharSequence> lines = font.split(text, maxWidth);

        int lineHeight = 10;
        int yPosition = guiTop + 27;

        for (FormattedCharSequence line : lines) {
            guiGraphics.drawString(font, line, guiLeft + xSize / 2 - font.width(line) / 2, yPosition, MilitaryGuiStyle.TEXT_DARK, false);
            yPosition += lineHeight;
        }
        Component status = backAction == null ? STATUS_BINARY : STATUS_TRINARY;
        Component statusClamped = MilitaryGuiStyle.clampLabel(font, status, xSize - 20);
        guiGraphics.drawString(font, statusClamped, guiLeft + 8, guiTop + ySize - 38, MilitaryGuiStyle.TEXT_DARK, false);
    }
}
