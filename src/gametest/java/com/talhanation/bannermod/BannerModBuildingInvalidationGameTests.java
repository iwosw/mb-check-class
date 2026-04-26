package com.talhanation.bannermod;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.config.WorkersServerConfig;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.settlement.building.BuildingType;
import com.talhanation.bannermod.settlement.building.BuildingValidationState;
import com.talhanation.bannermod.settlement.building.BuildingDefinitionRegistry;
import com.talhanation.bannermod.settlement.building.ValidatedBuildingRecord;
import com.talhanation.bannermod.settlement.building.ValidatedBuildingRegistryData;
import com.talhanation.bannermod.settlement.bootstrap.BootstrapResult;
import com.talhanation.bannermod.settlement.bootstrap.SettlementBootstrapService;
import com.talhanation.bannermod.settlement.bootstrap.SettlementRegistryData;
import com.talhanation.bannermod.settlement.building.ZoneRole;
import com.talhanation.bannermod.settlement.building.ZoneSelection;
import com.talhanation.bannermod.settlement.validation.BuildingValidationRequest;
import com.talhanation.bannermod.settlement.validation.BuildingValidationResult;
import com.talhanation.bannermod.settlement.validation.BuildingInvalidationQueueData;
import com.talhanation.bannermod.settlement.validation.BuildingInvalidationReason;
import com.talhanation.bannermod.settlement.validation.BuildingInvalidationRuntime;
import com.talhanation.bannermod.settlement.validation.DefaultBuildingValidator;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraftforge.gametest.GameTestHolder;
import net.minecraftforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

