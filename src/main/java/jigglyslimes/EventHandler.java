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

@Mod.EventBusSubscriber(Side.CLIENT)
public class EventHandler {

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        EntityLivingBase entity = event.getEntityLiving();
        if(entity.getClass() == EntitySlime.class && entity.world.isRemote) {
            EntitySlime entitySlime = (EntitySlime) entity;
            if(!SlimeJigglyBits.BY_ENTITY.containsKey(entitySlime)) {
                SlimeJigglyBits.BY_ENTITY.put(entitySlime, new SlimeJigglyBits());
            }
            SlimeJigglyBits.BY_ENTITY.get(entitySlime).update(entitySlime);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        // TODO: Some visual effect
    }

    @SubscribeEvent
    public static void onConfigChanged(ConfigChangedEvent.OnConfigChangedEvent event) {
        if(event.getModID().equals(JigglySlimes.MODID)) {
            ConfigManager.sync(JigglySlimes.MODID, Config.Type.INSTANCE);
            RenderSlime.createModelComponents();
        }
    }
}
