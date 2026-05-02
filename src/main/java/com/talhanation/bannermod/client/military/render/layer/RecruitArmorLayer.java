package com.talhanation.bannermod.client.military.render.layer;

import com.talhanation.bannermod.config.RecruitsClientConfig;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.VillagerHeadModel;
import net.minecraft.client.model.VillagerModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;
import net.minecraft.client.renderer.entity.SheepRenderer;

public class RecruitArmorLayer extends HumanoidModel {
    public RecruitArmorLayer(ModelPart p_170677_) {
        super(p_170677_);
    }
    //VillagerModel
    public static LayerDefinition createOuterArmorLayer() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(new CubeDeformation(1.0F), 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();

        // Villager armor layer removed since human model is forced

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    public static LayerDefinition createInnerArmorLayer() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(new CubeDeformation(0.51F), 0.0F);
        return LayerDefinition.create(meshdefinition, 64, 32);
    }
}
