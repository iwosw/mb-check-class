package com.talhanation.bannermod.settlement.prefab.validation;

import com.talhanation.bannermod.settlement.prefab.BuildingPrefab;
import com.talhanation.bannermod.settlement.prefab.BuildingPrefabRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.Optional;

/**
 * Server-side entry point for a player's "validate this player-built structure" request.
 *
 * <p>Steps:</p>
 * <ol>
 *   <li>Resolve prefab from id.</li>
 *   <li>Build bounding box from the player's two corner taps.</li>
 *   <li>Sanity-check the center tap lies inside the bounds.</li>
 *   <li>Run the per-prefab validator.</li>
 *   <li>Report every issue to the player with colour coding.</li>
 *   <li>If passed, grant a {@link ValidationRewardService} payout scaled by architecture score.</li>
 * </ol>
 */
public final class BuildingValidationService {
    public static final int MAX_BOX_DIMENSION = 40;

    private BuildingValidationService() {
    }

    public enum Outcome {
        OK,
        UNKNOWN_PREFAB,
        INVALID_BOUNDS,
        CENTER_OUTSIDE,
        NO_PLAYER
    }

    public record ValidationOutcome(Outcome outcome, @Nullable ValidationResult result, int rewardEmeralds) {
    }

    public static ValidationOutcome validate(@Nullable ServerPlayer player,
                                             ResourceLocation prefabId,
                                             BlockPos cornerA,
                                             BlockPos cornerB,
                                             BlockPos center) {
        if (player == null) {
            return new ValidationOutcome(Outcome.NO_PLAYER, null, 0);
        }
        if (prefabId == null || cornerA == null || cornerB == null || center == null) {
            player.sendSystemMessage(Component.translatable("bannermod.prefab.validate.incomplete").withStyle(ChatFormatting.RED));
            return new ValidationOutcome(Outcome.INVALID_BOUNDS, null, 0);
        }
        if (!(player.level() instanceof ServerLevel serverLevel)) {
            return new ValidationOutcome(Outcome.NO_PLAYER, null, 0);
        }

        BuildingPrefabRegistry.instance().ensureDefaultsLoaded();
        Optional<BuildingPrefab> maybePrefab = BuildingPrefabRegistry.instance().lookup(prefabId);
        if (maybePrefab.isEmpty()) {
            player.sendSystemMessage(Component.translatable(
                    "bannermod.prefab.place.unknown", prefabId.toString()).withStyle(ChatFormatting.RED));
            return new ValidationOutcome(Outcome.UNKNOWN_PREFAB, null, 0);
        }
        BuildingPrefab prefab = maybePrefab.get();

        AABB bounds = bounds(cornerA, cornerB);
        if (bounds.getXsize() > MAX_BOX_DIMENSION
                || bounds.getYsize() > MAX_BOX_DIMENSION
                || bounds.getZsize() > MAX_BOX_DIMENSION) {
            player.sendSystemMessage(Component.translatable(
                    "bannermod.prefab.validate.too_big", MAX_BOX_DIMENSION).withStyle(ChatFormatting.RED));
            return new ValidationOutcome(Outcome.INVALID_BOUNDS, null, 0);
        }
        if (!bounds.inflate(0.5).contains(center.getX() + 0.5, center.getY() + 0.5, center.getZ() + 0.5)) {
            player.sendSystemMessage(Component.translatable(
                    "bannermod.prefab.validate.center_outside").withStyle(ChatFormatting.RED));
            return new ValidationOutcome(Outcome.CENTER_OUTSIDE, null, 0);
        }

        BuildingValidatorRegistry.instance().ensureDefaultsLoaded();
        ValidationResult result = BuildingValidatorRegistry.instance().run(prefab, serverLevel, bounds);

        broadcastResult(player, prefab, result);
        int reward = ValidationRewardService.grantFor(player, result);
        if (reward > 0) {
            player.sendSystemMessage(Component.translatable(
                    "bannermod.prefab.validate.reward", reward).withStyle(ChatFormatting.GOLD));
        }
        return new ValidationOutcome(Outcome.OK, result, reward);
    }

    private static AABB bounds(BlockPos a, BlockPos b) {
        int minX = Math.min(a.getX(), b.getX());
        int minY = Math.min(a.getY(), b.getY());
        int minZ = Math.min(a.getZ(), b.getZ());
        int maxX = Math.max(a.getX(), b.getX()) + 1;
        int maxY = Math.max(a.getY(), b.getY()) + 1;
        int maxZ = Math.max(a.getZ(), b.getZ()) + 1;
        return new AABB(minX, minY, minZ, maxX, maxY, maxZ);
    }

    private static void broadcastResult(ServerPlayer player, BuildingPrefab prefab, ValidationResult result) {
        ChatFormatting headerColor = result.passed() ? ChatFormatting.GREEN : ChatFormatting.RED;
        MutableComponent header = Component.translatable(result.passed()
                        ? "bannermod.prefab.validate.passed"
                        : "bannermod.prefab.validate.failed",
                Component.translatable(prefab.descriptor().displayKey())).withStyle(headerColor);
        player.sendSystemMessage(header);

        ArchitectureTier tier = result.architectureTier();
        player.sendSystemMessage(Component.translatable(
                        "bannermod.prefab.validate.score",
                        result.architectureScore(),
                        Component.translatable(tier.translationKey()))
                .withStyle(tierColor(tier)));

        for (ValidationIssue issue : result.issues()) {
            ChatFormatting color = switch (issue.severity()) {
                case BLOCKER -> ChatFormatting.RED;
                case MAJOR -> ChatFormatting.YELLOW;
                case MINOR -> ChatFormatting.GRAY;
                case INFO -> ChatFormatting.AQUA;
            };
            player.sendSystemMessage(Component.translatable(issue.translationKey(), issue.args()).withStyle(color));
        }
    }

    private static ChatFormatting tierColor(ArchitectureTier tier) {
        return switch (tier) {
            case HOVEL -> ChatFormatting.DARK_RED;
            case ACCEPTABLE -> ChatFormatting.YELLOW;
            case GOOD -> ChatFormatting.GREEN;
            case GREAT -> ChatFormatting.AQUA;
            case MAJESTIC -> ChatFormatting.LIGHT_PURPLE;
        };
    }
}
