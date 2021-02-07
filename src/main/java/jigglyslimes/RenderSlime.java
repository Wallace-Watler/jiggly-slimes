package jigglyslimes;

import jigglyslimes.math.Vec3D;
import jigglyslimes.model.BoxMesh;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelSlime;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.util.ResourceLocation;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;

import javax.annotation.Nonnull;

@SideOnly(Side.CLIENT)
@Mod.EventBusSubscriber(Side.CLIENT)
public class RenderSlime extends RenderLiving<EntitySlime> {

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
        INNER_BODY = new BoxMesh(new Vec3D(0.125, 0.125, 0.125), new Vec3D(0.875, 0.875, 0.875), 0, 16, 6, 6, 6, (JSConfig.meshResolution * 3 + 3) / 4, SLIME_TEXTURES, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        RIGHT_EYE = new BoxMesh(new Vec3D(0.09375, 0.5, 0.6875), new Vec3D(0.34375, 0.75, 0.9375), 32, 0, 2, 2, 2, (JSConfig.meshResolution + 3) / 4, SLIME_TEXTURES, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        LEFT_EYE = new BoxMesh(new Vec3D(0.65625, 0.5, 0.6875), new Vec3D(0.90625, 0.75, 0.9375), 32, 4, 2, 2, 2, (JSConfig.meshResolution + 3) / 4, SLIME_TEXTURES, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        MOUTH = new BoxMesh(new Vec3D(0.5, 0.25, 0.8125), new Vec3D(0.625, 0.375, 0.9375), 32, 8, 1, 1, 1, (JSConfig.meshResolution + 7) / 8, SLIME_TEXTURES, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        OUTER_BODY = new BoxMesh(new Vec3D(0.0, 0.0, 0.0), new Vec3D(1.0, 1.0, 1.0), 0, 0, 8, 8, 8, JSConfig.meshResolution, SLIME_TEXTURES, TEXTURE_WIDTH, TEXTURE_HEIGHT);
    }

    public RenderSlime(RenderManager renderManager) {
        super(renderManager, new ModelSlime(16), 0.25F);
    }

    @Override
    protected ResourceLocation getEntityTexture(@Nonnull EntitySlime entity) {
        return SLIME_TEXTURES;
    }

    @Override
    public void doRender(@Nonnull EntitySlime entity, double x, double y, double z, float entityYaw, float partialTicks) {
        shadowSize = 0.25F * entity.getSlimeSize();

        if(!MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Pre<>(entity, this, partialTicks, x, y, z))) {
            if(!EventHandler.JB_MAP.containsKey(entity)) EventHandler.JB_MAP.put(entity, new SlimeJigglyBits());
            SlimeJigglyBits jigglyBits = EventHandler.JB_MAP.get(entity);

            for(int i = 0; i < 8; i++) {
                Vec3D.lerp(jigglyBits.prevPos[i], jigglyBits.pos[i], partialTicks, lerpedJigglyBits[i]);
            }

            GlStateManager.pushMatrix();
            GlStateManager.disableCull();

            try {
                GlStateManager.translate((float) x, (float) y, (float) z);
                GlStateManager.enableRescaleNormal();
                GlStateManager.enableAlpha();

                if(renderOutlines) {
                    boolean flag1 = setScoreTeamColor(entity);
                    GlStateManager.enableColorMaterial();
                    GlStateManager.enableOutlineMode(getTeamColor(entity));

                    if(!renderMarker) {
                        boolean visible = isVisible(entity);
                        boolean flag2 = !visible && !entity.isInvisibleToPlayer(Minecraft.getMinecraft().player);
                        if(visible || flag2) {
                            if(!bindEntityTexture(entity)) return;
                            if(flag2) GlStateManager.enableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
                            renderOpaqueModelComponents(lerpedJigglyBits[0], lerpedJigglyBits[1], lerpedJigglyBits[2], lerpedJigglyBits[3], lerpedJigglyBits[4], lerpedJigglyBits[5], lerpedJigglyBits[6], lerpedJigglyBits[7]);
                            if(flag2) GlStateManager.disableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
                        }
                    }

                    boolean flag2 = setBrightness(entity, partialTicks, true);
                    if(!entity.isInvisible()) {
                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                        GlStateManager.enableNormalize();
                        GlStateManager.enableBlend();
                        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                        renderTranslucentModelComponents(lerpedJigglyBits[0], lerpedJigglyBits[1], lerpedJigglyBits[2], lerpedJigglyBits[3], lerpedJigglyBits[4], lerpedJigglyBits[5], lerpedJigglyBits[6], lerpedJigglyBits[7]);
                        GlStateManager.disableBlend();
                        GlStateManager.disableNormalize();
                    }
                    if(flag2) unsetBrightness();

                    GlStateManager.disableOutlineMode();
                    GlStateManager.disableColorMaterial();

                    if(flag1) unsetScoreTeamColor();
                } else {
                    boolean flag = setDoRenderBrightness(entity, partialTicks);
                    boolean visible = isVisible(entity);
                    boolean flag2 = !visible && !entity.isInvisibleToPlayer(Minecraft.getMinecraft().player);
                    if(visible || flag2) {
                        if(!bindEntityTexture(entity)) return;
                        if(flag2) GlStateManager.enableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
                        renderOpaqueModelComponents(lerpedJigglyBits[0], lerpedJigglyBits[1], lerpedJigglyBits[2], lerpedJigglyBits[3], lerpedJigglyBits[4], lerpedJigglyBits[5], lerpedJigglyBits[6], lerpedJigglyBits[7]);
                        if(flag2) GlStateManager.disableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
                    }

                    if(flag) unsetBrightness();
                    GlStateManager.depthMask(true);

                    boolean flag1 = setBrightness(entity, partialTicks, true);
                    if(!entity.isInvisible()) {
                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                        GlStateManager.enableNormalize();
                        GlStateManager.enableBlend();
                        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                        renderTranslucentModelComponents(lerpedJigglyBits[0], lerpedJigglyBits[1], lerpedJigglyBits[2], lerpedJigglyBits[3], lerpedJigglyBits[4], lerpedJigglyBits[5], lerpedJigglyBits[6], lerpedJigglyBits[7]);
                        GlStateManager.disableBlend();
                        GlStateManager.disableNormalize();
                    }
                    if(flag1) unsetBrightness();
                }

                GlStateManager.disableRescaleNormal();
            } catch(Exception exception) {
                JigglySlimes.LOGGER.error("Couldn't render entity", exception);
            }

            GlStateManager.setActiveTexture(OpenGlHelper.lightmapTexUnit);
            GlStateManager.enableTexture2D();
            GlStateManager.setActiveTexture(OpenGlHelper.defaultTexUnit);
            GlStateManager.enableCull();
            GlStateManager.popMatrix();
            if(!renderOutlines) renderName(entity, x, y, z);

            MinecraftForge.EVENT_BUS.post(new RenderLivingEvent.Post<>(entity, this, partialTicks, x, y, z));
        }

        if(!renderOutlines) renderLeash(entity, x, y, z, entityYaw, partialTicks);
    }

    private void renderOpaqueModelComponents(Vec3D modelCorner0, Vec3D modelCorner1, Vec3D modelCorner2, Vec3D modelCorner3, Vec3D modelCorner4, Vec3D modelCorner5, Vec3D modelCorner6, Vec3D modelCorner7) {
        INNER_BODY.render(modelCorner0, modelCorner1, modelCorner2, modelCorner3, modelCorner4, modelCorner5, modelCorner6, modelCorner7);
        RIGHT_EYE.render(modelCorner0, modelCorner1, modelCorner2, modelCorner3, modelCorner4, modelCorner5, modelCorner6, modelCorner7);
        LEFT_EYE.render(modelCorner0, modelCorner1, modelCorner2, modelCorner3, modelCorner4, modelCorner5, modelCorner6, modelCorner7);
        MOUTH.render(modelCorner0, modelCorner1, modelCorner2, modelCorner3, modelCorner4, modelCorner5, modelCorner6, modelCorner7);
    }

    private void renderTranslucentModelComponents(Vec3D modelCorner0, Vec3D modelCorner1, Vec3D modelCorner2, Vec3D modelCorner3, Vec3D modelCorner4, Vec3D modelCorner5, Vec3D modelCorner6, Vec3D modelCorner7) {
        OUTER_BODY.render(modelCorner0, modelCorner1, modelCorner2, modelCorner3, modelCorner4, modelCorner5, modelCorner6, modelCorner7);
    }
}