package backtrace.io;

import backtrace.io.data.BacktraceReport;
import backtrace.io.log4j2.AppenderMock;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.Assert;
import org.junit.Test;


public class BacktraceReportTest {
    private final static String LOGGING_LEVEL_ATTRIBUTE_NAME = "log_level";
    private final String message = "TEST";

    @Test
    public void creatingBacktraceReportFromString() {
        // GIVEN
        final Level level = Level.INFO;
        Logger testLogger = (org.apache.logging.log4j.core.Logger) LogManager.getLogger();
        final LogEvent logEvent = Log4jLogEvent.newBuilder()
                .setLevel(level)
                .setLoggerName(testLogger.getName())
                .setMessage(new SimpleMessage(message))
                .build();

        // WHEN
        BacktraceReport report = AppenderMock.createBacktraceReport(logEvent);

        // THEN
        Assert.assertEquals(message, report.getMessage());
        Assert.assertTrue(report.getAttributes().containsKey(LOGGING_LEVEL_ATTRIBUTE_NAME));
    }


    @Test
    public void creatingBacktraceReportFromException() {
        // GIVEN
        final Exception exception = new Exception(message);
        Logger testLogger = (org.apache.logging.log4j.core.Logger) LogManager.getLogger();
        final Level level = Level.ERROR;
        final LogEvent logEvent = Log4jLogEvent.newBuilder()
                .setLevel(level)
                .setLoggerName(testLogger.getName())
                .setThrown(exception)
                .build();

        // WHEN
        BacktraceReport report = AppenderMock.createBacktraceReport(logEvent);

        // THEN
        Assert.assertEquals(message, report.getMessage());
        Assert.assertEquals(exception, report.getException());
        Assert.assertTrue(report.getAttributes().containsKey(LOGGING_LEVEL_ATTRIBUTE_NAME));
    }
}
