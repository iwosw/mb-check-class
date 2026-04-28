package com.talhanation.bannermod.client.military.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.client.military.PatrolLeaderControlController;
import com.talhanation.bannermod.client.military.gui.widgets.ScrollDropDownMenu;
import com.talhanation.bannermod.entity.military.AbstractLeaderEntity;
import com.talhanation.bannermod.persistence.military.RecruitsGroup;
import com.talhanation.bannermod.persistence.military.RecruitsRoute;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;

import java.util.List;

public class PatrolLeaderScreen extends RecruitsScreenBase {
    private static final MutableComponent BTN_START    = Component.translatable("gui.recruits.inv.text.start");
    private static final MutableComponent BTN_STOP     = Component.translatable("gui.recruits.inv.text.stop");
    private static final MutableComponent BTN_PAUSE    = Component.translatable("gui.recruits.inv.text.pause");
    private static final MutableComponent BTN_RESUME   = Component.translatable("gui.recruits.inv.text.resume");
    private static final MutableComponent TT_START     = Component.translatable("gui.recruits.inv.tooltip.patrol_leader_start");
    private static final MutableComponent TT_STOP      = Component.translatable("gui.recruits.inv.tooltip.patrol_leader_stop");
    private static final MutableComponent TT_PAUSE     = Component.translatable("gui.recruits.inv.tooltip.patrol_leader_pause");
    private static final MutableComponent TT_RESUME    = Component.translatable("gui.recruits.inv.tooltip.patrol_leader_resume");
    private static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/gui/professions/blank_gui.png");
    private static final int TEXTURE_W = 195;
    private static final int TEXTURE_H = 160;
    private static final int FONT_COLOR = 4210752;
    private final PatrolLeaderControlController controls;
    private int leftPos;
    private int topPos;
    private ScrollDropDownMenu<RecruitsRoute>  routeDropDown;
    private ScrollDropDownMenu<RecruitsGroup>  groupDropDown;

    public PatrolLeaderScreen(AbstractLeaderEntity leaderEntity, Player player) {
        super(Component.literal(""), 197,250);
        this.controls = new PatrolLeaderControlController(leaderEntity, player);
    }

    @Override
    protected void init() {
        super.init();
        this.leftPos = (this.width  - TEXTURE_W) / 2;
        this.topPos  = (this.height - TEXTURE_H) / 2;

        controls.init();
        buildWidgets();
    }

    // -------------------------------------------------------------------------

    private void buildWidgets() {
        clearWidgets();

        int x     = leftPos + 8;
        int y     = topPos  + 10;
        int btnH  = 18;
        int fullW = TEXTURE_W - 16;

        // --- Route dropdown ---
        List<RecruitsRoute> routeOptions = controls.getRouteOptions();

        routeDropDown = new ScrollDropDownMenu<>(
                controls.getSelectedRoute(),
                x, y, fullW, btnH,
                routeOptions,
                r -> r == null ? "-- No Route --" : r.getName(),
                r -> {
                    controls.selectRoute(r);
                    buildWidgets(); // refresh button states after route selection
                }
        );
        addRenderableWidget(routeDropDown);
        y += btnH + 6;

        // --- Start / Stop row ---
        AbstractLeaderEntity.State patrolState = controls.getPatrolState();
        boolean isPatrolling = patrolState == AbstractLeaderEntity.State.PATROLLING;
        boolean isPaused     = patrolState == AbstractLeaderEntity.State.PAUSED;
        boolean canStart     = controls.getSelectedRoute() != null
                && (patrolState == AbstractLeaderEntity.State.STOPPED
                || patrolState == AbstractLeaderEntity.State.IDLE
                ||  isPaused);

        int btnW = (fullW - 4) / 2;

        Component startLabel   = isPaused  ? BTN_RESUME : BTN_START;
        Component startTooltip = isPaused  ? TT_RESUME  : TT_START;
        Component stopLabel    = isPatrolling ? BTN_PAUSE  : BTN_STOP;
        Component stopTooltip  = isPatrolling ? TT_PAUSE   : TT_STOP;

        Button startButton = addRenderableWidget(new ExtendedButton(x, y, btnW, btnH, startLabel, btn -> {
            controls.startOrResumePatrol();
            buildWidgets();
        }));
        startButton.active = canStart;
        startButton.setTooltip(Tooltip.create(
                !canStart && controls.getSelectedRoute() == null ? Component.literal("No route selected") : startTooltip));

        Button stopButton = addRenderableWidget(new ExtendedButton(x + btnW + 4, y, btnW, btnH, stopLabel, btn -> {
            controls.stopOrPausePatrol();
            buildWidgets();
        }));
        stopButton.active = patrolState != AbstractLeaderEntity.State.STOPPED
                && patrolState != AbstractLeaderEntity.State.IDLE;
        stopButton.setTooltip(Tooltip.create(stopTooltip));
        y += btnH + 8;

        // --- Report ---
        String infoLabel = "Report: " + switch (controls.getInfoMode()) {
            case ALL     -> "All";
            case HOSTILE -> "Hostiles";
            case ENEMY   -> "Enemies";
            case NONE    -> "None";
        };
        addRenderableWidget(new ExtendedButton(x, y, fullW, btnH,
                Component.literal(infoLabel), btn -> {
                    controls.cycleInfoMode();
                    buildWidgets();
                }));
        y += btnH + 4;

        // --- On Enemy ---
        String actionLabel = "On Enemy: " + switch (controls.getEnemyAction()) {
            case CHARGE          -> "Charge";
            case HOLD            -> "Hold";
            case KEEP_PATROLLING -> "Keep Patrolling";
        };
        addRenderableWidget(new ExtendedButton(x, y, fullW, btnH,
                Component.literal(actionLabel), btn -> {
                    controls.cycleEnemyAction();
                    buildWidgets();
                }));
        y += btnH + 8;

        // --- Group dropdown ---
        List<RecruitsGroup> groupOptions = controls.getGroupOptions();

        groupDropDown = new ScrollDropDownMenu<>(
                controls.getSelectedGroup(),
                x, y, fullW, btnH,
                groupOptions,
                g -> g == null ? "-- No Group --" : g.getName(),
                g -> {
                    controls.selectGroup(g);
                    buildWidgets();
                }
        );
        addRenderableWidget(groupDropDown);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (routeDropDown != null) routeDropDown.onMouseClick(mouseX, mouseY);
        if (groupDropDown != null) groupDropDown.onMouseClick(mouseX, mouseY);
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (routeDropDown != null) routeDropDown.onMouseMove(mouseX, mouseY);
        if (groupDropDown != null) groupDropDown.onMouseMove(mouseX, mouseY);
        super.mouseMoved(mouseX, mouseY);
    }
    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        RenderSystem.setShader(GameRenderer::getPositionTexShader);
        RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
        RenderSystem.setShaderTexture(0, TEXTURE);
        guiGraphics.blit(TEXTURE, guiLeft, guiTop, 0, 0, xSize, ySize);
    }
    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        int fontColor = 4210752;
    }

    @Override
    public boolean isPauseScreen() { return false; }
}
