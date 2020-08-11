/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package gda.images.camera.mjpeg;

import java.awt.image.BufferedImage;
import java.awt.image.ComponentColorModel;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

/**
 * Task that captures frames from the MJPEG stream and dispatches SWT images.
 */
public class SwtFrameCaptureTaskFalseColours extends FrameCaptureTask<ImageData> {

	private int redMask;
	private int greenMask;
	private int blueMask;

	public SwtFrameCaptureTaskFalseColours(String urlSpec, ExecutorService imageDecodingService,
			BlockingQueue<Future<ImageData>> receivedImages, int redMask, int greenMask, int blueMask) {
		super(urlSpec, imageDecodingService, receivedImages);
		this.redMask = redMask;
		this.greenMask = greenMask;
		this.blueMask = blueMask;
	}

	public void setRedMask(int redMask) {
		this.redMask = redMask;
	}

	public void setBlueMask(int blueMask) {
		this.blueMask = blueMask;
	}

	public void setGreenMask(int greenMask) {
		this.greenMask = greenMask;
	}

	public ImageData convertByteArrayToImage(byte[] imageData) {
		return new ImageData(new ByteArrayInputStream(imageData));
	}

	@Override
	protected ImageData convertImage(BufferedImage image) {
		if (image.getColorModel() instanceof DirectColorModel) {
			DirectColorModel colorModel = (DirectColorModel) image.getColorModel();
			PaletteData palette = new PaletteData(colorModel.getRedMask(), colorModel.getGreenMask(),
					colorModel.getBlueMask());
			ImageData data = new ImageData(image.getWidth(), image.getHeight(),
					colorModel.getPixelSize(), palette);
			WritableRaster raster = image.getRaster();
			RGB rgb = new RGB(redMask, greenMask, blueMask);
			int pixel = 0;
			int[] pixelArray = new int[3];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					raster.getPixel(x, y, pixelArray);
					rgb.red = pixelArray[0];// >> redMask;
					rgb.green = pixelArray[1];// >>greenMask;
					rgb.blue = pixelArray[2];// >>blueMask;
					pixel = palette.getPixel(rgb);
					data.setPixel(x, y, pixel);
				}
			}
			return data;
		} else if (image.getColorModel() instanceof IndexColorModel) {
			IndexColorModel colorModel = (IndexColorModel) image.getColorModel();
			int size = colorModel.getMapSize();
			byte[] reds = new byte[size];
			byte[] greens = new byte[size];
			byte[] blues = new byte[size];
			colorModel.getReds(reds);
			colorModel.getGreens(greens);
			colorModel.getBlues(blues);
			RGB[] rgbs = new RGB[size];
			for (int i = 0; i < rgbs.length; i++) {
				rgbs[i] = new RGB(reds[i] & 0xFF, greens[i] & 0xFF, blues[i] & 0xFF);
			}
			PaletteData palette = new PaletteData(rgbs);
			ImageData data = new ImageData(image.getWidth(), image.getHeight(),
					colorModel.getPixelSize(), palette);
			data.transparentPixel = colorModel.getTransparentPixel();
			WritableRaster raster = image.getRaster();
			int[] pixelArray = new int[1];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					raster.getPixel(x, y, pixelArray);
					data.setPixel(x, y, pixelArray[0]);
				}
			}
			return data;
		} else if (image.getColorModel() instanceof ComponentColorModel) {
			ComponentColorModel colorModel = (ComponentColorModel) image.getColorModel();

			if (colorModel.getNumColorComponents() != 3) {
				return null;
			}
			// ASSUMES: 3 BYTE BGR IMAGE TYPE

			PaletteData palette = new PaletteData(0x0000FF, 0x00FF00, 0xFF0000);
			ImageData data = new ImageData(image.getWidth(), image.getHeight(),
					colorModel.getPixelSize(), palette);

			// This is valid because we are using a 3-byte Data model with no transparent pixels
			data.transparentPixel = -1;

			WritableRaster raster = image.getRaster();
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					int[] pixelArray = raster.getPixel(x, y, (int[]) null);
					int pixel = palette.getPixel(new RGB(pixelArray[0], pixelArray[1], pixelArray[2]));
					data.setPixel(x, y, pixel);
				}
			}
			return data;
		}
		return null;
	}
}
