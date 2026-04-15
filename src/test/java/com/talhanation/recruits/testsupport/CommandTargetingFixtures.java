package com.talhanation.recruits.testsupport;

import com.talhanation.recruits.network.CommandTargeting;

import java.util.List;
import java.util.UUID;

public final class CommandTargetingFixtures {

    public static final UUID OWNER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000101");
    public static final UUID ALLIED_UUID = UUID.fromString("00000000-0000-0000-0000-000000000102");
    public static final UUID FOREIGN_OWNER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000202");
    public static final UUID GROUP_ALPHA_UUID = UUID.fromString("00000000-0000-0000-0000-000000000301");
    public static final UUID GROUP_BETA_UUID = UUID.fromString("00000000-0000-0000-0000-000000000302");
    public static final String TEAM_ALPHA = "alpha";
    public static final String TEAM_BETA = "beta";
    public static final UUID ALPHA_NEAR_UUID = UUID.fromString("00000000-0000-0000-0000-000000000401");
    public static final UUID ALPHA_FAR_UUID = UUID.fromString("00000000-0000-0000-0000-000000000402");
    public static final UUID FOREIGN_NEAR_UUID = UUID.fromString("00000000-0000-0000-0000-000000000403");
    public static final UUID BETA_NEAR_UUID = UUID.fromString("00000000-0000-0000-0000-000000000404");
    public static final UUID QUIET_NEAR_UUID = UUID.fromString("00000000-0000-0000-0000-000000000405");

    private CommandTargetingFixtures() {
    }

    public static List<CommandTargeting.RecruitSnapshot> sampleNearbyRecruits() {
        return List.of(
                recruit(ALPHA_NEAR_UUID, OWNER_UUID, TEAM_ALPHA, GROUP_ALPHA_UUID, 99.0D),
                recruit(ALPHA_FAR_UUID, OWNER_UUID, TEAM_ALPHA, GROUP_ALPHA_UUID, 101.0D),
                recruit(FOREIGN_NEAR_UUID, FOREIGN_OWNER_UUID, TEAM_BETA, GROUP_ALPHA_UUID, 40.0D),
                recruit(BETA_NEAR_UUID, OWNER_UUID, TEAM_ALPHA, GROUP_BETA_UUID, 32.0D),
                silentRecruit(QUIET_NEAR_UUID, OWNER_UUID, TEAM_ALPHA, GROUP_ALPHA_UUID, 20.0D)
        );
    }

    public static CommandTargeting.RecruitSnapshot recruit(UUID recruitUuid, UUID ownerUuid, String teamId, UUID groupUuid, double distance) {
        return new CommandTargeting.RecruitSnapshot(recruitUuid, ownerUuid, teamId, groupUuid, true, true, true, distance * distance);
    }

    public static CommandTargeting.RecruitSnapshot silentRecruit(UUID recruitUuid, UUID ownerUuid, String teamId, UUID groupUuid, double distance) {
        return new CommandTargeting.RecruitSnapshot(recruitUuid, ownerUuid, teamId, groupUuid, true, true, false, distance * distance);
    }
}
