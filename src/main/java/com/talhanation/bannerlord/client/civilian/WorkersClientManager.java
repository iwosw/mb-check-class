package com.talhanation.bannerlord.client.civilian;

import com.talhanation.bannerlord.shared.settlement.BannerModSettlementBinding;
import com.talhanation.bannerlord.client.shared.ClientManager;
import net.minecraft.core.BlockPos;

public class WorkersClientManager {

    public static boolean configValueWorkAreaOnlyInFactionClaim;

    public static boolean isInFactionClaim(BlockPos pos){
        if (!configValueWorkAreaOnlyInFactionClaim) return true;

        String factionId = ClientManager.ownFaction == null ? null : ClientManager.ownFaction.getStringID();
        return BannerModSettlementBinding.resolveFactionStatus(ClientManager.recruitsClaims, pos, factionId).isFriendly();
    }
}
