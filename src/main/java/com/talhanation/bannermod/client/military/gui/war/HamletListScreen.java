package com.talhanation.bannermod.client.military.gui.war;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.client.military.gui.MilitaryGuiStyle;
import com.talhanation.bannermod.network.messages.civilian.MessageRegisterHamlet;
import com.talhanation.bannermod.network.messages.civilian.MessageRenameHamlet;
import com.talhanation.bannermod.network.messages.civilian.MessageRequestHamletSnapshot;
import com.talhanation.bannermod.society.NpcHamletHouseholdEntry;
import com.talhanation.bannermod.society.NpcHamletRecord;
import com.talhanation.bannermod.society.NpcHamletStatus;
import com.talhanation.bannermod.society.client.NpcHamletClientState;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.UUID;

public class HamletListScreen extends Screen {
    private static final int MIN_BOOK_W = 392;
    private static final int MAX_BOOK_W = 760;
    private static final int MIN_BOOK_H = 220;
    private static final int MAX_BOOK_H = 520;
    private static final int ROW_H = 18;
    private static final int BUTTON_H = 18;
    private static final int BOOK_BORDER = 10;
    private static final int PAGE_SHADE = 0xFFE0BC78;
    private static final int LEATHER = 0xFF4A2D18;
    private static final int LEATHER_DARK = 0xFF24150D;
    private static final int INK = 0xFF2D1B0F;
    private static final int INK_MUTED = 0xFF6C5030;
    private static final int GOLD = 0xFFFFD36A;
    private static final int WAX = 0xFF8E2E24;

    private final Screen parent;
    private int guiLeft;
    private int guiTop;
    private int guiW;
    private int guiH;
    private int listVisible = 8;
    private int scrollOffset;
    private int observedVersion = -1;
    private List<NpcHamletRecord> hamlets = List.of();
    @Nullable
    private NpcHamletRecord selected;

    private Button registerBtn;
    private Button renameBtn;
    private Button refreshBtn;
    private Button backBtn;

    public HamletListScreen(@Nullable Screen parent) {
        super(text("gui.bannermod.hamlets.title"));
        this.parent = parent;
    }

    @Override
    protected void init() {
        super.init();
        updateGeometry();
        this.registerBtn = actionButton(0, text("gui.bannermod.hamlets.action.register"), btn -> registerSelected());
        this.renameBtn = actionButton(1, text("gui.bannermod.hamlets.action.rename"), btn -> renameSelected());
        this.refreshBtn = actionButton(2, text("gui.bannermod.common.refresh"), btn -> requestSnapshot());
        this.backBtn = actionButton(3, text("gui.bannermod.common.back"), btn -> onClose());
        addRenderableWidget(this.registerBtn);
        addRenderableWidget(this.renameBtn);
        addRenderableWidget(this.refreshBtn);
        addRenderableWidget(this.backBtn);
        requestSnapshot();
    }

