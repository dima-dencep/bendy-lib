package io.github.kosmx.bendylib.mixin.sodium;

import com.llamalad7.mixinextras.sugar.Local;
import io.github.kosmx.bendylib.ModelPartAccessor;
import io.github.kosmx.bendylib.compat.sodium.ModelCuboidAccessor;
import io.github.kosmx.bendylib.impl.accessors.CuboidSideAccessor;
import io.github.kosmx.bendylib.impl.accessors.IModelPartAccessor;
import me.jellysquid.mods.sodium.client.render.immediate.model.EntityRenderer;
import me.jellysquid.mods.sodium.client.render.immediate.model.ModelCuboid;
import net.caffeinemc.mods.sodium.api.util.ColorABGR;
import net.caffeinemc.mods.sodium.api.vertex.buffer.VertexBufferWriter;
import net.caffeinemc.mods.sodium.api.vertex.format.common.ModelVertex;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.render.VertexConsumer;
import net.minecraft.client.util.math.MatrixStack;
import org.apache.logging.log4j.LogManager;
import org.lwjgl.system.MemoryStack;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(EntityRenderer.class)
public abstract class EntityRendererMixin {
    @Shadow
    private static void prepareNormals(MatrixStack.Entry matrices) {
    }

    @Shadow
    private static void prepareVertices(MatrixStack.Entry matrices, ModelCuboid cuboid) {
    }

    @Shadow
    private static int emitQuads(ModelCuboid cuboid, int color, int overlay, int light) {
        return 0;
    }

    @Shadow @Final private static long SCRATCH_BUFFER;

    @Unique
    private static VertexConsumer getVertexConsumer = null;

    @Redirect(
            method = "render",
            at = @At(
                    value = "INVOKE",
                    target = "Lme/jellysquid/mods/sodium/client/render/immediate/model/EntityRenderer;renderCuboids(Lnet/minecraft/client/util/math/MatrixStack$Entry;Lnet/caffeinemc/mods/sodium/api/vertex/buffer/VertexBufferWriter;[Lme/jellysquid/mods/sodium/client/render/immediate/model/ModelCuboid;III)V"
            )
    )
    private static void redirectRenderCuboids(MatrixStack.Entry matrices, VertexBufferWriter writer, ModelCuboid[] cuboids, int light, int overlay, int color, @Local(argsOnly = true) ModelPart modelPart) {
        IModelPartAccessor accessor = (IModelPartAccessor) modelPart;
        ModelPartAccessor.Workaround workaround = accessor.getWorkaround();

        getVertexConsumer = accessor.getVertexConsumer();

        LogManager.getLogger().info(getVertexConsumer);

        if(workaround == ModelPartAccessor.Workaround.ExportQuads){ // Unchecked
            for (ModelCuboid cuboid : cuboids) {
                ((CuboidSideAccessor) ((ModelCuboidAccessor) cuboid).getOriginal()).doSideSwapping(); //:D
            }

            renderCuboids(matrices, writer, cuboids, light, overlay, color);

            for(ModelCuboid cuboid : cuboids){
                ((CuboidSideAccessor) ((ModelCuboidAccessor) cuboid).getOriginal()).resetSides(); //:D
            }
        } else if(workaround == ModelPartAccessor.Workaround.VanillaDraw){
            if(!accessor.hasMutatedCuboid() || cuboids.length == 1 && !((ModelCuboidAccessor) cuboids[0]).isHackedByEmoteCraft()) {
                renderCuboids(matrices, writer, cuboids, light, overlay, color);
            }
            else {
                for (ModelCuboid cuboid : cuboids) {
                    ((ModelCuboidAccessor) cuboid).getOriginal()
                            .renderCuboid(matrices, getVertexConsumer, light, overlay, ColorABGR.unpackRed(color), ColorABGR.unpackGreen(color), ColorABGR.unpackBlue(color), ColorABGR.unpackAlpha(color));
                }
            }
        } else {
            renderCuboids(matrices, writer, cuboids, light, overlay, color);
        }
    }

    /**
     * @author dima_dencep
     * @reason fix bend
     */
    @Overwrite
    private static void renderCuboids(MatrixStack.Entry matrices, VertexBufferWriter writer, ModelCuboid[] cuboids, int light, int overlay, int color) {
        prepareNormals(matrices);

        for (ModelCuboid cuboid : cuboids) {

            if (cuboid instanceof ModelCuboidAccessor accessor && accessor.isHackedByEmoteCraft()) {
                accessor.getOriginal()
                        .renderCuboid(matrices, getVertexConsumer, light, overlay, ColorABGR.unpackRed(color), ColorABGR.unpackGreen(color), ColorABGR.unpackBlue(color), ColorABGR.unpackAlpha(color));
            } else {
                prepareVertices(matrices, cuboid);

                var vertexCount = emitQuads(cuboid, color, overlay, light);

                try (MemoryStack stack = MemoryStack.stackPush()) {
                    writer.push(stack, SCRATCH_BUFFER, vertexCount, ModelVertex.FORMAT);
                }
            }
        }
    }
}
