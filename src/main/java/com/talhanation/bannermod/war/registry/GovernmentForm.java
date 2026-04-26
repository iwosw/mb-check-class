package com.talhanation.bannermod.war.registry;

/**
 * Government form attached to every {@link PoliticalEntityRecord}.
 *
 * <p>POL-001 introduces only two forms — {@link #MONARCHY} and {@link #REPUBLIC} — to keep
 * the contract minimal. Authority delegation is captured here as a single boolean flag
 * (whether co-leaders may act as the leader); higher-level rules read the form rather than
 * branching on the enum value directly.</p>
 */
public enum GovernmentForm {
    MONARCHY(false),
    REPUBLIC(true);

    private final boolean coLeadersShareAuthority;

    GovernmentForm(boolean coLeadersShareAuthority) {
        this.coLeadersShareAuthority = coLeadersShareAuthority;
    }

    public boolean coLeadersShareAuthority() {
        return this.coLeadersShareAuthority;
    }

    /** Safe enum parser used by the persistence layer. Falls back to {@link #MONARCHY}. */
    public static GovernmentForm fromTagName(String name) {
        if (name == null || name.isBlank()) {
            return MONARCHY;
        }
        try {
            return GovernmentForm.valueOf(name);
        } catch (IllegalArgumentException exception) {
            return MONARCHY;
        }
    }
}
