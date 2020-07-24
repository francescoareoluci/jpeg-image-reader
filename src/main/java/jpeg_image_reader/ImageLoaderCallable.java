package jpeg_image_reader;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.Callable;
import java.util.concurrent.ConcurrentHashMap;

import javax.imageio.ImageIO;

public class ImageLoaderCallable implements Callable<Integer> 
{
	public ImageLoaderCallable(ConcurrentHashMap<String, BufferedImage> hashMap, 
															ArrayList<String> imagePaths)
	{
		this.concurrentMap = hashMap;
		this.imagePaths = imagePaths;
	}
	
	public Integer call()
	{
		BufferedImage img = null;
		int readImgs = 0;
    	
        for (String path : imagePaths) {
        	try {
        		img = ImageIO.read(new File(path));
        		if (img != null) {
        			this.concurrentMap.put(path, img);
        			readImgs++;
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
        
        return readImgs;
	}
	
	private ConcurrentHashMap<String, BufferedImage> concurrentMap;		///< Reference to the caller map
	private ArrayList<String> imagePaths;								///< Paths of images to be loaded
}
