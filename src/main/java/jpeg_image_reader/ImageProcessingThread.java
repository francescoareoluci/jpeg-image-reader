package jpeg_image_reader;

import java.awt.image.BufferedImage;
import java.util.concurrent.Callable;

public class ImageProcessingThread implements Callable<BufferedImage> {

	public ImageProcessingThread(Image image) 
	{
		this.image = image;
	}
	
	@Override
	public BufferedImage call() 
	{
		BufferedImage gs = this.image.grayscaleConversion();
		return gs;
	}
	
	private Image image;		///< Reference  the caller image
}
