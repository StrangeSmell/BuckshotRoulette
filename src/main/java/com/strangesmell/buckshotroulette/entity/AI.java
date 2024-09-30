package com.strangesmell.buckshotroulette.entity;

import com.strangesmell.buckshotroulette.block.TableBlock;
import com.strangesmell.buckshotroulette.block.TableBlockEntity;
import net.minecraft.core.NonNullList;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Random;

import static com.strangesmell.buckshotroulette.BuckshotRoulette.MODID;
import static com.strangesmell.buckshotroulette.Util.remove;
import static com.strangesmell.buckshotroulette.block.TableBlock.*;

public class AI {
    /**
     * 开始时需要初始化dealer的place和ammunitionList
     * 结束时需要重置place和ammunitionList
     * 每轮需要重置ammunitionList
     */
    public String selectPlayerAI(TableBlockEntity tableBlockEntity, Dealer dealer) {
        //使用tnt时选取哪一个玩家
        int usedNum = getUsedNum(tableBlockEntity);
        if (dealer.ammunitionList.get(usedNum).is(Items.GUNPOWDER) || getPercentageOfTNT(tableBlockEntity, dealer) >= 0.5) {
            if (dealer.getPlace() == 1) return tableBlockEntity.name2;
            if (dealer.getPlace() == 2) return tableBlockEntity.name1;
        } else {
            return dealer.getStringName() + dealer.getPlace();
        }

        return dealer.getStringName() + dealer.getPlace();
    }

    public static int getPlayer1IndexAI(Dealer dealer) {
        //钓竿，玩家2选取玩家1的物品，此时选取范围为0-7
        return removeIndexInList(dealer.toolList);
    }

    public static int getPlayer2IndexAI(Dealer dealer) {
        //钓竿，玩家1选取玩家2的物品，此时选取范围为0-7
        return removeIndexInList(dealer.toolList);
    }

    public static int removeIndexInList(int[] list) {
        int temp=-1;
        for (int i = 0; i < 17; i++) {
            if (list[i] != -1) {
                temp = list[i];
                list[i] = -1;
                return temp;
            }
        }
        return temp;
    }

    ItemStack FISHING_ROD = new ItemStack(Items.FISHING_ROD);   //0
    ItemStack SPYGLASS = new ItemStack(Items.SPYGLASS);         //1
    ItemStack APPLE = new ItemStack(Items.APPLE);               //2
    ItemStack COBWEB = new ItemStack(Items.COBWEB);             //3
    ItemStack GUNPOWDER = new ItemStack(Items.GUNPOWDER);       //4
    ItemStack POTATO = new ItemStack(Items.POTATO);             //5
    ItemStack BLACK_DYE = new ItemStack(Items.BLACK_DYE);       //6
    ItemStack OBSERVER = new ItemStack(Items.OBSERVER);         //7
    ItemStack PISTON = new ItemStack(Items.PISTON);             //8

