package cogi234.rebalance.mixin;

import net.minecraft.item.*;
import net.minecraft.recipe.Ingredient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(ToolItem.class)
public abstract class ToolItemMixin extends Item {

    @Shadow
    @Final
    private ToolMaterial material;

    @Override
    public boolean canRepair(ItemStack stack, ItemStack ingredient) {
        var repairIngredient = this.material.getRepairIngredient();

        //If the repair ingredient is netherite, we try diamond too
        if (repairIngredient.equals(Ingredient.ofItems(Items.NETHERITE_INGOT)) && Ingredient.ofItems(Items.DIAMOND).test(ingredient))
            return true;

        return repairIngredient.test(ingredient) || super.canRepair(stack, ingredient);
    }


    public ToolItemMixin(Settings settings) {
        super(settings);
    }
}
