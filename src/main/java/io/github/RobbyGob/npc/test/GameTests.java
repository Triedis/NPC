package io.github.RobbyGob.npc.test;

import com.mojang.brigadier.context.CommandContext;
import io.github.RobbyGob.npc.NPC_Mod;
import io.github.RobbyGob.npc.commands.ControlCommands;
import io.github.RobbyGob.npc.entity.EntityNPC;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.core.BlockPos;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.gametest.GameTestHolder;

import static net.minecraft.world.item.Items.*;


@GameTestHolder(NPC_Mod.MODID)
public class GameTests
{
    @GameTest(template = "npcmod:minetest")
    public static void mine_test(GameTestHelper helper)
    {
        Vec3 vec3 = helper.absoluteVec(new Vec3(2.5,2,1.5));

        EntityNPC npc = new EntityNPC(helper.getLevel(), vec3.x, vec3.y, vec3.z);
        npc.stopNPC();
        helper.getLevel().addFreshEntity(npc);

        Player player = helper.makeMockPlayer();
        player.teleportTo(npc.getX(), npc.getY(), npc.getZ());

        CommandContext<CommandSourceStack> command = new CommandContext<>(
                player.createCommandSourceStack(),
                "/npc setBlockType diamondOre",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false);
        ControlCommands manager = new ControlCommands();
        int i = manager.startMiningDiamondOre(command);

        helper.succeedWhen(()->{
            helper.assertBlockNotPresent(Blocks.DIAMOND_ORE, new BlockPos(2, 2, 13));
        });
    }

    @GameTest(template = "npcmod:empty3x3x3")
    public static void summon_test(GameTestHelper helper)
    {
        Vec3 vec3 = helper.absoluteVec(new Vec3(1.5,2,1.5));

        EntityNPC npc = new EntityNPC(helper.getLevel(), vec3.x, vec3.y, vec3.z);
        npc.stopNPC();
        helper.getLevel().addFreshEntity(npc);

        helper.succeedWhen(()->{
            helper.assertEntityInstancePresent(npc,1,2,1);
        });
    }

    @GameTest(template = "npcmod:pathfinding_test", timeoutTicks = 200)
    public static void pathfinding_test(GameTestHelper helper)
    {
        Vec3 vec3 = helper.absoluteVec(new Vec3(0.5,2,0.5));
        Vec3 vec3_target = new Vec3(vec3.x+9, vec3.y, vec3.z+9);

        EntityNPC npc = new EntityNPC(helper.getLevel(), vec3.x, vec3.y, vec3.z);
        //npc.stopNPC();
        //npc.stopAggression();
        helper.getLevel().addFreshEntity(npc);

        npc.setNewTarget(vec3_target);

        helper.succeedWhen(()->{
            helper.assertEntityInstancePresent(npc,9,2,9);
        });
    }

    @GameTest(template = "npcmod:5x3x5_closed", timeoutTicks = 200)
    public static void kills_zombie_test(GameTestHelper helper)
    {
        Vec3 vec3 = helper.absoluteVec(new Vec3(1.5,2,1.5));

        EntityNPC npc = new EntityNPC(helper.getLevel(), vec3.x, vec3.y, vec3.z);
        helper.getLevel().addFreshEntity(npc);

        helper.spawnWithNoFreeWill(EntityType.ZOMBIE, 1,2,1);

        helper.succeedWhen(()->{
            helper.assertEntityNotPresent(EntityType.ZOMBIE, new BlockPos(1,2,1));
        });
    }

    @GameTest(template = "npcmod:water", timeoutTicks = 100)
    public static void water_floating_test(GameTestHelper helper)
    {
        Vec3 vec3 = helper.absoluteVec(new Vec3(1.5,4,1.5));

        EntityNPC npc = new EntityNPC(helper.getLevel(), vec3.x, vec3.y, vec3.z);
        npc.stopNPC();
        helper.getLevel().addFreshEntity(npc);

        helper.runAfterDelay(80,()->{
            helper.succeedIf(()->{
                helper.assertEntityInstancePresent(npc,1,4,1);
            });
        });
    }

    @GameTest(template = "npcmod:empty10x3x3")
    public static void move_to_player_test(GameTestHelper helper)
    {
        Vec3 vec3 = helper.absoluteVec(new Vec3(0.5,2,1.5));

        EntityNPC npc = new EntityNPC(helper.getLevel(), vec3.x, vec3.y, vec3.z);
        helper.getLevel().addFreshEntity(npc);

        Player player = helper.makeMockPlayer();
        player.teleportTo(vec3.x+9, vec3.y, vec3.z);

        CommandContext<CommandSourceStack> command = new CommandContext<>(
                player.createCommandSourceStack(),
                "/npc moveTo player",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false);
        ControlCommands manager = new ControlCommands();
        int i = manager.moveToPlayer(command);

        helper.succeedWhen(()->{
            helper.assertEntityInstancePresent(npc,9,2,1);
        });
    }

