package com.talhanation.bannermod.war.registry;

import com.talhanation.bannermod.war.runtime.WarDeclarationRecord;
import com.talhanation.bannermod.war.runtime.WarDeclarationRuntime;

import java.util.UUID;

public final class PoliticalRelations {
    private PoliticalRelations() {
    }

    public static boolean atWar(WarDeclarationRuntime declarations, UUID a, UUID b) {
        if (declarations == null || a == null || b == null || a.equals(b)) {
            return false;
        }
        for (WarDeclarationRecord war : declarations.activeOrDeclared()) {
            if (war.opposingSides(a, b)) {
                return true;
            }
        }
        return false;
    }

    public static boolean ally(PoliticalRegistryRuntime registry, UUID a, UUID b) {
        return registry != null && a != null && a.equals(b) && registry.byId(a).isPresent();
    }

    public static boolean neutral(WarDeclarationRuntime declarations, UUID a, UUID b) {
        return a != null && b != null && !a.equals(b) && !atWar(declarations, a, b);
    }
}
