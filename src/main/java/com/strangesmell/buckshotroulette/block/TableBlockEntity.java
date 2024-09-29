package com.strangesmell.buckshotroulette.block;

import com.strangesmell.buckshotroulette.BuckshotRoulette;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.ListTag;
import net.minecraft.network.Connection;
import net.minecraft.network.chat.Component;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import javax.annotation.Nullable;
import java.util.Random;

import static com.strangesmell.buckshotroulette.BuckshotRoulette.MODID;
import static com.strangesmell.buckshotroulette.block.TableBlock.*;
import static com.strangesmell.buckshotroulette.block.TableRenderer.function1;
import static com.strangesmell.buckshotroulette.block.TableRenderer.function2;

public class TableBlockEntity extends BlockEntity {
    public boolean isPlayer1;
    public boolean isPlayer2;
    public Random random = new Random();
    public String name1 = "";
    public String name2 = "";
    public int player1WinNum = 0;
    public int player2WinNum = 0;
    public int maxHealth;
    public int health1;
    public int health2;
    public int oldHealth1;
    public int oldHealth2;
    public Boolean isRead;
    public NonNullList<ItemStack> player1 = NonNullList.withSize(8, ItemStack.EMPTY);
    public NonNullList<ItemStack> player2 = NonNullList.withSize(8, ItemStack.EMPTY);
    public int ammunitionNum;
    public int ammunition;
    public int badAmmunition;
    public int goodAmmunition;
    public NonNullList<ItemStack> ammunitionList = NonNullList.withSize(8, ItemStack.EMPTY);
    public int player1ToolNum;
    public int player2ToolNum;
    public int player1AddItemTime;
    public int player2AddItemTime;
    public boolean player1CanAddItem;
    public boolean player2CanAddItem;
    public NonNullList<ItemStack> addItem1 = NonNullList.withSize(1, ItemStack.EMPTY);
    public NonNullList<ItemStack> addItem2 = NonNullList.withSize(1, ItemStack.EMPTY);
    public NonNullList<ItemStack> pistonItem = NonNullList.withSize(1, ItemStack.EMPTY);
    public NonNullList<Integer> item1Time = NonNullList.withSize(8, 20);
    public NonNullList<Integer> item2Time = NonNullList.withSize(8, 20);
    public NonNullList<ItemStack> moveItem1 = NonNullList.withSize(8, ItemStack.EMPTY);
    public NonNullList<ItemStack> moveItem2 = NonNullList.withSize(8, ItemStack.EMPTY);
    public int[] moveItem1Time = {0, 0, 0, 0, 0, 0, 0, 0};
    public int[] moveItem2Time = {0, 0, 0, 0, 0, 0, 0, 0};
    public float moveItemX = 0;
    public float moveItemZ = 0;
    public boolean begin;
    public boolean end;
    public boolean roundBegin;
    public int roundBeginTime;
    public int roundBeginTimeMax = 30;
    public int roundEndTime;
    public int tntStartTime;
    public boolean tntExplosion;
    public int tntUpTime;
    public int toolMoveTime;

    public boolean initAmmunition;
    public boolean chestFinish;

    public boolean right1;
    public boolean right2;
    public boolean toolTime;
    public boolean selectPlayerTime;
    public boolean player1Round;
    public boolean player2Round;
    public boolean attackTime;

    public boolean player1IsWeb;
    public boolean player2IsWeb;

    public boolean shouldReRandom;
    public boolean spyglass;
    public boolean addGunPower;
    public boolean isFishingTime;
    public boolean fished;
    public boolean isPiston;
    public int pistonTime;

    public TableBlockEntity(BlockEntityType<?> p_155228_, BlockPos p_155229_, BlockState p_155230_) {
        super(p_155228_, p_155229_, p_155230_);
    }

