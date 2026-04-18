package com.talhanation.bannermod.events;
import com.talhanation.bannermod.bootstrap.BannerModMain;

import com.talhanation.bannermod.client.military.ClientManager;
import com.talhanation.bannermod.config.RecruitsServerConfig;
import com.talhanation.bannermod.entity.military.*;
import com.talhanation.bannermod.inventory.military.CommandMenu;
import com.talhanation.bannermod.network.messages.military.*;
import com.talhanation.bannermod.persistence.military.RecruitsGroup;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.item.*;
import net.minecraft.world.phys.*;
import net.minecraftforge.event.TickEvent;
import net.minecraftforge.event.entity.player.PlayerEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.network.NetworkHooks;
import net.minecraftforge.registries.ForgeRegistries;
import org.jetbrains.annotations.NotNull;

import java.util.*;

public class CommandEvents {

    //0 = wander
    //1 = follow
    //2 = hold your position
    //3 = back to position
    //4 = hold my position
    //5 = Protect
    //6 = move
    //7 = forward
    //8 = backward
    public static void onMovementCommand(Player player, List<AbstractRecruitEntity> recruits, int movementState, int formation) {
        MovementFormationCommandService.onMovementCommand(player, recruits, movementState, formation);
    }

    public static void onMovementCommand(Player player, List<AbstractRecruitEntity> recruits, int movementState, int formation, boolean tight) {
        MovementFormationCommandService.onMovementCommand(player, recruits, movementState, formation, tight);
    }
    public static void applyFormation(int formation, List<AbstractRecruitEntity> recruits, Player player, Vec3 targetPos) {
        MovementFormationCommandService.applyFormation(formation, recruits, player, targetPos);
    }

    public static void applyFormation(int formation, List<AbstractRecruitEntity> recruits, Player player, Vec3 targetPos, boolean tight) {
        MovementFormationCommandService.applyFormation(formation, recruits, player, targetPos, tight);
    }

    public static void onFaceCommand(Player player, List<AbstractRecruitEntity> recruits, int formation, boolean tight) {
        MovementFormationCommandService.onFaceCommand(player, recruits, formation, tight);
    }

    public static void onMovementCommandGUI(AbstractRecruitEntity recruit, int movementState) {
        MovementFormationCommandService.onMovementCommandGUI(recruit, movementState);
    }

    public static void checkPatrolLeaderState(AbstractRecruitEntity recruit) {
        MovementFormationCommandService.checkPatrolLeaderState(recruit);
    }

    public static void onAggroCommand(UUID player_uuid, AbstractRecruitEntity recruit, int x_state, UUID group, boolean fromGui) {
        if (recruit.isEffectedByCommand(player_uuid, group)){
            int state = recruit.getState();
            switch (x_state) {

                case 0:
                    if (state != 0)
                        recruit.setAggroState(0);
                    break;

                case 1:
                    if (state != 1)
                        recruit.setAggroState(1);
                    break;

                case 2:
                    if (state != 2)
                        recruit.setAggroState(2);
                    break;

                case 3:
                    if (state != 3)
                        recruit.setAggroState(3);
                    break;
            }
        }
    }
    public static void onAttackCommand(Player player, UUID player_uuid, List<AbstractRecruitEntity> list, UUID group) {
        HitResult hitResult = player.pick(100, 1F, false);
        BlockPos blockpos = null;
        AABB aabb = null;
        List<LivingEntity> targets = new ArrayList<>();
        if (hitResult.getType() == HitResult.Type.ENTITY){
            EntityHitResult entityHitResult = (EntityHitResult) hitResult;

            blockpos = entityHitResult.getEntity().getOnPos();
            if(entityHitResult.getEntity() instanceof LivingEntity living) targets.add(living);
        }
        else if (hitResult.getType() == HitResult.Type.BLOCK){
            BlockHitResult blockHitResult = (BlockHitResult) hitResult;
            blockpos = blockHitResult.getBlockPos();
        }
        else return;

        aabb = new AABB(blockpos).inflate(10);

        list.removeIf(recruit -> !recruit.isEffectedByCommand(player_uuid, group));

        List<LivingEntity> validTargets = player.getCommandSenderWorld()
                .getEntitiesOfClass(LivingEntity.class, aabb);

        if (list.isEmpty() || validTargets.isEmpty()) return;

        validTargets.removeIf(target -> list.stream().noneMatch(recruit -> recruit.canAssignCombatTarget(target)));

        if (validTargets.isEmpty()) return;

        for (int i = 0; i < list.size(); i++) {
            AbstractRecruitEntity recruit = list.get(i);
            LivingEntity target = validTargets.get(i % validTargets.size());
            if (recruit.assignOrderedCombatTarget(target)) {
                recruit.setHoldPos(target.position());
                recruit.setFollowState(3);
            }
        }
    }

