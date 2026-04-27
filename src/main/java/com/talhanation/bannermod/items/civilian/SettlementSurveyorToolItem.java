package com.talhanation.bannermod.items.civilian;

import com.talhanation.bannermod.settlement.building.ZoneRole;
import com.talhanation.bannermod.settlement.validation.SettlementSurveyorService;
import com.talhanation.bannermod.settlement.validation.SurveyorMode;
import com.talhanation.bannermod.settlement.validation.SurveyorSessionCodec;
import com.talhanation.bannermod.settlement.validation.ValidationSession;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;

import javax.annotation.Nullable;
import java.util.List;

public class SettlementSurveyorToolItem extends Item {
    private static final String TAG_PENDING_CORNER = "bannermod:settlement_survey_pending_corner";
    private static final String TAG_SELECTED_ROLE = "bannermod:settlement_survey_selected_role";

    public SettlementSurveyorToolItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, net.minecraft.world.InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (!player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                ValidationSession session = SurveyorSessionCodec.read(stack);
                if (session == null) {
                    player.sendSystemMessage(Component.literal("No survey session. Mark an anchor first.").withStyle(ChatFormatting.RED));
                    return InteractionResultHolder.sidedSuccess(stack, false);
                }
                if (player instanceof net.minecraft.server.level.ServerPlayer serverPlayer) {
                    SettlementSurveyorService.validateCurrentSession(serverPlayer, session);
                }
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }
        if (!level.isClientSide) {
            ValidationSession session = getOrCreateSession(player, stack);
            SurveyorMode nextMode = nextMode(session.mode());
            SurveyorSessionCodec.write(stack, session.withMode(nextMode));
            player.sendSystemMessage(Component.literal("Surveyor mode: " + nextMode.name()).withStyle(ChatFormatting.GOLD));
        }
        return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
    }

    @Override
    public InteractionResult useOn(UseOnContext context) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        ItemStack stack = context.getItemInHand();
        BlockPos clicked = context.getClickedPos();

        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                ZoneRole nextRole = cycleSelectedRole(stack);
                player.sendSystemMessage(Component.literal("Survey marker role: " + nextRole.name()).withStyle(ChatFormatting.YELLOW));
            }
            return InteractionResult.SUCCESS;
        }

        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        ValidationSession session = getOrCreateSession(player, stack);
        if (session.anchorPos().equals(BlockPos.ZERO)) {
            SurveyorSessionCodec.write(stack, session.withAnchor(clicked));
            player.sendSystemMessage(Component.literal("Survey anchor set: " + clicked.toShortString()).withStyle(ChatFormatting.AQUA));
            return InteractionResult.SUCCESS;
        }

        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(TAG_PENDING_CORNER)) {
            tag.putLong(TAG_PENDING_CORNER, clicked.asLong());
            player.sendSystemMessage(Component.literal("Zone corner A set: " + clicked.toShortString()).withStyle(ChatFormatting.AQUA));
            return InteractionResult.SUCCESS;
        }

        BlockPos cornerA = BlockPos.of(tag.getLong(TAG_PENDING_CORNER));
        tag.remove(TAG_PENDING_CORNER);
        ZoneRole role = selectedRole(stack);
        ValidationSession updated = session.upsertSelection(role, cornerA, clicked, clicked);
        SurveyorSessionCodec.write(stack, updated);
        player.sendSystemMessage(Component.literal("Zone " + role.name() + " captured.").withStyle(ChatFormatting.GREEN));
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, @Nullable Level level, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, level, tooltip, flag);
        ValidationSession session = SurveyorSessionCodec.read(stack);
        SurveyorMode mode = session == null ? SurveyorMode.BOOTSTRAP_FORT : session.mode();
        tooltip.add(Component.translatable("bannermod.surveyor.tooltip.mode", mode.name())
                .withStyle(ChatFormatting.GRAY));
        tooltip.add(Component.translatable("bannermod.surveyor.tooltip.use")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("bannermod.surveyor.tooltip.shift")
                .withStyle(ChatFormatting.DARK_GRAY));
    }

    private static ValidationSession getOrCreateSession(Player player, ItemStack stack) {
        ValidationSession existing = SurveyorSessionCodec.read(stack);
        if (existing != null) {
            return existing;
        }
        ValidationSession created = new ValidationSession(player.getUUID(), SurveyorMode.BOOTSTRAP_FORT, BlockPos.ZERO, java.util.List.of());
        SurveyorSessionCodec.write(stack, created);
        return created;
    }

    private static SurveyorMode nextMode(SurveyorMode mode) {
        SurveyorMode[] modes = SurveyorMode.values();
        int idx = mode.ordinal();
        return modes[(idx + 1) % modes.length];
    }

    private static ZoneRole cycleSelectedRole(ItemStack stack) {
        ZoneRole[] roles = ZoneRole.values();
        ZoneRole current = selectedRole(stack);
        ZoneRole next = roles[(current.ordinal() + 1) % roles.length];
        stack.getOrCreateTag().putString(TAG_SELECTED_ROLE, next.name());
        return next;
    }

    private static ZoneRole selectedRole(ItemStack stack) {
        CompoundTag tag = stack.getOrCreateTag();
        if (!tag.contains(TAG_SELECTED_ROLE)) {
            return ZoneRole.INTERIOR;
        }
        try {
            return ZoneRole.valueOf(tag.getString(TAG_SELECTED_ROLE));
        } catch (IllegalArgumentException ex) {
            return ZoneRole.INTERIOR;
        }
    }
}
