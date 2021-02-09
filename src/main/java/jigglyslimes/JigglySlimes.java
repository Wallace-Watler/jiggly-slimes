package jigglyslimes;

import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.javafmlmod.FMLJavaModLoadingContext;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.WeakHashMap;

@Mod(JigglySlimes.MODID)
public class JigglySlimes {

    public static final String MODID = "jigglyslimes";
    public static final float GRAVITY = -32.0F; // In m/s^2
    public static final float AIR_FRICTION = 50.0F;
    public static final float AIR_DENSITY = 1.2F; // In kg/m^3

    private static final Logger LOGGER = LogManager.getLogger();

    public JigglySlimes() {
        FMLJavaModLoadingContext.get().getModEventBus().addListener(this::doClientStuff);
    }

    private void doClientStuff(final FMLClientSetupEvent event) {
        RenderingRegistry.registerEntityRenderingHandler(EntityType.SLIME, SlimeRenderer::new);
        LOGGER.debug("Registered renderer for EntityType.SLIME.");
    }

    @Mod.EventBusSubscriber(value = Dist.CLIENT)
    public static class EventHandler {
        public static final WeakHashMap<SlimeEntity, SlimeJigglyBits> JB_MAP = new WeakHashMap<>();

        @SubscribeEvent
        public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
            LivingEntity entity = event.getEntityLiving();
            if(entity.getClass() == SlimeEntity.class && entity.world.isRemote) {
                SlimeEntity entitySlime = (SlimeEntity) entity;
                if(!JB_MAP.containsKey(entitySlime)) JB_MAP.put(entitySlime, new SlimeJigglyBits());
                JB_MAP.get(entitySlime).update(entitySlime);
            }
        }

        @SubscribeEvent
        public static void onLivingDeath(LivingDeathEvent event) {
            // TODO: Some visual effect
        }
    }
}
