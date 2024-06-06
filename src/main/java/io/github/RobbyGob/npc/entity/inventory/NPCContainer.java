package io.github.RobbyGob.npc.entity.inventory;

import com.google.common.collect.ImmutableList;
import io.github.RobbyGob.npc.entity.EntityNPC;
import net.minecraft.CrashReport;
import net.minecraft.CrashReportCategory;
import net.minecraft.ReportedException;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CollectionTag;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.nbt.Tag;
import net.minecraft.network.chat.Component;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.player.StackedContents;
import net.minecraft.world.food.Foods;
import net.minecraft.world.item.*;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.common.Tags;
import net.minecraftforge.common.capabilities.Capability;
import net.minecraftforge.common.capabilities.ForgeCapabilities;
import net.minecraftforge.items.ItemHandlerHelper;
import net.minecraftforge.registries.ForgeRegistries;

import javax.swing.plaf.basic.BasicComboBoxUI;
import javax.tools.Tool;
import java.security.DrbgParameters;
import java.util.Iterator;
import java.util.List;

public class NPCContainer implements Container {
    public static final int POP_TIME_DURATION = 5;
    public static final int INVENTORY_SIZE = 36;
    private static final int SELECTION_SIZE = 9;
    public static final int SLOT_OFFHAND = 40;
    public static final int NOT_FOUND_INDEX = -1;
    public static final int[] ALL_ARMOR_SLOTS = new int[]{0, 1, 2, 3};
    public static final int[] HELMET_SLOT_ONLY = new int[]{3};
    private final NonNullList<ItemStack> items;
    public final NonNullList<ItemStack> armor;
    public final NonNullList<ItemStack> offhand;
    private final List<NonNullList<ItemStack>> compartments;
    private int selected;
    private int timesChanged;
    public final EntityNPC owner;

    public NPCContainer(EntityNPC owner) {
        this.items = NonNullList.withSize(36, ItemStack.EMPTY);
        this.armor = NonNullList.withSize(4, ItemStack.EMPTY);
        this.offhand = NonNullList.withSize(1, ItemStack.EMPTY);
        this.compartments = ImmutableList.of(this.items, this.armor, this.offhand);
        this.owner = owner;
    }

    /**
     * Gets most powerfull item in inventory
     * @return sword item
     */
    public ItemStack findBestTool(TagKey<Item> tag) {
        ItemStack bestTool = ItemStack.EMPTY;
        float maxAttack = 0.0f;

        for (int i = 0; i < this.getContainerSize(); i++) {
            ItemStack stack = this.getItem(i);
            if (stack.is(tag)) {
                if(stack.is(ItemTags.SWORDS)) {
                    SwordItem sword = (SwordItem) stack.getItem();
                    float damage = sword.getDamage();
                    if (damage > maxAttack) {
                        maxAttack = damage;
                        bestTool = stack;
                    }
                }else if(stack.is(ItemTags.AXES)) {
                    AxeItem axe = (AxeItem) stack.getItem();
                    float speed = axe.getTier().getSpeed();
                    if (speed > maxAttack) {
                        maxAttack = speed;
                        bestTool = stack;
                    }
                }else if(stack.is(ItemTags.PICKAXES)){
                    PickaxeItem pickaxe = (PickaxeItem) stack.getItem();
                    float speed = pickaxe.getTier().getSpeed();
                    if (speed > maxAttack) {
                        maxAttack = speed;
                        bestTool = stack;
                    }
                }
                else if(stack.is(ItemTags.SHOVELS)){
                    ShovelItem shovel = (ShovelItem) stack.getItem();
                    float speed = shovel.getTier().getSpeed();
                    if (speed > maxAttack) {
                        maxAttack = speed;
                        bestTool = stack;
                    }
                }else if(stack.is(ItemTags.HOES)){
                    HoeItem hoe = (HoeItem) stack.getItem();
                    float speed = hoe.getTier().getSpeed();
                    if (speed > maxAttack) {
                        maxAttack = speed;
                        bestTool = stack;
                    }
                }
            }
        }

        return bestTool;
    }
    public ItemStack getBlock(Block block){
        if(block == null){
            return null;
        }
        ItemStack result = null;
        for (int i = 0; i < this.getContainerSize(); i++) {
            ItemStack stack = this.getItem(i);
            if(stack.is(block.asItem())){
                result = stack;
                return result;
            }
        }
        return result;
    }
    public ItemStack getFoodOrPotion(ItemStack item){
        if(item == null){
            return null;
        }
        if(!item.isEdible()){
            return null;
        }
        ItemStack result = null;
        for (int i = 0; i < this.getContainerSize(); i++) {
            ItemStack stack = this.getItem(i);
            if(stack.isEdible() && stack.is(item.getItem())){
                result = stack;
                return result;
            }
        }
        return result;
    }
    public ItemStack getUsableItem(ItemStack item){
        if(item == null){
            return null;
        }
        ItemStack result = null;
        for (int i = 0; i < this.getContainerSize(); i++) {
            ItemStack stack = this.getItem(i);
            if(stack.is(item.getItem())){
                if (stack.getCapability(ForgeCapabilities.ITEM_HANDLER).isPresent()) {
                    return stack;
                }else if(stack.is(ItemTags.SAPLINGS)){
                    return stack;
                }else if(stack.getItem() instanceof BucketItem){
                    return stack;
                }else if(stack.is(Tags.Items.SEEDS)){
                    return stack;
                }
            }

        }
        return result;
    }
    public ItemStack getShieldItem(){
        for(int i = 0; i < this.getContainerSize(); i++){
            ItemStack item = this.getItem(i);
            if(item.is(Items.SHIELD)){
                return item;
            }
        }
        return null;
    }
    public ItemStack getSelected() {
        return isHotbarSlot(this.selected) ? (ItemStack)this.items.get(this.selected) : ItemStack.EMPTY;
    }

