package jigglyslimes.model;

import jigglyslimes.math.Vec3D;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.BufferBuilder;
import net.minecraft.client.renderer.Tessellator;
import net.minecraft.client.renderer.vertex.DefaultVertexFormats;
import net.minecraft.util.ResourceLocation;
import org.lwjgl.opengl.GL11;

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
     * @param resolution - a number greater than or equal to 2; higher = smoother-looking mesh
     * @param texture - the texture file location
     * @param texWidth - width of the texture file in pixels
     * @param texHeight - height of the texture file in pixels
     */
    public BoxMesh(Vec3D modelPosLow, Vec3D modelPosHigh, int uOff, int vOff, int dxTex, int dyTex, int dzTex, int resolution, ResourceLocation texture, int texWidth, int texHeight) {
        this.texture = texture;
        Vec3D modelPos0 = new Vec3D(modelPosLow.x, modelPosLow.y, modelPosLow.z);
        Vec3D modelPos1 = new Vec3D(modelPosLow.x, modelPosLow.y, modelPosHigh.z);
        Vec3D modelPos2 = new Vec3D(modelPosLow.x, modelPosHigh.y, modelPosLow.z);
        Vec3D modelPos3 = new Vec3D(modelPosLow.x, modelPosHigh.y, modelPosHigh.z);
        Vec3D modelPos4 = new Vec3D(modelPosHigh.x, modelPosLow.y, modelPosLow.z);
        Vec3D modelPos5 = new Vec3D(modelPosHigh.x, modelPosLow.y, modelPosHigh.z);
        Vec3D modelPos6 = new Vec3D(modelPosHigh.x, modelPosHigh.y, modelPosLow.z);
        Vec3D modelPos7 = new Vec3D(modelPosHigh.x, modelPosHigh.y, modelPosHigh.z);
        leftFace = new QuadMesh(modelPos0, uOff, vOff + dxTex + dyTex, modelPos2, uOff, vOff + dxTex, modelPos3, uOff + dxTex, vOff + dxTex, modelPos1, uOff + dxTex, vOff + dxTex + dyTex, resolution, texture, texWidth, texHeight);
        frontFace = new QuadMesh(modelPos1, uOff + dxTex, vOff + dxTex + dyTex, modelPos3, uOff + dxTex, vOff + dxTex, modelPos7, uOff + dxTex + dzTex, vOff + dxTex, modelPos5, uOff + dxTex + dzTex, vOff + dxTex + dyTex, resolution, texture, texWidth, texHeight);
        rightFace = new QuadMesh(modelPos4, uOff + 2 * dxTex + dzTex, vOff + dxTex + dyTex, modelPos5, uOff + dxTex + dzTex, vOff + dxTex + dyTex, modelPos7, uOff + dxTex + dzTex, vOff + dxTex, modelPos6, uOff + 2 * dxTex + dzTex, vOff + dxTex, resolution, texture, texWidth, texHeight);
        backFace = new QuadMesh(modelPos0, uOff + 2 * dxTex + 2 * dzTex, vOff + dxTex + dyTex, modelPos4, uOff + 2 * dxTex + dzTex, vOff + dxTex + dyTex, modelPos6, uOff + 2 * dxTex + dzTex, vOff + dxTex, modelPos2, uOff + 2 * dxTex + 2 * dzTex, vOff + dxTex, resolution, texture, texWidth, texHeight);
        topFace = new QuadMesh(modelPos2, uOff + dxTex, vOff, modelPos6, uOff + dxTex + dzTex, vOff, modelPos7, uOff + dxTex + dzTex, vOff + dxTex, modelPos3, uOff + dxTex, vOff + dxTex, resolution, texture, texWidth, texHeight);
        bottomFace = new QuadMesh(modelPos0, uOff + dxTex + dzTex, vOff + dxTex, modelPos1, uOff + dxTex + dzTex, vOff, modelPos5, uOff + 2 * dxTex + dzTex, vOff, modelPos4, uOff + 2 * dxTex + dzTex, vOff + dxTex, resolution, texture, texWidth, texHeight);
    }

    @Override
    public void render(Vec3D modelCorner0, Vec3D modelCorner1, Vec3D modelCorner2, Vec3D modelCorner3, Vec3D modelCorner4, Vec3D modelCorner5, Vec3D modelCorner6, Vec3D modelCorner7) {
        leftFace.calculateLerps(modelCorner0, modelCorner1, modelCorner2, modelCorner3, modelCorner4, modelCorner5, modelCorner6, modelCorner7);
        frontFace.calculateLerps(modelCorner0, modelCorner1, modelCorner2, modelCorner3, modelCorner4, modelCorner5, modelCorner6, modelCorner7);
        rightFace.calculateLerps(modelCorner0, modelCorner1, modelCorner2, modelCorner3, modelCorner4, modelCorner5, modelCorner6, modelCorner7);
        backFace.calculateLerps(modelCorner0, modelCorner1, modelCorner2, modelCorner3, modelCorner4, modelCorner5, modelCorner6, modelCorner7);
        topFace.calculateLerps(modelCorner0, modelCorner1, modelCorner2, modelCorner3, modelCorner4, modelCorner5, modelCorner6, modelCorner7);
        bottomFace.calculateLerps(modelCorner0, modelCorner1, modelCorner2, modelCorner3, modelCorner4, modelCorner5, modelCorner6, modelCorner7);

        Minecraft.getMinecraft().getTextureManager().bindTexture(texture);
        Tessellator tessellator = Tessellator.getInstance();
        BufferBuilder bufferBuilder = tessellator.getBuffer();
        bufferBuilder.begin(GL11.GL_TRIANGLES, DefaultVertexFormats.POSITION_TEX_NORMAL);

        addToRenderBuffer(bufferBuilder);

        tessellator.draw();
    }

    /**
     * Adds the triangles that form this mesh to a {@code BufferBuilder}, but doesn't actually draw them.
     * @param bufferBuilder - a {@code BufferBuilder} set to draw in {@code GL11.GL_TRIANGLES} and
     *                        {@code DefaultVertexFormats.POSITION_TEX_NORMAL}
     */
    void addToRenderBuffer(BufferBuilder bufferBuilder) {
        leftFace.addToRenderBuffer(bufferBuilder);
        frontFace.addToRenderBuffer(bufferBuilder);
        rightFace.addToRenderBuffer(bufferBuilder);
        backFace.addToRenderBuffer(bufferBuilder);
        topFace.addToRenderBuffer(bufferBuilder);
        bottomFace.addToRenderBuffer(bufferBuilder);
    }
}
