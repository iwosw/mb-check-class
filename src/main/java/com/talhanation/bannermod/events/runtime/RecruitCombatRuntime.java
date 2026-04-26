package com.talhanation.bannermod.events.runtime;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.compat.IWeapon;
import com.talhanation.bannermod.config.RecruitsServerConfig;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.entity.military.MessengerEntity;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.scores.Team;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.UUID;

public final class RecruitCombatRuntime {
    private static final Set<Projectile> CANCELED_PROJECTILES = new HashSet<>();
    private static int tickCounter = 0;

    private RecruitCombatRuntime() {
    }

    public static void onProjectileImpact(ProjectileImpactEvent event) {
        Entity entity = event.getEntity();
        HitResult rayTrace = event.getRayTraceResult();

        if (CANCELED_PROJECTILES.contains(entity) || !(entity instanceof Projectile projectile)) {
            return;
        }
        Entity owner = projectile.getOwner();
        if (owner == null || rayTrace.getType() != HitResult.Type.ENTITY) {
            return;
        }

        Entity impactEntity = ((EntityHitResult) rayTrace).getEntity();
        String encode = impactEntity.getEncodeId();
        if (encode != null && encode.contains("corpse:corpse")) {
            event.setImpactResult(ProjectileImpactEvent.ImpactResult.SKIP_ENTITY);
            return;
        }
        if (!(impactEntity instanceof LivingEntity impactLiving)) {
            return;
        }

        if (projectile instanceof AbstractArrow arrow && arrow.getPierceLevel() > 0 && owner instanceof LivingEntity livingOwner && !canAttack(livingOwner, impactLiving)) {
            event.setImpactResult(ProjectileImpactEvent.ImpactResult.SKIP_ENTITY);
            CANCELED_PROJECTILES.add(projectile);
            return;
        }

        AbstractRecruitEntity recruit = RecruitEntityAccess.asRecruit(owner);
        if (recruit != null) {
            if (impactLiving instanceof Animal animal) {
                Entity passenger = animal.getFirstPassenger();
                AbstractRecruitEntity passengerRecruit = RecruitEntityAccess.asRecruit(passenger);
                if (passengerRecruit != null && !canAttack(recruit, passengerRecruit)) {
                    event.setImpactResult(ProjectileImpactEvent.ImpactResult.SKIP_ENTITY);
                    return;
                }
                if (passenger instanceof Player player && !canAttack(recruit, player)) {
                    event.setImpactResult(ProjectileImpactEvent.ImpactResult.SKIP_ENTITY);
                    return;
                }
            }

            if (!canAttack(recruit, impactLiving)) {
                event.setImpactResult(ProjectileImpactEvent.ImpactResult.SKIP_ENTITY);
                return;
            }
            recruit.addXp(2);
            recruit.checkLevel();
        }

        if (owner instanceof AbstractIllager illager && !RecruitsServerConfig.PillagerFriendlyFire.get() && illager.isAlliedTo(impactLiving)) {
            event.setImpactResult(ProjectileImpactEvent.ImpactResult.SKIP_ENTITY);
            CANCELED_PROJECTILES.add(projectile);
            return;
        }

        if (owner instanceof Player player && !canHarmTeam(player, impactLiving)) {
            event.setImpactResult(ProjectileImpactEvent.ImpactResult.SKIP_ENTITY);
        }
    }

    public static void onEntityLeaveWorld(EntityLeaveLevelEvent event) {
        if (!event.getLevel().isClientSide() && event.getEntity() instanceof Projectile projectile) {
            CANCELED_PROJECTILES.remove(projectile);
        }
    }

    public static void onPlayerInteractWithCaravan(PlayerInteractEvent.EntityInteract entityInteract) {
        if (entityInteract.getLevel().isClientSide()) {
            return;
        }

        Player player = entityInteract.getEntity();
        Entity interacting = entityInteract.getTarget();
        if (interacting instanceof AbstractChestedHorse chestedHorse) {
            CompoundTag nbt = chestedHorse.getPersistentData();
            if (!nbt.contains("Caravan") || !chestedHorse.hasChest()) {
                return;
            }
            player.getCommandSenderWorld().getEntitiesOfClass(
                    AbstractRecruitEntity.class,
                    player.getBoundingBox().inflate(64F),
                    recruit -> !recruit.isOwned() && (recruit.getName().getString().equals("Caravan Leader") || recruit.getName().getString().equals("Caravan Guard"))
            ).forEach(recruit -> recruit.assignReactiveCombatTarget(player));
        }
    }

