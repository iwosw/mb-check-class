package com.talhanation.bannermod.events;
import com.talhanation.bannermod.bootstrap.BannerModMain;

import com.talhanation.bannermod.governance.BannerModGovernorPolicy;
import com.talhanation.bannermod.compat.IWeapon;
import com.talhanation.bannermod.config.RecruitsServerConfig;
import com.talhanation.bannermod.util.DelayedExecutor;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.entity.military.ICompanion;
import com.talhanation.bannermod.entity.military.MessengerEntity;
import com.talhanation.bannermod.registry.military.ModEntityTypes;
import com.talhanation.bannermod.inventory.military.PromoteContainer;
import com.talhanation.bannermod.network.messages.military.MessageOpenPromoteScreen;
import com.talhanation.bannermod.persistence.military.*;
import com.talhanation.bannermod.events.RecruitEvent;
import com.talhanation.bannermod.events.runtime.RecruitCombatRuntime;
import com.talhanation.bannermod.events.runtime.RecruitWorldLifecycleService;
import com.talhanation.bannermod.ai.pathfinding.async.TrueAsyncPathfindingRuntime;
import net.neoforged.neoforge.common.NeoForge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.monster.AbstractIllager;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Projectile;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.EntityHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.scores.Team;
import net.neoforged.neoforge.event.entity.EntityJoinLevelEvent;
import net.neoforged.neoforge.event.entity.EntityLeaveLevelEvent;
import net.neoforged.neoforge.event.entity.EntityTeleportEvent;
import net.neoforged.neoforge.event.entity.ProjectileImpactEvent;
import net.neoforged.neoforge.event.entity.living.LivingDeathEvent;
import net.neoforged.neoforge.event.entity.living.LivingIncomingDamageEvent;
import net.neoforged.neoforge.event.entity.player.PlayerInteractEvent;
import net.neoforged.neoforge.event.level.LevelEvent;
import net.neoforged.neoforge.event.server.ServerStartedEvent;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.neoforge.event.tick.LevelTickEvent;
import com.talhanation.bannermod.network.compat.BannerModNetworkHooks;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class RecruitEvents {
    private static final Map<ServerLevel, RecruitsPatrolSpawn> RECRUIT_PATROL = new HashMap<>();
    private static final Map<ServerLevel, PillagerPatrolSpawn> PILLAGER_PATROL = new HashMap<>();
    public static RecruitsPlayerUnitManager recruitsPlayerUnitManager;
    public static RecruitsGroupsManager recruitsGroupsManager;

    public static MinecraftServer server;
    public static HashMap<Integer, EntityType<? extends AbstractRecruitEntity>> entitiesByProfession = new HashMap<>() {
        {
            put(0, ModEntityTypes.MESSENGER.get());
            put(1, ModEntityTypes.SCOUT.get());
            put(2, ModEntityTypes.PATROL_LEADER.get());
            put(3, ModEntityTypes.CAPTAIN.get());
        }
    };

    public static void promoteRecruit(AbstractRecruitEntity recruit, int profession, String name, ServerPlayer player) {
        if (!(recruit.getCommandSenderWorld() instanceof ServerLevel serverLevel)) {
            return;
        }

        // RecruitEvent.Promoted feuern – cancelable
        RecruitEvent.Promoted promoteEvent = new RecruitEvent.Promoted(recruit, profession, name, player);
        NeoForge.EVENT_BUS.post(promoteEvent);
        if (promoteEvent.isCanceled()) return;

        if (profession == 6) {
            RecruitGovernorWorkflow.tryPromoteRecruit(recruit, name, player);
            return;
        }

        if (!entitiesByProfession.containsKey(profession)) {
            player.sendSystemMessage(Component.translatable("chat.bannermod.promote.unsupported_profession"));
            return;
        }

        EntityType<? extends AbstractRecruitEntity> companionType = entitiesByProfession.get(profession);
        if (companionType == null) {
            player.sendSystemMessage(Component.translatable("chat.bannermod.promote.unsupported_profession"));
            return;
        }
        AbstractRecruitEntity abstractRecruit = companionType.create(recruit.getCommandSenderWorld());
        if (abstractRecruit instanceof ICompanion companion) {
            abstractRecruit.setCustomName(Component.literal(name));
            abstractRecruit.copyPosition(recruit);
            companion.applyRecruitValues(recruit);
            companion.setOwnerName(player.getName().getString());

            recruit.discard();
            abstractRecruit.getCommandSenderWorld().addFreshEntity(abstractRecruit);
        }
    }

    public static void openPromoteScreen(Player player, AbstractRecruitEntity recruit) {
        if (player instanceof ServerPlayer) {
            BannerModNetworkHooks.openScreen((ServerPlayer) player, new MenuProvider() {
                @Override
                public @NotNull Component getDisplayName() {
                    return recruit.getName();
                }

                @Override
                public AbstractContainerMenu createMenu(int i, @NotNull Inventory playerInventory, @NotNull Player playerEntity) {
                    return new PromoteContainer(i, playerEntity, recruit);
                }
            }, packetBuffer -> {
                packetBuffer.writeUUID(recruit.getUUID());
            });
        } else {
            BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageOpenPromoteScreen(player, recruit.getUUID()));
        }
    }

    public static void openGovernorScreen(Player player, AbstractRecruitEntity recruit) {
        RecruitGovernorWorkflow.openGovernorScreen(player, recruit);
    }

    public static void syncGovernorScreen(ServerPlayer player, AbstractRecruitEntity recruit) {
        RecruitGovernorWorkflow.syncGovernorScreen(player, recruit);
    }

    public static void updateGovernorPolicy(ServerPlayer player, AbstractRecruitEntity recruit, BannerModGovernorPolicy policy, int value) {
        RecruitGovernorWorkflow.updateGovernorPolicy(player, recruit, policy, value);
    }

    public static void handleGroupBackwardCompatibility(AbstractRecruitEntity recruit, int oldGroupNumber) {
        RecruitWorldLifecycleService.handleLegacyGroup(recruit, oldGroupNumber, server, recruitsGroupsManager);
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        server = event.getServer();
        RecruitWorldLifecycleService.RecruitManagers managers = RecruitWorldLifecycleService.initializeManagers(server);
        recruitsPlayerUnitManager = managers.playerUnitManager();
        recruitsGroupsManager = managers.groupsManager();
    }

    @SubscribeEvent
    public void onServerStarted(ServerStartedEvent event) {
        // start() hier und nicht in ServerStartingEvent:
        // ServerStartingEvent feuert bevor die Levels initialisiert sind — server.overworld()
        // kann dort eine NPE werfen und würde start() nie erreichen lassen.
        // ServerStartedEvent garantiert dass alle Levels geladen sind und der Executor
        // vor dem ersten Entity-Tick bereit ist.
        com.talhanation.bannermod.ai.pathfinding.AsyncPathProcessor.start();
    }


    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        RecruitWorldLifecycleService.saveManagers(server, recruitsPlayerUnitManager, recruitsGroupsManager);

        // Fix: Async-Executor sauber herunterfahren damit der Server nicht hängt
        com.talhanation.bannermod.ai.pathfinding.AsyncPathProcessor.shutdown();
        TrueAsyncPathfindingRuntime.instance().shutdown();
    }

    @SubscribeEvent
    public void onWorldSave(LevelEvent.Save event){
        RecruitWorldLifecycleService.saveManagers(server, recruitsPlayerUnitManager, recruitsGroupsManager);
    }

    @SubscribeEvent
    public void onPlayerJoin(EntityJoinLevelEvent event){
        if(event.getLevel().isClientSide()) return;

        if(event.getEntity() instanceof Player player){
            if (player instanceof ServerPlayer serverPlayer) {
                RecruitWorldLifecycleService.syncPlayerJoin(serverPlayer, recruitsPlayerUnitManager, recruitsGroupsManager);
            }
        }
    }

    @SubscribeEvent
    public void onTeleportEvent(EntityTeleportEvent event) {
        RecruitWorldLifecycleService.teleportFollowingRecruits(event);
    }

    @SubscribeEvent
    public void onServerTick(LevelTickEvent.Post event) {
        RecruitWorldLifecycleService.tickLevel(event, RECRUIT_PATROL, PILLAGER_PATROL);
    }

    public static void serverSideRecruitGroup(ServerLevel level){
        RecruitWorldLifecycleService.markRecruitsForGroupRefresh(level, recruitsGroupsManager);
    }

    private static final Set<Projectile> canceledProjectiles = new HashSet<>();

    @SubscribeEvent
    public void onProjectileImpact(ProjectileImpactEvent event) {
        RecruitCombatRuntime.onProjectileImpact(event);
    }

    @SubscribeEvent
    public void onEntityLeaveWorld(EntityLeaveLevelEvent event) {
        RecruitCombatRuntime.onEntityLeaveWorld(event);
    }

    @SubscribeEvent
    public void onPlayerInteractWithCaravan(PlayerInteractEvent.EntityInteract entityInteract) {
        RecruitCombatRuntime.onPlayerInteractWithCaravan(entityInteract);
    }

    @SubscribeEvent
    public void onLivingHurt(LivingIncomingDamageEvent event) {
        RecruitCombatRuntime.onLivingHurt(event);
    }

    @SubscribeEvent
    public void onLivingAttack(LivingIncomingDamageEvent event) {
        RecruitCombatRuntime.onLivingAttack(event);
    }

    @SubscribeEvent
    public void onHorseJoinWorld(EntityJoinLevelEvent event) {
        RecruitWorldLifecycleService.ensureHorseGoal(event.getEntity());
    }

    public static boolean canAttack(LivingEntity attacker, LivingEntity target) {
        return RecruitCombatRuntime.canAttack(attacker, target);
    }

    public static boolean canAttackAnimal(LivingEntity attacker, Animal animal) {
        return RecruitCombatRuntime.canAttackAnimal(attacker, animal);
    }

    public static boolean canAttackPlayer(LivingEntity attacker, Player player) {
        return RecruitCombatRuntime.canAttackPlayer(attacker, player);
    }

    public static boolean canAttackRecruit(LivingEntity attacker, AbstractRecruitEntity targetRecruit) {
        return RecruitCombatRuntime.canAttackRecruit(attacker, targetRecruit);
    }

    public static boolean isAlly(Team team1, Team team2) {
        return RecruitCombatRuntime.isAlly(team1, team2);
    }

    public static boolean isEnemy(Team team1, Team team2) {
        return RecruitCombatRuntime.isEnemy(team1, team2);
    }

    public static boolean isEnemy(LivingEntity attacker, LivingEntity target) {
        return RecruitCombatRuntime.isEnemy(attacker, target);
    }

    public static boolean isNeutral(Team team1, Team team2) {
        return RecruitCombatRuntime.isNeutral(team1, team2);
    }

    public static boolean canHarmTeam(LivingEntity attacker, LivingEntity target) {
        return RecruitCombatRuntime.canHarmTeam(attacker, target);
    }

    public static boolean canHarmTeamNoFriendlyFire(LivingEntity attacker, LivingEntity target) {
        return RecruitCombatRuntime.canHarmTeamNoFriendlyFire(attacker, target);
    }

    @SubscribeEvent
    public void onRecruitDeath(LivingDeathEvent event) {
        RecruitCombatRuntime.onRecruitDeath(event);
    }

    private final List<AbstractArrow> trackedArrows = new ArrayList<>();
    private int tickCounter = 0;

    @SubscribeEvent
    public void onWorldTickArrowCleaner(LevelTickEvent.Post event) {//for 1.18 and 1.19 use TickEvent.WorldTickEvent
        RecruitCombatRuntime.onWorldTickArrowCleaner(event);
    }
}
