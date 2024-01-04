package net.darkhax.betterburning;

import net.darkhax.betterburning.config.ConfigForge;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.monster.AbstractSkeleton;
import net.minecraft.world.entity.monster.Zombie;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.alchemy.Potion;
import net.minecraft.world.item.alchemy.PotionUtils;
import net.minecraft.world.item.alchemy.Potions;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.client.event.RenderBlockScreenEffectEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.EntityJoinLevelEvent;
import net.minecraftforge.event.entity.living.LivingAttackEvent;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.event.entity.player.PlayerInteractEvent;
import net.minecraftforge.event.level.BlockEvent;
import net.minecraftforge.fml.ModLoadingContext;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.config.ModConfig.Type;
import net.minecraftforge.fml.loading.FMLEnvironment;
import net.minecraftforge.items.ItemHandlerHelper;

@Mod(Constants.MOD_ID)
public class BetterBurningForge {

    private final ConfigForge configuration = new ConfigForge();

    public BetterBurningForge() {

        ModLoadingContext.get().registerConfig(Type.COMMON, this.configuration.getSpec());

        MinecraftForge.EVENT_BUS.addListener(this::onLivingDeath);
        MinecraftForge.EVENT_BUS.addListener(this::onEntityJoinWorld);
        MinecraftForge.EVENT_BUS.addListener(this::onLivingTick);
        MinecraftForge.EVENT_BUS.addListener(this::onLivingAttack);
        MinecraftForge.EVENT_BUS.addListener(this::onBlockBreak);
        MinecraftForge.EVENT_BUS.addListener(this::rightClickBlockWithItem);

        if (FMLEnvironment.dist == Dist.CLIENT) {

            MinecraftForge.EVENT_BUS.addListener(this::onBlockOverlay);
        }
    }

    private void rightClickBlockWithItem(PlayerInteractEvent.RightClickBlock event) {

        if (this.configuration.canExtinguishWithBottledWater() && event.getEntity() != null && event.getEntity().getClass() == ServerPlayer.class) {

            final BlockState state = event.getLevel().getBlockState(event.getPos());
            final Block block = state.getBlock();
            final ItemStack stack = event.getItemStack();
            final Potion potion = PotionUtils.getPotion(stack);

            if (block == Blocks.FIRE || block == Blocks.SOUL_FIRE) {

                if (stack.getItem() == Items.POTION && Potions.WATER == potion && !event.getLevel().isClientSide) {

                    event.getLevel().setBlockAndUpdate(event.getPos(), Blocks.AIR.defaultBlockState());
                    event.getLevel().levelEvent(null, 1009, event.getPos(), 0);
                    ItemHandlerHelper.giveItemToPlayer(event.getEntity(), new ItemStack(Items.GLASS_BOTTLE));
                    stack.shrink(1);
                }
            }
        }
    }

    private void onBlockBreak(BlockEvent.BreakEvent event) {

        if (event.getPlayer() != null && event.getPlayer().getClass() == ServerPlayer.class) {

            final Block block = event.getState().getBlock();

            // Players get burned when they try to punch out a fire block.
            final int burnTime = block == Blocks.FIRE ? this.configuration.getFirePunchBurnTime() : block == Blocks.SOUL_FIRE ? this.configuration.getSoulFirePunchBurnTime() : 0;

            if (burnTime > 0) {

                event.getPlayer().setSecondsOnFire(burnTime);
            }

            // Config to prevent punching out flames
            if (block instanceof BaseFireBlock && !this.configuration.canPunchOutFlames()) {

                event.setCanceled(true);
            }
        }
    }

    private void onLivingDeath(LivingDeathEvent event) {

        // Fixes some edge cases where fire damage sources won't cause mobs to drop
        // their cooked items.
        if (event.getSource().is(DamageTypeTags.IS_FIRE) && this.configuration.shouldDamageSourceCauseFire() && !event.getEntity().isOnFire() && !event.getEntity().level().isClientSide) {

            event.getEntity().setSecondsOnFire(1);
        }
    }

    private void onEntityJoinWorld(EntityJoinLevelEvent event) {

        // Allows skeletons to shoot flaming arrows when they are on fire.
        if (this.configuration.shouldSkeletonsShootFireArrows() && event.getEntity() instanceof Arrow arrow && !event.getEntity().level().isClientSide) {

            final Entity shooter = arrow.getOwner();

            if (shooter instanceof AbstractSkeleton && shooter.isOnFire() && shooter.isAlive() && this.tryPercentage(this.configuration.getSkeletonFlameArrowChance())) {

                arrow.setRemainingFireTicks(shooter.getRemainingFireTicks());
            }
        }
    }

    private void onLivingTick(LivingEvent.LivingTickEvent event) {

        // If entity has fire resistance it will extinguish them right away when on
        // fire.
        if (this.configuration.shouldFireResExtinguish() && !event.getEntity().level().isClientSide && event.getEntity().isOnFire() && event.getEntity().hasEffect(MobEffects.FIRE_RESISTANCE)) {

            event.getEntity().extinguishFire();
        }
    }

    private void onLivingAttack(LivingAttackEvent event) {

        if (this.configuration.shouldFireDamageSpread() && !event.getEntity().level().isClientSide) {

            final Entity sourceEntity = event.getSource().getEntity();

            if (sourceEntity instanceof LivingEntity sourceLiving) {

                final ItemStack heldItem = sourceLiving.getMainHandItem();

                // Allows fire damage to spread from entity to entity
                if (!(sourceLiving instanceof Zombie) && heldItem.isEmpty() && sourceLiving.isOnFire() && this.tryPercentage(this.configuration.getFireDamageSpreadChance())) {

                    final float damage = Math.max(1, event.getEntity().level().getCurrentDifficultyAt(event.getEntity().blockPosition()).getEffectiveDifficulty());
                    event.getEntity().setSecondsOnFire(2 * (int) damage);
                }

                // Allows flint and steel to do fire damage to mobs
                if (heldItem.getItem() == Items.FLINT_AND_STEEL && this.configuration.shouldFlintAndSteelDoFireDamage()) {

                    event.getEntity().setSecondsOnFire(this.configuration.getFlintAndSteelFireDamage());
                }
            }
        }
    }

    private void onBlockOverlay(RenderBlockScreenEffectEvent event) {

        if (event.getOverlayType() == RenderBlockScreenEffectEvent.OverlayType.FIRE && this.configuration.hideFireOverlay() && (event.getPlayer().fireImmune() || event.getPlayer().hasEffect(MobEffects.FIRE_RESISTANCE))) {

            event.setCanceled(true);
        }
    }

    private boolean tryPercentage(double chance) {

        return Math.random() < chance;
    }
}