package com.talhanation.bannermod.client.civilian.gui;

import com.talhanation.bannermod.client.military.gui.CommandScreen;
import com.talhanation.bannermod.client.military.gui.commandscreen.ICommandCategory;
import com.talhanation.bannermod.client.military.gui.group.RecruitsCommandButton;
import com.talhanation.bannermod.persistence.military.RecruitsGroup;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.network.messages.civilian.MessageRecoverWorkerControl;
import net.minecraft.client.gui.components.Tooltip;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class WorkerCommandScreen implements ICommandCategory {

    private static final MutableComponent TEXT_RECOVER_CONTROL = Component.translatable("gui.workers.command.text.recover_control");
    private static final MutableComponent TOOLTIP_RECOVER_CONTROL = Component.translatable("gui.workers.command.tooltip.recover_control");

    @Override
    public Component getToolTipName() {
        return Component.translatable("gui.workers.command.tooltip.workers");
    }

    @Override
    public ItemStack getIcon() {
        return new ItemStack(Items.IRON_PICKAXE);
    }

    @Override
    public void createButtons(CommandScreen screen, int x, int y, List<RecruitsGroup> groups, Player player) {
        boolean isOneGroupActive = groups.stream().anyMatch(g -> !g.isDisabled());

        RecruitsCommandButton recoverControl = new RecruitsCommandButton(x, y, TEXT_RECOVER_CONTROL,
                button -> {
                    List<UUID> members = getActiveWorkerMembers(groups);
                    if (members.isEmpty()) return;
                    BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageRecoverWorkerControl(members));
                });

        recoverControl.setTooltip(Tooltip.create(TOOLTIP_RECOVER_CONTROL));
        recoverControl.active = isOneGroupActive;
        screen.addRenderableWidget(recoverControl);
    }

    private List<UUID> getActiveWorkerMembers(List<RecruitsGroup> groups) {
        List<UUID> members = new ArrayList<>();
        for (RecruitsGroup group : groups) {
            if (group.isDisabled()) continue;
            members.addAll(group.members);
        }
        return members;
    }
}
