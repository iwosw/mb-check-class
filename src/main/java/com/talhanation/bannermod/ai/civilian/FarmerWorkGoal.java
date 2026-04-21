package com.talhanation.bannermod.ai.civilian;

import com.talhanation.bannermod.entity.civilian.AbstractWorkerEntity;
import com.talhanation.bannermod.entity.civilian.FarmerEntity;
import com.talhanation.bannermod.entity.civilian.WorkerBindingResume;
import com.talhanation.bannermod.entity.civilian.workarea.CropArea;
import com.talhanation.bannermod.persistence.civilian.NeededItem;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.*;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.IPlantable;
import net.minecraftforge.common.PlantType;

import javax.annotation.Nullable;
import java.util.*;

public class FarmerWorkGoal extends Goal {

    public FarmerEntity farmer;
    public State state;
    public String errorMessage;
    public boolean errorMessageDone;
    public BlockPos blockPos;
    public Stack<BlockPos> stackToPlant;
    public Stack<BlockPos> stackToBreak;
    public Stack<BlockPos> stackToPlow;
    public List<NeededItem> neededItems = new ArrayList<>();
    public FarmerWorkGoal(FarmerEntity farmer) {
        this.farmer = farmer;
        setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return !farmer.needsToSleep() && farmer.shouldWork() && !farmer.needsToGetToChest() && this.isCropAreaNotRemoved();
    }

    private boolean isCropAreaNotRemoved() {
        if(farmer.currentCropArea == null || !farmer.currentCropArea.isRemoved()) return true;
        else {
            farmer.currentCropArea = null;
        }
        return false;
    }

