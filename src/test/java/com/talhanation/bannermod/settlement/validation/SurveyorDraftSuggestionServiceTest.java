package com.talhanation.bannermod.settlement.validation;

import com.talhanation.bannermod.settlement.building.ZoneRole;
import com.talhanation.bannermod.settlement.building.ZoneSelection;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SurveyorDraftSuggestionServiceTest {
    @Test
    void houseSuggestionDraftsSleepingAndInteriorFromBeds() {
        TestBlockAccess level = new TestBlockAccess();
        level.put(new BlockPos(2, 64, 2), "block.minecraft.red_bed");
        level.put(new BlockPos(3, 64, 2), "block.minecraft.red_bed");

        ValidationSession session = new ValidationSession(UUID.randomUUID(), SurveyorMode.HOUSE, new BlockPos(0, 64, 0), List.of(), true);

        SurveyorDraftSuggestionService.DraftSuggestionResult result = SurveyorDraftSuggestionService.suggest(session, level);

        assertEquals(SurveyorDraftSuggestionService.Status.APPLIED, result.status());
        assertNotNull(selection(result.session(), ZoneRole.SLEEPING));
        assertNotNull(selection(result.session(), ZoneRole.INTERIOR));
    }

    @Test
    void farmSuggestionUsesFarmlandAndWaterCluster() {
        TestBlockAccess level = new TestBlockAccess();
        level.put(new BlockPos(1, 64, 1), "block.minecraft.farmland");
        level.put(new BlockPos(2, 64, 1), "block.minecraft.farmland");
        level.put(new BlockPos(1, 64, 2), "block.minecraft.water");

        ValidationSession session = new ValidationSession(UUID.randomUUID(), SurveyorMode.FARM, new BlockPos(0, 64, 0), List.of(), true);

        SurveyorDraftSuggestionService.DraftSuggestionResult result = SurveyorDraftSuggestionService.suggest(session, level);

        assertEquals(SurveyorDraftSuggestionService.Status.APPLIED, result.status());
        ZoneSelection workZone = selection(result.session(), ZoneRole.WORK_ZONE);
        assertNotNull(workZone);
        assertTrue(workZone.contains(new BlockPos(1, 64, 1)));
        assertTrue(workZone.contains(new BlockPos(1, 64, 2)));
    }

    @Test
    void suggestionKeepsExistingManualRoleMarks() {
        TestBlockAccess level = new TestBlockAccess();
        level.put(new BlockPos(2, 64, 2), "block.minecraft.red_bed");

        ZoneSelection manualSleeping = new ZoneSelection(ZoneRole.SLEEPING, new BlockPos(10, 64, 10), new BlockPos(12, 65, 12), new BlockPos(11, 64, 11));
        ValidationSession session = new ValidationSession(UUID.randomUUID(), SurveyorMode.HOUSE, new BlockPos(0, 64, 0), List.of(manualSleeping), true);

        SurveyorDraftSuggestionService.DraftSuggestionResult result = SurveyorDraftSuggestionService.suggest(session, level);

        assertEquals(SurveyorDraftSuggestionService.Status.APPLIED, result.status());
        ZoneSelection sleeping = selection(result.session(), ZoneRole.SLEEPING);
        assertNotNull(sleeping);
        assertEquals(manualSleeping.min(), sleeping.min());
        assertEquals(manualSleeping.max(), sleeping.max());
        assertNotNull(selection(result.session(), ZoneRole.INTERIOR));
    }

    @Test
    void suggestionReportsNoMatchesWhenHeuristicsMiss() {
        ValidationSession session = new ValidationSession(UUID.randomUUID(), SurveyorMode.STORAGE, new BlockPos(0, 64, 0), List.of(), true);

        SurveyorDraftSuggestionService.DraftSuggestionResult result = SurveyorDraftSuggestionService.suggest(session, new TestBlockAccess());

        assertEquals(SurveyorDraftSuggestionService.Status.NO_MATCHES, result.status());
        assertNull(selection(result.session(), ZoneRole.STORAGE));
    }

    private static ZoneSelection selection(ValidationSession session, ZoneRole role) {
        return session.selections().stream().filter(selection -> selection.role() == role).findFirst().orElse(null);
    }

    private static final class TestBlockAccess implements SurveyorDraftSuggestionService.SampledBlockAccess {
        private final Map<BlockPos, String> blocks = new HashMap<>();

        private void put(BlockPos pos, String blockId) {
            this.blocks.put(pos, blockId);
        }

        @Override
        public String blockId(BlockPos pos) {
            return this.blocks.getOrDefault(pos, "block.minecraft.air");
        }

        @Override
        public boolean isAir(BlockPos pos) {
            return "block.minecraft.air".equals(blockId(pos));
        }
    }
}
