package jigglyslimes;

import net.minecraft.client.renderer.entity.EntityRenderer;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.client.registry.IRenderFactory;
import net.minecraftforge.fml.client.registry.RenderingRegistry;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.event.lifecycle.FMLClientSetupEvent;
import net.minecraftforge.fml.event.lifecycle.FMLCommonSetupEvent;
import net.minecraftforge.fml.event.lifecycle.InterModProcessEvent;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.WeakHashMap;
import java.util.stream.Collectors;

@Mod(JigglySlimes.MODID)
public class JigglySlimes {

    public static final String MODID = "jigglyslimes";
    public static final double GRAVITY = -32.0; // In m/s^2
    public static final double AIR_FRICTION = 50.0;
    public static final double AIR_DENSITY = 1.2; // In kg/m^3

    public static final Logger LOGGER = LogManager.getLogger();

    public JigglySlimes() {
        MinecraftForge.EVENT_BUS.register(this);
    }

    @SubscribeEvent
    public void setup(final FMLCommonSetupEvent event) {

    }

    @SubscribeEvent
    public void doClientStuff(final FMLClientSetupEvent event) {
        //RenderingRegistry.<SlimeEntity>registerEntityRenderingHandler(EntityType.SLIME, manager -> new SlimeRenderer(manager, model, shadowSize));
    }

    @SubscribeEvent
    public void processIMC(final InterModProcessEvent event) {
        LOGGER.info("Got IMC {}", event.getIMCStream()
                .map(m -> m.getMessageSupplier().get())
                .collect(Collectors.toList()));
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
