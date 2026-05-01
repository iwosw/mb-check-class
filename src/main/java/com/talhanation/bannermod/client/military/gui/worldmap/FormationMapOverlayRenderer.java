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
    private static final int LABEL_FILL = 0xB8201810;
    private static final int LABEL_TEXT = 0xFFF4E6C2;

    private FormationMapOverlayRenderer() {
    }

    static void render(GuiGraphics graphics, double offsetX, double offsetZ, double scale, @Nullable UUID selectedContactId) {
        Minecraft minecraft = Minecraft.getInstance();
        for (FormationMapContact contact : ClientManager.formationMapContacts) {
            int x = worldToScreenX(contact.x(), offsetX, scale);
            int z = worldToScreenZ(contact.z(), offsetZ, scale);
            boolean selected = contact.contactId().equals(selectedContactId);
            int color = colorFor(contact.relation(), selected);
            int radius = selected ? 6 : 5;

            renderMarker(graphics, x, z, radius, color, selected);

            String label = relationGlyph(contact.relation()) + " " + contact.visibleUnitCount() + "/" + contact.unitCount();
            drawLabelChip(graphics, minecraft, label, x + radius + 6, z - 6, selected ? 0xFFE0B86A : 0xAA8A6A3A);
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
            case SUBORDINATE -> selected ? 0xFF7FB8FF : 0xDD4A88D6;
            case FRIENDLY -> selected ? 0xFF8FD58A : 0xDD5A9D56;
            case NEUTRAL -> selected ? 0xFFE3C57A : 0xDDAA8144;
            case HOSTILE -> selected ? 0xFFE07862 : 0xDD9F4334;
        };
    }

    private static String relationGlyph(FormationMapRelation relation) {
        return switch (relation) {
            case SUBORDINATE -> ">";
            case FRIENDLY -> "+";
            case NEUTRAL -> "~";
            case HOSTILE -> "!";
        };
    }

    private static void renderMarker(GuiGraphics graphics, int x, int z, int radius, int color, boolean selected) {
        graphics.fill(x - 1, z - radius, x + 2, z + radius + 1, 0xDD20150D);
        graphics.fill(x - radius, z - 1, x + radius + 1, z + 2, 0xDD20150D);
        graphics.fill(x - 1, z - radius + 1, x + 2, z + radius, color);
        graphics.fill(x - radius + 1, z - 1, x + radius, z + 2, color);
        graphics.fill(x - 2, z - 2, x + 3, z + 3, color);
        graphics.renderOutline(x - radius - 1, z - radius - 1, radius * 2 + 3, radius * 2 + 3, selected ? 0xFFE0B86A : 0xAA000000);
    }

    private static void drawLabelChip(GuiGraphics graphics, Minecraft minecraft, String label, int x, int y, int borderColor) {
        int width = minecraft.font.width(label) + 6;
        graphics.fill(x, y, x + width, y + 10, LABEL_FILL);
        graphics.renderOutline(x, y, width, 10, borderColor);
        graphics.drawString(minecraft.font, label, x + 3, y + 1, LABEL_TEXT, false);
    }
}
