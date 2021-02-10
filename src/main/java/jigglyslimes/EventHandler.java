package jigglyslimes;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

import java.util.WeakHashMap;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class EventHandler {
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
