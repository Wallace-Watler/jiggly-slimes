package jigglyslimes;

import net.minecraft.entity.EntityType;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(JigglySlimes.MODID)
public class JigglySlimes {

    public static final String MODID = "jigglyslimes";
    public static final float GRAVITY = -32.0F; // In m/s^2
    public static final float AIR_DENSITY = 1.2F; // In kg/m^3

    private static final Logger LOGGER = LogManager.getLogger();

    public JigglySlimes() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        JSConfig.loadConfig();

        RenderingRegistry.registerEntityRenderingHandler(EntityType.SLIME, SlimeRenderer::new);
        LOGGER.debug("Registered renderer for EntityType.SLIME.");
    }
}