    public void getToolIndexList(TableBlockEntity tableBlockEntity, Dealer dealer) {
        if (dealer.shouldGetToolList) {
            int[] toolnum1 = {0, 0, 0, 0, 0, 0, 0, 0, 0};
            int[] toolnum2 = {0, 0, 0, 0, 0, 0, 0, 0, 0};
            NonNullList<ItemStack> toolList1 = tableBlockEntity.player1;
            NonNullList<ItemStack> toolList2 = tableBlockEntity.player2;
            int index = 0;
            int[] toolList = {-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};

            initToolList(tableBlockEntity, toolnum1, toolnum2);
            //todo:若对方有鱼竿则尽量浪费道具
            if (isPlayer1(tableBlockEntity, dealer)) {            //本恼鬼是玩家1
                //蛛网
                if (toolnum1[3] > 0&&!tableBlockEntity.player2IsWeb&&tableBlockEntity.webRound==0) {                            //先手使用 蛛网
                    addAllToolIndex(1, COBWEB, toolList1, toolList, index);
                    toolnum1[3]--;
                }
                if (toolnum1[0] > 0) {       //若有鱼竿
                    if (toolnum1[0] > 0 && toolnum2[3] > 0) {      //且对手有蜘蛛网
                        addAllToolIndex(1, FISHING_ROD, toolList1, toolList, index);//todo:若能秒则不用
                        toolnum1[0]--;
                        addAllToolIndex(1, COBWEB, toolList2, toolList, index);
                        toolnum2[3]--;
                    }
                    if (toolnum1[0] > 0 && toolnum2[1] > 0) {//todo:获得当前tnt概率，是否用鱼竿拿望远镜
                        if (getPercentageOfTNT(tableBlockEntity, dealer) < 0.7 && getPercentageOfTNT(tableBlockEntity, dealer) > 0.3) {
                            addAllToolIndex(1, FISHING_ROD, toolList1, toolList, index);
                            toolnum1[0]--;
                            addAllToolIndex(1, SPYGLASS, toolList2, toolList, index);
                            toolnum2[1]--;
                        }
                    }
                    if (toolnum1[0] > 0 && toolnum2[2] > 0) {//对方有苹果则拿一个
                        if (tableBlockEntity.health1 != tableBlockEntity.maxHealth) {
                            addAllToolIndex(1, FISHING_ROD, toolList1, toolList, index);
                            toolnum1[0]--;
                            addAllToolIndex(1, APPLE, toolList2, toolList, index);
                            toolnum2[2]--;
                        }
                    }
                    if (toolnum1[0] > 0 && toolnum2[4] > 0) {//对方有火药则拿一个
                        if (getPercentageOfTNT(tableBlockEntity, dealer) > 0.7 ) {
                            addAllToolIndex(1, FISHING_ROD, toolList1, toolList, index);
                            toolnum1[0]--;
                            addAllToolIndex(1, GUNPOWDER, toolList2, toolList, index);
                            toolnum2[4]--;
                        }
                    }

                }
                //苹果
                if (toolnum1[2]>0) {
                    if (toolnum2[0]==0) {     //对方没有鱼钩
                        if (tableBlockEntity.name1.equals(dealer.getTableName())) {//todo:若能秒对方，则不使用苹果
                            if (tableBlockEntity.health1 != tableBlockEntity.maxHealth) {
                                addAllToolIndex(Math.min(toolnum1[2], tableBlockEntity.maxHealth - tableBlockEntity.health1), APPLE, toolList1, toolList, index);
                                toolnum1[2] = toolnum1[2] - Math.min(toolnum1[2], tableBlockEntity.maxHealth - tableBlockEntity.health1);
                            }
                        }
                    } else {
                        addAllToolIndex(toolnum1[2], APPLE, toolList1, toolList, index);
                        toolnum1[2] = 0;
                    }
                }
                //检测器
                if (toolnum1[7]>0) {//todo:加入一定概率的使用条件，若知道是否是tnt的概率小则用一个
                    if (dealer.ammunitionList.get(getUsedNum(tableBlockEntity)).isEmpty()) {
                        while ((getPercentageOfTNT(tableBlockEntity, dealer) < 0.7 && getPercentageOfTNT(tableBlockEntity, dealer) > 0.3)&&toolnum1[getIndex(OBSERVER)]>0) {
                            addAllToolIndex(1, OBSERVER, toolList1, toolList, index);
                            toolnum1[getIndex(OBSERVER)]--;
                        }
                    }
                }
                //望远镜
                if (toolnum1[1]>0) {//todo:加入一定概率的使用条件，若知道是否是tnt的概率小则用一个
                    if (dealer.ammunitionList.get(getUsedNum(tableBlockEntity)).isEmpty()) {
                        if (getPercentageOfTNT(tableBlockEntity, dealer) < 0.7 && getPercentageOfTNT(tableBlockEntity, dealer) > 0.3) {
                            addAllToolIndex(1, SPYGLASS, toolList1, toolList, index);
                            toolnum1[getIndex(SPYGLASS)]--;
                        }
                    }
                }
                //活塞
                if (toolnum1[8]>0) {//todo:加入一定概率的使用条件，若知道是否是tnt的概率小则用一个
                    if (dealer.ammunitionList.get(getUsedNum(tableBlockEntity)).isEmpty()) {
                        while ((getPercentageOfTNT(tableBlockEntity, dealer) < 0.6 && getPercentageOfTNT(tableBlockEntity, dealer) > 0.4)&&toolnum1[getIndex(PISTON)]>0) {
                            addAllToolIndex(1, PISTON, toolList1, toolList, index);
                            toolnum1[getIndex(PISTON)]--;
                        }
                    }
                }
                //染料
                if (toolnum1[6]>0) {
                    if (tableBlockEntity.health1 >= tableBlockEntity.health2) {
                        if (!dealer.ammunitionList.get(getUsedNum(tableBlockEntity)).equals(GUNPOWDER) || getPercentageOfTNT(tableBlockEntity, dealer) < 0.6) {
                            addAllToolIndex(1, BLACK_DYE, toolList1, toolList, index);
                            toolnum1[getIndex(BLACK_DYE)]--;
                        }
                    }//todo:转变会让下一个tnt概率变低
                }
                //土豆
                if (toolnum1[5]>0) {
                    if (tableBlockEntity.health1 >= 1&&tableBlockEntity.health1<tableBlockEntity.maxHealth-1) {//todo:若当前血必死，则使用,若满血则不吃
                        addAllToolIndex(1, POTATO, toolList1, toolList, index);
                        toolnum1[getIndex(POTATO)]--;
                    }
                }
            } else {//是玩家2
                //蛛网
                if (toolnum2[3] > 0&&!tableBlockEntity.player1IsWeb) {                            //先手使用 蛛网
                    addAllToolIndex(1, COBWEB, toolList2, toolList, index);//todo:若能秒则不用
                    toolnum2[3]--;
                }
                if (toolnum2[0] > 0) {       //若有鱼竿但无蜘蛛网
                    if (toolnum2[0] > 0 && toolnum1[3] > 0) {         //且对手有蜘蛛网
                        addAllToolIndex(1, FISHING_ROD, toolList2, toolList, index);//todo:若能秒则不用
                        toolnum2[0]--;
                        addAllToolIndex(1, COBWEB, toolList1, toolList, index);
                        toolnum1[3]--;
                    }
                    if (toolnum2[0] > 0 && toolnum1[1] > 0) {//todo:获得当前tnt概率，是否用鱼竿拿望远镜
                        if (getPercentageOfTNT(tableBlockEntity, dealer) < 0.7 && getPercentageOfTNT(tableBlockEntity, dealer) > 0.3) {
                            addAllToolIndex(1, FISHING_ROD, toolList2, toolList, index);
                            toolnum2[0]--;
                            addAllToolIndex(1, SPYGLASS, toolList1, toolList, index);
                            toolnum1[1]--;
                        }
                    }
                    if (toolnum2[0] > 0 && toolnum1[2] > 0) {//对方有苹果则拿一个
                        if (tableBlockEntity.health2 != tableBlockEntity.maxHealth) {
                            addAllToolIndex(1, FISHING_ROD, toolList2, toolList, index);
                            toolnum2[0]--;
                            addAllToolIndex(1, APPLE, toolList1, toolList, index);
                            toolnum1[2]--;
                        }
                    }
                    if (toolnum2[0] > 0 && toolnum1[4] > 0) {//对方有火药则拿一个
                        if (getPercentageOfTNT(tableBlockEntity, dealer) > 0.7 ) {
                            addAllToolIndex(1, FISHING_ROD, toolList2, toolList, index);
                            toolnum2[0]--;
                            addAllToolIndex(1, GUNPOWDER, toolList1, toolList, index);
                            toolnum1[4]--;
                        }
                    }
                }

                //苹果
                if (toolnum2[2]>0) {
                    if (toolnum1[0]==0) {     //对方没有鱼钩
                        if (tableBlockEntity.name2.equals(dealer.getTableName())) {//todo:若能秒对方，则不使用苹果
                            if (tableBlockEntity.health2 != tableBlockEntity.maxHealth) {
                                addAllToolIndex(Math.min(toolnum2[2], tableBlockEntity.maxHealth - tableBlockEntity.health2), APPLE, toolList2, toolList, index);
                                toolnum2[2] = toolnum2[2] - Math.min(toolnum2[2], tableBlockEntity.maxHealth - tableBlockEntity.health2);
                            }
                        }
                    } else {
                        addAllToolIndex(toolnum2[2], APPLE, toolList2, toolList, index);
                        toolnum2[2] = 0;
                    }
                }
                //检测器
                if (toolnum2[7]>0) {//todo:加入一定概率的使用条件，若知道是否是tnt的概率小则用一个
                    if (dealer.ammunitionList.get(getUsedNum(tableBlockEntity)).isEmpty()) {
                        while ((getPercentageOfTNT(tableBlockEntity, dealer) < 0.7 && getPercentageOfTNT(tableBlockEntity, dealer) > 0.3)&&toolnum2[getIndex(OBSERVER)]>0) {
                            addAllToolIndex(1, OBSERVER, toolList2, toolList, index);
                            toolnum2[getIndex(OBSERVER)]--;
                        }
                    }
                }
                //望远镜
                if (toolnum2[1]>0) {//todo:加入一定概率的使用条件，若知道是否是tnt的概率小则用一个
                    if (dealer.ammunitionList.get(getUsedNum(tableBlockEntity)).isEmpty()) {
                        if (getPercentageOfTNT(tableBlockEntity, dealer) < 0.7 && getPercentageOfTNT(tableBlockEntity, dealer) > 0.3) {
                            addAllToolIndex(1, SPYGLASS, toolList2, toolList, index);
                            toolnum2[getIndex(SPYGLASS)]--;
                        }
                    }
                }
                //活塞
                if (toolnum2[8]>0) {//todo:加入一定概率的使用条件，若知道是否是tnt的概率小则用一个
                    if (dealer.ammunitionList.get(getUsedNum(tableBlockEntity)).isEmpty()) {
                        while ((getPercentageOfTNT(tableBlockEntity, dealer) < 0.6 && getPercentageOfTNT(tableBlockEntity, dealer) > 0.4)&&toolnum2[getIndex(PISTON)]>0) {
                            addAllToolIndex(1, PISTON, toolList2, toolList, index);
                            toolnum2[getIndex(PISTON)]--;
                        }
                    }
                }
                //染料
                if (toolnum2[6] > 0) {
                    if (tableBlockEntity.health2 >= tableBlockEntity.health1) {
                        if (!dealer.ammunitionList.get(getUsedNum(tableBlockEntity)).equals(GUNPOWDER) || getPercentageOfTNT(tableBlockEntity, dealer) < 0.6) {
                            addAllToolIndex(1, BLACK_DYE, toolList2, toolList, index);
                            toolnum2[getIndex(BLACK_DYE)]--;
                        }
                    }//todo:转变会让下一个tnt概率变低

                }
                //土豆
                if (toolnum2[5] > 0) {
                    if (tableBlockEntity.health2 >= 1&&tableBlockEntity.health2<=tableBlockEntity.maxHealth-2) {//todo:若当前血必死，则使用
                        addAllToolIndex(1, POTATO, toolList2, toolList, index);
                        toolnum2[getIndex(POTATO)]--;
                    }
                }

            }
            toolList[firstRightIndex(toolList)] = 8;
            dealer.toolList = toolList;
        }
    }

