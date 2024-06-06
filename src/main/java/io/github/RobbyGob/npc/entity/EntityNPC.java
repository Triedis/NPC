package io.github.RobbyGob.npc.entity;

import io.github.RobbyGob.npc.entity.inventory.NPCContainer;
import io.github.RobbyGob.npc.goal.FarmGoal;
import io.github.RobbyGob.npc.goal.MineBlockGoal;
import io.github.RobbyGob.npc.goal.tryMoveToGoal;
import io.github.RobbyGob.npc.init.EntityInit;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.tags.TagKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.*;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.animal.*;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.monster.Creeper;
import net.minecraft.world.entity.monster.Enemy;
import net.minecraft.world.entity.player.Abilities;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.*;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.ForgeHooks;
import net.minecraftforge.common.Tags;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import static io.github.RobbyGob.npc.item.ModItems.NPC_CONTROLLER;

public class EntityNPC extends PathfinderMob implements MenuProvider
{
    private boolean IS_FARMING = false;
    private boolean IS_HUNTING = false;
    private boolean IS_AGGRESSIVE = true;
    private boolean busyMining = false;

    private Block targetBlock = null;

    private final NPCContainer inventory;
    private Vec3 vec3 = null;
    private ItemStack mainhand;

    public EntityNPC(EntityType<EntityNPC> type, Level level) {
        super(type, level);
        this.inventory = new NPCContainer(this);
        this.mainhand = ItemStack.EMPTY;
    }

    public EntityNPC(Level level, double x, double y, double z) {
        this(EntityInit.NPC_ENTITY.get(), level);
        setPos(x, y, z);
    }
    public EntityNPC(Level level, BlockPos position) {
        this(level, position.getX(), position.getY(), position.getZ());
    }

    @Override
    protected void registerGoals() {
        // Basic goals
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(1, new TemptGoal(this, 1.5D, Ingredient.of(NPC_CONTROLLER.get()), false));
        this.goalSelector.addGoal(2, new tryMoveToGoal(this, vec3));
        this.goalSelector.addGoal(3, new RandomLookAroundGoal(this));
        this.goalSelector.addGoal(4, new MoveTowardsTargetGoal(this, 0.9D, 16.0F));
        this.targetSelector.addGoal(5, new HurtByTargetGoal(this));

        // Conditional goals
        if (IS_HUNTING) {
            this.targetSelector.addGoal(6, new NearestAttackableTargetGoal<>(this, Animal.class, 1, false, true, (target) -> {
                return target instanceof Cow || target instanceof Pig || target instanceof Chicken || target instanceof Sheep;
            }));
        }

        if (IS_FARMING) {
            this.goalSelector.addGoal(7, new FarmGoal(this));
        }

        if (IS_AGGRESSIVE) {
            this.goalSelector.addGoal(8, new MeleeAttackGoal(this, 1.0D, true));
            this.targetSelector.addGoal(9, new NearestAttackableTargetGoal<>(this, Mob.class, 1, false, true, (p_28879_) -> {
                return p_28879_ instanceof Enemy && !(p_28879_ instanceof Creeper);
            }));
        }

        // Add MineBlockGoal
            this.goalSelector.addGoal(10, new MineBlockGoal(this, targetBlock));
    }

    public void setTargetBlock(Block input) {
        targetBlock = input;
        updateGoals();
    }
    public void removeTargetBlock(Block input) {
        targetBlock = null;
        updateGoals();
    }

