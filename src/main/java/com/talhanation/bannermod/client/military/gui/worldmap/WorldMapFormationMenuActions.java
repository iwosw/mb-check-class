package com.talhanation.bannermod.client.military.gui.worldmap;

import com.talhanation.bannermod.army.map.FormationMapContact;
import com.talhanation.bannermod.army.map.FormationMapRelation;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.network.messages.military.MessageCommandScreen;
import com.talhanation.bannermod.network.messages.military.MessageFormationMapEngage;
import com.talhanation.bannermod.network.messages.military.MessageFormationMapMoveOrder;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;

import javax.annotation.Nullable;

final class WorldMapFormationMenuActions {
    private static final Component TEXT_OPEN_COMMAND_SCREEN = Component.translatable("gui.bannermod.worldmap.formation.open_command_screen");
    private static final Component TEXT_ADVANCE_IN_FORMATION = Component.translatable("gui.bannermod.worldmap.formation.advance_in_formation");
    private static final Component TEXT_FREE_CHARGE = Component.translatable("gui.bannermod.worldmap.formation.free_charge");
    private static final Component TEXT_OPEN_FIRE = Component.translatable("gui.bannermod.worldmap.formation.open_fire");
    private static final Component TEXT_MOVE_HERE = Component.translatable("gui.bannermod.worldmap.formation.move_here");
    private static final Component TEXT_ORDER_PENDING = Component.translatable("gui.bannermod.worldmap.order.pending");

    private WorldMapFormationMenuActions() {
    }

    static void addEntries(WorldMapContextMenu menu,
                           WorldMapScreen screen,
                           @Nullable FormationMapContact ownSelection,
                           @Nullable FormationMapContact clickedContact) {
        if (ownSelection == null || ownSelection.relation() != FormationMapRelation.SUBORDINATE) {
            return;
        }

        boolean clickedOnHostile = clickedContact != null && clickedContact.relation() == FormationMapRelation.HOSTILE;
        boolean clickedOnSelf = clickedContact != null && clickedContact.contactId().equals(ownSelection.contactId());
        boolean clickedOnFriendly = clickedContact != null
                && clickedContact.relation() != FormationMapRelation.HOSTILE
                && !clickedOnSelf;

        if (clickedOnSelf) {
            menu.addEntry(TEXT_OPEN_COMMAND_SCREEN, () -> true, s ->
                    BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageCommandScreen(s.getPlayer())), "formation_self");
            return;
        }

        if (clickedOnFriendly) {
            return;
        }

        if (clickedOnHostile) {
            BlockPos enemyPos = BlockPos.containing(clickedContact.x(), clickedContact.y(), clickedContact.z());
            menu.addEntry(TEXT_ADVANCE_IN_FORMATION, () -> true, s -> {
                BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageFormationMapEngage(
                        ownSelection.contactId(),
                        ownSelection.groupId(),
                        clickedContact.contactId(),
                        enemyPos,
                        MessageFormationMapEngage.Mode.ADVANCE_IN_FORMATION));
                WorldMapMoveOrderMarker.trigger(
                        ownSelection.x(), ownSelection.z(),
                        clickedContact.x(), clickedContact.z(),
                        Component.translatable("gui.bannermod.worldmap.order.advance.sent"),
                        TEXT_ORDER_PENDING);
            }, "formation_advance");
            menu.addEntry(TEXT_FREE_CHARGE, () -> true, s -> {
                BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageFormationMapEngage(
                        ownSelection.contactId(),
                        ownSelection.groupId(),
                        clickedContact.contactId(),
                        enemyPos,
                        MessageFormationMapEngage.Mode.FREE_CHARGE));
                WorldMapMoveOrderMarker.trigger(
                        ownSelection.x(), ownSelection.z(),
                        clickedContact.x(), clickedContact.z(),
                        Component.translatable("gui.bannermod.worldmap.order.charge.sent"),
                        TEXT_ORDER_PENDING);
            }, "formation_charge");
            if (ownSelection.rangedUnitCount() > 0) {
                menu.addEntry(TEXT_OPEN_FIRE, () -> true, s -> {
                    BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageFormationMapEngage(
                            ownSelection.contactId(),
                            ownSelection.groupId(),
                            clickedContact.contactId(),
                            enemyPos,
                            MessageFormationMapEngage.Mode.OPEN_FIRE));
                    WorldMapMoveOrderMarker.trigger(
                            ownSelection.x(), ownSelection.z(),
                            clickedContact.x(), clickedContact.z(),
                            Component.translatable("gui.bannermod.worldmap.order.fire.sent"),
                            TEXT_ORDER_PENDING);
                }, "formation_fire");
            }
            return;
        }

        BlockPos clicked = screen.getClickedBlockPos();
        menu.addEntry(TEXT_MOVE_HERE, () -> true, s -> {
            BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageFormationMapMoveOrder(
                    ownSelection.contactId(),
                    ownSelection.groupId(),
                    clicked));
            WorldMapMoveOrderMarker.trigger(
                    ownSelection.x(), ownSelection.z(),
                    clicked.getX() + 0.5D, clicked.getZ() + 0.5D,
                    Component.translatable("gui.bannermod.worldmap.order.move.sent"),
                    TEXT_ORDER_PENDING);
        }, "formation_move");
    }
}
