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

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.UUID;

public class MessageAggro implements Message<MessageAggro> {

    private UUID player;
    private UUID recruit;
    private int state;
    private UUID group;
    private boolean fromGui;


    public MessageAggro() {
    }

    public MessageAggro(UUID player, int state, UUID group) {
        this.player = player;
        this.state = state;
        this.group = group;
        this.fromGui = false;
        this.recruit = null;
    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(NetworkEvent.Context context) {
        ServerPlayer player = Objects.requireNonNull(context.getSender());

        double boundBoxInflateModifier = fromGui ? 16.0D : 100.0D;

        List<AbstractRecruitEntity> pool = player.getCommandSenderWorld().getEntitiesOfClass(
                AbstractRecruitEntity.class,
                player.getBoundingBox().inflate(boundBoxInflateModifier)
        );

        List<AbstractRecruitEntity> actors = new ArrayList<>();
        for (AbstractRecruitEntity recruit : pool) {
            if (fromGui && !recruit.getUUID().equals(this.recruit)) {
                continue;
            }
            actors.add(recruit);
        }
        if (actors.isEmpty()) {
            return;
        }

        long gameTime = player.getCommandSenderWorld().getGameTime();
        CommandIntent intent = new CommandIntent.Aggro(
                gameTime,
                CommandIntentPriority.NORMAL,
                false,
                this.state,
                this.group,
                this.fromGui
        );
        CommandIntentDispatcher.dispatch(player, intent, actors);
    }

    public MessageAggro fromBytes(FriendlyByteBuf buf) {
        this.player = buf.readUUID();
        this.state = buf.readInt();
        this.group = buf.readUUID();
        if (this.recruit != null) this.recruit = buf.readUUID();
        this.fromGui = buf.readBoolean();
        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUUID(this.player);
        buf.writeInt(this.state);
        buf.writeUUID(this.group);
        buf.writeBoolean(this.fromGui);
        if (this.recruit != null) buf.writeUUID(this.recruit);
    }
}