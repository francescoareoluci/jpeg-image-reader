package jpeg_image_reader;

import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;

public class ThreadPool {
	static int DEFAULT_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();
	
	private ThreadPool() 
	{
		this.pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);
		this.pool.prestartAllCoreThreads();
	}
	
	// static method to create instance of Singleton class 
    public static ThreadPool getThreadPool() 
   { 
       // To ensure only one instance is created 
    	if (instance == null) { 
    		instance = new ThreadPool(); 
      }
    	
        return instance; 
    } 
	
	public void submitThread(Runnable r)
	{
		this.pool.submit(r);
	}
	
	public long getCompletedTask() 
	{
		return this.pool.getCompletedTaskCount();
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
		
		this.pool.setCorePoolSize(size);
		return true;
	}
	
	public int getPoolSize()
	{
		System.out.println(this.pool.getPoolSize());
		return this.pool.getPoolSize();
	}
	
	/**
	 * Shutdown the thread pool
	 *
	 * @return	true if successful, false otherwise
	 */
	public boolean closePool()
	{
		this.pool.shutdown();
		return true;
	}
	
	private ThreadPoolExecutor pool;				///< Thread pool to handle parallel image loading
    private static ThreadPool instance = null; 
}
