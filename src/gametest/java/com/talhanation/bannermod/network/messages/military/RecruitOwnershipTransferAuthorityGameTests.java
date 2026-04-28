package com.talhanation.bannermod.network.messages.military;

import com.mojang.authlib.GameProfile;
import com.talhanation.bannermod.BannerModDedicatedServerGameTestSupport;
import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import com.talhanation.bannermod.gametest.support.RecruitsBattleGameTestSupport;
import com.talhanation.bannermod.registry.military.ModEntityTypes;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.common.util.FakePlayer;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;

import java.util.UUID;

@GameTestHolder(BannerModMain.MOD_ID)
public class RecruitOwnershipTransferAuthorityGameTests {
    private static final UUID OWNER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000831");
    private static final UUID OUTSIDER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000832");
    private static final UUID RECIPIENT_UUID = UUID.fromString("00000000-0000-0000-0000-000000000833");
    private static final UUID ADMIN_UUID = UUID.fromString("00000000-0000-0000-0000-000000000834");
    private static final UUID OFFLINE_OWNER_UUID = UUID.fromString("00000000-0000-0000-0000-000000000835");

    @PrefixGameTestTemplate(false)
    @GameTest(template = "harness_empty")
    public static void recruitOwnershipTransferRejectsSpoofedSenderAndAllowsAuthorizedSenders(GameTestHelper helper) {
        ServerLevel level = helper.getLevel();
        ServerPlayer owner = createPlayer(helper, level, OWNER_UUID, "transfer-owner");
        ServerPlayer outsider = createPlayer(helper, level, OUTSIDER_UUID, "transfer-outsider");
        createPlayer(helper, level, RECIPIENT_UUID, "transfer-recipient");
        ServerPlayer admin = createAdminPlayer(level, ADMIN_UUID, "transfer-admin");

        AbstractRecruitEntity outsiderTarget = spawnOwnedRecruit(helper, OWNER_UUID, "Denied Transfer Target");
        helper.assertFalse(MessageAssignRecruitToPlayer.assignRecruitToPlayer(outsider, outsiderTarget, RECIPIENT_UUID),
                "Expected spoofed transfer from non-controller to be denied");
        helper.assertTrue(OWNER_UUID.equals(outsiderTarget.getOwnerUUID()),
                "Expected denied transfer to leave ownership unchanged");

        AbstractRecruitEntity ownerTarget = spawnOwnedRecruit(helper, OWNER_UUID, "Owner Transfer Target");
        helper.assertTrue(MessageAssignRecruitToPlayer.assignRecruitToPlayer(owner, ownerTarget, RECIPIENT_UUID),
                "Expected current owner to transfer recruit ownership");
        helper.assertTrue(RECIPIENT_UUID.equals(ownerTarget.getOwnerUUID()),
                "Expected owner-authorized transfer to assign the requested recipient");

        AbstractRecruitEntity adminTarget = spawnOwnedRecruit(helper, OFFLINE_OWNER_UUID, "Admin Transfer Target");
        helper.assertTrue(MessageAssignRecruitToPlayer.assignRecruitToPlayer(admin, adminTarget, RECIPIENT_UUID),
                "Expected operator override to transfer recruit ownership");
        helper.assertTrue(RECIPIENT_UUID.equals(adminTarget.getOwnerUUID()),
                "Expected admin-authorized transfer to assign the requested recipient");

        helper.succeed();
    }

    private static ServerPlayer createPlayer(GameTestHelper helper, ServerLevel level, UUID playerId, String name) {
        ServerPlayer player = (ServerPlayer) BannerModDedicatedServerGameTestSupport.createFakeServerPlayer(level, playerId, name);
        BlockPos pos = helper.absolutePos(RecruitsBattleGameTestSupport.SquadAnchor.WEST.anchor());
        player.moveTo(pos.getX() + 0.5D, pos.getY(), pos.getZ() + 0.5D, -90.0F, 0.0F);
        return player;
    }

    private static ServerPlayer createAdminPlayer(ServerLevel level, UUID playerId, String name) {
        return new FakePlayer(level, new GameProfile(playerId, name)) {
            @Override
            public boolean hasPermissions(int permissionLevel) {
                return true;
            }
        };
    }

    private static AbstractRecruitEntity spawnOwnedRecruit(GameTestHelper helper, UUID ownerId, String name) {
        return RecruitsBattleGameTestSupport.spawnConfiguredRecruit(
                helper,
                ModEntityTypes.RECRUIT.get(),
                RecruitsBattleGameTestSupport.SquadAnchor.WEST.recoveryLeftPos(),
                name,
                ownerId
        );
    }
}
