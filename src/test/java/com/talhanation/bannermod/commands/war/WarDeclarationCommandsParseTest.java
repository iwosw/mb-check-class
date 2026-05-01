package com.talhanation.bannermod.commands.war;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.ParseResults;
import net.minecraft.commands.CommandSourceStack;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

class WarDeclarationCommandsParseTest {

    @Test
    void declareParsesQuotedRussianStateNamesWithSpaces() {
        CommandDispatcher<CommandSourceStack> dispatcher = new CommandDispatcher<>();
        BannerModWarCommands.register(dispatcher);

        String input = "bannermod war declare \"Русское царство\" \"Тевтонский орден\" tribute";
        ParseResults<CommandSourceStack> parsed = dispatcher.parse(input, null);

        assertTrue(parsed.getExceptions().isEmpty(), () -> "Unexpected parse errors: " + parsed.getExceptions());
        assertEquals(input.length(), parsed.getReader().getCursor(), "Parser should consume the full declare command");
    }
}
