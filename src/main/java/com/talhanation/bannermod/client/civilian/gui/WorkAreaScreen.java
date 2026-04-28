package com.talhanation.bannermod.client.civilian.gui;

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
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;

public abstract class WorkAreaScreen extends RecruitsScreenBase {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/gui/workareascreen.png");
    private static final MutableComponent TEXT_FORWARD = Component.translatable("gui.workers.command.text.forward");
    private static final MutableComponent TEXT_BACKWARD = Component.translatable("gui.workers.command.text.back");
    private static final MutableComponent TEXT_LEFT = Component.translatable("gui.workers.command.text.left");
    private static final MutableComponent TEXT_RIGHT = Component.translatable("gui.workers.command.text.right");
    private static final MutableComponent TEXT_DESTROY = Component.translatable("gui.workers.command.text.destroy");
    private static final MutableComponent TEXT_UP = Component.translatable("gui.workers.command.text.up");
    private static final MutableComponent TEXT_DOWN = Component.translatable("gui.workers.command.text.down");
    private EditBox textFieldName;
    private Button moveForward;
    private Button moveBackward;
    private Button moveLeft;
    private Button moveRight;
    private Button moveUp;
    private Button moveDown;
    private Button destroy;
    private Button rotateLeft;
    private Button rotateRight;

    public Player player;
    public AbstractWorkAreaEntity workArea;

    public SelectedPlayerWidget selectedPlayerWidget;
    public RecruitsPlayerInfo playerInfo;

    protected WorkAreaScreen(Component title, AbstractWorkAreaEntity workArea, Player player) {
        super(title, 200, 222);
        this.workArea = workArea;
        this.player = player;
    }

    @Override
    protected void init() {
        super.init();
        workArea.showBox = true;
        playerInfo = new RecruitsPlayerInfo(workArea.getPlayerUUID(), workArea.getPlayerName());
        setButtons();
    }
    public int x;
    public int y;
    public void setButtons(){
        clearWidgets();
        x = this.width / 2;
        y = this.height / 2 - 70;
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
                this.onAreaMoved();
            }
        ));

        rotateRight = addRenderableWidget(new ExtendedButton(x - buttonWidth / 2 + buttonWidth, y - buttonHeight / 2 + buttonHeight, buttonWidth, buttonHeight, Component.literal("\u21BA"),
            btn -> {
                this.workArea.showBox = true;
                BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageRotateWorkArea(this.workArea.getUUID(), true));
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
                this.onAreaMoved();
            }
        ));
        //OWNER STUFF
        if(playerInfo != null){
            this.selectedPlayerWidget = new SelectedPlayerWidget(font, x + 80, y - 50, 120, 20, Component.literal("x"), // Button label
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
            Button selectPlayerButton = addRenderableWidget(new ExtendedButton(x + 80, y - 50 , 120, 20, SelectPlayerScreen.TITLE,
                button -> {
                    minecraft.setScreen(new SelectPlayerScreen(this, player, SelectPlayerScreen.TITLE, SelectPlayerScreen.BUTTON_SELECT, SelectPlayerScreen.BUTTON_SELECT_TOOLTIP, false, PlayersList.FilterType.NONE,
                            (playerInfo) -> {
                                this.playerInfo = playerInfo;
                                BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageUpdateOwner(this.workArea.getUUID(), playerInfo));
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

    public void onAreaMoved() {}

    public void onClose() {
        super.onClose();

        this.workArea.showBox = false;
    }
}
