package com.talhanation.bannermod.network.messages.military;

import com.talhanation.bannermod.army.command.RecruitSelectionService;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;

/**
 * Client → server: "these are the recruit UUIDs I just selected with the drag-box".
 * Server re-validates (owner match, radius, alive) before committing to the
 * {@link com.talhanation.bannermod.army.command.RecruitSelectionRegistry}.
 */
public class MessageSelectRecruits implements Message<MessageSelectRecruits> {

    public static final double SELECTION_RADIUS = 96.0D;

    public List<UUID> recruitUuids = new ArrayList<>();
    public boolean clearFirst;

    public MessageSelectRecruits() {
    }

    public MessageSelectRecruits(List<UUID> recruitUuids, boolean clearFirst) {
        this.recruitUuids = new ArrayList<>(recruitUuids == null ? List.of() : recruitUuids);
        this.clearFirst = clearFirst;
    }

    @Override
    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    @Override
    public void executeServerSide(NetworkEvent.Context context) {
        ServerPlayer player = Objects.requireNonNull(context.getSender());
        Set<UUID> set = new LinkedHashSet<>(this.recruitUuids);
        if (this.clearFirst || set.isEmpty()) {
            RecruitSelectionService.selectExplicit(player, set, SELECTION_RADIUS);
        } else {
            // Additive mode (shift+drag) — merge with current selection.
            Set<UUID> current = new LinkedHashSet<>(
                    com.talhanation.bannermod.army.command.RecruitSelectionRegistry.instance().get(player.getUUID()));
            current.addAll(set);
            RecruitSelectionService.selectExplicit(player, current, SELECTION_RADIUS);
        }
    }

    @Override
    public MessageSelectRecruits fromBytes(FriendlyByteBuf buf) {
        int size = buf.readVarInt();
        this.recruitUuids = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            this.recruitUuids.add(buf.readUUID());
        }
        this.clearFirst = buf.readBoolean();
        return this;
    }

    @Override
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeVarInt(this.recruitUuids.size());
        for (UUID uuid : this.recruitUuids) {
            buf.writeUUID(uuid);
        }
        buf.writeBoolean(this.clearFirst);
    }
}
