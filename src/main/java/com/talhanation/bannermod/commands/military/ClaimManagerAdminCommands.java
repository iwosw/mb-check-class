package com.talhanation.bannermod.commands.military;

import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.talhanation.bannermod.events.ClaimEvents;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.persistence.military.RecruitsClaimManager;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import com.mojang.brigadier.arguments.BoolArgumentType;

final class ClaimManagerAdminCommands {
    private ClaimManagerAdminCommands() {
    }

    static LiteralArgumentBuilder<CommandSourceStack> create() {
        return Commands.literal("claimManager")
                .then(Commands.literal("getClaimAtPosition")
                        .executes(ctx -> {
                            RecruitsClaim claim = getClaimAtPlayerPosition(ctx.getSource().getPlayerOrException());

                            if (claim == null) {
                                ctx.getSource().sendFailure(Component.literal("No claim found at your position."));
                                return 0;
                            }

                            ctx.getSource().sendSuccess(() -> Component.literal("Claim: [" + claim + "]"), false);
                            return 1;
                        }))
                .then(Commands.literal("setAdminChunk")
                        .then(Commands.argument("isAdmin", BoolArgumentType.bool())
                                .executes(ctx -> {
                                    boolean isAdmin = BoolArgumentType.getBool(ctx, "isAdmin");
                                    RecruitsClaim claim = getClaimAtPlayerPosition(ctx.getSource().getPlayerOrException());

                                    if (claim == null) {
                                        ctx.getSource().sendFailure(Component.literal("No claim found at your position."));
                                        return 0;
                                    }

                                    claim.setAdminClaim(isAdmin);
                                    ctx.getSource().sendSuccess(() ->
                                            Component.literal("Claim [" + claim + "] is now set to admin = " + isAdmin), false);
                                    broadcastClaims(ctx.getSource());
                                    return 1;
                                })))
                .then(Commands.literal("deleteClaim")
                        .executes(ctx -> {
                            RecruitsClaim claim = getClaimAtPlayerPosition(ctx.getSource().getPlayerOrException());

                            if (claim == null) {
                                ctx.getSource().sendFailure(Component.literal("No claim found at your position."));
                                return 0;
                            }

                            ClaimEvents.claimManager().removeClaim(claim);
                            ctx.getSource().sendSuccess(() ->
                                    Component.literal("Claim [" + claim + "] is now deleted."), false);
                            broadcastClaims(ctx.getSource());
                            return 1;
                        }))
                .then(Commands.literal("setBlockBreaking")
                        .then(Commands.argument("true/false", BoolArgumentType.bool())
                                .executes(ctx -> {
                                    boolean should = BoolArgumentType.getBool(ctx, "true/false");
                                    RecruitsClaim claim = getPlayerClaim(ctx.getSource().getPlayerOrException());

                                    if (claim == null) {
                                        ctx.getSource().sendFailure(Component.literal("No claim found at your position."));
                                        return 0;
                                    }

                                    claim.setBlockBreakingAllowed(should);
                                    ctx.getSource().sendSuccess(() ->
                                            Component.literal("Claim [" + claim + "] has now block breaking = " + should), false);
                                    broadcastClaims(ctx.getSource());
                                    return 1;
                                })))
                .then(Commands.literal("setBlockPlacing")
                        .then(Commands.argument("true/false", BoolArgumentType.bool())
                                .executes(ctx -> {
                                    boolean should = BoolArgumentType.getBool(ctx, "true/false");
                                    RecruitsClaim claim = getPlayerClaim(ctx.getSource().getPlayerOrException());

                                    if (claim == null) {
                                        ctx.getSource().sendFailure(Component.literal("No claim found at your position."));
                                        return 0;
                                    }

                                    claim.setBlockPlacementAllowed(should);
                                    ctx.getSource().sendSuccess(() ->
                                            Component.literal("Claim [" + claim + "] has now block placing = " + should), false);
                                    broadcastClaims(ctx.getSource());
                                    return 1;
                                })))
                .then(Commands.literal("setBlockInteraction")
                        .then(Commands.argument("true/false", BoolArgumentType.bool())
                                .executes(ctx -> {
                                    boolean should = BoolArgumentType.getBool(ctx, "true/false");
                                    RecruitsClaim claim = getPlayerClaim(ctx.getSource().getPlayerOrException());

                                    if (claim == null) {
                                        ctx.getSource().sendFailure(Component.literal("No claim found at your position."));
                                        return 0;
                                    }

                                    claim.setBlockInteractionAllowed(should);
                                    ctx.getSource().sendSuccess(() ->
                                            Component.literal("Claim [" + claim + "] has now block interaction = " + should), false);
                                    broadcastClaims(ctx.getSource());
                                    return 1;
                                })));
    }

    private static void broadcastClaims(CommandSourceStack source) {
        ClaimEvents.claimManager().broadcastClaimsToAll(source.getLevel());
    }

    private static RecruitsClaim getClaimAtPlayerPosition(ServerPlayer player) {
        ChunkPos chunkPos = player.chunkPosition();
        return RecruitsClaimManager.getClaimAt(chunkPos, ClaimEvents.claimManager().getAllClaims().stream().toList());
    }

    private static RecruitsClaim getPlayerClaim(ServerPlayer player) {
        return ClaimEvents.claimManager().getClaim(player.chunkPosition());
    }
}
