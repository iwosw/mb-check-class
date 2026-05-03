package com.talhanation.bannermod.client.military.gui.worldmap;

import com.mojang.blaze3d.systems.RenderSystem;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.client.military.ClientManager;
import com.talhanation.bannermod.client.military.gui.MilitaryGuiStyle;
import com.talhanation.bannermod.client.military.gui.RecruitsScreenBase;
import com.talhanation.bannermod.client.military.gui.player.PlayersList;
import com.talhanation.bannermod.client.military.gui.player.SelectPlayerScreen;
import com.talhanation.bannermod.client.military.gui.widgets.RecruitsCheckBox;
import com.talhanation.bannermod.client.military.gui.widgets.SelectedPlayerWidget;
import com.talhanation.bannermod.network.messages.military.MessageClaimIntent;
import com.talhanation.bannermod.network.messages.military.MessageUpdateClaim;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.persistence.military.RecruitsPlayerInfo;
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

import java.util.List;

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
    private String claimName;
    private String savedName;
    private WorldMapScreen parent;
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

        // Restored delete button: opens vanilla ConfirmScreen, then routes through
        // MessageClaimIntent.Action.DELETE so the server stays authoritative
        // (auth + chunk cleanup are server-side; client just signals intent).
        // Plain ExtendedButton with destructive label tint via ChatFormatting.RED;
        // this keeps the button at the same chrome as save/back without needing
        // to subclass for a foreground-color override.
        deleteButton = new ExtendedButton(x - 70, y + 138, 140, 20, BUTTON_DELETE,
                button -> openDeleteConfirm());
        deleteButton.setFGColor(MilitaryGuiStyle.TEXT_DENIED);
        addRenderableWidget(deleteButton);

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
    int panelHeight = 224;
    int panelX = -75;
    int panelY = -115;
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

        renderClaimMiniMapAreaFramed(guiGraphics, claimMiniX + x, claimMiniY + y , 140, 70, this.claim);
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
