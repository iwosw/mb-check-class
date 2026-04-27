package com.talhanation.bannermod.settlement.bootstrap;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertTrue;

class SettlementBootstrapServiceTest {
    @Test
    void starterWorkerReadinessMessageNamesReadyAndWaitingJobs() {
        String message = SettlementBootstrapService.starterWorkerReadinessMessage(4);

        assertTrue(message.contains("farmer has a starter crop area"));
        assertTrue(message.contains("miner needs a mine"));
        assertTrue(message.contains("lumberjack needs a lumber camp"));
        assertTrue(message.contains("builder needs an architect workshop/build area"));
        assertTrue(message.contains("Free citizens spawn separately"));
    }
}
