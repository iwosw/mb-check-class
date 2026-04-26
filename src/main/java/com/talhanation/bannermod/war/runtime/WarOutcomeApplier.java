package com.talhanation.bannermod.war.runtime;

import com.talhanation.bannermod.war.audit.WarAuditLogSavedData;
import com.talhanation.bannermod.war.registry.PoliticalEntityStatus;
import com.talhanation.bannermod.war.registry.PoliticalRegistryRuntime;
import net.minecraft.world.level.ChunkPos;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public final class WarOutcomeApplier {
    private final WarDeclarationRuntime declarations;
    private final SiegeStandardRuntime sieges;
    private final WarAuditLogSavedData audit;
    private final OccupationRuntime occupations;
    private final DemilitarizationRuntime demilitarizations;
    private final PoliticalRegistryRuntime registry;

    public WarOutcomeApplier(WarDeclarationRuntime declarations,
                             SiegeStandardRuntime sieges,
                             WarAuditLogSavedData audit) {
        this(declarations, sieges, audit, null, null, null);
    }

    public WarOutcomeApplier(WarDeclarationRuntime declarations,
                             SiegeStandardRuntime sieges,
                             WarAuditLogSavedData audit,
                             OccupationRuntime occupations,
                             DemilitarizationRuntime demilitarizations,
                             PoliticalRegistryRuntime registry) {
        this.declarations = declarations;
        this.sieges = sieges;
        this.audit = audit;
        this.occupations = occupations;
        this.demilitarizations = demilitarizations;
        this.registry = registry;
    }

    public Result applyWhitePeace(UUID warId, long gameTime) {
        Optional<WarDeclarationRecord> existing = declarations.byId(warId);
        if (existing.isEmpty()) {
            return Result.invalid("unknown_war");
        }
        WarDeclarationRecord war = existing.get();
        if (war.state() == WarState.RESOLVED || war.state() == WarState.CANCELLED) {
            return Result.invalid("already_closed");
        }
        declarations.updateState(warId, WarState.RESOLVED);
        clearSieges(warId);
        audit.append(warId, "OUTCOME_APPLIED", "type=WHITE_PEACE", gameTime);
        return Result.ok(WarOutcomeType.WHITE_PEACE);
    }

    public Result applyTribute(UUID warId, long tributeAmount, long gameTime) {
        Optional<WarDeclarationRecord> existing = declarations.byId(warId);
        if (existing.isEmpty()) {
            return Result.invalid("unknown_war");
        }
        if (tributeAmount < 0) {
            return Result.invalid("negative_tribute");
        }
        WarDeclarationRecord war = existing.get();
        if (war.state() == WarState.RESOLVED || war.state() == WarState.CANCELLED) {
            return Result.invalid("already_closed");
        }
        declarations.updateState(warId, WarState.RESOLVED);
        clearSieges(warId);
        audit.append(warId, "OUTCOME_APPLIED", "type=TRIBUTE;amount=" + tributeAmount, gameTime);
        return Result.ok(WarOutcomeType.TRIBUTE);
    }

    public Result cancel(UUID warId, long gameTime, String reason) {
        Optional<WarDeclarationRecord> existing = declarations.byId(warId);
        if (existing.isEmpty()) {
            return Result.invalid("unknown_war");
        }
        WarDeclarationRecord war = existing.get();
        if (war.state() != WarState.DECLARED) {
            return Result.invalid("not_cancellable");
        }
        declarations.updateState(warId, WarState.CANCELLED);
        clearSieges(warId);
        audit.append(warId, "WAR_CANCELLED", "reason=" + (reason == null ? "" : reason), gameTime);
        return Result.ok(WarOutcomeType.WHITE_PEACE);
    }

    public Result applyVassalize(UUID warId, long gameTime) {
        if (registry == null) {
            return Result.invalid("no_registry");
        }
        Optional<WarDeclarationRecord> existing = declarations.byId(warId);
        if (existing.isEmpty()) {
            return Result.invalid("unknown_war");
        }
        WarDeclarationRecord war = existing.get();
        if (war.state() == WarState.RESOLVED || war.state() == WarState.CANCELLED) {
            return Result.invalid("already_closed");
        }
        boolean changed = registry.updateStatus(war.defenderPoliticalEntityId(), PoliticalEntityStatus.VASSAL);
        if (!changed) {
            return Result.invalid("defender_not_found");
        }
        declarations.updateState(warId, WarState.RESOLVED);
        clearSieges(warId);
        audit.append(warId, "OUTCOME_APPLIED",
                "type=VASSALIZATION;defender=" + war.defenderPoliticalEntityId(), gameTime);
        return Result.ok(WarOutcomeType.VASSALIZATION);
    }

    public Result applyOccupation(UUID warId, List<ChunkPos> chunks, long gameTime) {
        if (occupations == null) {
            return Result.invalid("no_occupation_runtime");
        }
        if (chunks == null || chunks.isEmpty()) {
            return Result.invalid("no_chunks");
        }
        Optional<WarDeclarationRecord> existing = declarations.byId(warId);
        if (existing.isEmpty()) {
            return Result.invalid("unknown_war");
        }
        WarDeclarationRecord war = existing.get();
        if (war.state() == WarState.RESOLVED || war.state() == WarState.CANCELLED) {
            return Result.invalid("already_closed");
        }
        Optional<OccupationRecord> placed = occupations.place(
                warId,
                war.attackerPoliticalEntityId(),
                war.defenderPoliticalEntityId(),
                chunks, gameTime);
        if (placed.isEmpty()) {
            return Result.invalid("occupation_failed");
        }
        declarations.updateState(warId, WarState.RESOLVED);
        clearSieges(warId);
        audit.append(warId, "OUTCOME_APPLIED",
                "type=OCCUPATION;occupier=" + war.attackerPoliticalEntityId()
                        + ";occupied=" + war.defenderPoliticalEntityId()
                        + ";chunks=" + chunks.size(),
                gameTime);
        return Result.ok(WarOutcomeType.OCCUPATION);
    }

    public Result applyAnnex(UUID warId, int chunksTransferred, int chunkLimit, long gameTime) {
        Optional<WarDeclarationRecord> existing = declarations.byId(warId);
        if (existing.isEmpty()) {
            return Result.invalid("unknown_war");
        }
        WarDeclarationRecord war = existing.get();
        if (war.state() == WarState.RESOLVED || war.state() == WarState.CANCELLED) {
            return Result.invalid("already_closed");
        }
        if (chunksTransferred <= 0) {
            return Result.invalid("no_chunks_transferred");
        }
        if (chunksTransferred > chunkLimit) {
            return Result.invalid("annex_over_limit");
        }
        declarations.updateState(warId, WarState.RESOLVED);
        clearSieges(warId);
        audit.append(warId, "OUTCOME_APPLIED",
                "type=ANNEX_LIMITED_CHUNKS;chunks=" + chunksTransferred + ";limit=" + chunkLimit,
                gameTime);
        return Result.ok(WarOutcomeType.ANNEX_LIMITED_CHUNKS);
    }

    public Result applyDemilitarization(UUID warId, long durationTicks, long gameTime) {
        if (demilitarizations == null) {
            return Result.invalid("no_demilitarization_runtime");
        }
        if (durationTicks <= 0) {
            return Result.invalid("invalid_duration");
        }
        Optional<WarDeclarationRecord> existing = declarations.byId(warId);
        if (existing.isEmpty()) {
            return Result.invalid("unknown_war");
        }
        WarDeclarationRecord war = existing.get();
        if (war.state() == WarState.RESOLVED || war.state() == WarState.CANCELLED) {
            return Result.invalid("already_closed");
        }
        Optional<DemilitarizationRecord> imposed = demilitarizations.impose(
                war.defenderPoliticalEntityId(), warId, gameTime + durationTicks);
        if (imposed.isEmpty()) {
            return Result.invalid("demilitarization_failed");
        }
        declarations.updateState(warId, WarState.RESOLVED);
        clearSieges(warId);
        audit.append(warId, "OUTCOME_APPLIED",
                "type=FORCED_DEMILITARIZATION;defender=" + war.defenderPoliticalEntityId()
                        + ";endsAt=" + (gameTime + durationTicks),
                gameTime);
        return Result.ok(WarOutcomeType.FORCED_DEMILITARIZATION);
    }

    public boolean removeOccupationOnRevoltSuccess(UUID occupationId, long gameTime) {
        if (occupations == null) {
            return false;
        }
        Optional<OccupationRecord> existing = occupations.byId(occupationId);
        if (existing.isEmpty()) {
            return false;
        }
        OccupationRecord record = existing.get();
        boolean removed = occupations.remove(occupationId);
        if (removed) {
            audit.append(record.warId(), "REVOLT_SUCCESS",
                    "occupation=" + occupationId
                            + ";rebel=" + record.occupiedEntityId()
                            + ";occupier=" + record.occupierEntityId(),
                    gameTime);
        }
        return removed;
    }

    private void clearSieges(UUID warId) {
        for (SiegeStandardRecord record : sieges.forWar(warId)) {
            sieges.remove(record.id());
        }
    }

    public record Result(boolean valid, String reason, WarOutcomeType outcome) {
        public static Result ok(WarOutcomeType outcome) {
            return new Result(true, "", outcome);
        }

        public static Result invalid(String reason) {
            return new Result(false, reason, null);
        }
    }
}
