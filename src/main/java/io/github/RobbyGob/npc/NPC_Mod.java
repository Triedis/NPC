package io.github.RobbyGob.npc;

import io.github.RobbyGob.npc.init.EntityInit;
import io.github.RobbyGob.npc.item.ModItems;
import net.minecraft.world.item.CreativeModeTabs;
import net.minecraftforge.event.BuildCreativeModeTabContentsEvent;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;

@Mod(NPC_Mod.MODID)
public class NPC_Mod {
    public static final String MODID = "npcmod";
    public NPC_Mod(){
        IEventBus bus = FMLJavaModLoadingContext.get().getModEventBus();
        ModItems.register(bus);
        EntityInit.ENTITIES.register(bus);
        bus.addListener(this::addCreative);
    }
    private void addCreative(BuildCreativeModeTabContentsEvent event)
    {
        if (event.getTabKey() == CreativeModeTabs.TOOLS_AND_UTILITIES)
        {
            event.accept(ModItems.NPC_CONTROLLER);
            event.accept(ModItems.SPAWN_NPC);
        }
    }
}
