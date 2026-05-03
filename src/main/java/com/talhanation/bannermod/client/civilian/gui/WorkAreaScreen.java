package com.talhanation.bannermod.client.civilian.gui;

import com.talhanation.bannermod.client.military.gui.MilitaryGuiStyle;
import com.talhanation.bannermod.client.military.gui.RecruitsScreenBase;
import com.talhanation.bannermod.client.military.gui.player.PlayersList;
import com.talhanation.bannermod.client.military.gui.player.SelectPlayerScreen;
import com.talhanation.bannermod.client.military.gui.widgets.SelectedPlayerWidget;
import com.talhanation.bannermod.persistence.military.RecruitsPlayerInfo;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.civilian.workarea.AbstractWorkAreaEntity;
import com.talhanation.bannermod.network.messages.civilian.MessageRotateWorkArea;
import com.talhanation.bannermod.network.messages.civilian.MessageUpdateOwner;
import com.talhanation.bannermod.network.messages.civilian.MessageUpdateWorkArea;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;

import java.util.List;

public abstract class WorkAreaScreen extends RecruitsScreenBase {

    private static final MutableComponent TEXT_FORWARD = Component.translatable("gui.workers.command.text.forward");
    private static final MutableComponent TEXT_BACKWARD = Component.translatable("gui.workers.command.text.back");
    private static final MutableComponent TEXT_LEFT = Component.translatable("gui.workers.command.text.left");
    private static final MutableComponent TEXT_RIGHT = Component.translatable("gui.workers.command.text.right");
    private static final MutableComponent TEXT_DESTROY = Component.translatable("gui.workers.command.text.destroy");
    private static final MutableComponent TEXT_UP = Component.translatable("gui.workers.command.text.up");
    private static final MutableComponent TEXT_DOWN = Component.translatable("gui.workers.command.text.down");
    private static final MutableComponent TEXT_SECTION_POSITION = Component.translatable("gui.bannermod.work_area.section.position");
    private static final MutableComponent TEXT_SECTION_OWNER = Component.translatable("gui.bannermod.work_area.section.owner");
    private static final MutableComponent TEXT_SECTION_SETTINGS = Component.translatable("gui.bannermod.work_area.section.settings");
    private static final MutableComponent TEXT_HINT_MOVE = Component.translatable("gui.bannermod.work_area.hint.move");
    private static final MutableComponent TEXT_HINT_SHIFT = Component.translatable("gui.bannermod.work_area.hint.shift");
    private static final MutableComponent TEXT_HINT_OWNER_MISSING = Component.translatable("gui.bannermod.work_area.hint.owner_missing");
    private static final MutableComponent TEXT_HINT_OWNER_READY = Component.translatable("gui.bannermod.work_area.hint.owner_ready");
    private Button moveForward;
    private Button moveBackward;
    private Button moveLeft;
    private Button moveRight;
    private Button moveUp;
    private Button moveDown;
    private Button destroy;
    private Button rotateLeft;
    private Button rotateRight;
    private long workAreaPendingUntilTick;

    public Player player;
    public AbstractWorkAreaEntity workArea;

    public SelectedPlayerWidget selectedPlayerWidget;
    public RecruitsPlayerInfo playerInfo;

    protected WorkAreaScreen(Component title, AbstractWorkAreaEntity workArea, Player player) {
        super(title, 320, 282);
        this.workArea = workArea;
        this.player = player;
    }

    @Override
    protected void init() {
        super.init();
        workArea.showBox = true;
        playerInfo = workArea.getPlayerUUID() == null ? null : new RecruitsPlayerInfo(workArea.getPlayerUUID(), workArea.getPlayerName());
        setButtons();
    }
    public int x;
    public int y;
    public void setButtons(){
        clearWidgets();
        x = this.width / 2;
        y = this.guiTop + 76;
        int buttonWidth = 80;
        int buttonHeight = 20;

        // Move Forward
        moveForward = addRenderableWidget(new ExtendedButton(x - buttonWidth / 2, y + buttonHeight / 2 - buttonHeight*2, buttonWidth, buttonHeight, TEXT_FORWARD,
            btn -> {
                this.workArea.showBox = true;
                int x = 1;
                if(hasShiftDown()){
                    x = 5;
                }
                Vec3 newPos = workArea.position().relative(player.getDirection(), x);
                BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageUpdateWorkArea(this.workArea.getUUID(), this.workArea.getCustomName().getString(), newPos, false));
                this.markWorkAreaPending();
                this.onAreaMoved();
            }
        ));

