package jigglyslimes.math;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3f;

public final class MathUtil {

    public static Vector3f lerp(Vector3f lower, Vector3f upper, float slide, Vector3f result) {
        result.set(lower.getX(), lower.getY(), lower.getZ());
        result.lerp(upper, slide);
        return result;
    }

    public static Vector3f add(Vector3f a, double x, double y, double z) {
        a.add((float) x, (float) y, (float) z);
        return a;
    }

    public static Vector3f sub(Vector3f a, Vector3f b, Vector3f result) {
        result.set(a.getX(), a.getY(), a.getZ());
        result.sub(b);
        return result;
    }

    public static Vector3f sub(Vector3f a, double x, double y, double z) {
        a.add((float) -x, (float) -y, (float) -z);
        return a;
    }

    public static float length(Vector3f v) {
        return MathHelper.sqrt(v.dot(v));
    }
}