    public static void onStrategicFireCommand(Player player, UUID player_uuid, AbstractRecruitEntity recruit, UUID group, boolean should) {
        if (recruit.isEffectedByCommand(player_uuid, group)){

            if (recruit instanceof IStrategicFire bowman){
                HitResult hitResult = player.pick(100, 1F, false);
                bowman.setShouldStrategicFire(should);
                if (hitResult != null) {
                    if (hitResult.getType() == HitResult.Type.BLOCK) {
                        BlockHitResult blockHitResult = (BlockHitResult) hitResult;
                        BlockPos blockpos = blockHitResult.getBlockPos();
                        bowman.setStrategicFirePos(blockpos);
                    }
                }
            }
        }
    }

    public static void openCommandScreen(Player player) {
        if (player instanceof ServerPlayer) {
            NetworkHooks.openScreen((ServerPlayer) player, new MenuProvider() {

                @Override
                public @NotNull Component getDisplayName() {
                    return Component.literal("command_screen");
                }

                @Override
                public @NotNull AbstractContainerMenu createMenu(int i, @NotNull Inventory playerInventory, @NotNull Player playerEntity) {
                    return new CommandMenu(i, playerEntity);
                }
            }, packetBuffer -> {packetBuffer.writeUUID(player.getUUID());});
        } else {
            BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageCommandScreen(player));
        }
    }
    @SubscribeEvent
    public void onServerPlayerTick(TickEvent.PlayerTickEvent event){
        if(event.player instanceof ServerPlayer serverPlayer && serverPlayer.tickCount % 20 == 0){
            MovementFormationCommandService.onServerPlayerTick(serverPlayer);
        }
    }

    @SubscribeEvent
    public static void onPlayerLoggedIn(PlayerEvent.PlayerLoggedInEvent event) {
        MovementFormationCommandService.initializePlayerCommandState(event.getEntity());
    }

    public static int getSavedFormation(Player player) {
        return MovementFormationCommandService.getSavedFormation(player);
    }

    public static void saveFormation(Player player, int formation) {
        MovementFormationCommandService.saveFormation(player, formation);
    }


    public static void saveUUIDList(Player player, String key, Collection<UUID> uuids) {
        MovementFormationCommandService.saveUUIDList(player, key, uuids);
    }

    public static List<UUID> getSavedUUIDList(Player player, String key) {
        return MovementFormationCommandService.getSavedUUIDList(player, key);
    }

    public static int[] getSavedFormationPos(Player player) {
        return MovementFormationCommandService.getSavedFormationPos(player);
    }

    public static void saveFormationPos(Player player, int[] pos) {
        MovementFormationCommandService.saveFormationPos(player, pos);
    }

    public static void saveFormationCenter(Player player, Vec3 center) {
        MovementFormationCommandService.saveFormationCenter(player, center);
    }

    public static Vec3 getSavedFormationCenter(Player player) {
        return MovementFormationCommandService.getSavedFormationCenter(player);
    }

    public static boolean handleRecruiting(Player player, RecruitsGroup group, AbstractRecruitEntity recruit, boolean message){
        String name = recruit.getName().getString() + ": ";
        int sollPrice = recruit.getCost();
        Inventory playerInv = player.getInventory();
        int playerEmeralds = 0;

        String str = RecruitsServerConfig.RecruitCurrency.get();
        Optional<Holder<Item>> holder = ForgeRegistries.ITEMS.getHolder(ResourceLocation.tryParse(str));

        ItemStack currencyItemStack = holder.map(itemHolder -> itemHolder.value().getDefaultInstance()).orElseGet(Items.EMERALD::getDefaultInstance);

        Item currency = currencyItemStack.getItem();//

        //checkPlayerMoney
        for (int i = 0; i < playerInv.getContainerSize(); i++){
            ItemStack itemStackInSlot = playerInv.getItem(i);
            Item itemInSlot = itemStackInSlot.getItem();
            if (itemInSlot.equals(currency)){
                playerEmeralds = playerEmeralds + itemStackInSlot.getCount();
            }
        }

        boolean playerCanPay = playerEmeralds >= sollPrice;

        if (playerCanPay || player.isCreative()){
            if(recruit.hire(player, group, message)) {
                //give player tradeGood
                //remove playerEmeralds ->add left
                //
                playerEmeralds = playerEmeralds - sollPrice;

                //merchantEmeralds = merchantEmeralds + sollPrice;

                //remove playerEmeralds
                for (int i = 0; i < playerInv.getContainerSize(); i++) {
                    ItemStack itemStackInSlot = playerInv.getItem(i);
                    Item itemInSlot = itemStackInSlot.getItem();
                    if (itemInSlot.equals(currency)) {
                        playerInv.removeItemNoUpdate(i);
                    }
                }

                //add leftEmeralds to playerInventory
                ItemStack emeraldsLeft = currencyItemStack.copy();
                emeraldsLeft.setCount(playerEmeralds);
                playerInv.add(emeraldsLeft);


                if(player.getTeam() != null){
                    if(player.getCommandSenderWorld().isClientSide){
                        BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageAddRecruitToTeam(player.getTeam().getName(), 1));
                    }
                    else {
                        ServerPlayer serverPlayer = (ServerPlayer) player;
                        FactionEvents.addNPCToData(serverPlayer.serverLevel(), player.getTeam().getName(), 1);
                    }
                }

                return true;
            }
        }
        else
            player.sendSystemMessage(TEXT_HIRE_COSTS(name, sollPrice, currency));

        return false;
    }

    public static void onMountButton(UUID player_uuid, AbstractRecruitEntity recruit, UUID mount_uuid, UUID group) {
        if (recruit.isEffectedByCommand(player_uuid, group)){
            if(mount_uuid != null) recruit.shouldMount(true, mount_uuid);
            else if(recruit.getMountUUID() != null) recruit.shouldMount(true, recruit.getMountUUID());
            recruit.dismount = 0;
        }
    }

    public static void onDismountButton(UUID player_uuid, AbstractRecruitEntity recruit, UUID group) {
        if (recruit.isEffectedByCommand(player_uuid, group)){
            recruit.shouldMount(false, null);
            if(recruit.isPassenger()){
                recruit.stopRiding();
                recruit.dismount = 180;
            }
        }
    }

    public static void onProtectButton(UUID player_uuid, AbstractRecruitEntity recruit, UUID protect_uuid, UUID group) {
        if (recruit.isEffectedByCommand(player_uuid, group)){
            recruit.shouldProtect(true, protect_uuid);
        }
    }

    public static void onClearTargetButton(UUID player_uuid, AbstractRecruitEntity recruit, UUID group) {
        if (recruit.isEffectedByCommand(player_uuid, group)){
            //BannerModMain.LOGGER.debug("event: clear");
            recruit.setTarget(null);
            recruit.setLastHurtByPlayer(null);
            recruit.setLastHurtMob(null);
            recruit.setLastHurtByMob(null);
        }
    }

    public static void onClearUpkeepButton(UUID player_uuid, AbstractRecruitEntity recruit, UUID group) {
        if (recruit.isEffectedByCommand(player_uuid, group)){
            //BannerModMain.LOGGER.debug("event: clear");
            recruit.clearUpkeepEntity();
            recruit.clearUpkeepPos();
        }
    }
    public static void onUpkeepCommand(UUID player_uuid, AbstractRecruitEntity recruit, UUID group, boolean isEntity, UUID entity_uuid, BlockPos blockPos) {
        if (recruit.isEffectedByCommand(player_uuid, group)){
            if (isEntity) {
                //BannerModMain.LOGGER.debug("server: entity_uuid: " + entity_uuid);
                recruit.setUpkeepUUID(Optional.of(entity_uuid));
                recruit.clearUpkeepPos();
            }
            else {
                recruit.setUpkeepPos(blockPos);
                recruit.clearUpkeepEntity();
            }
            recruit.forcedUpkeep = true;
            recruit.setUpkeepTimer(0);
            onClearTargetButton(player_uuid, recruit, group);
        }
    }

    public static void onShieldsCommand(Player player, UUID player_uuid, AbstractRecruitEntity recruit, UUID group, boolean shields) {
        if (recruit.isEffectedByCommand(player_uuid, group)){
            recruit.setShouldBlock(shields);
        }
    }

    public static void onShieldsCommand(ServerPlayer serverPlayer, UUID player_uuid, AbstractRecruitEntity recruit, UUID group, boolean shields) {
        onShieldsCommand((Player) serverPlayer, player_uuid, recruit, group, shields);
    }

    public static void onRangedFireCommand(ServerPlayer serverPlayer, UUID player_uuid, AbstractRecruitEntity recruit, UUID group, boolean should) {
        if (recruit.isEffectedByCommand(player_uuid, group)){
            recruit.setShouldRanged(should);

            if(should){
                if(recruit instanceof CrossBowmanEntity) recruit.switchMainHandItem(itemStack -> itemStack.getItem() instanceof CrossbowItem);
                if(recruit instanceof BowmanEntity) recruit.switchMainHandItem(itemStack -> itemStack.getItem() instanceof BowItem);
            }
            else{
                recruit.switchMainHandItem(itemStack -> itemStack.getItem() instanceof SwordItem);
            }
        }
    }

    public static void onRestCommand(ServerPlayer serverPlayer, UUID player_uuid, AbstractRecruitEntity recruit, UUID group, boolean should) {
        if (recruit.isEffectedByCommand(player_uuid, group)){
            onClearTargetButton(player_uuid, recruit, group);
            recruit.setShouldRest(should);
        }
    }

    private static MutableComponent TEXT_HIRE_COSTS(String name, int sollPrice, Item item) {
        return Component.translatable("chat.recruits.text.hire_costs", name, String.valueOf(sollPrice), item.getDescription().getString());
    }
}
