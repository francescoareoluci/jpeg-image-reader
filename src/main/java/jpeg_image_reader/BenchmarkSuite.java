package jpeg_image_reader;

import java.awt.image.BufferedImage;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public final class BenchmarkSuite {
	
	private BenchmarkSuite() {}
	
	public static boolean runSequentialLoad(String folderPath, int sleepTime)
	{
		System.out.println("Starting sequential load of images in path " + folderPath);
		
		ImageLoader imageLoader = new ImageLoader();
		ThreadPool pool = ThreadPool.getThreadPool();
		
		long startTime = System.currentTimeMillis();
		
		if (!imageLoader.loadImages(folderPath, sleepTime)) {
			System.out.println("Unable to load requested folder");
			return false;
		}
		
		long endTime = System.currentTimeMillis();
		
		System.out.println("Readed " + imageLoader.getNumberOfImages() + " images");
		System.out.println("Sequential loading execution time: " + (endTime - startTime) + "ms"); 
		
		imageLoader.resetImages();
		pool.closePool();
		
		return true;
	}
	
	public static boolean runParallelLoad(String folderPath, int threadsNumber,  int sleepTime)
	{
		System.out.println("Starting parallel load using thread pool of images in path " + folderPath);
		
		ImageLoader imageLoader = new ImageLoader();
		ThreadPool pool = ThreadPool.getThreadPool();
		
		long startTime = System.currentTimeMillis();
		
		imageLoader.parallelLoadImages(folderPath, threadsNumber, ImageLoader.ThreadType.POOL_THREAD, sleepTime);
		
		// Sync wait for termination of tasks in pool
		pool.closePool();
		pool.waitTermination();
		
		long endTime = System.currentTimeMillis();
		
		System.out.println("Readed " + imageLoader.getNumberOfImages() + " images");
		System.out.println("Parallel loading execution time: " + (endTime - startTime) + "ms"); 
		
		imageLoader.resetImages();
		
		return true;
	}
	
	public static boolean runCallableLoad(String folderPath, int threadsNumber, int sleepTime)
	{
		System.out.println("Starting parallel load using callables of images in path " + folderPath);
		
		ImageLoader imageLoader = new ImageLoader();
		ThreadPool pool = ThreadPool.getThreadPool();
		
		long startTime = System.currentTimeMillis();
		
		ArrayList<Future<Integer>> futureList = new ArrayList<Future<Integer>>();
		futureList = imageLoader.callableLoadImages(folderPath, threadsNumber);
		int imagesCount = 0;
		
		// Sync wait for callables to finish
		for (Future<Integer> f : futureList) {
			try {
				imagesCount += f.get();
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		
		long endTime = System.currentTimeMillis();
		
		System.out.println("Readed " + imagesCount + " images");
		System.out.println("Parallel loading (callable) execution time: " + (endTime - startTime) + "ms"); 
		
		imageLoader.resetImages();
		pool.closePool();
		
		return true;
	}
	
	public static boolean runParallelLoadNoPool(String folderPath, int threadsNumber, int sleepTime)
	{
		System.out.println("Starting parallel load using threads of images in path " + folderPath);
		
		ImageLoader imageLoader = new ImageLoader();
		ThreadPool pool = ThreadPool.getThreadPool();
		
		long startTime = System.currentTimeMillis();
		
		ArrayList<Thread> threadList;
		threadList = imageLoader.parallelLoadImages(folderPath, threadsNumber, ImageLoader.ThreadType.NO_POOL_THREAD, 0);
		
		// Sync wait thread termination
		for (Thread t : threadList) {
			try {
				t.join();
			} catch (InterruptedException e) {
				e.printStackTrace();
			}
		}
		
		long endTime = System.currentTimeMillis();
		
		System.out.println("Readed " + imageLoader.getNumberOfImages() + " images");
		System.out.println("Parallel loading (without pool) execution time: " + (endTime - startTime) + "ms"); 
		
		imageLoader.resetImages();
		pool.closePool();
		
		return true;
	}
	
	public static boolean runSequentialLoadOp(String folderPath, int sleepTime)
	{
		System.out.println("Starting sequential load and processing of images in path " + folderPath);
		
		ImageLoader imageLoader = new ImageLoader();
		ThreadPool pool = ThreadPool.getThreadPool();
		
		long startTime = System.currentTimeMillis();
		
		if (!imageLoader.loadImages(folderPath, sleepTime)) {
			System.out.println("Unable to load requested folder");
			return false;
		}
		
		long endTime = System.currentTimeMillis();
		long loadTime = endTime - startTime;
		
		System.out.println("Sequential image loading took: " + (loadTime) + "ms"); 
	
		startTime = System.currentTimeMillis();
		
		BufferedImage image = null;
		Image img = new Image();
		ArrayList<BufferedImage> gsImages = new ArrayList<BufferedImage>();
		
		while (imageLoader.getNumberOfImages() != 0) {
			image = imageLoader.popImage();
				
			if (image != null) {
				img.setImage(image);
				gsImages.add(img.grayscaleConversion());
			}
		}
		
		endTime = System.currentTimeMillis();
		long processingTime = endTime - startTime;
		
		System.out.println("Sequential image processing took: " + (processingTime) + "ms"); 
		System.out.println("Converted " + gsImages.size() + " images to grayscale");
		System.out.println("Sequential loading + processing execution time: " + (loadTime + processingTime) + "ms"); 
		
		imageLoader.resetImages();
		pool.closePool();
		
		return true;
	}
	
	public static boolean runSequentialLoadParallelOp(String folderPath, int sleepTime)
	{
		System.out.println("Starting sequential load and parallel processing of images in path " + folderPath);
		
		ImageLoader imageLoader = new ImageLoader();
		ThreadPool pool = ThreadPool.getThreadPool();
		
		long startTime = System.currentTimeMillis();
		
		if (!imageLoader.loadImages(folderPath, sleepTime)) {
			System.out.println("Unable to load requested folder");
			return false;
		}
		
		long endTime = System.currentTimeMillis();
		long loadTime = endTime - startTime;
		System.out.println("Sequential image loading took: " + (loadTime) + "ms"); 
	
		BufferedImage image = null;
		Image img = new Image();
		ArrayList<BufferedImage> gsImages = new ArrayList<BufferedImage>();
		ArrayList<Future<BufferedImage>> futureList = new ArrayList<Future<BufferedImage>>();
		
		startTime = System.currentTimeMillis();
		
		while (imageLoader.getNumberOfImages() != 0) {
			image = imageLoader.popImage();
				
			if (image != null) {
				img.setImage(image);
				futureList.add(pool.submitCallableProcess(new ImageProcessingThread(img)));
			}
		}
		
		System.out.println("All images have been assigned to Callables for processing...");
		
		for (Future<BufferedImage> f : futureList) {
			try {
				// Sync wait
				gsImages.add(f.get());
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		
		endTime = System.currentTimeMillis();
		long processingTime = endTime - startTime;
		
		System.out.println("Parallel image processing took: " + (processingTime) + "ms"); 
		System.out.println("Converted " + gsImages.size() + " images to grayscale");
		System.out.println("Sequential loading + parallel processing execution time: " + (loadTime + processingTime) + "ms"); 
		
		imageLoader.resetImages();
		pool.closePool();
		
		return true;
	}
	
	public static boolean runParallelLoadOp(String folderPath, int threadsNumber, int sleepTime)
	{
		System.out.println("Starting parallel load and async processing of images in path " + folderPath);
		
		ImageLoader imageLoader = new ImageLoader();
		ThreadPool pool = ThreadPool.getThreadPool();
		BufferedImage image = null;
		Image img = new Image();
		ArrayList<BufferedImage> gsImages = new ArrayList<BufferedImage>();
		ArrayList<Future<BufferedImage>> futureList = new ArrayList<Future<BufferedImage>>();
		
		long startTime = System.currentTimeMillis();
		
		imageLoader.parallelLoadImages(folderPath, threadsNumber, ImageLoader.ThreadType.POOL_THREAD, sleepTime);
		
		// Async pop images and assign them to callables
		while (!imageLoader.getCompleted() || !imageLoader.isMapEmpty()) {
			imageLoader.waitForProducer();
			image = imageLoader.popImage();

			if (image != null) {
				img.setImage(image);
				futureList.add(pool.submitCallableProcess(new ImageProcessingThread(img)));
			}
		}
		
		long endTime = System.currentTimeMillis();
		long loadTime = endTime - startTime;
		System.out.println("Async parallel image loading took: " + (loadTime) + "ms"); 
		
		System.out.println("All images have been assigned to Callables for processing...");
		
		for (Future<BufferedImage> f : futureList) {
			try {
				// Sync wait
				gsImages.add(f.get());
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		
		endTime = System.currentTimeMillis();
		
		System.out.println("Converted " + gsImages.size() + " images to grayscale");
		System.out.println("Parallel loading + parallel processing execution time: " + (endTime - startTime) + "ms"); 
		
		imageLoader.resetImages();
		pool.closePool();
		
		return true;
	}
	
	public static boolean runParallelLoadOpSync(String folderPath, int threadsNumber, int sleepTime)
	{
		System.out.println("Starting parallel load and async processing of images in path " + folderPath);
		
		ImageLoader imageLoader = new ImageLoader();
		ThreadPool pool = ThreadPool.getThreadPool();
		
		long startTime = System.currentTimeMillis();
		
		imageLoader.parallelLoadImages(folderPath, threadsNumber, ImageLoader.ThreadType.POOL_THREAD, sleepTime);
		
		BufferedImage image = null;
		Image img = new Image();
		ArrayList<BufferedImage> gsImages = new ArrayList<BufferedImage>();
		ArrayList<Future<BufferedImage>> futureList = new ArrayList<Future<BufferedImage>>();
		
		while(!imageLoader.getCompleted()){
			imageLoader.waitForProducer();
		}
		
		long endTime = System.currentTimeMillis();
		long loadTime = endTime - startTime;
		System.out.println("Sync parallel image loading took: " + (loadTime) + "ms"); 
		
		startTime = System.currentTimeMillis();
		
		// Sync pop images and assign them to callables
		while (!imageLoader.isMapEmpty()) {
			image = imageLoader.popImage();
				
			if (image != null) {
				img.setImage(image);
				futureList.add(pool.submitCallableProcess(new ImageProcessingThread(img)));
			}
		}
		
		System.out.println("All images have been assigned to Callables for processing...");
		
		for (Future<BufferedImage> f : futureList) {
			try {
				// Sync wait
				gsImages.add(f.get());
			} catch (InterruptedException e) {
				e.printStackTrace();
			} catch (ExecutionException e) {
				e.printStackTrace();
			}
		}
		
		endTime = System.currentTimeMillis();
		long processingTime = endTime - startTime;
		
		System.out.println("Parallel image processing took: " + (processingTime) + "ms"); 
		System.out.println("Converted " + gsImages.size() + " images to grayscale");
		System.out.println("Parallel loading + parallel processing execution time: " + (loadTime + processingTime) + "ms"); 
		
		imageLoader.resetImages();
		pool.closePool();
		
		return true;
	}

}
