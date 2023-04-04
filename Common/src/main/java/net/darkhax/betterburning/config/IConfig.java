package net.darkhax.betterburning.config;

interface IConfig {

    boolean shouldFireResExtinguish();

    boolean shouldDamageSourceCauseFire();

    boolean shouldFireDamageSpread();

    double getFireDamageSpreadChance();

    boolean shouldSkeletonsShootFireArrows();

    double getSkeletonFlameArrowChance();

    boolean shouldFlintAndSteelDoFireDamage();

    int getFlintAndSteelFireDamage();

    boolean hideFireOverlay();

    int getFirePunchBurnTime();

    int getSoulFirePunchBurnTime();

    boolean canPunchOutFlames();

    boolean canExtinguishWithBottledWater();
}