    public TableBlockEntity(BlockPos pPos, BlockState pBlockState) {
        super(BuckshotRoulette.TableBlockEntity.get(), pPos, pBlockState);
        isRead = false;
        begin = true;
        end = true;
        shouldReRandom = true;
        spyglass = false;
        addGunPower = false;
        isFishingTime = false;
        fished = false;
        isPiston = false;
        roundBegin = false;
        initAmmunition = false;
        chestFinish = false;
        right1 = false;
        right2 = false;
        toolTime = false;
        selectPlayerTime = false;
        player1Round = false;
        player2Round = false;
        attackTime = false;
        player1IsWeb = false;
        player2IsWeb = false;
        player1CanAddItem = true;
        player2CanAddItem = true;
        isPlayer1 = true;
        isPlayer2 = true;
        roundBeginTime = 0;
        roundEndTime = 0;

        tntStartTime = 0;
        tntExplosion = false;
        tntUpTime = 0;
        toolMoveTime = 0;
        for (int i = 0; i < 8; i++) {
            moveItem1Time[i] = 0;
            moveItem2Time[i] = 0;
        }
    }

    public void init() {
        name1 = "";
        name2 = "";
        player1WinNum = 0;
        player2WinNum = 0;
        player1 = NonNullList.withSize(8, ItemStack.EMPTY);
        player2 = NonNullList.withSize(8, ItemStack.EMPTY);
        ammunitionList = NonNullList.create();
        ammunition=0;
        goodAmmunition=0;
        badAmmunition=0;
        addItem1 = NonNullList.withSize(1, ItemStack.EMPTY);
        addItem2 = NonNullList.withSize(1, ItemStack.EMPTY);
        pistonItem = NonNullList.withSize(1, ItemStack.EMPTY);
        item1Time = NonNullList.withSize(8, 20);
        item2Time = NonNullList.withSize(8, 20);
        for (int i = 0; i < 8; i++) {
            moveItem1Time[i] = 0;
            moveItem2Time[i] = 0;
        }
    }

    public void initHealth() {
        health1 = random.nextInt(2, 5);
        health2 = health1;
        maxHealth = health1;
        oldHealth1 = health1;
        oldHealth2 = health1;
    }

    public void initTool() {
        player1ToolNum = random.nextInt(1, Math.min(maxHealth+2,6));
        player2ToolNum = player1ToolNum;
        if(isPlayer1){
            byName(level, name1).sendSystemMessage(Component.translatable("toolNum").append(player1ToolNum + ""));
        }
        if(isPlayer2){
            byName(level, name2).sendSystemMessage(Component.translatable("toolNum").append(player1ToolNum + ""));
        }
    }

    public void initAmmunition() {
        ammunition = random.nextInt(2, 9);
        ammunitionNum=ammunition;
        badAmmunition = 0;
        goodAmmunition = 0;
        ammunitionList = NonNullList.withSize(ammunition, ItemStack.EMPTY);
        for (int i = 0; i < ammunition; i++) {
            if (random.nextInt(0, 2) == 1) {
                ammunitionList.set(i, new ItemStack(Items.GUNPOWDER));
                goodAmmunition++;
            } else {
                ammunitionList.set(i, new ItemStack(Items.REDSTONE));
                badAmmunition++;
            }
        }
        if(goodAmmunition==0){
            ammunitionList.set(random.nextInt(0,ammunition), new ItemStack(Items.GUNPOWDER));
            goodAmmunition++;
            badAmmunition--;
        }
        if(isPlayer1){
            byName(level, name1).sendSystemMessage(Component.translatable("gunpowderNum").append(goodAmmunition + ""));
            byName(level, name1).sendSystemMessage(Component.translatable("redStoneNum").append(badAmmunition + ""));
        }
        if(isPlayer2){
            byName(level, name2).sendSystemMessage(Component.translatable("gunpowderNum").append(goodAmmunition + ""));
            byName(level, name2).sendSystemMessage(Component.translatable("redStoneNum").append(badAmmunition + ""));
        }
        initTool();
    }

    public void initFlag() {
        tntExplosion = false;
        isRead = false;
        begin = true;
        end = true;
        shouldReRandom = true;
        spyglass = false;
        addGunPower = false;
        isFishingTime = false;
        fished = false;
        isPiston = false;
        roundBegin = false;
        initAmmunition = false;
        chestFinish = false;
        right1 = false;
        right2 = false;
        toolTime = false;
        selectPlayerTime = false;
        player1Round = false;
        player2Round = false;
        attackTime = false;
        player1IsWeb = false;
        player2IsWeb = false;
        player1CanAddItem = true;
        player2CanAddItem = true;
        isPlayer2 = true;
        isPlayer2 = true;

    }

