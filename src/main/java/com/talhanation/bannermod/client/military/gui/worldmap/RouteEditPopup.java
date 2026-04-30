package com.talhanation.bannermod.client.military.gui.worldmap;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.client.military.gui.player.PlayersList;
import com.talhanation.bannermod.client.military.gui.player.SelectPlayerScreen;
import com.talhanation.bannermod.network.messages.military.MessageTransferRoute;
import com.talhanation.bannermod.persistence.military.RecruitsRoute;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import org.lwjgl.glfw.GLFW;

public class RouteEditPopup {

    private static final int WIDTH = 200;
    private static final int HEIGHT = 150;
    private static final int BG_COLOR = 0x80000000;
    private static final int OUTLINE_COLOR = 0x40FFFFFF;
    private static final int BTN_COLOR = 0x80222222;
    private static final int BTN_HOVERED_COLOR = 0x80444444;
    private static final int BTN_DELETE_COLOR = 0x80330000;
    private static final int BTN_DELETE_HOVERED = 0x80660000;
    private static final int TEXT_COLOR = 0xFFFFFF;
    private static final int BTN_H = 16;
    private static final int BTN_W_FULL = WIDTH - 16;
    private static final Component TEXT_TITLE = Component.translatable("gui.recruits.map.route.edit.title");
    private static final Component TEXT_HINT = Component.translatable("gui.recruits.map.route.edit.hint");
    private static final Component TEXT_TRANSFER_TITLE = Component.translatable("gui.recruits.map.route.edit.transfer.title");
    private static final Component TEXT_TRANSFER_ACTION = Component.translatable("gui.recruits.map.route.edit.transfer.action");
    private static final Component TEXT_TRANSFER_HINT = Component.translatable("gui.recruits.map.route.edit.transfer.hint");
    private static final Component TEXT_SAVE = Component.translatable("gui.recruits.map.route.edit.save");
    private static final Component TEXT_TRANSFER = Component.translatable("gui.recruits.map.route.edit.transfer");
    private static final Component TEXT_DELETE = Component.translatable("gui.recruits.map.route.edit.delete");
    private static final Component TEXT_BACK = Component.translatable("gui.recruits.map.route.edit.back");

    private final WorldMapScreen parent;
    private final Player player;
    private final WorldMapRouteMutationController routeController;

    private boolean visible = false;
    private RecruitsRoute route;
    private EditBox nameField;

    public RouteEditPopup(WorldMapScreen parent, Player player, WorldMapRouteMutationController routeController) {
        this.parent = parent;
        this.player = player;
        this.routeController = routeController;
    }

    public boolean isVisible() {
        return visible;
    }

    public void open(RecruitsRoute route) {
        this.route = route;

        int px = (parent.width - WIDTH) / 2;
        int py = (parent.height - HEIGHT) / 2;
        int fieldX = px + 8;
        int fieldY = py + 40;
        int fieldW = WIDTH - 16;

        nameField = new EditBox(Minecraft.getInstance().font,
                fieldX, fieldY + 3, fieldW, 8, Component.empty());
        nameField.setMaxLength(32);
        nameField.setValue(route.getName());
        nameField.setFocused(true);
        nameField.setBordered(false);
        nameField.setTextColor(TEXT_COLOR);
        this.visible = true;
    }

    public void close() {
        this.visible = false;
        this.route = null;
        this.nameField = null;
    }

    private void save() {
        if (nameField == null || nameField.getValue().isBlank()) return;

        String trimmed = nameField.getValue().trim();
        if (!trimmed.equals(route.getName())) {
            routeController.renameRoute(route, trimmed);
        }

        close();
    }

