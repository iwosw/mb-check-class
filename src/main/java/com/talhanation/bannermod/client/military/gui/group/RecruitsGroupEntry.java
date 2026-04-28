package com.talhanation.bannermod.client.military.gui.group;


import com.mojang.blaze3d.systems.RenderSystem;
import com.talhanation.bannermod.client.military.gui.widgets.ListScreenEntryBase;
import com.talhanation.bannermod.client.military.gui.widgets.ListScreenListBase;
import com.talhanation.bannermod.persistence.military.RecruitsGroup;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import org.jetbrains.annotations.NotNull;

@OnlyIn(Dist.CLIENT)
public class RecruitsGroupEntry extends ListScreenEntryBase<RecruitsGroupEntry> {
    protected final Minecraft minecraft;
    protected final IGroupSelection screen;
    protected final @NotNull RecruitsGroup group;
    protected ResourceLocation image;
    public RecruitsGroupEntry(IGroupSelection screen, @NotNull RecruitsGroup group) {
        this.minecraft = Minecraft.getInstance();
        this.screen = screen;
        this.group = group;
        this.image = RecruitsGroup.IMAGES.get(group.getImage());
    }

    @Override
    public void render(GuiGraphics guiGraphics, int index, int top, int left, int width, int height, int mouseX, int mouseY, boolean hovered, float delta) {
        renderElement(guiGraphics, top, left, width, height, hovered, iconX(left), iconY(top, height), textX(left), textY(minecraft, top, height));
    }

    public void renderElement(GuiGraphics guiGraphics, int top, int left, int width, int height, boolean hovered, int skinX, int skinY, int textX, int textY) {
        boolean selected = screen.getSelected() != null && group.getUUID().equals(screen.getSelected().getUUID());
        renderRowBackground(guiGraphics, left, top, width, height, hovered, selected, ROW_FILL_ALT);

        if(this.image != null){
            RenderSystem.setShader(GameRenderer::getPositionTexShader);
            RenderSystem.setShaderColor(1F, 1F, 1F, 1F);
            RenderSystem.setShaderTexture(0, this.image);
            guiGraphics.blit(this.image,  left + 5,  top + 5, 0, 0, 21, 21, 21, 21);
        }

        guiGraphics.drawString(minecraft.font, group.getName(), (float) textX, (float) textY,  ROW_TEXT, false);
        guiGraphics.drawString(minecraft.font, "[" + group.getCount() + "/" + group.getSize() + "]", (float) textX + 130, (float) textY,  ROW_TEXT, false);

    }
    public RecruitsGroup getGroup() {
        return group;
    }

    @Override
    public ListScreenListBase<RecruitsGroupEntry> getList() {
        return screen.getGroupList();
    }
}
