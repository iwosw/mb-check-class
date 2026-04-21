package com.talhanation.bannermod.ai.civilian;

import com.talhanation.bannermod.entity.civilian.AnimalFarmerEntity;
import com.talhanation.bannermod.entity.civilian.WorkerBindingResume;
import com.talhanation.bannermod.entity.civilian.workarea.AnimalPenArea;
import com.talhanation.bannermod.persistence.civilian.NeededItem;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.animal.Animal;
import net.minecraft.world.entity.animal.Sheep;
import net.minecraft.world.entity.projectile.ThrownEgg;
import net.minecraft.world.item.AxeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.*;

public class AnimalFarmerWorkGoal extends Goal {

    public AnimalFarmerEntity animalFarmerEntity;
    public State state;
    public boolean errorMessageDone;
    public Animal animal;
    public AnimalPenArea.AnimalTypes animalType;
    public int amountToBreed;
    public int amountToSlaughter;
    public Stack<Animal> stackOfAnimals = new Stack<>();
    public List<NeededItem> neededItems = new ArrayList<>();
    public int time;
    public boolean isHolding;

    public AnimalFarmerWorkGoal(AnimalFarmerEntity animalFarmerEntity) {
        this.animalFarmerEntity = animalFarmerEntity;
        setFlags(EnumSet.of(Flag.LOOK, Flag.MOVE));
    }

    @Override
    public boolean canUse() {
        return !animalFarmerEntity.needsToSleep() && animalFarmerEntity.shouldWork() && !animalFarmerEntity.needsToGetToChest();
    }

    @Override
    public void start() {
        super.start();
        if(this.animalFarmerEntity.getCommandSenderWorld().isClientSide()) return;
        animalFarmerEntity.setAggroState(3);
        setState(State.SELECT_WORK_AREA);
    }

    @Override
    public void stop() {
        super.stop();
        animalFarmerEntity.setAggroState(0);
        clearCurrentPenBusyState();
    }

