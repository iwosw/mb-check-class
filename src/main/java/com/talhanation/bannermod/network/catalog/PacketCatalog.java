package com.talhanation.bannermod.network.catalog;

import de.maxhenkel.corelib.CommonRegistry;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public final class PacketCatalog {
    private final String name;
    private final Class<?>[] messages;

    public PacketCatalog(String name, Class<?>[] messages) {
        this.name = name;
        this.messages = messages;
    }

    public String name() {
        return name;
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
