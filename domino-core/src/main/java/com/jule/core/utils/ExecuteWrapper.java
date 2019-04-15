package com.jule.core.utils;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * @author xujian
 */
public class ExecuteWrapper implements Runnable {
    private static final Logger logger = LoggerFactory.getLogger(ExecuteWrapper.class);

    private final Runnable runnable;

    private long maximumRuntimeInMillisecWithoutWarning = Long.MAX_VALUE;

    public ExecuteWrapper(Runnable runnable, long maximumRuntimeInMillisecWithoutWarning) {
        this.runnable = runnable;
        this.maximumRuntimeInMillisecWithoutWarning = maximumRuntimeInMillisecWithoutWarning;
    }

    @Override
    public final void run() {
        ExecuteWrapper.execute(runnable, getMaximumRuntimeInMillisecWithoutWarning());
    }

    public long getMaximumRuntimeInMillisecWithoutWarning() {
        return maximumRuntimeInMillisecWithoutWarning;
    }

    public static void execute(Runnable runnable, long maximumRuntimeInMillisecWithoutWarning) {
        long now = System.currentTimeMillis();
        try {
            runnable.run();
        } catch (Exception e) {
            logger.error("RunnableWrapper", e);
        } finally {
//            logger.debug(runnable.getClass() + " exec: " + (System.currentTimeMillis() - now) + " ms");

            if (System.currentTimeMillis() - now > maximumRuntimeInMillisecWithoutWarning) {
                logger.warn(runnable.getClass() + " exec: > " + maximumRuntimeInMillisecWithoutWarning + " ms");
            }
        }
    }
}
