package jigglyslimes;

import jigglyslimes.math.Vec3D;
import jigglyslimes.model.BoxMesh;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.model.SlimeModel;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class SlimeRenderer extends MobRenderer<SlimeEntity, SlimeModel<SlimeEntity>> {
    private static final ResourceLocation SLIME_TEXTURES = new ResourceLocation("textures/entity/slime/slime.png");
    private static final int TEXTURE_WIDTH = 64;
    private static final int TEXTURE_HEIGHT = 32;
    private static BoxMesh INNER_BODY;
    private static BoxMesh RIGHT_EYE;
    private static BoxMesh LEFT_EYE;
    private static BoxMesh MOUTH;
    private static BoxMesh OUTER_BODY;

    // Temporary vectors
    private static final Vec3D[] lerpedJigglyBits = new Vec3D[8];

    static {
        createModelComponents();
        for(int i = 0; i < 8; i++) lerpedJigglyBits[i] = new Vec3D();
    }

    public static void createModelComponents() {
        final int meshResolution = 4;
        INNER_BODY = new BoxMesh(new Vec3D(0.125, 0.125, 0.125), new Vec3D(0.875, 0.875, 0.875), 0, 16, 6, 6, 6, meshResolution, SLIME_TEXTURES, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        RIGHT_EYE = new BoxMesh(new Vec3D(0.09375, 0.5, 0.6875), new Vec3D(0.34375, 0.75, 0.9375), 32, 0, 2, 2, 2, Math.max(meshResolution - 2, 0), SLIME_TEXTURES, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        LEFT_EYE = new BoxMesh(new Vec3D(0.65625, 0.5, 0.6875), new Vec3D(0.90625, 0.75, 0.9375), 32, 4, 2, 2, 2, Math.max(meshResolution - 2, 0), SLIME_TEXTURES, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        MOUTH = new BoxMesh(new Vec3D(0.5, 0.25, 0.8125), new Vec3D(0.625, 0.375, 0.9375), 32, 8, 1, 1, 1, Math.max(meshResolution - 3, 0), SLIME_TEXTURES, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        OUTER_BODY = new BoxMesh(new Vec3D(0.0, 0.0, 0.0), new Vec3D(1.0, 1.0, 1.0), 0, 0, 8, 8, 8, meshResolution, SLIME_TEXTURES, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public SlimeRenderer(EntityRendererManager renderManagerIn, SlimeModel<SlimeEntity> entityModelIn, float shadowSizeIn) {
        super(renderManagerIn, entityModelIn, shadowSizeIn);
    }

    @Override
    @Nonnull
    public ResourceLocation getEntityTexture(@Nonnull SlimeEntity entity) {
        return SLIME_TEXTURES;
    }

    private void renderOpaqueModelComponents(int resReduction, Vec3D modelCorner0, Vec3D modelCorner1, Vec3D modelCorner2, Vec3D modelCorner3, Vec3D modelCorner4, Vec3D modelCorner5, Vec3D modelCorner6, Vec3D modelCorner7) {
        INNER_BODY.render(resReduction, modelCorner0, modelCorner1, modelCorner2, modelCorner3, modelCorner4, modelCorner5, modelCorner6, modelCorner7);
        RIGHT_EYE.render(resReduction, modelCorner0, modelCorner1, modelCorner2, modelCorner3, modelCorner4, modelCorner5, modelCorner6, modelCorner7);
        LEFT_EYE.render(resReduction, modelCorner0, modelCorner1, modelCorner2, modelCorner3, modelCorner4, modelCorner5, modelCorner6, modelCorner7);
        MOUTH.render(resReduction, modelCorner0, modelCorner1, modelCorner2, modelCorner3, modelCorner4, modelCorner5, modelCorner6, modelCorner7);
    }

    private void renderTranslucentModelComponents(int resReduction, Vec3D modelCorner0, Vec3D modelCorner1, Vec3D modelCorner2, Vec3D modelCorner3, Vec3D modelCorner4, Vec3D modelCorner5, Vec3D modelCorner6, Vec3D modelCorner7) {
        OUTER_BODY.render(resReduction, modelCorner0, modelCorner1, modelCorner2, modelCorner3, modelCorner4, modelCorner5, modelCorner6, modelCorner7);
    }
}
