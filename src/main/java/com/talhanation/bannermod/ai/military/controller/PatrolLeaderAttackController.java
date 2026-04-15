package com.talhanation.bannermod.ai.military.controller;

import com.talhanation.bannermod.entity.military.*;
import com.talhanation.recruits.util.FormationUtils;
import com.talhanation.recruits.util.RecruitCommanderUtil;
import com.talhanation.recruits.util.NPCArmy;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;

import java.util.List;


public class PatrolLeaderAttackController implements IAttackController {

    public final AbstractLeaderEntity leader;

    public int timeOut = 1000;
    public Vec3 initPos;

    public PatrolLeaderAttackController(AbstractLeaderEntity recruit) {
        this.leader = recruit;
    }

    public void start(){
        if(!this.leader.getCommandSenderWorld().isClientSide() && leader.enemyArmy != null && leader.army != null){
            double distanceToTarget = this.leader.army.getPosition().distanceToSqr(leader.enemyArmy.getPosition());

            this.leader.army.updateArmy();
            this.leader.enemyArmy.updateArmy();

            if(leader.enemyArmy.size() == 0){
                // enemy army defeated
                this.leader.enemyArmy = null;
                return;
            }
            //To far from init pos -> enemy army is retreating
            if(initPos != null && initPos.distanceToSqr(this.leader.army.getPosition()) > 5000){
                this.leader.enemyArmy = null;
                return;
            }

            RecruitCommanderUtil.setRecruitsAggroState(this.leader.army.getAllRecruitUnits(), leader.getState());


            RecruitCommanderUtil.setRecruitsMoveSpeed(this.leader.army.getAllRecruitUnits(), 1.0F);
            this.setRecruitsTargets();

            if(distanceToTarget < 2500) {
                if(isArmyScattered()){
                    regroupArmy();
                    return;
                }
                leader.commandCooldown = 400;


                if(leader.getEnemyAction() == AbstractLeaderEntity.EnemyAction.HOLD.getIndex()){
                    this.leader.getNavigation().stop();
                    this.setRecruitsTargets();
                    return;
                }

                commandArmy(this.leader.army, this.leader.enemyArmy);
            }
            else{
                if(leader.getOwner() != null) this.leader.getOwner().sendSystemMessage(Component.literal(leader.getName().getString() + ": Enemy contact! Im advancing, their size is " + leader.enemyArmy.size()));
                forwarding();
                leader.commandCooldown = 250;
            }
        }
    }

    public void tick() {
        if(leader.commandCooldown == 0){
            leader.commandCooldown = 400;
            start();
        }
    }

    @Override
    public void setInitPos(Vec3 pos) {
        initPos = pos;
    }

    @Override
    public boolean isTargetInRange() {
        return false;
    }

    private boolean isArmyScattered() {
        List<AbstractRecruitEntity> recruits = this.leader.army.getAllRecruitUnits();
        if (recruits.isEmpty()) return false;

        Vec3 commanderPos = this.leader.position();
        double maxDistance = 500.0;

        int scatteredCount = 0;
        for (AbstractRecruitEntity recruit : recruits) {
            double distance = recruit.position().distanceToSqr(commanderPos);
            if (distance > maxDistance) {
                scatteredCount++;
            }
        }


        return scatteredCount >= (recruits.size() / 2);
    }
    public void commandArmy(NPCArmy playerArmy, NPCArmy enemyArmy) {
        double distance = playerArmy.getPosition().distanceTo(enemyArmy.getPosition());
        BattleTacticDecider.ArmySnapshot ownArmy = BattleTacticDecider.snapshot(playerArmy);
        BattleTacticDecider.ArmySnapshot enemyArmySnapshot = BattleTacticDecider.snapshot(enemyArmy);

        BattleTacticDecider.Tactic tactic = BattleTacticDecider.decide(distance, ownArmy, enemyArmySnapshot);

        switch (tactic) {
            case CHARGE -> {
                sendToOwner("We have overwhelming advantage! Charging!");
                charge();
            }
            case ADVANCE -> {
                if (ownArmy.averageMorale() > 70 && enemyArmySnapshot.averageMorale() < 30) {
                    sendToOwner("We have a morale advantage! Advancing!");
                }
                else if (ownArmy.totalUnits() >= 2 * enemyArmySnapshot.totalUnits() || ownArmy.averageHealth() > 50) {
                    sendToOwner("We have overwhelming advantage! Charging!");
                }
                else {
                    sendToOwner("Default attacking!");
                }
                forwarding();
            }
            case RETREAT -> {
                if (enemyArmySnapshot.rangedUnits() > ownArmy.cavalryUnits() + ownArmy.shieldUnits()) {
                    sendToOwner("Enemy has ranged superiority! Need assistance!");
                }
                else {
                    sendToOwner("We are at a disadvantage! Retreating!");
                }
                back();
            }
            case DEFAULT_ATTACK -> {
                sendToOwner("Default attacking!");
                defaultAttack();
            }
        }

        if(distance < 200){
            RecruitCommanderUtil.setRecruitsShields(this.leader.army.getRecruitShieldmen(), false);
        }
        else if(enemyArmySnapshot.rangedUnits() >= ownArmy.shieldUnits()){
            RecruitCommanderUtil.setRecruitsShields(this.leader.army.getRecruitShieldmen(), true);
        }

    }

