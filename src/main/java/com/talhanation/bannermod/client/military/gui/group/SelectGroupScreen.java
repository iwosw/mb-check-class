package com.talhanation.bannermod.client.military.gui.group;

import com.mojang.blaze3d.systems.RenderSystem;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.client.military.ClientManager;
import com.talhanation.bannermod.client.military.gui.widgets.ListScreenBase;
import com.talhanation.bannermod.client.military.gui.widgets.ListScreenListBase;
import com.talhanation.bannermod.persistence.military.RecruitsGroup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;

import java.util.List;
import java.util.Locale;
import java.util.function.Consumer;

public class SelectGroupScreen extends ListScreenBase implements IGroupSelection {

    protected static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/gui/select_player.png");
    protected static final Component BUTTON_BACK = Component.translatable("gui.recruits.button.back");
    protected static Component BUTTON_TEXT;
    protected static Component TOOLTIP_BUTTON;
    protected static Component TITLE;
    protected static final Component HINT_TEXT = Component.translatable("gui.recruits.list.hint.select_group");
    protected static final Component ACTION_DISABLED_TOOLTIP = Component.translatable("gui.recruits.list.tooltip.action_disabled_group");
    protected static final int HEADER_SIZE = 16;
    protected static final int FOOTER_SIZE = 32;
    protected static final int SEARCH_HEIGHT = 16;
    protected static final int UNIT_SIZE = 18;
    protected static final int CELL_HEIGHT = 36;
    public RecruitsGroupList groupList;
    protected EditBox searchBox;
    protected String lastSearch;
    protected int units;

    protected Screen parent;
    public RecruitsGroup selected;
    public RecruitsGroup groupIn;
    private Button backButton;
    private Button actionButton;
    private final Consumer<RecruitsGroup> buttonAction;
    public SelectGroupScreen(Screen parent, RecruitsGroup groupIn, Component title, Component buttonText, Component buttonTooltip, Consumer<RecruitsGroup> buttonAction){
        super(title,236,0);
        this.parent = parent;
        this.buttonAction = buttonAction;
        this.groupIn = groupIn;
        TITLE = title;
        BUTTON_TEXT = buttonText;
        TOOLTIP_BUTTON = buttonTooltip;
    }

    @Override
    protected void init() {
        super.init();

        ListLayout layout = calculateListLayout(HEADER_SIZE, FOOTER_SIZE, SEARCH_HEIGHT, UNIT_SIZE, CELL_HEIGHT);

        guiLeft = guiLeft + 2;
        guiTop = layout.gapTop;

        units = layout.units;
        ySize = HEADER_SIZE + units * UNIT_SIZE + FOOTER_SIZE;
        int listY = guiTop + HEADER_SIZE + SEARCH_HEIGHT;
        int listHeight = units * UNIT_SIZE - SEARCH_HEIGHT;

        if (groupList != null) {
            groupList.setListBounds(width, listHeight, 0, listY);
        } else {
            groupList = new RecruitsGroupList(width, listHeight, 0, listY, CELL_HEIGHT, this, groupIn == null ? null : List.of(groupIn.getUUID()));
        }
        String string = searchBox != null ? searchBox.getValue() : "";
        searchBox = createSearchBox(string, guiLeft + 8, guiTop + HEADER_SIZE, 220, SEARCH_HEIGHT, this::checkSearchStringUpdate);
        addWidget(searchBox);
        addWidget(groupList);

        this.setInitialFocus(searchBox);

        int buttonY = guiTop + HEADER_SIZE + 5 + units * UNIT_SIZE;

        backButton = new ExtendedButton(guiLeft + 129, buttonY, 100, 20, BUTTON_BACK,
                button -> {
                    minecraft.setScreen(parent);
         });

        actionButton = new ExtendedButton(guiLeft + 7, buttonY, 100, 20, BUTTON_TEXT,
                button -> {
                buttonAction.accept(selected);

                this.groupList.setFocused(null);
                this.selected = null;
                this.init();
        });
        actionButton.active = false;
        actionButton.setTooltip(net.minecraft.client.gui.components.Tooltip.create(ACTION_DISABLED_TOOLTIP));

        addRenderableWidget(backButton);
        addRenderableWidget(actionButton);
    }

    @Override
    public void tick() {
        super.tick();
        ClientManager.updateGroups();
        if(searchBox != null){
        }

        if(groupList != null){
            groupList.tick();
        }

    }

    @Override
    public boolean keyPressed(int p_96552_, int p_96553_, int p_96554_) {
        boolean flag = super.keyPressed(p_96552_, p_96553_, p_96554_);
        this.selected = null;
        this.groupList.setFocused(null);
        this.actionButton.active = false;

        return flag;
    }

    @Override
    public void onClose() {
        super.onClose();
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        RenderSystem.setShaderTexture(0, TEXTURE);
        renderListPanel(guiGraphics, TEXTURE, HEADER_SIZE, UNIT_SIZE, FOOTER_SIZE, units);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        guiGraphics.drawString(font, this.getTitle(), width / 2 - font.width(TITLE) / 2, guiTop + 5, 4210752, false);
        renderSearchableList(guiGraphics, groupList, searchBox, mouseX, mouseY, delta, HEADER_SIZE, UNIT_SIZE, units);

        Component status = selected == null
                ? HINT_TEXT
                : Component.translatable("gui.recruits.list.status.group_selected", selected.getName());
        guiGraphics.drawString(font, status, guiLeft + 8, guiTop + HEADER_SIZE + units * UNIT_SIZE + 9, 0x5B4A32, false);
    }

    private void checkSearchStringUpdate(String string) {
        if (!(string = string.toLowerCase(Locale.ROOT)).equals(lastSearch)) {
            groupList.setFilter(string);
            lastSearch = string;
        }
    }

    @Override
    public boolean mouseClicked(double x, double y, int z) {
        if(groupList != null) groupList.mouseClicked(x,y,z);
        boolean flag = super.mouseClicked(x, y, z);
        RecruitsGroupEntry entry = groupList.getGroupEntryAtPosition(x, y);
        this.selected = entry == null ? null : entry.getGroup();
        this.actionButton.active = selected != null && (groupIn == null || !selected.getUUID().equals(groupIn.getUUID()));
        this.actionButton.setTooltip(this.actionButton.active ? net.minecraft.client.gui.components.Tooltip.create(TOOLTIP_BUTTON) : net.minecraft.client.gui.components.Tooltip.create(ACTION_DISABLED_TOOLTIP));

        return flag;
    }

    @Override
    public Component getTitle() {
        return title;
    }
    @Override
    public RecruitsGroup getSelected() {
        return selected;
    }
    @Override
    public ListScreenListBase<RecruitsGroupEntry> getGroupList() {
        return groupList;
    }
}
