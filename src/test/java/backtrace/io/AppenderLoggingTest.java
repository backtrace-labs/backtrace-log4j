package backtrace.io;

import backtrace.io.data.BacktraceData;
import backtrace.io.events.RequestHandler;
import backtrace.io.http.BacktraceResult;
import backtrace.io.log4j2.Appender;
import backtrace.io.log4j2.AppenderMock;
import net.jodah.concurrentunit.Waiter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.Before;
import org.junit.Test;
import org.slf4j.MDC;

import java.util.concurrent.TimeUnit;

public class AppenderLoggingTest {

    private final static String URL = "https://backtrace.io/";

    private final String message = "test";
    private BacktraceConfig config;

    @Before
    public void init() {
        this.config = new BacktraceConfig(URL);
        this.config.disableDatabase();
    }

    @Test
    public void appendMessageAndNull() throws Exception {
        // GIVEN
        final Waiter waiter = new Waiter();
        final BacktraceClient client = new BacktraceClient(config);
        client.setCustomRequestHandler(new RequestHandler() {
            @Override
            public BacktraceResult onRequest(BacktraceData data) {
                waiter.resume();
                return BacktraceResult.onSuccess(data.getReport(), data.getReport().getMessage());
            }
        });

        // WHEN
        Appender appender = new AppenderMock(client, "backtrace", null, null, false, null);


        // WHEN
        final LogEvent logEvent = Log4jLogEvent.newBuilder()
                .setLevel(Level.DEBUG)
                .setLoggerName("backtrace")
                .setMessage(new SimpleMessage(message))
                .build();

        appender.append(null);
        appender.append(logEvent);

        // THEN
        waiter.await(1000, 1);
        appender.stop();
    }

    @Test
    public void appendMessage() throws Exception {
        // GIVEN
        final Waiter waiter = new Waiter();
        final BacktraceClient client = new BacktraceClient(config);
        client.setCustomRequestHandler(new RequestHandler() {
            @Override
            public BacktraceResult onRequest(BacktraceData data) {
                waiter.resume();
                return BacktraceResult.onSuccess(data.getReport(), data.getReport().getMessage());
            }
        });

        // WHEN
        Appender appender = new AppenderMock(client, "backtrace", null, null, false, null);


        // WHEN
        final LogEvent logEvent = Log4jLogEvent.newBuilder()
                .setLevel(Level.DEBUG)
                .setLoggerName("backtrace")
                .setMessage(new SimpleMessage(message))
                .build();

        appender.append(logEvent);

        // THEN
        waiter.await(1000, 1);
        appender.stop();
    }

    @Test
    public void multipleAppend() throws Exception {
        // GIVEN
        final Waiter waiter = new Waiter();

        this.config.setRequestHandler(new RequestHandler() {
            @Override
            public BacktraceResult onRequest(BacktraceData data) {
                waiter.resume();
                return BacktraceResult.onSuccess(data.getReport(), "");
            }
        });

        final BacktraceClient client = new BacktraceClient(config);

        Appender appender = new AppenderMock(client, "backtrace", null, null, false, null);

        final LogEvent logEvent = Log4jLogEvent.newBuilder()
                .setLevel(Level.DEBUG)
                .setLoggerName("backtrace")
                .setMessage(new SimpleMessage(message))
                .build();
        // WHEN
        appender.append(logEvent);
        appender.append(logEvent);
        appender.append(logEvent);
        appender.append(logEvent);

        // THEN
        appender.await(2000, TimeUnit.MILLISECONDS);
        waiter.await(500, 4);
        appender.stop();
    }

    @Test
    public void createAttributesBasedOnMDC() throws Exception {
        // GIVEN
        final Waiter waiter = new Waiter();
        final String key = "test-key";
        final String value = "test-value";

        config.setRequestHandler(new RequestHandler() {
            @Override
            public BacktraceResult onRequest(BacktraceData data) {
                // THEN
                waiter.assertTrue(data.getReport().getAttributes().containsKey(key));
                waiter.assertEquals(value, data.getReport().getAttributes().get(key));
                waiter.resume();
                return BacktraceResult.onSuccess(data.getReport(), "");
            }
        });
        final BacktraceClient client = new BacktraceClient(config);

        Appender appender = new AppenderMock(client, "backtrace", null, null, false, null);

        // WHEN
        MDC.put(key, value);
        final LogEvent logEvent = Log4jLogEvent.newBuilder()
                .setLevel(Level.DEBUG)
                .setLoggerName("backtrace")
                .setMessage(new SimpleMessage(message))
                .build();
        appender.append(logEvent);

        waiter.await(1000, 1);
        appender.stop();
    }
}
