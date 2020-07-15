package jpeg_image_reader;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPool {
	static int DEFAULT_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();
	
	public ThreadPool() 
	{
		this.threadPool = (ThreadPoolExecutor) Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);
	}
	
	public void submitThread(Runnable r)
	{
		this.threadPool.submit(r);
	}
	
	/**
	 * Set thread pool size
	 *
	 * @param size: the pool thread size
	 * @return	    	true if successful, false otherwise
	 */
	public boolean setThreadSize(int size)
	{
		if (size <= 0) {
			System.out.println("Invalid thread pool size");
			return false;
		}
		
		this.threadPool.setCorePoolSize(size);
		return true;
	}
	
	/**
	 * Shutdown the thread pool
	 *
	 * @return	true if successful, false otherwise
	 */
	public boolean closePool()
	{
		this.threadPool.shutdown();
		return true;
	}
	
	private ThreadPoolExecutor threadPool;				///< Thread pool to handle parallel image loading
}
