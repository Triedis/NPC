package io.github.RobbyGob.npc.init;

import io.github.RobbyGob.npc.entity.EntityNPC;
import io.github.RobbyGob.npc.NPC_Mod;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

public class EntityInit {
    public static final DeferredRegister<EntityType<?>> ENTITIES = DeferredRegister.create(ForgeRegistries.ENTITY_TYPES, NPC_Mod.MODID);
    public static final RegistryObject<EntityType<EntityNPC>> NPC_ENTITY = ENTITIES.register("npc",
            () -> EntityType.Builder.<EntityNPC>of(EntityNPC::new, MobCategory.CREATURE)
                    .sized(1.0f, 2.0f)
                    .build(new ResourceLocation(NPC_Mod.MODID, "npc").toString())
    );
}
