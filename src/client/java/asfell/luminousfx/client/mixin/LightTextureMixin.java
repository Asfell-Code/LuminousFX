package asfell.luminousfx.client.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import net.minecraft.client.Minecraft;
import net.minecraft.client.OptionInstance;
import net.minecraft.client.renderer.LightTexture;
import asfell.luminousfx.client.NightVisionModule;

@Mixin(LightTexture.class)
public abstract class LightTextureMixin {
    @Shadow
    private Minecraft minecraft;

    @Shadow
    private boolean updateLightTexture;

    @Inject(method = "tick()V", at = @At("HEAD"))
    private void luminousfx$forceLightmapRefresh(CallbackInfo info) {
        this.updateLightTexture = true;
    }

    @Redirect(method = "updateLightTexture(F)V", at = @At(value = "INVOKE",
            target = "Lnet/minecraft/client/OptionInstance;get()Ljava/lang/Object;", ordinal = 1))
    private Object luminousfx$useCustomGammaForLightmap(OptionInstance<?> option) {
        if (option == this.minecraft.options.gamma() && NightVisionModule.isEnabled()) {
            return NightVisionModule.getConfiguredGamma();
        }
        return option.get();
    }
}