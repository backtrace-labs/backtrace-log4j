package backtrace.io;

import backtrace.io.log4j2.AppenderMock;
import org.junit.Assert;
import org.junit.Test;

public class BacktraceConfigureAppenderTest {

    private final int MAX_DATABASE_RECORD_COUNT = 1;
    private final long MAX_DATABASE_SIZE = 2;
    private final int MAX_RETRY_LIMIT = 3;
    private final String URL = "https://backtrace.io/";

    @Test
    public void configureConfig() {
        // GIVEN

        boolean useDatabase = true;


        // WHEN
        BacktraceConfig preparedConfig = AppenderMock.createBacktraceConfig(URL, null, null,
                useDatabase, MAX_DATABASE_SIZE, MAX_DATABASE_RECORD_COUNT, MAX_RETRY_LIMIT);

        // THEN
        int actualMaxDatabaseRecordCount = preparedConfig.getDatabaseConfig().getDatabaseMaxRecordCount();
        long actualMaxSize = preparedConfig.getDatabaseConfig().getDatabaseMaxSize();
        int actualRetryLimit = preparedConfig.getDatabaseConfig().getDatabaseRetryLimit();
        String actualUrl = preparedConfig.getSubmissionUrl();

        Assert.assertTrue(preparedConfig.getDatabaseConfig().isDatabaseEnabled());
        Assert.assertEquals(MAX_DATABASE_RECORD_COUNT, actualMaxDatabaseRecordCount);
        Assert.assertEquals(MAX_DATABASE_SIZE, actualMaxSize);
        Assert.assertEquals(MAX_RETRY_LIMIT, actualRetryLimit);
        Assert.assertEquals(URL, actualUrl);
    }

    @Test
    public void disableDatabase() {
        // GIVEN
        boolean useDatabase = false;

        // WHEN
        BacktraceConfig preparedConfig = AppenderMock.createBacktraceConfig(URL, "", "",
                useDatabase, MAX_DATABASE_SIZE, MAX_DATABASE_RECORD_COUNT, MAX_RETRY_LIMIT);

        // THEN
        boolean isDatabaseEnabled = preparedConfig.getDatabaseConfig().isDatabaseEnabled();
        Assert.assertFalse(isDatabaseEnabled);
    }
}
