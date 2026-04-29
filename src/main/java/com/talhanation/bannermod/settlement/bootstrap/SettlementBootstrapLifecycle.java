package com.talhanation.bannermod.settlement.bootstrap;

/**
 * Canonical settlement bootstrap lifecycle shared by manual founding and automatic claim seeding.
 *
 * <p>Manual founding is the authoritative path for creating a formal settlement: a validated
 * STARTER_FORT establishes or reuses the claim, writes a {@link SettlementRecord}, spawns the
 * starter worker set, and seeds free citizens for later profession vacancies. Manual validated
 * buildings after the fort must resolve an existing settlement record before they register
 * building records or vacancies.</p>
 *
 * <p>Automatic claim bootstrap is a compatibility/growth path for eligible existing claims. It
 * places a starter center when needed, then delegates to {@link SettlementBootstrapService} so the
 * formal record, starter workers, and free citizens match manual founding.</p>
 */
public enum SettlementBootstrapLifecycle {
    /** Claim ownership and authority position are resolved before any settlement state is written. */
    CLAIM_AUTHORITY,

    /** Starter structure exists or is placed: manual STARTER_FORT validation, or auto starter center. */
    STARTER_STRUCTURE,

    /** Formal persisted settlement identity exists in {@link SettlementRegistryData}. */
    FORMAL_SETTLEMENT_RECORD,

    /** Starter workers and unassigned citizens are seeded according to the bootstrap mode. */
    STARTER_POPULATION,

    /** Later validated buildings create profession vacancies for available free citizens. */
    PROFESSION_VACANCIES
}
