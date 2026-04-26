package com.talhanation.bannermod.army.map;

import com.talhanation.bannermod.army.command.CommandHierarchy;
import com.talhanation.bannermod.army.command.CommandRole;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.entity.military.IStrategicFire;
import com.talhanation.bannermod.entity.military.RecruitIndex;
import com.talhanation.bannermod.events.FactionEvents;
import com.talhanation.bannermod.persistence.military.RecruitsDiplomacyManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public final class FormationMapSnapshotService {
    private FormationMapSnapshotService() {
    }

    public static List<FormationMapContact> buildSnapshot(ServerPlayer viewer) {
        if (viewer == null) return List.of();
        int viewDistanceBlocks = Math.max(16, viewer.server.getPlayerList().getViewDistance() * 16);
        double viewDistanceSqr = (double) viewDistanceBlocks * (double) viewDistanceBlocks;

        ServerLevel level = viewer.serverLevel();
        List<AbstractRecruitEntity> recruits = RecruitIndex.instance().all(level, true);
        if (recruits == null) {
            recruits = level.getEntitiesOfClass(
                    AbstractRecruitEntity.class,
                    new AABB(viewer.blockPosition()).inflate(viewDistanceBlocks)
            );
        }

        Map<UUID, Bucket> buckets = new LinkedHashMap<>();
        for (AbstractRecruitEntity recruit : recruits) {
            if (recruit == null || !recruit.isAlive() || recruit.distanceToSqr(viewer) > viewDistanceSqr) continue;
            CommandRole role = CommandHierarchy.roleFor(viewer, recruit);
            FormationMapRelation relation = relationFor(viewer, recruit, role);
            boolean subordinate = relation == FormationMapRelation.SUBORDINATE;
            boolean visible = subordinate || FormationMapVisibilityPolicy.canRevealContact(viewer, recruit, viewDistanceSqr);
            if (!visible) continue;

            UUID groupId = recruit.getGroup();
            UUID contactId = groupId == null ? recruit.getUUID() : groupId;
            Bucket bucket = buckets.computeIfAbsent(contactId, ignored -> new Bucket(contactId, groupId, recruit));
            bucket.add(recruit, role, relation, subordinate || visible);
        }

        List<FormationMapContact> contacts = new ArrayList<>(buckets.size());
        for (Bucket bucket : buckets.values()) {
            contacts.add(bucket.toContact());
        }
        return contacts;
    }

    private static FormationMapRelation relationFor(ServerPlayer viewer, AbstractRecruitEntity recruit, CommandRole role) {
        if (role != CommandRole.NONE) return FormationMapRelation.SUBORDINATE;
        String viewerTeam = viewer.getTeam() == null ? null : viewer.getTeam().getName();
        String recruitTeam = recruit.getTeam() == null ? null : recruit.getTeam().getName();
        if (viewerTeam != null && viewerTeam.equals(recruitTeam)) return FormationMapRelation.FRIENDLY;
        if (viewerTeam != null && recruitTeam != null && FactionEvents.recruitsDiplomacyManager != null) {
            RecruitsDiplomacyManager.DiplomacyStatus status = FactionEvents.recruitsDiplomacyManager.getRelation(viewerTeam, recruitTeam);
            if (status == RecruitsDiplomacyManager.DiplomacyStatus.ENEMY) return FormationMapRelation.HOSTILE;
            if (status == RecruitsDiplomacyManager.DiplomacyStatus.ALLY) return FormationMapRelation.FRIENDLY;
        }
        return FormationMapRelation.NEUTRAL;
    }

    private static final class Bucket {
        private final UUID contactId;
        private final UUID groupId;
        private final String teamId;
        private UUID leaderId;
        private FormationMapRelation relation = FormationMapRelation.NEUTRAL;
        private CommandRole commandRole = CommandRole.NONE;
        private double x;
        private double y;
        private double z;
        private int unitCount;
        private int visibleUnitCount;
        private int rangedUnitCount;

        private Bucket(UUID contactId, UUID groupId, AbstractRecruitEntity first) {
            this.contactId = contactId;
            this.groupId = groupId;
            this.teamId = first.getTeam() == null ? null : first.getTeam().getName();
        }

        private void add(AbstractRecruitEntity recruit, CommandRole role, FormationMapRelation newRelation, boolean visible) {
            unitCount++;
            if (visible) visibleUnitCount++;
            if (recruit instanceof IStrategicFire) rangedUnitCount++;
            if (leaderId == null) leaderId = recruit.getUUID();
            if (role.ordinal() > commandRole.ordinal()) commandRole = role;
            if (relationPriority(newRelation) > relationPriority(relation)) relation = newRelation;
            x += recruit.getX();
            y += recruit.getY();
            z += recruit.getZ();
        }

        private FormationMapContact toContact() {
            int divisor = Math.max(1, unitCount);
            return new FormationMapContact(
                    contactId,
                    groupId,
                    leaderId,
                    teamId,
                    relation,
                    commandRole,
                    x / divisor,
                    y / divisor,
                    z / divisor,
                    unitCount,
                    visibleUnitCount,
                    rangedUnitCount
            );
        }

        private static int relationPriority(FormationMapRelation relation) {
            return switch (relation) {
                case SUBORDINATE -> 4;
                case HOSTILE -> 3;
                case FRIENDLY -> 2;
                case NEUTRAL -> 1;
            };
        }
    }
}
