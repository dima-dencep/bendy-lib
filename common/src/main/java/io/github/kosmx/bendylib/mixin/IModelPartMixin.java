package io.github.kosmx.bendylib.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import io.github.kosmx.bendylib.ModelPartAccessor;
import io.github.kosmx.bendylib.MutableCuboid;
import io.github.kosmx.bendylib.impl.accessors.CuboidSideAccessor;
import io.github.kosmx.bendylib.impl.accessors.IModelPartAccessor;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import org.spongepowered.asm.mixin.*;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.Iterator;
import java.util.List;
import java.util.Map;

@Mixin(ModelPart.class)
public abstract class IModelPartMixin implements IModelPartAccessor {

    @Shadow @Final private Map<String, ModelPart> children;

    @Shadow @Final private List<ModelPart.Cuboid> cuboids;

    @Unique
    private boolean hasMutatedCuboid = false;
    /**
     * VanillaDraw won't cause slowdown in vanilla and will fix many issues.
     * If needed, use {@link IModelPartAccessor#setWorkaround(ModelPartAccessor.Workaround)} to set the workaround function
     * {@link ModelPartAccessor.Workaround#None} to do nothing about it. It will work in Vanilla, but not with Sodium/OF
     */
    private ModelPartAccessor.Workaround workaround = ModelPartAccessor.Workaround.VanillaDraw;

    @Override
    public List<ModelPart.Cuboid> getCuboids() {
        hasMutatedCuboid = true;
        return cuboids;
    }

    @Override
    public Map<String, ModelPart> getChildren() {
        return children;
    }

    @Inject(method = "copyTransform", at = @At("RETURN"))
    private void copyTransformExtended(ModelPart part, CallbackInfo ci){
        if(((IModelPartAccessor)part).getCuboids() == null || cuboids == null) return; // Not copying state
        Iterator<ModelPart.Cuboid> iterator0 = ((IModelPartAccessor)part).getCuboids().iterator();
        Iterator<ModelPart.Cuboid> iterator1 = cuboids.iterator();

        while (iterator0.hasNext() && iterator1.hasNext()){
            MutableCuboid cuboid1 = (MutableCuboid) iterator1.next();
            MutableCuboid cuboid0 = (MutableCuboid) iterator0.next();
            cuboid1.copyStateFrom(cuboid0);
        }

    }

    @WrapOperation(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;III)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;renderCuboids(Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/VertexConsumer;III)V"), require = 0) //It might not find anything if OF already broke the game
    private void redirectRenderCuboids(ModelPart modelPart, MatrixStack.Entry entry, VertexConsumer vertexConsumer, int light, int overlay, int color, Operation<Void> original){
        redirectedFunction(modelPart, entry, vertexConsumer, light, overlay, color, original);
    }

    /* // check what they do here
    @Dynamic("render function is replaced with this by Optifine")
    @Redirect(method = "render(Lnet/minecraft/client/util/math/MatrixStack;Lnet/minecraft/client/render/VertexConsumer;IIFFFFZ)V", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/model/ModelPart;renderCuboids(Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/minecraft/client/render/VertexConsumer;IIFFFF)V"), require = 0)
    private void redirectOF(ModelPart modelPart, MatrixStack.Entry entry, VertexConsumer vertexConsumer, int light, int overlay, float red, float green, float blue, float alpha) {
        redirectedFunction(modelPart, entry, vertexConsumer, light, overlay, red, green, blue, alpha);
    }*/

    @Unique
    private void redirectedFunction(ModelPart modelPart, MatrixStack.Entry entry, VertexConsumer vertexConsumer, int light, int overlay, int color, Operation<Void> original) {
        if(workaround == ModelPartAccessor.Workaround.ExportQuads){
            for(ModelPart.Cuboid cuboid:cuboids){
                ((CuboidSideAccessor)cuboid).doSideSwapping(); //:D
            }
            original.call(modelPart, entry, vertexConsumer, light, overlay, color);
            for(ModelPart.Cuboid cuboid:cuboids){
                ((CuboidSideAccessor)cuboid).resetSides(); //:D
            }
        }
        else if(workaround == ModelPartAccessor.Workaround.VanillaDraw){
            if(!hasMutatedCuboid || cuboids.size() == 1 && ((MutableCuboid)cuboids.get(0)).getActiveMutator() == null){
                original.call(modelPart, entry, vertexConsumer, light, overlay, color);
            }
            else {
                for(ModelPart.Cuboid cuboid:cuboids){
                    cuboid.renderCuboid(entry, vertexConsumer, light, overlay, color);
                }
            }
        }
        else {
            original.call(modelPart, entry, vertexConsumer, light, overlay, color);
        }
    }

    @Override
    public void setWorkaround(ModelPartAccessor.Workaround workaround) {
        this.workaround = workaround;
    }
}