    public static AttributeSupplier.Builder createAttributes() {
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 50D)
                .add(Attributes.FOLLOW_RANGE, 50D)
                .add(Attributes.MOVEMENT_SPEED, 0.25D)
                .add(Attributes.ARMOR_TOUGHNESS, 0.1f)
                .add(Attributes.ATTACK_KNOCKBACK, 1f)
                .add(Attributes.ATTACK_SPEED, 2f)
                .add(Attributes.ATTACK_DAMAGE, 3f);
    }
    public void equipDiamondGear()
    {
        this.inventory.setArmorInSlot(3,new ItemStack(Items.DIAMOND_HELMET));
        this.inventory.setArmorInSlot(2,new ItemStack(Items.DIAMOND_CHESTPLATE));
        this.inventory.setArmorInSlot(1,new ItemStack(Items.DIAMOND_LEGGINGS));
        this.inventory.setArmorInSlot(0,new ItemStack(Items.DIAMOND_BOOTS));
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.DIAMOND_HOE));

        this.inventory.equipArmor();
    }

    public NPCContainer getInventory()
    {
        return inventory;
    }
    public void setNewTarget(Vec3 target)
    {
        vec3 = target;
        updateGoals();
    }
    public void updateGoals()
    {
        removeAllGoals();
        registerGoals();
    }
    private void removeAllGoals()
    {
        this.goalSelector.removeAllGoals(Goal -> true);
        this.targetSelector.removeAllGoals(Goal -> true);
    }

    public void startFarming()
    {
        IS_FARMING = true;
        updateGoals();
    }

    public void stopFarming()
    {
        IS_FARMING = false;
        updateGoals();
    }

    public void startHunting()
    {
        IS_HUNTING = true;
        updateGoals();
    }

    public void stopHunting()
    {
        IS_HUNTING = false;
        updateGoals();
    }

    public void stopNPC()
    {
        //this.getNavigation().stop();
        removeAllGoals();
    }
    public void continueNPC()
    {
        updateGoals();
    }

    public void setBusyMining(boolean busyMining) {
        this.busyMining = busyMining;
    }

    public boolean isBusyMining() {
        return busyMining;
    }

    public void startAggression()
    {
        IS_AGGRESSIVE = true;
        updateGoals();
    }

    public void stopAggression()
    {
        IS_AGGRESSIVE = false;
        updateGoals();
    }

