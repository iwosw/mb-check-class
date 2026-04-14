package com.talhanation.bannerlord.network;

import com.talhanation.bannerlord.bootstrap.BannerlordMain;
import com.talhanation.bannerlord.network.civilian.WorkersNetworkRegistrar;
import com.talhanation.bannerlord.network.military.RecruitsNetworkRegistrar;
import com.talhanation.bannerlord.compat.workers.WorkersRuntime;
import de.maxhenkel.corelib.CommonRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class BannerlordNetworkBootstrap {
    private final RecruitsNetworkRegistrar recruitsNetworkRegistrar;
    private final WorkersNetworkRegistrar workersNetworkRegistrar;

    public BannerlordNetworkBootstrap() {
        this.recruitsNetworkRegistrar = new RecruitsNetworkRegistrar();
        this.workersNetworkRegistrar = new WorkersNetworkRegistrar();
    }

    public SimpleChannel createSharedChannel() {
        SimpleChannel channel = CommonRegistry.registerChannel(BannerlordMain.MOD_ID, "default");
        recruitsNetworkRegistrar.registerAll(channel);
        workersNetworkRegistrar.registerAll(channel, workerPacketOffset());
        WorkersRuntime.bindChannel(channel);
        return channel;
    }

    public int workerPacketOffset() {
        return recruitsNetworkRegistrar.messageCount();
    }
}
