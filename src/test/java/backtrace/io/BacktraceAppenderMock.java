package backtrace.io;

import backtrace.io.data.BacktraceReport;
import org.apache.log4j.spi.LoggingEvent;

final class BacktraceAppenderMock extends BacktraceAppender {
    private BacktraceConfig config;

    void setBacktraceConfig(BacktraceConfig config) {
        this.config = config;
    }

    BacktraceConfig getBacktraceConfig() {
        return config;
    }

    BacktraceConfig createBacktraceConfig() {
        return getBacktraceConfig();
    }

    static BacktraceReport createBacktraceReport(LoggingEvent event) {
        return BacktraceAppender.createBacktraceReport(event);
    }
}

