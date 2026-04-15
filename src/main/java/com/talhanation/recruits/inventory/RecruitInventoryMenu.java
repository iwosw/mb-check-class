package com.talhanation.recruits.inventory;

import com.mojang.datafixers.util.Pair;
import com.talhanation.recruits.Main;
import com.talhanation.recruits.entities.AbstractRecruitEntity;
import com.talhanation.recruits.init.ModScreens;
import de.maxhenkel.corelib.inventory.ContainerBase;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.Container;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.HorseInventoryMenu;
import net.minecraft.world.inventory.InventoryMenu;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BannerItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.ShieldItem;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

public class RecruitInventoryMenu extends ContainerBase {
    private final Container recruitInventory;
    private final AbstractRecruitEntity recruit;
    private final com.talhanation.bannerlord.entity.shared.AbstractRecruitEntity bannerlordRecruit;
    private static final ResourceLocation[] TEXTURE_EMPTY_SLOTS = new ResourceLocation[]{
            InventoryMenu.EMPTY_ARMOR_SLOT_BOOTS,
            InventoryMenu.EMPTY_ARMOR_SLOT_LEGGINGS,
            InventoryMenu.EMPTY_ARMOR_SLOT_CHESTPLATE,
            InventoryMenu.EMPTY_ARMOR_SLOT_HELMET
    };
    public static final EquipmentSlot[] SLOT_IDS = new EquipmentSlot[]{
            EquipmentSlot.HEAD,
            EquipmentSlot.CHEST,
            EquipmentSlot.LEGS,
            EquipmentSlot.FEET,
            EquipmentSlot.OFFHAND,
            EquipmentSlot.MAINHAND
    };

    public RecruitInventoryMenu(int id, AbstractRecruitEntity recruit, Inventory playerInventory) {
        super(ModScreens.RECRUIT_CONTAINER_TYPE.get(), id, playerInventory, recruit.getInventory());
        this.recruit = recruit;
        this.bannerlordRecruit = null;
        this.recruitInventory = recruit.getInventory();

        addPlayerInventorySlots();

        addRecruitHandSlots();
        addRecruitEquipmentSlots();
        addRecruitInventorySlots();
    }

    public RecruitInventoryMenu(int id, com.talhanation.bannerlord.entity.shared.AbstractRecruitEntity recruit, Inventory playerInventory) {
        super(ModScreens.RECRUIT_CONTAINER_TYPE.get(), id, playerInventory, recruit.getInventory());
        this.recruit = null;
        this.bannerlordRecruit = recruit;
        this.recruitInventory = recruit.getInventory();

        addPlayerInventorySlots();

        addRecruitHandSlots();
        addRecruitEquipmentSlots();
        addRecruitInventorySlots();
    }

    public AbstractRecruitEntity getRecruit() {
        return recruit;
    }

    public com.talhanation.bannerlord.entity.shared.AbstractRecruitEntity getBannerlordRecruit() {
        return bannerlordRecruit;
    }

    private Container recruitContainer() {
        return recruit != null ? recruit.inventory : bannerlordRecruit.inventory;
    }

    private boolean recruitIsUsingItem() {
        return recruit != null ? recruit.isUsingItem() : bannerlordRecruit.isUsingItem();
    }

    private boolean recruitCanHoldItem(ItemStack stack) {
        return recruit != null ? recruit.canHoldItem(stack) : bannerlordRecruit.canHoldItem(stack);
    }

    private void setRecruitItemSlot(EquipmentSlot slot, ItemStack stack) {
        if (recruit != null) {
            recruit.setItemSlot(slot, stack);
        }
        else {
            bannerlordRecruit.setItemSlot(slot, stack);
        }
    }

    private void onRecruitItemStackAdded(ItemStack stack) {
        if (recruit != null) {
            recruit.onItemStackAdded(stack);
        }
        else {
            bannerlordRecruit.onItemStackAdded(stack);
        }
    }

    private void onRecruitInventoryChanged() {
        if (recruit != null) {
            recruit.onInventoryChanged();
        }
        else {
            bannerlordRecruit.onInventoryChanged();
        }
    }

    private int inventoryColumns() {
        return recruit != null ? recruit.getInventoryColumns() : bannerlordRecruit.getInventoryColumns();
    }

    @Override
    public int getInvOffset() {
        return 56;
    }

    //iv slots
    //0 = head
    //1 = chest
    //2 = legs
    //3 = boots
    //4 = offhand
    //5 = mainhand
    //6+ -> inventory

