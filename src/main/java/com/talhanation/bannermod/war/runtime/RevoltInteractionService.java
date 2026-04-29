package com.talhanation.bannermod.war.runtime;

import java.util.UUID;

public final class RevoltInteractionService {
    private RevoltInteractionService() {
    }

    public static Result resolve(RevoltRuntime revolts,
                                 OccupationRuntime occupations,
                                 WarOutcomeApplier applier,
                                 UUID revoltId,
                                 RevoltState outcome,
                                 boolean operator,
                                 long gameTime) {
        if (!operator) {
            return Result.denied("op_only");
        }
        if (revolts == null || occupations == null || applier == null || revoltId == null) {
            return Result.denied("missing_context");
        }
        if (outcome != RevoltState.SUCCESS && outcome != RevoltState.FAILED) {
            return Result.denied("invalid_outcome");
        }
        RevoltRecord record = revolts.byId(revoltId).orElse(null);
        if (record == null) {
            return Result.denied("not_found");
        }
        if (record.state() != RevoltState.PENDING) {
            return Result.denied("not_pending");
        }
        if (outcome == RevoltState.SUCCESS && !applier.removeOccupationOnRevoltSuccess(record.occupationId(), gameTime)) {
            return Result.denied("occupation_missing");
        }
        if (!revolts.resolve(record.id(), outcome, gameTime)) {
            return Result.denied("resolve_failed");
        }
        if (outcome == RevoltState.FAILED) {
            OccupationRecord occupation = occupations.byId(record.occupationId()).orElse(null);
            if (occupation == null) {
                applier.recordRevoltFailureWithoutOccupation(record, gameTime);
            } else {
                applier.recordRevoltFailure(record, occupation, gameTime, 0, 1);
            }
        }
        return Result.allowed(outcome);
    }

    public record Result(boolean allowed, String reason, RevoltState outcome) {
        static Result allowed(RevoltState outcome) {
            return new Result(true, "ok", outcome);
        }

        static Result denied(String reason) {
            return new Result(false, reason, null);
        }
    }
}