    @GameTest(template = "npcmod:empty3x3x3")
    public static void drop_item_test(GameTestHelper helper)
    {
        Vec3 vec3 = helper.absoluteVec(new Vec3(1.5,2,1.5));

        EntityNPC npc = new EntityNPC(helper.getLevel(), vec3.x, vec3.y, vec3.z);
        npc.stopNPC();
        helper.getLevel().addFreshEntity(npc);

        npc.addItem(DIAMOND);

        npc.kill();
        helper.succeedWhen(()->{
            helper.assertItemEntityPresent(DIAMOND,new BlockPos(1,1,1),3);
        });
    }

    @GameTest(template = "npcmod:empty3x3x3")
    public static void pick_up_item_test(GameTestHelper helper)
    {
        Vec3 vec3 = helper.absoluteVec(new Vec3(1.5,2,1.5));

        EntityNPC npc = new EntityNPC(helper.getLevel(), vec3.x, vec3.y, vec3.z);
        npc.stopNPC();
        helper.getLevel().addFreshEntity(npc);

        helper.spawnItem(DIAMOND, 1,1,1);

        helper.runAfterDelay(20,()->{
            helper.succeedWhen(()->{
                helper.assertItemEntityNotPresent(DIAMOND,new BlockPos(1,1,1),3);
            });
        });
    }

    @GameTest(template = "npcmod:empty3x3x3")
    public static void clear_inventory_test(GameTestHelper helper)
    {
        Vec3 vec3 = helper.absoluteVec(new Vec3(1.5,2,1.5));

        EntityNPC npc = new EntityNPC(helper.getLevel(), vec3.x, vec3.y, vec3.z);
        npc.stopNPC();
        helper.getLevel().addFreshEntity(npc);

        npc.addItem(DIAMOND);

        npc.clearInventory();

        npc.kill();
        helper.succeedWhen(()->{
            helper.assertItemEntityNotPresent(DIAMOND,new BlockPos(1,1,1),3);
        });
    }

    @GameTest(template = "npcmod:empty5x5x5", timeoutTicks = 100)
    public static void pause_test(GameTestHelper helper) {

        Vec3 vec3 = helper.absoluteVec(new Vec3(0.5,2,0.5));
        Vec3 vec3_target = new Vec3(vec3.x+4, vec3.y, vec3.z+4);

        EntityNPC npc = new EntityNPC(helper.getLevel(), vec3.x, vec3.y, vec3.z);
        npc.stopNPC();
        helper.getLevel().addFreshEntity(npc);

        Player player = helper.makeMockPlayer();
        player.teleportTo(vec3.x, vec3.y, vec3.z);

        npc.stopAggression();
        npc.setNewTarget(vec3_target);


        CommandContext<CommandSourceStack> command = new CommandContext<>(
                player.createCommandSourceStack(),
                "/npc pause",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false);
        ControlCommands manager = new ControlCommands();
        int i = manager.pause(command);

        helper.runAfterDelay(80,()->{
            helper.succeedIf(()->{
                helper.assertEntityInstancePresent(npc,0,2,0);
            });
        });
    }

    @GameTest(template = "npcmod:empty5x5x5", timeoutTicks = 100)
    public static void unpause_test(GameTestHelper helper) {

        Vec3 vec3 = helper.absoluteVec(new Vec3(0.5,2,0.5));
        Vec3 vec3_target = new Vec3(vec3.x+4, vec3.y, vec3.z+4);

        EntityNPC npc = new EntityNPC(helper.getLevel(), vec3.x, vec3.y, vec3.z);
        npc.stopNPC();
        helper.getLevel().addFreshEntity(npc);

        Player player = helper.makeMockPlayer();
        player.teleportTo(vec3.x, vec3.y, vec3.z);

        npc.stopAggression();
        npc.setNewTarget(vec3_target);


        CommandContext<CommandSourceStack> command = new CommandContext<>(
                player.createCommandSourceStack(),
                "/npc unpause",
                null,
                null,
                null,
                null,
                null,
                null,
                null,
                false);
        ControlCommands manager = new ControlCommands();
        int iNotNeeded = manager.pause(command);
        int i = manager.unpause(command);

        helper.runAfterDelay(80,()->{
            helper.succeedIf(()->{
                helper.assertEntityInstancePresent(npc,4,2,4);
            });
        });
    }

    @GameTest(template = "npcmod:empty3x3x3")
    public static void picked_item_stored_in_inventory_test(GameTestHelper helper)
    {
        Vec3 vec3 = helper.absoluteVec(new Vec3(1.5,2,1.5));

        EntityNPC npc = new EntityNPC(helper.getLevel(), vec3.x, vec3.y, vec3.z);
        npc.stopNPC();
        helper.getLevel().addFreshEntity(npc);

        helper.spawnItem(DIAMOND, 1,1,1);

        helper.runAfterDelay(20,()->{
            helper.succeedWhen(()->{
                helper.assertItemEntityNotPresent(DIAMOND,new BlockPos(1,1,1),3);
                helper.assertTrue(!npc.inventoryIsEmpty(), "Is inventory empty?");
            });
        });
    }

}
