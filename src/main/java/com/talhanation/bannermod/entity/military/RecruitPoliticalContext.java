package com.talhanation.bannermod.entity.military;

import com.talhanation.bannermod.war.registry.PoliticalEntityRecord;
import com.talhanation.bannermod.war.registry.PoliticalRegistryRuntime;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.scores.Team;

import javax.annotation.Nullable;
import java.util.UUID;

public final class RecruitPoliticalContext {
    private RecruitPoliticalContext() {
    }

    @Nullable
    public static UUID politicalEntityIdOf(@Nullable LivingEntity entity, @Nullable PoliticalRegistryRuntime registry) {
        if (entity == null || registry == null) {
            return null;
        }
        UUID participantId = participantUuid(entity);
        UUID participantEntityId = politicalEntityIdForParticipant(participantId, registry);
        if (participantEntityId != null) {
            return participantEntityId;
        }
        Team team = entity.getTeam();
        return team == null ? null : politicalEntityIdForToken(team.getName(), registry);
    }

    @Nullable
    public static UUID politicalEntityIdForToken(@Nullable String token, @Nullable PoliticalRegistryRuntime registry) {
        if (token == null || token.isBlank() || registry == null) {
            return null;
        }
        try {
            UUID id = UUID.fromString(token);
            return registry.byId(id).map(PoliticalEntityRecord::id).orElse(null);
        } catch (IllegalArgumentException ignored) {
            return registry.byName(token).map(PoliticalEntityRecord::id).orElse(null);
        }
    }

    @Nullable
    private static UUID participantUuid(LivingEntity entity) {
        if (entity instanceof AbstractRecruitEntity recruit) {
            return recruit.getOwnerUUID();
        }
        if (entity instanceof Player player) {
            return player.getUUID();
        }
        return null;
    }

    @Nullable
    private static UUID politicalEntityIdForParticipant(@Nullable UUID participantId, PoliticalRegistryRuntime registry) {
        if (participantId == null) {
            return null;
        }
        for (PoliticalEntityRecord record : registry.all()) {
            if (participantId.equals(record.leaderUuid()) || record.coLeaderUuids().contains(participantId)) {
                return record.id();
            }
        }
        return null;
    }
}
