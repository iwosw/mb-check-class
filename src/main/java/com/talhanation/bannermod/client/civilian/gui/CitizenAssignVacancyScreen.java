package com.talhanation.bannermod.client.civilian.gui;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.client.military.gui.MilitaryGuiStyle;
import com.talhanation.bannermod.entity.citizen.CitizenEntity;
import com.talhanation.bannermod.entity.civilian.workarea.AbstractWorkAreaEntity;
import com.talhanation.bannermod.entity.civilian.workarea.AnimalPenArea;
import com.talhanation.bannermod.entity.civilian.workarea.CropArea;
import com.talhanation.bannermod.entity.civilian.workarea.FishingArea;
import com.talhanation.bannermod.entity.civilian.workarea.LumberArea;
import com.talhanation.bannermod.entity.civilian.workarea.MiningArea;
import com.talhanation.bannermod.network.messages.civilian.MessageAssignCitizenVacancy;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.AbstractButton;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.narration.NarrationElementOutput;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.client.gui.widget.ExtendedButton;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;

/**
 * Popup that lists nearby work-areas the player can assign this citizen to.
 *
 * <p>Client-side scan: walks the local entity manager for {@link AbstractWorkAreaEntity}
 * instances within ~96 blocks (matching VACANCY_ASSIGN_RADIUS_SQR). Filters to types
 * that map to a worker profession (CropArea/MiningArea/LumberArea/FishingArea/AnimalPenArea).
 * On click, sends {@link MessageAssignCitizenVacancy} and returns to the parent profile
 * screen. Server still validates ownership and existence.
 */
public class CitizenAssignVacancyScreen extends Screen {

    private static final double SCAN_RADIUS = 96.0D;
    private static final int LIST_TOP = 40;
    private static final int ROW_HEIGHT = 22;
    private static final int MAX_VISIBLE_ROWS = 8;

    private final Screen parent;
    private final CitizenEntity citizen;
    private final List<VacancyEntry> vacancies = new ArrayList<>();

    public CitizenAssignVacancyScreen(Screen parent, CitizenEntity citizen) {
        super(Component.translatable("gui.bannermod.citizen_profile.assign_vacancy.title"));
        this.parent = parent;
        this.citizen = citizen;
    }

    @Override
    protected void init() {
        super.init();
        this.vacancies.clear();
        this.vacancies.addAll(scanNearbyVacancies());

        int panelWidth = 220;
        int panelX = (this.width - panelWidth) / 2;

        for (int i = 0; i < Math.min(vacancies.size(), MAX_VISIBLE_ROWS); i++) {
            VacancyEntry entry = this.vacancies.get(i);
            int y = LIST_TOP + i * ROW_HEIGHT;
            this.addRenderableWidget(new ExtendedButton(panelX + 4, y, panelWidth - 8, 20,
                    Component.literal(entry.label()),
                    button -> onPick(entry)));
        }

        this.addRenderableWidget(Button.builder(
                Component.translatable("gui.bannermod.citizen_profile.assign_vacancy.back"),
                button -> close()).bounds(panelX + 4, this.height - 30, panelWidth - 8, 20).build());
    }