    @Override
    public void start() {
        super.start();

        setState(State.SELECT_WORK_AREA);
    }
    boolean workDone;
    int cooldown;
    @Override
    public void tick() {
        super.tick();
        if(this.farmer.getCommandSenderWorld().isClientSide()) return;

        if(state == null) return;
        if(blockPos != null) this.farmer.getLookControl().setLookAt(blockPos.getCenter());
        if(farmer.tickCount % 5 != 0) return;

        if(!isCropAreaNotRemoved()) return;

        if(state != State.SELECT_WORK_AREA && this.farmer.currentCropArea == null){
            setState(State.SELECT_WORK_AREA);
            return;
        }

        if(blockPos != null && moveToPosition(blockPos, 20)) return;

        switch(state){
            case SELECT_WORK_AREA -> {
                if(this.farmer.currentCropArea != null) {
                    setState(State.MOVE_TO_WORK_AREA);
                    return;
                }

                if(!FarmerAreaSelectionTiming.shouldSearchForArea(false, ++cooldown)) return;
                this.cooldown = 0;

                List<CropArea> areas = getAvailableWorkAreasByPriority((ServerLevel) farmer.getCommandSenderWorld(), farmer, this.farmer.currentCropArea);

                if (!areas.isEmpty()) {
                    this.farmer.currentCropArea = areas.get(0);
                }

                if(this.farmer.currentCropArea == null) {
                    farmer.reportIdleReason("farmer_no_area", Component.literal(farmer.getName().getString() + ": Waiting for a crop area."));
                    return;
                }

                farmer.clearWorkStatus();
                this.farmer.currentCropArea.setBeingWorkedOn(true);
                this.farmer.currentCropArea.setTime(0);
                this.workDone = false;
                setState(State.MOVE_TO_WORK_AREA);
            }

            case MOVE_TO_WORK_AREA ->{
                this.blockPos = null;
                if(this.moveToPosition(this.farmer.currentCropArea.getOnPos(), 20)) return;
                setState(State.PREPARE_BREAK_BLOCKS);
            }
            case PREPARE_BREAK_BLOCKS -> {
                this.farmer.currentCropArea.scanBreakArea();

                this.stackToBreak = this.farmer.currentCropArea.stackToBreak;

                if(stackToBreak.isEmpty()){
                    setState(State.PREPARE_PLOWING);
                    return;
                }

                farmer.switchMainHandItem(itemStack -> itemStack.getItem().getDefaultInstance().isEmpty());

                setState(State.BREAK_BLOCKS);
            }
            case BREAK_BLOCKS -> {
                if(this.breakBlocks(this.stackToBreak)) return;

                setState(State.PREPARE_WATER_SPOT);
            }
            case PREPARE_WATER_SPOT -> {
                BlockState centerPosState = farmer.getCommandSenderWorld().getBlockState(this.farmer.currentCropArea.getWaterPosCenter());
                if(centerPosState.isAir()){

                    ItemStack itemStack = farmer.getMatchingItem(item -> farmer.isBucketWithWater(item));
                    if(itemStack == null){
                        farmer.requestRequiredItem(new NeededItem(item -> farmer.isBucketWithWater(item),  1, true),
                                "farmer_missing_water_bucket",
                                Component.literal(farmer.getName().getString() + ": I need a water bucket to prepare this field."));
                        return;
                    }
                    else if(itemStack.getItem() instanceof BucketItem bucketItem){
                        farmer.switchMainHandItem(item -> farmer.isBucketWithWater(item));

                        bucketItem.emptyContents(null,  farmer.getCommandSenderWorld(), this.farmer.currentCropArea.getWaterPosCenter(), null);
                    }
                }

                setState(State.PREPARE_PLOWING);
            }

            case PREPARE_PLOWING -> {
                this.farmer.currentCropArea.scanPlowArea();

                this.stackToPlow = this.farmer.currentCropArea.stackToPlow;
                if(stackToPlow.isEmpty()){
                    applyLoopDecision(FarmerLoopProgress.selectNextAction(false, false, true));
                    return;
                }

                farmer.switchMainHandItem(itemStack -> itemStack.getItem() instanceof HoeItem);

                boolean hasHoe = farmer.getMainHandItem().getItem() instanceof HoeItem;
                if(!hasHoe){
                    farmer.requestRequiredItem(new NeededItem(stack -> stack.getItem() instanceof HoeItem, 1, true),
                            "farmer_missing_hoe",
                            Component.literal(farmer.getName().getString() + ": I need a hoe to keep working."));
                    this.blockPos = null;
                    applyLoopDecision(FarmerLoopProgress.waitForRequiredItem(FarmerLoopProgress.Action.PREPARE_PLOWING));
                    return;
                }

                setState(State.PLOWING);
            }
            case PLOWING -> {
                if(this.plowBlocks(stackToPlow)) return;

                setState(State.PREPARE_PLANT_SEEDS);
            }

            case PREPARE_PLANT_SEEDS -> {
                this.farmer.currentCropArea.scanPlantArea();

                this.stackToPlant = this.farmer.currentCropArea.stackToPlant;
                if(stackToPlant.isEmpty()){
                    applyLoopDecision(FarmerLoopProgress.selectNextAction(false, false, false));
                    return;
                }

                ItemStack seedTemplate = FarmerPlantingPreparation.resolveSeedTemplate(this.farmer.currentCropArea.getSeedStack(), this.farmer.getInventory().items);
                if(!seedTemplate.isEmpty() && this.farmer.currentCropArea.getSeedStack().isEmpty()){
                    this.farmer.currentCropArea.setSeedStack(seedTemplate);
                    this.farmer.currentCropArea.updateType();
                }

                if(this.farmer.currentCropArea.getSeedStack().isEmpty()){
                    farmer.requestRequiredItem(new NeededItem(FarmerPlantingPreparation::isSupportedSeed, stackToPlant.size(), true),
                            "farmer_missing_seeds",
                            Component.literal(farmer.getName().getString() + ": I need seeds for this field."));
                    this.blockPos = null;
                    applyLoopDecision(FarmerLoopProgress.waitForRequiredItem(FarmerLoopProgress.Action.PREPARE_PLANT_SEEDS));
                    return;
                }

                ItemStack seedFromInv = farmer.getMatchingItem(itemStack -> ItemStack.isSameItemSameTags(itemStack, this.farmer.currentCropArea.getSeedStack()));
                if(seedFromInv == null){
                    ItemStack seedStack = this.farmer.currentCropArea.getSeedStack();
                    farmer.requestRequiredItem(new NeededItem(itemStack -> ItemStack.isSameItemSameTags(itemStack, seedStack),  stackToPlant.size(), true),
                            "farmer_missing_seeds",
                            Component.literal(farmer.getName().getString() + ": I need more seeds for this field."));
                    this.blockPos = null;
                    applyLoopDecision(FarmerLoopProgress.waitForRequiredItem(FarmerLoopProgress.Action.PREPARE_PLANT_SEEDS));
                    return;
                }

                farmer.clearWorkStatus();
                this.farmer.switchMainHandItem(itemStack -> itemStack.is(this.farmer.currentCropArea.getSeedStack().getItem()));

                setState(State.PLANT_SEEDS);
            }

            case PLANT_SEEDS -> {
                if(this.plantSeeds(stackToPlant)) return;

                setState(State.DONE);
            }

            case DONE -> {
                if(!workDone){
                    workDone = true;
                    setState(State.SELECT_WORK_AREA);

                    this.farmer.currentCropArea.setBeingWorkedOn(false);
                    blockPos = null;
                    this.farmer.currentCropArea = null;
                    this.cooldown = FarmerAreaSelectionTiming.cooldownAfterWorkCycle();

                    if(!this.neededItems.isEmpty()){
                        for(NeededItem neededItem : neededItems){
                            this.farmer.addNeededItem(neededItem);
                        }
                        this.neededItems.clear();
                    }
                    else {
                        farmer.clearWorkStatus();
                    }
                }
            }

            case ERROR ->{
                if(!errorMessageDone){
                    errorMessageDone = true;
                }
            }
        }
    }

