package jpeg_image_reader;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

public class Image {
	
		public Image() {}
	
		public Image(BufferedImage image)
		{
			this.image = image;
		}
	
		public void setImage(BufferedImage image)
		{
			this.image = image;
		}
		
		public BufferedImage getImage()
		{
			return this.image;
		}
		
		public int getFirstPixel()
		{
			return this.image.getRGB(0, 0);
		}
		
		public int getMean()
		{
			int sum = 0;
			for (int i = 0; i < this.image.getWidth() / 4; i++) {
				for (int j = 0; j < this.image.getHeight() / 4; j++) {
					sum += this.image.getRGB(i, j);
				}
			}
			return sum / (this.image.getHeight() * this.image.getWidth());
		}
		
		/**
		 * Perform a grayscale conversion of the
	     * set image and return it.
		 *
		 * @return the grayscale BufferedImage
		 */
		public BufferedImage grayscaleConversion()
		{	
			BufferedImage grayscaleImage = null;
			
			if (this.image == null) {
				System.out.println("Unable to convert image to grayscale");
				return grayscaleImage;
			}
			
			int width = this.image.getWidth();
			int height = this.image.getHeight();
			
			grayscaleImage = new BufferedImage(width, height, BufferedImage.TYPE_BYTE_GRAY);
			
			for (int x = 0; x < width; ++x) {
			    for (int y = 0; y < height; ++y) {
			        int rgb = this.image.getRGB(x, y);
			        int r = (rgb >> 16) & 0xFF;
			        int g = (rgb >> 8) & 0xFF;
			        int b = (rgb & 0xFF);

			        // Normalize and gamma correct:
			        float rr = (float) Math.pow(r / 255.0, 2.2);
			        float gg = (float) Math.pow(g / 255.0, 2.2);
			        float bb = (float) Math.pow(b / 255.0, 2.2);

			        // Calculate luminance:
			        float lum = (float) (0.2126 * rr + 0.7152 * gg + 0.0722 * bb);

			        // Gamma compand and rescale to byte range:
			        int grayLevel = (int) (255.0 * Math.pow(lum, 1.0 / 2.2));
			        int gray = (grayLevel << 16) + (grayLevel << 8) + grayLevel; 
			      grayscaleImage.setRGB(x, y, gray);
			   }
			}
		
			return grayscaleImage;
		}
		
		/**
		 * Save image in requested path
		 *
		 * @param 	path: String containing the destination path
		 * @return		  True is successful, false otherwise
		 */
		public boolean saveImage(String path)
		{
			try {
			    File outputfile = new File(path);
			    ImageIO.write(this.image, "jpg", outputfile);
			    System.out.println("Saved");
			} catch (IOException e) {
			    System.out.println("Error");
			    return false;
			}
			return true;
		}
	
		private BufferedImage image;
}