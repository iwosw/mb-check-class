package com.talhanation.bannermod.client.military.gui;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.inventory.military.PromoteContainer;
import com.talhanation.bannermod.network.messages.military.*;
import de.maxhenkel.corelib.inventory.ScreenBase;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;

import net.neoforged.neoforge.client.gui.widget.ExtendedButton;

import org.lwjgl.glfw.GLFW;


public class PromoteScreen extends ScreenBase<PromoteContainer> {


    private static final ResourceLocation RESOURCE_LOCATION = ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/gui/professions/professions_main_gui.png");
    private final Player player;
    private final AbstractRecruitEntity recruit;
    private EditBox textField;
    private int leftPos;
    private int topPos;

    private static final MutableComponent BUTTON_MESSENGER = Component.translatable("gui.bannermod.inv.text.messenger");
    private static final MutableComponent TOOLTIP_MESSENGER = Component.translatable("gui.bannermod.inv.tooltip.messenger");

    private static final MutableComponent BUTTON_PATROL_LEADER = Component.translatable("gui.bannermod.inv.text.patrol_leader");
    private static final MutableComponent TOOLTIP_PATROL_LEADER = Component.translatable("gui.bannermod.inv.tooltip.patrol_leader");
    private static final MutableComponent BUTTON_CAPTAIN = Component.translatable("gui.bannermod.inv.text.captain");
    private static final MutableComponent TOOLTIP_CAPTAIN = Component.translatable("gui.bannermod.inv.tooltip.captain");
    private static final MutableComponent TOOLTIP_CAPTAIN_DISABLED = Component.translatable("gui.bannermod.inv.tooltip.captain_disabled");
    private static final MutableComponent BUTTON_SCOUT = Component.translatable("gui.bannermod.inv.text.scout");
    private static final MutableComponent TOOLTIP_SCOUT = Component.translatable("gui.bannermod.inv.tooltip.scout");

    private static final MutableComponent BUTTON_GOVERNOR = Component.translatable("gui.bannermod.inv.text.governor");
    private static final MutableComponent TOOLTIP_GOVERNOR = Component.translatable("gui.bannermod.inv.tooltip.governor");

    private static final MutableComponent BUTTON_ASSASSIN = Component.translatable("gui.bannermod.inv.text.assassin");
    private static final MutableComponent TOOLTIP_ASSASSIN = Component.translatable("gui.bannermod.inv.tooltip.assassin");
    private static final MutableComponent TOOLTIP_ASSASSIN_DISABLED = Component.translatable("gui.bannermod.inv.tooltip.assassin_disabled");

    private static final MutableComponent BUTTON_SPY = Component.translatable("gui.bannermod.inv.text.spy");
    private static final MutableComponent TOOLTIP_SPY = Component.translatable("gui.bannermod.inv.tooltip.spy");
    private static final MutableComponent TOOLTIP_SPY_DISABLED = Component.translatable("gui.bannermod.inv.tooltip.spy_disabled");

    private static final MutableComponent BUTTON_SIEGE_ENGINEER = Component.translatable("gui.bannermod.inv.text.siege_engineer");
    private static final MutableComponent TOOLTIP_SIEGE_ENGINEER = Component.translatable("gui.bannermod.inv.tooltip.siege_engineer");
    private static final MutableComponent TOOLTIP_SIEGE_ENGINEER_DISABLED = Component.translatable("gui.bannermod.inv.tooltip.siege_engineer_disabled");

    private static final MutableComponent BUTTON_ROGUE = Component.translatable("gui.bannermod.inv.text.rogue");
    private static final MutableComponent TOOLTIP_ROGUE = Component.translatable("gui.bannermod.inv.tooltip.rogue");
    private static final MutableComponent TOOLTIP_ROGUE_DISABLED = Component.translatable("gui.bannermod.inv.tooltip.rogue_disabled");
    private static final MutableComponent NAME_LABEL = Component.translatable("gui.recruits.promote.name_label");
    private static final MutableComponent TITLE = Component.translatable("gui.recruits.promote.screen.title");

    public PromoteScreen(PromoteContainer container, Inventory playerInventory, Component title) {
        super(RESOURCE_LOCATION, container, playerInventory, Component.literal(""));
        this.imageWidth = 197;
        this.imageHeight = 250;
        this.player = container.getPlayerEntity();
        this.recruit = container.getRecruit();

    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width - this.imageWidth) / 2;
        this.topPos = (this.height - this.imageHeight) / 2;

