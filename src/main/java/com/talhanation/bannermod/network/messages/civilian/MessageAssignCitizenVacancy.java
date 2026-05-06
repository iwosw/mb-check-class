package com.talhanation.bannermod.network.messages.civilian;

import com.talhanation.bannermod.entity.citizen.CitizenEntity;
import com.talhanation.bannermod.entity.civilian.workarea.AbstractWorkAreaEntity;
import com.talhanation.bannermod.network.compat.BannerModNetworkContext;
import com.talhanation.bannermod.network.payload.BannerModMessage;
import com.talhanation.bannermod.settlement.prefab.staffing.PrefabAutoStaffingRuntime;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.Entity;

import java.util.UUID;

/**
 * Player → server: explicitly bind a citizen to a specific work-area vacancy.
 *
 * <p>Unlike the auto-assign tick, this packet bypasses the distance and "nearest"
 * picking — the player has chosen which farm/mine/lumber-camp/etc. they want this
 * citizen to staff. Server validates ownership and that the work area exists,
 * then delegates to {@link PrefabAutoStaffingRuntime#assignCitizenToSpecificVacancy}.
 */
public class MessageAssignCitizenVacancy implements BannerModMessage<MessageAssignCitizenVacancy> {

    private UUID citizenUuid;
    private UUID anchorUuid;

    public MessageAssignCitizenVacancy() {
    }

    public MessageAssignCitizenVacancy(UUID citizenUuid, UUID anchorUuid) {
        this.citizenUuid = citizenUuid;
        this.anchorUuid = anchorUuid;
    }

    @Override
    public PacketFlow getExecutingSide() {
        return BannerModMessage.serverbound();
    }

    @Override
    public void executeServerSide(BannerModNetworkContext context) {
        context.enqueueWork(() -> {
            ServerPlayer sender = context.getSender();
            if (sender == null || this.citizenUuid == null || this.anchorUuid == null) return;
            ServerLevel level = sender.serverLevel();

            Entity citizenEntity = level.getEntity(this.citizenUuid);
            if (!(citizenEntity instanceof CitizenEntity citizen)) {
                sender.sendSystemMessage(Component.translatable("chat.bannermod.assign_vacancy.citizen_missing"));
                return;
            }
            if (!isOwnedBy(citizen, sender)) {
                sender.sendSystemMessage(Component.translatable("chat.bannermod.assign_vacancy.denied"));
                return;
            }
            // Don't reassign if the citizen has already converted into a worker/recruit on
            // a previous tick — at that point the citizen entity is gone or has an active
            // profession, and the manual binding would do nothing useful.
            if (citizen.activeProfession() != com.talhanation.bannermod.citizen.CitizenProfession.NONE) {
                sender.sendSystemMessage(Component.translatable("chat.bannermod.assign_vacancy.already_employed"));
                return;
            }

            Entity anchorEntity = level.getEntity(this.anchorUuid);
            if (!(anchorEntity instanceof AbstractWorkAreaEntity workArea) || !workArea.isAlive()) {
                sender.sendSystemMessage(Component.translatable("chat.bannermod.assign_vacancy.area_missing"));
                return;
            }
            if (!workArea.canPlayerSee(sender)) {
                sender.sendSystemMessage(Component.translatable("chat.bannermod.assign_vacancy.area_denied"));
                return;
            }

            boolean assigned = PrefabAutoStaffingRuntime.assignCitizenToSpecificVacancy(level, citizen, this.anchorUuid);
            if (assigned) {
                sender.sendSystemMessage(Component.translatable("chat.bannermod.assign_vacancy.ok"));
            } else {
                sender.sendSystemMessage(Component.translatable("chat.bannermod.assign_vacancy.no_profession"));
            }
        });
    }

    private static boolean isOwnedBy(CitizenEntity citizen, ServerPlayer sender) {
        if (sender.isCreative() && sender.hasPermissions(2)) return true;
        UUID owner = citizen.getOwnerUUID();
        return owner != null && owner.equals(sender.getUUID());
    }

    @Override
    public MessageAssignCitizenVacancy fromBytes(FriendlyByteBuf buf) {
        this.citizenUuid = buf.readUUID();
        this.anchorUuid = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.citizenUuid);
        buf.writeUUID(this.anchorUuid);
    }
}
