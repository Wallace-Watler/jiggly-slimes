package jigglyslimes;

import net.minecraft.client.renderer.RenderState;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.client.renderer.vertex.VertexFormat;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

/**
 * A {@code RenderType} defines how a class of objects will be rendered.
 */
public class JSRenderType extends RenderType {

    public static RenderType getEntityTranslucentTris(ResourceLocation textureLocation, boolean outline) {
        return RenderType.makeType("entity_translucent_tris", DefaultVertexFormats.ENTITY, GL11.GL_TRIANGLES, 256, true, false,
                RenderType.State.getBuilder()
                        .texture(new RenderState.TextureState(textureLocation, false, false))
                        .transparency(TRANSLUCENT_TRANSPARENCY)
                        .diffuseLighting(DIFFUSE_LIGHTING_ENABLED)
                        .alpha(DEFAULT_ALPHA)
                        .cull(CULL_DISABLED)
                        .lightmap(LIGHTMAP_ENABLED)
                        .overlay(OVERLAY_ENABLED)
                        .build(outline)
        );
    }

    public static RenderType getEntityTranslucentTris(ResourceLocation textureLocation) {
        return getEntityTranslucentTris(textureLocation, true);
    }

    public static RenderType getEntityCutoutNoCullTris(ResourceLocation textureLocation, boolean outline) {
        return makeType("entity_cutout_no_cull", DefaultVertexFormats.ENTITY, GL11.GL_TRIANGLES, 256, true, false,
                RenderType.State.getBuilder()
                        .texture(new RenderState.TextureState(textureLocation, false, false))
                        .transparency(NO_TRANSPARENCY)
                        .diffuseLighting(DIFFUSE_LIGHTING_ENABLED)
                        .alpha(DEFAULT_ALPHA)
                        .cull(CULL_DISABLED)
                        .lightmap(LIGHTMAP_ENABLED)
                        .overlay(OVERLAY_ENABLED)
                        .build(outline)
        );
    }

    public static RenderType getEntityCutoutNoCullTris(ResourceLocation textureLocation) {
        return getEntityCutoutNoCullTris(textureLocation, true);
    }

    @SuppressWarnings("unused")
    public JSRenderType(String name, VertexFormat format, int drawMode, int bufferSize, boolean useDelegate, boolean needsSorting, Runnable setupTask, Runnable clearTask) {
        super(name, format, drawMode, bufferSize, useDelegate, needsSorting, setupTask, clearTask);
    }
}
