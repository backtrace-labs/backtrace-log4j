package backtrace.io.mock;

import backtrace.io.BacktraceConfig;
import backtrace.io.log4j12.Appender;

public class BacktraceAppenderMock extends Appender {
    private BacktraceConfig config;

    public void setBacktraceConfig(BacktraceConfig config) {
        this.config = config;
    }

    public BacktraceConfig getBacktraceConfig() {
        return config;
    }

    @Override
    protected BacktraceConfig createBacktraceConfig() {
        return getBacktraceConfig();
    }
}

