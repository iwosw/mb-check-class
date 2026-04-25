package com.talhanation.bannermod.client.military.models;

import com.talhanation.bannermod.entity.military.AbstractRecruitEntity;
import net.minecraft.client.model.HumanoidModel;
import net.minecraft.client.model.geom.ModelPart;

public class RecruitHumanModel extends HumanoidModel<AbstractRecruitEntity> {
    public RecruitHumanModel(ModelPart root) {
        super(root);
    }

    @Override
    public void setupAnim(AbstractRecruitEntity recruit, float limbSwing, float limbSwingAmount, float ageInTicks, float netHeadYaw, float headPitch) {
        super.setupAnim(recruit, limbSwing, limbSwingAmount, ageInTicks, netHeadYaw, headPitch);
        BetterCombatRecruitPose.apply(this, recruit);
    }
}
