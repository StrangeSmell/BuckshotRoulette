package com.strangesmell.buckshotroulette.structure;

import com.mojang.serialization.Codec;
import com.strangesmell.buckshotroulette.BuckshotRoulette;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.levelgen.WorldgenRandom;
import net.minecraft.world.level.levelgen.structure.Structure;
import net.minecraft.world.level.levelgen.structure.StructureType;
import net.minecraft.world.level.levelgen.structure.pieces.StructurePiecesBuilder;

import java.util.Optional;


public class Room extends Structure {

    //一种简单的方法就是通过Structure提供的方法这样创建，这样的创建简单的结构对于我们来说已经是够用了。
    // 直接传入你的structure的构造方法即可。
    public static final Codec<Room> CODEC = simpleCodec(Room::new);

    public Room(StructureSettings pSettings) {
        super(pSettings);
    }

    @Override
    protected Optional<GenerationStub> findGenerationPoint(GenerationContext pContext) {
        return onTopOfChunkCenter(pContext, Heightmap.Types.WORLD_SURFACE_WG, structurePiecesBuilder -> this.generatePieces(structurePiecesBuilder, pContext));
    }

    private void generatePieces(StructurePiecesBuilder pBuilder, Structure.GenerationContext pContext) {
        ChunkPos chunkpos = pContext.chunkPos();
        WorldgenRandom worldgenrandom = pContext.random();
        BlockPos blockpos = new BlockPos(chunkpos.getMinBlockX()+8, 64, chunkpos.getMinBlockZ()+8);
        Rotation rotation = Rotation.getRandom(worldgenrandom);
        RoomStructurePieces.addPieces(pContext.structureTemplateManager(), blockpos, rotation, pBuilder, worldgenrandom);
    }

    @Override
    public StructureType<?> type() {
        return BuckshotRoulette.ROOM.get();
    }
}
