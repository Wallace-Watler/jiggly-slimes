package jigglyslimes;

import net.minecraft.block.material.Material;
import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityLiving;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.Vec3d;
import net.minecraft.util.text.TextFormatting;
import org.lwjgl.util.vector.Vector3f;

import java.util.List;

/**
 * Eight point-masses that interact with each other and the environment to simulate the physics of a slime.
 */
public class SlimeJigglyBits {

    public static final float DENSITY = 1200.0F; // In kg/m^3
    public static final float RIGIDITY = 30.0F;
    public static final float INTERNAL_FRICTION = 0.055F;
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
    public void update(EntityLiving entity) {
        if(!entity.world.isRemote) return;

        final float w = entity.width;
        final float h = entity.height;
        final float diagonal = MathHelper.sqrt(w * w + w * w + h * h);
        final float volume = w * w * h;

        // Calculates the acceleration and updates velocity of each jiggly bit due to compressive and tensile forces.
        calculateInteraction(0, 4, w);
        calculateInteraction(1, 5, w);
        calculateInteraction(2, 6, w);
        calculateInteraction(3, 7, w);
        calculateInteraction(0, 2, h);
        calculateInteraction(1, 3, h);
        calculateInteraction(4, 6, h);
        calculateInteraction(5, 7, h);
        calculateInteraction(0, 1, w);
        calculateInteraction(2, 3, w);
        calculateInteraction(4, 5, w);
        calculateInteraction(6, 7, w);
        calculateInteraction(0, 7, diagonal);
        calculateInteraction(1, 6, diagonal);
        calculateInteraction(2, 5, diagonal);
        calculateInteraction(3, 4, diagonal);

        /*
        Apply friction due to internal forces such as compression, tension, and shearing.

        This is done by approximating quadratic drag between the particles of the material. True quadratic drag can be
        achieved by multiplying the velocity (v) by 1 - Cv, where C represents various factors. When numerically
        integrating though, large velocities will make that value negative, resulting in numerical instability. Here the
        expression e^(-Cv) is used instead as it is always positive and is tangent to 1 - Cv at v = 0.

        Quadratic drag is used here instead of linear drag because it doesn't affect slow velocities as much as higher
        ones. This allows the jiggly bits to continuously move around a bit while also keeping them from flying out of
        control whenever the entity moves suddenly.
         */
        final float C = INTERNAL_FRICTION / (float) Math.pow(volume, 1.0 / 3);
        for(int i = 0; i < 8; i++) {
            vel[i].scale((float) Math.exp(-C * vel[i].length()));
        }

        // Calculates the acceleration and updates velocity of each jiggly bit due to forces that restore rotation and relative position.
        boolean renderUpsideDown = false;
        if(entity.hasCustomName()) {
            String s = TextFormatting.getTextWithoutFormattingCodes(entity.getName());
            renderUpsideDown = "Dinnerbone".equals(s) || "Grumm".equals(s);
        }
        float cosTheta = (float) Math.cos(Math.toRadians(entity.renderYawOffset));
        float sinTheta = (float) Math.sin(Math.toRadians(entity.renderYawOffset));
        float halfWidth = entity.width / 2;
        // Ratio of surface area to volume represents metabolism; larger creatures tend to move slower.
        final float accelMagnitude = RIGIDITY * (2 * w * w + 4 * w * h) / volume;
        for(int i = 0; i < 8; i++) {
            float xx = (((i & 0x04) == 0x00) != renderUpsideDown) ? -halfWidth : halfWidth;
            float zz = (i & 0x01) == 0x00 ? -halfWidth : halfWidth;
            // temp is the position to target
            temp.x = xx * cosTheta - zz * sinTheta;
            temp.y = (((i & 0x02) == 0x00) != renderUpsideDown) ? 0.0F : entity.height;
            temp.z = zz * cosTheta + xx * sinTheta;
            Vector3f.sub(temp, pos[i], temp).scale(accelMagnitude * 0.05F);
            Vector3f.add(vel[i], temp, vel[i]);
        }

        // Apply gravity, atmospheric buoyancy, and friction due to collisions with blocks and entities.
        translateToWorldCoords(entity);
        for(int i = 0; i < 8; i++) {
            Vec3d position = new Vec3d(pos[i].x, pos[i].y, pos[i].z);
            Material materialAtPos = entity.world.getBlockState(new BlockPos(position)).getMaterial();
            if(materialAtPos.isSolid() || materialAtPos.isLiquid()) {
                vel[i].scale(COLLISION_FRICTION);
            } else {
                float airDensityRatio = JigglySlimes.AIR_DENSITY / DENSITY;
                if(!entity.hasNoGravity()) vel[i].y += (1.0F - airDensityRatio) * JigglySlimes.GRAVITY * 0.05F;
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
            prevPos[i].set(pos[i]);
            temp.set(vel[i]).scale(0.05F);
            Vector3f.add(pos[i], temp, pos[i]);
        }
    }

    private void calculateInteraction(int jbIndex1, int jbIndex2, float preferredDist) {
        Vector3f.sub(pos[jbIndex2], pos[jbIndex1], temp);
        float dist = temp.length();
        float accelMagnitude = dist == 0.0F ? 0.0F : (dist - preferredDist) * RIGIDITY / dist;
        temp.scale(accelMagnitude * 0.05F);
        Vector3f.add(vel[jbIndex1], temp, vel[jbIndex1]);
        Vector3f.sub(vel[jbIndex2], temp, vel[jbIndex2]);
    }

    private void translateToWorldCoords(Entity entity) {
        for(int i = 0; i < 8; i++) {
            pos[i].translate((float) entity.posX, (float) entity.posY, (float) entity.posZ);
            vel[i].translate((float) (entity.posX - entity.prevPosX) / 0.05F, (float) (entity.posY - entity.prevPosY) / 0.05F, (float) (entity.posZ - entity.prevPosZ) / 0.05F);
        }
    }

    private void translateToEntityCoords(Entity entity) {
        for(int i = 0; i < 8; i++) {
            pos[i].translate((float) -entity.posX, (float) -entity.posY, (float) -entity.posZ);
            vel[i].translate((float) -(entity.posX - entity.prevPosX) / 0.05F, (float) -(entity.posY - entity.prevPosY) / 0.05F, (float) -(entity.posZ - entity.prevPosZ) / 0.05F);
        }
    }
}
