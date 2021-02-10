package jigglyslimes;

import jigglyslimes.math.MathUtil;
import jigglyslimes.model.BoxMesh;
import net.minecraft.client.Minecraft;
import net.minecraft.client.model.ModelSlime;
import net.minecraft.client.renderer.GlStateManager;
import net.minecraft.client.renderer.OpenGlHelper;
import net.minecraft.client.renderer.entity.RenderLiving;
import net.minecraft.client.renderer.entity.RenderManager;
import net.minecraft.entity.monster.EntitySlime;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import net.minecraftforge.client.event.RenderLivingEvent;
import net.minecraftforge.common.MinecraftForge;
import net.minecraftforge.fml.common.Mod;
import net.minecraftforge.fml.relauncher.Side;
import net.minecraftforge.fml.relauncher.SideOnly;
import org.lwjgl.util.vector.Vector3f;

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
    private static final Vector3f[] lerpedJigglyBits = new Vector3f[8];

    static {
        createModelComponents();
        for(int i = 0; i < 8; i++) lerpedJigglyBits[i] = new Vector3f();
    }

    public static void createModelComponents() {
        INNER_BODY = new BoxMesh(new Vector3f(0.125F, 0.125F, 0.125F), new Vector3f(0.875F, 0.875F, 0.875F), 0, 16, 6, 6, 6, JSConfig.meshResolution, SLIME_TEXTURES, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        RIGHT_EYE = new BoxMesh(new Vector3f(0.09375F, 0.5F, 0.6875F), new Vector3f(0.34375F, 0.75F, 0.9375F), 32, 0, 2, 2, 2, Math.max(JSConfig.meshResolution - 2, 0), SLIME_TEXTURES, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        LEFT_EYE = new BoxMesh(new Vector3f(0.65625F, 0.5F, 0.6875F), new Vector3f(0.90625F, 0.75F, 0.9375F), 32, 4, 2, 2, 2, Math.max(JSConfig.meshResolution - 2, 0), SLIME_TEXTURES, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        MOUTH = new BoxMesh(new Vector3f(0.5F, 0.25F, 0.8125F), new Vector3f(0.625F, 0.375F, 0.9375F), 32, 8, 1, 1, 1, Math.max(JSConfig.meshResolution - 3, 0), SLIME_TEXTURES, TEXTURE_WIDTH, TEXTURE_HEIGHT);
        OUTER_BODY = new BoxMesh(new Vector3f(0.0F, 0.0F, 0.0F), new Vector3f(1.0F, 1.0F, 1.0F), 0, 0, 8, 8, 8, JSConfig.meshResolution, SLIME_TEXTURES, TEXTURE_WIDTH, TEXTURE_HEIGHT);
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
                MathUtil.lerp(jigglyBits.prevPos[i], jigglyBits.pos[i], partialTicks, lerpedJigglyBits[i]);
            }

            // resReduction = max(log2(distance) - 4, 0)
            final int resReduction = Math.max((MathHelper.log2((int) (x * x + y * y + z * z)) >> 1) - 4, 0);

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
                            renderOpaqueModelComponents(resReduction, lerpedJigglyBits);
                            if(flag2) GlStateManager.disableBlendProfile(GlStateManager.Profile.TRANSPARENT_MODEL);
                        }
                    }

                    boolean flag2 = setBrightness(entity, partialTicks, true);
                    if(!entity.isInvisible()) {
                        GlStateManager.color(1.0F, 1.0F, 1.0F, 1.0F);
                        GlStateManager.enableNormalize();
                        GlStateManager.enableBlend();
                        GlStateManager.blendFunc(GlStateManager.SourceFactor.SRC_ALPHA, GlStateManager.DestFactor.ONE_MINUS_SRC_ALPHA);
                        renderTranslucentModelComponents(resReduction, lerpedJigglyBits);
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
                        renderOpaqueModelComponents(resReduction, lerpedJigglyBits);
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
                        renderTranslucentModelComponents(resReduction, lerpedJigglyBits);
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

    private void renderOpaqueModelComponents(int resReduction, Vector3f[] modelCorners) {
        INNER_BODY.render(resReduction, modelCorners);
        RIGHT_EYE.render(resReduction, modelCorners);
        LEFT_EYE.render(resReduction, modelCorners);
        MOUTH.render(resReduction, modelCorners);
    }

    private void renderTranslucentModelComponents(int resReduction, Vector3f[] modelCorners) {
        OUTER_BODY.render(resReduction, modelCorners);
    }
}
