package com.talhanation.bannermod.client.military.gui;

import com.mojang.blaze3d.vertex.PoseStack;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.military.AssassinLeaderEntity;
import com.talhanation.bannermod.inventory.military.AssassinLeaderMenu;
import com.talhanation.bannermod.network.messages.military.MessageAssassinCount;
import com.talhanation.bannermod.network.messages.military.MessageAssassinate;
import de.maxhenkel.corelib.inventory.ScreenBase;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.lwjgl.glfw.GLFW;

import java.awt.*;


@OnlyIn(Dist.CLIENT)
public class AssassinLeaderScreen extends ScreenBase<AssassinLeaderMenu> {
    private static final ResourceLocation RESOURCE_LOCATION = ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/gui/assassin_gui.png");

    private static final MutableComponent TITLE = Component.translatable("gui.recruits.assassin.title");
    private static final MutableComponent STATUS = Component.translatable("gui.recruits.assassin.status.unavailable");

    private static final int fontColor = 4210752;

    private final Inventory playerInventory;
    private final AssassinLeaderEntity assassinLeaderEntity;
    private EditBox textField;

    private int count;

    public AssassinLeaderScreen(AssassinLeaderMenu container, Inventory playerInventory, Component title) {
        super(RESOURCE_LOCATION, container, playerInventory, Component.literal(""));
        this.playerInventory = playerInventory;
        this.assassinLeaderEntity = container.getEntity();
        this.count = assassinLeaderEntity.getCount();
        imageWidth = 176;
        imageHeight = 218;
    }

    @Override
    protected void init() {
        super.init();
        /*
        addRenderableWidget(new Button(leftPos + 10, topPos + 60, 8, 12, Component.literal("<"), button -> {
            this.count = assassinLeaderEntity.getCount();
            if (this.count != 0) {
                this.count--;
                BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageAssassinCount(this.count, assassinLeaderEntity.getUUID()));
            }
        }));

        addRenderableWidget(new Button(leftPos + 10 + 30, topPos + 60, 8, 12, Component.literal(">"), button -> {
            this.count = assassinLeaderEntity.getCount();
            if (this.count != assassinLeaderEntity.getMaxAssassinCount()) {
                this.count++;
                BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageAssassinCount(this.count, assassinLeaderEntity.getUUID()));
            }
        }));

        //HUNT
        addRenderableWidget(new Button(leftPos + 77 + 25, topPos + 4, 50, 12, Component.literal("Assassinate"), button -> {
            int assassinateCost = assassinLeaderEntity.calculateAssassinateCosts(assassinLeaderEntity.getAssassinCosts(), this.count);
            //if(AssassinEvents.playerHasEnoughEmeralds(playerInventory.player, assassinateCost))
                BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageAssassinate(textField.getValue(), this.count, assassinateCost));
            //else
                playerInventory.player.sendSystemMessage(Component.literal(assassinLeaderEntity.getName() + ": You dont have enough Emeralds"));
        onClose();
        }));

        textField = new EditBox(font, leftPos + 30, topPos + 30, 116, 16, Component.literal(("")));
        textField.setTextColor(-1);
        textField.setTextColorUneditable(-1);
        textField.setBordered(true);
        textField.setMaxLength(24);

        addRenderableOnly(textField);
        setInitialFocus(textField);

         */
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        guiGraphics.drawString(font, TITLE, 10, 8, fontColor, false);
        guiGraphics.drawString(font, Component.translatable("gui.recruits.assassin.count", this.count, assassinLeaderEntity.getMaxAssassinCount()), 10, 28, fontColor, false);
        guiGraphics.drawString(font, Component.translatable("gui.recruits.assassin.cost_each", assassinLeaderEntity.getAssassinCosts()), 10, 40, fontColor, false);
        guiGraphics.drawString(font, Component.translatable("gui.recruits.assassin.cost_total", assassinLeaderEntity.calculateAssassinateCosts(this.count, assassinLeaderEntity.getAssassinCosts())), 10, 52, fontColor, false);
        guiGraphics.drawString(font, STATUS, 10, 70, 0x8A1F11, false);
    }


    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, partialTicks, mouseX, mouseY);

    }


    @Override
    public boolean keyPressed(int key, int a, int b) {
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            minecraft.player.closeContainer();
            return true;
        }

        if (textField != null && (textField.keyPressed(key, a, b) || textField.canConsumeInput())) {
            return true;
        }
        return super.keyPressed(key, a, b);
    }

    @Override
    public void onClose() {
        super.onClose();
    }

    @Override
    public boolean isPauseScreen() {
        return false;

    }
}
