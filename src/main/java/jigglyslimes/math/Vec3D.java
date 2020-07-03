package jigglyslimes.math;

public class Vec3D {

    public double x, y, z;

    public Vec3D(double x, double y, double z) {
        this.x = x;
        this.y = y;
        this.z = z;
    }

    public Vec3D() {
        this(0.0, 0.0, 0.0);
    }

    public Vec3D copy(Vec3D dest) {
        dest.x = this.x;
        dest.y = this.y;
        dest.z = this.z;
        return dest;
    }

    public double lengthSqr() {
        return x * x + y * y + z * z;
    }

    public double length() {
        return Math.sqrt(lengthSqr());
    }

    public Vec3D add(double x, double y, double z) {
        this.x += x;
        this.y += y;
        this.z += z;
        return this;
    }

    public Vec3D add(Vec3D other, Vec3D result) {
        result.x = this.x + other.x;
        result.y = this.y + other.y;
        result.z = this.z + other.z;
        return result;
    }

    public Vec3D add(Vec3D other) {
        return add(other, this);
    }

    public Vec3D subtract(double x, double y, double z) {
        this.x -= x;
        this.y -= y;
        this.z -= z;
        return this;
    }

    public Vec3D subtract(Vec3D other, Vec3D result) {
        result.x = this.x - other.x;
        result.y = this.y - other.y;
        result.z = this.z - other.z;
        return result;
    }

    public Vec3D subtract(Vec3D other) {
        return subtract(other, this);
    }

    public Vec3D scale(double factor, Vec3D result) {
        result.x = this.x * factor;
        result.y = this.y * factor;
        result.z = this.z * factor;
        return result;
    }

    public Vec3D scale(double factor) {
        return scale(factor, this);
    }

    public Vec3D crossProduct(Vec3D other, Vec3D result) {
        double x = this.y * other.z - this.z * other.y;
        double y = this.z * other.x - this.x * other.z;
        double z = this.x * other.y - this.y * other.x;
        result.x = x;
        result.y = y;
        result.z = z;
        return result;
    }

    public Vec3D normalize(Vec3D result) {
        double lengthSqr = lengthSqr();
        if(lengthSqr != 0) {
            double length = Math.sqrt(lengthSqr);
            result.x = this.x / length;
            result.y = this.y / length;
            result.z = this.z / length;
        } else if(result != this) {
            result.x = 0.0;
            result.y = 0.0;
            result.z = 0.0;
        }
        return result;
    }

    public Vec3D normalize() {
        return normalize(this);
    }

    public static Vec3D lerp(Vec3D lower, Vec3D upper, double slide, Vec3D result) {
        upper.subtract(lower, result).scale(slide).add(lower);
        return result;
    }

    @Override
    public boolean equals(Object obj) {
        if(obj instanceof Vec3D) {
            Vec3D other = (Vec3D) obj;
            return this.x == other.x && this.y == other.y && this.z == other.z;
        }
        return false;
    }
}
