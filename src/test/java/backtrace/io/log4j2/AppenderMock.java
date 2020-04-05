package backtrace.io.log4j2;


import backtrace.io.BacktraceClient;
import backtrace.io.BacktraceConfig;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.config.Property;

import java.io.Serializable;


public final class AppenderMock extends Appender {
    private static BacktraceConfig config;

    public AppenderMock(BacktraceClient backtraceClient, String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions, Property[] properties) {
        super(backtraceClient, name, filter, layout, ignoreExceptions, properties);
    }

    static BacktraceConfig createBacktraceConfig(String submissionUrl, String endpointUrl, String submissionToken,
                                                 boolean useDatabase, Long maxDatabaseSize,
                                                 Integer maxDatabaseRecordCount, Integer maxDatabaseRetryLimit) {
        return AppenderMock.config;
    }

    public static void setBacktraceConfig(BacktraceConfig config) {
        AppenderMock.config = config;
    }
}
