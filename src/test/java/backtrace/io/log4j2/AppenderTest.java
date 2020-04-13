package backtrace.io.log4j2;

import backtrace.io.data.BacktraceData;
import backtrace.io.events.RequestHandler;
import backtrace.io.http.BacktraceResult;
import net.jodah.concurrentunit.Waiter;
import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.impl.Log4jLogEvent;
import org.apache.logging.log4j.message.SimpleMessage;
import org.junit.Assert;
import org.junit.Test;
public class AppenderTest {

    @Test
    public void createAppenderWithoutName(){
        // GIVEN
        final String appenderName = null;

        // WHEN
        Appender appender = Appender.createAppender(appenderName, null, null, null, null,
                "https://backtrace.io/", false, null,
                null, false, 0, 0, 0);

        // THEN
        Assert.assertNull(appender);
    }

    @Test
    public void checkAppNameAndAppVersion() throws Exception {
        // GIVEN
        final Waiter waiter = new Waiter();
        final String message = "test-message";
        final String appName = "backtrace-app";
        final String appVersion = "1.0.0";

        Appender appender = AppenderMock.createAppender("backtrace", null, null, null, null,
                "https://backtrace.io/", false, appVersion,
                appName, false, 0, 0, 0);

        // WHEN
        appender.getBacktraceClient().setCustomRequestHandler(new RequestHandler() {
            @Override
            public BacktraceResult onRequest(BacktraceData data) {
                // THEN
                waiter.assertEquals(message, data.getReport().getMessage());
                waiter.assertEquals(appName, data.getAttributes().get("application"));
                waiter.assertEquals(appVersion, data.getAttributes().get("version"));
                waiter.resume();
                return BacktraceResult.onSuccess(data.getReport(), data.getReport().getMessage());
            }
        });

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
}
