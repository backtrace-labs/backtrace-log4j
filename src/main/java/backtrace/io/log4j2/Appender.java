package backtrace.io.log4j2;


import backtrace.io.BacktraceClient;
import backtrace.io.BacktraceConfig;
import backtrace.io.data.BacktraceReport;
import org.apache.logging.log4j.core.Core;
import org.apache.logging.log4j.core.Filter;
import org.apache.logging.log4j.core.Layout;
import org.apache.logging.log4j.core.LogEvent;
import org.apache.logging.log4j.core.appender.AbstractAppender;
import org.apache.logging.log4j.core.config.Property;
import org.apache.logging.log4j.core.config.plugins.Plugin;
import org.apache.logging.log4j.core.config.plugins.PluginAttribute;
import org.apache.logging.log4j.core.config.plugins.PluginElement;
import org.apache.logging.log4j.core.config.plugins.PluginFactory;
import org.apache.logging.log4j.status.StatusLogger;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@Plugin(
        name = "BacktraceAppender",
        category = Core.CATEGORY_NAME,
        elementType = org.apache.logging.log4j.core.Appender.ELEMENT_TYPE)

public class Appender extends AbstractAppender {
    private final BacktraceClient backtraceClient;

    private final StatusLogger internalLogger = StatusLogger.getLogger();
    private final static String ATTRIBUTE_LOGGING_LEVEL_NAME = "log_level";

    protected Appender(BacktraceClient backtraceClient, String name, Filter filter, Layout<? extends Serializable> layout, boolean ignoreExceptions, Property[] properties) {
        super(name, filter, layout, ignoreExceptions, properties);
        this.backtraceClient = backtraceClient;
        this.addFilter(new BacktraceLogsFilter());
    }

    @PluginFactory
    @SuppressWarnings("unused")
    public static Appender createAppender(
            @PluginAttribute("name") String name,
            @PluginElement("Layout") Layout<? extends Serializable> layout,
            @PluginElement("Filter") final Filter filter,
            @PluginAttribute(value = "endpointUrl") String endpointUrl,
            @PluginAttribute(value = "submissionToken") String submissionToken,
            @PluginAttribute(value = "submissionUrl") String submissionUrl,
            @PluginAttribute(value = "enableUncaughtExceptionHandler") Boolean enableUncaughtExceptionHandler,
            @PluginAttribute(value = "appVersion") String appVersion,
            @PluginAttribute(value = "appName") String appName,
            @PluginAttribute(value = "useDatabase") boolean useDatabase,
            @PluginAttribute(value = "maxDatabaseSize", defaultLong = -1) long maxDatabaseSize, // -1 is unlimited
            @PluginAttribute(value = "maxDatabaseRecordCount", defaultInt = -1) int maxDatabaseRecordCount, // -1 is unlimited
            @PluginAttribute(value = "maxDatabaseRetryLimit", defaultInt = 3) int maxDatabaseRetryLimit
    ) {
        if (name == null) {
            LOGGER.error("No name provided for BacktraceAppender");
            return null;
        }

        BacktraceConfig backtraceConfig = Appender.createBacktraceConfig(submissionUrl, endpointUrl, submissionToken,
                useDatabase, maxDatabaseSize,
                maxDatabaseRecordCount, maxDatabaseRetryLimit);

        BacktraceClient backtraceClient = Appender.createBacktraceClient(backtraceConfig, enableUncaughtExceptionHandler, appName, appVersion);

        return new Appender(backtraceClient, name, filter, layout, true, null);
    }

    BacktraceClient getBacktraceClient() {
        return backtraceClient;
    }

    /**
     * Check is passed string is not null and not empty otherwise true
     *
     * @param s string
     * @return false if string is null or empty
     */
    private static boolean isStringNotEmpty(final String s) {
        // Null-safe, short-circuit evaluation.
        return !(s == null || s.trim().isEmpty());
    }

    /**
     * Send log message to Backtrace console
     */
    @Override
    public void append(LogEvent logEvent) {
        if (logEvent == null) {
            return;
        }

        internalLogger.debug("Sending report with message " + logEvent.getMessage().getFormattedMessage());
        BacktraceReport report = createBacktraceReport(logEvent);
        this.getBacktraceClient().send(report);
    }


