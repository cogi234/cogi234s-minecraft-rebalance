package cogi234.rebalance;

import net.fabricmc.api.ModInitializer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cogi234sRebalance implements ModInitializer {
	public static final String MOD_ID = "cogi234rebalance";
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	//ANVIL
	//Do repairs cost more and more when done on the same object
	public static boolean repairsAccumulateCost = false;
	//How many diamonds to fully repair a diamond sword
	public static int materialCountToFullyRepair = 1;
	//Can you level up enchants by combining 2 of the same level
	public static boolean enchantsCanLevelUp = false;

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
	}
}