    public void shieldWall(){
        Vec3 target = leader.enemyArmy.getPosition();
        Vec3 toTarget = leader.position().vectorTo(target).normalize();
        Vec3 movePosInfantry = getPosTowardsTarget(target, 0.05);
        Vec3 movePosRanged = getPosTowardsTarget(target, -0.1);

        FormationUtils.lineFormation(toTarget, leader.army.getRecruitShieldmen(), movePosInfantry, 30, 1.0);
        FormationUtils.lineFormation(toTarget, leader.army.getRecruitInfantry(), movePosInfantry, 30, 1.0);

        FormationUtils.lineFormation(toTarget, leader.army.getRecruitRanged(), movePosRanged, 20, 1.8);

        RecruitCommanderUtil.setRecruitsWanderFreely(this.leader.army.getRecruitCavalry());
    }

    public void charge(){
        BlockPos movePosLeader = getBlockPosTowardsTarget(this.leader.enemyArmy.getPosition(), 0.2);
        this.leader.setHoldPos(Vec3.atCenterOf(movePosLeader));
        this.leader.setFollowState(3);//LEADER BACK TO POS

        RecruitCommanderUtil.setRecruitsWanderFreely(this.leader.army.getAllRecruitUnits());

        this.setRecruitsTargets();
    }
    public void defaultAttack(){
        Vec3 target = leader.enemyArmy.getPosition();
        Vec3 toTarget = leader.position().vectorTo(target).normalize();
        Vec3 movePosRanged = getPosTowardsTarget(target, 0.4);
        BlockPos movePosLeader = getBlockPosTowardsTarget(target, 0.2);
        Vec3 movePosInfantry = getPosTowardsTarget(target, 0.6);

        FormationUtils.lineFormation(toTarget, this.leader.army.getRecruitInfantry(), movePosInfantry, 20, 3.25);
        RecruitCommanderUtil.setRecruitsWanderFreely(this.leader.army.getRecruitShieldmen());

        FormationUtils.lineFormation(toTarget, leader.army.getRecruitRanged(), movePosRanged, 20, 3.25);

        RecruitCommanderUtil.setRecruitsWanderFreely(this.leader.army.getRecruitCavalry());

        this.setRecruitsTargets();

        this.leader.setHoldPos(Vec3.atCenterOf(movePosLeader));
        this.leader.setFollowState(3);//LEADER BACK TO POS
    }

    public void regroupArmy(){
        sendToOwner("Recruits regroup!");
        Vec3 target = leader.enemyArmy.getPosition();
        Vec3 toTarget = leader.position().vectorTo(target).normalize();
        Vec3 movePosInfantry = getPosTowardsTarget(target, 0.1);
        Vec3 movePosRanged = getPosTowardsTarget(target, -0.1);
        Vec3 movePosCav = getPosTowardsTarget(target, 0.0);

        FormationUtils.lineFormation(toTarget, leader.army.getRecruitInfantry(), movePosInfantry, 20, 1.75);
        FormationUtils.lineFormation(toTarget, leader.army.getRecruitShieldmen(), movePosInfantry, 10, 2.25);

        FormationUtils.lineFormation(toTarget, leader.army.getRecruitRanged(), movePosRanged, 20, 3.0);
        FormationUtils.squareFormation(toTarget, leader.army.getRecruitCavalry(), movePosCav, 2.0);

    }

