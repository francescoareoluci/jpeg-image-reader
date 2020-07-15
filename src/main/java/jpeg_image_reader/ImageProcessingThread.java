package jpeg_image_reader;

public class ImageProcessingThread implements Runnable {

	public ImageProcessingThread(Image image) 
	{
		this.image = image;
	}
	
	public void run() 
	{
		image.grayscaleConversion();
	}
	
	private Image image;
}
