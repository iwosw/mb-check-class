package com.talhanation.recruits.world;

import com.talhanation.recruits.config.RecruitsServerConfig;
import com.talhanation.recruits.init.ModEntityTypes;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;

import javax.annotation.Nullable;
import java.util.*;
public class RecruitsHireTradesRegistry {
    public static int totalTraderTypes = 2;// 0 inclusive
    private static final Component TITLE_RECRUIT = Component.translatable("description.bannermod.title.recruit");
    private static final Component TITLE_SHIELDMAN = Component.translatable("description.bannermod.title.shieldman");
    private static final Component TITLE_BOWMAN = Component.translatable("description.bannermod.title.bowman");
    private static final Component TITLE_CROSSBOWMAN = Component.translatable("description.bannermod.title.crossbowman");
    private static final Component TITLE_NOMAD = Component.translatable("description.bannermod.title.nomad");
    private static final Component TITLE_HORSEMAN = Component.translatable("description.bannermod.title.horseman");
    private static final Component TITLE_MESSENGER = Component.translatable("description.bannermod.title.messenger");
    private static final Component TITLE_SCOUT = Component.translatable("description.bannermod.title.scout");
    private static final Component TITLE_CAPTAIN = Component.translatable("description.bannermod.title.captain");
    private static final Component TITLE_PATROL_LEADER = Component.translatable("description.bannermod.title.commander");

    private static final Component DESCRIPTION_RECRUIT = Component.translatable("description.bannermod.recruit");
    private static final Component DESCRIPTION_SHIELDMAN = Component.translatable("description.bannermod.shieldman");
    private static final Component DESCRIPTION_BOWMAN = Component.translatable("description.bannermod.bowman");
    private static final Component DESCRIPTION_CROSSBOWMAN = Component.translatable("description.bannermod.crossbowman");
    private static final Component DESCRIPTION_NOMAD = Component.translatable("description.bannermod.nomad");
    private static final Component DESCRIPTION_HORSEMAN = Component.translatable("description.bannermod.horseman");
    private static final Component DESCRIPTION_MESSENGER = Component.translatable("description.bannermod.messenger");
    private static final Component DESCRIPTION_SCOUT = Component.translatable("description.bannermod.scout");
    private static final Component DESCRIPTION_CAPTAIN = Component.translatable("description.bannermod.captain");
    private static final Component DESCRIPTION_PATROL_LEADER = Component.translatable("description.bannermod.commander");



    public static RecruitsHireTrade RECRUIT = new RecruitsHireTrade(ModEntityTypes.RECRUIT.getId(), RecruitsServerConfig.RecruitCost.get(), TITLE_RECRUIT, DESCRIPTION_RECRUIT);
    public static RecruitsHireTrade SHIELDMAN = new RecruitsHireTrade(ModEntityTypes.RECRUIT_SHIELDMAN.getId(), RecruitsServerConfig.ShieldmanCost.get(), TITLE_SHIELDMAN, DESCRIPTION_SHIELDMAN);
    public static RecruitsHireTrade BOWMAN = new RecruitsHireTrade(ModEntityTypes.BOWMAN.getId(), RecruitsServerConfig.BowmanCost.get(), TITLE_BOWMAN, DESCRIPTION_BOWMAN);
    public static RecruitsHireTrade CROSSBOWMAN = new RecruitsHireTrade(ModEntityTypes.CROSSBOWMAN.getId(), RecruitsServerConfig.CrossbowmanCost.get(), TITLE_CROSSBOWMAN, DESCRIPTION_CROSSBOWMAN);

    public static RecruitsHireTrade NOMAD = new RecruitsHireTrade(ModEntityTypes.NOMAD.getId(), RecruitsServerConfig.NomadCost.get(), TITLE_NOMAD, DESCRIPTION_NOMAD);
    public static RecruitsHireTrade HORSEMAN = new RecruitsHireTrade(ModEntityTypes.HORSEMAN.getId(), RecruitsServerConfig.HorsemanCost.get(), TITLE_HORSEMAN, DESCRIPTION_HORSEMAN);

    public static RecruitsHireTrade MESSENGER = new RecruitsHireTrade(ModEntityTypes.MESSENGER.getId(),32, TITLE_MESSENGER, DESCRIPTION_MESSENGER);
    public static RecruitsHireTrade SCOUT = new RecruitsHireTrade(ModEntityTypes.SCOUT.getId(), 32, TITLE_SCOUT, DESCRIPTION_SCOUT);

    public static RecruitsHireTrade CAPTAIN = new RecruitsHireTrade(ModEntityTypes.CAPTAIN.getId(), 64, TITLE_CAPTAIN, DESCRIPTION_CAPTAIN);
    public static RecruitsHireTrade PATROL_LEADER = new RecruitsHireTrade(ModEntityTypes.PATROL_LEADER.getId(), 64, TITLE_PATROL_LEADER, DESCRIPTION_PATROL_LEADER);

    private static final Map<String, Map<Integer, List<RecruitsHireTrade>>> TRADES = new HashMap<>();
    public static void registerTrades() {
        // === Trader Type 0 ===
        addTrade("infantry", 1, RECRUIT, SHIELDMAN);
        addTrade("infantry", 2, HORSEMAN);
        addTrade("infantry", 3, CAPTAIN, PATROL_LEADER);

        // === Trader Type 1 ===
        addTrade("ranged", 1, RECRUIT, BOWMAN);
        addTrade("ranged", 2, NOMAD);
        addTrade("ranged", 3, MESSENGER, SCOUT);

        // === Trader Type 2 ===
        addTrade("ranged2", 1, RECRUIT, CROSSBOWMAN);
        addTrade("ranged2", 2, SHIELDMAN);
        addTrade("ranged2", 3, MESSENGER, SCOUT);

        /*
        // === Trader Type 3 (Workers Addon) ===
        addTrade("workers", 1, "miner", "lumberjack");
        addTrade("workers", 2, "builder");
        addTrade("workers", 3, "recruit");

        // === Trader Type 4 (Workers Addon) ===
        addTrade("workers2", 1, "farmer", "lumberjack");
        addTrade("workers2", 2, "miner");
        addTrade("workers2", 3, "recruit");
        */
    }

    public static void addTrade(String traderType, int level, RecruitsHireTrade... units) {
        TRADES.computeIfAbsent(traderType, k -> new HashMap<>())
                .put(level, Arrays.asList(units));
    }

    public static List<RecruitsHireTrade> getTrades(String traderType, int level) {
        return TRADES.getOrDefault(traderType, Collections.emptyMap())
                .getOrDefault(level, Collections.emptyList());
    }

    public static List<String> getAllTraderTypes() {
        List<String> list = new ArrayList<>(TRADES.keySet());
        Collections.sort(list);
        return list;
    }

    @Nullable
    public static RecruitsHireTrade getByResourceLocation(ResourceLocation resourceLocation) {
        for (Map<Integer, List<RecruitsHireTrade>> levelMap : TRADES.values()) {
            for (List<RecruitsHireTrade> tradeList : levelMap.values()) {
                for (RecruitsHireTrade trade : tradeList) {
                    if (trade != null && resourceLocation.equals(trade.resourceLocation)) {
                        return trade;
                    }
                }
            }
        }
        return null;
    }
}