    public static void onLivingHurt(LivingHurtEvent event) {
        if (event.getEntity().getCommandSenderWorld().isClientSide()) {
            return;
        }

        if (BannerModMain.isMusketModLoaded) {
            Entity sourceEntity = event.getSource().getEntity();
            AbstractRecruitEntity owner = RecruitEntityAccess.asRecruit(sourceEntity);
            if (owner != null && IWeapon.isMusketModWeapon(owner.getMainHandItem())) {
                LivingEntity impactEntity = event.getEntity();
                if (!canAttack(owner, impactEntity)) {
                    event.setCanceled(true);
                } else {
                    owner.addXp(2);
                    owner.checkLevel();
                }
            }
        }

        Entity target = event.getEntity();
        Entity source = event.getSource().getEntity();
        if (source instanceof LivingEntity sourceEntity) {
            if (target.getTeam() == null) {
                return;
            }
            target.getCommandSenderWorld().getEntitiesOfClass(
                    AbstractRecruitEntity.class,
                    target.getBoundingBox().inflate(32D),
                    recruit -> recruit.getTarget() == null && recruit.getTeam() != null && recruit.getTeam().equals(target.getTeam())
            ).forEach(recruit -> recruit.assignReactiveCombatTarget(sourceEntity));
        }
    }

    public static void onLivingAttack(LivingAttackEvent event) {
        if (event.getEntity().getCommandSenderWorld().isClientSide()) {
            return;
        }

        Entity target = event.getEntity();
        Entity source = event.getSource().getEntity();
        if (!target.getCommandSenderWorld().isClientSide() && target instanceof LivingEntity livingTarget && source instanceof LivingEntity livingSource) {
            if (!canAttack(livingSource, livingTarget)) {
                event.setCanceled(true);
            } else {
                handleSignificantDamage(livingSource, livingTarget, event.getAmount(), (ServerLevel) livingTarget.getCommandSenderWorld());
            }
        }
    }

    public static void onRecruitDeath(LivingDeathEvent event) {
        Entity target = event.getEntity();
        AbstractRecruitEntity recruit = RecruitEntityAccess.asRecruit(target);
        if (recruit != null) {
            if (!recruit.getIsOwned()) {
                return;
            }
            UUID owner = recruit.getOwnerUUID();
            recruit.getCommandSenderWorld().getEntitiesOfClass(
                    AbstractRecruitEntity.class,
                    recruit.getBoundingBox().inflate(64.0D),
                    entity -> entity.getOwnerUUID() != null && entity.getOwnerUUID().equals(owner)
            ).forEach(entity -> entity.setMoral(Math.max(entity.getMorale() - 0.1F, 0F)));
        }
    }

    public static void onWorldTickArrowCleaner(TickEvent.LevelTickEvent event) {
        if (event.level.isClientSide() || !RecruitsServerConfig.AllowArrowCleaning.get() || event.phase != TickEvent.Phase.END) {
            return;
        }
        if (++tickCounter < 100) {
            return;
        }
        tickCounter = 0;

        for (AbstractArrow arrow : event.level.getEntitiesOfClass(AbstractArrow.class, event.level.getWorldBorder().getCollisionShape().bounds())) {
            if (arrow.pickup == AbstractArrow.Pickup.DISALLOWED && arrow.inGroundTime > 300) {
                arrow.discard();
            }
        }
    }

    public static boolean canAttack(LivingEntity attacker, LivingEntity target) {
        return RecruitAttackPolicy.canAttack(attacker, target);
    }

    public static boolean canAttackAnimal(LivingEntity attacker, Animal animal) {
        return RecruitAttackPolicy.canAttackAnimal(attacker, animal);
    }

    public static boolean canAttackPlayer(LivingEntity attacker, Player player) {
        return RecruitAttackPolicy.canAttackPlayer(attacker, player);
    }

    public static boolean canAttackRecruit(LivingEntity attacker, AbstractRecruitEntity targetRecruit) {
        return RecruitAttackPolicy.canAttackRecruit(attacker, targetRecruit);
    }

    public static boolean isAlly(Team team1, Team team2) {
        return RecruitDiplomacyPolicy.isAlly(team1, team2);
    }

    public static boolean isEnemy(Team team1, Team team2) {
        return RecruitDiplomacyPolicy.isEnemy(team1, team2);
    }

    public static boolean isNeutral(Team team1, Team team2) {
        return RecruitDiplomacyPolicy.isNeutral(team1, team2);
    }

    public static boolean canHarmTeam(LivingEntity attacker, LivingEntity target) {
        return RecruitDiplomacyPolicy.canHarmTeam(attacker, target);
    }

    public static boolean canHarmTeamNoFriendlyFire(LivingEntity attacker, LivingEntity target) {
        return RecruitDiplomacyPolicy.canHarmTeamNoFriendlyFire(attacker, target);
    }

    private static void handleSignificantDamage(LivingEntity attacker, LivingEntity target, double damage, ServerLevel level) {
        RecruitDiplomacyPolicy.handleSignificantDamage(attacker, target, damage, level);
    }

    private static void setTeamsAsEnemies(Team attackerTeam, Team targetTeam, ServerLevel level) {
        RecruitDiplomacyPolicy.setTeamsAsEnemies(attackerTeam, targetTeam, level);
    }
}
