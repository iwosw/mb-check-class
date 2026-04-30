package com.talhanation.bannermod.items.civilian;

import com.talhanation.bannermod.bootstrap.BannerModMain;
import com.talhanation.bannermod.client.civilian.gui.PlaceBuildingScreen;
import com.talhanation.bannermod.network.messages.civilian.MessageRequestPlaceBuilding;
import com.talhanation.bannermod.network.messages.civilian.MessageRequestRegisterBuilding;
import com.talhanation.bannermod.network.messages.civilian.MessageRequestValidateBuilding;
import com.talhanation.bannermod.util.ItemStackComponentData;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.context.UseOnContext;
import net.minecraft.world.level.Level;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.api.distmarker.OnlyIn;

import javax.annotation.Nullable;
import java.util.List;

/**
 * Dual-mode wand: place prefab buildings OR validate player-built ones.
 *
 * <p>Controls:</p>
 * <ul>
 *   <li>Right-click in air → open the {@link PlaceBuildingScreen} to pick a prefab. The
 *       selected id is stored under {@value #TAG_SELECTED_PREFAB}.</li>
 *   <li>Shift + right-click in air → toggle between {@link #MODE_PLACE} and
 *       {@link #MODE_VALIDATE} modes.</li>
 *   <li>Place mode + right-click on a block → request server placement.</li>
 *   <li>Validate mode + right-click on a block → capture tap (corner A → corner B →
 *       center). Third tap submits validation.</li>
 *   <li>Shift + right-click on a block → reset the current validation tap sequence.</li>
 * </ul>
 */
public class BuildingPlacementWandItem extends Item {

    public static final String TAG_SELECTED_PREFAB = "bannermod:selected_prefab";
    public static final String TAG_WAND_MODE = "bannermod:wand_mode";
    public static final String TAG_VALIDATION_CORNER_A = "bannermod:validation_corner_a";
    public static final String TAG_VALIDATION_CORNER_B = "bannermod:validation_corner_b";
    public static final String TAG_VALIDATION_CENTER = "bannermod:validation_center";
    public static final String MODE_PLACE = "place";
    public static final String MODE_VALIDATE = "validate";
    public static final String MODE_REGISTER = "register";

    public BuildingPlacementWandItem(Properties properties) {
        super(properties);
    }

    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack stack = player.getItemInHand(hand);
        if (player.isShiftKeyDown()) {
            if (!level.isClientSide) {
                String mode = readMode(stack);
                String newMode;
                if (MODE_PLACE.equals(mode)) {
                    newMode = MODE_VALIDATE;
                } else if (MODE_VALIDATE.equals(mode)) {
                    newMode = MODE_REGISTER;
                } else {
                    newMode = MODE_PLACE;
                }
                ItemStackComponentData.update(stack, tag -> tag.putString(TAG_WAND_MODE, newMode));
                clearTapState(stack);
                player.sendSystemMessage(Component.translatable(
                        modeTranslation(newMode))
                        .withStyle(ChatFormatting.GOLD));
            }
            return InteractionResultHolder.sidedSuccess(stack, level.isClientSide);
        }

        if (level.isClientSide) {
            openSelectionScreen(stack);
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

        if (player.isShiftKeyDown()) {
            clearTapState(stack);
            if (!level.isClientSide) {
                player.sendSystemMessage(Component.translatable(
                        "bannermod.prefab.wand.validate.reset").withStyle(ChatFormatting.YELLOW));
            }
            return InteractionResult.SUCCESS;
        }

        String selected = readSelectedPrefab(stack);
        if (selected == null || selected.isEmpty()) {
            if (!level.isClientSide) {
                player.sendSystemMessage(Component.translatable("bannermod.prefab.wand.no_selection"));
            }
            return InteractionResult.SUCCESS;
        }
        ResourceLocation prefabId = ResourceLocation.tryParse(selected);
        if (prefabId == null) {
            return InteractionResult.SUCCESS;
        }

        String mode = readMode(stack);

        if (MODE_VALIDATE.equals(mode)) {
            return handleValidateTap(context, stack, prefabId);
        }
        if (MODE_REGISTER.equals(mode)) {
            return handleRegisterTap(context, stack, prefabId);
        }

        if (level.isClientSide) {
            BlockPos targetPos = context.getClickedPos().relative(context.getClickedFace());
            Direction facing = player.getDirection();
            BannerModMain.SIMPLE_CHANNEL.sendToServer(new MessageRequestPlaceBuilding(prefabId, targetPos, facing));
        }
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleValidateTap(UseOnContext context, ItemStack stack, ResourceLocation prefabId) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }

