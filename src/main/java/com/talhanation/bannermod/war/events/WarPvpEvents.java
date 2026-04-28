package com.talhanation.bannermod.war.events;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.war.WarRuntimeContext;
import com.talhanation.bannermod.war.config.WarServerConfig;
import com.talhanation.bannermod.war.registry.PoliticalMembership;
import com.talhanation.bannermod.war.registry.PoliticalRegistryRuntime;
import com.talhanation.bannermod.war.runtime.SiegeStandardRuntime;
import com.talhanation.bannermod.war.runtime.WarDeclarationRecord;
import com.talhanation.bannermod.war.runtime.WarPvpGate;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.bus.api.SubscribeEvent;

import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public class WarPvpEvents {

    @SubscribeEvent
    public void onLivingHurt(LivingIncomingDamageEvent event) {
        if (event.isCanceled()) {
            return;
        }
        if (!WarServerConfig.RegulatedPvpEnabled.get()) {
            return;
        }
        LivingEntity target = event.getEntity();
        if (target.level().isClientSide()) {
            return;
        }
        if (!(target instanceof Player targetPlayer)) {
            return;
        }
        Entity attacker = event.getSource().getEntity();
        if (attacker == null || attacker == targetPlayer) {
            return;
        }
        UUID attackerPlayerId = resolveControllingPlayer(attacker);
        if (attackerPlayerId == null) {
            return;
        }
        if (attackerPlayerId.equals(targetPlayer.getUUID())) {
            return;
        }
        ServerLevel level = (ServerLevel) target.level();
        PoliticalRegistryRuntime registry = WarRuntimeContext.registry(level);
        UUID attackerEntity = PoliticalMembership.entityIdFor(registry, attackerPlayerId);
        UUID defenderEntity = PoliticalMembership.entityIdFor(registry, targetPlayer.getUUID());

        Collection<WarDeclarationRecord> wars = WarRuntimeContext.declarations(level).all();
        Set<UUID> activeWarIds = new HashSet<>();
        for (WarDeclarationRecord war : wars) {
            if (war.state().allowsBattleWindowActivation()) {
                activeWarIds.add(war.id());
            }
        }

        SiegeStandardRuntime sieges = WarRuntimeContext.sieges(level);
        boolean insideWarZone = sieges.isInsideAnyZone(targetPlayer.blockPosition(), activeWarIds);

        boolean allowed = WarPvpGate.allowsWarPvp(
                attackerEntity,
                defenderEntity,
                wars,
                WarServerConfig.resolveSchedule(),
                ZonedDateTime.now(ZoneId.systemDefault()),
                insideWarZone
        );

        if (!allowed) {
            event.setCanceled(true);
        }
    }

    private static UUID resolveControllingPlayer(Entity attacker) {
        if (attacker instanceof Player player) {
            return player.getUUID();
        }
        if (attacker instanceof AbstractRecruitEntity recruit) {
            return recruit.getOwnerUUID();
        }
        return null;
    }
}
