package com.strangesmell.buckshotroulette.block;

import com.strangesmell.buckshotroulette.BuckshotRoulette;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.data.PackOutput;
import net.minecraft.data.tags.IntrinsicHolderTagsProvider;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraftforge.common.data.ExistingFileHelper;

import java.util.concurrent.CompletableFuture;

import static com.strangesmell.buckshotroulette.BuckshotRoulette.MODID;

public class BlockTagGenerator extends IntrinsicHolderTagsProvider<Block> {
    public BlockTagGenerator(PackOutput output, CompletableFuture<HolderLookup.Provider> future, ExistingFileHelper helper) {
        super(output, Registries.BLOCK, future, block -> block.builtInRegistryHolder().key(), MODID, helper);
    }

    @Override
    protected void addTags(HolderLookup.Provider p_256380_) {
        tag(BlockTags.MINEABLE_WITH_PICKAXE).add(BuckshotRoulette.TableBlock.get());
    }
}