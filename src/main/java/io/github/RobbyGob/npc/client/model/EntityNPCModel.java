package io.github.RobbyGob.npc.client.model;

import io.github.RobbyGob.npc.NPC_Mod;
import io.github.RobbyGob.npc.entity.EntityNPC;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.PlayerModel;
import net.minecraft.client.model.geom.ModelLayerLocation;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.resources.ResourceLocation;

public class EntityNPCModel<T extends EntityNPC> extends HumanoidModel<T> {
	public static final ModelLayerLocation LAYER_LOCATION = new ModelLayerLocation(new ResourceLocation(NPC_Mod.MODID, "npc"), "main");

	public EntityNPCModel(ModelPart p_170677_) {
		super(p_170677_);
	}

	public static LayerDefinition createBodyLayer() {
		MeshDefinition meshdefinition = HumanoidModel.createMesh(CubeDeformation.NONE, 0.0f);
		return LayerDefinition.create(meshdefinition, 64, 64);
	}

}