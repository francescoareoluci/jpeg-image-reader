package jpeg_image_reader;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

public class ImageLoaderThread implements Runnable {
	
	public ImageLoaderThread(ConcurrentHashMap<String, BufferedImage> hashMap, 
								AtomicBoolean loadCompleted, 
								ArrayList<String> imagesPath, 
								AtomicInteger completedThreads, 
								int number, BlockingQueue<Integer> queue,
								int timeout)
	{
		this.concurrentMap = hashMap;
		this.loadCompleted = loadCompleted;
		this.imagesPath = imagesPath;
		this.completedThreads = completedThreads;
		this.totalThreads = number;
		this.queue = queue;
		this.timeout = timeout;
	}
	
	@Override
	public void run() {
		BufferedImage img = null;
        	
        for (String path : imagesPath) {
        	try {
        		img = ImageIO.read(new File(path));
        		if (img != null) {
        			this.concurrentMap.put(path, img);
        			this.queue.add(1);
        			if (timeout != 0) {
        				try {
        					Thread.sleep(timeout);
        				} catch (InterruptedException e) {
        					e.printStackTrace();
        				}
        			}
        		}
        	}
        	catch (IOException e) {
        		e.printStackTrace();
        		System.out.println("Unable to load image in " + path);
        	}
    		catch (OutOfMemoryError e) {
    			e.printStackTrace();
    			System.out.println("Cannot read more images");
    		}	
        }
        	
        int value = this.completedThreads.incrementAndGet();
        if (value == this.totalThreads) {
        	this.loadCompleted.set(true);
        }
    }
		
	private ConcurrentHashMap<String, BufferedImage> concurrentMap;		///< Reference to the caller map
	private ArrayList<String> imagesPath;																	///< Paths of images to be loaded
	private AtomicBoolean loadCompleted;																	///< Reference to the caller completed boolean
	private AtomicInteger completedThreads;																///< Reference to the caller completed threads
	private int totalThreads;																								///< Total number of threads created for the load
	private BlockingQueue<Integer> queue;																///< Reference to the caller blocking queue
	private int timeout;
}