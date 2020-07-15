package jpeg_image_reader;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

public class ImageLoader {
	
		static int DEFAULT_THREAD_POOL_SIZE = Runtime.getRuntime().availableProcessors();
	
		public ImageLoader() 
		{
			this.loadedPath = new String();
			this.imageMap = new ConcurrentHashMap<String, BufferedImage>();
			this.threadPool = ThreadPool.getThreadPool();
			this.completed = new AtomicBoolean(true);
			this.threadsCompleted = new AtomicInteger(0);
			//this.threadPool.prestartAllCoreThreads();
		}
	
		public String getLoadedPath()
		{
			return this.loadedPath;
		}
		
		public int getNumberOfImages()
		{
			return this.imageMap.size();
		}
		
		public boolean getCompleted()
		{
			return this.completed.get();
		}
		
		/**
		 * Set thread pool size
		 *
		 * @param size: the pool thread size
		 * @return	    true if successful, false otherwise
		 */
		public boolean setThreadSize(int size)
		{
			if (size <= 0) {
				System.out.println("Invalid thread pool size");
				return false;
			}
			
			if (!this.completed.get()) {
				System.out.println("A loading is ongoing, cannot resize pool");
				return false;
			}
			
			this.threadPool.setThreadSize(size);
			return true;
		}
	
		/**
		 * This method can be used to load a set of images 
		 * from the requested path
		 *
		 * @param path: directory from which images should be loaded
		 * @return		true if successful, false otherwise
		 */
		public boolean loadImages(String path)
		{
			// Check if path is a directory
			File dir = new File(path);
			if (!dir.isDirectory()) {
				// TODO: log
				return false;
			}
			
			this.loadedPath = path;
			
			// Iterate in path for jpg images
			File[] files = new File(path).listFiles();
			BufferedImage img;
			String fileName = new String();
			for (File file : files) {
				if (file.isDirectory()) {
					continue;
				}
				
				fileName = file.getName();
				if (fileName.endsWith("jpg") ||
						fileName.endsWith("jpeg")) {
					img = this.loadImage(file.getAbsolutePath());
					if (img != null) {
						this.imageMap.put(file.getAbsolutePath(), img);
					}
				}
			}
	
			return true;
		}
		
		/**
		 * This method can be used to load in parallel
		 * mode the requested images using the thread pool
		 *
		 * @param path: directory from which images should be loaded
		 * @return		true if successful, false otherwise
		 */
		public boolean parallelLoadImages(String path, int threadsNumbers)
		{
			// Check if path is a directory
			File dir = new File(path);
			if (!dir.isDirectory()) {
				return false;
			}
			
			this.completed.set(false);
			this.threadsCompleted.set(0);
						
			this.loadedPath = path;
						
			// Iterate in path for jpg images
			File[] files = new File(path).listFiles();
			String fileName = new String();
			ArrayList<String> paths = new ArrayList<String>();
			
			for (File file : files) {
				if (file.isDirectory()) {
					continue;
				}
							
				fileName = file.getName();
				if (fileName.endsWith("jpg") ||
						fileName.endsWith("jpeg")) {
					paths.add(file.getAbsolutePath());
				}
			}
			
			int imagesPerThread = Math.floorDiv(paths.size(), threadsNumbers) + 1;
			int start = 0;
			int stop = 0;
			
			int end = threadsNumbers > paths.size() ? paths.size() : threadsNumbers;
						
			long startTime = System.currentTimeMillis();
			for (int i = 0; i < end; i++) {
				if (i != 0) {
					start = stop;
				}
				stop = (i + 1) * imagesPerThread;
				if ((stop > paths.size()) || (i == threadsNumbers - 1)) {
					stop = paths.size();
				}
				//System.out.println(start + " - " + stop);
				
				threadPool.submitThread(new ImageLoaderThread(this.imageMap,
																this.completed,
																new ArrayList<String>(paths.subList(start, stop)),
																this.threadsCompleted, 
																end));
			}
			long endTime = System.currentTimeMillis();
			System.out.println("Thread creation took: " + (endTime - startTime) + "ms"); 
		
			return true;
		}
		
