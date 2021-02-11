package jigglyslimes;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import jigglyslimes.math.MathUtil;
import jigglyslimes.model.BoxMesh;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.WorldVertexBufferUploader;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.model.SlimeModel;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.entity.Entity;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.world.LightType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

@OnlyIn(Dist.CLIENT)
public class SlimeRenderer extends LivingRenderer<SlimeEntity, SlimeModel<SlimeEntity>> {
    public static final BufferBuilder BUFFER = new BufferBuilder(256);

    private static final ResourceLocation SLIME_TEXTURES = new ResourceLocation("textures/entity/slime/slime.png");
    private static final int TEXTURE_WIDTH = 64;
    private static final int TEXTURE_HEIGHT = 32;
    private static BoxMesh INNER_BODY;
    private static BoxMesh RIGHT_EYE;
    private static BoxMesh LEFT_EYE;
    private static BoxMesh MOUTH;
    private static BoxMesh OUTER_BODY;

    // Temporary vectors
    private static final Vector3f[] lerpedJigglyBits = new Vector3f[8];

    static {
        createModelComponents();
        for(int i = 0; i < 8; i++) lerpedJigglyBits[i] = new Vector3f();
    }

    public static void createModelComponents() {
        INNER_BODY = new BoxMesh(new Vector3f(0.125F, 0.125F, 0.125F), new Vector3f(0.875F, 0.875F, 0.875F), 0, 16, 6, 6, 6, JSConfig.meshResolution.getValue(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
        RIGHT_EYE = new BoxMesh(new Vector3f(0.09375F, 0.5F, 0.6875F), new Vector3f(0.34375F, 0.75F, 0.9375F), 32, 0, 2, 2, 2, Math.max(JSConfig.meshResolution.getValue() - 2, 0), TEXTURE_WIDTH, TEXTURE_HEIGHT);
        LEFT_EYE = new BoxMesh(new Vector3f(0.65625F, 0.5F, 0.6875F), new Vector3f(0.90625F, 0.75F, 0.9375F), 32, 4, 2, 2, 2, Math.max(JSConfig.meshResolution.getValue() - 2, 0), TEXTURE_WIDTH, TEXTURE_HEIGHT);
        MOUTH = new BoxMesh(new Vector3f(0.5F, 0.25F, 0.8125F), new Vector3f(0.625F, 0.375F, 0.9375F), 32, 8, 1, 1, 1, Math.max(JSConfig.meshResolution.getValue() - 3, 0), TEXTURE_WIDTH, TEXTURE_HEIGHT);
        OUTER_BODY = new BoxMesh(new Vector3f(0.0F, 0.0F, 0.0F), new Vector3f(1.0F, 1.0F, 1.0F), 0, 0, 8, 8, 8, JSConfig.meshResolution.getValue(), TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public SlimeRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn, new SlimeModel<>(16), 0.25F);
    }

    /**
     * Returns the location of an entity's texture.
     */
    @Override
    @Nonnull
    public ResourceLocation getEntityTexture(@Nonnull SlimeEntity entity) {
        return SLIME_TEXTURES;
    }

    @Override
    protected boolean canRenderName(@Nonnull SlimeEntity entity) {
        return super.canRenderName(entity) && (entity.getAlwaysRenderNameTagForRender() || entity.hasCustomName() && entity == this.renderManager.pointedEntity);
    }

    @Override
    public boolean shouldRender(@Nonnull SlimeEntity livingEntityIn, @Nonnull ClippingHelper camera, double camX, double camY, double camZ) {
        if(super.shouldRender(livingEntityIn, camera, camX, camY, camZ)) {
            return true;
        } else {
            Entity entity = livingEntityIn.getLeashHolder();
            return entity != null && camera.isBoundingBoxInFrustum(entity.getRenderBoundingBox());
        }
    }

    @Override
    public void render(SlimeEntity entity, float entityYaw, float partialTicks, @Nonnull MatrixStack matrixStack, @Nonnull IRenderTypeBuffer renderTypeBuffer, int packedLightIn) {
        this.shadowSize = 0.25F * entity.getSlimeSize();

        if(!MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Pre<>(entity, this, partialTicks, matrixStack, renderTypeBuffer, packedLightIn))) {
            if(!SlimeJigglyBits.BY_ENTITY.containsKey(entity)) {
                SlimeJigglyBits.BY_ENTITY.put(entity, new SlimeJigglyBits(entity.getPositionVec()));
            }
            SlimeJigglyBits jigglyBits = SlimeJigglyBits.BY_ENTITY.get(entity);

            for(int i = 0; i < 8; i++) {
                MathUtil.lerp(jigglyBits.prevPos[i], jigglyBits.pos[i], partialTicks, lerpedJigglyBits[i]);
            }

            final Minecraft minecraft = Minecraft.getInstance();
            // resReduction = max(log2(distance) - 4, 0)
            final int resReduction = minecraft.player == null ? 0 : Math.max((MathHelper.log2((int) entity.getDistanceSq(minecraft.player)) >> 1) - 4, 0);
            final boolean entityIsVisible = this.isVisible(entity);
            final int packedOverlay = getPackedOverlay(entity, this.getOverlayProgress(entity, partialTicks));

            RenderType renderType;
            if(!entityIsVisible && !entity.isInvisibleToPlayer(minecraft.player)) {
                // TODO
                renderType = RenderType.getItemEntityTranslucentCull(getEntityTexture(entity));
                IVertexBuilder vertexBuilder = renderTypeBuffer.getBuffer(renderType);
                renderOpaqueModelComponents(matrixStack.getLast(), vertexBuilder, packedLightIn, packedOverlay, 0.15F, resReduction);
            } else if(entityIsVisible) {
                renderType = JSRenderType.getEntityCutoutNoCullTris(getEntityTexture(entity));
                BUFFER.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.ENTITY);
                renderOpaqueModelComponents(matrixStack.getLast(), BUFFER, packedLightIn, packedOverlay, 1.0F, resReduction);
                BUFFER.finishDrawing();
                renderType.setupRenderState();
                WorldVertexBufferUploader.draw(BUFFER);
                renderType.clearRenderState();
            } else if(minecraft.isEntityGlowing(entity)) {
                // TODO
                renderType = RenderType.getOutline(getEntityTexture(entity));
                IVertexBuilder vertexBuilder = renderTypeBuffer.getBuffer(renderType);
                renderOpaqueModelComponents(matrixStack.getLast(), vertexBuilder, packedLightIn, packedOverlay, 1.0F, resReduction);
            }
            if(!entity.isInvisible()) {
                renderType = JSRenderType.getEntityTranslucentTris(getEntityTexture(entity));
                BUFFER.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.ENTITY);
                renderTranslucentModelComponents(matrixStack.getLast(), BUFFER, packedLightIn, LivingRenderer.getPackedOverlay(entity, 0.0F), resReduction);
                BUFFER.finishDrawing();
                renderType.setupRenderState();
                WorldVertexBufferUploader.draw(BUFFER);
                renderType.clearRenderState();
            }

            RenderNameplateEvent renderNameplateEvent = new RenderNameplateEvent(entity, entity.getDisplayName(), this, matrixStack, renderTypeBuffer, packedLightIn, partialTicks);
            MinecraftForge.EVENT_BUS.post(renderNameplateEvent);
            if(renderNameplateEvent.getResult() != Event.Result.DENY && (renderNameplateEvent.getResult() == Event.Result.ALLOW || this.canRenderName(entity))) {
                this.renderName(entity, renderNameplateEvent.getContent(), matrixStack, renderTypeBuffer, packedLightIn);
            }

            MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Post<>(entity, this, partialTicks, matrixStack, renderTypeBuffer, packedLightIn));
        }

        final Entity leashHolder = entity.getLeashHolder();
        if(leashHolder != null) {
            this.renderLeash(entity, partialTicks, matrixStack, renderTypeBuffer, leashHolder);
        }
    }

    private <E extends Entity> void renderLeash(SlimeEntity entityLivingIn, float partialTicks, MatrixStack matrixStack, IRenderTypeBuffer renderTypeBuffer, E leashHolder) {
        matrixStack.push();
        Vector3d vector3d = leashHolder.getLeashPosition(partialTicks);
        double d0 = (double) (MathHelper.lerp(partialTicks, entityLivingIn.renderYawOffset, entityLivingIn.prevRenderYawOffset) * ((float) Math.PI / 180F)) + (Math.PI / 2D);
        Vector3d vector3d1 = entityLivingIn.func_241205_ce_();
        double d1 = Math.cos(d0) * vector3d1.z + Math.sin(d0) * vector3d1.x;
        double d2 = Math.sin(d0) * vector3d1.z - Math.cos(d0) * vector3d1.x;
        double d3 = MathHelper.lerp(partialTicks, entityLivingIn.prevPosX, entityLivingIn.getPosX()) + d1;
        double d4 = MathHelper.lerp(partialTicks, entityLivingIn.prevPosY, entityLivingIn.getPosY()) + vector3d1.y;
        double d5 = MathHelper.lerp(partialTicks, entityLivingIn.prevPosZ, entityLivingIn.getPosZ()) + d2;
        matrixStack.translate(d1, vector3d1.y, d2);
        float f = (float) (vector3d.x - d3);
        float f1 = (float) (vector3d.y - d4);
        float f2 = (float) (vector3d.z - d5);
        IVertexBuilder vertexBuilder = renderTypeBuffer.getBuffer(RenderType.getLeash());
        Matrix4f matrix4f = matrixStack.getLast().getMatrix();
        float f4 = MathHelper.fastInvSqrt(f * f + f2 * f2) * 0.025F / 2.0F;
        float f5 = f2 * f4;
        float f6 = f * f4;
        BlockPos blockpos = new BlockPos(entityLivingIn.getEyePosition(partialTicks));
        BlockPos blockpos1 = new BlockPos(leashHolder.getEyePosition(partialTicks));
        int i = this.getBlockLight(entityLivingIn, blockpos);
        //int j = this.renderManager.getRenderer(leashHolder).getBlockLight(leashHolder, blockpos1);
        int j = leashHolder.isBurning() ? 15 : leashHolder.world.getLightFor(LightType.BLOCK, blockpos1);
        int k = entityLivingIn.world.getLightFor(LightType.SKY, blockpos);
        int l = entityLivingIn.world.getLightFor(LightType.SKY, blockpos1);
        MobRenderer.renderSide(vertexBuilder, matrix4f, f, f1, f2, i, j, k, l, 0.025F, 0.025F, f5, f6);
        MobRenderer.renderSide(vertexBuilder, matrix4f, f, f1, f2, i, j, k, l, 0.025F, 0.0F, f5, f6);
        matrixStack.pop();
    }

    private static void renderOpaqueModelComponents(MatrixStack.Entry lastMatrixEntry, IVertexBuilder vertexBuilder, int packedLightIn, int packedOverlayIn, float alpha, int resReduction) {
        INNER_BODY.render(lastMatrixEntry, vertexBuilder, packedLightIn, packedOverlayIn, 1.0F, 1.0F, 1.0F, alpha, resReduction, lerpedJigglyBits);
        RIGHT_EYE.render(lastMatrixEntry, vertexBuilder, packedLightIn, packedOverlayIn, 1.0F, 1.0F, 1.0F, alpha, resReduction, lerpedJigglyBits);
        LEFT_EYE.render(lastMatrixEntry, vertexBuilder, packedLightIn, packedOverlayIn, 1.0F, 1.0F, 1.0F, alpha, resReduction, lerpedJigglyBits);
        MOUTH.render(lastMatrixEntry, vertexBuilder, packedLightIn, packedOverlayIn, 1.0F, 1.0F, 1.0F, alpha, resReduction, lerpedJigglyBits);
    }

    private static void renderTranslucentModelComponents(MatrixStack.Entry lastMatrixEntry, IVertexBuilder vertexBuilder, int packedLightIn, int packedOverlayIn, int resReduction) {
        OUTER_BODY.render(lastMatrixEntry, vertexBuilder, packedLightIn, packedOverlayIn, 1.0F, 1.0F, 1.0F, 1.0F, resReduction, lerpedJigglyBits);
    }
}
