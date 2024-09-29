package com.strangesmell.buckshotroulette.entity;

import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.Items;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import static com.strangesmell.buckshotroulette.BuckshotRoulette.MODID;

@Mod.EventBusSubscriber(modid = MODID, bus = Mod.EventBusSubscriber.Bus.FORGE)
public class DealerInteract {
    @SubscribeEvent
    public static void gatherData(PlayerInteractEvent.EntityInteract event) {
        if(event.getTarget() instanceof Dealer dealer ){
            if(event.getItemStack().is(Items.POTATO)){
                if(!dealer.canJoinPlayer2){
                    dealer.canJoinPlayer2=true;
                    event.getItemStack().shrink(1);
                }

            }else if(event.getItemStack().is(Items.POISONOUS_POTATO)){
                if(dealer.canJoinPlayer2){
                    dealer.canJoinPlayer2=false;
                    event.getItemStack().shrink(1);
                }
            }
        }
        event.setCancellationResult(InteractionResult.SUCCESS);
    }

}
