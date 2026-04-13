package com.talhanation.bannermod.governance;

import com.talhanation.bannermod.logistics.BannerModSupplyStatus;
import com.talhanation.bannermod.settlement.BannerModSettlementBinding;

import java.util.ArrayList;
import java.util.EnumSet;
import java.util.List;

public final class BannerModGovernorHeartbeat {

    private BannerModGovernorHeartbeat() {
    }

    public static HeartbeatReport evaluate(HeartbeatInput input) {
        EnumSet<BannerModGovernorIncident> incidents = EnumSet.noneOf(BannerModGovernorIncident.class);
        EnumSet<BannerModGovernorRecommendation> recommendations = EnumSet.noneOf(BannerModGovernorRecommendation.class);

        int citizens = Math.max(0, input.villagerCount()) + Math.max(0, input.workerCount());
        int taxesDue = 0;
        int taxesCollected = 0;

        switch (input.settlementStatus()) {
            case HOSTILE_CLAIM -> incidents.add(BannerModGovernorIncident.HOSTILE_CLAIM);
            case DEGRADED_MISMATCH -> incidents.add(BannerModGovernorIncident.DEGRADED_SETTLEMENT);
            case UNCLAIMED -> incidents.add(BannerModGovernorIncident.DEGRADED_SETTLEMENT);
            case FRIENDLY_CLAIM -> {
                taxesDue = citizens * 2;
                taxesCollected = taxesDue;
            }
        }

        if (input.underSiege()) {
            incidents.add(BannerModGovernorIncident.UNDER_SIEGE);
            taxesCollected = 0;
        }

        if (input.settlementStatus() != BannerModSettlementBinding.Status.FRIENDLY_CLAIM) {
            taxesCollected = 0;
        }

        if (input.workerCount() <= 0) {
            incidents.add(BannerModGovernorIncident.WORKER_SHORTAGE);
        }

        if (input.recruitCount() < Math.max(1, citizens / 2)) {
            recommendations.add(BannerModGovernorRecommendation.INCREASE_GARRISON);
            recommendations.add(BannerModGovernorRecommendation.STRENGTHEN_FORTIFICATIONS);
        }

        if (input.workerSupplyStatus() != null && input.workerSupplyStatus().blocked()) {
            incidents.add(BannerModGovernorIncident.SUPPLY_BLOCKED);
            recommendations.add(BannerModGovernorRecommendation.RELIEVE_SUPPLY_PRESSURE);
        }

        if (input.recruitSupplyStatus() != null && input.recruitSupplyStatus().blocked()) {
            incidents.add(BannerModGovernorIncident.RECRUIT_UPKEEP_BLOCKED);
            recommendations.add(BannerModGovernorRecommendation.RELIEVE_SUPPLY_PRESSURE);
        }

        if (recommendations.isEmpty()) {
            recommendations.add(BannerModGovernorRecommendation.HOLD_COURSE);
        }

        return new HeartbeatReport(
                citizens,
                taxesDue,
                taxesCollected,
                List.copyOf(incidents),
                List.copyOf(recommendations),
                input.gameTime(),
                taxesCollected > 0 ? input.gameTime() : input.snapshot().lastCollectionTick()
        );
    }

    public static List<String> incidentTokens(List<BannerModGovernorIncident> incidents) {
        List<String> tokens = new ArrayList<>();
        for (BannerModGovernorIncident incident : incidents) {
            tokens.add(incident.name().toLowerCase());
        }
        return tokens;
    }

    public static List<String> recommendationTokens(List<BannerModGovernorRecommendation> recommendations) {
        List<String> tokens = new ArrayList<>();
        for (BannerModGovernorRecommendation recommendation : recommendations) {
            tokens.add(recommendation.name().toLowerCase());
        }
        return tokens;
    }

    public record HeartbeatInput(BannerModSettlementBinding.Status settlementStatus,
                                 boolean underSiege,
                                 int villagerCount,
                                 int workerCount,
                                 int recruitCount,
                                 BannerModSupplyStatus.WorkerSupplyStatus workerSupplyStatus,
                                 BannerModSupplyStatus.RecruitSupplyStatus recruitSupplyStatus,
                                 long gameTime,
                                 long previousHeartbeatTick,
                                 BannerModGovernorSnapshot snapshot) {
    }

    public record HeartbeatReport(int citizenCount,
                                  int taxesDue,
                                  int taxesCollected,
                                  List<BannerModGovernorIncident> incidents,
                                  List<BannerModGovernorRecommendation> recommendations,
                                  long heartbeatTick,
                                  long collectionTick) {
    }
}
