package com.talhanation.bannermod.events;

import com.talhanation.bannermod.governance.BannerModGovernorHeartbeat;
import com.talhanation.bannermod.governance.BannerModGovernorManager;
import com.talhanation.bannermod.config.RecruitsServerConfig;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.util.ClaimUtil;
import com.talhanation.bannermod.persistence.military.*;
import net.minecraft.core.BlockPos;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.player.AttackEntityEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.event.level.ExplosionEvent;
import net.minecraftforge.event.level.LevelEvent;
import net.minecraftforge.event.server.ServerStartingEvent;
import net.minecraftforge.event.server.ServerStoppingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import java.util.List;

public class ClaimEvents {

    public static MinecraftServer server;
    public static RecruitsClaimManager recruitsClaimManager;

    public static int siegeCounter;

    public static int detectionCounter;

    private static final int SIEGE_TICK_INTERVAL = 100;

    private static final int DETECTION_TICK_INTERVAL = 300;

    private static final int GOVERNOR_TICK_INTERVAL = 200;

    public static int governorCounter;

    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        server = event.getServer();
        ServerLevel level = server.overworld();

        recruitsClaimManager = new RecruitsClaimManager();
        recruitsClaimManager.load(level);
    }

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        recruitsClaimManager.save(server.overworld());
    }

    @SubscribeEvent
    public void onWorldSave(LevelEvent.Save event){
        recruitsClaimManager.save(server.overworld());
    }

    @SubscribeEvent
    public void onPlayerJoin(EntityJoinLevelEvent event){
        if(event.getLevel().isClientSide()) return;

        if(event.getEntity() instanceof Player){
            recruitsClaimManager.broadcastClaimsToAll(server.overworld());
        }
    }

    @SubscribeEvent
    public void onServerTick(TickEvent.ServerTickEvent event){
        if (event.phase != TickEvent.Phase.END) return;
        if (server == null || recruitsClaimManager == null) return;

        ServerLevel level = server.overworld();
        if(level == null || level.isClientSide()) return;

        siegeCounter++;
        detectionCounter++;
        governorCounter++;

        if(siegeCounter >= SIEGE_TICK_INTERVAL){
            siegeCounter = 0;
            siegeRuntime().tickActiveSieges(level);
        }

        if(detectionCounter >= DETECTION_TICK_INTERVAL){
            detectionCounter = 0;
            siegeRuntime().tickDetection(level);
        }

        if(governorCounter >= GOVERNOR_TICK_INTERVAL){
            governorCounter = 0;
            BannerModGovernorHeartbeat.runGovernedClaimHeartbeat(level, recruitsClaimManager, BannerModGovernorManager.get(level));
        }
    }


    public static float calculateSiegeSpeedPercent(int attackerCount, int defenderCount) {
        return ClaimSiegeRuntime.calculateSiegeSpeedPercent(attackerCount, defenderCount);
    }

    public static List<AbstractRecruitEntity> getRecruitsOfTeamInRange(Level level, Player attackingPlayer, double radius, String teamId) {

        return level.getEntitiesOfClass(AbstractRecruitEntity.class, attackingPlayer.getBoundingBox().inflate(radius)).stream()
                .filter(recruit -> recruit.isAlive() && recruit.getTeam() != null && teamId.equals(recruit.getTeam().getName()))
                .toList();
    }
    @SubscribeEvent
    public void onBlockBreakEvent(BlockEvent.BreakEvent event) {
        if(event.getLevel().isClientSide()) return;
        if(claimProtectionPolicy().shouldDenyBlockBreak(event.getLevel(), event.getPos(), event.getPlayer())) event.setCanceled(true);
    }

    @SubscribeEvent
    public void onBlockPlaceEvent(BlockEvent.EntityPlaceEvent event) {
        if(event.getLevel().isClientSide()) return;
        if(claimProtectionPolicy().shouldDenyBlockPlacement(event.getLevel(), event.getPos(), event.getEntity())) event.setCanceled(true);
    }

    @SubscribeEvent
    public void onFluidPlaceBlockEvent(BlockEvent.FluidPlaceBlockEvent event) {
        LevelAccessor level = event.getLevel();
        if(level.isClientSide()) return;
        if(claimProtectionPolicy().shouldDenyFluidPlacement(level, event.getPos(), event.getLiquidPos())) event.setCanceled(true);
    }

    @SubscribeEvent
    public void onExplosion(ExplosionEvent event) {
        if(event.getLevel().isClientSide()) return;
        Vec3 vec = event.getExplosion().getPosition();
        BlockPos pos = new BlockPos((int) vec.x, (int) vec.y, (int) vec.z);
        ChunkAccess access = server.overworld().getChunk(pos);
        RecruitsClaim claim = recruitsClaimManager.getClaim(access.getPos());

        Entity entity = event.getExplosion().getDirectSourceEntity();
        if(entity instanceof Player player && player.isCreative() && player.hasPermissions(2)){
            return;
        }

        if(claim != null && RecruitsServerConfig.ExplosionProtectionInClaims.get()){
            event.setCanceled(true);
        }
    }
    @SubscribeEvent
    public void onBlockInteract(PlayerInteractEvent.RightClickBlock event) {
        if(event.getLevel().isClientSide()) return;
        Player player = event.getEntity();
        if(claimProtectionPolicy().shouldDenyBlockInteraction(event.getLevel(), event.getPos(), player, event.getHand())){
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent
    public void onItemInteract(PlayerInteractEvent.RightClickItem event) {
        if(event.getLevel().isClientSide()) return;
        BlockPos targetPos = ClaimInteractionTargetResolver.resolveItemInteractionTarget(event.getEntity(), event.getHand());
        if(targetPos == null) return;
        if(claimProtectionPolicy().shouldDenyBlockInteraction(event.getLevel(), targetPos, event.getEntity(), event.getHand())){
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent
    public void onEntityInteract(PlayerInteractEvent.EntityInteract event) {
        if(event.getLevel().isClientSide()) return;
        if(claimProtectionPolicy().shouldDenyEntityInteraction(event.getEntity(), event.getTarget())){
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent
    public void onEntityInteractSpecific(PlayerInteractEvent.EntityInteractSpecific event) {
        if(event.getLevel().isClientSide()) return;
        if(claimProtectionPolicy().shouldDenyEntityInteraction(event.getEntity(), event.getTarget())){
            event.setCanceled(true);
            event.setCancellationResult(InteractionResult.FAIL);
        }
    }

    @SubscribeEvent
    public void onAttackEntity(AttackEntityEvent event) {
        Player player = event.getEntity();
        if(player.level().isClientSide()) return;
        if(claimProtectionPolicy().shouldDenyEntityAttack(player, event.getTarget())){
            event.setCanceled(true);
        }
    }

    private ClaimProtectionPolicy claimProtectionPolicy() {
        return new ClaimProtectionPolicy(recruitsClaimManager);
    }

    private ClaimSiegeRuntime siegeRuntime() {
        return new ClaimSiegeRuntime(server, recruitsClaimManager);
    }

    public static void sendVillagersHome(ServerLevel level, RecruitsClaim claim) {
        ClaimSiegeRuntime.sendVillagersHome(level, claim);
    }

    public static void siegeOverVillagers(ServerLevel level, RecruitsClaim claim) {
        ClaimSiegeRuntime.siegeOverVillagers(level, claim);
    }

}
