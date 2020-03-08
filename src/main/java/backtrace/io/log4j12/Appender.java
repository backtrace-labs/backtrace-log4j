package backtrace.io.log4j12;

import backtrace.io.BacktraceClient;
import backtrace.io.BacktraceConfig;
import backtrace.io.data.BacktraceReport;
import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.spi.Filter;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.TimeUnit;


public class Appender extends AppenderSkeleton {
    public final static String NAME = "backtrace";
    private final static String ATTRIBUTE_LOGGING_LEVEL_NAME = "log_level";
    // Backtrace config
    private BacktraceClient backtraceClient;
    private String endpointUrl;
    private String submissionToken;
    private String submissionUrl;
    private boolean enableUncaughtExceptionHandler;
    // Application attributes
    private String appVersion;
    private String appName;
    // Database settings
    private boolean disableDatabase;
    private Long maxDatabaseSize;
    private Integer maxDatabaseRecordCount;
    private Integer maxDatabaseRetryLimit;

    public Appender() {
        this.addFilter(new Filter() {
            @Override
            public int decide(LoggingEvent event) {
                String loggerName = event.getLoggerName();
                if (loggerName != null && loggerName.toLowerCase().startsWith(NAME)) {
                    return Filter.DENY;
                }
                return Filter.NEUTRAL;
            }
        });
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

    private BacktraceClient getBacktraceClient() {
        return backtraceClient;
    }

    private void setBacktraceClient(BacktraceClient backtraceClient) {
        this.backtraceClient = backtraceClient;
    }

    private String getEndpointUrl() {
        return endpointUrl;
    }

    @SuppressWarnings("unused")
    public void setEndpointUrl(String endpointUrl) {
        this.endpointUrl = endpointUrl;
    }

    private String getSubmissionToken() {
        return submissionToken;
    }

    @SuppressWarnings("unused")
    public void setSubmissionToken(String submissionToken) {
        this.submissionToken = submissionToken;
    }

    private String getSubmissionUrl() {
        return submissionUrl;
    }

    @SuppressWarnings("unused")
    public void setSubmissionUrl(String submissionUrl) {
        this.submissionUrl = submissionUrl;
    }

    private boolean isEnableUncaughtExceptionHandler() {
        return enableUncaughtExceptionHandler;
    }

    @SuppressWarnings("unused")
    public void setEnableUncaughtExceptionHandler(boolean enableUncaughtExceptionHandler) {
        this.enableUncaughtExceptionHandler = enableUncaughtExceptionHandler;
    }

    private String getAppVersion() {
        return appVersion;
    }

    @SuppressWarnings("unused")
    public void setAppVersion(String appVersion) {
        this.appVersion = appVersion;
    }

    private String getAppName() {
        return appName;
    }

    @SuppressWarnings("unused")
    public void setAppName(String appName) {
        this.appName = appName;
    }

    private boolean isDisableDatabase() {
        return disableDatabase;
    }

    @SuppressWarnings("unused")
    public void setDisableDatabase(boolean disableDatabase) {
        this.disableDatabase = disableDatabase;
    }

    private Integer getMaxDatabaseRetryLimit() {
        return maxDatabaseRetryLimit;
    }

    @SuppressWarnings("unused")
    public void setMaxDatabaseRetryLimit(int maxDatabaseRetryLimit) {
        this.maxDatabaseRetryLimit = maxDatabaseRetryLimit;
    }

    private Long getMaxDatabaseSize() {
        return maxDatabaseSize;
    }

    @SuppressWarnings("unused")
    public void setMaxDatabaseSize(long maxDatabaseSize) {
        this.maxDatabaseSize = maxDatabaseSize;
    }

    private Integer getMaxDatabaseRecordCount() {
        return maxDatabaseRecordCount;
    }

    @SuppressWarnings("unused")
    public void setMaxDatabaseRecordCount(int maxDatabaseRecordCount) {
        this.maxDatabaseRecordCount = maxDatabaseRecordCount;
    }

    /**
     * Initialize the appender
     */
    @Override
    public void activateOptions() {
        BacktraceConfig config = createBacktraceConfig();
        this.configureBacktraceDatabaseSettings(config);

        BacktraceClient backtraceClient = new BacktraceClient(config);
        setBacktraceClient(backtraceClient);

        configureBacktraceClient(getBacktraceClient());
    }

    /**
     * Send log message to Backtrace console
     */
    @Override
    public void append(LoggingEvent event) {
        if (event == null) {
            return;
        }

        LogLog.debug("Sending report with message " + event.getRenderedMessage());
        BacktraceReport report = createBacktraceReport(event);
        this.backtraceClient.send(report);
    }


    /**
     * Close all of Backtrace library resources and wait until last of messages will be process and
     */
    @Override
    public void close() {
        LogLog.debug("Closing BacktraceAppender");
        try {
            this.backtraceClient.close();
        } catch (InterruptedException e) {
            LogLog.error("Error occurs during closing Backtrace client", e);
        }
    }

    /**
     * The BacktraceAppender doesn't require a layout. Hence, this method returns false
     *
     * @return false
     */
    @Override
    public boolean requiresLayout() {
        return false;
    }

    /**
     * Wait until last of messages will be process and close all of Backtrace library resources
     *
     * @throws InterruptedException if the current thread is interrupted while waiting
     */
    public void await() throws InterruptedException {
        this.backtraceClient.await();
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
        return this.backtraceClient.await(timeout, unit);
    }

    /**
     * Create instance of Backtrace Client based on passed properties
     *
     * @return Backtrace library configuration
     */
    protected BacktraceConfig createBacktraceConfig() {
        String submissionUrl = this.getSubmissionUrl();
        if (isStringNotEmpty(submissionUrl)) {
            return new BacktraceConfig(submissionUrl);
        }
        return new BacktraceConfig(getEndpointUrl(), getSubmissionToken());
    }

    /**
     * Set in Backtrace configuration all options related to database
     *
     * @param backtraceConfig backtrace library configuration
     */
    private void configureBacktraceDatabaseSettings(BacktraceConfig backtraceConfig) {
        if (this.isDisableDatabase()) {
            backtraceConfig.disableDatabase();
            return;
        }

        if (this.getMaxDatabaseSize() != null) {
            backtraceConfig.setMaxDatabaseSize(this.getMaxDatabaseSize());
        }
        if (this.getMaxDatabaseRecordCount() != null) {
            backtraceConfig.setMaxRecordCount(this.getMaxDatabaseRecordCount());
        }
        if (this.getMaxDatabaseRetryLimit() != null) {
            backtraceConfig.setDatabaseRetryLimit(this.getMaxDatabaseRetryLimit());
        }
    }

    /**
     * Configure Backtrace Client - enable uncaught exception handler, set app name and app version
     *
     * @param client Backtrace client instance
     */
    private void configureBacktraceClient(BacktraceClient client) {
        if (this.isEnableUncaughtExceptionHandler()) {
            client.enableUncaughtExceptionsHandler();
        }

        String appName = this.getAppName();
        if (isStringNotEmpty(appName)) {
            client.setApplicationName(appName);
        }

        String appVersion = this.getAppVersion();
        if (isStringNotEmpty(appVersion)) {
            client.setApplicationVersion(appVersion);
        }
    }

    /**
     * Get attributes from logging event
     *
     * @param loggingEvent representation of logging events
     * @return map with attributes
     */
    private static Map<String, Object> getAttributes(LoggingEvent loggingEvent) {
        Map<String, Object> attributes = new HashMap<>();

        attributes.put(ATTRIBUTE_LOGGING_LEVEL_NAME, loggingEvent.getLevel().toString());
        Map<String, Object> properties = (Map<String, Object>) loggingEvent.getProperties();

        if (properties.size() != 0){
            for (Map.Entry<String, Object> mdcEntry : properties.entrySet()) {
                attributes.put(mdcEntry.getKey(), mdcEntry.getValue());
            }
        }

        return attributes;
    }

    /**
     * Generate BacktraceReport based on logging event
     *
     * @param event representation of logging events
     * @return Backtrace report
     */
    public static BacktraceReport createBacktraceReport(LoggingEvent event) {
        BacktraceReport report;
        ThrowableInformation throwableInformation = event.getThrowableInformation();
        Map<String, Object> attributes = getAttributes(event);

        if (throwableInformation != null) {
            Exception exception = (Exception) throwableInformation.getThrowable();
            report = new BacktraceReport(exception, attributes);
        } else {
            report = new BacktraceReport(event.getRenderedMessage(), attributes);
        }
        return report;
    }
}