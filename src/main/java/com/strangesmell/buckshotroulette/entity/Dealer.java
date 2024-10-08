package com.strangesmell.buckshotroulette.entity;

import com.strangesmell.buckshotroulette.BuckshotRoulette;
import com.strangesmell.buckshotroulette.block.TableBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.NonNullList;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.DifficultyInstance;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.MoveControl;
import net.minecraft.world.entity.ai.goal.FloatGoal;
import net.minecraft.world.entity.ai.goal.Goal;
import net.minecraft.world.entity.ai.goal.LookAtPlayerGoal;
import net.minecraft.world.entity.ai.goal.target.HurtByTargetGoal;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.raid.Raider;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ServerLevelAccessor;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.gameevent.GameEvent;
import net.minecraft.world.phys.Vec3;

import javax.annotation.Nullable;
import java.util.EnumSet;
import java.util.UUID;


public class Dealer extends Monster {
    public static final int TICKS_PER_FLAP = Mth.ceil(3.9279907F);
    protected static final EntityDataAccessor<Integer> PLACE = SynchedEntityData.defineId(Dealer.class, EntityDataSerializers.INT);
    public NonNullList<ItemStack> ammunitionList = NonNullList.withSize(8, ItemStack.EMPTY);
    public boolean shouldGetToolList;
    public int[] toolList;
    @Nullable
    private BlockPos boundOrigin;
    private boolean hasLimitedLife;
    private boolean hasTable;
    public boolean canJoinPlayer2 = false;
    private int limitedLifeTicks;
    public int stepTicks = 0;

    public String getTableName() {
        return this.getDisplayName().getString() + getPlace();
    }

    public String getStringName() {
        return this.getDisplayName().getString();
    }

    public Dealer(EntityType<? extends Monster> p_33002_, Level p_33003_) {
        super(p_33002_, p_33003_);
        this.moveControl = new Dealer.DealerMoveControl(this);
        this.xpReward = 3;
        hasTable = false;
        toolList = new int[]{-1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1, -1};
    }

    protected float getStandingEyeHeight(Pose p_260180_, EntityDimensions p_260049_) {
        return p_260049_.height - 0.28125F;
    }

    public boolean isFlapping() {
        return this.tickCount % TICKS_PER_FLAP == 0;
    }

    public void move(MoverType p_33997_, Vec3 p_33998_) {
        super.move(p_33997_, p_33998_);
        this.checkInsideBlocks();
    }

    public void tick() {
        if (stepTicks > 0) stepTicks--;
        this.noPhysics = true;
        super.tick();
        this.noPhysics = false;
        this.setNoGravity(true);
        if (this.hasLimitedLife && --this.limitedLifeTicks <= 0) {
            this.limitedLifeTicks = 20;
            this.hurt(this.damageSources().starve(), 1.0F);
        }

    }

    protected void registerGoals() {
        super.registerGoals();
        this.goalSelector.addGoal(0, new DealerChargeAttackGoal(this));
        this.goalSelector.addGoal(0, new FloatGoal(this));
        this.goalSelector.addGoal(8, new Dealer.DealerRandomMoveGoal());
        this.goalSelector.addGoal(9, new LookAtPlayerGoal(this, Player.class, 3.0F, 1.0F));
        this.goalSelector.addGoal(10, new LookAtPlayerGoal(this, Mob.class, 8.0F));
        this.targetSelector.addGoal(1, (new HurtByTargetGoal(this, Raider.class)).setAlertOthers());
    }

