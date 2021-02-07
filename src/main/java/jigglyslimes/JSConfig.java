package jigglyslimes;

import net.minecraftforge.common.config.Config;

@Config(modid = JigglySlimes.MODID)
public class JSConfig {

    @Config.Comment("Higher numbers produce smoother-looking slimes.")
    @Config.RangeInt(min = 0, max = 9)
    public static int meshResolution = 4;
}
