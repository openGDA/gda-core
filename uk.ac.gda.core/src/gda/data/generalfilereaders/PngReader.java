/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council Daresbury Laboratory
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

package gda.data.generalfilereaders;

import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

/**
 * Class to read PNG files.
 */
public class PngReader {

	/**
	 * @param filename
	 * @return image data as a 2D array
	 * @throws IOException
	 */
	public static int[][] readPNG(String filename) throws IOException {
		int[][] imageData;

		File f = new File(filename);
		BufferedImage image = ImageIO.read(f);

		int pix[] = new int[image.getWidth() * image.getHeight()];
		int x, y, rgb, val;
		for (y = 0; y < image.getHeight(); y++) {
			for (x = 0; x < image.getWidth(); x++) {
				rgb = image.getRGB(x, y);
				if (rgb == 0xff000000) { // if black
					val = 0;
				} else
					val = 1;
				pix[y * image.getWidth() + x] = val;
			}
		}

		imageData = transform1Dto2D(pix, image.getHeight(), image.getWidth());

		return imageData;
	}

	/**
	 * @param filename
	 * @return the image data as a 1D array.
	 * @throws IOException
	 */
	public static int[] readPNGto1D(String filename) throws IOException {
		int[] imageData;

		File f = new File(filename);
		BufferedImage image = ImageIO.read(f);

		int pix[] = new int[image.getWidth() * image.getHeight()];
		int x, y, rgb, val;
		for (y = 0; y < image.getHeight(); y++) {
			for (x = 0; x < image.getWidth(); x++) {
				rgb = image.getRGB(x, y);
				if (rgb == 0xff000000) { // if black
					val = 0;
				} else
					val = 1;
				pix[y * image.getWidth() + x] = val;
			}
		}

		imageData = pix;

		return imageData;
	}

	private static int[][] transform1Dto2D(int[] pix, int r, int c) {
		if (r * c != pix.length) {
			String err = "No transformation possible; " + r + "*" + c + " != " + pix.length + ".";
			throw new IllegalArgumentException(err);
		}

		int[][] pix2D = new int[r][c];
		int col = 0, row = 0;

		for (int i = 0; i < pix.length; i++) {
			if (i > 0 && i % c == 0) {
				col = 0;
				row++;
			}
			pix2D[row][col] = pix[i];
			col++;
		}
		return pix2D;
	}

	/**
	 * Main method for testing.
	 * 
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

	}

}
