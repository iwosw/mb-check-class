package com.talhanation.bannermod.citizen;

import javax.annotation.Nullable;
import java.util.Objects;

/**
 * Encapsulates the "swap the currently-active profession controller"
 * state machine so the logic is unit-testable without a live {@link CitizenCore}
 * entity. {@link com.talhanation.bannermod.entity.citizen.CitizenEntity}
 * delegates {@code switchProfession} to this helper; tests provide a fake
 * {@link CitizenCore} implementation instead of a full Minecraft
 * {@code PathfinderMob}.
 *
 * <p>Thread-unsafe by design — each citizen owns one exclusive
 * switcher instance, and Minecraft entities tick on the main thread.
 */
public final class CitizenProfessionSwitcher {

    private final CitizenProfessionRegistry registry;
    private final CitizenCore citizen;
    private CitizenProfession activeProfession;
    private CitizenProfessionController activeController;
    private int switchCount;

    public CitizenProfessionSwitcher(CitizenProfessionRegistry registry,
                                     CitizenCore citizen,
                                     CitizenProfession initialProfession) {
        this.registry = Objects.requireNonNull(registry, "registry");
        this.citizen = Objects.requireNonNull(citizen, "citizen");
        this.activeProfession = Objects.requireNonNull(initialProfession, "initialProfession");
        this.activeController = this.registry.lookup(initialProfession);
        this.switchCount = 0;
    }

    public CitizenProfession activeProfession() {
        return this.activeProfession;
    }

    public CitizenProfessionController activeController() {
        return this.activeController;
    }

    public int switchCount() {
        return this.switchCount;
    }

    /**
     * Apply {@link CitizenProfessionController#installGoals(CitizenCore)} for
     * the active controller. Called once after a freshly-constructed
     * citizen has applied its snapshot and is ready to run.
     */
    public void install() {
        this.activeController.installGoals(this.citizen);
    }

    /**
     * Tear down whichever controller is currently active without selecting
     * a replacement. After {@code uninstall()} the switcher still reports
     * the profession as active but the controller's goals have been
     * removed.
     */
    public void uninstall() {
        this.activeController.uninstallGoals(this.citizen);
    }

    /**
     * Switch to the new profession at runtime. Returns {@code true} if a
     * switch actually occurred; {@code false} when {@code newProfession}
     * matches the active one (a no-op that still counts against the
     * cooldown logic callers may overlay on top of this class).
     */
    public boolean switchTo(CitizenProfession newProfession, @Nullable CitizenRoleContext context) {
        Objects.requireNonNull(newProfession, "newProfession");
        if (newProfession == this.activeProfession) {
            return false;
        }

        CitizenProfession previous = this.activeProfession;
        CitizenProfessionController previousController = this.activeController;
        CitizenProfessionController nextController = this.registry.lookup(newProfession);

        previousController.uninstallGoals(this.citizen);

        this.activeProfession = newProfession;
        this.activeController = nextController;
        this.switchCount++;

        nextController.installGoals(this.citizen);
        if (context != null) {
            nextController.onProfessionReplaced(context, previous);
        }

        return true;
    }

    /**
     * Re-resolve the controller from the registry using the current
     * profession. Useful after a registry registration happens late
     * (e.g., a test overriding the default noop).
     */
    public void refresh() {
        this.activeController = this.registry.lookup(this.activeProfession);
    }
}
