package com.talhanation.bannermod.network.messages.civilian;

import com.talhanation.bannermod.events.ClaimEvents;
import com.talhanation.bannermod.persistence.military.RecruitsClaim;
import com.talhanation.bannermod.config.WorkersServerConfig;
import com.talhanation.bannermod.entity.civilian.workarea.*;
import com.talhanation.bannermod.registry.civilian.ModEntityTypes;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.network.NetworkEvent;

public class MessageAddWorkArea implements Message<MessageAddWorkArea> {
    public BlockPos pos;
    public int type;
    public MessageAddWorkArea() {

    }

    public MessageAddWorkArea(BlockPos pos, int type) {
        this.pos = pos;
        this.type = type;
    }

    public Dist getExecutingSide() {
        return Dist.DEDICATED_SERVER;
    }

    public void executeServerSide(NetworkEvent.Context context){
        ServerPlayer player = context.getSender();
        if(player == null) return;
        // Buildings-first migration: legacy packet keeps supporting raw BuildArea only.
        // All profession surfaces should be created through prefab placement.
        if (this.type != 2) {
            this.sendDecision(player, WorkAreaAuthoringRules.Decision.INVALID_REQUEST);
            return;
        }

        AbstractWorkAreaEntity workArea = this.createWorkArea(player);
        if (workArea == null) {
            this.sendDecision(player, WorkAreaAuthoringRules.Decision.INVALID_REQUEST);
            return;
        }

        String teamStringID = "";
        if(player.getTeam() != null){
            teamStringID = player.getTeam().getName();
        }

        workArea.setFacing(player.getDirection());
        workArea.moveTo(pos.above(), 0, 0);
        workArea.createArea();
        workArea.setTeamStringID(teamStringID);
        workArea.setDone(false);
        workArea.setPlayerName(player.getName().getString());
        workArea.setPlayerUUID(player.getUUID());
        workArea.setCustomName(Component.literal(""));

        WorkAreaAuthoringRules.Decision decision = WorkAreaAuthoringRules.createDecision(this.isInsideOwnFactionClaim(player, pos),
                AbstractWorkAreaEntity.isAreaOverlapping(player.level(), null, workArea.getArea()));
        if (!WorkAreaAuthoringRules.isAllowed(decision)) {
            this.sendDecision(player, decision);
            return;
        }

        player.level().addFreshEntity(workArea);
        if (player.level() instanceof ServerLevel serverLevel) {
            WorkAreaMessageSupport.refreshSettlementSnapshot(serverLevel, workArea.blockPosition());
        }
    }

    private AbstractWorkAreaEntity createWorkArea(ServerPlayer player) {
        return switch (type) {
            case 0 -> {
                CropArea workArea = new CropArea(ModEntityTypes.CROPAREA.get(), player.level());
                workArea.setWidthSize(9);
                workArea.setHeightSize(2);
                workArea.setDepthSize(9);
                yield workArea;
            }
            case 1 -> {
                LumberArea workArea = new LumberArea(ModEntityTypes.LUMBERAREA.get(), player.level());
                workArea.setWidthSize(16);
                workArea.setHeightSize(12);
                workArea.setDepthSize(16);
                yield workArea;
            }
            case 2 -> {
                BuildArea workArea = new BuildArea(ModEntityTypes.BUILDAREA.get(), player.level());
                workArea.setWidthSize(4);
                workArea.setHeightSize(4);
                workArea.setDepthSize(4);
                yield workArea;
            }
            case 3 -> {
                MiningArea workArea = new MiningArea(ModEntityTypes.MININGAREA.get(), player.level());
                workArea.setWidthSize(8);
                workArea.setHeightSize(4);
                workArea.setDepthSize(8);
                yield workArea;
            }
            case 4 -> {
                StorageArea workArea = new StorageArea(ModEntityTypes.STORAGEAREA.get(), player.level());
                workArea.setWidthSize(5);
                workArea.setHeightSize(5);
                workArea.setDepthSize(5);
                yield workArea;
            }
            case 5 -> {
                FishingArea workArea = new FishingArea(ModEntityTypes.FISHINGAREA.get(), player.level());
                workArea.setWidthSize(9);
                workArea.setHeightSize(2);
                workArea.setDepthSize(9);
                yield workArea;
            }
            case 6 -> {
                AnimalPenArea workArea = new AnimalPenArea(ModEntityTypes.ANIMAL_PEN_AREA.get(), player.level());
                workArea.setWidthSize(12);
                workArea.setHeightSize(4);
                workArea.setDepthSize(12);
                yield workArea;
            }
            case 7 -> {
                MarketArea workArea = new MarketArea(ModEntityTypes.MARKETAREA.get(), player.level());
                workArea.setWidthSize(4);
                workArea.setHeightSize(8);
                workArea.setDepthSize(4);
                yield workArea;
            }
            default -> null;
        };
    }

    private boolean isInsideOwnFactionClaim(ServerPlayer player, BlockPos targetPos) {
        if (!WorkersServerConfig.ShouldWorkAreaOnlyBeInFactionClaim.get()) {
            return true;
        }
        if (player.getTeam() == null || ClaimEvents.recruitsClaimManager == null) {
            return false;
        }

        ChunkPos chunkPos = new ChunkPos(targetPos);
        RecruitsClaim claim = ClaimEvents.recruitsClaimManager.getClaim(chunkPos);
        return claim != null
                && claim.containsChunk(chunkPos)
                && claim.getOwnerFaction() != null
                && player.getTeam().getName().equals(claim.getOwnerFaction().getStringID());
    }

    private void sendDecision(ServerPlayer player, WorkAreaAuthoringRules.Decision decision) {
        String messageKey = WorkAreaAuthoringRules.getMessageKey(decision);
        if (messageKey != null) {
            player.sendSystemMessage(Component.translatable(messageKey));
        }
    }
    public MessageAddWorkArea fromBytes(FriendlyByteBuf buf) {
        this.pos = buf.readBlockPos();
        this.type = buf.readInt();

        return this;
    }

    public void toBytes(FriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(type);
    }

}
