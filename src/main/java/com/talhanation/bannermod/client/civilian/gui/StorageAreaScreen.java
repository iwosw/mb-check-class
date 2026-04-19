package com.talhanation.bannermod.client.civilian.gui;

import com.talhanation.bannermod.client.military.gui.widgets.RecruitsCheckBox;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.civilian.workarea.StorageArea;
import com.talhanation.bannermod.network.messages.civilian.MessageUpdateStorageArea;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;

public class StorageAreaScreen extends WorkAreaScreen {

    private static final MutableComponent TEXT_ALL = Component.translatable("gui.workers.checkbox.all");
    private static final MutableComponent TEXT_MINERS = Component.translatable("gui.workers.checkbox.miners");
    private static final MutableComponent TEXT_LUMBERS = Component.translatable("gui.workers.checkbox.lumbers");
    private static final MutableComponent TEXT_BUILDERS = Component.translatable("gui.workers.checkbox.builders");
    private static final MutableComponent TEXT_ANIMAL_FARMERS = Component.translatable("gui.workers.checkbox.animalFarmers");
    private static final MutableComponent TEXT_FARMERS = Component.translatable("gui.workers.checkbox.farmers");
    private static final MutableComponent TEXT_MERCHANTS = Component.translatable("gui.workers.checkbox.merchants");
    private static final MutableComponent TEXT_FISHERMAN = Component.translatable("gui.workers.checkbox.fisherman");

    private static final Component TEXT_STORAGE_NAME = Component.translatable("entity.workers.storage");;
    private static final Component TEXT_ROUTE_DESTINATION = Component.literal("Route destination UUID");
    private static final Component TEXT_ROUTE_FILTER = Component.literal("Route filter item ids");
    private static final Component TEXT_ROUTE_COUNT = Component.literal("Route count");
    private static final Component TEXT_ROUTE_PRIORITY = Component.literal("Route priority");
    private static final Component TEXT_PORT_ENTRYPOINT = Component.literal("Port entrypoint");
    private static final Component TEXT_ROUTE_BLOCKED = Component.literal("Blocked state");
    public final StorageArea storageArea;
    private boolean replant;
    private boolean stripLogs;
    private boolean shearLeaves;
    private RecruitsCheckBox minersCheckBox;
    private RecruitsCheckBox lumbersCheckBox;
    private RecruitsCheckBox buildersCheckBox;
    private RecruitsCheckBox farmersCheckBox;
    private RecruitsCheckBox merchantsCheckBox;
    private RecruitsCheckBox fishermanCheckBox;
    private RecruitsCheckBox animalFarmerCheckBox;
    private RecruitsCheckBox portEntrypointCheckBox;
    private boolean miners;
    private boolean lumbers;
    private boolean builders;
    private boolean farmers;
    private boolean merchants;
    private boolean fisherman;
    private boolean animalFarmer;
    private EnumSet<StorageArea.StorageType> types;
    public EditBox nameEditBox;
    public EditBox routeDestinationEditBox;
    public EditBox routeFilterEditBox;
    public EditBox routeCountEditBox;
    public EditBox routePriorityEditBox;
    public Component savedName;
    public String routeDestination;
    public String routeFilter;
    public String routeCount;
    public String routePriority;
    public boolean portEntrypoint;
    public StorageAreaScreen(StorageArea storageArea, Player player) {
        super(storageArea.getCustomName(), storageArea, player);
        this.storageArea = storageArea;
        this.types = storageArea.getStorageTypes();
        this.miners = types.contains(StorageArea.StorageType.MINERS);
        this.lumbers = types.contains(StorageArea.StorageType.LUMBERS);
        this.builders = types.contains(StorageArea.StorageType.BUILDERS);
        this.farmers = types.contains(StorageArea.StorageType.FARMERS);
        this.merchants = types.contains(StorageArea.StorageType.MERCHANTS);
        this.fisherman = types.contains(StorageArea.StorageType.FISHERMAN);
        this.animalFarmer = types.contains(StorageArea.StorageType.ANIMAL_FARMERS);
        this.routeDestination = storageArea.getRouteDestinationText();
        this.routeFilter = storageArea.getRouteFilterText();
        this.routeCount = Integer.toString(storageArea.getRouteRequestedCount());
        this.routePriority = storageArea.getRoutePriorityText();
        this.portEntrypoint = storageArea.isPortEntrypoint();
    }

