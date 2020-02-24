package app;

import log4j.BacktraceAppender;
import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;

public class Demo {


    public static void main(String[] args) throws InterruptedException {

        BasicConfigurator.configure();
        Logger logger = Logger.getLogger(Demo.class);
//        logger.info("test");
        try{
            int x = 0;
            int y = 0;
            int w = x/(y);
        }
        catch (Exception exception){
            logger.error("Welcome from MAIN - log4j", exception);
        }

        BacktraceAppender x = (BacktraceAppender)Logger.getRootLogger().getAppender(BacktraceAppender.NAME);
//        x.getBacktraceClient().close();
        x.await();
        System.out.println("WORKS");
    }
}