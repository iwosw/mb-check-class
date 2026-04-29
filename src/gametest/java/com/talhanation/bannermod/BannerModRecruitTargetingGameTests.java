package com.talhanation.bannermod;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.gametest.support.RecruitsBattleGameTestSupport;
import com.talhanation.bannermod.registry.military.ModEntityTypes;
import com.talhanation.bannermod.war.WarRuntimeContext;
import com.talhanation.bannermod.war.runtime.WarGoalType;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder(BannerModMain.MOD_ID)
public class BannerModRecruitTargetingGameTests {
    private static final UUID OWNER_UUID = UUID.fromString("00000000-0000-0000-0000-000000007701");
    private static final UUID NEUTRAL_UUID = UUID.fromString("00000000-0000-0000-0000-000000007702");
    private static final UUID HOSTILE_UUID = UUID.fromString("00000000-0000-0000-0000-000000007703");
    private static final String OWNER_FACTION = "vanilla007_target_owner";
    private static final String NEUTRAL_FACTION = "vanilla007_target_neutral";
    private static final String HOSTILE_FACTION = "vanilla007_target_hostile";

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void recruitDropsProtectedClaimTargetAndRetainsWarTarget(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos claimPos = helper.absolutePos(new BlockPos(4, 2, 4));
        ServerPlayer owner = createPlayer(level, claimPos, OWNER_UUID, "target-owner", OWNER_FACTION);
        ServerPlayer neutralLeader = createPlayer(level, helper.absolutePos(new BlockPos(2, 2, 4)), NEUTRAL_UUID, "target-neutral", NEUTRAL_FACTION);
        ServerPlayer hostileLeader = createPlayer(level, helper.absolutePos(new BlockPos(2, 2, 6)), HOSTILE_UUID, "target-hostile", HOSTILE_FACTION);

        BannerModDedicatedServerGameTestSupport.seedClaim(level, claimPos, OWNER_FACTION, owner.getUUID(), owner.getScoreboardName());

        AbstractRecruitEntity neutralRecruit = spawnRecruit(helper, new BlockPos(2, 2, 3), "neutral-recruit", NEUTRAL_UUID, NEUTRAL_FACTION);
        helper.assertFalse(neutralRecruit.canAttack(owner),
                "Expected neutral recruit targeting to respect protected claim authority");
        neutralRecruit.setTarget(owner);
        neutralRecruit.tick();
        helper.assertTrue(neutralRecruit.getTarget() == null,
                "Expected neutral recruit to drop a retained protected claim target on tick");

        AbstractRecruitEntity ownerRecruit = spawnRecruit(helper, new BlockPos(5, 2, 4), "owner-recruit", OWNER_UUID, OWNER_FACTION);
        helper.assertFalse(neutralRecruit.canAttack(ownerRecruit),
                "Expected neutral recruit to reject protected allied/owner recruit inside the claim");

        declareWar(level, HOSTILE_FACTION, OWNER_FACTION);
        AbstractRecruitEntity hostileRecruit = spawnRecruit(helper, new BlockPos(2, 2, 7), "hostile-recruit", HOSTILE_UUID, HOSTILE_FACTION);
        helper.assertTrue(hostileRecruit.canAttack(owner),
                "Expected declared war to authorize hostile target acquisition inside the protected claim");
        hostileRecruit.setTarget(owner);
        hostileRecruit.tick();
        helper.assertTrue(hostileRecruit.getTarget() == owner,
                "Expected war-authorized hostile target to stay retained after recruit tick");
        helper.succeed();
    }

    private static ServerPlayer createPlayer(ServerLevel level, BlockPos spawnPos, UUID playerId, String name, String factionId) {
        ServerPlayer player = (ServerPlayer) BannerModDedicatedServerGameTestSupport.createPositionedFakeServerPlayer(level, playerId, name, spawnPos);
        BannerModDedicatedServerGameTestSupport.ensureFaction(level, factionId, playerId, name);
        BannerModDedicatedServerGameTestSupport.joinTeam(level, factionId, player);
        return player;
    }

    private static AbstractRecruitEntity spawnRecruit(GameTestHelper helper, BlockPos relativePos, String name, UUID ownerId, String factionId) {
        AbstractRecruitEntity recruit = RecruitsBattleGameTestSupport.spawnConfiguredRecruit(
                helper,
                ModEntityTypes.RECRUIT.get(),
                relativePos,
                name,
                ownerId
        );
        BannerModDedicatedServerGameTestSupport.joinTeam(helper.getLevel(), factionId, recruit);
        recruit.setAggroState(1);
        return recruit;
    }

    private static void declareWar(ServerLevel level, String attackerFaction, String defenderFaction) {
        UUID attackerEntityId = WarRuntimeContext.registry(level).byName(attackerFaction).orElseThrow().id();
        UUID defenderEntityId = WarRuntimeContext.registry(level).byName(defenderFaction).orElseThrow().id();
        WarRuntimeContext.declarations(level).declareWar(
                attackerEntityId,
                defenderEntityId,
                WarGoalType.WHITE_PEACE,
                "vanilla-007-gametest",
                List.of(),
                List.of(),
                List.of(),
                level.getGameTime(),
                0L
        ).orElseThrow();
    }
}
