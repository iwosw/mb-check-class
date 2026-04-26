package com.talhanation.bannermod.client.military.gui.worldmap;

import com.talhanation.bannermod.army.map.FormationMapContact;
import com.talhanation.bannermod.army.map.FormationMapRelation;
import com.talhanation.bannermod.client.military.ClientManager;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;

import javax.annotation.Nullable;
import java.util.UUID;

final class FormationMapOverlayRenderer {
    private static final int HIT_RADIUS = 7;

    private FormationMapOverlayRenderer() {
    }

    static void render(GuiGraphics graphics, double offsetX, double offsetZ, double scale, @Nullable UUID selectedContactId) {
        Minecraft minecraft = Minecraft.getInstance();
        for (FormationMapContact contact : ClientManager.formationMapContacts) {
            int x = worldToScreenX(contact.x(), offsetX, scale);
            int z = worldToScreenZ(contact.z(), offsetZ, scale);
            boolean selected = contact.contactId().equals(selectedContactId);
            int color = colorFor(contact.relation(), selected);
            int radius = selected ? 6 : 4;

            graphics.fill(x - radius, z - radius, x + radius + 1, z + radius + 1, color);
            graphics.renderOutline(x - radius - 1, z - radius - 1, radius * 2 + 3, radius * 2 + 3, selected ? 0xFFFFFFFF : 0xAA000000);

            String label = contact.visibleUnitCount() + "/" + contact.unitCount();
            graphics.drawString(minecraft.font, label, x + radius + 3, z - 4, 0xFFE8E8E8, true);
        }
    }

    @Nullable
    static FormationMapContact contactAt(double mouseX, double mouseY, double offsetX, double offsetZ, double scale) {
        FormationMapContact nearest = null;
        double nearestDistance = Double.MAX_VALUE;
        for (FormationMapContact contact : ClientManager.formationMapContacts) {
            int x = worldToScreenX(contact.x(), offsetX, scale);
            int z = worldToScreenZ(contact.z(), offsetZ, scale);
            double dx = mouseX - x;
            double dz = mouseY - z;
            double distSqr = dx * dx + dz * dz;
            if (distSqr <= HIT_RADIUS * HIT_RADIUS && distSqr < nearestDistance) {
                nearest = contact;
                nearestDistance = distSqr;
            }
        }
        return nearest;
    }

    private static int worldToScreenX(double worldX, double offsetX, double scale) {
        return (int) Math.round(worldX * scale + offsetX);
    }

    private static int worldToScreenZ(double worldZ, double offsetZ, double scale) {
        return (int) Math.round(worldZ * scale + offsetZ);
    }

    private static int colorFor(FormationMapRelation relation, boolean selected) {
        return switch (relation) {
            case SUBORDINATE -> selected ? 0xFF55AAFF : 0xCC2277DD;
            case FRIENDLY -> 0xCC55DD55;
            case NEUTRAL -> 0xCCDDBB55;
            case HOSTILE -> 0xCCDD4444;
        };
    }
}
