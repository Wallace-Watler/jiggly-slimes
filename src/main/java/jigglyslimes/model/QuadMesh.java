package jigglyslimes.model;

import jigglyslimes.JigglySlimes;
import jigglyslimes.math.MathUtil;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;
import org.lwjgl.util.vector.Vector3f;

/**
 * A "quadrilateral" having UV texture mappings for each of its four vertices. Unlike a typical quadrilateral, the
 * vertices need not be coplanar; extra vertices are added in between using bilinear interpolation.
 */
public class QuadMesh implements ModelComponent {

    private final int numVertices;
    /** The vertices of this mesh relative to the entity model. */
    private final Vector3f[][] modelPos;
    /** The vertices of this mesh relative to the entity position. */
    private final Vector3f[][] lerpedModelPos;
    private final float[][] u, v;
    private final ResourceLocation texture;

    // Temporary vectors
    private static final Vector3f temp0 = new Vector3f();
    private static final Vector3f temp1 = new Vector3f();
    private static final Vector3f temp2 = new Vector3f();
    private static final Vector3f temp3 = new Vector3f();

    /**
     * Construct a {@code QuadMesh} using four sets of model positions and UV pairs. Model positions should be given
     * in model space. UV coordinates should be between 0 and 1, inclusive.
     * @param maxResolution - higher numbers produce smoother meshes; should be >= 0
     * @param texture - the location of the texture to apply
     */
    public QuadMesh(Vector3f modelPos0, float u0, float v0, Vector3f modelPos1, float u1, float v1, Vector3f modelPos2, float u2, float v2, Vector3f modelPos3, float u3, float v3, int maxResolution, ResourceLocation texture) {
        if(maxResolution < 0) {
            JigglySlimes.LOGGER.warn("Invalid QuadMesh resolution of '" + maxResolution + "' received, changing to 0.");
            maxResolution = 0;
        }

        this.numVertices = (1 << maxResolution) + 1; // # of vertices = 2 to the power of the resolution + 1
        modelPos = new Vector3f[numVertices][numVertices];
        lerpedModelPos = new Vector3f[numVertices][numVertices];
        u = new float[numVertices][numVertices];
        v = new float[numVertices][numVertices];

        for(int j = 0; j < numVertices; j++) {
            for(int i = 0; i < numVertices; i++) {
                float iSlide = (float) i / (numVertices - 1);
                float jSlide = (float) j / (numVertices - 1);
                modelPos[i][j] = MathUtil.lerp(MathUtil.lerp(modelPos1, modelPos0, iSlide, temp0), MathUtil.lerp(modelPos2, modelPos3, iSlide, temp1), jSlide, new Vector3f());
                lerpedModelPos[i][j] = new Vector3f(modelPos[i][j]);
                u[i][j] = (float) MathHelper.clampedLerp(MathHelper.clampedLerp(u1, u0, iSlide), MathHelper.clampedLerp(u2, u3, iSlide), jSlide);
                v[i][j] = (float) MathHelper.clampedLerp(MathHelper.clampedLerp(v1, v0, iSlide), MathHelper.clampedLerp(v2, v3, iSlide), jSlide);
            }
        }

        this.texture = texture;
    }

    /**
     * Construct a {@code QuadMesh} using four sets of model positions and UV pairs. Model positions should be given
     * in model space. UV coordinates are in pixels.
     * @param maxResolution - higher numbers produce smoother meshes; should be >= 0
     * @param texture - the location of the texture to apply
     * @param texWidth - width of the texture in pixels
     * @param texHeight - height of the texture in pixels
     */
    public QuadMesh(Vector3f modelPos0, int u0, int v0, Vector3f modelPos1, int u1, int v1, Vector3f modelPos2, int u2, int v2, Vector3f modelPos3, int u3, int v3, int maxResolution, ResourceLocation texture, int texWidth, int texHeight) {
        this(modelPos0, (float) u0 / texWidth, (float) v0 / texHeight, modelPos1, (float) u1 / texWidth, (float) v1 / texHeight, modelPos2, (float) u2 / texWidth, (float) v2 / texHeight, modelPos3, (float) u3 / texWidth, (float) v3 / texHeight, maxResolution, texture);
    }

    @Override
    public void render(int resReduction, Vector3f[] modelCorners) {
        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);

