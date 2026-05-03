package com.talhanation.bannermod.client.military.gui.worldmap;

import com.mojang.blaze3d.systems.RenderSystem;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.client.military.ClientManager;
import com.talhanation.bannermod.client.military.gui.MilitaryGuiStyle;
import com.talhanation.bannermod.client.military.gui.RecruitsScreenBase;
import com.talhanation.bannermod.client.military.gui.player.PlayersList;
import com.talhanation.bannermod.client.military.gui.player.SelectPlayerScreen;
import com.talhanation.bannermod.client.military.gui.widgets.DropDownMenu;
import com.talhanation.bannermod.client.military.gui.widgets.RecruitsCheckBox;
import com.talhanation.bannermod.client.military.gui.widgets.SelectedPlayerWidget;
import com.talhanation.bannermod.network.messages.military.MessageClaimIntent;
import com.talhanation.bannermod.network.messages.military.MessageReassignClaimPoliticalEntity;
import com.talhanation.bannermod.network.messages.military.MessageUpdateClaim;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.persistence.military.RecruitsPlayerInfo;
import com.talhanation.bannermod.war.client.WarClientState;
import com.talhanation.bannermod.war.registry.PoliticalEntityAuthority;
import com.talhanation.bannermod.war.registry.PoliticalEntityRecord;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.EditBox;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.ConfirmScreen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.ChunkPos;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;
import javax.annotation.Nullable;

public class ClaimEditScreen extends RecruitsScreenBase {
    private static final Component TITLE = Component.translatable("gui.recruits.claim_edit.title");
    private static final Component BUTTON_SAVE = Component.translatable("gui.recruits.button.save");
    protected static final Component BUTTON_BACK = Component.translatable("gui.recruits.button.back");
    private static final Component BUTTON_DELETE = Component.translatable("gui.bannermod.map.claim.delete");
    private static final Component DELETE_CONFIRM_TITLE = Component.translatable("gui.bannermod.map.claim.delete.confirm.title");
    private static final Component DELETE_CONFIRM_BODY = Component.translatable("gui.bannermod.map.claim.delete.confirm.body");
    protected static final Component CHECKBOX_ALLOW_BREAKING = Component.translatable("gui.recruits.checkbox.allowBlockBreaking");
    protected static final Component CHECKBOX_ALLOW_PLACING = Component.translatable("gui.recruits.checkbox.allowBlockPlacing");
    protected static final Component CHECKBOX_ALLOW_INTERACTING = Component.translatable("gui.recruits.checkbox.allowBlockInteracting");
    private final RecruitsClaim claim;
    private final Player player;

    private EditBox editNameBox;
    private boolean allowBlockBreaking;
    private boolean allowBlockPlacing;
    private boolean allowBlockInteracting;
    private RecruitsCheckBox blockBreakingCheckBox;
    private RecruitsCheckBox blockPlacingCheckBox;
    private RecruitsCheckBox blockInteractionCheckBox;
    private SelectedPlayerWidget selectedPlayerWidget;
    private RecruitsPlayerInfo playerInfo;
    private Button saveButton;
    private Button backButton;
    private Button membersButton;
    private Button deleteButton;
    private Button transferButton;
    private DropDownMenu<TransferOption> transferDropdown;
    private final List<TransferOption> transferOptions = new ArrayList<>();
    private TransferOption selectedTransferOption;
    private String claimName;
    private String savedName;
    private WorldMapScreen parent;

    /** Choice row for the transfer-to-state dropdown. {@code id == null} means detach. */
    public record TransferOption(@Nullable UUID id, Component label) {}
    public ClaimEditScreen(WorldMapScreen screen, RecruitsClaim claim, Player player) {
        super(TITLE, 1,1);
        this.parent = screen;
        this.claim = claim;
        this.player = player;
        this.playerInfo = claim.getPlayerInfo();
    }

