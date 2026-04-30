package com.talhanation.bannermod.client.military.gui.worldmap;

import com.talhanation.bannermod.client.military.gui.widgets.DropDownMenu;
import com.talhanation.bannermod.persistence.military.RecruitsRoute;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

final class WorldMapRouteToolbar {
    private static final int ROUTE_UI_X = 10;
    private static final int ROUTE_UI_Y = 10;
    private static final int ROUTE_DROPDOWN_W = 140;
    private static final int ROUTE_BTN_SIZE = 20;
    private static final int ROUTE_BTN_GAP = 3;
    private static final int HELP_W = 236;
    private static final int HELP_H = 50;
    private static final Component TEXT_ROUTE_PLACEHOLDER = Component.translatable("gui.recruits.map.route.dropdown");
    private static final Component TEXT_ROUTE_HELP = Component.translatable("gui.recruits.map.route.help");
    private static final Component TEXT_ROUTE_HELP_SELECTED = Component.translatable("gui.recruits.map.route.help_selected");
    private static final Component TEXT_ADD_TOOLTIP = Component.translatable("gui.recruits.map.route.tooltip.add_route");
    private static final Component TEXT_EDIT_TOOLTIP = Component.translatable("gui.recruits.map.route.tooltip.edit_route");
    private static final Component TEXT_TRANSPARENCY_TOOLTIP = Component.translatable("gui.recruits.map.route.tooltip.claims");

    private final WorldMapScreen screen;

    private DropDownMenu<RecruitsRoute> routeDropDown;

    WorldMapRouteToolbar(WorldMapScreen screen) {
        this.screen = screen;
    }

    void refresh(List<RecruitsRoute> options, @Nullable RecruitsRoute selectedRoute,
                 Consumer<RecruitsRoute> onSelectedRouteChange) {
        routeDropDown = new DropDownMenu<>(
                selectedRoute,
                ROUTE_UI_X,
                ROUTE_UI_Y,
                ROUTE_DROPDOWN_W,
                ROUTE_BTN_SIZE,
                options,
                route -> route == null ? TEXT_ROUTE_PLACEHOLDER.getString() : route.getName(),
                onSelectedRouteChange
        );

        routeDropDown.setBgFill(0x80333333);
        routeDropDown.setBgFillHovered(0x80555555);
        routeDropDown.setBgFillSelected(0x80222222);
    }

