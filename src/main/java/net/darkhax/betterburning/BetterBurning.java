package net.darkhax.betterburning;

import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.AbstractSkeletonEntity;
import net.minecraft.entity.monster.ZombieEntity;
import net.minecraft.entity.player.ServerPlayerEntity;
import net.minecraft.entity.projectile.ArrowEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.potion.Effects;
import net.minecraft.util.Hand;
import net.minecraft.util.math.BlockPos;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderBlockOverlayEvent;
import net.minecraftforge.client.event.RenderBlockOverlayEvent.OverlayType;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinWorldEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent.LivingUpdateEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.loading.FMLEnvironment;

@Mod("betterburning")
public class BetterBurning {
    
    private final Configuration configuration = new Configuration();
    
    public BetterBurning() {
        
        ModLoadingContext.get().registerConfig(Type.COMMON, this.configuration.getSpec());
        
        MinecraftForge.EVENT_BUS.addListener(this::onLivingDeath);
        MinecraftForge.EVENT_BUS.addListener(this::onEntityJoinWorld);
        MinecraftForge.EVENT_BUS.addListener(this::onLivingTick);
        MinecraftForge.EVENT_BUS.addListener(this::onLivingAttack);
        
        if (FMLEnvironment.dist == Dist.CLIENT) {
            
            MinecraftForge.EVENT_BUS.addListener(this::onBlockOverlay);
        }
    }
    
    private void onLivingDeath (LivingDeathEvent event) {
        
        // Fixes some edge cases where fire damage sources won't cause mobs to drop
        // their cooked items.
        if (event.getSource().isFireDamage() && this.configuration.shouldDamageSourceCauseFire() && !event.getEntityLiving().isBurning() && !event.getEntity().world.isRemote) {
            
            event.getEntityLiving().setFire(1);
        }
    }
    
    private void onEntityJoinWorld (EntityJoinWorldEvent event) {
        
        // Allows skeletons to shoot flaming arrows when they are on fire.
        if (this.configuration.shouldSkeletonsShootFireArrows() && event.getEntity() instanceof ArrowEntity && !event.getEntity().world.isRemote) {
            
            final ArrowEntity arrowEntity = (ArrowEntity) event.getEntity();
            final Entity shooter = arrowEntity.func_234616_v_();
            
            if (shooter instanceof AbstractSkeletonEntity && shooter.isBurning() && shooter.isAlive() && this.tryPercentage(this.configuration.getSkeletonFlameArrowChance())) {
                
                arrowEntity.setFire(shooter.getFireTimer());
            }
        }
    }
    
    private void onLivingTick (LivingUpdateEvent event) {
        
        // If entity has fire resistance it will extinguish them right away when on
        // fire.
        if (this.configuration.shouldFireResExtinguish() && !event.getEntityLiving().world.isRemote && event.getEntityLiving().isBurning() && event.getEntityLiving().isPotionActive(Effects.FIRE_RESISTANCE)) {
            
            event.getEntityLiving().extinguish();
        }
    }
    
    private void onLivingAttack (LivingAttackEvent event) {
        
        if (this.configuration.shouldFireDamageSpread() && !event.getEntity().world.isRemote) {
            
            final Entity sourceEntity = event.getSource().getTrueSource();
            
            if (sourceEntity instanceof LivingEntity) {
                
                final LivingEntity sourceLiving = (LivingEntity) sourceEntity;
                final ItemStack heldItem = sourceLiving.getHeldItemMainhand();
                
                // Allows fire damage to spread from entity to entity
                if (!(sourceLiving instanceof ZombieEntity) && heldItem.isEmpty() && sourceLiving.isBurning() && this.tryPercentage(this.configuration.getFireDamageSpreadChance())) {
                    
                    final float damage = Math.max(1, event.getEntityLiving().world.getDifficultyForLocation(new BlockPos(event.getEntity().getPositionVec())).getAdditionalDifficulty());
                    event.getEntityLiving().setFire(2 * (int) damage);
                }
                
                // Allows flint and steel to do fire damage to mobs
                else if (heldItem.getItem() == Items.FLINT_AND_STEEL && this.configuration.shouldFlintAndSteelDoFireDamage()) {
                    
                    event.getEntityLiving().setFire(this.configuration.getFlintAndSteelFireDamage());
                    final ServerPlayerEntity player = sourceLiving instanceof ServerPlayerEntity ? (ServerPlayerEntity) sourceLiving : null;
                    
                    if (player == null || !player.isCreative()) {
                        
                        if(heldItem.attemptDamageItem(1, sourceLiving.getRNG(), player)) {
                            player.sendBreakAnimation(Hand.MAIN_HAND);
                            player.setHeldItem(Hand.MAIN_HAND, ItemStack.EMPTY);
                        }
                    }
                }
            }
        }
    }
    
    private void onBlockOverlay (RenderBlockOverlayEvent event) {
        
        if (event.getOverlayType() == OverlayType.FIRE && this.configuration.hideFireOverlay() && (event.getPlayer().isImmuneToFire() || event.getPlayer().isPotionActive(Effects.FIRE_RESISTANCE))) {
            
            event.setCanceled(true);
        }
    }
    
    private boolean tryPercentage (double chance) {
        
        return Math.random() < chance;
    }
}
