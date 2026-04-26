package com.talhanation.bannermod.client.military.gui.worldmap;

import com.talhanation.bannermod.army.map.FormationMapContact;
import com.talhanation.bannermod.army.map.FormationMapRelation;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.network.messages.military.MessageCommandScreen;
import com.talhanation.bannermod.network.messages.military.MessageFormationMapEngage;
import com.talhanation.bannermod.network.messages.military.MessageFormationMapMoveOrder;
import net.minecraft.core.BlockPos;

import javax.annotation.Nullable;

final class WorldMapFormationMenuActions {

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
            menu.addEntry("Open command screen", () -> true, s ->
                    BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageCommandScreen(s.getPlayer())));
            return;
        }

        if (clickedOnFriendly) {
            return;
        }

        if (clickedOnHostile) {
            BlockPos enemyPos = BlockPos.containing(clickedContact.x(), clickedContact.y(), clickedContact.z());
            menu.addEntry("Advance in formation", () -> true, s -> {
                BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageFormationMapEngage(
                        ownSelection.contactId(),
                        ownSelection.groupId(),
                        clickedContact.contactId(),
                        enemyPos,
                        MessageFormationMapEngage.Mode.ADVANCE_IN_FORMATION));
                WorldMapMoveOrderMarker.trigger(
                        ownSelection.x(), ownSelection.z(),
                        clickedContact.x(), clickedContact.z());
            });
            menu.addEntry("Free charge", () -> true, s -> {
                BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageFormationMapEngage(
                        ownSelection.contactId(),
                        ownSelection.groupId(),
                        clickedContact.contactId(),
                        enemyPos,
                        MessageFormationMapEngage.Mode.FREE_CHARGE));
                WorldMapMoveOrderMarker.trigger(
                        ownSelection.x(), ownSelection.z(),
                        clickedContact.x(), clickedContact.z());
            });
            if (ownSelection.rangedUnitCount() > 0) {
                menu.addEntry("Open fire", () -> true, s -> {
                    BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageFormationMapEngage(
                            ownSelection.contactId(),
                            ownSelection.groupId(),
                            clickedContact.contactId(),
                            enemyPos,
                            MessageFormationMapEngage.Mode.OPEN_FIRE));
                    WorldMapMoveOrderMarker.trigger(
                            ownSelection.x(), ownSelection.z(),
                            clickedContact.x(), clickedContact.z());
                });
            }
            return;
        }

        BlockPos clicked = screen.getClickedBlockPos();
        menu.addEntry("Move here", () -> true, s -> {
            BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageFormationMapMoveOrder(
                    ownSelection.contactId(),
                    ownSelection.groupId(),
                    clicked));
            WorldMapMoveOrderMarker.trigger(
                    ownSelection.x(), ownSelection.z(),
                    clicked.getX() + 0.5D, clicked.getZ() + 0.5D);
        });
    }
}
