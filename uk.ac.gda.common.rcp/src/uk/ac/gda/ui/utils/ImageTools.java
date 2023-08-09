/*-
 * Copyright © 2023 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.utils;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.swt.graphics.GC;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;
import org.eclipse.swt.graphics.Transform;
import org.eclipse.swt.widgets.Display;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ImageTools {
	private static final Logger logger = LoggerFactory.getLogger(ImageTools.class);

	private ImageTools() {
		// utility class
	}

	/**
	 * Generate rotated image
	 * Origin is top left of image, image is rotated clockwise starting from 3 o'clock position.
	 *
	 * @param image
	 * @param rotationDegrees - rotation angle wrt 3 o'clock position (can be +ve or -ve).
	 * @return rotated image
	 */
	public static Image getRotatedImage(Image image, int rotationDegrees) {

		// ensure rotation is between 0 and 360 degrees
		int rotation = (rotationDegrees+360)%360;
		if (rotation == 0) {
			return image;
		}

		logger.debug("Creating rotated image : rotation = {} degrees", rotation);

		double rotRadians = rotation*Math.PI/180.0;

		double cosRot = Math.cos(rotRadians);
		double sinRot = Math.sin(rotRadians);

		var origWidth= image.getBounds().width;
		var origHeight= image.getBounds().height;

		double newWidth = Math.abs(origWidth*Math.abs(cosRot) + origHeight*Math.abs(sinRot));
		double newHeight = Math.abs(origHeight*Math.abs(cosRot) + origWidth*Math.abs(sinRot));

		logger.debug("Image sizes : original = {}x{}, rotated = {}x{}", origWidth, origHeight, newWidth, newHeight);

		// Setup transform to rotate the image
		Transform transform = new Transform(Display.getDefault());

		// Calculate translation in x and y to make rotated image to lie in +ve x-y quadrant
		if (rotation < 90) {
			transform.translate((int)(origHeight*sinRot), 0);
		} else if (rotation < 180) {
			transform.translate((int)(origHeight*sinRot - origWidth*cosRot),
								(int)(-origHeight*cosRot));
		} else if ( rotation < 270) {
			transform.translate((int)(-origWidth*cosRot),
								-(int)(origHeight*cosRot+origWidth*sinRot));
		} else if ( rotation < 360) {
			transform.translate(0, -(int)(origWidth*sinRot));
		}
		transform.rotate(rotation);

		//Make image to contain the rotated image
		Image rotatedImage = new Image(Display.getDefault(), (int) newWidth, (int) newHeight);

		// Draw the rotated image
		GC gc = new GC(rotatedImage);
        gc.setTransform(transform);
        gc.drawImage(image, 0, 0);

        // Dispose of resources
        gc.dispose();
        transform.dispose();

        return rotatedImage;
	}

	public static Image setTransparentPixels(Image image, int threshold) {
		return setTransparentPixels(image, new RGB(255,255,255), threshold);
	}

	/**
	 * Generate a new image with specific colour set to transparent
	 *
	 * @param image
	 * @param transparentColour - colour to set to transparent (usually white, e.g RGB(255,255,255))
	 * @param threshold pixels with RGB components exceeding this value will be set the transparent
	 *  (set to zero to ignore)
	 *
	 * @return new image with transparency
	 */
	public static Image setTransparentPixels(Image image, RGB transparentColour, int threshold) {
		var imageData = image.getImageData();
		PaletteData palette = imageData.palette;
		imageData.transparentPixel = palette.getPixel(transparentColour);

		if (threshold > 0) {
			// Set 'off white' pixels to white as well, so they are also transparent
			for (int j = 0; j < imageData.height; j++) {
				for (int i = 0; i < imageData.width; i++) {
					var rgb = palette.getRGB(imageData.getPixel(i, j));
					if (rgb.red > threshold && rgb.green > threshold && rgb.blue > threshold) {
						imageData.setPixel(i, j, palette.getPixel(transparentColour));
					}
				}
			}
		}

        // Generate new image from the transparent image data
        Image rotImage = new Image(Display.getDefault(), imageData);

        // Dispose of the original image
        image.dispose();

        return rotImage;
	}

	/**
	 * Replace 'target' colour in an image with a different colour.
	 *
	 * @param image
	 * @param targetColour RGB colour to be replaced
 	 * @param replacementColour RGB replacement colour
 	 * @param threshold
 	 *
	 * @return new image with the colours replaced
	 */
	public static Image replaceColour(Image image, RGB targetColour, RGB replacementColour, int threshold) {
		var imageData = image.getImageData();
		PaletteData palette = imageData.palette;
		// Replace pixels of specified colour with replacement colour
		for (int j = 0; j < imageData.height; j++) {
			for (int i = 0; i < imageData.width; i++) {
				var rgb = palette.getRGB(imageData.getPixel(i, j));

				if (Math.abs(rgb.red-targetColour.red)<threshold &&
					Math.abs(rgb.green-targetColour.green)<threshold &&
					Math.abs(rgb.blue-targetColour.blue)<threshold ) {

					imageData.setPixel(i, j, palette.getPixel(replacementColour));
				}
			}
		}

        // Generate new image from the modified image data
        Image newImamage = new Image(Display.getDefault(), imageData);

        return newImamage;
	}

	public static RGB getDominantColour(Image image) {
		var imageData = image.getImageData();
		int width = image.getBounds().width;
		int height = image.getBounds().height;
		Map<Integer, Integer> colourMap = new HashMap<>();

		int[] pixels = new int[width];
		for(int i=0; i<height; i++) {
			imageData.getPixels(0, i, pixels.length, pixels, 0);
			for(int val : pixels) {
				if (val>0) {
					colourMap.computeIfAbsent(val, key -> 1);
					colourMap.put(val, colourMap.get(val)+1);
				}
			}
		}

		var maxEntry = colourMap.entrySet().stream().max( (ent1, ent2) ->  ent1.getValue().compareTo(ent2.getValue()));

		if (maxEntry.isPresent()) {
			return imageData.palette.getRGB(maxEntry.get().getKey());
		}
		return new RGB(0,0,0);
	}
}
