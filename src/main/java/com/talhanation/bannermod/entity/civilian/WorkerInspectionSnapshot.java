package com.talhanation.bannermod.entity.civilian;

import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nullable;
import java.util.UUID;

public record WorkerInspectionSnapshot(
        int entityId,
        UUID workerUuid,
        String workerName,
        String professionKey,
        String ownerLabel,
        String politicalLabel,
        String claimRelationKey,
        String assignmentLabel,
        String problemLabel,
        String transportLabel,
        boolean canConvert,
        @Nullable String convertBlockedReasonKey
) {
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeUUID(workerUuid);
        buf.writeUtf(workerName);
        buf.writeUtf(professionKey);
        buf.writeUtf(ownerLabel);
        buf.writeUtf(politicalLabel);
        buf.writeUtf(claimRelationKey);
        buf.writeUtf(assignmentLabel);
        buf.writeUtf(problemLabel);
        buf.writeUtf(transportLabel);
        buf.writeBoolean(canConvert);
        buf.writeBoolean(convertBlockedReasonKey != null);
        if (convertBlockedReasonKey != null) {
            buf.writeUtf(convertBlockedReasonKey);
        }
    }

    public static WorkerInspectionSnapshot fromBytes(FriendlyByteBuf buf) {
        return new WorkerInspectionSnapshot(
                buf.readVarInt(),
                buf.readUUID(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readUtf(),
                buf.readBoolean(),
                buf.readBoolean() ? buf.readUtf() : null
        );
    }
}
