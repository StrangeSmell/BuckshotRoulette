package com.strangesmell.buckshotroulette;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.config.ModConfigEvent;


@Mod.EventBusSubscriber(modid = BuckshotRoulette.MODID, bus = Mod.EventBusSubscriber.Bus.MOD)
public class Config
{
    private static final ForgeConfigSpec.Builder BUILDER = new ForgeConfigSpec.Builder();

    private static final ForgeConfigSpec.BooleanValue HAVE_STAKE = BUILDER
            .comment("Whether or not to take life as a stake.")
            .comment("是否抽取生命作为赌注。")
            .define("have_stake", true);

    static final ForgeConfigSpec SPEC = BUILDER.build();

    public static boolean have_stake;

    @SubscribeEvent
    static void onLoad(final ModConfigEvent event)
    {
        have_stake = HAVE_STAKE.get();
    }
}