    public int x;
    public int y;
    @Override
    protected void init() {
        this.allowBlockBreaking = claim.isBlockBreakingAllowed();
        this.allowBlockPlacing = claim.isBlockPlacementAllowed();
        this.allowBlockInteracting = claim.isBlockInteractionAllowed();

        if(savedName == null) savedName = claim.getName();
        x = this.width / 2;
        y = this.height / 2;

        setWidgets();
    }

    private void setWidgets(){
        clearWidgets();
        editNameBox = new EditBox(font, x - 70, y - 110, 140, 20, Component.literal(""));
        editNameBox.setTextColor(MilitaryGuiStyle.TEXT_DARK);
        editNameBox.setTextColorUneditable(MilitaryGuiStyle.TEXT_DARK);
        editNameBox.setBordered(true);
        editNameBox.setMaxLength(32);
        editNameBox.setValue(savedName);
        editNameBox.setResponder(this::onTextInput);
        this.addRenderableWidget(editNameBox);
        if(playerInfo != null){
            selectedPlayerWidget = new SelectedPlayerWidget(font, x - 70, y - 87, 140, 20, Component.literal("x"),
                    () -> {
                        playerInfo = null;
                        this.selectedPlayerWidget.setPlayer(null, null);

                        setWidgets();
                    }
            );
            selectedPlayerWidget.setPlayer(claim.playerInfo.getUUID(), claim.playerInfo.getName());
            this.addRenderableWidget(selectedPlayerWidget);
        }
        else{
            Button selectPlayerButton = addRenderableWidget(new ExtendedButton(x - 70, y - 87, 140, 20, SelectPlayerScreen.TITLE,
                    button -> {
                        minecraft.setScreen(new SelectPlayerScreen(this, player, SelectPlayerScreen.TITLE, SelectPlayerScreen.BUTTON_SELECT, SelectPlayerScreen.BUTTON_SELECT_TOOLTIP, true, PlayersList.FilterType.ANY_TEAM,
                                (playerInfo) -> {
                                    this.playerInfo = playerInfo;
                                    this.claim.setPlayer(playerInfo);
                                    minecraft.setScreen(this);
                                }
                        ));
                    }
            ));
            this.addRenderableWidget(selectPlayerButton);
        }

        int checkBoxWidth = 140;
        int checkBoxHeight = 20;

        this.blockPlacingCheckBox = new RecruitsCheckBox(x - 70, y + 20, checkBoxWidth, checkBoxHeight, CHECKBOX_ALLOW_PLACING,
                this.allowBlockPlacing,
                (bool) -> {
                    this.allowBlockPlacing = bool;
                }
        );
        this.addRenderableWidget(blockPlacingCheckBox);


        this.blockBreakingCheckBox = new RecruitsCheckBox(x - 70, y + 40, checkBoxWidth, checkBoxHeight, CHECKBOX_ALLOW_BREAKING,
                this.allowBlockBreaking,
                (bool) -> {
                    this.allowBlockBreaking = bool;
                }
        );
        this.addRenderableWidget(blockBreakingCheckBox);


        this.blockInteractionCheckBox = new RecruitsCheckBox(x - 70, y + 60, checkBoxWidth, checkBoxHeight, CHECKBOX_ALLOW_INTERACTING,
                this.allowBlockInteracting,
                (bool) -> {
                    this.allowBlockInteracting = bool;
                }
        );
        addRenderableWidget(blockInteractionCheckBox);

        membersButton = new ExtendedButton(x - 70, y + 90, 140, 20,
                Component.translatable("gui.bannermod.claim.members.button"),
                button -> this.minecraft.setScreen(new ClaimTrustedMembersScreen(this, this.claim, this.player)));
        addRenderableWidget(membersButton);

        backButton = new ExtendedButton(x + 5, y + 114, 70, 20, BUTTON_BACK,
                button -> {
                    this.minecraft.setScreen(this.parent);
                });
        addRenderableWidget(backButton);

        saveButton = new ExtendedButton(x - 75, y + 114, 70, 20, BUTTON_SAVE,
                button -> {
                    this.claim.setName(editNameBox.getValue());
                    this.claim.setPlayer(playerInfo);
                    this.claim.setBlockInteractionAllowed(this.allowBlockInteracting);
                    this.claim.setBlockPlacementAllowed(this.allowBlockPlacing);
                     this.claim.setBlockBreakingAllowed(this.allowBlockBreaking);

                     BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageUpdateClaim(this.claim));
                     ClientManager.markClaimsStale();
                     this.parent.showMapNotice(Component.translatable("gui.bannermod.claim.feedback.save_sent"), 0xFFFFD36A);

                     this.minecraft.setScreen(this.parent);
                 });
        addRenderableWidget(saveButton);
        this.checkSaveActive();

