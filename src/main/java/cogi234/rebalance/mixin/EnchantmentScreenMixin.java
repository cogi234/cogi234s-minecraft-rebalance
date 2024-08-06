package cogi234.rebalance.mixin;

import cogi234.rebalance.Cogi234sRebalance;
import net.minecraft.client.gui.DrawContext;
import net.minecraft.client.gui.screen.ingame.EnchantmentScreen;
import net.minecraft.client.gui.screen.ingame.HandledScreen;
import net.minecraft.screen.EnchantmentScreenHandler;
import net.minecraft.util.Identifier;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EnchantmentScreen.class)
public abstract class EnchantmentScreenMixin {
    @Shadow @Final
    private static Identifier[] LEVEL_TEXTURES;
    @Shadow @Final
    private static Identifier[] LEVEL_DISABLED_TEXTURES;

    @Redirect(method = "drawBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V", ordinal = 2))
    private void redirectedDrawGuiTexture1(DrawContext instance, Identifier texture, int x, int y, int width, int height){
        instance.drawGuiTexture(LEVEL_DISABLED_TEXTURES[Cogi234sRebalance.CONFIG.enchantingCost() - 1], x, y, width, height);
    }

    @Redirect(method = "drawBackground", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/DrawContext;drawGuiTexture(Lnet/minecraft/util/Identifier;IIII)V", ordinal = 5))
    private void redirectedDrawGuiTexture2(DrawContext instance, Identifier texture, int x, int y, int width, int height){
        instance.drawGuiTexture(LEVEL_TEXTURES[Cogi234sRebalance.CONFIG.enchantingCost() - 1], x, y, width, height);
    }
}
