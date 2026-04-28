package com.talhanation.bannermod.ai.civilian;

import com.talhanation.bannermod.entity.civilian.LumberjackEntity;
import com.talhanation.bannermod.entity.civilian.WorkerBindingResume;
import com.talhanation.bannermod.entity.civilian.workarea.LumberArea;
import com.talhanation.bannermod.persistence.civilian.NeededItem;
import com.talhanation.bannermod.persistence.civilian.Tree;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShearsItem;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SaplingBlock;
import net.minecraft.world.level.block.state.BlockState;

import javax.annotation.Nullable;
import java.util.*;

public class LumberjackWorkGoal extends Goal {

    private static final int AREA_SEARCH_COOLDOWN_TICKS = 20;
    private static final int PATH_REQUEST_COOLDOWN_TICKS = 20;

    public LumberjackEntity lumberjack;
    public State state;
    public String errorMessage;
    public boolean errorMessageDone;
    public BlockPos blockPos;
    public Stack<Tree> stackOfTrees;
    public Stack<BlockPos> stackToPlant;
    public Tree currentTree;
    private int lastAreaSearchTick = -AREA_SEARCH_COOLDOWN_TICKS;
    private int lastPathRequestTick = -PATH_REQUEST_COOLDOWN_TICKS;
    @Nullable
    private BlockPos lastPathRequestPos;

    public LumberjackWorkGoal(LumberjackEntity lumberjack) {
        this.lumberjack = lumberjack;
        setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return !lumberjack.needsToSleep() && lumberjack.shouldWork() && !lumberjack.needsToGetToChest();
    }

