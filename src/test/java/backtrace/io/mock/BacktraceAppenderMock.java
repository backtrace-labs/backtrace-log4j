package backtrace.io.mock;

import backtrace.io.BacktraceConfig;
import backtrace.io.data.BacktraceData;
import backtrace.io.data.BacktraceReport;
import backtrace.io.events.RequestHandler;
import backtrace.io.http.BacktraceResult;
import log4j.BacktraceAppender;
import net.jodah.concurrentunit.Waiter;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.LoggingEvent;
import org.junit.Before;
import org.junit.Test;
import org.powermock.api.mockito.PowerMockito;
import org.powermock.core.classloader.annotations.PrepareForTest;
import org.slf4j.MDC;

public final class BacktraceAppenderMock extends BacktraceAppender {
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

    public static BacktraceReport createBacktraceReport(LoggingEvent event) {
        return BacktraceAppender.createBacktraceReport(event);
    }
}

