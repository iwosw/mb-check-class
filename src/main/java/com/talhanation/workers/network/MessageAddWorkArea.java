package com.talhanation.workers.network;

import com.talhanation.bannerlord.shared.settlement.BannerModSettlementBinding;
import com.talhanation.recruits.ClaimEvents;
import com.talhanation.workers.config.WorkersServerConfig;
import com.talhanation.bannerlord.entity.civilian.workarea.*;
import com.talhanation.workers.init.ModEntityTypes;
import de.maxhenkel.corelib.net.Message;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
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
        this.executeForPlayer(context.getSender());
    }

    public boolean executeForPlayer(ServerPlayer player) {
        if(player == null) return false;

        AbstractWorkAreaEntity workArea = this.createWorkArea(player);
        if (workArea == null) {
            this.sendDecision(player, WorkAreaAuthoringRules.Decision.INVALID_REQUEST);
            return false;
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

        WorkAreaAuthoringRules.Decision decision = WorkAreaAuthoringRules.createDecision(this.isInsideOwnFactionClaim(player),
                AbstractWorkAreaEntity.isAreaOverlapping(player.level(), null, workArea.getArea()));
        if (!WorkAreaAuthoringRules.isAllowed(decision)) {
            this.sendDecision(player, decision);
            return false;
        }

        player.level().addFreshEntity(workArea);
        return true;
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

    private boolean isInsideOwnFactionClaim(ServerPlayer player) {
        String factionId = player.getTeam() == null ? null : player.getTeam().getName();
        return BannerModSettlementBinding.allowsWorkAreaPlacement(
                ClaimEvents.recruitsClaimManager,
                pos,
                factionId,
                WorkersServerConfig.ShouldWorkAreaOnlyBeInFactionClaim.get()
        );
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
