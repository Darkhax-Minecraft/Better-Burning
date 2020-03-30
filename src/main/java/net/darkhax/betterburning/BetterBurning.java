package net.darkhax.betterburning;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySkeleton;
import net.minecraft.entity.monster.EntityZombie;
import net.minecraft.entity.projectile.EntityArrow;
import net.minecraft.init.Items;
import net.minecraft.item.ItemStack;
import net.minecraft.potion.Potion;
import net.minecraft.util.BlockPos;
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
        if (event.source.isFireDamage() && this.configuration.shouldDamageSourceCauseFire() && !event.entityLiving.isBurning() && !event.entity.worldObj.isRemote) {
            
            event.entityLiving.setFire(1);
        }
    }
    
    @SubscribeEvent
    public void onEntityJoinWorld (EntityJoinWorldEvent event) {
        
        // Allows skeletons to shoot flaming arrows when they are on fire.
        if (this.configuration.shouldSkeletonsShootFireArrows() && event.entity instanceof EntityArrow && !event.entity.worldObj.isRemote) {
            
            final EntityArrow arrowEntity = (EntityArrow) event.entity;
            final Entity shooter = arrowEntity.shootingEntity;
            
            if (shooter instanceof EntitySkeleton && shooter.isBurning() && !shooter.isDead && this.tryPercentage(this.configuration.getSkeletonFlameArrowChance())) {
                
                arrowEntity.setFire(100);
            }
        }
    }
    
    @SubscribeEvent
    public void onLivingTick (LivingUpdateEvent event) {
        
        // If entity has fire resistance it will extinguish them right away when on fire.
        if (this.configuration.shouldFireResExtinguish() && !event.entityLiving.worldObj.isRemote && event.entityLiving.isBurning() && event.entityLiving.isPotionActive(Potion.fireResistance)) {
            
            event.entityLiving.extinguish();
        }
    }
    
    @SubscribeEvent
    public void onLivingAttack (LivingAttackEvent event) {
        
        if (this.configuration.shouldFireDamageSpread() && !event.entity.worldObj.isRemote) {
            
            final Entity sourceEntity = event.source.getEntity();
            
            if (sourceEntity instanceof EntityLivingBase) {
                
                final EntityLivingBase sourceLiving = (EntityLivingBase) sourceEntity;
                final ItemStack heldItem = sourceLiving.getHeldItem();
                
                // Allows fire damage to spread from entity to entity
                if (!(sourceLiving instanceof EntityZombie) && heldItem == null && sourceLiving.isBurning() && this.tryPercentage(this.configuration.getFireDamageSpreadChance())) {
                    
                    final float damage = Math.max(1, event.entityLiving.worldObj.getDifficultyForLocation(new BlockPos(event.entity)).getAdditionalDifficulty());
                    event.entityLiving.setFire(2 * (int) damage);
                }
                
                // Allows flint and steel to do fire damage to mobs
                else if (heldItem != null && heldItem.getItem() == Items.flint_and_steel && this.configuration.shouldFlintAndSteelDoFireDamage()) {
                    
                    event.entityLiving.setFire(this.configuration.getFlintAndSteelFireDamage());
                    heldItem.attemptDamageItem(1, sourceLiving.getRNG());
                }
            }
        }
    }
    
    private boolean tryPercentage (double chance) {
        
        return Math.random() < chance;
    }
}