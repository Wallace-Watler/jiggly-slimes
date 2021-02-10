package jigglyslimes;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.PrintWriter;
import java.util.Scanner;

public final class JSConfig {

    public static int meshResolution = 3;
    private static final int meshResolutionMin = 0;
    private static final int meshResolutionMax = 9;

    private static final Logger LOGGER = LogManager.getLogger();
    private static final String CONFIG_FILE_LOCATION = "./config/" + JigglySlimes.MODID + ".toml";

    public static void loadConfig() {
        final File configFile = new File(CONFIG_FILE_LOCATION);
        try(Scanner sc = new Scanner(configFile)) {
            while(sc.hasNext()) {
                String nextLine = sc.nextLine();
                final int commentBegin = nextLine.indexOf('#');
                if(commentBegin >= 0) nextLine = nextLine.substring(0, commentBegin);
                String[] tokens = nextLine.split("=", 2);
                tokens[0] = tokens[0].trim();
                if(!tokens[0].isEmpty()) {
                    if(!tokens[0].equals("meshResolution")) {
                        LOGGER.warn("Unrecognized config entry '" + tokens[0] + "', skipping over");
                    } else if(tokens.length == 2) {
                        try {
                            meshResolution = Integer.parseInt(tokens[1].trim());
                            if(meshResolution < meshResolutionMin) {
                                LOGGER.warn("Value '" + meshResolution + "' for config value meshResolution is out of range, changing to " + meshResolutionMin);
                                meshResolution = meshResolutionMin;
                            } else if(meshResolution > meshResolutionMax) {
                                LOGGER.warn("Value '" + meshResolution + "' for config value meshResolution is out of range, changing to " + meshResolutionMax);
                                meshResolution = meshResolutionMax;
                            } else {
                                LOGGER.debug("Set meshResolution to '" + meshResolution + "' from config");
                            }
                        } catch(NumberFormatException e) {
                            LOGGER.warn("Could not parse meshResolution config value, defaulting to 3");
                            LOGGER.debug(e);
                        }
                    }
                }
            }
        } catch(FileNotFoundException e) {
            writeConfig(configFile);
        }
    }

    public static void writeConfig(File configFile) {
        try(PrintWriter wr = new PrintWriter(configFile)) {
            wr.println("# Higher numbers produce smoother-looking slimes.");
            wr.println("# int: " + meshResolutionMin + " - " + meshResolutionMax);
            wr.println("meshResolution=" + meshResolution);
            LOGGER.info("Created new config file at " + configFile.getAbsolutePath());
        } catch(FileNotFoundException e) {
            LOGGER.warn("Could not create a new config file at " + configFile.getAbsolutePath());
        }
    }
}
