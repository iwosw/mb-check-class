package com.talhanation.bannermod.citizen;

/**
 * Evolves {@link CitizenRoleController} with profession-scoped hooks. One
 * controller instance owns the behavior, goal set, loadout hints, and
 * inventory size for a single {@link CitizenProfession}. Controllers are
 * registered in {@link CitizenProfessionRegistry} and swapped in and out
 * of a {@code CitizenEntity} at runtime when its profession changes.
 *
 * <p>All hooks are optional defaults so early controllers can be stubs.
 * Real profession controllers (Cit-03) override the install/uninstall
 * pair to register/unregister Minecraft goals through a narrow adapter
 * on {@link CitizenCore}.
 */
public interface CitizenProfessionController extends CitizenRoleController {

    CitizenProfession profession();

    /**
     * Back-compat: the coarse-category role this profession belongs to.
     * Defaults to {@link CitizenProfession#coarseRole()} so implementers
     * don't restate the mapping.
     */
    @Override
    default CitizenRole role() {
        return this.profession().coarseRole();
    }

    /**
     * Invoked when the controller becomes active on a citizen — either
     * because the citizen just spawned or because {@code switchProfession}
     * selected this controller. Default: no-op. Concrete implementations
     * register goals, apply attribute modifiers, and initialise inventory.
     */
    default void installGoals(CitizenCore citizen) {
    }

    /**
     * Invoked when the controller is being removed — either because the
     * citizen is despawning or because {@code switchProfession} is moving
     * to a different profession. Default: no-op. Concrete implementations
     * unregister previously-installed goals and remove attribute modifiers.
     */
    default void uninstallGoals(CitizenCore citizen) {
    }

    /**
     * Called after the citizen has finished its initial spawn and any
     * pending snapshot apply, signalling the controller that it's safe to
     * begin behavior.
     */
    default void onProfessionAssigned(CitizenRoleContext context) {
    }

    /**
     * Called during {@code switchProfession} after {@link #uninstallGoals}
     * has run on the old controller and {@link #installGoals} on this one,
     * giving the new controller a chance to inspect the previous profession
     * for carry-over state (e.g., half-finished stockpile delivery).
     */
    default void onProfessionReplaced(CitizenRoleContext context, CitizenProfession previousProfession) {
    }

    /**
     * How many inventory slots this profession wants. Used by
     * {@code CitizenEntity} to size its {@link net.minecraft.world.SimpleContainer}.
     * Default 27 (generic chest-like).
     */
    default int preferredInventorySize() {
        return 27;
    }

    /**
     * Build a noop controller bound to the given profession. Used as a
     * registry default before the real profession controller lands.
     */
    static CitizenProfessionController noop(CitizenProfession profession) {
        return () -> profession;
    }
}
