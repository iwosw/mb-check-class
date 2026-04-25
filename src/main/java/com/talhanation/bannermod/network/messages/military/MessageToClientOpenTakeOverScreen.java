package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.client.military.gui.faction.TakeOverScreen;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.client.Minecraft;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.world.entity.player.Player;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;
public class MessageToClientOpenTakeOverScreen implements Message<MessageToClientOpenTakeOverScreen> {

    private int entityId;
    private UUID recruit;

    public MessageToClientOpenTakeOverScreen() {

    }

    public MessageToClientOpenTakeOverScreen(UUID recruit) {
        this.entityId = -1;
        this.recruit = recruit;
    }

    public MessageToClientOpenTakeOverScreen(int entityId, UUID recruit) {
        this.entityId = entityId;
        this.recruit = recruit;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.CLIENT;
    }

    @Override
    @OnlyIn(Dist.CLIENT)
    public void executeClientSide(NetworkEvent.Context context) {
        Player player = Minecraft.getInstance().player;
        if (player == null || player.level() == null) {
            return;
        }
        if (player.level().getEntity(this.entityId) instanceof AbstractRecruitEntity recruit
                && recruit.getUUID().equals(this.recruit)
                && recruit.isAlive()
                && player.getBoundingBox().inflate(16.0D).intersects(recruit.getBoundingBox())) {
            Minecraft.getInstance().setScreen(new TakeOverScreen(recruit, player));
            return;
        }
    }

    @Override
    public MessageToClientOpenTakeOverScreen fromBytes(FriendlyByteBuf buf) {
        this.entityId = buf.readVarInt();
        this.recruit = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(entityId);
        buf.writeUUID(recruit);
    }
}
