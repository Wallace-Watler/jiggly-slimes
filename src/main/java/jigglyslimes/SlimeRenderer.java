package jigglyslimes;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.culling.ClippingHelper;
import net.minecraft.client.renderer.entity.EntityRendererManager;
import net.minecraft.client.renderer.entity.LivingRenderer;
import net.minecraft.client.renderer.entity.MobRenderer;
import net.minecraft.client.renderer.entity.layers.LayerRenderer;
import net.minecraft.client.renderer.entity.layers.SlimeGelLayer;
import net.minecraft.client.renderer.entity.model.SlimeModel;
import net.minecraft.entity.Entity;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.Pose;
import net.minecraft.entity.monster.SlimeEntity;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerModelPart;
import net.minecraft.util.Direction;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.text.TextFormatting;
import net.minecraft.world.LightType;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.client.event.RenderNameplateEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.eventbus.api.Event;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

@OnlyIn(Dist.CLIENT)
public class SlimeRenderer extends LivingRenderer<SlimeEntity, SlimeModel<SlimeEntity>> {
    private static final ResourceLocation SLIME_TEXTURES = new ResourceLocation("textures/entity/slime/slime.png");

    public SlimeRenderer(EntityRendererManager renderManagerIn) {
        super(renderManagerIn, new SlimeModel<>(16), 0.25F);
        this.addLayer(new SlimeGelLayer<>(this));
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
    public void render(SlimeEntity entityIn, float entityYaw, float partialTicks, @Nonnull MatrixStack matrixStackIn, @Nonnull IRenderTypeBuffer bufferIn, int packedLightIn) {
        this.shadowSize = 0.25F * entityIn.getSlimeSize();

        if(!MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Pre<>(entityIn, this, partialTicks, matrixStackIn, bufferIn, packedLightIn))) {
            matrixStackIn.push();
            this.entityModel.swingProgress = 0.0F;

            boolean shouldSit = entityIn.isPassenger() && (entityIn.getRidingEntity() != null && entityIn.getRidingEntity().shouldRiderSit());
            this.entityModel.isSitting = shouldSit;
            this.entityModel.isChild = false;
            float renderYawOffset = MathHelper.interpolateAngle(partialTicks, entityIn.prevRenderYawOffset, entityIn.renderYawOffset);
            final float rotationYawHead = MathHelper.interpolateAngle(partialTicks, entityIn.prevRotationYawHead, entityIn.rotationYawHead);
            float f2 = rotationYawHead - renderYawOffset;
            if(shouldSit && entityIn.getRidingEntity() instanceof LivingEntity) {
                LivingEntity livingentity = (LivingEntity) entityIn.getRidingEntity();
                renderYawOffset = MathHelper.interpolateAngle(partialTicks, livingentity.prevRenderYawOffset, livingentity.renderYawOffset);
                f2 = rotationYawHead - renderYawOffset;
                float f3 = MathHelper.wrapDegrees(f2);
                if(f3 < -85.0F) {
                    f3 = -85.0F;
                }

                if(f3 >= 85.0F) {
                    f3 = 85.0F;
                }

                renderYawOffset = rotationYawHead - f3;
                if(f3 * f3 > 2500.0F) { // if(abs(f3) > 50.0F)
                    renderYawOffset += f3 * 0.2F;
                }

                f2 = rotationYawHead - renderYawOffset;
            }

            final float ageInTicks = this.handleRotationFloat(entityIn, partialTicks);
            this.applyRotations(entityIn, matrixStackIn, ageInTicks, renderYawOffset, partialTicks);
            matrixStackIn.scale(-1.0F, -1.0F, 1.0F);
            this.preRenderCallback(entityIn, matrixStackIn, partialTicks);
            matrixStackIn.translate(0.0D, -1.501F, 0.0D);
            float limbSwingAmount = 0.0F;
            float f5 = 0.0F;
            if(!shouldSit && entityIn.isAlive()) {
                limbSwingAmount = MathHelper.lerp(partialTicks, entityIn.prevLimbSwingAmount, entityIn.limbSwingAmount);
                f5 = entityIn.limbSwing - entityIn.limbSwingAmount * (1.0F - partialTicks);

                if(limbSwingAmount > 1.0F) {
                    limbSwingAmount = 1.0F;
                }
            }

            this.entityModel.setLivingAnimations(entityIn, f5, limbSwingAmount, partialTicks); // Does nothing
            this.entityModel.setRotationAngles(entityIn, f5, limbSwingAmount, ageInTicks, f2, 0.0F); // Does nothing
            final Minecraft minecraft = Minecraft.getInstance();
            final boolean entityIsVisible = this.isVisible(entityIn);
            final boolean flag1 = !entityIsVisible && !entityIn.isInvisibleToPlayer(minecraft.player);
            RenderType rendertype = this.func_230496_a_(entityIn, entityIsVisible, flag1, minecraft.isEntityGlowing(entityIn));
            if(rendertype != null) {
                IVertexBuilder ivertexbuilder = bufferIn.getBuffer(rendertype);
                int i = getPackedOverlay(entityIn, this.getOverlayProgress(entityIn, partialTicks));
                this.entityModel.render(matrixStackIn, ivertexbuilder, packedLightIn, i, 1.0F, 1.0F, 1.0F, flag1 ? 0.15F : 1.0F);
            }

            for(LayerRenderer<SlimeEntity, SlimeModel<SlimeEntity>> layerrenderer : this.layerRenderers) {
                layerrenderer.render(matrixStackIn, bufferIn, packedLightIn, entityIn, f5, limbSwingAmount, partialTicks, ageInTicks, f2, 0.0F);
            }

            matrixStackIn.pop();

            RenderNameplateEvent renderNameplateEvent = new RenderNameplateEvent(entityIn, entityIn.getDisplayName(), this, matrixStackIn, bufferIn, packedLightIn, partialTicks);
            MinecraftForge.EVENT_BUS.post(renderNameplateEvent);
            if(renderNameplateEvent.getResult() != Event.Result.DENY && (renderNameplateEvent.getResult() == Event.Result.ALLOW || this.canRenderName(entityIn))) {
                this.renderName(entityIn, renderNameplateEvent.getContent(), matrixStackIn, bufferIn, packedLightIn);
            }

            MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Post<>(entityIn, this, partialTicks, matrixStackIn, bufferIn, packedLightIn));
        }

