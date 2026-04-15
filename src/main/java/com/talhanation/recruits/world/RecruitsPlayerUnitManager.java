package com.talhanation.recruits.world;

import com.talhanation.recruits.FactionEvents;
import com.talhanation.recruits.Main;
import com.talhanation.recruits.RecruitEvents;
import com.talhanation.recruits.config.RecruitsServerConfig;
import com.talhanation.recruits.network.MessageToClientUpdateUnitInfo;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.network.PacketDistributor;

import java.util.*;

public class RecruitsPlayerUnitManager {
    private Map<UUID, Integer> recruitCountMap = new HashMap<>();
    public void load(ServerLevel level) {
        RecruitPlayerUnitSaveData data = RecruitPlayerUnitSaveData.get(level);

        recruitCountMap = data.getRecruitCountMap();
    }

    public void save(ServerLevel level) {
        RecruitPlayerUnitSaveData data = RecruitPlayerUnitSaveData.get(level);

        for (Map.Entry<UUID, Integer> entry : recruitCountMap.entrySet()) {
            data.setRecruitCount(entry.getKey(), entry.getValue());
        }

        data.setDirty();
    }

    public int getRecruitCount(UUID playerUUID) {
        return recruitCountMap.getOrDefault(playerUUID, 0);
    }

    public void setRecruitCount(Player player, int count) {
        setRecruitCount(player.getUUID(), count, getLiveSaveData());
    }

    public void addRecruits(UUID playerUUID, int count) {
        addRecruits(playerUUID, count, getLiveSaveData());
    }

    public void removeRecruits(UUID playerUUID, int count) {
        removeRecruits(playerUUID, count, getLiveSaveData());
    }

    void setRecruitCount(UUID playerUUID, int count, RecruitPlayerUnitSaveData data) {
        recruitCountMap.put(playerUUID, count);
        if (data != null) {
            data.setRecruitCount(playerUUID, count);
            data.setDirty();
        }
    }

    void addRecruits(UUID playerUUID, int count, RecruitPlayerUnitSaveData data) {
        setRecruitCount(playerUUID, getRecruitCount(playerUUID) + count, data);
    }

    void removeRecruits(UUID playerUUID, int count, RecruitPlayerUnitSaveData data) {
        setRecruitCount(playerUUID, Math.max(getRecruitCount(playerUUID) - count, 0), data);
    }

    private RecruitPlayerUnitSaveData getLiveSaveData() {
        if (RecruitEvents.server == null) {
            return null;
        }
        return RecruitPlayerUnitSaveData.get(RecruitEvents.server.overworld());
    }

    public boolean canPlayerRecruit(String stringId, UUID playerUUID) {
        RecruitsFaction recruitsFaction = FactionEvents.recruitsFactionManager.getFactionByStringID(stringId);

        int currentRecruitCount = getRecruitCount(playerUUID);
        int maxRecruitCount = 0;

        if (recruitsFaction == null) {
            maxRecruitCount = RecruitsServerConfig.MaxRecruitsForPlayer.get();
        } else {
            int maxRecruitsInFaction = recruitsFaction.maxNPCs;
            if(maxRecruitsInFaction == 0) maxRecruitsInFaction = 1000000000;

            if (playerUUID.equals(recruitsFaction.getTeamLeaderUUID())) {
                maxRecruitCount = maxRecruitsInFaction;
            } else {
                maxRecruitCount = recruitsFaction.getMaxNPCsPerPlayer();
            }

            if (recruitsFaction.npcs >= maxRecruitsInFaction) {
                return false;
            }
        }

        return currentRecruitCount < maxRecruitCount;
    }
    public int getRemainingRecruitSlots(String stringId, UUID playerUUID) {
        RecruitsFaction recruitsFaction = FactionEvents.recruitsFactionManager.getFactionByStringID(stringId);

        int currentRecruitCount = getRecruitCount(playerUUID);
        int maxRecruitCount;

        if (recruitsFaction == null) {
            maxRecruitCount = RecruitsServerConfig.MaxRecruitsForPlayer.get();
        } else {
            if (playerUUID.equals(recruitsFaction.getTeamLeaderUUID())) {
                maxRecruitCount = recruitsFaction.maxNPCs;
            } else {
                maxRecruitCount = recruitsFaction.getMaxNPCsPerPlayer();
            }

            if (recruitsFaction.npcs >= recruitsFaction.maxNPCs) {
                return 0;
            }
        }

        int remaining = maxRecruitCount - currentRecruitCount;
        return Math.max(remaining, 0);
    }

    public void broadCastUnitInfoToPlayer(Player player) {
        if (player == null) return;

        String factionID = null;
        if(player.getTeam() != null){
            factionID = player.getTeam().getName();
        }

        Main.SIMPLE_CHANNEL.send(PacketDistributor.PLAYER.with(()-> (ServerPlayer) player),
                new MessageToClientUpdateUnitInfo(
                        RecruitsServerConfig.NobleVillagerNeedsVillagers.get(),
                        getRemainingRecruitSlots(factionID, player.getUUID())
                ));
    }

}
