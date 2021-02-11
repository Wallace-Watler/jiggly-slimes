package jigglyslimes;

import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.event.entity.living.LivingDeathEvent;
import net.minecraftforge.event.entity.living.LivingEvent;
import net.minecraftforge.eventbus.api.SubscribeEvent;
import net.minecraftforge.fml.common.Mod;

@Mod.EventBusSubscriber(value = Dist.CLIENT)
public class EventHandler {

    @SubscribeEvent
    public static void onLivingUpdate(LivingEvent.LivingUpdateEvent event) {
        LivingEntity entity = event.getEntityLiving();
        if(entity.getClass() == SlimeEntity.class && entity.world.isRemote) {
            SlimeEntity entitySlime = (SlimeEntity) entity;
            if(!SlimeJigglyBits.BY_ENTITY.containsKey(entitySlime)) {
                SlimeJigglyBits.BY_ENTITY.put(entitySlime, new SlimeJigglyBits(entitySlime.getPositionVec()));
            }
            SlimeJigglyBits.BY_ENTITY.get(entitySlime).update(entitySlime);
        }
    }

    @SubscribeEvent
    public static void onLivingDeath(LivingDeathEvent event) {
        // TODO: Some visual effect
    }
}
