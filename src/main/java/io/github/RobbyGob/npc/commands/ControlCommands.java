package io.github.RobbyGob.npc.commands;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;;
import io.github.RobbyGob.npc.entity.EntityNPC;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.blocks.BlockInput;
import net.minecraft.commands.arguments.blocks.BlockPredicateArgument;
import net.minecraft.commands.arguments.coordinates.Vec3Argument;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.server.ServerLifecycleHooks;

import java.util.List;
import java.util.Objects;

import static net.minecraft.world.level.Level.OVERWORLD;

public class ControlCommands {
    private static final double OFFSET_X = 0.5;
    private static final double OFFSET_Z = 0.5;

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher) {
        dispatcher.register(Commands.literal("npc")
                .then(Commands.literal("moveTo")
                        .then(Commands.argument("destination", Vec3Argument.vec3()).executes(ControlCommands::moveToPos))
                        .then(Commands.literal("player").executes(ControlCommands::moveToPlayer)))
                .then(Commands.literal("pause").executes(ControlCommands::pause))
                .then(Commands.literal("unpause").executes(ControlCommands::unpause))
                .then(Commands.literal("huntStart").executes(ControlCommands::startTheHunt))
                .then(Commands.literal("huntStop").executes(ControlCommands::stopTheHunt))
                .then(Commands.literal("TestDiamondGear").executes(ControlCommands::Test))
                .then(Commands.literal("startFarming").executes(ControlCommands::startFarming))
                .then(Commands.literal("stopFarming").executes(ControlCommands::stopFarming))
                .then(Commands.literal("setBlockType")
                        .then(Commands.literal("diamondOre").executes(ControlCommands::startMiningDiamondOre))
                        .then(Commands.literal("clearBlockType").executes(ControlCommands::stopMining))));
    }

    public static int startMiningDiamondOre(CommandContext<CommandSourceStack> context) {
        Player player = getPlayer(context.getSource());
        if (player != null) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            ServerLevel world = server.getLevel(OVERWORLD);

            List<EntityNPC> npcList = getNPCsInRange(world, player.blockPosition(), 100);
            for (EntityNPC npc : npcList) {
                npc.setTargetBlock(Blocks.DIAMOND_ORE);
            }
            player.sendSystemMessage(Component.literal("Set targeted block type for NPCs to mine: Diamond Ore"));
        }

        return Command.SINGLE_SUCCESS;
    }

    public static int stopMining(CommandContext<CommandSourceStack> context) {
        Player player = getPlayer(context.getSource());
        if (player != null) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            ServerLevel world = server.getLevel(OVERWORLD);

            List<EntityNPC> npcList = getNPCsInRange(world, player.blockPosition(), 100);
            for (EntityNPC npc : npcList) {
                npc.setTargetBlock(null);
            }
            player.sendSystemMessage(Component.literal("Stopped mining process"));
        }

        return Command.SINGLE_SUCCESS;
    }

    public static int startFarming(CommandContext<CommandSourceStack> command)
    {
        Player player = getPlayer(command.getSource());
        if (player != null) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            ServerLevel world = server.getLevel(OVERWORLD);

            List<EntityNPC> npcList = getNPCsInRange(world, player.blockPosition(), 100);
            for (EntityNPC npc : npcList) {
                npc.startFarming();
            }
            player.sendSystemMessage(Component.literal("NPC's are farming"));
        }

        return Command.SINGLE_SUCCESS;
    }

    public static int stopFarming(CommandContext<CommandSourceStack> command)
    {
        Player player = getPlayer(command.getSource());
        if (player != null) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            ServerLevel world = server.getLevel(OVERWORLD);

            List<EntityNPC> npcList = getNPCsInRange(world, player.blockPosition(), 100);
            for (EntityNPC npc : npcList) {
                npc.stopFarming();
            }
            player.sendSystemMessage(Component.literal("NPC's stopped farming"));
        }

        return Command.SINGLE_SUCCESS;
    }

    public static int Test(CommandContext<CommandSourceStack> command)
    {
        Player player = getPlayer(command.getSource());
        if (player != null) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            ServerLevel world = server.getLevel(OVERWORLD);

            List<EntityNPC> npcList = getNPCsInRange(world, player.blockPosition(), 100);
            for (EntityNPC npc : npcList) {
                npc.equipDiamondGear();
            }
            player.sendSystemMessage(Component.literal("Did it work???"));
        }

        return Command.SINGLE_SUCCESS;
    }

    public static int startTheHunt(CommandContext<CommandSourceStack> command)
    {
        Player player = getPlayer(command.getSource());
        if (player != null) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            ServerLevel world = server.getLevel(OVERWORLD);

            List<EntityNPC> npcList = getNPCsInRange(world, player.blockPosition(), 100);
            for (EntityNPC npc : npcList) {
                npc.startHunting();
            }
            player.sendSystemMessage(Component.literal("NPC's are hunting for food"));
        }

        return Command.SINGLE_SUCCESS;
    }

    public static int stopTheHunt(CommandContext<CommandSourceStack> command)
    {
        Player player = getPlayer(command.getSource());
        if (player != null) {
            MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
            ServerLevel world = server.getLevel(OVERWORLD);

            List<EntityNPC> npcList = getNPCsInRange(world, player.blockPosition(), 100);
            for (EntityNPC npc : npcList) {
                npc.stopHunting();
            }
            player.sendSystemMessage(Component.literal("NPC's stopped the hunt"));
        }

        return Command.SINGLE_SUCCESS;
    }

    public static int moveToPlayer(CommandContext<CommandSourceStack> command)
    {
        Player player = getPlayer(command.getSource());
        if (player != null) {
            Vec3 playerPosition = player.position();
            sendDestinationMessage(player, playerPosition);

            List<EntityNPC> npcList = getNPCsInRange(Objects.requireNonNull(ServerLifecycleHooks.getCurrentServer().getLevel(OVERWORLD)), player.blockPosition(), 10);
            sendNPCCountMessage(player, npcList.size());
            moveNPCsToTarget(npcList, playerPosition);
        }
        return Command.SINGLE_SUCCESS;
    }

    public static int moveToPos(CommandContext<CommandSourceStack> command) {
        Vec3 destination = Vec3Argument.getVec3(command, "destination");
        Player player = getPlayer(command.getSource());

        if (player != null) {
            sendDestinationMessage(player, destination);
            List<EntityNPC> npcList = getNPCsInRange(Objects.requireNonNull(ServerLifecycleHooks.getCurrentServer().getLevel(OVERWORLD)), player.blockPosition(), 100);
            sendNPCCountMessage(player, npcList.size());
            moveNPCsToTarget(npcList, destination);
        }
        return Command.SINGLE_SUCCESS;
    }
    public static int pause(CommandContext<CommandSourceStack> command) {
        Player player = getPlayer(command.getSource());

        if (player != null) {
            sendControlMessage(player, "Pause");
            controlNPCs(player, false);
        }
        return Command.SINGLE_SUCCESS;
    }
    public static int unpause(CommandContext<CommandSourceStack> command) {
        Player player = getPlayer(command.getSource());

        if (player != null) {
            sendControlMessage(player, "Unpause");
            controlNPCs(player, true);
        }
        return Command.SINGLE_SUCCESS;
    }

    private static void controlNPCs(Player player, boolean continueNPCs) {
        MinecraftServer server = ServerLifecycleHooks.getCurrentServer();
        ServerLevel world = server.getLevel(OVERWORLD);

        List<EntityNPC> npcList = getNPCsInRange(world, player.blockPosition(), 100);
        sendNPCCountMessage(player, npcList.size());

        for (EntityNPC npc : npcList) {
            if (continueNPCs) {
                npc.continueNPC();
            } else {
                npc.stopNPC();
            }
        }
    }

    private static Player getPlayer(CommandSourceStack source) {
        Entity entity = source.getEntity();
        return (entity instanceof Player) ? (Player) entity : null;
    }

    private static void sendDestinationMessage(Player player, Vec3 position) {
        String destinationString = String.format("x: %.4f; z: %.4f; y: %.4f", position.x - OFFSET_X, position.y, position.z - OFFSET_Z);
        player.sendSystemMessage(Component.literal(destinationString));
    }

    private static List<EntityNPC> getNPCsInRange(ServerLevel world, BlockPos playerPosition, double range) {
        return world.getEntitiesOfClass(EntityNPC.class, new AABB(playerPosition).inflate(range));
    }

    private static void sendNPCCountMessage(Player player, int count) {
        player.sendSystemMessage(Component.literal("Number of NPCs: " + count));
    }

    private static void sendControlMessage(Player player, String message) {
        player.sendSystemMessage(Component.literal(message));
    }

    static void moveNPCsToTarget(List<EntityNPC> npcList, Vec3 target) {
        for (EntityNPC npc : npcList) {
            npc.setNewTarget(target);
        }
    }
}