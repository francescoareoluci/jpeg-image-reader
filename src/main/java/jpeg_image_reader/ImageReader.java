package jpeg_image_reader;

public class ImageReader {
	
	public static int DEFAULT_LOADER_THREADS = Runtime.getRuntime().availableProcessors() / 2;

	public static void main(String[] args) {
		System.out.println("======= jpeg_image_reader =======");
		
		String sourceFolder = "";
		if (args.length == 0) {
			System.out.println("Source images folder not specified");
			System.out.println("Usage: java -jar jpeg_image_loader source_folder operation");
			System.out.println("source_folder: directory containing jpg images to be loaded");
			System.out.println("operation: optional - specify how to load images and processing");
			System.out.println("	1: Sequential load of source_folder jpg images. Default: 1");
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
		
		// Test bench usage
		if (args.length > 1) {
			switch (Integer.parseInt(args[1])) {
				case 1:
					BenchmarkSuite.runSequentialLoad(sourceFolder);
					break;
				case 2:
					BenchmarkSuite.runParallelLoad(sourceFolder, DEFAULT_LOADER_THREADS);
					break;
				case 3:
					BenchmarkSuite.runCallableLoad(sourceFolder, DEFAULT_LOADER_THREADS);
					break;
				case 4:
					BenchmarkSuite.runParallelLoadNoPool(sourceFolder, DEFAULT_LOADER_THREADS);
					break;
				case 5:
					BenchmarkSuite.runSequentialLoadOp(sourceFolder);
					break;
				case 6:
					BenchmarkSuite.runSequentialLoadParallelOp(sourceFolder);
					break;
				case 7:
					BenchmarkSuite.runParallelLoadOp(sourceFolder, DEFAULT_LOADER_THREADS);
					break;
				default:
					BenchmarkSuite.runSequentialLoad(sourceFolder);
					break;
			}
			return;
		}
		
		// Default if no args are specified
		BenchmarkSuite.runSequentialLoad(sourceFolder);
		return;
	}
}
