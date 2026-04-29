package com.talhanation.bannermod.client.civilian.render;

import com.talhanation.bannermod.client.military.render.RecruitVillagerRenderer;
import net.minecraft.client.renderer.entity.EntityRendererProvider;


public class WorkerVillagerRenderer extends RecruitVillagerRenderer implements IRenderWorkArea{
    public WorkerVillagerRenderer(EntityRendererProvider.Context mgr) {
        super(mgr);
    }
}
