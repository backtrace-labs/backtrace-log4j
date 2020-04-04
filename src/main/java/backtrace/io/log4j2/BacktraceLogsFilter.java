package backtrace.io.log4j2;

import org.apache.logging.log4j.Level;
import org.apache.logging.log4j.Marker;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.Logger;
import org.apache.logging.log4j.core.filter.AbstractFilter;
import org.apache.logging.log4j.message.Message;

class BacktraceLogsFilter extends AbstractFilter {
    private final static String NAME = "backtrace.io";

    @Override
    public Result filter(Logger logger, Level level, Marker marker, String msg, Object... params) {
        return filterBacktraceLogs(logger.getName());
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Object msg, Throwable t) {
        return filterBacktraceLogs(logger.getName());
    }

    @Override
    public Result filter(Logger logger, Level level, Marker marker, Message msg, Throwable t) {
        return filterBacktraceLogs(logger.getName());
    }

    @Override
    public Result filter(LogEvent event) {
        return filterBacktraceLogs(event.getLoggerName());
    }

    private Result filterBacktraceLogs(String loggerName) {
        if (loggerName != null && loggerName.toLowerCase().startsWith(NAME)) {
            return Result.DENY;
        }
        return Result.NEUTRAL;
    }
}
