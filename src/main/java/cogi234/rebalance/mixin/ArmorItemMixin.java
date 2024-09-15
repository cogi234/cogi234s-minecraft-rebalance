package cogi234.rebalance.mixin;

import net.minecraft.item.*;
import net.minecraft.recipe.Ingredient;
import net.minecraft.registry.entry.RegistryEntry;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ArmorItem.class)
public abstract class ArmorItemMixin extends Item implements Equipment {

    @Shadow
    @Final
    protected RegistryEntry<ArmorMaterial> material;

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        var repairIngredient = (Ingredient)this.material.value().repairIngredient().get();

        //If the repair ingredient is netherite, we try diamond too
        if (repairIngredient.equals(Ingredient.ofItems(Items.NETHERITE_INGOT)) && Ingredient.ofItems(Items.DIAMOND).test(ingredient))
            return true;

        return repairIngredient.test(ingredient) || super.canRepair(stack, ingredient);
    }

    public ArmorItemMixin(Settings settings) {
        super(settings);
    }
}
