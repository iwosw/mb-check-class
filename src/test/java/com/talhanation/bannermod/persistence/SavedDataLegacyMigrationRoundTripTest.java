package com.talhanation.bannermod.persistence;

import com.talhanation.bannermod.governance.BannerModTreasuryLedgerSnapshot;
import com.talhanation.bannermod.governance.BannerModTreasuryManager;
import com.talhanation.bannermod.war.audit.WarAuditEntry;
import com.talhanation.bannermod.war.audit.WarAuditLogSavedData;
import com.talhanation.bannermod.war.cooldown.WarCooldownKind;
import com.talhanation.bannermod.war.cooldown.WarCooldownRuntime;
import com.talhanation.bannermod.war.cooldown.WarCooldownSavedData;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.world.level.ChunkPos;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class SavedDataLegacyMigrationRoundTripTest {

    @Test
    void warCooldownLegacyTagLoadsAndUpgradesToCurrentVersionOnNextSave() {
        WarCooldownRuntime runtime = new WarCooldownRuntime();
        UUID entity = UUID.randomUUID();
        runtime.grant(entity, WarCooldownKind.LOST_TERRITORY_IMMUNITY, 200L);

        CompoundTag legacyTag = new CompoundTag();
        legacyTag.put("WarCooldowns",
                runtime.toTag().getList("WarCooldowns", Tag.TAG_COMPOUND));
        assertFalse(legacyTag.contains(SavedDataVersioning.DATA_VERSION_KEY),
                "precondition: legacy tag must not carry DataVersion");

        WarCooldownSavedData reloaded = WarCooldownSavedData.load(legacyTag, null);
        CompoundTag resaved = reloaded.save(new CompoundTag(), null);

        assertEquals(1, SavedDataVersioning.getVersion(resaved),
                "reloading legacy tag and saving again must stamp DataVersion=1");
        assertTrue(reloaded.runtime().isActive(entity, WarCooldownKind.LOST_TERRITORY_IMMUNITY, 100L),
                "legacy cooldown record must survive the v0->v1 migration");
    }

    @Test
    void treasuryLegacyTagLoadsAndUpgradesToCurrentVersionOnNextSave() {
        UUID claimUuid = UUID.randomUUID();
        BannerModTreasuryLedgerSnapshot ledger = BannerModTreasuryLedgerSnapshot
                .create(claimUuid, new ChunkPos(2, 3), "demo")
                .withDeposit(500, 10L);

        CompoundTag legacyTag = new CompoundTag();
        ListTag list = new ListTag();
        list.add(ledger.toTag());
        legacyTag.put("Ledgers", list);
        assertFalse(legacyTag.contains(SavedDataVersioning.DATA_VERSION_KEY),
                "precondition: legacy tag must not carry DataVersion");

        BannerModTreasuryManager reloaded = BannerModTreasuryManager.load(legacyTag, null);
        CompoundTag resaved = reloaded.save(new CompoundTag(), null);

        assertEquals(1, SavedDataVersioning.getVersion(resaved),
                "reloading legacy tag and saving again must stamp DataVersion=1");
        assertEquals(ledger, reloaded.getLedger(claimUuid),
                "legacy treasury ledger must survive the v0->v1 migration unchanged");
    }

    @Test
    void warAuditLogLegacyTagLoadsAndUpgradesToCurrentVersionOnNextSave() {
        WarAuditLogSavedData seed = new WarAuditLogSavedData();
        UUID warId = UUID.randomUUID();
        WarAuditEntry appended = seed.append(warId, "INCIDENT", "demo", 42L);

        // Capture the legacy on-disk shape that pre-DataVersion saves produced:
        // the saved tag minus the DataVersion key.
        CompoundTag savedShape = seed.save(new CompoundTag(), null);
        CompoundTag legacyTag = new CompoundTag();
        legacyTag.put("Entries", savedShape.getList("Entries", Tag.TAG_COMPOUND));
        assertFalse(legacyTag.contains(SavedDataVersioning.DATA_VERSION_KEY),
                "precondition: hand-built legacy tag must not carry DataVersion");

        WarAuditLogSavedData reloaded = WarAuditLogSavedData.load(legacyTag, null);
        CompoundTag resaved = reloaded.save(new CompoundTag(), null);

        assertEquals(1, SavedDataVersioning.getVersion(resaved),
                "reloading legacy tag and saving again must stamp DataVersion=1");
        List<WarAuditEntry> all = reloaded.all();
        assertEquals(1, all.size());
        assertEquals(appended.warId(), all.get(0).warId());
        assertEquals(appended.type(), all.get(0).type());
        assertEquals(appended.detail(), all.get(0).detail());
        assertEquals(appended.gameTime(), all.get(0).gameTime());
    }
}
