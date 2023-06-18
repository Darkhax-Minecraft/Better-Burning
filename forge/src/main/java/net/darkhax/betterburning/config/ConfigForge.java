package net.darkhax.betterburning.config;

import net.minecraftforge.common.ForgeConfigSpec;
import net.minecraftforge.common.ForgeConfigSpec.*;

public class ConfigForge implements IConfig {

    private final ForgeConfigSpec spec;

    private final BooleanValue fireResExtinguish;

    private final BooleanValue fireFromDamagesource;

    private final BooleanValue fireDamageSpreads;
    private final DoubleValue fireDamageSpreadChance;

    private final BooleanValue flameArrowSkeletons;
    private final ConfigValue<Double> flameArrowChance;

    private final BooleanValue flintAndSteelDealsFireDamage;
    private final IntValue flintAndSteelFireDamage;

    private final BooleanValue fireOverlay;

    private final IntValue fireHitBurnTime;
    private final IntValue soulfireHitBurnTime;

    private final BooleanValue punchOutFlames;

    private final BooleanValue extinguishWithBottle;

    public ConfigForge() {

        final ForgeConfigSpec.Builder builder = new ForgeConfigSpec.Builder();

        // General Configs
        builder.comment("General settings for the mod.");
        builder.push("general");

        builder.comment("Should having fire resistance stop things from being on fire visually?");
        this.fireResExtinguish = builder.define("fireResistanceExtinguish", true);

        builder.comment("Fixes some fire related damage sources not causing mobs to drop cooked items?");
        this.fireFromDamagesource = builder.define("fireFromDamagesource", true);

        builder.comment("Should fire damage spread between mobs when they hurt eachother? Zombies already do this and won't be changed.");
        this.fireDamageSpreads = builder.define("fireDamageSpreads", true);

        builder.comment("What is the % chance that fire damage will spread between mobs?");
        this.fireDamageSpreadChance = builder.defineInRange("fireDamageSpreadChance", 0.3d, 0d, 1d);

        builder.comment("Should skeletons shoot flaming arrows while on fire?");
        this.flameArrowSkeletons = builder.define("flameArrowSkeletons", true);

        builder.comment("What is the % chance skeletons will shoot flaming arrows while on fire?");
        this.flameArrowChance = builder.defineInRange("flameArrowSkeletonChance", 0.7d, 0d, 1d);

        builder.comment("Should flint and steel deal fire damage when used as a weapon?");
        this.flintAndSteelDealsFireDamage = builder.define("ShouldFlintAndSteelDoFireDamage", true);

        builder.comment("How much fire damage should flint and steel do?");
        this.flintAndSteelFireDamage = builder.defineInRange("flintAndSteelFireDamage", 3, 0, Integer.MAX_VALUE);

        builder.comment("Should the fire/burning HUD overlay be hidden if the player has fire immunity?");
        this.fireOverlay = builder.define("hideFireOverlayWhenImmune", true);

        builder.comment("How long should Fire burn players if they try to punch it out?");
        this.fireHitBurnTime = builder.defineInRange("fireHitBurnTime", 1, 0, Integer.MAX_VALUE);

        builder.comment("How long should Soulfire burn players if they try to punch it out?");
        this.soulfireHitBurnTime = builder.defineInRange("soulfireHitBurnTime", 2, 0, Integer.MAX_VALUE);

        builder.comment("Should players be able to put out fire blocks by punching them?");
        this.punchOutFlames = builder.define("punchOutFlames", true);

        builder.comment("Can players put out fire with bottled water?");
        this.extinguishWithBottle = builder.define("extinguishWithBottledWater", true);
        this.spec = builder.build();
    }

    public ForgeConfigSpec getSpec () {

        return this.spec;
    }

    public boolean shouldFireResExtinguish () {

        return this.fireResExtinguish.get();
    }

    public boolean shouldDamageSourceCauseFire () {

        return this.fireFromDamagesource.get();
    }

    public boolean shouldFireDamageSpread () {

        return this.fireDamageSpreads.get();
    }

    public double getFireDamageSpreadChance () {

        return this.fireDamageSpreadChance.get();
    }

    public boolean shouldSkeletonsShootFireArrows () {

        return this.flameArrowSkeletons.get();
    }

    public double getSkeletonFlameArrowChance () {

        return this.flameArrowChance.get();
    }

    public boolean shouldFlintAndSteelDoFireDamage () {

        return this.flintAndSteelDealsFireDamage.get();
    }

    public int getFlintAndSteelFireDamage () {

        return this.flintAndSteelFireDamage.get();
    }

    public boolean hideFireOverlay () {

        return this.fireOverlay.get();
    }

    public int getFirePunchBurnTime () {

        return this.fireHitBurnTime.get();
    }

    public int getSoulFirePunchBurnTime () {

        return this.soulfireHitBurnTime.get();
    }

    public boolean canPunchOutFlames () {

        return this.punchOutFlames.get();
    }

    public boolean canExtinguishWithBottledWater () {

        return this.extinguishWithBottle.get();
    }
}
