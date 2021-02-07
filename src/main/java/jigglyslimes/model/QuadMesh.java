package jigglyslimes.model;

import jigglyslimes.JigglySlimes;
import jigglyslimes.math.Vec3D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import net.minecraft.util.math.MathHelper;
import org.lwjgl.opengl.GL11;

/**
 * A "quadrilateral" having UV texture mappings for each of its four vertices. Unlike a typical quadrilateral, the
 * vertices need not be coplanar; extra vertices are added in between using bilinear interpolation.
 */
public class QuadMesh implements ModelComponent {

    private final int resolution;
    private final Vec3D[][] modelPos;
    private final Vec3D[][] lerpedModelPos;
    private final float[][] u, v;
    private final ResourceLocation texture;

    // Temporary vectors
    private static final Vec3D temp0 = new Vec3D();
    private static final Vec3D temp1 = new Vec3D();
    private static final Vec3D temp2 = new Vec3D();
    private static final Vec3D temp3 = new Vec3D();

    /**
     * Construct a {@code QuadMesh} using four sets of model positions and UV pairs. Model positions should be given
     * in model space. UV coordinates should be between 0 and 1, inclusive.
     * @param resolution - the number of "quads per edge"; higher numbers produce smoother meshes; should be >= 1
     * @param texture - the location of the texture to apply
     */
    public QuadMesh(Vec3D modelPos0, float u0, float v0, Vec3D modelPos1, float u1, float v1, Vec3D modelPos2, float u2, float v2, Vec3D modelPos3, float u3, float v3, int resolution, ResourceLocation texture) {
        if(resolution < 1) {
            JigglySlimes.LOGGER.warn("Invalid QuadMesh resolution of '" + resolution + "' received, changing to 1.");
            resolution = 1;
        }
        resolution++;

        this.resolution = resolution;
        modelPos = new Vec3D[resolution][resolution];
        lerpedModelPos = new Vec3D[resolution][resolution];
        u = new float[resolution][resolution];
        v = new float[resolution][resolution];

        for(int j = 0; j < resolution; j++) {
            for(int i = 0; i < resolution; i++) {
                double iSlide = (double) i / (resolution - 1);
                double jSlide = (double) j / (resolution - 1);
                modelPos[i][j] = Vec3D.lerp(Vec3D.lerp(modelPos1, modelPos0, iSlide, temp0), Vec3D.lerp(modelPos2, modelPos3, iSlide, temp1), jSlide, new Vec3D());
                lerpedModelPos[i][j] = modelPos[i][j].copy(new Vec3D());
                u[i][j] = (float) MathHelper.clampedLerp(MathHelper.clampedLerp(u1, u0, iSlide), MathHelper.clampedLerp(u2, u3, iSlide), jSlide);
                v[i][j] = (float) MathHelper.clampedLerp(MathHelper.clampedLerp(v1, v0, iSlide), MathHelper.clampedLerp(v2, v3, iSlide), jSlide);
            }
        }

        this.texture = texture;
    }

    /**
     * Construct a {@code QuadMesh} using four sets of model positions and UV pairs. Model positions should be given
     * in model space. UV coordinates are in pixels.
     * @param resolution - the number of vertices per "edge"; higher numbers produce smoother meshes; should be >= 2
     * @param texture - the location of the texture to apply
     * @param texWidth - width of the texture in pixels
     * @param texHeight - height of the texture in pixels
     */
    public QuadMesh(Vec3D modelPos0, int u0, int v0, Vec3D modelPos1, int u1, int v1, Vec3D modelPos2, int u2, int v2, Vec3D modelPos3, int u3, int v3, int resolution, ResourceLocation texture, int texWidth, int texHeight) {
        this(modelPos0, (float) u0 / texWidth, (float) v0 / texHeight, modelPos1, (float) u1 / texWidth, (float) v1 / texHeight, modelPos2, (float) u2 / texWidth, (float) v2 / texHeight, modelPos3, (float) u3 / texWidth, (float) v3 / texHeight, resolution, texture);
    }

    @Override
    public void render(Vec3D modelCorner0, Vec3D modelCorner1, Vec3D modelCorner2, Vec3D modelCorner3, Vec3D modelCorner4, Vec3D modelCorner5, Vec3D modelCorner6, Vec3D modelCorner7) {
        calculateLerps(modelCorner0, modelCorner1, modelCorner2, modelCorner3, modelCorner4, modelCorner5, modelCorner6, modelCorner7);

        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);

