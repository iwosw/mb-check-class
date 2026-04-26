package com.talhanation.bannermod.war.runtime;

import com.talhanation.bannermod.registry.war.ModWarBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class SiegeStandardBlockEntity extends BlockEntity {
    @Nullable
    private UUID warId;
    @Nullable
    private UUID sidePoliticalEntityId;

    public SiegeStandardBlockEntity(BlockPos pos, BlockState state) {
        super(ModWarBlockEntities.SIEGE_STANDARD.get(), pos, state);
    }

    public void bind(UUID warId, UUID sidePoliticalEntityId) {
        this.warId = warId;
        this.sidePoliticalEntityId = sidePoliticalEntityId;
        setChanged();
    }

    @Nullable
    public UUID warId() {
        return warId;
    }

    @Nullable
    public UUID sidePoliticalEntityId() {
        return sidePoliticalEntityId;
    }

    @Override
    protected void saveAdditional(CompoundTag tag) {
        super.saveAdditional(tag);
        if (warId != null) {
            tag.putUUID("WarId", warId);
        }
        if (sidePoliticalEntityId != null) {
            tag.putUUID("SidePoliticalEntityId", sidePoliticalEntityId);
        }
    }

    @Override
    public void load(CompoundTag tag) {
        super.load(tag);
        warId = tag.hasUUID("WarId") ? tag.getUUID("WarId") : null;
        sidePoliticalEntityId = tag.hasUUID("SidePoliticalEntityId")
                ? tag.getUUID("SidePoliticalEntityId")
                : null;
    }

    /**
     * Sync the bound war/side ids on chunk load so the client renderer can resolve the
     * political color without an extra packet round-trip.
     */
    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag tag = super.getUpdateTag();
        saveAdditional(tag);
        return tag;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
}
