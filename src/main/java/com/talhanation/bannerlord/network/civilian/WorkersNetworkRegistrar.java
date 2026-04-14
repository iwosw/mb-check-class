package com.talhanation.bannerlord.network.civilian;

import de.maxhenkel.corelib.CommonRegistry;
import net.minecraftforge.network.simple.SimpleChannel;

public class WorkersNetworkRegistrar {
    private static final Class<?>[] MESSAGE_TYPES = {
            com.talhanation.workers.network.MessageAddWorkArea.class,
            com.talhanation.workers.network.MessageToClientOpenWorkAreaScreen.class,
            com.talhanation.workers.network.MessageUpdateWorkArea.class,
            com.talhanation.workers.network.MessageUpdateCropArea.class,
            com.talhanation.workers.network.MessageUpdateLumberArea.class,
            com.talhanation.workers.network.MessageUpdateBuildArea.class,
            com.talhanation.workers.network.MessageUpdateMiningArea.class,
            com.talhanation.workers.network.MessageUpdateMerchantTrade.class,
            com.talhanation.workers.network.MessageUpdateMerchant.class,
            com.talhanation.workers.network.MessageDoTradeWithMerchant.class,
            com.talhanation.workers.network.MessageOpenMerchantEditTradeScreen.class,
            com.talhanation.workers.network.MessageOpenMerchantTradeScreen.class,
            com.talhanation.workers.network.MessageToClientUpdateConfig.class,
            com.talhanation.workers.network.MessageUpdateStorageArea.class,
            com.talhanation.workers.network.MessageUpdateAnimalPenArea.class,
            com.talhanation.workers.network.MessageRotateWorkArea.class,
            com.talhanation.workers.network.MessageMoveMerchantTrade.class,
            com.talhanation.workers.network.MessageUpdateMarketArea.class,
            com.talhanation.workers.network.MessageUpdateOwner.class,
            com.talhanation.workers.network.MessageRecoverWorkerControl.class
    };

    @SuppressWarnings({"rawtypes", "unchecked"})
    public void registerAll(SimpleChannel channel, int startId) {
        for (int i = 0; i < MESSAGE_TYPES.length; i++) {
            CommonRegistry.registerMessage(channel, startId + i, (Class) MESSAGE_TYPES[i]);
        }
    }

    public int messageCount() {
        return MESSAGE_TYPES.length;
    }
}
