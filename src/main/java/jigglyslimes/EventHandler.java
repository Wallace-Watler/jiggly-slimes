package jigglyslimes;

import net.minecraft.entity.EntityLivingBase;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraftforge.common.config.Config;
import net.minecraftforge.common.config.ConfigManager;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.fml.client.event.ConfigChangedEvent;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.common.eventhandler.SubscribeEvent;
import net.minecraftforge.fml.relauncher.Side;

import java.util.WeakHashMap;

@Mod.EventBusSubscriber(Side.CLIENT)
public class EventHandler {

    public static final WeakHashMap<EntitySlime, SlimeJigglyBits> JB_MAP = new WeakHashMap<>();

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if(entity.getClass() == EntitySlime.class && entity.world.isRemote) {
            EntitySlime entitySlime = (EntitySlime) entity;
            if(!JB_MAP.containsKey(entitySlime)) JB_MAP.put(entitySlime, new SlimeJigglyBits());
            JB_MAP.get(entitySlime).update(entitySlime);
        }
    }

    // TODO: Try removing this, might fix the odd look when slimes die
    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if(entity.getClass() == EntitySlime.class && entity.world.isRemote) JB_MAP.remove(entity);
    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if(event.getModID().equals(JigglySlimes.MODID)) {
            ConfigManager.sync(JigglySlimes.MODID, Config.Type.INSTANCE);
            RenderSlime.createModelComponents();
        }
    }
}
