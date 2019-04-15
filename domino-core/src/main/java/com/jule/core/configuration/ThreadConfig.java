package com.jule.core.configuration;

public class ThreadConfig {

    /**
     * Default thread pool max size.
     */
    @Property(key = "thread.pool.max.size", defaultValue = "4")
    public static int THREAD_POOL_MAX_SIZE = 4;

    @Property(key = "db.thread.pool.max.size", defaultValue = "4")
    public static int DB_THREAD_POOL_MAX_SIZE = 4;

    @Property(key = "childGroup.threads.max.size", defaultValue = "50")
    public static int CHILD_GROUP_THREADS = 50;
}
