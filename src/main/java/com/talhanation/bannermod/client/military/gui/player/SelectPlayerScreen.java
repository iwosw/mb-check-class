package com.talhanation.bannermod.client.military.gui.player;

import com.mojang.blaze3d.systems.RenderSystem;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.client.military.ClientManager;
import com.talhanation.bannermod.client.military.gui.widgets.ListScreenBase;
import com.talhanation.bannermod.client.military.gui.widgets.ListScreenListBase;
import com.talhanation.bannermod.persistence.military.RecruitsPlayerInfo;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;

import java.util.Locale;
import java.util.function.Consumer;

public class SelectPlayerScreen extends ListScreenBase implements IPlayerSelection{

    protected static final ResourceLocation TEXTURE = ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/gui/select_player.png");
    public static final Component TITLE = Component.translatable("gui.recruits.select_player_screen.title");
    public static final Component BUTTON_SELECT = Component.translatable("gui.recruits.select_player_screen.selectPlayer");
    public static final Component BUTTON_SELECT_TOOLTIP = Component.translatable("gui.recruits.select_player_screen.selectPlayerTooltip");
    protected static final Component BUTTON_BACK = Component.translatable("gui.recruits.button.back");
    protected static Component BUTTON_TEXT;
    protected static Component TOOLTIP_BUTTON;
    protected static final int HEADER_SIZE = 16;
    protected static final int FOOTER_SIZE = 32;
    protected static final int SEARCH_HEIGHT = 16;
    protected static final int UNIT_SIZE = 18;
    protected static final int CELL_HEIGHT = 36;
    public PlayersList playerList;
    protected EditBox searchBox;
    protected String lastSearch;
    protected int units;

    protected Screen parent;
    public RecruitsPlayerInfo selected;
    private Button backButton;
    private Button actionButton;
    private final Consumer<RecruitsPlayerInfo> buttonAction;
    private final Player player;
    private final boolean includeSelf;
    private final PlayersList.FilterType filterType;

    public SelectPlayerScreen(Screen parent, Player player, Component title, Component buttonText, Component buttonTooltip, boolean includeSelf, PlayersList.FilterType filterType, Consumer<RecruitsPlayerInfo> buttonAction){
        super(title,236,0);
        this.parent = parent;
        this.buttonAction = buttonAction;
        this.player = player;
        this.includeSelf = includeSelf;
        this.filterType = filterType;
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

        if (playerList != null) {
            playerList.setListBounds(width, listHeight, 0, listY);
        } else {
            playerList = new PlayersList(width, listHeight, 0, listY, CELL_HEIGHT, this, filterType, player, includeSelf);
        }
        String string = searchBox != null ? searchBox.getValue() : "";
        searchBox = createSearchBox(string, guiLeft + 8, guiTop + HEADER_SIZE, 220, SEARCH_HEIGHT, this::checkSearchStringUpdate);
        addWidget(searchBox);
        addWidget(playerList);

        this.setInitialFocus(searchBox);

        int buttonY = guiTop + HEADER_SIZE + 5 + units * UNIT_SIZE;

        backButton = new ExtendedButton(guiLeft + 129, buttonY, 100, 20, BUTTON_BACK,
                button -> {
                    minecraft.setScreen(parent);
         });

        actionButton = new ExtendedButton(guiLeft + 7, buttonY, 100, 20, BUTTON_TEXT,
                button -> {
                buttonAction.accept(selected);

                this.playerList.setFocused(null);
                this.selected = null;
                this.init();
        });
        actionButton.active = false;

        addRenderableWidget(backButton);
        addRenderableWidget(actionButton);
    }

    @Override
    public void tick() {
        super.tick();
        if(searchBox != null){
        }

        if(playerList != null){
            playerList.tick();
        }
    }

    @Override
    public boolean keyPressed(int p_96552_, int p_96553_, int p_96554_) {
        boolean flag = super.keyPressed(p_96552_, p_96553_, p_96554_);
        this.selected = null;
        this.playerList.setFocused(null);
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
        guiGraphics.drawString(font, this.getTitle(), width / 2 - font.width(this.getTitle()) / 2, guiTop + 5, 4210752, false);

        renderSearchableList(guiGraphics, playerList, searchBox, mouseX, mouseY, delta, HEADER_SIZE, UNIT_SIZE, units);
    }

    private void checkSearchStringUpdate(String string) {
        if (!(string = string.toLowerCase(Locale.ROOT)).equals(lastSearch)) {
            playerList.setFilter(string);
            lastSearch = string;
        }
    }

    @Override
    public boolean mouseClicked(double x, double y, int z) {
        if(playerList != null) playerList.mouseClicked(x,y,z);
        boolean flag = super.mouseClicked(x, y, z);
        RecruitsPlayerEntry entry = playerList.getPlayerEntryAtPosition(x, y);
        this.selected = entry == null ? null : entry.getPlayerInfo();
        this.actionButton.active = selected != null;

        return flag;
    }

    @Override
    public Component getTitle() {
        return title;
    }
    @Override
    public RecruitsPlayerInfo getSelected() {
        return selected;
    }
    @Override
    public ListScreenListBase<RecruitsPlayerEntry> getPlayerList() {
        return playerList;
    }
}