        addToRenderBuffer(bufferBuilder);

        tessellator.draw();
    }

    /**
     * Calculate and update the interpolated vertices using the eight corners of the entity's model, relative to the
     * entity origin.
     */
    void calculateLerps(Vec3D modelCorner0, Vec3D modelCorner1, Vec3D modelCorner2, Vec3D modelCorner3, Vec3D modelCorner4, Vec3D modelCorner5, Vec3D modelCorner6, Vec3D modelCorner7) {
        for(int j = 0; j < resolution; j++) {
            for(int i = 0; i < resolution; i++) {
                Vec3D.lerp(modelCorner0, modelCorner4, modelPos[i][j].x, temp0);
                Vec3D.lerp(modelCorner2, modelCorner6, modelPos[i][j].x, temp1);
                Vec3D.lerp(temp0, temp1, modelPos[i][j].y, temp2);
                Vec3D.lerp(modelCorner1, modelCorner5, modelPos[i][j].x, temp0);
                Vec3D.lerp(modelCorner3, modelCorner7, modelPos[i][j].x, temp1);
                Vec3D.lerp(temp0, temp1, modelPos[i][j].y, temp3);
                Vec3D.lerp(temp2, temp3, modelPos[i][j].z, lerpedModelPos[i][j]);
            }
        }
    }

    /**
     * Adds the triangles that form this mesh to a {@code BufferBuilder}, but doesn't actually draw them.
     * @param bufferBuilder - a {@code BufferBuilder} set to draw in {@code GL11.GL_TRIANGLES} and
     *                        {@code DefaultVertexFormats.POSITION_TEX_NORMAL}
     */
    void addToRenderBuffer(BufferBuilder bufferBuilder) {
        // TODO: Calculate lerps in here instead of in render()?

        for(int j = 0; j < resolution - 1; j++) {
            for(int i = 0; i < resolution - 1; i++) {
                // Tri 1
                Vec3D lerpedModelPos0 = lerpedModelPos[i + 1][j];
                Vec3D lerpedModelPos1 = lerpedModelPos[i][j];
                Vec3D lerpedModelPos2 = lerpedModelPos[i][j + 1];

                lerpedModelPos0.subtract(lerpedModelPos1, temp0);
                lerpedModelPos2.subtract(lerpedModelPos1, temp1);
                temp0.crossProduct(temp1, temp0).normalize();

                bufferBuilder.pos(lerpedModelPos0.x, lerpedModelPos0.y, lerpedModelPos0.z).tex(u[i + 1][j], v[i + 1][j]).normal((float) temp0.x, (float) temp0.y, (float) temp0.z).endVertex();
                bufferBuilder.pos(lerpedModelPos1.x, lerpedModelPos1.y, lerpedModelPos1.z).tex(u[i][j], v[i][j]).normal((float) temp0.x, (float) temp0.y, (float) temp0.z).endVertex();
                bufferBuilder.pos(lerpedModelPos2.x, lerpedModelPos2.y, lerpedModelPos2.z).tex(u[i][j + 1], v[i][j + 1]).normal((float) temp0.x, (float) temp0.y, (float) temp0.z).endVertex();

                // Tri 2
                lerpedModelPos1 = lerpedModelPos[i][j + 1];
                lerpedModelPos2 = lerpedModelPos[i + 1][j + 1];

                lerpedModelPos0.subtract(lerpedModelPos1, temp0);
                lerpedModelPos2.subtract(lerpedModelPos1, temp1);
                temp0.crossProduct(temp1, temp0).normalize();

                bufferBuilder.pos(lerpedModelPos0.x, lerpedModelPos0.y, lerpedModelPos0.z).tex(u[i + 1][j], v[i + 1][j]).normal((float) temp0.x, (float) temp0.y, (float) temp0.z).endVertex();
                bufferBuilder.pos(lerpedModelPos1.x, lerpedModelPos1.y, lerpedModelPos1.z).tex(u[i][j + 1], v[i][j + 1]).normal((float) temp0.x, (float) temp0.y, (float) temp0.z).endVertex();
                bufferBuilder.pos(lerpedModelPos2.x, lerpedModelPos2.y, lerpedModelPos2.z).tex(u[i + 1][j + 1], v[i + 1][j + 1]).normal((float) temp0.x, (float) temp0.y, (float) temp0.z).endVertex();
            }
        }
    }
}
