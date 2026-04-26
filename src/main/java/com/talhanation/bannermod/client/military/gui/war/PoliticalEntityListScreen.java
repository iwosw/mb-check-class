package com.talhanation.bannermod.client.military.gui.war;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.network.messages.war.MessageCreatePoliticalEntity;
import com.talhanation.bannermod.network.messages.war.MessageRenamePoliticalEntity;
import com.talhanation.bannermod.network.messages.war.MessageSetPoliticalEntityCapital;
import com.talhanation.bannermod.war.client.WarClientState;
import com.talhanation.bannermod.war.registry.PoliticalEntityRecord;
import com.talhanation.bannermod.war.registry.PoliticalEntityStatus;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class PoliticalEntityListScreen extends Screen {
    private static final int W = 360;
    private static final int H = 220;
    private static final int ROW_H = 16;
    private static final int LIST_VISIBLE = 9;

    private final Screen parent;
    private int guiLeft;
    private int guiTop;
    private int scrollOffset;
    private List<PoliticalEntityRecord> entities = List.of();
    @Nullable
    private PoliticalEntityRecord selected;
    @Nullable
    private Button renameButton;
    @Nullable
    private Button setCapitalButton;

    public PoliticalEntityListScreen(@Nullable Screen parent) {
        super(Component.literal("States"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        this.guiLeft = (this.width - W) / 2;
        this.guiTop = (this.height - H) / 2;
        this.entities = new ArrayList<>(WarClientState.entities());
        if (this.selected != null) {
            this.selected = WarClientState.entityById(this.selected.id());
        }
        addRenderableWidget(Button.builder(Component.literal("Create"), btn -> openCreateDialog())
                .bounds(guiLeft + 8, guiTop + H - 24, 60, 18).build());
        this.renameButton = addRenderableWidget(Button.builder(Component.literal("Rename"), btn -> openRenameDialog())
                .bounds(guiLeft + 72, guiTop + H - 24, 64, 18).build());
        this.setCapitalButton = addRenderableWidget(Button.builder(Component.literal("Capital here"), btn -> setCapitalHere())
                .bounds(guiLeft + 140, guiTop + H - 24, 84, 18).build());
        addRenderableWidget(Button.builder(Component.literal("Refresh"), btn -> refresh())
                .bounds(guiLeft + W - 172, guiTop + H - 24, 80, 18).build());
        addRenderableWidget(Button.builder(Component.literal("Back"), btn -> onClose())
                .bounds(guiLeft + W - 86, guiTop + H - 24, 80, 18).build());
        updateLeaderButtons();
    }

    private void refresh() {
        this.entities = new ArrayList<>(WarClientState.entities());
        if (this.selected != null) {
            this.selected = WarClientState.entityById(this.selected.id());
        }
        updateLeaderButtons();
    }

    private void openCreateDialog() {
        Minecraft.getInstance().setScreen(new PoliticalEntityNameInputScreen(
                this,
                "Create state",
                "Pick a name (3–32 chars):",
                "",
                this::sendCreate
        ));
    }

    private void openRenameDialog() {
        if (this.selected == null) return;
        UUID id = this.selected.id();
        Minecraft.getInstance().setScreen(new PoliticalEntityNameInputScreen(
                this,
                "Rename state",
                "New name (3–32 chars):",
                this.selected.name(),
                newName -> sendRename(id, newName)
        ));
    }

    private void setCapitalHere() {
        if (this.selected == null) return;
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageSetPoliticalEntityCapital(this.selected.id()));
    }

    private void sendCreate(String name) {
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageCreatePoliticalEntity(name));
    }

    private void sendRename(UUID entityId, String newName) {
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageRenamePoliticalEntity(entityId, newName));
    }

    private void updateLeaderButtons() {
        boolean leader = isLocalPlayerLeader(this.selected);
        if (this.renameButton != null) {
            this.renameButton.active = leader;
        }
        if (this.setCapitalButton != null) {
            this.setCapitalButton.active = leader;
        }
    }

    private static boolean isLocalPlayerLeader(@Nullable PoliticalEntityRecord entity) {
        if (entity == null) return false;
        Player player = Minecraft.getInstance().player;
        if (player == null) return false;
        UUID leader = entity.leaderUuid();
        return leader != null && leader.equals(player.getUUID());
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics);
        graphics.fill(guiLeft, guiTop, guiLeft + W, guiTop + H, 0xC0101010);
        graphics.renderOutline(guiLeft, guiTop, W, H, 0xFFFFFFFF);
        graphics.drawCenteredString(font, "Political States (not claims or settlements)", guiLeft + W / 2, guiTop + 7, 0xFFFFFF);
        renderList(graphics, mouseX, mouseY);
        renderDetails(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    private void renderList(GuiGraphics graphics, int mouseX, int mouseY) {
        int listX = guiLeft + 8;
        int listY = guiTop + 26;
        int listW = 156;
        graphics.fill(listX, listY, listX + listW, listY + LIST_VISIBLE * ROW_H, 0x60000000);
        int rendered = Math.min(LIST_VISIBLE, Math.max(0, entities.size() - scrollOffset));
        for (int i = 0; i < rendered; i++) {
            PoliticalEntityRecord entity = entities.get(scrollOffset + i);
            int rowY = listY + i * ROW_H;
            boolean hovered = mouseX >= listX && mouseX < listX + listW && mouseY >= rowY && mouseY < rowY + ROW_H;
            boolean picked = selected != null && selected.id().equals(entity.id());
            if (picked || hovered) {
                graphics.fill(listX + 1, rowY, listX + listW - 1, rowY + ROW_H, picked ? 0xFF3B5BFF : 0x60FFFFFF);
            }
            graphics.drawString(font, entity.status().name(), listX + 4, rowY + 4, statusColor(entity.status()), false);
            graphics.drawString(font, font.plainSubstrByWidth(" " + displayName(entity), listW - 76), listX + 76, rowY + 4, 0xFFFFFF, false);
        }
        if (entities.isEmpty()) {
            graphics.drawCenteredString(font, "No states yet", listX + listW / 2, listY + 62, 0xAAAAAA);
        }
    }

    private void renderDetails(GuiGraphics graphics) {
        int x = guiLeft + 174;
        int y = guiTop + 28;
        int w = W - 182;
        graphics.drawString(font, "State Detail", x, y, 0xFFFFFF, false);
        if (selected == null) {
            graphics.drawString(font, "Select a state.", x, y + 14, 0xAAAAAA, false);
            graphics.drawString(font, "Settlement = place", x, y + 30, 0xAAAAAA, false);
            graphics.drawString(font, "Claim = protected land", x, y + 42, 0xAAAAAA, false);
            graphics.drawString(font, "State = political side", x, y + 54, 0xAAAAAA, false);
            return;
        }
        String[] lines = {
                "Name: " + displayName(selected),
                "Status: " + selected.status().name(),
                "Leader: " + shortId(selected.leaderUuid()),
                "Co-leaders: " + selected.coLeaderUuids().size(),
                "Capital: " + (selected.capitalPos() == null ? "(none)" : selected.capitalPos().toShortString()),
                "Color: " + (selected.color().isBlank() ? "(none)" : selected.color()),
                "Region: " + (selected.homeRegion().isBlank() ? "(none)" : selected.homeRegion()),
                "Wars: " + involvedWarCount(selected)
        };
        for (int i = 0; i < lines.length; i++) {
            graphics.drawString(font, font.plainSubstrByWidth(lines[i], w), x, y + 14 + i * 12, 0xFFFFFF, false);
        }
    }

    private static int involvedWarCount(PoliticalEntityRecord entity) {
        int count = 0;
        for (var war : WarClientState.wars()) {
            if (war.involves(entity.id())) {
                count++;
            }
        }
        return count;
    }

    private static String displayName(PoliticalEntityRecord entity) {
        return entity.name().isBlank() ? shortId(entity.id()) : entity.name();
    }

    private static String shortId(java.util.UUID id) {
        if (id == null) return "?";
        String s = id.toString();
        return s.length() > 8 ? s.substring(0, 8) : s;
    }

    private static int statusColor(PoliticalEntityStatus status) {
        return switch (status) {
            case STATE -> 0xFF55FF55;
            case VASSAL -> 0xFFFFFF55;
            case PEACEFUL -> 0xFF55AAFF;
            case SETTLEMENT -> 0xFFAAAAAA;
        };
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int listX = guiLeft + 8;
            int listY = guiTop + 26;
            int listW = 156;
            if (mouseX >= listX && mouseX < listX + listW && mouseY >= listY && mouseY < listY + LIST_VISIBLE * ROW_H) {
                int idx = scrollOffset + (int) ((mouseY - listY) / ROW_H);
                if (idx >= 0 && idx < entities.size()) {
                    selected = entities.get(idx);
                    updateLeaderButtons();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double delta) {
        int max = Math.max(0, entities.size() - LIST_VISIBLE);
        scrollOffset = Math.max(0, Math.min(max, scrollOffset - (int) Math.signum(delta)));
        return true;
    }

    @Override
    public void onClose() {
        if (parent != null) {
            this.minecraft.setScreen(parent);
        } else {
            super.onClose();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }
}
