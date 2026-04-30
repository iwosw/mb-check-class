package com.talhanation.bannermod.client.military.gui.war;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.network.messages.war.MessageCreatePoliticalEntity;
import com.talhanation.bannermod.network.messages.war.MessageRenamePoliticalEntity;
import com.talhanation.bannermod.network.messages.war.MessageSetGovernmentForm;
import com.talhanation.bannermod.network.messages.war.MessageSetPoliticalEntityCapital;
import com.talhanation.bannermod.network.messages.war.MessageSetPoliticalEntityCharter;
import com.talhanation.bannermod.network.messages.war.MessageSetPoliticalEntityColor;
import com.talhanation.bannermod.network.messages.war.MessageSetPoliticalEntityStatus;
import com.talhanation.bannermod.network.messages.war.MessageUpdateCoLeader;
import com.talhanation.bannermod.util.GameProfileUtils;
import com.talhanation.bannermod.war.client.WarClientState;
import com.talhanation.bannermod.war.registry.GovernmentForm;
import com.talhanation.bannermod.war.registry.PoliticalEntityAuthority;
import com.talhanation.bannermod.war.registry.PoliticalEntityRecord;
import com.talhanation.bannermod.war.registry.PoliticalEntityStatus;
import com.talhanation.bannermod.war.registry.PoliticalRegistryValidation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
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
    private int observedWarStateVersion = -1;
    @Nullable
    private PoliticalEntityRecord selected;
    @Nullable
    private Button renameButton;
    @Nullable
    private Button setCapitalButton;
    @Nullable
    private Button toggleFormButton;
    @Nullable
    private Button setColorButton;
    @Nullable
    private Button setCharterButton;
    @Nullable
    private Button addCoLeaderButton;
    @Nullable
    private Button removeCoLeaderButton;
    @Nullable
    private Button promoteStateButton;

    public PoliticalEntityListScreen(@Nullable Screen parent) {
        super(text("gui.bannermod.states.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        this.guiLeft = (this.width - W) / 2;
        this.guiTop = (this.height - H) / 2;
        addRenderableWidget(Button.builder(text("gui.bannermod.states.create"), btn -> openCreateDialog())
                .bounds(guiLeft + 8, guiTop + H - 24, 60, 18).build());
        this.renameButton = addRenderableWidget(Button.builder(text("gui.bannermod.states.rename"), btn -> openRenameDialog())
                .bounds(guiLeft + 72, guiTop + H - 24, 64, 18).build());
        this.setCapitalButton = addRenderableWidget(Button.builder(text("gui.bannermod.states.capital_here"), btn -> setCapitalHere())
                .bounds(guiLeft + 140, guiTop + H - 24, 84, 18).build());
        this.toggleFormButton = addRenderableWidget(Button.builder(text("gui.bannermod.states.toggle_form"), btn -> toggleGovernmentForm())
                .bounds(guiLeft + 228, guiTop + H - 24, 96, 18).build());
        // Second row of leader-only mutators (color / charter). Placed above the bottom row to
        // keep the spatial separation between "create / rename / capital / form" identity ops
        // and the "presentation" ops (color / charter) so the leader's eyes don't get tangled.
        this.setColorButton = addRenderableWidget(Button.builder(text("gui.bannermod.states.color"), btn -> openColorDialog())
                .bounds(guiLeft + 8, guiTop + H - 44, 60, 18).build());
        this.setCharterButton = addRenderableWidget(Button.builder(text("gui.bannermod.states.charter"), btn -> openCharterDialog())
                .bounds(guiLeft + 72, guiTop + H - 44, 80, 18).build());
        this.addCoLeaderButton = addRenderableWidget(Button.builder(text("gui.bannermod.states.add_co_leader"), btn -> openCoLeaderDialog(true))
                .bounds(guiLeft + 156, guiTop + H - 44, 96, 18).build());
        this.removeCoLeaderButton = addRenderableWidget(Button.builder(text("gui.bannermod.states.remove_co_leader"), btn -> openCoLeaderDialog(false))
                .bounds(guiLeft + 256, guiTop + H - 44, 86, 18).build());
        this.promoteStateButton = addRenderableWidget(Button.builder(text("gui.bannermod.states.promote_state"), btn -> promoteToState())
                .bounds(guiLeft + 346, guiTop + H - 44, 96, 18).build());
        addRenderableWidget(Button.builder(text("gui.bannermod.common.refresh"), btn -> refresh())
                .bounds(guiLeft + W - 172, guiTop + H - 64, 80, 18).build());
        addRenderableWidget(Button.builder(text("gui.bannermod.common.back"), btn -> onClose())
                .bounds(guiLeft + W - 86, guiTop + H - 24, 80, 18).build());
        refresh();
    }

    @Override
    public void tick() {
        super.tick();
        if (observedWarStateVersion != WarClientState.version()) {
            refresh();
        }
    }

    private void refresh() {
        this.entities = new ArrayList<>(WarClientState.entities());
        if (this.selected != null) {
            this.selected = WarClientState.entityById(this.selected.id());
        }
        this.scrollOffset = Math.max(0, Math.min(this.scrollOffset, Math.max(0, entities.size() - LIST_VISIBLE)));
        this.observedWarStateVersion = WarClientState.version();
        updateLeaderButtons();
    }

    private void openCreateDialog() {
        Minecraft.getInstance().setScreen(new PoliticalEntityNameInputScreen(
                this,
                text("gui.bannermod.states.dialog.create.title"),
                text("gui.bannermod.states.dialog.create.prompt"),
                "",
                this::sendCreate
        ));
    }

    private void openRenameDialog() {
        if (this.selected == null) return;
        UUID id = this.selected.id();
        Minecraft.getInstance().setScreen(new PoliticalEntityNameInputScreen(
                this,
                text("gui.bannermod.states.dialog.rename.title"),
                text("gui.bannermod.states.dialog.rename.prompt"),
                this.selected.name(),
                newName -> sendRename(id, newName)
        ));
    }

    private void setCapitalHere() {
        if (this.selected == null) return;
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageSetPoliticalEntityCapital(this.selected.id()));
    }

    private void toggleGovernmentForm() {
        if (this.selected == null) return;
        GovernmentForm next = this.selected.governmentForm() == GovernmentForm.MONARCHY
                ? GovernmentForm.REPUBLIC
                : GovernmentForm.MONARCHY;
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageSetGovernmentForm(this.selected.id(), next));
    }

    private void promoteToState() {
        if (selected == null) return;
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageSetPoliticalEntityStatus(selected.id(), PoliticalEntityStatus.STATE));
    }

    private void openColorDialog() {
        if (this.selected == null) return;
        UUID id = this.selected.id();
        // 9 chars: optional '#' + up to 8 hex digits (covers AARRGGBB).
        Minecraft.getInstance().setScreen(new PoliticalEntityNameInputScreen(
                this,
                text("gui.bannermod.states.dialog.color.title"),
                text("gui.bannermod.states.dialog.color.prompt"),
                this.selected.color(),
                value -> sendColor(id, value),
                9,
                /* allowEmpty */ true
        ));
    }

    private void openCharterDialog() {
        if (this.selected == null) return;
        UUID id = this.selected.id();
        Minecraft.getInstance().setScreen(new PoliticalEntityNameInputScreen(
                this,
                text("gui.bannermod.states.dialog.charter.title"),
                text("gui.bannermod.states.dialog.charter.prompt", PoliticalRegistryValidation.MAX_CHARTER_LENGTH),
                this.selected.charter(),
                value -> sendCharter(id, value),
                PoliticalRegistryValidation.MAX_CHARTER_LENGTH,
                /* allowEmpty */ true
        ));
    }

    private void openCoLeaderDialog(boolean add) {
        if (this.selected == null) return;
        UUID id = this.selected.id();
        Minecraft.getInstance().setScreen(new PoliticalEntityNameInputScreen(
                this,
                text(add ? "gui.bannermod.states.dialog.co_leader_add.title" : "gui.bannermod.states.dialog.co_leader_remove.title"),
                text("gui.bannermod.states.dialog.co_leader.prompt"),
                "",
                value -> sendCoLeader(id, value, add),
                36,
                false
        ));
    }

    private void sendColor(UUID entityId, String newColor) {
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageSetPoliticalEntityColor(entityId, newColor));
    }

    private void sendCharter(UUID entityId, String newCharter) {
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageSetPoliticalEntityCharter(entityId, newCharter));
    }

    private void sendCreate(String name) {
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageCreatePoliticalEntity(name));
    }

    private void sendRename(UUID entityId, String newName) {
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageRenamePoliticalEntity(entityId, newName));
    }

    private void sendCoLeader(UUID entityId, String coLeaderUuidText, boolean add) {
        try {
            BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageUpdateCoLeader(entityId, UUID.fromString(coLeaderUuidText), add));
        } catch (IllegalArgumentException ignored) {
            if (this.minecraft != null && this.minecraft.player != null) {
                this.minecraft.player.displayClientMessage(text("gui.bannermod.states.invalid_uuid"), false);
            }
        }
    }

    private void updateLeaderButtons() {
        boolean canAct = canLocalPlayerAct(this.selected);
        boolean leader = isLocalPlayerLeader(this.selected);
        Component selectState = text("gui.bannermod.states.tooltip.select_state");
        Component needAuthority = text("gui.bannermod.states.tooltip.need_authority");
        Component needLeader = text("gui.bannermod.states.tooltip.need_leader");
        if (this.renameButton != null) {
            this.renameButton.active = canAct;
            this.renameButton.setTooltip(canAct ? null : Tooltip.create(this.selected == null ? selectState : needAuthority));
        }
        if (this.setCapitalButton != null) {
            this.setCapitalButton.active = canAct;
            this.setCapitalButton.setTooltip(canAct ? null : Tooltip.create(this.selected == null ? selectState : needAuthority));
        }
        if (this.toggleFormButton != null) {
            this.toggleFormButton.active = leader;
            if (this.selected != null) {
                this.toggleFormButton.setMessage(text(this.selected.governmentForm() == GovernmentForm.MONARCHY
                        ? "gui.bannermod.states.to_republic"
                        : "gui.bannermod.states.to_monarchy"));
            }
            this.toggleFormButton.setTooltip(leader ? null : Tooltip.create(this.selected == null ? selectState : needLeader));
        }
        if (this.setColorButton != null) {
            this.setColorButton.active = canAct;
            this.setColorButton.setTooltip(canAct ? null : Tooltip.create(this.selected == null ? selectState : needAuthority));
        }
        if (this.setCharterButton != null) {
            this.setCharterButton.active = canAct;
            this.setCharterButton.setTooltip(canAct ? null : Tooltip.create(this.selected == null ? selectState : needAuthority));
        }
        if (this.addCoLeaderButton != null) {
            this.addCoLeaderButton.active = leader;
            this.addCoLeaderButton.setTooltip(leader ? null : Tooltip.create(this.selected == null ? selectState : needLeader));
        }
        if (this.removeCoLeaderButton != null) {
            this.removeCoLeaderButton.active = leader && this.selected != null && !this.selected.coLeaderUuids().isEmpty();
            Component tooltip = this.selected != null && this.selected.coLeaderUuids().isEmpty()
                    ? text("gui.bannermod.states.tooltip.no_co_leaders")
                    : (this.selected == null ? selectState : needLeader);
            this.removeCoLeaderButton.setTooltip(this.removeCoLeaderButton.active ? null : Tooltip.create(tooltip));
        }
        if (this.promoteStateButton != null) {
            boolean canPromote = canAct && this.selected != null && this.selected.status() != PoliticalEntityStatus.STATE;
            this.promoteStateButton.active = canPromote;
            this.promoteStateButton.setTooltip(canPromote ? Tooltip.create(text("gui.bannermod.states.tooltip.promote_state")) : Tooltip.create(this.selected == null ? selectState : needAuthority));
        }
    }

    private static boolean isLocalPlayerLeader(@Nullable PoliticalEntityRecord entity) {
        if (entity == null) return false;
        Player player = Minecraft.getInstance().player;
        if (player == null) return false;
        UUID leader = entity.leaderUuid();
        return leader != null && leader.equals(player.getUUID());
    }

    private static boolean canLocalPlayerAct(@Nullable PoliticalEntityRecord entity) {
        Player player = Minecraft.getInstance().player;
        return player != null && PoliticalEntityAuthority.canAct(player.getUUID(), false, entity);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fill(guiLeft, guiTop, guiLeft + W, guiTop + H, 0xC0101010);
        graphics.renderOutline(guiLeft, guiTop, W, H, 0xFFFFFFFF);
        graphics.drawCenteredString(font, text("gui.bannermod.states.heading").getString(), guiLeft + W / 2, guiTop + 7, 0xFFFFFF);
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
            graphics.drawString(font, localizedStatus(entity.status()), listX + 4, rowY + 4, statusColor(entity.status()), false);
            graphics.drawString(font, font.plainSubstrByWidth(" " + displayName(entity), listW - 76), listX + 76, rowY + 4, 0xFFFFFF, false);
        }
        if (entities.isEmpty()) {
            String empty = text(WarClientState.hasSnapshot()
                    ? "gui.bannermod.states.empty"
                    : "gui.bannermod.states.waiting_sync").getString();
            graphics.drawCenteredString(font, empty, listX + listW / 2, listY + 62, 0xAAAAAA);
        }
    }

    private void renderDetails(GuiGraphics graphics) {
        int x = guiLeft + 174;
        int y = guiTop + 28;
        int w = W - 182;
        graphics.drawString(font, text("gui.bannermod.states.detail"), x, y, 0xFFFFFF, false);
        if (selected == null) {
            graphics.drawString(font, text("gui.bannermod.states.select_state"), x, y + 14, 0xAAAAAA, false);
            graphics.drawString(font, text("gui.bannermod.states.help.settlement"), x, y + 30, 0xAAAAAA, false);
            graphics.drawString(font, text("gui.bannermod.states.help.claim"), x, y + 42, 0xAAAAAA, false);
            graphics.drawString(font, text("gui.bannermod.states.help.state"), x, y + 54, 0xAAAAAA, false);
            return;
        }
        String[] lines = {
                text("gui.bannermod.states.detail.name", displayName(selected)).getString(),
                text("gui.bannermod.states.detail.status", localizedStatus(selected.status())).getString(),
                text("gui.bannermod.states.detail.government", localizedGovernmentForm(selected.governmentForm()), text(selected.governmentForm().coLeadersShareAuthority() ? "gui.bannermod.states.authority.shared" : "gui.bannermod.states.authority.leader_only")).getString(),
                text("gui.bannermod.states.detail.leader", playerName(selected.leaderUuid())).getString(),
                text("gui.bannermod.states.detail.co_leaders", coLeaderSummary(selected)).getString(),
                text("gui.bannermod.states.detail.co_leader_authority", text(selected.governmentForm().coLeadersShareAuthority() ? "gui.bannermod.states.co_authority.active" : "gui.bannermod.states.co_authority.locked").getString()).getString(),
                text("gui.bannermod.states.detail.capital", selected.capitalPos() == null ? text("gui.bannermod.common.none").getString() : selected.capitalPos().toShortString()).getString(),
                text("gui.bannermod.states.detail.color", selected.color().isBlank() ? text("gui.bannermod.common.none").getString() : selected.color()).getString(),
                text("gui.bannermod.states.detail.region", selected.homeRegion().isBlank() ? text("gui.bannermod.common.none").getString() : selected.homeRegion()).getString(),
                text("gui.bannermod.states.detail.wars", involvedWarCount(selected)).getString()
        };
        for (int i = 0; i < lines.length; i++) {
            graphics.drawString(font, font.plainSubstrByWidth(lines[i], w), x, y + 14 + i * 12, 0xFFFFFF, false);
        }
        int reqY = y + 14 + lines.length * 12 + 6;
        graphics.drawString(font, font.plainSubstrByWidth(text("gui.bannermod.states.progression.requirements").getString(), w), x, reqY, 0xFFD77A, false);
        graphics.drawString(font, font.plainSubstrByWidth(text("gui.bannermod.states.progression.requirement_fort").getString(), w), x, reqY + 12, 0xAAAAAA, false);
        graphics.drawString(font, font.plainSubstrByWidth(text("gui.bannermod.states.progression.requirement_storage").getString(), w), x, reqY + 24, 0xAAAAAA, false);
        graphics.drawString(font, font.plainSubstrByWidth(text("gui.bannermod.states.progression.requirement_market").getString(), w), x, reqY + 36, 0xAAAAAA, false);
        graphics.drawString(font, font.plainSubstrByWidth(text("gui.bannermod.states.progression.server_checked").getString(), w), x, reqY + 48, 0x888888, false);
    }

    private static Component text(String key, Object... args) {
        return Component.translatable(key, args);
    }

    private static Component localizedGovernmentForm(GovernmentForm form) {
        return switch (form) {
            case MONARCHY -> text("gui.bannermod.states.government.monarchy");
            case REPUBLIC -> text("gui.bannermod.states.government.republic");
        };
    }

    private static Component localizedStatus(PoliticalEntityStatus status) {
        return switch (status) {
            case SETTLEMENT -> text("gui.bannermod.states.status.settlement");
            case STATE -> text("gui.bannermod.states.status.state");
            case VASSAL -> text("gui.bannermod.states.status.vassal");
            case PEACEFUL -> text("gui.bannermod.states.status.peaceful");
        };
    }

    private String coLeaderSummary(PoliticalEntityRecord entity) {
        if (entity.coLeaderUuids().isEmpty()) {
            return text("gui.bannermod.common.none").getString();
        }
        List<String> names = new ArrayList<>();
        for (int i = 0; i < Math.min(3, entity.coLeaderUuids().size()); i++) {
            names.add(playerName(entity.coLeaderUuids().get(i)));
        }
        String suffix = entity.coLeaderUuids().size() > names.size() ? " +" + (entity.coLeaderUuids().size() - names.size()) : "";
        return String.join(", ", names) + suffix;
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
        return entity.name().isBlank() ? text("gui.bannermod.states.unnamed").getString() : entity.name();
    }

    private static String playerName(@Nullable UUID id) {
        if (id == null) {
            return text("gui.bannermod.common.unknown").getString();
        }
        String name = GameProfileUtils.getPlayerName(id);
        return name == null || name.isBlank() ? text("gui.bannermod.common.unknown").getString() : name;
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
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double delta) {
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
