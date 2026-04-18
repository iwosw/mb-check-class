package com.talhanation.bannermod.events.runtime;

import com.talhanation.bannermod.events.FactionEvents;
import com.talhanation.bannermod.persistence.military.RecruitsDiplomacyManager;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.scores.Team;

final class RecruitDiplomacyPolicy {
    private static final double DAMAGE_THRESHOLD_PERCENTAGE = 0.75;

    private RecruitDiplomacyPolicy() {
    }

    static boolean isAlly(Team team1, Team team2) {
        if (team1 == null || team2 == null || FactionEvents.recruitsDiplomacyManager == null) return false;
        return FactionEvents.recruitsDiplomacyManager.getRelation(team1.getName(), team2.getName()) == RecruitsDiplomacyManager.DiplomacyStatus.ALLY;
    }

    static boolean isEnemy(Team team1, Team team2) {
        if (team1 == null || team2 == null || FactionEvents.recruitsDiplomacyManager == null) return false;
        return FactionEvents.recruitsDiplomacyManager.getRelation(team1.getName(), team2.getName()) == RecruitsDiplomacyManager.DiplomacyStatus.ENEMY;
    }

    static boolean isNeutral(Team team1, Team team2) {
        if (team1 == null || team2 == null || FactionEvents.recruitsDiplomacyManager == null) return true;
        return FactionEvents.recruitsDiplomacyManager.getRelation(team1.getName(), team2.getName()) == RecruitsDiplomacyManager.DiplomacyStatus.NEUTRAL;
    }

    static boolean canHarmTeam(LivingEntity attacker, LivingEntity target) {
        Team attackerTeam = attacker.getTeam();
        Team targetTeam = target.getTeam();
        if (attackerTeam == null || targetTeam == null) return true;
        if (attackerTeam.equals(targetTeam) && !attackerTeam.isAllowFriendlyFire()) return false;
        if (isAlly(attackerTeam, targetTeam)) return false;
        if (FactionEvents.recruitsTreatyManager != null && FactionEvents.recruitsTreatyManager.hasTreaty(attackerTeam.getName(), targetTeam.getName())) return false;
        return true;
    }

    static boolean canHarmTeamNoFriendlyFire(LivingEntity attacker, LivingEntity target) {
        Team team = attacker.getTeam();
        Team team1 = target.getTeam();
        if (team == null || team1 == null) return true;
        if (team == team1) return false;
        RecruitsDiplomacyManager.DiplomacyStatus relation = FactionEvents.recruitsDiplomacyManager.getRelation(team.getName(), team1.getName());
        if (relation == RecruitsDiplomacyManager.DiplomacyStatus.ALLY) return false;
        return FactionEvents.recruitsTreatyManager == null || !FactionEvents.recruitsTreatyManager.hasTreaty(team.getName(), team1.getName());
    }

    static void handleSignificantDamage(LivingEntity attacker, LivingEntity target, double damage, ServerLevel level) {
        Team attackerTeam = attacker.getTeam();
        Team targetTeam = target.getTeam();
        if (attackerTeam == null || targetTeam == null) return;
        if (target.getHealth() - damage < target.getMaxHealth() * DAMAGE_THRESHOLD_PERCENTAGE) {
            setTeamsAsEnemies(attackerTeam, targetTeam, level);
        }
    }

    static void setTeamsAsEnemies(Team attackerTeam, Team targetTeam, ServerLevel level) {
        String attackerTeamName = attackerTeam.getName();
        String targetTeamName = targetTeam.getName();
        if (FactionEvents.recruitsDiplomacyManager != null) {
            FactionEvents.recruitsDiplomacyManager.setRelation(attackerTeamName, targetTeamName, RecruitsDiplomacyManager.DiplomacyStatus.ENEMY, level);
            FactionEvents.recruitsDiplomacyManager.setRelation(targetTeamName, attackerTeamName, RecruitsDiplomacyManager.DiplomacyStatus.ENEMY, level);
        }
    }
}
