package backtrace.io;

import backtrace.io.data.BacktraceReport;
import backtrace.io.log4j12.Appender;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Assert;
import org.junit.Test;

public class BacktraceReportTest {
    private final static String LOGGING_LEVEL_ATTRIBUTE_NAME = "log_level";
    private final String message = "TEST";

    @Test
    public void creatingBacktraceReportFromString() {
        // GIVEN
        final Level level = Level.INFO;
        final Logger testLogger = new Logger(null) {
        };
        LoggingEvent loggingEvent = new LoggingEvent(null, testLogger, level, message, null);

        // WHEN
        BacktraceReport report = Appender.createBacktraceReport(loggingEvent);

        // THEN
        Assert.assertEquals(loggingEvent.getRenderedMessage(), report.getMessage());
        Assert.assertTrue(report.getAttributes().containsKey(LOGGING_LEVEL_ATTRIBUTE_NAME));
    }


    @Test
    public void creatingBacktraceReportFromException() {
        // GIVEN
        final Exception exception = new Exception(message);
        final Logger testLogger = new Logger(null) {
        };
        final Level level = Level.ERROR;
        LoggingEvent loggingEvent = new LoggingEvent(null, testLogger, level, null, exception);

        // WHEN
        BacktraceReport report = Appender.createBacktraceReport(loggingEvent);

        // THEN
        Assert.assertEquals(loggingEvent.getRenderedMessage(), report.getMessage());
        Assert.assertEquals(exception, report.getException());
        Assert.assertTrue(report.getAttributes().containsKey(LOGGING_LEVEL_ATTRIBUTE_NAME));
    }
}