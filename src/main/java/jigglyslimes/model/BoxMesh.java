package jigglyslimes.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import com.mojang.blaze3d.vertex.IVertexBuilder;
import net.minecraft.util.math.vector.Vector3f;

/**
 * A "rectangular prism" mesh to be rendered as part of a model. Each face is a {@code QuadMesh}. The model faces the
 * +Z axis. Assumes the textured faces are arranged a la vanilla Minecraft.
 */
public class BoxMesh implements ModelComponent {

    private final QuadMesh leftFace; // +x
    private final QuadMesh frontFace; // +z
    private final QuadMesh rightFace; // -x
    private final QuadMesh backFace; // -z
    private final QuadMesh topFace; // +y
    private final QuadMesh bottomFace; // -y

    /**
     * @param modelPosLow - the lower corner position within the model
     * @param modelPosHigh - the upper corner position within the model
     * @param uOff - the u-coordinate of the top-left corner of the box's texture in the file
     * @param vOff - the v-coordinate of the top-left corner of the box's texture in the file
     * @param dxTex - the number of texels to apply to the x-dimension
     * @param dyTex - the number of texels to apply to the y-dimension
     * @param dzTex - the number of texels to apply to the z-dimension
     * @param maxResolution - higher numbers produce smoother meshes; should be >= 0
     * @param texWidth - width of the texture file in pixels
     * @param texHeight - height of the texture file in pixels
     */
    public BoxMesh(Vector3f modelPosLow, Vector3f modelPosHigh, int uOff, int vOff, int dxTex, int dyTex, int dzTex, int maxResolution, int texWidth, int texHeight) {
        Vector3f modelPos0 = new Vector3f(modelPosLow.getX(), modelPosLow.getY(), modelPosLow.getZ());
        Vector3f modelPos1 = new Vector3f(modelPosLow.getX(), modelPosLow.getY(), modelPosHigh.getZ());
        Vector3f modelPos2 = new Vector3f(modelPosLow.getX(), modelPosHigh.getY(), modelPosLow.getZ());
        Vector3f modelPos3 = new Vector3f(modelPosLow.getX(), modelPosHigh.getY(), modelPosHigh.getZ());
        Vector3f modelPos4 = new Vector3f(modelPosHigh.getX(), modelPosLow.getY(), modelPosLow.getZ());
        Vector3f modelPos5 = new Vector3f(modelPosHigh.getX(), modelPosLow.getY(), modelPosHigh.getZ());
        Vector3f modelPos6 = new Vector3f(modelPosHigh.getX(), modelPosHigh.getY(), modelPosLow.getZ());
        Vector3f modelPos7 = new Vector3f(modelPosHigh.getX(), modelPosHigh.getY(), modelPosHigh.getZ());
        leftFace = new QuadMesh(modelPos0, uOff, vOff + dxTex + dyTex, modelPos2, uOff, vOff + dxTex, modelPos3, uOff + dxTex, vOff + dxTex, modelPos1, uOff + dxTex, vOff + dxTex + dyTex, maxResolution, texWidth, texHeight);
        frontFace = new QuadMesh(modelPos1, uOff + dxTex, vOff + dxTex + dyTex, modelPos3, uOff + dxTex, vOff + dxTex, modelPos7, uOff + dxTex + dzTex, vOff + dxTex, modelPos5, uOff + dxTex + dzTex, vOff + dxTex + dyTex, maxResolution, texWidth, texHeight);
        rightFace = new QuadMesh(modelPos4, uOff + 2 * dxTex + dzTex, vOff + dxTex + dyTex, modelPos5, uOff + dxTex + dzTex, vOff + dxTex + dyTex, modelPos7, uOff + dxTex + dzTex, vOff + dxTex, modelPos6, uOff + 2 * dxTex + dzTex, vOff + dxTex, maxResolution, texWidth, texHeight);
        backFace = new QuadMesh(modelPos0, uOff + 2 * dxTex + 2 * dzTex, vOff + dxTex + dyTex, modelPos4, uOff + 2 * dxTex + dzTex, vOff + dxTex + dyTex, modelPos6, uOff + 2 * dxTex + dzTex, vOff + dxTex, modelPos2, uOff + 2 * dxTex + 2 * dzTex, vOff + dxTex, maxResolution, texWidth, texHeight);
        topFace = new QuadMesh(modelPos2, uOff + dxTex, vOff, modelPos6, uOff + dxTex + dzTex, vOff, modelPos7, uOff + dxTex + dzTex, vOff + dxTex, modelPos3, uOff + dxTex, vOff + dxTex, maxResolution, texWidth, texHeight);
        bottomFace = new QuadMesh(modelPos0, uOff + dxTex + dzTex, vOff + dxTex, modelPos1, uOff + dxTex + dzTex, vOff, modelPos5, uOff + 2 * dxTex + dzTex, vOff, modelPos4, uOff + 2 * dxTex + dzTex, vOff + dxTex, maxResolution, texWidth, texHeight);
    }

    @Override
    public void render(MatrixStack.Entry lastMatrixEntry, IVertexBuilder vertexBuilder, int packedLightIn, int packedOverlayIn, float red, float green, float blue, float alpha, int resReduction, Vector3f[] modelCorners) {
        leftFace.render(lastMatrixEntry, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, alpha, resReduction, modelCorners);
        frontFace.render(lastMatrixEntry, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, alpha, resReduction, modelCorners);
        rightFace.render(lastMatrixEntry, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, alpha, resReduction, modelCorners);
        backFace.render(lastMatrixEntry, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, alpha, resReduction, modelCorners);
        topFace.render(lastMatrixEntry, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, alpha, resReduction, modelCorners);
        bottomFace.render(lastMatrixEntry, vertexBuilder, packedLightIn, packedOverlayIn, red, green, blue, alpha, resReduction, modelCorners);
    }
}