    public static int getSelectionSize() {
        return 9;
    }

    private boolean hasRemainingSpaceForItem(ItemStack pDestination, ItemStack pOrigin) {
        return !pDestination.isEmpty() && ItemStack.isSameItemSameTags(pDestination, pOrigin) && pDestination.isStackable() && pDestination.getCount() < pDestination.getMaxStackSize() && pDestination.getCount() < this.getMaxStackSize();
    }
    public int getFreeSlot() {
        for(int i = 0; i < this.items.size(); ++i) {
            if (((ItemStack)this.items.get(i)).isEmpty()) {
                return i;
            }
        }

        return -1;
    }

//    public void setPickedItem(ItemStack pStack) {
//        int i = this.findSlotMatchingItem(pStack);
//        if (isHotbarSlot(i)) {
//            this.selected = i;
//        } else if (i == -1) {
//            this.selected = this.getSuitableHotbarSlot();
//            if (!((ItemStack)this.items.get(this.selected)).isEmpty()) {
//                int j = this.getFreeSlot();
//                if (j != -1) {
//                    this.items.set(j, (ItemStack)this.items.get(this.selected));
//                }
//            }
//
//            this.items.set(this.selected, pStack);
//        } else {
//            this.pickSlot(i);
//        }
//
//    }
//
//    public void pickSlot(int pIndex) {
//        this.selected = this.getSuitableHotbarSlot();
//        ItemStack itemstack = (ItemStack)this.items.get(this.selected);
//        this.items.set(this.selected, (ItemStack)this.items.get(pIndex));
//        this.items.set(pIndex, itemstack);
//    }

    public static boolean isHotbarSlot(int pIndex) {
        return pIndex >= 0 && pIndex < 9;
    }

