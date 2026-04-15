package com.talhanation.bannermod.client.civilian.gui;

import com.talhanation.bannermod.client.military.gui.widgets.BlackShowingTextField;
import com.talhanation.bannermod.client.military.gui.widgets.RecruitsCheckBox;
import com.talhanation.workers.WorkersRuntime;
import com.talhanation.bannermod.entity.civilian.workarea.MiningArea;
import com.talhanation.bannermod.entity.civilian.workarea.MiningPatternSettings;
import com.talhanation.workers.network.MessageUpdateMiningArea;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.client.gui.widget.ExtendedButton;

import java.util.UUID;

public class MiningAreaScreen extends WorkAreaScreen {
    private static final MutableComponent TEXT_CLOSE_FLOOR = Component.translatable("gui.bannermod.workers.checkbox.closeFloor");
    public final MiningArea miningArea;
    public Button xSizePlusButton;
    public Button xSizeMinusButton;
    public Button ySizePlusButton;
    public Button ySizeMinusButton;
    public int areaXSize;
    public int areaYSize;
    public int areaYOffset;
    public int branchSpacing;
    public int branchLength;
    public int descentStep;
    private RecruitsCheckBox closeFloorCheckBox;
    private boolean closeFloor;
    private MiningPatternSettings.Mode miningMode;
    public MiningAreaScreen(MiningArea miningArea, Player player) {
        super(miningArea.getCustomName(), miningArea, player);
        this.miningArea = miningArea;
    }

    @Override
    protected void init() {
        this.areaXSize = miningArea.getWidthSize();
        this.areaYSize = miningArea.getHeightSize();
        this.areaYOffset = miningArea.getHeightOffset();
        this.closeFloor = miningArea.getCloseFloor();
        this.branchSpacing = miningArea.getBranchSpacing();
        this.branchLength = miningArea.getBranchLength();
        this.descentStep = miningArea.getDescentStep();
        this.miningMode = miningArea.getPatternSettings().mode();

        this.setButtons();
    }

    @Override
    public void tick() {
        super.tick();

    }

