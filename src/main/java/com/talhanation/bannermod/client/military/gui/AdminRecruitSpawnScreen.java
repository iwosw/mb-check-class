package com.talhanation.bannermod.client.military.gui;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.inventory.military.AdminRecruitSpawnMenu;
import com.talhanation.bannermod.network.messages.military.MessageAdminRecruitSpawn;
import com.talhanation.bannermod.registry.military.ModEntityTypes;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.inventory.AbstractContainerScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import java.util.List;
import java.util.function.Supplier;

public class AdminRecruitSpawnScreen extends AbstractContainerScreen<AdminRecruitSpawnMenu> {
    private static final int GUI_WIDTH = 236;
    private static final int GUI_HEIGHT = 152;
    private static final List<Supplier<? extends EntityType<? extends AbstractRecruitEntity>>> RECRUIT_TYPES = List.of(
            ModEntityTypes.RECRUIT::get,
            ModEntityTypes.RECRUIT_SHIELDMAN::get,
            ModEntityTypes.BOWMAN::get,
            ModEntityTypes.CROSSBOWMAN::get,
            ModEntityTypes.HORSEMAN::get,
            ModEntityTypes.NOMAD::get,
            ModEntityTypes.SCOUT::get,
            ModEntityTypes.MESSENGER::get,
            ModEntityTypes.PATROL_LEADER::get,
            ModEntityTypes.CAPTAIN::get
    );

    private int selectedIndex;
    private int spawnCount = 1;

    public AdminRecruitSpawnScreen(AdminRecruitSpawnMenu menu, Inventory inventory, Component title) {
        super(menu, inventory, title);
        this.imageWidth = GUI_WIDTH;
        this.imageHeight = GUI_HEIGHT;
        this.inventoryLabelY = 10000;
        this.titleLabelY = 10000;
    }

    public static void openLocal(Player player) {
        if (player == null) {
            return;
        }
        Minecraft minecraft = Minecraft.getInstance();
        minecraft.setScreen(new AdminRecruitSpawnScreen(
                new AdminRecruitSpawnMenu(0, player),
                player.getInventory(),
                Component.translatable("gui.bannermod.admin_recruit_spawn.title")
        ));
    }

    @Override
    protected void init() {
        super.init();
        int left = this.leftPos;
        int top = this.topPos;

        this.addRenderableWidget(new AdminButton(left + 14, top + 34, 20, 20, Component.literal("<"), button -> cycleType(-1)));
        this.addRenderableWidget(new AdminButton(left + 202, top + 34, 20, 20, Component.literal(">"), button -> cycleType(1)));
        this.addRenderableWidget(new AdminButton(left + 70, top + 86, 20, 20, Component.literal("-"), button -> changeCount(-1)));
        this.addRenderableWidget(new AdminButton(left + 146, top + 86, 20, 20, Component.literal("+"), button -> changeCount(1)));
        this.addRenderableWidget(new AdminButton(left + 14, top + 118, 100, 20,
                Component.translatable("gui.bannermod.admin_recruit_spawn.spawn"), button -> spawnSelected()));
        this.addRenderableWidget(new AdminButton(left + 122, top + 118, 100, 20,
                Component.translatable("gui.bannermod.admin_recruit_spawn.close"), button -> onClose()));
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        this.renderTooltip(graphics, mouseX, mouseY);
    }

    @Override
    protected void renderBg(GuiGraphics graphics, float partialTick, int mouseX, int mouseY) {
        MilitaryGuiStyle.parchmentPanel(graphics, this.leftPos, this.topPos, this.imageWidth, this.imageHeight);
        MilitaryGuiStyle.titleStrip(graphics, this.leftPos + 6, this.topPos + 6, this.imageWidth - 12, 14);
        MilitaryGuiStyle.insetPanel(graphics, this.leftPos + 14, this.topPos + 32, this.imageWidth - 28, 24);
        MilitaryGuiStyle.parchmentInset(graphics, this.leftPos + 62, this.topPos + 84, 112, 24);
        graphics.fill(this.leftPos + 14, this.topPos + 72, this.leftPos + this.imageWidth - 14, this.topPos + 73, 0x665A4025);
    }

    @Override
    protected void renderLabels(GuiGraphics graphics, int mouseX, int mouseY) {
        graphics.drawCenteredString(this.font, this.title, this.imageWidth / 2, 10, MilitaryGuiStyle.TEXT);
        graphics.drawString(this.font, Component.translatable("gui.bannermod.admin_recruit_spawn.type"), 14, 24, MilitaryGuiStyle.TEXT_DARK, false);
        graphics.drawCenteredString(this.font, selectedType().getDescription(), this.imageWidth / 2, 41, MilitaryGuiStyle.TEXT);
        graphics.drawString(this.font, Component.translatable("gui.bannermod.admin_recruit_spawn.count"), 14, 76, MilitaryGuiStyle.TEXT_DARK, false);
        graphics.drawCenteredString(this.font, Component.literal(Integer.toString(this.spawnCount)), this.imageWidth / 2, 92, MilitaryGuiStyle.TEXT_DARK);
        graphics.drawString(this.font, Component.translatable("gui.bannermod.admin_recruit_spawn.hint"), 14, 62, 0xFF6E5535, false);
    }

    @Override
    protected void renderTooltip(GuiGraphics graphics, int x, int y) {
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private void cycleType(int delta) {
        this.selectedIndex = Math.floorMod(this.selectedIndex + delta, RECRUIT_TYPES.size());
    }

    private void changeCount(int delta) {
        this.spawnCount = Math.max(1, Math.min(16, this.spawnCount + delta));
    }

    private EntityType<? extends AbstractRecruitEntity> selectedType() {
        return RECRUIT_TYPES.get(this.selectedIndex).get();
    }

    private ResourceLocation selectedTypeId() {
        return EntityType.getKey(selectedType());
    }

    private void spawnSelected() {
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageAdminRecruitSpawn(selectedTypeId().toString(), this.spawnCount));
    }

    private static class AdminButton extends Button {
        protected AdminButton(int x, int y, int width, int height, Component message, OnPress onPress) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            MilitaryGuiStyle.commandButton(graphics, Minecraft.getInstance().font, mouseX, mouseY,
                    getX(), getY(), width, height, getMessage(), this.active, this.isHoveredOrFocused());
        }
    }
}
