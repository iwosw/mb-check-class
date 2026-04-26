package com.talhanation.bannermod.commands.war;

import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.talhanation.bannermod.war.WarRuntimeContext;
import com.talhanation.bannermod.war.registry.PoliticalEntityRecord;
import com.talhanation.bannermod.war.registry.PoliticalRegistryRuntime;
import com.talhanation.bannermod.war.runtime.WarDeclarationRecord;
import com.talhanation.bannermod.war.runtime.WarDeclarationRuntime;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;

public final class WarCommandSupport {
    public static final SimpleCommandExceptionType ERR_NOT_FOUND =
            new SimpleCommandExceptionType(Component.literal("Political entity not found."));
    public static final SimpleCommandExceptionType ERR_WAR_NOT_FOUND =
            new SimpleCommandExceptionType(Component.literal("War declaration not found."));
    public static final SimpleCommandExceptionType ERR_NOT_LEADER =
            new SimpleCommandExceptionType(Component.literal("Only the political entity leader (or an op) can do that."));

    private WarCommandSupport() {
    }

    public static ServerLevel level(CommandContext<CommandSourceStack> context) {
        return context.getSource().getServer().overworld();
    }

    public static PoliticalRegistryRuntime registry(CommandContext<CommandSourceStack> context) {
        return WarRuntimeContext.registry(level(context));
    }

    public static WarDeclarationRuntime declarations(CommandContext<CommandSourceStack> context) {
        return WarRuntimeContext.declarations(level(context));
    }

    public static PoliticalEntityRecord requireEntity(CommandContext<CommandSourceStack> context, String token)
            throws CommandSyntaxException {
        return registry(context).byNameOrIdFragment(token).orElseThrow(ERR_NOT_FOUND::create);
    }

    public static WarDeclarationRecord requireWar(CommandContext<CommandSourceStack> context, String token)
            throws CommandSyntaxException {
        return declarations(context).byIdFragment(token).orElseThrow(ERR_WAR_NOT_FOUND::create);
    }

    public static boolean isLeaderOrOp(CommandContext<CommandSourceStack> context, PoliticalEntityRecord record) {
        if (context.getSource().hasPermission(2)) {
            return true;
        }
        java.util.UUID actor = context.getSource().getEntity() == null
                ? null
                : context.getSource().getEntity().getUUID();
        return actor != null && actor.equals(record.leaderUuid());
    }

    public static void reply(CommandContext<CommandSourceStack> context, String message) {
        context.getSource().sendSuccess(() -> Component.literal(message), false);
    }

    public static void replyComponent(CommandContext<CommandSourceStack> context, Component component) {
        context.getSource().sendSuccess(() -> component, false);
    }
}
