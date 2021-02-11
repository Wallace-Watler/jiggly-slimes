package jigglyslimes.config;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.annotation.Nonnull;
import java.io.PrintWriter;
import java.util.function.Function;

/**
 * A configuration setting consisting of a name and a value.
 * @param <T> - the type of value stored
 */
public interface Config<T> extends Comparable<Config> {

    String getName();
    T getValue();
    void parse(String token);
    void write(PrintWriter wr);

    @Override
    default int compareTo(@Nonnull Config o) {
        return this.getName().compareTo(o.getName());
    }

    class Range<T extends Comparable<T>, U extends Range<T, U>> implements Config<T> {
        private static final Logger LOGGER = LogManager.getLogger();

        private final String name;
        private String comment = "";
        private T value;
        private final T defaultValue;
        private T min;
        private T max;
        private final Function<String, T> parseFunc;

        public Range(String name, T defaultValue, T min, T max, Function<String, T> parseFunc) {
            this.name = name;
            this.value = defaultValue;
            this.defaultValue = defaultValue;
            this.min = min;
            this.max = max;
            this.parseFunc = parseFunc;
        }

        @Override
        public String getName() {
            return name;
        }

        @Override
        public T getValue() {
            return value;
        }

        @Override
        public void parse(String token) {
            try {
                value = parseFunc.apply(token);
                if(value.compareTo(min) < 0) {
                    LOGGER.warn("Value '" + value + "' for config '" + name + "' is out of range, changing to the min value '" + min + "'");
                    value = min;
                } else if(value.compareTo(max) > 0) {
                    LOGGER.warn("Value '" + max + "' for config '" + name + "' is out of range, changing to the max value '" + max + "'");
                    value = max;
                } else {
                    LOGGER.debug("Set '" + name + "' to '" + value + "' loaded from config");
                }
            } catch(NumberFormatException e) {
                LOGGER.warn("Could not parse '" + name + "' config value, defaulting to " + defaultValue);
                LOGGER.debug(e);
            }
        }

        @Override
        public void write(PrintWriter wr) {
            if(!comment.isEmpty()) {
                wr.println("# " + comment);
            }
            wr.println("# Default: " + defaultValue);
            wr.println("# Min: " + min);
            wr.println("# Max: " + max);
            wr.println(name + "=" + value);
        }

        public U setMin(T min) {
            this.min = min;
            return (U) this;
        }

        public U setMax(T max) {
            this.max = max;
            return (U) this;
        }

        public U setComment(String comment) {
            this.comment = comment;
            return (U) this;
        }

        public static class Int extends Range<Integer, Int> {
            public Int(String name, int defaultValue) {
                super(name, defaultValue, Integer.MIN_VALUE, Integer.MAX_VALUE, Integer::parseInt);
            }
        }

        public static class Double extends Range<java.lang.Double, Double> {
            public Double(String name, double defaultValue) {
                super(name, defaultValue, -java.lang.Double.MAX_VALUE, java.lang.Double.MAX_VALUE, java.lang.Double::parseDouble);
            }
        }
    }
}
