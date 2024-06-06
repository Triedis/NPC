package io.github.RobbyGob.npc.events;

import io.github.RobbyGob.npc.NPC_Mod;
import io.github.RobbyGob.npc.entity.EntityNPC;
import io.github.RobbyGob.npc.init.EntityInit;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.EntityAttributeCreationEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(modid = NPC_Mod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class CommonModEvents {
    @SubscribeEvent
    public static void entityAttributes(EntityAttributeCreationEvent event)
    {
        event.put(EntityInit.NPC_ENTITY.get(), EntityNPC.createAttributes().build());

    }
}
