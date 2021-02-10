package jigglyslimes.model;

import org.lwjgl.util.vector.Vector3f;

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
    void render(int resReduction, Vector3f[] modelCorners);
}