        setWidgets();
    }

    protected void containerTick() {
        super.containerTick();
    }


    private void setEditBox() {
        Component name = Component.translatable("gui.recruits.promote.field.name");
        if(recruit.getCustomName() != null) name = recruit.getCustomName();

        textField = new EditBox(font, leftPos + 16, topPos + 8, 170, 20, name);
        textField.setValue(name.getString());
        textField.setTextColor(-1);
        textField.setTextColorUneditable(-1);
        textField.setBordered(true);
        textField.setFocused(true);
        textField.setMaxLength(13);

        addRenderableWidget(textField);
        setInitialFocus(textField);
    }

    @Override
    public boolean mouseClicked(double p_97748_, double p_97749_, int p_97750_) {
        textField.setFocused(true);

        return super.mouseClicked(p_97748_, p_97749_, p_97750_);

    }

    private void setWidgets() {
        clearWidgets();
        setEditBox();
        createProfessionButtons(BUTTON_MESSENGER, TOOLTIP_MESSENGER, 0, recruit.getXpLevel() >= 3);
        createProfessionButtons(BUTTON_SCOUT, TOOLTIP_SCOUT, 1, recruit.getXpLevel() >= 3);

        createProfessionButtons(BUTTON_PATROL_LEADER, TOOLTIP_PATROL_LEADER, 2,recruit.getXpLevel() >= 5);
        createProfessionButtons(BUTTON_CAPTAIN, BannerModMain.isSmallShipsCompatible ? TOOLTIP_CAPTAIN : TOOLTIP_CAPTAIN_DISABLED, 3, recruit.getXpLevel() >= 5 && BannerModMain.isSmallShipsLoaded && BannerModMain.isSmallShipsCompatible);
        // Assassin / Spy / Rogue / Siege-Engineer are gated off until the systems land.
        // Surface a "feature disabled" tooltip rather than the active-state description so a
        // greyed button explains itself to the player.
        createProfessionButtons(BUTTON_ASSASSIN, TOOLTIP_ASSASSIN_DISABLED, 4, false);
        createProfessionButtons(BUTTON_SIEGE_ENGINEER, TOOLTIP_SIEGE_ENGINEER_DISABLED, 5, false);

        createProfessionButtons(BUTTON_GOVERNOR, TOOLTIP_GOVERNOR, 6, canDesignateGovernor());
        createProfessionButtons(BUTTON_SPY, TOOLTIP_SPY_DISABLED, 7, false);
        createProfessionButtons(BUTTON_ROGUE, TOOLTIP_ROGUE_DISABLED, 8, false);
    }

    private boolean canDesignateGovernor() {
        return recruit.getXpLevel() >= 7 && recruit.getOwnerUUID() != null;
    }

    private Button createProfessionButtons(Component buttonText, Component buttonTooltip, int professionID, boolean active){
        Component clamped = MilitaryGuiStyle.clampLabel(font, buttonText, 80 - 6);
        Button professionButton = addRenderableWidget(new ExtendedButton(leftPos + 59, 31 + topPos + 23 * professionID, 80, 20, clamped,
                btn -> {
                    if (recruit != null) {
                        String name = this.textField.getValue();
                        if(name.isEmpty() || name.isBlank()){
                            name = recruit.getName().getString();
                        }

                        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessagePromoteRecruit(this.recruit.getUUID(), professionID, name));
                        onClose();
                    }
                }
        ));
        professionButton.setTooltip(Tooltip.create(buttonTooltip));
        professionButton.active = active;
        return professionButton;
    }

    @Override
    protected void renderBg(GuiGraphics guiGraphics, float partialTicks, int mouseX, int mouseY) {
        // Replace ad-hoc texture chrome with parchment palette.
        MilitaryGuiStyle.parchmentPanel(guiGraphics, leftPos, topPos, imageWidth, imageHeight);
        MilitaryGuiStyle.titleStrip(guiGraphics, leftPos + 10, topPos + 8, imageWidth - 20, 16);
        MilitaryGuiStyle.parchmentInset(guiGraphics, leftPos + 14, topPos + 30, imageWidth - 28, 200);
        MilitaryGuiStyle.insetPanel(guiGraphics, leftPos + 14, topPos + 232, imageWidth - 28, 14);
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        super.renderLabels(guiGraphics, mouseX, mouseY);
        MilitaryGuiStyle.drawCenteredTitle(guiGraphics, font, TITLE, 0, 11, imageWidth);
        guiGraphics.drawString(font, NAME_LABEL, 16, 32, MilitaryGuiStyle.TEXT_DARK, false);
        Component clampedName = MilitaryGuiStyle.clampLabel(font, recruit.getDisplayName(), imageWidth - 24);
        guiGraphics.drawString(font, clampedName, 16, 50, MilitaryGuiStyle.TEXT_DARK, false);

        Component status = recruit.getXpLevel() >= 7
                ? Component.translatable("gui.recruits.promote.status.ready")
                : recruit.getXpLevel() >= 5
                ? Component.translatable("gui.recruits.promote.status.level7")
                : recruit.getXpLevel() >= 3
                ? Component.translatable("gui.recruits.promote.status.level5")
                : Component.translatable("gui.recruits.promote.status.level3");
        Component statusClamped = MilitaryGuiStyle.clampLabel(font, status, imageWidth - 28);
        guiGraphics.drawString(font, statusClamped, 16, 235, MilitaryGuiStyle.TEXT, false);
    }

    @Override
    public boolean keyPressed(int key, int a, int b) {
        if (key == GLFW.GLFW_KEY_ESCAPE) {
            this.onClose();
            return true;
        }
        setFocused(textField);

        return textField.keyPressed(key, a, b) || textField.canConsumeInput() || super.keyPressed(key, a, b);
    }
}
