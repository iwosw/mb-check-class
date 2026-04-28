package com.talhanation.bannermod.client.military.gui.widgets;

import com.talhanation.bannermod.client.military.gui.RecruitsScreenBase;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.Mth;

public abstract class ListScreenBase extends RecruitsScreenBase {

    private Runnable postRender;

    public ListScreenBase(Component title, int xSize, int ySize) {
        super(title, xSize, ySize);
    }

    @Override
    public void render(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.render(guiGraphics, mouseX, mouseY, delta);
        if (postRender != null) {
            postRender.run();
            postRender = null;
        }
    }

    public void postRender(Runnable postRender) {
        this.postRender = postRender;
    }

    protected ListLayout calculateListLayout(int headerSize, int footerSize, int searchHeight, int unitSize, int cellHeight) {
        int gapTop = (int) (this.height * 0.1F);
        int gapBottom = (int) (this.height * 0.1F);
        int minUnits = Mth.ceil((float) (cellHeight + searchHeight + 4) / (float) unitSize);
        int units = Math.max(minUnits, (height - headerSize - footerSize - gapTop - gapBottom - searchHeight) / unitSize);
        return new ListLayout(gapTop, gapBottom, units);
    }

    protected EditBox createSearchBox(String value, int x, int y, int width, int height, java.util.function.Consumer<String> responder) {
        EditBox box = new EditBox(font, x, y, width, height, Component.translatable("gui.recruits.list.search_hint"));
        box.setMaxLength(16);
        box.setTextColor(0xFFFFFF);
        box.setValue(value);
        box.setResponder(responder);
        return box;
    }

    protected void renderListPanel(GuiGraphics guiGraphics, ResourceLocation texture, int headerSize, int unitSize, int footerSize, int units) {
        guiGraphics.blit(texture, guiLeft, guiTop, 0, 0, xSize, headerSize);
        for (int i = 0; i < units; i++) {
            guiGraphics.blit(texture, guiLeft, guiTop + headerSize + unitSize * i, 0, headerSize, xSize, unitSize);
        }
        guiGraphics.blit(texture, guiLeft, guiTop + headerSize + unitSize * units, 0, headerSize + unitSize, xSize, footerSize);
        guiGraphics.blit(texture, guiLeft + 10, guiTop + headerSize + 6 - 2, xSize, 0, 12, 12);
    }

    protected void renderSearchableList(GuiGraphics guiGraphics, ListScreenListBase<?> list, EditBox searchBox, int mouseX, int mouseY, float delta, int headerSize, int unitSize, int units) {
        if (!list.isEmpty()) {
            list.render(guiGraphics, mouseX, mouseY, delta);
        } else if (!searchBox.getValue().isEmpty()) {
            guiGraphics.drawCenteredString(font, Component.translatable("gui.recruits.list.empty_search"), width / 2, guiTop + headerSize + (units * unitSize) / 2 - font.lineHeight / 2, -1);
        }
        searchBox.render(guiGraphics, mouseX, mouseY, delta);
    }

    protected static class ListLayout {
        public final int gapTop;
        public final int gapBottom;
        public final int units;

        private ListLayout(int gapTop, int gapBottom, int units) {
            this.gapTop = gapTop;
            this.gapBottom = gapBottom;
            this.units = units;
        }
    }

}
