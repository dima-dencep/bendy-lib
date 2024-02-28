package io.github.kosmx.bendylib.compat.sodium;

import net.minecraft.client.model.ModelPart;
import net.minecraft.client.util.math.MatrixStack;
import net.minecraft.util.Pair;

public interface ModelCuboidAccessor {
    boolean isHackedByEmoteCraft();

    ModelPart.Cuboid getOriginal();
    void setOriginal(ModelPart.Cuboid cuboid);
}
