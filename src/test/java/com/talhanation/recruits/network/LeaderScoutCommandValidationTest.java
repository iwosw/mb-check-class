package com.talhanation.recruits.network;

import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class LeaderScoutCommandValidationTest {

    private static final UUID OWNER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000601");
    private static final UUID RECRUIT_UUID = UUID.fromString("00000000-0000-0000-0000-000000000602");

    @Test
    void patrolAndScoutValidationRejectForeignDeadOrOutOfRadiusSelections() {
        CommandTargeting.SingleRecruitSelection foreign = selection(CommandTargeting.Failure.NOT_OWNED_BY_SENDER);
        CommandTargeting.SingleRecruitSelection dead = selection(CommandTargeting.Failure.NOT_COMMANDABLE);
        CommandTargeting.SingleRecruitSelection far = selection(CommandTargeting.Failure.OUT_OF_RADIUS);

        assertEquals(MessagePatrolLeaderSetPatrolState.ValidationResult.INVALID_TARGET, MessagePatrolLeaderSetPatrolState.validateSelection(foreign, (byte) 1));
        assertEquals(MessagePatrolLeaderSetRoute.ValidationResult.INVALID_TARGET, MessagePatrolLeaderSetRoute.validateSelection(dead, UUID.randomUUID(), List.of(new BlockPos(1, 2, 3)), List.of(0)));
        assertEquals(MessageScoutTask.ValidationResult.INVALID_TARGET, MessageScoutTask.validateSelection(far, 1));
    }

    @Test
    void invalidPayloadsNeverProduceMutationReadyValidation() {
        CommandTargeting.SingleRecruitSelection validSelection = selection(CommandTargeting.Failure.NONE);

        assertEquals(MessagePatrolLeaderSetPatrolState.ValidationResult.INVALID_STATE, MessagePatrolLeaderSetPatrolState.validateSelection(validSelection, (byte) 99));
        assertEquals(MessagePatrolLeaderSetRoute.ValidationResult.INVALID_ROUTE_DATA, MessagePatrolLeaderSetRoute.validateSelection(validSelection, UUID.randomUUID(), List.of(new BlockPos(1, 2, 3)), List.of()));
        assertEquals(MessageScoutTask.ValidationResult.INVALID_STATE, MessageScoutTask.validateSelection(validSelection, 99));
    }

    @Test
    void validSelectionsRemainMutationReady() {
        CommandTargeting.SingleRecruitSelection validSelection = selection(CommandTargeting.Failure.NONE);

        assertEquals(MessagePatrolLeaderSetPatrolState.ValidationResult.OK, MessagePatrolLeaderSetPatrolState.validateSelection(validSelection, (byte) 1));
        assertEquals(MessagePatrolLeaderSetRoute.ValidationResult.OK, MessagePatrolLeaderSetRoute.validateSelection(validSelection, UUID.randomUUID(), List.of(new BlockPos(1, 2, 3)), List.of(5)));
        assertEquals(MessageScoutTask.ValidationResult.OK, MessageScoutTask.validateSelection(validSelection, 1));
    }

    private static CommandTargeting.SingleRecruitSelection selection(CommandTargeting.Failure failure) {
        if (failure == CommandTargeting.Failure.NONE) {
            return new CommandTargeting.SingleRecruitSelection(Optional.of(new CommandTargeting.RecruitSnapshot(
                    RECRUIT_UUID,
                    OWNER_UUID,
                    null,
                    null,
                    true,
                    true,
                    true,
                    1.0D
            )), CommandTargeting.Failure.NONE);
        }
        return CommandTargeting.SingleRecruitSelection.empty(failure);
    }
}
