package jigglyslimes;

import jigglyslimes.math.MathUtil;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

/**
 * Eight point-masses that interact with each other and the environment to simulate the physics of a slime.
 */
public class SlimeJigglyBits {

    public static final float DENSITY = 1200.0F; // In kg/m^3
    public static final float RIGIDITY = 30.0F;
    public static final float INTERNAL_FRICTION = 0.95F;
    public static final float COLLISION_FRICTION = 0.5F;

    // Coordinates are relative to the entity origin (non-rotated).
    public final Vector3f[] prevPos = new Vector3f[8];
    public final Vector3f[] pos = new Vector3f[8];
    public final Vector3f[] vel = new Vector3f[8];

    // Temporary vectors
    private static final Vector3f temp = new Vector3f();

    public SlimeJigglyBits() {
        for(int i = 0; i < 8; i++) {
            prevPos[i] = new Vector3f();
            pos[i] = new Vector3f();
            vel[i] = new Vector3f();
        }
    }

    /**
     * Called whenever the corresponding entity is updated.
     * @param entity - the entity being updated
     */
    public void update(LivingEntity entity) {
        if(!entity.world.isRemote) return;

        // Calculates the acceleration and updates velocity of each jiggly bit due to compressive and tensile forces.
        calculateInteraction(0, 4, entity.getWidth());
        calculateInteraction(1, 5, entity.getWidth());
        calculateInteraction(2, 6, entity.getWidth());
        calculateInteraction(3, 7, entity.getWidth());
        calculateInteraction(0, 2, entity.getHeight());
        calculateInteraction(1, 3, entity.getHeight());
        calculateInteraction(4, 6, entity.getHeight());
        calculateInteraction(5, 7, entity.getHeight());
        calculateInteraction(0, 1, entity.getWidth());
        calculateInteraction(2, 3, entity.getWidth());
        calculateInteraction(4, 5, entity.getWidth());
        calculateInteraction(6, 7, entity.getWidth());
        calculateInteraction(0, 7, MathHelper.sqrt(2 * entity.getWidth() * entity.getWidth() + entity.getHeight() * entity.getHeight()));
        calculateInteraction(1, 6, MathHelper.sqrt(2 * entity.getWidth() * entity.getWidth() + entity.getHeight() * entity.getHeight()));
        calculateInteraction(2, 5, MathHelper.sqrt(2 * entity.getWidth() * entity.getWidth() + entity.getHeight() * entity.getHeight()));
        calculateInteraction(3, 4, MathHelper.sqrt(2 * entity.getWidth() * entity.getWidth() + entity.getHeight() * entity.getHeight()));

        // Apply friction due to compression, tension, and shearing.
        for(int i = 0; i < 8; i++) {
            vel[i].mul(INTERNAL_FRICTION);
        }

        // Calculates the acceleration and updates velocity of each jiggly bit due to forces that restore rotation and relative position.
        boolean renderUpsideDown = false;
        if(entity.hasCustomName()) {
            String s = TextFormatting.getTextWithoutFormattingCodes(entity.getName().getString());
            renderUpsideDown = "Dinnerbone".equals(s) || "Grumm".equals(s);
        }
        double cosTheta = Math.cos(Math.toRadians(entity.renderYawOffset));
        double sinTheta = Math.sin(Math.toRadians(entity.renderYawOffset));
        double halfWidth = entity.getWidth() / 2;
        for(int i = 0; i < 8; i++) {
            double xx = (((i & 0x04) == 0x00) != renderUpsideDown) ? -halfWidth : halfWidth;
            double zz = (i & 0x01) == 0x00 ? -halfWidth : halfWidth;
            // temp is the position to target
            temp.setX((float) (xx * cosTheta - zz * sinTheta));
            temp.setY((((i & 0x02) == 0x00) != renderUpsideDown) ? 0.0F : entity.getHeight());
            temp.setZ((float) (xx * sinTheta + zz * cosTheta));
            // Ratio of surface area to volume represents metabolism; larger creatures tend to move slower.
            float accelMagnitude = RIGIDITY * (2 * entity.getWidth() * entity.getWidth() + 4 * entity.getWidth() * entity.getHeight()) / (entity.getWidth() * entity.getWidth() * entity.getHeight());
            temp.sub(pos[i]);
            temp.mul(accelMagnitude * 0.05F);
            vel[i].add(temp);
        }

        // TODO: Optimize translations
        // Apply gravity, atmospheric buoyancy, and friction due to air and collisions with blocks and entities.
        translateToWorldCoords(entity);
        for(int i = 0; i < 8; i++) {
            Vector3d position = new Vector3d(pos[i]);
            Material materialAtPos = entity.world.getBlockState(new BlockPos(position)).getMaterial();
            if(materialAtPos.isSolid() || materialAtPos.isLiquid()) {
                vel[i].mul(COLLISION_FRICTION);
            } else {
                translateToEntityCoords(entity); // Air resistance is relative to the entity to prevent bits lagging behind too much.
                float airDensityRatio = JigglySlimes.AIR_DENSITY / DENSITY;
                if(!entity.hasNoGravity()) vel[i].add(0.0F, (1.0F - airDensityRatio) * JigglySlimes.GRAVITY * 0.05F, 0.0F);
                /*
                Approximate quadratic drag in air. True quadratic drag can be achieved by multiplying the
                velocity (v) by 1 - Cv, where C represents numerous factors such as the air density, the drag
                coefficient of air, and the cross-sectional area presented. In this context, large velocities will
                make that value negative, resulting in numerical instability. The expression e^(-Cv) can be used
                instead as it is always positive and is tangent to 1 - Cv at v = 0, and it would probably work well
                server-side if the entity velocity were affected as well. When client-side though, this also has a
                problem in that the jiggly bits can experience such high acceleration that they get stuck and no longer
                move; e^(-Cv) approaches 0 as v -> infinity. Therefore, the expression 1 - Cve^(-Cv) is used here. It is
                also always positive and tangent to 1 - Cv at v = 0, but is asymptotically equal to 1. This means that
                jiggly bits at extreme velocities won't get stuck while those at small and medium velocities will behave
                similarly to quadratic drag.
                 */
                double Cv = MathUtil.length(vel[i]) * airDensityRatio * JigglySlimes.AIR_FRICTION / Math.pow(entity.getWidth() * entity.getWidth() * entity.getHeight(), 1.0 / 3);
                double velRatioModified = 1 - Cv * Math.exp(-Cv);
                vel[i].mul((float) velRatioModified);
                translateToWorldCoords(entity);
            }

            List<Entity> collidedEntities = entity.world.getEntitiesInAABBexcluding(entity, entity.getRenderBoundingBox(), collided -> collided != null && collided.isAlive() && collided.getRenderBoundingBox().contains(position));
            for(Entity collided : collidedEntities) {
                translateToEntityCoords(collided);
                vel[i].mul(COLLISION_FRICTION);
                translateToWorldCoords(collided);
            }
        }
        translateToEntityCoords(entity);

        // Update jiggly bit positions.
        for(int i = 0; i < 8; i++) {
            prevPos[i].set(pos[i].getX(), pos[i].getY(), pos[i].getZ());
            temp.set(vel[i].getX(), vel[i].getY(), vel[i].getZ());
            temp.mul(0.05F);
            pos[i].add(temp);
        }
    }