    private void requestSnapshot() {
        NpcHamletClientState.beginSync();
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageRequestHamletSnapshot());
        refreshLocal();
    }

    private void refreshLocal() {
        this.hamlets = new ArrayList<>(NpcHamletClientState.hamlets());
        this.hamlets.sort(Comparator
                .comparingInt((NpcHamletRecord hamlet) -> hamletSeverity(hamlet.status()))
                .thenComparing(hamlet -> hamlet.name().toLowerCase(Locale.ROOT)));
        if (this.selected != null) {
            this.selected = this.hamlets.stream().filter(hamlet -> hamlet.hamletId().equals(this.selected.hamletId())).findFirst().orElse(null);
        }
        this.scrollOffset = clamp(this.scrollOffset, 0, Math.max(0, this.hamlets.size() - this.listVisible));
        this.observedVersion = NpcHamletClientState.version();
        updateButtons();
    }

    private void registerSelected() {
        if (this.selected == null) {
            return;
        }
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageRegisterHamlet(this.selected.hamletId()));
    }

    private void renameSelected() {
        if (this.selected == null) {
            return;
        }
        UUID hamletId = this.selected.hamletId();
        Minecraft.getInstance().setScreen(new PoliticalEntityNameInputScreen(
                this,
                text("gui.bannermod.hamlets.rename.title"),
                text("gui.bannermod.hamlets.rename.prompt"),
                this.selected.name(),
                newName -> BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageRenameHamlet(hamletId, newName))
        ));
    }

    private Button actionButton(int index, Component label, Button.OnPress onPress) {
        return new MedievalButton(actionButtonX(index), actionButtonY(index), actionButtonW(), BUTTON_H, label, onPress);
    }

    private void updateButtons() {
        boolean hasSelection = this.selected != null;
        boolean canManage = NpcHamletClientState.canManage();
        this.registerBtn.active = hasSelection && canManage && this.selected.status() == NpcHamletStatus.INFORMAL;
        this.renameBtn.active = hasSelection && canManage;
        this.registerBtn.setTooltip(registerTooltip(hasSelection, canManage));
        this.renameBtn.setTooltip(renameTooltip(hasSelection, canManage));
    }

    private @Nullable Tooltip registerTooltip(boolean hasSelection, boolean canManage) {
        if (this.registerBtn.active) {
            return null;
        }
        if (!hasSelection) {
            return Tooltip.create(text("gui.bannermod.hamlets.tooltip.select_hamlet"));
        }
        if (!canManage) {
            return Tooltip.create(readOnlyReason());
        }
        if (this.selected != null && this.selected.status() == NpcHamletStatus.REGISTERED) {
            return Tooltip.create(text("gui.bannermod.hamlets.tooltip.already_registered"));
        }
        return Tooltip.create(text("gui.bannermod.hamlets.tooltip.unavailable"));
    }

    private @Nullable Tooltip renameTooltip(boolean hasSelection, boolean canManage) {
        if (this.renameBtn.active) {
            return null;
        }
        if (!hasSelection) {
            return Tooltip.create(text("gui.bannermod.hamlets.tooltip.select_hamlet"));
        }
        if (!canManage) {
            return Tooltip.create(readOnlyReason());
        }
        return Tooltip.create(text("gui.bannermod.hamlets.tooltip.unavailable"));
    }

    @Override
    public void tick() {
        super.tick();
        if (this.observedVersion != NpcHamletClientState.version()) {
            refreshLocal();
        }
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        super.renderBackground(graphics, mouseX, mouseY, partialTick);
        graphics.fill(0, 0, width, height, 0x66000000);
        renderBookFrame(graphics);
        renderHeader(graphics);
        renderList(graphics, mouseX, mouseY);
        renderDetails(graphics);
        renderActionLedger(graphics);
        super.render(graphics, mouseX, mouseY, partialTick);
    }

    @Override
    public void renderBackground(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
    }

    private void renderBookFrame(GuiGraphics graphics) {
        graphics.fill(guiLeft + 4, guiTop + 5, guiLeft + guiW + 4, guiTop + guiH + 5, 0x66000000);
        MilitaryGuiStyle.parchmentPanel(graphics, guiLeft, guiTop, guiW, guiH);

        int pageY = contentTop();
        int pageH = Math.max(36, contentBottom() - pageY);
        MilitaryGuiStyle.parchmentInset(graphics, leftPageX(), pageY, leftPageW(), pageH);
        MilitaryGuiStyle.parchmentInset(graphics, rightPageX(), pageY, rightPageW(), pageH);

        int spineX = leftPageX() + leftPageW() + pageGap() / 2 - 1;
        graphics.fill(spineX, pageY + 3, spineX + 2, pageY + pageH - 3, PAGE_SHADE);
        graphics.fill(spineX + 2, pageY + 3, spineX + 3, pageY + pageH - 3, 0x88FFF3C5);

        MilitaryGuiStyle.parchmentInset(graphics, actionLedgerX(), actionLedgerTop(), actionLedgerW(), actionLedgerH());
    }

    private void renderHeader(GuiGraphics graphics) {
        graphics.drawCenteredString(font, text("gui.bannermod.hamlets.heading").getString(), guiLeft + guiW / 2, guiTop + 9, GOLD);
        graphics.drawString(font,
                font.plainSubstrByWidth(this.title.getString(), Math.max(40, innerW() / 2 - 8)),
                innerX() + 4, guiTop + 25, INK_MUTED, false);
        graphics.drawString(font,
                text("gui.bannermod.hamlets.ledger_title").getString(),
                actionLedgerX() + 8, actionLedgerTop() + 5, INK_MUTED, false);
    }

    private void renderList(GuiGraphics graphics, int mouseX, int mouseY) {
        int listX = listX();
        int listY = listY();
        int listW = listW();
        int listH = this.listVisible * ROW_H;
        graphics.drawString(font, text("gui.bannermod.hamlets.list_title").getString(), listX, contentTop() + 8, INK, false);
        graphics.fill(listX, listY, listX + listW, listY + listH, 0x22FFFFFF);
        graphics.renderOutline(listX, listY, listW, listH, PAGE_SHADE);
        int rendered = Math.min(this.listVisible, Math.max(0, this.hamlets.size() - this.scrollOffset));
        for (int i = 0; i < rendered; i++) {
            NpcHamletRecord hamlet = this.hamlets.get(this.scrollOffset + i);
            int rowY = listY + i * ROW_H;
            boolean hovered = mouseX >= listX && mouseX < listX + listW && mouseY >= rowY && mouseY < rowY + ROW_H;
            boolean picked = this.selected != null && this.selected.hamletId().equals(hamlet.hamletId());
            if (picked || hovered) {
                graphics.fill(listX + 1, rowY + 1, listX + listW - 1, rowY + ROW_H - 1, picked ? 0x669E3A23 : 0x33FFFFFF);
            }
            String badge = "[" + localizedStatus(hamlet.status()).getString() + "]";
            graphics.drawString(font, badge, listX + 4, rowY + 4, statusColor(hamlet.status()), false);
            graphics.drawString(font,
                    font.plainSubstrByWidth(" " + hamlet.name(), Math.max(20, listW - 86)),
                    listX + 84, rowY + 4, INK, false);
        }
        if (showEmptyPanel()) {
            String empty = emptyListMessage().getString();
            graphics.renderOutline(listX + 8, listY + listH / 2 - 14, Math.max(20, listW - 16), 28, INK_MUTED);
            graphics.drawCenteredString(font, font.plainSubstrByWidth(empty, Math.max(20, listW - 20)), listX + listW / 2, listY + listH / 2 - 4, INK_MUTED);
        }
    }

    private boolean showEmptyPanel() {
        return this.hamlets.isEmpty() || NpcHamletClientState.syncPending();
    }

    private Component emptyListMessage() {
        if (NpcHamletClientState.syncPending() || !NpcHamletClientState.hasSnapshot()) {
            return text("gui.bannermod.hamlets.waiting_sync");
        }
        if (!NpcHamletClientState.hasClaim()) {
            return text("gui.bannermod.hamlets.no_claim");
        }
        if (this.hamlets.isEmpty()) {
            return text("gui.bannermod.hamlets.empty");
        }
        return text("gui.bannermod.hamlets.select_hamlet");
    }

    private void renderDetails(GuiGraphics graphics) {
        int x = rightPageX() + 8;
        int y = contentTop() + 8;
        int w = Math.max(40, rightPageW() - 16);
        graphics.drawString(font, text("gui.bannermod.hamlets.detail").getString(), x, y, INK, false);
        if (this.selected == null) {
            graphics.drawString(font, font.plainSubstrByWidth(text("gui.bannermod.hamlets.select_hamlet").getString(), w), x, y + 14, INK_MUTED, false);
            graphics.drawString(font, font.plainSubstrByWidth(text("gui.bannermod.hamlets.help").getString(), w), x, y + 28, INK_MUTED, false);
            return;
        }
        List<String> lines = new ArrayList<>();
        lines.add(text("gui.bannermod.hamlets.detail.name", this.selected.name()).getString());
        lines.add(text("gui.bannermod.hamlets.detail.status", localizedStatus(this.selected.status())).getString());
        lines.add(text("gui.bannermod.hamlets.detail.anchor", this.selected.anchorPos().getX(), this.selected.anchorPos().getY(), this.selected.anchorPos().getZ()).getString());
        lines.add(text("gui.bannermod.hamlets.detail.households", this.selected.householdCount()).getString());
        lines.add(text("gui.bannermod.hamlets.detail.founder", shortId(this.selected.founderHouseholdId())).getString());
        lines.add(text("gui.bannermod.hamlets.detail.claim", shortId(this.selected.claimUuid())).getString());
        lines.add(text("gui.bannermod.hamlets.detail.last_hostile", this.selected.lastHostileActionGameTime() <= 0L ? text("gui.bannermod.common.none").getString() : Long.toString(this.selected.lastHostileActionGameTime())).getString());
        for (NpcHamletHouseholdEntry entry : this.selected.householdEntries()) {
            lines.add(text("gui.bannermod.hamlets.detail.household_line",
                    shortId(entry.householdId()),
                    entry.plotPos().getX(),
                    entry.plotPos().getZ(),
                    entry.homeBuildingUuid() == null ? "-" : shortId(entry.homeBuildingUuid())
            ).getString());
        }
        int maxLines = maxDetailLines(y);
        for (int i = 0; i < lines.size() && i < maxLines; i++) {
            graphics.drawString(font, font.plainSubstrByWidth(lines.get(i), w), x, y + 14 + i * 12, i >= 7 ? INK_MUTED : INK, false);
        }
    }

    private void renderActionLedger(GuiGraphics graphics) {
        int x = actionLedgerX() + 8;
        int y = actionLedgerTop() + 18;
        int w = Math.max(40, actionLedgerW() - 16);
        Component status = visibleActionStatus();
        graphics.drawString(font, font.plainSubstrByWidth(status.getString(), w), x, y, INK, false);
    }

    private Component visibleActionStatus() {
        if (NpcHamletClientState.syncPending() || !NpcHamletClientState.hasSnapshot()) {
            return text("gui.bannermod.hamlets.waiting_sync");
        }
        if (!NpcHamletClientState.hasClaim()) {
            return text("gui.bannermod.hamlets.no_claim");
        }
        if (!NpcHamletClientState.canManage()) {
            return readOnlyReason();
        }
        if (this.selected == null) {
            return text("gui.bannermod.hamlets.select_hamlet");
        }
        if (this.selected.status() == NpcHamletStatus.INFORMAL) {
            return text("gui.bannermod.hamlets.action.register_ready");
        }
        return text("gui.bannermod.hamlets.action.authorized");
    }

    private Component readOnlyReason() {
        String denialKey = NpcHamletClientState.denialKey();
        return denialKey == null || denialKey.isBlank()
                ? text("gui.bannermod.hamlets.action.read_only")
                : Component.translatable(denialKey);
    }

    @Override
    public boolean mouseClicked(double mouseX, double mouseY, int button) {
        if (button == 0) {
            int listX = listX();
            int listY = listY();
            int listW = listW();
            int listH = this.listVisible * ROW_H;
            if (mouseX >= listX && mouseX < listX + listW && mouseY >= listY && mouseY < listY + listH) {
                int idx = this.scrollOffset + (int) ((mouseY - listY) / ROW_H);
                if (idx >= 0 && idx < this.hamlets.size()) {
                    this.selected = this.hamlets.get(idx);
                    updateButtons();
                    return true;
                }
            }
        }
        return super.mouseClicked(mouseX, mouseY, button);
    }

    @Override
    public boolean mouseScrolled(double mouseX, double mouseY, double scrollX, double delta) {
        int listX = listX();
        int listY = listY();
        int listW = listW();
        int listH = this.listVisible * ROW_H;
        if (mouseX < listX || mouseX >= listX + listW || mouseY < listY || mouseY >= listY + listH) {
            return super.mouseScrolled(mouseX, mouseY, scrollX, delta);
        }
        int max = Math.max(0, this.hamlets.size() - this.listVisible);
        this.scrollOffset = clamp(this.scrollOffset - (int) Math.signum(delta), 0, max);
        return true;
    }

    private void updateGeometry() {
        int viewportW = Math.max(1, this.width - 12);
        int viewportH = Math.max(1, this.height - 12);
        int minW = Math.min(MIN_BOOK_W, viewportW);
        int minH = Math.min(MIN_BOOK_H, viewportH);
        this.guiW = Math.min(MAX_BOOK_W, Math.max(minW, this.width - 28));
        this.guiH = Math.min(MAX_BOOK_H, Math.max(minH, this.height - 24));
        this.guiLeft = (this.width - this.guiW) / 2;
        this.guiTop = (this.height - this.guiH) / 2;
        this.listVisible = Math.max(1, listH() / ROW_H);
    }

    private int innerX() {
        return this.guiLeft + BOOK_BORDER + 8;
    }

    private int innerW() {
        return this.guiW - (BOOK_BORDER + 8) * 2;
    }

    private int pageGap() {
        return Math.max(12, this.guiW / 54);
    }

    private int contentTop() {
        return this.guiTop + 38;
    }

    private int contentBottom() {
        return actionLedgerTop() - 8;
    }

    private int leftPageX() {
        return innerX();
    }

    private int leftPageW() {
        int available = innerW() - pageGap();
        return clamp(available * 2 / 5, 136, Math.max(136, available - 148));
    }

    private int rightPageX() {
        return leftPageX() + leftPageW() + pageGap();
    }

    private int rightPageW() {
        return innerW() - leftPageW() - pageGap();
    }

    private int listX() {
        return leftPageX() + 8;
    }

    private int listY() {
        return contentTop() + 24;
    }

    private int listW() {
        return Math.max(80, leftPageW() - 16);
    }

    private int listH() {
        return Math.max(ROW_H, contentBottom() - listY() - 8);
    }

    private int actionLedgerTop() {
        return guiTop + guiH - actionLedgerH() - 8;
    }

    private int actionLedgerH() {
        return 32 + actionRows() * (BUTTON_H + 4);
    }

    private int actionRows() {
        return 1;
    }

    private int actionLedgerX() {
        return innerX();
    }

    private int actionLedgerW() {
        return innerW();
    }

    private int actionButtonW() {
        return Math.max(64, (actionLedgerW() - 16 - 3 * 6) / 4);
    }

    private int actionButtonX(int index) {
        return actionLedgerX() + 8 + index * (actionButtonW() + 6);
    }

    private int actionButtonY(int index) {
        return actionLedgerTop() + 30;
    }

    private int maxDetailLines(int titleY) {
        int firstLineY = titleY + 14;
        int detailBottom = contentBottom() - 8;
        if (detailBottom < firstLineY) {
            return 0;
        }
        return ((detailBottom - firstLineY) / 12) + 1;
    }

    private static Component localizedStatus(NpcHamletStatus status) {
        return Component.translatable("gui.bannermod.society.hamlet.status." + (status == null ? NpcHamletStatus.INFORMAL : status).name().toLowerCase(Locale.ROOT));
    }

    private static int statusColor(NpcHamletStatus status) {
        return switch (status == null ? NpcHamletStatus.INFORMAL : status) {
            case INFORMAL -> 0xFFD9A441;
            case REGISTERED -> 0xFF79B15A;
            case ABANDONED -> 0xFFAAAAAA;
        };
    }

    private static int hamletSeverity(NpcHamletStatus status) {
        return switch (status == null ? NpcHamletStatus.INFORMAL : status) {
            case INFORMAL -> 0;
            case REGISTERED -> 1;
            case ABANDONED -> 2;
        };
    }

    private static String shortId(@Nullable UUID uuid) {
        if (uuid == null) {
            return "-";
        }
        String raw = uuid.toString();
        return raw.length() > 8 ? raw.substring(0, 8) : raw;
    }

    private static Component text(String key, Object... args) {
        return Component.translatable(key, args);
    }

    private static int clamp(int v, int min, int max) {
        return Math.max(min, Math.min(max, v));
    }

    @Override
    public void onClose() {
        if (this.parent != null) {
            this.minecraft.setScreen(this.parent);
        } else {
            super.onClose();
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    private static class MedievalButton extends Button {
        MedievalButton(int x, int y, int width, int height, Component message, OnPress onPress) {
            super(x, y, width, height, message, onPress, DEFAULT_NARRATION);
        }

        @Override
        protected void renderWidget(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
            int x = getX();
            int y = getY();
            int w = getWidth();
            int h = getHeight();
            boolean hovered = isHoveredOrFocused();
            int border = active ? (hovered ? GOLD : PAGE_SHADE) : 0xFF7C6C55;
            int fill = active ? (hovered ? 0xFF6A3D1F : LEATHER) : 0xFF4C3A28;

            graphics.fill(x, y, x + w, y + h, LEATHER_DARK);
            graphics.fill(x + 1, y + 1, x + w - 1, y + h - 1, fill);
            graphics.fill(x + 2, y + 2, x + w - 2, y + 4, 0x557A4C24);
            graphics.renderOutline(x, y, w, h, border);
            graphics.renderOutline(x + 1, y + 1, w - 2, h - 2, 0x661A100A);

            Font font = Minecraft.getInstance().font;
            String label = clippedLabel(font, getMessage().getString(), Math.max(4, w - 10));
            int textColor = active ? GOLD : 0xFFB8A17A;
            graphics.drawCenteredString(font, label, x + w / 2, y + (h - 8) / 2, textColor);
        }

        private static String clippedLabel(Font font, String label, int maxWidth) {
            if (font.width(label) <= maxWidth) {
                return label;
            }
            String ellipsis = "...";
            int textWidth = Math.max(1, maxWidth - font.width(ellipsis));
            return font.plainSubstrByWidth(label, textWidth) + ellipsis;
        }
    }
}
