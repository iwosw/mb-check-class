package com.talhanation.bannermod.citizen;

import javax.annotation.Nullable;
import java.util.Collections;
import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;

/**
 * Central lookup from {@link CitizenProfession} → {@link CitizenProfessionController}.
 *
 * <p>Default state contains a noop stub controller for every profession so
 * {@code CitizenEntity} can safely construct at any profession during
 * boot, even before Cit-03 lands the real controllers. Real controllers
 * register themselves via {@link #register(CitizenProfessionController)}
 * during mod setup.
 *
 * <p>Registry is an instance (not static singleton) so tests can build a
 * clean one without touching global state. Production code typically
 * holds a single {@link #defaults()} instance somewhere in bootstrap and
 * hands it to every citizen.
 */
public final class CitizenProfessionRegistry {

    private final Map<CitizenProfession, CitizenProfessionController> bindings = new EnumMap<>(CitizenProfession.class);

    public CitizenProfessionRegistry() {
        for (CitizenProfession profession : CitizenProfession.values()) {
            this.bindings.put(profession, CitizenProfessionController.noop(profession));
        }
    }

    /**
     * Fresh registry with default noop controllers for every profession.
     * Identical to {@code new CitizenProfessionRegistry()} — provided as a
     * named factory for readability at the bootstrap site.
     */
    public static CitizenProfessionRegistry defaults() {
        return new CitizenProfessionRegistry();
    }

    /**
     * Install a concrete controller for its declared profession. Overwrites
     * whatever was registered previously (typically the noop stub). Returns
     * the registry for fluent setup.
     */
    public CitizenProfessionRegistry register(CitizenProfessionController controller) {
        Objects.requireNonNull(controller, "controller");
        Objects.requireNonNull(controller.profession(), "controller.profession()");
        this.bindings.put(controller.profession(), controller);
        return this;
    }

    /**
     * Look up the controller bound to {@code profession}. Returns the noop
     * stub if no real controller has been registered yet — never null.
     */
    public CitizenProfessionController lookup(CitizenProfession profession) {
        Objects.requireNonNull(profession, "profession");
        return this.bindings.getOrDefault(profession, CitizenProfessionController.noop(profession));
    }

    /**
     * Look up by the legacy or active per-profession entity id string (e.g.
     * {@code "recruits:bowman"} or {@code "bannermod:bowman"}). Returns empty when no profession matches
     * — caller should treat as a completely unknown entity id.
     */
    public Optional<CitizenProfessionController> lookupByLegacyEntityId(@Nullable String legacyEntityId) {
        if (legacyEntityId == null || legacyEntityId.isEmpty()) {
            return Optional.empty();
        }
        CitizenProfession profession = CitizenProfession.fromLegacyEntityId(legacyEntityId);
        if (profession == CitizenProfession.NONE && !"NONE".equals(legacyEntityId)) {
            return Optional.empty();
        }
        return Optional.of(this.lookup(profession));
    }

    public int size() {
        return this.bindings.size();
    }

    /** Read-only view of every current binding — useful for telemetry. */
    public Map<CitizenProfession, CitizenProfessionController> snapshot() {
        return Collections.unmodifiableMap(new EnumMap<>(this.bindings));
    }
}