@GameTestHolder(BannerModMain.MOD_ID)
public class BannerModBuildingInvalidationGameTests {

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void failingHouseRevalidationBecomesDegradedWithinGrace(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        WorkersServerConfig.SettlementHouseGraceTicks.set(24_000L);

        UUID buildingId = UUID.randomUUID();
        ValidatedBuildingRegistryData registry = ValidatedBuildingRegistryData.get(level);
        registry.registerBuilding(newRecord(buildingId, BuildingType.HOUSE, BuildingValidationState.VALID, 0L, level.getGameTime()));
        BuildingInvalidationQueueData.get(level).enqueue(buildingId, BuildingInvalidationReason.BLOCK_BROKEN, level.getGameTime());

        BuildingInvalidationRuntime.tickBatch(level, 8);
        ValidatedBuildingRecord updated = registry.getById(buildingId);
        helper.assertTrue(updated != null && updated.state() == BuildingValidationState.DEGRADED,
                "Expected failing house to transition to DEGRADED while grace period is active.");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void failingHouseRevalidationBecomesInvalidAfterGrace(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        WorkersServerConfig.SettlementHouseGraceTicks.set(1L);

        UUID buildingId = UUID.randomUUID();
        long now = level.getGameTime();
        ValidatedBuildingRegistryData registry = ValidatedBuildingRegistryData.get(level);
        registry.registerBuilding(newRecord(buildingId, BuildingType.HOUSE, BuildingValidationState.DEGRADED, now - 10L, now));
        BuildingInvalidationQueueData.get(level).enqueue(buildingId, BuildingInvalidationReason.BLOCK_BROKEN, now);

        BuildingInvalidationRuntime.tickBatch(level, 8);
        ValidatedBuildingRecord updated = registry.getById(buildingId);
        helper.assertTrue(updated != null && updated.state() == BuildingValidationState.INVALID,
                "Expected failing house to transition to INVALID after grace period elapsed.");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void failingFortRevalidationBecomesSuspendedBeforeGrace(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        WorkersServerConfig.SettlementFortGraceTicks.set(24_000L);

        UUID buildingId = UUID.randomUUID();
        long now = level.getGameTime();
        ValidatedBuildingRegistryData registry = ValidatedBuildingRegistryData.get(level);
        registry.registerBuilding(newRecord(buildingId, BuildingType.STARTER_FORT, BuildingValidationState.VALID, 0L, now));
        BuildingInvalidationQueueData.get(level).enqueue(buildingId, BuildingInvalidationReason.BLOCK_BROKEN, now);

        BuildingInvalidationRuntime.tickBatch(level, 8);
        ValidatedBuildingRecord updated = registry.getById(buildingId);
        helper.assertTrue(updated != null && updated.state() == BuildingValidationState.SUSPENDED,
                "Expected failing fort to transition to SUSPENDED while grace period is active.");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void failingFortExplosionRevalidationBecomesInvalidAfterExplosionGrace(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        WorkersServerConfig.SettlementFortExplosionGraceTicks.set(1L);

        UUID buildingId = UUID.randomUUID();
        long now = level.getGameTime();
        ValidatedBuildingRegistryData registry = ValidatedBuildingRegistryData.get(level);
        registry.registerBuilding(newRecord(buildingId, BuildingType.STARTER_FORT, BuildingValidationState.SUSPENDED, now - 10L, now));
        BuildingInvalidationQueueData.get(level).enqueue(buildingId, BuildingInvalidationReason.EXPLOSION, now);

        BuildingInvalidationRuntime.tickBatch(level, 8);
        ValidatedBuildingRecord updated = registry.getById(buildingId);
        helper.assertTrue(updated != null && updated.state() == BuildingValidationState.INVALID,
                "Expected failing fort to transition to INVALID after explosion grace elapsed.");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void invalidationQueueRespectsBatchDrainLimit(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        WorkersServerConfig.SettlementHouseGraceTicks.set(24_000L);

        UUID buildingIdA = UUID.randomUUID();
        UUID buildingIdB = UUID.randomUUID();
        UUID buildingIdC = UUID.randomUUID();
        long now = level.getGameTime();
        ValidatedBuildingRegistryData registry = ValidatedBuildingRegistryData.get(level);
        registry.registerBuilding(newRecord(buildingIdA, BuildingType.HOUSE, BuildingValidationState.VALID, 0L, now));
        registry.registerBuilding(newRecord(buildingIdB, BuildingType.HOUSE, BuildingValidationState.VALID, 0L, now));
        registry.registerBuilding(newRecord(buildingIdC, BuildingType.HOUSE, BuildingValidationState.VALID, 0L, now));

        BuildingInvalidationQueueData queue = BuildingInvalidationQueueData.get(level);
        queue.enqueue(buildingIdA, BuildingInvalidationReason.BLOCK_BROKEN, now);
        queue.enqueue(buildingIdB, BuildingInvalidationReason.BLOCK_BROKEN, now);
        queue.enqueue(buildingIdC, BuildingInvalidationReason.BLOCK_BROKEN, now);

        BuildingInvalidationRuntime.BatchResult firstBatch = BuildingInvalidationRuntime.tickBatch(level, 1);
        helper.assertTrue(firstBatch.processed() == 1,
                "Expected first revalidation batch to process exactly one building.");
        helper.assertTrue(firstBatch.backlogRemaining() == 2,
                "Expected first revalidation batch to leave two buildings in backlog.");

        BuildingInvalidationRuntime.BatchResult secondBatch = BuildingInvalidationRuntime.tickBatch(level, 2);
        helper.assertTrue(secondBatch.processed() == 2,
                "Expected second revalidation batch to process remaining two buildings.");
        helper.assertTrue(secondBatch.backlogRemaining() == 0,
                "Expected revalidation backlog to be fully drained after second batch.");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void repairedHouseRevalidationReturnsToValid(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        WorkersServerConfig.SettlementHouseGraceTicks.set(24_000L);

        BlockPos origin = new BlockPos(1, 2, 1);
        buildValidHouse(level, origin);

        UUID buildingId = UUID.randomUUID();
        long now = level.getGameTime();
        ValidatedBuildingRegistryData registry = ValidatedBuildingRegistryData.get(level);
        registry.registerBuilding(staleHouseRecord(buildingId, now, origin));

        BuildingInvalidationQueueData queue = BuildingInvalidationQueueData.get(level);
        queue.enqueue(buildingId, BuildingInvalidationReason.MANUAL_RECHECK, now);
        BuildingInvalidationRuntime.tickBatch(level, 8);

        ValidatedBuildingRecord updated = registry.getById(buildingId);
        helper.assertTrue(updated != null && updated.state() == BuildingValidationState.VALID,
                "Expected repaired/valid house to transition back to VALID on revalidation.");
        helper.assertTrue(updated != null && updated.capacity() > 0,
                "Expected successful house revalidation to refresh capacity above zero.");
        helper.assertTrue(updated != null && updated.anchorPos().equals(origin.offset(2, 1, 2)),
                "Expected successful house revalidation to refresh anchor from validation snapshot.");
        helper.assertTrue(updated != null && updated.bounds().minX <= origin.getX() + 1 && updated.bounds().maxX >= origin.getX() + 4,
                "Expected successful house revalidation to refresh building bounds from validation snapshot.");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void overlappingDifferentBuildingTypeIsRejected(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = new BlockPos(1, 2, 1);
        UUID settlementId = UUID.randomUUID();

        ValidatedBuildingRegistryData registry = ValidatedBuildingRegistryData.get(level);
        registry.registerBuilding(new ValidatedBuildingRecord(
                UUID.randomUUID(),
                settlementId,
                BuildingType.SMITHY,
                Level.OVERWORLD,
                origin.offset(2, 1, 2),
                List.of(new ZoneSelection(ZoneRole.WORK_ZONE, origin.offset(1, 1, 1), origin.offset(4, 2, 4), null)),
                new AABB(origin.offset(1, 1, 1), origin.offset(4, 2, 4)),
                BuildingValidationState.VALID,
                1,
                50,
                List.of(),
                level.getGameTime(),
                level.getGameTime(),
                0L
        ));

        DefaultBuildingValidator validator = new DefaultBuildingValidator(new BuildingDefinitionRegistry());
        BuildingValidationRequest request = new BuildingValidationRequest(
                settlementId,
                BuildingType.HOUSE,
                origin.offset(2, 1, 2),
                List.of(
                        new ZoneSelection(ZoneRole.INTERIOR, origin.offset(1, 1, 1), origin.offset(4, 2, 4), null),
                        new ZoneSelection(ZoneRole.SLEEPING, origin.offset(2, 1, 2), origin.offset(3, 1, 3), null)
                )
        );
        BuildingValidationResult result = validator.validate(level, null, request);
        helper.assertTrue(!result.valid(), "Expected overlap with existing different building type to be rejected.");
        helper.assertTrue(result.failures().stream().anyMatch(issue -> "overlap_conflict".equals(issue.code())),
                "Expected overlap rejection to report overlap_conflict failure code.");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void nonPrimaryOverlapDoesNotTriggerConflict(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = new BlockPos(1, 2, 1);
        UUID settlementId = UUID.randomUUID();

        level.setBlockAndUpdate(origin.offset(2, 1, 2), Blocks.CHEST.defaultBlockState());

        ValidatedBuildingRegistryData registry = ValidatedBuildingRegistryData.get(level);
        registry.registerBuilding(new ValidatedBuildingRecord(
                UUID.randomUUID(),
                settlementId,
                BuildingType.SMITHY,
                Level.OVERWORLD,
                origin.offset(2, 1, 2),
                List.of(new ZoneSelection(ZoneRole.WORK_ZONE, origin.offset(1, 1, 1), origin.offset(4, 2, 4), null)),
                new AABB(origin.offset(1, 1, 1), origin.offset(4, 2, 4)),
                BuildingValidationState.VALID,
                1,
                50,
                List.of(),
                level.getGameTime(),
                level.getGameTime(),
                0L
        ));

        DefaultBuildingValidator validator = new DefaultBuildingValidator(new BuildingDefinitionRegistry());
        BuildingValidationRequest request = new BuildingValidationRequest(
                settlementId,
                BuildingType.STORAGE,
                origin.offset(2, 1, 2),
                List.of(new ZoneSelection(ZoneRole.STORAGE, origin.offset(1, 1, 1), origin.offset(4, 2, 4), null))
        );
        BuildingValidationResult result = validator.validate(level, null, request);
        helper.assertTrue(result.valid(), "Expected STORAGE overlap without primary-zone conflict to remain valid.");
        helper.assertTrue(result.failures().stream().noneMatch(issue -> "overlap_conflict".equals(issue.code())),
                "Expected non-primary overlap to avoid overlap_conflict failure.");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void primaryZoneMatrixAllowsDisjointZonesWithinOverlappingBounds(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        BlockPos origin = new BlockPos(1, 2, 1);
        UUID settlementId = UUID.randomUUID();

        ValidatedBuildingRegistryData registry = ValidatedBuildingRegistryData.get(level);
        registry.registerBuilding(new ValidatedBuildingRecord(
                UUID.randomUUID(),
                settlementId,
                BuildingType.SMITHY,
                Level.OVERWORLD,
                origin.offset(1, 1, 1),
                List.of(new ZoneSelection(ZoneRole.WORK_ZONE, origin.offset(1, 1, 1), origin.offset(2, 2, 2), null)),
                new AABB(origin.offset(1, 1, 1), origin.offset(8, 3, 8)),
                BuildingValidationState.VALID,
                1,
                40,
                List.of(),
                level.getGameTime(),
                level.getGameTime(),
                0L
        ));

        buildValidHouse(level, origin);
        DefaultBuildingValidator validator = new DefaultBuildingValidator(new BuildingDefinitionRegistry());
        BuildingValidationRequest request = new BuildingValidationRequest(
                settlementId,
                BuildingType.HOUSE,
                origin.offset(4, 1, 4),
                List.of(
                        new ZoneSelection(ZoneRole.INTERIOR, origin.offset(3, 1, 1), origin.offset(4, 2, 4), null),
                        new ZoneSelection(ZoneRole.SLEEPING, origin.offset(4, 1, 4), origin.offset(4, 1, 4), null)
                )
        );
        BuildingValidationResult result = validator.validate(level, null, request);
        helper.assertTrue(result.valid(),
                "Expected disjoint primary zones to remain valid even when coarse building bounds intersect.");
        helper.assertTrue(result.failures().stream().noneMatch(issue -> "overlap_conflict".equals(issue.code())),
                "Expected no overlap_conflict when prohibited role pairs do not intersect spatially.");
        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void fortBootstrapCreatesSettlementRecord(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        Player player = helper.makeMockPlayer();
        BlockPos fortOrigin = new BlockPos(20, 2, 20);
        BlockPos anchor = fortOrigin.offset(5, 1, 5);

        RecruitsClaim claim = BannerModDedicatedServerGameTestSupport.seedClaim(
                level, anchor, "bootstrap_test_faction", player.getUUID(), player.getName().getString());
        helper.assertTrue(claim != null, "Expected claim seeding for fort bootstrap test.");

        buildValidFort(level, fortOrigin);
        DefaultBuildingValidator validator = new DefaultBuildingValidator(new BuildingDefinitionRegistry());
        BuildingValidationResult fortValidation = validator.validate(
                level,
                null,
                new BuildingValidationRequest(
                        new java.util.UUID(0L, 0L),
                        BuildingType.STARTER_FORT,
                        anchor,
                        List.of(
                                new ZoneSelection(ZoneRole.AUTHORITY_POINT, anchor, anchor, anchor),
                                new ZoneSelection(ZoneRole.INTERIOR, fortOrigin.offset(1, 1, 1), fortOrigin.offset(8, 1, 8), anchor)
                        )
                )
        );
        helper.assertTrue(fortValidation.valid(), "Expected prepared fort fixture to pass STARTER_FORT validation.");

        BootstrapResult result = SettlementBootstrapService.bootstrapSettlement(level, player, fortValidation);
        helper.assertTrue(result.success(), "Expected bootstrap to succeed for validated fort in claimed chunk.");
        helper.assertTrue(result.settlementOptional().isPresent(), "Expected bootstrap result to include created settlement record.");

        SettlementRegistryData settlementRegistry = SettlementRegistryData.get(level);
        helper.assertTrue(settlementRegistry.get(result.settlement().settlementId()) != null,
                "Expected created settlement to be persisted in SettlementRegistryData.");
        helper.succeed();
    }

    private static ValidatedBuildingRecord newRecord(UUID buildingId,
                                                     BuildingType type,
                                                     BuildingValidationState state,
                                                     long invalidSince,
                                                     long now) {
        BlockPos anchor = new BlockPos(1, 2, 1);
        return new ValidatedBuildingRecord(
                buildingId,
                UUID.randomUUID(),
                type,
                Level.OVERWORLD,
                anchor,
                List.of(),
                new AABB(anchor),
                state,
                0,
                0,
                List.of(),
                now,
                now,
                invalidSince
        );
    }

    private static ValidatedBuildingRecord validHouseRecord(UUID buildingId, long now, BlockPos origin) {
        BlockPos anchor = origin.offset(2, 1, 2);
        List<ZoneSelection> zones = List.of(
                new ZoneSelection(ZoneRole.INTERIOR, origin.offset(1, 1, 1), origin.offset(4, 2, 4), null),
                new ZoneSelection(ZoneRole.SLEEPING, origin.offset(2, 1, 2), origin.offset(3, 1, 3), null)
        );
        AABB bounds = new AABB(origin, origin.offset(5, 3, 5));
        return new ValidatedBuildingRecord(
                buildingId,
                UUID.randomUUID(),
                BuildingType.HOUSE,
                Level.OVERWORLD,
                anchor,
                zones,
                bounds,
                BuildingValidationState.DEGRADED,
                0,
                0,
                List.of(),
                now,
                now,
                now - 200L
        );
    }

    private static ValidatedBuildingRecord staleHouseRecord(UUID buildingId, long now, BlockPos origin) {
        BlockPos staleAnchor = origin.offset(-3, 0, -3);
        List<ZoneSelection> staleZones = List.of(
                new ZoneSelection(ZoneRole.INTERIOR, origin.offset(-3, 0, -3), origin.offset(-2, 1, -2), null),
                new ZoneSelection(ZoneRole.SLEEPING, origin.offset(-3, 0, -3), origin.offset(-3, 0, -3), null)
        );
        AABB staleBounds = new AABB(origin.offset(-3, 0, -3), origin.offset(-2, 1, -2));
        return new ValidatedBuildingRecord(
                buildingId,
                UUID.randomUUID(),
                BuildingType.HOUSE,
                Level.OVERWORLD,
                staleAnchor,
                staleZones,
                staleBounds,
                BuildingValidationState.DEGRADED,
                0,
                0,
                List.of(),
                now,
                now,
                now - 200L
        );
    }

    private static void buildValidHouse(ServerLevel level, BlockPos origin) {
        int minX = origin.getX();
        int minY = origin.getY();
        int minZ = origin.getZ();
        int maxX = minX + 5;
        int maxY = minY + 3;
        int maxZ = minZ + 5;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                level.setBlockAndUpdate(new BlockPos(x, minY, z), Blocks.STONE_BRICKS.defaultBlockState());
            }
        }

        for (int y = minY + 1; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    boolean wall = x == minX || x == maxX || z == minZ || z == maxZ;
                    if (y == maxY) {
                        level.setBlockAndUpdate(new BlockPos(x, y, z), Blocks.STONE_BRICKS.defaultBlockState());
                    } else if (wall) {
                        level.setBlockAndUpdate(new BlockPos(x, y, z), Blocks.COBBLESTONE.defaultBlockState());
                    } else {
                        level.setBlockAndUpdate(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }

        level.setBlockAndUpdate(origin.offset(2, 1, 2), Blocks.RED_BED.defaultBlockState());
    }

    private static void buildValidFort(ServerLevel level, BlockPos origin) {
        int minX = origin.getX();
        int minY = origin.getY();
        int minZ = origin.getZ();
        int maxX = minX + 9;
        int maxY = minY + 4;
        int maxZ = minZ + 9;

        for (int x = minX; x <= maxX; x++) {
            for (int z = minZ; z <= maxZ; z++) {
                level.setBlockAndUpdate(new BlockPos(x, minY, z), Blocks.STONE_BRICKS.defaultBlockState());
            }
        }

        for (int y = minY + 1; y <= maxY; y++) {
            for (int x = minX; x <= maxX; x++) {
                for (int z = minZ; z <= maxZ; z++) {
                    boolean wall = x == minX || x == maxX || z == minZ || z == maxZ;
                    if (y == maxY) {
                        level.setBlockAndUpdate(new BlockPos(x, y, z), Blocks.STONE_BRICKS.defaultBlockState());
                    } else if (wall) {
                        level.setBlockAndUpdate(new BlockPos(x, y, z), Blocks.COBBLESTONE.defaultBlockState());
                    } else {
                        level.setBlockAndUpdate(new BlockPos(x, y, z), Blocks.AIR.defaultBlockState());
                    }
                }
            }
        }

        // Entrance at wall edge
        level.setBlockAndUpdate(origin.offset(0, 1, 5), Blocks.AIR.defaultBlockState());
        level.setBlockAndUpdate(origin.offset(0, 2, 5), Blocks.AIR.defaultBlockState());

        // Banner near anchor
        level.setBlockAndUpdate(origin.offset(6, 1, 5), Blocks.WHITE_BANNER.defaultBlockState());
    }
}
