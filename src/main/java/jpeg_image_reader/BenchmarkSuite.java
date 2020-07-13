package jpeg_image_reader;

import java.awt.image.BufferedImage;

public class BenchmarkSuite {
	
	public BenchmarkSuite() {}
	
	public boolean performSequentialLoadBench(String folderPath)
	{
		ImageLoader imageLoader = new ImageLoader();
		
		long startTime = System.currentTimeMillis();
		
		if (!imageLoader.loadImages(folderPath)) {
			System.out.println("Unable to load requested folder");
			return false;
		}
		
		long endTime = System.currentTimeMillis();
		
		System.out.println("Sequential loading execution time: " + (endTime - startTime) + "ms"); 
		
		imageLoader.resetImages();
		imageLoader.closeImageLoader();
		
		return true;
	}
	
	public boolean performParallelLoadBench(String folderPath)
	{
		ImageLoader imageLoader = new ImageLoader();
		
		long startTime = System.currentTimeMillis();
		
		imageLoader.parallelLoadImages(folderPath);
		while (!imageLoader.getCompleted()) {}
		
		long endTime = System.currentTimeMillis();
		
		System.out.println("Parallel loading execution time: " + (endTime - startTime) + "ms"); 
		
		imageLoader.resetImages();
		imageLoader.closeImageLoader();
		
		return true;
	}
	
	public boolean performSequentialLoadOpBench(String folderPath)
	{
		ImageLoader imageLoader = new ImageLoader();
		// Keep a thread to execute operation
		imageLoader.setThreadSize(Runtime.getRuntime().availableProcessors() - 2);
		
		long startTime = System.currentTimeMillis();
		
		if (!imageLoader.loadImages(folderPath)) {
			System.out.println("Unable to load requested folder");
			return false;
		}
		
		int count = 0;
		int size = imageLoader.getNumberOfImages();
		BufferedImage image = null;
		Image img = new Image();
		
		while (true) {
			if (count == size) {
				break;
			}
			
			image = imageLoader.popImage();
				
			if (image != null) {
				img.setImage(image);
				img.getMean();
				count++;
			}
		}
		
		long endTime = System.currentTimeMillis();
		
		System.out.println("Sequential loading+op execution time: " + (endTime - startTime) + "ms"); 
		
		imageLoader.resetImages();
		imageLoader.closeImageLoader();
		
		return true;
	}
	
	public boolean performParallelLoadOpBench(String folderPath)
	{
		ImageLoader imageLoader = new ImageLoader();
		
		long startTime = System.currentTimeMillis();
		
		imageLoader.parallelLoadImages(folderPath);
		
		int count = 0;
		int size = imageLoader.getNumberOfImages();
		BufferedImage image = null;
		Image img = new Image();
		
		while (true) {
			// TODO: how to know number of images a priori?
			if (count == 300) {
				break;
			}
			
			image = imageLoader.popImage();
				
			if (image != null) {
				img.setImage(image);
				img.getMean();
				count++;
			}
		}
		
		long endTime = System.currentTimeMillis();
		
		System.out.println("Parallel loading+op execution time: " + (endTime - startTime) + "ms"); 
		
		imageLoader.resetImages();
		imageLoader.closeImageLoader();
		
		return true;
	}

}
