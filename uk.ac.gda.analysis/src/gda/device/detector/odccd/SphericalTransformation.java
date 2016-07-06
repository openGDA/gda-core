/*-
 * Copyright © 2010 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

package gda.device.detector.odccd;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

import javax.imageio.ImageIO;

import org.eclipse.dawnsci.analysis.api.io.ScanFileHolderException;
import org.eclipse.january.dataset.Dataset;

import uk.ac.diamond.scisoft.analysis.io.DataHolder;
import uk.ac.diamond.scisoft.analysis.io.PilatusTiffLoader;

public class SphericalTransformation {


	public static void stitchImages(String[] inputFiles, String image, int imagesX, int imagesY) throws IOException {
		int[][] stitched = stitch(inputFiles, image, imagesX, imagesY);
		convert(stitched, image);
	}

	public void flattenImages(String[] inputFiles, String image, int imagesX, int imagesY, double x1, double y1, double x2, double y2) throws IOException {
		int[][] stitched = flatten(inputFiles, image, imagesX, imagesY, x1,y1,x2,y2);
		convert(stitched, image);
	}

	public void unflattenImages(String[] inputFiles, String image, int imagesX, int imagesY, double x1, double y1, double x2, double y2) throws IOException {
		int[][] stitched = unflatten(inputFiles, image, imagesX, imagesY, x1,y1,x2,y2);
		convert(stitched, image);
	}



	@SuppressWarnings("null")
	public static void convert(int[][] stitched, String image) throws IOException{
		int width = stitched[0].length;
		int height = stitched.length;

		int size = 1024;

		if (width > 1024 || height > 1024)
			size = 2048;

		@SuppressWarnings("resource")
		ModifyCrysalisHeader crysalisFile = new ModifyCrysalisHeader(image);

		crysalisFile.editStringHeader("NX", Integer.toString(size));
		crysalisFile.editStringHeader("NY", Integer.toString(size));
		crysalisFile.editStringHeader("COMPRESSION", " NO       ");
		crysalisFile.editStringHeader("NHEADER", "4864");
		crysalisFile.editStringHeader("NSUPPLEMENT", "0");

		crysalisFile.editIntHeader("bx", 2);
		crysalisFile.editIntHeader("by", 2);
		crysalisFile.editIntHeader("x1", 1);
		crysalisFile.editIntHeader("y1", 1);
		crysalisFile.editIntHeader("x2", size*2);
		crysalisFile.editIntHeader("y2", size*2);

		int inc = 0;
		int pix = 0;
		byte[] pixBytes = null;
		byte[] data = new byte[size * size * 4];

		int offsetX = (size-width)/2;
		int offsetY = (size-height)/2;

		for (int i = 0; i < size; i++) {
			for (int j = 0; j < size; j++) {
				if (((j > offsetX)&&( j < width+offsetX)) && ((i > offsetY)&&(i < height+offsetY))){
					pix = stitched[i-offsetY][j-offsetX];
					pixBytes = intToByteArray(pix, 4);
				}
				for (int k = 0; k < 4; k++) {
					if (((j > offsetX)&&( j < width+offsetX)) && ((i > offsetY)&&(i < height+offsetY))) {
						data[inc] = pixBytes[k];
					} else
						data[inc] = intToByteArray(-999999999, 4)[k];
					inc++;
				}
			}
		}

		int ASCII_HEADER_SIZE = 256;
		int BIN_HEADER_SIZE = 4864;
		int COMBINED_HEADER_SIZE = ASCII_HEADER_SIZE + BIN_HEADER_SIZE;
		byte[] header = new byte[COMBINED_HEADER_SIZE];

		File inputFile = new File(image);
		InputStream is = null;
		try {
			is = new FileInputStream(inputFile);
			is.read(header, 0, COMBINED_HEADER_SIZE);
		} catch (IOException e) {
			e.printStackTrace();
		}

		writeFile(image, header, data);
	}

	public static void writeFile(String inputFileName, byte[] header, byte[] data){




		File outputImage = new File(inputFileName);
		try {
			outputImage.createNewFile();
			FileOutputStream out = new FileOutputStream(outputImage);
			out.write(header);
			out.write(data);
			out.flush();
			out.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public static byte[] intToByteArray(int value, int length) {
		byte[] b = new byte[length];
		for (int i = 0; i < length; i++) {
			int offset = (b.length - 1 - i) * 8;
			b[length-i-1] = (byte) ((value >>> offset) & 0xFF);
		}
		return b;
	}

	public static int[][] stitch(String[] inputImages, @SuppressWarnings("unused") String outputFile, int noImagesX, int noImagesY) {
		int[][] stitchedImage = stitchImages(importImages(inputImages, noImagesX, noImagesY), noImagesX, noImagesY);
		return stitchedImage;
	}

	public int[][] flatten(String[] inputImages, @SuppressWarnings("unused") String outputFile, int noImagesX, int noImagesY, double x1, double y1,
			double x2, double y2) {
		int[][] stitchedImage = stitchImages(importImages(inputImages, noImagesX, noImagesY), noImagesX, noImagesY);
		int[][] curvedImage = convertFromCurveToPlane(stitchedImage, x1, y1, x2, y2);
		return curvedImage;
	}

	public int[][] unflatten(String[] inputImages, @SuppressWarnings("unused") String outputFile, int noImagesX, int noImagesY, double x1, double y1,
			double x2, double y2) {
		int[][] stitchedImage = stitchImages(importImages(inputImages, noImagesX, noImagesY), noImagesX, noImagesY);
		int[][] planeImage = convertFromPlaneToCurve(stitchedImage, x1, y1, x2, y2);
		return planeImage;
	}

	public static HashMap<String, int[][]> importImages(String[] inputFiles, int noImagesX, int noImagesY) {
		final HashMap<String, int[][]> images = new HashMap<String, int[][]>();
		int count = 0;
		for (int i = 0; i < noImagesX; i++) {
			for (int j = 0; j < noImagesY; j++) {
				images.put("" + i + "_" + j, getPilatusImage(inputFiles[count]));
				count++;
			}
		}
		return images;
	}

	public int[][] importImage(String imageFileName) throws IOException {

		File imageFile = new File(imageFileName);

		BufferedImage inputImage = ImageIO.read(imageFile);

		int width = inputImage.getWidth();
		int height = inputImage.getHeight();

		int[][] imageArray = new int[width][height];

		for (int i = 0; i < width; i++) {
			for (int j = 0; j < height; j++) {
				imageArray[i][j] = inputImage.getRGB(i, j);
			}
		}
		return imageArray;
	}

	public static int[][] stitchImages(HashMap<String, int[][]> images, int imagesX, int imagesY) {

		int totalWidth = 0;
		int totalHeight = 0;

		int width = 0;
		int height = 0;

		for (int i = 0; i < imagesX; i++) {
			totalHeight = 0;
			for (int j = 0; j < imagesY; j++) {
				int[][] image = images.get("" + i + "_" + j);
				width = image.length;
				height = image[0].length;
				totalHeight += height;
			}
			totalWidth += width;
		}

		int offsetX = 0;
		int offsetY = 0;

		int max = 0;
		int min = Integer.MAX_VALUE;

		int[][] stitchedImage = new int[totalWidth][totalHeight];

		for (int i = 0; i < imagesX; i++) {
			totalHeight = 0;
			offsetY = 0;
			for (int j = 0; j < imagesY; j++) {
				int[][] image = images.get("" + i + "_" + j);
				width = image.length;
				height = image[0].length;
				for (int k = 0; k < width; k++) {
					for (int l = 0; l < height; l++) {

						stitchedImage[k + offsetX][l + offsetY] = image[k][l];

						if (image[k][l] > max)
							max = image[k][l];

						if (image[k][l] < min)
							min = image[k][l];
					}
				}
				totalHeight += height;
				offsetY += height;
			}

			offsetX += width;
			totalWidth += width;

		}

		return stitchedImage;
	}

	public int[][] convertFromCurveToPlane(int[][] image, double x1, double y1, double x2, double y2) {

		final double radStartX = Math.toRadians(x1);
		final double radStartY = Math.toRadians(y1);
		final double radEndX = Math.toRadians(x2);
		final double radEndY = Math.toRadians(y2);

		int width = image.length;
		int height = image[0].length;

		final double pixel_to_angle_ratio_x = (Math.toRadians(x2 - x1) / width);
		final double pixel_to_angle_ratio_y = (Math.toRadians(y2 - y1) / height);

		double[] startPoint = findPlanePoint(radStartX, radStartY);
		double startX = (startPoint[0]) / pixel_to_angle_ratio_x;
		double startY = (startPoint[1]) / pixel_to_angle_ratio_y;

		double[] endPoint = findPlanePoint(radEndX, radEndY);
		double endX = (endPoint[0]) / pixel_to_angle_ratio_x;
		double endY = (endPoint[1]) / pixel_to_angle_ratio_y;

		int newWidth = (int) (Math.ceil(endX - startX));
		int newHeight = (int) (Math.ceil(endY - startY));

		int[][] newImage = new int[newWidth][newHeight];

		for(int i=0;i<newWidth;i++){
			for(int j=0;j<newHeight;j++){
				newImage[i][j] = -127;
			}
		}

		for (double lat = radStartX; lat <= radEndX; lat += pixel_to_angle_ratio_x) {
			for (double lon = radStartY; lon <= radEndY; lon += pixel_to_angle_ratio_y) {

				int oldX = (int) ((lat - radStartX) / pixel_to_angle_ratio_x);
				int oldY = (int) ((lon - radStartY) / pixel_to_angle_ratio_y);

				int rgb = image[oldX][oldY];

				double[] newPoint = findPlanePoint(lat, lon);
				int newLat = (int) ((newPoint[0] / pixel_to_angle_ratio_x) - startX);
				int newLon = (int) ((newPoint[1] / pixel_to_angle_ratio_y) - startY);

				newImage[newLat][newLon] = rgb-127;
			}
		}

		return newImage;
	}

	public int[][] convertFromPlaneToCurve(int[][] image, double x1, double y1, double x2, double y2) {

		final double radStartX = Math.toRadians(x1);
		final double radStartY = Math.toRadians(y1);
		final double radEndX = Math.toRadians(x2);
		final double radEndY = Math.toRadians(y2);

		int width = image.length;
		int height = image[0].length;

		final double pixel_to_angle_ratio_x = (Math.toRadians(x2 - x1) / width);
		final double pixel_to_angle_ratio_y = (Math.toRadians(y2 - y1) / height);

		double startX = 0;
		double startY = 0;
		double endX = 0;
		double endY = 0;

		for (double lat = radStartX; lat <= radEndX; lat += pixel_to_angle_ratio_x) {
			for (double lon = radStartY; lon <= radEndY; lon += pixel_to_angle_ratio_y) {

				double[] newPoint = findCurvePoint(lat, lon);

				if (newPoint[0] > endX)
					endX = newPoint[0];
				if (newPoint[1] > endY)
					endY = newPoint[1];

				if (newPoint[0] < startX)
					startX = newPoint[0];
				if (newPoint[1] < startY)
					startY = newPoint[1];
			}
		}

		startX = startX / pixel_to_angle_ratio_x;
		startY = startY / pixel_to_angle_ratio_y;

		endX = endX / pixel_to_angle_ratio_x;
		endY = endY / pixel_to_angle_ratio_y;

		int newWidth = (int) (Math.ceil(endX - startX));
		int newHeight = (int) (Math.ceil(endY - startY));

		int[][] newImage = new int[newWidth][newHeight];

		for(int i=0;i<newWidth;i++){
			for(int j=0;j<newHeight;j++){
				newImage[i][j] = -127;
			}
		}

		for (double lat = radStartX; lat <= radEndX; lat += pixel_to_angle_ratio_x) {
			for (double lon = radStartY; lon <= radEndY; lon += pixel_to_angle_ratio_y) {

				int oldX = (int) ((lat - radStartX) / pixel_to_angle_ratio_x);
				int oldY = (int) ((lon - radStartY) / pixel_to_angle_ratio_y);

				int rgb = image[oldX][oldY];

				double[] newPoint = findCurvePoint(lat, lon);
				int newLat = (int) ((newPoint[0] / pixel_to_angle_ratio_x) - startX);
				int newLon = (int) ((newPoint[1] / pixel_to_angle_ratio_y) - startY);

				newImage[newLon][newLat] = rgb-127;
			}
		}

		return newImage;
	}

	public double[] findPlanePoint(double x, double y) {

		double[] point = new double[2];
		double c = Math.cos(x) * Math.cos(y);
		point[0] = Math.sin(y) / c;
		point[1] = Math.sin(x) / c;
		return point;
	}

	public double[] findCurvePoint(double x, double y) {

		double[] point = new double[2];
		double c = Math.cos(x) * Math.cos(y);
		point[0] = c * Math.asin(y);
		point[1] = c * Math.asin(x);
		return point;
	}

	public static int[][] getPilatusImage(String path) {

		PilatusTiffLoader pilLoad = new PilatusTiffLoader(path);
		DataHolder pilDataHolder = null;
		try {
			pilDataHolder = pilLoad.loadFile();

			Dataset myData = pilDataHolder.getDataset(0);

			int PILATUS_SIZE_X = myData.getShape()[1]; // was 195;
			int PILATUS_SIZE_Y = myData.getShape()[0]; // was 487;

			int[][] pilatusImage = new int[PILATUS_SIZE_X][PILATUS_SIZE_Y];

			for (int i = 0; i < PILATUS_SIZE_Y; i++) {
				for (int j = 0; j < PILATUS_SIZE_X; j++) {
					pilatusImage[j][i] = myData.getInt(i, j);
				}
			}
			return pilatusImage;
		} catch (ScanFileHolderException e) {
			e.printStackTrace();
		}
		return new int[0][0];
	}
}
