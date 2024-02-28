package io.github.kosmx.bendylib.mixin.sodium;

import io.github.kosmx.bendylib.compat.sodium.ModelCuboidAccessor;
import me.jellysquid.mods.sodium.client.render.immediate.model.ModelCuboid;
import net.minecraft.client.model.ModelPart;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ModelPart.Cuboid.class)
public class CuboidMixin {
    @Inject(
            method = "sodium$copy",
            at = @At(
                    value = "RETURN"
            )
    )
    public void sodium$copy(CallbackInfoReturnable<ModelCuboid> cir) {
        ((ModelCuboidAccessor) cir.getReturnValue())
                .setOriginal((ModelPart.Cuboid) (Object) this);
    }
}
