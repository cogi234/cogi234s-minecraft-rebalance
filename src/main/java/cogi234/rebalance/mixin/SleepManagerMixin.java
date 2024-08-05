package cogi234.rebalance.mixin;

import cogi234.rebalance.Cogi234sRebalance;
import net.minecraft.server.world.SleepManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SleepManager.class)
public class SleepManagerMixin {

    //Control if sleeping can skip the night
    @Inject(method = "canSkipNight", at = @At(value = "HEAD"), cancellable = true)
    private void canSkipNight(int percentage, CallbackInfoReturnable<Boolean> cir){
        if (!Cogi234sRebalance.CONFIG.sleepCanSkipNight()){
            cir.setReturnValue(false);
            cir.cancel();
        }
    }
}
