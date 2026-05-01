package com.talhanation.bannermod.client.civilian.render;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.settlement.building.ZoneRole;
import com.talhanation.bannermod.settlement.validation.SurveyorModeGuidance;
import com.talhanation.bannermod.settlement.validation.ValidationSession;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

import javax.annotation.Nullable;

@EventBusSubscriber(modid = BannerModMain.MOD_ID, value = Dist.CLIENT)
public final class SettlementSurveyorPinnedPreviewState {
    @Nullable
    private static PinnedSurveyPreview pinnedPreview;

    private SettlementSurveyorPinnedPreviewState() {
    }

    public static boolean hasPinnedPreview() {
        return pinnedPreview != null;
    }

    public static boolean canPin(@Nullable ValidationSession session) {
        return session != null && !session.anchorPos().equals(BlockPos.ZERO);
    }

    public static void pin(Level level, ValidationSession session, @Nullable ZoneRole selectedRole) {
        if (level == null || !canPin(session)) {
            return;
        }
        ZoneRole resolvedRole = selectedRole == null ? SurveyorModeGuidance.defaultRole(session.mode()) : selectedRole;
        pinnedPreview = new PinnedSurveyPreview(level.dimension(), session, resolvedRole);
    }

    public static void clear() {
        pinnedPreview = null;
    }

    @Nullable
    public static PinnedSurveyPreview previewFor(@Nullable Level level) {
        if (level == null || pinnedPreview == null) {
            return null;
        }
        return pinnedPreview.dimension().equals(level.dimension()) ? pinnedPreview : null;
    }

    @SubscribeEvent
    public static void onClientLogin(ClientPlayerNetworkEvent.LoggingIn event) {
        clear();
    }

    @SubscribeEvent
    public static void onClientLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        clear();
    }

    public record PinnedSurveyPreview(ResourceKey<Level> dimension, ValidationSession session, ZoneRole selectedRole) {
    }
}
