package jigglyslimes;

import net.minecraftforge.common.config.Config;

@Config(modid = JigglySlimes.MODID)
public class JSConfig {

    @Config.Comment("Higher numbers produce smoother-looking slimes.")
    @Config.RangeInt(min = 1, max = 1000)
    public static int meshResolution = 8;
}