    @Override
    public void tick() {
        super.tick();
        if(this.animalFarmerEntity.getCommandSenderWorld().isClientSide()) return;
        if(state == null) return;
        if(state != State.SELECT_WORK_AREA && !hasUsableCurrentPen()) {
            discardInvalidCurrentPen();
            setState(State.SELECT_WORK_AREA);
            return;
        }
        if(animal != null) this.animalFarmerEntity.getLookControl().setLookAt(animal);
        if(this.animalFarmerEntity.tickCount % 20 != 0) return;

        switch(state){
            case SELECT_WORK_AREA ->{
                if(hasUsableCurrentPen()) {
                    prepareCurrentPen();
                    setState(State.MOVE_TO_WORK_AREA);
                    return;
                }

                discardInvalidCurrentPen();

                List<AnimalPenArea> areas = getAvailableWorkAreasByPriority((ServerLevel) animalFarmerEntity.getCommandSenderWorld(), animalFarmerEntity, animalFarmerEntity.currentAnimalPen);

                if (!areas.isEmpty()) {
                    this.animalFarmerEntity.currentAnimalPen = areas.get(0);
                }

                if(this.animalFarmerEntity.currentAnimalPen == null) {
                    animalFarmerEntity.reportIdleReason("animal_farmer_no_pen", Component.literal(animalFarmerEntity.getName().getString() + ": Waiting for an animal pen."));
                    return;
                }

                prepareCurrentPen();

                setState(State.MOVE_TO_WORK_AREA);
            }

            case MOVE_TO_WORK_AREA ->{
                this.animal = null;
                if(this.moveToPosition(animalFarmerEntity.currentAnimalPen.position(), 100)) return;

                setState(State.MOVE_TO_CENTER);
            }

            case MOVE_TO_CENTER ->{
                this.animal = null;
                if(this.moveToPosition(animalFarmerEntity.currentAnimalPen.position(), 30)) return;

                setState(State.PREPARE_BREED);
            }

            case PREPARE_BREED -> {
                if(!animalFarmerEntity.currentAnimalPen.getBreed() || !animalFarmerEntity.currentAnimalPen.isBreedTime()){
                    applyLoopDecision(AnimalFarmerLoopProgress.selectNextAction(false, false, 0,
                            animalFarmerEntity.currentAnimalPen.getSpecial(), 1,
                            animalType == AnimalPenArea.AnimalTypes.CHICKEN,
                            animalFarmerEntity.currentAnimalPen.getSlaughter(),
                            animalFarmerEntity.currentAnimalPen.animalsToSlaughter.size(),
                            animalFarmerEntity.currentAnimalPen.getMaxAnimals()));
                    return;
                }

                animalFarmerEntity.currentAnimalPen.scanAnimalBreed();
                this.stackOfAnimals = animalFarmerEntity.currentAnimalPen.animalsToBreed;

                if(stackOfAnimals.isEmpty()){
                    setState(State.PREPARE_SPECIAL_TASK);
                    return;
                }

                amountToBreed = stackOfAnimals.size();
                amountToBreed -= amountToBreed % 2;

                Item breedItem = animalType.getBreedItem();
                animalFarmerEntity.switchMainHandItem(itemStack -> itemStack.is(breedItem));
                ItemStack mainHandItem = animalFarmerEntity.getMainHandItem();

                boolean hasBreedItem = mainHandItem.is(breedItem);
                if(!hasBreedItem){
                    animalFarmerEntity.requestRequiredItem(new NeededItem(stack -> stack.is(breedItem), amountToBreed, true),
                            "animal_farmer_missing_breed_item",
                            Component.literal(animalFarmerEntity.getName().getString() + ": I need breeding items to continue."));
                    this.animal = null;
                    applyLoopDecision(AnimalFarmerLoopProgress.waitForRequiredItem(AnimalFarmerLoopProgress.Action.PREPARE_BREED));
                    return;
                }

                setState(State.BREED);
            }

            case BREED -> {
                if(animalFarmerEntity.currentAnimalPen.getBreed() && animalFarmerEntity.currentAnimalPen.isBreedTime() && this.breed()) return;

                this.animalFarmerEntity.currentAnimalPen.setBreedTime(2000);
                applyLoopDecision(AnimalFarmerLoopProgress.selectNextAction(false, false, 0,
                        animalFarmerEntity.currentAnimalPen.getSpecial(), 1,
                        animalType == AnimalPenArea.AnimalTypes.CHICKEN,
                        animalFarmerEntity.currentAnimalPen.getSlaughter(),
                        animalFarmerEntity.currentAnimalPen.animalsToSlaughter.size(),
                        animalFarmerEntity.currentAnimalPen.getMaxAnimals()));
            }

            case PREPARE_SPECIAL_TASK -> {
                if(!animalFarmerEntity.currentAnimalPen.getSpecial()){
                    applyLoopDecision(AnimalFarmerLoopProgress.selectNextAction(false, false, 0,
                            false, 0, false,
                            animalFarmerEntity.currentAnimalPen.getSlaughter(),
                            animalFarmerEntity.currentAnimalPen.animalsToSlaughter.size(),
                            animalFarmerEntity.currentAnimalPen.getMaxAnimals()));
                    return;
                }

                if(!animalFarmerEntity.hasFreeInvSlot()){
                    animalFarmerEntity.forcedDeposit = true;
                    applyLoopDecision(AnimalFarmerLoopProgress.waitForDeposit(AnimalFarmerLoopProgress.Action.PREPARE_SPECIAL_TASK));
                    return;
                }

                this.animalFarmerEntity.currentAnimalPen.scanAnimalSpecial();
                this.stackOfAnimals = animalFarmerEntity.currentAnimalPen.animalsForSpecialTask;

                if(stackOfAnimals.isEmpty() && animalType != AnimalPenArea.AnimalTypes.CHICKEN){
                    applyLoopDecision(AnimalFarmerLoopProgress.selectNextAction(false, false, 0,
                            false, 0, false,
                            animalFarmerEntity.currentAnimalPen.getSlaughter(),
                            animalFarmerEntity.currentAnimalPen.animalsToSlaughter.size(),
                            animalFarmerEntity.currentAnimalPen.getMaxAnimals()));
                    return;
                }
                setState(State.SPECIAL_TASK);
            }

            case SPECIAL_TASK -> {
                if(animalFarmerEntity.currentAnimalPen.getSpecial() && this.doSpecialTask()) return;
                applyLoopDecision(AnimalFarmerLoopProgress.selectNextAction(false, false, 0,
                        false, 0, false,
                        animalFarmerEntity.currentAnimalPen.getSlaughter(),
                        animalFarmerEntity.currentAnimalPen.animalsToSlaughter.size(),
                        animalFarmerEntity.currentAnimalPen.getMaxAnimals()));
            }

            case PREPARE_SLAUGHTER -> {
                if(!animalFarmerEntity.currentAnimalPen.getSlaughter()){
                    applyLoopDecision(AnimalFarmerLoopProgress.selectNextAction(false, false, 0,
                            false, 0, false,
                            false, 0,
                            animalFarmerEntity.currentAnimalPen.getMaxAnimals()));
                    return;
                }

                if(!animalFarmerEntity.hasFreeInvSlot()){
                    animalFarmerEntity.forcedDeposit = true;
                    applyLoopDecision(AnimalFarmerLoopProgress.waitForDeposit(AnimalFarmerLoopProgress.Action.PREPARE_SLAUGHTER));
                    return;
                }

                this.animalFarmerEntity.currentAnimalPen.scanAnimalSlaughter();
                this.stackOfAnimals = animalFarmerEntity.currentAnimalPen.animalsToSlaughter;

                int max = animalFarmerEntity.currentAnimalPen.getMaxAnimals();
                int size = stackOfAnimals.size();

                if(max >= size){
                    applyLoopDecision(AnimalFarmerLoopProgress.selectNextAction(false, false, 0,
                            false, 0, false,
                            true, size,
                            max));
                    return;
                }

                amountToSlaughter = size - max;

                animalFarmerEntity.switchMainHandItem(itemStack -> itemStack.getItem() instanceof AxeItem);

                boolean hasAxe = animalFarmerEntity.getMainHandItem().getItem() instanceof AxeItem;
                if(!hasAxe){
                    animalFarmerEntity.requestRequiredItem(new NeededItem(stack -> stack.getItem() instanceof AxeItem, 1, true),
                            "animal_farmer_missing_axe",
                            Component.literal(animalFarmerEntity.getName().getString() + ": I need an axe to continue."));
                    this.animal = null;
                    applyLoopDecision(AnimalFarmerLoopProgress.waitForRequiredItem(AnimalFarmerLoopProgress.Action.PREPARE_SLAUGHTER));
                    return;
                }

                setState(State.SLAUGHTER);
            }
            case SLAUGHTER -> {
                if(animalFarmerEntity.currentAnimalPen.getSlaughter() && this.slaughter()) return;

                applyLoopDecision(AnimalFarmerLoopProgress.selectNextAction(false, false, 0,
                        false, 0, false,
                        false, 0,
                        animalFarmerEntity.currentAnimalPen.getMaxAnimals()));
            }

            case DONE -> {
                finishCurrentPen();
                animalFarmerEntity.switchMainHandItem(ItemStack::isEmpty);
                animalFarmerEntity.clearWorkStatus();
                setState(State.SELECT_WORK_AREA);

                if(!this.neededItems.isEmpty()){
                    for(NeededItem neededItem : neededItems){
                        this.animalFarmerEntity.addNeededItem(neededItem);
                    }
                    this.neededItems.clear();
                }
            }

            case ERROR ->{
                if(!errorMessageDone){
                    errorMessageDone = true;
                }

                if(++time > 1000){
                    time = 0;
                    this.start();
                }

            }
        }
    }

