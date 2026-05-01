package com.talhanation.bannermod.client.military.gui;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.network.messages.military.MessageDebugGui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;

public class RenameRecruitScreen extends Screen {

    private static final int fontColor = 4210752;
    private EditBox editBox;
    private final Screen parent;
    private final AbstractRecruitEntity recruit;
    private int leftPos;
    private int topPos;
    private int imageWidth;
    private int imageHeight;
    private static final MutableComponent TEXT_CANCEL = Component.translatable("gui.recruits.groups.cancel");
    private static final MutableComponent TEXT_SAVE = Component.translatable("gui.recruits.groups.save");
    private static final MutableComponent TEXT_RENAME_RECRUIT = Component.translatable("gui.recruits.inv.rename");
    private static final MutableComponent TEXT_RENAME_HINT = Component.translatable("gui.recruits.rename.hint");
    private static final MutableComponent TEXT_RENAME_BLANK = Component.translatable("gui.recruits.rename.status.blank");
    private static final MutableComponent TEXT_RENAME_SENT = Component.translatable("gui.recruits.rename.status.sent");
    private Component status = TEXT_RENAME_HINT;
    public RenameRecruitScreen(Screen parent, AbstractRecruitEntity recruit) {
        super(Component.literal(""));
        this.recruit = recruit;
        this.parent = parent;
        this.imageWidth = 250;
        this.imageHeight = 83;
    }

    @Override
    protected void init() {
        super.init();

        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        editBox = new EditBox(this.font, leftPos + 10, topPos + 20, 220, 20, Component.literal(""));
        if (recruit != null) {
            editBox.setValue(recruit.getName().getString());
        }
        editBox.setMaxLength(32);
        editBox.setFocused(true);
        this.addRenderableWidget(editBox);
        setInitialFocus(editBox);

        ExtendedButton saveButton = this.addRenderableWidget(new ExtendedButton(leftPos + 10, topPos + 55, 60, 20, TEXT_SAVE,
            button -> {
                String newName = editBox.getValue();
                if (newName.isBlank()) {
                    status = TEXT_RENAME_BLANK;
                    return;
                }
                if (recruit != null) {
                    BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageDebugGui(99, recruit.getUUID(), newName));
                    if (minecraft != null && minecraft.player != null) {
                        minecraft.player.sendSystemMessage(TEXT_RENAME_SENT);
                    }
                    this.minecraft.setScreen(this.parent);
                }
        }));
        saveButton.setTooltip(Tooltip.create(TEXT_RENAME_HINT));

        this.addRenderableWidget(new ExtendedButton(leftPos + 170, topPos + 55, 60, 20, TEXT_CANCEL, button -> {
            this.minecraft.setScreen(this.parent);
        }));
    }

    @Override
    public void tick() {
        super.tick();
    }

    private void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.drawString(font, TEXT_RENAME_RECRUIT, leftPos + 10  , topPos + 5, MilitaryGuiStyle.TEXT, false);
        guiGraphics.drawString(font, status, leftPos + 10, topPos + 42, status == TEXT_RENAME_BLANK ? MilitaryGuiStyle.TEXT_DENIED : fontColor, false);
    }
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        MilitaryGuiStyle.parchmentPanel(guiGraphics, leftPos, topPos, imageWidth, imageHeight);
        MilitaryGuiStyle.titleStrip(guiGraphics, leftPos + 5, topPos + 4, imageWidth - 10, 14);
        MilitaryGuiStyle.parchmentInset(guiGraphics, leftPos + 7, topPos + 18, imageWidth - 14, 32);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.renderBackground(guiGraphics, mouseX, mouseY, delta);
        this.renderBackground(guiGraphics, mouseX, mouseY, delta);
        super.render(guiGraphics, mouseX, mouseY, delta);
        this.renderForeground(guiGraphics, mouseX, mouseY, delta);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
