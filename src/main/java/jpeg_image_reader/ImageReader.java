package jpeg_image_reader;

public class ImageReader {
	
	public static int DEFAULT_LOADER_THREADS = 1;

	public static void main(String[] args) {
		System.out.println("======= jpeg_image_reader =======");
		
		String sourceFolder = "";
		if (args.length == 0) {
			System.out.println("Source images folder not specified");
			System.out.println("Usage: java -jar jpeg_image_loader source_folder operation");
			System.out.println("source_folder: directory containing jpg images to be loaded");
			System.out.println("operation: optional - specify how to load images and processing. Default: 1");
			System.out.println("	1: Sequential load of source_folder jpg images.");
			System.out.println("	2: Parallel load using a thread pool");
			System.out.println("	3: Parallel load using Callables");
			System.out.println("	4: Parallel load using simple threads");
			System.out.println("	5: Sequential load and sequential grayscale conversion of loaded images");
			System.out.println("	6: Sequential load and parallel grayscale conversion of loaded images");
			System.out.println("	7: Parallel load and parallel grayscale conversion of loaded images in async mode");
			return;
		}
		else {
			sourceFolder = args[0];
		}
		
		int sleepTime = 0;
		if (args.length > 2) {
			sleepTime = Integer.parseInt(args[2]);
			if (sleepTime < 0) {
				sleepTime = 0;
			}
		}
		
		// Test bench usage
		if (args.length > 1) {
			switch (Integer.parseInt(args[1])) {
				case 1:
					BenchmarkSuite.runSequentialLoad(sourceFolder, sleepTime);
					break;
				case 2:
					BenchmarkSuite.runParallelLoad(sourceFolder, DEFAULT_LOADER_THREADS, sleepTime);
					break;
				case 3:
					BenchmarkSuite.runCallableLoad(sourceFolder, DEFAULT_LOADER_THREADS, sleepTime);
					break;
				case 4:
					BenchmarkSuite.runParallelLoadNoPool(sourceFolder, DEFAULT_LOADER_THREADS, sleepTime);
					break;
				case 5:
					BenchmarkSuite.runSequentialLoadOp(sourceFolder, sleepTime);
					break;
				case 6:
					BenchmarkSuite.runSequentialLoadParallelOp(sourceFolder, sleepTime);
					break;
				case 7:
					BenchmarkSuite.runParallelLoadOp(sourceFolder, DEFAULT_LOADER_THREADS, sleepTime);
					break;
				case 8:
					BenchmarkSuite.runParallelLoadOpSync(sourceFolder, DEFAULT_LOADER_THREADS, sleepTime);
					break;
				default:
					BenchmarkSuite.runSequentialLoad(sourceFolder, sleepTime);
					break;
			}
			return;
		}
		
		// Default if no args are specified
		BenchmarkSuite.runSequentialLoad(sourceFolder, sleepTime);
		return;
	}
}
