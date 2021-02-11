package jigglyslimes;

import jigglyslimes.config.Config;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;
import java.util.SortedSet;
import java.util.TreeSet;

public final class JSConfig {

    public static Config.Range.Int meshResolution = new Config.Range.Int("meshResolution", 3).setMin(0).setMax(9).setComment("Higher numbers produce smoother-looking slimes.");

    public static final Slime slime = new Slime();
    private static class Slime {
        public Config.Range.Double collisionFriction = new Config.Range.Double("slime.collisionFriction", 0.5).setMin(0.0).setMax(1.0);
        public Config.Range.Double density = new Config.Range.Double("slime.density", 1200.0).setMin(0.0);
        public Config.Range.Double internalFriction = new Config.Range.Double("slime.internalFriction", 0.055).setMin(0.0);
        public Config.Range.Double rigidity = new Config.Range.Double("slime.rigidity", 30.0).setMin(0.0);
    }

    private static final File CONFIG_FILE = new File("./config/" + JigglySlimes.MODID + ".toml");
    private static final SortedSet<Config> CONFIGS = new TreeSet<>();
    private static final Logger LOGGER = LogManager.getLogger();

    static {
        CONFIGS.add(meshResolution);
        CONFIGS.add(slime.collisionFriction);
        CONFIGS.add(slime.density);
        CONFIGS.add(slime.internalFriction);
        CONFIGS.add(slime.rigidity);
    }

    public static void loadConfig() {
        try(Scanner sc = new Scanner(CONFIG_FILE)) {
            while(sc.hasNextLine()) {
                String nextLine = sc.nextLine();
                final int commentBegin = nextLine.indexOf('#');
                if(commentBegin >= 0) {
                    nextLine = nextLine.substring(0, commentBegin);
                }
                final String[] tokens = nextLine.split("=", 2);
                tokens[0] = tokens[0].trim();

                if(!tokens[0].isEmpty() && tokens.length > 1) {
                    tokens[1] = tokens[1].trim();
                    boolean matched = false;
                    for(Config config : CONFIGS) {
                        if(tokens[0].equals(config.getName())) {
                            config.parse(tokens[1]);
                            matched = true;
                            break;
                        }
                    }
                    if(!matched) {
                        LOGGER.warn("Unrecognized config entry '" + tokens[0] + "', skipping over");
                    }
                }
            }
        } catch(FileNotFoundException e) {
            writeConfig();
        }
    }

    public static void writeConfig() {
        try(PrintWriter wr = new PrintWriter(CONFIG_FILE)) {
            for(Config configOption : CONFIGS) {
                configOption.write(wr);
                wr.println();
            }
            LOGGER.info("Created new config file at " + CONFIG_FILE.getAbsolutePath());
        } catch(FileNotFoundException e) {
            LOGGER.warn("Could not create a new config file at " + CONFIG_FILE.getAbsolutePath());
        }
    }
}
