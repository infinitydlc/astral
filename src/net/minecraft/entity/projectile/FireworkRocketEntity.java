//
// Source code recreated from a .class file by IntelliJ IDEA
// (powered by FernFlower decompiler)
//

package net.minecraft.entity.projectile;

import java.util.OptionalInt;
import javax.annotation.Nullable;

import im.expensive.Expensive;
import im.expensive.functions.impl.combat.KillAura;
import net.minecraft.client.entity.player.ClientPlayerEntity;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.IRendersAsItem;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.MoverType;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.nbt.CompoundNBT;
import net.minecraft.nbt.ListNBT;
import net.minecraft.network.IPacket;
import net.minecraft.network.datasync.DataParameter;
import net.minecraft.network.datasync.DataSerializers;
import net.minecraft.network.datasync.EntityDataManager;
import net.minecraft.network.play.server.SSpawnObjectPacket;
import net.minecraft.particles.ParticleTypes;
import net.minecraft.util.DamageSource;
import net.minecraft.util.SoundCategory;
import net.minecraft.util.SoundEvents;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.BlockRayTraceResult;
import net.minecraft.util.math.EntityRayTraceResult;
import net.minecraft.util.math.RayTraceContext;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.util.math.RayTraceContext.BlockMode;
import net.minecraft.util.math.RayTraceContext.FluidMode;
import net.minecraft.util.math.RayTraceResult.Type;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.world.World;

public class FireworkRocketEntity extends ProjectileEntity implements IRendersAsItem {
    private static final DataParameter<ItemStack> FIREWORK_ITEM;
    private static final DataParameter<OptionalInt> BOOSTED_ENTITY_ID;
    private static final DataParameter<Boolean> field_213895_d;
    private int fireworkAge;
    private int lifetime;
    private LivingEntity boostedEntity;

    public FireworkRocketEntity(EntityType<? extends FireworkRocketEntity> p_i50164_1_, World p_i50164_2_) {
        super(p_i50164_1_, p_i50164_2_);
    }

    public FireworkRocketEntity(World worldIn, double x, double y, double z, ItemStack givenItem) {
        super(EntityType.FIREWORK_ROCKET, worldIn);
        this.fireworkAge = 0;
        this.setPosition(x, y, z);
        int i = 1;
        if (!givenItem.isEmpty() && givenItem.hasTag()) {
            this.dataManager.set(FIREWORK_ITEM, givenItem.copy());
            i += givenItem.getOrCreateChildTag("Fireworks").getByte("Flight");
        }

        this.setMotion(this.rand.nextGaussian() * 0.001, 0.05, this.rand.nextGaussian() * 0.001);
        this.lifetime = 10 * i + this.rand.nextInt(6) + this.rand.nextInt(7);
    }

    public FireworkRocketEntity(World p_i231581_1_, @Nullable Entity p_i231581_2_, double p_i231581_3_, double p_i231581_5_, double p_i231581_7_, ItemStack p_i231581_9_) {
        this(p_i231581_1_, p_i231581_3_, p_i231581_5_, p_i231581_7_, p_i231581_9_);
        this.setShooter(p_i231581_2_);
    }

    public FireworkRocketEntity(World p_i47367_1_, ItemStack p_i47367_2_, LivingEntity p_i47367_3_) {
        this(p_i47367_1_, p_i47367_3_, p_i47367_3_.getPosX(), p_i47367_3_.getPosY(), p_i47367_3_.getPosZ(), p_i47367_2_);
        this.dataManager.set(BOOSTED_ENTITY_ID, OptionalInt.of(p_i47367_3_.getEntityId()));
        this.boostedEntity = p_i47367_3_;
    }

    public FireworkRocketEntity(World p_i50165_1_, ItemStack p_i50165_2_, double p_i50165_3_, double p_i50165_5_, double p_i50165_7_, boolean p_i50165_9_) {
        this(p_i50165_1_, p_i50165_3_, p_i50165_5_, p_i50165_7_, p_i50165_2_);
        this.dataManager.set(field_213895_d, p_i50165_9_);
    }

    public FireworkRocketEntity(World p_i231582_1_, ItemStack p_i231582_2_, Entity p_i231582_3_, double p_i231582_4_, double p_i231582_6_, double p_i231582_8_, boolean p_i231582_10_) {
        this(p_i231582_1_, p_i231582_2_, p_i231582_4_, p_i231582_6_, p_i231582_8_, p_i231582_10_);
        this.setShooter(p_i231582_3_);
    }

    protected void registerData() {
        this.dataManager.register(FIREWORK_ITEM, ItemStack.EMPTY);
        this.dataManager.register(BOOSTED_ENTITY_ID, OptionalInt.empty());
        this.dataManager.register(field_213895_d, false);
    }

    public boolean isInRangeToRenderDist(double distance) {
        return distance < (double)4096.0F && !this.isAttachedToEntity();
    }

    public boolean isInRangeToRender3d(double x, double y, double z) {
        return super.isInRangeToRender3d(x, y, z) && !this.isAttachedToEntity();
    }

