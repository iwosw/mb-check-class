package com.talhanation.bannermod.settlement.household;

/**
 * Why this resident is bound to a particular building at night.
 *
 * <p>ASSIGNED — stable, resident owns (or co-owns) the slot.
 * SHARED — resident shares a home with other residents (multi-occupancy).
 * TEMPORARY_SHELTER — fallback slot granted when no proper home was available.
 * NONE — placeholder for residents tracked but not yet bound to any slot.
 */
public enum HomePreference {
    ASSIGNED,
    SHARED,
    TEMPORARY_SHELTER,
    NONE
}