    @Override
    public void start() {
        super.start();
        if(this.lumberjack.getCommandSenderWorld().isClientSide()) return;


        setState(State.SELECT_WORK_AREA);
    }
    boolean workDone;
    @Override
    public void tick() {
        super.tick();
        if(this.lumberjack.getCommandSenderWorld().isClientSide()) return;
        if(state == null) return;
        if(blockPos != null) this.lumberjack.getLookControl().setLookAt(blockPos.getCenter());

        if(state == State.WOOD_CUTTING){
            if(this.breakBlocks(currentTree.getStackToBreak())) return;

            currentTree.setInWork(false);
            setState(State.SELECT_TREE);
        }

        if(lumberjack.tickCount % 10 != 0) return;

        switch(state){
            case SELECT_WORK_AREA ->{
                if(lumberjack.getCurrentLumberArea() != null) setState(State.MOVE_TO_WORK_AREA);

                if(lumberjack.tickCount - lastAreaSearchTick < AREA_SEARCH_COOLDOWN_TICKS) return;
                lastAreaSearchTick = lumberjack.tickCount;

                List<LumberArea> areas = getAvailableWorkAreasByPriority((ServerLevel) lumberjack.getCommandSenderWorld(), lumberjack, lumberjack.getCurrentLumberArea());

                if (!areas.isEmpty()) {
                    lumberjack.setCurrentWorkArea(areas.get(0));
                }

                if(lumberjack.getCurrentLumberArea() == null) {
                    lumberjack.reportIdleReason("lumberjack_no_area", Component.literal(lumberjack.getName().getString() + ": Waiting for a lumber area."));
                    return;
                }

                lumberjack.clearWorkStatus();
                lumberjack.getCurrentLumberArea().setBeingWorkedOn(true);
                this.lumberjack.getCurrentLumberArea().setTime(0);
                workDone = false;
                setState(State.MOVE_TO_WORK_AREA);
            }

            case MOVE_TO_WORK_AREA ->{
                this.blockPos = null;
                if(this.moveToPosition(lumberjack.getCurrentLumberArea().getOnPos(), 100)) return;

                setState(State.SCAN_TREES);
            }

            case SCAN_TREES ->{
                if(lumberjack.getCurrentLumberArea().stackOfTrees.isEmpty()){
                    lumberjack.getCurrentLumberArea().scanForTrees();
                }

                this.stackOfTrees = lumberjack.getCurrentLumberArea().stackOfTrees;
                this.stackOfTrees.sort(Comparator.comparing(tree -> tree.getPosition().getCenter().distanceToSqr(lumberjack.position())));

                if(stackOfTrees.isEmpty()){

                    setState(State.PREPARE_PLANT_SAPLINGS);
                    return;
                }

                setState(State.SELECT_TREE);
            }

            case SELECT_TREE -> {
                if(stackOfTrees.isEmpty()){
                    setState(State.SCAN_TREES);
                    return;
                }

                this.currentTree = this.stackOfTrees.pop();
                this.currentTree.setInWork(true);
                setState(State.MOVE_TO_TREE);
            }

            case MOVE_TO_TREE -> {
                if(this.moveToPosition(currentTree.getPosition(), 30)) return;

                setState(State.PREPARE_SHEAR_LEAVES);
            }
            case PREPARE_SHEAR_LEAVES -> {
                if(!lumberjack.getCurrentLumberArea().getShearLeaves()){
                    setState(State.PREPARE_STRIP_LOGS);
                    return;
                }
                lumberjack.switchMainHandItem(itemStack -> itemStack.getItem() instanceof ShearsItem);

                boolean hasShears = lumberjack.getMainHandItem().getItem() instanceof ShearsItem;
                if(!hasShears){
                    lumberjack.requestRequiredItem(new NeededItem(stack -> stack.getItem() instanceof ShearsItem, 1, true),
                            "lumberjack_missing_shears",
                            Component.literal(lumberjack.getName().getString() + ": I need shears to trim leaves."));
                    this.blockPos = null;
                    return;
                }

                setState(State.SHEAR_LEAVES);
            }
            case SHEAR_LEAVES -> {
                if(lumberjack.getCurrentLumberArea().getShearLeaves() && this.shearLeaves(currentTree.getStackToShear())) return;

                setState(State.PREPARE_STRIP_LOGS);
            }
            case PREPARE_STRIP_LOGS -> {
                if(!lumberjack.getCurrentLumberArea().getStripLogs()){
                    setState(State.PREPARE_WOOD_CUTTING);
                    return;
                }

                lumberjack.switchMainHandItem(itemStack -> itemStack.getItem() instanceof AxeItem);

                boolean hasAxe = lumberjack.getMainHandItem().getItem() instanceof AxeItem;
                if(!hasAxe){
                    lumberjack.requestRequiredItem(new NeededItem(stack -> stack.getItem() instanceof AxeItem, 1, true),
                            "lumberjack_missing_axe",
                            Component.literal(lumberjack.getName().getString() + ": I need an axe to continue."));
                    this.blockPos = null;
                    return;
                }

                setState(State.STRIP_WOOD);
            }
            case STRIP_WOOD -> {
                if(lumberjack.getCurrentLumberArea().getStripLogs() && this.stripLogs(currentTree.getStackToStrip())) return;

                setState(State.PREPARE_WOOD_CUTTING);
            }
            case PREPARE_WOOD_CUTTING -> {
                lumberjack.switchMainHandItem(itemStack -> itemStack.getItem() instanceof AxeItem);

                boolean hasAxe = lumberjack.getMainHandItem().getItem() instanceof AxeItem;
                if(!hasAxe){
                    lumberjack.requestRequiredItem(new NeededItem(stack -> stack.getItem() instanceof AxeItem, 1, true),
                            "lumberjack_missing_axe",
                            Component.literal(lumberjack.getName().getString() + ": I need an axe to continue."));
                    this.blockPos = null;
                    return;
                }

                setState(State.WOOD_CUTTING);
            }
            case WOOD_CUTTING -> {

            }

            case PREPARE_PLANT_SAPLINGS -> {
                if(!lumberjack.getCurrentLumberArea().getReplant()){
                    setState(State.DONE);
                    return;
                }

                this.lumberjack.getCurrentLumberArea().scanPlantArea();
                this.stackToPlant = lumberjack.getCurrentLumberArea().stackToPlant;

                if(stackToPlant.isEmpty()){
                    setState(State.DONE);
                    return;
                }

                setState(State.PLANT_SAPLINGS);
            }

            case PLANT_SAPLINGS -> {
                if(lumberjack.getCurrentLumberArea().getReplant() && this.plantSaplings(lumberjack.getCurrentLumberArea().getStackToPlant())) return;

                setState(State.DONE);
            }

            case DONE -> {
                if(!workDone){
                    workDone = true;
                    lumberjack.getCurrentLumberArea().setBeingWorkedOn(false);
                    blockPos = null;
                    lumberjack.setCurrentWorkArea(null);
                    lumberjack.clearWorkStatus();
                    this.start();
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
        //if(lumberjack.getOwner() != null) lumberjack.getOwner().sendSystemMessage(Component.literal(state.toString()));
        this.state = state;
    }

    int blockBreakTime;
    public boolean breakBlocks(Stack<BlockPos> positions){
        if(positions != null){
            if(blockPos == null){
                if(!positions.isEmpty()) blockPos = positions.pop();
                return blockPos != null;
            }

            BlockState state = lumberjack.getCommandSenderWorld().getBlockState(blockPos);
            if(state.isAir()){
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
                this.lumberjack.mineBlock(blockPos);
                this.lumberjack.swing(InteractionHand.MAIN_HAND);
            }
            return true;
        }
        return false;
    }

    public boolean stripLogs(Stack<BlockPos> positions){
        if(positions != null){
            if(blockPos == null){
                if(!positions.isEmpty()) blockPos = positions.pop();
                return blockPos != null;
            }

            BlockState state = lumberjack.getCommandSenderWorld().getBlockState(blockPos);
            if(AxeItem.STRIPPABLES.containsValue(state.getBlock())){
                if(!positions.isEmpty()){
                    blockPos = positions.pop();
                }
                else{
                    this.blockPos = null;
                    return false;
                }
            }
            else{
                Block strippedBlock = AxeItem.STRIPPABLES.get(state.getBlock());

                if(strippedBlock == null){
                    this.blockPos = null;
                    return false;
                }

                this.lumberjack.getCommandSenderWorld().setBlock(blockPos, strippedBlock.defaultBlockState(), 3);
                this.lumberjack.getCommandSenderWorld().playSound(null, blockPos, SoundEvents.AXE_STRIP, SoundSource.BLOCKS, 1.0F, 1.0F);

                this.lumberjack.swing(InteractionHand.MAIN_HAND);
            }
            return true;
        }
        return false;
    }

    public boolean shearLeaves(Stack<BlockPos> positions){
        if(positions != null){
            if(blockPos == null){
                if(!positions.isEmpty()) blockPos = positions.pop();
                return blockPos != null;
            }

            BlockState state = lumberjack.getCommandSenderWorld().getBlockState(blockPos);
            if(state.isAir()){
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
                this.lumberjack.mineBlock(blockPos);
                this.lumberjack.swing(InteractionHand.MAIN_HAND);
            }
            return true;
        }
        return false;
    }

    public boolean plantSaplings(Stack<BlockPos> positions){
        if(positions != null){
            ItemStack saplingFromInv;
            if(lumberjack.getCurrentLumberArea().getSaplingStack().isEmpty()){
                saplingFromInv = lumberjack.getMatchingItem(itemStack -> itemStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof SaplingBlock);
                if(saplingFromInv == null){
                    lumberjack.addNeededItem(new NeededItem(itemStack -> itemStack.getItem() instanceof BlockItem blockItem && blockItem.getBlock() instanceof SaplingBlock,  8, false));
                    this.blockPos = null;
                    return false;
                }

            }
            else{
                saplingFromInv = lumberjack.getMatchingItem(itemStack -> itemStack.is(lumberjack.getCurrentLumberArea().getSaplingStack().getItem()));
                if(saplingFromInv == null){
                    lumberjack.addNeededItem(new NeededItem(itemStack -> ItemStack.isSameItemSameComponents(itemStack, lumberjack.getCurrentLumberArea().getSaplingStack()),  8, false));
                    this.blockPos = null;
                    return false;
                }
            }

            if(blockPos == null){
                if(!positions.isEmpty()) blockPos = positions.pop();
                return blockPos != null;
            }

            BlockState state = lumberjack.getCommandSenderWorld().getBlockState(blockPos);
            if(!state.isAir()){
                if(!positions.isEmpty()){
                    blockPos = positions.pop();
                }
                else{
                    this.blockPos = null;
                    return false;
                }
            }
            else if (saplingFromInv.getItem() instanceof BlockItem blockItem) {
                lumberjack.getCommandSenderWorld().setBlockAndUpdate(blockPos, blockItem.getBlock().defaultBlockState());

                lumberjack.getCommandSenderWorld().playSound(null, blockPos.getX(), blockPos.getY(), blockPos.getZ(), SoundEvents.CROP_PLANTED, SoundSource.BLOCKS, 1.0F, 1.0F);
                saplingFromInv.shrink(1);
                this.lumberjack.swing(InteractionHand.MAIN_HAND);
            }
            return true;
        }
        this.blockPos = null;
        return false;
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

    public static List<LumberArea> getAvailableWorkAreasByPriority(ServerLevel level, LumberjackEntity lumberjack, @Nullable LumberArea currentArea) {
        List<LumberArea> list = com.talhanation.bannermod.entity.civilian.workarea.WorkAreaIndex.instance()
                .queryInRange(lumberjack, 64, LumberArea.class);

        Map<LumberArea, Integer> priorityMap = new HashMap<>();

        for (LumberArea area : list) {
            if (area == null || area == currentArea || !area.canWorkHere(lumberjack)) continue;

            int priority = 0;

            boolean perfectCandidate = area.isWorkerPerfectCandidate(lumberjack);

            if (perfectCandidate) {
                priority += 10;
            } else {
                priority += 1;
            }

            if (!area.isBeingWorkedOn()) {
                priority += 3;
            }

            priority += area.time;
            priority += WorkerBindingResume.priorityBoost(lumberjack.getBoundWorkAreaUUID(), area.getUUID());

            //double dist = area.position().distanceToSqr(lumberjack.position());
            //priority -= dist / 10.0;

            priorityMap.put(area, priority);
        }

        List<LumberArea> sorted = new ArrayList<>(priorityMap.keySet());
        sorted.sort((a, b) -> Integer.compare(priorityMap.get(b), priorityMap.get(a)));

        return sorted;
    }



    public boolean moveToPosition(BlockPos pos, int threshold){
        if(pos == null){
            return false;
        }
        else{
            double distance = lumberjack.getHorizontalDistanceTo(pos.getCenter());
            if(distance < threshold){
                lastPathRequestPos = null;
                return false;
            }
            else{
                if(shouldRequestPath(pos)){
                    lumberjack.getNavigation().moveTo(pos.getX(), pos.getY(), pos.getZ(), 0.8F);
                }
                lumberjack.setFollowState(6); //Working
                lumberjack.getLookControl().setLookAt(pos.getCenter());
            }
            return true;
        }
    }

    private boolean shouldRequestPath(BlockPos pos) {
        if(!pos.equals(lastPathRequestPos) || lumberjack.tickCount - lastPathRequestTick >= PATH_REQUEST_COOLDOWN_TICKS){
            lastPathRequestPos = pos;
            lastPathRequestTick = lumberjack.tickCount;
            return true;
        }
        return false;
    }

    public enum State{
        SELECT_WORK_AREA,
        MOVE_TO_WORK_AREA,
        SCAN_TREES,
        SELECT_TREE,
        MOVE_TO_TREE,
        PREPARE_SHEAR_LEAVES,
        SHEAR_LEAVES,
        PREPARE_STRIP_LOGS,
        STRIP_WOOD,
        PREPARE_WOOD_CUTTING,
        WOOD_CUTTING,
        PREPARE_PLANT_SAPLINGS,
        PLANT_SAPLINGS,
        DONE,
        ERROR

    }
}