    private int addResource(ItemStack pStack) {
        int i = this.getSlotWithRemainingSpace(pStack);
        if (i == -1) {
            i = this.getFreeSlot();
        }

        return i == -1 ? pStack.getCount() : this.addResource(i, pStack);
    }
    private int addResource(int pSlot, ItemStack pStack) {
        Item item = pStack.getItem();
        int i = pStack.getCount();
        ItemStack itemstack = this.getItem(pSlot);
        if (itemstack.isEmpty()) {
            itemstack = pStack.copy();
            itemstack.setCount(0);
            if (pStack.hasTag()) {
                itemstack.setTag(pStack.getTag().copy());
            }

            this.setItem(pSlot, itemstack);
        }

        int j = i;
        if (i > itemstack.getMaxStackSize() - itemstack.getCount()) {
            j = itemstack.getMaxStackSize() - itemstack.getCount();
        }

        if (j > this.getMaxStackSize() - itemstack.getCount()) {
            j = this.getMaxStackSize() - itemstack.getCount();
        }

        if (j == 0) {
            return i;
        } else {
            i -= j;
            itemstack.grow(j);
            itemstack.setPopTime(5);
            return i;
        }
    }
    public int getSlotWithRemainingSpace(ItemStack pStack) {
        if (this.hasRemainingSpaceForItem(this.getItem(this.selected), pStack)) {
            return this.selected;
        } else if (this.hasRemainingSpaceForItem(this.getItem(40), pStack)) {
            return 40;
        } else {
            for(int i = 0; i < this.items.size(); ++i) {
                if (this.hasRemainingSpaceForItem((ItemStack)this.items.get(i), pStack)) {
                    return i;
                }
            }

            return -1;
        }
    }

    public void tick() {
        int idx = 0;
        Iterator var2 = this.compartments.iterator();

        while(var2.hasNext()) {
            NonNullList<ItemStack> nonnulllist = (NonNullList)var2.next();

            for(int i = 0; i < nonnulllist.size(); ++i) {
                if (!((ItemStack)nonnulllist.get(i)).isEmpty()) {
                    //((ItemStack)nonnulllist.get(i)).onInventoryTick(this.owner.level(), this.owner, idx, this.selected);
                    ((ItemStack)nonnulllist.get(i)).onUseTick(this.owner.level(), this.owner, idx);
                }

                ++idx;
            }
        }

    }
    public boolean add(ItemStack pStack) {
        return this.add(-1, pStack);
    }

    public boolean add(int pSlot, ItemStack pStack) {
        if (pStack.isEmpty()) {
            return false;
        } else {
            try {
                if (pStack.isDamaged()) {
                    if (pSlot == -1) {
                        pSlot = this.getFreeSlot();
                    }

                    if (pSlot >= 0) {
                        this.items.set(pSlot, pStack.copyAndClear());
                        ((ItemStack)this.items.get(pSlot)).setPopTime(5);
                        return true;
                    } else if (this.owner.getAbilities().instabuild) {
                        pStack.setCount(0);
                        return true;
                    } else {
                        return false;
                    }
                } else {
                    int i;
                    do {
                        i = pStack.getCount();
                        if (pSlot == -1) {
                            pStack.setCount(this.addResource(pStack));
                        } else {
                            pStack.setCount(this.addResource(pSlot, pStack));
                        }
                    } while(!pStack.isEmpty() && pStack.getCount() < i);

                    if (pStack.getCount() == i && this.owner.getAbilities().instabuild) {
                        pStack.setCount(0);
                        return true;
                    } else {
                        return pStack.getCount() < i;
                    }
                }
            } catch (Throwable var6) {
                CrashReport crashreport = CrashReport.forThrowable(var6, "Adding item to inventory");
                CrashReportCategory crashreportcategory = crashreport.addCategory("Item being added");
                crashreportcategory.setDetail("Registry Name", () -> {
                    return String.valueOf(ForgeRegistries.ITEMS.getKey(pStack.getItem()));
                });
                crashreportcategory.setDetail("Item Class", () -> {
                    return pStack.getItem().getClass().getName();
                });
                crashreportcategory.setDetail("Item ID", Item.getId(pStack.getItem()));
                crashreportcategory.setDetail("Item data", pStack.getDamageValue());
                crashreportcategory.setDetail("Item name", () -> {
                    return pStack.getHoverName().getString();
                });
                throw new ReportedException(crashreport);
            }
        }
    }

    public ItemStack removeItem(int pIndex, int pCount) {
        List<ItemStack> list = null;

        NonNullList nonnulllist;
        for(Iterator var4 = this.compartments.iterator(); var4.hasNext(); pIndex -= nonnulllist.size()) {
            nonnulllist = (NonNullList)var4.next();
            if (pIndex < nonnulllist.size()) {
                list = nonnulllist;
                break;
            }
        }

        return list != null && !((ItemStack)list.get(pIndex)).isEmpty() ? ContainerHelper.removeItem(list, pIndex, pCount) : ItemStack.EMPTY;
    }

