package org.platformlayer.ops.log;

import org.apache.log4j.AppenderSkeleton;
import org.apache.log4j.Level;
import org.apache.log4j.Logger;
import org.apache.log4j.PatternLayout;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.platformlayer.ops.OpsContext;

public class PerJobAppender extends AppenderSkeleton {

    public static void attachToRootLogger() {
        Logger rootLogger = Logger.getRootLogger();
        PerJobAppender appender = new PerJobAppender();
        appender.setLayout(new PatternLayout("%d [%t] %-5p %c %x - %m%n"));
        rootLogger.addAppender(appender);
    }

    protected void append(LoggingEvent event) {
        OpsContext opsContext = OpsContext.get();

        if (opsContext != null) {
            String message = null;
            Object o = event.getMessage();
            if (o != null)
                message = o.toString();
            Level level = event.getLevel();
            int levelInt = level.toInt();

            String[] exceptionInfo = null;

            ThrowableInformation throwableInformation = event.getThrowableInformation();
            if (throwableInformation != null) {
                exceptionInfo = throwableInformation.getThrowableStrRep();
            }

            if (message != null || exceptionInfo != null) {
                opsContext.getJobLogger().logMessage(message, exceptionInfo, levelInt);

                if (levelInt >= Level.ERROR_INT) {
                    // String key = "warn-" + OpsSystem.buildSimpleTimeString() + "-" + (System.nanoTime() % 1000);
                    if (opsContext != null) { // && opsContext.getOperation() != null) {
                        if (exceptionInfo != null && exceptionInfo.length >= 1)
                            message += "; " + exceptionInfo[0];

                        opsContext.addWarning(null, message);
                    }
                }
            }
        }
    }

    public void close() {
    }

    public boolean requiresLayout() {
        return false;
    }
}