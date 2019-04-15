package com.jule.core.service;

import java.util.concurrent.SynchronousQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPoolManager {
	
	private static ThreadPoolManager ins;
	
	private ThreadPoolExecutor pool;

	public ThreadPoolManager() {		
		pool = new ThreadPoolExecutor(8, 32, 60, TimeUnit.SECONDS, new SynchronousQueue<Runnable>());
	}

	
	public static ThreadPoolManager getInstance(){
		if(ins == null)
			ins = new ThreadPoolManager();
		return ins;
	}
	
	public void addTask(Runnable runnable) {
		pool.execute(runnable);
	}

	public long getTaskSum() {
		return pool.getQueue().size();
	}

	public static void shutdown() {
		if(ins!=null)
			ins.shutdown();
	}

}