//    /**
//     * Function implemented for testing purposes. If needed can be deleted
//     * @param pPlayer
//     * @param pHand
//     * @return
//     */
//    @Override
//    protected InteractionResult mobInteract(Player pPlayer, InteractionHand pHand) {
//        if(pHand == InteractionHand.MAIN_HAND) {
//            for(int i = 0; i < inventory.getContainerSize(); i++) {
//                System.out.println(inventory.getItem(i).toString());
//            }
//        }
//        useItem(Items.BUCKET.getDefaultInstance(), this.getOnPos().offset(0, 0, 1));
//        useShield();
//        useAndPlaceBlock(this.getOnPos().offset(0, 1, 1), Blocks.DIAMOND_BLOCK);
//
//        Entity entity = getNearestEntity(100);
//        if(entity != null){
//            useSword(entity);
//        }
//
//        useAxe(this.getOnPos());
//        usePickaxe(this.getOnPos());
//        useShovel(this.getOnPos());
//        useHoe(this.getOnPos());
//        return InteractionResult.SUCCESS;
//    }

    public LivingEntity getNearestEntity(double radius) {
        List<Entity> entities = this.level().getEntities(this, this.getBoundingBox().inflate(radius), entity -> entity instanceof LivingEntity);
        LivingEntity nearestEntity = null;
        double nearestDistance = Double.MAX_VALUE;
        for (Entity entity : entities) {
            double distance = this.distanceTo(entity);
            if (distance < nearestDistance) {
                nearestEntity = (LivingEntity) entity;
                nearestDistance = distance;
            }
        }
        return nearestEntity;
    }
    @Override
    public void addAdditionalSaveData(CompoundTag pCompound) {
        super.addAdditionalSaveData(pCompound);
        pCompound.put("Inventory", this.inventory.writeInventoryToNBT());
    }

    @Override
    public void readAdditionalSaveData(CompoundTag pCompound) {
        super.readAdditionalSaveData(pCompound);
        if (pCompound.contains("Inventory")) {
            this.inventory.readInventoryFromNBT(pCompound.getCompound("Inventory"));
        }
    }

    @Override
    public void load(CompoundTag pCompound) {
        super.load(pCompound);
    }

    // Method to check for nearby items and pick them up
    private void pickUpItems() {
        if (this.isAlive()) {
            AABB pickupRange = new AABB(getX() - 2.0D, getY() - 1.0D, getZ() - 2.0D, getX() + 2.0D, getY() + 1.0D, getZ() + 2.0D);
            level().getEntitiesOfClass(ItemEntity.class, pickupRange).forEach(itemEntity -> {
                ItemStack stack = itemEntity.getItem();
                if (!stack.isEmpty()) {
                    // Try adding the item to the NPC's inventory
                    if (inventory.add(stack)) {
                        itemEntity.remove(RemovalReason.UNLOADED_TO_CHUNK);
                    }
                }
            });
        }
    }

    // Override tick method to periodically check for nearby items
    @Override
    public void tick() {
        super.tick();
        if (!this.level().isClientSide) {
            pickUpItems();
        }
    }

    @Override
    protected void dropAllDeathLoot(DamageSource pDamageSource) {
        Entity entity = pDamageSource.getEntity();
        int lootingLevel = ForgeHooks.getLootingLevel(this, entity, pDamageSource);
        this.captureDrops(new ArrayList<>());
        boolean killedByPlayer = this.lastHurtByPlayerTime > 0;

        if (this.shouldDropLoot() && this.level().getGameRules().getBoolean(GameRules.RULE_DOMOBLOOT)) {
            this.dropFromLootTable(pDamageSource, killedByPlayer);
            this.dropCustomDeathLoot(pDamageSource, lootingLevel, killedByPlayer);
        }

        this.dropEquipment();

        // Drop the NPC's inventory
        inventory.dropAllItems(this.getX(), this.getY(), this.getZ(), this.level());

        Collection<ItemEntity> drops = this.captureDrops((Collection<ItemEntity>) null);
        if (!ForgeHooks.onLivingDrops(this, pDamageSource, drops, lootingLevel, killedByPlayer)) {
            drops.forEach(e -> this.level().addFreshEntity(e));
        }
    }

    public Abilities getAbilities() {
        return this.getAbilities();
    }

    private ItemStack getTool(TagKey<Item> toolTag, String toolName) {
        ItemStack toolToUse = this.inventory.findBestTool(toolTag);
        if (toolToUse.isEmpty()) {
            System.out.println("No " + toolName + " found");
            return null;
        }
        this.setMainhand(toolToUse);
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(toolToUse.getItem()));
        return toolToUse;
    }
    public void attack(LivingEntity targetEntity) {
        if (targetEntity == null) {
            return;
        }

        // Assuming a simple attack behavior for the NPC
        // You can customize this logic based on your game's combat system
        float damageAmount = getMainHandItem().getDamageValue(); // Adjust the damage amount as needed

        // Apply damage to the target entity
        DamageSource damageSource = damageSources().mobAttack(this);
        targetEntity.hurt(damageSource, damageAmount);
    }

    public void useSword(Entity target) {
        ItemStack stack = getTool(ItemTags.SWORDS, "sword");
        if(stack == null){
            return;
        }
        Level world = this.level();
        // Assuming the NPC should attack the target entity
        // You can adjust this logic based on your requirements
        this.attack((LivingEntity) target);

        // Play attack sound or any other effects
        // For example, if the NPC is a mob, you can play a sound effect like this:
        this.playSound(SoundEvents.PLAYER_ATTACK_SWEEP, 1.0f, 1.0f);

        // Damage the sword
        this.hurtMainItemInHand();
    }
    public void useAxe(BlockPos blockPos) {
        ItemStack stack = getTool(ItemTags.AXES, "axe");
        if(stack == null){
            return;
        }
        Level world = this.level();
        AxeItem axe = (AxeItem) stack.getItem();
        if (world.getBlockState(blockPos).is(BlockTags.MINEABLE_WITH_AXE)) {
            Vec3 pos = this.position();
            double radiusSq = pos.distanceToSqr(Vec3.atCenterOf(blockPos));
            if(radiusSq <= 9){
                float speed = world.getBlockState(blockPos).getDestroySpeed(world, blockPos);
                world.destroyBlock(blockPos, true);

                hurtMainItemInHand();
            }
        }
        System.out.println(this.getMainHandItem().getDisplayName().getString());
    }


    public void usePickaxe(BlockPos blockPos) {
        ItemStack stack = getTool(ItemTags.PICKAXES, "pickaxe");
        if(stack == null){
            return;
        }
        Level world = this.level();
        PickaxeItem pickaxe = (PickaxeItem) stack.getItem();
        if (world.getBlockState(blockPos).is(BlockTags.MINEABLE_WITH_PICKAXE)) {
            Vec3 pos = this.position();
            double radiusSq = pos.distanceToSqr(Vec3.atCenterOf(blockPos));
            if(radiusSq <= 9){
                world.destroyBlock(blockPos, true);

                hurtMainItemInHand();
            }
        }
        System.out.println(this.getMainHandItem().getDisplayName().getString());
    }

    public void useShovel(BlockPos blockPos) {
        ItemStack shovelStack = getTool(ItemTags.SHOVELS, "shovel");
        if(shovelStack == null){
            return;
        }
        Level world = this.level();
        ShovelItem shovel = (ShovelItem) shovelStack.getItem();
        if (world.getBlockState(blockPos).is(BlockTags.MINEABLE_WITH_SHOVEL)) {
            Vec3 pos = this.position();
            double radiusSq = pos.distanceToSqr(Vec3.atCenterOf(blockPos));
            if(radiusSq <= 9){
                world.destroyBlock(blockPos, true);

                hurtMainItemInHand();
            }
        }
        System.out.println(this.getMainHandItem().getDisplayName().getString());
    }

    public void useHoe(BlockPos blockPos) {
        ItemStack hoeStack = getTool(ItemTags.HOES, "hoe");
        if (hoeStack == null) {
            return;
        }
        Level world = this.level();
        if (world.getBlockState(blockPos).is(Blocks.GRASS_BLOCK) || world.getBlockState(blockPos).is(Blocks.DIRT)) {
            Vec3 pos = this.position();
            double radiusSq = pos.distanceToSqr(Vec3.atCenterOf(blockPos));
            if(radiusSq <= 9){
                if(world.getBlockState(blockPos).is(BlockTags.MINEABLE_WITH_HOE)){
                    world.destroyBlock(blockPos, true);
                }

                // Convert the block at the position into farmland
                world.setBlockAndUpdate(blockPos, Blocks.FARMLAND.defaultBlockState());
                this.playSound(SoundType.CROP.getBreakSound());
                hurtMainItemInHand();
            }
        }
        System.out.println(this.getMainHandItem().getDisplayName().getString());
    }
    public void useAndPlaceBlock(BlockPos targetPos, Block blockToPlace) {
        ItemStack blockStack = this.inventory.getBlock(blockToPlace); // Implement this method to retrieve the block from NPC's inventory
        if (blockStack == null) {
            System.out.println("No " + blockToPlace.getName().getString() + " found in inventory");
            return;
        }
        System.out.println(blockStack.getDisplayName().getString());
        Level world = this.level();
        BlockState blockState = world.getBlockState(targetPos);
        if(blockState.isAir()){
            world.setBlock(targetPos, blockToPlace.defaultBlockState(), 3);
            blockStack.shrink(1);
        }
    }
    public void useFood(ItemStack item){
        ItemStack stack = this.inventory.getFoodOrPotion(item);
        if(stack == null){
            return;
        }
        this.eat(this.level(), stack);
        if(stack.getItem() instanceof PotionItem){
            this.playSound(stack.getDrinkingSound());
        }
        stack.shrink(1);
    }

    public void useItem(ItemStack item, BlockPos blockPos) {
        ItemStack stack = this.inventory.getUsableItem(item);
        if (stack == null) {
            return;
        }
        Level world = this.level();
        BlockState targetBlockState = this.level().getBlockState(blockPos);
        if (stack.is(Tags.Items.SEEDS)) {
            if (targetBlockState.is(Blocks.FARMLAND)) {
                this.playSound(SoundEvents.CROP_PLANTED);
                world.setBlock(blockPos.above(), targetBlockState.getBlock().defaultBlockState(), 3);
                stack.shrink(1);
                System.out.println("Planted " + targetBlockState.getBlock().getName().getString() + " at " + blockPos);
            }
        } else if (stack.is(ItemTags.SAPLINGS)) {
            this.playSound(SoundEvents.BAMBOO_SAPLING_PLACE);
            if (targetBlockState.is(Blocks.DIRT) || targetBlockState.is(Blocks.GRASS_BLOCK)) {
                world.setBlock(blockPos.above(), Block.byItem(stack.getItem()).defaultBlockState(), 3);
                stack.shrink(1);
                System.out.println("Planted sapling at " + blockPos);
            }
        } else if (stack.getItem() == Items.WATER_BUCKET) {
            // Place water at the target position
            if (targetBlockState.isAir()) {
                world.setBlock(blockPos, Blocks.WATER.defaultBlockState(), 3);
                System.out.println("Placed water at " + blockPos);
                this.inventory.removeItem(stack);
                this.playSound(SoundEvents.BUCKET_EMPTY);
                this.inventory.add(new ItemStack(Items.BUCKET)); // Add an empty bucket back to the inventory
            }
        } else if (stack.getItem() == Items.LAVA_BUCKET) {
            // Place lava at the target position
            if (targetBlockState.isAir()) {
                this.inventory.removeItem(stack);
                this.playSound(SoundEvents.BUCKET_EMPTY_LAVA);
                world.setBlock(blockPos, Blocks.LAVA.defaultBlockState(), 3);
                System.out.println("Placed lava at " + blockPos);
                this.inventory.add(new ItemStack(Items.BUCKET)); // Add an empty bucket back to the inventory
            }
        } else if (stack.getItem() == Items.MILK_BUCKET) {
            // Use the milk bucket (e.g., remove potion effects)
            this.inventory.removeItem(stack);
            this.playSound(SoundEvents.GENERIC_DRINK);
            this.curePotionEffects(new ItemStack(Items.MILK_BUCKET));
            System.out.println("Used milk bucket");
            this.inventory.add(new ItemStack(Items.BUCKET)); // Add an empty bucket back to the inventory
        } else if (stack.getItem() == Items.BUCKET) {
            // Use empty bucket to collect fluids
            if (targetBlockState.getBlock() == Blocks.WATER) {
                this.playSound(SoundEvents.BUCKET_FILL);
                world.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
                System.out.println("Collected water at " + blockPos);
                stack.shrink(1);
                this.inventory.add(new ItemStack(Items.WATER_BUCKET)); // Add a water bucket to the inventory
            } else if (targetBlockState.getBlock() == Blocks.LAVA) {
                this.playSound(SoundEvents.BUCKET_FILL_LAVA);
                world.setBlock(blockPos, Blocks.AIR.defaultBlockState(), 3);
                System.out.println("Collected lava at " + blockPos);
                stack.shrink(1);
                this.inventory.add(new ItemStack(Items.LAVA_BUCKET)); // Add a lava bucket to the inventory
            }
        }
    }
    public void useShield(){
        ItemStack shield = this.inventory.getShieldItem();
        if(shield == null){
            return;
        }
        this.setItemSlot(EquipmentSlot.OFFHAND, shield);
        this.playSound(SoundEvents.SHIELD_BLOCK);
    }


    public void hurtMainItemInHand(){
        int damageAmount = 1;
        getMainHandItem().hurtAndBreak(damageAmount, this, (entity) -> {
            entity.broadcastBreakEvent(InteractionHand.MAIN_HAND);
        });
    }

    @Override
    public ItemStack getOffhandItem() {
        return super.getOffhandItem();
    }


    @Override
    public ItemStack getMainHandItem() {
        return this.mainhand;
    }

    public void setMainhand(ItemStack stack){
        this.mainhand = stack;
    }
    @Override
    protected InteractionResult mobInteract(Player player, InteractionHand hand) {
        if (!this.level().isClientSide) {
            ItemStack heldItem = player.getItemInHand(hand);

            if (heldItem.getItem() == Items.POTATO && this.getHealth() < this.getMaxHealth()) {
                this.heal(4.0F); // Heal for 2 hearts (4 health points)
                heldItem.shrink(1); // Consume one potato
                this.playSound(SoundEvents.PLAYER_BURP, 1.0F, 1.0F);
                return InteractionResult.SUCCESS;
            }
            if (hand == InteractionHand.MAIN_HAND) {
                player.openMenu(this); // Open the inventory GUI
                return InteractionResult.SUCCESS;
            }
        }
        return InteractionResult.PASS;
    }
    @Override
    public AbstractContainerMenu createMenu(int syncId, Inventory playerInventory, Player playerEntity) {
        return new ChestMenu(MenuType.GENERIC_9x4, syncId, playerInventory, inventory, 4); //cia paprastas variantas kur matomas NPC inventorius kaip chestas
    }
    public void addItem(Item item)
    {
        inventory.add(new ItemStack(item));
    }

    public void clearInventory()
    {
        inventory.clearContent();
    }
    public boolean inventoryIsEmpty()
    {
        return inventory.isEmpty();
    }

    public void lookAt(double x, double y, double z, float yaw, float pitch) {
        double xDiff = x - this.getX();
        double yDiff = y - this.getEyeY();
        double zDiff = z - this.getZ();
        double distance = Math.sqrt(xDiff * xDiff + zDiff * zDiff);

        float yawAngle = (float) Math.toDegrees(Math.atan2(zDiff, xDiff)) - 90.0F;
        float pitchAngle = (float) Math.toDegrees(-Math.atan2(yDiff, distance));

        this.setYRot(yawAngle);
        this.setXRot(pitchAngle);
    }
}
