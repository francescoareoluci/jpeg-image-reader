package jpeg_image_reader;

import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;
import java.util.concurrent.Future;
import java.util.concurrent.atomic.AtomicInteger;

public class ImageProcessingThread implements Callable<BufferedImage> {

	public ImageProcessingThread(Image image) 
	{
		this.image = image;
	}
	
	public BufferedImage call() 
	{
		BufferedImage gs = this.image.grayscaleConversion();
		return gs;
	}
	
	private Image image;
}
