package com.talhanation.bannermod.network.catalog;

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
            CommonRegistry.registerMessage(registrar, message);
        }
    }
}
