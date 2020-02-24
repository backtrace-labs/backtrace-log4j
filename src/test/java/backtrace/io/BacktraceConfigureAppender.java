package backtrace.io;

import backtrace.io.mock.BacktraceAppenderMock;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

public class BacktraceConfigureAppender {
    private final Logger testLogger = new Logger("") {};
    private final String message = "test";

    @Before
    public void init(){
        BasicConfigurator.configure();
    }

    @Test
    public void configureDatabase() {
        // GIVEN
        BacktraceAppenderMock appender = new BacktraceAppenderMock();
        int maxDatabaseRecordCount = 1;
        long maxDatabaseSize = 2;
        int maxRetryLimit = 3;

        // WHEN
        appender.setMaxDatabaseRecordCount(maxDatabaseRecordCount);
        appender.setBacktraceConfig(new BacktraceConfig("https://backtrace.io/"));
        appender.setMaxDatabaseSize(maxDatabaseSize);
        appender.setMaxDatabaseRetryLimit(maxRetryLimit);
        appender.activateOptions();

        // THEN
        BacktraceConfig config = appender.getBacktraceConfig();
        int actualMaxDatabaseRecordCount = config.getDatabaseConfig().getDatabaseMaxRecordCount();
        long actualMaxSize = config.getDatabaseConfig().getDatabaseMaxSize();
        int actualRetryLimit = config.getDatabaseConfig().getDatabaseRetryLimit();

        Assert.assertEquals(maxDatabaseRecordCount, actualMaxDatabaseRecordCount);
        Assert.assertEquals(maxDatabaseSize, actualMaxSize);
        Assert.assertEquals(maxRetryLimit, actualRetryLimit);
    }

    @Test
    public void disableDatabase() {
        // GIVEN
        BacktraceAppenderMock appender = new BacktraceAppenderMock();
        appender.setBacktraceConfig(new BacktraceConfig("https://backtrace.io/", "token"));
        BacktraceConfig config = appender.getBacktraceConfig();

        // WHEN
        appender.setDisableDatabase(true);
        appender.activateOptions();

        // THEN
        boolean isDatabaseEnabled = config.getDatabaseConfig().isDatabaseEnabled();
        Assert.assertFalse(isDatabaseEnabled);
    }
}
