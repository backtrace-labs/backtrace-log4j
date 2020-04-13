package backtrace.io;

import backtrace.io.data.BacktraceData;
import backtrace.io.events.RequestHandler;
import backtrace.io.http.BacktraceResult;
import backtrace.io.log4j2.Appender;
import backtrace.io.log4j2.AppenderMock;
import net.jodah.concurrentunit.Waiter;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.core.Logger;
import org.junit.Test;

import java.util.concurrent.TimeoutException;

public class BacktraceLoggerTest {
    private final String URL = "https://backtrace.io/";
    private final String LOGGER_NAME = "backtrace.io.Demo";
    private final String MESSAGE = "test";

    @Test
    public void filterOutMessageFromBacktraceLogger() {
        // GIVEN
        Waiter waiter = new Waiter();

        Logger logger = (org.apache.logging.log4j.core.Logger) LogManager.getLogger(LOGGER_NAME);
        BacktraceConfig config = new BacktraceConfig(URL);
        config.disableDatabase();
        config.setRequestHandler(new RequestHandler() {
            @Override
            public BacktraceResult onRequest(BacktraceData data) {
                waiter.fail("This code should not be called because we are filtering messages from this logger");
                return BacktraceResult.onSuccess(data.getReport(), "");
            }
        });
        Appender appender = new AppenderMock(new BacktraceClient(config), LOGGER_NAME, null, null, false, null);

        // WHEN
        appender.start();
        logger.addAppender(appender);
        logger.error(MESSAGE);

        // THEN
        appender.stop();
    }

    @Test
    public void allowForMessagesFromOtherThanBacktraceLogger() throws TimeoutException, InterruptedException {
        // GIVEN
        String name = "com.test.Demo";
        Waiter waiter = new Waiter();

        Logger logger = (org.apache.logging.log4j.core.Logger) LogManager.getLogger(name);
        BacktraceConfig config = new BacktraceConfig(URL);
        config.disableDatabase();
        config.setRequestHandler(new RequestHandler() {
            @Override
            public BacktraceResult onRequest(BacktraceData data) {
                waiter.resume();
                return BacktraceResult.onSuccess(data.getReport(), "");
            }
        });

        Appender appender = new AppenderMock(new BacktraceClient(config), name, null, null, false, null);

        // WHEN
        appender.start();
        logger.addAppender(appender);
        logger.error(MESSAGE);

        // THEN
        waiter.await(1000, 1);
        appender.await();
        appender.stop();
    }
}