    //加入的数量、物品种类、道具列表、执行索引表、执行索引表的索引
    public void addAllToolIndex(int num, ItemStack itemStack, NonNullList<ItemStack> playerList, int[] toolList, int index) {
        int index2 = 0;
        for (int i = 0; i < 8; i++) {
            if (playerList.get(i).is(itemStack.getItem())) {
                toolList[firstRightIndex(toolList)] = i;
                index++;
                index2++;
            }
            if (index2 == num) break;
        }
    }

    //获得第num个某道具index
    public int getNumToolIndex(int num, ItemStack itemStack, NonNullList<ItemStack> toolList) {
        int index = 0;
        for (int i = 0; i < 8; i++) {
            if (toolList.get(i).is(itemStack.getItem())) index++;
            if (index == num) return i;
        }
        return -1;
    }

    public int firstRightIndex(int[] toolList) {
        for (int i = 0; i < 17; i++) {
            if (toolList[i] == -1) return i;
        }
        return -1;
    }

    public int getIndex(ItemStack itemStack) {
        if (itemStack.is(Items.FISHING_ROD)) return 0;
        else if (itemStack.is(Items.SPYGLASS)) return 1;
        else if (itemStack.is(Items.APPLE)) return 2;
        else if (itemStack.is(Items.COBWEB)) return 3;
        else if (itemStack.is(Items.GUNPOWDER)) return 4;
        else if (itemStack.is(Items.POTATO)) return 5;
        else if (itemStack.is(Items.BLACK_DYE)) return 6;
        else if (itemStack.is(Items.OBSERVER)) return 7;
        else if (itemStack.is(Items.PISTON)) return 8;
        return -1;
    }

    public boolean isPlayer1(TableBlockEntity tableBlockEntity, Dealer dealer) {
        return tableBlockEntity.name1.equals(dealer.getTableName());
    }

    public double getPercentageOfTNT(TableBlockEntity tableBlockEntity, Dealer dealer) {
        int gunpowderNum = 0;
        int redStoneNum = 0;
        int usedNum = tableBlockEntity.ammunitionNum - tableBlockEntity.ammunition;
        for (int i = usedNum; i < tableBlockEntity.ammunition; i++) {
            if (dealer.ammunitionList.get(i).is(Items.GUNPOWDER)) {
                gunpowderNum++;
            } else if (dealer.ammunitionList.get(i).is(Items.REDSTONE)) {
                redStoneNum++;
            }
        }
        if(tableBlockEntity.ammunition - gunpowderNum - redStoneNum==0) {
            if(redStoneNum==0) return 1;
            if(gunpowderNum==0) return 0;
            return (double) gunpowderNum /redStoneNum*gunpowderNum;
        }
        return (double) (tableBlockEntity.goodAmmunition - gunpowderNum) / (double) (tableBlockEntity.ammunition - gunpowderNum - redStoneNum);
    }