    public void tick() {
        super.tick();
        if (this.isAttachedToEntity()) {
            if (this.boostedEntity == null) {
                ((OptionalInt)this.dataManager.get(BOOSTED_ENTITY_ID)).ifPresent((p_213891_1_) -> {
                    Entity entity = this.world.getEntityByID(p_213891_1_);
                    if (entity instanceof LivingEntity) {
                        this.boostedEntity = (LivingEntity)entity;
                    }

                });
            }

            if (this.boostedEntity != null) {
                if (this.boostedEntity.isElytraFlying()) {
                    double defaultSpeed = (double)1.75F;
                    double boostedSpeed = defaultSpeed;


                    KillAura KillAura = Expensive.getInstance().getFunctionRegistry().getKillAura();
                    if (KillAura.isState() && (Boolean)KillAura.getOptions().getValueByName("Коррекция движения").get() && this.boostedEntity instanceof ClientPlayerEntity) {
                        Vector3d vector3d = this.getVectorForRotation(KillAura.rotateVector.y, KillAura.rotateVector.x);
                        Vector3d vector3d1 = this.boostedEntity.getMotion();
                        this.boostedEntity.setMotion(vector3d1.add(vector3d.x * 0.1 + (vector3d.x * boostedSpeed - vector3d1.x) * (double)0.5F, vector3d.y * 0.1 + (vector3d.y * boostedSpeed - vector3d1.y) * boostedSpeed, vector3d.z * 0.1 + (vector3d.z * boostedSpeed - vector3d1.z) * (double)0.5F));
                    } else {
                        Vector3d vector3d = this.boostedEntity.getLookVec();
                        Vector3d vector3d1 = this.boostedEntity.getMotion();
                        this.boostedEntity.setMotion(vector3d1.add(vector3d.x * 0.1 + (vector3d.x * boostedSpeed - vector3d1.x) * (double)0.5F, vector3d.y * 0.1 + (vector3d.y * boostedSpeed - vector3d1.y) * (double)0.5F, vector3d.z * 0.1 + (vector3d.z * boostedSpeed - vector3d1.z) * (double)0.5F));
                    }
                }

                this.setPosition(this.boostedEntity.getPosX(), this.boostedEntity.getPosY(), this.boostedEntity.getPosZ());
                this.setMotion(this.boostedEntity.getMotion());
            }
        } else {
            if (!this.func_213889_i()) {
                double d2 = this.collidedHorizontally ? (double)1.0F : 1.15;
                this.setMotion(this.getMotion().mul(d2, (double)1.0F, d2).add((double)0.0F, 0.04, (double)0.0F));
            }

            Vector3d vector3d2 = this.getMotion();
            this.move(MoverType.SELF, vector3d2);
            this.setMotion(vector3d2);
        }

        RayTraceResult raytraceresult = ProjectileHelper.func_234618_a_(this, this::func_230298_a_);
        if (!this.noClip) {
            this.onImpact(raytraceresult);
            this.isAirBorne = true;
        }

        this.func_234617_x_();
        if (this.fireworkAge == 0 && !this.isSilent()) {
            this.world.playSound((PlayerEntity)null, this.getPosX(), this.getPosY(), this.getPosZ(), SoundEvents.ENTITY_FIREWORK_ROCKET_LAUNCH, SoundCategory.AMBIENT, 3.0F, 1.0F);
        }

        ++this.fireworkAge;
        if (this.world.isRemote && this.fireworkAge % 2 < 2) {
            this.world.addParticle(ParticleTypes.FIREWORK, this.getPosX(), this.getPosY() - 0.3, this.getPosZ(), this.rand.nextGaussian() * 0.05, -this.getMotion().y * (double)0.5F, this.rand.nextGaussian() * 0.05);
        }

        if (!this.world.isRemote && this.fireworkAge > this.lifetime) {
            this.func_213893_k();
        }

    }

    private void func_213893_k() {
        this.world.setEntityState(this, (byte)17);
        this.dealExplosionDamage();
        this.remove();
    }

    protected void onEntityHit(EntityRayTraceResult p_213868_1_) {
        super.onEntityHit(p_213868_1_);
        if (!this.world.isRemote) {
            this.func_213893_k();
        }

    }

    protected void func_230299_a_(BlockRayTraceResult p_230299_1_) {
        BlockPos blockpos = new BlockPos(p_230299_1_.getPos());
        this.world.getBlockState(blockpos).onEntityCollision(this.world, blockpos, this);
        if (!this.world.isRemote() && this.func_213894_l()) {
            this.func_213893_k();
        }

        super.func_230299_a_(p_230299_1_);
    }

    private boolean func_213894_l() {
        ItemStack itemstack = (ItemStack)this.dataManager.get(FIREWORK_ITEM);
        CompoundNBT compoundnbt = itemstack.isEmpty() ? null : itemstack.getChildTag("Fireworks");
        ListNBT listnbt = compoundnbt != null ? compoundnbt.getList("Explosions", 10) : null;
        return listnbt != null && !listnbt.isEmpty();
    }

