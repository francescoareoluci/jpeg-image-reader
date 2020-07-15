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
	
	public boolean performParallelLoadBench(String folderPath, int threadsNumber)
	{
		ImageLoader imageLoader = new ImageLoader();
		
		long startTime = System.currentTimeMillis();
		
		imageLoader.parallelLoadImages(folderPath, threadsNumber);
		while (!imageLoader.getCompleted()) {}
		
		long endTime = System.currentTimeMillis();
		
		System.out.println("Parallel loading execution time: " + (endTime - startTime) + "ms"); 
		
		imageLoader.resetImages();
		imageLoader.closeImageLoader();
		
		return true;
	}
	
	public boolean performParallelLoadNoPoolBench(String folderPath, int threadsNumber)
	{
		ImageLoader imageLoader = new ImageLoader();
		
		long startTime = System.currentTimeMillis();
		
		imageLoader.parallelLoadImagesNoPool(folderPath, threadsNumber);
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
		ThreadPool pool = ThreadPool.getThreadPool();
		
		long startTime = System.currentTimeMillis();
		
		if (!imageLoader.loadImages(folderPath)) {
			System.out.println("Unable to load requested folder");
			return false;
		}
	
		BufferedImage image = null;
		Image img = new Image();
		long tasks = 0;
		
		while (imageLoader.getNumberOfImages() != 0) {
			image = imageLoader.popImage();
				
			if (image != null) {
				img.setImage(image);
				//pool.submitThread(new ImageProcessingThread(img));
				//img.getMean();
				img.grayscaleConversion();
				tasks++;
			}
		}
		
		//while (pool.getCompletedTask() != tasks) {}
		
		long endTime = System.currentTimeMillis();
		System.out.println("Sequential loading+op execution time: " + (endTime - startTime) + "ms"); 
		
		imageLoader.resetImages();
		imageLoader.closeImageLoader();
		
		return true;
	}
	
	public boolean performSequentialLoadParallelOpBench(String folderPath)
	{
		ImageLoader imageLoader = new ImageLoader();
		ThreadPool pool = ThreadPool.getThreadPool();
		
		long startTime = System.currentTimeMillis();
		
		if (!imageLoader.loadImages(folderPath)) {
			System.out.println("Unable to load requested folder");
			return false;
		}
	
		BufferedImage image = null;
		Image img = new Image();
		long tasks = 0;
		
		while (imageLoader.getNumberOfImages() != 0) {
			image = imageLoader.popImage();
				
			if (image != null) {
				img.setImage(image);
				pool.submitThread(new ImageProcessingThread(img));
				//img.getMean();
				//img.grayscaleConversion();
				tasks++;
			}
		}
		
		while (pool.getCompletedTask() != tasks) {}
		
		long endTime = System.currentTimeMillis();
		System.out.println("Sequential loading+op execution time: " + (endTime - startTime) + "ms"); 
		
		imageLoader.resetImages();
		imageLoader.closeImageLoader();
		
		return true;
	}
	
	public boolean performParallelLoadOpBench(String folderPath, int threadsNumber)
	{
		ImageLoader imageLoader = new ImageLoader();
		ThreadPool pool = ThreadPool.getThreadPool();
		
		long startTime = System.currentTimeMillis();
		
		imageLoader.parallelLoadImages(folderPath, threadsNumber);
		
		BufferedImage image = null;
		Image img = new Image();
		long tasks = threadsNumber;
		
		while (imageLoader.getNumberOfImages() == 0) {}
		while (!imageLoader.getCompleted() || imageLoader.getNumberOfImages() != 0) {
			// TODO: how to know number of images a priori?
			
			image = imageLoader.popImage();
				
			if (image != null) {
				img.setImage(image);
				pool.submitThread(new ImageProcessingThread(img));
				//img.getMean();
				tasks++;
			}
		}
		
		while (pool.getCompletedTask() != tasks) {}
		
		long endTime = System.currentTimeMillis();
		System.out.println("Parallel loading+op execution time: " + (endTime - startTime) + "ms"); 
		
		imageLoader.resetImages();
		imageLoader.closeImageLoader();
		
		return true;
	}

}
