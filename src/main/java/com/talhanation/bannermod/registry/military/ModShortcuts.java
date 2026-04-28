package com.talhanation.bannermod.registry.military;

import net.minecraft.client.KeyMapping;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RegisterKeyMappingsEvent;
import net.neoforged.bus.api.SubscribeEvent;
import org.lwjgl.glfw.GLFW;

public class ModShortcuts {
    public static KeyMapping COMMAND_SCREEN_KEY;
    public static KeyMapping WAR_ROOM_KEY;
    public static KeyMapping MAP_SCREEN_KEY;

    @SubscribeEvent
    @OnlyIn(Dist.CLIENT)
    public static void registerBindings(RegisterKeyMappingsEvent event) {
        COMMAND_SCREEN_KEY = new KeyMapping("key.bannermod.command_screen_key", GLFW.GLFW_KEY_R, "category.bannermod");
        WAR_ROOM_KEY =  new KeyMapping("key.bannermod.war_room_key", GLFW.GLFW_KEY_U, "category.bannermod");
        MAP_SCREEN_KEY =  new KeyMapping("key.bannermod.map_screen_key", GLFW.GLFW_KEY_M, "category.bannermod");

        event.register(COMMAND_SCREEN_KEY);
        event.register(WAR_ROOM_KEY);
        event.register(MAP_SCREEN_KEY);
    }
}
