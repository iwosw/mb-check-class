package com.talhanation.bannermod.war.registry;

import java.util.UUID;

public final class PoliticalMembership {
    private PoliticalMembership() {
    }

    public static UUID entityIdFor(PoliticalRegistryRuntime registry, UUID playerUuid) {
        if (registry == null || playerUuid == null) {
            return null;
        }
        for (PoliticalEntityRecord record : registry.all()) {
            if (playerUuid.equals(record.leaderUuid()) || record.coLeaderUuids().contains(playerUuid)) {
                return record.id();
            }
        }
        return null;
    }
}