        // WORLDMAPCLAIMPE-001 transfer-to-state row: label + dropdown + button.
        // The dropdown is added LAST so its open list renders above the delete
        // button below it. Server still re-validates authority on both source
        // and target PE in MessageReassignClaimPoliticalEntity.
        rebuildTransferOptions();

        transferButton = new ExtendedButton(x - 70, y + 192, 140, 20,
                Component.translatable("gui.bannermod.claim.transfer.button"),
                button -> openTransferConfirm());
        transferButton.setFGColor(MilitaryGuiStyle.TEXT_DENIED);
        addRenderableWidget(transferButton);

        // Restored delete button: opens vanilla ConfirmScreen, then routes through
        // MessageClaimIntent.Action.DELETE so the server stays authoritative
        // (auth + chunk cleanup are server-side; client just signals intent).
        // Plain ExtendedButton with destructive label tint via ChatFormatting.RED;
        // this keeps the button at the same chrome as save/back without needing
        // to subclass for a foreground-color override.
        deleteButton = new ExtendedButton(x - 70, y + 216, 140, 20, BUTTON_DELETE,
                button -> openDeleteConfirm());
        deleteButton.setFGColor(MilitaryGuiStyle.TEXT_DENIED);
        addRenderableWidget(deleteButton);

        rebuildTransferDropdown();
        updateTransferButtonState();
    }

    private void rebuildTransferOptions() {
        transferOptions.clear();
        Player local = Minecraft.getInstance().player;
        UUID actorId = local == null ? null : local.getUUID();
        UUID currentOwner = claim.getOwnerPoliticalEntityId();
        boolean detached = currentOwner == null;
        // Detach option: only useful when claim already detached (no-op shows
        // we kept that as a label); the server still rejects non-admin detach.
        if (detached) {
            transferOptions.add(new TransferOption(null,
                    Component.translatable("gui.bannermod.claim.transfer.option.detach")));
        }
        for (PoliticalEntityRecord record : WarClientState.entities()) {
            if (record == null || record.id() == null) continue;
            if (Objects.equals(record.id(), currentOwner)) continue; // skip current owner
            if (actorId != null && PoliticalEntityAuthority.canAct(actorId, false, record)) {
                String clamped = MilitaryGuiStyle.clampLabel(font, record.name(), Math.max(8, 140 - 16));
                transferOptions.add(new TransferOption(record.id(), Component.literal(clamped)));
            }
        }
        selectedTransferOption = transferOptions.isEmpty() ? null : transferOptions.get(0);
    }

    private void rebuildTransferDropdown() {
        if (transferDropdown != null) {
            removeWidget(transferDropdown);
        }
        if (transferOptions.isEmpty()) {
            transferDropdown = null;
            return;
        }
        transferDropdown = new DropDownMenu<>(
                selectedTransferOption,
                x - 70, y + 168, 140, 14,
                transferOptions,
                opt -> opt == null ? "" : opt.label().getString(),
                opt -> {
                    selectedTransferOption = opt;
                    updateTransferButtonState();
                });
        addRenderableWidget(transferDropdown);
    }

    private void updateTransferButtonState() {
        if (transferButton == null) return;
        Player local = Minecraft.getInstance().player;
        UUID actorId = local == null ? null : local.getUUID();
        boolean hasSourceAuthority = hasSourceTransferAuthority(actorId);
        if (!hasSourceAuthority) {
            transferButton.active = false;
            transferButton.setTooltip(Tooltip.create(Component.translatable(
                    "gui.bannermod.claim.transfer.disabled.no_source_authority")));
            return;
        }
        if (transferOptions.isEmpty() || selectedTransferOption == null) {
            transferButton.active = false;
            transferButton.setTooltip(Tooltip.create(Component.translatable(
                    "gui.bannermod.claim.transfer.disabled.no_targets")));
            return;
        }
        transferButton.active = true;
        transferButton.setTooltip(null);
    }

    private boolean hasSourceTransferAuthority(@Nullable UUID actorId) {
        if (actorId == null) return false;
        UUID currentOwner = claim.getOwnerPoliticalEntityId();
        if (currentOwner == null) {
            // Detached claim — only the original owner player or admin should
            // see the action live; the server still re-validates and admin
            // bypass works there. Keep UI permissive: if there's a stored
            // playerInfo and it's the local player, allow.
            return claim.getPlayerInfo() != null && actorId.equals(claim.getPlayerInfo().getUUID());
        }
        for (PoliticalEntityRecord record : WarClientState.entities()) {
            if (record != null && Objects.equals(record.id(), currentOwner)) {
                if (PoliticalEntityAuthority.canAct(actorId, false, record)) return true;
                break;
            }
        }
        // Owner-player fallback: ClaimPacketAuthority lets the registered
        // playerInfo edit the claim regardless of PE.
        return claim.getPlayerInfo() != null && actorId.equals(claim.getPlayerInfo().getUUID());
    }

    private void openTransferConfirm() {
        Minecraft mc = this.minecraft;
        if (mc == null || selectedTransferOption == null) return;
        TransferOption target = selectedTransferOption;
        String currentName = currentOwnerDisplayName();
        String targetName = target.label().getString();
        Component body = Component.translatable("gui.bannermod.claim.transfer.confirm.body",
                claim.getName(),
                targetName);
        // currentName referenced for narration completeness; kept as part of body builder if needed later.
        if (!currentName.isEmpty()) {
            // no-op — body already mentions claim + target; current PE shown as title context.
        }
        mc.setScreen(new ConfirmScreen(confirmed -> {
            if (confirmed) {
                BannerModMain.SIMPLE_CHANNEL.sendToServer(
                        new MessageReassignClaimPoliticalEntity(claim.getUUID(), target.id()));
                ClientManager.markClaimsStale();
                mc.setScreen(this.parent);
            } else {
                mc.setScreen(this);
            }
        },
                Component.translatable("gui.bannermod.claim.transfer.confirm.title"),
                body));
    }

    private String currentOwnerDisplayName() {
        UUID currentOwner = claim.getOwnerPoliticalEntityId();
        if (currentOwner == null) {
            return Component.translatable("gui.bannermod.claim.transfer.option.detach").getString();
        }
        for (PoliticalEntityRecord record : WarClientState.entities()) {
            if (record != null && Objects.equals(record.id(), currentOwner)) {
                return record.name();
            }
        }
        return "";
    }

    private void openDeleteConfirm() {
        Minecraft mc = this.minecraft;
        if (mc == null) return;
        mc.setScreen(new ConfirmScreen(confirmed -> {
            if (confirmed) {
                ClientManager.recruitsClaims.remove(this.claim);
                ClientManager.markClaimsStale();
                // DELETE ignores the chunk on the server — pass center if known, else
                // the first claimed chunk, else origin (0,0) as a benign placeholder.
                ChunkPos anchor = this.claim.getCenter();
                if (anchor == null) {
                    anchor = this.claim.getClaimedChunks().isEmpty()
                            ? new ChunkPos(0, 0)
                            : this.claim.getClaimedChunks().get(0);
                }
                BannerModMain.SIMPLE_CHANNEL.sendToServer(
                        new MessageClaimIntent(MessageClaimIntent.Action.DELETE, this.claim.getUUID(), anchor));
                mc.setScreen(this.parent);
            } else {
                mc.setScreen(this);
            }
        }, DELETE_CONFIRM_TITLE, DELETE_CONFIRM_BODY));
    }

    public void checkSaveActive(){
        this.saveButton.active = playerInfo != null;
        this.saveButton.setTooltip(this.saveButton.active ? null : Tooltip.create(Component.translatable("gui.bannermod.claim.disabled.no_owner")));
    }

    private void onTextInput(String string) {
        this.savedName = string;
    }
    int panelWidth = 150;
    int panelHeight = 360;
    int panelX = -75;
    int panelY = -140;
    int claimMiniX = -70;
    int claimMiniY = -60;

    @Override
    public void renderBackground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.renderBackground(guiGraphics, mouseX, mouseY, delta);
        WorldMapRenderPrimitives.panel(guiGraphics, panelX + x, panelY + y, panelWidth, panelHeight);
        guiGraphics.fill(panelX + x + 1, panelY + y + 18, panelX + x + panelWidth - 1, panelY + y + 19, 0x665A4025);
    }

    @Override
    public void renderForeground(GuiGraphics guiGraphics, int mouseX, int mouseY, float delta) {
        super.renderForeground(guiGraphics, mouseX, mouseY, delta);

        Component status;
        int color;
        if (ClientManager.claimsSnapshotStale) {
            status = Component.translatable("gui.bannermod.claim.state.stale");
            color = 0xFFFFD36A;
        } else if (claim.getClaimedChunks().isEmpty()) {
            status = Component.translatable("gui.bannermod.claim.state.empty");
            color = 0xFF8FA8FF;
        } else {
            status = Component.translatable("gui.bannermod.claim.state.ready", claim.getClaimedChunks().size());
            color = FONT_COLOR;
        }
        guiGraphics.drawString(font, status, x - 70, y - 130, color, false);
        guiGraphics.drawString(font, Component.translatable("gui.bannermod.claim.player"), x - 70, y - 100, 0xFFE0B86A, false);
        guiGraphics.drawString(font, Component.translatable("gui.bannermod.claim").copy().append(":"), x - 70, y - 72, 0xFFE0B86A, false);
        guiGraphics.drawString(font, Component.translatable("gui.bannermod.claim.diplomacy"), x - 70, y + 4, 0xFFE0B86A, false);
        if (playerInfo == null) {
            guiGraphics.drawWordWrap(font, Component.translatable("gui.bannermod.claim.disabled.no_owner"), x - 70, y + 92, 140, 0xFFFFD36A);
        }
        // Transfer-to-state row label drawn in muted-text colour to read as a section header.
        guiGraphics.drawString(font, Component.translatable("gui.bannermod.claim.transfer.label"),
                x - 70, y + 156, MilitaryGuiStyle.TEXT_MUTED, false);

        renderClaimMiniMapAreaFramed(guiGraphics, claimMiniX + x, claimMiniY + y , 140, 70, this.claim);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        // Drive the dropdown explicitly — DropDownMenu.onClick is intentionally
        // inert, so option clicks must route through onMouseClick.
        if (this.transferDropdown != null && this.transferDropdown.active && this.transferDropdown.isMouseOver(mouseX, mouseY)) {
            this.transferDropdown.onMouseClick(mouseX, mouseY);
            return true;
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public void mouseMoved(double mouseX, double mouseY) {
        if (this.transferDropdown != null) this.transferDropdown.onMouseMove(mouseX, mouseY);
        super.mouseMoved(mouseX, mouseY);
    }

    private void renderClaimMiniMapAreaFramed(GuiGraphics guiGraphics, int x, int y, int width, int height, RecruitsClaim claim) {
        List<ChunkPos> chunks = claim.getClaimedChunks();
        if (chunks.isEmpty()) return;
        if (claim.getOwnerPoliticalEntityId() == null) return;

        Minecraft mc = Minecraft.getInstance();
        Font font = mc.font;

        // Chunk-Grenzen bestimmen
        int minX = chunks.stream().mapToInt(c -> c.x).min().orElse(0);
        int maxX = chunks.stream().mapToInt(c -> c.x).max().orElse(0);
        int minZ = chunks.stream().mapToInt(c -> c.z).min().orElse(0);
        int maxZ = chunks.stream().mapToInt(c -> c.z).max().orElse(0);

        int chunkWidth = maxX - minX + 1;
        int chunkHeight = maxZ - minZ + 1;

        // Zelle pro Chunk berechnen (Skalierung)
        float scaleX = (float) width / chunkWidth;
        float scaleZ = (float) height / chunkHeight;
        float cellSize = Math.min(scaleX, scaleZ);

        // Offset zur Zentrierung berechnen
        float usedWidth = chunkWidth * cellSize;
        float usedHeight = chunkHeight * cellSize;
        float offsetX = x + (width - usedWidth) / 2f;
        float offsetY = y + (height - usedHeight) / 2f;

        // Scissor aktivieren
        int scale = (int) mc.getWindow().getGuiScale();
        int screenHeight = mc.getWindow().getHeight();
        RenderSystem.enableScissor(x * scale, screenHeight - (y + height) * scale, width * scale, height * scale);

        // Farbwerte vorbereiten
        int alpha = 190;
        int rgb = 0x6699CC;
        int r = (rgb >> 16) & 0xFF;
        int g = (rgb >> 8) & 0xFF;
        int b = rgb & 0xFF;
        int argb = (alpha << 24) | (r << 16) | (g << 8) | b;

        // Zeichnen
        for (ChunkPos pos : chunks) {
            float px = offsetX + (pos.x - minX) * cellSize;
            float py = offsetY + (pos.z - minZ) * cellSize;

            guiGraphics.fill((int) px, (int) py, (int) (px + cellSize), (int) (py + cellSize), argb);

            // Ränder (Grenzen)
            ChunkPos[] dirs = {
                    new ChunkPos(pos.x, pos.z - 1),
                    new ChunkPos(pos.x, pos.z + 1),
                    new ChunkPos(pos.x - 1, pos.z),
                    new ChunkPos(pos.x + 1, pos.z)
            };
            boolean top = !claim.containsChunk(dirs[0]);
            boolean bottom = !claim.containsChunk(dirs[1]);
            boolean left = !claim.containsChunk(dirs[2]);
            boolean right = !claim.containsChunk(dirs[3]);

            int borderColor = 0xFFFFFFFF;
            if (top)    guiGraphics.fill((int) px, (int) py, (int) (px + cellSize), (int) py + 1, borderColor);
            if (bottom) guiGraphics.fill((int) px, (int) (py + cellSize - 1), (int) (px + cellSize), (int) (py + cellSize), borderColor);
            if (left)   guiGraphics.fill((int) px, (int) py, (int) px + 1, (int) (py + cellSize), borderColor);
            if (right)  guiGraphics.fill((int) (px + cellSize - 1), (int) py, (int) (px + cellSize), (int) (py + cellSize), borderColor);
        }

        // Claim-Name über Zentrum
        ChunkPos center = claim.getCenter();
        float cx = offsetX + (center.x - minX + 0.5f) * cellSize;
        float cz = offsetY + (center.z - minZ + 0.5f) * cellSize;

        int textWidth = font.width(claim.getName());
        guiGraphics.drawString(font, claim.getName(), (int) (cx - textWidth / 2f), (int) (cz - 6), 0xFFFFFF, false);

        RenderSystem.disableScissor();
    }
}
