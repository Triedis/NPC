package io.github.RobbyGob.npc.goal;

import io.github.RobbyGob.npc.entity.EntityNPC;
import net.minecraft.core.BlockPos;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

public class MineBlockGoal extends Goal {
    private final EntityNPC npc;
    private final Block targetBlock;
    private BlockPos targetPos;

    public MineBlockGoal(EntityNPC npc, Block targetBlock) {
        this.npc = npc;
        this.targetBlock = targetBlock;
    }

    @Override
    public boolean canUse() {
        // Check if NPC is not currently mining and if there is a target block available
        return !npc.isBusyMining() && findNearestBlock();
    }

    @Override
    public boolean canContinueToUse() {
        // Continue as long as the target block is still valid
        return targetPos != null && npc.level().getBlockState(targetPos).getBlock() == targetBlock;
    }

    @Override
    public void start() {
        npc.setBusyMining(true);
        npc.lookAt(targetPos.getX() + 0.5, targetPos.getY() + 0.5, targetPos.getZ() + 0.5, 0, 0);
    }

    @Override
    public void stop() {
        npc.setBusyMining(false);
        targetPos = null;
    }

    @Override
    public void tick() {
        if (targetPos != null) {
            double distanceSq = npc.distanceToSqr(Vec3.atCenterOf(targetPos));
            if (distanceSq <= 2.5 * 2.5) {
                BlockState state = npc.level().getBlockState(targetPos);
                npc.level().destroyBlock(targetPos, true);
                npc.playSound(state.getSoundType().getBreakSound(), 1.0f, 1.0f);
                npc.setItemSlot(EquipmentSlot.MAINHAND, ItemStack.EMPTY);
                targetPos = null;
            } else {
                // Move towards the target block if not in range
                npc.getNavigation().moveTo(targetPos.getX(), targetPos.getY(), targetPos.getZ(), 1.0);
                npc.getLookControl().setLookAt(targetPos.getX(), targetPos.getY(), targetPos.getZ());
            }
        }
    }

    private boolean findNearestBlock() {
        BlockPos npcPos = npc.blockPosition();
        BlockPos.MutableBlockPos mutablePos = new BlockPos.MutableBlockPos();
        double nearestDistanceSq = Double.MAX_VALUE;
        BlockPos nearestPos = null;

        for (int dx = -20; dx <= 20; dx++) {
            for (int dy = -20; dy <= 20; dy++) {
                for (int dz = -20; dz <= 20; dz++) {
                    mutablePos.set(npcPos.getX() + dx, npcPos.getY() + dy, npcPos.getZ() + dz);
                    BlockState state = npc.level().getBlockState(mutablePos);
                    if (state.getBlock() == targetBlock) {
                        double distanceSq = npc.distanceToSqr(Vec3.atCenterOf(mutablePos));
                        if (distanceSq < nearestDistanceSq) {
                            nearestDistanceSq = distanceSq;
                            nearestPos = mutablePos.immutable();
                        }
                    }
                }
            }
        }

        targetPos = nearestPos;
        return targetPos != null;
    }
}