    void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks,
                @Nullable RecruitsRoute selectedRoute, boolean claimTransparency) {
        renderDropdown(guiGraphics, mouseX, mouseY, partialTicks, selectedRoute);

        renderButton(guiGraphics, mouseX, mouseY, getAddButtonX(), "+", 0xFFFFFF, false);
        if (selectedRoute == null) {
            if (routeDropDown != null && routeDropDown.isOpen()) {
                renderButtonTooltip(guiGraphics, mouseX, mouseY, getAddButtonX(), TEXT_ADD_TOOLTIP);
                return;
            }
            renderHelp(guiGraphics, TEXT_ROUTE_HELP);
            renderButtonTooltip(guiGraphics, mouseX, mouseY, getAddButtonX(), TEXT_ADD_TOOLTIP);
            return;
        }

        renderButton(guiGraphics, mouseX, mouseY, getEditButtonX(), "\u2699", 0xFFFFFF, false);
        renderButton(guiGraphics, mouseX, mouseY, getTransparencyButtonX(), "\u25A1",
                claimTransparency ? 0xFFFFAA00 : 0xFFFFFF, claimTransparency);
        if (routeDropDown != null && routeDropDown.isOpen()) {
            renderButtonTooltip(guiGraphics, mouseX, mouseY, getAddButtonX(), TEXT_ADD_TOOLTIP);
            renderButtonTooltip(guiGraphics, mouseX, mouseY, getEditButtonX(), TEXT_EDIT_TOOLTIP);
            renderButtonTooltip(guiGraphics, mouseX, mouseY, getTransparencyButtonX(), TEXT_TRANSPARENCY_TOOLTIP);
            return;
        }

        renderHelp(guiGraphics, TEXT_ROUTE_HELP_SELECTED);
        renderButtonTooltip(guiGraphics, mouseX, mouseY, getAddButtonX(), TEXT_ADD_TOOLTIP);
        renderButtonTooltip(guiGraphics, mouseX, mouseY, getEditButtonX(), TEXT_EDIT_TOOLTIP);
        renderButtonTooltip(guiGraphics, mouseX, mouseY, getTransparencyButtonX(), TEXT_TRANSPARENCY_TOOLTIP);
    }

    boolean isUiHovered(double mouseX, double mouseY) {
        return (routeDropDown != null && routeDropDown.isMouseOver(mouseX, mouseY))
                || WorldMapRenderPrimitives.contains(mouseX, mouseY,
                ROUTE_UI_X, ROUTE_UI_Y, HELP_W, ROUTE_BTN_SIZE + ROUTE_BTN_GAP + HELP_H)
                || isOverButton(mouseX, mouseY, getAddButtonX())
                || isOverButton(mouseX, mouseY, getEditButtonX())
                || isOverButton(mouseX, mouseY, getTransparencyButtonX());
    }

    void mouseMoved(double mouseX, double mouseY) {
        if (routeDropDown != null) {
            routeDropDown.onMouseMove(mouseX, mouseY);
        }
    }

    boolean clickDropdown(double mouseX, double mouseY) {
        if (routeDropDown == null || !routeDropDown.isMouseOver(mouseX, mouseY)) {
            return false;
        }
        routeDropDown.onMouseClick(mouseX, mouseY);
        return true;
    }

    boolean isAddButtonHovered(double mouseX, double mouseY) {
        return isOverButton(mouseX, mouseY, getAddButtonX());
    }

    boolean isEditButtonHovered(double mouseX, double mouseY) {
        return isOverButton(mouseX, mouseY, getEditButtonX());
    }

    boolean isTransparencyButtonHovered(double mouseX, double mouseY) {
        return isOverButton(mouseX, mouseY, getTransparencyButtonX());
    }

    private void renderDropdown(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks,
                                @Nullable RecruitsRoute selectedRoute) {
        if (routeDropDown == null) {
            return;
        }

        WorldMapRenderPrimitives.panel(guiGraphics, ROUTE_UI_X, ROUTE_UI_Y, ROUTE_DROPDOWN_W, ROUTE_BTN_SIZE);

        if (routeDropDown.isOpen()) {
            routeDropDown.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
            return;
        }

        String label = selectedRoute != null ? selectedRoute.getName() : TEXT_ROUTE_PLACEHOLDER.getString();
        guiGraphics.drawCenteredString(screen.getScreenFont(), label,
                ROUTE_UI_X + ROUTE_DROPDOWN_W / 2, ROUTE_UI_Y + (ROUTE_BTN_SIZE - 8) / 2, 0xFFFFFF);
    }

    private void renderHelp(GuiGraphics guiGraphics, Component text) {
        int helpY = ROUTE_UI_Y + ROUTE_BTN_SIZE + ROUTE_BTN_GAP;
        WorldMapRenderPrimitives.panel(guiGraphics, ROUTE_UI_X, helpY, HELP_W, HELP_H);
        guiGraphics.drawWordWrap(screen.getScreenFont(), text, ROUTE_UI_X + 6, helpY + 6, HELP_W - 12, 0xFFE6D6A8);
    }

    private void renderButtonTooltip(GuiGraphics guiGraphics, int mouseX, int mouseY, int buttonX, Component text) {
        if (isOverButton(mouseX, mouseY, buttonX)) {
            guiGraphics.renderTooltip(Minecraft.getInstance().font, text, mouseX, mouseY);
        }
    }

    private void renderButton(GuiGraphics guiGraphics, int mouseX, int mouseY, int buttonX,
                              String label, int labelColor, boolean selected) {
        WorldMapRenderPrimitives.button(guiGraphics, screen.getScreenFont(), mouseX, mouseY,
                buttonX, ROUTE_UI_Y, ROUTE_BTN_SIZE, ROUTE_BTN_SIZE, label, labelColor, selected);
    }

    private int getAddButtonX() {
        return ROUTE_UI_X + ROUTE_DROPDOWN_W + ROUTE_BTN_GAP;
    }

    private int getEditButtonX() {
        return getAddButtonX() + ROUTE_BTN_SIZE + ROUTE_BTN_GAP;
    }

    private int getTransparencyButtonX() {
        return getEditButtonX() + ROUTE_BTN_SIZE + ROUTE_BTN_GAP;
    }

    private boolean isOverButton(double mouseX, double mouseY, int buttonX) {
        return WorldMapRenderPrimitives.contains(mouseX, mouseY,
                buttonX, ROUTE_UI_Y, ROUTE_BTN_SIZE, ROUTE_BTN_SIZE);
    }
}
