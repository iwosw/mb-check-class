package com.talhanation.bannermod.society;

import net.minecraft.network.FriendlyByteBuf;

import javax.annotation.Nullable;
import java.util.Locale;

public record NpcMemorySummarySnapshot(
        String typeTag,
        String scopeTag,
        @Nullable String actorLabel,
        int intensity,
        boolean positive
) {
    public void toBytes(FriendlyByteBuf buf) {
        buf.writeUtf(safeTag(this.typeTag));
        buf.writeUtf(safeTag(this.scopeTag));
        buf.writeBoolean(this.actorLabel != null && !this.actorLabel.isBlank());
        if (this.actorLabel != null && !this.actorLabel.isBlank()) {
            buf.writeUtf(this.actorLabel);
        }
        buf.writeVarInt(Math.max(0, this.intensity));
        buf.writeBoolean(this.positive);
    }

    public static NpcMemorySummarySnapshot fromBytes(FriendlyByteBuf buf) {
        return new NpcMemorySummarySnapshot(
                buf.readUtf(),
                buf.readUtf(),
                buf.readBoolean() ? buf.readUtf() : null,
                buf.readVarInt(),
                buf.readBoolean()
        );
    }

    public String typeTranslationKey() {
        return "gui.bannermod.society.memory.type." + safeTag(this.typeTag).toLowerCase(Locale.ROOT);
    }

    public String scopeTranslationKey() {
        return "gui.bannermod.society.memory.scope." + safeTag(this.scopeTag).toLowerCase(Locale.ROOT);
    }

    public String actorLabelOrDash() {
        return this.actorLabel == null || this.actorLabel.isBlank() ? "-" : this.actorLabel;
    }

    private static String safeTag(@Nullable String value) {
        return value == null || value.isBlank() ? "UNSPECIFIED" : value;
    }
}
