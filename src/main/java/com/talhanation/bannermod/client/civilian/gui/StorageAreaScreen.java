package com.talhanation.bannermod.client.civilian.gui;

import com.talhanation.bannermod.client.military.gui.MilitaryGuiStyle;
import com.talhanation.bannermod.client.military.gui.widgets.RecruitsCheckBox;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.civilian.workarea.StorageArea;
import com.talhanation.bannermod.network.messages.civilian.MessageUpdateStorageArea;
import com.talhanation.bannermod.shared.logistics.BannerModLogisticsAuthoringState;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;

import java.util.EnumSet;
import java.util.List;

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
    private static final Component TEXT_ROUTE_DESTINATION = Component.translatable("gui.workers.storage.route.destination");
    private static final Component TEXT_ROUTE_FILTER = Component.translatable("gui.workers.storage.route.filter");
    private static final Component TEXT_ROUTE_COUNT = Component.translatable("gui.workers.storage.route.count");
    private static final Component TEXT_ROUTE_PRIORITY = Component.translatable("gui.workers.storage.route.priority");
    private static final Component TEXT_APPLY_ROUTE = Component.translatable("gui.workers.storage.route.apply");
    private static final Component TEXT_PORT_ENTRYPOINT = Component.translatable("gui.workers.storage.route.port_entrypoint");
    private static final Component TEXT_ROUTE_BLOCKED = Component.translatable("gui.workers.storage.route.blocked_state");
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
    private Component routeStatus = text("gui.workers.storage.route.status.pending_apply");
    private int routeStatusColor = 0xFFAAAAAA;
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
        int typesColumnWidth = checkBoxWidth - 4;
        int typesColumnLeft = checkBoxX - typesColumnWidth - 4;
        int typesColumnRight = checkBoxX + 4;
        int typesRowHeight = 18;
        int typesY = checkBoxY + 10;

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

        addRenderableWidget(Button.builder(TEXT_APPLY_ROUTE, button -> applyRoute())
                .bounds(routeFieldX, routeFieldY + 96, routeFieldWidth, checkBoxHeight)
                .build());

        // Worker-type filters laid out as a compact 2-column grid (4 + 3) instead of a
        // 7-row stack. Same authoritative state, just less vertical real estate.
        this.minersCheckBox = addRenderableWidget(new RecruitsCheckBox(typesColumnLeft, typesY, typesColumnWidth, checkBoxHeight, TEXT_MINERS,
                this.miners, bool -> toggleType(StorageArea.StorageType.MINERS, bool)));
        this.lumbersCheckBox = addRenderableWidget(new RecruitsCheckBox(typesColumnRight, typesY, typesColumnWidth, checkBoxHeight, TEXT_LUMBERS,
                this.lumbers, bool -> toggleType(StorageArea.StorageType.LUMBERS, bool)));
        this.buildersCheckBox = addRenderableWidget(new RecruitsCheckBox(typesColumnLeft, typesY + typesRowHeight, typesColumnWidth, checkBoxHeight, TEXT_BUILDERS,
                this.builders, bool -> toggleType(StorageArea.StorageType.BUILDERS, bool)));
        this.farmersCheckBox = addRenderableWidget(new RecruitsCheckBox(typesColumnRight, typesY + typesRowHeight, typesColumnWidth, checkBoxHeight, TEXT_FARMERS,
                this.farmers, bool -> toggleType(StorageArea.StorageType.FARMERS, bool)));
        this.merchantsCheckBox = addRenderableWidget(new RecruitsCheckBox(typesColumnLeft, typesY + typesRowHeight * 2, typesColumnWidth, checkBoxHeight, TEXT_MERCHANTS,
                this.merchants, bool -> toggleType(StorageArea.StorageType.MERCHANTS, bool)));
        this.fishermanCheckBox = addRenderableWidget(new RecruitsCheckBox(typesColumnRight, typesY + typesRowHeight * 2, typesColumnWidth, checkBoxHeight, TEXT_FISHERMAN,
                this.fisherman, bool -> toggleType(StorageArea.StorageType.FISHERMAN, bool)));
        this.animalFarmerCheckBox = addRenderableWidget(new RecruitsCheckBox(typesColumnLeft, typesY + typesRowHeight * 3, typesColumnWidth * 2 + 8, checkBoxHeight, TEXT_ANIMAL_FARMERS,
                this.animalFarmer, bool -> toggleType(StorageArea.StorageType.ANIMAL_FARMERS, bool)));
    }

    private void toggleType(StorageArea.StorageType type, boolean enabled) {
        if (enabled) {
            this.types.add(type);
        } else {
            this.types.remove(type);
        }
        switch (type) {
            case MINERS -> this.miners = enabled;
            case LUMBERS -> this.lumbers = enabled;
            case BUILDERS -> this.builders = enabled;
            case FARMERS -> this.farmers = enabled;
            case MERCHANTS -> this.merchants = enabled;
            case FISHERMAN -> this.fisherman = enabled;
            case ANIMAL_FARMERS -> this.animalFarmer = enabled;
        }
        sendMessage();
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

    private void applyRoute() {
        try {
            BannerModLogisticsAuthoringState parsed = BannerModLogisticsAuthoringState.parse(
                    this.routeDestination, this.routeFilter, this.routeCount, this.routePriority);
            this.routeDestination = parsed.destinationText();
            this.routeFilter = parsed.filterText();
            this.routeCount = parsed.requestedCountText();
            this.routePriority = parsed.priorityText();
            if (this.routeDestinationEditBox != null) this.routeDestinationEditBox.setValue(this.routeDestination);
            if (this.routeFilterEditBox != null) this.routeFilterEditBox.setValue(this.routeFilter);
            if (this.routeCountEditBox != null) this.routeCountEditBox.setValue(this.routeCount);
            if (this.routePriorityEditBox != null) this.routePriorityEditBox.setValue(this.routePriority);
            this.routeStatus = parsed.destinationStorageAreaId() == null
                    ? text("gui.workers.storage.route.status.disabled_no_destination")
                    : text("gui.workers.storage.route.status.valid", shortId(parsed.destinationStorageAreaId()), parsed.requestedCount(), priorityName(parsed.priority()));
            this.routeStatusColor = parsed.destinationStorageAreaId() == null ? 0xFFFFFF88 : 0xFFAAFFAA;
            sendMessage();
        } catch (IllegalArgumentException exception) {
            this.routeStatus = routeErrorText(exception.getMessage());
            this.routeStatusColor = 0xFFFF8888;
        }
    }

    private static Component routeErrorText(String message) {
        if ("Route destination must be a valid storage-area UUID.".equals(message)) {
            return text("gui.workers.storage.route.error.invalid_destination");
        }
        if ("Route requested count must be greater than 0.".equals(message)) {
            return text("gui.workers.storage.route.error.count_positive");
        }
        if ("Route requested count must be a whole number.".equals(message)) {
            return text("gui.workers.storage.route.error.count_whole");
        }
        if ("Route priority must be HIGH, NORMAL, or LOW.".equals(message)) {
            return text("gui.workers.storage.route.error.priority");
        }
        if ("Route filter must be a comma-separated list of item ids.".equals(message)) {
            return text("gui.workers.storage.route.error.filter");
        }
        return Component.literal(message == null ? "" : message);
    }

    private static Component priorityName(com.talhanation.bannermod.shared.logistics.BannerModLogisticsPriority priority) {
        return switch (priority) {
            case HIGH -> text("gui.workers.storage.route.priority.high");
            case NORMAL -> text("gui.workers.storage.route.priority.normal");
            case LOW -> text("gui.workers.storage.route.priority.low");
        };
    }

    private static Component text(String key, Object... args) {
        return Component.translatable(key, args);
    }

    private static String shortId(java.util.UUID uuid) {
        String value = uuid.toString();
        return value.substring(0, Math.min(8, value.length()));
    }

    @Override
    protected List<Component> getSettingSummaryLines() {
        if (this.routeDestination == null || this.routeDestination.isBlank()) {
            return List.of(
                    text("gui.workers.storage.summary.depot_access"),
                    text("gui.workers.storage.summary.route_missing")
            );
        }

        if (!this.storageArea.getRouteBlockedMessage().isBlank()) {
            return List.of(
                    text("gui.workers.storage.summary.route_ready", this.routeDestination, this.routeCount),
                    Component.literal(this.storageArea.getRouteBlockedMessage())
            );
        }

        if (!this.storageArea.getRouteBlockedReasonToken().isBlank()) {
            return List.of(
                    text("gui.workers.storage.summary.route_ready", this.routeDestination, this.routeCount),
                    text("gui.workers.storage.summary.route_blocked", this.storageArea.getRouteBlockedReasonToken())
            );
        }

        return List.of(
                text("gui.workers.storage.summary.depot_access"),
                text("gui.workers.storage.summary.route_ready", this.routeDestination, this.routeCount)
        );
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float partialTicks) {
        super.render(guiGraphics, mouseX, mouseY, partialTicks);
        int labelX = x + 40;
        int labelY = y - 42;
        int labelWidth = 150;
        int labelColor = 0xFF000000 | MilitaryGuiStyle.TEXT;
        int mutedColor = 0xFF000000 | MilitaryGuiStyle.TEXT_MUTED;
        int errorColor = 0xFF000000 | MilitaryGuiStyle.TEXT_DENIED;
        drawClamped(guiGraphics, TEXT_ROUTE_DESTINATION, labelX, labelY, labelWidth, labelColor);
        drawClamped(guiGraphics, TEXT_ROUTE_FILTER, labelX, labelY + 24, labelWidth, labelColor);
        drawClamped(guiGraphics, TEXT_ROUTE_COUNT, labelX, labelY + 48, 70, labelColor);
        drawClamped(guiGraphics, TEXT_ROUTE_PRIORITY, labelX + 80, labelY + 48, 70, labelColor);
        drawClamped(guiGraphics, TEXT_ROUTE_BLOCKED, labelX, labelY + 100, labelWidth, labelColor);
        Component destination = this.routeDestination == null || this.routeDestination.isBlank()
                ? text("gui.workers.storage.route.destination_value", text("gui.bannermod.common.none"))
                : text("gui.workers.storage.route.destination_value", this.routeDestination);
        drawClamped(guiGraphics, destination, labelX, labelY + 112, labelWidth, mutedColor);
        drawClamped(guiGraphics, this.routeStatus, labelX, labelY + 124, labelWidth, this.routeStatusColor);
        boolean blocked = !this.storageArea.getRouteBlockedReasonToken().isBlank() || !this.storageArea.getRouteBlockedMessage().isBlank();
        Component blockedLine = this.storageArea.getRouteBlockedReasonToken().isBlank()
                ? text("gui.workers.storage.route.blocked_value", text("gui.bannermod.common.none"))
                : text("gui.workers.storage.route.blocked_value", this.storageArea.getRouteBlockedReasonToken());
        drawClamped(guiGraphics, blockedLine, labelX, labelY + 136, labelWidth, blocked ? errorColor : mutedColor);
        if (!this.storageArea.getRouteBlockedMessage().isBlank()) {
            drawClamped(guiGraphics, Component.literal(this.storageArea.getRouteBlockedMessage()), labelX, labelY + 148, labelWidth, errorColor);
        }
    }

    private void drawClamped(GuiGraphics guiGraphics, Component text, int x, int y, int width, int color) {
        guiGraphics.drawString(font, font.plainSubstrByWidth(text.getString(), width), x, y, color, false);
    }

    @Override
    public void onClose() {
        super.onClose();
    }
}
