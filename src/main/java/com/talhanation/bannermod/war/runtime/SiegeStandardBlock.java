package com.talhanation.bannermod.war.runtime;

import com.talhanation.bannermod.war.WarRuntimeContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.UUID;

public class SiegeStandardBlock extends Block implements EntityBlock {
    public SiegeStandardBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SiegeStandardBlockEntity(pos, state);
    }

    @Override
    public void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean isMoving) {
        if (!level.isClientSide() && !state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof SiegeStandardBlockEntity siegeBe) {
                UUID warId = siegeBe.warId();
                if (warId != null && level instanceof ServerLevel serverLevel) {
                    SiegeStandardRuntime runtime = WarRuntimeContext.sieges(serverLevel);
                    for (SiegeStandardRecord record : runtime.forWar(warId)) {
                        if (record.pos().equals(pos)) {
                            runtime.remove(record.id());
                            WarRuntimeContext.audit(serverLevel).append(
                                    warId, "SIEGE_REMOVED",
                                    "pos=" + pos.toShortString() + ";via=block_break",
                                    serverLevel.getGameTime()
                            );
                            break;
                        }
                    }
                }
            }
        }
        super.onRemove(state, level, pos, newState, isMoving);
    }
}
