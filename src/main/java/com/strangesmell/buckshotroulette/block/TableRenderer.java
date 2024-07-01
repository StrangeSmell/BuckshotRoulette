package com.strangesmell.buckshotroulette.block;

import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import com.strangesmell.buckshotroulette.BuckshotRoulette;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.model.geom.ModelLayers;
import net.minecraft.client.model.geom.ModelPart;
import net.minecraft.client.particle.DustParticle;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.core.BlockPos;
import net.minecraft.core.particles.DustParticleOptions;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.properties.ChestType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.joml.Vector3f;

import java.util.Calendar;
@OnlyIn(Dist.CLIENT)

public class TableRenderer implements BlockEntityRenderer<TableBlockEntity> {
    private final ItemRenderer itemRenderer;
    public int playerAddTimeMax = 20;
    public double onePix = 0.03125;
    private final ModelPart lid;
    private final ModelPart bottom;
    private final ModelPart lock;
    private final ModelPart lid2;
    private final ModelPart bottom2;
    private final ModelPart lock2;
    private boolean xmasTextures;
    private final Font font;

    public TableRenderer(BlockEntityRendererProvider.Context pContext) {
        Calendar calendar = Calendar.getInstance();
        this.itemRenderer = pContext.getItemRenderer();
        ModelPart modelpart = pContext.bakeLayer(ModelLayers.CHEST);
        ModelPart modelpart2 = pContext.bakeLayer(ModelLayers.CHEST);
        this.bottom = modelpart.getChild("bottom");
        this.lid = modelpart.getChild("lid");
        this.lock = modelpart.getChild("lock");
        this.bottom2 = modelpart.getChild("bottom");
        this.lid2 = modelpart.getChild("lid");
        this.lock2 = modelpart.getChild("lock");
        if (calendar.get(2) + 1 == 12 && calendar.get(5) >= 24 && calendar.get(5) <= 26) {
            this.xmasTextures = true;
        }
        this.font = pContext.getFont();
    }

