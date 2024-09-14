package cogi234.rebalance.mixin;

import net.minecraft.item.Items;
import net.minecraft.item.ToolMaterial;
import net.minecraft.item.ToolMaterials;
import net.minecraft.recipe.Ingredient;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.function.Supplier;

@Mixin(ToolMaterials.class)
public abstract class ToolMaterialsMixin implements ToolMaterial {

    @Shadow
    @Final
    private Supplier<Ingredient> repairIngredient;

    //This should replace netherite with diamond when minecraft asks for the repair ingredient
    @Override
    public Ingredient getRepairIngredient() {
        var baseRepairIngredient = (Ingredient)this.repairIngredient.get();
        if (baseRepairIngredient.equals(Ingredient.ofItems(Items.NETHERITE_INGOT)))
            return Ingredient.ofItems(Items.DIAMOND);
        return baseRepairIngredient;
    }
}