    public void setState(State state) {
        //if(farmer.getOwner() != null) farmer.getOwner().sendSystemMessage(Component.literal(state.toString()));
        this.state = state;
    }

    private void applyLoopDecision(FarmerLoopProgress.Decision decision) {
        if (decision == null) {
            return;
        }

        switch (decision.action()) {
            case PREPARE_BREAK_BLOCKS -> setState(State.PREPARE_BREAK_BLOCKS);
            case PREPARE_PLOWING -> setState(State.PREPARE_PLOWING);
            case PREPARE_PLANT_SEEDS -> setState(State.PREPARE_PLANT_SEEDS);
            case WAIT_FOR_ITEM -> setState(mapActionToState(decision.resumeAction()));
            case FINISHED -> setState(State.DONE);
        }
    }

    private State mapActionToState(FarmerLoopProgress.Action action) {
        return switch (action) {
            case PREPARE_BREAK_BLOCKS -> State.PREPARE_BREAK_BLOCKS;
            case PREPARE_PLOWING -> State.PREPARE_PLOWING;
            case PREPARE_PLANT_SEEDS -> State.PREPARE_PLANT_SEEDS;
            case WAIT_FOR_ITEM, FINISHED -> state;
        };
    }

    @Override
    public boolean canContinueToUse() {
        return canUse();
    }

