package com.talhanation.workers;

import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.network.simple.SimpleChannel;
import org.apache.logging.log4j.Logger;

public class WorkersMain {
    public static final String MOD_ID = WorkersRuntime.MOD_ID;
    public static final Logger LOGGER = WorkersRuntime.logger();
    @Deprecated(forRemoval = false)
    public static SimpleChannel SIMPLE_CHANNEL;
    private final WorkersSubsystem subsystem;

    public WorkersMain() {
        this(new WorkersSubsystem());
    }

    public WorkersMain(WorkersSubsystem subsystem) {
        this.subsystem = subsystem;
    }

    public void registerCommon(net.minecraftforge.eventbus.api.IEventBus modEventBus) {
        subsystem.registerCommon(modEventBus);
    }

    public void registerRuntimeListeners() {
        subsystem.registerRuntimeListeners();
    }

    public void registerNetwork(SimpleChannel channel) {
        SIMPLE_CHANNEL = channel;
        subsystem.registerNetwork(channel);
    }

    public int networkMessageCount() {
        return subsystem.networkMessageCount();
    }

    @OnlyIn(Dist.CLIENT)
    public void clientSetup(FMLClientSetupEvent event) {
        subsystem.clientSetup(event);
    }

    public void registerClientRuntimeListeners() {
        subsystem.registerClientRuntimeListeners();
    }

    public void addCreativeTabs(BuildCreativeModeTabContentsEvent event) {
        subsystem.addCreativeTabs(event);
    }
}