    private void calculateInteraction(int jbIndex1, int jbIndex2, float preferredDist) {
        MathUtil.sub(pos[jbIndex2], pos[jbIndex1], temp);
        float dist = MathUtil.length(temp);
        float accelMagnitude = dist == 0.0 ? 0.0F : (dist - preferredDist) * RIGIDITY / dist;
        temp.mul(accelMagnitude * 0.05F);
        vel[jbIndex1].add(temp);
        vel[jbIndex2].sub(temp);
    }

    private void translateToWorldCoords(Entity entity) {
        for(int i = 0; i < 8; i++) {
            MathUtil.add(pos[i], entity.getPosX(), entity.getPosY(), entity.getPosZ());
            MathUtil.add(vel[i], (entity.getPosX() - entity.prevPosX) / 0.05, (entity.getPosY() - entity.prevPosY) / 0.05, (entity.getPosZ() - entity.prevPosZ) / 0.05);
        }
    }

    private void translateToEntityCoords(Entity entity) {
        for(int i = 0; i < 8; i++) {
            MathUtil.sub(pos[i], entity.getPosX(), entity.getPosY(), entity.getPosZ());
            MathUtil.sub(vel[i], (entity.getPosX() - entity.prevPosX) / 0.05, (entity.getPosY() - entity.prevPosY) / 0.05, (entity.getPosZ() - entity.prevPosZ) / 0.05);
        }
    }
}
