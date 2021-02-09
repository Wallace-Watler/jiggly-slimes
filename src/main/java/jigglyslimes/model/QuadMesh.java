package jigglyslimes.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import jigglyslimes.math.MathUtil;
import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Matrix3f;
import net.minecraft.util.math.vector.Matrix4f;
import net.minecraft.util.math.vector.Vector3f;
import net.minecraft.util.math.vector.Vector4f;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

/**
 * A "quadrilateral" having UV texture mappings for each of its four vertices. Unlike a typical quadrilateral, the
 * vertices need not be coplanar; extra vertices are added in between using bilinear interpolation.
 */
public class QuadMesh implements ModelComponent {

    private static final Logger LOGGER = LogManager.getLogger();

    private final int numVertices;
    /** The vertices of this mesh relative to the entity model. */
    private final Vector3f[][] modelPos;
    /** The vertices of this mesh relative to the entity position. */
    private final Vector3f[][] lerpedModelPos;
    private final float[][] u, v;

    // Temporary vectors
    private static final Vector3f temp0 = new Vector3f();
    private static final Vector3f temp1 = new Vector3f();
    private static final Vector3f temp2 = new Vector3f();
    private static final Vector3f temp3 = new Vector3f();
    private static final Vector4f temp4 = new Vector4f();

