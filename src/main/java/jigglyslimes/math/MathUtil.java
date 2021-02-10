package jigglyslimes.math;

import net.minecraft.util.math.MathHelper;
import org.lwjgl.util.vector.Vector3f;

public final class MathUtil {

    public static Vector3f lerp(Vector3f lower, Vector3f upper, float slide, Vector3f result) {
        Vector3f.sub(upper, lower, result).scale(slide);
        return Vector3f.add(result, lower, result);
    }

    public static Vector3f normalize(Vector3f v) {
        float lengthSqr = v.lengthSquared();
        if(lengthSqr != 0.0F) {
            v.scale((float) MathHelper.fastInvSqrt(lengthSqr));
        } else {
            v.set(0.0F, 0.0F, 0.0F);
        }
        return v;
    }
}
