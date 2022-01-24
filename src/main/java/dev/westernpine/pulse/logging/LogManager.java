package dev.westernpine.pulse.logging;

import dev.westernpine.bettertry.Try;
import dev.westernpine.lib.object.SQL;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Handler;
import java.util.logging.Level;
import java.util.logging.LogRecord;

import static dev.westernpine.pulse.logging.Logger.logger;
import static java.util.logging.Level.*;

public class LogManager extends Handler {

    public static final List<Level> allLevels = List.of(OFF, SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST, ALL);
    public static final List<Level> levels = List.of(SEVERE, WARNING, INFO, CONFIG, FINE, FINER, FINEST, ALL);

    private static final String tableName = "logs";

    public static LogManager instance;
    private static String identity;
    private static Function<LogRecord, String> formatter;
    private static boolean consoleLogging = false;
    private static boolean fileLogging = false;
    private static PrintWriter fileWriter;
    private static boolean sqlLogging = false;
    private static Timer timer;
    private static SQL sql;
    private static Collection<Level> consoleLevels;
    private static Collection<Level> fileLevels;
    private static Collection<Level> sqlLevels;
    private static LinkedList<LogRecord> logs;

    private LogManager() {
        if (sqlLogging) {
            timer.cancel();
            LinkedList<LogRecord> finalLogs = new LinkedList<>(logs);
            logs = null;
            Try.of(() -> compileAndUpdate(finalLogs));
            sql.getConnection().close();
        }
        if (fileLogging) {
            Try.of(() -> fileWriter.close());
            fileWriter = null;
        }
    }

    public static void initialize(@Nonnull Function<LogRecord, String> logFormatter,
                                  @Nonnull String identifier,
                                  @Nullable File logFile,
                                  @Nullable SQL sqlConsole,
                                  @Nullable Collection<Level> consoleLogLevels,
                                  @Nullable Collection<Level> fileLogLevels,
                                  @Nullable Collection<Level> sqlLogLevels) throws FileNotFoundException {
        if (instance != null)
            throw new RuntimeException("The log manager is already initialized!");
        formatter = logFormatter;
        logger.setUseParentHandlers(false);
        instance = new LogManager();
        identity = identifier;
        logger.addHandler(instance);
        if (consoleLogLevels != null && !consoleLogLevels.isEmpty()) {
            consoleLogging = true;
            consoleLevels = consoleLogLevels;
        }
        if (logFile != null && fileLogLevels != null && !fileLogLevels.isEmpty()) {
            fileLogging = true;
            fileWriter = new PrintWriter(new FileOutputStream(logFile));
            fileLevels = fileLogLevels;
        }
        if (sqlConsole != null && sqlConsole.canConnect() && sqlLogLevels != null && !sqlLogLevels.isEmpty()) {
            sqlLogging = true;
            timer = new Timer("SQL Logger Timer");
            sql = sqlConsole;
            sqlLevels = sqlLogLevels;
            logs = new LinkedList<>();
            sql.getConnection().open();
            sql.update("CREATE TABLE IF NOT EXISTS `%s` (`datetime` DATETIME NOT NULL DEFAULT now(), `identity` VARCHAR(255) NOT NULL, `severity` VARCHAR(10) NOT NULL, `log` LONGTEXT NOT NULL);".formatted(tableName));
            timer.schedule(new TimerTask() {
                @Override
                public void run() {
                    if (Try.of(() -> compileAndUpdate(logs)).orElse(false))
                        logs.clear();
                }
            }, 5000L, 5000L);
        }

    }

    public static String getIdentity() {
        return identity;
    }

    public static boolean isConsoleLogging() {
        return consoleLogging;
    }

    public static boolean isFileLogging() {
        return fileLogging;
    }

    public static boolean isSqlLogging() {
        return sqlLogging;
    }

    public static Collection<Level> getConsoleLevels() {
        return consoleLevels;
    }

    public static Collection<Level> getFileLevels() {
        return fileLevels;
    }

    public static Collection<Level> getSqlLevels() {
        return sqlLevels;
    }

    public static Collection<Level> compileLevelsFromMinimum(int intValue, Collection<Level> levels) {
        return new ArrayList<>(levels).stream().filter(level -> level.intValue() >= intValue).toList();
    }

    public static Collection<Level> compileLevelsFromMaximum(int intValue, Collection<Level> levels) {
        return new ArrayList<>(levels).stream().filter(level -> level.intValue() <= intValue).toList();
    }

    private static boolean compileAndUpdate(LinkedList<LogRecord> logs) throws SQLException {
        if (!sqlLogging || sql == null || Try.of(() -> sql.getConnection().getConnection().isClosed()).orElse(true) || logs == null || logs.isEmpty())
            return false;
        String statement = "INSERT INTO `%s` VALUES(now(),?,?,?);".formatted(tableName);
        logs.forEach(log -> sql.update(statement, identity, log.getLevel().toString(), log.getMessage()));
        return true;
    }

    @Override
    public void publish(LogRecord record) {
        record.setMessage(formatter.apply(record));
        if (consoleLogging) {
            if (consoleLevels.contains(record.getLevel())) {
                System.out.println(record.getMessage());
            }
        }
        if (fileLogging) {
            if (fileWriter != null && fileLevels.contains(record.getLevel())) {
                fileWriter.write(record.getMessage() + "\n");
                fileWriter.flush();
            }
        }
        if (sqlLogging) {
            if (logs != null && sqlLevels.contains(record.getLevel())) {
                logs.add(record);
            }
        }
    }

    @Override
    public void flush() {
    }

    @Override
    public void close() throws SecurityException {
        if (sqlLogging) {
            timer.cancel();
            LinkedList<LogRecord> finalLogs = new LinkedList<>(logs);
            logs = null;
            Try.of(() -> compileAndUpdate(finalLogs));
            sql.getConnection().close();
        }
        if (fileLogging) {
            Try.of(() -> fileWriter.close());
            fileWriter = null;
        }
    }
}
