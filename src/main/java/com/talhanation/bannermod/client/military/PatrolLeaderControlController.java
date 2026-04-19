package com.talhanation.bannermod.client.military;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.military.AbstractLeaderEntity;
import com.talhanation.bannermod.entity.military.AbstractLeaderEntity.EnemyAction;
import com.talhanation.bannermod.entity.military.AbstractLeaderEntity.InfoMode;
import com.talhanation.bannermod.network.messages.military.MessageAssignGroupToCompanion;
import com.talhanation.bannermod.network.messages.military.MessagePatrolLeaderSetEnemyAction;
import com.talhanation.bannermod.network.messages.military.MessagePatrolLeaderSetInfoMode;
import com.talhanation.bannermod.network.messages.military.MessagePatrolLeaderSetPatrolState;
import com.talhanation.bannermod.network.messages.military.MessageRemoveAssignedGroupFromCompanion;
import com.talhanation.bannermod.network.messages.military.MessageSetLeaderGroup;
import com.talhanation.bannermod.persistence.military.RecruitsGroup;
import com.talhanation.bannermod.persistence.military.RecruitsRoute;
import net.minecraft.world.entity.player.Player;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.List;

public final class PatrolLeaderControlController {
    private final Player player;
    private final AbstractLeaderEntity leaderEntity;
    private final PatrolRouteAssignmentController routeAssignments = new PatrolRouteAssignmentController();

    private AbstractLeaderEntity.State patrolState;
    private InfoMode infoMode;
    private EnemyAction enemyAction;
    @Nullable
    private RecruitsRoute selectedRoute;
    @Nullable
    private RecruitsGroup selectedGroup;

    public PatrolLeaderControlController(AbstractLeaderEntity leaderEntity, Player player) {
        this.player = player;
        this.leaderEntity = leaderEntity;
    }

    public void init() {
        patrolState = AbstractLeaderEntity.State.fromIndex(leaderEntity.getPatrollingState());
        infoMode = InfoMode.fromIndex(leaderEntity.getInfoMode());
        enemyAction = EnemyAction.fromIndex(leaderEntity.getEnemyAction());

        routeAssignments.loadClientRoutes();
        selectedRoute = routeAssignments.getAssignedRoute(leaderEntity);
        selectedGroup = leaderEntity.getGroup() == null ? null : ClientManager.getGroup(leaderEntity.getGroup());
    }

    public List<RecruitsRoute> getRouteOptions() {
        return routeAssignments.getRouteOptions();
    }

    public List<RecruitsGroup> getGroupOptions() {
        List<RecruitsGroup> groupOptions = new ArrayList<>();
        groupOptions.add(null);
        groupOptions.addAll(ClientManager.groups);
        return groupOptions;
    }

    public AbstractLeaderEntity.State getPatrolState() {
        return patrolState;
    }

    public InfoMode getInfoMode() {
        return infoMode;
    }

    public EnemyAction getEnemyAction() {
        return enemyAction;
    }

    @Nullable
    public RecruitsRoute getSelectedRoute() {
        return selectedRoute;
    }

    @Nullable
    public RecruitsGroup getSelectedGroup() {
        return selectedGroup;
    }

    public void selectRoute(@Nullable RecruitsRoute route) {
        selectedRoute = route;
        routeAssignments.sendRouteAssignment(leaderEntity.getUUID(), route);
    }

    public void startOrResumePatrol() {
        patrolState = AbstractLeaderEntity.State.PATROLLING;
        routeAssignments.sendRouteAssignment(leaderEntity.getUUID(), selectedRoute);
        BannerModMain.SIMPLE_CHANNEL.sendToServer(
                new MessagePatrolLeaderSetPatrolState(leaderEntity.getUUID(), (byte) patrolState.getIndex()));
    }

    public void stopOrPausePatrol() {
        patrolState = patrolState == AbstractLeaderEntity.State.PATROLLING
                ? AbstractLeaderEntity.State.PAUSED
                : AbstractLeaderEntity.State.STOPPED;
        BannerModMain.SIMPLE_CHANNEL.sendToServer(
                new MessagePatrolLeaderSetPatrolState(leaderEntity.getUUID(), (byte) patrolState.getIndex()));
    }

    public void cycleInfoMode() {
        infoMode = infoMode.getNext();
        BannerModMain.SIMPLE_CHANNEL.sendToServer(
                new MessagePatrolLeaderSetInfoMode(leaderEntity.getUUID(), infoMode.getIndex()));
    }

    public void cycleEnemyAction() {
        enemyAction = enemyAction.getNext();
        BannerModMain.SIMPLE_CHANNEL.sendToServer(
                new MessagePatrolLeaderSetEnemyAction(leaderEntity.getUUID(), enemyAction.getIndex()));
    }

    public void selectGroup(@Nullable RecruitsGroup group) {
        RecruitsGroup previousGroup = selectedGroup;
        selectedGroup = group;

        if (group != null) {
            BannerModMain.SIMPLE_CHANNEL.sendToServer(
                    new MessageSetLeaderGroup(leaderEntity.getUUID(), group.getUUID()));
            BannerModMain.SIMPLE_CHANNEL.sendToServer(
                    new MessageAssignGroupToCompanion(player.getUUID(), leaderEntity.getUUID()));
            group.leaderUUID = leaderEntity.getUUID();
            return;
        }

        if (previousGroup != null) {
            BannerModMain.SIMPLE_CHANNEL.sendToServer(
                    new MessageRemoveAssignedGroupFromCompanion(player.getUUID(), leaderEntity.getUUID()));
            previousGroup.leaderUUID = null;
        }
    }
}
