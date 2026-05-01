package com.talhanation.bannermod.client.military.gui.group;

import com.mojang.blaze3d.systems.RenderSystem;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.client.military.ClientManager;
import com.talhanation.bannermod.client.military.gui.widgets.ListScreenBase;
import com.talhanation.bannermod.client.military.gui.widgets.ListScreenListBase;
import com.talhanation.bannermod.network.messages.military.MessageApplyNoGroup;
import com.talhanation.bannermod.network.messages.military.MessageUpdateGroup;
import com.talhanation.bannermod.persistence.military.RecruitsGroup;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;

import java.util.Locale;

@OnlyIn(Dist.CLIENT)
public class RecruitsGroupListScreen extends ListScreenBase implements IGroupSelection {

    protected static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/gui/select_player.png");
    protected static final Component TITLE = Component.translatable("gui.recruits.groups.title");
    protected static final Component ADD_BUTTON = Component.translatable("gui.recruits.groups.add");
    protected static final Component EDIT_BUTTON = Component.translatable("gui.recruits.groups.edit");
    protected static final Component REMOVE_BUTTON = Component.translatable("gui.recruits.groups.remove");
    protected static final Component HINT_TEXT = Component.translatable("gui.recruits.list.hint.manage_groups");
    protected static final Component EDIT_DISABLED_TOOLTIP = Component.translatable("gui.recruits.list.tooltip.edit_disabled");
    protected static final Component REMOVE_DISABLED_TOOLTIP = Component.translatable("gui.recruits.list.tooltip.remove_disabled");
    protected static final Component ADD_DISABLED_TOOLTIP = Component.translatable("gui.recruits.list.tooltip.add_disabled");
    protected static final int HEADER_SIZE = 16;
    protected static final int FOOTER_SIZE = 32;
    protected static final int SEARCH_HEIGHT = 16;
    protected static final int UNIT_SIZE = 18;
    protected static final int CELL_HEIGHT = 36;

    protected RecruitsGroupList groupList;
    protected EditBox searchBox;
    protected String lastSearch;
    protected int units;

    protected Screen parent;
    private RecruitsGroup selected;
    private Button editButton;
    private Button removeButton;
    private Button addButton;
    private final Player player;

    public RecruitsGroupListScreen(Player player){
        super(TITLE,236,0);
        this.player = player;
    }

    @Override
    protected void init() {
        super.init();
        clearWidgets();
        ClientManager.updateGroups(true);

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
            groupList = new RecruitsGroupList(width, listHeight, 0, listY, CELL_HEIGHT, this, null);
        }
        String string = searchBox != null ? searchBox.getValue() : "";
        searchBox = createSearchBox(string, guiLeft + 8, guiTop + HEADER_SIZE, 220, SEARCH_HEIGHT, this::checkSearchStringUpdate);
        addWidget(searchBox);
        addWidget(groupList);

        int buttonY = guiTop + HEADER_SIZE + 5 + units * UNIT_SIZE;

        this.addButton = createAddGroupButton(guiLeft + 7, buttonY);
        addRenderableWidget(this.addButton);

        this.editButton =  createEditGroupButton(guiLeft + 87, buttonY);
        addRenderableWidget(this.editButton);

        this.removeButton = createRemoveGroupButton(guiLeft + 167, buttonY);
        addRenderableWidget(this.removeButton);

        checkButtons();
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

    private Button createRemoveGroupButton(int x, int y) {
        return new ExtendedButton(x, y, 60, 20, REMOVE_BUTTON, button -> {
            if (selected != null) {

                ClientManager.groups.removeIf(predicate -> selected.getUUID().equals(predicate.getUUID()));
                ClientManager.markGroupsChanged();

                selected.removed = true;
                BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageApplyNoGroup(player.getUUID(), selected.getUUID()));
                BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageUpdateGroup(selected));
                this.selected = null;

                this.init();
            }
        });
    }

    public void checkButtons(){
        this.editButton.active = selected != null;
        this.removeButton.active = selected != null;
        this.addButton.active = selected == null;
        this.editButton.setTooltip(selected == null ? net.minecraft.client.gui.components.Tooltip.create(EDIT_DISABLED_TOOLTIP) : null);
        this.removeButton.setTooltip(selected == null ? net.minecraft.client.gui.components.Tooltip.create(REMOVE_DISABLED_TOOLTIP) : null);
        this.addButton.setTooltip(selected != null ? net.minecraft.client.gui.components.Tooltip.create(ADD_DISABLED_TOOLTIP) : null);
    }

    private Button createAddGroupButton(int x, int y) {
        return new ExtendedButton(x, y, 60, 20, ADD_BUTTON, btn -> {
            this.minecraft.setScreen(new EditOrAddGroupScreen(this));
        });
    }

    private Button createEditGroupButton(int x, int y) {
        return new ExtendedButton(x, y, 60, 20, EDIT_BUTTON, btn -> {
            if(selected != null) this.minecraft.setScreen(new EditOrAddGroupScreen(this, selected));
        });
    }

    @Override
    public boolean keyPressed(int p_96552_, int p_96553_, int p_96554_) {
        boolean flag = super.keyPressed(p_96552_, p_96553_, p_96554_);
        this.selected = null;
        this.groupList.setFocused(null);
        this.checkButtons();

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
    private long lastClickTime = 0;
    private static final long DOUBLE_CLICK_THRESHOLD = 200;
    @Override
    public boolean mouseClicked(double x, double y, int button) {
        if (groupList != null && groupList.isMouseOver(x,y)) {
            groupList.mouseClicked(x, y, button);

            RecruitsGroupEntry entry = groupList.getGroupEntryAtPosition(x,y);
            if(entry != null){
                selected = entry.getGroup();
            }
            else selected = null;

            boolean isDoubleClick = false;
            long now = System.currentTimeMillis();

            if (button == 0) {
                if (now - lastClickTime <= DOUBLE_CLICK_THRESHOLD) {
                    isDoubleClick = true;
                }
                lastClickTime = now;
            }

            if (isDoubleClick && this.selected != null) {
                onDoubleClick(this.selected);
            }
        }
        this.checkButtons();

        return super.mouseClicked(x, y, button);
    }

    private void onDoubleClick(RecruitsGroup group) {
        this.minecraft.setScreen(new EditOrAddGroupScreen(this, group));
    }

    public RecruitsGroup getSelected(){
        return this.selected;
    }

    @Override
    public ListScreenListBase<RecruitsGroupEntry> getGroupList() {
        return this.groupList;
    }

    @Override
    public Component getTitle() {
        return TITLE;
    }

}