    private boolean breed() {
        if(this.animal == null) {
            if(!stackOfAnimals.isEmpty()) animal = stackOfAnimals.pop();
            else return false;
        }
        if(this.moveToPosition(animal.position(), 6)) return true;
        this.animalFarmerEntity.getLookControl().setLookAt(animal);

        Item breedItem = animalType.getBreedItem();
        animalFarmerEntity.switchMainHandItem(itemStack -> itemStack.is(breedItem));
        ItemStack mainHandItem = animalFarmerEntity.getMainHandItem();

        boolean hasBreedItem = mainHandItem.is(breedItem);
        if(!hasBreedItem){
            animalFarmerEntity.addNeededItem(new NeededItem(stack -> stack.is(breedItem), amountToBreed, false));
            this.animal = null;
            return false;
        }

        this.animalFarmerEntity.swing(InteractionHand.MAIN_HAND);
        this.animalFarmerEntity.getMainHandItem().shrink(1);
        amountToBreed--;
        animal.setAge(0);

        animal.setInLove(null);

        this.animal = null;
        return true;
    }

    private boolean doSpecialTask() {
        Item specialItem = animalType.getSpecialItem();

        animalFarmerEntity.switchMainHandItem(itemStack -> itemStack.is(specialItem));

        ItemStack mainHandItem = animalFarmerEntity.getMainHandItem();

        boolean hasSpecialItem = mainHandItem.is(specialItem);
        if(!hasSpecialItem){
            boolean chicken = animalType == AnimalPenArea.AnimalTypes.CHICKEN;

            animalFarmerEntity.requestRequiredItem(new NeededItem(stack -> stack.is(specialItem),  chicken ? 32 : 1, !chicken),
                    "animal_farmer_missing_special_item",
                    Component.literal(animalFarmerEntity.getName().getString() + ": I need the right tool or item to continue."));
            this.animal = null;
            applyLoopDecision(AnimalFarmerLoopProgress.waitForRequiredItem(AnimalFarmerLoopProgress.Action.PREPARE_SPECIAL_TASK));
            return true;
        }
        if(animalType == AnimalPenArea.AnimalTypes.CHICKEN){

            return throwEggs();
        }

        if(this.animal == null) {
            if(!stackOfAnimals.isEmpty()) animal = stackOfAnimals.pop();
            else return false;
        }
        if(this.moveToPosition(animal.position(), 6)) return true;

        if(animalType == AnimalPenArea.AnimalTypes.SHEEP){
            return sheerSheep();
        } else if (animalType == AnimalPenArea.AnimalTypes.COW) {
            return milkCow();
        }

        return false;
    }