        BlockPos clicked = context.getClickedPos();

        if (!level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        CompoundTag tag = ItemStackComponentData.read(stack);
        if (tag == null || !tag.contains(TAG_VALIDATION_CORNER_A)) {
            ItemStackComponentData.update(stack, data -> data.putLong(TAG_VALIDATION_CORNER_A, clicked.asLong()));
            player.sendSystemMessage(Component.translatable(
                    "bannermod.prefab.wand.validate.corner_a",
                    clicked.getX(), clicked.getY(), clicked.getZ()).withStyle(ChatFormatting.AQUA));
            return InteractionResult.SUCCESS;
        }
        if (!tag.contains(TAG_VALIDATION_CORNER_B)) {
            ItemStackComponentData.update(stack, data -> data.putLong(TAG_VALIDATION_CORNER_B, clicked.asLong()));
            player.sendSystemMessage(Component.translatable(
                    "bannermod.prefab.wand.validate.corner_b",
                    clicked.getX(), clicked.getY(), clicked.getZ()).withStyle(ChatFormatting.AQUA));
            return InteractionResult.SUCCESS;
        }

        BlockPos cornerA = BlockPos.of(tag.getLong(TAG_VALIDATION_CORNER_A));
        BlockPos cornerB = BlockPos.of(tag.getLong(TAG_VALIDATION_CORNER_B));
        BlockPos center = clicked;
        clearTapState(stack);

        BannerModMain.SIMPLE_CHANNEL.sendToServer(
                new MessageRequestValidateBuilding(prefabId, cornerA, cornerB, center));
        return InteractionResult.SUCCESS;
    }

