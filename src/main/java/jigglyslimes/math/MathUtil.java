package jigglyslimes.math;

import net.minecraft.util.math.MathHelper;
import net.minecraft.util.math.vector.Vector3d;
import net.minecraft.util.math.vector.Vector3f;

public final class MathUtil {

    public static void set(Vector3f dest, Vector3d src) {
        dest.set((float) src.getX(), (float) src.getY(), (float) src.getZ());
    }

    public static Vector3f lerp(Vector3f lower, Vector3f upper, float slide, Vector3f result) {
        result.set(lower.getX(), lower.getY(), lower.getZ());
        result.lerp(upper, slide);
        return result;
    }

    public static void add(Vector3f a, Vector3d b) {
        a.add((float) b.getX(), (float) b.getY(), (float) b.getZ());
    }

    public static void add(Vector3f a, double x, double y, double z) {
        a.add((float) x, (float) y, (float) z);
    }

    public static void sub(Vector3f a, Vector3f b, Vector3f result) {
        result.set(a.getX(), a.getY(), a.getZ());
        result.sub(b);
    }

    public static void sub(Vector3f a, Vector3d b) {
        a.add((float) -b.getX(), (float) -b.getY(), (float) -b.getZ());
    }

    public static void sub(Vector3f a, double x, double y, double z) {
        a.add((float) -x, (float) -y, (float) -z);
    }

    public static float length(Vector3f v) {
        return MathHelper.sqrt(v.dot(v));
    }
}