        // Move Backward
        moveBackward = addRenderableWidget(new ExtendedButton(x - buttonWidth / 2, y - buttonHeight / 2 + buttonHeight, buttonWidth, buttonHeight, TEXT_BACKWARD,
                    btn -> {
                        this.workArea.showBox = true;
                        int x = 1;
                        if(hasShiftDown()){
                            x = 5;
                        }
                        Vec3 newPos = workArea.position().relative(player.getDirection().getOpposite(), x);
                        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageUpdateWorkArea(this.workArea.getUUID(), this.workArea.getCustomName().getString(), newPos, false));
                        this.markWorkAreaPending();
                        this.onAreaMoved();
                    }
        ));

        // Move Left
        moveLeft = addRenderableWidget(new ExtendedButton(x - buttonWidth / 2 - buttonWidth, y - buttonHeight / 2, buttonWidth, buttonHeight, TEXT_LEFT,
            btn -> {
                this.workArea.showBox = true;
                int x = 1;
                if(hasShiftDown()){
                    x = 5;
                }
                Vec3 newPos = workArea.position().relative(player.getDirection().getCounterClockWise(), x);
                BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageUpdateWorkArea(this.workArea.getUUID(), this.workArea.getCustomName().getString(), newPos, false));
                this.markWorkAreaPending();
                this.onAreaMoved();
            }
        ));

        // Move Right
        moveRight = addRenderableWidget(new ExtendedButton(x - buttonWidth / 2 + buttonWidth, y - buttonHeight / 2, buttonWidth, buttonHeight,  TEXT_RIGHT,
            btn -> {
                this.workArea.showBox = true;
                int x = 1;
                if(hasShiftDown()){
                    x = 5;
                }
                Vec3 newPos = workArea.position().relative(player.getDirection().getClockWise(), x);
                BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageUpdateWorkArea(this.workArea.getUUID(), this.workArea.getCustomName().getString(), newPos, false));
                this.markWorkAreaPending();
                this.onAreaMoved();
            }
        ));

        // Destroy
        destroy = addRenderableWidget(new ExtendedButton(x - buttonWidth / 2, y - buttonHeight / 2, buttonWidth, buttonHeight, TEXT_DESTROY,
            btn -> {
                BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageUpdateWorkArea(this.workArea.getUUID(), this.workArea.getCustomName().getString(), workArea.position(), true));
                this.onClose();
            }
        ));

        rotateLeft = addRenderableWidget(new ExtendedButton(x - buttonWidth / 2 - buttonWidth, y - buttonHeight / 2 + buttonHeight, buttonWidth, buttonHeight, Component.literal("\u21BB"),
            btn -> {
                this.workArea.showBox = true;
                BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageRotateWorkArea(this.workArea.getUUID(), false));
                this.markWorkAreaPending();
                this.onAreaMoved();
            }
        ));

        rotateRight = addRenderableWidget(new ExtendedButton(x - buttonWidth / 2 + buttonWidth, y - buttonHeight / 2 + buttonHeight, buttonWidth, buttonHeight, Component.literal("\u21BA"),
            btn -> {
                this.workArea.showBox = true;
                BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageRotateWorkArea(this.workArea.getUUID(), true));
                this.markWorkAreaPending();
                this.onAreaMoved();
            }
        ));

        // Move Up — nudge the work area one block up (shift = five).
        moveUp = addRenderableWidget(new ExtendedButton(x - buttonWidth / 2 - buttonWidth, y + buttonHeight / 2 - buttonHeight * 2, buttonWidth, buttonHeight, TEXT_UP,
            btn -> {
                this.workArea.showBox = true;
                int delta = hasShiftDown() ? 5 : 1;
                Vec3 newPos = new Vec3(workArea.getX(), workArea.getY() + delta, workArea.getZ());
                BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageUpdateWorkArea(this.workArea.getUUID(), this.workArea.getCustomName().getString(), newPos, false));
                this.markWorkAreaPending();
                this.onAreaMoved();
            }
        ));

        // Move Down — nudge the work area one block down (shift = five).
        moveDown = addRenderableWidget(new ExtendedButton(x - buttonWidth / 2 + buttonWidth, y + buttonHeight / 2 - buttonHeight * 2, buttonWidth, buttonHeight, TEXT_DOWN,
            btn -> {
                this.workArea.showBox = true;
                int delta = hasShiftDown() ? 5 : 1;
                Vec3 newPos = new Vec3(workArea.getX(), workArea.getY() - delta, workArea.getZ());
                BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageUpdateWorkArea(this.workArea.getUUID(), this.workArea.getCustomName().getString(), newPos, false));
                this.markWorkAreaPending();
                this.onAreaMoved();
            }
        ));
        //OWNER STUFF
        if(playerInfo != null){
            this.selectedPlayerWidget = new SelectedPlayerWidget(font, this.guiLeft + 180, this.guiTop + 52, 126, 20, Component.literal("x"),
                () -> {
                    playerInfo = null;
                    this.selectedPlayerWidget.setPlayer(null, null);
                    this.setButtons();
                }
            );

            this.selectedPlayerWidget.setPlayer(playerInfo.getUUID(), playerInfo.getName());
            addRenderableWidget(this.selectedPlayerWidget);
        }
        else {
            addRenderableWidget(new ExtendedButton(this.guiLeft + 180, this.guiTop + 52 , 126, 20, SelectPlayerScreen.TITLE,
                button -> {
                    minecraft.setScreen(new SelectPlayerScreen(this, player, SelectPlayerScreen.TITLE, SelectPlayerScreen.BUTTON_SELECT, SelectPlayerScreen.BUTTON_SELECT_TOOLTIP, false, PlayersList.FilterType.NONE,
                            (playerInfo) -> {
                                this.playerInfo = playerInfo;
                                BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageUpdateOwner(this.workArea.getUUID(), playerInfo));
                                this.markWorkAreaPending();
                                this.workArea.setPlayerName(playerInfo.getName());
                                this.workArea.setPlayerUUID(playerInfo.getUUID());
                                minecraft.setScreen(this);
                                this.onClose();
                            }
                    ));
                }
            ));
        }
    }

    protected void markWorkAreaPending() {
        this.workAreaPendingUntilTick = this.player.tickCount + 60L;
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        int panelLeft = this.guiLeft;
        int panelTop = this.guiTop;

        MilitaryGuiStyle.parchmentPanel(guiGraphics, panelLeft, panelTop, this.xSize, this.ySize);
        MilitaryGuiStyle.titleStrip(guiGraphics, panelLeft + 3, panelTop + 3, this.xSize - 6, 19);
        MilitaryGuiStyle.insetPanel(guiGraphics, panelLeft + 10, panelTop + 48, this.xSize - 20, 70);
        MilitaryGuiStyle.insetPanel(guiGraphics, panelLeft + 176, panelTop + 48, 134, 46);
        MilitaryGuiStyle.insetPanel(guiGraphics, panelLeft + 10, panelTop + 124, this.xSize - 20, this.ySize - 134);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.renderForeground(guiGraphics, mouseX, mouseY, delta);
        Component status;
        int color;
        if (this.player.tickCount < this.workAreaPendingUntilTick) {
            status = Component.translatable("gui.bannermod.work_area.state.stale");
            color = MilitaryGuiStyle.TEXT_WARN;
        } else if (this.workArea.getPlayerUUID() == null) {
            status = Component.translatable("gui.bannermod.work_area.state.empty_owner");
            color = MilitaryGuiStyle.TEXT_MUTED;
        } else {
            status = Component.translatable("gui.bannermod.work_area.state.ready");
            color = MilitaryGuiStyle.TEXT_GOOD;
        }
        guiGraphics.drawCenteredString(this.font, this.title, this.width / 2, this.guiTop + 9, MilitaryGuiStyle.TEXT);
        guiGraphics.drawString(this.font, status, this.guiLeft + this.xSize - 16 - this.font.width(status), this.guiTop + 9, color, false);

        guiGraphics.drawString(this.font, TEXT_SECTION_POSITION, this.guiLeft + 16, this.guiTop + 32, MilitaryGuiStyle.TEXT, false);
        guiGraphics.drawString(this.font, TEXT_SECTION_OWNER, this.guiLeft + 182, this.guiTop + 32, MilitaryGuiStyle.TEXT, false);
        guiGraphics.drawString(this.font, TEXT_SECTION_SETTINGS, this.guiLeft + 16, this.guiTop + 108, MilitaryGuiStyle.TEXT, false);

        int movementHintY = drawWrapped(guiGraphics, TEXT_HINT_MOVE, this.guiLeft + 16, this.guiTop + 93, 150, MilitaryGuiStyle.TEXT_MUTED);
        drawWrapped(guiGraphics, TEXT_HINT_SHIFT, this.guiLeft + 16, movementHintY + 2, 150, MilitaryGuiStyle.TEXT_MUTED);

        Component ownerHint = this.workArea.getPlayerUUID() == null ? TEXT_HINT_OWNER_MISSING : TEXT_HINT_OWNER_READY;
        drawWrapped(guiGraphics, ownerHint, this.guiLeft + 182, this.guiTop + 76, 122, MilitaryGuiStyle.TEXT_MUTED);

        int summaryY = this.guiTop + 138;
        for (Component line : getSettingSummaryLines()) {
            summaryY = drawWrapped(guiGraphics, line, this.guiLeft + 18, summaryY, this.xSize - 36, MilitaryGuiStyle.TEXT_MUTED) + 2;
        }
    }

    protected List<Component> getSettingSummaryLines() {
        return List.of();
    }

    private int drawWrapped(GuiGraphics guiGraphics, Component text, int x, int y, int width, int color) {
        for (var line : this.font.split(text, width)) {
            guiGraphics.drawString(this.font, line, x, y, color, false);
            y += 10;
        }
        return y;
    }

    public void onAreaMoved() {}

    public void onClose() {
        super.onClose();

        this.workArea.showBox = false;
    }
}
