package cogi234.rebalance.util;

import net.minecraft.block.Block;
import net.minecraft.item.*;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.registry.RegistryKey;
import net.minecraft.util.Identifier;

import java.util.function.UnaryOperator;

public class ModItems {

    //TOOLS
    public static final Item NETHERITE_SWORD = register(
            "netherite_sword",
            new SwordItem(
                    ToolMaterials.NETHERITE, new Item.Settings().fireproof().attributeModifiers(SwordItem.createAttributeModifiers(ModToolMaterials.NETHERITE, 3, -2.4F))
            )
    );
    public static final Item NETHERITE_SHOVEL = register(
            "netherite_shovel",
            new ShovelItem(
                    ToolMaterials.NETHERITE, new Item.Settings().fireproof().attributeModifiers(ShovelItem.createAttributeModifiers(ModToolMaterials.NETHERITE, 1.5F, -3.0F))
            )
    );
    public static final Item NETHERITE_PICKAXE = register(
            "netherite_pickaxe",
            new PickaxeItem(
                    ToolMaterials.NETHERITE, new Item.Settings().fireproof().attributeModifiers(PickaxeItem.createAttributeModifiers(ModToolMaterials.NETHERITE, 1.0F, -2.8F))
            )
    );
    public static final Item NETHERITE_AXE = register(
            "netherite_axe",
            new AxeItem(
                    ToolMaterials.NETHERITE, new Item.Settings().fireproof().attributeModifiers(AxeItem.createAttributeModifiers(ModToolMaterials.NETHERITE, 5.0F, -3.0F))
            )
    );
    public static final Item NETHERITE_HOE = register(
            "netherite_hoe",
            new HoeItem(
                    ToolMaterials.NETHERITE, new Item.Settings().fireproof().attributeModifiers(HoeItem.createAttributeModifiers(ModToolMaterials.NETHERITE, -4.0F, 0.0F))
            )
    );

    //ARMOR
    public static final Item NETHERITE_HELMET = register(
            "netherite_helmet",
            new ArmorItem(ModMaterials.ARMOR_NETHERITE, ArmorItem.Type.HELMET, new Item.Settings().fireproof().maxDamage(ArmorItem.Type.HELMET.getMaxDamage(37)))
    );
    public static final Item NETHERITE_CHESTPLATE = register(
            "netherite_chestplate",
            new ArmorItem(ModMaterials.ARMOR_NETHERITE, ArmorItem.Type.CHESTPLATE, new Item.Settings().fireproof().maxDamage(ArmorItem.Type.CHESTPLATE.getMaxDamage(37)))
    );
    public static final Item NETHERITE_LEGGINGS = register(
            "netherite_leggings",
            new ArmorItem(ModMaterials.ARMOR_NETHERITE, ArmorItem.Type.LEGGINGS, new Item.Settings().fireproof().maxDamage(ArmorItem.Type.LEGGINGS.getMaxDamage(37)))
    );
    public static final Item NETHERITE_BOOTS = register(
            "netherite_boots",
            new ArmorItem(ModMaterials.ARMOR_NETHERITE, ArmorItem.Type.BOOTS, new Item.Settings().fireproof().maxDamage(ArmorItem.Type.BOOTS.getMaxDamage(37)))
    );


    public static Item register(Block block) {
        return register(new BlockItem(block, new Item.Settings()));
    }

    public static Item register(Block block, UnaryOperator<Item.Settings> settingsOperator) {
        return register(new BlockItem(block, (Item.Settings)settingsOperator.apply(new Item.Settings())));
    }

    public static Item register(Block block, Block... blocks) {
        BlockItem blockItem = new BlockItem(block, new Item.Settings());

        for (Block block2 : blocks) {
            Item.BLOCK_ITEMS.put(block2, blockItem);
        }

        return register(blockItem);
    }

    public static Item register(BlockItem item) {
        return register(item.getBlock(), item);
    }

    public static Item register(Block block, Item item) {
        return register(Registries.BLOCK.getId(block), item);
    }

    public static Item register(String id, Item item) {
        return register(Identifier.ofVanilla(id), item);
    }

    public static Item register(Identifier id, Item item) {
        return register(RegistryKey.of(Registries.ITEM.getKey(), id), item);
    }

    public static Item register(RegistryKey<Item> key, Item item) {
        if (item instanceof BlockItem) {
            ((BlockItem)item).appendBlocks(Item.BLOCK_ITEMS, item);
        }

        return Registry.register(Registries.ITEM, key, item);
    }
}
