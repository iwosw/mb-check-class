package com.talhanation.bannermod.client.military.render.layer;

import com.talhanation.bannermod.config.RecruitsClientConfig;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.model.geom.PartPose;
import net.minecraft.client.model.geom.builders.*;

public class RecruitArmorLayer extends HumanoidModel {
    public RecruitArmorLayer(ModelPart root) {
        super(root);
    }

    public static LayerDefinition createOuterArmorLayer() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(new CubeDeformation(1.0F), 0.0F);
        PartDefinition partdefinition = meshdefinition.getRoot();

        if(RecruitsClientConfig.RecruitsLookLikeVillagers.get()) {
            partdefinition.addOrReplaceChild("head",
                    CubeListBuilder.create()
                            .texOffs(0, 0)
                            .addBox(-4.0F, -10.0F, -4.0F, 8.0F, 8.0F, 8.0F,
                                    new CubeDeformation(1.0F)),
                    PartPose.offset(0.0F, 1.0F, 0.0F));
        }

        return LayerDefinition.create(meshdefinition, 64, 32);
    }

    public static LayerDefinition createInnerArmorLayer() {
        MeshDefinition meshdefinition = HumanoidModel.createMesh(new CubeDeformation(0.51F), 0.0F);
        return LayerDefinition.create(meshdefinition, 64, 32);
    }
}
