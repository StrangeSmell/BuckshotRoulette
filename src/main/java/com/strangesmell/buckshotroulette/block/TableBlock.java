package com.strangesmell.buckshotroulette.block;

import com.strangesmell.buckshotroulette.BuckshotRoulette;
import com.strangesmell.buckshotroulette.Config;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.properties.NoteBlockInstrument;
import net.minecraft.world.level.material.MapColor;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraftforge.common.util.FakePlayer;
import org.jetbrains.annotations.Nullable;

import java.util.Objects;
import java.util.UUID;

import static com.strangesmell.buckshotroulette.BuckshotRoulette.MODID;
import static com.strangesmell.buckshotroulette.BuckshotRoulette.items;
import static com.strangesmell.buckshotroulette.Util.remove;


public class TableBlock extends BaseEntityBlock {
    public static int pistonTimeMax = 30;

    public TableBlock() {
        super(BlockBehaviour.Properties.of().mapColor(MapColor.COLOR_BLACK).instrument(NoteBlockInstrument.BASEDRUM).requiresCorrectToolForDrops().strength(50.0F, 1200.0F));
    }

    public InteractionResult use(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        if (level.getBlockEntity(pos) instanceof TableBlockEntity tableBlockEntity) {
            if(player.isCreative()&&isInTools(player.getItemInHand(player.getUsedItemHand()))) {
                Vec3 viewPose = result.getLocation();
                int index=0;
                if (viewPose.z - pos.getZ() > 0.5) {
                    index = getPlayer1Index(state, level, pos, player, hand, result);
                    if((index>=8||index<0)||!tableBlockEntity.isRead) return InteractionResult.PASS;
                    tableBlockEntity.player1.set(index,player.getItemInHand(player.getUsedItemHand()));
                }else {
                    index = getPlayer2Index(state, level, pos, player, hand, result);
                    if((index>=8||index<0)||!tableBlockEntity.isRead) return InteractionResult.PASS;
                    tableBlockEntity.player2.set(index,player.getItemInHand(player.getUsedItemHand()));
                }
            }
        }
        if (level.isClientSide) {
            return InteractionResult.SUCCESS;
        } else {
            if (level.getBlockEntity(pos) instanceof TableBlockEntity tableBlockEntity) {
                if (!tableBlockEntity.isRead) {
                    if (tableBlockEntity.name1.equals("") || tableBlockEntity.name2.equals("")) {
                        if (!(Objects.equals(tableBlockEntity.name1, player.getName().getString()) || Objects.equals(tableBlockEntity.name2, player.getName().getString()))) {
                        //if (true) {
                            if (tableBlockEntity.name1.equals("")) {
                                tableBlockEntity.isPlayer1=true;
                                tableBlockEntity.id1=player.getUUID();
                                tableBlockEntity.name1 = player.getName().getString();
                                player.sendSystemMessage(Component.translatable(MODID + ".ready"));
                                player.sendSystemMessage(Component.translatable(MODID + ".ready2").withStyle(ChatFormatting.RED));
                            } else {
                                if (tableBlockEntity.name2.equals("")) {
                                    tableBlockEntity.isPlayer2=true;
                                    tableBlockEntity.id2=player.getUUID();
                                    tableBlockEntity.name2 = player.getName().getString();
                                    player.sendSystemMessage(Component.translatable(MODID + ".ready"));
                                    player.sendSystemMessage(Component.translatable(MODID + ".ready2").withStyle(ChatFormatting.RED));
                                    tableBlockEntity.isRead = true;
                                    if(tableBlockEntity.isPlayer1){
                                        Player player1 = byName(level, tableBlockEntity.name1);
                                        player1.sendSystemMessage(Component.translatable(MODID + ".all_ready"));
                                        player1.sendSystemMessage(Component.translatable(MODID + ".take_bet"));
                                        begin(player1);
                                    }
                                    if(tableBlockEntity.isPlayer2){
                                        Player player2 = byName(level, tableBlockEntity.name2);
                                        player2.sendSystemMessage(Component.translatable(MODID + ".all_ready"));
                                        player2.sendSystemMessage(Component.translatable(MODID + ".take_bet"));
                                        begin(player2);
                                    }
                                }
                            }
                        }
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

                        if (tableBlockEntity.chestFinish) {
                            //可以开始拿道具
                            if (player.getName().getString().equals(tableBlockEntity.name1)) {
                                if (tableBlockEntity.player1ToolNum > 0 && tableBlockEntity.player1.contains(ItemStack.EMPTY)) {
                                    int i = tableBlockEntity.player1.indexOf(ItemStack.EMPTY);
                                    if (tableBlockEntity.player1AddItemTime == 0) {
                                        tableBlockEntity.player1AddItemTime = 20;
                                        tableBlockEntity.player1ToolNum--;
                                        ItemStack itemStack = new ItemStack(tableBlockEntity.getRandomItem());
                                        tableBlockEntity.addItem1.set(0, itemStack);
                                        tableBlockEntity.player1.set(i, itemStack);
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
                                        tableBlockEntity.player2Round = false;
                                        if(tableBlockEntity.isPlayer1) byName(level, tableBlockEntity.name1).sendSystemMessage(Component.translatable(MODID + ".first").append(tableBlockEntity.name1));
                                        if(tableBlockEntity.isPlayer2) byName(level, tableBlockEntity.name2).sendSystemMessage(Component.translatable(MODID + ".first").append(tableBlockEntity.name1));

                                    } else {
                                        tableBlockEntity.player2Round = true;
                                        tableBlockEntity.player1Round = false;
                                        if(tableBlockEntity.isPlayer1) byName(level, tableBlockEntity.name1).sendSystemMessage(Component.translatable(MODID + ".first").append(tableBlockEntity.name2));
                                        if(tableBlockEntity.isPlayer2) byName(level, tableBlockEntity.name2).sendSystemMessage(Component.translatable(MODID + ".first").append(tableBlockEntity.name2));

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
                        level.sendBlockUpdated(pos, state, state, 2);
                    }

                    if ((tableBlockEntity.player1Round || tableBlockEntity.player2IsWeb) && !tableBlockEntity.roundBegin&&tableBlockEntity.chestFinish) {
                        tableBlockEntity.toolTime=true;
                        //为了能让玩家一可以在玩家2被网时解放蜘蛛网
                        if (tableBlockEntity.player2IsWeb && tableBlockEntity.player2Round) {
                            player.sendSystemMessage(Component.literal(tableBlockEntity.name2).append(Component.translatable("isWeb")));
                            tableBlockEntity.player2IsWeb = false;
                            tableBlockEntity.player2Round = false;
                            tableBlockEntity.player1Round = true;
                        }
                        if(tableBlockEntity.player1IsWeb){
                            tableBlockEntity.toolTime=false;
                            tableBlockEntity.player1Round = false;
                            tableBlockEntity.player2Round = true;
                            tableBlockEntity.player1IsWeb = false;
                            tableBlockEntity.setWebRound(1);//跳过了一回合
                        }else{
                           if(tableBlockEntity.player1Round ) if(tableBlockEntity.webRound==1) tableBlockEntity.setWebRound(0);
                        }
                        if (player.getName().getString().equals(tableBlockEntity.name1)) {
                            //某玩家回合flag：player1=true。player2=false
                            //道具时间开始
                            if (tableBlockEntity.toolTime) {
                                //使用道具
                                int index = getPlayer1Index(state, level, pos, player, hand, result);
                                if (tableBlockEntity.selectPlayerTime) {
                                    //获取选取的玩家
                                    String select = "";
                                    Vec3 viewPose = result.getLocation();
                                    if (viewPose.z - pos.getZ() > 0.5) select = tableBlockEntity.name1;
                                    else select = tableBlockEntity.name2;
                                    if (select.equals("")) {
                                        level.sendBlockUpdated(pos, state, state, 2);
                                        return InteractionResult.CONSUME;
                                    } else tableBlockEntity.selectPlayerTime = false;
                                    //may should add a time flag
                                    if (remove(0, tableBlockEntity.ammunitionList).is(Items.GUNPOWDER)) {
                                        tableBlockEntity.tntExplosion = true;
                                        tableBlockEntity.ammunition--;
                                        tableBlockEntity.goodAmmunition--;
                                        if (select.equals(tableBlockEntity.name1)) {
                                            tableBlockEntity.tntStartTime = tableBlockEntity.roundBeginTimeMax;

                                            if (tableBlockEntity.health1 >= 2) {
                                                tableBlockEntity.health1--;
                                                if (tableBlockEntity.addGunPower) {
                                                    tableBlockEntity.health1--;

                                                }
                                            } else {
                                                tableBlockEntity.health1--;
                                            }
                                            level.sendBlockUpdated(pos, state, state, 2);
                                        } else {
                                            tableBlockEntity.tntStartTime = -tableBlockEntity.roundBeginTimeMax;
                                            if (tableBlockEntity.health2 >= 2) {
                                                tableBlockEntity.health2--;
                                                if (tableBlockEntity.addGunPower) {
                                                    tableBlockEntity.health2--;
                                                }
                                            } else {
                                                tableBlockEntity.health2--;
                                            }
                                            level.sendBlockUpdated(pos, state, state, 2);
                                        }
                                        //tnt的移动,爆炸特效
                                        tableBlockEntity.player1Round = false;
                                        tableBlockEntity.player2Round = true;
                                    } else {
                                        tableBlockEntity.tntExplosion = false;
                                        if (select.equals(tableBlockEntity.name1)) {
                                            tableBlockEntity.tntStartTime = tableBlockEntity.roundBeginTimeMax;
                                            byName(level, tableBlockEntity.name1).sendSystemMessage(Component.translatable("continue"));

                                        } else {
                                            tableBlockEntity.player1Round = false;
                                            tableBlockEntity.player2Round = true;
                                            tableBlockEntity.tntStartTime = -tableBlockEntity.roundBeginTimeMax;
                                        }
                                        tableBlockEntity.ammunition--;
                                        tableBlockEntity.badAmmunition--;

                                    }
                                    tableBlockEntity.addGunPower = false;
                                    level.sendBlockUpdated(pos, state, state, 2);
                                    return InteractionResult.CONSUME;
                                }

                                if (tableBlockEntity.isFishingTime) {
                                    int fishIndex = getPlayer2Index(state, level, pos, player, hand, result);
                                    if (fishIndex >= 0 && fishIndex < 8) {
                                        ItemStack itemStack = tableBlockEntity.player2.get(fishIndex);
                                        if (itemStack.is(Items.FISHING_ROD)) return InteractionResult.CONSUME;
                                        if (tableBlockEntity.webRound2==1&&itemStack.is(Items.COBWEB)) return InteractionResult.CONSUME;

                                        tableBlockEntity.player2.set(fishIndex, ItemStack.EMPTY);
                                        level.playSound(null, tableBlockEntity.getBlockPos(), SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.AMBIENT, 1, 1);
                                        itemFunction(tableBlockEntity.name1, itemStack, tableBlockEntity, level, player);
                                        tableBlockEntity.isFishingTime = false;
                                        tableBlockEntity.pistonTime = pistonTimeMax;
                                    } else {
                                        level.sendBlockUpdated(pos, state, state, 2);
                                        return InteractionResult.CONSUME;
                                    }
                                }

                                if (index >= 0) {
                                    if (index == 8) {
                                        //若使用tnt
                                        player.sendSystemMessage(Component.translatable("useTNT"));
                                        tableBlockEntity.selectPlayerTime = true;
                                        tableBlockEntity.spyglass = false;
                                        level.sendBlockUpdated(pos, state, state, 2);
                                        return InteractionResult.CONSUME;
                                    } else {
                                        //使用道具
                                        ItemStack itemStack = tableBlockEntity.player1.get(index);
                                        if (tableBlockEntity.webRound2==1&&itemStack.is(Items.COBWEB)) return InteractionResult.CONSUME;

                                        tableBlockEntity.moveItem1.set(index, itemStack);
                                        tableBlockEntity.moveItem1Time[index] = 20;
                                        itemFunction(player.getName().getString(), itemStack, tableBlockEntity, level, player);
                                        tableBlockEntity.player1.set(index, ItemStack.EMPTY);
                                        level.sendBlockUpdated(pos, state, state, 2);
                                    }
                                } else {
                                    level.sendBlockUpdated(pos, state, state, 2);
                                    return InteractionResult.CONSUME;
                                }

                            }
                        }

                    }
                    if ((tableBlockEntity.player2Round || tableBlockEntity.player1IsWeb) && !tableBlockEntity.roundBegin&&tableBlockEntity.chestFinish) {
                        tableBlockEntity.toolTime=true;
                        if (tableBlockEntity.player1IsWeb && tableBlockEntity.player1Round) {
                            player.sendSystemMessage(Component.literal(tableBlockEntity.name1).append(Component.translatable("isWeb")));
                            tableBlockEntity.player1IsWeb = false;
                            tableBlockEntity.player1Round = false;
                            tableBlockEntity.player2Round = true;
                        }
                        if(tableBlockEntity.player2IsWeb){
                            tableBlockEntity.toolTime=false;
                            tableBlockEntity.player2Round = false;
                            tableBlockEntity.player1Round = true;
                            tableBlockEntity.player2IsWeb = false;
                            tableBlockEntity.setWebRound2(1);
                        }else{
                            if(tableBlockEntity.player2Round ) if(tableBlockEntity.webRound2==1) tableBlockEntity.setWebRound2(0);
                        }
                        if (player.getName().getString().equals(tableBlockEntity.name2)) {
                            //某玩家回合flag：player1=true。player2=false
                            //道具时间开始
                            if (tableBlockEntity.toolTime) {
                                //使用道具
                                int index = getPlayer2Index(state, level, pos, player, hand, result);
                                if (tableBlockEntity.selectPlayerTime) {
                                    //获取选取的玩家
                                    String select = "";
                                    Vec3 viewPose = result.getLocation();
                                    String s ;
                                    if (viewPose.z - pos.getZ() > 0.5) select = tableBlockEntity.name1;
                                    else select = tableBlockEntity.name2;
                                    if (select.equals("")) {
                                        level.sendBlockUpdated(pos, state, state, 2);
                                        return InteractionResult.CONSUME;
                                    } else tableBlockEntity.selectPlayerTime = false;
                                    //may should add a time flag

                                    if (remove(0, tableBlockEntity.ammunitionList).is(Items.GUNPOWDER)) {
                                        tableBlockEntity.tntExplosion = true;
                                        tableBlockEntity.ammunition--;
                                        tableBlockEntity.goodAmmunition--;
                                        if (select.equals(tableBlockEntity.name1)) {
                                            tableBlockEntity.tntStartTime = tableBlockEntity.roundBeginTimeMax;

                                            if (tableBlockEntity.health1 >= 2) {
                                                tableBlockEntity.health1--;
                                                if (tableBlockEntity.addGunPower) {
                                                    tableBlockEntity.health1--;
                                                }
                                            } else {
                                                tableBlockEntity.health1--;
                                            }
                                            level.sendBlockUpdated(pos, state, state, 2);
                                        } else {
                                            tableBlockEntity.tntStartTime = -tableBlockEntity.roundBeginTimeMax;
                                            if (tableBlockEntity.health2 >= 2) {
                                                tableBlockEntity.health2--;
                                                if (tableBlockEntity.addGunPower) {
                                                    tableBlockEntity.health2--;
                                                }
                                            } else {
                                                tableBlockEntity.health2--;
                                            }
                                            level.sendBlockUpdated(pos, state, state, 2);
                                        }
                                        tableBlockEntity.player1Round = true;
                                        tableBlockEntity.player2Round = false;
                                    } else {
                                        tableBlockEntity.tntExplosion=false;
                                        if (select.equals(tableBlockEntity.name1)) {
                                            tableBlockEntity.tntStartTime = tableBlockEntity.roundBeginTimeMax;
                                            tableBlockEntity.player1Round = true;
                                            tableBlockEntity.player2Round = false;
                                        } else {
                                            tableBlockEntity.tntStartTime = -tableBlockEntity.roundBeginTimeMax;
                                            byName(level, tableBlockEntity.name2).sendSystemMessage(Component.translatable("continue"));

                                        }
                                        tableBlockEntity.ammunition--;
                                        tableBlockEntity.badAmmunition--;

                                    }
                                    tableBlockEntity.addGunPower = false;
                                    level.sendBlockUpdated(pos, state, state, 2);

                                    return InteractionResult.CONSUME;
                                }

                                if (tableBlockEntity.isFishingTime) {
                                    int fishIndex = getPlayer1Index(state, level, pos, player, hand, result);
                                    if (fishIndex >= 0 && fishIndex < 8) {
                                        ItemStack itemStack = tableBlockEntity.player1.get(fishIndex);
                                        if (itemStack.is(Items.FISHING_ROD)) return InteractionResult.CONSUME;
                                        if (tableBlockEntity.webRound==1&&itemStack.is(Items.COBWEB)) return InteractionResult.CONSUME;
                                        tableBlockEntity.player1.set(fishIndex, ItemStack.EMPTY);
                                        level.playSound(null, tableBlockEntity.getBlockPos(), SoundEvents.FISHING_BOBBER_RETRIEVE, SoundSource.AMBIENT, 1, 1);
                                        itemFunction(tableBlockEntity.name2, itemStack, tableBlockEntity, level, player);
                                        tableBlockEntity.isFishingTime = false;
                                        tableBlockEntity.pistonTime = pistonTimeMax;
                                    } else {
                                        level.sendBlockUpdated(pos, state, state, 2);
                                        return InteractionResult.CONSUME;
                                    }
                                }

                                if (index >= 0) {
                                    if (index == 8) {
                                        //若使用tnt
                                        player.sendSystemMessage(Component.translatable("useTNT"));
                                        tableBlockEntity.selectPlayerTime = true;
                                        tableBlockEntity.spyglass = false;
                                        level.sendBlockUpdated(pos, state, state, 2);
                                        return InteractionResult.CONSUME;
                                    } else {
                                        //使用道具
                                        ItemStack itemStack = tableBlockEntity.player2.get(index);
                                        if (tableBlockEntity.webRound==1&&itemStack.is(Items.COBWEB)) return InteractionResult.CONSUME;
                                        tableBlockEntity.moveItem2.set(index, itemStack);
                                        tableBlockEntity.moveItem2Time[index] = 20;
                                        itemFunction(player.getName().getString(), itemStack, tableBlockEntity, level, player);
                                        tableBlockEntity.player2.set(index, ItemStack.EMPTY);
                                        level.sendBlockUpdated(pos, state, state, 2);
                                    }
                                } else {
                                    level.sendBlockUpdated(pos, state, state, 2);
                                    return InteractionResult.CONSUME;
                                }
                            }
                        }
                    }
                }
            }
            level.sendBlockUpdated(pos, state, state, 2);
            return InteractionResult.CONSUME;
        }
    }

    static String nameUuid = MODID + "modifier";
    static byte[] nameBytes = nameUuid.getBytes();
    public static final UUID uuid = UUID.nameUUIDFromBytes(nameBytes);

    public static void begin(Player player) {
        //收取赌注
        if(!Config.have_stake){
            return;
        }
        double amount = 0;

        if (player.getAttribute(Attributes.MAX_HEALTH).getModifier(uuid) != null) {
            amount = player.getAttribute(Attributes.MAX_HEALTH).getModifier(uuid).getAmount();
            player.getAttribute(Attributes.MAX_HEALTH).removePermanentModifier(uuid);
        }
        AttributeModifier modifier = new AttributeModifier(uuid, MODID + "modifier", amount - 1, AttributeModifier.Operation.ADDITION);
        player.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(modifier);
    }

    public static void outOfAmmunition(TableBlockEntity tableBlockEntity) {
        if (tableBlockEntity.ammunition <= 0) {
            //弹药耗尽回合结束
            tableBlockEntity.initAmmunition = true;
            tableBlockEntity.shouldReRandom = false;
            tableBlockEntity.roundBeginTime = tableBlockEntity.roundBeginTimeMax;
            //byName(tableBlockEntity.getLevel(), tableBlockEntity.name1).sendSystemMessage(Component.translatable("new_round"));
            //byName(tableBlockEntity.getLevel(), tableBlockEntity.name2).sendSystemMessage(Component.translatable("new_round"));
            tableBlockEntity.roundBegin = true;
        }
    }

    public static Player byName(Level level, String name1) {
        for (Player player : level.players()) {
            if (player.getName().getString().equals(name1)) {
                return player;
            }
        }
        return null;
    }

    public static void end(Level level, TableBlockEntity tableBlockEntity) {
        //结束游戏，重置信息
        tableBlockEntity.init();
        tableBlockEntity.initFlag();
    }

    public static void end2(String winner, TableBlockEntity tableBlockEntity) {

        //给予奖励
        Player player = byName(tableBlockEntity.getLevel(), winner);

        player.sendSystemMessage(Component.translatable(MODID + ".winner"));
        player.sendSystemMessage(Component.translatable(MODID + ".stronger"));
        if(!Config.have_stake){
            return;
        }
        double amount = 0;
        if (player.getAttribute(Attributes.MAX_HEALTH).getModifier(uuid) != null) {
            amount = player.getAttribute(Attributes.MAX_HEALTH).getModifier(uuid).getAmount();
            player.getAttribute(Attributes.MAX_HEALTH).removePermanentModifier(uuid);
        }
        AttributeModifier modifier = new AttributeModifier(uuid, MODID + "modifier", amount + 2, AttributeModifier.Operation.ADDITION);
        player.getAttribute(Attributes.MAX_HEALTH).addPermanentModifier(modifier);


    }
    public static void player1Dead(Level level, TableBlockEntity tableBlockEntity) {
        level.playSound(null, tableBlockEntity.getBlockPos(), SoundEvents.PLAYER_DEATH, SoundSource.AMBIENT, 1, 1);
        tableBlockEntity.shouldReRandom = true;
        tableBlockEntity.player2WinNum++;
        tableBlockEntity.initHealth();
        tableBlockEntity.roundBeginTime = tableBlockEntity.roundBeginTimeMax;
        if (tableBlockEntity.player2WinNum == 2) {
            if(tableBlockEntity.isPlayer2){
                end2(tableBlockEntity.name2, tableBlockEntity);
            }
            end(level, tableBlockEntity);
            tableBlockEntity.end = true;
        }
        tableBlockEntity.roundBegin = true;
        tableBlockEntity.initAmmunition = true;
    }

    public static void player2Dead(Level level, TableBlockEntity tableBlockEntity) {
        level.playSound(null, tableBlockEntity.getBlockPos(), SoundEvents.PLAYER_DEATH, SoundSource.AMBIENT, 1, 1);
        tableBlockEntity.shouldReRandom = true;
        tableBlockEntity.player1WinNum++;
        tableBlockEntity.initHealth();
        tableBlockEntity.roundBeginTime = tableBlockEntity.roundBeginTimeMax;
        if (tableBlockEntity.player1WinNum == 2) {
            if(tableBlockEntity.isPlayer1){
                end2(tableBlockEntity.name1, tableBlockEntity);
            }
            end(level, tableBlockEntity);
            tableBlockEntity.end = true;
        }
        tableBlockEntity.roundBegin = true;
        tableBlockEntity.initAmmunition = true;
    }

    public double onePix = 0.03125;

    public int getPlayer1Index(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        Direction hitFace = result.getDirection();
        Vec3 viewPose = result.getLocation();
        viewPose = new Vec3(viewPose.x - pos.getX(), viewPose.y - pos.getY(), viewPose.z - pos.getZ());
        int line = -1;
        int row = -1;
        if (hitFace == Direction.UP) {
            if (viewPose.x > 13 * onePix && viewPose.x < 19 * onePix && viewPose.z > 13 * onePix && viewPose.z < 19 * onePix) {
                return 8;
            }
            if (viewPose.z > 0.5) {//右键了玩家1的区域
                if (25 * onePix > viewPose.z && viewPose.z > 21 * onePix) {
                    line = 0;
                    if (6 * onePix > viewPose.x && viewPose.x > 2 * onePix) {
                        row = 0;
                    } else if (11 * onePix > viewPose.x && viewPose.x > 7 * onePix) {
                        row = 1;
                    } else if (25 * onePix > viewPose.x && viewPose.x > 21 * onePix) {
                        row = 2;
                    } else if (30 * onePix > viewPose.x && viewPose.x > 26 * onePix) {
                        row = 3;
                    }
                } else if (26 * onePix < viewPose.z && viewPose.z < 30 * onePix) {
                    line = 1;
                    if (6 * onePix > viewPose.x && viewPose.x > 2 * onePix) {
                        row = 0;
                    } else if (11 * onePix > viewPose.x && viewPose.x > 7 * onePix) {
                        row = 1;
                    } else if (25 * onePix > viewPose.x && viewPose.x > 21 * onePix) {
                        row = 2;
                    } else if (30 * onePix > viewPose.x && viewPose.x > 26 * onePix) {
                        row = 3;
                    }
                }
            }
        }
        if (line == -1 || row == -1) return -1;
        else return line * 4 + row;
    }

    public int getPlayer2Index(BlockState state, Level level, BlockPos pos, Player player, InteractionHand hand, BlockHitResult result) {
        Direction hitFace = result.getDirection();
        Vec3 viewPose = result.getLocation();
        viewPose = new Vec3(viewPose.x - pos.getX(), viewPose.y - pos.getY(), viewPose.z - pos.getZ());
        int line = -1;
        int row = -1;
        if (hitFace == Direction.UP) {
            if (viewPose.x > 13 * onePix && viewPose.x < 19 * onePix && viewPose.z > 13 * onePix && viewPose.z < 19 * onePix) {
                return 8;
            }
            if (viewPose.z < 0.5) {//右键了玩家2的区域
                if (11 * onePix > viewPose.z && viewPose.z > 7 * onePix) {
                    line = 0;
                    if (30 * onePix > viewPose.x && viewPose.x > 26 * onePix) {
                        row = 0;
                    } else if (25 * onePix > viewPose.x && viewPose.x > 21 * onePix) {
                        row = 1;
                    } else if (11 * onePix > viewPose.x && viewPose.x > 7 * onePix) {
                        row = 2;
                    } else if (6 * onePix > viewPose.x && viewPose.x > 2 * onePix) {
                        row = 3;
                    }
                } else if (2 * onePix < viewPose.z && viewPose.z < 6 * onePix) {
                    line = 1;
                    if (30 * onePix > viewPose.x && viewPose.x > 26 * onePix) {
                        row = 0;
                    } else if (25 * onePix > viewPose.x && viewPose.x > 21 * onePix) {
                        row = 1;
                    } else if (11 * onePix > viewPose.x && viewPose.x > 7 * onePix) {
                        row = 2;
                    } else if (6 * onePix > viewPose.x && viewPose.x > 2 * onePix) {
                        row = 3;
                    }
                }
            }
        }
        if (line == -1 || row == -1) return -1;
        else return line * 4 + row;
    }

    public void itemFunction(String useName, ItemStack itemStack, TableBlockEntity tableBlockEntity, Level level,@javax.annotation.Nullable Player player) {
        if(tableBlockEntity.name1==null||tableBlockEntity.name2==null)return;
        if (itemStack.is(Items.SPYGLASS)) {
            //加一个init=30的int，>0时渲染红石或者火药
            level.playSound(null, tableBlockEntity.getBlockPos(), SoundEvents.SPYGLASS_USE, SoundSource.AMBIENT, 1, 1);
            if(tableBlockEntity.isPlayer1){
                byName(level,tableBlockEntity.name1).sendSystemMessage(Component.literal(useName).append(Component.translatable(MODID+".use_spyglass")));
            }
            if(tableBlockEntity.isPlayer2){
                byName(level,tableBlockEntity.name2).sendSystemMessage(Component.literal(useName).append(Component.translatable(MODID+".use_spyglass")));
            }
            player.sendSystemMessage(Component.translatable(MODID + ".spyglass").append(tableBlockEntity.ammunitionList.get(0).getDisplayName()));
            tableBlockEntity.spyglass = true;

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
            player.sendSystemMessage(Component.translatable(MODID + ".cobweb"));
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
            player.sendSystemMessage(Component.translatable(MODID + ".potato"));
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
            player.sendSystemMessage(Component.translatable(MODID + ".dye"));
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
            player.sendSystemMessage(Component.translatable(MODID + ".apple"));
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
            player.sendSystemMessage(Component.translatable(MODID + ".gunpowder"));
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
            player.sendSystemMessage(Component.translatable(MODID + ".fishing_rod"));
        } else if (itemStack.is(Items.OBSERVER)) {
            if(tableBlockEntity.isPlayer1){
                byName(level,tableBlockEntity.name1).sendSystemMessage(Component.literal(useName).append(Component.translatable(MODID+".use_observer")));
            }
            if(tableBlockEntity.isPlayer2){
                byName(level,tableBlockEntity.name2).sendSystemMessage(Component.literal(useName).append(Component.translatable(MODID+".use_observer")));
            }
            level.playSound(null, tableBlockEntity.getBlockPos(), SoundEvents.TRIPWIRE_CLICK_ON, SoundSource.AMBIENT, 1, 1);
            int i = tableBlockEntity.random.nextInt(1, tableBlockEntity.ammunition);
            player.sendSystemMessage(Component.translatable(MODID + ".observer").append(i + 1 + " ").append(Component.translatable(MODID + ".observer2")).append(tableBlockEntity.ammunitionList.get(i).getDisplayName()));
        } else if (itemStack.is(Items.PISTON)) {
            level.playSound(null, tableBlockEntity.getBlockPos(), SoundEvents.PISTON_EXTEND, SoundSource.AMBIENT, 1, 1);
            if(tableBlockEntity.isPlayer1) byName(level, tableBlockEntity.name1).sendSystemMessage(Component.translatable(MODID + ".piston").append(tableBlockEntity.ammunitionList.get(0).getDisplayName()));
            if(tableBlockEntity.isPlayer2) byName(level, tableBlockEntity.name2).sendSystemMessage(Component.translatable(MODID + ".piston").append(tableBlockEntity.ammunitionList.get(0).getDisplayName()));
            ItemStack removeItem=remove(0, tableBlockEntity.ammunitionList);
            tableBlockEntity.pistonItem.set(0,removeItem );
            if(!tableBlockEntity.ammunitionList.get(0).is(Items.AIR)){

                if(removeItem.is(Items.GUNPOWDER)) tableBlockEntity.goodAmmunition--;
                if(removeItem.is(Items.REDSTONE)) tableBlockEntity.badAmmunition--;
                tableBlockEntity.ammunition--;
            }

            tableBlockEntity.isPiston = true;
            tableBlockEntity.pistonTime = 0;
            tableBlockEntity.tntUpTime = -tableBlockEntity.roundBeginTimeMax;
            outOfAmmunition(tableBlockEntity);
        }
        tableBlockEntity.oldHealth1=tableBlockEntity.health1;
        tableBlockEntity.oldHealth2=tableBlockEntity.health2;
    }

    @Override
    public RenderShape getRenderShape(BlockState p_51567_) {
        return RenderShape.MODEL;
    }

    public BlockEntityType<? extends TableBlockEntity> blockEntityType() {
        return BuckshotRoulette.TableBlockEntity.get();
    }

    @Nullable
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level pLevel, BlockState pState, BlockEntityType<T> pBlockEntityType) {
        return createTickerHelper(pBlockEntityType, this.blockEntityType(), TableBlockEntity::tick);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pPos, BlockState pState) {
        return new TableBlockEntity(pPos, pState);
    }

    public static boolean isInTools(ItemStack itemStack) {
        for(Item item : items){
            if(itemStack.is(item))return true;
        }
        return false;
    }
}
