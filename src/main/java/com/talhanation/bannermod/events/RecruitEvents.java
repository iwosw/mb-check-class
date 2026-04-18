package com.talhanation.bannermod.events;
import com.talhanation.bannermod.bootstrap.BannerModMain;

import com.talhanation.bannermod.governance.BannerModGovernorService;
import com.talhanation.bannermod.governance.BannerModGovernorManager;
import com.talhanation.bannermod.governance.BannerModGovernorAuthority;
import com.talhanation.bannermod.governance.BannerModGovernorPolicy;
import com.talhanation.bannermod.governance.BannerModGovernorRecommendation;
import com.talhanation.bannermod.governance.BannerModGovernorSnapshot;
import com.talhanation.bannermod.shared.settlement.BannerModSettlementBinding;
import com.talhanation.bannermod.compat.IWeapon;
import com.talhanation.bannermod.config.RecruitsServerConfig;
import com.talhanation.bannermod.util.DelayedExecutor;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.entity.military.ICompanion;
import com.talhanation.bannermod.entity.military.MessengerEntity;
import com.talhanation.bannermod.ai.military.horse.HorseRiddenByRecruitGoal;
import com.talhanation.bannermod.registry.military.ModEntityTypes;
import com.talhanation.bannermod.inventory.military.PromoteContainer;
import com.talhanation.bannermod.inventory.military.GovernorContainer;
import com.talhanation.bannermod.network.messages.military.MessageOpenGovernorScreen;
import com.talhanation.bannermod.network.messages.military.MessageOpenPromoteScreen;
import com.talhanation.bannermod.network.messages.military.MessageToClientUpdateGovernorScreen;
import com.talhanation.bannermod.persistence.military.*;
import com.talhanation.bannermod.events.RecruitEvent;
import com.talhanation.bannermod.events.runtime.RecruitCombatRuntime;
import net.minecraftforge.common.MinecraftForge;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.horse.AbstractChestedHorse;
import net.minecraft.world.entity.animal.horse.AbstractHorse;
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
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.EntityLeaveLevelEvent;
import net.minecraftforge.event.entity.EntityTeleportEvent;
import net.minecraftforge.event.entity.ProjectileImpactEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingHurtEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartedEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.PacketDistributor;
import net.minecraftforge.network.NetworkHooks;
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
        MinecraftForge.EVENT_BUS.post(promoteEvent);
        if (promoteEvent.isCanceled()) return;

        if (profession == 6) {
            if (recruit.getXpLevel() < 7 || recruit.getOwnerUUID() == null) {
                player.sendSystemMessage(Component.literal("Governor designation denied: recruit is not eligible"));
                return;
            }
            if (name != null && !name.isBlank()) {
                recruit.setCustomName(Component.literal(name));
            }

            RecruitsClaim claim = ClaimEvents.recruitsClaimManager == null
                    ? null
                    : ClaimEvents.recruitsClaimManager.getClaim(new ChunkPos(recruit.blockPosition()));
            BannerModGovernorService service = new BannerModGovernorService(
                    BannerModGovernorManager.get(serverLevel)
            );
            BannerModGovernorService.OperationResult result = service.assignGovernor(claim, player, recruit);
            if (result.allowed()) {
                player.sendSystemMessage(Component.literal(recruit.getName().getString() + " designated as governor"));
                openGovernorScreen(player, recruit);
            } else {
                player.sendSystemMessage(Component.literal("Governor designation denied: " + result.governorDecision().name().toLowerCase()));
            }
            return;
        }

        EntityType<? extends AbstractRecruitEntity> companionType = entitiesByProfession.get(profession);
        if (companionType == null) {
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
            NetworkHooks.openScreen((ServerPlayer) player, new MenuProvider() {
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
        if (player instanceof ServerPlayer serverPlayer) {
            NetworkHooks.openScreen(serverPlayer, new MenuProvider() {
                @Override
                public @NotNull Component getDisplayName() {
                    return Component.literal("Governor");
                }

                @Override
                public AbstractContainerMenu createMenu(int i, @NotNull Inventory playerInventory, @NotNull Player playerEntity) {
                    return new GovernorContainer(i, playerEntity, recruit);
                }
            }, packetBuffer -> packetBuffer.writeUUID(recruit.getUUID()));
            syncGovernorScreen(serverPlayer, recruit);
        } else {
            BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageOpenGovernorScreen(recruit.getUUID(), true));
        }
    }

    public static void syncGovernorScreen(ServerPlayer player, AbstractRecruitEntity recruit) {
        RecruitsClaim claim = ClaimEvents.recruitsClaimManager == null
                ? null
                : ClaimEvents.recruitsClaimManager.getClaim(new ChunkPos(recruit.blockPosition()));
        BannerModGovernorService service = new BannerModGovernorService(BannerModGovernorManager.get((ServerLevel) recruit.getCommandSenderWorld()));
        BannerModSettlementBinding.Binding binding = claim == null
                ? BannerModSettlementBinding.resolveSettlementStatus(ClaimEvents.recruitsClaimManager, recruit.blockPosition(), recruit.getTeam() == null ? null : recruit.getTeam().getName())
                : BannerModSettlementBinding.resolveSettlementStatus(claim, claim.getCenter() == null ? new ChunkPos(recruit.blockPosition()) : claim.getCenter(), claim.getOwnerFactionStringID());
        var snapshot = claim == null
                ? null
                : service.getOrCreateGovernorSnapshot(claim);

        BannerModMain.SIMPLE_CHANNEL.send(PacketDistributor.PLAYER.with(() -> player), new MessageToClientUpdateGovernorScreen(
                recruit.getUUID(),
                binding.status().name().toLowerCase(),
                snapshot == null ? 0 : snapshot.citizenCount(),
                snapshot == null ? 0 : snapshot.taxesDue(),
                snapshot == null ? 0 : snapshot.taxesCollected(),
                snapshot == null ? 0L : snapshot.lastHeartbeatTick(),
                recommendationLabel(snapshot, true),
                recommendationLabel(snapshot, false),
                snapshot == null ? BannerModGovernorPolicy.DEFAULT_VALUE : snapshot.garrisonPriority(),
                snapshot == null ? BannerModGovernorPolicy.DEFAULT_VALUE : snapshot.fortificationPriority(),
                snapshot == null ? BannerModGovernorPolicy.DEFAULT_VALUE : snapshot.taxPressure(),
                snapshot == null ? List.of() : snapshot.incidentTokens(),
                snapshot == null ? List.of() : snapshot.recommendationTokens()
        ));
    }

    public static void updateGovernorPolicy(ServerPlayer player, AbstractRecruitEntity recruit, BannerModGovernorPolicy policy, int value) {
        if (!(recruit.getCommandSenderWorld() instanceof ServerLevel serverLevel)) {
            return;
        }
        RecruitsClaim claim = ClaimEvents.recruitsClaimManager == null
                ? null
                : ClaimEvents.recruitsClaimManager.getClaim(new ChunkPos(recruit.blockPosition()));
        BannerModGovernorService service = new BannerModGovernorService(BannerModGovernorManager.get(serverLevel));
        BannerModGovernorService.OperationResult result = service.updatePolicy(claim, BannerModGovernorAuthority.actor(player), policy, value);
        if (!result.allowed()) {
            player.sendSystemMessage(Component.literal("Governor policy update denied: " + result.governorDecision().name().toLowerCase()));
            return;
        }
        syncGovernorScreen(player, recruit);
    }

    private static String recommendationLabel(BannerModGovernorSnapshot snapshot, boolean garrison) {
        if (snapshot == null) {
            return BannerModGovernorRecommendation.HOLD_COURSE.token();
        }
        for (String token : snapshot.recommendationTokens()) {
            if (garrison && BannerModGovernorRecommendation.INCREASE_GARRISON.token().equals(token)) {
                return token;
            }
            if (!garrison && BannerModGovernorRecommendation.STRENGTHEN_FORTIFICATIONS.token().equals(token)) {
                return token;
            }
        }
        return BannerModGovernorRecommendation.HOLD_COURSE.token();
    }

    public static void handleGroupBackwardCompatibility(AbstractRecruitEntity recruit, int oldGroupNumber) {
        if(recruit.getCommandSenderWorld().isClientSide()) return;
        if(recruit.getOwner() != null){
            ServerPlayer serverPlayer = (ServerPlayer) recruit.getOwner();
            String name = "Group " + oldGroupNumber;
            RecruitsGroup group = recruitsGroupsManager.getPlayersGroupByName(serverPlayer, name);
            if(group == null){
                group = new RecruitsGroup(name, serverPlayer, 0);
            }
            recruit.setGroupUUID(group.getUUID());
            group.addMember(recruit.getUUID());
            recruitsGroupsManager.addOrUpdateGroup(server.overworld(), serverPlayer, group);

            recruitsGroupsManager.broadCastGroupsToPlayer(serverPlayer);
        }
    }

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        server = event.getServer();

        recruitsPlayerUnitManager = new RecruitsPlayerUnitManager();
        recruitsPlayerUnitManager.load(server.overworld());

        recruitsGroupsManager = new RecruitsGroupsManager();
        recruitsGroupsManager.load(server.overworld());
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
        recruitsPlayerUnitManager.save(server.overworld());

        recruitsGroupsManager.save(server.overworld());

        // Fix: Async-Executor sauber herunterfahren damit der Server nicht hängt
        com.talhanation.bannermod.ai.pathfinding.AsyncPathProcessor.shutdown();
    }

    @SubscribeEvent
    public void onWorldSave(LevelEvent.Save event){
        recruitsPlayerUnitManager.save(server.overworld());
        recruitsGroupsManager.save(server.overworld());
    }

    @SubscribeEvent
    public void onPlayerJoin(EntityJoinLevelEvent event){
        if(event.getLevel().isClientSide()) return;

        if(event.getEntity() instanceof Player player){
            if (player instanceof ServerPlayer serverPlayer) {
                recruitsPlayerUnitManager.broadCastUnitInfoToPlayer(serverPlayer);
                recruitsGroupsManager.broadCastGroupsToPlayer(serverPlayer);
            }
        }
    }

    @SubscribeEvent
    public void onTeleportEvent(EntityTeleportEvent event) {
        if (event.getEntity() instanceof ServerPlayer player && !(event instanceof EntityTeleportEvent.EnderPearl) && !(event instanceof EntityTeleportEvent.ChorusFruit) && !(event instanceof EntityTeleportEvent.EnderEntity)) {
            double targetX = event.getTargetX();
            double targetY = event.getTargetY();
            double targetZ = event.getTargetZ();
            UUID player_uuid = player.getUUID();

            List<AbstractRecruitEntity> recruits = player.getCommandSenderWorld().getEntitiesOfClass(
                    AbstractRecruitEntity.class,
                    player.getBoundingBox()
                            .inflate(64, 32, 64),
                    recruit -> recruit.isAlive() && recruit.getFollowState() == 1 && recruit.getOwnerUUID().equals(player_uuid)
            );

            recruits.forEach(recruit -> recruit.teleportTo(targetX, targetY, targetZ));
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.LevelTickEvent event) {
        if (!event.level.isClientSide && event.level instanceof ServerLevel serverWorld) {
            if (RecruitsServerConfig.ShouldRecruitPatrolsSpawn.get()) {
                RECRUIT_PATROL.computeIfAbsent(serverWorld,
                        serverLevel -> new RecruitsPatrolSpawn(serverWorld));
                RecruitsPatrolSpawn spawner = RECRUIT_PATROL.get(serverWorld);
                spawner.tick();
            }

            if (RecruitsServerConfig.ShouldPillagerPatrolsSpawn.get()) {
                PILLAGER_PATROL.computeIfAbsent(serverWorld,
                        serverLevel -> new PillagerPatrolSpawn(serverWorld));
                PillagerPatrolSpawn pillagerSpawner = PILLAGER_PATROL.get(serverWorld);
                pillagerSpawner.tick();
            }

            // Treaty expiry check (every 20 ticks = 1 second)
            if (serverWorld.getGameTime() % 20 == 0 && FactionEvents.recruitsTreatyManager != null) {
                FactionEvents.recruitsTreatyManager.tick(serverWorld);
            }
        }
    }

    public static void serverSideRecruitGroup(ServerLevel level){
        List<AbstractRecruitEntity> recruitList = new ArrayList<>();
        for(Entity entity : level.getEntities().getAll()){
            if(entity instanceof AbstractRecruitEntity recruit)
                recruitList.add(recruit);
        }
        for(AbstractRecruitEntity recruit : recruitList){
            recruit.needsGroupUpdate = true;
        }

        recruitsGroupsManager.save(level);
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
    public void onLivingHurt(LivingHurtEvent event) {
        RecruitCombatRuntime.onLivingHurt(event);
    }

    private static final double DAMAGE_THRESHOLD_PERCENTAGE = 0.75;

    @SubscribeEvent
    public void onLivingAttack(LivingAttackEvent event) {
        RecruitCombatRuntime.onLivingAttack(event);
    }

    private void handleSignificantDamage(LivingEntity attacker, LivingEntity target, double damage, ServerLevel level) {
        Team attackerTeam = attacker.getTeam();
        Team targetTeam = target.getTeam();

        if (attackerTeam == null || targetTeam == null) return;


        double newHealth = target.getHealth() - damage;
        double damageThreshold = target.getMaxHealth() * DAMAGE_THRESHOLD_PERCENTAGE;


        if (newHealth < damageThreshold) {
            setTeamsAsEnemies(attackerTeam, targetTeam, level);
        }
    }

    private void setTeamsAsEnemies(Team attackerTeam, Team targetTeam, ServerLevel level) {
        String attackerTeamName = attackerTeam.getName();
        String targetTeamName = targetTeam.getName();

        if (FactionEvents.recruitsDiplomacyManager != null) {
            FactionEvents.recruitsDiplomacyManager.setRelation(attackerTeamName, targetTeamName,
                    RecruitsDiplomacyManager.DiplomacyStatus.ENEMY, level);
            FactionEvents.recruitsDiplomacyManager.setRelation(targetTeamName, attackerTeamName,
                    RecruitsDiplomacyManager.DiplomacyStatus.ENEMY, level);
        }
    }

    @SubscribeEvent
    public void onHorseJoinWorld(EntityJoinLevelEvent event) {
        Entity entity = event.getEntity();

        if (entity instanceof AbstractHorse horse) {
            horse.goalSelector.addGoal(0, new HorseRiddenByRecruitGoal(horse));
        }
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
    public void onWorldTickArrowCleaner(TickEvent.LevelTickEvent event) {//for 1.18 and 1.19 use TickEvent.WorldTickEvent
        RecruitCombatRuntime.onWorldTickArrowCleaner(event);
    }
}
