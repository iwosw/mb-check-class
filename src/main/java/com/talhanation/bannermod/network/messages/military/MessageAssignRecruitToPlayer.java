package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.army.command.RecruitCommandAuthority;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.UUID;

public class MessageAssignRecruitToPlayer implements Message<MessageAssignRecruitToPlayer> {

    private UUID recruit;
    private UUID newOwner;
    public MessageAssignRecruitToPlayer() {
    }

    public MessageAssignRecruitToPlayer(UUID recruit, UUID newOwner) {
        this.recruit = recruit;
        this.newOwner = newOwner;
    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(NetworkEvent.Context context) {
        ServerPlayer serverPlayer = Objects.requireNonNull(context.getSender());

        AbstractRecruitEntity recruit = RecruitMessageEntityResolver.resolveRecruitInInflatedBox(serverPlayer, this.recruit, 64.0D);
        assignRecruitToPlayer(serverPlayer, recruit, newOwner);
    }

    static boolean assignRecruitToPlayer(ServerPlayer serverPlayer, AbstractRecruitEntity recruit, UUID newOwner) {
        if (recruit == null || !canTransferRecruit(serverPlayer, recruit)) {
            return false;
        }
        recruit.assignToPlayer(newOwner, null);
        return true;
    }

    static boolean canTransferRecruit(ServerPlayer serverPlayer, AbstractRecruitEntity recruit) {
        return RecruitCommandAuthority.canDirectlyControl(serverPlayer, recruit) || serverPlayer.hasPermissions(2);
    }

    public MessageAssignRecruitToPlayer fromBytes(FriendlyByteBuf buf) {
        this.recruit = buf.readUUID();
        this.newOwner = buf.readUUID();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.recruit);
        buf.writeUUID(this.newOwner);
    }
}
