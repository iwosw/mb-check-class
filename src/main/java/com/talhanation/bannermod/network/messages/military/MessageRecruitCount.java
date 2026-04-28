package com.talhanation.bannermod.network.messages.military;

import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.neoforged.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.UUID;

/**
 * Legacy diagnostic packet originally added in 2021 ({@code 4a32ddce "fixed aggro and raid"})
 * with a stub server-side handler. Preserved here as a no-op pass-through so the
 * {@link com.talhanation.bannermod.network.BannerModNetworkBootstrap#MILITARY_MESSAGES}
 * packet ID catalog ordering stays stable across the Phase 21 source consolidation.
 *
 * <p>The original implementation never ran anything beyond a commented-out loop. Reintroducing
 * the empty handler keeps the wire ID slot reserved without altering observable behavior. Any
 * future "recruit count" RPC should replace this class outright rather than threading new
 * logic through the deprecated payload.</p>
 */
public class MessageRecruitCount implements Message<MessageRecruitCount> {

    private int group;
    private UUID uuid;

    public MessageRecruitCount() {
    }

    public MessageRecruitCount(int group, UUID uuid) {
        this.group = group;
        this.uuid = uuid;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        // Intentional no-op: the original handler body was commented out at introduction.
    }

    @Override
    public MessageRecruitCount fromBytes(FriendlyByteBuf buf) {
        this.group = buf.readInt();
        this.uuid = buf.readUUID();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeInt(group);
        buf.writeUUID(uuid);
    }
}
