package com.talhanation.bannermod.client.military.gui;

import org.junit.jupiter.api.Test;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class MinecraftStyleContractTest {
    private static final Path ROOT = Path.of("");

    private static final List<String> REDESIGNED_SCREENS = List.of(
            "src/main/java/com/talhanation/bannermod/client/military/gui/PromoteScreen.java",
            "src/main/java/com/talhanation/bannermod/client/military/gui/RenameRecruitScreen.java",
            "src/main/java/com/talhanation/bannermod/client/military/gui/ConfirmScreen.java",
            "src/main/java/com/talhanation/bannermod/client/military/gui/RecruitHireScreen.java",
            "src/main/java/com/talhanation/bannermod/client/military/gui/RecruitMoreScreen.java",
            "src/main/java/com/talhanation/bannermod/client/military/gui/ScoutScreen.java",
            "src/main/java/com/talhanation/bannermod/client/military/gui/PatrolLeaderScreen.java",
            "src/main/java/com/talhanation/bannermod/client/military/gui/MessengerScreen.java",
            "src/main/java/com/talhanation/bannermod/client/military/gui/MessengerMainScreen.java",
            "src/main/java/com/talhanation/bannermod/client/military/gui/MessengerAnswerScreen.java",
            "src/main/java/com/talhanation/bannermod/client/military/gui/RecruitInventoryScreen.java",
            "src/main/java/com/talhanation/bannermod/client/military/gui/CommandScreen.java",
            "src/main/java/com/talhanation/bannermod/client/military/gui/commandscreen/CombatCategory.java"
    );

    @Test
    void foundationHelpersRemainInPlace() throws IOException {
        String style = read("src/main/java/com/talhanation/bannermod/client/military/gui/MilitaryGuiStyle.java");
        assertTrue(style.contains("public static String clampLabel(Font font") || style.contains("clampLabel(Font font"),
                "MilitaryGuiStyle.clampLabel(Font, ...) helper missing");
        assertTrue(style.contains("parchmentPanel"), "parchmentPanel helper missing");
        assertTrue(style.contains("insetPanel"), "insetPanel helper missing");
        assertTrue(style.contains("commandButton"), "commandButton helper missing");
        assertTrue(Files.exists(ROOT.resolve("src/main/java/com/talhanation/bannermod/client/military/gui/widgets/ActionMenuButton.java")),
                "ActionMenuButton widget missing");
    }

    @Test
    void redesignedScreensAdoptMilitaryGuiStyle() throws IOException {
        for (String path : REDESIGNED_SCREENS) {
            String src = read(path);
            boolean styled = src.contains("MilitaryGuiStyle") || src.contains("ActionMenuButton");
            assertTrue(styled,
                    path + " must reference MilitaryGuiStyle or ActionMenuButton (parchment chrome / clampLabel / collapsed action menu)");
        }
    }

    @Test
    void redesignedScreensDoNotUseLegacyFontColorLiteral() throws IOException {
        Pattern legacy = Pattern.compile("(?<![\\w.])4210752(?![\\w])");
        for (String path : REDESIGNED_SCREENS) {
            String src = read(path);
            Matcher m = legacy.matcher(src);
            assertFalse(m.find(),
                    path + " still uses legacy fontColor literal 4210752 (should be MilitaryGuiStyle.TEXT_DARK)");
        }
    }

    @Test
    void langFilesContainEveryRecentRecruitsKeyInBothEnAndRu() throws IOException {
        String en = read("src/main/resources/assets/bannermod/lang/en_us.json");
        String ru = read("src/main/resources/assets/bannermod/lang/ru_ru.json");

        List<String> requiredKeys = List.of(
                "gui.recruits.command.menu.aggro",
                "gui.recruits.command.menu.fire",
                "gui.recruits.command.menu.stance",
                "gui.recruits.command.menu.shields",
                "gui.recruits.inv.menu.orders",
                "gui.recruits.inv.menu.mount",
                "gui.recruits.promote.screen.title",
                "gui.recruits.command.text.formation_testudo",
                "gui.recruits.command.text.tight.on",
                "gui.recruits.command.text.tight.off",
                "gui.workers.merchant.button.manage",
                "gui.workers.merchant.manage.move_up",
                "gui.workers.merchant.manage.move_down",
                "gui.bannermod.hud.war_vs"
        );

        for (String key : requiredKeys) {
            assertTrue(en.contains("\"" + key + "\""),
                    "en_us.json missing key: " + key);
            assertTrue(ru.contains("\"" + key + "\""),
                    "ru_ru.json missing key: " + key);
        }
    }

    @Test
    void actionMenuButtonStaysAvailableForReuse() throws IOException {
        String widget = read("src/main/java/com/talhanation/bannermod/client/military/gui/widgets/ActionMenuButton.java");
        assertTrue(widget.contains("ContextMenuEntry"),
                "ActionMenuButton must accept ContextMenuEntry list");
        assertTrue(widget.contains("MilitaryGuiStyle"),
                "ActionMenuButton must reuse MilitaryGuiStyle palette");
    }

    @Test
    void hudOverlayWarLabelStaysLocalized() throws IOException {
        String hud = read("src/main/java/com/talhanation/bannermod/client/military/gui/overlay/HudOverlayCoordinator.java");
        assertTrue(hud.contains("gui.bannermod.hud.war_vs"),
                "HudOverlayCoordinator dropped the localized war_vs key");
        assertFalse(hud.contains("\" vs \""),
                "HudOverlayCoordinator regressed to hardcoded \" vs \" separator");
    }

    /**
     * Player names in player-facing screens must come from a name-resolving call
     * (display name, profile name, last-known nick). Showing the raw UUID via
     * `getUUID().toString()` next to a label is a known regression.
     */
    @Test
    void playerFacingScreensDoNotRenderRawUuidAsLabel() throws IOException {
        Pattern uuidLeak = Pattern.compile("getUUID\\(\\)\\.toString\\(\\)");
        for (String path : REDESIGNED_SCREENS) {
            String src = read(path);
            assertFalse(uuidLeak.matcher(src).find(),
                    path + " uses getUUID().toString() — players must see resolved nicks, not raw UUIDs");
        }
    }

    /**
     * Player-facing screens must not stack > 7 same-tier `Button.builder(...)` /
     * `addRenderableWidget(new Button(...))` widgets. Above that threshold it
     * counts as a button wall and must collapse via ActionMenuButton or
     * DropDownMenu. Threshold is intentionally above 5 to allow a small grace
     * window for category triggers + escape buttons.
     */
    @Test
    void redesignedScreensDoNotStackButtonWalls() throws IOException {
        Pattern button = Pattern.compile("Button\\.builder\\(|new ExtendedButton\\(|new RecruitsCommandButton\\(|new Button\\(");
        Pattern actionMenuOrDropdown = Pattern.compile("ActionMenuButton|DropDownMenu|ScrollDropDownMenu");
        for (String path : REDESIGNED_SCREENS) {
            String src = read(path);
            int buttons = countMatches(button, src);
            boolean hasCollapse = actionMenuOrDropdown.matcher(src).find();
            assertTrue(buttons <= 7 || hasCollapse,
                    path + " has " + buttons + " button widgets but no ActionMenuButton/DropDownMenu — collapse the wall");
        }
    }

    /**
     * Newly-redesigned screens shouldn't keep raw `Component.literal("...")`
     * with English text — those must move to translation keys. Single-character
     * symbols like "+", "-", ">", "x" are allowed as universal-symbol controls.
     */
    @Test
    void redesignedScreensDoNotShipHardcodedEnglishLiterals() throws IOException {
        Pattern literal = Pattern.compile("Component\\.literal\\(\"([^\"]*)\"\\)");
        for (String path : REDESIGNED_SCREENS) {
            String src = read(path);
            Matcher m = literal.matcher(src);
            while (m.find()) {
                String text = m.group(1);
                if (text.isEmpty() || text.length() <= 2) continue; // symbols / single chars
                if (text.matches("[\\p{Punct}\\p{Digit}\\s]+")) continue; // numeric / punct only
                if (text.startsWith("gui.") || text.startsWith("bannermod.")) continue; // accidental key passthrough
                throw new AssertionError(path + " ships hardcoded English literal: \"" + text + "\" — move to translation key");
            }
        }
    }

    /**
     * The dropdown widgets we rely on must keep their public hooks for screens
     * to drive them, otherwise hover/click integration silently breaks.
     */
    @Test
    void dropDownMenuKeepsPublicMouseHooks() throws IOException {
        String dd = read("src/main/java/com/talhanation/bannermod/client/military/gui/widgets/DropDownMenu.java");
        assertTrue(dd.contains("onMouseClick"), "DropDownMenu missing onMouseClick hook");
        assertTrue(dd.contains("onMouseMove"), "DropDownMenu missing onMouseMove hook");
        String amb = read("src/main/java/com/talhanation/bannermod/client/military/gui/widgets/ActionMenuButton.java");
        assertTrue(amb.contains("mouseClicked") || amb.contains("onPress"),
                "ActionMenuButton must expose a click entry-point");
    }

    /**
     * Redesigned screens must reference at least one clamping / wrapping helper
     * so dynamic strings (player nicks, group names, market names) can't
     * overflow the parchment inset at GUI scale 2/3.
     */
    @Test
    void redesignedScreensClampDynamicLabels() throws IOException {
        Pattern clamp = Pattern.compile("clampLabel|plainSubstrByWidth|font\\.split|drawWrapped");
        for (String path : REDESIGNED_SCREENS) {
            String src = read(path);
            // CombatCategory uses ActionMenuButton which clamps internally; allow that path.
            boolean exempt = src.contains("ActionMenuButton")
                    && !src.contains("guiGraphics.drawString(font, recruit.")
                    && !src.contains("guiGraphics.drawString(font, group");
            if (exempt) continue;
            assertTrue(clamp.matcher(src).find(),
                    path + " draws labels without clampLabel / plainSubstrByWidth / Font.split / drawWrapped");
        }
    }

    /**
     * Foundation palette constants are the only sanctioned source for body /
     * muted / state text colors in redesigned screens. Catch a few common
     * legacy literals that used to flood these files.
     */
    @Test
    void redesignedScreensPreferStylePaletteOverLegacyHexes() throws IOException {
        Pattern[] legacy = {
                Pattern.compile("(?<![\\w.])0xF0E6D2(?![\\w])"),
                Pattern.compile("(?<![\\w.])0xB59A6A(?![\\w])"),
                Pattern.compile("(?<![\\w.])0x6E5A45(?![\\w])"),
                Pattern.compile("(?<![\\w.])0x2E5D32(?![\\w])"),
                Pattern.compile("(?<![\\w.])0x8A1F11(?![\\w])"),
                Pattern.compile("(?<![\\w.])16250871(?![\\w])")
        };
        for (String path : REDESIGNED_SCREENS) {
            String src = read(path);
            for (Pattern p : legacy) {
                assertFalse(p.matcher(src).find(),
                        path + " still uses legacy hex/literal " + p.pattern() + " (route through MilitaryGuiStyle constants)");
            }
        }
    }

    private static int countMatches(Pattern p, String src) {
        Matcher m = p.matcher(src);
        int n = 0;
        while (m.find()) n++;
        return n;
    }

    private static String read(String relativePath) throws IOException {
        return Files.readString(ROOT.resolve(relativePath));
    }
}
