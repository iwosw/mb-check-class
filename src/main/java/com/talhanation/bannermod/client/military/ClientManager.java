package com.talhanation.bannermod.client.military;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.army.map.FormationMapContact;
import com.talhanation.bannermod.persistence.military.*;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.io.IOException;
import java.util.*;

public class ClientManager {
    private static final ClientSyncState SYNCHRONIZED_STATE = new ClientSyncState();
    private static final int GROUP_COUNT_UPDATE_INTERVAL_TICKS = 10;
    private static int lastGroupCountUpdateTick = Integer.MIN_VALUE;
    private static int lastGroupCountScanTick = Integer.MIN_VALUE;
    private static UUID lastGroupCountPlayer;
    private static Map<UUID, Integer> lastGroupCounts = Map.of();
    public static List<RecruitsClaim> recruitsClaims = new ArrayList<>();
    public static Map<UUID, RecruitsClaim> recruitsClaimsByUUID = new HashMap<>();
    public static Map<Long, RecruitsClaim> recruitsClaimsByChunk = new HashMap<>();
    public static int recruitsClaimsVersion;
    public static Map<UUID, RecruitsClaim> activeSiegeClaims = new HashMap<>();
    public static List<RecruitsFaction> factions = new ArrayList<>();
    public static int factionsVersion;
    public static List<RecruitsGroup> groups = new ArrayList<>();
    public static int groupsVersion;
    public static List<FormationMapContact> formationMapContacts = new ArrayList<>();
    public static int formationMapContactsVersion;
    public static RecruitsFaction ownFaction;
    public static Map<String, Map<String, RecruitsDiplomacyManager.DiplomacyStatus>> diplomacyMap = new HashMap<>();
    public static Map<String, Long> treaties = new HashMap<>();
    public static int diplomacyVersion;
    public static int configValueClaimCost;
    public static int configValueChunkCost;
    public static boolean configValueCascadeClaimCost;
    public static ItemStack currencyItemStack;
    public static boolean isFactionEditingAllowed;
    public static boolean isFactionManagingAllowed;
    public static List<RecruitsPlayerInfo> onlinePlayers = new ArrayList<>();
    public static int onlinePlayersVersion;
    public static ItemStack currency;
    public static int factionCreationPrice;
    public static int factionMaxRecruitsPerPlayerConfigSetting;
    public static boolean configValueNobleNeedsVillagers;
    public static int availableRecruitsToHire;
    public static int formationSelection;
    public static int groupSelection;
    @Nullable
    public static RecruitsClaim currentClaim;
    public static boolean configValueIsClaimingAllowed;
    public static boolean configFogOfWarEnabled;

    public static Map<String, RecruitsRoute> routesMap = new HashMap<>();
    public static boolean canPlayerHire;

    public static ItemStack getCurrencyItemStackOrDefault() {
        return currencyItemStack == null || currencyItemStack.isEmpty() ? Items.EMERALD.getDefaultInstance() : currencyItemStack;
    }

    public static void resetSynchronizedState() {
        com.talhanation.bannermod.migration.StatePersistenceSeams.ClientSyncState resetState = SYNCHRONIZED_STATE.resetPreservingRoutes(routesMap);
        recruitsClaims = new ArrayList<>(resetState.claims());
        markClaimsChanged();
        activeSiegeClaims = new HashMap<>(resetState.activeSieges());
        factions = new ArrayList<>();
        factionsVersion++;
        groups = new ArrayList<>();
        groupsVersion++;
        formationMapContacts = new ArrayList<>();
        formationMapContactsVersion++;
        ownFaction = null;
        diplomacyMap = new HashMap<>();
        treaties = new HashMap<>();
        diplomacyVersion++;
        configValueClaimCost = 0;
        configValueChunkCost = 0;
        configValueCascadeClaimCost = false;
        currencyItemStack = null;
        isFactionEditingAllowed = false;
        isFactionManagingAllowed = false;
        onlinePlayers = new ArrayList<>();
        onlinePlayersVersion++;
        currency = null;
        factionCreationPrice = 0;
        factionMaxRecruitsPerPlayerConfigSetting = 0;
        configValueNobleNeedsVillagers = false;
        availableRecruitsToHire = 0;
        formationSelection = 0;
        groupSelection = 0;
        currentClaim = null;
        configValueIsClaimingAllowed = false;
        configFogOfWarEnabled = false;
        canPlayerHire = false;
        routesMap = resetState.routes();
    }

