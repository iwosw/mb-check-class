package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.army.command.CommandIntent;
import com.talhanation.bannermod.army.command.CommandIntentDispatcher;
import com.talhanation.bannermod.army.command.CommandIntentPriority;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MessageFaceCommand implements Message<MessageFaceCommand> {

    private UUID player_uuid;
    private UUID group;
    private int formation;
    private boolean tight;

    public MessageFaceCommand(){
    }

    public MessageFaceCommand(UUID player_uuid, UUID group, int formation, boolean tight) {
        this.player_uuid = player_uuid;
        this.group = group;
        this.formation = formation;
        this.tight = tight;
    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(NetworkEvent.Context context){
        ServerPlayer sender = Objects.requireNonNull(context.getSender());
        List<AbstractRecruitEntity> list = sender.getCommandSenderWorld().getEntitiesOfClass(
                AbstractRecruitEntity.class, sender.getBoundingBox().inflate(100));
        list.removeIf(recruit -> !recruit.isEffectedByCommand(this.player_uuid, this.group));

        long gameTime = sender.getCommandSenderWorld().getGameTime();
        CommandIntent intent = new CommandIntent.Face(
                gameTime,
                CommandIntentPriority.NORMAL,
                false,
                this.formation,
                this.tight
        );
        CommandIntentDispatcher.dispatch(sender, intent, list);
    }

    public MessageFaceCommand fromBytes(FriendlyByteBuf buf) {
        this.player_uuid = buf.readUUID();
        this.group = buf.readUUID();
        this.formation = buf.readInt();
        this.tight = buf.readBoolean();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.player_uuid);
        buf.writeUUID(this.group);
        buf.writeInt(this.formation);
        buf.writeBoolean(this.tight);
    }

}