    public void removeItem(ItemStack pStack) {
        Iterator var2 = this.compartments.iterator();

        while(true) {
            while(var2.hasNext()) {
                NonNullList<ItemStack> nonnulllist = (NonNullList)var2.next();

                for(int i = 0; i < nonnulllist.size(); ++i) {
                    if (nonnulllist.get(i) == pStack) {
                        nonnulllist.set(i, ItemStack.EMPTY);
                        break;
                    }
                }
            }

            return;
        }
    }
    public ItemStack removeItemNoUpdate(int pIndex) {
        NonNullList<ItemStack> nonnulllist = null;

        NonNullList nonnulllist1;
        for(Iterator var3 = this.compartments.iterator(); var3.hasNext(); pIndex -= nonnulllist1.size()) {
            nonnulllist1 = (NonNullList)var3.next();
            if (pIndex < nonnulllist1.size()) {
                nonnulllist = nonnulllist1;
                break;
            }
        }

        if (nonnulllist != null && !((ItemStack)nonnulllist.get(pIndex)).isEmpty()) {
            ItemStack itemstack = (ItemStack)nonnulllist.get(pIndex);
            nonnulllist.set(pIndex, ItemStack.EMPTY);
            return itemstack;
        } else {
            return ItemStack.EMPTY;
        }
    }
    public void setItem(int pIndex, ItemStack pStack) {
        NonNullList<ItemStack> nonnulllist = null;

        NonNullList nonnulllist1;
        for(Iterator var4 = this.compartments.iterator(); var4.hasNext(); pIndex -= nonnulllist1.size()) {
            nonnulllist1 = (NonNullList)var4.next();
            if (pIndex < nonnulllist1.size()) {
                nonnulllist = nonnulllist1;
                break;
            }
        }

        if (nonnulllist != null) {
            nonnulllist.set(pIndex, pStack);
        }

    }
    public float getDestroySpeed(BlockState pState) {
        return ((ItemStack)this.items.get(this.selected)).getDestroySpeed(pState);
    }

    public ListTag save(ListTag pListTag) {
        int k;
        CompoundTag compoundtag2;
        for(k = 0; k < this.items.size(); ++k) {
            if (!((ItemStack)this.items.get(k)).isEmpty()) {
                compoundtag2 = new CompoundTag();
                compoundtag2.putByte("Slot", (byte)k);
                ((ItemStack)this.items.get(k)).save(compoundtag2);
                pListTag.add(compoundtag2);
            }
        }

        for(k = 0; k < this.armor.size(); ++k) {
            if (!((ItemStack)this.armor.get(k)).isEmpty()) {
                compoundtag2 = new CompoundTag();
                compoundtag2.putByte("Slot", (byte)(k + 100));
                ((ItemStack)this.armor.get(k)).save(compoundtag2);
                pListTag.add(compoundtag2);
            }
        }

        for(k = 0; k < this.offhand.size(); ++k) {
            if (!((ItemStack)this.offhand.get(k)).isEmpty()) {
                compoundtag2 = new CompoundTag();
                compoundtag2.putByte("Slot", (byte)(k + 150));
                ((ItemStack)this.offhand.get(k)).save(compoundtag2);
                pListTag.add(compoundtag2);
            }
        }

        return pListTag;
    }
    public void load(ListTag pListTag) {
        this.items.clear();
        this.armor.clear();
        this.offhand.clear();

        for(int i = 0; i < pListTag.size(); ++i) {
            CompoundTag compoundtag = pListTag.getCompound(i);
            int j = compoundtag.getByte("Slot") & 255;
            ItemStack itemstack = ItemStack.of(compoundtag);
            if (!itemstack.isEmpty()) {
                if (j >= 0 && j < this.items.size()) {
                    this.items.set(j, itemstack);
                } else if (j >= 100 && j < this.armor.size() + 100) {
                    this.armor.set(j - 100, itemstack);
                } else if (j >= 150 && j < this.offhand.size() + 150) {
                    this.offhand.set(j - 150, itemstack);
                }
            }
        }

    }

