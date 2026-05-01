package com.talhanation.bannermod.client.military.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.client.military.gui.player.PlayersList;
import com.talhanation.bannermod.client.military.gui.player.SelectPlayerScreen;
import com.talhanation.bannermod.client.military.gui.widgets.SelectedPlayerWidget;
import com.talhanation.bannermod.entity.military.MessengerEntity;
import com.talhanation.bannermod.network.messages.military.MessageSendMessenger;
import com.talhanation.bannermod.persistence.military.RecruitsPlayerInfo;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.MultiLineEditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.FastColor;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.lwjgl.glfw.GLFW;


public class MessengerScreen extends RecruitsScreenBase {

    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/gui/professions/blank_gui.png");
    protected static final int PLAYER_NAME_COLOR = FastColor.ARGB32.color(255, 255, 255, 255);
    private final Player player;
    public static RecruitsPlayerInfo playerInfo;
    private final MessengerEntity messenger;
    private MultiLineEditBox textFieldMessage;
    private SelectedPlayerWidget selectedPlayerWidget;
    private Button sendButton;
    private boolean sendAccepted;
    private Component dispatchStatus = Component.empty();
    private int dispatchStatusColor = FONT_COLOR;
    private static final MutableComponent TOOLTIP_MESSENGER = Component.translatable("gui.recruits.inv.tooltip.messenger");
    private static final MutableComponent BUTTON_SEND_MESSENGER = Component.translatable("gui.recruits.inv.text.send_messenger");
    private static final Component TITLE = Component.translatable("gui.recruits.messenger.compose_title");
    private static final Component LABEL_PLAYER = Component.translatable("gui.recruits.messenger.compose_player");
    private static final Component LABEL_MESSAGE = Component.translatable("gui.recruits.messenger.compose_message");
    private static final Component LABEL_PARCEL = Component.translatable("gui.recruits.messenger.compose_parcel");
    private static final Component LABEL_EMPTY_PARCEL = Component.translatable("gui.recruits.messenger.compose_no_parcel");
    public MessengerScreen(MessengerEntity messenger, Player player) {
        super(TITLE, 197,250);
        this.player = player;
        this.messenger = messenger;
    }

    @Override
    public boolean keyPressed(int key, int a, int b) {
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }

