package net.darkhax.betterburning;

import java.io.File;

import net.minecraftforge.common.config.Configuration;

public class Config {
    
    private final Configuration spec;
    
    private final boolean fireResExtinguish;
    
    private final boolean fireFromDamagesource;
    
    private final boolean fireDamageSpreads;
    private final double fireDamageSpreadChance;
    
    private final boolean flameArrowSkeletons;
    private final double flameArrowChance;
    
    private final boolean flintAndSteelDealsFireDamage;
    private final int flintAndSteelFireDamage;
    
    public Config() {
        
    	this.spec = new Configuration(new File("config/better-burning.cfg"));
    	
    	this.fireResExtinguish = this.spec.getBoolean("fireResistanceExtinguish", "general", true, "Should having fire resistance stop things from being on fire visually?");
        
        this.fireFromDamagesource = this.spec.getBoolean("fireFromDamagesource", "general", true, "Fixes some fire related damage sources not causing mobs to drop cooked items?");
        
        this.fireDamageSpreads = this.spec.getBoolean("fireDamageSpreads", "mobspread", true, "Should fire damage spread between mobs when they hurt eachother? Zombies already do this and won't be changed.");
        this.fireDamageSpreadChance = this.spec.getFloat("fireDamageSpreadChance", "mobspread", 0.3f, 0f, 1f, "What is the % chance that fire damage will spread between mobs?");
        
        this.flameArrowSkeletons = this.spec.getBoolean("flameArrowSkeletons", "arrows", true, "Should skeletons shoot flaming arrows while on fire?");
        this.flameArrowChance = this.spec.getFloat("flameArrowSkeletonChance", "arrows", 0.7f, 0f, 1f, "What is the % chance skeletons will shoot flaming arrows while on fire?");
        
        this.flintAndSteelDealsFireDamage = this.spec.getBoolean("ShouldFlintAndSteelDoFireDamage", "flintandsteel", true, "Should flint and steel deal fire damage when used as a weapon?");       
        this.flintAndSteelFireDamage = this.spec.getInt("flintAndSteelFireDamage", "flintandsteel", 3, 0, 4096, "How much fire damage should flint and steel do?");
        
        if (this.spec.hasChanged()) {
        	
        	this.spec.save();
        }
    }
    
    public boolean shouldFireResExtinguish () {
        
        return this.fireResExtinguish;
    }
    
    public boolean shouldDamageSourceCauseFire () {
        
        return this.fireFromDamagesource;
    }
    
    public boolean shouldFireDamageSpread () {
        
        return this.fireDamageSpreads;
    }
    
    public double getFireDamageSpreadChance () {
        
        return this.fireDamageSpreadChance;
    }
    
    public boolean shouldSkeletonsShootFireArrows () {
        
        return this.flameArrowSkeletons;
    }
    
    public double getSkeletonFlameArrowChance () {
        
        return this.flameArrowChance;
    }
    
    public boolean shouldFlintAndSteelDoFireDamage () {
        
        return this.flintAndSteelDealsFireDamage;
    }
    
    public int getFlintAndSteelFireDamage () {
        
        return this.flintAndSteelFireDamage;
    }
}