    public static void rebuildActiveSieges() {
        activeSiegeClaims = new HashMap<>(SYNCHRONIZED_STATE.rebuild(recruitsClaims));
    }

    public static void updateActiveSiege(RecruitsClaim claim) {
        activeSiegeClaims = new HashMap<>(SYNCHRONIZED_STATE.update(activeSiegeClaims, claim));
    }

    @OnlyIn(Dist.CLIENT)
    public static RecruitsDiplomacyManager.DiplomacyStatus getRelation(String team, String otherTeam) {
        return diplomacyMap.getOrDefault(team, new HashMap<>()).getOrDefault(otherTeam, RecruitsDiplomacyManager.DiplomacyStatus.NEUTRAL);
    }

    @OnlyIn(Dist.CLIENT)
    public static boolean hasTreaty(String factionA, String factionB) {
        String[] sorted = new String[]{factionA, factionB};
        java.util.Arrays.sort(sorted);
        String key = sorted[0] + "|" + sorted[1];
        Long expiry = treaties.get(key);
        if (expiry == null) return false;
        return System.currentTimeMillis() < expiry;
    }

    @OnlyIn(Dist.CLIENT)
    public static long getTreatyRemainingMillis(String factionA, String factionB) {
        String[] sorted = new String[]{factionA, factionB};
        java.util.Arrays.sort(sorted);
        String key = sorted[0] + "|" + sorted[1];
        long expiry = treaties.getOrDefault(key, 0L);
        return Math.max(0L, expiry - System.currentTimeMillis());
    }

