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

package gda.device.detector.uviewnew;

import java.awt.Rectangle;

import gda.device.DeviceException;

public interface UViewController {

	public String getVersion() throws DeviceException;
	
	public class GrayAdjustment {
		private int windowHigh;
		private int windowLow;
		
		public GrayAdjustment(int high, int low) {
			windowHigh = high;
			windowLow = low;
		}
		
		public int getWindowLow() {
			return windowLow;
		}
		public int getWindowHigh() {
			return windowHigh;
		}
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
			DAT, PNG, TIFF, BMP, JPG, TIFF_UNCOMPRESSED
		}
		public enum ImageContentsType {
			RGB_XYZ, RGB_XYZ_RAW, GRAYLEVEL16
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

	/*
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
	*/
	
	public class RegionOfInterest {
		private Rectangle region;
		public final int id;

		public RegionOfInterest(int x, int y, int width, int height, int id){
			this.id = id;
			this.region = new Rectangle(x, y, width, height);
		}
		public RegionOfInterest(Rectangle region, int id) {
			this(region.x, region.y, region.width, region.height, id);
		}
		
		public Rectangle getRegion() {
			return new Rectangle(region);
		}
		
		public void setRegion(int x, int y, int width, int height) {
			this.region = new Rectangle(x, y, width, height);
		}
		
		public void setRegion(Rectangle region) {
			this.region = new Rectangle(region);
		}
	}

	public GrayAdjustment doGrayAdjust() throws DeviceException;
	public GrayAdjustment getGrayAdjustment() throws DeviceException;

	public ImageData getImageData() throws DeviceException;

	public int getCameraExpTime() throws DeviceException;
	public void setCameraExpTime(int newMsecTime) throws DeviceException;

	public boolean getSequential() throws DeviceException;
	public void setSequential(boolean newSeq) throws DeviceException;

	public int getFrameAverage() throws DeviceException;
	public void setFrameAverage(int newAveraging) throws DeviceException;

	public boolean getNewImageReady() throws DeviceException;

	public boolean getAcquisitionInProgress() throws DeviceException;
	public void setAcquisitionInProgess(boolean newAcqusitionStatus) throws DeviceException;

	public void acquireSingleImage(int imageId) throws DeviceException;

	public void exportImage(final ImageFile fileDetails) throws DeviceException;

	public void defineRoi(final RegionOfInterest roi) throws DeviceException;
	void activateROI(RegionOfInterest roi) throws DeviceException;
	void deactivateROI(RegionOfInterest roi) throws DeviceException;
	boolean isROIActive(RegionOfInterest roi) throws DeviceException;
	public double getRoiData(int roiId) throws DeviceException;

	public void setPixelClock(int MHz) throws DeviceException;
	public int getPixelClock() throws DeviceException;

	public void setTriggerMode(int mode) throws DeviceException;
	public int getTriggerMode() throws DeviceException;

	public void setCameraADC(int adc) throws DeviceException;
	public int getCameraADC() throws DeviceException;
}
