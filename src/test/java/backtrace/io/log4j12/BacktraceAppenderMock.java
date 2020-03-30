package backtrace.io.log4j12;

import backtrace.io.BacktraceConfig;
import backtrace.io.data.BacktraceReport;
import backtrace.io.log4j12.Appender;
import org.apache.log4j.spi.LoggingEvent;

public final class BacktraceAppenderMock extends Appender {
    private BacktraceConfig config;

    public void setBacktraceConfig(BacktraceConfig config) {
        this.config = config;
    }

    public BacktraceConfig getBacktraceConfig() {
        return config;
    }

    @Override
    public BacktraceConfig createBacktraceConfig() {
        return getBacktraceConfig();
    }
}

