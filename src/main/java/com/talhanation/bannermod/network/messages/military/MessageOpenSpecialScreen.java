package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.compat.workers.IVillagerWorker;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.entity.military.ICompanion;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.Objects;
import java.util.UUID;

public class MessageOpenSpecialScreen implements Message<MessageOpenSpecialScreen> {

    private UUID player;
    private UUID recruit;

    public MessageOpenSpecialScreen() {
        this.player = new UUID(0, 0);
    }

    public MessageOpenSpecialScreen(Player player, UUID recruit) {
        this.player = player.getUUID();
        this.recruit = recruit;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        ServerPlayer player = Objects.requireNonNull(context.getSender());
        if (!player.getUUID().equals(this.player)) {
            return;
        }

        player.getCommandSenderWorld().getEntitiesOfClass(
                AbstractRecruitEntity.class,
                player.getBoundingBox().inflate(16.0D),
                v -> v.getUUID().equals(this.recruit) && v.isAlive()
        ).forEach((recruit) -> tryToOpenSpecialGUI(recruit, player));
    }

    private void tryToOpenSpecialGUI(AbstractRecruitEntity recruit, ServerPlayer player) {
        if(recruit instanceof ICompanion companion) {
            companion.openSpecialGUI(player);
        }
        else if (recruit instanceof IVillagerWorker worker){
            worker.openSpecialGUI(player);
        }
    }

    @Override
    public MessageOpenSpecialScreen fromBytes(FriendlyByteBuf buf) {
        this.player = buf.readUUID();
        this.recruit = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(player);
        buf.writeUUID(recruit);
    }
}