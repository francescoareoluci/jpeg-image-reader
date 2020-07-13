package jpeg_image_reader;

import java.awt.image.BufferedImage;
import java.util.ArrayList;


public class ImageReader {
	
	public static String DEFAULT_FOLDER = "/home/francesco/misc/STS-Workspace/jpeg_image_reader/images";

	public static void main(String[] args) {
		System.out.println("======= jpeg_image_reader =======");
		
		// Test bench usage
		if (args.length > 0) {
			BenchmarkSuite bs = new BenchmarkSuite();
			switch (Integer.parseInt(args[0])) {
				case 1:
					bs.performSequentialLoadBench(DEFAULT_FOLDER);
					break;
				case 2:
					bs.performParallelLoadBench(DEFAULT_FOLDER);
					break;
				case 3:
					bs.performSequentialLoadOpBench(DEFAULT_FOLDER);
					break;
				case 4:
					bs.performParallelLoadOpBench(DEFAULT_FOLDER);
					break;
				default:
					bs.performSequentialLoadBench(DEFAULT_FOLDER);
					break;
			}
			return;
		}
		
		ImageLoader imageLoader = new ImageLoader();
		
		// Sequential load images
		long startTime = System.currentTimeMillis();
		if (!imageLoader.loadImages(DEFAULT_FOLDER)) {
			System.out.println("Unable to load requested folder");
			return;
		}
		long endTime = System.currentTimeMillis();
	    System.out.println("Total execution time: " + (endTime - startTime) + "ms"); 
		
		
		// Get number of images
		int loadedImagesCount = imageLoader.getNumberOfImages();
		System.out.println("Number of images loaded: " + loadedImagesCount);
		
		// Get loaded path
		String loadedImageFolder = imageLoader.getLoadedPath();
		System.out.println("Loaded path: " + loadedImageFolder);
		
		if (loadedImagesCount == 0 || loadedImageFolder == "") {
			System.out.println("No images has been loaded");
			return;
		}
		
		// Get all loaded image paths
		ArrayList<String> paths = imageLoader.getLoadedImagePaths();
		for (String path : paths) {
			//System.out.println(path);
		}
		
		if (paths.isEmpty()) {
			System.out.println("No images has been loaded");
			return;
		}
		
		// Load image using its path
		BufferedImage image = imageLoader.getImage(paths.get(0));
		if (image != null) {
			//System.out.println("Loaded image " + paths.get(0));
		}
		
		// Load image using its index in map
		if (imageLoader.getNumberOfImages() > 1) {
			BufferedImage image2 = imageLoader.getImage(2);
			//System.out.println("Loaded image number 2, width: " + image2.getWidth());
		}
		
		// Clear the loader
		boolean result = imageLoader.resetImages();
		System.out.println("Clean result: " + result);
		
		// Parallel loading
		imageLoader.parallelLoadImages(DEFAULT_FOLDER);
		
		startTime = System.currentTimeMillis();
		while (!imageLoader.getCompleted()) {}
		endTime = System.currentTimeMillis();
	    System.out.println("Total execution time: " + (endTime - startTime) + "ms"); 
		
		paths = imageLoader.getLoadedImagePaths();
		for (String path : paths) {
			//System.out.println(path);
		}
		
		if (paths.isEmpty()) {
			System.out.println("Exit");
			imageLoader.closeImageLoader();
			return;
		}
		
		// Load image using its path
		image = imageLoader.getImage(paths.get(0));
		if (image != null) {
			//System.out.println("Loaded image " + paths.get(0));
		}
		
		// Grayscale conversion
		Image img = new Image(image);
		Image grayscaleImage = new Image(img.grayscaleConversion());
		grayscaleImage.saveImage("result.jpg");
		
		// Number of images
		System.out.println("Size: " + imageLoader.getNumberOfImages());
		
		// Pop images
		startTime = System.currentTimeMillis();
		int size = imageLoader.getNumberOfImages();
		for (int i = 0; i < size; i++) {
			image = imageLoader.popImage();
			if (image != null) {
				//img.getFirstPixel();
				img.getMean();
				//System.out.println("Image width: " + image.getWidth() + " height: " + image.getHeight());
			}
		}
		endTime = System.currentTimeMillis();
		System.out.println("Total execution time: " + (endTime - startTime) + "ms"); 
		System.out.println("Size: " + imageLoader.getNumberOfImages());
			    
		startTime = System.currentTimeMillis();
		// Parallel loading
		imageLoader.parallelLoadImages(DEFAULT_FOLDER);
		
		// Pop images while still loading
		int count = 0;
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
		endTime = System.currentTimeMillis();
	    System.out.println("Parallel + extraction execution time: " + (endTime - startTime) + "ms"); 
		
		imageLoader.resetImages();
		
		// Sequential load images
		startTime = System.currentTimeMillis();
		if (!imageLoader.loadImages(DEFAULT_FOLDER)) {
			System.out.println("Unable to load requested folder");
			return;
		}
		endTime = System.currentTimeMillis();
		System.out.println("Sequential execution time: " + (endTime - startTime) + "ms"); 
		count = 0;
		while (true) {
			if (count == size) {
				break;
			}
				
			if (image != null) {
				img.setImage(image);
				img.getMean();
				count++;
			}
		}
		System.out.println(count);
		endTime = System.currentTimeMillis();
		System.out.println("Sequential + extraction execution time: " + (endTime - startTime) + "ms"); 
		
		// Close thread pool
		imageLoader.closeImageLoader();
		
		// Clear the loader
		result = imageLoader.resetImages();
		System.out.println("Clean result: " + result);
		
		return;
	}
}
