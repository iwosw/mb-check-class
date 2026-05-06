package com.talhanation.bannermod.persistence;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.war.audit.WarAuditEntry;
import com.talhanation.bannermod.war.audit.WarAuditLogSavedData;
import com.talhanation.bannermod.war.cooldown.WarCooldownKind;
import com.talhanation.bannermod.war.cooldown.WarCooldownRuntime;
import com.talhanation.bannermod.war.cooldown.WarCooldownSavedData;
import net.minecraft.core.HolderLookup;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.server.level.ServerLevel;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.List;
import java.util.UUID;

/**
 * DATAVERSION-001 acceptance gametest — drives the
 * {@link com.talhanation.bannermod.persistence.SavedDataVersioning} machinery
 * inside a real {@link ServerLevel} so the {@code load()} entry points exercised
 * by {@code DimensionDataStorage} are guaranteed to handle a legacy
 * (no-{@code DataVersion}) {@link CompoundTag} cleanly.
 *
 * <p>Logical contract: a {@link CompoundTag} shaped exactly the way the
 * pre-DATAVERSION code would have written it (no {@code DataVersion} key)
 * must:
 * <ol>
 *   <li>Load through the production {@code SavedData.load(...)} entry point
 *       without throwing.</li>
 *   <li>Preserve every persisted record into the in-memory shape.</li>
 *   <li>Stamp {@code DataVersion = 1} when the loaded SavedData is
 *       re-saved.</li>
 * </ol>
 *
 * <p>Why this proves the acceptance: this is the same legacy-load behavior
 * the migration test guards in JUnit, but executed inside the
 * GameTest harness with a real {@link HolderLookup.Provider} taken from
 * {@code helper.getLevel().registryAccess()} — so the gametest pins down
 * the contract under the same registry context production code sees.
 */
@GameTestHolder(BannerModMain.MOD_ID)
public class SavedDataLegacyMigrationGameTests {

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void legacyWarCooldownSaveLoadsCleanlyAndIsRestampedToCurrentVersion(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        HolderLookup.Provider registries = level.registryAccess();

        // Produce a v0-style tag: shape it the way pre-DataVersion code wrote
        // a WarCooldown payload — payload only, no DataVersion key.
        WarCooldownRuntime runtime = new WarCooldownRuntime();
        UUID entity = UUID.randomUUID();
        runtime.grant(entity, WarCooldownKind.LOST_TERRITORY_IMMUNITY, 200L);

        CompoundTag legacyTag = new CompoundTag();
        legacyTag.put("WarCooldowns",
                runtime.toTag().getList("WarCooldowns", Tag.TAG_COMPOUND));
        helper.assertTrue(
                !legacyTag.contains(SavedDataVersioning.DATA_VERSION_KEY),
                "Precondition: synthesized legacy tag must NOT carry DataVersion"
        );

        // Production load entry point. Must not throw on the legacy shape.
        WarCooldownSavedData reloaded = WarCooldownSavedData.load(legacyTag, registries);

        helper.assertTrue(
                reloaded.runtime().isActive(entity, WarCooldownKind.LOST_TERRITORY_IMMUNITY, 100L),
                "Legacy WarCooldown record must survive the v0->v1 load"
        );

        // Re-save through the production save() override and observe the
        // DataVersion stamp. This is what guarantees the next on-disk write
        // carries the schema marker and that future loads can branch on it.
        CompoundTag resaved = reloaded.save(new CompoundTag(), registries);

        helper.assertTrue(
                resaved.contains(SavedDataVersioning.DATA_VERSION_KEY, Tag.TAG_INT),
                "Resaved tag must carry the DataVersion key"
        );
        helper.assertTrue(
                resaved.getInt(SavedDataVersioning.DATA_VERSION_KEY) == 1,
                "Resaved DataVersion must be the current schema version (1); got "
                        + resaved.getInt(SavedDataVersioning.DATA_VERSION_KEY)
        );
        helper.assertTrue(
                resaved.contains("WarCooldowns", Tag.TAG_LIST),
                "Resaved tag must still carry the WarCooldowns payload after the v0->v1 round trip"
        );

        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void legacyWarAuditLogSaveLoadsCleanlyAndIsRestampedToCurrentVersion(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        HolderLookup.Provider registries = level.registryAccess();

        // Build an audit log payload, then strip DataVersion to simulate the
        // shape pre-DataVersion code persisted.
        WarAuditLogSavedData seed = new WarAuditLogSavedData();
        UUID warId = UUID.randomUUID();
        WarAuditEntry appended = seed.append(warId, "INCIDENT", "gametest-legacy", 7L);
        CompoundTag savedShape = seed.save(new CompoundTag(), registries);

        CompoundTag legacyTag = new CompoundTag();
        legacyTag.put("Entries", savedShape.getList("Entries", Tag.TAG_COMPOUND));
        helper.assertTrue(
                !legacyTag.contains(SavedDataVersioning.DATA_VERSION_KEY),
                "Precondition: hand-shaped legacy tag must NOT carry DataVersion"
        );

        WarAuditLogSavedData reloaded = WarAuditLogSavedData.load(legacyTag, registries);

        List<WarAuditEntry> all = reloaded.all();
        helper.assertTrue(all.size() == 1, "Exactly one audit entry must survive the legacy load");
        helper.assertTrue(
                appended.warId().equals(all.get(0).warId())
                        && appended.type().equals(all.get(0).type())
                        && appended.detail().equals(all.get(0).detail())
                        && appended.gameTime() == all.get(0).gameTime(),
                "Loaded audit entry must equal the seeded entry on warId/type/detail/gameTime"
        );

        CompoundTag resaved = reloaded.save(new CompoundTag(), registries);
        helper.assertTrue(
                resaved.contains(SavedDataVersioning.DATA_VERSION_KEY, Tag.TAG_INT)
                        && resaved.getInt(SavedDataVersioning.DATA_VERSION_KEY) == 1,
                "Resaved audit log must stamp DataVersion=1"
        );
        helper.assertTrue(
                resaved.getList("Entries", Tag.TAG_COMPOUND).size() == 1,
                "Resaved audit log must preserve the entries payload"
        );

        helper.succeed();
    }

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void freshSavedDataAlwaysWritesCurrentDataVersionOnFirstSave(GameTestHelper helper) {
        // Sanity guard: a freshly constructed WarCooldownSavedData (no legacy
        // tag involved) must also stamp DataVersion=1, so future loads from a
        // fresh world boot get a versioned payload from the start.
        WarCooldownSavedData fresh = new WarCooldownSavedData();
        fresh.runtime().grant(UUID.randomUUID(), WarCooldownKind.PEACEFUL_TOGGLE_RECENT, 60L);

        ListTag emptyEntries = new ListTag();
        CompoundTag scratch = new CompoundTag();
        scratch.put("WarCooldowns", emptyEntries);

        CompoundTag saved = fresh.save(new CompoundTag(), helper.getLevel().registryAccess());
        helper.assertTrue(
                saved.contains(SavedDataVersioning.DATA_VERSION_KEY, Tag.TAG_INT)
                        && saved.getInt(SavedDataVersioning.DATA_VERSION_KEY) == 1,
                "Fresh SavedData must carry DataVersion=1 on its first save"
        );
        helper.succeed();
    }
}
