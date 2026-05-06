package com.talhanation.bannermod.war.events;

import com.talhanation.bannermod.war.WarRuntimeContext;
import com.talhanation.bannermod.war.audit.WarAuditLogSavedData;
import com.talhanation.bannermod.war.retention.WarRetentionPolicy;
import com.talhanation.bannermod.war.runtime.OccupationRuntime;
import com.talhanation.bannermod.war.runtime.WarDeclarationRecord;
import com.talhanation.bannermod.war.runtime.WarState;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.ServerTickEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Periodic sweeper for WARRETENTION-001. Every {@link WarRetentionPolicy#SWEEPER_INTERVAL_TICKS}
 * server ticks, scans the audit log + occupations and prunes records belonging to wars whose
 * state is {@link WarState#RESOLVED} or {@link WarState#CANCELLED} and whose entries are older
 * than {@link WarRetentionPolicy#RESOLVED_WAR_RETENTION_GAME_DAYS} game-days.
 *
 * <p>The hard caps in {@link com.talhanation.bannermod.war.audit.WarAuditLogSavedData},
 * {@link OccupationRuntime}, and {@link com.talhanation.bannermod.war.runtime.RevoltRuntime}
 * are the absolute upper bound; this sweeper just lets long-resolved wars age out earlier so
 * the live SavedData stays lean even before the cap is reached.</p>
 */
public class WarRetentionSweeper {
    private int counter = 0;

    @SubscribeEvent
    public void onServerTick(ServerTickEvent.Post event) {
        MinecraftServer server = event.getServer();
        if (server == null) return;
        if (++counter < WarRetentionPolicy.SWEEPER_INTERVAL_TICKS) return;
        counter = 0;

        ServerLevel level = server.overworld();
        if (level == null) return;

        sweep(level);
    }

    /** Public for unit + gametest access. Not part of the event handler contract. */
    public static int sweep(ServerLevel level) {
        Set<UUID> resolvedWarIds = collectResolvedWarIds(level);
        if (resolvedWarIds.isEmpty()) {
            return 0;
        }
        long now = level.getGameTime();
        long retention = WarRetentionPolicy.resolvedWarRetentionTicks();
        WarAuditLogSavedData audit = WarRuntimeContext.audit(level);
        OccupationRuntime occupations = WarRuntimeContext.occupations(level);
        int removed = 0;
        removed += audit.pruneResolved(resolvedWarIds, now, retention);
        removed += occupations.pruneResolved(resolvedWarIds, now, retention);
        return removed;
    }

    private static Set<UUID> collectResolvedWarIds(ServerLevel level) {
        Set<UUID> ids = new HashSet<>();
        for (WarDeclarationRecord record : WarRuntimeContext.declarations(level).all()) {
            if (record.state() == WarState.RESOLVED || record.state() == WarState.CANCELLED) {
                ids.add(record.id());
            }
        }
        return ids;
    }
}
