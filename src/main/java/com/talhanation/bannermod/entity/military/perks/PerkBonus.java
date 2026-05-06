package com.talhanation.bannermod.entity.military.perks;

import java.util.Objects;

/**
 * Single (stat, amount) tuple carried by a {@link PerkNode}. Combat hooks added in
 * SKILLTREE-003/004 read these and translate them into attribute modifiers or
 * damage / accuracy multipliers; phase 1 only stores them.
 */
public record PerkBonus(PerkStat stat, double amount) {
    public PerkBonus {
        Objects.requireNonNull(stat, "stat");
    }
}