    public int getContainerSize() {
        return this.items.size() + this.armor.size() + this.offhand.size();
    }
    public boolean isEmpty() {
        Iterator var1 = this.items.iterator();

        ItemStack itemstack2;
        do {
            if (!var1.hasNext()) {
                var1 = this.armor.iterator();

                do {
                    if (!var1.hasNext()) {
                        var1 = this.offhand.iterator();

                        do {
                            if (!var1.hasNext()) {
                                return true;
                            }

                            itemstack2 = (ItemStack)var1.next();
                        } while(itemstack2.isEmpty());

                        return false;
                    }

                    itemstack2 = (ItemStack)var1.next();
                } while(itemstack2.isEmpty());

                return false;
            }

            itemstack2 = (ItemStack)var1.next();
        } while(itemstack2.isEmpty());

        return false;
    }
    public ItemStack getItem(int pIndex) {
        List<ItemStack> list = null;

        NonNullList nonnulllist;
        for(Iterator var3 = this.compartments.iterator(); var3.hasNext(); pIndex -= nonnulllist.size()) {
            nonnulllist = (NonNullList)var3.next();
            if (pIndex < nonnulllist.size()) {
                list = nonnulllist;
                break;
            }
        }

        return list == null ? ItemStack.EMPTY : (ItemStack)list.get(pIndex);
    }
    public Component getName() {
        return Component.translatable("container.inventory");
    }

    public ItemStack getArmor(int pSlot) {
        return (ItemStack)this.armor.get(pSlot);
    }

    public void hurtArmor(DamageSource pSource, float pDamage, int[] pArmorPieces) {
        if (!(pDamage <= 0.0F)) {
            pDamage /= 4.0F;
            if (pDamage < 1.0F) {
                pDamage = 1.0F;
            }

            int[] var4 = pArmorPieces;
            int var5 = pArmorPieces.length;

            for(int var6 = 0; var6 < var5; ++var6) {
                int i = var4[var6];
                ItemStack itemstack = (ItemStack)this.armor.get(i);
                if ((!pSource.is(DamageTypeTags.IS_FIRE) || !itemstack.getItem().isFireResistant()) && itemstack.getItem() instanceof ArmorItem) {
                    itemstack.hurtAndBreak((int)pDamage, this.owner, (p_35997_) -> {
                        p_35997_.broadcastBreakEvent(EquipmentSlot.byTypeAndIndex(EquipmentSlot.Type.ARMOR, i));
                    });
                }
            }
        }

    }
    public void dropAllItems(double x, double y, double z, Level level) {
        for (int slot = 0; slot < getContainerSize(); slot++) {
            ItemStack stack = getItem(slot);
            if (!stack.isEmpty()) {
                ItemEntity itemEntity = new ItemEntity(level, x, y, z, stack.copy());
                level.addFreshEntity(itemEntity);
                setItem(slot, ItemStack.EMPTY);
            }
        }
    }
    public void setChanged() {
        ++this.timesChanged;
    }

    public int getTimesChanged() {
        return this.timesChanged;
    }

    public boolean stillValid(Player pPlayer) {
        if (this.owner.isRemoved()) {
            return false;
        } else {
            return !(pPlayer.distanceToSqr(this.owner) > 64.0);
        }
    }
    public boolean contains(ItemStack pStack) {
        Iterator var2 = this.compartments.iterator();

        while(var2.hasNext()) {
            List<ItemStack> list = (List)var2.next();
            Iterator var4 = list.iterator();

            while(var4.hasNext()) {
                ItemStack itemstack = (ItemStack)var4.next();
                if (!itemstack.isEmpty() && ItemStack.isSameItemSameTags(itemstack, pStack)) {
                    return true;
                }
            }
        }

        return false;
    }

    public boolean contains(TagKey<Item> pTag) {
        Iterator var2 = this.compartments.iterator();

        while(var2.hasNext()) {
            List<ItemStack> list = (List)var2.next();
            Iterator var4 = list.iterator();

            while(var4.hasNext()) {
                ItemStack itemstack = (ItemStack)var4.next();
                if (!itemstack.isEmpty() && itemstack.is(pTag)) {
                    return true;
                }
            }
        }

        return false;
    }