    @Override
    public boolean isInterruptable() {
        return true;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    public boolean plantSeeds(Stack<BlockPos> positions){
        if(positions != null){
            ItemStack seedFromInv = farmer.getMatchingItem(itemStack -> itemStack.is(this.farmer.currentCropArea.getSeedStack().getItem()));
            if(seedFromInv == null){
                seedFromInv = farmer.getMatchingItem(itemStack -> ItemStack.isSameItemSameTags(itemStack, this.farmer.currentCropArea.getSeedStack()));
            }
            if(seedFromInv == null){
                setState(State.PREPARE_PLANT_SEEDS);
                return false;
            }

            if(blockPos == null){
                if(!positions.isEmpty()) blockPos = positions.pop();
                return blockPos != null;
            }

            BlockState state = farmer.getCommandSenderWorld().getBlockState(blockPos);
            if(state.getBlock() instanceof CropBlock || state.getBlock() instanceof StemBlock){
                if(!positions.isEmpty()){
                    blockPos = positions.pop();
                }
                else{
                    this.blockPos = null;
                    return false;
                }
            }
            else if (seedFromInv.getItem() instanceof BlockItem blockItem) {
                farmer.getCommandSenderWorld().setBlockAndUpdate(blockPos, blockItem.getBlock().defaultBlockState());

                farmer.getCommandSenderWorld().playSound(null, blockPos.getX(), blockPos.getY(), blockPos.getZ(), SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
                seedFromInv.shrink(1);
                this.farmer.swing(InteractionHand.MAIN_HAND);
            }
            else if (seedFromInv.getItem() instanceof IPlantable plantable) {
                if (plantable.getPlantType(farmer.getCommandSenderWorld(), blockPos) == PlantType.CROP) {
                    farmer.getCommandSenderWorld().setBlock(blockPos, plantable.getPlant(farmer.getCommandSenderWorld(), blockPos), 3);

                    farmer.getCommandSenderWorld().playSound(null, blockPos.getX(), blockPos.getY(), blockPos.getZ(), SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
                    seedFromInv.shrink(1);
                    this.farmer.swing(InteractionHand.MAIN_HAND);
                }
            }
            return true;
        }
        this.blockPos = null;
        return false;
    }

    public boolean plowBlocks(Stack<BlockPos> positions){
        if(positions != null){
            boolean hasTool = farmer.getMainHandItem().getItem() instanceof HoeItem;
            if(!hasTool){
                setState(State.PREPARE_PLOWING);
                return true;
            }

            if(blockPos == null){
                if(!positions.isEmpty()) blockPos = positions.pop();
                return blockPos != null;
            }

            BlockState state = farmer.getCommandSenderWorld().getBlockState(blockPos);
            if(state.getBlock() instanceof FarmBlock){
                if(!positions.isEmpty()){
                    blockPos = positions.pop();
                }
                else{
                    this.blockPos = null;
                    return false;
                }
            }
            else{
                this.farmer.swing(InteractionHand.MAIN_HAND);
                farmer.getCommandSenderWorld().setBlock(blockPos, Blocks.FARMLAND.defaultBlockState(), 3);
                farmer.getCommandSenderWorld().playSound(null, blockPos.getX(), blockPos.getY(), blockPos.getZ(), SoundEvents.HOE_TILL, SoundSource.BLOCKS, 1.0F, 1.0F);
                this.farmer.damageMainHandItem();
            }
            return true;
        }
        return false;
    }
    int blockBreakTime;
    public boolean breakBlocks(Stack<BlockPos> positions){
        if(positions != null){
            if(blockPos == null){
                if(!positions.isEmpty()) blockPos = positions.pop();
                return blockPos != null;
            }

            if(AbstractWorkerEntity.isPosBroken(blockPos, this.farmer.getCommandSenderWorld(), true)){
                if(!positions.isEmpty()){
                    blockPos = positions.pop();
                }
                else{
                    this.blockPos = null;
                    return false;
                }
                blockBreakTime = 0;

            }
            else{
                this.farmer.mineBlock(blockPos);
                this.farmer.swing(InteractionHand.MAIN_HAND);
            }
            return true;
        }
        return false;
    }

    public static List<CropArea> getAvailableWorkAreasByPriority(ServerLevel level, FarmerEntity farmer, @Nullable CropArea currentArea) {
        List<CropArea> list = com.talhanation.bannermod.entity.civilian.workarea.WorkAreaIndex.instance()
                .queryInRange(farmer, 64, CropArea.class);

        Map<CropArea, Integer> priorityMap = new HashMap<>();

        for (CropArea area : list) {
            if (area == null || area == currentArea || !area.canWorkHere(farmer)) continue;

            int priority = 0;

            boolean perfectCandidate = area.isWorkerPerfectCandidate(farmer);

            if (perfectCandidate) {
                priority += 10;
            } else {
                priority += 1;
            }

            if (!area.isBeingWorkedOn()) {
                priority += 10;
            }

            priority += area.getTime() * 10;
            priority += WorkerBindingResume.priorityBoost(farmer.getBoundWorkAreaUUID(), area.getUUID());

            priorityMap.put(area, priority);
        }

        List<CropArea> sorted = new ArrayList<>(priorityMap.keySet());
        sorted.sort((a, b) -> Integer.compare(priorityMap.get(b), priorityMap.get(a)));

        return sorted;
    }



    public boolean moveToPosition(BlockPos pos, int threshold){
        if(pos == null){
            return false;
        }
        else{
            double distance = farmer.getHorizontalDistanceTo(pos.getCenter());
            if(distance < threshold){
                farmer.getNavigation().stop();
                return false;
            }
            else{
                farmer.getNavigation().moveTo(pos.getX(), pos.getY(), pos.getZ(), 0.8F);
                farmer.setFollowState(6); //Working
                farmer.getLookControl().setLookAt(pos.getCenter());
            }
            return true;
        }
    }

    public enum State{
        SELECT_WORK_AREA,
        MOVE_TO_WORK_AREA,
        PREPARE_BREAK_BLOCKS,
        BREAK_BLOCKS,
        PREPARE_WATER_SPOT,
        PREPARE_PLOWING,
        PLOWING,
        PREPARE_PLANT_SEEDS,
        PLANT_SEEDS,
        DONE,
        ERROR

    }
}