    public boolean slaughter() {
        animalFarmerEntity.switchMainHandItem(itemStack -> itemStack.getItem() instanceof AxeItem);

        boolean hasAxe = animalFarmerEntity.getMainHandItem().getItem() instanceof AxeItem;
        if(!hasAxe){
            animalFarmerEntity.requestRequiredItem(new NeededItem(stack -> stack.getItem() instanceof AxeItem, 1, true),
                    "animal_farmer_missing_axe",
                    Component.literal(animalFarmerEntity.getName().getString() + ": I need an axe to continue."));
            this.animal = null;
            applyLoopDecision(AnimalFarmerLoopProgress.waitForRequiredItem(AnimalFarmerLoopProgress.Action.PREPARE_SLAUGHTER));
            return false;
        }

        int max = animalFarmerEntity.currentAnimalPen.getMaxAnimals();
        int size = stackOfAnimals.size();

        if(max >= size || amountToSlaughter == 0){
            setState(State.DONE);
            return false;
        }

        if(this.animal == null) {
            if(!stackOfAnimals.isEmpty()) animal = stackOfAnimals.pop();
            else return false;
        }

        if(this.moveToPosition(animal.position(), 6)) return true;
        this.animalFarmerEntity.getLookControl().setLookAt(animal);

        animalFarmerEntity.playSound(SoundEvents.PLAYER_ATTACK_STRONG);
        this.animalFarmerEntity.swing(InteractionHand.MAIN_HAND);

        animal.kill();
        amountToSlaughter--;

        animalFarmerEntity.damageMainHandItem();

        animal = null;
        return true;
    }