    public Item getRandomItem() {
        int num = random.nextInt(0, BuckshotRoulette.items.size());
        return BuckshotRoulette.items.get(num);
    }

    public void loadFlag(CompoundTag compound) {
        name1 = compound.getString(MODID + " name1");
        name2 = compound.getString(MODID + " name2");
        health1 = compound.getInt(MODID + " health1");
        health2 = compound.getInt(MODID + " health2");
        oldHealth1 = compound.getInt(MODID + " oldHealth1");
        oldHealth2 = compound.getInt(MODID + " oldHealth2");
        player1WinNum = compound.getInt(MODID + " player1WinNum");
        player2WinNum = compound.getInt(MODID + " player2WinNum");
        isRead = compound.getBoolean(MODID + " table_isRead");
        ammunitionNum = compound.getInt(MODID + " ammunitionNum");
        ammunition = compound.getInt(MODID + " ammunition");
        badAmmunition = compound.getInt(MODID + " badAmmunition");
        goodAmmunition = compound.getInt(MODID + " goodAmmunition");

        begin = compound.getBoolean(MODID + " begin");
        end = compound.getBoolean(MODID + " end");
        shouldReRandom = compound.getBoolean(MODID + " shouldReRandom");
        spyglass = compound.getBoolean(MODID + " spyglass");
        addGunPower = compound.getBoolean(MODID + " addGunPower");
        isFishingTime = compound.getBoolean(MODID + " isFishingTime");
        fished = compound.getBoolean(MODID + " fished");
        isPiston = compound.getBoolean(MODID + " isPiston");
        roundBegin = compound.getBoolean(MODID + " roundBegin");
        initAmmunition = compound.getBoolean(MODID + " initAmmunition");
        chestFinish = compound.getBoolean(MODID + " chestFinish");
        right1 = compound.getBoolean(MODID + " right1");
        right2 = compound.getBoolean(MODID + " right2");
        toolTime = compound.getBoolean(MODID + " toolTime");
        selectPlayerTime = compound.getBoolean(MODID + " selectPlayerTime");
        player1Round = compound.getBoolean(MODID + " player1Round");
        player2Round = compound.getBoolean(MODID + " player2Round");
        attackTime = compound.getBoolean(MODID + " attackTime");
        player1IsWeb = compound.getBoolean(MODID + " player1IsWeb");
        player2IsWeb = compound.getBoolean(MODID + " player2IsWeb");
        player1CanAddItem = compound.getBoolean(MODID + " player1CanAddItem");
        player2CanAddItem = compound.getBoolean(MODID + " player2CanAddItem");
        isPlayer1 = compound.getBoolean(MODID + " isPlayer1");
        isPlayer2 = compound.getBoolean(MODID + " isPlayer2");
        player1ToolNum = compound.getInt(MODID + " player1ToolNum");
        player2ToolNum = compound.getInt(MODID + " player2ToolNum");
        player1AddItemTime = compound.getInt(MODID + " player1AddItemTime");
        player2AddItemTime = compound.getInt(MODID + " player2AddItemTime");
        maxHealth = compound.getInt(MODID + " maxHealth");
        roundBeginTime = compound.getInt(MODID + " roundBeginTime");
        tntStartTime = compound.getInt(MODID + " tntStartTime");
        tntExplosion = compound.getBoolean(MODID + " tntExplosion");
        toolMoveTime = compound.getInt(MODID + " toolMoveTime");
        tntUpTime = compound.getInt(MODID + " tntUpTime");
        moveItemX = compound.getFloat(MODID + " moveItemX");
        moveItemZ = compound.getFloat(MODID + " moveItemZ");
        moveItem1Time = compound.getIntArray(MODID + " moveItem1Time");
        moveItem2Time = compound.getIntArray(MODID + " moveItem2Time");
        loadAllItems(compound, this.ammunitionList, MODID + " ammunitionList");
        loadAllItems(compound, this.addItem1, MODID + " addItem1");
        loadAllItems(compound, this.addItem2, MODID + " addItem2");
        loadAllItems(compound, this.pistonItem, MODID + " pistonItem");
        loadAllItems(compound, this.player1, MODID + " player1");
        loadAllItems(compound, this.player2, MODID + " player2");
        loadAllItems(compound, this.moveItem1, MODID + " moveItem1");
        loadAllItems(compound, this.moveItem2, MODID + " moveItem2");

    }

