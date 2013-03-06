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

package gda.images.camera;

import gda.device.DeviceException;

import java.awt.image.BufferedImage;
import java.awt.image.DirectColorModel;
import java.awt.image.IndexColorModel;
import java.awt.image.WritableRaster;
import java.io.ByteArrayInputStream;

import javax.media.Buffer;
import javax.media.format.VideoFormat;

import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.graphics.PaletteData;
import org.eclipse.swt.graphics.RGB;

/**
 * Connects to an RTP stream, and runs a capture task at a regular fixed interval that captures frames from the stream
 * at (approximately) the desired frame rate, and passes them to a list of {@link ImageListener}s. By default, the
 * desired frame rate is 25fps.
 * <p>
 * Configuring the receiver establishes the RTP connection and begins image capture. Image capture can be turned off and
 * on using the {@link #stop()} and {@link #start()} methods.
 * <p>
 * The entire connection can be terminated using {@link #closeConnection()}, and then started again using
 * {@link #createConnection()} (which will also resume image capture once the connection has been established).
 */
public class RTPStreamReceiverSWT extends RTPStreamReceiverBase<ImageData> {

	@Override
	public ImageData getImage() throws DeviceException {
		if (frameGrabber == null) {
			throw new DeviceException("Couldn't capture image - receiver not ready");
		}

		Buffer buf = frameGrabber.grabFrame();

		if (buf.getData() instanceof byte[]) {
			return new ImageData(new ByteArrayInputStream((byte[]) buf.getData()));
		}
		if (buf.getData() instanceof int[]) {
			VideoFormat vf = (VideoFormat) buf.getFormat();
			if (vf.getEncoding().equals("rgb")) {
				PaletteData palette = new PaletteData(0xFF0000, 0xFF00, 0xFF);
				int[] ints = (int[]) buf.getData();
				byte[] bytes = new byte[ints.length * 3];
				for (int i = 0; i < ints.length; i++) {
					bytes[i * 3 + 0] = (byte) (ints[i] >> 16);
					bytes[i * 3 + 1] = (byte) (ints[i] >> 8);
					bytes[i * 3 + 2] = (byte) (ints[i]);
				}
				ImageData data = new ImageData(vf.getSize().width, vf.getSize().height, 24, palette, 1, bytes);
				return data;
			}
		}

		BufferedImage bufferedImage = RTPStreamReceiver.getBufferedImage(buf); 
		return convertToSWT(bufferedImage);
	 }

	static ImageData convertToSWT(BufferedImage bufferedImage) {
		if (bufferedImage.getColorModel() instanceof DirectColorModel) {
			DirectColorModel colorModel = (DirectColorModel) bufferedImage.getColorModel();
			PaletteData palette = new PaletteData(colorModel.getRedMask(), colorModel.getGreenMask(), colorModel
					.getBlueMask());

			ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel
					.getPixelSize(), palette);

			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[3];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					raster.getPixel(x, y, pixelArray);
					int pixel = palette.getPixel(new RGB(pixelArray[0], pixelArray[1], pixelArray[2]));
					data.setPixel(x, y, pixel);
				}
			}
			return data;
		} else if (bufferedImage.getColorModel() instanceof IndexColorModel) {
			IndexColorModel colorModel = (IndexColorModel) bufferedImage.getColorModel();
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
			ImageData data = new ImageData(bufferedImage.getWidth(), bufferedImage.getHeight(), colorModel
					.getPixelSize(), palette);
			data.transparentPixel = colorModel.getTransparentPixel();
			WritableRaster raster = bufferedImage.getRaster();
			int[] pixelArray = new int[1];
			for (int y = 0; y < data.height; y++) {
				for (int x = 0; x < data.width; x++) {
					raster.getPixel(x, y, pixelArray);
					data.setPixel(x, y, pixelArray[0]);
				}
			}
			return data;
		}
		return null;
	}

}
