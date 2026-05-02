package com.talhanation.bannermod.client.civilian.gui;

import com.mojang.blaze3d.systems.RenderSystem;
import com.talhanation.bannermod.client.military.gui.MilitaryGuiStyle;
import com.talhanation.bannermod.client.military.gui.widgets.ContextMenuEntry;
import com.talhanation.bannermod.client.military.gui.widgets.DropDownMenu;
import com.talhanation.bannermod.client.military.gui.widgets.GuiWidgetBounds;
import com.talhanation.bannermod.client.military.gui.widgets.ListScreenEntryBase;
import com.talhanation.bannermod.client.military.gui.widgets.ListScreenListBase;
import com.talhanation.bannermod.client.military.gui.widgets.RecruitsCheckBox;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.civilian.MerchantEntity;
import com.talhanation.bannermod.inventory.civilian.MerchantTradeContainer;
import com.talhanation.bannermod.network.messages.civilian.MessageDoTradeWithMerchant;
import com.talhanation.bannermod.network.messages.civilian.MessageMoveMerchantTrade;
import com.talhanation.bannermod.network.messages.civilian.MessageUpdateMerchant;
import com.talhanation.bannermod.network.messages.civilian.MessageUpdateMerchantTrade;
import com.talhanation.bannermod.persistence.civilian.WorkersMerchantTrade;
import de.maxhenkel.corelib.inventory.ScreenBase;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class MerchantTradeScreen extends ScreenBase<MerchantTradeContainer> {

    private static final ResourceLocation RESOURCE_LOCATION = ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/gui/merchant.png" );
    private static final ResourceLocation ARROW_IMAGE = ResourceLocation.fromNamespaceAndPath(BannerModMain.MOD_ID, "textures/gui/arrow.png");
    private static final MutableComponent BUTTON_ADD = Component.translatable("gui.workers.button.add");
    private static final MutableComponent BUTTON_EDIT = Component.translatable("gui.workers.button.edit");
    private static final MutableComponent BUTTON_REMOVE = Component.translatable("gui.workers.button.remove");
    private static final MutableComponent BUTTON_COPY = Component.translatable("gui.workers.button.copy");
    private static final MutableComponent BUTTON_TRADE = Component.translatable("gui.workers.button.trade");
    private static final MutableComponent BUTTON_MANAGE = Component.translatable("gui.workers.merchant.button.manage");
    private static final MutableComponent BUTTON_MOVE_UP = Component.translatable("gui.workers.merchant.manage.move_up");
    private static final MutableComponent BUTTON_MOVE_DOWN = Component.translatable("gui.workers.merchant.manage.move_down");
    private static final MutableComponent TEXT_CREATIVE = Component.translatable("gui.workers.text.creative");
    private static final MutableComponent TEXT_DAILY_REFRESH = Component.translatable("gui.workers.text.dailyRefresh");
    private static final Component TITLE_STALL_LEDGER = Component.translatable("gui.workers.merchant.title");
    private static final int fontColor = 4210752;
    private final MerchantEntity merchantEntity;
    private final Player player;
    private final boolean isOwner;
    public WorkersMerchantTrade selection;
    public MerchantTradeContainer tradeContainer;
    private ExtendedButton tradeButton;
    private ExtendedButton addEditTradeButton;
    private RecruitsCheckBox creativeCheckbox;
    private RecruitsCheckBox dailyRefreshCheckbox;
    private DropDownMenu<ContextMenuEntry> manageMenu;
    private boolean isCreative;
    private boolean isDailyRefresh;
    private ItemStack hoveredTooltipStack = ItemStack.EMPTY;
    private int hoveredTooltipX = 0;
    private int hoveredTooltipY = 0;
    private TradeList tradeList;

    private static final int LIST_X = 5;
    private static final int LIST_Y = 18;
    private static final int LIST_W = 85;
    private static final int LIST_H = 170;
    private static final int TRADE_TITLE_X = 98;
    private static final int TRADE_TITLE_Y = 58;

    public MerchantTradeScreen(MerchantTradeContainer tradeContainer, Inventory playerInventory, Component title) {
        super(RESOURCE_LOCATION, tradeContainer, playerInventory, TITLE_STALL_LEDGER);
        this.tradeContainer = tradeContainer;
        this.merchantEntity = tradeContainer.getMerchantEntity();
        this.player = playerInventory.player;
        this.isOwner = player.getUUID().equals(merchantEntity.getOwnerUUID());
        imageWidth = 256;
        imageHeight = 197;
    }

    @Override
    protected void init() {
        super.init();
        this.isCreative = merchantEntity.isCreative();
        this.isDailyRefresh = merchantEntity.isDailyRefresh();
        this.setWidgets();
    }

    @Override
    protected void containerTick() {
        super.containerTick();
        //this.loadTrades();
    }

    public void setWidgets(){
        this.clearWidgets();


        int listLeft = leftPos + LIST_X;
        int listTop = topPos + LIST_Y;
        int listWidth = LIST_W;
        int listHeight = LIST_H;
        int itemHeight = 40;
        int itemWidth = LIST_W - 10;

        this.tradeList = new TradeList(listWidth, listHeight, listLeft, listTop, itemHeight, itemWidth);

        this.loadTrades();

        this.addRenderableWidget(this.tradeList);

        if((merchantEntity.isCreative() && player.isCreative()) || isOwner){
            // Primary actions: Trade + Add/Edit on the top row (60px wide each, 18 tall, ~80px gap for the dropdown).
            tradeButton = new ExtendedButton(leftPos + 88, topPos + 58, 78, 18, BUTTON_TRADE,
                    button -> {
                        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageDoTradeWithMerchant(merchantEntity.getUUID(), selection.uuid));
                        this.selection.currentTrades++;
                        this.updateButtonState();
                    });
            addRenderableWidget(tradeButton);

            addEditTradeButton = new ExtendedButton(leftPos + 168, topPos + 58, 78, 18, Component.empty(),
                    button -> {
                        WorkersMerchantTrade trade = selection == null ? new WorkersMerchantTrade() : selection;
                        merchantEntity.openAddEditTradeGUI(player, trade);
                        tradeList.addEntry(this.tradeList.new TradeEntry(trade));
                        this.selection = null;
                        tradeList.setSelected(null);
                        updateButtonState();
                    });
            addRenderableWidget(addEditTradeButton);

            // Manage dropdown: Copy / Remove / Move up / Move down. Collapses 4 same-tier buttons.
            List<ContextMenuEntry> manageEntries = new ArrayList<>();
            manageEntries.add(new ContextMenuEntry(BUTTON_COPY.getString(), this::onCopySelectedTrade, true));
            manageEntries.add(new ContextMenuEntry(BUTTON_REMOVE.getString(), this::onRemoveSelectedTrade, true));
            manageEntries.add(new ContextMenuEntry(BUTTON_MOVE_UP.getString(), this::onMoveSelectedUp, true));
            manageEntries.add(new ContextMenuEntry(BUTTON_MOVE_DOWN.getString(), this::onMoveSelectedDown, true));
            manageMenu = new DropDownMenu<>(
                    null,
                    leftPos + 88,
                    topPos + 78,
                    158,
                    18,
                    manageEntries,
                    entry -> entry == null ? BUTTON_MANAGE.getString() : entry.label,
                    entry -> {
                        if (entry != null && entry.enabled && entry.action != null) {
                            entry.action.run();
                        }
                    }
            );
            manageMenu.setBgFill(0xCC2A2119);
            manageMenu.setBgFillHovered(0xCC4B3928);
            manageMenu.setBgFillSelected(0xCC1A1310);
            manageMenu.setDisplayColor(MilitaryGuiStyle.TEXT);
            manageMenu.setOptionTextColor(MilitaryGuiStyle.TEXT);
            addRenderableWidget(manageMenu);

            if(player.isCreative()) {
                this.creativeCheckbox = new RecruitsCheckBox(leftPos + 256, topPos + 172, 100, 20, TEXT_CREATIVE,
                        this.isCreative,
                        (bool) -> {
                            this.isCreative = bool;
                            BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageUpdateMerchant(merchantEntity.getUUID(), isCreative, true, isDailyRefresh));
                            setWidgets(); // rebuild so daily-refresh checkbox shows/hides
                        }
                );
                addRenderableWidget(creativeCheckbox);

                if (this.isCreative) {
                    this.dailyRefreshCheckbox = new RecruitsCheckBox(leftPos + 256, topPos + 192, 100, 20, TEXT_DAILY_REFRESH,
                            this.isDailyRefresh,
                            (bool) -> {
                                this.isDailyRefresh = bool;
                                BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageUpdateMerchant(merchantEntity.getUUID(), isCreative, true, isDailyRefresh));
                            }
                    );
                    addRenderableWidget(dailyRefreshCheckbox);
                }
            }
        }
        else{
            if(player.isCreative()) {
                this.creativeCheckbox = new RecruitsCheckBox(leftPos + 256, topPos + 172, 100, 20, TEXT_CREATIVE,
                        this.isCreative,
                        (bool) -> {
                            this.isCreative = bool;
                            BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageUpdateMerchant(merchantEntity.getUUID(), isCreative, true, isDailyRefresh));
                            setWidgets(); // rebuild so daily-refresh checkbox shows/hides
                        }
                );
                addRenderableWidget(creativeCheckbox);

                if (this.isCreative) {
                    this.dailyRefreshCheckbox = new RecruitsCheckBox(leftPos + 256, topPos + 192, 100, 20, TEXT_DAILY_REFRESH,
                            this.isDailyRefresh,
                            (bool) -> {
                                this.isDailyRefresh = bool;
                                BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageUpdateMerchant(merchantEntity.getUUID(), isCreative, true, isDailyRefresh));
                            }
                    );
                    addRenderableWidget(dailyRefreshCheckbox);
                }
            }

            tradeButton = new ExtendedButton(leftPos + 97, topPos + 66, 140, 20, BUTTON_TRADE,
                    button -> {
                        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageDoTradeWithMerchant(merchantEntity.getUUID(), selection.uuid));
                        this.selection.currentTrades++;
                        this.updateButtonState();
                    });
            addRenderableWidget(tradeButton);
        }
        this.updateButtonState();
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (this.manageMenu != null) {
            this.manageMenu.onMouseClick(mouseX, mouseY);
        }

        boolean clicked = super.mouseClicked(mouseX, mouseY, button);

        boolean overAddEdit = this.addEditTradeButton != null && this.addEditTradeButton.isHovered();
        boolean overTrade = this.tradeButton != null && this.tradeButton.isHovered();
        boolean overTradeList = this.tradeList != null && this.tradeList.isMouseOver(mouseX, mouseY);
        boolean overManage = this.manageMenu != null && this.manageMenu.isMouseOver(mouseX, mouseY);

        if (!overAddEdit && !overTrade && !overTradeList && !overManage) {
            this.selection = null;
            if (this.tradeList != null)
                this.tradeList.setSelected(null);
            this.updateButtonState();
        }

        return clicked;
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (this.manageMenu != null) {
            this.manageMenu.onMouseMove(mouseX, mouseY);
        }
        super.mouseMoved(mouseX, mouseY);
    }

    private void onCopySelectedTrade() {
        WorkersMerchantTrade trade = selection == null ? new WorkersMerchantTrade() : selection.copy();
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageUpdateMerchantTrade(this.merchantEntity.getUUID(), trade, false));
        tradeList.addEntry(this.tradeList.new TradeEntry(trade));
        this.selection = null;
        tradeList.setSelected(null);
        updateButtonState();
    }

    private void onRemoveSelectedTrade() {
        if (selection == null) return;
        tradeList.children().removeIf(tradeEntry -> tradeEntry.trade.uuid.equals(selection.uuid));
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageUpdateMerchantTrade(merchantEntity.getUUID(), selection, true));
        this.selection = null;
        tradeList.setSelected(null);
        updateButtonState();
    }

    private void onMoveSelectedUp() {
        if (selection == null) return;
        UUID selectedUuid = selection.uuid;
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageMoveMerchantTrade(merchantEntity.getUUID(), selectedUuid, true));
        List<WorkersMerchantTrade> list = new ArrayList<>(merchantEntity.getTrades());
        for (int i = 1; i < list.size(); i++) {
            if (list.get(i).uuid.equals(selectedUuid)) {
                WorkersMerchantTrade tmp = list.get(i - 1);
                list.set(i - 1, list.get(i));
                list.set(i, tmp);
                break;
            }
        }
        merchantEntity.setTrades(list);
        loadTrades();
        restoreSelection(selectedUuid);
    }

    private void onMoveSelectedDown() {
        if (selection == null) return;
        UUID selectedUuid = selection.uuid;
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageMoveMerchantTrade(merchantEntity.getUUID(), selectedUuid, false));
        List<WorkersMerchantTrade> list = new ArrayList<>(merchantEntity.getTrades());
        for (int i = 0; i < list.size() - 1; i++) {
            if (list.get(i).uuid.equals(selectedUuid)) {
                WorkersMerchantTrade tmp = list.get(i + 1);
                list.set(i + 1, list.get(i));
                list.set(i, tmp);
                break;
            }
        }
        merchantEntity.setTrades(list);
        loadTrades();
        restoreSelection(selectedUuid);
    }

    private void restoreSelection(UUID tradeUuid) {
        for (TradeList.TradeEntry entry : tradeList.children()) {
            if (entry.trade.uuid.equals(tradeUuid)) {
                this.tradeList.setSelected(entry);
                this.selection = entry.trade;
                return;
            }
        }
        this.selection = null;
        tradeList.setSelected(null);
        updateButtonState();
    }

    public void loadTrades(){
        this.tradeList.clearEntries();
        List<WorkersMerchantTrade> trades = merchantEntity.getTrades();
        for(WorkersMerchantTrade merchantTrade : trades){
            if(!merchantTrade.enabled && !isOwner) continue;
            tradeList.addEntry(this.tradeList.new TradeEntry(merchantTrade));
        }
    }

    public void onSelected(TradeList.TradeEntry entry){
        this.selection = entry.trade;
        this.updateButtonState();
    }

    public void updateButtonState(){
        if(addEditTradeButton != null){
            this.addEditTradeButton.setMessage(selection != null ? BUTTON_EDIT : BUTTON_ADD);
        }
        if(manageMenu != null){
            manageMenu.active = selection != null;
        }

        this.tradeButton.active = false;

        if(selection != null){
            boolean playerFreeSlot = player.getInventory().getFreeSlot() != -1;
            boolean withinLimit = selection.maxTrades == -1 || selection.currentTrades < selection.maxTrades;
            this.tradeButton.active = playerFreeSlot && withinLimit && selection.enabled;
        }
    }

    private Component text(String key, Object... args) {
        return Component.translatable(key, args);
    }

    private int drawWrapped(GuiGraphics guiGraphics, Component text, int x, int y, int width, int color) {
        for (var line : this.font.split(text, width)) {
            guiGraphics.drawString(this.font, line, x, y, color, false);
            y += 10;
        }
        return y;
    }

    private Component currentStatusText() {
        if (this.tradeList == null || this.tradeList.children().isEmpty()) {
            return text(this.isOwner
                    ? "gui.workers.merchant.status.no_offers_owner"
                    : "gui.workers.merchant.status.no_offers_customer");
        }
        if (this.selection == null) {
            return text("gui.workers.merchant.status.select_offer");
        }
        if (!this.selection.enabled) {
            return text("gui.workers.merchant.status.closed_offer");
        }
        if (this.selection.maxTrades != -1 && this.selection.currentTrades >= this.selection.maxTrades) {
            return text("gui.workers.merchant.status.sold_out");
        }
        if (this.player.getInventory().getFreeSlot() == -1) {
            return text("gui.workers.merchant.status.inventory_full");
        }
        return text("gui.workers.merchant.status.ready");
    }

    private Component currentHintText() {
        if (this.tradeList == null || this.tradeList.children().isEmpty()) {
            return text(this.isOwner
                    ? "gui.workers.merchant.hint.no_offers_owner"
                    : "gui.workers.merchant.hint.no_offers_customer");
        }
        if (this.selection == null) {
            return text(this.isOwner
                    ? "gui.workers.merchant.hint.select_offer_owner"
                    : "gui.workers.merchant.hint.select_offer_customer");
        }
        if (!this.selection.enabled) {
            return text(this.isOwner
                    ? "gui.workers.merchant.hint.closed_offer_owner"
                    : "gui.workers.merchant.hint.closed_offer_customer");
        }
        if (this.selection.maxTrades != -1 && this.selection.currentTrades >= this.selection.maxTrades) {
            return text(this.isOwner
                    ? "gui.workers.merchant.hint.sold_out_owner"
                    : "gui.workers.merchant.hint.sold_out_customer");
        }
        if (this.player.getInventory().getFreeSlot() == -1) {
            return text("gui.workers.merchant.hint.inventory_full");
        }
        return text(this.isOwner
                ? "gui.workers.merchant.hint.ready_owner"
                : "gui.workers.merchant.hint.ready_customer");
    }

    private int currentStatusColor() {
        if (this.tradeList == null || this.tradeList.children().isEmpty() || this.selection == null) {
            return 0xFFB58D4A;
        }
        if (!this.selection.enabled || (this.selection.maxTrades != -1 && this.selection.currentTrades >= this.selection.maxTrades)
                || this.player.getInventory().getFreeSlot() == -1) {
            return 0xFFB04A3A;
        }
        return 0xFF4C7A43;
    }

    @Override
    public void render(GuiGraphics guiGraphics, int x, int y, float partialTicks) {
        this.hoveredTooltipStack = ItemStack.EMPTY;
        super.render(guiGraphics, x, y, partialTicks);
        if (!this.hoveredTooltipStack.isEmpty()) {
            this.renderItemStackTooltip(guiGraphics, this.hoveredTooltipStack, this.hoveredTooltipX, this.hoveredTooltipY);
        }
    }

    @Override
    protected void renderLabels(GuiGraphics guiGraphics, int mouseX, int mouseY) {
        guiGraphics.drawString(font, title, 8, 5, fontColor, false);

        // Show "MerchantName - MarketName" if merchant is in a market, else just merchant name.
        // The header strip from x=92 to about x=244 gives ~150px of usable space; clamp so long names do not overflow.
        String marketName = merchantEntity.getCurrentMarketName();
        String rawNameLabel = marketName.isEmpty()
                ? merchantEntity.getDisplayName().getString()
                : merchantEntity.getDisplayName().getString() + " - " + marketName;
        guiGraphics.drawString(font, clampWithEllipsis(rawNameLabel, 150), 92, 5, fontColor, false);

        int hintY = drawWrapped(guiGraphics, currentStatusText(), 96, 20, 148, currentStatusColor());
        drawWrapped(guiGraphics, currentHintText(), 96, hintY + 2, 148, 0xFF5D4630);

        guiGraphics.drawString(font, player.getInventory().getDisplayName().getVisualOrderText(), 92, this.imageHeight - 96 + 2, fontColor, false);
    }

    private String clampWithEllipsis(String raw, int maxWidth) {
        if (this.font.width(raw) <= maxWidth) return raw;
        String ellipsis = "…";
        int ellipsisWidth = this.font.width(ellipsis);
        return this.font.plainSubstrByWidth(raw, Math.max(0, maxWidth - ellipsisWidth)) + ellipsis;
    }

    protected void renderItemStackTooltip(GuiGraphics guiGraphics, ItemStack itemstack, int mouseX, int mouseY) {
        guiGraphics.renderTooltip(this.font, this.getTooltipFromContainerItem(itemstack), itemstack.getTooltipImage(), itemstack, mouseX, mouseY);
    }


    int xOffset = 2;
    int yOffset = 2;
    private class TradeList extends ListScreenListBase<TradeList.TradeEntry> {
        public int itemWidth;
        public TradeList(int width, int height, int left, int top, int itemHeight, int itemWidth) {
            super(width, height, left, top, itemHeight);
            this.itemWidth = itemWidth;
        }

        @Override
        protected int addEntry(TradeList.TradeEntry p_93487_) {
            return super.addEntry(p_93487_);
        }

        @Override
        protected void clearEntries() {
            super.clearEntries();
        }

        @Override
        protected int getScrollbarPosition() {
            return this.getRowLeft() + this.getRowWidth() + 5;
        }

        @Override
        public int getRowLeft() {
            return super.getRowLeft() - 7;
        }

        @Override
        public int getRowWidth() {
            return LIST_W - 12;
        }
        public void setSelected(@Nullable TradeList.TradeEntry entry) {
            super.setSelected(entry);
            if(entry == null) return;
            MerchantTradeScreen.this.onSelected(entry);
        }

        public class TradeEntry extends ListScreenEntryBase<TradeEntry> {
            private final WorkersMerchantTrade trade;
            public TradeEntry(WorkersMerchantTrade trade) {
                this.trade = trade;
            }
            @Override
            public void render(GuiGraphics guiGraphics, int index, int top, int left, int entryWidth, int entryHeight, int mouseX, int mouseY, boolean hovered, float partialTicks) {
                int rowLeft = TradeList.this.getRowLeft();
                int rowWidth = TradeList.this.itemWidth;
                int x = rowLeft + 4;
                int y = top + 7;
                int iconX = 23;
                int iconY = 0;
                boolean selected = (TradeList.this.getSelected() == this);
                boolean out = trade.maxTrades != -1 && trade.currentTrades >= trade.maxTrades;
                boolean disabled = !trade.enabled;
                int textureY = getButtonTextureY(hovered, selected, out || disabled);

                float alpha = disabled ? 0.45f : 1.0f;
                guiGraphics.setColor(1.0F, 1.0F, 1.0F, alpha);
                RenderSystem.enableBlend();
                RenderSystem.enableDepthTest();
                guiGraphics.fill(rowLeft, top, rowLeft + rowWidth, top + entryHeight, hovered ? 0xAA707070 : 0xAA303030);
                guiGraphics.setColor(1.0F, 1.0F, 1.0F, alpha);

                RenderSystem.setShaderTexture(0, ARROW_IMAGE);
                guiGraphics.blit(ARROW_IMAGE,  x + iconX, y + iconY, 0, 0, 21, 21, 21, 21);

                // Positionen der beiden Items (16x16 Icons)
                final int item1X = x + xOffset;
                final int item1Y = y + yOffset;
                final int item2X = item1X + 46;
                final int item2Y = item1Y;

                // Render erster (currency) Item
                guiGraphics.renderFakeItem(trade.currencyItem, item1X, item1Y);
                guiGraphics.renderItemDecorations(font, trade.currencyItem, item1X, item1Y);

                // Render zweiter (trade) Item
                guiGraphics.renderFakeItem(trade.tradeItem, item2X, item2Y);
                guiGraphics.renderItemDecorations(font, trade.tradeItem, item2X, item2Y);

                guiGraphics.setColor(1.0F, 1.0F, 1.0F, 1.0F);


                if (!trade.currencyItem.isEmpty() && GuiWidgetBounds.contains(item1X, item1Y, 15, 15, mouseX, mouseY)) {
                    MerchantTradeScreen.this.hoveredTooltipStack = trade.currencyItem;
                    MerchantTradeScreen.this.hoveredTooltipX = mouseX;
                    MerchantTradeScreen.this.hoveredTooltipY = mouseY;
                } else if (!trade.tradeItem.isEmpty() && GuiWidgetBounds.contains(item2X, item2Y, 15, 15, mouseX, mouseY)) {
                    MerchantTradeScreen.this.hoveredTooltipStack = trade.tradeItem;
                    MerchantTradeScreen.this.hoveredTooltipX = mouseX;
                    MerchantTradeScreen.this.hoveredTooltipY = mouseY;
                }
            }

            @Override
            public boolean mouseClicked(double mouseX, double mouseY, int button) {
                TradeList.this.setSelected(this);
                MerchantTradeScreen.this.selection = this.trade;
                MerchantTradeScreen.this.updateButtonState();
                return true;
            }

            public Component getNarration() {
                return Component.empty();
            }

            @Override
            public ListScreenListBase<TradeEntry> getList() {
                return TradeList.this;
            }

            private int getButtonTextureY(boolean hovered, boolean selected, boolean out) {
                final int BUTTON_Y_OUT = 46;
                final int BUTTON_Y_NORMAL = 66;
                final int BUTTON_Y_HOVER = 86;
                final int BUTTON_Y_PRESSED = 86;

                if (out) return BUTTON_Y_OUT;
                if (selected) return BUTTON_Y_PRESSED;
                if (hovered) return BUTTON_Y_HOVER;

                return BUTTON_Y_NORMAL;
            }
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageUpdateMerchant(merchantEntity.getUUID(), isCreative, false, isDailyRefresh));
    }

}
