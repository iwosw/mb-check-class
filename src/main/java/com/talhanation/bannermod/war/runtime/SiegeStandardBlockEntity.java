package com.talhanation.bannermod.war.runtime;

import com.talhanation.bannermod.registry.war.ModWarBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
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
}
