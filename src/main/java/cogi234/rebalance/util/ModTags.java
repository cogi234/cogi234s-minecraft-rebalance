package cogi234.rebalance.util;

import cogi234.rebalance.Cogi234sRebalance;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.tag.EnchantmentTags;
import net.minecraft.registry.tag.TagKey;
import net.minecraft.util.Identifier;

public class ModTags {
    public static class Enchantments {
        public static final TagKey<Enchantment> EXCLUSIVE_SET_DURABILITY =
                createTag("exclusive_set/durability");


        private static TagKey<Enchantment> createTag(String name) {
            return TagKey.of(RegistryKeys.ENCHANTMENT, Identifier.of(Cogi234sRebalance.MOD_ID, name));
        }
    }
}
