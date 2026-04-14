package com.talhanation.bannermod.citizen;

import com.talhanation.bannerlord.entity.shared.AbstractRecruitEntity;
import com.talhanation.workers.entities.AbstractWorkerEntity;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Method;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CitizenRoleControllerTest {

    @Test
    void recruitWrapperExposesSharedCitizenCoreAndRoleMetadataAccessors() throws Exception {
        assertMethod(AbstractRecruitEntity.class, "getCitizenCore", CitizenCore.class);
        assertMethod(AbstractRecruitEntity.class, "getCitizenRole", CitizenRole.class);
        assertMethod(AbstractRecruitEntity.class, "getCitizenRoleController", CitizenRoleController.class);
        assertMethod(AbstractRecruitEntity.class, "setCitizenRoleController", void.class, CitizenRoleController.class);
    }

    @Test
    void workerWrapperExposesSharedCitizenCoreAndRoleMetadataAccessors() throws Exception {
        assertMethod(AbstractWorkerEntity.class, "getCitizenCore", CitizenCore.class);
        assertMethod(AbstractWorkerEntity.class, "getCitizenRole", CitizenRole.class);
        assertMethod(AbstractWorkerEntity.class, "getCitizenRoleController", CitizenRoleController.class);
        assertMethod(AbstractWorkerEntity.class, "setCitizenRoleController", void.class, CitizenRoleController.class);
    }

    @Test
    void sharedRoleControllerHooksStayReusableAcrossRecruitAndWorkerRoles() {
        CitizenRoleController recruitController = CitizenRoleController.noop(CitizenRole.RECRUIT);
        CitizenRoleController workerController = CitizenRoleController.noop(CitizenRole.WORKER);

        assertEquals(CitizenRole.RECRUIT, recruitController.role());
        assertEquals(CitizenRole.WORKER, workerController.role());
        recruitController.onRecoveredControl(new CitizenRoleContext(CitizenRole.RECRUIT, new StubCitizenCore(), null, null, null));
        workerController.onBoundWorkAreaRemembered(new CitizenRoleContext(CitizenRole.WORKER, new StubCitizenCore(), null, null, null));
    }

    private static void assertMethod(Class<?> type, String name, Class<?> returnType, Class<?>... parameterTypes) throws Exception {
        Method method = type.getMethod(name, parameterTypes);
        assertEquals(returnType, method.getReturnType());
    }

    private static final class StubCitizenCore implements CitizenCore {
        @Override
        public java.util.UUID getOwnerUUID() {
            return null;
        }

        @Override
        public void setOwnerUUID(java.util.Optional<java.util.UUID> ownerUuid) {
        }

        @Override
        public int getFollowState() {
            return 0;
        }

        @Override
        public void setFollowState(int state) {
        }

        @Override
        public net.minecraft.world.SimpleContainer getInventory() {
            return new net.minecraft.world.SimpleContainer(0);
        }

        @Override
        public String getTeamId() {
            return null;
        }

        @Override
        public net.minecraft.world.phys.Vec3 getHoldPos() {
            return null;
        }

        @Override
        public void setHoldPos(net.minecraft.world.phys.Vec3 holdPos) {
        }

        @Override
        public void clearHoldPos() {
        }

        @Override
        public net.minecraft.core.BlockPos getMovePos() {
            return null;
        }

        @Override
        public void setMovePos(net.minecraft.core.BlockPos movePos) {
        }

        @Override
        public void clearMovePos() {
        }

        @Override
        public boolean isOwned() {
            return false;
        }

        @Override
        public void setOwned(boolean owned) {
        }

        @Override
        public boolean isWorking() {
            return false;
        }

        @Override
        public java.util.UUID getBoundWorkAreaUUID() {
            return null;
        }

        @Override
        public void setBoundWorkAreaUUID(java.util.UUID boundWorkAreaUuid) {
        }

        @Override
        public boolean getRuntimeFlag(RuntimeFlag flag) {
            return false;
        }

        @Override
        public void setRuntimeFlag(RuntimeFlag flag, boolean value) {
        }
    }
}
