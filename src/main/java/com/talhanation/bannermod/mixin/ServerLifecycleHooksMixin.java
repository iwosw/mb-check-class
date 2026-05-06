package com.talhanation.bannermod.mixin;

import com.talhanation.bannermod.config.BannerModConfigMigration;
import net.minecraft.server.MinecraftServer;
import net.neoforged.fml.loading.FMLPaths;
import net.neoforged.neoforge.server.ServerLifecycleHooks;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.nio.file.Path;

/**
 * Mixin: migrate legacy split BannerMod server configs into the unified file BEFORE
 * NeoForge calls {@code ConfigTracker.loadConfigs(SERVER, ...)} for the world.
 *
 * <p>Without this, NeoForge would load {@code bannermod-server.toml} with stock defaults and
 * the operator's tuned values from the legacy {@code bannermod-recruits-server.toml} /
 * {@code bannermod-workers-server.toml} would be silently ignored on the very first boot
 * after the upgrade. The migration helper itself is idempotent: subsequent boots see no
 * legacy files and become no-ops.
 *
 * <p>Inject site is the {@code HEAD} of {@link ServerLifecycleHooks#handleServerAboutToStart},
 * which fires for both dedicated servers and integrated singleplayer worlds, after the per-world
 * {@code serverconfig/} directory has been resolved by NeoForge but before SERVER configs
 * are read off disk.
 */
@Mixin(value = ServerLifecycleHooks.class, remap = false)
public class ServerLifecycleHooksMixin {

    @Inject(method = "handleServerAboutToStart", at = @At("HEAD"))
    private static void bannermod$migrateLegacyServerConfigs(MinecraftServer server, CallbackInfo ci) {
        try {
            Path serverConfigDir = server.getWorldPath(net.minecraft.world.level.storage.LevelResource.ROOT)
                    .resolve("serverconfig");
            BannerModConfigMigration.migrate(serverConfigDir);
            // Also migrate the global default-config dir so that even fresh worlds inherit the
            // correct shape; harmless if no legacy file lives there.
            BannerModConfigMigration.migrate(FMLPaths.CONFIGDIR.get());
        } catch (RuntimeException ignored) {
            // Migration failure must never abort server startup; a server admin can still
            // hand-merge the legacy values from the renamed *.legacy file.
        }
    }
}