    public void saveFlag(CompoundTag compound) {
        compound.putString(MODID + " name1", name1);
        compound.putString(MODID + " name2", name2);
        compound.putInt(MODID + " health1", health1);
        compound.putInt(MODID + " health2", health2);
        compound.putInt(MODID + " oldHealth1", oldHealth1);
        compound.putInt(MODID + " oldHealth2", oldHealth2);
        compound.putInt(MODID + " player1WinNum", player1WinNum);
        compound.putInt(MODID + " player2WinNum", player2WinNum);
        compound.putBoolean(MODID + " table_isRead", isRead);
        compound.putInt(MODID + " ammunitionNum", ammunitionNum);
        compound.putInt(MODID + " ammunition", ammunition);
        compound.putInt(MODID + " badAmmunition", badAmmunition);
        compound.putInt(MODID + " goodAmmunition", goodAmmunition);

        compound.putBoolean(MODID + " begin", begin);
        compound.putBoolean(MODID + " end", end);
        compound.putBoolean(MODID + " shouldReRandom", shouldReRandom);
        compound.putBoolean(MODID + " spyglass", spyglass);
        compound.putBoolean(MODID + " addGunPower", addGunPower);
        compound.putBoolean(MODID + " isFishingTime", isFishingTime);
        compound.putBoolean(MODID + " fished", fished);
        compound.putBoolean(MODID + " isPiston", isPiston);
        compound.putBoolean(MODID + " roundBegin", roundBegin);
        compound.putBoolean(MODID + " initAmmunition", initAmmunition);
        compound.putBoolean(MODID + " chestFinish", chestFinish);
        compound.putBoolean(MODID + " right1", right1);
        compound.putBoolean(MODID + " right2", right2);
        compound.putBoolean(MODID + " toolTime", toolTime);
        compound.putBoolean(MODID + " selectPlayerTime", selectPlayerTime);
        compound.putBoolean(MODID + " player1Round", player1Round);
        compound.putBoolean(MODID + " player2Round", player2Round);
        compound.putBoolean(MODID + " attackTime", attackTime);
        compound.putBoolean(MODID + " player1IsWeb", player1IsWeb);
        compound.putBoolean(MODID + " player2IsWeb", player2IsWeb);
        compound.putBoolean(MODID + " player1CanAddItem", player1CanAddItem);
        compound.putBoolean(MODID + " player2CanAddItem", player2CanAddItem);
        compound.putBoolean(MODID + " isPlayer1", isPlayer1);
        compound.putBoolean(MODID + " isPlayer2", isPlayer2);
        compound.putInt(MODID + " player1ToolNum", player1ToolNum);
        compound.putInt(MODID + " player2ToolNum", player2ToolNum);
        compound.putInt(MODID + " player1AddItemTime", player1AddItemTime);
        compound.putInt(MODID + " player2AddItemTime", player2AddItemTime);
        compound.putInt(MODID + " maxHealth", maxHealth);
        compound.putInt(MODID + " roundBeginTime", roundBeginTime);
        compound.putInt(MODID + " tntStartTime", tntStartTime);
        compound.putBoolean(MODID + " tntExplosion", tntExplosion);
        compound.putInt(MODID + " toolMoveTime", toolMoveTime);
        compound.putInt(MODID + " tntUpTime", tntUpTime);
        compound.putFloat(MODID + " moveItemX", moveItemX);
        compound.putFloat(MODID + " moveItemZ", moveItemZ);
        compound.putIntArray(MODID + " moveItem1Time", moveItem1Time);
        compound.putIntArray(MODID + " moveItem2Time", moveItem2Time);
        saveAllItems(compound, this.ammunitionList, MODID + " ammunitionList");
        saveAllItems(compound, this.addItem1, MODID + " addItem1");
        saveAllItems(compound, this.addItem2, MODID + " addItem2");
        saveAllItems(compound, this.pistonItem, MODID + " pistonItem");
        saveAllItems(compound, this.player1, MODID + " player1");
        saveAllItems(compound, this.player2, MODID + " player2");
        saveAllItems(compound, this.moveItem1, MODID + " moveItem1");
        saveAllItems(compound, this.moveItem2, MODID + " moveItem2");
    }