    private void openTransfer() {
        Minecraft.getInstance().setScreen(new SelectPlayerScreen(
                parent, player,
                TEXT_TRANSFER_TITLE,
                TEXT_TRANSFER_ACTION,
                TEXT_TRANSFER_HINT,
                false,
                PlayersList.FilterType.NONE,
                (playerInfo) -> {
                    BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageTransferRoute(playerInfo.getUUID(), route));
                    Minecraft.getInstance().setScreen(parent);
                }
        ));
    }

    private void deleteRoute() {
        routeController.deleteRoute(route);
        parent.setSelectedRoute(routeController.getSelectedRoute());
        close();
    }

    public void tick() {
    }

    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        if (!visible) return;

        int px = (parent.width - WIDTH) / 2;
        int py = (parent.height - HEIGHT) / 2;
        int x = px + 8;

        guiGraphics.fill(0, 0, parent.width, parent.height, 0x88000000);
        guiGraphics.fill(px, py, px + WIDTH, py + HEIGHT, BG_COLOR);
        guiGraphics.renderOutline(px, py, WIDTH, HEIGHT, OUTLINE_COLOR);

        guiGraphics.drawCenteredString(Minecraft.getInstance().font, TEXT_TITLE,
                px + WIDTH / 2, py + 6, TEXT_COLOR);
        guiGraphics.drawWordWrap(Minecraft.getInstance().font, TEXT_HINT, x, py + 18, BTN_W_FULL, 0xFFE6D6A8);

        // Name field
        int fieldY = py + 40;
        guiGraphics.fill(x - 1, fieldY - 1, x + BTN_W_FULL + 1, fieldY + 15, 0x80303030);
        guiGraphics.renderOutline(x - 1, fieldY - 1, BTN_W_FULL + 2, 16, OUTLINE_COLOR);
        if (nameField != null) nameField.render(guiGraphics, mouseX, mouseY, 0);

        int y = fieldY + 18;
        renderButton(guiGraphics, mouseX, mouseY, TEXT_SAVE,     x, y, BTN_W_FULL, BTN_H, false); y += BTN_H + 4;
        renderButton(guiGraphics, mouseX, mouseY, TEXT_TRANSFER, x, y, BTN_W_FULL, BTN_H, false); y += BTN_H + 4;
        renderButton(guiGraphics, mouseX, mouseY, TEXT_DELETE,   x, y, BTN_W_FULL, BTN_H, true);  y += BTN_H + 4;
        renderButton(guiGraphics, mouseX, mouseY, TEXT_BACK,     x, y, BTN_W_FULL, BTN_H, false);
    }

    private void renderButton(GuiGraphics guiGraphics, int mouseX, int mouseY,
                              Component label, int x, int y, int w, int h, boolean red) {
        boolean hovered = mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
        int bg = red ? (hovered ? BTN_DELETE_HOVERED : BTN_DELETE_COLOR)
                     : (hovered ? BTN_HOVERED_COLOR  : BTN_COLOR);
        guiGraphics.fill(x, y, x + w, y + h, bg);
        guiGraphics.renderOutline(x, y, w, h, OUTLINE_COLOR);
        guiGraphics.drawCenteredString(Minecraft.getInstance().font, label,
                x + w / 2, y + (h - 8) / 2, TEXT_COLOR);
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

        // Forward to EditBox for cursor repositioning
        if (nameField != null) nameField.mouseClicked(mouseX, mouseY, 0);

        int x = px + 8;
        int y = py + 40 + 18; // after hint and name field

        if (isOver(mouseX, mouseY, x, y, BTN_W_FULL, BTN_H)) { save();         return true; } y += BTN_H + 4;
        if (isOver(mouseX, mouseY, x, y, BTN_W_FULL, BTN_H)) { openTransfer(); return true; } y += BTN_H + 4;
        if (isOver(mouseX, mouseY, x, y, BTN_W_FULL, BTN_H)) { deleteRoute();  return true; } y += BTN_H + 4;
        if (isOver(mouseX, mouseY, x, y, BTN_W_FULL, BTN_H)) { close();        return true; }

        return true;
    }

    private boolean isOver(double mouseX, double mouseY, int x, int y, int w, int h) {
        return mouseX >= x && mouseX <= x + w && mouseY >= y && mouseY <= y + h;
    }

    public boolean keyPressed(int keyCode) {
        if (!visible) return false;

        if (keyCode == GLFW.GLFW_KEY_ENTER || keyCode == GLFW.GLFW_KEY_KP_ENTER) {
            save();
            return true;
        }
        if (keyCode == GLFW.GLFW_KEY_ESCAPE) {
            close();
            return true;
        }

        if (nameField != null) nameField.keyPressed(keyCode, 0, 0);
        return true;
    }

    public boolean charTyped(char chr, int modifiers) {
        if (!visible) return false;
        if (nameField != null) nameField.charTyped(chr, modifiers);
        return true;
    }
}
