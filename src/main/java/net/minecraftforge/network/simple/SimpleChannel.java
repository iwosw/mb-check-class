package net.minecraftforge.network.simple;

import de.maxhenkel.corelib.net.Message;
import net.minecraftforge.network.PacketDistributor;

public final class SimpleChannel {
    public void sendToServer(Message<?> message) {
        net.neoforged.neoforge.network.PacketDistributor.sendToServer(message);
    }

    public void send(PacketDistributor.Target target, Message<?> message) {
        target.send(message);
    }
}