    @Override
    protected void init() {
        this.savedName = workArea.getCustomName();
        if(this.savedName == null || this.savedName.getString().isEmpty()){
            this.savedName = TEXT_STORAGE_NAME;
        }
        setButtons();
    }

    @Override
    public void setButtons() {
        super.setButtons();

        int checkBoxWidth = 100;
        int checkBoxHeight = 20;
        int routeFieldWidth = 150;

        int checkBoxX = x - checkBoxWidth / 2;
        int checkBoxY = y + checkBoxHeight / 2 + 40;
        int routeFieldX = x + 40;
        int routeFieldY = y - 30;

        nameEditBox = new EditBox(font, checkBoxX , checkBoxY - 20, checkBoxWidth, checkBoxHeight, Component.literal(""));
        nameEditBox.setValue(savedName.getString());
        nameEditBox.setTextColor(-1);
        nameEditBox.setTextColorUneditable(-1);
        nameEditBox.setBordered(true);
        nameEditBox.setMaxLength(32);
        nameEditBox.setResponder(this::setName);
        this.addRenderableWidget(nameEditBox);

        routeDestinationEditBox = new EditBox(font, routeFieldX, routeFieldY, routeFieldWidth, checkBoxHeight, Component.empty());
        routeDestinationEditBox.setValue(this.routeDestination);
        routeDestinationEditBox.setMaxLength(36);
        routeDestinationEditBox.setResponder(value -> this.routeDestination = value);
        this.addRenderableWidget(routeDestinationEditBox);

        routeFilterEditBox = new EditBox(font, routeFieldX, routeFieldY + 24, routeFieldWidth, checkBoxHeight, Component.empty());
        routeFilterEditBox.setValue(this.routeFilter);
        routeFilterEditBox.setMaxLength(128);
        routeFilterEditBox.setResponder(value -> this.routeFilter = value);
        this.addRenderableWidget(routeFilterEditBox);

        routeCountEditBox = new EditBox(font, routeFieldX, routeFieldY + 48, 70, checkBoxHeight, Component.empty());
        routeCountEditBox.setValue(this.routeCount);
        routeCountEditBox.setMaxLength(5);
        routeCountEditBox.setResponder(value -> this.routeCount = value);
        this.addRenderableWidget(routeCountEditBox);

        routePriorityEditBox = new EditBox(font, routeFieldX + 80, routeFieldY + 48, 70, checkBoxHeight, Component.empty());
        routePriorityEditBox.setValue(this.routePriority);
        routePriorityEditBox.setMaxLength(10);
        routePriorityEditBox.setResponder(value -> this.routePriority = value);
        this.addRenderableWidget(routePriorityEditBox);

        this.portEntrypointCheckBox = new RecruitsCheckBox(routeFieldX, routeFieldY + 72, routeFieldWidth, checkBoxHeight, TEXT_PORT_ENTRYPOINT,
                this.portEntrypoint,
                (bool) -> {
                    this.portEntrypoint = bool;
                    sendMessage();
                }
        );
        addRenderableWidget(portEntrypointCheckBox);

        this.minersCheckBox = new RecruitsCheckBox(checkBoxX, 10 + checkBoxY, checkBoxWidth, checkBoxHeight, TEXT_MINERS,
                this.miners,
                (bool) -> {
                    this.miners = bool;
                    if(miners){
                        types.add(StorageArea.StorageType.MINERS);
                    }
                    else{
                        types.remove(StorageArea.StorageType.MINERS);
                    }

                    sendMessage();
                }
        );
        addRenderableWidget(minersCheckBox);

        this.lumbersCheckBox = new RecruitsCheckBox(checkBoxX, 30 + checkBoxY, checkBoxWidth, checkBoxHeight, TEXT_LUMBERS,
                this.lumbers,
                (bool) -> {
                    this.lumbers = bool;
                    if(lumbers){
                        types.add(StorageArea.StorageType.LUMBERS);
                    }
                    else{
                        types.remove(StorageArea.StorageType.LUMBERS);
                    }
                    sendMessage();
                }
        );
        addRenderableWidget(lumbersCheckBox);

        this.buildersCheckBox = new RecruitsCheckBox(checkBoxX, 50 + checkBoxY, checkBoxWidth, checkBoxHeight, TEXT_BUILDERS,
                this.builders,
                (bool) -> {
                    this.builders = bool;
                    if(builders){
                        types.add(StorageArea.StorageType.BUILDERS);
                    }
                    else{
                        types.remove(StorageArea.StorageType.BUILDERS);
                    }
                    sendMessage();
                }
        );
        addRenderableWidget(buildersCheckBox);

        this.farmersCheckBox = new RecruitsCheckBox(checkBoxX, 70 + checkBoxY, checkBoxWidth, checkBoxHeight, TEXT_FARMERS,
                this.farmers,
                (bool) -> {
                    this.farmers = bool;
                    if(farmers){
                        types.add(StorageArea.StorageType.FARMERS);
                    }
                    else{
                        types.remove(StorageArea.StorageType.FARMERS);
                    }
                    sendMessage();
                }
        );
        addRenderableWidget(farmersCheckBox);

        this.merchantsCheckBox = new RecruitsCheckBox(checkBoxX, 90 + checkBoxY, checkBoxWidth, checkBoxHeight, TEXT_MERCHANTS,
                this.merchants,
                (bool) -> {
                    this.merchants = bool;
                    if(merchants){
                        types.add(StorageArea.StorageType.MERCHANTS);
                    }
                    else{
                        types.remove(StorageArea.StorageType.MERCHANTS);
                    }
                    sendMessage();
                }
        );
        addRenderableWidget(merchantsCheckBox);

        this.fishermanCheckBox = new RecruitsCheckBox(checkBoxX, 110 + checkBoxY, checkBoxWidth, checkBoxHeight, TEXT_FISHERMAN,
                this.fisherman,
                (bool) -> {
                    this.fisherman = bool;
                    if(fisherman){
                        types.add(StorageArea.StorageType.FISHERMAN);
                    }
                    else{
                        types.remove(StorageArea.StorageType.FISHERMAN);
                    }
                    sendMessage();
                }
        );
        addRenderableWidget(fishermanCheckBox);

        this.animalFarmerCheckBox = new RecruitsCheckBox(checkBoxX, 130 + checkBoxY, checkBoxWidth, checkBoxHeight, TEXT_ANIMAL_FARMERS,
                this.animalFarmer,
                (bool) -> {
                    this.animalFarmer = bool;
                    if(animalFarmer){
                        types.add(StorageArea.StorageType.ANIMAL_FARMERS);
                    }
                    else{
                        types.remove(StorageArea.StorageType.ANIMAL_FARMERS);
                    }
                    sendMessage();
                }
        );
        addRenderableWidget(animalFarmerCheckBox);

    }

