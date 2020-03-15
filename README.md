# Backtrace Log4j support
[Backtrace](http://backtrace.io/) provides support for log4j by providing a special appender that can be connected to any application to send logged information also to the Backtrace dashboard. BacktraceAppender sends also MDC properties and using offline database for error report storage and re-submission in case of network outage. 

# Installation via Gradle or Maven<a name="installation"></a>
Will be added after first release.

<!--
* Gradle
```
dependencies {
    implementation 'com.github.backtrace-labs.backtrace-log4j:backtrace-log4j:1.0.0'
}
```

* Maven
```
<dependency>
  <groupId>com.github.backtrace-labs.backtrace-log4j</groupId>
  <artifactId>backtrace-log4j</artifactId>
  <version>1.0.0</version>
</dependency>
```
-->

# Configuration

In order to configure the BacktraceAppender, it is mandatory to set the `submissionUrl` parameter or two parameters such as `endpointUrl` and `submissionToken`. Other parameters are optional.

### Example basic configuration using the log4j.properties
```
log4j.rootLogger=CONSOLE, backtrace

log4j.appender.CONSOLE=org.apache.log4j.ConsoleAppender
log4j.appender.CONSOLE.layout=org.apache.log4j.PatternLayout
log4j.appender.CONSOLE.layout.ConversionPattern=%-4r [%t] %-5p %c %x - %m%n

log4j.appender.backtrace=BacktraceAppender
log4j.appender.backtrace.threshold=ERROR
log4j.appender.backtrace.endpointUrl=https://<yourInstance>.sp.backtrace.io:6098/
log4j.appender.backtrace.submissionToken=<submissionToken>
```

### Optional parameters:
- `threshold` - minimum threshold of what kind of messages are to be sent, eg. `DEBUG`, `WARN`, `ERROR`
- `appVersion` - string value which represents your's application version
- `appName` - string value which represents your's application name
- `allThreads` - boolean value, default `true` (also if this parameter is not specified) the library will gather information about all threads. If `false` library will gather only information from thread in which caused exception
- `enableUncaughtExceptionHandler` - boolean value, if `true` library will catch all uncaught exceptions and send to server
- `useDatabase` - boolean value, default `true` (also if this parameter is not specified) the library will store unsent messages in files, if `false` library will not be using any files to store reports
- `maxDatabaseSize` - maximum database size in bytes, by default size is unlimited
- `maxDatabaseRecordCount` - number of times library will try to send the error report again if sending will finished with fail
- `maxDatabaseRetryLimit` - maximum number of messages in database. If a limit is set, the oldest error reports will be deleted if there will be try to exceed the limit


# Waiting 
By default Backtrace is sending all messages asynchronously and doesn't block main thread. You can wait until all currently messages will be sent with executing method `await`. Optionally as a parameter to `await` method you can pass the maximum time you want to wait for that.

```java
BacktraceAppender backtrace = (BacktraceAppender) Logger.getRootLogger().getAppender("backtrace");
backtrace.await();
```

# Closing 
To make sure that all resources allocated by the library are released, call the `close` method. This method will send the currently processed message and then free all resources. Below you can find example how to do it.

```java
BacktraceAppender backtrace = (BacktraceAppender) Logger.getRootLogger().getAppender("backtrace");
backtrace.close();
```