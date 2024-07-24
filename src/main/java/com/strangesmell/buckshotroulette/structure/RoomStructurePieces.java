package com.strangesmell.buckshotroulette.structure;

import com.google.common.collect.ImmutableMap;
import com.strangesmell.buckshotroulette.BuckshotRoulette;
import com.strangesmell.buckshotroulette.block.TableBlock;
import com.strangesmell.buckshotroulette.entity.Dealer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BarrelBlockEntity;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.ChestBlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.block.state.properties.StructureMode;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.structure.BoundingBox;
import net.minecraft.world.level.levelgen.structure.StructurePieceAccessor;
import net.minecraft.world.level.levelgen.structure.TemplateStructurePiece;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePieceSerializationContext;
import net.minecraft.world.level.levelgen.structure.templatesystem.BlockIgnoreProcessor;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.level.storage.loot.BuiltInLootTables;

import java.util.Map;
import java.util.Random;

import static com.strangesmell.buckshotroulette.BuckshotRoulette.MODID;

public class RoomStructurePieces {
    static final ResourceLocation STRUCTURE_LOCATION_ROOM = new ResourceLocation(MODID,"deliciousness");
    static final Map<ResourceLocation, BlockPos> PIVOTS = ImmutableMap.of(
            STRUCTURE_LOCATION_ROOM, new BlockPos(4, 0, 4)
    );
    static final Map<ResourceLocation, BlockPos> OFFSETS = ImmutableMap.of(
            STRUCTURE_LOCATION_ROOM, BlockPos.ZERO
    );

    public static void addPieces(
            StructureTemplateManager pStructureTemplateManager, BlockPos pStartPos, Rotation pRotation, StructurePieceAccessor pPieces, RandomSource pRandom
    ) {
        pPieces.addPiece(new RoomStructurePieces.RoomStructurePiece(pStructureTemplateManager, STRUCTURE_LOCATION_ROOM, pStartPos, pRotation, 0));
    }

    public static class RoomStructurePiece extends TemplateStructurePiece {

        public RoomStructurePiece(StructureTemplateManager pStructureTemplateManager, CompoundTag pTag) {
            super(BuckshotRoulette.RoomPieceType.get(), pTag, pStructureTemplateManager,   resourceLocation -> makeSettings(Rotation.valueOf(pTag.getString("Rot")), resourceLocation));
        }

        public RoomStructurePiece(StructureTemplateManager pStructureTemplateManager, ResourceLocation pLocation, BlockPos pStartPos, Rotation pRotation, int pDown) {
            super(BuckshotRoulette.RoomPieceType.get(), 0, pStructureTemplateManager, pLocation, pLocation.toString(),
                    makeSettings(pRotation, pLocation),
                    makePosition(pLocation, pStartPos, pDown)
            );
        }



        private static StructurePlaceSettings makeSettings(Rotation pRotation, ResourceLocation pLocation) {
            return new StructurePlaceSettings()
                    .setRotation(pRotation)
                    .setMirror(Mirror.NONE)
                    .setRotationPivot(RoomStructurePieces.PIVOTS.get(pLocation))
                    .addProcessor(BlockIgnoreProcessor.STRUCTURE_BLOCK);
        }

        private static BlockPos makePosition(ResourceLocation pLocation, BlockPos pPos, int pDown) {
            return pPos.offset(RoomStructurePieces.OFFSETS.get(pLocation)).below(2);
        }
        @Override
        protected void addAdditionalSaveData(StructurePieceSerializationContext pContext, CompoundTag pTag) {
            super.addAdditionalSaveData(pContext, pTag);
            pTag.putString("Rot", this.placeSettings.getRotation().name());
        }

        @Override
        protected void handleDataMarker(String pName, BlockPos pPos, ServerLevelAccessor pLevel, RandomSource pRandom, BoundingBox pBox) {

        }

        @Override
        public void postProcess(
                WorldGenLevel pLevel,
                StructureManager pStructureManager,
                ChunkGenerator pGenerator,
                RandomSource pRandom,
                BoundingBox pBox,
                ChunkPos pChunkPos,
                BlockPos pPos
        ) {
            super.postProcess(pLevel, pStructureManager, pGenerator, pRandom, pBox, pChunkPos, pPos);
            this.placeSettings.setBoundingBox(pBox);
            this.boundingBox = this.template.getBoundingBox(this.placeSettings, this.templatePosition);
            if (this.template.placeInWorld(pLevel, this.templatePosition, pPos, this.placeSettings, pRandom, 2)) {
                for(StructureTemplate.StructureBlockInfo structuretemplate$structureblockinfo : this.template.filterBlocks(this.templatePosition, this.placeSettings, Blocks.BARREL)) {
                    if (structuretemplate$structureblockinfo.pos() != null) {
                        if(pLevel.getBlockEntity(structuretemplate$structureblockinfo.pos()) instanceof BarrelBlockEntity barrelBlockEntity){
                            if(pLevel.getRandom().nextFloat()>0.99)  barrelBlockEntity.setItem(pLevel.getRandom().nextInt(0,28),new ItemStack(BuckshotRoulette.Heart.get()));
                        }
                    }
                }
            }
        }
    }
}
