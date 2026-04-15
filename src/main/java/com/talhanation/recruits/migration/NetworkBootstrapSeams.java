package com.talhanation.recruits.migration;

import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.network.simple.SimpleChannel;

import java.util.List;

/**
 * Contract-only networking and bootstrap seams for Phase 7 migration prep.
 *
 * <p>Source anchors are documented in
 * {@code .planning/phases/07-migration-ready-internal-seams/07-SEAM-INVENTORY.md} and currently
 * live primarily in {@code Main.setup(...)} where channel creation, packet ordering, and lifecycle
 * registration are still inlined.</p>
 */
public final class NetworkBootstrapSeams {

    private NetworkBootstrapSeams() {
    }

    /**
     * Ordered wire-registration contract extracted from the inline packet array in {@code Main}.
     */
    public record MessageRegistration(int id, Class<?> messageClass) {
    }

    /**
     * Supplies the canonical ordered packet registrations that must retain stable ids.
     */
    public interface OrderedMessageCatalog {
        List<MessageRegistration> orderedMessageTypes();
    }

    /**
     * Applies ordered packet registrations to an already-created channel.
     */
    public interface ChannelRegistrar {
        void registerAll(SimpleChannel channel);
    }

    /**
     * Owns common lifecycle and deferred-register binding currently assembled directly in {@code Main}.
     */
    public interface LifecycleBinder {
        void registerCommon(IEventBus modEventBus);
    }
}
