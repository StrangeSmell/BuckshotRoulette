package com.strangesmell.buckshotroulette.entity;

import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.renderer.entity.EntityRendererProvider;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.ItemInHandLayer;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

@OnlyIn(Dist.CLIENT)
public class DealerRenderer extends MobRenderer<Dealer, DealerModel> {
    private static final ResourceLocation VEX_LOCATION = new ResourceLocation("minecraft","textures/entity/illager/vex.png");
    private static final ResourceLocation VEX_CHARGING_LOCATION = new ResourceLocation("minecraft","textures/entity/illager/vex_charging.png");

    public DealerRenderer(EntityRendererProvider.Context p_174435_) {
        super(p_174435_, new DealerModel(p_174435_.bakeLayer(ModelLayers.VEX)), 0.3F);
        this.addLayer(new ItemInHandLayer(this, p_174435_.getItemInHandRenderer()));
    }

    @Override
    public ResourceLocation getTextureLocation(Dealer dealer) {
        return  VEX_LOCATION;
    }

    protected int getBlockLightLevel(Dealer p_116298_, BlockPos p_116299_) {
        return 15;
    }

}
