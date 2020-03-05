package backtrace.io;

import backtrace.io.data.BacktraceData;
import backtrace.io.events.RequestHandler;
import backtrace.io.http.BacktraceResult;
import backtrace.io.mock.BacktraceAppenderMock;
import log4j.BacktraceAppender;
import net.jodah.concurrentunit.Waiter;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.Priority;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;

import java.util.concurrent.TimeUnit;

public class BacktraceAppenderTest {
    private final static String LOGGING_LEVEL_ATTRIBUTE_NAME = "log_level";
    private final static String URL = "https://backtrace.io/";
    private final Logger testLogger = new Logger(null) {
    };
    private final String message = "test";
    private BacktraceConfig config;

    @Before
    public void init() {
        BasicConfigurator.configure();
        this.config = new BacktraceConfig(URL);
        this.config.disableDatabase();
    }

    @Test
    public void appendNullMessage() {
        BacktraceAppender appender = new BacktraceAppender();
        appender.setSubmissionUrl(URL);
        appender.activateOptions();
        appender.append(null);
    }

    @Test
    public void appendMessage() throws Exception {
        // GIVEN
        final Waiter waiter = new Waiter();

        final BacktraceAppenderMock appender = new BacktraceAppenderMock();

        config.setRequestHandler(new RequestHandler() {
            @Override
            public BacktraceResult onRequest(BacktraceData data) {
                LogLog.debug("Custom test request handler");
                waiter.resume();
                return BacktraceResult.onSuccess(data.getReport(), data.getReport().getMessage());
            }
        });

        appender.setBacktraceConfig(config);
        appender.activateOptions();

        // WHEN
        final LoggingEvent loggingEvent = new LoggingEvent(null, testLogger, Level.DEBUG, message, null) {
        };
        appender.append(loggingEvent);

        // THEN
        waiter.await(1000, 1);
        appender.close();
    }

    @Test
    public void createAttributesBasedOnMDC() throws Exception {
        // GIVEN
        final Waiter waiter = new Waiter();
        final String key = "test-key";
        final String value = "test-value";
        MDC.put(key, value);

        config.setRequestHandler(new RequestHandler() {
            @Override
            public BacktraceResult onRequest(BacktraceData data) {
                // THEN
                waiter.assertTrue(data.getReport().getAttributes().containsKey(key));
                waiter.assertEquals(value, data.getReport().getAttributes().get(key));
                return BacktraceResult.onSuccess(data.getReport(), "");
            }
        });
        final BacktraceAppenderMock appender = new BacktraceAppenderMock();
        appender.setBacktraceConfig(config);
        appender.activateOptions();

        // WHEN
        final LoggingEvent loggingEvent = new LoggingEvent(null, testLogger, Level.DEBUG, message, null) {
        };
        appender.append(loggingEvent);
        appender.close();
    }


    @Test
    public void createAttributesBasedOnProperties() throws Exception {
        // GIVEN
        final Waiter waiter = new Waiter();
        final String appName = "app-name";
        final String appVersion = "app-version";

        config.setRequestHandler(new RequestHandler() {
            @Override
            public BacktraceResult onRequest(BacktraceData data) {
                // THEN
                waiter.assertEquals(appVersion, data.getReport().getAttributes().get("version"));
                waiter.assertEquals(appName, data.getReport().getAttributes().get("application"));
                waiter.resume();
                return BacktraceResult.onSuccess(data.getReport(), "");
            }
        });
        final BacktraceAppenderMock appender = new BacktraceAppenderMock();
        appender.setAppVersion(appVersion);
        appender.setAppName(appName);
        appender.setBacktraceConfig(config);
        appender.activateOptions();

        // WHEN
        final LoggingEvent loggingEvent = new LoggingEvent(null, testLogger, Level.DEBUG, message, null) {
        };
        appender.append(loggingEvent);
        appender.await();
        waiter.await(2000, 1);
    }

    @Test
    public void multipleAppend() throws Exception {
        // GIVEN
        Waiter waiter = new Waiter();

        this.config.setRequestHandler(new RequestHandler() {
            @Override
            public BacktraceResult onRequest(BacktraceData data) {
                waiter.resume();
                return BacktraceResult.onSuccess(data.getReport(), "");
            }
        });

        BacktraceAppenderMock appender = new BacktraceAppenderMock();
        appender.setBacktraceConfig(config);
        appender.activateOptions();

        final LoggingEvent loggingEvent = new LoggingEvent(null,
                testLogger, Level.DEBUG, message, null) {
        };

        // WHEN
        appender.append(loggingEvent);
        appender.append(loggingEvent);
        appender.append(loggingEvent);
        appender.append(loggingEvent);

        // THEN
        appender.close();
        waiter.await(2000, 4);
    }

    @Test
    public void appendFromBacktraceLogger() throws Exception {
        // GIVEN
        Waiter waiter = new Waiter();
        this.config.setRequestHandler(new RequestHandler() {
            @Override
            public BacktraceResult onRequest(BacktraceData data) {
                waiter.fail();
                return BacktraceResult.onError(data.getReport(), null);
            }
        });

        Logger logger = Logger.getRootLogger();
        BacktraceAppenderMock appender = new BacktraceAppenderMock();
        appender.setBacktraceConfig(config);
        appender.activateOptions();

        final LoggingEvent loggingEvent = new LoggingEvent(null, new Logger(BacktraceAppender.NAME) {
        }, Level.DEBUG, message, null) {
        };

        // WHEN
        logger.addAppender(appender);
        logger.callAppenders(loggingEvent);

        // THEN
        appender.await(1, TimeUnit.SECONDS);
    }

    @Test
    public void logExceptionWithErrorLevelFromLogger() throws Exception {
        // GIVEN
        final Waiter waiter = new Waiter();
        final Exception exception = new Exception("test message");
        this.config.setRequestHandler(new RequestHandler() {
            @Override
            public BacktraceResult onRequest(BacktraceData data) {
                waiter.assertEquals(exception, data.getReport().getException());
                waiter.resume();
                return BacktraceResult.onSuccess(data.getReport(), "");
            }
        });

        Logger logger = Logger.getRootLogger();
        BacktraceAppenderMock appender = new BacktraceAppenderMock();
        appender.setBacktraceConfig(config);
        appender.activateOptions();
        logger.addAppender(appender);

        // WHEN
        logger.error("", exception);

        // THEN
        waiter.await(1000, 1);
    }

    @Test
    public void logMessageWithDebugLevelFromLogger() throws Exception {
        // GIVEN
        final Waiter waiter = new Waiter();
        final String message = "test message";
        this.config.setRequestHandler(new RequestHandler() {
            @Override
            public BacktraceResult onRequest(BacktraceData data) {
                waiter.assertEquals(message, data.getReport().getMessage());
                waiter.resume();
                return BacktraceResult.onSuccess(data.getReport(), "");
            }
        });

        Logger logger = Logger.getRootLogger();
        BacktraceAppenderMock appender = new BacktraceAppenderMock();
        appender.setBacktraceConfig(config);
        appender.activateOptions();
        logger.addAppender(appender);

        // WHEN
        logger.debug(message);

        // THEN
        waiter.await(1000, 1);
    }
}