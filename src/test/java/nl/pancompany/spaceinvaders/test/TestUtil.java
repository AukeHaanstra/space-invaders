package nl.pancompany.spaceinvaders.test;

import nl.pancompany.eventstore.EventBus;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LoggerContext;
import org.apache.logging.log4j.core.config.Configuration;
import org.apache.logging.log4j.core.config.LoggerConfig;


public class TestUtil {

    private TestUtil() {
    }

    public static void withoutLogging(Runnable runnable) {
        setLogLevel(EventBus.class.getName(), Level.OFF);
        runnable.run();
        setLogLevel(EventBus.class.getName(), Level.INFO);
    }

    public static void setLogLevel(String loggerName, Level level) {
        LoggerContext context = (LoggerContext) LogManager.getContext(false);
        Configuration config = context.getConfiguration();
        LoggerConfig loggerConfig = config.getLoggerConfig(loggerName);
        loggerConfig.setLevel(level);
        context.updateLoggers();
    }
}
