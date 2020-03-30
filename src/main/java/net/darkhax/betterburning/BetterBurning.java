package net.darkhax.betterburning;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.AbstractSkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.init.MobEffects;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;

@Mod(modid = "betterburning", name = "Better Burning", version = "@VERSION@", certificateFingerprint = "@FINGERPRINT@")
public class BetterBurning {
    
    private final Config configuration = new Config();
    
    public BetterBurning() {
        
    	MinecraftForge.EVENT_BUS.register(this);
    }
    
    @SubscribeEvent
    public void onLivingDeath (LivingDeathEvent event) {
        
        // Fixes some edge cases where fire damage sources won't cause mobs to drop their
        // cooked items.
        if (event.getSource().isFireDamage() && this.configuration.shouldDamageSourceCauseFire() && !event.getEntityLiving().isBurning() && !event.getEntity().world.isRemote) {
            
            event.getEntityLiving().setFire(1);
        }
    }
    
    @SubscribeEvent
    public void onEntityJoinWorld (EntityJoinWorldEvent event) {
        
        // Allows skeletons to shoot flaming arrows when they are on fire.
        if (this.configuration.shouldSkeletonsShootFireArrows() && event.getEntity() instanceof EntityArrow && !event.getEntity().world.isRemote) {
            
            final EntityArrow arrowEntity = (EntityArrow) event.getEntity();
            final Entity shooter = arrowEntity.shootingEntity;
            
            if (shooter instanceof AbstractSkeleton && shooter.isBurning() && !shooter.isDead && this.tryPercentage(this.configuration.getSkeletonFlameArrowChance())) {
                
                arrowEntity.setFire(100);
            }
        }
    }
    
    @SubscribeEvent
    public void onLivingTick (LivingUpdateEvent event) {
        
        // If entity has fire resistance it will extinguish them right away when on fire.
        if (this.configuration.shouldFireResExtinguish() && !event.getEntityLiving().world.isRemote && event.getEntityLiving().isBurning() && event.getEntityLiving().isPotionActive(MobEffects.FIRE_RESISTANCE)) {
            
            event.getEntityLiving().extinguish();
        }
    }
    
    @SubscribeEvent
    public void onLivingAttack (LivingAttackEvent event) {
        
        if (this.configuration.shouldFireDamageSpread() && !event.getEntity().world.isRemote) {
            
            final Entity sourceEntity = event.getSource().getTrueSource();
            
            if (sourceEntity instanceof EntityLivingBase) {
                
                final EntityLivingBase sourceLiving = (EntityLivingBase) sourceEntity;
                final ItemStack heldItem = sourceLiving.getHeldItemMainhand();
                
                // Allows fire damage to spread from entity to entity
                if (!(sourceLiving instanceof EntityZombie) && heldItem.isEmpty() && sourceLiving.isBurning() && this.tryPercentage(this.configuration.getFireDamageSpreadChance())) {
                    
                    final float damage = Math.max(1, event.getEntityLiving().world.getDifficultyForLocation(new BlockPos(event.getEntity())).getAdditionalDifficulty());
                    event.getEntityLiving().setFire(2 * (int) damage);
                }
                
                // Allows flint and steel to do fire damage to mobs
                else if (heldItem.getItem() == Items.FLINT_AND_STEEL && this.configuration.shouldFlintAndSteelDoFireDamage()) {
                    
                    event.getEntityLiving().setFire(this.configuration.getFlintAndSteelFireDamage());
                    heldItem.attemptDamageItem(1, sourceLiving.getRNG());
                }
            }
        }
    }
    
    private boolean tryPercentage (double chance) {
        
        return Math.random() < chance;
    }
}