    @OnlyIn(Dist.CLIENT)
    public static RecruitsPlayerInfo getPlayerInfo() {
        Player player = Minecraft.getInstance().player;
        if (player != null) return new RecruitsPlayerInfo(player.getUUID(), player.getName().getString(), ownFaction);
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    public static RecruitsGroup getGroup(UUID groupUUID) {
        for (RecruitsGroup group : groups) {
            if (group.getUUID().equals(groupUUID)) return group;
        }
        return null;
    }

    @OnlyIn(Dist.CLIENT)
    public static RecruitsGroup getSelectedGroup() {
        if (groups != null && !groups.isEmpty()) {
            try { return groups.get(groupSelection); }
            catch (Exception e) { groupSelection = 0; return groups.get(0); }
        }
        return null;
    }

    public static void updateGroups() {
        updateGroups(false);
    }

    public static void updateGroups(boolean force) {
        Player player = Minecraft.getInstance().player;
        if (player == null || groups == null || groups.isEmpty()) return;
        if (!force && player.tickCount - lastGroupCountUpdateTick < GROUP_COUNT_UPDATE_INTERVAL_TICKS) return;
        lastGroupCountUpdateTick = player.tickCount;

        Map<UUID, Integer> groupCounts = groupCountsFor(player);

        boolean changed = false;
        for (RecruitsGroup group : ClientManager.groups) {
            int count = groupCounts.getOrDefault(group.getUUID(), 0);
            if (group.getCount() != count) {
                group.setCount(count);
                changed = true;
            }
        }
        if (changed || force) {
            ClientManager.groups.sort((a, b) -> Integer.compare(b.getCount(), a.getCount()));
        }
        if (changed) {
            groupsVersion++;
        }
    }

    private static Map<UUID, Integer> groupCountsFor(Player player) {
        UUID playerUuid = player.getUUID();
        if (lastGroupCountScanTick == player.tickCount && playerUuid.equals(lastGroupCountPlayer)) {
            return lastGroupCounts;
        }

        List<AbstractRecruitEntity> recruits = player.level()
                .getEntitiesOfClass(AbstractRecruitEntity.class, player.getBoundingBox().inflate(100),
                        r -> r.isEffectedByCommand(playerUuid));

        Map<UUID, Integer> groupCounts = new HashMap<>();
        for (AbstractRecruitEntity recruit : recruits) {
            UUID groupId = recruit.getGroup();
            if (groupId == null) continue;
            groupCounts.put(groupId, groupCounts.getOrDefault(groupId, 0) + 1);
        }
        lastGroupCountScanTick = player.tickCount;
        lastGroupCountPlayer = playerUuid;
        lastGroupCounts = groupCounts;
        return groupCounts;
    }

    public static void markClaimsChanged() {
        rebuildClaimIndices();
        recruitsClaimsVersion++;
    }

    public static void rebuildClaimIndices() {
        Map<UUID, RecruitsClaim> byUUID = new HashMap<>();
        Map<Long, RecruitsClaim> byChunk = new HashMap<>();
        for (RecruitsClaim claim : recruitsClaims) {
            byUUID.put(claim.getUUID(), claim);
            for (net.minecraft.world.level.ChunkPos chunk : claim.getClaimedChunks()) {
                byChunk.put(net.minecraft.world.level.ChunkPos.asLong(chunk.x, chunk.z), claim);
            }
        }
        recruitsClaimsByUUID = byUUID;
        recruitsClaimsByChunk = byChunk;
    }

    @Nullable
    public static RecruitsClaim getClaim(UUID uuid) {
        if (!recruitsClaimsByUUID.isEmpty()) return recruitsClaimsByUUID.get(uuid);
        for (RecruitsClaim claim : recruitsClaims) {
            if (claim.getUUID().equals(uuid)) return claim;
        }
        return null;
    }

    @Nullable
    public static RecruitsClaim getClaimAtChunk(net.minecraft.world.level.ChunkPos chunk) {
        if (!recruitsClaimsByChunk.isEmpty()) return recruitsClaimsByChunk.get(net.minecraft.world.level.ChunkPos.asLong(chunk.x, chunk.z));
        for (RecruitsClaim claim : recruitsClaims) {
            if (claim.containsChunk(chunk)) return claim;
        }
        return null;
    }

    public static void markFactionsChanged() {
        factionsVersion++;
    }

    public static void markGroupsChanged() {
        groupsVersion++;
    }

    public static void markFormationMapContactsChanged() {
        formationMapContactsVersion++;
    }

    public static void markOnlinePlayersChanged() {
        onlinePlayersVersion++;
    }

    public static void markDiplomacyChanged() {
        diplomacyVersion++;
    }

    // -------------------------------------------------------------------------
    // Route helpers
    // -------------------------------------------------------------------------

    @OnlyIn(Dist.CLIENT)
    public static void loadRoutes() {
        routesMap.clear();
        try {
            List<RecruitsRoute> loaded = RecruitsRoute.loadAllRoutes(RecruitsRoute.getRoutesDirectory());
            for (RecruitsRoute route : loaded) routesMap.put(route.getId().toString(), route);
        } catch (IOException e) {
            // start with empty map
        }
    }

    @OnlyIn(Dist.CLIENT)
    public static void saveRoute(RecruitsRoute route) {
        try {
            route.saveToFile(RecruitsRoute.getRoutesDirectory());
            routesMap.put(route.getId().toString(), route);
        } catch (IOException e) {
            // could not save
        }
    }

    /**
     * Renames a route: deletes the old name-based file, then saves under the new name.
     */
    @OnlyIn(Dist.CLIENT)
    public static void renameRoute(RecruitsRoute route, String newName) {
        route.deleteFile(RecruitsRoute.getRoutesDirectory());
        route.setName(newName);
        saveRoute(route);
    }

    @OnlyIn(Dist.CLIENT)
    public static void deleteRoute(RecruitsRoute route) {
        routesMap.remove(route.getId().toString());
        route.deleteFile(RecruitsRoute.getRoutesDirectory());
    }

    @OnlyIn(Dist.CLIENT)
    public static List<RecruitsRoute> getRoutesList() {
        return new ArrayList<>(routesMap.values());
    }
}
