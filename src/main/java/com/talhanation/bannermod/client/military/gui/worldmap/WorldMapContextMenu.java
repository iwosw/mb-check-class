package com.talhanation.bannermod.client.military.gui.worldmap;

import com.talhanation.bannermod.army.map.FormationMapContact;
import com.talhanation.bannermod.client.military.ClientManager;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.function.BooleanSupplier;
import java.util.function.Consumer;

public class WorldMapContextMenu {

    private final List<ContextMenuEntry> entries = new ArrayList<>();
    private int x, y;
    private boolean visible = false;
    private final int width = 150;
    private final int entryHeight = 20;
    private final WorldMapScreen worldMapScreen;

    /** Snapshot of mouse position taken when the menu was opened. Used for waypoint hit-detection. */
    private double snapshotMouseX;
    private double snapshotMouseY;

    public WorldMapContextMenu(WorldMapScreen worldMapScreen) {
        this(worldMapScreen, null, null);
    }

    public WorldMapContextMenu(WorldMapScreen worldMapScreen,
                               @Nullable FormationMapContact ownSelection,
                               @Nullable FormationMapContact clickedContact) {
        this.worldMapScreen = worldMapScreen;
        ItemStack currencyItemStack = ClientManager.getCurrencyItemStackOrDefault();
        ItemStack itemStackClaimChunk = new ItemStack(currencyItemStack.getItem());
        itemStackClaimChunk.setCount(ClientManager.configValueChunkCost);
        ItemStack itemStackClaimArea = new ItemStack(currencyItemStack.getItem());
        itemStackClaimArea.setCount(worldMapScreen.getClaimCost());
        WorldMapClaimMenuActions claimMenuActions = new WorldMapClaimMenuActions(worldMapScreen);
        WorldMapGeneralMenuActions generalMenuActions = new WorldMapGeneralMenuActions(worldMapScreen);

        WorldMapFormationMenuActions.addEntries(this, worldMapScreen, ownSelection, clickedContact);
        claimMenuActions.addEntries(this, itemStackClaimChunk, itemStackClaimArea);
        generalMenuActions.addEntries(this);
    }

    public void addEntry(String text, BooleanSupplier condition, Consumer<WorldMapScreen> action, ItemStack itemStack, String tag) {
        entries.add(new ContextMenuEntry(Component.literal(text), condition, action, itemStack, tag));
    }
    public void addEntry(String text, BooleanSupplier condition, Consumer<WorldMapScreen> action, String tag) {
        entries.add(new ContextMenuEntry(Component.literal(text), condition, action, ItemStack.EMPTY, tag));
    }
    public void addEntry(String text, BooleanSupplier condition, Consumer<WorldMapScreen> action) {
        entries.add(new ContextMenuEntry(Component.literal(text), condition, action, ItemStack.EMPTY, ""));
    }
    public void addEntry(String text, Consumer<WorldMapScreen> action) {
        addEntry(text, () -> true, action);
    }

    public void openAt(int x, int y) {
        this.x = Math.max(10, Math.min(x, worldMapScreen.width - width - 10));
        this.y = Math.max(10, Math.min(y, worldMapScreen.height - entries.size() * entryHeight - 10));
        this.visible = true;
        // Snapshot mouse position at open time
        this.snapshotMouseX = worldMapScreen.mouseX;
        this.snapshotMouseY = worldMapScreen.mouseY;
    }

    public void close() { this.visible = false; }
    public boolean isVisible() { return visible; }
    double getSnapshotMouseX() { return snapshotMouseX; }
    double getSnapshotMouseY() { return snapshotMouseY; }

    private String hoveredEntryTag = null;
    public String getHoveredEntryTag() { return hoveredEntryTag; }

    public void render(GuiGraphics guiGraphics, WorldMapScreen screen) {
        if (!visible) return;

        hoveredEntryTag = null;

        int visibleEntries = (int) entries.stream().filter(e -> e.shouldShow(screen)).count();
        int height = visibleEntries * entryHeight;

        guiGraphics.fill(x, y, x + width, y + height, 0xFF1A1A1A);
        guiGraphics.renderOutline(x, y, width, height, 0xFF555555);

        int entryY = y;
        for (ContextMenuEntry entry : entries) {
            if (entry.shouldShow(screen)) {
                boolean hovered = isMouseOverEntry(x, entryY);
                if (hovered) hoveredEntryTag = entry.getTag();

                guiGraphics.fill(x, entryY, x + width, entryY + entryHeight, hovered ? 0xFF333333 : 0xFF1A1A1A);

                int textColor;
                if (entry.getTag().equals("admin")) {
                    textColor = hovered ? 0xFFFF5555 : 0xFFAA4444;
                } else {
                    textColor = hovered ? 0xFFFFFF : 0xCCCCCC;
                }

                guiGraphics.drawString(screen.getMinecraft().font, entry.text(), x + 8, entryY + 6, textColor);

                if (!entry.stack.isEmpty()) {
                    guiGraphics.renderFakeItem(entry.stack, x + width - 20, entryY + 1);
                    guiGraphics.renderItemDecorations(screen.getMinecraft().font, entry.stack, x + width - 20, entryY + 1);
                }

                entryY += entryHeight;
            }
        }
    }

    public boolean mouseClicked(double mouseX, double mouseY, int button, WorldMapScreen screen) {
        if (!visible || button != 0) return false;

        int entryY = y;
        for (ContextMenuEntry entry : entries) {
            if (entry.shouldShow(screen)) {
                if (mouseX >= x && mouseX <= x + width && mouseY >= entryY && mouseY <= entryY + entryHeight) {
                    entry.execute(screen);
                    close();
                    return true;
                }
                entryY += entryHeight;
            }
        }

        close();
        return false;
    }

    private boolean isMouseOverEntry(int entryX, int entryY) {
        double mouseX = this.worldMapScreen.mouseX;
        double mouseY = this.worldMapScreen.mouseY;
        return mouseX >= entryX && mouseX <= entryX + width && mouseY >= entryY && mouseY <= entryY + entryHeight;
    }

    private record ContextMenuEntry(Component text, BooleanSupplier condition, Consumer<WorldMapScreen> action, ItemStack stack, String tag) {
        public String getTag() { return tag; }
        public boolean shouldShow(WorldMapScreen screen) { return condition.getAsBoolean(); }
        public void execute(WorldMapScreen screen) { action.accept(screen); }
    }
}
