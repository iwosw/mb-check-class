package com.talhanation.bannermod.war.runtime;

import com.talhanation.bannermod.registry.war.ModWarBlocks;
import com.talhanation.bannermod.war.WarRuntimeContext;
import com.talhanation.bannermod.war.audit.WarAuditLogSavedData;
import com.talhanation.bannermod.war.config.WarServerConfig;
import com.talhanation.bannermod.war.registry.PoliticalEntityAuthority;
import com.talhanation.bannermod.war.registry.PoliticalEntityRecord;
import com.talhanation.bannermod.war.registry.PoliticalRegistryRuntime;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.block.entity.BlockEntity;

import javax.annotation.Nullable;
import java.util.Optional;
import java.util.UUID;

/**
 * Stateless service that gates and performs siege-standard placement.
 *
 * <p>Both {@code SiegeStandardCommands} (slash) and {@code MessagePlaceSiegeStandardHere}
 * (player-facing UI) call into this service so validation and audit are written once.
 * Validation results use a token enum so the caller picks the right localised message
 * (chat for the slash path, system message for the packet path).</p>
 */
public final class SiegeStandardPlacementService {

    private SiegeStandardPlacementService() {
    }

    public enum Outcome {
        OK,
        WAR_NOT_FOUND,
        WAR_CLOSED,
        SIDE_NOT_FOUND,
        SIDE_NOT_PARTICIPANT,
        NOT_LEADER,
        MISSING_POSITION,
        RUNTIME_REJECTED
    }

    public record Result(Outcome outcome, @Nullable SiegeStandardRecord record) {
        public boolean ok() {
            return outcome == Outcome.OK;
        }

        public static Result of(Outcome outcome) {
            return new Result(outcome, null);
        }

        public static Result success(SiegeStandardRecord record) {
            return new Result(Outcome.OK, record);
        }
    }

    /**
     * Runs validation only; does not mutate world or runtime. Useful when the UI wants to
     * pre-grey a button without triggering audit/place side-effects.
     */
    public static Outcome validate(ServerLevel level,
                                   @Nullable ServerPlayer actor,
                                   @Nullable UUID warId,
                                   @Nullable UUID sideId) {
        if (level == null || warId == null || sideId == null) {
            return Outcome.WAR_NOT_FOUND;
        }
        Optional<WarDeclarationRecord> warOpt = WarRuntimeContext.declarations(level).byId(warId);
        if (warOpt.isEmpty()) {
            return Outcome.WAR_NOT_FOUND;
        }
        WarDeclarationRecord war = warOpt.get();
        if (war.state() == WarState.RESOLVED || war.state() == WarState.CANCELLED) {
            return Outcome.WAR_CLOSED;
        }
        PoliticalRegistryRuntime registry = WarRuntimeContext.registry(level);
        Optional<PoliticalEntityRecord> sideOpt = registry.byId(sideId);
        if (sideOpt.isEmpty()) {
            return Outcome.SIDE_NOT_FOUND;
        }
        PoliticalEntityRecord side = sideOpt.get();
        if (!war.involves(side.id())) {
            return Outcome.SIDE_NOT_PARTICIPANT;
        }
        if (!PoliticalEntityAuthority.isLeaderOrOp(actor, side)) {
            return Outcome.NOT_LEADER;
        }
        return Outcome.OK;
    }

    /**
     * Validate, then place a siege standard at {@code pos} (or the actor's position when
     * {@code pos} is null). Writes the runtime record, places the visible block, binds the
     * block entity, and appends a SIEGE_PLACED audit entry on success.
     */
    public static Result placeAt(ServerLevel level,
                                 @Nullable ServerPlayer actor,
                                 @Nullable UUID warId,
                                 @Nullable UUID sideId,
                                 @Nullable BlockPos pos,
                                 int requestedRadius) {
        Outcome validation = validate(level, actor, warId, sideId);
        if (validation != Outcome.OK) {
            return Result.of(validation);
        }
        BlockPos placePos = pos;
        if (placePos == null) {
            if (actor == null) {
                return Result.of(Outcome.MISSING_POSITION);
            }
            placePos = actor.blockPosition();
        }
        int radius = requestedRadius > 0 ? requestedRadius : WarServerConfig.DefaultSiegeRadius.get();
        SiegeStandardRuntime runtime = WarRuntimeContext.sieges(level);
        long gameTime = level.getGameTime();
        Optional<SiegeStandardRecord> placed = runtime.place(warId, sideId, placePos, radius, gameTime);
        if (placed.isEmpty()) {
            return Result.of(Outcome.RUNTIME_REJECTED);
        }
        level.setBlockAndUpdate(placePos, ModWarBlocks.SIEGE_STANDARD.get().defaultBlockState());
        BlockEntity blockEntity = level.getBlockEntity(placePos);
        if (blockEntity instanceof SiegeStandardBlockEntity siegeBe) {
            siegeBe.bind(warId, sideId);
        }
        WarAuditLogSavedData audit = WarRuntimeContext.audit(level);
        SiegeStandardRecord record = placed.get();
        audit.append(warId, "SIEGE_PLACED",
                "side=" + sideId + ";pos=" + placePos.toShortString() + ";radius=" + radius,
                gameTime);
        return Result.success(record);
    }

    /** English summary suitable for chat/system-message feedback. */
    public static String describe(Outcome outcome) {
        return switch (outcome) {
            case OK -> "Siege standard placed.";
            case WAR_NOT_FOUND -> "War not found.";
            case WAR_CLOSED -> "War is closed; cannot place a siege standard.";
            case SIDE_NOT_FOUND -> "Side not found.";
            case SIDE_NOT_PARTICIPANT -> "Side is not a participant of this war.";
            case NOT_LEADER -> "Only the political entity leader (or an op) can do that.";
            case MISSING_POSITION -> "No placement position available.";
            case RUNTIME_REJECTED -> "Failed to place siege standard.";
        };
    }
}