        setFocused(textFieldMessage);
        return textFieldMessage.keyPressed(key, a, b) || textFieldMessage.isFocused() || super.keyPressed(key, a, b);
    }
    @Override
    protected void init() {
        super.init();

        setButtons();
    }
    public void tick() {
        super.tick();
        updateDispatchState();
    }

    public boolean mouseClicked(double p_100753_, double p_100754_, int p_100755_) {
        if (this.textFieldMessage.isFocused()) {
            this.textFieldMessage.mouseClicked(p_100753_, p_100754_, p_100755_);
        }
        return super.mouseClicked(p_100753_, p_100754_, p_100755_);
    }

    private void setButtons() {
        clearWidgets();

        this.textFieldMessage = new MultiLineEditBox(font, guiLeft + 3, guiTop + ySize - 203,  186, 150, Component.literal(""), Component.literal(""));
        this.textFieldMessage.setValue(messenger.getMessage());
        addRenderableWidget(textFieldMessage);

        this.sendButton = addRenderableWidget(new ExtendedButton(guiLeft + 33, guiTop + ySize - 52 , 128, 20, BUTTON_SEND_MESSENGER,
                button -> {
                    BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageSendMessenger(messenger.getUUID(), playerInfo, textFieldMessage.getValue(), true));
                    this.sendAccepted = true;
                    updateDispatchState();
                }
        ));
        this.sendButton.setTooltip(Tooltip.create(TOOLTIP_MESSENGER));

        if(playerInfo != null){
            this.selectedPlayerWidget = new SelectedPlayerWidget(font, guiLeft + 33, guiTop + ySize - 235, 128, 20, Component.literal("x"), // Button label
                    () -> {
                        sendAccepted = false;
                        playerInfo = null;
                        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageSendMessenger(messenger.getUUID(), playerInfo, textFieldMessage.getValue(), false));
                        this.selectedPlayerWidget.setPlayer(null, null);
                        this.setButtons();
                    }
            );

            this.selectedPlayerWidget.setPlayer(playerInfo.getUUID(), playerInfo.getName());
            addRenderableWidget(this.selectedPlayerWidget);
        }
        else
        {
            Button selectPlayerButton = addRenderableWidget(new ExtendedButton(guiLeft + 33, guiTop + ySize - 235, 128, 20, SelectPlayerScreen.TITLE,
                    button -> {
                        sendAccepted = false;
                        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageSendMessenger(messenger.getUUID(), playerInfo, textFieldMessage.getValue(), false));
                        minecraft.setScreen(new SelectPlayerScreen(this, player, SelectPlayerScreen.TITLE, SelectPlayerScreen.BUTTON_SELECT, SelectPlayerScreen.BUTTON_SELECT_TOOLTIP, false, PlayersList.FilterType.NONE,
                                (playerInfo) -> {
                                    sendAccepted = false;
                                    MessengerScreen.playerInfo = playerInfo;
                                    minecraft.setScreen(this);
                                }
                        ));

                    }
            ));
            selectPlayerButton.setTooltip(Tooltip.create(TOOLTIP_MESSENGER));
        }
        updateDispatchState();
    }

    private void updateDispatchState() {
        if (this.sendButton == null || this.textFieldMessage == null) {
            return;
        }
        boolean hasRecipient = playerInfo != null;
        boolean hasMessage = !this.textFieldMessage.getValue().isBlank();
        if (this.sendAccepted) {
            this.dispatchStatus = Component.translatable("gui.recruits.messenger.compose_status.accepted");
            this.dispatchStatusColor = 0x2E5D32;
            this.sendButton.active = false;
            return;
        }
        if (!hasRecipient) {
            this.dispatchStatus = Component.translatable("gui.recruits.messenger.compose_status.select_player");
            this.dispatchStatusColor = 0x8A1F11;
            this.sendButton.active = false;
            return;
        }
        if (!hasMessage) {
            this.dispatchStatus = Component.translatable("gui.recruits.messenger.compose_status.write_message");
            this.dispatchStatusColor = 0x8A1F11;
            this.sendButton.active = false;
            return;
        }
        this.dispatchStatus = Component.translatable("gui.recruits.messenger.compose_status.ready");
        this.dispatchStatusColor = 0x2E5D32;
        this.sendButton.active = true;
    }

    public void onClose(){
        super.onClose();
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageSendMessenger(messenger.getUUID(), playerInfo, textFieldMessage.getValue(), false));
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        guiGraphics.blit(TEXTURE, guiLeft, guiTop, 0, 0, xSize, ySize);
        drawFramedPanel(guiGraphics, guiLeft + 12, guiTop + 18, xSize - 24, 26);
        drawFramedPanel(guiGraphics, guiLeft + 8, guiTop + 48, xSize - 16, 158);
        drawDarkInset(guiGraphics, guiLeft + 24, guiTop + ySize - 80, xSize - 48, 18);
    }
    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.drawString(font, TITLE, guiLeft + xSize / 2 - font.width(TITLE) / 2, guiTop + 6, FONT_COLOR, false);
        guiGraphics.drawString(font, LABEL_PLAYER, guiLeft + 16, guiTop + 24, FONT_COLOR, false);
        guiGraphics.drawString(font, LABEL_MESSAGE, guiLeft + 16, guiTop + 52, FONT_COLOR, false);
        guiGraphics.drawString(font, dispatchStatus, guiLeft + 28, guiTop + ySize - 75, dispatchStatusColor, false);

        if(!messenger.getMainHandItem().isEmpty()){
            guiGraphics.drawString(font, LABEL_PARCEL, guiLeft + 120, guiTop + ySize - 75, FONT_COLOR, false);
            guiGraphics.renderFakeItem(messenger.getMainHandItem(), guiLeft + 140, guiTop + ySize - 48);
            guiGraphics.renderItemDecorations(font, messenger.getMainHandItem(),guiLeft + 140, guiTop + ySize - 48);
        } else {
            guiGraphics.drawString(font, LABEL_EMPTY_PARCEL, guiLeft + 110, guiTop + ySize - 48, 0x6E5A45, false);
        }
    }
}