    public void clearContent() {
        Iterator var1 = this.compartments.iterator();

        while(var1.hasNext()) {
            List<ItemStack> list = (List)var1.next();
            list.clear();
        }

    }
    public void fillStackedContents(StackedContents pStackedContent) {
        Iterator var2 = this.items.iterator();

        while(var2.hasNext()) {
            ItemStack itemstack = (ItemStack)var2.next();
            pStackedContent.accountSimpleStack(itemstack);
        }

    }

    public ItemStack removeFromSelected(boolean pRemoveStack) {
        ItemStack itemstack = this.getSelected();
        return itemstack.isEmpty() ? ItemStack.EMPTY : this.removeItem(this.selected, pRemoveStack ? itemstack.getCount() : 1);
    }

    public CompoundTag writeInventoryToNBT() {
        CompoundTag tag = new CompoundTag();
        ListTag itemList = new ListTag();

        // Write each item in the inventory to NBT
        for (int i = 0; i < items.size(); i++) {
            ItemStack stack = items.get(i);
            if (!stack.isEmpty()) {
                CompoundTag itemTag = new CompoundTag();
                itemTag.putByte("Slot", (byte) i);
                stack.save(itemTag);
                itemList.add(itemTag);
            }
        }

        // Add the list of items to the main tag
        tag.put("Items", itemList);
        return tag;
    }

    public void readInventoryFromNBT(CompoundTag tag) {
        ListTag itemList = tag.getList("Items", CompoundTag.TAG_COMPOUND);

        // Clear the existing inventory
        clearContent();

        // Read each item from NBT and add it to the inventory
        for (int i = 0; i < itemList.size(); i++) {
            CompoundTag itemTag = itemList.getCompound(i);
            int slot = itemTag.getByte("Slot") & 255;
            if (slot >= 0 && slot < items.size()) {
                items.set(slot, ItemStack.of(itemTag));
            }
        }
    }

    @Override
    public boolean canPlaceItem(int index, ItemStack stack) {
        return true;
    }

    @Override
    public int getMaxStackSize() {
        return 64;
    }

    public boolean containsItem(Item item)
    {
        for (NonNullList<ItemStack> compartment : this.compartments) {
            for (ItemStack stack : compartment) {
                if (!stack.isEmpty() && stack.is(item)) {
                    return true;
                }
            }
        }
        return false;
    }
    public ItemStack getItemStack(Item item) {
        for (int i = 0; i < this.getContainerSize(); i++) {
            ItemStack stack = this.getItem(i);
            if (!stack.isEmpty() && stack.getItem() == item) {
                return stack;
            }
        }
        return ItemStack.EMPTY;
    }
    public boolean setArmorInSlot(int slot, ItemStack stack) {
        if (slot >= 0 && slot < this.armor.size() && this.isValidArmorSlot(slot, stack)) {
            this.armor.set(slot, stack);
            return true;
        }
        return false;
    }
    public boolean isValidArmorSlot(int slot, ItemStack stack) {
        if (slot >= 0 && slot < this.armor.size()) {

            if (!stack.isEmpty() && stack.getItem() instanceof ArmorItem)
            {
                ArmorItem armorItem = (ArmorItem) stack.getItem();
                ArmorItem.Type equipmentSlot = armorItem.getType();

                switch (equipmentSlot) {
                    case HELMET:
                        return slot == 3; // Only allow helmet in the helmet slot
                    case CHESTPLATE:
                        return slot == 2; // Only allow chestplate in the chestplate slot
                    case LEGGINGS:
                        return slot == 1; // Only allow leggings in the leggings slot
                    case BOOTS:
                        return slot == 0; // Only allow boots in the boots slot
                    default:
                        return false;
                }
            }
        }
        return false;
    }
    public void equipArmor() {
        owner.setItemSlot(EquipmentSlot.HEAD,this.armor.get(3));
        owner.setItemSlot(EquipmentSlot.CHEST,this.armor.get(2));
        owner.setItemSlot(EquipmentSlot.LEGS,this.armor.get(1));
        owner.setItemSlot(EquipmentSlot.FEET,this.armor.get(0));
    }
}