    @Override
    public void render(TableBlockEntity blockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        pPackedLight = LevelRenderer.getLightColor(blockEntity.getLevel(), blockEntity.getBlockPos().above());
        renderName(blockEntity, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
        if(blockEntity.end) return;
        renderWinNum(blockEntity, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
        float f1;
        int oRoundBeginTime ;
        if(blockEntity.tntStartTime==0){
            oRoundBeginTime=getNextNum(blockEntity.roundBeginTime);
        }else{
            oRoundBeginTime=blockEntity.roundBeginTime;
        }
        f1 = Mth.lerp(pPartialTick, (float) (blockEntity.roundBeginTime) / (float) (blockEntity.roundBeginTimeMax), (float) (oRoundBeginTime) / (float) (blockEntity.roundBeginTimeMax));
        f1 = f1 * f1 * f1;

        renderTNT(blockEntity, pPoseStack, pBuffer, pPackedLight, pPackedOverlay, pPartialTick);

        renderHealth(blockEntity, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);

        if (blockEntity.roundBegin) {
            renderAmmunition(blockEntity, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);
        }
        renderWeb(blockEntity, pPoseStack, pBuffer, pPackedLight, pPackedOverlay);

        if (blockEntity.player1AddItemTime > 0) {
            renderAddItem1(blockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, pPackedOverlay, f1);
        }
        if (blockEntity.player2AddItemTime > 0) {
            renderAddItem2(blockEntity, pPartialTick, pPoseStack, pBuffer, pPackedLight, pPackedOverlay, f1);
        }
        renderMoveItem1(blockEntity, pPoseStack, pBuffer, pPackedLight, pPackedOverlay, pPartialTick);
        renderMoveItem2(blockEntity, pPoseStack, pBuffer, pPackedLight, pPackedOverlay, pPartialTick);

        for (int i = 0; i < 8; i++) {
            if (blockEntity.item1Time.get(i) == 0) {
                renderTable1Item(blockEntity, pPoseStack, pBuffer, pPackedLight, pPackedOverlay, blockEntity.player1.get(i), i);
            }
            if (blockEntity.item2Time.get(i) == 0) {
                renderTable2Item(blockEntity, pPoseStack, pBuffer, pPackedLight, pPackedOverlay, blockEntity.player2.get(i), i);
            }
        }

        if (blockEntity.roundBeginTime == 0) {
            if (blockEntity.roundBegin) {
                renderChest(blockEntity, pPoseStack, pBuffer, pPackedLight, pPackedOverlay, 0, pPartialTick);
            }
        } else {
            renderChest(blockEntity, pPoseStack, pBuffer, pPackedLight, pPackedOverlay, f1, pPartialTick);
        }


    }

    @OnlyIn(Dist.CLIENT)
    public static void function2(TableBlockEntity blockEntity){
        BlockPos blockPos = blockEntity.getBlockPos();
        Level level = blockEntity.getLevel();
        level.playSound(Minecraft.getInstance().player, blockPos,SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4, (1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F);

        for(int i=0;i<10;i++){
            DustParticle particle = (DustParticle)Minecraft.getInstance().particleEngine.createParticle(new DustParticleOptions(new Vector3f(0,0,0),4),blockPos.getX()+0.5,blockPos.getY()+1.1,blockPos.getZ(),0,0,0);

            Minecraft.getInstance().particleEngine.add(particle);
        }
    }
    @OnlyIn(Dist.CLIENT)
    public static void function1(TableBlockEntity blockEntity){

        BlockPos blockPos = blockEntity.getBlockPos();
        Level level = blockEntity.getLevel();
        level.playSound(Minecraft.getInstance().player, blockPos,SoundEvents.GENERIC_EXPLODE, SoundSource.BLOCKS, 4, (1.0F + (level.random.nextFloat() - level.random.nextFloat()) * 0.2F) * 0.7F);

        for(int i=0;i<10;i++){
            DustParticle particle = (DustParticle)Minecraft.getInstance().particleEngine.createParticle(new DustParticleOptions(new Vector3f(0,0,0),4),blockPos.getX()+0.5,blockPos.getY()+1.1,blockPos.getZ()+1,0,0,0);

            Minecraft.getInstance().particleEngine.add(particle);
        }
    }

    private void renderMoveItem1(TableBlockEntity blockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay, float pPartialTick) {
        int size = blockEntity.moveItem1.size();
        for (int index = 0; index < size; index++) {
            if (blockEntity.moveItem1.get(index).isEmpty()) continue;
            float f1 = Mth.lerp(pPartialTick, 1 - (float) blockEntity.moveItem1Time[index] / 20, 1 - (float) getNextNum(blockEntity.moveItem1Time[index]) / 20);
            f1 = f1 * f1 * f1;
            pPoseStack.pushPose();
            pPoseStack.translate(0, 1, 0);
            if (index > 3) {
                if (index - 4 == 0) {
                    pPoseStack.translate(4 * onePix, 0, 28 * onePix);
                    pPoseStack.translate(12 * onePix * f1, 0, -12 * onePix * f1);
                } else if (index - 4 == 1) {
                    pPoseStack.translate(9 * onePix, 0, 28 * onePix);
                    pPoseStack.translate(7 * onePix * f1, 0, -12 * onePix * f1);
                } else if (index - 4 == 2) {
                    pPoseStack.translate(23 * onePix, 0, 28 * onePix);
                    pPoseStack.translate(-7 * onePix * f1, 0, -12 * onePix * f1);
                } else if (index - 4 == 3) {
                    pPoseStack.translate(28 * onePix, 0, 28 * onePix);
                    pPoseStack.translate(-12 * onePix * f1, 0, -12 * onePix * f1);
                }
            } else {
                if (index == 0) {
                    pPoseStack.translate(4 * onePix, 0, 23 * onePix);
                    pPoseStack.translate(12 * onePix * f1, 0, -7 * onePix * f1);
                } else if (index == 1) {
                    pPoseStack.translate(9 * onePix, 0, 23 * onePix);
                    pPoseStack.translate(7 * onePix * f1, 0, -7 * onePix * f1);
                } else if (index == 2) {
                    pPoseStack.translate(23 * onePix, 0, 23 * onePix);
                    pPoseStack.translate(-7 * onePix * f1, 0, -7 * onePix * f1);
                } else if (index == 3) {
                    pPoseStack.translate(28 * onePix, 0, 23 * onePix);
                    pPoseStack.translate(-12 * onePix * f1, 0, -7 * onePix * f1);
                }
            }

            pPoseStack.mulPose(Axis.XP.rotationDegrees(-90));
            pPoseStack.scale(0.125f, 0.125f, 0.125f);

            this.itemRenderer.renderStatic(blockEntity.moveItem1.get(index), ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, blockEntity.getLevel(), 0);
            pPoseStack.popPose();
        }

    }

    private void renderMoveItem2(TableBlockEntity blockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay, float pPartialTick) {
        int size = blockEntity.moveItem2.size();
        for (int index = 0; index < size; index++) {
            if (blockEntity.moveItem2.get(index).isEmpty()) continue;
            float f1 = Mth.lerp(pPartialTick, 1 - (float) blockEntity.moveItem2Time[index] / 20, 1 - (float) getNextNum(blockEntity.moveItem2Time[index]) / 20);
            f1 = f1 * f1 * f1;
            pPoseStack.pushPose();
            pPoseStack.translate(0, 1, 0);
            if (index > 3) {
                if (index - 4 == 0) {
                    pPoseStack.translate(28 * onePix, 0, 4 * onePix);
                    pPoseStack.translate(-12 * onePix * f1, 0, 12 * onePix * f1);
                } else if (index - 4 == 1) {
                    pPoseStack.translate(23 * onePix, 0, 4 * onePix);
                    pPoseStack.translate(-7 * onePix * f1, 0, 12 * onePix * f1);
                } else if (index - 4 == 2) {
                    pPoseStack.translate(9 * onePix, 0, 4 * onePix);
                    pPoseStack.translate(7 * onePix * f1, 0, 12 * onePix * f1);
                } else if (index - 4 == 3) {
                    pPoseStack.translate(4 * onePix, 0, 4 * onePix);
                    pPoseStack.translate(12 * onePix * f1, 0, 12 * onePix * f1);
                }
            } else {
                if (index == 0) {
                    pPoseStack.translate(28 * onePix, 0, 9 * onePix);
                    pPoseStack.translate(-12 * onePix * f1, 0, 7 * onePix * f1);
                } else if (index == 1) {
                    pPoseStack.translate(23 * onePix, 0, 9 * onePix);
                    pPoseStack.translate(-7 * onePix * f1, 0, 7 * onePix * f1);
                } else if (index == 2) {
                    pPoseStack.translate(9 * onePix, 0, 9 * onePix);
                    pPoseStack.translate(7 * onePix * f1, 0, 7 * onePix * f1);
                } else if (index == 3) {
                    pPoseStack.translate(4 * onePix, 0, 9 * onePix);
                    pPoseStack.translate(12 * onePix * f1, 0, 7 * onePix * f1);
                }
            }
            pPoseStack.mulPose(Axis.XP.rotationDegrees(90));
            pPoseStack.scale(0.125f, 0.125f, 0.125f);
            this.itemRenderer.renderStatic(blockEntity.moveItem2.get(index), ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, blockEntity.getLevel(), 0);
            pPoseStack.popPose();
        }

    }

    private void renderWinNum(TableBlockEntity blockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        if (!blockEntity.name1.equals("")) {
            pPoseStack.pushPose();
            pPoseStack.translate(1, 1 + 0.00390625, 0.5 + 0.0625f * 1.5);
            pPoseStack.mulPose(Axis.XP.rotationDegrees(-90));
            pPoseStack.scale(0.0625f, 0.0625f, 0.0625f);
            pPoseStack.translate(0.5, 0, 0);
            for (int i = 0; i < 2; i++) {
                pPoseStack.translate(-1, 0, 0);
                if (i <= blockEntity.player1WinNum - 1) {
                    itemRenderer.renderStatic(new ItemStack(Items.ENDER_EYE), ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, blockEntity.getLevel(), 0);
                } else {
                    itemRenderer.renderStatic(new ItemStack(Items.ENDER_PEARL), ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, blockEntity.getLevel(), 0);
                }
            }
            pPoseStack.popPose();
        }
        if (!blockEntity.name2.equals("")) {
            pPoseStack.pushPose();
            pPoseStack.translate(1, 1 + 0.00390625, 0.5 - 0.0625f * 1.5);
            pPoseStack.mulPose(Axis.XP.rotationDegrees(90));
            pPoseStack.scale(0.0625f, 0.0625f, 0.0625f);
            pPoseStack.translate(0.5, 0, 0);
            for (int i = 0; i < 2; i++) {
                pPoseStack.translate(-1, 0, 0);
                if (i <= blockEntity.player2WinNum - 1) {
                    itemRenderer.renderStatic(new ItemStack(Items.ENDER_EYE), ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, blockEntity.getLevel(), 0);
                } else {
                    itemRenderer.renderStatic(new ItemStack(Items.ENDER_PEARL), ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, blockEntity.getLevel(), 0);
                }
            }
            pPoseStack.popPose();
        }
    }

    private void renderHealth(TableBlockEntity blockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        if (blockEntity.end) return;

        pPoseStack.pushPose();
        pPoseStack.translate(0, 1 + 0.00390625, 0.5 + 0.0625f * 1.5);
        pPoseStack.mulPose(Axis.XP.rotationDegrees(-90));
        pPoseStack.scale(0.0625f, 0.0625f, 0.0625f);
        pPoseStack.translate(-0.5, 0, 0);

        for (int i = 0; i < blockEntity.oldHealth1; i++) {
            pPoseStack.translate(1, 0, 0);
            itemRenderer.renderStatic(new ItemStack(BuckshotRoulette.Heart.get()), ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, blockEntity.getLevel(), 0);

        }
        pPoseStack.popPose();

        pPoseStack.pushPose();
        pPoseStack.translate(0, 1 + 0.00390625, 0.5 - 0.0625f * 1.5);
        pPoseStack.mulPose(Axis.XP.rotationDegrees(90));
        pPoseStack.scale(0.0625f, 0.0625f, 0.0625f);
        pPoseStack.translate(-0.5, 0, 0);
        for (int i = 0; i < blockEntity.oldHealth2; i++) {
            pPoseStack.translate(1, 0, 0);
            itemRenderer.renderStatic(new ItemStack(BuckshotRoulette.Heart.get()), ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, blockEntity.getLevel(), 0);

        }
        pPoseStack.popPose();
    }

    private void renderName(TableBlockEntity blockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        pPoseStack.pushPose();
        pPoseStack.translate(0, 1.01, 0.5);
        pPoseStack.mulPose(Axis.XP.rotationDegrees(90));
        pPoseStack.scale(0.0078125f, 0.0078125f, 0.0078125f);
        this.font.drawInBatch(blockEntity.name1, 0, 0, 0xFFFFFF, false, pPoseStack.last().pose(), pBuffer, Font.DisplayMode.NORMAL, 0x00FFFFFF, pPackedLight);
        pPoseStack.popPose();

        pPoseStack.pushPose();
        pPoseStack.translate(this.font.width(blockEntity.name2) * 0.0078125f, 1.01, 0.5);

        pPoseStack.mulPose(Axis.XP.rotationDegrees(90));
        pPoseStack.mulPose(Axis.ZP.rotationDegrees(180));
        pPoseStack.scale(0.0078125f, 0.0078125f, 0.0078125f);
        this.font.drawInBatch(blockEntity.name2, 0, 0, 0xFFFFFF, false, pPoseStack.last().pose(), pBuffer, Font.DisplayMode.NORMAL, 0x00FFFFFF, pPackedLight);
        pPoseStack.popPose();
    }

    private void renderTNT(TableBlockEntity blockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay, float pPartialTick) {
        float f1;
        f1 = Mth.lerp(pPartialTick, (float) (blockEntity.tntUpTime) / (float) (blockEntity.roundBeginTimeMax), (float) (getNextNum(blockEntity.tntUpTime)) / (float) (blockEntity.roundBeginTimeMax));
        f1 = f1 * f1 * f1;
        pPoseStack.pushPose();
        pPoseStack.translate(0.5, 1 + 2 * onePix, 0.5);
        if (blockEntity.tntUpTime >= 0) {
            pPoseStack.translate(0, -1, 0);
            pPoseStack.translate(0, 1 - f1, 0);
        } else {
            pPoseStack.translate(0, -1, 0);
            pPoseStack.translate(0, -f1, 0);
        }

        if (blockEntity.tntStartTime > 0) {
            float f2 = Mth.lerp(pPartialTick, (float) blockEntity.tntStartTime / (float) blockEntity.roundBeginTimeMax, (float) getNextNum(blockEntity.tntStartTime )/ (float) blockEntity.roundBeginTimeMax);
            f2 = f2 * f2 * f2;
            pPoseStack.translate(0, 0, (1 - f2) / 3);
            if (blockEntity.tntStartTime == 1) {
                blockEntity.tntUpTime = blockEntity.roundBeginTimeMax;
            }
        } else if (blockEntity.tntStartTime < 0) {
            float f2 = Mth.lerp(pPartialTick, (float) blockEntity.tntStartTime / (float) blockEntity.roundBeginTimeMax, (float) getNextNum(blockEntity.tntStartTime) / (float) blockEntity.roundBeginTimeMax);
            f2 = f2 * f2 * f2;
            pPoseStack.translate(0, 0, (-f2 - 1) / 3);
            if (blockEntity.tntStartTime == -1) {
                blockEntity.tntUpTime = blockEntity.roundBeginTimeMax;
            }
        }

        pPoseStack.scale(0.25f, 0.25f, 0.25f);
        itemRenderer.renderStatic(new ItemStack(Items.TNT), ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, blockEntity.getLevel(), 0);
        pPoseStack.popPose();
    }

    private void renderAmmunition(TableBlockEntity blockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        for (int i = 0; i < blockEntity.badAmmunition; i++) {
            pPoseStack.pushPose();
            pPoseStack.translate(22 * onePix + i * onePix, 1.025, 0.5);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(90));
            if (i == blockEntity.ammunition - 1) {
                pPoseStack.translate(0, -0.015, 0);
                pPoseStack.mulPose(Axis.XP.rotationDegrees(90));
            } else {
                pPoseStack.mulPose(Axis.XP.rotationDegrees(75));
            }
            pPoseStack.scale(0.15625f, 0.15625f, 0.15625f);
            itemRenderer.renderStatic(new ItemStack(Items.REDSTONE), ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, blockEntity.getLevel(), 0);
            pPoseStack.popPose();
        }

        for (int i = 0; i < blockEntity.goodAmmunition; i++) {
            pPoseStack.pushPose();
            pPoseStack.translate(22 * onePix + (i + blockEntity.badAmmunition) * onePix, 1.025, 0.5);
            pPoseStack.mulPose(Axis.YP.rotationDegrees(90));
            if (i == blockEntity.goodAmmunition - 1) {
                pPoseStack.translate(0, -0.015, 0);
                pPoseStack.mulPose(Axis.XP.rotationDegrees(90));
            } else {
                pPoseStack.mulPose(Axis.XP.rotationDegrees(75));
            }
            pPoseStack.scale(0.15625f, 0.15625f, 0.15625f);
            itemRenderer.renderStatic(new ItemStack(Items.GUNPOWDER), ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, blockEntity.getLevel(), 0);
            pPoseStack.popPose();
        }
    }

    private void renderWeb(TableBlockEntity blockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay) {
        if (blockEntity.player1IsWeb) {
            pPoseStack.pushPose();
            pPoseStack.translate(0.5, 1, 0.75);
            pPoseStack.mulPose(Axis.XP.rotationDegrees(-90));
            pPoseStack.scale(0.5f, 0.5f, 0.5f);
            itemRenderer.renderStatic(new ItemStack(Items.COBWEB), ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, blockEntity.getLevel(), 0);
            pPoseStack.popPose();
        }

        if (blockEntity.player2IsWeb) {
            pPoseStack.pushPose();
            pPoseStack.translate(0.5, 1, 0.25);
            pPoseStack.mulPose(Axis.XP.rotationDegrees(90));
            pPoseStack.scale(0.5f, 0.5f, 0.5f);
            itemRenderer.renderStatic(new ItemStack(Items.COBWEB), ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, blockEntity.getLevel(), 0);
            pPoseStack.popPose();
        }

    }

    private void renderTable1Item(TableBlockEntity blockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay, ItemStack itemStack, int index) {
        pPoseStack.pushPose();
        pPoseStack.translate(0, 1, 0);
        if (index > 3) {
            if (index - 4 == 0) {
                pPoseStack.translate(4 * onePix, 0, 28 * onePix);
            } else if (index - 4 == 1) {
                pPoseStack.translate(9 * onePix, 0, 28 * onePix);
            } else if (index - 4 == 2) {
                pPoseStack.translate(23 * onePix, 0, 28 * onePix);
            } else if (index - 4 == 3) {
                pPoseStack.translate(28 * onePix, 0, 28 * onePix);
            }
        } else {
            if (index == 0) {
                pPoseStack.translate(4 * onePix, 0, 23 * onePix);
            } else if (index == 1) {
                pPoseStack.translate(9 * onePix, 0, 23 * onePix);
            } else if (index == 2) {
                pPoseStack.translate(23 * onePix, 0, 23 * onePix);
            } else if (index == 3) {
                pPoseStack.translate(28 * onePix, 0, 23 * onePix);
            }
        }
        pPoseStack.mulPose(Axis.XP.rotationDegrees(-90));

        pPoseStack.scale(0.125f, 0.125f, 0.125f);

        this.itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, blockEntity.getLevel(), 0);
        pPoseStack.popPose();
    }

    private void renderTable2Item(TableBlockEntity blockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay, ItemStack itemStack, int index) {
        pPoseStack.pushPose();
        pPoseStack.translate(0, 1, 0);
        if (index > 3) {
            if (index - 4 == 0) {
                pPoseStack.translate(28 * onePix, 0, 4 * onePix);
            } else if (index - 4 == 1) {
                pPoseStack.translate(23 * onePix, 0, 4 * onePix);
            } else if (index - 4 == 2) {
                pPoseStack.translate(9 * onePix, 0, 4 * onePix);
            } else if (index - 4 == 3) {
                pPoseStack.translate(4 * onePix, 0, 4 * onePix);
            }
        } else {
            if (index == 0) {
                pPoseStack.translate(28 * onePix, 0, 9 * onePix);
            } else if (index == 1) {
                pPoseStack.translate(23 * onePix, 0, 9 * onePix);
            } else if (index == 2) {
                pPoseStack.translate(9 * onePix, 0, 9 * onePix);
            } else if (index == 3) {
                pPoseStack.translate(4 * onePix, 0, 9 * onePix);
            }
        }
        pPoseStack.mulPose(Axis.XP.rotationDegrees(90));
        pPoseStack.scale(0.125f, 0.125f, 0.125f);
        this.itemRenderer.renderStatic(itemStack, ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, blockEntity.getLevel(), 0);
        pPoseStack.popPose();
    }

    private void renderAddItem1(TableBlockEntity blockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay, float roundBeginTime) {
        pPoseStack.pushPose();
        float f1 = Mth.lerp(pPartialTick, (float) (playerAddTimeMax - blockEntity.player1AddItemTime) / (float) playerAddTimeMax, (float) (playerAddTimeMax -getNextNum(blockEntity.player1AddItemTime)) / (float) playerAddTimeMax);
        pPoseStack.translate(0.5, f1 * 0.15 + 1, 28 * onePix);
        pPoseStack.mulPose(Axis.XP.rotationDegrees(-90));
        pPoseStack.scale(0.1093f, 0.1093f, 0.1093f);
        //pPoseStack.scale(0.109375f, 0.109375f, 0.109375f);
        this.itemRenderer.renderStatic(blockEntity.addItem1.get(0), ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, blockEntity.getLevel(), 0);
        pPoseStack.popPose();
    }

    private void renderAddItem2(TableBlockEntity blockEntity, float pPartialTick, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay, float roundBeginTime) {
        pPoseStack.pushPose();
        float f1 = Mth.lerp(pPartialTick, (float) (playerAddTimeMax - blockEntity.player2AddItemTime) / (float) playerAddTimeMax, (float) (playerAddTimeMax - getNextNum( blockEntity.player2AddItemTime)) / (float) playerAddTimeMax);
        pPoseStack.translate(0.5, f1 * 0.15 + 1, 4 * onePix);
        pPoseStack.mulPose(Axis.XP.rotationDegrees(90));
        pPoseStack.scale(0.1093f, 0.1093f, 0.1093f);
        this.itemRenderer.renderStatic(blockEntity.addItem2.get(0), ItemDisplayContext.FIXED, pPackedLight, pPackedOverlay, pPoseStack, pBuffer, blockEntity.getLevel(), 0);
        pPoseStack.popPose();
    }

    private void renderChest(TableBlockEntity blockEntity, PoseStack pPoseStack, MultiBufferSource pBuffer, int pPackedLight, int pPackedOverlay, float roundBeginTime, float pPartialTick) {
        VertexConsumer vertexconsumer = Sheets.chooseMaterial(blockEntity, ChestType.SINGLE, this.xmasTextures).buffer(pBuffer, RenderType::entityCutout);
        pPoseStack.pushPose();
        pPoseStack.translate(0, 1, 0);
        pPoseStack.translate(0.4375, 0, 0.8125);
        pPoseStack.scale(0.125f, 0.125f, 0.125f);
        this.render(pPoseStack, vertexconsumer, this.lid, this.lock, this.bottom, roundBeginTime, pPackedLight, pPackedOverlay);
        pPoseStack.popPose();

        pPoseStack.pushPose();
        pPoseStack.translate(0.5F, 0.5F, 0.5F);
        pPoseStack.mulPose(Axis.YP.rotationDegrees(180));
        pPoseStack.translate(-0.5F, -0.5F, -0.5F);
        pPoseStack.translate(0.4375, 1, 0.8125);
        pPoseStack.scale(0.125f, 0.125f, 0.125f);
        this.render(pPoseStack, vertexconsumer, this.lid2, this.lock2, this.bottom2, roundBeginTime, pPackedLight, pPackedOverlay);
        pPoseStack.popPose();
    }

    private void render(PoseStack pPoseStack, VertexConsumer pConsumer, ModelPart pLidPart, ModelPart pLockPart, ModelPart pBottomPart, float pLidAngle, int pPackedLight, int pPackedOverlay) {
        pPoseStack.pushPose();
        if (pLidAngle >= 0) {
            pPoseStack.translate(0, -pLidAngle, 0);
            pLidPart.xRot = -((1 - pLidAngle) * ((float) Math.PI / 2F));
        } else {
            pPoseStack.translate(0, -pLidAngle - 1, 0);
            pLidPart.xRot = pLidAngle * (float) Math.PI / 2F;
        }
        pLockPart.xRot = pLidPart.xRot;
        pLidPart.render(pPoseStack, pConsumer, pPackedLight, pPackedOverlay);
        pLockPart.render(pPoseStack, pConsumer, pPackedLight, pPackedOverlay);
        pBottomPart.render(pPoseStack, pConsumer, pPackedLight, pPackedOverlay);
        pPoseStack.popPose();
    }

    public int getNextNum(int num){
        if(num>0) return num-1;
        else if(num<0) return num+1;
        else return  0;
    }
}