    public void addRecruitHandSlots() {
        this.addSlot(new Slot(recruitContainer(), 4,44,90) {
            @Override
            public boolean mayPlace(ItemStack stack){
                return !recruitIsUsingItem() && stack.getItem() instanceof ShieldItem;
            }

            @Override
            public boolean mayPickup(Player player) {
                return !recruitIsUsingItem();
            }

            @Override
            public void set(ItemStack stack){
                super.set(stack);
                setRecruitItemSlot(EquipmentSlot.OFFHAND, stack);
                onRecruitItemStackAdded(stack);
            }

            @Override
            public void setChanged() {
                super.setChanged();
                onRecruitInventoryChanged();
            }

            @Override
            public Pair<ResourceLocation, ResourceLocation> getNoItemIcon () {
                return Pair.of(InventoryMenu.BLOCK_ATLAS, InventoryMenu.EMPTY_ARMOR_SLOT_SHIELD);
            }
        });

        this.addSlot(new Slot(recruitContainer(), 5,26,90) {
            @Override
            public boolean mayPlace(ItemStack itemStack) {
                return recruitCanHoldItem(itemStack);
            }

            @Override
            public void set(ItemStack stack){
                super.set(stack);
                setRecruitItemSlot(EquipmentSlot.MAINHAND, stack);
                onRecruitItemStackAdded(stack);
            }

            @Override
            public void setChanged() {
                super.setChanged();
                onRecruitInventoryChanged();
            }
        });
    }
    public void addRecruitEquipmentSlots() {
        for (int slotIndex = 0; slotIndex < 4; ++slotIndex) {
            final EquipmentSlot equipmentslottype = SLOT_IDS[slotIndex];
            this.addSlot(new Slot(recruitContainer(), slotIndex, 8, 18 + slotIndex * 18) {
                public int getMaxStackSize() {
                    return 1;
                }

                public boolean mayPlace(ItemStack itemStack) {
                    return itemStack.canEquip(equipmentslottype, recruit != null ? recruit : bannerlordRecruit)
                            || (itemStack.getItem() instanceof BannerItem && equipmentslottype.equals(EquipmentSlot.HEAD));
                }

                @Override
                public void set(ItemStack stack){
                    super.set(stack);
                    setRecruitItemSlot(equipmentslottype, stack);
                    onRecruitItemStackAdded(stack);
                }

                @Override
                public void setChanged() {
                    super.setChanged();
                    onRecruitInventoryChanged();
                }

                @OnlyIn(Dist.CLIENT)
                public Pair<ResourceLocation, ResourceLocation> getNoItemIcon() {
                    return Pair.of(InventoryMenu.BLOCK_ATLAS, TEXTURE_EMPTY_SLOTS[equipmentslottype.getIndex()]);
                }
            });
        }
    }

    public void addRecruitInventorySlots() {
        for (int k = 0; k < 3; ++k) {
            for (int l = 0; l < 3; ++l) {
                this.addSlot(new Slot(recruitInventory, 6 + l + k * inventoryColumns(), 2 * 18 + 82 + l * 18,  18 + k * 18){
                     @Override
                     public void set(ItemStack stack){
                         super.set(stack);
                         onRecruitItemStackAdded(stack);
                     }

                    @Override
                    public void setChanged() {
                        super.setChanged();
                        onRecruitInventoryChanged();
                    }
                }
                );
            }
        }
    }

    public ItemStack quickMoveStack(Player playerIn, int index) {
        ItemStack itemstack = ItemStack.EMPTY;
        Slot slot = this.getSlot(index);
        if (slot != null && slot.hasItem()) {
            ItemStack stack = slot.getItem();
            itemstack = stack.copy();
            if (index <= 35){// <= 35 Itemstack from player inventory

                //HEAD
                if (this.getSlot(38).mayPlace(stack) && !this.getSlot(38).hasItem()) {
                    if (!this.moveItemStackTo(stack, 38, this.slots.size(), false)) {
                        return ItemStack.EMPTY;
                    }
                }
                //CHEST
                else if (this.getSlot(39).mayPlace(stack) && !this.getSlot(39).hasItem()) {
                    if (!this.moveItemStackTo(stack, 39, this.slots.size(), false)) {
                        return ItemStack.EMPTY;
                    }
                }
                //LEGS
                else if (this.getSlot(40).mayPlace(stack) && !this.getSlot(40).hasItem()) {
                    if (!this.moveItemStackTo(stack, 40, this.slots.size(), false)) {
                        return ItemStack.EMPTY;
                    }
                }
                //FEET
                else if (this.getSlot(41).mayPlace(stack) && !this.getSlot(41).hasItem()) {
                    if (!this.moveItemStackTo(stack, 41, this.slots.size(),  false)) {
                        return ItemStack.EMPTY;
                    }
                }

                //OFFHAND
                else if (this.getSlot(36).mayPlace(stack) && !this.getSlot(36).hasItem()) {
                    if (!this.moveItemStackTo(stack, 36, this.slots.size(), false)) {
                        return ItemStack.EMPTY;
                    }
                }
                //MAINHAND
                else if (this.getSlot(37).mayPlace(stack) && !this.getSlot(37).hasItem()) {
                    if (!this.moveItemStackTo(stack, 37, this.slots.size(), false)) {
                        return ItemStack.EMPTY;
                    }
                }

                //RECRUIT INV
                //par1: ItemStack, par2: TargetSlot, par3: inventory size, par4: reverse
                else if (!this.moveItemStackTo(stack, 42, this.slots.size(), false)) {
                    return ItemStack.EMPTY;
                }
            }
            //Itemstack from RecruitInventory
            //Note: par3 must be 35 as if not there is a duplication bug
            else if (!this.moveItemStackTo(stack, 0, 35, false)) {
                return ItemStack.EMPTY;
            }

            if (stack.isEmpty()) {
                slot.set(ItemStack.EMPTY);
            } else {
                slot.setChanged();
            }
        }

        return itemstack;
    }
    //Handslots
    // 37: Mainhand
    // 36: Offhand
    //Armor Slots:
    // 38: Head
    // 39: Chest
    // 40: Leggs
    // 41: Feet
    // Recruit Inventory: 42 - 50
    // Player Inventory: 0 - 35
}
