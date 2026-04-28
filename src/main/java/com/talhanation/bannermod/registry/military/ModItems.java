package com.talhanation.bannermod.registry.military;

import com.google.common.collect.Lists;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.SpawnEggItem;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.List;

import static com.talhanation.bannermod.util.RegistryUtils.createSpawnEggItem;

public class ModItems {
    public static final DeferredRegister<Item> ITEMS = DeferredRegister.create(Registries.ITEM, BannerModMain.MOD_ID);
    public static final List<DeferredHolder<Item, SpawnEggItem>> SPAWN_EGGS = Lists.newArrayList();

    public static final DeferredHolder<Item, SpawnEggItem> RECRUIT_SPAWN_EGG = createSpawnEggItem("recruit", ModEntityTypes.RECRUIT::get, 16755200, 16777045);
    public static final DeferredHolder<Item, SpawnEggItem> BOWMAN_SPAWN_EGG = createSpawnEggItem("bowman", ModEntityTypes.BOWMAN::get, 16755200, 16777045);
    public static final DeferredHolder<Item, SpawnEggItem> NOMAD_SPAWN_EGG = createSpawnEggItem("nomad", ModEntityTypes.NOMAD::get, 16755200, 16777045);
    public static final DeferredHolder<Item, SpawnEggItem> RECRUIT_SHIELD_SPAWN_EGG = createSpawnEggItem("recruit_shieldman", ModEntityTypes.RECRUIT_SHIELDMAN::get, 16755200, 16777045);
    public static final DeferredHolder<Item, SpawnEggItem> HORSEMAN_SPAWN_EGG = createSpawnEggItem("horseman", ModEntityTypes.HORSEMAN::get, 16755200, 16777045);
    public static final DeferredHolder<Item, SpawnEggItem> CROSSBOWMAN_SPAWN_EGG = createSpawnEggItem("crossbowman", ModEntityTypes.CROSSBOWMAN::get, 16755200, 16777045);
    public static final DeferredHolder<Item, SpawnEggItem> VILLAGER_NOBLE_SPAWN_EGG = createSpawnEggItem("villager_noble", ModEntityTypes.VILLAGER_NOBLE::get, 16755200, 16777045);
    public static final DeferredHolder<Item, SpawnEggItem> MESSENGER_SPAWN_EGG = createSpawnEggItem("messenger", ModEntityTypes.MESSENGER::get, 16755200, 16777045);
    public static final DeferredHolder<Item, SpawnEggItem> SCOUT_SPAWN_EGG = createSpawnEggItem("scout", ModEntityTypes.SCOUT::get, 16755200, 16777045);
    public static final DeferredHolder<Item, SpawnEggItem> PATROL_LEADER_SPAWN_EGG = createSpawnEggItem("patrol_leader", ModEntityTypes.PATROL_LEADER::get, 16755200, 16777045);
    public static final DeferredHolder<Item, SpawnEggItem> CAPTAIN_SPAWN_EGG = createSpawnEggItem("captain", ModEntityTypes.CAPTAIN::get, 16755200, 16777045);

    public static final DeferredHolder<Item, BlockItem> RECRUIT_BLOCK = ITEMS.register("recruit_block", () -> new BlockItem(ModBlocks.RECRUIT_BLOCK.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> BOWMAN_BLOCK = ITEMS.register("bowman_block", () -> new BlockItem(ModBlocks.BOWMAN_BLOCK.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> RECRUIT_SHIELD_BLOCK = ITEMS.register("recruit_shield_block", () -> new BlockItem(ModBlocks.RECRUIT_SHIELD_BLOCK.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> CROSSBOWMAN_BLOCK = ITEMS.register("crossbowman_block", () -> new BlockItem(ModBlocks.CROSSBOWMAN_BLOCK.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> HORSEMAN_BLOCK = ITEMS.register("horseman_block", () -> new BlockItem(ModBlocks.HORSEMAN_BLOCK.get(), new Item.Properties()));
    public static final DeferredHolder<Item, BlockItem> NOMAD_BLOCK = ITEMS.register("nomad_block", () -> new BlockItem(ModBlocks.NOMAD_BLOCK.get(), new Item.Properties()));

}
