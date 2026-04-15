package com.talhanation.bannerlord.client.shared.gui.commandscreen;

import com.talhanation.bannerlord.entity.shared.*;

import com.talhanation.bannerlord.shared.logistics.BannerModUpkeepProviders;
import com.talhanation.recruits.Main;
import com.talhanation.bannerlord.client.shared.gui.CommandScreen;
import com.talhanation.bannerlord.client.shared.gui.group.RecruitsCommandButton;
import com.talhanation.bannerlord.client.shared.gui.faction.FactionMainScreen;
import com.talhanation.recruits.network.*;
import com.talhanation.bannerlord.persistence.military.RecruitsGroup;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.List;

public class OtherCategory implements ICommandCategory {

    private static final MutableComponent TOOLTIP_CLEAR_UPKEEP = Component.translatable("gui.bannermod.command.tooltip.clear_upkeep");
    private static final MutableComponent TEXT_CLEAR_UPKEEP = Component.translatable("gui.bannermod.command.text.clear_upkeep");
    private static final MutableComponent TEXT_BACK_TO_MOUNT = Component.translatable("gui.bannermod.command.text.backToMount");
    private static final MutableComponent TEXT_UPKEEP = Component.translatable("gui.bannermod.command.text.upkeep");
    private static final MutableComponent TEXT_REST = Component.translatable("gui.bannermod.command.text.rest");
    private static final MutableComponent TEXT_TEAM = Component.translatable("gui.bannermod.command.text.team");
    private static final MutableComponent TOOLTIP_BACK_TO_MOUNT = Component.translatable("gui.bannermod.command.tooltip.backToMount");
    private static final MutableComponent TOOLTIP_UPKEEP = Component.translatable("gui.bannermod.command.tooltip.upkeep");
    private static final MutableComponent TOOLTIP_REST = Component.translatable("gui.bannermod.command.tooltip.rest");
    private static final MutableComponent TOOLTIP_TEAM = Component.translatable("gui.bannermod.command.tooltip.team");
    private static final MutableComponent TEXT_DISMOUNT = Component.translatable("gui.bannermod.command.text.dismount");
    private static final MutableComponent TEXT_MOUNT = Component.translatable("gui.bannermod.command.text.mount");
    private static final MutableComponent TOOLTIP_DISMOUNT = Component.translatable("gui.bannermod.command.tooltip.dismount");
    private static final MutableComponent TOOLTIP_MOUNT = Component.translatable("gui.bannermod.command.tooltip.mount");
    private static final MutableComponent TOOLTIP_PROTECT = Component.translatable("gui.bannermod.command.tooltip.protect");
    private static final MutableComponent TEXT_PROTECT = Component.translatable("gui.bannermod.command.text.protect");
    private static final MutableComponent TOOLTIP_OTHER = Component.translatable("gui.bannermod.command.tooltip.other");

