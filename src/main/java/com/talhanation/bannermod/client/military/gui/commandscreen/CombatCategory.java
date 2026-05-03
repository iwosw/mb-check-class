package com.talhanation.bannermod.client.military.gui.commandscreen;

import com.talhanation.bannermod.ai.military.CombatStance;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.client.military.gui.CommandScreen;
import com.talhanation.bannermod.client.military.gui.group.RecruitsCommandButton;
import com.talhanation.bannermod.client.military.gui.widgets.ActionMenuButton;
import com.talhanation.bannermod.client.military.gui.widgets.ContextMenuEntry;
import com.talhanation.bannermod.network.messages.military.*;
import com.talhanation.bannermod.persistence.military.RecruitsGroup;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;

public class CombatCategory implements ICommandCategory {
    private static final MutableComponent TOOLTIP_STRATEGIC_FIRE = Component.translatable("gui.recruits.command.tooltip.strategic_fire");
    private static final MutableComponent TOOLTIP_HOLD_STRATEGIC_FIRE = Component.translatable("gui.recruits.command.tooltip.hold_strategic_fire");
    private static final MutableComponent TOOLTIP_PASSIVE = Component.translatable("gui.recruits.command.tooltip.passive");
    private static final MutableComponent TOOLTIP_NEUTRAL = Component.translatable("gui.recruits.command.tooltip.neutral");
    private static final MutableComponent TOOLTIP_AGGRESSIVE = Component.translatable("gui.recruits.command.tooltip.aggressive");
    private static final MutableComponent TOOLTIP_RAID = Component.translatable("gui.recruits.command.tooltip.raid");
    private static final MutableComponent TOOLTIP_CLEAR_TARGET = Component.translatable("gui.recruits.command.tooltip.clearTargets");
    private static final MutableComponent TOOLTIP_HOLD_FIRE = Component.translatable("gui.recruits.command.tooltip.hold_fire");
    private static final MutableComponent TOOLTIP_FIRE_AT_WILL = Component.translatable("gui.recruits.command.tooltip.fire_at_will");
    private static final MutableComponent TOOLTIP_STANCE_LOOSE = Component.translatable("gui.recruits.command.tooltip.stance_loose");
    private static final MutableComponent TOOLTIP_STANCE_LINE_HOLD = Component.translatable("gui.recruits.command.tooltip.stance_line_hold");
    private static final MutableComponent TOOLTIP_STANCE_SHIELD_WALL = Component.translatable("gui.recruits.command.tooltip.stance_shield_wall");
    private static final MutableComponent TEXT_PASSIVE = Component.translatable("gui.recruits.command.text.passive");
    private static final MutableComponent TEXT_NEUTRAL = Component.translatable("gui.recruits.command.text.neutral");
    private static final MutableComponent TEXT_AGGRESSIVE = Component.translatable("gui.recruits.command.text.aggressive");
    private static final MutableComponent TEXT_RAID = Component.translatable("gui.recruits.command.text.raid");
    private static final MutableComponent TEXT_STRATEGIC_FIRE = Component.translatable("gui.recruits.command.text.strategic_fire");
    private static final MutableComponent TEXT_HOLD_STRATEGIC_FIRE = Component.translatable("gui.recruits.command.text.hold_strategic_fire");
    private static final MutableComponent TEXT_FIRE_AT_WILL = Component.translatable("gui.recruits.command.text.fire_at_will");
    private static final MutableComponent TEXT_HOLD_FIRE = Component.translatable("gui.recruits.command.text.hold_fire");
    private static final MutableComponent TEXT_CLEAR_TARGET = Component.translatable("gui.recruits.command.text.clearTargets");
    private static final MutableComponent TEXT_STANCE_LOOSE = Component.translatable("gui.recruits.command.text.stance_loose");
    private static final MutableComponent TEXT_STANCE_LINE_HOLD = Component.translatable("gui.recruits.command.text.stance_line_hold");
    private static final MutableComponent TEXT_STANCE_SHIELD_WALL = Component.translatable("gui.recruits.command.text.stance_shield_wall");
    private static final MutableComponent TOOLTIP_COMBAT = Component.translatable("gui.recruits.command.tooltip.combat");

    private static final MutableComponent TEXT_ATTACK = Component.translatable("gui.recruits.command.text.attack");
    private static final MutableComponent TOOLTIP_ATTACK = Component.translatable("gui.recruits.command.tooltip.attack");

