package cogi234.rebalance;

import cogi234.rebalance.util.*;
import cogi234.rebalance.util.Cogi234sRebalanceConfig;
import com.feintha.regedit.RegistryEditEvent;
import net.fabricmc.api.ModInitializer;
import net.minecraft.block.Blocks;
import net.minecraft.item.ArmorMaterials;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Cogi234sRebalance implements ModInitializer {
	public static final String MOD_ID = "cogi234rebalance";
	// This logger is used to write text to the console and the log file.
	// It is considered best practice to use your mod id as the logger's name.
	// That way, it's clear which mod wrote info, warnings, and errors.
    public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	// CONFIG
	public static final Cogi234sRebalanceConfig CONFIG = Cogi234sRebalanceConfig.createAndLoad();

	@Override
	public void onInitialize() {
		// This code runs as soon as Minecraft is in a mod-load-ready state.
		// However, some things (like resources) may still be uninitialized.
		// Proceed with mild caution.
		ModLootTableModifiers.modifyLootTables();

		

		 //This is for Regedit, which is not out for 1.21 yet
		RegistryEditEvent.EVENT.register(manipulator -> {
			//This should replace the vanilla packed ice with our own custom packed ice
			manipulator.Redirect(Registries.BLOCK, Blocks.PACKED_ICE, ModBlocks.PACKED_ICE);
			manipulator.Redirect(Registries.ITEM, Items.NETHERITE_AXE, ModItems.NETHERITE_AXE);
			manipulator.Redirect(Registries.ITEM, Items.NETHERITE_HOE, ModItems.NETHERITE_HOE);
			manipulator.Redirect(Registries.ITEM, Items.NETHERITE_PICKAXE, ModItems.NETHERITE_PICKAXE);
			manipulator.Redirect(Registries.ITEM, Items.NETHERITE_SHOVEL, ModItems.NETHERITE_SHOVEL);
			manipulator.Redirect(Registries.ITEM, Items.NETHERITE_SWORD, ModItems.NETHERITE_SWORD);
			manipulator.Redirect(Registries.ITEM, Items.NETHERITE_HELMET, ModItems.NETHERITE_HELMET);
			manipulator.Redirect(Registries.ITEM, Items.NETHERITE_CHESTPLATE, ModItems.NETHERITE_CHESTPLATE);
			manipulator.Redirect(Registries.ITEM, Items.NETHERITE_LEGGINGS, ModItems.NETHERITE_LEGGINGS);
			manipulator.Redirect(Registries.ITEM, Items.NETHERITE_BOOTS, ModItems.NETHERITE_BOOTS);
		});
	}
}