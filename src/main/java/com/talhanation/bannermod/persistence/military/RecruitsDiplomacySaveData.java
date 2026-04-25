package com.talhanation.bannermod.persistence.military;

import com.talhanation.bannermod.persistence.military.RecruitsDiplomacyManager.DiplomacyStatus;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.Map;

public class RecruitsDiplomacySaveData extends SavedData {
    public static final Map<String, Map<String, DiplomacyStatus>> diplomacyMap = new HashMap<>();
    private static final String DATA_NAME = "diplomacy_data";

    public RecruitsDiplomacySaveData() {
    }

    public static RecruitsDiplomacySaveData get(ServerLevel level) {
        return level.getDataStorage().computeIfAbsent(RecruitsDiplomacySaveData::load, RecruitsDiplomacySaveData::new, DATA_NAME);
    }

    public static RecruitsDiplomacySaveData load(CompoundTag nbt) {
        RecruitsDiplomacySaveData data = new RecruitsDiplomacySaveData();
        diplomacyMap.clear();
        CompoundTag teamsTag = nbt.getCompound("teams");

        for (String teamKey : teamsTag.getAllKeys()) {
            CompoundTag relationsTag = teamsTag.getCompound(teamKey);
            Map<String, DiplomacyStatus> relations = new HashMap<>();

            for (String relationKey : relationsTag.getAllKeys()) {
                byte relationStatusByte = relationsTag.getByte(relationKey);
                DiplomacyStatus relationStatus = DiplomacyStatus.fromByte(relationStatusByte);
                relations.put(relationKey, relationStatus);
            }
            diplomacyMap.put(teamKey, relations);
        }
        return data;
    }

    @Override
    public CompoundTag save(CompoundTag nbt) {
        CompoundTag teamsTag = new CompoundTag();

        for (Map.Entry<String, Map<String, DiplomacyStatus>> teamEntry : diplomacyMap.entrySet()) {
            CompoundTag relationsTag = new CompoundTag();
            for (Map.Entry<String, DiplomacyStatus> relationEntry : teamEntry.getValue().entrySet()) {
                relationsTag.putByte(relationEntry.getKey(), relationEntry.getValue().getByteValue());
            }
            teamsTag.put(teamEntry.getKey(), relationsTag);
        }
        nbt.put("teams", teamsTag);
        return nbt;
    }

    public void setRelation(String team, String otherTeam, byte relationByte) {
        DiplomacyStatus relation = DiplomacyStatus.fromByte(relationByte);
        Map<String, DiplomacyStatus> relations = diplomacyMap.computeIfAbsent(team, k -> new HashMap<>());
        if (relations.get(otherTeam) == relation) {
            return;
        }
        relations.put(otherTeam, relation);
        this.setDirty();
    }

    public Map<String, Map<String, DiplomacyStatus>> getDiplomacyMap() {
        return diplomacyMap;
    }
}
