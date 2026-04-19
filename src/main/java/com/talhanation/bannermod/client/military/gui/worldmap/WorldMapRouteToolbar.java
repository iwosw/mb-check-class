package com.talhanation.bannermod.client.military.gui.worldmap;

import com.talhanation.bannermod.client.military.gui.widgets.DropDownMenu;
import com.talhanation.bannermod.persistence.military.RecruitsRoute;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nullable;
import java.util.List;
import java.util.function.Consumer;

final class WorldMapRouteToolbar {
    private static final int ROUTE_UI_X = 10;
    private static final int ROUTE_UI_Y = 10;
    private static final int ROUTE_DROPDOWN_W = 140;
    private static final int ROUTE_BTN_SIZE = 20;
    private static final int ROUTE_BTN_GAP = 3;

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
                route -> route == null ? "-- Route --" : route.getName(),
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
            return;
        }

        renderButton(guiGraphics, mouseX, mouseY, getEditButtonX(), "\u2699", 0xFFFFFF, false);
        renderButton(guiGraphics, mouseX, mouseY, getTransparencyButtonX(), "\u25A1",
                claimTransparency ? 0xFFFFAA00 : 0xFFFFFF, claimTransparency);
    }

    boolean isUiHovered(double mouseX, double mouseY) {
        return routeDropDown != null && routeDropDown.isMouseOver(mouseX, mouseY);
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

        guiGraphics.fill(ROUTE_UI_X, ROUTE_UI_Y, ROUTE_UI_X + ROUTE_DROPDOWN_W, ROUTE_UI_Y + ROUTE_BTN_SIZE, 0x80222222);
        guiGraphics.renderOutline(ROUTE_UI_X, ROUTE_UI_Y, ROUTE_DROPDOWN_W, ROUTE_BTN_SIZE, 0x40FFFFFF);

        if (routeDropDown.isOpen()) {
            routeDropDown.renderWidget(guiGraphics, mouseX, mouseY, partialTicks);
            return;
        }

        String label = selectedRoute != null ? selectedRoute.getName() : "--- Routes ---";
        guiGraphics.drawCenteredString(screen.getScreenFont(), label,
                ROUTE_UI_X + ROUTE_DROPDOWN_W / 2, ROUTE_UI_Y + (ROUTE_BTN_SIZE - 8) / 2, 0xFFFFFF);
    }

    private void renderButton(GuiGraphics guiGraphics, int mouseX, int mouseY, int buttonX,
                              String label, int labelColor, boolean selected) {
        boolean hovered = isOverButton(mouseX, mouseY, buttonX);
        int bg = selected ? 0x80555555 : (hovered ? 0x80444444 : 0x80222222);
        guiGraphics.fill(buttonX, ROUTE_UI_Y, buttonX + ROUTE_BTN_SIZE, ROUTE_UI_Y + ROUTE_BTN_SIZE, bg);
        guiGraphics.renderOutline(buttonX, ROUTE_UI_Y, ROUTE_BTN_SIZE, ROUTE_BTN_SIZE, 0x40FFFFFF);
        guiGraphics.drawCenteredString(screen.getScreenFont(), label, buttonX + ROUTE_BTN_SIZE / 2, ROUTE_UI_Y + 6, labelColor);
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
        return mouseX >= buttonX && mouseX <= buttonX + ROUTE_BTN_SIZE
                && mouseY >= ROUTE_UI_Y && mouseY <= ROUTE_UI_Y + ROUTE_BTN_SIZE;
    }
}