    // Group triggers (collapsing the original button wall into named menus).
    private static final MutableComponent TEXT_MENU_AGGRO = Component.translatable("gui.recruits.command.menu.aggro");
    private static final MutableComponent TEXT_MENU_FIRE = Component.translatable("gui.recruits.command.menu.fire");
    private static final MutableComponent TEXT_MENU_STANCE = Component.translatable("gui.recruits.command.menu.stance");
    private static final MutableComponent TEXT_MENU_SHIELDS = Component.translatable("gui.recruits.command.menu.shields");
    private static final MutableComponent TEXT_SHIELDS_UP = Component.translatable("gui.recruits.command.text.shields_up");
    private static final MutableComponent TEXT_SHIELDS_DOWN = Component.translatable("gui.recruits.command.text.shields_down");

    @Override
    public Component getToolTipName() {
        return TOOLTIP_COMBAT;
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.IRON_SWORD);
    }

    @Override
    public void createButtons(CommandScreen screen, int x, int y, List<RecruitsGroup> groups, Player player) {
        boolean isOneGroupActive = groups.stream().anyMatch(g -> !g.isDisabled());
        boolean canTargetBlock = isOneGroupActive && screen.rayBlockPos != null;

        // Tier 1 (left column): aggro / stance triggers — server-authoritative actions; client only sends intent.
        ActionMenuButton aggroMenu = new ActionMenuButton(x - 140, y - 38, 80, 20, TEXT_MENU_AGGRO, List.of(
                new ContextMenuEntry(TEXT_PASSIVE.getString(), () -> sendAggro(screen, groups, player, 3, 13), isOneGroupActive),
                new ContextMenuEntry(TEXT_NEUTRAL.getString(), () -> sendAggro(screen, groups, player, 0, 10), isOneGroupActive),
                new ContextMenuEntry(TEXT_AGGRESSIVE.getString(), () -> sendAggro(screen, groups, player, 1, 11), isOneGroupActive),
                new ContextMenuEntry(TEXT_RAID.getString(), () -> sendAggro(screen, groups, player, 2, 12), isOneGroupActive)
        ));
        aggroMenu.setTooltip(Tooltip.create(TOOLTIP_PASSIVE.copy().append("\n").append(TOOLTIP_NEUTRAL).append("\n").append(TOOLTIP_AGGRESSIVE).append("\n").append(TOOLTIP_RAID)));
        aggroMenu.active = isOneGroupActive;
        screen.addRenderableWidget(aggroMenu);

        ActionMenuButton stanceMenu = new ActionMenuButton(x - 140, y - 12, 80, 20, TEXT_MENU_STANCE, List.of(
                new ContextMenuEntry(TEXT_STANCE_LOOSE.getString(), () -> sendStance(screen, groups, player, CombatStance.LOOSE, 76), isOneGroupActive),
                new ContextMenuEntry(TEXT_STANCE_LINE_HOLD.getString(), () -> sendStance(screen, groups, player, CombatStance.LINE_HOLD, 77), isOneGroupActive),
                new ContextMenuEntry(TEXT_STANCE_SHIELD_WALL.getString(), () -> sendStance(screen, groups, player, CombatStance.SHIELD_WALL, 78), isOneGroupActive)
        ));
        stanceMenu.setTooltip(Tooltip.create(TOOLTIP_STANCE_LOOSE.copy().append("\n").append(TOOLTIP_STANCE_LINE_HOLD).append("\n").append(TOOLTIP_STANCE_SHIELD_WALL)));
        stanceMenu.active = isOneGroupActive;
        screen.addRenderableWidget(stanceMenu);

        // Tier 2 (right column): fire policy / shields menus.
        ActionMenuButton fireMenu = new ActionMenuButton(x + 60, y - 38, 80, 20, TEXT_MENU_FIRE, List.of(
                new ContextMenuEntry(TEXT_FIRE_AT_WILL.getString(), () -> {
                    sendFireAtWill(screen, groups, player, true);
                    screen.sendCommandInChat(70);
                }, isOneGroupActive),
                new ContextMenuEntry(TEXT_HOLD_FIRE.getString(), () -> {
                    sendStrategicFire(groups, player, false);
                    sendFireAtWill(screen, groups, player, false);
                    screen.sendCommandInChat(71);
                }, isOneGroupActive),
                new ContextMenuEntry(TEXT_STRATEGIC_FIRE.getString(), () -> {
                    sendStrategicFire(groups, player, true);
                    screen.sendCommandInChat(72);
                }, canTargetBlock),
                new ContextMenuEntry(TEXT_HOLD_STRATEGIC_FIRE.getString(), () -> {
                    sendStrategicFire(groups, player, false);
                    screen.sendCommandInChat(73);
                }, isOneGroupActive)
        ));
        fireMenu.setTooltip(Tooltip.create(TOOLTIP_FIRE_AT_WILL.copy().append("\n").append(TOOLTIP_HOLD_FIRE).append("\n").append(TOOLTIP_STRATEGIC_FIRE).append("\n").append(TOOLTIP_HOLD_STRATEGIC_FIRE)));
        fireMenu.active = isOneGroupActive;
        screen.addRenderableWidget(fireMenu);

        ActionMenuButton shieldsMenu = new ActionMenuButton(x + 60, y - 12, 80, 20, TEXT_MENU_SHIELDS, List.of(
                new ContextMenuEntry(TEXT_SHIELDS_UP.getString(), () -> {
                    sendShields(groups, player, true);
                    screen.sendCommandInChat(74);
                }, isOneGroupActive),
                new ContextMenuEntry(TEXT_SHIELDS_DOWN.getString(), () -> {
                    sendShields(groups, player, false);
                    screen.sendCommandInChat(75);
                }, isOneGroupActive)
        ));
        shieldsMenu.active = isOneGroupActive;
        screen.addRenderableWidget(shieldsMenu);

        // Tier 3 (bottom row, centered): primary actions.
        RecruitsCommandButton attackButton = new RecruitsCommandButton(x, y + 30, TEXT_ATTACK,
                button -> sendAttack(screen, groups, player));
        attackButton.setTooltip(Tooltip.create(TOOLTIP_ATTACK));
        attackButton.active = canTargetBlock;
        screen.addRenderableWidget(attackButton);

        RecruitsCommandButton clearTargetsButton = new RecruitsCommandButton(x, y + 55, TEXT_CLEAR_TARGET,
                button -> sendClearTarget(screen, groups, player));
        clearTargetsButton.setTooltip(Tooltip.create(TOOLTIP_CLEAR_TARGET));
        clearTargetsButton.active = isOneGroupActive;
        screen.addRenderableWidget(clearTargetsButton);
    }

    private static void sendAggro(CommandScreen screen, List<RecruitsGroup> groups, Player player, int aggroState, int chatState) {
        if (groups.isEmpty()) {
            return;
        }
        for (RecruitsGroup group : groups) {
            if (!group.isDisabled()) {
                BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageAggro(player.getUUID(), aggroState, group.getUUID()));
            }
        }
        screen.sendCommandInChat(chatState);
    }

    private static void sendFireAtWill(CommandScreen screen, List<RecruitsGroup> groups, Player player, boolean fire) {
        for (RecruitsGroup group : groups) {
            if (!group.isDisabled()) {
                BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageRangedFire(player.getUUID(), group.getUUID(), fire));
            }
        }
    }

    private static void sendStrategicFire(List<RecruitsGroup> groups, Player player, boolean fire) {
        for (RecruitsGroup group : groups) {
            if (!group.isDisabled()) {
                BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageStrategicFire(player.getUUID(), group.getUUID(), fire));
            }
        }
    }

    private static void sendShields(List<RecruitsGroup> groups, Player player, boolean up) {
        for (RecruitsGroup group : groups) {
            if (!group.isDisabled()) {
                BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageShields(player.getUUID(), group.getUUID(), up));
            }
        }
    }

    private static void sendAttack(CommandScreen screen, List<RecruitsGroup> groups, Player player) {
        if (groups.isEmpty()) {
            return;
        }
        for (RecruitsGroup group : groups) {
            if (!group.isDisabled()) {
                BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageAttack(player.getUUID(), group.getUUID()));
            }
        }
        screen.sendCommandInChat(69);
    }

    private static void sendClearTarget(CommandScreen screen, List<RecruitsGroup> groups, Player player) {
        if (groups.isEmpty()) {
            return;
        }
        for (RecruitsGroup group : groups) {
            if (!group.isDisabled()) {
                BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageClearTarget(player.getUUID(), group.getUUID()));
            }
        }
        screen.sendCommandInChat(9);
    }

    private static void sendStance(CommandScreen screen,
                                   List<RecruitsGroup> groups,
                                   Player player,
                                   CombatStance stance,
                                   int chatState) {
        if (groups.isEmpty()) {
            return;
        }
        for (RecruitsGroup group : groups) {
            if (!group.isDisabled()) {
                BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageCombatStance(player.getUUID(), group.getUUID(), stance));
            }
        }
        screen.sendCommandInChat(chatState);
    }

    @SuppressWarnings("unused")
    private static List<ContextMenuEntry> buildList(ContextMenuEntry... entries) {
        return new ArrayList<>(List.of(entries));
    }
}
