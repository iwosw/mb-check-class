package com.talhanation.bannermod.client.military.gui.worldmap;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Pins the WORLDMAPCLAIMPE-001 transfer-to-state contract — server-authoritative
 * dual-PE auth, ClaimEditScreen UI wiring, packet registration, and lang
 * coverage in both locales. Source-level invariants only — no client bootstrap.
 */
class ClaimTransferContractTest {
    private static final Path ROOT = Path.of("");

    private static final String MESSAGE =
            "src/main/java/com/talhanation/bannermod/network/messages/military/MessageReassignClaimPoliticalEntity.java";
    private static final String SCREEN =
            "src/main/java/com/talhanation/bannermod/client/military/gui/worldmap/ClaimEditScreen.java";
    private static final String CATALOG =
            "src/main/java/com/talhanation/bannermod/network/catalog/MilitaryPacketCatalog.java";
    private static final String EN_LANG =
            "src/main/resources/assets/bannermod/lang/en_us.json";
    private static final String RU_LANG =
            "src/main/resources/assets/bannermod/lang/ru_ru.json";
    private static final String GUIDE_EN = "MULTIPLAYER_GUIDE_EN.md";
    private static final String GUIDE_RU = "MULTIPLAYER_GUIDE_RU.md";

    @Test
    void serverMessageEnforcesDualPeAuthority() throws IOException {
        assertTrue(Files.exists(ROOT.resolve(MESSAGE)),
                "MessageReassignClaimPoliticalEntity.java must exist");
        String src = read(MESSAGE);
        assertTrue(src.contains("serverbound"),
                "MessageReassignClaimPoliticalEntity must declare a serverbound packet flow");
        assertTrue(src.contains("ClaimPacketAuthority.canEditClaim"),
                "Server message must validate source-PE authority via ClaimPacketAuthority.canEditClaim");
        assertTrue(src.contains("PoliticalEntityAuthority.canAct"),
                "Server message must validate target-PE authority via PoliticalEntityAuthority.canAct");
        assertTrue(src.contains("addOrUpdateClaim"),
                "Server message must persist via ClaimEvents.claimManager().addOrUpdateClaim");

        // All five denial keys must be present in the server-side handler.
        String[] denialKeys = {
                "chat.bannermod.claim.transfer.denied.missing",
                "chat.bannermod.claim.transfer.denied.target_missing",
                "chat.bannermod.claim.transfer.denied.same",
                "chat.bannermod.claim.transfer.denied.no_source_authority",
                "chat.bannermod.claim.transfer.denied.no_target_authority",
        };
        for (String key : denialKeys) {
            assertTrue(src.contains(key),
                    "MessageReassignClaimPoliticalEntity missing denial key: " + key);
        }
    }

    @Test
    void claimEditScreenWiresTransferAction() throws IOException {
        String src = read(SCREEN);
        assertTrue(src.contains("MessageReassignClaimPoliticalEntity"),
                "ClaimEditScreen must send MessageReassignClaimPoliticalEntity to transfer the claim");
        assertTrue(src.contains("gui.bannermod.claim.transfer.button"),
                "ClaimEditScreen must label the Transfer button via the translatable key");
        assertTrue(src.contains("WarClientState.entities"),
                "ClaimEditScreen must source the dropdown from WarClientState.entities()");
        assertTrue(src.contains("PoliticalEntityAuthority.canAct"),
                "ClaimEditScreen must filter dropdown to PEs the caller has authority in");
        assertTrue(src.contains("ConfirmScreen"),
                "ClaimEditScreen must guard the transfer with a vanilla ConfirmScreen");
    }

    @Test
    void militaryCatalogRegistersTransferPacket() throws IOException {
        String catalog = read(CATALOG);
        assertTrue(catalog.contains("MessageReassignClaimPoliticalEntity.class"),
                "MilitaryPacketCatalog must register MessageReassignClaimPoliticalEntity");
    }

    @Test
    void langKeysPresentInBothLocales() throws IOException {
        String[] keys = {
                "gui.bannermod.claim.transfer.label",
                "gui.bannermod.claim.transfer.button",
                "gui.bannermod.claim.transfer.option.detach",
                "gui.bannermod.claim.transfer.disabled.no_targets",
                "gui.bannermod.claim.transfer.disabled.no_source_authority",
                "gui.bannermod.claim.transfer.confirm.title",
                "gui.bannermod.claim.transfer.confirm.body",
                "chat.bannermod.claim.transfer.success",
                "chat.bannermod.claim.transfer.detached",
                "chat.bannermod.claim.transfer.denied.missing",
                "chat.bannermod.claim.transfer.denied.target_missing",
                "chat.bannermod.claim.transfer.denied.same",
                "chat.bannermod.claim.transfer.denied.no_source_authority",
                "chat.bannermod.claim.transfer.denied.no_target_authority",
        };
        String en = read(EN_LANG);
        String ru = read(RU_LANG);
        for (String key : keys) {
            assertTrue(en.contains("\"" + key + "\""), "en_us.json missing key: " + key);
            assertTrue(ru.contains("\"" + key + "\""), "ru_ru.json missing key: " + key);
        }
    }

    @Test
    void playerGuidesDocumentTransferFlow() throws IOException {
        String en = read(GUIDE_EN);
        assertTrue(en.contains("Transfer to state"),
                "MULTIPLAYER_GUIDE_EN.md must reference the new \"Transfer to state\" flow");
        String ru = read(GUIDE_RU);
        assertTrue(ru.contains("Передать государству"),
                "MULTIPLAYER_GUIDE_RU.md must reference the new \"Передать государству\" flow");
    }

    private String read(String relativePath) throws IOException {
        return Files.readString(ROOT.resolve(relativePath));
    }
}
