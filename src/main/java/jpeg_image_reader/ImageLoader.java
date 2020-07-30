package jpeg_image_reader;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

import javax.imageio.ImageIO;

public class ImageLoader {
		
		private static int POLL_TIMEOUT_MS = 500;
		enum ThreadType {
			NO_POOL_THREAD,
			POOL_THREAD,
		};
	
		public ImageLoader() 
		{
			this.loadedPath = new String();
			this.imageMap = new ConcurrentHashMap<String, BufferedImage>();
			this.threadPool = ThreadPool.getThreadPool();
			this.loadCompleted = new AtomicBoolean(true);
			this.completedThreads = new AtomicInteger(0);
		}
	
		public String getLoadedPath()
		{
			return this.loadedPath;
		}
		
		public int getNumberOfImages()
		{
			return this.imageMap.size();
		}
		
		public boolean isMapEmpty()
		{
			return this.imageMap.isEmpty();
		}
		
		public boolean getCompleted()
		{
			return this.loadCompleted.get();
		}
		
		/**
		 * Wait for producers to insert images in map.
		 * This is done by polling a blocking queue 
		 * that threads use to signal that an image has been
		 * added.
		 * 
		 */
		public void waitForProducer()
		{
				try {
					this.queue.poll(POLL_TIMEOUT_MS, TimeUnit.MILLISECONDS);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
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
			
			if (!this.loadCompleted.get()) {
				System.out.println("A loading is ongoing, cannot resize pool");
				return false;
			}
			
			this.threadPool.setThreadSize(size);
			return true;
		}
	
		/**
		 * This method can be used to sequentially load a 
		 * set of images from the requested path
		 *
		 * @param path: directory from which images should be loaded
		 * @return		true if successful, false otherwise
		 */
		public boolean loadImages(String path, int timeout)
		{
			// Check if path is a directory
			File dir = new File(path);
			if (!dir.isDirectory()) {
				System.out.println("Requested path is not a directory");
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
						if (timeout != 0) {
							try {
								Thread.sleep(timeout);
							} catch (InterruptedException e) {
								e.printStackTrace();
							}
						}
					}
				}
			}
	
			return true;
		}
		
		/**
		 * This method can be used to load in parallel
		 * mode the requested images
		 *
		 * @param path: directory from which images should be loaded
		 * @return		thread list that can be used to wait for termination
		 * 				if pool is not used
		 */
		public ArrayList<Thread> parallelLoadImages(String path, int threadsNumbers, ThreadType type, int timeout)
		{
			ArrayList<Thread> threadList = new ArrayList<Thread>();
			
			// Check if path is a directory
			File dir = new File(path);
			if (!dir.isDirectory()) {
				System.out.println("Requested path is not a directory");
				return threadList;
			}
			
			this.loadCompleted.set(false);
			this.completedThreads.set(0);
						
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
			
			this.queue = new ArrayBlockingQueue<Integer>(paths.size());
			int imagesPerThread = Math.floorDiv(paths.size(), threadsNumbers) + 1;
			int start = 0;
			int stop = 0;
			
			// Evaluate number of threads
			int end = threadsNumbers > paths.size() ? paths.size() : threadsNumbers;
						
			for (int i = 0; i < end; i++) {
				// Evaluate number of images per thread
				if (i != 0) {
					start = stop;
				}
				stop = (i + 1) * imagesPerThread;
				if ((stop > paths.size()) || (i == threadsNumbers - 1)) {
					stop = paths.size();
				}
				
				ImageLoaderThread imt = new ImageLoaderThread(this.imageMap,
						this.loadCompleted,
						new ArrayList<String>(paths.subList(start, stop)),
						this.completedThreads, 
						end, this.queue, timeout);
				
				if (type == ThreadType.POOL_THREAD) {
					// Start loading by submitting a task to the pool
					threadPool.submitThread(imt);
				}
				else {
					// Start loading by starting a thread
					Thread th = new Thread(imt);
					th.start();
					threadList.add(th);
				}
			}
		
			return threadList;
		}
		
		/**
		 * This method can be used to load in parallel
		 * mode the requested images using Callables
		 *
		 * @param path: directory from which images should be loaded
		 * @return		list of futures
		 */
		public ArrayList<Future<Integer>> callableLoadImages(String path, int threadsNumbers)
		{
			ArrayList<Future<Integer>> futureList = new ArrayList<Future<Integer>>();
			
			// Check if path is a directory
			File dir = new File(path);
			if (!dir.isDirectory()) {
				System.out.println("Requested path is not a directory");
				return futureList;
			}
			
			this.loadCompleted.set(false);
			this.completedThreads.set(0);
						
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

			for (int i = 0; i < end; i++) {
				if (i != 0) {
					start = stop;
				}
				stop = (i + 1) * imagesPerThread;
				if ((stop > paths.size()) || (i == threadsNumbers - 1)) {
					stop = paths.size();
				}
				
				futureList.add(threadPool.submitCallableReader(new ImageLoaderCallable(this.imageMap,
																							new ArrayList<String>(paths.subList(start, stop)))));
			}
		
			return futureList;
		}
		
		/**
		 * This method can be used to retrieve an image from
		 * the loaded map, specifying its absolute path
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
			if (this.imageMap.isEmpty()) {
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
		
		private String loadedPath;																				///< Path from which images are loaded
		private ConcurrentHashMap<String, BufferedImage> imageMap;		///< Map containing images
		private ThreadPool threadPool;																	///< Thread pool to handle parallel image loading
		private AtomicBoolean loadCompleted;														///< Atomic flag to signal loading completion
		private AtomicInteger completedThreads;													///< Atomic integer to count the number of completed threads
		private BlockingQueue<Integer> queue;													///< Using this queue, client can wait for producer to insert element in map
}