    private InteractionResult handleRegisterTap(UseOnContext context, ItemStack stack, ResourceLocation prefabId) {
        Level level = context.getLevel();
        Player player = context.getPlayer();
        if (player == null) {
            return InteractionResult.PASS;
        }
        if (!level.isClientSide) {
            return InteractionResult.SUCCESS;
        }

        BlockPos clicked = context.getClickedPos();
        CompoundTag tag = ItemStackComponentData.read(stack);
        if (tag == null || !tag.contains(TAG_VALIDATION_CORNER_A)) {
            ItemStackComponentData.update(stack, data -> data.putLong(TAG_VALIDATION_CORNER_A, clicked.asLong()));
            player.sendSystemMessage(Component.literal("Corner A selected").withStyle(ChatFormatting.AQUA));
            return InteractionResult.SUCCESS;
        }
        if (!tag.contains(TAG_VALIDATION_CORNER_B)) {
            ItemStackComponentData.update(stack, data -> data.putLong(TAG_VALIDATION_CORNER_B, clicked.asLong()));
            player.sendSystemMessage(Component.literal("Corner B selected").withStyle(ChatFormatting.AQUA));
            return InteractionResult.SUCCESS;
        }
        if (!tag.contains(TAG_VALIDATION_CENTER)) {
            ItemStackComponentData.update(stack, data -> data.putLong(TAG_VALIDATION_CENTER, clicked.asLong()));
            player.sendSystemMessage(Component.literal("Center selected. Tap key block.").withStyle(ChatFormatting.YELLOW));
            return InteractionResult.SUCCESS;
        }

        BlockPos cornerA = BlockPos.of(tag.getLong(TAG_VALIDATION_CORNER_A));
        BlockPos cornerB = BlockPos.of(tag.getLong(TAG_VALIDATION_CORNER_B));
        BlockPos center = BlockPos.of(tag.getLong(TAG_VALIDATION_CENTER));
        BlockPos keyBlock = clicked;
        clearTapState(stack);

        BannerModMain.SIMPLE_CHANNEL.sendToServer(
                new MessageRequestRegisterBuilding(prefabId, cornerA, cornerB, center, keyBlock));
        return InteractionResult.SUCCESS;
    }

    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        String mode = readMode(stack);
        MutableComponent modeLine = Component.translatable(
                        "bannermod.prefab.wand.tooltip.mode",
                        Component.translatable(modeTranslation(mode) + ".label"))
                .withStyle(ChatFormatting.GRAY);
        tooltip.add(modeLine);
        tooltip.add(Component.translatable("bannermod.prefab.wand.tooltip.use")
                .withStyle(ChatFormatting.DARK_GRAY));
        tooltip.add(Component.translatable("bannermod.prefab.wand.tooltip.shift")
                .withStyle(ChatFormatting.DARK_GRAY));
        String selected = readSelectedPrefab(stack);
        if (selected != null && !selected.isEmpty()) {
            tooltip.add(Component.translatable("bannermod.prefab.wand.tooltip.selected", selected)
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        if (MODE_VALIDATE.equals(mode) || MODE_REGISTER.equals(mode)) {
            CompoundTag tag = ItemStackComponentData.read(stack);
            int taps = 0;
            if (tag != null) {
                if (tag.contains(TAG_VALIDATION_CORNER_A)) taps++;
                if (tag.contains(TAG_VALIDATION_CORNER_B)) taps++;
                if (tag.contains(TAG_VALIDATION_CENTER)) taps++;
            }
            int needed = MODE_REGISTER.equals(mode) ? 4 : 3;
            tooltip.add(Component.translatable("bannermod.prefab.wand.tooltip.taps", taps, needed)
                    .withStyle(ChatFormatting.DARK_GRAY));
        }
        tooltip.add(Component.translatable("bannermod.prefab.wand.tooltip.loop_1")
                .withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("bannermod.prefab.wand.tooltip.loop_2")
                .withStyle(ChatFormatting.YELLOW));
        tooltip.add(Component.translatable("bannermod.prefab.wand.tooltip.loop_3")
                .withStyle(ChatFormatting.YELLOW));
    }

    private static String readMode(ItemStack stack) {
        CompoundTag tag = ItemStackComponentData.read(stack);
        if (tag == null || !tag.contains(TAG_WAND_MODE)) {
            return MODE_PLACE;
        }
        return tag.getString(TAG_WAND_MODE);
    }

    private static void clearTapState(ItemStack stack) {
        ItemStackComponentData.update(stack, tag -> {
            tag.remove(TAG_VALIDATION_CORNER_A);
            tag.remove(TAG_VALIDATION_CORNER_B);
            tag.remove(TAG_VALIDATION_CENTER);
        });
    }

    private static String readSelectedPrefab(ItemStack stack) {
        CompoundTag tag = ItemStackComponentData.read(stack);
        if (tag == null || !tag.contains(TAG_SELECTED_PREFAB)) {
            return null;
        }
        return tag.getString(TAG_SELECTED_PREFAB);
    }

    private static String modeTranslation(String mode) {
        if (MODE_VALIDATE.equals(mode)) {
            return "bannermod.prefab.wand.mode.validate";
        }
        if (MODE_REGISTER.equals(mode)) {
            return "bannermod.prefab.wand.mode.register";
        }
        return "bannermod.prefab.wand.mode.place";
    }

    @OnlyIn(Dist.CLIENT)
    private static void openSelectionScreen(ItemStack stack) {
        Minecraft.getInstance().setScreen(new PlaceBuildingScreen(stack));
    }
}
