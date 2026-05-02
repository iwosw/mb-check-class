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

    private EditBox editBox;
    private ExtendedButton saveButton;
    private final Screen parent;
    private final AbstractRecruitEntity recruit;
    private int leftPos;
    private int topPos;
    private int imageWidth;
    private int imageHeight;
    private static final MutableComponent TEXT_CANCEL = Component.translatable("gui.recruits.groups.cancel");
    private static final MutableComponent TEXT_SAVE = Component.translatable("gui.recruits.groups.save");
    private static final MutableComponent TEXT_RENAME_RECRUIT = Component.translatable("gui.recruits.inv.rename");
    private static final MutableComponent TEXT_STATUS = Component.translatable("gui.recruits.rename.status");
    private static final MutableComponent TOOLTIP_SAVE_DISABLED = Component.translatable("gui.recruits.rename.tooltip.save_disabled");
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
        editBox.setMaxLength(24);
        this.addRenderableWidget(editBox);
        this.setInitialFocus(editBox);

        this.saveButton = this.addRenderableWidget(new ExtendedButton(leftPos + 10, topPos + 55, 60, 20, TEXT_SAVE,
            button -> {
                String newName = editBox.getValue().trim();
                if (!newName.isEmpty()) {
                    recruit.setCustomName(Component.literal(newName));

                    BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageDebugGui(99, recruit.getUUID(), newName));

                    this.minecraft.setScreen(this.parent);
                }
        }));

        this.addRenderableWidget(new ExtendedButton(leftPos + 170, topPos + 55, 60, 20, TEXT_CANCEL, button -> {
            this.minecraft.setScreen(this.parent);
        }));
    }

    @Override
    public void tick() {
        super.tick();
        boolean hasName = editBox != null && !editBox.getValue().trim().isEmpty();
        if (saveButton != null) {
            saveButton.active = hasName;
            saveButton.setTooltip(hasName ? null : Tooltip.create(TOOLTIP_SAVE_DISABLED));
        }
    }

    private void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        MilitaryGuiStyle.drawCenteredTitle(guiGraphics, font, TEXT_RENAME_RECRUIT, leftPos, topPos + 7, imageWidth);
        Component statusClamped = MilitaryGuiStyle.clampLabel(font, TEXT_STATUS, imageWidth - 20);
        guiGraphics.drawString(font, statusClamped, leftPos + 10, topPos + 45, MilitaryGuiStyle.TEXT_DARK, false);
    }
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        MilitaryGuiStyle.parchmentPanel(guiGraphics, leftPos, topPos, imageWidth, imageHeight);
        MilitaryGuiStyle.titleStrip(guiGraphics, leftPos + 6, topPos + 4, imageWidth - 12, 14);
        MilitaryGuiStyle.parchmentInset(guiGraphics, leftPos + 8, topPos + 18, imageWidth - 16, 26);
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
