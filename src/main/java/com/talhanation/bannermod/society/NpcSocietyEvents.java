package com.talhanation.bannermod.society;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.citizen.AbstractCitizenEntity;
import com.talhanation.bannermod.entity.citizen.CitizenEntity;
import com.talhanation.bannermod.entity.civilian.AbstractWorkerEntity;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.player.Player;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;

import javax.annotation.Nullable;
import java.util.UUID;

@EventBusSubscriber(modid = BannerModMain.MOD_ID, bus = EventBusSubscriber.Bus.GAME)
public final class NpcSocietyEvents {
    private NpcSocietyEvents() {
    }

    @SubscribeEvent
    public static void onEntityJoin(EntityJoinLevelEvent event) {
        if (!(event.getLevel() instanceof ServerLevel serverLevel)) {
            return;
        }
        if (!(event.getEntity() instanceof CitizenEntity)
                && !(event.getEntity() instanceof AbstractCitizenEntity)
                && !(event.getEntity() instanceof AbstractWorkerEntity)
                && !(event.getEntity() instanceof AbstractRecruitEntity)) {
            return;
        }
        NpcSocietyAccess.ensureResidentForEntity(serverLevel, event.getEntity());
    }

    @SubscribeEvent
    public static void onResidentDamaged(LivingIncomingDamageEvent event) {
        if (event.isCanceled()) {
            return;
        }
        LivingEntity target = event.getEntity();
        if (!(target.level() instanceof ServerLevel serverLevel) || !NpcMemoryAccess.isSocietyResident(target)) {
            return;
        }
        UUID actorUuid = resolveControllingPlayer(event.getSource());
        if (actorUuid == null) {
            return;
        }
        int intensity = Math.max(20, Math.min(100, Math.round(event.getAmount() * 12.0F)));
        NpcMemoryAccess.rememberAssaultByPlayer(serverLevel, target.getUUID(), actorUuid, intensity, serverLevel.getGameTime());
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        LivingEntity victim = event.getEntity();
        if (!(victim.level() instanceof ServerLevel serverLevel)) {
            return;
        }
        UUID actorUuid = resolveControllingPlayer(event.getSource());
        if (actorUuid != null && NpcMemoryAccess.isSocietyResident(victim)) {
            NpcMemoryAccess.rememberAssaultByPlayer(serverLevel, victim.getUUID(), actorUuid, 95, serverLevel.getGameTime());
            return;
        }
        if (actorUuid != null && victim instanceof Mob mob) {
            NpcMemoryAccess.onProtectedByPlayer(serverLevel, mob, actorUuid, serverLevel.getGameTime());
        }
    }

    private static @Nullable UUID resolveControllingPlayer(@Nullable DamageSource source) {
        if (source == null) {
            return null;
        }
        Entity actor = source.getEntity();
        if (actor instanceof Player player) {
            return player.getUUID();
        }
        if (actor instanceof AbstractRecruitEntity recruit) {
            return recruit.getOwnerUUID();
        }
        return null;
    }
}
