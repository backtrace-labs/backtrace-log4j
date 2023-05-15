package backtrace.io;

import backtrace.io.data.BacktraceData;
import backtrace.io.events.RequestHandler;
import backtrace.io.http.BacktraceResult;
import backtrace.io.log4j12.BacktraceAppenderMock;
import net.jodah.concurrentunit.Waiter;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.util.concurrent.TimeoutException;

public class BacktraceLoggerTest {

    @Test
    public void filterOutMessageFromBacktraceLogger() {
        // GIVEN
        Waiter waiter = new Waiter();
        BacktraceAppenderMock appender = new BacktraceAppenderMock();
        Logger logger = Logger.getLogger("backtrace.io.Demo");
        BacktraceConfig config = new BacktraceConfig("https://backtrace.io/");
        config.setRequestHandler(new RequestHandler() {
            @Override
            public BacktraceResult onRequest(BacktraceData data) {
                waiter.fail("This code should not be called because we are filtering messages from this logger");
                return BacktraceResult.onSuccess(data.getReport(), "");
            }
        });
        appender.setBacktraceConfig(config);
        appender.activateOptions();
        logger.addAppender(appender);

        // WHEN
        logger.error("test");

        // THEN
        appender.close();
    }

    @Test
    public void allowForMessagesFromOtherThanBacktraceLogger() throws TimeoutException, InterruptedException {
        // GIVEN
        Waiter waiter = new Waiter();
        BacktraceAppenderMock appender = new BacktraceAppenderMock();
        Logger logger = Logger.getLogger("com.test.Demo");
        BacktraceConfig config = new BacktraceConfig("https://backtrace.io/");
        config.setRequestHandler(new RequestHandler() {
            @Override
            public BacktraceResult onRequest(BacktraceData data) {
                waiter.resume();
                return BacktraceResult.onSuccess(data.getReport(), "");
            }
        });
        appender.setBacktraceConfig(config);
        logger.addAppender(appender);
        appender.activateOptions();

        // WHEN
        logger.error("test");

        // THEN
        waiter.await(1000, 1);
        appender.close();
    }
}
