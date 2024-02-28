package io.github.kosmx.bendylib.mixin.sodium;

import io.github.kosmx.bendylib.MutableCuboid;
import io.github.kosmx.bendylib.compat.sodium.ModelCuboidAccessor;
import me.jellysquid.mods.sodium.client.render.immediate.model.ModelCuboid;
import net.minecraft.client.model.ModelPart;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Pair;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

@Mixin(ModelCuboid.class)
public class ModelCuboidMixin implements ModelCuboidAccessor {
    @Unique
    public ModelPart.Cuboid orgiginal;

    @Override
    public boolean isHackedByEmoteCraft() {
        if (!(orgiginal instanceof MutableCuboid mutableCuboid))
            return false;

        return mutableCuboid.getActiveMutator() != null;
    }

    @Override
    public ModelPart.Cuboid getOriginal() {
        return orgiginal;
    }

    @Override
    public void setOriginal(ModelPart.Cuboid cuboid) {
        this.orgiginal = cuboid;
    }
}