        addToRenderBuffer(bufferBuilder, resReduction, modelCorners);

        tessellator.draw();
    }

    /**
     * Adds the triangles that form this mesh to a {@code BufferBuilder}, but doesn't actually draw them. This involves
     * calculating the interpolated vertices using the eight corners of the entity's model, relative to the entity position.
     * @param bufferBuilder - a {@code BufferBuilder} set to draw in {@code GL11.GL_TRIANGLES} and {@code DefaultVertexFormats.POSITION_TEX_NORMAL}
     */
    void addToRenderBuffer(BufferBuilder bufferBuilder, int resReduction, Vector3f[] modelCorners) {
        final int skip = Math.min(1 << resReduction, numVertices - 1);

        // Calculate the interpolated vertices using the eight corners of the entity's model, relative to the entity origin.
        for(int j = 0; j < numVertices; j += skip) {
            for(int i = 0; i < numVertices; i += skip) {
                MathUtil.lerp(modelCorners[0], modelCorners[4], modelPos[i][j].x, temp0);
                MathUtil.lerp(modelCorners[2], modelCorners[6], modelPos[i][j].x, temp1);
                MathUtil.lerp(temp0, temp1, modelPos[i][j].y, temp2);
                MathUtil.lerp(modelCorners[1], modelCorners[5], modelPos[i][j].x, temp0);
                MathUtil.lerp(modelCorners[3], modelCorners[7], modelPos[i][j].x, temp1);
                MathUtil.lerp(temp0, temp1, modelPos[i][j].y, temp3);
                MathUtil.lerp(temp2, temp3, modelPos[i][j].z, lerpedModelPos[i][j]);
            }
        }

        // Add to BufferBuilder
        for(int j = 0; j < numVertices - 1; j += skip) {
            for(int i = 0; i < numVertices - 1; i += skip) {
                // Tri 1
                Vector3f lerpedModelPos0 = lerpedModelPos[i + skip][j];
                Vector3f lerpedModelPos1 = lerpedModelPos[i][j];
                Vector3f lerpedModelPos2 = lerpedModelPos[i][j + skip];

                Vector3f.sub(lerpedModelPos0, lerpedModelPos1, temp0);
                Vector3f.sub(lerpedModelPos2, lerpedModelPos1, temp1);
                Vector3f.cross(temp0, temp1, temp0);
                MathUtil.normalize(temp0);

                bufferBuilder.pos(lerpedModelPos0.x, lerpedModelPos0.y, lerpedModelPos0.z).tex(u[i + skip][j], v[i + skip][j]).normal(temp0.x, temp0.y, temp0.z).endVertex();
                bufferBuilder.pos(lerpedModelPos1.x, lerpedModelPos1.y, lerpedModelPos1.z).tex(u[i][j], v[i][j]).normal(temp0.x, temp0.y, temp0.z).endVertex();
                bufferBuilder.pos(lerpedModelPos2.x, lerpedModelPos2.y, lerpedModelPos2.z).tex(u[i][j + skip], v[i][j + skip]).normal(temp0.x, temp0.y, temp0.z).endVertex();

                // Tri 2
                lerpedModelPos1 = lerpedModelPos[i][j + skip];
                lerpedModelPos2 = lerpedModelPos[i + skip][j + skip];

                Vector3f.sub(lerpedModelPos0, lerpedModelPos1, temp0);
                Vector3f.sub(lerpedModelPos2, lerpedModelPos1, temp1);
                Vector3f.cross(temp0, temp1, temp0);
                MathUtil.normalize(temp0);

                bufferBuilder.pos(lerpedModelPos0.x, lerpedModelPos0.y, lerpedModelPos0.z).tex(u[i + skip][j], v[i + skip][j]).normal(temp0.x, temp0.y, temp0.z).endVertex();
                bufferBuilder.pos(lerpedModelPos1.x, lerpedModelPos1.y, lerpedModelPos1.z).tex(u[i][j + skip], v[i][j + skip]).normal(temp0.x, temp0.y, temp0.z).endVertex();
                bufferBuilder.pos(lerpedModelPos2.x, lerpedModelPos2.y, lerpedModelPos2.z).tex(u[i + skip][j + skip], v[i + skip][j + skip]).normal(temp0.x, temp0.y, temp0.z).endVertex();
            }
        }
    }
}