    public void use(BlockEntity blockEntity, Dealer dealer) {
        if (blockEntity instanceof TableBlockEntity tableBlockEntity) {
            Level level = blockEntity.getLevel();
            if (!tableBlockEntity.isRead) {
                if (tableBlockEntity.name1.equals("") || tableBlockEntity.name2.equals("")) {
                    //if (!((Objects.equals(tableBlockEntity.name1, dealer.getStringName() + "1") &&Objects.equals(tableBlockEntity.name1, ""))|| Objects.equals(tableBlockEntity.name2, dealer.getStringName() + "2"))) {
                        if (tableBlockEntity.name1.equals("")) {
                            tableBlockEntity.name1 = dealer.getStringName() + "1";
                            dealer.setPlace(1);
                            tableBlockEntity.isPlayer1 = false;
                        } else {
                            if (tableBlockEntity.name2.equals("")&&dealer.getPlace()!=1&&dealer.canJoinPlayer2) {
                                tableBlockEntity.name2 = dealer.getStringName() + "2";
                                dealer.setPlace(2);
                                tableBlockEntity.isRead = true;
                                tableBlockEntity.isPlayer2 = false;
                                if(tableBlockEntity.isPlayer1){
                                    Player player1 = byName(level, tableBlockEntity.name1);
                                    player1.sendSystemMessage(Component.translatable(MODID + ".all_ready"));
                                    player1.sendSystemMessage(Component.translatable(MODID + ".take_bet"));
                                    begin(player1);
                                }
                            }
                        }
                    //}
                }
            } else {
                if (tableBlockEntity.begin) {
                    //游戏开始，初始化双方道具区、生命、flag:begin=true,初始完后begin=false，flag:roundBegin=true
                    tableBlockEntity.initHealth();
                    tableBlockEntity.begin = false;
                    tableBlockEntity.end = false;
                    tableBlockEntity.roundBegin = true;
                    tableBlockEntity.tntUpTime = tableBlockEntity.roundBeginTimeMax;
                    tableBlockEntity.roundBeginTime = tableBlockEntity.roundBeginTimeMax;
                    tableBlockEntity.initAmmunition = true;

                }
                if (tableBlockEntity.roundBegin) {
                    //回合开始，出现箱子，开始拿道具，若某人道具区满则直接停止
                    if (tableBlockEntity.initAmmunition) {
                        tableBlockEntity.initAmmunition();
                        tableBlockEntity.initAmmunition = false;
                    }
                    if(tableBlockEntity.ammunition==0){
                        TableBlock.end(tableBlockEntity.getLevel(),tableBlockEntity);
                        return;
                    }
                    dealer.shouldGetToolList = true;
                    if (tableBlockEntity.chestFinish) {
                        //可以开始拿道具
                        if (dealer.getTableName().equals(tableBlockEntity.name1)) {
                            if (tableBlockEntity.player1ToolNum > 0 && tableBlockEntity.player1.contains(ItemStack.EMPTY)) {
                                int i = tableBlockEntity.player1.indexOf(ItemStack.EMPTY);
                                if (tableBlockEntity.player1AddItemTime == 0) {
                                    tableBlockEntity.player1AddItemTime = 20;
                                    tableBlockEntity.player1ToolNum--;
                                    ItemStack itemStack = new ItemStack(tableBlockEntity.getRandomItem());
                                    tableBlockEntity.addItem1.set(0, itemStack);
                                    tableBlockEntity.player1.set(i, itemStack);
                                    if(!(tableBlockEntity.player1ToolNum > 0 && tableBlockEntity.player1.contains(ItemStack.EMPTY))) tableBlockEntity.right1 = true;
                                }
                            } else {
                                tableBlockEntity.right1 = true;
                            }
                        } else {
                            if (tableBlockEntity.player2ToolNum > 0 && tableBlockEntity.player2.contains(ItemStack.EMPTY)) {
                                int i = tableBlockEntity.player2.indexOf(ItemStack.EMPTY);
                                if (tableBlockEntity.player2AddItemTime == 0) {
                                    tableBlockEntity.item2Time.set(i, 20);
                                    tableBlockEntity.player2AddItemTime = 20;
                                    tableBlockEntity.player2ToolNum--;
                                    ItemStack itemStack = new ItemStack(tableBlockEntity.getRandomItem());
                                    tableBlockEntity.addItem2.set(0, itemStack);
                                    tableBlockEntity.player2.set(i, itemStack);
                                    if(!(tableBlockEntity.player2ToolNum > 0 && tableBlockEntity.player2.contains(ItemStack.EMPTY))) tableBlockEntity.right2 = true;
                                }
                            } else {
                                tableBlockEntity.right2 = true;
                            }
                        }
                        //双方都拿完后或者道具区满了后flag：allRight=right1=true&&right2=true，初始化tnt队列,flag:roundBegin=false，flag:tool=true
                        if (tableBlockEntity.right1 && tableBlockEntity.right2 && tableBlockEntity.player1AddItemTime == 0 && tableBlockEntity.player2AddItemTime == 0) {

                            if (tableBlockEntity.shouldReRandom) {//随机玩家先手
                                if (tableBlockEntity.random.nextInt(1, 3) == 1) {
                                    tableBlockEntity.player1Round = true;
                                    if (tableBlockEntity.isPlayer1) {
                                        byName(level, tableBlockEntity.name1).sendSystemMessage(Component.translatable(MODID + ".first").append(tableBlockEntity.name1));
                                    }
                                    if (tableBlockEntity.isPlayer2) {
                                        byName(level, tableBlockEntity.name2).sendSystemMessage(Component.translatable(MODID + ".first").append(tableBlockEntity.name1));
                                    }
                                } else {
                                    if (tableBlockEntity.isPlayer1) {
                                        byName(level, tableBlockEntity.name1).sendSystemMessage(Component.translatable(MODID + ".first").append(tableBlockEntity.name2));
                                    }
                                    if (tableBlockEntity.isPlayer2) {
                                        byName(level, tableBlockEntity.name2).sendSystemMessage(Component.translatable(MODID + ".first").append(tableBlockEntity.name2));
                                    }
                                    tableBlockEntity.player2Round = true;
                                }

                            }
                            tableBlockEntity.toolTime = true;
                            tableBlockEntity.roundBegin = false;
                            tableBlockEntity.roundBeginTime = -tableBlockEntity.roundBeginTimeMax;
                            tableBlockEntity.roundEndTime = -tableBlockEntity.roundBeginTimeMax;
                            tableBlockEntity.right1 = false;
                            tableBlockEntity.right2 = false;
                        }
                    }
                    level.sendBlockUpdated(tableBlockEntity.getBlockPos(), tableBlockEntity.getBlockState(), tableBlockEntity.getBlockState(), 2);
                }

                if ((tableBlockEntity.player1Round || tableBlockEntity.player2IsWeb) && !tableBlockEntity.roundBegin&&tableBlockEntity.chestFinish) {
                    tableBlockEntity.toolTime=true;
                    if (tableBlockEntity.player2IsWeb && tableBlockEntity.player2Round) {
                        if (tableBlockEntity.isPlayer1) {
                            byName(level, tableBlockEntity.name1).sendSystemMessage(Component.literal(tableBlockEntity.name2).append(Component.translatable("isWeb")));
                        }
                        if (tableBlockEntity.isPlayer2) {
                            byName(level, tableBlockEntity.name2).sendSystemMessage(Component.literal(tableBlockEntity.name2).append(Component.translatable("isWeb")));
                        }
                        tableBlockEntity.player2IsWeb = false;
                        tableBlockEntity.player2Round = false;
                        tableBlockEntity.player1Round = true;
                        return;
                    }
                    if(tableBlockEntity.player1IsWeb){
                        tableBlockEntity.toolTime=false;
                    }
                    if (isPlayer1(tableBlockEntity, dealer)) {
                        getToolIndexList(tableBlockEntity, dealer);
                        dealer.shouldGetToolList = false;
                    }
                    if (dealer.getTableName().equals(tableBlockEntity.name1)) {
                        //某玩家回合flag：player1=true。player2=false
                        //道具时间开始
                        if (tableBlockEntity.toolTime) {
                            //使用道具
                            int index = getPlayer1IndexAI(dealer);//todo
                            if (tableBlockEntity.selectPlayerTime) {
                                //获取选取的玩家
                                String select = selectPlayerAI(tableBlockEntity, dealer);
                                tableBlockEntity.selectPlayerTime = false;
                                //may should add a time flag

                                if (remove(0, tableBlockEntity.ammunitionList).is(Items.GUNPOWDER)) {
                                    if(!select.equals(dealer.getTableName())){
                                        dealer.playSound(SoundEvents.VEX_CHARGE,2,2);
                                    }
                                    tableBlockEntity.tntExplosion = true;
                                    tableBlockEntity.ammunition--;
                                    tableBlockEntity.goodAmmunition--;
                                    if (select.equals(tableBlockEntity.name1)) {
/*                                        if(tableBlockEntity.isPlayer2){
                                            byName(level, tableBlockEntity.name2).sendSystemMessage(Component.literal(tableBlockEntity.name1+" ").append(Component.translatable("useTo")).append(Component.literal(tableBlockEntity.name1+" ")).append(Component.translatable("isGunpowder")));
                                        }*/
                                        tableBlockEntity.tntStartTime = tableBlockEntity.roundBeginTimeMax;
                                        if (tableBlockEntity.health1 >= 2) {
                                            tableBlockEntity.health1--;
                                            if (tableBlockEntity.addGunPower) {
                                                tableBlockEntity.health1--;
                                            }
                                        } else {
                                            tableBlockEntity.health1--;
                                        }
                                        level.sendBlockUpdated(tableBlockEntity.getBlockPos(), tableBlockEntity.getBlockState(), tableBlockEntity.getBlockState(), 2);
                                    } else {
                                        tableBlockEntity.tntStartTime = -tableBlockEntity.roundBeginTimeMax;
/*                                        if(tableBlockEntity.isPlayer2){
                                            byName(level, tableBlockEntity.name2).sendSystemMessage(Component.literal(tableBlockEntity.name1+" ").append(Component.translatable("useTo")).append(Component.literal(tableBlockEntity.name2+" ")).append(Component.translatable("noGunpowder")));
                                        }*/
                                        if (tableBlockEntity.health2 >= 2) {
                                            tableBlockEntity.health2--;
                                            if (tableBlockEntity.addGunPower) {
                                                tableBlockEntity.health2--;
                                            }
                                        } else {
                                            tableBlockEntity.health2--;
                                        }
                                        level.sendBlockUpdated(tableBlockEntity.getBlockPos(), tableBlockEntity.getBlockState(), tableBlockEntity.getBlockState(), 2);

                                    }
                                    //tnt的移动,爆炸特效
                                    tableBlockEntity.player1Round = false;
                                    tableBlockEntity.player2Round = true;
                                    if (isPlayer1(tableBlockEntity, dealer)) {
                                        dealer.shouldGetToolList = true;
                                    }
                                } else {
                                    if(select.equals(dealer.getTableName())){
                                        dealer.playSound(SoundEvents.VEX_CHARGE,2,2);
                                    }else{
                                        dealer.playSound(SoundEvents.VEX_AMBIENT,2,2);
                                    }
                                    tableBlockEntity.tntExplosion = false;
                                    if (select.equals(tableBlockEntity.name1)) {
                                        tableBlockEntity.tntStartTime = tableBlockEntity.roundBeginTimeMax;
                                        if (isPlayer1(tableBlockEntity, dealer)) {
                                            dealer.shouldGetToolList = true;
                                        }
                                    } else {
                                        tableBlockEntity.player1Round = false;
                                        tableBlockEntity.player2Round = true;
                                        if (isPlayer1(tableBlockEntity, dealer)) {
                                            dealer.shouldGetToolList = true;
                                        }
                                        tableBlockEntity.tntStartTime = -tableBlockEntity.roundBeginTimeMax;
                                    }
                                    tableBlockEntity.ammunition--;
                                    tableBlockEntity.badAmmunition--;

                                }
                                tableBlockEntity.addGunPower = false;
                                level.sendBlockUpdated(tableBlockEntity.getBlockPos(), tableBlockEntity.getBlockState(), tableBlockEntity.getBlockState(), 2);
                                return;
                            }

                            if (tableBlockEntity.isFishingTime) {
                                if(index==-1){
                                    index = getRandomRightIndex(tableBlockEntity.player2);
                                }
                                if (index >= 0 && index < 8) {
                                    ItemStack itemStack = tableBlockEntity.player2.get(index);
                                    if (itemStack.is(Items.FISHING_ROD)) return;
                                    tableBlockEntity.player2.set(index, ItemStack.EMPTY);
                                    level.playSound(null, tableBlockEntity.getBlockPos(), SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.AMBIENT, 1, 1);
                                    itemFunction(tableBlockEntity.name1, itemStack, tableBlockEntity, level, dealer);
                                    tableBlockEntity.isFishingTime = false;
                                    tableBlockEntity.pistonTime = TableBlock.pistonTimeMax;
                                } else {
                                    level.sendBlockUpdated(tableBlockEntity.getBlockPos(), tableBlockEntity.getBlockState(), tableBlockEntity.getBlockState(), 2);
                                    return;
                                }
                            }

                            if (index >= 0) {
                                if (index == 8) {
                                    //若使用tnt
                                    tableBlockEntity.selectPlayerTime = true;
                                    tableBlockEntity.spyglass = false;
                                    level.sendBlockUpdated(tableBlockEntity.getBlockPos(), tableBlockEntity.getBlockState(), tableBlockEntity.getBlockState(), 2);

                                    return;
                                } else {
                                    //使用道具
                                    ItemStack itemStack = tableBlockEntity.player1.get(index);
                                    tableBlockEntity.moveItem1.set(index, itemStack);
                                    tableBlockEntity.moveItem1Time[index] = 20;
                                    itemFunction(dealer.getTableName(), itemStack, tableBlockEntity, level, dealer);
                                    tableBlockEntity.player1.set(index, ItemStack.EMPTY);
                                    level.sendBlockUpdated(tableBlockEntity.getBlockPos(), tableBlockEntity.getBlockState(), tableBlockEntity.getBlockState(), 2);

                                }
                            } else {
                                tableBlockEntity.selectPlayerTime = true;
                                tableBlockEntity.spyglass = false;

                                level.sendBlockUpdated(tableBlockEntity.getBlockPos(), tableBlockEntity.getBlockState(), tableBlockEntity.getBlockState(), 2);
                                return;
                            }

                        }
                    }

                }
                if ((tableBlockEntity.player2Round || tableBlockEntity.player1IsWeb) && !tableBlockEntity.roundBegin) {
                    tableBlockEntity.toolTime=true;
                    if (tableBlockEntity.player1IsWeb && tableBlockEntity.player1Round) {
                        if (tableBlockEntity.isPlayer1) {
                            byName(level, tableBlockEntity.name1).sendSystemMessage(Component.literal(tableBlockEntity.name1).append(Component.translatable("isWeb")));
                        }
                        if (tableBlockEntity.isPlayer2) {
                            byName(level, tableBlockEntity.name2).sendSystemMessage(Component.literal(tableBlockEntity.name1).append(Component.translatable("isWeb")));
                        }
                        tableBlockEntity.player1IsWeb = false;
                        tableBlockEntity.player1Round = false;
                        tableBlockEntity.player2Round = true;
                        return;
                    }
                    if(tableBlockEntity.player2IsWeb){
                        tableBlockEntity.toolTime=false;
                    }
                    if (!isPlayer1(tableBlockEntity, dealer)) {
                        getToolIndexList(tableBlockEntity, dealer);
                        dealer.shouldGetToolList = false;
                    }
                    if (dealer.getTableName().equals(tableBlockEntity.name2)) {
                        //某玩家回合flag：player1=true。player2=false
                        //道具时间开始
                        if (tableBlockEntity.toolTime) {
                            //使用道具
                            int index = getPlayer2IndexAI(dealer);//todo
                            if (tableBlockEntity.selectPlayerTime) {
                                //获取选取的玩家
                                String select = selectPlayerAI(tableBlockEntity, dealer);
                                tableBlockEntity.selectPlayerTime = false;
                                //may should add a time flag

                                if (remove(0, tableBlockEntity.ammunitionList).is(Items.GUNPOWDER)) {
                                    if(select.equals(dealer.getTableName())){
                                        dealer.playSound(SoundEvents.VEX_HURT,1,1);
                                    }else{
                                        dealer.playSound(SoundEvents.VEX_CHARGE,1,1);
                                    }
                                    tableBlockEntity.tntExplosion = true;
                                    tableBlockEntity.ammunition--;
                                    tableBlockEntity.goodAmmunition--;
                                    if (select.equals(tableBlockEntity.name1)) {
                                        tableBlockEntity.tntStartTime = tableBlockEntity.roundBeginTimeMax;
/*                                        if(tableBlockEntity.isPlayer1){
                                            byName(level, tableBlockEntity.name1).sendSystemMessage(Component.literal(tableBlockEntity.name2+" ").append(Component.translatable("useTo")).append(Component.literal(tableBlockEntity.name1+" ")).append(Component.translatable("isGunpowder")));
                                        }*/
                                        if (tableBlockEntity.health1 >= 2) {
                                            tableBlockEntity.health1--;
                                            if (tableBlockEntity.addGunPower) {
                                                tableBlockEntity.health1--;
                                            }
                                        } else {
                                            tableBlockEntity.health1--;
                                        }
                                        level.sendBlockUpdated(tableBlockEntity.getBlockPos(), tableBlockEntity.getBlockState(), tableBlockEntity.getBlockState(), 2);

                                    } else {
                                        tableBlockEntity.tntStartTime = -tableBlockEntity.roundBeginTimeMax;
/*                                        if(tableBlockEntity.isPlayer1){
                                            byName(level, tableBlockEntity.name1).sendSystemMessage(Component.literal(tableBlockEntity.name2+" ").append(Component.translatable("useTo")).append(Component.literal(tableBlockEntity.name2+" ")).append(Component.translatable("noGunpowder")));
                                        }*/
                                        if (tableBlockEntity.health2 >= 2) {
                                            tableBlockEntity.health2--;
                                            if (tableBlockEntity.addGunPower) {
                                                tableBlockEntity.health2--;
                                            }
                                        } else {
                                            tableBlockEntity.health2--;
                                        }
                                        level.sendBlockUpdated(tableBlockEntity.getBlockPos(), tableBlockEntity.getBlockState(), tableBlockEntity.getBlockState(), 2);

                                    }
                                    tableBlockEntity.player1Round = true;
                                    tableBlockEntity.player2Round = false;
                                    if (!isPlayer1(tableBlockEntity, dealer)) {
                                        dealer.shouldGetToolList = true;
                                    }
                                } else {
                                    if(select.equals(dealer.getTableName())){
                                        dealer.playSound(SoundEvents.VEX_CHARGE,1,1);
                                    }else{
                                        dealer.playSound(SoundEvents.VEX_AMBIENT,1,1);
                                    }
                                    tableBlockEntity.tntExplosion = false;
                                    if (select.equals(tableBlockEntity.name1)) {
                                        tableBlockEntity.tntStartTime = tableBlockEntity.roundBeginTimeMax;
                                        tableBlockEntity.player1Round = true;
                                        tableBlockEntity.player2Round = false;
                                        if (!isPlayer1(tableBlockEntity, dealer)) {
                                            dealer.shouldGetToolList = true;
                                        }
                                    } else {
                                        tableBlockEntity.tntStartTime = -tableBlockEntity.roundBeginTimeMax;
                                        if (!isPlayer1(tableBlockEntity, dealer)) {
                                            dealer.shouldGetToolList = true;
                                        }
                                    }
                                    tableBlockEntity.ammunition--;
                                    tableBlockEntity.badAmmunition--;

                                }
                                tableBlockEntity.addGunPower = false;
                                level.sendBlockUpdated(tableBlockEntity.getBlockPos(), tableBlockEntity.getBlockState(), tableBlockEntity.getBlockState(), 2);

                                return;
                            }

                            if (tableBlockEntity.isFishingTime) {
                                if (index >= 0 && index < 8) {
                                    if(index==-1){
                                        index = getRandomRightIndex(tableBlockEntity.player1);
                                    }
                                    ItemStack itemStack = tableBlockEntity.player1.get(index);
                                    if (itemStack.is(Items.FISHING_ROD)) return;
                                    tableBlockEntity.player1.set(index, ItemStack.EMPTY);
                                    level.playSound(null, tableBlockEntity.getBlockPos(), SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.AMBIENT, 1, 1);
                                    itemFunction(tableBlockEntity.name2, itemStack, tableBlockEntity, level, dealer);
                                    tableBlockEntity.isFishingTime = false;
                                    tableBlockEntity.pistonTime = pistonTimeMax;
                                }
                            }

                            if (index >= 0) {
                                if (index == 8) {
                                    //若使用tnt
                                    tableBlockEntity.selectPlayerTime = true;
                                    tableBlockEntity.spyglass = false;
                                    level.sendBlockUpdated(tableBlockEntity.getBlockPos(), tableBlockEntity.getBlockState(), tableBlockEntity.getBlockState(), 2);
                                    return;
                                } else {
                                    //使用道具
                                    ItemStack itemStack = tableBlockEntity.player2.get(index);
                                    tableBlockEntity.moveItem2.set(index, itemStack);
                                    tableBlockEntity.moveItem2Time[index] = 20;
                                    itemFunction(dealer.getTableName(), itemStack, tableBlockEntity, level, dealer);
                                    tableBlockEntity.player2.set(index, ItemStack.EMPTY);
                                    level.sendBlockUpdated(tableBlockEntity.getBlockPos(), tableBlockEntity.getBlockState(), tableBlockEntity.getBlockState(), 2);
                                }
                            } else {
                                level.sendBlockUpdated(tableBlockEntity.getBlockPos(), tableBlockEntity.getBlockState(), tableBlockEntity.getBlockState(), 2);
                                return;
                            }
                        }
                    }
                }
            }
            level.sendBlockUpdated(tableBlockEntity.getBlockPos(), tableBlockEntity.getBlockState(), tableBlockEntity.getBlockState(), 2);
        }
    }