    private boolean throwEggs() {
        if(!this.animalFarmerEntity.getMainHandItem().is(Items.EGG)){
            return false;
        }
        if(this.moveToPosition(animalFarmerEntity.currentAnimalPen.getArea().getCenter(), 10)){
            return true;
        }

        animalFarmerEntity.getCommandSenderWorld().playSound(null, animalFarmerEntity.getX(), animalFarmerEntity.getY(), animalFarmerEntity.getZ(), SoundEvents.EGG_THROW, SoundSource.PLAYERS, 0.5F, 0.4F / (animalFarmerEntity.getRandom().nextFloat() * 0.4F + 0.8F));
        ThrownEgg thrownegg = new ThrownEgg(animalFarmerEntity.getCommandSenderWorld(), animalFarmerEntity);
        thrownegg.setItem(new ItemStack(Items.EGG));

        double d0 = animalFarmerEntity.currentAnimalPen.getArea().getCenter().x() - this.animalFarmerEntity.getX();
        double d2 = animalFarmerEntity.currentAnimalPen.getArea().getCenter().z() - this.animalFarmerEntity.getZ();

        thrownegg.shoot(d0, 0, d2, 0.1F, 0F);

        if(animalFarmerEntity.getCommandSenderWorld().addFreshEntity(thrownegg)){
            this.animalFarmerEntity.getMainHandItem().shrink(1);
        }

        return true;
    }

    public boolean sheerSheep() {
        if(animal == null) return false;

        if(animal instanceof Sheep sheep){
            sheep.shear(SoundSource.PLAYERS);
            sheep.setSheared(true);

            this.animalFarmerEntity.swing(InteractionHand.MAIN_HAND);
            this.animalFarmerEntity.damageMainHandItem();
        }
        this.animal = null;
        return true;
    }

    public boolean milkCow() {
        if(animal == null) return false;

        animalFarmerEntity.getMainHandItem().shrink(1);
        animalFarmerEntity.getInventory().addItem(Items.MILK_BUCKET.getDefaultInstance());
        animal.playSound(SoundEvents.COW_MILK, 1.0F, 1.0F);

        animal = null;
        return true;
    }

    public void setState(State state) {
        this.state = state;
    }

    private void applyLoopDecision(AnimalFarmerLoopProgress.Decision decision) {
        if (decision == null) {
            return;
        }

        switch (decision.action()) {
            case PREPARE_BREED -> setState(State.PREPARE_BREED);
            case PREPARE_SPECIAL_TASK -> setState(State.PREPARE_SPECIAL_TASK);
            case PREPARE_SLAUGHTER -> setState(State.PREPARE_SLAUGHTER);
            case WAIT_FOR_ITEM, WAIT_FOR_DEPOSIT -> setState(mapActionToState(decision.resumeAction()));
            case FINISHED -> setState(State.DONE);
        }
    }

    private State mapActionToState(AnimalFarmerLoopProgress.Action action) {
        return switch (action) {
            case PREPARE_BREED -> State.PREPARE_BREED;
            case PREPARE_SPECIAL_TASK -> State.PREPARE_SPECIAL_TASK;
            case PREPARE_SLAUGHTER -> State.PREPARE_SLAUGHTER;
            case WAIT_FOR_ITEM, WAIT_FOR_DEPOSIT, FINISHED -> state;
        };
    }

