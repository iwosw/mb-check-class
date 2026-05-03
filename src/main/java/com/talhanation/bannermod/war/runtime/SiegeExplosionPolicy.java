package com.talhanation.bannermod.war.runtime;

import com.talhanation.bannermod.war.WarRuntimeContext;
import com.talhanation.bannermod.war.config.WarServerConfig;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

/**
 * Pure policy that answers "should an explosion at this point be allowed inside a
 * claimed/settlement chunk?" The default answer is no — settlements are protected.
 *
 * <p>The exception is an active siege: if the point is inside any
 * {@link SiegeStandardRuntime} zone whose owning war is in
 * {@code allowsBattleWindowActivation} state AND the configured
 * {@link BattleWindowSchedule} is currently open in server-local time, then the
 * explosion is permitted (e.g. a Medieval Siege Machines projectile or TNT charge
 * being used for an actual assault, not as a peacetime grief vector).
 */
public final class SiegeExplosionPolicy {
    private SiegeExplosionPolicy() {
    }

    /**
     * @return {@code true} when an explosion at {@code center} is permitted to damage
     *         the surrounding claim — i.e. an active siege is happening here right now.
     */
    public static boolean isExplosionAllowedDuringActiveSiege(ServerLevel level, BlockPos center) {
        if (level == null || center == null) return false;

        Collection<WarDeclarationRecord> wars = WarRuntimeContext.declarations(level).all();
        if (wars == null || wars.isEmpty()) return false;

        Set<UUID> activeWarIds = new HashSet<>();
        for (WarDeclarationRecord war : wars) {
            if (war.state().allowsBattleWindowActivation()) {
                activeWarIds.add(war.id());
            }
        }
        if (activeWarIds.isEmpty()) return false;

        SiegeStandardRuntime sieges = WarRuntimeContext.sieges(level);
        if (sieges == null || !sieges.isInsideAnyZone(center, activeWarIds)) return false;

        BattleWindowSchedule schedule = WarServerConfig.resolveSchedule();
        if (schedule == null) return false;
        return schedule.isOpen(ZonedDateTime.now(ZoneId.systemDefault()));
    }
}
