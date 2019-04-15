package com.jule.core.utils.fifo;

import com.jule.core.utils.ExecuteWrapper;
import com.jule.core.utils.ThreadPoolManager;

/**
 * @author xujian
 */
public abstract class FIFORunnableQueue<T extends Runnable> extends FIFOSimpleExecutableQueue<T> {


	@Override
	protected final void removeAndExecuteFirst() {
		ExecuteWrapper.execute(removeFirst(), ThreadPoolManager.MAXIMUM_RUNTIME_IN_MILLISEC_WITHOUT_WARNING);
	} 
}
