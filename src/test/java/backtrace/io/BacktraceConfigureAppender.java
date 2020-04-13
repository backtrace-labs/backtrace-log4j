package backtrace.io;

import backtrace.io.log4j2.AppenderMock;
import org.junit.Assert;
import org.junit.Test;

public class BacktraceConfigureAppender {

    private final int maxDatabaseRecordCount = 1;
    private final long maxDatabaseSize = 2;
    private final int maxRetryLimit = 3;
    private final String url = "https://backtrace.io/";
    @Test
    public void configureConfig() {
        // GIVEN

        boolean useDatabase = true;


        // WHEN
        BacktraceConfig preparedConfig = AppenderMock.createBacktraceConfig(url, null, null,
                useDatabase, maxDatabaseSize, maxDatabaseRecordCount, maxRetryLimit);

        // THEN
        int actualMaxDatabaseRecordCount = preparedConfig.getDatabaseConfig().getDatabaseMaxRecordCount();
        long actualMaxSize = preparedConfig.getDatabaseConfig().getDatabaseMaxSize();
        int actualRetryLimit = preparedConfig.getDatabaseConfig().getDatabaseRetryLimit();
        String actualUrl = preparedConfig.getSubmissionUrl();

        Assert.assertTrue(preparedConfig.getDatabaseConfig().isDatabaseEnabled());
        Assert.assertEquals(maxDatabaseRecordCount, actualMaxDatabaseRecordCount);
        Assert.assertEquals(maxDatabaseSize, actualMaxSize);
        Assert.assertEquals(maxRetryLimit, actualRetryLimit);
        Assert.assertEquals(url, actualUrl);
    }

    @Test
    public void disableDatabase() {
        // GIVEN
        boolean useDatabase = false;

        // WHEN
        BacktraceConfig preparedConfig = AppenderMock.createBacktraceConfig(url, "", "",
                useDatabase, maxDatabaseSize, maxDatabaseRecordCount, maxRetryLimit);

        // THEN
        boolean isDatabaseEnabled = preparedConfig.getDatabaseConfig().isDatabaseEnabled();
        Assert.assertFalse(isDatabaseEnabled);
    }
}