    private boolean hasUsableCurrentPen() {
        return this.animalFarmerEntity.currentAnimalPen != null
                && !this.animalFarmerEntity.currentAnimalPen.isRemoved()
                && this.animalFarmerEntity.currentAnimalPen.canWorkHere(this.animalFarmerEntity);
    }

    private void prepareCurrentPen() {
        if (!hasUsableCurrentPen()) {
            return;
        }

        animalFarmerEntity.clearWorkStatus();
        this.animalFarmerEntity.currentAnimalPen.setBeingWorkedOn(true);
        this.animalFarmerEntity.currentAnimalPen.setTime(0);
        this.animalType = this.animalFarmerEntity.currentAnimalPen.getAnimalType();
    }

    private void clearCurrentPenBusyState() {
        if (this.animalFarmerEntity.currentAnimalPen != null) {
            this.animalFarmerEntity.currentAnimalPen.setBeingWorkedOn(false);
        }
    }

    private void discardInvalidCurrentPen() {
        clearCurrentPenBusyState();
        this.animalFarmerEntity.currentAnimalPen = null;
        this.animal = null;
    }

    private void finishCurrentPen() {
        clearCurrentPenBusyState();
        this.animalFarmerEntity.currentAnimalPen = null;
        this.animal = null;
    }

    @Override
    public boolean canContinueToUse() {
        return canUse() || this.isHolding;
    }

    @Override
    public boolean isInterruptable() {
        return true;
    }

    @Override
    public boolean requiresUpdateEveryTick() {
        return true;
    }

    public static List<AnimalPenArea> getAvailableWorkAreasByPriority(ServerLevel level, AnimalFarmerEntity animalFarmerEntity, @Nullable AnimalPenArea currentArea) {
        List<AnimalPenArea> list = com.talhanation.bannermod.entity.civilian.workarea.WorkAreaIndex.instance()
                .queryInRange(animalFarmerEntity, 64, AnimalPenArea.class);

        Map<AnimalPenArea, Integer> priorityMap = new HashMap<>();

        for (AnimalPenArea area : list) {
            if (area == null || area == currentArea || !area.canWorkHere(animalFarmerEntity)) continue;

            int priority = 0;

            boolean perfectCandidate = area.isWorkerPerfectCandidate(animalFarmerEntity);

            if (perfectCandidate) {
                priority += 10;
            } else {
                priority += 1;
            }

            if (!area.isBeingWorkedOn()) {
                priority += 3;
            }

            priority += area.time;
            priority += WorkerBindingResume.priorityBoost(animalFarmerEntity.getBoundWorkAreaUUID(), area.getUUID());

            priorityMap.put(area, priority);
        }

        List<AnimalPenArea> sorted = new ArrayList<>(priorityMap.keySet());
        sorted.sort((a, b) -> Integer.compare(priorityMap.get(b), priorityMap.get(a)));

        return sorted;
    }

    public boolean moveToPosition(Vec3 pos, int threshold){
        if(pos == null){
            return false;
        }
        else{
            double distance = animalFarmerEntity.getHorizontalDistanceTo(pos);
            if(distance < threshold){
                return false;
            }
            else{
                animalFarmerEntity.getNavigation().moveTo(pos.x(), pos.y(), pos.z(), 0.8F);
                animalFarmerEntity.setFollowState(6); //Working
                animalFarmerEntity.getLookControl().setLookAt(pos);
            }
            return true;
        }
    }

    public enum State{
        SELECT_WORK_AREA,
        MOVE_TO_WORK_AREA,
        MOVE_TO_CENTER,
        PREPARE_BREED,
        BREED,
        PREPARE_SLAUGHTER,
        SLAUGHTER,
        PREPARE_SPECIAL_TASK,
        SPECIAL_TASK,
        DONE,
        ERROR;

    }
}
