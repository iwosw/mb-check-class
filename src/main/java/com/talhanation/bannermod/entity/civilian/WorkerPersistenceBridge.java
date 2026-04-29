package com.talhanation.bannermod.entity.civilian;

import com.talhanation.bannermod.citizen.CitizenCore;
import com.talhanation.bannermod.citizen.CitizenPersistenceBridge;
import com.talhanation.bannermod.citizen.CitizenRole;
import net.minecraft.nbt.CompoundTag;

final class WorkerPersistenceBridge {
    private WorkerPersistenceBridge() {
    }

    static void writeWorkerData(AbstractWorkerEntity worker, CompoundTag nbt) {
        nbt.putInt("farmedItems", worker.farmedItems);
        if (worker.lastStorage != null) {
            nbt.putUUID("lastStorage", worker.lastStorage);
        }
        nbt.putString("CitizenRole", CitizenRole.CONTROLLED_WORKER.name());
        persistCitizenStateToLegacy(worker.getCitizenCore(), nbt);
    }

    static void readWorkerData(AbstractWorkerEntity worker, CompoundTag nbt) {
        worker.farmedItems = nbt.getInt("farmedItems");
        if (nbt.contains("lastStorage")) {
            worker.lastStorage = nbt.getUUID("lastStorage");
        }
        hydrateCitizenStateFromLegacy(worker.getCitizenCore(), nbt);
    }

    static void hydrateCitizenStateFromLegacy(CitizenCore citizenCore, CompoundTag nbt) {
        citizenCore.apply(CitizenPersistenceBridge.fromWorkerLegacy(nbt));
    }

    static CompoundTag persistCitizenStateToLegacy(CitizenCore citizenCore, CompoundTag nbt) {
        return CitizenPersistenceBridge.writeWorkerLegacy(citizenCore.snapshot(), nbt);
    }
}