    public CompoundTag saveAllItems(CompoundTag p_18977_, NonNullList<ItemStack> p_18978_, String s) {
        ListTag listtag = new ListTag();
        for (int i = 0; i < p_18978_.size(); ++i) {
            ItemStack itemstack = p_18978_.get(i);
            if (!itemstack.isEmpty()) {
                CompoundTag compoundtag = new CompoundTag();
                compoundtag.putByte("Slot", (byte) i);
                itemstack.save(compoundtag);
                listtag.add(compoundtag);
            }
        }
        p_18977_.put(s, listtag);
        return p_18977_;
    }

    public void loadAllItems(CompoundTag p_18981_, NonNullList<ItemStack> itemList, String s) {
        ListTag listtag = p_18981_.getList(s, 10);
        for (int i = 0; i < itemList.size(); i++) {
            itemList.set(i, ItemStack.EMPTY);
        }
        for (int i = 0; i < listtag.size(); ++i) {
            CompoundTag compoundtag = listtag.getCompound(i);
            int j = compoundtag.getByte("Slot") & 255;
            if (j >= 0 && j < itemList.size()) {
                itemList.set(j, ItemStack.of(compoundtag));
            }
        }

    }

    public void load(CompoundTag compound) {
        super.load(compound);
        loadFlag(compound);
    }

    public void saveAdditional(CompoundTag compound) {
        super.saveAdditional(compound);
        saveFlag(compound);
    }

    @Override
    public CompoundTag getUpdateTag() {
        CompoundTag compound = new CompoundTag();
        saveFlag(compound);
        return compound;
    }

    @Nullable
    public ClientboundBlockEntityDataPacket getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void onDataPacket(Connection net, ClientboundBlockEntityDataPacket pkt) {
        if (pkt.getTag() == null) return;
        loadFlag(pkt.getTag());
    }

    @Override
    public void handleUpdateTag(CompoundTag tag) {
        loadFlag(tag);
    }

