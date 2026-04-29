package com.talhanation.bannermod.client.military.gui.worldmap;

import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import org.lwjgl.glfw.GLFW;

public class RouteNamePopup {

    private static final int WIDTH = 200;
    private static final int HEIGHT = 64;
    private static final int TEXT_COLOR = 0xFFFFFF;

    private final WorldMapScreen parent;
    private final WorldMapRouteMutationController routeController;

    private boolean visible = false;
    private EditBox nameField;

    public RouteNamePopup(WorldMapScreen parent, WorldMapRouteMutationController routeController) {
        this.parent = parent;
        this.routeController = routeController;
    }

    public boolean isVisible() {
        return visible;
    }

    public void open() {
        int px = (parent.width - WIDTH) / 2;
        int py = (parent.height - HEIGHT) / 2;
        int fieldX = px + 8;
        int fieldY = py + 20;
        int fieldW = WIDTH - 16;

        nameField = new EditBox(Minecraft.getInstance().font,
                fieldX, fieldY + 3, fieldW, 8, Component.empty());
        nameField.setMaxLength(32);
        nameField.setValue("");
        nameField.setFocused(true);
        nameField.setBordered(false);
        nameField.setTextColor(TEXT_COLOR);
        this.visible = true;
    }

    public void close() {
        this.visible = false;
        this.nameField = null;
    }

    private void confirm() {
        if (nameField == null || nameField.getValue().isBlank()) {
            close();
            return;
        }

        routeController.createRoute(nameField.getValue().trim());
        parent.setSelectedRoute(routeController.getSelectedRoute());
        close();
    }

    public void tick() {
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!visible) return;

        int px = (parent.width - WIDTH) / 2;
        int py = (parent.height - HEIGHT) / 2;

        guiGraphics.fill(0, 0, parent.width, parent.height, 0x88000000);
        WorldMapRenderPrimitives.panel(guiGraphics, px, py, WIDTH, HEIGHT);

        guiGraphics.drawCenteredString(Minecraft.getInstance().font, "Route Name:",
                px + WIDTH / 2, py + 6, TEXT_COLOR);

        int fieldX = px + 8;
        int fieldY = py + 20;
        int fieldW = WIDTH - 16;
        guiGraphics.fill(fieldX - 1, fieldY - 1, fieldX + fieldW + 1, fieldY + 15, 0x80303030);
        guiGraphics.renderOutline(fieldX - 1, fieldY - 1, fieldW + 2, 16, 0xAA8A6A3A);

        if (nameField != null) nameField.render(guiGraphics, mouseX, mouseY, 0);

        int btnY = py + HEIGHT - 18;
        renderButton(guiGraphics, mouseX, mouseY, "OK",     px + 8,          btnY, 80, 14);
        renderButton(guiGraphics, mouseX, mouseY, "Cancel", px + WIDTH - 88, btnY, 80, 14);
    }

    private void renderButton(GuiGraphics guiGraphics, int mouseX, int mouseY,
                               String label, int x, int y, int w, int h) {
        WorldMapRenderPrimitives.button(guiGraphics, Minecraft.getInstance().font, mouseX, mouseY,
                x, y, w, h, label, TEXT_COLOR, false);
    }

    public boolean mouseClicked(double mouseX, double mouseY) {
        if (!visible) return false;

        int px = (parent.width - WIDTH) / 2;
        int py = (parent.height - HEIGHT) / 2;

        // Click outside — close
        if (mouseX < px || mouseX > px + WIDTH || mouseY < py || mouseY > py + HEIGHT) {
            close();
            return true;
        }

        // Forward to EditBox so cursor repositions on click
        if (nameField != null) nameField.mouseClicked(mouseX, mouseY, 0);

        int btnY = py + HEIGHT - 18;

        if (WorldMapRenderPrimitives.contains(mouseX, mouseY, px + 8, btnY, 80, 14)) {
            confirm();
            return true;
        }
        if (WorldMapRenderPrimitives.contains(mouseX, mouseY, px + WIDTH - 88, btnY, 80, 14)) {
            close();
            return true;
        }

        return true;
    }

    public boolean keyPressed(int keyCode) {
        if (!visible) return false;

        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            confirm();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }

        // Forward all other keys to EditBox (arrows, home, end, backspace, delete, ctrl+a, etc.)
        if (nameField != null) nameField.keyPressed(keyCode, 0, 0);
        return true;
    }

    public boolean charTyped(char chr, int modifiers) {
        if (!visible) return false;
        if (nameField != null) nameField.charTyped(chr, modifiers);
        return true;
    }
}