    /**
     * Construct a {@code QuadMesh} using four sets of model positions and UV pairs. Model positions should be given
     * in model space. UV coordinates should be between 0 and 1, inclusive.
     * @param maxResolution - higher numbers produce smoother meshes; should be >= 0
     */
    public QuadMesh(Vector3f modelPos0, float u0, float v0, Vector3f modelPos1, float u1, float v1, Vector3f modelPos2, float u2, float v2, Vector3f modelPos3, float u3, float v3, int maxResolution) {
        if(maxResolution < 0) {
            LOGGER.warn("Invalid QuadMesh resolution of '" + maxResolution + "' received, changing to 0.");
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
                lerpedModelPos[i][j] = modelPos[i][j].copy();
                u[i][j] = (float) MathHelper.clampedLerp(MathHelper.clampedLerp(u1, u0, iSlide), MathHelper.clampedLerp(u2, u3, iSlide), jSlide);
                v[i][j] = (float) MathHelper.clampedLerp(MathHelper.clampedLerp(v1, v0, iSlide), MathHelper.clampedLerp(v2, v3, iSlide), jSlide);
            }
        }
    }

    /**
     * Construct a {@code QuadMesh} using four sets of model positions and UV pairs. Model positions should be given
     * in model space. UV coordinates are in pixels.
     * @param maxResolution - higher numbers produce smoother meshes; should be >= 0
     * @param texWidth - width of the texture in pixels
     * @param texHeight - height of the texture in pixels
     */
    public QuadMesh(Vector3f modelPos0, int u0, int v0, Vector3f modelPos1, int u1, int v1, Vector3f modelPos2, int u2, int v2, Vector3f modelPos3, int u3, int v3, int maxResolution, int texWidth, int texHeight) {
        this(modelPos0, (float) u0 / texWidth, (float) v0 / texHeight, modelPos1, (float) u1 / texWidth, (float) v1 / texHeight, modelPos2, (float) u2 / texWidth, (float) v2 / texHeight, modelPos3, (float) u3 / texWidth, (float) v3 / texHeight, maxResolution);
    }

    /**
     * Adds the triangles that form this mesh to an {@code IVertexBuilder}, so they can be drawn. This involves
     * calculating the interpolated vertices using the eight corners of the entity's model, relative to the entity
     * position.
     * @param vertexBuilder - the {@code IVertexBuilder} to append to
     */
    @Override
    public void render(MatrixStack.Entry lastMatrixEntry, IVertexBuilder vertexBuilder, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha, int resReduction, Vector3f[] modelCorners) {
        // TODO: Figure this out
        final Matrix4f matrix4f = lastMatrixEntry.getMatrix();
        final Matrix3f matrix3f = lastMatrixEntry.getNormal();
        /*
        Vector3f vector3f = modelrenderer$texturedquad.normal.copy();
        vector3f.transform(matrix3f);
        float f = vector3f.getX();
        float f1 = vector3f.getY();
        float f2 = vector3f.getZ();

        for(int i = 0; i < 4; ++i) {
            ModelRenderer.PositionTextureVertex modelrenderer$positiontexturevertex = modelrenderer$texturedquad.vertexPositions[i];
            float f3 = modelrenderer$positiontexturevertex.position.getX() / 16.0F;
            float f4 = modelrenderer$positiontexturevertex.position.getY() / 16.0F;
            float f5 = modelrenderer$positiontexturevertex.position.getZ() / 16.0F;
            Vector4f vector4f = new Vector4f(f3, f4, f5, 1.0F);
            vector4f.transform(matrix4f);
            vertexBuilder.addVertex(vector4f.getX(), vector4f.getY(), vector4f.getZ(), red, green, blue, alpha, modelrenderer$positiontexturevertex.textureU, modelrenderer$positiontexturevertex.textureV, packedOverlayIn, packedLightIn, f, f1, f2);
        }*/

        ////////

        final int skip = Math.min(1 << resReduction, numVertices - 1);

        // Calculate the interpolated vertices using the eight corners of the entity's model, relative to the entity origin.
        for(int j = 0; j < numVertices; j += skip) {
            for(int i = 0; i < numVertices; i += skip) {
                MathUtil.lerp(modelCorners[0], modelCorners[4], modelPos[i][j].getX(), temp0);
                MathUtil.lerp(modelCorners[2], modelCorners[6], modelPos[i][j].getX(), temp1);
                MathUtil.lerp(temp0, temp1, modelPos[i][j].getY(), temp2);
                MathUtil.lerp(modelCorners[1], modelCorners[5], modelPos[i][j].getX(), temp0);
                MathUtil.lerp(modelCorners[3], modelCorners[7], modelPos[i][j].getX(), temp1);
                MathUtil.lerp(temp0, temp1, modelPos[i][j].getY(), temp3);
                MathUtil.lerp(temp2, temp3, modelPos[i][j].getZ(), lerpedModelPos[i][j]);
            }
        }

        // Add to BufferBuilder
        for(int j = 0; j < numVertices - 1; j += skip) {
            for(int i = 0; i < numVertices - 1; i += skip) {
                // Tri 1
                Vector3f lerpedModelPos0 = lerpedModelPos[i + skip][j].copy();
                Vector3f lerpedModelPos1 = lerpedModelPos[i][j].copy();
                Vector3f lerpedModelPos2 = lerpedModelPos[i][j + skip].copy();

                MathUtil.sub(lerpedModelPos0, lerpedModelPos1, temp0);
                MathUtil.sub(lerpedModelPos2, lerpedModelPos1, temp1);
                temp0.cross(temp1);
                temp0.normalize();
                temp0.set(0.0F, 1.0F, 0.0F); // TODO: Temp code
                temp0.transform(matrix3f);

                transform(lerpedModelPos0, matrix4f);
                transform(lerpedModelPos1, matrix4f);
                transform(lerpedModelPos2, matrix4f);

                vertexBuilder.addVertex(lerpedModelPos0.getX(), lerpedModelPos0.getY(), lerpedModelPos0.getZ(), red, green, blue, alpha, u[i + skip][j], v[i + skip][j], packedOverlayIn, packedLightIn, temp0.getX(), temp0.getY(), temp0.getZ());
                vertexBuilder.addVertex(lerpedModelPos1.getX(), lerpedModelPos1.getY(), lerpedModelPos1.getZ(), red, green, blue, alpha, u[i][j], v[i][j], packedOverlayIn, packedLightIn, temp0.getX(), temp0.getY(), temp0.getZ());
                vertexBuilder.addVertex(lerpedModelPos2.getX(), lerpedModelPos2.getY(), lerpedModelPos2.getZ(), red, green, blue, alpha, u[i][j + skip], v[i][j + skip], packedOverlayIn, packedLightIn, temp0.getX(), temp0.getY(), temp0.getZ());

                // Tri 2
                lerpedModelPos1 = lerpedModelPos[i][j + skip].copy();
                lerpedModelPos2 = lerpedModelPos[i + skip][j + skip].copy();

                MathUtil.sub(lerpedModelPos0, lerpedModelPos1, temp0);
                MathUtil.sub(lerpedModelPos2, lerpedModelPos1, temp1);
                temp0.cross(temp1);
                temp0.normalize();
                temp0.set(0.0F, 1.0F, 0.0F); // TODO: Temp code
                temp0.transform(matrix3f);

                transform(lerpedModelPos1, matrix4f);
                transform(lerpedModelPos2, matrix4f);

                vertexBuilder.addVertex(lerpedModelPos0.getX(), lerpedModelPos0.getY(), lerpedModelPos0.getZ(), red, green, blue, alpha, u[i + skip][j], v[i + skip][j], packedOverlayIn, packedLightIn, temp0.getX(), temp0.getY(), temp0.getZ());
                vertexBuilder.addVertex(lerpedModelPos1.getX(), lerpedModelPos1.getY(), lerpedModelPos1.getZ(), red, green, blue, alpha, u[i][j + skip], v[i][j + skip], packedOverlayIn, packedLightIn, temp0.getX(), temp0.getY(), temp0.getZ());
                vertexBuilder.addVertex(lerpedModelPos2.getX(), lerpedModelPos2.getY(), lerpedModelPos2.getZ(), red, green, blue, alpha, u[i + skip][j + skip], v[i + skip][j + skip], packedOverlayIn, packedLightIn, temp0.getX(), temp0.getY(), temp0.getZ());
            }
        }
    }

    private static void transform(Vector3f v3f, Matrix4f m4f) {
        temp4.set(v3f.getX(), v3f.getY(), v3f.getZ(), 1.0F);
        temp4.transform(m4f);
        v3f.set(temp4.getX(), temp4.getY(), temp4.getZ());
    }
}
