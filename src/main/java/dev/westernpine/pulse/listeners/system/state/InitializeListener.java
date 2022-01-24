package dev.westernpine.pulse.listeners.system.state;

import dev.westernpine.eventapi.objects.EventHandler;
import dev.westernpine.eventapi.objects.Listener;
import dev.westernpine.lib.object.State;
import dev.westernpine.pulse.Pulse;
import dev.westernpine.pulse.controller.ControllerFactory;
import dev.westernpine.pulse.events.system.StateChangeEvent;
import dev.westernpine.pulse.logging.LogManager;
import dev.westernpine.pulse.properties.IdentityProperties;
import dev.westernpine.pulse.properties.SqlProperties;
import dev.westernpine.pulse.properties.SystemProperties;

import java.io.File;
import java.io.IOException;
import java.util.Date;
import java.util.logging.Level;

import static dev.westernpine.pulse.logging.Logger.logger;

public class InitializeListener implements Listener {

    // The STARTUP state is set in the main method.
    @EventHandler
    public void onInitialization(StateChangeEvent event) {
        if (!event.getNewState().is(State.INITIALIZATION))
            return;

        try {
            /*
            Load up system properties.
             */
            System.out.println(State.INITIALIZATION.getName() + " >> Loading system properties.");
            Pulse.systemProperties = new SystemProperties();

            /*
            Load up identity properties.
             */
            System.out.println(State.INITIALIZATION.getName() + " >> Loading " + Pulse.systemProperties.get(SystemProperties.IDENTITY) + " identity properties.");
            Pulse.identityProperties = new IdentityProperties(Pulse.systemProperties.get(SystemProperties.IDENTITY));

            /*
            Load up logging sql properties.
             */
            System.out.println(State.INITIALIZATION.getName() + " >> Loading " + Pulse.identityProperties.get(IdentityProperties.LOGGING_SQL_BACKEND) + " sql properties.");
            Pulse.loggingSqlProperties = new SqlProperties(Pulse.identityProperties.get(IdentityProperties.LOGGING_SQL_BACKEND));

            System.out.println(State.INITIALIZATION.getName() + " >> Initializing logger.");
            File file = new File(new File(Pulse.class.getProtectionDomain().getCodeSource().getLocation().getPath()).getParentFile().toString() + File.separator + "logs");
            if (!file.exists() && !file.mkdirs()) throw new IOException("Unable to create logs folder.");
            file = new File(file.getPath(), Pulse.simpleDateFormatter.format(new Date()) + ".log");
            LogManager.initialize(
                    record -> "[%s] %s: %s%s".formatted(Pulse.dateFormatter.format(new Date()), record.getLevel(), Pulse.getState().isLoggable() ? "" + Pulse.getState().getName() + " >>" : "", record.getMessage()),
                    Pulse.systemProperties.get(SystemProperties.IDENTITY),
                    file,
                    Pulse.loggingSqlProperties.toSql(),
                    LogManager.compileLevelsFromMinimum(Level.FINE.intValue(), LogManager.levels),
                    LogManager.compileLevelsFromMinimum(Level.ALL.intValue(), LogManager.allLevels),
                    LogManager.compileLevelsFromMinimum(Level.INFO.intValue(), LogManager.allLevels));

            System.out.println(State.INITIALIZATION.getName() + " >> Switching to logger.");
            logger.fine("Logger Setup: ");
            logger.fine(" - Identity: " + LogManager.getIdentity());
            logger.fine(" - Logging to console: " + LogManager.isConsoleLogging());
            if (LogManager.isConsoleLogging())
                logger.fine(" - Logged Console Levels: " + String.join(", ", LogManager.getConsoleLevels().stream().map(Level::toString).toList()));
            logger.fine(" - Logging to file: " + LogManager.isFileLogging());
            if (LogManager.isFileLogging())
                logger.fine(" - Logged File Levels: " + String.join(", ", LogManager.getFileLevels().stream().map(Level::toString).toList()));
            logger.fine(" - Logging to SQL: " + LogManager.isSqlLogging());
            if (LogManager.isSqlLogging())
                logger.fine(" - Logged Sql Levels: " + String.join(", ", LogManager.getSqlLevels().stream().map(Level::toString).toList()));

            logger.info("Initializing backend controllers.");
            ControllerFactory.initializeBackend();
        } catch (Throwable e) {
            e.printStackTrace();
            System.out.println("Unable to initialize the application.");
            Pulse.setState(State.SHUTDOWN);
        }

    }

}
