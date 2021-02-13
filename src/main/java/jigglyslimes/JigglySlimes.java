package jigglyslimes;

import net.minecraft.entity.monster.EntitySlime;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.event.FMLInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPostInitializationEvent;
import net.minecraftforge.fml.common.event.FMLPreInitializationEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

@Mod(modid = JigglySlimes.MODID, useMetadata = true, clientSideOnly = true, updateJSON = "https://raw.githubusercontent.com/Wallace-Watler/jiggly-slimes/updateJSON/updates.json")
public class JigglySlimes {

    public static final String MODID = "jigglyslimes";
    public static final double GRAVITY = -32.0; // In m/s^2
    public static final double AIR_DENSITY = 1.2; // In kg/m^3

    public static final Logger LOGGER = LogManager.getLogger();

    @Mod.EventHandler
    public static void preInit(FMLPreInitializationEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(EntitySlime.class, RenderSlime::new);
    }

    @Mod.EventHandler
    public static void init(FMLInitializationEvent event) {}

    @Mod.EventHandler
    public static void postInit(FMLPostInitializationEvent event) {}
}