    public static AttributeSupplier setAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 40)
                .add(Attributes.ATTACK_DAMAGE, 4)
                .add(Attributes.ATTACK_SPEED, 1)
                .add(Attributes.MOVEMENT_SPEED, 0.4f)
                .build();
    }

    protected void defineSynchedData() {
        super.defineSynchedData();
        this.entityData.define(PLACE, 0);
    }

    public void readAdditionalSaveData(CompoundTag tag) {
        super.readAdditionalSaveData(tag);
        ContainerHelper.loadAllItems(tag, ammunitionList);
        toolList = tag.getIntArray("toolList");
        shouldGetToolList = tag.getBoolean("shouldGetToolList");
        canJoinPlayer2 = tag.getBoolean("canJoinPlayer2");
        stepTicks = tag.getInt("stepTicks");
        if (tag.contains("LifeTicks")) {
            this.setLimitedLife(tag.getInt("LifeTicks"));
        }

    }

    public void addAdditionalSaveData(CompoundTag tag) {
        super.addAdditionalSaveData(tag);
        ContainerHelper.saveAllItems(tag, ammunitionList);
        tag.putIntArray("toolList", toolList);
        tag.putBoolean("shouldGetToolList", shouldGetToolList);
        tag.putBoolean("canJoinPlayer2", canJoinPlayer2);
        tag.putInt("stepTicks", stepTicks);
        if (this.hasLimitedLife) {
            tag.putInt("LifeTicks", this.limitedLifeTicks);
        }

    }

    public Boolean getTable() {
        return this.hasTable;
    }

    public void setTable(Boolean bo) {
        this.hasTable = bo;
    }

    @Nullable
    public BlockPos getBoundOrigin() {
        return this.boundOrigin;
    }

    public void setBoundOrigin(@Nullable BlockPos p_34034_) {
        this.boundOrigin = p_34034_;
    }

    public int getPlace() {
        return this.entityData.get(PLACE);
    }

    public void setPlace(int place) {
        this.entityData.set(PLACE, place);
    }

    public void setLimitedLife(int p_33988_) {
        this.hasLimitedLife = true;
        this.limitedLifeTicks = p_33988_;
    }

    protected SoundEvent getAmbientSound() {
        return SoundEvents.VEX_AMBIENT;
    }

    protected SoundEvent getDeathSound() {
        return SoundEvents.VEX_DEATH;
    }

    protected SoundEvent getHurtSound(DamageSource p_34023_) {
        return SoundEvents.VEX_HURT;
    }

    public float getLightLevelDependentMagicValue() {
        return 1.0F;
    }

    @Nullable
    public SpawnGroupData finalizeSpawn(ServerLevelAccessor p_34002_, DifficultyInstance p_34003_, MobSpawnType p_34004_, @Nullable SpawnGroupData p_34005_, @Nullable CompoundTag p_34006_) {
        RandomSource randomsource = p_34002_.getRandom();
        this.populateDefaultEquipmentSlots(randomsource, p_34003_);
        this.populateDefaultEquipmentEnchantments(randomsource, p_34003_);
        return super.finalizeSpawn(p_34002_, p_34003_, p_34004_, p_34005_, p_34006_);
    }

    protected void populateDefaultEquipmentSlots(RandomSource p_219135_, DifficultyInstance p_219136_) {
        this.setItemSlot(EquipmentSlot.MAINHAND, new ItemStack(Items.IRON_SWORD));
        this.setDropChance(EquipmentSlot.MAINHAND, 0.0F);
    }

    public double getMyRidingOffset() {
        return 0.4D;
    }

    class DealerChargeAttackGoal extends Goal {
        protected BlockPos blockPos = BlockPos.ZERO;
        protected BlockEntity blockEntity;
        private final Dealer dealer;

        public DealerChargeAttackGoal(Dealer dealer) {
            this.dealer = dealer;
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        public boolean canUse() {
            return level().getGameTime() % 30 == 0 || (level().getGameTime() - 1) % 20 == 0;

        }

        protected boolean findNearestBlock() {
            int i = 5;
            int j = 5;
            BlockPos blockpos = this.dealer.blockPosition();
            BlockPos.MutableBlockPos blockpos$mutableblockpos = new BlockPos.MutableBlockPos();

            for (int k = 0; k <= j; k = k > 0 ? -k : 1 - k) {
                for (int l = 0; l < i; ++l) {
                    for (int i1 = 0; i1 <= l; i1 = i1 > 0 ? -i1 : 1 - i1) {
                        for (int j1 = i1 < l && i1 > -l ? l : 0; j1 <= l; j1 = j1 > 0 ? -j1 : 1 - j1) {
                            blockpos$mutableblockpos.setWithOffset(blockpos, i1, k - 1, j1);
                            if (this.dealer.isWithinRestriction(blockpos$mutableblockpos) && this.isValidTarget(this.dealer.level(), blockpos$mutableblockpos)) {
                                this.blockPos = blockpos$mutableblockpos;
                                setBoundOrigin(this.blockPos);
                                blockEntity = level().getBlockEntity(blockPos);
                                hasTable = true;
                                return true;
                            }
                        }
                    }
                }
            }
            hasTable = false;
            return false;
        }

        public boolean canContinueToUse() {
            return Dealer.this.getMoveControl().hasWanted() && Dealer.this.getTarget() != null && Dealer.this.getTarget().isAlive();
        }

        public void start() {
            if (findNearestBlock()) {
                if (Dealer.this.getPlace() == 2) {
                    Dealer.this.moveControl.setWantedPosition(blockPos.getX() + 0.5, blockPos.getY() + 1, blockPos.getZ(), 1.0D);
                    return;
                }
                Dealer.this.moveControl.setWantedPosition(blockPos.getX() + 0.5, blockPos.getY() + 1, blockPos.getZ() + 1, 1.0D);
            }
        }

        public void stop() {

        }

        public boolean requiresUpdateEveryTick() {
            return true;
        }

        protected boolean isValidTarget(LevelReader p_25153_, BlockPos pos) {
            if (!p_25153_.isEmptyBlock(pos.above())) {
                return false;
            } else {
                if (dealer.level().getBlockEntity(pos) instanceof TableBlockEntity tableBlockEntity) {
                    if (Dealer.this.canJoinPlayer2) {
                        if (tableBlockEntity.id2 != dealer.getUUID() && !tableBlockEntity.id2.equals(UUID.nameUUIDFromBytes("dealer".getBytes())))
                            return false;
                        if (tableBlockEntity.id2 == dealer.getUUID()) return true;
                        return (tableBlockEntity.name1.equals("") || tableBlockEntity.name1.equals(dealer.getTableName()) || tableBlockEntity.name2.equals("") || tableBlockEntity.name2.equals(dealer.getTableName()));
                    } else {
                        if (tableBlockEntity.id1 != dealer.getUUID() && !tableBlockEntity.id1.equals(UUID.nameUUIDFromBytes("dealer".getBytes())))
                            return false;
                        if (tableBlockEntity.id1 == dealer.getUUID()) return true;
                        return (tableBlockEntity.name1.equals("") || tableBlockEntity.name1.equals(dealer.getTableName()));
                    }

                }else return false;

            }
        }

        public void tick() {
            //todo:每tick调用还是每次canuse？
            if (blockEntity instanceof TableBlockEntity tableBlockEntity) {
                stepTicks--;
                AI ai = new AI();
                if (stepTicks <= 0 && tableBlockEntity.tntStartTime == 0) {
                    ai.use(tableBlockEntity, dealer);
                    stepTicks = 20;
                }

            }
        }
    }


    class DealerMoveControl extends MoveControl {
        public DealerMoveControl(Dealer p_34062_) {
            super(p_34062_);
        }

        public void tick() {
            if (this.operation == MoveControl.Operation.MOVE_TO) {
                Vec3 vec3 = new Vec3(this.wantedX - Dealer.this.getX(), this.wantedY - Dealer.this.getY(), this.wantedZ - Dealer.this.getZ());
                double d0 = vec3.length();
                if (d0 < Dealer.this.getBoundingBox().getSize()) {
                    this.operation = MoveControl.Operation.WAIT;
                    Dealer.this.setDeltaMovement(Dealer.this.getDeltaMovement().scale(0.5D));
                } else {
                    Dealer.this.setDeltaMovement(Dealer.this.getDeltaMovement().add(vec3.scale(this.speedModifier * 0.03D / d0)));
                    if (Dealer.this.getTarget() == null) {
                        Vec3 vec31 = Dealer.this.getDeltaMovement();
                        Dealer.this.setYRot(-((float) Mth.atan2(vec31.x, vec31.z)) * (180F / (float) Math.PI));
                        Dealer.this.yBodyRot = Dealer.this.getYRot();
                    } else {
                        double d2 = Dealer.this.getTarget().getX() - Dealer.this.getX();
                        double d1 = Dealer.this.getTarget().getZ() - Dealer.this.getZ();
                        Dealer.this.setYRot(-((float) Mth.atan2(d2, d1)) * (180F / (float) Math.PI));
                        Dealer.this.yBodyRot = Dealer.this.getYRot();
                    }
                }

            }
        }
    }

    class DealerRandomMoveGoal extends Goal {
        public DealerRandomMoveGoal() {
            this.setFlags(EnumSet.of(Goal.Flag.MOVE));
        }

        public boolean canUse() {
            return !Dealer.this.getMoveControl().hasWanted() && Dealer.this.random.nextInt(reducedTickDelay(7)) == 0;
        }

        public boolean canContinueToUse() {
            return false;
        }

        public void tick() {
            BlockPos blockpos = Dealer.this.getBoundOrigin();
            if (blockpos == null) {
                blockpos = Dealer.this.blockPosition();
            }

            for (int i = 0; i < 3; ++i) {
                BlockPos blockpos1;
                if (Dealer.this.hasTable) {
                    blockpos1 = blockpos.offset(Dealer.this.random.nextInt(1), Dealer.this.random.nextInt(1), Dealer.this.random.nextInt(1));
                    if (Dealer.this.getPlace() == 1) {
                        Dealer.this.moveControl.setWantedPosition((double) blockpos1.getX() + 0.5, (double) blockpos1.getY() + 1, (double) blockpos1.getZ() + 1, 0.25D);
                    } else if (Dealer.this.getPlace() == 2) {
                        Dealer.this.moveControl.setWantedPosition((double) blockpos1.getX() + 0.5, (double) blockpos1.getY() + 1, (double) blockpos1.getZ(), 0.25D);
                    }
                    if (Dealer.this.getTarget() == null) {
                        Dealer.this.getLookControl().setLookAt((double) blockpos1.getX() + 0.5D, (double) blockpos1.getY() + 0.5D, (double) blockpos1.getZ() + 0.5D, 180.0F, 20.0F);
                    }
                    break;

                } else {
                    blockpos1 = blockpos.offset(Dealer.this.random.nextInt(15) - 7, Dealer.this.random.nextInt(11) - 5, Dealer.this.random.nextInt(15) - 7);
                    if (Dealer.this.level().isEmptyBlock(blockpos1)) {

                        Dealer.this.moveControl.setWantedPosition((double) blockpos1.getX(), (double) blockpos1.getY() - 1, (double) blockpos1.getZ(), 0.25D);
                        if (Dealer.this.getTarget() == null) {
                            Dealer.this.getLookControl().setLookAt((double) blockpos1.getX() + 0.5D, (double) blockpos1.getY() + 0.5D, (double) blockpos1.getZ() + 0.5D, 180.0F, 20.0F);
                        }
                        break;
                    }
                }

            }

        }
    }
}