    public void forwarding(){
        Vec3 target = leader.enemyArmy.getPosition();
        Vec3 toTarget = leader.position().vectorTo(target).normalize();
        Vec3 movePosInfantry = getPosTowardsTarget(target, 0.6);
        Vec3 movePosRanged = getPosTowardsTarget(target, 0.4);
        Vec3 movePosCav = getPosTowardsTarget(target, 0.2);
        BlockPos movePosLeader = getBlockPosTowardsTarget(target, 0.3);

        FormationUtils.lineFormation(toTarget, leader.army.getRecruitInfantry(), movePosInfantry, 20, 1.75);
        FormationUtils.lineFormation(toTarget, leader.army.getRecruitShieldmen(), movePosInfantry, 10, 2.25);

        FormationUtils.lineFormation(toTarget, leader.army.getRecruitRanged(), movePosRanged, 20, 3.0);
        FormationUtils.squareFormation(toTarget, leader.army.getRecruitCavalry(), movePosCav, 2.0);

        this.leader.setHoldPos(Vec3.atCenterOf(movePosLeader));
        this.leader.setFollowState(3);//LEADER BACK TO POS
    }

    public void back(){
        Vec3 target = leader.enemyArmy.getPosition();
        Vec3 toTarget = leader.position().vectorTo(target).normalize();
        Vec3 movePosInfantry = getPosTowardsTarget(target, -0.4);
        Vec3 movePosRanged = getPosTowardsTarget(target, -0.6);
        BlockPos movePosLeader = getBlockPosTowardsTarget(target, -0.7);

        FormationUtils.lineFormation(toTarget, leader.army.getRecruitInfantry(), movePosInfantry, 20, 1.25);
        FormationUtils.lineFormation(toTarget, leader.army.getRecruitShieldmen(), movePosInfantry, 20, 1.25);

        FormationUtils.lineFormation(toTarget, leader.army.getRecruitRanged(), movePosRanged, 20, 2.25);
        FormationUtils.lineFormation(toTarget, leader.army.getRecruitCavalry(), movePosRanged, 20, 2.25);


        this.leader.setHoldPos(Vec3.atCenterOf(movePosLeader));
        this.leader.setFollowState(3);//LEADER BACK TO POS
    }

    public BlockPos getBlockPosTowardsTarget(Vec3 target, double x){
        Vec3 pos = leader.position().lerp(target, x);
        return FormationUtils.getPositionOrSurface(leader.getCommandSenderWorld(), new BlockPos((int) pos.x, (int) pos.y, (int) pos.z));
    }
    public Vec3 getPosTowardsTarget(Vec3 target, double x){
        return leader.position().lerp(target, x);
    }

    public void setRecruitsTargets() {
        List<AbstractRecruitEntity> recruits = this.leader.army.getAllRecruitUnits();
        List<LivingEntity> enemies = this.leader.enemyArmy.getAllUnits();

        if (recruits.isEmpty() || enemies.isEmpty()) return;

        for (int i = 0; i < recruits.size(); i++) {
            AbstractRecruitEntity recruit = recruits.get(i);
            LivingEntity target = enemies.get(i % enemies.size());
            recruit.setTarget(target);
        }
    }

    public boolean canAttack(LivingEntity living) {
        int aggroState = this.leader.getState();
        switch(aggroState){
            case 0 -> { //Neutral
                if(living instanceof Monster){
                    return this.leader.canAttack(living);
                }
            }
            case 1 -> { //AGGRO
                if(living instanceof Player || living instanceof AbstractRecruitEntity || living instanceof Monster){
                    return this.leader.canAttack(living);
                }
            }
            case 2 -> { //RAID
                return this.leader.canAttack(living);
            }

            default -> {
                return false;
            }
        }
        return false;
    }

    public void sendToOwner(String string){
        if(leader.getOwner() != null)
            this.leader.getOwner().sendSystemMessage(Component.literal(leader.getName().getString() + ": " + string));

    }

}

