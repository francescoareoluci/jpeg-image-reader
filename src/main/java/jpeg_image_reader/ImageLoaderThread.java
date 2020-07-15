package jpeg_image_reader;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

public class ImageLoaderThread implements Runnable {
	
	public ImageLoaderThread(ConcurrentHashMap<String, BufferedImage> hashMap, 
								AtomicBoolean completed, 
								ArrayList<String> imagePaths, 
								AtomicInteger threadsCompleted, 
								int number)
	{
		this.concurrentMap = hashMap;
		this.completed = completed;
		this.imagePaths = imagePaths;
		this.thNumber = threadsCompleted;
		this.totalThreads = number;
	}
	
	public void run() {
		BufferedImage img = null;
        	
        for (String path : imagePaths) {
        	try {
        		img = ImageIO.read(new File(path));
        		if (img != null) {
        			this.concurrentMap.put(path, img);
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
        	
        int value = this.thNumber.incrementAndGet();
        if (value == this.totalThreads) {
        	this.completed.set(true);
      }
    }
		
	private ConcurrentHashMap<String, BufferedImage> concurrentMap;
	private ArrayList<String> imagePaths;
	private AtomicBoolean completed;
	private AtomicInteger thNumber;
	private int totalThreads;
}