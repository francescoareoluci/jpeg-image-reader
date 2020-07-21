# JPEG Image Reader

This repository contains an application that demonstrate the usage of a multithreading approach to read jpg images from a directory. \
Purpose of this approach is to show how to asynchrounously process images during image loading. It supports multiple types of loading:

* sequential
* parallel with Runnables
* parallel with Runnables using a thread pool
* parallel with Callables using a thread pool

The processing operation used in this application is the rgb to grayscale color conversion. This processing can be benchmarked in the following ways:

* sequential load + sequential processing
* sequential load + parallel processing using Callables and thread pool
* parallel load using Runnables and thread pool + parallel processing using Callables and thread pool

The thread pool is shared among clients using a singleton pattern.

## Application build

The application is based on Maven. Thus, to build the jar file, from the root folder, run:

> mvn package

## Application usage

Once the application has been built, the jar file will be accessible in the target folder. It can be launched by specifying the image folder and the type of benchmark that has to be done:

```
Usage: java -jar jpeg_image_loader source_folder operation
source_folder: directory containing jpg images to be loaded
operation: optional - specify how to load images and processing. Default: 1
	1: Sequential load of source_folder jpg images.
	2: Parallel load using a thread pool
	3: Parallel load using Callables
	4: Parallel load using simple threads
	5: Sequential load and sequential grayscale conversion of loaded images
	6: Sequential load and parallel grayscale conversion of loaded images
	7: Parallel load and parallel grayscale conversion of loaded images in async mode
```