		/**
		 * This method can be used to load in parallel
		 * mode the requested images without using a thread pool
		 *
		 * @param path: directory from which images should be loaded
		 * @return		true if successful, false otherwise
		 */
		public boolean parallelLoadImagesNoPool(String path, int threadsNumbers)
		{
			// Check if path is a directory
			File dir = new File(path);
			if (!dir.isDirectory()) {
				return false;
			}
			
			this.completed.set(false);
			this.threadsCompleted.set(0);
						
			this.loadedPath = path;
						
			// Iterate in path for jpg images
			File[] files = new File(path).listFiles();
			String fileName = new String();
			ArrayList<String> paths = new ArrayList<String>();
			
			for (File file : files) {
				if (file.isDirectory()) {
					continue;
				}
							
				fileName = file.getName();
				if (fileName.endsWith("jpg") ||
						fileName.endsWith("jpeg")) {
					paths.add(file.getAbsolutePath());
				}
			}
			
			int imagesPerThread = Math.floorDiv(paths.size(), threadsNumbers) + 1;
			int start = 0;
			int stop = 0;
			
			int end = threadsNumbers > paths.size() ? paths.size() : threadsNumbers;
						
			long startTime = System.currentTimeMillis();
			for (int i = 0; i < end; i++) {
				if (i != 0) {
					start = stop;
				}
				stop = (i + 1) * imagesPerThread;
				if ((stop > paths.size()) || (i == threadsNumbers - 1)) {
					stop = paths.size();
				}
				
				ImageLoaderThread imt = new ImageLoaderThread(this.imageMap,
																													this.completed,
																													new ArrayList<String>(paths.subList(start, stop)),
																													this.threadsCompleted, 
																													end);
				Thread th = new Thread(imt);
				th.start();
			}
			long endTime = System.currentTimeMillis();
			System.out.println("Thread creation took: " + (endTime - startTime) + "ms"); 
		
			return true;
		}
		
		/**
		 * This method can be used to retrieve an image from
		 * the loaded list, specifying its index in the list
		 *
		 * @param number: index number of the image that should be returned
		 * @return		  BufferedImage of the requested image, if not
		 *				  existing returns null
		 */
		public BufferedImage getImage(int number)
		{
			BufferedImage img = null;
			int idx = 0;
			
			for (String key : this.imageMap.keySet()) {
				if (number == idx) {
					img = this.imageMap.get(key);
					break;
				}
				idx++;
			}
			
			return img;
		}
		
		/**
		 * This method can be used to retrieve an image from
		 * the loaded list, specifying its absolute path
		 *
		 * @param filePath: absolute image path that should be returned
		 * @return			BufferedImage of the requested image, if not
		 *					existing returns null
		 */
		public BufferedImage getImage(String filePath)
		{
			BufferedImage img = null;
			img = this.imageMap.get(filePath);
		
			return img;
		}
		
		/**
		 * Method to retrieve paths of loaded images
		 *
		 * @return	an ArrayList containing all the paths of
		 *			the loaded images
		 */
		public ArrayList<String> getLoadedImagePaths()
		{
			ArrayList<String> paths = new ArrayList<String>();
			
			for (String key : this.imageMap.keySet()) {
				paths.add(key);
			}
			
			return paths;
		}
		
		/**
		 * Pop an image from the map
		 * 
		 * @return	the first BufferedImage of the map, if
		 *			map size is zero returns null
		 */
		public BufferedImage popImage()
		{
			BufferedImage img = null;
			if (this.getNumberOfImages() == 0) {
				return img;
			}
			
			// Get first element
			String firstKey = this.imageMap.keySet().stream().findFirst().get();
			img = this.imageMap.get(firstKey);
			this.imageMap.remove(firstKey);
			
			return img;
		}
		
		/**
		 * Reset the map containing the loaded images
		 *
		 * @return	true if successful, false otherwise
		 */
		public boolean resetImages()
		{			
			imageMap.clear();
			this.loadedPath = "";
			return true;
		}
		
		/**
		 * Shutdown the thread pool
		 *
		 * @return	true if successful, false otherwise
		 */
		public boolean closeImageLoader()
		{
			this.threadPool.closePool();
			return true;
		}
	
		/**
		 * Load an image from requested path
		 *
		 * @param filePath: path of the image to be loaded
		 * @return			true if successful, false otherwise
		 */
		private BufferedImage loadImage(String filePath)
		{
			BufferedImage img = null;
			try {
				img = ImageIO.read(new File(filePath));
			}
			catch (IOException e) {
				e.printStackTrace();
				System.out.println("Unable to load image in " + filePath);
			}
			catch (OutOfMemoryError e) {
				e.printStackTrace();
				System.out.println("Cannot read more images");
			}
			return img;
		}
	
		private String loadedPath;									///< Path from which images should be loaded
		private ConcurrentHashMap<String, BufferedImage> imageMap;	///< Map containing images
		private ThreadPool threadPool;				///< Thread pool to handle parallel image loading
		private AtomicBoolean completed;							///< Atomic flag for signal loading completion
		private AtomicInteger threadsCompleted;						///< Atomic integer to count the number of completed threads
}
