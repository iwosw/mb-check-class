package com.talhanation.bannermod.network.catalog;

import com.talhanation.bannermod.network.payload.BannerModMessageSides;
import de.maxhenkel.corelib.CommonRegistry;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class PacketCatalog {
    private final Class<?>[] messages;

    public PacketCatalog(Class<?>[] messages) {
        this.messages = messages;
    }

    public Class<?>[] messages() {
        return messages;
    }

    @SuppressWarnings({"unchecked", "rawtypes"})
    public void register(PayloadRegistrar registrar) {
        for (Class message : messages) {
            // Record declared sides (and fail bootstrap if the class declares
            // neither executeServerSide nor executeClientSide). Performed before
            // delegation so we never hand a malformed packet to NeoForge.
            BannerModMessageSides.register(message);
            CommonRegistry.registerMessage(registrar, message);
        }
    }
}