    static final TargetingConditions BREED_TARGETING = TargetingConditions.forNonCombat().range(8.0D);
    public static void tick(Level level, BlockPos blockPos, BlockState blockState, TableBlockEntity tableBlockEntity) {
/*        if(!level.isClientSide){
            level.getNearbyEntities(LivingEntity.class,BREED_TARGETING,null,new AABB(blockPos.offset(-5,-2,-5),blockPos.offset(5,2,5)));

        }*/

        for (int i = 0; i < 8; i++) {
            if (tableBlockEntity.moveItem1Time[i] > 0) {
                tableBlockEntity.moveItem1Time[i]--;
            }
            if (tableBlockEntity.moveItem2Time[i] > 0) {
                tableBlockEntity.moveItem2Time[i]--;
            }
            if (tableBlockEntity.moveItem1Time[i] == 0) {
                tableBlockEntity.moveItem1.set(i, ItemStack.EMPTY);
            }
            if (tableBlockEntity.moveItem2Time[i] == 0) {
                tableBlockEntity.moveItem2.set(i, ItemStack.EMPTY);
            }
        }

        if (tableBlockEntity.tntStartTime == 0) {
            if (tableBlockEntity.roundBeginTime < 0) {
                tableBlockEntity.roundBeginTime++;
            } else if (tableBlockEntity.roundBeginTime > 0) {
                tableBlockEntity.roundBeginTime--;
            } else {
                //let chestFinish = true after roundBegin and select tool after chest lid 90°
                if (tableBlockEntity.roundBegin)
                    tableBlockEntity.chestFinish = true;
            }
        }

        if (tableBlockEntity.toolMoveTime <= tableBlockEntity.roundBeginTimeMax) {
            tableBlockEntity.toolMoveTime++;
        }

        if (tableBlockEntity.tntStartTime > 0) {
            tableBlockEntity.tntStartTime--;
            if (tableBlockEntity.tntStartTime == 0) {
                tableBlockEntity.oldHealth1 = tableBlockEntity.health1;
                tableBlockEntity.oldHealth2 = tableBlockEntity.health2;
                if (tableBlockEntity.oldHealth1 == 0&&!level.isClientSide) {
                    //死亡，回合结束
                    player1Dead(level, tableBlockEntity);
                } else {
                    //若弹药耗尽则结束
                    outOfAmmunition(tableBlockEntity);
                }
                if (tableBlockEntity.tntExplosion) {
                    tableBlockEntity.oldHealth1 = tableBlockEntity.health1;

                    tableBlockEntity.tntExplosion = false;
                    if (tableBlockEntity.level.isClientSide) {
                        function1(tableBlockEntity);
                    }
                }else{
                    if(tableBlockEntity.player1Round&&level.isClientSide){
                        //if(tableBlockEntity.isPlayer1) byName(level, tableBlockEntity.name1).sendSystemMessage(Component.translatable("continue"));
                    }

                }
            }

        } else if (tableBlockEntity.tntStartTime < 0) {
            tableBlockEntity.tntStartTime++;
            if (tableBlockEntity.tntStartTime == 0) {
                tableBlockEntity.oldHealth1 = tableBlockEntity.health1;
                tableBlockEntity.oldHealth2 = tableBlockEntity.health2;
                if (tableBlockEntity.oldHealth2 == 0&&!level.isClientSide) {
                    //死亡，回合结束
                    player2Dead(level, tableBlockEntity);
                } else {
                    //若弹药耗尽则结束
                    outOfAmmunition(tableBlockEntity);
                }
                if (tableBlockEntity.tntExplosion) {
                    tableBlockEntity.oldHealth2 = tableBlockEntity.health2;

                    tableBlockEntity.tntExplosion = false;
                    if (tableBlockEntity.level.isClientSide) {
                        function2(tableBlockEntity);
                    }
                }else{
                    if(tableBlockEntity.player2Round&&level.isClientSide){
                        //if(tableBlockEntity.isPlayer2) byName(level, tableBlockEntity.name2).sendSystemMessage(Component.translatable("continue"));
                    }

                }
            }

        }

        if (tableBlockEntity.tntUpTime > 0) {
            tableBlockEntity.tntUpTime--;
        } else if (tableBlockEntity.tntUpTime < 0) {
            tableBlockEntity.tntUpTime++;
        }

        if (tableBlockEntity.roundEndTime > 0) {
            tableBlockEntity.roundEndTime--;
        } else if (tableBlockEntity.roundEndTime < 0) {
            tableBlockEntity.roundEndTime++;
        }

        if (tableBlockEntity.player1AddItemTime > 0) {
            tableBlockEntity.player1AddItemTime--;
        }
        if (tableBlockEntity.player2AddItemTime > 0) {
            tableBlockEntity.player2AddItemTime--;
        }

        if (tableBlockEntity.pistonTime > 0) {
            tableBlockEntity.pistonTime--;
        }

        for (int i = 0; i < 8; i++) {
            if (tableBlockEntity.player1.get(i) != ItemStack.EMPTY) {
                if (tableBlockEntity.item1Time.get(i) > 0)
                    tableBlockEntity.item1Time.set(i, tableBlockEntity.item1Time.get(i) - 1);
            } else {
                tableBlockEntity.item1Time.set(i, 20);
            }
            if (tableBlockEntity.player2.get(i) != ItemStack.EMPTY) {
                if (tableBlockEntity.item2Time.get(i) > 0)
                    tableBlockEntity.item2Time.set(i, tableBlockEntity.item2Time.get(i) - 1);
            } else {
                tableBlockEntity.item2Time.set(i, 20);
            }
        }
    }


}
