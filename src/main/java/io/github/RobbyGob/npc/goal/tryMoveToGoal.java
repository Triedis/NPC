package io.github.RobbyGob.npc.goal;

import net.minecraft.world.entity.PathfinderMob;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;

public class tryMoveToGoal extends Goal {
    private final PathfinderMob mob;
    private  Vec3 vec3;

    public tryMoveToGoal(PathfinderMob mob, Vec3 vec3) {
        this.mob = mob;
        this.vec3 = vec3;
    }

    /**
     * Checks if the target x, y, z coordinates are within the range of the npc
     * @return true if outside range, false if within the range
     */
    @Override
    public boolean canUse() {
        if(vec3 != null) {
            AABB npcAABB = mob.getBoundingBox();
            AABB targetAABB = new AABB(vec3, vec3).inflate(1);
            if(!npcAABB.intersects(targetAABB))
            {
                return true;
            }
            else {
                vec3 = null;
                return false;
            }
        }
            return false;
    }

    public void tick() {
        this.mob.getNavigation().moveTo(vec3.x, vec3.y, vec3.z, 1.0f);
    }
}
