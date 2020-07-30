package jpeg_image_reader;

import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

public class ThreadPool {
	static int DEFAULT_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors() - 2;
	
	private ThreadPool() 
	{
		this.pool = (ThreadPoolExecutor) Executors.newFixedThreadPool(DEFAULT_THREAD_POOL_SIZE);
		this.pool.setMaximumPoolSize(DEFAULT_THREAD_POOL_SIZE);
		this.pool.prestartAllCoreThreads();
	}
	
	/**
	 * Singleton constructor
	 * 
	 * @return the instance of ThreadPool
	 */
    public static ThreadPool getThreadPool() 
    { 
    	if (instance == null) { 
    		instance = new ThreadPool(); 
    	}
    	
        return instance; 
    }
    
    /**
     * Method to wait for thread in pool to terminate
     * 
     */
    public void waitTermination()
    {
    	try {
			pool.awaitTermination(24L, TimeUnit.HOURS);
		} catch (InterruptedException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
    }
    
    public long getCompletedTask() 
	{
		return this.pool.getCompletedTaskCount();
	}
    
    public int getPoolSize()
	{
		System.out.println(this.pool.getPoolSize());
		return this.pool.getPoolSize();
	}
    
    /**
     * This method can be used to submit 
     * a Runnable to the pool
     * 
     * @param r: Runnable
     */
    public void submitThread(Runnable r)
    {
    	// Submit a Runnable object
    	this.pool.submit(r);
    }
	
    /**
     * This method can be used to submit 
     * a Callable object to process images
     * 
     * @param c: Callable
     * @return		The Future containing the BufferedImage
     */
	public Future<BufferedImage> submitCallableProcess(Callable<BufferedImage> c)
	{
		// Submit a Callable object
		Future<BufferedImage> future;
		future = this.pool.submit(c);
		
		 return future;
	}
	
	/**
     * This method can be used to submit 
     * a Callable object to load images
     * 
     * @param c: Callable
     * @return		The Future containing the BufferedImage
     */
	public Future<Integer> submitCallableReader(Callable<Integer> c)
	{
		// Submit a Callable object
		Future<Integer> future;
		future = this.pool.submit(c);
		
		return future;
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
    private static ThreadPool instance = null; 		///< To implement Singleton constructor
}
