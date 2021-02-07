package jigglyslimes.model;

import jigglyslimes.math.Vec3D;

/**
 * A shape that is part of an entity's model, such as a {@code QuadMesh} or a {@code BoxMesh}. The overall model is
 * assumed to be a rectangular prism, as it is in vanilla Minecraft, but can be rendered "warped" as the entity moves
 * and interacts with its environment.
 */
public interface ModelComponent {

    /**
     * Renders this component given the eight corners of the entity's model, relative to the entity origin. The corners
     * need not form a rectangular prism.
     */
    void render(int resReduction, Vec3D modelCorner0, Vec3D modelCorner1, Vec3D modelCorner2, Vec3D modelCorner3, Vec3D modelCorner4, Vec3D modelCorner5, Vec3D modelCorner6, Vec3D modelCorner7);
}
