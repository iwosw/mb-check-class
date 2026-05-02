package com.talhanation.bannermod.client.military.gui;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.military.AssassinLeaderEntity;
import com.talhanation.bannermod.inventory.military.AssassinLeaderMenu;
import com.talhanation.bannermod.network.messages.military.MessageAssassinCount;
import de.maxhenkel.corelib.inventory.ScreenBase;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Inventory;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;
import org.lwjgl.glfw.GLFW;


@OnlyIn(Dist.CLIENT)
public class AssassinLeaderScreen extends ScreenBase<AssassinLeaderMenu> {
    private static final ResourceLocation RESOURCE_LOCATION = ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/gui/assassin_gui.png");

    private static final MutableComponent TITLE = Component.translatable("gui.recruits.assassin.title");
    private static final MutableComponent SUBTITLE = Component.translatable("gui.recruits.assassin.subtitle");
    private static final MutableComponent SECTION_READINESS = Component.translatable("gui.recruits.assassin.section.readiness");
    private static final MutableComponent SECTION_CONTRACT = Component.translatable("gui.recruits.assassin.section.contract");
    private static final MutableComponent CONTRACT_HINT = Component.translatable("gui.recruits.assassin.contract_hint");
    private static final MutableComponent ACTION_DISPATCH = Component.translatable("gui.recruits.assassin.action.dispatch");
    private static final MutableComponent STATUS = Component.translatable("gui.recruits.assassin.status.unavailable");
    private static final MutableComponent STATUS_NEXT_STEP = Component.translatable("gui.recruits.assassin.status.next_step");

    private static final int fontColor = 4210752;

    private final Inventory playerInventory;
    private final AssassinLeaderEntity assassinLeaderEntity;
    private int count;
    private Component statusLine = STATUS_NEXT_STEP;
    private int statusColor = MilitaryGuiStyle.TEXT_WARN;
    private Button decreaseButton;
    private Button increaseButton;
    private Button dispatchButton;

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
        this.count = Mth.clamp(this.assassinLeaderEntity.getCount(), 0, this.assassinLeaderEntity.getMaxAssassinCount());

        this.decreaseButton = addRenderableWidget(new ExtendedButton(leftPos + 18, topPos + 76, 20, 16, Component.literal("-"),
                button -> adjustCount(-1)));
        this.increaseButton = addRenderableWidget(new ExtendedButton(leftPos + 42, topPos + 76, 20, 16, Component.literal("+"),
                button -> adjustCount(1)));
        this.dispatchButton = addRenderableWidget(new ExtendedButton(leftPos + 108, topPos + 76, 56, 16, ACTION_DISPATCH,
                button -> {
                }));
        this.dispatchButton.active = false;

        updateButtons();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        this.count = Mth.clamp(this.assassinLeaderEntity.getCount(), 0, this.assassinLeaderEntity.getMaxAssassinCount());
        updateButtons();
    }

    private void adjustCount(int delta) {
        int updated = Mth.clamp(this.count + delta, 0, this.assassinLeaderEntity.getMaxAssassinCount());
        if (updated == this.count) {
            return;
        }
        this.count = updated;
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageAssassinCount(this.count, this.assassinLeaderEntity.getUUID()));
        this.statusLine = Component.translatable("gui.recruits.assassin.status.count_updated", this.count);
        this.statusColor = MilitaryGuiStyle.TEXT_GOOD;
        updateButtons();
    }

    private void updateButtons() {
        if (this.decreaseButton != null) {
            this.decreaseButton.active = this.count > 0;
            this.decreaseButton.setTooltip(Tooltip.create(Component.translatable(
                    this.decreaseButton.active
                            ? "gui.recruits.assassin.action.decrease.tooltip"
                            : "gui.recruits.assassin.action.decrease.disabled")));
        }
        if (this.increaseButton != null) {
            this.increaseButton.active = this.count < this.assassinLeaderEntity.getMaxAssassinCount();
            this.increaseButton.setTooltip(Tooltip.create(Component.translatable(
                    this.increaseButton.active
                            ? "gui.recruits.assassin.action.increase.tooltip"
                            : "gui.recruits.assassin.action.increase.disabled")));
        }
        if (this.dispatchButton != null) {
            this.dispatchButton.active = false;
            this.dispatchButton.setTooltip(Tooltip.create(Component.translatable("gui.recruits.assassin.action.dispatch.disabled")));
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        guiGraphics.drawString(font, TITLE, 10, 8, fontColor, false);
        guiGraphics.drawString(font, SUBTITLE, 10, 20, MilitaryGuiStyle.TEXT_MUTED, false);
        guiGraphics.drawString(font, SECTION_READINESS, 14, 34, MilitaryGuiStyle.TEXT_DARK, false);
        guiGraphics.drawString(font, SECTION_CONTRACT, 96, 34, MilitaryGuiStyle.TEXT_DARK, false);
        guiGraphics.drawString(font, Component.translatable("gui.recruits.assassin.count", this.count, assassinLeaderEntity.getMaxAssassinCount()), 14, 48, fontColor, false);
        guiGraphics.drawString(font, Component.translatable("gui.recruits.assassin.cost_each", assassinLeaderEntity.getAssassinCosts()), 14, 60, fontColor, false);
        guiGraphics.drawString(font, Component.translatable("gui.recruits.assassin.cost_total", assassinLeaderEntity.calculateAssassinateCosts(this.count, assassinLeaderEntity.getAssassinCosts())), 14, 72, fontColor, false);
        drawWrapped(guiGraphics, STATUS, 96, 48, 66, MilitaryGuiStyle.TEXT_DENIED);
        drawWrapped(guiGraphics, CONTRACT_HINT, 96, 72, 66, MilitaryGuiStyle.TEXT_MUTED);
        guiGraphics.drawString(font, statusLine, 14, 96, statusColor, false);
    }


    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        super.renderBg(guiGraphics, partialTicks, mouseX, mouseY);
        MilitaryGuiStyle.parchmentPanel(guiGraphics, leftPos + 6, topPos + 6, imageWidth - 12, 98);
        MilitaryGuiStyle.parchmentInset(guiGraphics, leftPos + 10, topPos + 30, 68, 60);
        MilitaryGuiStyle.parchmentInset(guiGraphics, leftPos + 92, topPos + 30, 74, 60);
    }

    private void drawWrapped(GuiGraphics guiGraphics, Component text, int x, int y, int width, int color) {
        int lineY = y;
        for (var line : this.font.split(text, width)) {
            guiGraphics.drawString(this.font, line, x, lineY, color, false);
            lineY += 9;
        }
    }


    @Override
    public boolean keyPressed(int key, int a, int b) {
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            minecraft.player.closeContainer();
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
