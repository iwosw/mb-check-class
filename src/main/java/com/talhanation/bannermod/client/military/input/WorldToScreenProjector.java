package com.talhanation.bannermod.client.military.input;

import com.mojang.blaze3d.platform.Window;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.phys.Vec3;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;

/**
 * Cheap world-space → screen-space projector used by the drag-box selection code.
 *
 * <p>Reconstructs the view matrix from the current {@link Camera}'s yaw + pitch and
 * combines it with a perspective projection built from the client's FOV and viewport
 * aspect ratio. Only the results needed by selection are returned — screen pixel
 * coordinates and a "behind camera" flag.</p>
 *
 * <p>Not suitable for HUD-perfect placement; good enough for "did this entity roughly
 * fall inside the 2D selection rectangle?".</p>
 */
@OnlyIn(Dist.CLIENT)
public final class WorldToScreenProjector {
    private WorldToScreenProjector() {
    }

    /**
     * Result of projecting a single world-space point to screen coordinates. {@link #visible}
     * is false when the point is behind the camera or too close to the clip plane.
     */
    public record Projection(double screenX, double screenY, boolean visible) {
    }

    @Nullable
    public static Projection project(Vec3 worldPos) {
        Minecraft mc = Minecraft.getInstance();
        if (mc == null || mc.gameRenderer == null || mc.gameRenderer.getMainCamera() == null) {
            return null;
        }
        Camera camera = mc.gameRenderer.getMainCamera();
        if (!camera.isInitialized()) {
            return null;
        }
        Window window = mc.getWindow();
        if (window == null) {
            return null;
        }
        double fov = mc.options.fov().get();

        Vec3 delta = worldPos.subtract(camera.getPosition());

        // Minecraft's camera looks toward -Z in view space after applying yaw+pitch.
        // Rotate world delta by -yaw (around Y) then -pitch (around X) to get view-space.
        double yaw = -Mth.DEG_TO_RAD * camera.getYRot();
        double pitch = -Mth.DEG_TO_RAD * camera.getXRot();

        double cosY = Math.cos(yaw);
        double sinY = Math.sin(yaw);
        // Yaw rotation around Y
        double vx = delta.x * cosY + delta.z * sinY;
        double vz = -delta.x * sinY + delta.z * cosY;
        double vy = delta.y;

        double cosP = Math.cos(pitch);
        double sinP = Math.sin(pitch);
        // Pitch rotation around X
        double vy2 = vy * cosP - vz * sinP;
        double vz2 = vy * sinP + vz * cosP;

        // Minecraft looks down +Z after the above transform family in Forge's convention —
        // the player's forward is actually -Z in view space, so we invert for perspective.
        double viewZ = -vz2;

        if (viewZ < 0.1) {
            return new Projection(0, 0, false);
        }

        double aspect = (double) window.getWidth() / Math.max(1, window.getHeight());
        double f = 1.0 / Math.tan(Math.toRadians(fov) * 0.5);

        double ndcX = (vx / viewZ) * (f / aspect);
        double ndcY = (vy2 / viewZ) * f;

        double guiWidth = window.getGuiScaledWidth();
        double guiHeight = window.getGuiScaledHeight();
        double screenX = (ndcX + 1.0) * 0.5 * guiWidth;
        double screenY = (1.0 - ndcY) * 0.5 * guiHeight;

        return new Projection(screenX, screenY, true);
    }
}
