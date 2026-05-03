package com.talhanation.bannermod.client.civilian.gui;

import com.talhanation.bannermod.client.military.gui.MilitaryGuiStyle;
import com.talhanation.bannermod.client.military.gui.component.ActivateableButton;
import com.talhanation.bannermod.client.military.gui.widgets.BlackShowingTextField;
import com.talhanation.bannermod.bootstrap.WorkersRuntime;
import com.talhanation.bannermod.client.civilian.gui.widgets.DisplayTextItemScrollDropDownMenu;
import com.talhanation.bannermod.client.civilian.gui.widgets.ScrollDropDownMenuWithFolders;
import com.talhanation.bannermod.persistence.civilian.ScannedBlock;
import com.talhanation.bannermod.client.civilian.gui.structureRenderer.StructurePreviewWidget;
import com.talhanation.bannermod.entity.civilian.workarea.BuildArea;
import com.talhanation.bannermod.network.messages.civilian.MessageUpdateBuildArea;
import com.talhanation.bannermod.persistence.civilian.StructureManager;
import com.talhanation.bannermod.persistence.civilian.StructureTemplateLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.util.FastColor;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class BuildAreaScreen extends WorkAreaScreen {

    private static final int PANEL_TITLE = 0xFF000000 | MilitaryGuiStyle.TEXT;
    private static final int PANEL_TEXT = 0xFF000000 | MilitaryGuiStyle.TEXT_MUTED;

    public final BuildArea buildArea;
    public Button scanButton;
    public Button buildButton;
    public Button placeButton;
    public StructurePreviewWidget structurePreview;
    public ScrollDropDownMenuWithFolders structureOptions;
    public CompoundTag structureNBT;
    public List<ScannedBlock> structure;
    public Mode mode;
    public EditBox scanNameEditBox;
    public Button modeScanButton;
    public Button modeLoadButton;
    public Button saveButton;
    public Button xSizePlusButton;
    public Button xSizeMinusButton;
    public Button ySizePlusButton;
    public Button ySizeMinusButton;
    public Button zSizePlusButton;
    public Button zSizeMinusButton;
    public String savedName;
    public int areaWidthSize;
    public int areaHeightSize;
    public int areaDepthSize;
    public List< ItemStack> requiredItems = new ArrayList<>();
    public DisplayTextItemScrollDropDownMenu requiredItemsDropDownMenu;
    private Component statusText = text("gui.workers.build.status.ready");
    private int statusColor = 0xFFAAAAAA;
    public BuildAreaScreen(BuildArea buildArea, Player player) {
        super(buildArea.getCustomName(), buildArea, player);
        this.buildArea = buildArea;
    }

    @Override
    protected void init() {
        structureNBT = buildArea.getStructureNBT();
        if(structureNBT != null && !structureNBT.isEmpty()){
            mode = Mode.LOAD;
            structure = StructureManager.parseStructureFromNBT(structureNBT);
            this.requiredItems = buildArea.getRequiredMaterials(structureNBT);
            setStatus(text("gui.workers.build.status.loaded_structure", this.structure == null ? 0 : this.structure.size()), 0xFFAAFFAA);
        }
        else {
            mode = Mode.SCAN;
            setModeHintStatus();
        }

        this.areaWidthSize = buildArea.getWidthSize();
        this.areaHeightSize = buildArea.getHeightSize();
        this.areaDepthSize = buildArea.getDepthSize();
        setButtons();
    }

    @Override
    public void tick() {
        super.tick();
    }

    @Override
    public void setButtons() {
        this.clearWidgets();
        super.setButtons();
        structureOptions = null;
        int buttonWidth = 100;
        int buttonHeight = 20;
        int previewWidth = 200;
        int previewHeight = 100;
        int boxWidth = 80;
        int boxHeight = 20;
        y = y - 20;
        //MODE
        modeScanButton = addRenderableWidget(new ActivateableButton(x - buttonWidth - 101, y - previewHeight / 2 + 130, buttonWidth, buttonHeight, text("gui.workers.build.mode.scan"),
                btn -> {
                    this.mode = Mode.SCAN;

                    this.resetScan();
                    this.setButtons();
                }
        ));
        modeScanButton.active = this.mode == Mode.SCAN;

        modeLoadButton = addRenderableWidget(new ActivateableButton(x - buttonWidth - 101 , y - previewHeight / 2 + 130 + buttonHeight, buttonWidth, buttonHeight, text("gui.workers.build.mode.load"),
                btn -> {
                    this.mode = Mode.LOAD;

                    this.resetScan();
                    this.setButtons();
                }
        ));
        modeLoadButton.active = this.mode == Mode.LOAD;

        switch (mode){
            case SCAN -> {
                xSizePlusButton = addRenderableWidget(new ExtendedButton(x - buttonWidth - 41, y + 121, 20, 20, Component.literal("+"),
                        btn -> {
                            if(hasShiftDown()) areaWidthSize += 5;
                            else areaWidthSize++;
                            areaWidthSize = Mth.clamp(areaWidthSize, 3, 32);

                            this.workArea.setWidthSize(areaWidthSize);
                            WorkersRuntime.channel().sendToServer(new MessageUpdateBuildArea(this.workArea.getUUID(), areaWidthSize, areaHeightSize, areaDepthSize, structureNBT, false, false));

                            this.resetScan();
                            this.setButtons();
                        }
                ));

                xSizeMinusButton = addRenderableWidget(new ExtendedButton(x - buttonWidth - 21, y + 121, 20, 20, Component.literal("-"),
                        btn -> {
                            if(hasShiftDown()) areaWidthSize -= 5;
                            else areaWidthSize--;
                            areaWidthSize = Mth.clamp(areaWidthSize, 3, 32);

                            this.workArea.setWidthSize(areaWidthSize);
                            WorkersRuntime.channel().sendToServer(new MessageUpdateBuildArea(this.workArea.getUUID(), areaWidthSize, areaHeightSize, areaDepthSize,  structureNBT, false, false));

                            this.resetScan();
                            this.setButtons();
                        }
                ));

                ySizePlusButton = addRenderableWidget(new ExtendedButton(x - buttonWidth - 41, y + 141, 20, 20, Component.literal("+"),
                        btn -> {
                            if(hasShiftDown()) areaHeightSize += 5;
                            else areaHeightSize++;
                            areaHeightSize = Mth.clamp(areaHeightSize, 3, 32);

                            this.workArea.setHeightSize(areaHeightSize);
                            WorkersRuntime.channel().sendToServer(new MessageUpdateBuildArea(this.workArea.getUUID(), areaWidthSize, areaHeightSize, areaDepthSize, structureNBT, false, false));

                            this.resetScan();
                            this.setButtons();
                        }
                ));

                ySizeMinusButton = addRenderableWidget(new ExtendedButton(x - buttonWidth - 21, y + 141, 20, 20, Component.literal("-"),
                        btn -> {
                            if(hasShiftDown()) areaHeightSize -= 5;
                            else areaHeightSize--;
                            areaHeightSize = Mth.clamp(areaHeightSize, 3, 32);

                            this.workArea.setHeightSize(areaHeightSize);
                            UUID uuid = this.buildArea.getUUID();
                            WorkersRuntime.channel().sendToServer(new MessageUpdateBuildArea(uuid, areaWidthSize, areaHeightSize, areaDepthSize, structureNBT, false, false));
                            this.resetScan();
                            this.setButtons();
                        }
                ));

                zSizePlusButton = addRenderableWidget(new ExtendedButton(x - buttonWidth - 41, y + 161, 20, 20, Component.literal("+"),
                        btn -> {
                            if(hasShiftDown()) areaDepthSize += 5;
                            else areaDepthSize++;
                            areaDepthSize = Mth.clamp(areaDepthSize, 3, 32);

                            this.workArea.setDepthSize(areaDepthSize);
                            WorkersRuntime.channel().sendToServer(new MessageUpdateBuildArea(this.workArea.getUUID(), areaWidthSize, areaHeightSize, areaDepthSize, structureNBT, false, false));
                            this.resetScan();
                            this.setButtons();
                        }
                ));

                zSizeMinusButton = addRenderableWidget(new ExtendedButton(x - buttonWidth - 21, y + 161, 20, 20, Component.literal("-"),
                        btn -> {
                            if(hasShiftDown()) areaDepthSize -= 5;
                            else areaDepthSize--;
                            areaDepthSize = Mth.clamp(areaDepthSize, 3, 32);

                            this.workArea.setDepthSize(areaDepthSize);
                            UUID uuid = this.buildArea.getUUID();
                            WorkersRuntime.channel().sendToServer(new MessageUpdateBuildArea(uuid, areaWidthSize, areaHeightSize, areaDepthSize, structureNBT, false , false));
                            this.resetScan();
                            this.setButtons();
                        }
                ));

                scanNameEditBox = new EditBox(font, x - previewWidth/2 , y - previewHeight / 2 + 130 - boxHeight - 2, previewWidth, boxHeight, Component.literal(""));
                scanNameEditBox.setValue(savedName != null ? savedName.toLowerCase(java.util.Locale.ROOT) : "");
                scanNameEditBox.setTextColor(-1);
                scanNameEditBox.setTextColorUneditable(-1);
                scanNameEditBox.setBordered(true);
                scanNameEditBox.setMaxLength(32);
                scanNameEditBox.setEditable(mode == Mode.SCAN);
                // Only allow lowercase letters, digits, underscores and slashes — no capitals
                scanNameEditBox.setFilter(s -> s.equals(s.toLowerCase(java.util.Locale.ROOT)));
                scanNameEditBox.setResponder(this::checkScanNameUpdate);
                this.addRenderableWidget(scanNameEditBox);

                scanButton = addRenderableWidget(new ExtendedButton(x - buttonWidth / 2, y - buttonHeight / 2 + 130, buttonWidth, buttonHeight, text("gui.workers.build.action.scan_area"),
                        btn ->{
                            this.performClientScan();
                            this.checkScanButtonActive();

                            checkSaveButtonActive(this.scanNameEditBox.getValue());
                        }
                ));

                saveButton = addRenderableWidget(new ExtendedButton(x - buttonWidth / 2, y + 182, buttonWidth, buttonHeight, text("gui.workers.build.action.save"),
                        btn -> StructureManager.saveStructureToFile(this.scanNameEditBox.getValue(), this.structureNBT)
                ));
                // Always run the disabled-state tooltip path so a freshly-opened screen
                // shows "scan first" / "name needed" instead of a silent grey button.
                checkSaveButtonActive(scanNameEditBox.getValue());

                structurePreview = new StructurePreviewWidget(x - previewWidth / 2, y - previewHeight / 2 + 130, previewWidth, previewHeight, buildArea.getWidthSize(), buildArea.getDepthSize());
                addRenderableWidget(structurePreview);
                checkScanButtonActive();
            }

            case LOAD -> {
                java.nio.file.Path scanRoot = java.nio.file.Path.of(
                        Minecraft.getInstance().gameDirectory.getAbsolutePath(), "workers", "scan");

                structureOptions = new ScrollDropDownMenuWithFolders(
                        x - previewWidth / 2 - 1,
                        y - previewHeight / 2 + 131 - boxHeight - 2,
                        previewWidth + 2,
                        boxHeight + 2,
                        scanRoot,
                        selectedRelPath -> {
                            CompoundTag tag = StructureManager.loadScanNbt(selectedRelPath);
                            if (tag != null) {
                                int width  = tag.getInt("width");
                                int height = tag.getInt("height");
                                int depth  = tag.getInt("depth");
                                this.savedName      = tag.getString("name");
                                this.areaWidthSize  = width;
                                this.areaHeightSize = height;
                                this.areaDepthSize  = depth;
                                this.buildArea.setWidthSize(width);
                                this.buildArea.setHeightSize(height);
                                this.buildArea.setDepthSize(depth);
                                WorkersRuntime.channel().sendToServer(
                                        new MessageUpdateBuildArea(this.buildArea.getUUID(), width, height, depth, tag, false, false));
                                this.structureNBT = tag;
                                this.structure    = StructureManager.parseStructureFromNBT(tag);
                                this.setStructure(this.structure, this.structureNBT);
                                setStatus(text("gui.workers.build.status.loaded_structure", this.structure == null ? 0 : this.structure.size()), 0xFFAAFFAA);
                                checkBuildButtonActive();
                                this.setButtons();
                            } else {
                                setStatus(text("gui.workers.build.status.load_rejected"), 0xFFFF8888);
                            }
                        }
                );
                addRenderableWidget(structureOptions);

                buildButton = addRenderableWidget(new ExtendedButton(x - buttonWidth / 2, y + 182, buttonWidth, buttonHeight, text("gui.workers.build.action.build"),
                        btn -> {
                            if (!validateStructureForBuild(false)) return;
                            setStatus(text("gui.workers.build.status.build_sent"), 0xFFAAFFAA);
                            WorkersRuntime.channel().sendToServer(new MessageUpdateBuildArea(this.buildArea.getUUID(), areaWidthSize, areaHeightSize, areaDepthSize, this.structureNBT, true, false));
                        }
                ));

                if(player.isCreative()){
                    placeButton = addRenderableWidget(new ExtendedButton(x - buttonWidth/2 + buttonWidth, y + 182, buttonWidth, buttonHeight, text("gui.workers.build.action.place"),
                            btn -> {
                                if (!validateStructureForBuild(true)) return;
                                setStatus(text("gui.workers.build.status.place_sent"), 0xFFAAFFAA);
                                WorkersRuntime.channel().sendToServer(new MessageUpdateBuildArea(this.buildArea.getUUID(), areaWidthSize, areaHeightSize, areaDepthSize, this.structureNBT, true, true));
                            }
                    ));
                }

                requiredItemsDropDownMenu = new DisplayTextItemScrollDropDownMenu(ItemStack.EMPTY, text("gui.workers.build.required_blocks").getString(), x + 101 , y + 60, 110, boxHeight, requiredItems, null);
                requiredItemsDropDownMenu.setBgFillSelected(FastColor.ARGB32.color(255, 139, 139, 139));
                requiredItemsDropDownMenu.setCanSelectItem(false);
                requiredItemsDropDownMenu.setResetCount(false);

                addRenderableWidget(requiredItemsDropDownMenu);

                structurePreview = new StructurePreviewWidget(x - previewWidth / 2, y - previewHeight / 2 + 130, previewWidth, previewHeight, buildArea.getWidthSize(), buildArea.getDepthSize());
                addRenderableWidget(structurePreview);
                if(structure != null) structurePreview.setStructure(this.structure, this.structureNBT);
                checkBuildButtonActive();

            }
        }

        int blackboxWidth = mode == Mode.SCAN ? 40 : 70;
        int blackboxWidth2 = mode == Mode.SCAN ? 20 : 30;
        int blackboxHeight = 20;
        int blackBoxPosX = x - 201;
        int blackBoxPosY = y + 100;
        addRenderableWidget(new BlackShowingTextField(blackBoxPosX, blackBoxPosY + 21, blackboxWidth, blackboxHeight, text("gui.workers.build.dimension.width")));
        addRenderableWidget(new BlackShowingTextField(blackBoxPosX, blackBoxPosY + 41, blackboxWidth, blackboxHeight, text("gui.workers.build.dimension.height")));
        addRenderableWidget(new BlackShowingTextField(blackBoxPosX, blackBoxPosY + 61, blackboxWidth, blackboxHeight, text("gui.workers.build.dimension.depth")));
        addRenderableWidget(new BlackShowingTextField(blackBoxPosX + blackboxWidth, blackBoxPosY + 21, blackboxWidth2, blackboxHeight, Component.literal("" + areaWidthSize)));
        addRenderableWidget(new BlackShowingTextField(blackBoxPosX + blackboxWidth, blackBoxPosY + 41, blackboxWidth2, blackboxHeight, Component.literal("" + areaHeightSize)));
        addRenderableWidget(new BlackShowingTextField(blackBoxPosX + blackboxWidth, blackBoxPosY + 61, blackboxWidth2, blackboxHeight, Component.literal("" + areaDepthSize)));
    }

    private void setStructure(List<ScannedBlock> structure, CompoundTag structureNBT) {
        this.requiredItems = buildArea.getRequiredMaterials(structureNBT);
        if (this.structurePreview != null) {
            this.structurePreview.setStructure(structure, structureNBT);
        }
        if (this.requiredItemsDropDownMenu != null) {
            this.requiredItemsDropDownMenu.setOptions(requiredItems);
        }
    }

    public void resetScan(){
        structure = null;
        structureNBT = new CompoundTag();
        if(this.structurePreview != null) structurePreview.setStructure(null, null);
        this.requiredItems = new ArrayList<>();
        setModeHintStatus();
    }

    private void setModeHintStatus() {
        if (this.mode == Mode.LOAD) {
            setStatus(text("gui.workers.build.status.next_load"), PANEL_TEXT);
        } else {
            setStatus(text("gui.workers.build.status.next_scan"), PANEL_TEXT);
        }
    }


    private void checkScanNameUpdate(String s) {
        checkSaveButtonActive(s);
    }

    private void checkScanButtonActive(){
        if(this.scanButton == null) return;
        boolean active = this.structure == null;
        scanButton.active = active;
        scanButton.visible = active;
        scanButton.setTooltip(active ? null : Tooltip.create(text("gui.workers.build.tooltip.scan_complete")));
    }
    private void checkBuildButtonActive() {
        if(this.buildButton == null) return;

        this.buildButton.active = this.structure != null && !this.structure.isEmpty();
        this.buildButton.setTooltip(this.buildButton.active ? null : Tooltip.create(text("gui.workers.build.tooltip.need_structure")));
        if(placeButton != null) {
            this.placeButton.active = this.buildButton.active;
            this.placeButton.setTooltip(this.placeButton.active ? null : Tooltip.create(text("gui.workers.build.tooltip.need_structure")));
        }
    }

    private void checkSaveButtonActive(String s) {
        if(this.saveButton == null) return;

        this.saveButton.active = this.structure != null && s != null && s.length() >= 3;
        this.saveButton.setTooltip(this.saveButton.active ? null : Tooltip.create(text(this.structure == null
                ? "gui.workers.build.tooltip.need_scan_before_save"
                : "gui.workers.build.tooltip.need_name")));
    }

    private void performClientScan() {
        Level level = Minecraft.getInstance().level;
        if (level == null) return;
        if (this.scanNameEditBox == null || this.scanNameEditBox.getValue().trim().length() < 3) {
            setStatus(text("gui.workers.build.status.scan_rejected_name"), 0xFFFF8888);
            return;
        }
        if (!dimensionsValid()) {
            setStatus(text("gui.workers.build.status.invalid_dimensions_scan"), 0xFFFF8888);
            return;
        }

        this.structureNBT = StructureManager.scanStructure(level, this.buildArea, this.scanNameEditBox.getValue());
        this.structure = StructureManager.parseStructureFromNBT(structureNBT);
        this.structurePreview.setStructure(structure, structureNBT);
        if (this.structure == null || this.structure.isEmpty()) {
            setStatus(text("gui.workers.build.status.scan_empty"), 0xFFFF8888);
        } else {
            setStatus(text("gui.workers.build.status.scan_accepted", this.structure.size()), 0xFFAAFFAA);
        }
    }

    private boolean validateStructureForBuild(boolean creativePlace) {
        if (!dimensionsValid()) {
            setStatus(text("gui.workers.build.status.invalid_dimensions_build"), 0xFFFF8888);
            return false;
        }
        if (this.structureNBT == null || this.structureNBT.isEmpty() || !this.structureNBT.contains("blocks", Tag.TAG_LIST)) {
            setStatus(text("gui.workers.build.status.need_structure"), 0xFFFF8888);
            return false;
        }
        if (this.structure == null || this.structure.isEmpty()) {
            setStatus(text("gui.workers.build.status.no_blocks"), 0xFFFF8888);
            return false;
        }
        if (!creativePlace && this.requiredItems != null && !this.requiredItems.isEmpty()) {
            setStatus(text("gui.workers.build.status.need_materials"), 0xFFFFFF88);
        }
        return true;
    }

    private boolean dimensionsValid() {
        return areaWidthSize >= 3 && areaWidthSize <= 32
                && areaHeightSize >= 3 && areaHeightSize <= 32
                && areaDepthSize >= 3 && areaDepthSize <= 32;
    }

    private void setStatus(Component statusText, int color) {
        this.statusText = statusText == null ? Component.empty() : statusText;
        this.statusColor = color;
    }

    private static Component text(String key, Object... args) {
        return Component.translatable(key, args);
    }

    @Override
    public void mouseMoved(double x, double y) {
        if(structureOptions != null){
            structureOptions.onMouseMove(x,y);
        }
        if(requiredItemsDropDownMenu != null){
            requiredItemsDropDownMenu.onMouseMove(x,y);
        }
        super.mouseMoved(x, y);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (requiredItemsDropDownMenu != null && requiredItemsDropDownMenu.isMouseOver(mouseX, mouseY)) {
            this.requiredItemsDropDownMenu.onMouseClick(mouseX, mouseY);
            return true;
        }

        if (structureOptions != null && structureOptions.isMouseOver(mouseX, mouseY)) {
            this.resetScan();
            this.structureOptions.onMouseClick(mouseX, mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }
    @Override
    public boolean mouseScrolled(double x, double y, double scrollX, double d) {
        if(structureOptions != null  && structureOptions.isMouseOver(x, y)) structureOptions.mouseScrolled(x, y, scrollX, d);
        if(requiredItemsDropDownMenu != null && requiredItemsDropDownMenu.isMouseOver(x, y)) requiredItemsDropDownMenu.mouseScrolled(x, y, scrollX, d);
        return super.mouseScrolled(x, y, scrollX, d);
    }

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.renderBackground(guiGraphics, mouseX, mouseY, delta);

        int previewLeft = x - 110;
        int previewTop = y + 74;
        int dimensionsLeft = x - 214;
        int dimensionsTop = y + 116;
        int statusLeft = x - 110;
        int statusTop = y + 198;

        drawPanel(guiGraphics, previewLeft, previewTop, 220, 112);
        drawPanel(guiGraphics, dimensionsLeft, dimensionsTop, 98, 72);
        drawPanel(guiGraphics, statusLeft, statusTop, 220, 40);

        if (this.mode == Mode.LOAD) {
            drawPanel(guiGraphics, x + 96, y + 54, 120, 32);
        }
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.renderForeground(guiGraphics, mouseX, mouseY, delta);

        int previewLeft = x - 110;
        int previewTop = y + 74;
        int statusLeft = x - 104;
        int statusTop = y + 204;

        guiGraphics.drawString(this.font, text("gui.workers.build.section.preview"), previewLeft + 6, previewTop + 4, PANEL_TITLE, false);
        guiGraphics.drawString(this.font, text("gui.workers.build.section.dimensions"), x - 208, y + 120, PANEL_TITLE, false);
        guiGraphics.drawString(this.font, text("gui.workers.build.section.status"), statusLeft, y + 202, PANEL_TITLE, false);
        if (this.mode == Mode.LOAD) {
            guiGraphics.drawString(this.font, text("gui.workers.build.section.materials"), x + 102, y + 58, PANEL_TITLE, false);
            // LOAD mode never instantiates scanNameEditBox; surface a disabled-style
            // placeholder so the SCAN-mode "Name" slot doesn't read as missing.
            guiGraphics.drawString(this.font,
                    Component.translatable("gui.workers.build.load.scan_name_na"),
                    x - 110 + 6, y + 74 - 12,
                    MilitaryGuiStyle.TEXT_MUTED, false);
        }

        Component previewHint = this.mode == Mode.LOAD
                ? text("gui.workers.build.preview.hint.load")
                : text("gui.workers.build.preview.hint.scan");
        drawWrapped(guiGraphics, previewHint, previewLeft + 108, previewTop + 4, 104, PANEL_TEXT);
        drawWrapped(guiGraphics, this.statusText, statusLeft, statusTop + 10, 208, this.statusColor);
    }

    private void drawPanel(GuiGraphics guiGraphics, int left, int top, int width, int height) {
        // Inset over the parent WorkAreaScreen wood panel — keep title strip + dark interior
        // so labels read at GUI scale 2 without fighting the parchment behind them.
        MilitaryGuiStyle.insetPanel(guiGraphics, left, top, width, height);
        MilitaryGuiStyle.titleStrip(guiGraphics, left + 1, top + 1, width - 2, 14);
    }

    private void drawWrapped(GuiGraphics guiGraphics, Component text, int x, int y, int width, int color) {
        for (var line : this.font.split(text, width)) {
            guiGraphics.drawString(this.font, line, x, y, color, false);
            y += 9;
        }
    }

    @Override
    public void onAreaMoved() {
        this.resetScan();
        checkScanButtonActive();
    }

    public enum Mode{
        SCAN,
        LOAD
    }


}
