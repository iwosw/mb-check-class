package com.talhanation.bannermod.war.runtime;

import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.war.WarRuntimeContext;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ChunkPos;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

/**
 * Single source of truth for "is this claim currently under siege" in the warfare-RP runtime.
 *
 * <p>A claim counts as under siege when at least one chunk it owns intersects an active
 * {@link SiegeStandardRecord} whose war is in a state that allows battle-window activation
 * (i.e. {@link WarState#allowsBattleWindowActivation()}). All consumers — governor heartbeat,
 * settlement growth priority shifts, militia mobilization — read through this helper so that
 * the check stays consistent and the data record never has to carry a duplicated siege flag.
 */
public final class WarSiegeQueries {
    private WarSiegeQueries() {
    }

    public static boolean isClaimUnderSiege(ServerLevel level, RecruitsClaim claim) {
        if (level == null || claim == null) {
            return false;
        }
        Collection<UUID> activeWarIds = activeWarIds(level);
        if (activeWarIds.isEmpty()) {
            return false;
        }
        SiegeStandardRuntime sieges = WarRuntimeContext.sieges(level);
        for (SiegeStandardRecord record : sieges.all()) {
            if (!activeWarIds.contains(record.warId())) {
                continue;
            }
            if (claimIntersectsSiegeRadius(claim, record)) {
                return true;
            }
        }
        return false;
    }

    public static boolean claimIntersectsSiegeRadius(RecruitsClaim claim, SiegeStandardRecord record) {
        if (claim == null || record == null || record.pos() == null) {
            return false;
        }
        double radiusSqr = (double) record.radius() * (double) record.radius();
        double x = record.pos().getX() + 0.5D;
        double z = record.pos().getZ() + 0.5D;
        for (ChunkPos chunk : claim.getClaimedChunks()) {
            if (chunk == null) continue;
            double closestX = clamp(x, chunk.getMinBlockX(), chunk.getMaxBlockX() + 1.0D);
            double closestZ = clamp(z, chunk.getMinBlockZ(), chunk.getMaxBlockZ() + 1.0D);
            double dx = x - closestX;
            double dz = z - closestZ;
            if (dx * dx + dz * dz <= radiusSqr) {
                return true;
            }
        }
        return false;
    }

    private static double clamp(double value, double min, double max) {
        return Math.max(min, Math.min(max, value));
    }

    private static Collection<UUID> activeWarIds(ServerLevel level) {
        WarDeclarationRuntime declarations = WarRuntimeContext.declarations(level);
        List<UUID> ids = new ArrayList<>();
        for (WarDeclarationRecord war : declarations.all()) {
            if (war.state() != null && war.state().allowsBattleWindowActivation()) {
                ids.add(war.id());
            }
        }
        return ids;
    }
}
