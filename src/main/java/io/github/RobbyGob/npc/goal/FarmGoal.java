package io.github.RobbyGob.npc.goal;

import io.github.RobbyGob.npc.entity.EntityNPC;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.CropBlock;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.EnumSet;
import java.util.logging.Level;
import java.util.logging.Logger;

public class FarmGoal extends Goal {
    private static final Logger LOGGER = Logger.getLogger(FarmGoal.class.getName());

    private final EntityNPC npc;
    private BlockPos plantingPos;
    private BlockPos harvestingPos;

    public FarmGoal(EntityNPC npc) {
        this.npc = npc;
        this.setFlags(EnumSet.of(Goal.Flag.MOVE, Goal.Flag.LOOK));
    }

    @Override
    public boolean canUse() {

        plantingPos = findNearestFarmlandForPlanting();
        harvestingPos = findNearestFarmlandForHarvesting();

        if (plantingPos == null && harvestingPos == null) {
            LOGGER.log(Level.INFO, "No Target for harvesting or planting");
            return false;
        }
        LOGGER.log(Level.INFO, "There is target for planting or harvesting");
        return true;
    }

    @Override
    public void start() {
        if (plantingPos != null) {
            npc.getNavigation().moveTo(plantingPos.getX(), plantingPos.getY(), plantingPos.getZ(), 1.0D);
            LOGGER.log(Level.INFO, "Moving towards farmland for planting at " + plantingPos);
        } else if (harvestingPos != null) {
            npc.getNavigation().moveTo(harvestingPos.getX(), harvestingPos.getY(), harvestingPos.getZ(), 1.0D);
            LOGGER.log(Level.INFO, "Moving towards farmland for harvesting at " + harvestingPos);
        }
    }
    @Override
    public void tick() {
        if (harvestingPos != null) {
            double distanceToHarvestingPos = npc.position().distanceToSqr(Vec3.atCenterOf(harvestingPos));
            if (distanceToHarvestingPos < 2.0D) {
                harvestCrops(harvestingPos);
                LOGGER.log(Level.INFO, "Harvested crops at " + harvestingPos);
                harvestingPos = findNearestFarmlandForHarvesting();
                if (harvestingPos != null) {
                    npc.getNavigation().moveTo(harvestingPos.getX(), harvestingPos.getY(), harvestingPos.getZ(), 1.0D);
                    LOGGER.log(Level.INFO, "Moving towards farmland for harvesting at " + harvestingPos);
                    return;
                }
            } else {
                npc.getNavigation().moveTo(harvestingPos.getX(), harvestingPos.getY(), harvestingPos.getZ(), 1.0D);
                LOGGER.log(Level.INFO, "Moving towards farmland for harvesting at " + harvestingPos);
                return;
            }
        }

        if (npc.getInventory().containsItem(Items.WHEAT_SEEDS)) {
            if (plantingPos != null) {
                double distanceToPlantingPos = npc.position().distanceToSqr(Vec3.atCenterOf(plantingPos));
                if (distanceToPlantingPos < 2.0D) {
                    plantSeed(plantingPos);
                    LOGGER.log(Level.INFO, "Planted wheat seeds at " + plantingPos);
                    plantingPos = findNearestFarmlandForPlanting();
                    if (plantingPos != null) {
                        npc.getNavigation().moveTo(plantingPos.getX(), plantingPos.getY(), plantingPos.getZ(), 1.0D);
                        LOGGER.log(Level.INFO, "Moving towards farmland for planting at " + plantingPos);
                    }
                } else {
                    npc.getNavigation().moveTo(plantingPos.getX(), plantingPos.getY(), plantingPos.getZ(), 1.0D);
                    LOGGER.log(Level.INFO, "Moving towards farmland for planting at " + plantingPos);
                }
            }
        } else {
            LOGGER.log(Level.INFO, "Does not have seeds for planting");
        }
    }

    private BlockPos findNearestFarmlandForPlanting() {
        BlockPos npcPos = npc.blockPosition();
        int searchRadius = 10;
        for (int dx = -searchRadius; dx <= searchRadius; dx++) {
            for (int dz = -searchRadius; dz <= searchRadius; dz++) {
                for (int dy = -searchRadius / 2; dy <= searchRadius / 2; dy++) {
                    BlockPos pos = npcPos.offset(dx, dy, dz);
                    BlockState blockState = npc.level().getBlockState(pos);
                    BlockState aboveBlockState = npc.level().getBlockState(pos.above());
                    if (blockState.is(Blocks.FARMLAND) && aboveBlockState.isAir()) {
                        return pos.above();
                    }
                }
            }
        }
        return null;
    }

    private BlockPos findNearestFarmlandForHarvesting() {
        BlockPos npcPos = npc.blockPosition();
        int searchRadius = 10;
        for (int dx = -searchRadius; dx <= searchRadius; dx++) {
            for (int dz = -searchRadius; dz <= searchRadius; dz++) {
                for (int dy = -searchRadius / 2; dy <= searchRadius / 2; dy++) {
                    BlockPos pos = npcPos.offset(dx, dy, dz);
                    BlockState blockState = npc.level().getBlockState(pos);
                    if (blockState.getBlock() instanceof CropBlock && ((CropBlock) blockState.getBlock()).isMaxAge(blockState)) {
                        return pos;
                    }
                }
            }
        }
        return null;
    }

    private void plantSeed(BlockPos pos) {
        ItemStack seeds = npc.getInventory().getItemStack(Items.WHEAT_SEEDS);
        if (!seeds.isEmpty()) {
            npc.level().setBlock(pos, Blocks.WHEAT.defaultBlockState(), 3);
            seeds.shrink(1);
        } else {
            LOGGER.log(Level.WARNING, "No wheat seeds found in NPC inventory");
        }
    }

    private void harvestCrops(BlockPos pos) {
        BlockState blockState = npc.level().getBlockState(pos);
        if (blockState.getBlock() instanceof CropBlock) {
            CropBlock cropBlock = (CropBlock) blockState.getBlock();
            if (cropBlock.isMaxAge(blockState)) {
                npc.level().destroyBlock(pos, true);
                cropBlock.dropResources(blockState, npc.level(), pos, null, npc, ItemStack.EMPTY);
            }
        }
    }
}