    @Override
    public void setButtons() {
        super.setButtons();
        int previewHeight = 100;
        int boxWidth = 120;
        int boxHeight = 20;

        int sizeButtonX = 120;
        int sizeButtonY = 130;
        addRenderableWidget(new ExtendedButton(x - boxWidth/2, y - previewHeight / 2 + 105, boxWidth, boxHeight,
                Component.literal("Mode: " + this.miningMode.name()), btn -> {
                    this.miningMode = nextMode(this.miningMode);
                    this.sendMessage();
                    this.setButtons();
                }));
        addRenderableWidget(new BlackShowingTextField(x - boxWidth/2, y - previewHeight / 2 + 130, boxWidth, boxHeight, Component.literal("width: " + areaXSize)));
        addRenderableWidget(new BlackShowingTextField(x - boxWidth/2, y - previewHeight / 2 + 130 + boxHeight, boxWidth, boxHeight, Component.literal( "height: " + areaYSize)));
        addRenderableWidget(new BlackShowingTextField(x - boxWidth/2, y - previewHeight / 2 + 130 + boxHeight*2, boxWidth, boxHeight, Component.literal("descent: " + descentStep)));

        addRenderableWidget(new ExtendedButton(x - boxWidth/2 + sizeButtonX, y - previewHeight / 2 + sizeButtonY + 40, 20, 20, Component.literal("+"), btn -> {
            descentStep = Mth.clamp(descentStep + 1, 1, 8);
            this.sendMessage();
            this.setButtons();
        }));
        addRenderableWidget(new ExtendedButton(x - boxWidth/2 + sizeButtonX + 20, y - previewHeight / 2 + sizeButtonY + 40, 20, 20, Component.literal("-"), btn -> {
            descentStep = Mth.clamp(descentStep - 1, 1, 8);
            this.sendMessage();
            this.setButtons();
        }));


        xSizePlusButton = addRenderableWidget(new ExtendedButton(x - boxWidth/2 + sizeButtonX, y - previewHeight / 2 + sizeButtonY, 20, 20, Component.literal("+"),
                btn -> {
                    if(hasShiftDown()) areaXSize += 5;
                    else areaXSize++;
                    areaXSize = Mth.clamp(areaXSize, 1, 16);

                    this.sendMessage();
                    this.setButtons();
                }
        ));

        xSizeMinusButton = addRenderableWidget(new ExtendedButton(x - boxWidth/2 + sizeButtonX + 20, y - previewHeight / 2 + sizeButtonY, 20, 20, Component.literal("-"),
                btn -> {
                    if(hasShiftDown()) areaXSize -= 5;
                    else areaXSize--;
                    areaXSize = Mth.clamp(areaXSize, 1, 16);

                    this.miningArea.setWidthSize(areaXSize);
                    this.sendMessage();
                    this.setButtons();
                }
        ));

        ySizePlusButton = addRenderableWidget(new ExtendedButton(x - boxWidth/2 + sizeButtonX, y - previewHeight / 2 + sizeButtonY + 20, 20, 20, Component.literal("+"),
                btn -> {
                    if(hasShiftDown()) areaYSize += 5;
                    else areaYSize++;
                    areaYSize = Mth.clamp(areaYSize, 2, 8);

                    this.miningArea.setHeightSize(areaYSize);
                    this.sendMessage();
                    this.setButtons();
                }
        ));

        ySizeMinusButton = addRenderableWidget(new ExtendedButton(x - boxWidth/2 + sizeButtonX + 20, y - previewHeight / 2 + sizeButtonY + 20, 20, 20, Component.literal("-"),
                btn -> {
                    if(hasShiftDown()) areaYSize -= 5;
                    else areaYSize--;
                    areaYSize = Mth.clamp(areaYSize, 2, 8);

                    this.miningArea.setHeightSize(areaYSize);
                    this.sendMessage();
                    this.setButtons();
                }
        ));

        this.closeFloorCheckBox = new RecruitsCheckBox(x - boxWidth/2, y - previewHeight / 2 + 155 + boxHeight*2, boxWidth, boxHeight, TEXT_CLOSE_FLOOR,
                this.closeFloor,
                (bool) -> {
                    this.closeFloor = bool;
                    this.sendMessage();
                }
        );
        addRenderableWidget(closeFloorCheckBox);

        if (this.miningMode == MiningPatternSettings.Mode.BRANCH) {
            int rowY = y - previewHeight / 2 + 155 + boxHeight * 3;
            addRenderableWidget(new BlackShowingTextField(x - boxWidth / 2, rowY, boxWidth, boxHeight, Component.literal("spacing: " + branchSpacing)));
            addRenderableWidget(new ExtendedButton(x - boxWidth/2 + sizeButtonX, rowY, 20, 20, Component.literal("+"), btn -> {
                branchSpacing = Mth.clamp(branchSpacing + 1, 1, 16);
                this.sendMessage();
                this.setButtons();
            }));
            addRenderableWidget(new ExtendedButton(x - boxWidth/2 + sizeButtonX + 20, rowY, 20, 20, Component.literal("-"), btn -> {
                branchSpacing = Mth.clamp(branchSpacing - 1, 1, 16);
                this.sendMessage();
                this.setButtons();
            }));

            int lengthY = rowY + boxHeight;
            addRenderableWidget(new BlackShowingTextField(x - boxWidth / 2, lengthY, boxWidth, boxHeight, Component.literal("length: " + branchLength)));
            addRenderableWidget(new ExtendedButton(x - boxWidth/2 + sizeButtonX, lengthY, 20, 20, Component.literal("+"), btn -> {
                branchLength = Mth.clamp(branchLength + 1, 1, 32);
                this.sendMessage();
                this.setButtons();
            }));
            addRenderableWidget(new ExtendedButton(x - boxWidth/2 + sizeButtonX + 20, lengthY, 20, 20, Component.literal("-"), btn -> {
                branchLength = Mth.clamp(branchLength - 1, 1, 32);
                this.sendMessage();
                this.setButtons();
            }));
        }
    }

    private MiningPatternSettings.Mode nextMode(MiningPatternSettings.Mode mode) {
        if (mode == MiningPatternSettings.Mode.TUNNEL) {
            return MiningPatternSettings.Mode.BRANCH;
        }
        return MiningPatternSettings.Mode.TUNNEL;
    }

    public void sendMessage(){
        if(miningArea == null) return;

        this.miningArea.setWidthSize(areaXSize);
        WorkersRuntime.channel().sendToServer(new MessageUpdateMiningArea(
                this.miningArea.getUUID(),
                areaXSize,
                areaYSize,
                areaYOffset,
                closeFloor,
                this.miningMode.getIndex(),
                branchSpacing,
                branchLength,
                descentStep
        ));
    }
    @Override
    public void mouseMoved(double x, double y) {
        super.mouseMoved(x, y);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        return super.mouseClicked(mouseX, mouseY, button);
    }
    @Override
    public boolean mouseScrolled(double x, double y, double d) {
        return super.mouseScrolled(x, y, d);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
    }

    @Override
    public void onAreaMoved() {

    }
}
