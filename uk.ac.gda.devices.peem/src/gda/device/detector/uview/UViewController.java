/*-
 * Copyright © 2014 Diamond Light Source Ltd.
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

package gda.device.detector.uview;

import gda.device.DeviceException;

/**
 *
 */
public interface UViewController {

	public interface GrayAdjustment {
		public int getWindowLow();
		public int getWindowHigh();
	}

	public class ImageData {
		private final int height;
		private final int width;
		private final short[] data;

		public ImageData(int height, int width, short[] data) {
			super();
			this.height = height;
			this.width = width;
			this.data = data;
		}

		public short[] getData() {
			return data;
		}

		public int getWidth() {
			return width;
		}

		public int getHeight() {
			return height;
		}
	}

	public class ImageFile {

		public enum ImageFormat {
			DAT, PNG, TIFF, BMP, JPG
		}
		public enum ImageContentsType {
			RGB_XYZ, RGB_XY_RAWZ, GRAYLEVEL16
		}
		
		private final String fileName;
		private final ImageFormat format;
		private final ImageContentsType contentType;

		public ImageFile(String fileName, ImageFormat format, ImageContentsType contentType) {
			super();
			this.fileName = fileName;
			this.format = format;
			this.contentType = contentType;
		}

		public String getFileName() {
			return fileName;
		}

		public ImageFormat getFormat() {
			return format;
		}

		public ImageContentsType getContentType() {
			return contentType;
		}
	}

	public interface RegionOfInterest {

		public class Point {
			private final int pointX;
			private final int pointY;

			Point(int x, int y) {
				this.pointX = x;
				this.pointY = y;
			}

			public int getX() {
				return pointX;
			}

			public int getY() {
				return pointY;
			}
		}

		public int getIndex();

		public Point getLowerBound();
		public Point getUpperBound();

		public void addPoint(final Point point);

		public void resetData();

	}

	public GrayAdjustment doGrayAdjust();
	public GrayAdjustment getGrayAdjustment();

	public ImageData getImageData() throws DeviceException;

	public int getCameraExpTime();
	public void setCameraExpTime(int newMsecTime);

	public boolean getSequential();
	public void setSequential(boolean newSeq);

	public int getFrameAverage();
	public void setFrameAverage(int newAveraging);

	public boolean getNewImageReady();

	public boolean getAcquisitionInProgress() throws DeviceException;
	public void setAcquisitionInProgess(boolean newAcqusitionStatus);

	public void acquireSingleImage(int imageId) throws DeviceException;

	public void exportImage(final ImageFile fileDetails);

	public void roiData(final RegionOfInterest roi);

}
