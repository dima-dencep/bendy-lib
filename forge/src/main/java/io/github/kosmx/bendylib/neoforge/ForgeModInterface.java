package io.github.kosmx.bendylib.neoforge;

import io.github.kosmx.bendylib.compat.tr7zw.TDSkinCompat;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.fml.ModList;
import net.neoforged.fml.common.Mod;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Mod(value = "bendylib", dist = Dist.CLIENT)
public class ForgeModInterface {
    public static Logger LOGGER = LoggerFactory.getLogger("bendy-lib");

    public ForgeModInterface() {
        if (ModList.get().isLoaded("skinlayers3d")) {
            LOGGER.info("Initializing 3D Skin Layers compatibility");

            try {
                TDSkinCompat.init();
            } catch(NoClassDefFoundError|ClassNotFoundException e) {
                LOGGER.error("Failed to initialize 3D Skin Layers compatibility");
            }
        }
    }
}