    public void itemFunction(String useName, ItemStack itemStack, TableBlockEntity tableBlockEntity, Level level, Dealer dealer) {
        if(tableBlockEntity.name1==null||tableBlockEntity.name2==null)return;
        if (itemStack.is(Items.SPYGLASS)) {
            //加一个init=30的int，>0时渲染红石或者火药
            level.playSound(null, tableBlockEntity.getBlockPos(), SoundEvents.SPYGLASS_USE, SoundSource.AMBIENT, 1, 1);
            tableBlockEntity.spyglass = true;
            if(tableBlockEntity.isPlayer1){
                byName(level,tableBlockEntity.name1).sendSystemMessage(Component.literal(useName).append(Component.translatable(MODID+".use_spyglass")));
            }
            if(tableBlockEntity.isPlayer2){
                byName(level,tableBlockEntity.name2).sendSystemMessage(Component.literal(useName).append(Component.translatable(MODID+".use_spyglass")));
            }
            dealer.ammunitionList.set(tableBlockEntity.ammunitionNum - tableBlockEntity.ammunition, tableBlockEntity.ammunitionList.get(0));
        } else if (itemStack.is(Items.COBWEB)) {
            level.playSound(null, tableBlockEntity.getBlockPos(), SoundEvents.SPIDER_AMBIENT, SoundSource.AMBIENT, 1, 1);
            if (tableBlockEntity.name1.equals(useName)) {
                tableBlockEntity.player2IsWeb = true;
            } else {
                tableBlockEntity.player1IsWeb = true;
            }
            if(tableBlockEntity.isPlayer1){
                byName(level,tableBlockEntity.name1).sendSystemMessage(Component.literal(useName).append(Component.translatable(MODID+".use_cobweb")));
            }
            if(tableBlockEntity.isPlayer2){
                byName(level,tableBlockEntity.name2).sendSystemMessage(Component.literal(useName).append(Component.translatable(MODID+".use_cobweb")));
            }
        } else if (itemStack.is(Items.POTATO)) {
            level.playSound(null, tableBlockEntity.getBlockPos(), SoundEvents.GENERIC_EAT, SoundSource.AMBIENT, 1, 1);
            if (tableBlockEntity.name1.equals(useName)) {
                if (tableBlockEntity.random.nextFloat(0, 1) > 0.6) {
                    if (tableBlockEntity.health1 <= tableBlockEntity.maxHealth - 2) {
                        tableBlockEntity.health1 = tableBlockEntity.health1 + 2;
                    } else {
                        tableBlockEntity.health1 = tableBlockEntity.maxHealth;
                    }
                } else {
                    tableBlockEntity.health1--;
                    if (tableBlockEntity.health1 == 0) {
                        if (useName.equals(tableBlockEntity.name1)) player1Dead(level, tableBlockEntity);
                    }
                }
            } else {
                if (tableBlockEntity.random.nextFloat(0, 1) > 0.6) {
                    if (tableBlockEntity.health2 <= tableBlockEntity.maxHealth - 2) {
                        tableBlockEntity.health2 = tableBlockEntity.health2 + 2;
                    } else {
                        tableBlockEntity.health2 = tableBlockEntity.maxHealth;
                    }
                } else {
                    tableBlockEntity.health2--;

                    if (tableBlockEntity.health2 == 0) {
                        if (useName.equals(tableBlockEntity.name2)) player2Dead(level, tableBlockEntity);
                    }
                }
            }
            if(tableBlockEntity.isPlayer1){
                byName(level,tableBlockEntity.name1).sendSystemMessage(Component.literal(useName).append(Component.translatable(MODID+".use_potato")));
            }
            if(tableBlockEntity.isPlayer2){
                byName(level,tableBlockEntity.name2).sendSystemMessage(Component.literal(useName).append(Component.translatable(MODID+".use_potato")));
            }
        } else if (itemStack.is(Items.BLACK_DYE)) {
            level.playSound(null, tableBlockEntity.getBlockPos(), SoundEvents.GENERIC_EAT, SoundSource.AMBIENT, 1, 1);
            if (tableBlockEntity.ammunitionList.get(0).is(Items.REDSTONE)) {
                tableBlockEntity.ammunitionList.set(0, new ItemStack(Items.GUNPOWDER));
                tableBlockEntity.goodAmmunition++;
                tableBlockEntity.badAmmunition--;
            } else {
                tableBlockEntity.ammunitionList.set(0, new ItemStack(Items.REDSTONE));
                tableBlockEntity.goodAmmunition--;
                tableBlockEntity.badAmmunition++;
            }
            if(tableBlockEntity.isPlayer1){
                byName(level,tableBlockEntity.name1).sendSystemMessage(Component.literal(useName).append(Component.translatable(MODID+".use_dye")));
            }
            if(tableBlockEntity.isPlayer2){
                byName(level,tableBlockEntity.name2).sendSystemMessage(Component.literal(useName).append(Component.translatable(MODID+".use_dye")));
            }
        } else if (itemStack.is(Items.APPLE)) {
            level.playSound(null, tableBlockEntity.getBlockPos(), SoundEvents.GENERIC_EAT, SoundSource.AMBIENT, 1, 1);
            if (tableBlockEntity.name1.equals(useName)) {
                if (tableBlockEntity.health1 < tableBlockEntity.maxHealth) {
                    tableBlockEntity.health1++;
                }
            } else {
                if (tableBlockEntity.health2 < tableBlockEntity.maxHealth) {
                    tableBlockEntity.health2++;
                }
            }
            if(tableBlockEntity.isPlayer1){
                byName(level,tableBlockEntity.name1).sendSystemMessage(Component.literal(useName).append(Component.translatable(MODID+".use_apple")));
            }
            if(tableBlockEntity.isPlayer2){
                byName(level,tableBlockEntity.name2).sendSystemMessage(Component.literal(useName).append(Component.translatable(MODID+".use_apple")));
            }
        } else if (itemStack.is(Items.GUNPOWDER)) {
            //设置标志
            level.playSound(null, tableBlockEntity.getBlockPos(), SoundEvents.CHISELED_BOOKSHELF_PICKUP, SoundSource.AMBIENT, 1, 1);
            tableBlockEntity.addGunPower = true;
            if(tableBlockEntity.isPlayer1){
                byName(level,tableBlockEntity.name1).sendSystemMessage(Component.literal(useName).append(Component.translatable(MODID+".use_gunpowder")));
            }
            if(tableBlockEntity.isPlayer2){
                byName(level,tableBlockEntity.name2).sendSystemMessage(Component.literal(useName).append(Component.translatable(MODID+".use_gunpowder")));
            }
        } else if (itemStack.is(Items.FISHING_ROD)) {
            level.playSound(null, tableBlockEntity.getBlockPos(), SoundEvents.FISHING_BOBBER_THROW, SoundSource.AMBIENT, 1, 1);
            //设置标志，选取对方一个物品，不能选取钓鱼竿
            tableBlockEntity.isFishingTime = true;
            if(tableBlockEntity.isPlayer1){
                byName(level,tableBlockEntity.name1).sendSystemMessage(Component.literal(useName).append(Component.translatable(MODID+".use_fishing_rod")));
            }
            if(tableBlockEntity.isPlayer2){
                byName(level,tableBlockEntity.name2).sendSystemMessage(Component.literal(useName).append(Component.translatable(MODID+".use_fishing_rod")));
            }
        } else if (itemStack.is(Items.OBSERVER)) {
            if(tableBlockEntity.isPlayer1){
                byName(level,tableBlockEntity.name1).sendSystemMessage(Component.literal(useName).append(Component.translatable(MODID+".use_observer")));
            }
            if(tableBlockEntity.isPlayer2){
                byName(level,tableBlockEntity.name2).sendSystemMessage(Component.literal(useName).append(Component.translatable(MODID+".use_observer")));
            }
            level.playSound(null, tableBlockEntity.getBlockPos(), SoundEvents.TRIPWIRE_CLICK_ON, SoundSource.AMBIENT, 1, 1);
            int i;
            if(tableBlockEntity.ammunition>1)i = tableBlockEntity.random.nextInt(1, tableBlockEntity.ammunition);
            else return;
            dealer.ammunitionList.set(i + tableBlockEntity.ammunitionNum - tableBlockEntity.ammunition, tableBlockEntity.ammunitionList.get(i));
        } else if (itemStack.is(Items.PISTON)) {
            level.playSound(null, tableBlockEntity.getBlockPos(), SoundEvents.PISTON_EXTEND, SoundSource.AMBIENT, 1, 1);
            if (tableBlockEntity.isPlayer1) {
                byName(level, tableBlockEntity.name1).sendSystemMessage(Component.translatable(MODID + ".piston").append(tableBlockEntity.ammunitionList.get(0).getDisplayName()));
            }
            if (tableBlockEntity.isPlayer2) {
                byName(level, tableBlockEntity.name2).sendSystemMessage(Component.translatable(MODID + ".piston").append(tableBlockEntity.ammunitionList.get(0).getDisplayName()));
            }
            ItemStack removeItem=remove(0, tableBlockEntity.ammunitionList);
            tableBlockEntity.pistonItem.set(0,removeItem );
            if(!tableBlockEntity.ammunitionList.get(0).is(Items.AIR)){
                if(removeItem.is(Items.GUNPOWDER)) tableBlockEntity.goodAmmunition--;
                if(removeItem.is(Items.REDSTONE)) tableBlockEntity.badAmmunition--;
                tableBlockEntity.ammunition--;
            }else{
                tableBlockEntity.ammunition=0;
                tableBlockEntity.badAmmunition=0;
                tableBlockEntity.goodAmmunition=0;
            }
            tableBlockEntity.tntUpTime = -tableBlockEntity.roundBeginTimeMax;
            tableBlockEntity.isPiston = true;
            tableBlockEntity.pistonTime = 0;
        }
        tableBlockEntity.oldHealth1 = tableBlockEntity.health1;
        tableBlockEntity.oldHealth2 = tableBlockEntity.health2;
    }

    public void initToolList(TableBlockEntity tableBlockEntity, int[] a, int[] b) {
        for (int i = 0; i < 8; i++) {
            ItemStack itemStack = tableBlockEntity.player1.get(i);
            if (getIndex(itemStack) != -1) {
                a[getIndex(itemStack)]++;
            }
        }

        for (int i = 0; i < 8; i++) {
            ItemStack itemStack = tableBlockEntity.player2.get(i);
            if (getIndex(itemStack) != -1) {
                b[getIndex(itemStack)]++;
            }
        }
    }

    public int getUsedNum(TableBlockEntity tableBlockEntity) {
        return Math.min(tableBlockEntity.ammunitionNum - tableBlockEntity.ammunition,7);
    }

    public int getRandomRightIndex(NonNullList<ItemStack> nonNullList){
        List<Integer> list =NonNullList.create();
        for(int i =0;i<nonNullList.size();i++){
            if(!nonNullList.get(i).isEmpty()) {
                list.add(i);
            }
        }
        if(list.size()==0) return -1;
        Random random = new Random();
        return list.get(random.nextInt(0,list.size()));
    }
}
