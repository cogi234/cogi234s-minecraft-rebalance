package cogi234.rebalance.util;

import cogi234.rebalance.Cogi234sRebalance;
import io.wispforest.owo.config.annotation.Config;
import io.wispforest.owo.config.annotation.Modmenu;
import io.wispforest.owo.config.annotation.RangeConstraint;
import io.wispforest.owo.config.annotation.SectionHeader;

@Modmenu(modId = Cogi234sRebalance.MOD_ID)
@Config(name = "cogi234s-rebalance", wrapperName = "Cogi234sRebalanceConfig")
public class ConfigModel {
    @SectionHeader("Anvil")
    //Do repairs cost more and more when done on the same object
    public boolean repairsAccumulateCost = false;
    //How many diamonds to fully repair a diamond sword
    @RangeConstraint(min = 1, max = 100)
    public int materialCountToFullyRepair = 1;
    //Can you level up enchants by combining 2 of the same level
    public boolean enchantsCanLevelUp = false;

    @SectionHeader("Sleep")
    public boolean sleepCanSkipNight = false;
    public boolean bedCanSetSpawn = false;

    @SectionHeader("Minecart")
    @RangeConstraint(min = 8, max = 100)
    public int minecartMaxSpeed = 30;

    @SectionHeader("Enchanting")
    @RangeConstraint(min = 1, max = 3)
    public int enchantingCost = 3;
}
