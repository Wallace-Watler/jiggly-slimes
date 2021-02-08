package jigglyslimes.model;

import com.mojang.blaze3d.matrix.MatrixStack;
import jigglyslimes.math.Vec3D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.IRenderTypeBuffer;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

import javax.annotation.Nonnull;

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
    private final ResourceLocation texture;

    /**
     * @param modelPosLow - the lower corner position within the model
     * @param modelPosHigh - the upper corner position within the model
     * @param uOff - the u-coordinate of the top-left corner of the box's texture in the file
     * @param vOff - the v-coordinate of the top-left corner of the box's texture in the file
     * @param dxTex - the number of texels to apply to the x-dimension
     * @param dyTex - the number of texels to apply to the y-dimension
     * @param dzTex - the number of texels to apply to the z-dimension
     * @param maxResolution - higher numbers produce smoother meshes; should be >= 0
     * @param texture - the texture file location
     * @param texWidth - width of the texture file in pixels
     * @param texHeight - height of the texture file in pixels
     */
    public BoxMesh(Vec3D modelPosLow, Vec3D modelPosHigh, int uOff, int vOff, int dxTex, int dyTex, int dzTex, int maxResolution, ResourceLocation texture, int texWidth, int texHeight) {
        this.texture = texture;
        Vec3D modelPos0 = new Vec3D(modelPosLow.x, modelPosLow.y, modelPosLow.z);
        Vec3D modelPos1 = new Vec3D(modelPosLow.x, modelPosLow.y, modelPosHigh.z);
        Vec3D modelPos2 = new Vec3D(modelPosLow.x, modelPosHigh.y, modelPosLow.z);
        Vec3D modelPos3 = new Vec3D(modelPosLow.x, modelPosHigh.y, modelPosHigh.z);
        Vec3D modelPos4 = new Vec3D(modelPosHigh.x, modelPosLow.y, modelPosLow.z);
        Vec3D modelPos5 = new Vec3D(modelPosHigh.x, modelPosLow.y, modelPosHigh.z);
        Vec3D modelPos6 = new Vec3D(modelPosHigh.x, modelPosHigh.y, modelPosLow.z);
        Vec3D modelPos7 = new Vec3D(modelPosHigh.x, modelPosHigh.y, modelPosHigh.z);
        leftFace = new QuadMesh(modelPos0, uOff, vOff + dxTex + dyTex, modelPos2, uOff, vOff + dxTex, modelPos3, uOff + dxTex, vOff + dxTex, modelPos1, uOff + dxTex, vOff + dxTex + dyTex, maxResolution, texture, texWidth, texHeight);
        frontFace = new QuadMesh(modelPos1, uOff + dxTex, vOff + dxTex + dyTex, modelPos3, uOff + dxTex, vOff + dxTex, modelPos7, uOff + dxTex + dzTex, vOff + dxTex, modelPos5, uOff + dxTex + dzTex, vOff + dxTex + dyTex, maxResolution, texture, texWidth, texHeight);
        rightFace = new QuadMesh(modelPos4, uOff + 2 * dxTex + dzTex, vOff + dxTex + dyTex, modelPos5, uOff + dxTex + dzTex, vOff + dxTex + dyTex, modelPos7, uOff + dxTex + dzTex, vOff + dxTex, modelPos6, uOff + 2 * dxTex + dzTex, vOff + dxTex, maxResolution, texture, texWidth, texHeight);
        backFace = new QuadMesh(modelPos0, uOff + 2 * dxTex + 2 * dzTex, vOff + dxTex + dyTex, modelPos4, uOff + 2 * dxTex + dzTex, vOff + dxTex + dyTex, modelPos6, uOff + 2 * dxTex + dzTex, vOff + dxTex, modelPos2, uOff + 2 * dxTex + 2 * dzTex, vOff + dxTex, maxResolution, texture, texWidth, texHeight);
        topFace = new QuadMesh(modelPos2, uOff + dxTex, vOff, modelPos6, uOff + dxTex + dzTex, vOff, modelPos7, uOff + dxTex + dzTex, vOff + dxTex, modelPos3, uOff + dxTex, vOff + dxTex, maxResolution, texture, texWidth, texHeight);
        bottomFace = new QuadMesh(modelPos0, uOff + dxTex + dzTex, vOff + dxTex, modelPos1, uOff + dxTex + dzTex, vOff, modelPos5, uOff + 2 * dxTex + dzTex, vOff, modelPos4, uOff + 2 * dxTex + dzTex, vOff + dxTex, maxResolution, texture, texWidth, texHeight);
    }

    @Override
    public void render(int resReduction, Vec3D[] modelCorners) {
        Minecraft.getInstance().getTextureManager().bindTexture(texture);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        //bufferBuilder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);

        addToRenderBuffer(bufferBuilder, resReduction, modelCorners);

        //tessellator.draw();
    }

    /**
     * Adds the triangles that form this mesh to a {@code BufferBuilder}, but doesn't actually draw them.
     * @param bufferBuilder - a {@code BufferBuilder} set to draw in {@code GL11.GL_TRIANGLES} and
     *                        {@code DefaultVertexFormats.POSITION_TEX_NORMAL}
     */
    void addToRenderBuffer(BufferBuilder bufferBuilder, int resReduction, Vec3D[] modelCorners) {
        leftFace.addToRenderBuffer(bufferBuilder, resReduction, modelCorners);
        frontFace.addToRenderBuffer(bufferBuilder, resReduction, modelCorners);
        rightFace.addToRenderBuffer(bufferBuilder, resReduction, modelCorners);
        backFace.addToRenderBuffer(bufferBuilder, resReduction, modelCorners);
        topFace.addToRenderBuffer(bufferBuilder, resReduction, modelCorners);
        bottomFace.addToRenderBuffer(bufferBuilder, resReduction, modelCorners);
    }
}
