package io.github.RobbyGob.npc.item;

import io.github.RobbyGob.npc.NPC_Mod;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Rarity;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class ModItems {
    public static DeferredRegister<Item> ITEMS =
            DeferredRegister.create(ForgeRegistries.ITEMS, NPC_Mod.MODID);
    public static final RegistryObject<Item> NPC_CONTROLLER = ITEMS.register("npc_controller", () -> new Item(new Item.Properties().stacksTo(1).rarity(Rarity.UNCOMMON)));
    public static final RegistryObject<Item> SPAWN_NPC = ITEMS.register("spawn_npc", () -> new Item(new Item.Properties().rarity(Rarity.UNCOMMON)));
    public static void register(IEventBus eventBus)
    {
        ITEMS.register(eventBus);
    }
}
