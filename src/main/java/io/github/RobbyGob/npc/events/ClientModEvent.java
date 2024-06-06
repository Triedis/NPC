package io.github.RobbyGob.npc.events;

import io.github.RobbyGob.npc.NPC_Mod;
import io.github.RobbyGob.npc.client.model.EntityNPCModel;
import io.github.RobbyGob.npc.client.renderer.NPCEntityRenderer;
import io.github.RobbyGob.npc.init.EntityInit;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;


@Mod.EventBusSubscriber(modid = NPC_Mod.MODID, bus = Mod.EventBusSubscriber.Bus.MOD, value = Dist.CLIENT)
public class ClientModEvent {
    @SubscribeEvent
    public static void registerRenderers(EntityRenderersEvent.RegisterRenderers event)
    {
        event.registerEntityRenderer(EntityInit.NPC_ENTITY.get(), NPCEntityRenderer::new);
    }
    @SubscribeEvent
    public static void registerLayerDefinitions(EntityRenderersEvent.RegisterLayerDefinitions event)
    {
       event.registerLayerDefinition(EntityNPCModel.LAYER_LOCATION, EntityNPCModel::createBodyLayer);
    }
}
