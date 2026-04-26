package com.talhanation.bannermod.war.config;

import com.talhanation.bannermod.war.cooldown.WarCooldownPolicy;
import com.talhanation.bannermod.war.runtime.BattleWindow;
import com.talhanation.bannermod.war.runtime.BattleWindowSchedule;
import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.fml.common.Mod;

import java.time.DayOfWeek;
import java.time.LocalTime;
import java.time.format.DateTimeParseException;
import java.util.ArrayList;
import java.util.List;

@Mod.EventBusSubscriber
public class WarServerConfig {
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();
    public static final ForgeConfigSpec SERVER;

    public static final ForgeConfigSpec.BooleanValue RegulatedPvpEnabled;
    public static final ForgeConfigSpec.ConfigValue<List<String>> BattleWindows;
    public static final ForgeConfigSpec.IntValue PeaceCooldownDays;
    public static final ForgeConfigSpec.IntValue DefenderDailyDeclarations;
    public static final ForgeConfigSpec.IntValue DefaultSiegeRadius;
    public static final ForgeConfigSpec.IntValue MinDeclarationDelayTicks;

    static {
        RegulatedPvpEnabled = BUILDER.comment("""
                        Master switch for the regulated warfare-RP PvP gate.
                        \tWhen false, the WarPvpGate is bypassed and damage is decided by other rules.
                        \tdefault: true""")
                .define("RegulatedPvpEnabled", true);

        BattleWindows = BUILDER.comment("""
                        Battle windows when war-PvP and siege progression activate.
                        \tFormat per entry: DAY,HH:MM,HH:MM (server local time).
                        \tDefault windows: WEDNESDAY 19:00-20:30, FRIDAY 19:00-20:30, SUNDAY 18:00-19:30.""")
                .define("BattleWindows", List.of(
                        "WEDNESDAY,19:00,20:30",
                        "FRIDAY,19:00,20:30",
                        "SUNDAY,18:00,19:30"
                ), value -> value instanceof List<?>);

        PeaceCooldownDays = BUILDER.comment("""
                        Days of forced peace between two states after a war is concluded or cancelled.
                        \tdefault: 7""")
                .defineInRange("PeaceCooldownDays", 7, 0, 365);

        DefenderDailyDeclarations = BUILDER.comment("""
                        Maximum number of offensive wars a single defender can receive per real day.
                        \tdefault: 1""")
                .defineInRange("DefenderDailyDeclarations", 1, 1, 100);

        DefaultSiegeRadius = BUILDER.comment("""
                        Default radius (blocks) of a Siege Standard war zone.
                        \tdefault: 64""")
                .defineInRange("DefaultSiegeRadius", 64, 8, 512);

        MinDeclarationDelayTicks = BUILDER.comment("""
                        Minimum delay (ticks) between declaration and earliest activation.
                        \tdefault: 6000 (5 minutes at 20 TPS)""")
                .defineInRange("MinDeclarationDelayTicks", 6000, 0, Integer.MAX_VALUE);

        SERVER = BUILDER.build();
    }

    public static long peaceCooldownTicks() {
        return Math.max(0L, PeaceCooldownDays.get().longValue()) * WarCooldownPolicy.TICKS_PER_DAY;
    }

    public static BattleWindowSchedule resolveSchedule() {
        List<BattleWindow> windows = new ArrayList<>();
        for (String entry : BattleWindows.get()) {
            BattleWindow window = parseWindow(entry);
            if (window != null) {
                windows.add(window);
            }
        }
        if (windows.isEmpty()) {
            return BattleWindowSchedule.defaultSchedule();
        }
        return new BattleWindowSchedule(windows);
    }

    private static BattleWindow parseWindow(String entry) {
        if (entry == null) {
            return null;
        }
        String[] parts = entry.split(",");
        if (parts.length != 3) {
            return null;
        }
        try {
            DayOfWeek day = DayOfWeek.valueOf(parts[0].trim().toUpperCase(java.util.Locale.ROOT));
            LocalTime start = LocalTime.parse(parts[1].trim());
            LocalTime end = LocalTime.parse(parts[2].trim());
            return new BattleWindow(day, start, end);
        } catch (IllegalArgumentException | DateTimeParseException ex) {
            return null;
        }
    }
}
