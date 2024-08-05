package cogi234.rebalance.mixin;

import cogi234.rebalance.Cogi234sRebalance;
import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ItemEnchantmentsComponent;
import net.minecraft.enchantment.Enchantment;
import net.minecraft.enchantment.EnchantmentHelper;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.*;
import net.minecraft.text.Text;
import net.minecraft.util.StringHelper;
import net.minecraft.util.math.MathHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyVariable;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(value = AnvilScreenHandler.class)
public abstract class AnvilMixin extends ForgingScreenHandler{

    public AnvilMixin(@Nullable ScreenHandlerType<?> type, int syncId, PlayerInventory playerInventory, ScreenHandlerContext context) {
        super(type, syncId, playerInventory, context);
    }

    //Ignore accumulated repair costs when calculating the total experience cost
    @ModifyVariable(method = "updateResult()V", at = @At(value = "STORE", ordinal = 4), ordinal = 2)
    private int ignoreAccumulatedRepairCosts(int t, @Local(ordinal = 0) int i) {
        if (Cogi234sRebalance.repairsAccumulateCost)
            return t;
        else
            return i;
    }

    //Stop accumulating repair costs
    @Inject(method = "updateResult()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/item/ItemStack;set(Lnet/minecraft/component/ComponentType;Ljava/lang/Object;)Ljava/lang/Object;", shift = At.Shift.AFTER, ordinal = 1))
    private void stopAccumulatingRepairCosts(CallbackInfo ci, @Local(ordinal = 1) ItemStack itemStack2){
        if (!Cogi234sRebalance.repairsAccumulateCost)
            itemStack2.remove(DataComponentTypes.REPAIR_COST);
    }

    //Override the amount of repairs when the function calls Math.min to calculate it.
    @Redirect(method = "updateResult()V", at = @At(value = "INVOKE", target = "Ljava/lang/Math;min(II)I"))
    private int modifyRepairAmount(int a, int b, @Local(ordinal = 1) ItemStack itemStack2){
        if (a == itemStack2.getDamage() && b <= itemStack2.getMaxDamage())
            return Math.min(itemStack2.getDamage(), itemStack2.getMaxDamage() / Cogi234sRebalance.materialCountToFullyRepair);
        else
            return Math.min(a, b);
    }

    //Stop enchants from levelling up when you combine two with the same level
    @ModifyVariable(method = "updateResult()V", at = @At(value = "STORE", ordinal = 3), ordinal = 2)
    private int dontLevelUpEnchants1(int q, @Local ItemEnchantmentsComponent.Builder builder, @Local RegistryEntry<Enchantment> registryEntry){
        if (Cogi234sRebalance.enchantsCanLevelUp)
            return builder.getLevel(registryEntry);
        else
            return 0;
    }
    @ModifyVariable(method = "updateResult()V", at = @At(value = "STORE", ordinal = 3), ordinal = 3)
    private int dontLevelUpEnchants2(int r, @Local ItemEnchantmentsComponent.Builder builder, @Local RegistryEntry<Enchantment> registryEntry, @Local Object2IntMap.Entry<RegistryEntry<Enchantment>> entry){
        if (Cogi234sRebalance.enchantsCanLevelUp)
            return entry.getIntValue();
        else
            return Math.max(entry.getIntValue(), builder.getLevel(registryEntry));
    }
}
