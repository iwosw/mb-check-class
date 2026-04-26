package com.talhanation.bannermod.war.registry;

import com.talhanation.bannermod.war.runtime.WarDeclarationRuntime;
import com.talhanation.bannermod.war.runtime.WarGoalType;
import net.minecraft.core.BlockPos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class PoliticalRelationsTest {
    @Test
    void atWarUsesDeclaredOpposingSides() {
        UUID attacker = UUID.randomUUID();
        UUID defender = UUID.randomUUID();
        UUID neutral = UUID.randomUUID();
        WarDeclarationRuntime declarations = new WarDeclarationRuntime();
        declarations.declareWar(attacker, defender, WarGoalType.OCCUPATION, "test", List.of(BlockPos.ZERO), List.of(), List.of(), 1L, 0L);

        assertTrue(PoliticalRelations.atWar(declarations, attacker, defender));
        assertTrue(PoliticalRelations.atWar(declarations, defender, attacker));
        assertFalse(PoliticalRelations.atWar(declarations, attacker, neutral));
    }

    @Test
    void neutralRequiresDistinctNonWarringEntities() {
        UUID first = UUID.randomUUID();
        UUID second = UUID.randomUUID();
        WarDeclarationRuntime declarations = new WarDeclarationRuntime();

        assertTrue(PoliticalRelations.neutral(declarations, first, second));
        assertFalse(PoliticalRelations.neutral(declarations, first, first));
        assertFalse(PoliticalRelations.neutral(declarations, first, null));
    }
}
