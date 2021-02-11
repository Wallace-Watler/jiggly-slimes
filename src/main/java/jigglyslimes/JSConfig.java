package jigglyslimes;

import net.minecraftforge.common.config.Config;

@Config(modid = JigglySlimes.MODID)
public class JSConfig {

    @Config.Comment("Higher numbers produce smoother-looking slimes.")
    @Config.RangeInt(min = 0, max = 9)
    public static int meshResolution = 4;

    @Config.Comment("Physics properties of slimes. Change with caution; setting these values very low or very high can produce weird effects.")
    public static final Slime slime = new Slime();
    public static class Slime {
        @Config.RangeDouble(min = 0.0, max = 1.0)
        public double collisionFriction = 0.5;

        @Config.Comment("In kg/m^3. The density of water is 1000.")
        @Config.RangeDouble(min = 0.001)
        public double density = 1200.0;

        @Config.RangeDouble(min = 0.0)
        public double internalFriction = 0.055;

        @Config.RangeDouble(min = 0.0)
        public double rigidity = 30.0;
    }
}
