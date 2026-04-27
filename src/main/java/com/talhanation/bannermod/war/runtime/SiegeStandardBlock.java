package com.talhanation.bannermod.war.runtime;

import com.talhanation.bannermod.combat.SiegeObjectivePolicy;
import com.talhanation.bannermod.war.WarRuntimeContext;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;
import java.util.UUID;

public class SiegeStandardBlock extends Block implements EntityBlock {

    /** Damage per direct player attack. The default control pool is 100, so a sustained
     *  assault of ~25 hits drains a full standard — long enough that a lone attacker can't
     *  trivially destroy it but short enough that a coordinated team can within a battle. */
    public static final int ATTACK_DAMAGE = 4;
    public SiegeStandardBlock(Properties properties) {
        super(properties);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new SiegeStandardBlockEntity(pos, state);
    }

    /**
     * Player punching the standard drains its control pool (COMBAT-006). The destroy +
     * audit + block-removal sequence runs through {@link SiegeStandardRuntime#applyDamage}
     * so the {@code SIEGE_STANDARD_DESTROYED} audit row fires exactly once even if the
     * block stays in-world for a tick after the pool hits zero.
     */
    @Override
    public void attack(BlockState state, Level level, BlockPos pos, Player player) {
        super.attack(state, level, pos, player);
        if (level.isClientSide() || !(level instanceof ServerLevel serverLevel)) {
            return;
        }
        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof SiegeStandardBlockEntity siegeBe)) return;
        UUID warId = siegeBe.warId();
        if (warId == null) return;
        SiegeStandardRuntime runtime = WarRuntimeContext.sieges(serverLevel);
        Optional<SiegeStandardRecord> match = runtime.byPos(pos);
        if (match.isEmpty()) return;
        SiegeStandardRecord record = match.get();
        // Reject same-side attacks at the policy level so a player on the standard's faction
        // cannot accidentally drain their own pool.
        UUID attackerSide = WarRuntimeContext.factionOf(serverLevel, player);
        if (!SiegeObjectivePolicy.canAttackStandard(attackerSide, record.sidePoliticalEntityId())) {
            return;
        }
        Optional<SiegeObjectivePolicy.DamageOutcome> outcome = runtime.applyDamage(record.id(), ATTACK_DAMAGE);
        if (outcome.isEmpty()) return;
        SiegeObjectivePolicy.DamageOutcome o = outcome.get();
        WarRuntimeContext.audit(serverLevel).append(
                warId, "SIEGE_STANDARD_HIT",
                "id=" + record.id() + ";pos=" + pos.toShortString()
                        + ";control=" + o.controlAfter() + "/" + record.maxControlPool()
                        + ";attacker=" + player.getName().getString(),
                serverLevel.getGameTime()
        );
        if (o.destroyed()) {
            WarRuntimeContext.audit(serverLevel).append(
                    warId, "SIEGE_STANDARD_DESTROYED",
                    "id=" + record.id() + ";pos=" + pos.toShortString()
                            + ";attacker=" + player.getName().getString(),
                    serverLevel.getGameTime()
            );
            runtime.remove(record.id());
            level.destroyBlock(pos, false);
        }
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
