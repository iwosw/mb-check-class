package com.talhanation.bannermod.client.civilian.gui;

import com.talhanation.bannermod.items.civilian.BuildingPlacementWandItem;
import com.talhanation.bannermod.settlement.prefab.BuildingPrefab;
import com.talhanation.bannermod.settlement.prefab.BuildingPrefabDescriptor;
import com.talhanation.bannermod.settlement.prefab.BuildingPrefabRegistry;
import com.talhanation.bannermod.util.ItemStackComponentData;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;

import java.util.List;

/**
 * Screen used by {@link BuildingPlacementWandItem} to pick which prefab the wand will
 * place on the next block right-click. The chosen prefab id is written to the wand
 * item stack's custom data under {@link BuildingPlacementWandItem#TAG_SELECTED_PREFAB}.
 */
public class PlaceBuildingScreen extends net.minecraft.client.gui.screens.Screen {

    private static final int BUTTON_WIDTH = 200;
    private static final int BUTTON_HEIGHT = 20;
    private static final int BUTTON_SPACING = 2;
    private static final int BUTTONS_PER_COLUMN = 15;
    private static final int TITLE_MARGIN = 30;

    private final ItemStack wandStack;
    private List<BuildingPrefab> prefabs;
    private int page;
    private int pageCount = 1;
    private Button prevPageButton;
    private Button nextPageButton;
    private Button closeButton;

    public PlaceBuildingScreen(ItemStack wandStack) {
        super(Component.translatable("bannermod.prefab.wand.title"));
        this.wandStack = wandStack;
        this.page = 0;
    }

    @Override
    protected void init() {
        super.init();

        // Make sure defaults are registered when the screen opens in a dev world where no
        // server-side preloading has happened yet on the client.
        BuildingPrefabRegistry registry = BuildingPrefabRegistry.instance();
        registry.ensureDefaultsLoaded();
        this.prefabs = registry.all();

        int total = this.prefabs.size();
        this.pageCount = Math.max(1, (int) Math.ceil(total / (double) BUTTONS_PER_COLUMN));
        if (this.page >= this.pageCount) {
            this.page = this.pageCount - 1;
        }

        rebuildPrefabButtons();
    }

    private void rebuildPrefabButtons() {
        this.clearWidgets();

        int centerX = this.width / 2;
        int topY = TITLE_MARGIN + 10;
        int startIndex = this.page * BUTTONS_PER_COLUMN;
        int endIndex = Math.min(startIndex + BUTTONS_PER_COLUMN, this.prefabs.size());

        for (int i = startIndex; i < endIndex; i++) {
            BuildingPrefab prefab = this.prefabs.get(i);
            BuildingPrefabDescriptor descriptor = prefab.descriptor();
            int row = i - startIndex;
            int buttonX = centerX - BUTTON_WIDTH / 2;
            int buttonY = topY + row * (BUTTON_HEIGHT + BUTTON_SPACING);

            Component label = buildButtonLabel(descriptor);
            this.addRenderableWidget(new ExtendedButton(buttonX, buttonY, BUTTON_WIDTH, BUTTON_HEIGHT, label,
                    btn -> onSelect(descriptor)));
        }

        int navY = topY + BUTTONS_PER_COLUMN * (BUTTON_HEIGHT + BUTTON_SPACING) + 8;

        this.prevPageButton = this.addRenderableWidget(new ExtendedButton(centerX - BUTTON_WIDTH / 2, navY, 60, BUTTON_HEIGHT,
                Component.literal("<"),
                btn -> {
                    if (this.page > 0) {
                        this.page--;
                        rebuildPrefabButtons();
                    }
                }));
        this.prevPageButton.active = this.page > 0;
        this.prevPageButton.visible = this.pageCount > 1;

        this.nextPageButton = this.addRenderableWidget(new ExtendedButton(centerX + BUTTON_WIDTH / 2 - 60, navY, 60, BUTTON_HEIGHT,
                Component.literal(">"),
                btn -> {
                    if (this.page < this.pageCount - 1) {
                        this.page++;
                        rebuildPrefabButtons();
                    }
                }));
        this.nextPageButton.active = this.page < this.pageCount - 1;
        this.nextPageButton.visible = this.pageCount > 1;

        this.closeButton = this.addRenderableWidget(new ExtendedButton(centerX - 40, navY + BUTTON_HEIGHT + 4, 80, BUTTON_HEIGHT,
                Component.translatable("gui.cancel"),
                btn -> this.onClose()));
    }

    private static Component buildButtonLabel(BuildingPrefabDescriptor descriptor) {
        String dims = descriptor.width() + "x" + descriptor.height() + "x" + descriptor.depth();
        return Component.translatable(descriptor.displayKey())
                .append(Component.literal("  "))
                .append(Component.literal(dims))
                .append(Component.literal("  "))
                .append(Component.literal(descriptor.profession().name()));
    }

    private void onSelect(BuildingPrefabDescriptor descriptor) {
        String id = descriptor.id().toString();
        ItemStackComponentData.update(this.wandStack, tag -> tag.putString(BuildingPlacementWandItem.TAG_SELECTED_PREFAB, id));

        Minecraft mc = Minecraft.getInstance();
        Component name = Component.translatable(descriptor.displayKey());
        if (mc.player != null) {
            mc.player.sendSystemMessage(Component.translatable("bannermod.prefab.wand.selected", name));
        }
        this.onClose();
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics);

        Component title = Component.translatable("bannermod.prefab.wand.title");
        int titleWidth = this.font.width(title);
        graphics.drawString(this.font, title, (this.width - titleWidth) / 2, 10, 0xFFFFFFFF, true);

        if (this.pageCount > 1) {
            String pageText = (this.page + 1) + " / " + this.pageCount;
            int pageWidth = this.font.width(pageText);
            graphics.drawString(this.font, pageText, (this.width - pageWidth) / 2, 22, 0xFFAAAAAA, false);
        }

        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