    private void dealExplosionDamage() {
        float f = 0.0F;
        ItemStack itemstack = (ItemStack)this.dataManager.get(FIREWORK_ITEM);
        CompoundNBT compoundnbt = itemstack.isEmpty() ? null : itemstack.getChildTag("Fireworks");
        ListNBT listnbt = compoundnbt != null ? compoundnbt.getList("Explosions", 10) : null;
        if (listnbt != null && !listnbt.isEmpty()) {
            f = 5.0F + (float)(listnbt.size() * 2);
        }

        if (f > 0.0F) {
            if (this.boostedEntity != null) {
                this.boostedEntity.attackEntityFrom(DamageSource.func_233548_a_(this, this.func_234616_v_()), 5.0F + (float)(listnbt.size() * 2));
            }

            double d0 = (double)5.0F;
            Vector3d vector3d = this.getPositionVec();

            for(LivingEntity livingentity : this.world.getEntitiesWithinAABB(LivingEntity.class, this.getBoundingBox().grow((double)5.0F))) {
                if (livingentity != this.boostedEntity && !(this.getDistanceSq(livingentity) > (double)25.0F)) {
                    boolean flag = false;

                    for(int i = 0; i < 2; ++i) {
                        Vector3d vector3d1 = new Vector3d(livingentity.getPosX(), livingentity.getPosYHeight((double)0.5F * (double)i), livingentity.getPosZ());
                        RayTraceResult raytraceresult = this.world.rayTraceBlocks(new RayTraceContext(vector3d, vector3d1, BlockMode.COLLIDER, FluidMode.NONE, this));
                        if (raytraceresult.getType() == Type.MISS) {
                            flag = true;
                            break;
                        }
                    }

                    if (flag) {
                        float f1 = f * (float)Math.sqrt(((double)5.0F - (double)this.getDistance(livingentity)) / (double)5.0F);
                        livingentity.attackEntityFrom(DamageSource.func_233548_a_(this, this.func_234616_v_()), f1);
                    }
                }
            }
        }

    }

    private boolean isAttachedToEntity() {
        return ((OptionalInt)this.dataManager.get(BOOSTED_ENTITY_ID)).isPresent();
    }

    public boolean func_213889_i() {
        return (Boolean)this.dataManager.get(field_213895_d);
    }

    public void handleStatusUpdate(byte id) {
        if (id == 17 && this.world.isRemote) {
            if (!this.func_213894_l()) {
                for(int i = 0; i < this.rand.nextInt(3) + 2; ++i) {
                    this.world.addParticle(ParticleTypes.POOF, this.getPosX(), this.getPosY(), this.getPosZ(), this.rand.nextGaussian() * 0.05, 0.005, this.rand.nextGaussian() * 0.05);
                }
            } else {
                ItemStack itemstack = (ItemStack)this.dataManager.get(FIREWORK_ITEM);
                CompoundNBT compoundnbt = itemstack.isEmpty() ? null : itemstack.getChildTag("Fireworks");
                Vector3d vector3d = this.getMotion();
                this.world.makeFireworks(this.getPosX(), this.getPosY(), this.getPosZ(), vector3d.x, vector3d.y, vector3d.z, compoundnbt);
            }
        }

        super.handleStatusUpdate(id);
    }

    public void writeAdditional(CompoundNBT compound) {
        super.writeAdditional(compound);
        compound.putInt("Life", this.fireworkAge);
        compound.putInt("LifeTime", this.lifetime);
        ItemStack itemstack = (ItemStack)this.dataManager.get(FIREWORK_ITEM);
        if (!itemstack.isEmpty()) {
            compound.put("FireworksItem", itemstack.write(new CompoundNBT()));
        }

        compound.putBoolean("ShotAtAngle", (Boolean)this.dataManager.get(field_213895_d));
    }

    public void readAdditional(CompoundNBT compound) {
        super.readAdditional(compound);
        this.fireworkAge = compound.getInt("Life");
        this.lifetime = compound.getInt("LifeTime");
        ItemStack itemstack = ItemStack.read(compound.getCompound("FireworksItem"));
        if (!itemstack.isEmpty()) {
            this.dataManager.set(FIREWORK_ITEM, itemstack);
        }

        if (compound.contains("ShotAtAngle")) {
            this.dataManager.set(field_213895_d, compound.getBoolean("ShotAtAngle"));
        }

    }

    public ItemStack getItem() {
        ItemStack itemstack = (ItemStack)this.dataManager.get(FIREWORK_ITEM);
        return itemstack.isEmpty() ? new ItemStack(Items.FIREWORK_ROCKET) : itemstack;
    }

    public boolean canBeAttackedWithItem() {
        return false;
    }

    public IPacket<?> createSpawnPacket() {
        return new SSpawnObjectPacket(this);
    }

    static {
        FIREWORK_ITEM = EntityDataManager.createKey(FireworkRocketEntity.class, DataSerializers.ITEMSTACK);
        BOOSTED_ENTITY_ID = EntityDataManager.createKey(FireworkRocketEntity.class, DataSerializers.OPTIONAL_VARINT);
        field_213895_d = EntityDataManager.createKey(FireworkRocketEntity.class, DataSerializers.BOOLEAN);
    }
}
