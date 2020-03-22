package app;

import backtrace.io.log4j2.Appender;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.core.config.Configurator;
import org.apache.logging.log4j.core.config.DefaultConfiguration;


public class Demo {


    public static void main(String[] args) throws InterruptedException {
//        Configurator.initialize(new DefaultConfiguration());
        Logger logger = LogManager.getLogger(Demo.class);
        try{
            int x = 0;
            int y = 0;
            int w = x/(y);
        }
        catch (Exception exception){
            logger.error("[LOG4J2] Welcome from MAIN after fixes");
        }

//        LoggerConfig.RootLogger.

//        Appender x = Logger.getAppender(BacktraceAppender.NAME);
//        x.getBacktraceClient().close();
//        x.await();
//        Logger rootLogger = LogManager.getRootLogger();

        Thread.sleep(10000);
        System.out.println("WORKS");
    }
}