        Entity entity = entityIn.getLeashHolder();
        if(entity != null) {
            this.renderLeash(entityIn, partialTicks, matrixStackIn, bufferIn, entity);
        }
    }

    @Override
    @Nullable
    protected RenderType func_230496_a_(@Nonnull SlimeEntity entityIn, boolean entityIsVisible, boolean p_230496_3_, boolean entityIsGlowing) {
        ResourceLocation resourcelocation = this.getEntityTexture(entityIn);
        if(p_230496_3_) {
            return RenderType.getItemEntityTranslucentCull(resourcelocation);
        } else if(entityIsVisible) {
            return this.entityModel.getRenderType(resourcelocation);
        } else {
            return entityIsGlowing ? RenderType.getOutline(resourcelocation) : null;
        }
    }

    @Override
    protected void applyRotations(@Nonnull SlimeEntity entityLiving, @Nonnull MatrixStack matrixStackIn, float ageInTicks, float rotationYaw, float partialTicks) {
        matrixStackIn.rotate(Vector3f.YP.rotationDegrees(180.0F - rotationYaw));

        if(entityLiving.deathTime > 0) {
            float f = ((float) entityLiving.deathTime + partialTicks - 1.0F) / 20.0F * 1.6F;
            f = MathHelper.sqrt(f);
            if(f > 1.0F) {
                f = 1.0F;
            }
            matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(f * this.getDeathMaxRotation(entityLiving)));
        } else if(entityLiving.hasCustomName()) {
            String s = TextFormatting.getTextWithoutFormattingCodes(entityLiving.getName().getString());
            if("Dinnerbone".equals(s) || "Grumm".equals(s)) {
                matrixStackIn.translate(0.0D, entityLiving.getHeight() + 0.1F, 0.0D);
                matrixStackIn.rotate(Vector3f.ZP.rotationDegrees(180.0F));
            }
        }
    }

    @Override
    protected void preRenderCallback(SlimeEntity entitylivingbaseIn, MatrixStack matrixStackIn, float partialTickTime) {
        float f = 0.999F;
        matrixStackIn.scale(0.999F, 0.999F, 0.999F);
        matrixStackIn.translate(0.0D, (double)0.001F, 0.0D);
        float f1 = (float) entitylivingbaseIn.getSlimeSize();
        float f2 = MathHelper.lerp(partialTickTime, entitylivingbaseIn.prevSquishFactor, entitylivingbaseIn.squishFactor) / (f1 * 0.5F + 1.0F);
        float f3 = 1.0F / (f2 + 1.0F);
        matrixStackIn.scale(f3 * f1, 1.0F / f3 * f1, f3 * f1);
    }

    private <E extends Entity> void renderLeash(SlimeEntity entityLivingIn, float partialTicks, MatrixStack matrixStackIn, IRenderTypeBuffer bufferIn, E leashHolder) {
        matrixStackIn.push();
        Vector3d vector3d = leashHolder.getLeashPosition(partialTicks);
        double d0 = (double) (MathHelper.lerp(partialTicks, entityLivingIn.renderYawOffset, entityLivingIn.prevRenderYawOffset) * ((float) Math.PI / 180F)) + (Math.PI / 2D);
        Vector3d vector3d1 = entityLivingIn.func_241205_ce_();
        double d1 = Math.cos(d0) * vector3d1.z + Math.sin(d0) * vector3d1.x;
        double d2 = Math.sin(d0) * vector3d1.z - Math.cos(d0) * vector3d1.x;
        double d3 = MathHelper.lerp(partialTicks, entityLivingIn.prevPosX, entityLivingIn.getPosX()) + d1;
        double d4 = MathHelper.lerp(partialTicks, entityLivingIn.prevPosY, entityLivingIn.getPosY()) + vector3d1.y;
        double d5 = MathHelper.lerp(partialTicks, entityLivingIn.prevPosZ, entityLivingIn.getPosZ()) + d2;
        matrixStackIn.translate(d1, vector3d1.y, d2);
        float f = (float) (vector3d.x - d3);
        float f1 = (float) (vector3d.y - d4);
        float f2 = (float) (vector3d.z - d5);
        float f3 = 0.025F;
        IVertexBuilder ivertexbuilder = bufferIn.getBuffer(RenderType.getLeash());
        Matrix4f matrix4f = matrixStackIn.getLast().getMatrix();
        float f4 = MathHelper.fastInvSqrt(f * f + f2 * f2) * 0.025F / 2.0F;
        float f5 = f2 * f4;
        float f6 = f * f4;
        BlockPos blockpos = new BlockPos(entityLivingIn.getEyePosition(partialTicks));
        BlockPos blockpos1 = new BlockPos(leashHolder.getEyePosition(partialTicks));
        int i = this.getBlockLight(entityLivingIn, blockpos);
        //int j = this.renderManager.getRenderer(leashHolder).getBlockLight(leashHolder, blockpos1); // TODO
        int j = 0;
        int k = entityLivingIn.world.getLightFor(LightType.SKY, blockpos);
        int l = entityLivingIn.world.getLightFor(LightType.SKY, blockpos1);
        renderSide(ivertexbuilder, matrix4f, f, f1, f2, i, j, k, l, 0.025F, 0.025F, f5, f6);
        renderSide(ivertexbuilder, matrix4f, f, f1, f2, i, j, k, l, 0.025F, 0.0F, f5, f6);
        matrixStackIn.pop();
    }

    public static void renderSide(IVertexBuilder bufferIn, Matrix4f matrixIn, float p_229119_2_, float p_229119_3_, float p_229119_4_, int blockLight, int holderBlockLight, int skyLight, int holderSkyLight, float p_229119_9_, float p_229119_10_, float p_229119_11_, float p_229119_12_) {
        int i = 24;

        for(int j = 0; j < 24; ++j) {
            float f = (float)j / 23.0F;
            int k = (int)MathHelper.lerp(f, (float)blockLight, (float)holderBlockLight);
            int l = (int)MathHelper.lerp(f, (float)skyLight, (float)holderSkyLight);
            int i1 = LightTexture.packLight(k, l);
            addVertexPair(bufferIn, matrixIn, i1, p_229119_2_, p_229119_3_, p_229119_4_, p_229119_9_, p_229119_10_, 24, j, false, p_229119_11_, p_229119_12_);
            addVertexPair(bufferIn, matrixIn, i1, p_229119_2_, p_229119_3_, p_229119_4_, p_229119_9_, p_229119_10_, 24, j + 1, true, p_229119_11_, p_229119_12_);
        }
    }

    public static void addVertexPair(IVertexBuilder bufferIn, Matrix4f matrixIn, int packedLight, float p_229120_3_, float p_229120_4_, float p_229120_5_, float p_229120_6_, float p_229120_7_, int p_229120_8_, int p_229120_9_, boolean p_229120_10_, float p_229120_11_, float p_229120_12_) {
        float f = 0.5F;
        float f1 = 0.4F;
        float f2 = 0.3F;
        if (p_229120_9_ % 2 == 0) {
            f *= 0.7F;
            f1 *= 0.7F;
            f2 *= 0.7F;
        }

        float f3 = (float)p_229120_9_ / (float)p_229120_8_;
        float f4 = p_229120_3_ * f3;
        float f5 = p_229120_4_ > 0.0F ? p_229120_4_ * f3 * f3 : p_229120_4_ - p_229120_4_ * (1.0F - f3) * (1.0F - f3);
        float f6 = p_229120_5_ * f3;
        if (!p_229120_10_) {
            bufferIn.pos(matrixIn, f4 + p_229120_11_, f5 + p_229120_6_ - p_229120_7_, f6 - p_229120_12_).color(f, f1, f2, 1.0F).lightmap(packedLight).endVertex();
        }

        bufferIn.pos(matrixIn, f4 - p_229120_11_, f5 + p_229120_7_, f6 + p_229120_12_).color(f, f1, f2, 1.0F).lightmap(packedLight).endVertex();
        if (p_229120_10_) {
            bufferIn.pos(matrixIn, f4 + p_229120_11_, f5 + p_229120_6_ - p_229120_7_, f6 - p_229120_12_).color(f, f1, f2, 1.0F).lightmap(packedLight).endVertex();
        }
    }

    /**
     * Returns the location of an entity's texture.
     */
    @Override
    @Nonnull
    public ResourceLocation getEntityTexture(@Nonnull SlimeEntity entity) {
        return SLIME_TEXTURES;
    }

    private static float getFacingAngle(Direction facingIn) {
        switch(facingIn) {
            case SOUTH:
                return 90.0F;
            case NORTH:
                return 270.0F;
            case EAST:
                return 180.0F;
            default:
                return 0.0F;
        }
    }
}