    /**
     * Close all of Backtrace library resources and wait until last of messages will be process and
     */
    @Override
    public void stop() {
        internalLogger.debug("Closing BacktraceAppender");
        try {
            super.stop();
            this.getBacktraceClient().close();
        } catch (InterruptedException e) {
            internalLogger.error("Error occurs during closing Backtrace client", e);
        }
    }


    /**
     * Wait until last of messages will be process and close all of Backtrace library resources
     *
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    public void await() throws InterruptedException {
        this.getBacktraceClient().await();
    }

    /**
     * Wait until all messages in queue will be sent
     *
     * @param timeout the maximum time to wait
     * @param unit    the time unit of the {@code timeout} argument
     * @return {@code true} if all messages are sent in passed time and {@code false}
     * if the waiting time elapsed before all messages has been sent
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    public boolean await(long timeout, TimeUnit unit) throws InterruptedException {
        return this.getBacktraceClient().await(timeout, unit);
    }

    /**
     * Create instance of Backtrace Client based on properties from configuration file (eg. log4j.properties)
     *
     * @return Backtrace library configuration
     */
    static BacktraceConfig createBacktraceConfig(String submissionUrl, String endpointUrl, String submissionToken,
                                                 boolean useDatabase, Long maxDatabaseSize,
                                                 Integer maxDatabaseRecordCount, Integer maxDatabaseRetryLimit) {
        BacktraceConfig backtraceConfig = isStringNotEmpty(submissionUrl) ? new BacktraceConfig(submissionUrl) : new BacktraceConfig(endpointUrl, submissionToken);

        if (!useDatabase) {
            backtraceConfig.disableDatabase();
            return backtraceConfig;
        }

        if (maxDatabaseSize != null) {
            backtraceConfig.setMaxDatabaseSize(maxDatabaseSize);
        }

        if (maxDatabaseRecordCount != null) {
            backtraceConfig.setMaxRecordCount(maxDatabaseRecordCount);
        }

        if (maxDatabaseRetryLimit != null) {
            backtraceConfig.setDatabaseRetryLimit(maxDatabaseRetryLimit);
        }

        return backtraceConfig;
    }

    /**
     * Configure Backtrace Client - enable uncaught exception handler, set app name and app version
     *
     * @param config                           library configuration
     * @param isEnableUncaughtExceptionHandler is uncaught exception handler should be enabled
     * @param appName                          application name
     * @param appVersion                       application version
     * @return configured Backtrace Client instance
     */
    static BacktraceClient createBacktraceClient(BacktraceConfig config, boolean isEnableUncaughtExceptionHandler, String appName, String appVersion) {

        BacktraceClient client = new BacktraceClient(config);
        if (isEnableUncaughtExceptionHandler) {
            client.enableUncaughtExceptionsHandler();
        }

        if (isStringNotEmpty(appName)) {
            client.setApplicationName(appName);
        }

        if (isStringNotEmpty(appVersion)) {
            client.setApplicationVersion(appVersion);
        }

        return client;
    }

    /**
     * Get attributes from logging event
     *
     * @param logEvent representation of logging events
     * @return map with attributes
     */
    private static Map<String, Object> getAttributes(LogEvent logEvent) {
        Map<String, Object> attributes = new HashMap<>();

        attributes.put(ATTRIBUTE_LOGGING_LEVEL_NAME, logEvent.getLevel().toString());
        Map<String, String> properties = logEvent.getContextData().toMap();

        if (properties.size() != 0) {
            for (Map.Entry<String, String> mdcEntry : properties.entrySet()) {
                attributes.put(mdcEntry.getKey(), mdcEntry.getValue());
            }
        }

        return attributes;
    }

    /**
     * Generate BacktraceReport based on logging event
     *
     * @param logEvent representation of logging events
     * @return Backtrace report
     */
    static BacktraceReport createBacktraceReport(LogEvent logEvent) {
        BacktraceReport report;
        Throwable throwable = logEvent.getThrown();
        Map<String, Object> attributes = getAttributes(logEvent);

        if (throwable != null) {
            report = new BacktraceReport((Exception) throwable, attributes);
        } else {
            report = new BacktraceReport(logEvent.getMessage().getFormattedMessage(), attributes);
        }
        return report;
    }

}