    @Override
    public Component getToolTipName() {
        return TOOLTIP_OTHER;
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.CHEST);
    }

    @Override
    public void createButtons(CommandScreen screen, int x, int y, List<RecruitsGroup> groups, Player player) {
        boolean isOneGroupActive = groups.stream().anyMatch(g -> !g.isDisabled());

        //PROTECT
        RecruitsCommandButton protectButton = new RecruitsCommandButton(x, y - 25, TEXT_PROTECT,
                button -> {
                    if (screen.rayEntity != null && !groups.isEmpty()) {
                        for(RecruitsGroup group : groups) {
                            if (!group.isDisabled()) {
                                Main.SIMPLE_CHANNEL.sendToServer(new MessageProtectEntity(player.getUUID(), screen.rayEntity.getUUID(), group.getUUID()));
                                Main.SIMPLE_CHANNEL.sendToServer(new MessageMovement(player.getUUID(), 5, group.getUUID(), CommandScreen.formation.getIndex(), CommandScreen.tightFormation));
                            }
                        }
                        screen.sendCommandInChat(5);
                    }
                });
        protectButton.setTooltip(Tooltip.create(TOOLTIP_PROTECT));
        protectButton.active = isOneGroupActive && screen.rayEntity != null;
        screen.addRenderableWidget(protectButton);

        //MOUNT
        RecruitsCommandButton mountButton = new RecruitsCommandButton(x, y + 25, TEXT_MOUNT,
                button -> {
                    if (screen.rayEntity != null && !groups.isEmpty()) {
                        for (RecruitsGroup group : groups) {
                            if (!group.isDisabled()) {
                                Main.SIMPLE_CHANNEL.sendToServer(new MessageMountEntity(player.getUUID(), screen.rayEntity.getUUID(), group.getUUID()));
                            }
                        }
                        screen.sendCommandInChat(99);
                    }
                });
        mountButton.setTooltip(Tooltip.create(TOOLTIP_MOUNT));
        mountButton.active = isOneGroupActive && screen.rayEntity != null;
        screen.addRenderableWidget(mountButton);

        //TEAM
        RecruitsCommandButton factionButton = new RecruitsCommandButton(x - 60, y + 50, TEXT_TEAM,
                button -> {
                    screen.getMinecraft().setScreen(new FactionMainScreen(player));
                });
        factionButton.setTooltip(Tooltip.create(TOOLTIP_TEAM));
        screen.addRenderableWidget(factionButton);

        //BACK TO MOUNT
        RecruitsCommandButton backToMountButton = new RecruitsCommandButton(x + 100, y + 25, TEXT_BACK_TO_MOUNT,
                button -> {
                    if (!groups.isEmpty()) {
                        for (RecruitsGroup group : groups) {
                            if (!group.isDisabled()) {
                                Main.SIMPLE_CHANNEL.sendToServer(new MessageBackToMountEntity(player.getUUID(), group.getUUID()));
                            }
                        }
                        screen.sendCommandInChat(91);
                    }
                });
        backToMountButton.setTooltip(Tooltip.create(TOOLTIP_BACK_TO_MOUNT));
        backToMountButton.active = isOneGroupActive;
        screen.addRenderableWidget(backToMountButton);

        //DISMOUNT
        RecruitsCommandButton dismountButton = new RecruitsCommandButton(x - 100, y + 25, TEXT_DISMOUNT,
                button -> {
                    if (!groups.isEmpty()) {
                        for (RecruitsGroup group : groups) {
                            if (!group.isDisabled()) {
                                Main.SIMPLE_CHANNEL.sendToServer(new MessageDismount(player.getUUID(), group.getUUID()));
                            }
                        }
                        screen.sendCommandInChat(98);
                    }
                });
        dismountButton.setTooltip(Tooltip.create(TOOLTIP_DISMOUNT));
        dismountButton.active = isOneGroupActive;
        screen.addRenderableWidget(dismountButton);

        //UPKEEP
        RecruitsCommandButton upkeepButton = new RecruitsCommandButton(x + 100, y, TEXT_UPKEEP,
                button -> {
                    if (!groups.isEmpty()) {
                        for (RecruitsGroup group : groups) {
                            if (!group.isDisabled() && screen.rayEntity != null) {
                                Main.SIMPLE_CHANNEL.sendToServer(new MessageUpkeepEntity(player.getUUID(), screen.rayEntity.getUUID(), group.getUUID()));
                            } else if (!group.isDisabled() && screen.rayBlockPos != null)
                                Main.SIMPLE_CHANNEL.sendToServer(new MessageUpkeepPos(player.getUUID(), group.getUUID(), screen.rayBlockPos));
                        }
                        screen.sendCommandInChat(92);
                    }
                });
        upkeepButton.setTooltip(Tooltip.create(TOOLTIP_UPKEEP));
        upkeepButton.active = isOneGroupActive && (isUpkeepPosition(screen.rayBlockPos, player)|| isUpkeepEntity(screen.rayEntity));
        screen.addRenderableWidget(upkeepButton);

        //Clear Upkeep
        RecruitsCommandButton clearUpkeepButton = new RecruitsCommandButton(x + 60, y + 50, TEXT_CLEAR_UPKEEP,
                button -> {
                    if (!groups.isEmpty()) {
                        for (RecruitsGroup group : groups) {
                            if (!group.isDisabled()) {
                                Main.SIMPLE_CHANNEL.sendToServer(new MessageClearUpkeep(player.getUUID(), group.getUUID()));
                            }
                        }
                        screen.sendCommandInChat(93);
                    }
                });
        clearUpkeepButton.setTooltip(Tooltip.create(TOOLTIP_CLEAR_UPKEEP));
        clearUpkeepButton.active = isOneGroupActive;
        screen.addRenderableWidget(clearUpkeepButton);

        //REST
        RecruitsCommandButton restButton = new RecruitsCommandButton(x - 100, y, TEXT_REST,
                button -> {
                    if (!groups.isEmpty()) {
                        for (RecruitsGroup group : groups) {
                            if (!group.isDisabled()) {
                                Main.SIMPLE_CHANNEL.sendToServer(new MessageRest(player.getUUID(), group.getUUID(), true));
                            }
                        }
                        screen.sendCommandInChat(88);
                    }
                });
        restButton.setTooltip(Tooltip.create(TOOLTIP_REST));
        restButton.active = isOneGroupActive;
        screen.addRenderableWidget(restButton);
    }


    private boolean isUpkeepPosition(BlockPos rayBlockPos, Player player) {
        return BannerModUpkeepProviders.isValidBlockTarget(player.getCommandSenderWorld(), rayBlockPos);
    }

    private boolean isUpkeepEntity(Entity rayEntity) {
        return BannerModUpkeepProviders.isValidEntityTarget(rayEntity);
    }
}