    private void onPick(VacancyEntry entry) {
        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageAssignCitizenVacancy(this.citizen.getUUID(), entry.uuid()));
        close();
    }

    private void close() {
        if (this.minecraft != null) this.minecraft.setScreen(this.parent);
    }

    @Override
    public void render(GuiGraphics graphics, int mouseX, int mouseY, float partialTick) {
        this.renderBackground(graphics, mouseX, mouseY, partialTick);
        super.render(graphics, mouseX, mouseY, partialTick);
        graphics.drawCenteredString(this.font, this.title, this.width / 2, 16, 0xFFFFFFFF);
        if (this.vacancies.isEmpty()) {
            graphics.drawCenteredString(this.font,
                    Component.translatable("gui.bannermod.citizen_profile.assign_vacancy.empty"),
                    this.width / 2, LIST_TOP + 12, MilitaryGuiStyle.TEXT_DENIED);
        }
    }

    @Override
    public boolean isPauseScreen() {
        return false;
    }

    @Override
    protected void updateNarratedWidget(NarrationElementOutput output) {
        // No special narration — let the default widget narration handle button labels.
    }

    private List<VacancyEntry> scanNearbyVacancies() {
        Minecraft mc = Minecraft.getInstance();
        if (mc.level == null) return List.of();
        Player viewer = mc.player;
        if (viewer == null) return List.of();

        // Prefer the citizen's host claim — its drawn boundary is the player's stated
        // intent for "where my workers should work", which beats an arbitrary radius.
        net.minecraft.world.level.ChunkPos citizenChunk = new net.minecraft.world.level.ChunkPos(this.citizen.blockPosition());
        com.talhanation.bannermod.persistence.military.RecruitsClaim claim =
                com.talhanation.bannermod.client.military.ClientManager.getClaimAtChunk(citizenChunk);

        AABB scanBox;
        if (claim != null && !claim.getClaimedChunks().isEmpty()) {
            // Bounding box across all claim chunks; we still filter per-entity by
            // claim.containsChunk to ignore anything spilling outside the bbox.
            int minX = Integer.MAX_VALUE, minZ = Integer.MAX_VALUE;
            int maxX = Integer.MIN_VALUE, maxZ = Integer.MIN_VALUE;
            for (net.minecraft.world.level.ChunkPos cp : claim.getClaimedChunks()) {
                if (cp.x < minX) minX = cp.x;
                if (cp.z < minZ) minZ = cp.z;
                if (cp.x > maxX) maxX = cp.x;
                if (cp.z > maxZ) maxZ = cp.z;
            }
            scanBox = new AABB(
                    minX * 16, mc.level.getMinBuildHeight(), minZ * 16,
                    maxX * 16 + 16, mc.level.getMaxBuildHeight(), maxZ * 16 + 16);
        } else {
            // Fallback: citizen sits outside any claim — keep the legacy 96-block scan
            // so the button still does something useful in that edge case.
            scanBox = this.citizen.getBoundingBox().inflate(SCAN_RADIUS);
        }

        List<VacancyEntry> entries = new ArrayList<>();
        for (Entity entity : mc.level.getEntities(this.citizen, scanBox)) {
            if (entity == this.citizen) continue;
            if (!(entity instanceof AbstractWorkAreaEntity area) || !area.isAlive()) continue;
            String label = labelFor(area);
            if (label == null) continue;
            // Clip to the actual claim shape, not just its bbox — claims can be L-shaped.
            if (claim != null && !claim.containsChunk(new net.minecraft.world.level.ChunkPos(area.blockPosition()))) {
                continue;
            }
            entries.add(new VacancyEntry(area.getUUID(), label, this.citizen.distanceToSqr(area)));
        }
        entries.sort(Comparator.comparingDouble(VacancyEntry::distSqr));
        return entries;
    }

    /** Returns null for non-bindable types (BuildArea/StorageArea/MarketArea). */
    private static String labelFor(AbstractWorkAreaEntity area) {
        String professionKey;
        if (area instanceof CropArea) professionKey = "bannermod.prefab.profession.farmer";
        else if (area instanceof MiningArea) professionKey = "bannermod.prefab.profession.miner";
        else if (area instanceof LumberArea) professionKey = "bannermod.prefab.profession.lumberjack";
        else if (area instanceof FishingArea) professionKey = "bannermod.prefab.profession.fisherman";
        else if (area instanceof AnimalPenArea) professionKey = "bannermod.prefab.profession.animal_farmer";
        else return null;
        return Component.translatable(professionKey).getString()
                + "  ("
                + area.blockPosition().getX() + ", "
                + area.blockPosition().getY() + ", "
                + area.blockPosition().getZ() + ")";
    }

    private record VacancyEntry(java.util.UUID uuid, String label, double distSqr) {
    }
}
