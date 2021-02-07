package jigglyslimes;

import jigglyslimes.math.Vec3D;
import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;

import java.util.List;

/**
 * Eight point-masses that interact with each other and the environment to simulate the physics of a slime.
 */
public class SlimeJigglyBits {

    public static final double DENSITY = 1200.0; // In kg/m^3
    public static final double RIGIDITY = 30.0;
    public static final double INTERNAL_FRICTION = 0.95;
    public static final double COLLISION_FRICTION = 0.5;

    // Coordinates are relative to the entity origin (non-rotated).
    public final Vec3D[] prevPos = new Vec3D[8];
    public final Vec3D[] pos = new Vec3D[8];
    public final Vec3D[] vel = new Vec3D[8];

    // Temporary vectors
    private static final Vec3D temp = new Vec3D();
    private static final Vec3D[] velTimesDT = new Vec3D[8];

    static {
        for(int i = 0; i < 8; i++) velTimesDT[i] = new Vec3D();
    }

    public SlimeJigglyBits() {
        for(int i = 0; i < 8; i++) {
            prevPos[i] = new Vec3D();
            pos[i] = new Vec3D();
            vel[i] = new Vec3D();
        }
    }

    /**
     * Called whenever the corresponding entity is updated.
     * @param entity - the entity being updated
     */
    public void update(EntitySlime entity) {
        if(!entity.world.isRemote) return;

        // Calculates the acceleration and updates velocity of each jiggly bit due to compressive and tensile forces.
        calculateInteraction(0, 4, entity.width);
        calculateInteraction(1, 5, entity.width);
        calculateInteraction(2, 6, entity.width);
        calculateInteraction(3, 7, entity.width);
        calculateInteraction(0, 2, entity.height);
        calculateInteraction(1, 3, entity.height);
        calculateInteraction(4, 6, entity.height);
        calculateInteraction(5, 7, entity.height);
        calculateInteraction(0, 1, entity.width);
        calculateInteraction(2, 3, entity.width);
        calculateInteraction(4, 5, entity.width);
        calculateInteraction(6, 7, entity.width);
        calculateInteraction(0, 7, Math.sqrt(2 * entity.width * entity.width + entity.height * entity.height));
        calculateInteraction(1, 6, Math.sqrt(2 * entity.width * entity.width + entity.height * entity.height));
        calculateInteraction(2, 5, Math.sqrt(2 * entity.width * entity.width + entity.height * entity.height));
        calculateInteraction(3, 4, Math.sqrt(2 * entity.width * entity.width + entity.height * entity.height));

        // Apply friction due to compression, tension, and shearing.
        for(int i = 0; i < 8; i++) {
            vel[i].scale(INTERNAL_FRICTION);
        }

        // Calculates the acceleration and updates velocity of each jiggly bit due to forces that restore rotation and relative position.
        String s = TextFormatting.getTextWithoutFormattingCodes(entity.getName());
        boolean renderUpsideDown = "Dinnerbone".equals(s) || "Grumm".equals(s);
        double cosTheta = Math.cos(Math.toRadians(entity.renderYawOffset));
        double sinTheta = Math.sin(Math.toRadians(entity.renderYawOffset));
        double halfWidth = entity.width / 2;
        for(int i = 0; i < 8; i++) {
            double xx = (((i & 0x04) == 0x00) != renderUpsideDown) ? -halfWidth : halfWidth;
            double zz = (i & 0x01) == 0x00 ? -halfWidth : halfWidth;
            // temp is the position to target
            temp.x = xx * cosTheta - zz * sinTheta;
            temp.y = (((i & 0x02) == 0x00) != renderUpsideDown) ? 0.0 : entity.height;
            temp.z = zz * cosTheta + xx * sinTheta;
            // Ratio of surface area to volume represents metabolism; larger creatures tend to move slower.
            double accelMagnitude = RIGIDITY * (2 * entity.width * entity.width + 4 * entity.width * entity.height) / (entity.width * entity.width * entity.height);
            vel[i].add(temp.subtract(pos[i]).scale(accelMagnitude * 0.05));
        }

        // Apply gravity, atmospheric buoyancy, and friction due to air and collisions with blocks and entities.
        translateToWorldCoords(entity);
        for(int i = 0; i < 8; i++) {
            Vec3d position = new Vec3d(pos[i].x, pos[i].y, pos[i].z);
            Material materialAtPos = entity.world.getBlockState(new BlockPos(position)).getMaterial();
            if(materialAtPos.isSolid() || materialAtPos.isLiquid()) {
                vel[i].scale(COLLISION_FRICTION);
            } else {
                translateToEntityCoords(entity); // Air resistance is relative to the entity to prevent bits lagging behind too much.
                double airDensityRatio = JigglySlimes.AIR_DENSITY / DENSITY;
                if(!entity.hasNoGravity()) vel[i].y += (1.0 - airDensityRatio) * JigglySlimes.GRAVITY * 0.05;
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
                double Cv = vel[i].length() * airDensityRatio * JigglySlimes.AIR_FRICTION / Math.pow(entity.width * entity.width * entity.height, 1.0 / 3);
                double velRatioModified = 1 - Cv * Math.exp(-Cv);
                vel[i].scale(velRatioModified);
                translateToWorldCoords(entity);
            }

            List<Entity> collidedEntities = entity.world.getEntities(Entity.class, collided -> collided != null && collided != entity && collided.isEntityAlive() && collided.getRenderBoundingBox().contains(position));
            for(Entity collided : collidedEntities) {
                translateToEntityCoords(collided);
                vel[i].scale(COLLISION_FRICTION);
                translateToWorldCoords(collided);
            }
        }
        translateToEntityCoords(entity);

        // Update jiggly bit positions.
        for(int i = 0; i < 8; i++) {
            pos[i].copy(prevPos[i]);
            pos[i].add(vel[i].scale(0.05, velTimesDT[i]));
        }
    }

    private void calculateInteraction(int jbIndex1, int jbIndex2, double preferredDist) {
        pos[jbIndex2].subtract(pos[jbIndex1], temp);
        double dist = temp.length();
        double accelMagnitude = dist == 0.0 ? 0.0 : (dist - preferredDist) * RIGIDITY / dist;
        temp.scale(accelMagnitude * 0.05);
        vel[jbIndex1].add(temp);
        vel[jbIndex2].subtract(temp);
    }

    private void translateToWorldCoords(Entity entity) {
        for(int i = 0; i < 8; i++) {
            pos[i].add(entity.posX, entity.posY, entity.posZ);
            vel[i].add((entity.posX - entity.prevPosX) / 0.05, (entity.posY - entity.prevPosY) / 0.05, (entity.posZ - entity.prevPosZ) / 0.05);
        }
    }

    private void translateToEntityCoords(Entity entity) {
        for(int i = 0; i < 8; i++) {
            pos[i].subtract(entity.posX, entity.posY, entity.posZ);
            vel[i].subtract((entity.posX - entity.prevPosX) / 0.05, (entity.posY - entity.prevPosY) / 0.05, (entity.posZ - entity.prevPosZ) / 0.05);
        }
    }
}