    private void setName(String s) {
        savedName = Component.literal(s);
    }

    public void sendMessage(){
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageUpdateStorageArea(
                storageArea.getUUID(),
                storageArea.getStorageMask(types),
                savedName.getString(),
                this.routeDestination,
                this.routeFilter,
                this.routeCount,
                this.routePriority,
                this.portEntrypoint
        ));
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        int labelX = x + 40;
        int labelY = y - 42;
        guiGraphics.drawString(font, TEXT_ROUTE_DESTINATION, labelX, labelY, 4210752, false);
        guiGraphics.drawString(font, TEXT_ROUTE_FILTER, labelX, labelY + 24, 4210752, false);
        guiGraphics.drawString(font, TEXT_ROUTE_COUNT, labelX, labelY + 48, 4210752, false);
        guiGraphics.drawString(font, TEXT_ROUTE_PRIORITY, labelX + 80, labelY + 48, 4210752, false);
        guiGraphics.drawString(font, TEXT_ROUTE_BLOCKED, labelX, labelY + 100, 4210752, false);
        guiGraphics.drawString(font, Component.literal(this.storageArea.getRouteBlockedReasonToken().isBlank() ? "none" : this.storageArea.getRouteBlockedReasonToken()), labelX, labelY + 112, 4210752, false);
        if (!this.storageArea.getRouteBlockedMessage().isBlank()) {
            guiGraphics.drawString(font, Component.literal(this.storageArea.getRouteBlockedMessage()), labelX, labelY + 124, 4210752, false);
        }
    }

    @Override
    public void onClose() {
        super.onClose();
        sendMessage();
    }
}
