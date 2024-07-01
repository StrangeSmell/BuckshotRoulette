package com.strangesmell.buckshotroulette;

import com.strangesmell.buckshotroulette.block.TableRenderer;
import net.minecraftforge.client.event.EntityRenderersEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(bus = Mod.EventBusSubscriber.Bus.MOD)

public class RegisterRenderer {
    @SubscribeEvent
    public static void  registerRenderer(EntityRenderersEvent.RegisterRenderers event){
        event.registerBlockEntityRenderer(BuckshotRoulette.TableBlockEntity.get(), TableRenderer::new);

    }
}
