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

package gda.device.detector.uview;

import gda.device.detector.uview.corba.impl.CorbaBridgeConnection;
// CORBA Studbs
// import java.awt.image.*;
import gda.device.peem.MicroscopeControl.Microscope;
import gda.device.peem.MicroscopeControl.MicroscopePackage.UNBOUNDED_SEQUENCEHolder;
import gda.device.peem.MicroscopeControl.MicroscopePackage.UNBOUNDED_SHORTSEQUENCEHolder;
import gda.factory.Finder;

import java.awt.Graphics;
import java.awt.GraphicsConfiguration;
import java.awt.GraphicsDevice;
import java.awt.GraphicsEnvironment;
import java.awt.HeadlessException;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.Transparency;
import java.awt.image.BufferedImage;
import java.awt.image.ColorModel;
import java.awt.image.MemoryImageSource;
import java.awt.image.PixelGrabber;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;
import javax.swing.ImageIcon;
import javax.swing.JPanel;

import org.omg.CORBA.IntHolder;
import org.omg.CORBA.ShortHolder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * UViewClient class
 */
public class UViewClient {
	private static final Logger logger = LoggerFactory.getLogger(UViewClient.class);

	boolean isConfigured = false;

	String corbaBridgeName = null;

	CorbaBridgeConnection bridge = null;

	Microscope msImpl = null;

	/**
	 * Maximum Image Size in Bytes
	 */
	public static final int MaxImageArrayinBytes = 512 * 512;

	/**
	 * 
	 */
	public int imageGrayWindowLow = 0;

	/**
	 * 
	 */
	public int imageGrayWindowHigh = 255;

	/**
	 * Image width
	 */
	public int imageWidth = 512;

	/**
	 * Image Height
	 */
	public int imageHeight = 512;

	short[] imageData = new short[MaxImageArrayinBytes];

	char[] imageDataBitmap = new char[MaxImageArrayinBytes];

	short compression = 0;

	/**
	 * 
	 */
	public int[] pixData = new int[MaxImageArrayinBytes];

	/**
	 * 
	 */
	public MemoryImageSource mis;

	/**
	 * 
	 */
	public Image image;

	/**
	 * 
	 */
	public BufferedImage bImage;

	boolean useMemoryImageSource = false;

	boolean useBufferedImage = true;

	/**
	 * no image rendering
	 */
	static public final int NO_RENDERING = 0;

	/**
	 * auto image rendering
	 */
	static public final int AUTO_RENDERING = 1;

	/**
	 * (0.5 ~ 99.5) scaled image
	 */
	static public final int SCALE_RENDERING = 2;

	/**
	 * rendering
	 */
	static public final int CUSTOMER_RENDERING = 3; // Customer self defined

	// scaled image rendering

	int imageRendering = NO_RENDERING;

	Graphics g;

	JPanel imagePanel;

	// abstract public void adjustGreyLevel(short op);
	// abstract public int getPixelData16();
	// abstract public int getPixelDataBitmap();

	gda.device.peem.MicroscopeControl.MicroscopePackage.UNBOUNDED_SHORTSEQUENCEHolder data = new UNBOUNDED_SHORTSEQUENCEHolder(
			imageData);

	org.omg.CORBA.IntHolder width = new IntHolder();

	org.omg.CORBA.IntHolder height = new IntHolder();

	/**
	 * Constructor.
	 */
	public UViewClient() {
	}

	/**
	 * Connect PEEM Client to Server by using the given Corba bridge
	 * 
	 * @param corbaBridgeName
	 * @return boolean true if connected else false
	 */
	public boolean connect(String corbaBridgeName) {
		boolean ret = false;
		if (!isConfigured) {
			Finder finder = Finder.getInstance();
			bridge = (CorbaBridgeConnection) finder.find(corbaBridgeName);
			if (bridge != null) {// find Corba birdge with given name
				logger.debug("Find PEEM Corba Bridge!");
			} else {
				bridge = new CorbaBridgeConnection();
				logger.debug("Create PEEM Corba Bridge!");
			}

			if (bridge != null) {
				msImpl = bridge.connect();
				isConfigured = true;
				ret = true;
				logger.info("PEEM find. Commnnication enabled");
			} else {
				logger.error("UViewClient Can NOT connect to PEEM. Check PEEM and its CORBA Bridge is running");
			}
		}

		return ret;
	}

	/**
	 * Set image rendering method
	 * 
	 * @param opt
	 *            int value of the image rendering option
	 */
	public void setRenderingMethod(int opt) {
		this.imageRendering = opt;
	}

	/**
	 * 
	 */
	public void renderingImage() {
		switch (imageRendering) {
		case NO_RENDERING:
			getGreyLevel();
			break;
		case AUTO_RENDERING:
			autoRendering();
			break;
		case SCALE_RENDERING:
			break;
		case CUSTOMER_RENDERING:
			break;
		default:
			autoRendering();
		}
	}

	/**
	 * Pure Java UViewClient part to setup graphics
	 * 
	 * @param jp
	 *            the image panel
	 */
	public void setupGraphics(JPanel jp) {
		imagePanel = jp;
		g = jp.getGraphics();
	}

	/**
	 * Pure Java UViewClient part to setup an image
	 * 
	 * @param opt
	 *            int option 0 = 'use the MemoryImageSource for drawing', 1 = 'use bufferedImage for drawing',
	 */
	public void setupImage(int opt) {
		getPixelData16();
		switch (opt) {
		case 0: // use the MemoryImageSource for drawing
			// Create the MemoryImageSource and associate it with pixData
			// array
			mis = new MemoryImageSource(imageWidth, imageHeight, ColorModel.getRGBdefault(), pixData, 0, imageWidth);
			mis.setAnimated(true);
			mis.setFullBufferUpdates(true);
			// Create a image from the MemoryImageSource
			image = Toolkit.getDefaultToolkit().createImage(mis);
			// image.setAccelerationPriority(1);
			useMemoryImageSource = true;
			useBufferedImage = false;
			break;

		case 1: // use bufferedImage for drawing
			// Create a buffered image that supports transparency
			bImage = new BufferedImage(imageWidth, imageHeight, BufferedImage.TYPE_INT_ARGB);
			useMemoryImageSource = false;
			useBufferedImage = true;
			break;
		default:
			break;
		}
	}

	/**
	 * Display the image
	 */
	public void showImage() {
		if (useMemoryImageSource && !useBufferedImage) {
			g.drawImage(image, 1, 1, imagePanel);
		} else if (!useMemoryImageSource && useBufferedImage) {
			g.drawImage(bImage, 1, 1, imagePanel);
		} else {
			logger.debug("No MemoryImageSource or BufferedImage available");
		}
	}

	/**
	 * Save the image to the file named or "UViewImage" if this is null
	 * 
	 * @param imageName
	 *            the String file name to save image to
	 */
	public void saveImage(String imageName) {
		String imn = "UViewImage";
		BufferedImage bf = null;

		if (imageName != null) {
			imn = imageName;
		}

		File outputFile = new File(imn);

		try {
			if (useMemoryImageSource && !useBufferedImage) {
				bf = toBufferedImage(image);
			} else if (!useMemoryImageSource && useBufferedImage) {
				bf = bImage;

			} else {
				logger.debug("No MemoryImageSource or BufferedImage available");
			}
			ImageIO.write(bf, "PNG", outputFile);

		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	/**
	 * Assemble the image from buffer or memory
	 * 
	 * @param len
	 *            int the length of the image in short ints
	 */
	public void assembleImage(int len) {
		if (useMemoryImageSource && !useBufferedImage) {
			assembleImageMemory(len);
		} else if (!useMemoryImageSource && useBufferedImage) {
			assembleImageBuffer(len);
		} else {
			logger.debug("No MemoryImageSource or BufferedImage available");
		}
	}

	/**
	 * Assemble the image from memory
	 * 
	 * @param len
	 *            int the length of the image in short ints
	 */
	public void assembleImageMemory(int len) {
		if (!useMemoryImageSource) {
			logger.debug("No MemoryImageSource available");
			return;
		}
		short alpha = 255;
		short s = 0;
		int x1, x2, y1, y2;
		double factor = 256.0 / (imageGrayWindowHigh - imageGrayWindowLow);
		for (int offset = 0; offset < len; offset++) {
			s = (short) (factor * (imageData[offset] - imageGrayWindowLow));
			if (s < 0)
				s = 0;
			else if (s > 255)
				s = 255;
			x1 = offset % imageWidth;
			y1 = (offset - x1) / imageHeight;
			x2 = x1;
			y2 = imageHeight - 1 - y1;
			this.pixData[x2 + imageWidth * y2] = (alpha << 24) | (s << 16) | (s << 8) | s;
		}
		mis.newPixels();
	}

	/**
	 * Assemble the image from buffer
	 * 
	 * @param len
	 *            int the length of the image in short ints
	 */
	public void assembleImageBuffer(int len) {
		if (!useBufferedImage) {
			logger.debug("No BufferedImage available");
			return;
		}
		short alpha = 255;
		short s = 0;
		int x1, x2, y1, y2;
		double factor = 256.0 / (imageGrayWindowHigh - imageGrayWindowLow);

		for (int offset = 0; offset < len; offset++) {
			s = (short) (factor * (imageData[offset] - imageGrayWindowLow));
			// s = (short) (factor * (data.value[offset] -
			// imageGrayWindowLow));
			if (s < 0)
				s = 0;
			else if (s > 255)
				s = 255;
			x1 = offset % imageWidth;
			y1 = (offset - x1) / imageHeight;
			x2 = x1;
			y2 = imageHeight - 1 - y1;
			// this.pixData[x2 + imageWidth * y2] = (alpha << 24) | (s <<
			// 16) | (s
			// << 8) | s;
			bImage.setRGB(x2, y2, (alpha << 24) | (s << 16) | (s << 8) | s);
		}

	}

	/**
	 * Returns a buffered image with the contents of an image
	 * 
	 * @param image
	 *            the image
	 * @return BufferedImage the image
	 */
	public static BufferedImage toBufferedImage(Image image) {
		if (image instanceof BufferedImage) {
			return (BufferedImage) image;
		}

		// This code ensures that all the pixels in the image are loaded
		image = new ImageIcon(image).getImage();

		// Determine if the image has transparent pixels; for this method's
		// implementation, see e661 Determining If an Image Has Transparent
		// Pixels
		boolean hasAlpha = hasAlpha(image);

		// Create a buffered image with a format that's compatible with the
		// screen
		BufferedImage bimage = null;
		GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
		try {
			// Determine the type of transparency of the new buffered image
			int transparency = Transparency.OPAQUE;
			if (hasAlpha) {
				transparency = Transparency.BITMASK;
			}

			// Create the buffered image
			GraphicsDevice gs = ge.getDefaultScreenDevice();
			GraphicsConfiguration gc = gs.getDefaultConfiguration();
			bimage = gc.createCompatibleImage(image.getWidth(null), image.getHeight(null), transparency);
		} catch (HeadlessException e) {
			// The system does not have a screen
		}

		if (bimage == null) {
			// Create a buffered image using the default color model
			int type = BufferedImage.TYPE_INT_RGB;
			if (hasAlpha) {
				type = BufferedImage.TYPE_INT_ARGB;
			}
			bimage = new BufferedImage(image.getWidth(null), image.getHeight(null), type);
		}

		// Copy image to buffered image
		Graphics g = bimage.createGraphics();

		// Paint the image onto the buffered image
		g.drawImage(image, 0, 0, null);
		g.dispose();

		return bimage;
	}

	/**
	 * This method whether the specified image has transparent pixels
	 * 
	 * @param image
	 *            the Image
	 * @return static boolean true if the specified image has transparent pixels
	 */
	public static boolean hasAlpha(Image image) {
		// If buffered image, the color model is readily available
		if (image instanceof BufferedImage) {
			BufferedImage bimage = (BufferedImage) image;
			return bimage.getColorModel().hasAlpha();
		}

		// Use a pixel grabber to retrieve the image's color model;
		// grabbing a single pixel is usually sufficient
		PixelGrabber pg = new PixelGrabber(image, 0, 0, 1, 1, false);
		try {
			pg.grabPixels();
		} catch (InterruptedException e) {
		}

		// Get the image's color model
		ColorModel cm = pg.getColorModel();
		return cm.hasAlpha();
	}

	/**
	 * Invoke the CORBA Client stubs to get the Gray Level without adjust it
	 */
	public void getGreyLevel() {
		org.omg.CORBA.ShortHolder low = new ShortHolder();
		org.omg.CORBA.ShortHolder hi = new ShortHolder();

		msImpl.DoGrayAdjust((short) 0, low, hi);

		imageGrayWindowHigh = hi.value;
		imageGrayWindowLow = low.value;
	}

	/**
	 * Invoke the CORBA Client stubs to get the Gray Level and adjust the contrast and brightmess automatically
	 */
	public void autoRendering() {
		org.omg.CORBA.ShortHolder low = new ShortHolder();
		org.omg.CORBA.ShortHolder hi = new ShortHolder();

		msImpl.DoGrayAdjust((short) 1, low, hi);
		imageGrayWindowHigh = hi.value;
		imageGrayWindowLow = low.value;
	}

	/**
	 * Invoke the CORBA Client stubs to do grey adjustment
	 * 
	 * @param op
	 *            short value of the grey level
	 */
	public void adjustGreyLevel(short op) {
		org.omg.CORBA.ShortHolder low = new ShortHolder();
		org.omg.CORBA.ShortHolder hi = new ShortHolder();

		msImpl.DoGrayAdjust(op, low, hi);
		imageGrayWindowHigh = hi.value;
		imageGrayWindowLow = low.value;
	}

	/**
	 * Invoke the CORBA Client stubs to get image data and assemble them into an array
	 * 
	 * @return int length of image else 0='invalid image data'
	 */
	public int getPixelData16() {
		int length = msImpl.GetImageData(data, width, height, compression);
		if (length < 0 || length > MaxImageArrayinBytes) {
			logger.debug("GetImageData() returned with fault: wrong size: " + length);
			return 0;
		}

		imageWidth = width.value;
		imageHeight = height.value;
		imageData = data.value;

		// logger.debug("Dumped image data details: total byte " + length + " =
		// "
		// + imageWidth + " x " +imageHeight);

		return length;
	}

	/**
	 * Get pixel data bitmap
	 * 
	 * @return int the length of the data else 0 if error
	 */
	public int getPixelDataBitmap() {
		gda.device.peem.MicroscopeControl.MicroscopePackage.UNBOUNDED_SEQUENCEHolder dataBitmap = new UNBOUNDED_SEQUENCEHolder(
				new char[512 * 512]);
		int length = msImpl.GetImageBitmapData(dataBitmap, width, height, compression);

		if (length < 0) {
			logger.debug("GetImageData() returned with fault: " + length);
			return 0;
		}
		if (length > MaxImageArrayinBytes) {
			logger.debug("GetImageData() returned with fault: image too big");
			return 0;
		}

		imageWidth = width.value;
		imageHeight = height.value;
		imageDataBitmap = dataBitmap.value;

		char alpha = 0xff;
		int x1 = 0, x2 = 0, y1 = 0, y2 = 0;
		for (int offset = 0; offset < length; offset += 3) {
			x1 = offset / 3 % imageWidth;
			y1 = (offset / 3 - x1) / imageHeight;
			x2 = x1;
			y2 = imageHeight - 1 - y1;
			bImage.setRGB(x2, y2, (alpha << 24) | (imageDataBitmap[offset] << 16) | (imageDataBitmap[offset + 1] << 8)
					| imageDataBitmap[offset + 2]);
		}

		return length;
	}

	/**
	 * Invoke the CORBA Client stubs to get camera exposure time in unit second
	 * 
	 * @return double value of exposure time in seconds
	 */
	public double getCameraExposureTime() {
		return 0.001 * msImpl.GetCameraExpTime();
	}

	/**
	 * Invoke the CORBA Client stubs to set camera exposure time in unit second
	 * 
	 * @param et
	 *            double value of exposure time in seconds
	 */
	public void setCameraExposureTime(double et) {
		msImpl.PutCameraExpTime((float) (et * 1000));
	}

	/**
	 * Invoke the CORBA Client stubs to set PCO camera acquisition submode sequential = true / simultaneous = false
	 * 
	 * @param bs
	 *            boolean true if sequential mode else false
	 */
	public void setCameraSequentialMode(boolean bs) {
		msImpl.PutSequential(bs);
	}

	/**
	 * Invoke the CORBA Client stubs to get PCO camera acquisition submode
	 * 
	 * @return boolean true if sequential mode
	 */
	public boolean getCameraSequentialMode() {
		return msImpl.GetSequential();
	}

	
	/**
	 * Invoke the CORBA Client stubs to get the number the PCO camera used to average images
	 * 
	 * @return the number of images used for averaging
	 */
	public int getAverageImageNumber() {
		return msImpl.GetAverageImages();
	}

	/**
	 * Invoke the CORBA Client stubs to set the number that PCO camera used to average images
	 * 
	 * @param numberOfImages
	 *            number Of images used for averaging
	 */
	public void setAverageImageNumber(int numberOfImages) {
		msImpl.PutAverageImages( (short)numberOfImages );
	}
	
	
	/**
	 * Invoke the CORBA Client stubs to check if new image is ready when "continuous acquisition" is ON
	 * 
	 * @return boolean true if image ready
	 */
	public boolean isImageReady() {
		return msImpl.GetNewImageReady();
	}

	/**
	 * Invoke the CORBA Client stubs to check the current state of "single image acquisition" when "continuous
	 * acquisition" is OFF
	 * 
	 * @return boolean true if in progress
	 */
	public boolean isInProgress() {
		return msImpl.GetAcquisitionInProgress();
	}

	/**
	 * Invoke the CORBA Client stubs to set the current state of "single image acquisition" when "continuous
	 * acquisition" is OFF
	 * 
	 * @param bs
	 *            boolean true if sequential mode else false
	 */
	public void setInProgress(boolean bs) {
		// TODO New implementation in 1.4.2
		msImpl.SetAcquisitionInProgress(bs);
	}

	/**
	 * Invoke the CORBA Client stubs to check the current state of "single image acquisition" when "continuous
	 * acquisition" is OFF
	 * 
	 * @param imageID
	 *            int value of the image ID
	 */
	public void shotSingleImage(int imageID) {
		// TODO New implementation in 1.4.2
		// msImpl.AcquireSingleImage(imageID);

		msImpl.AcquireSingleImage((short) imageID);
	}

	/**
	 * Save image file from the PEEM Control machine (Windows) directly
	 * 
	 * @param imageFileNameWin32
	 *            the file to save image to
	 */
	public void exportImage(String imageFileNameWin32, short format, short imagecontents) {
		msImpl.ExportImage(imageFileNameWin32, format, imagecontents);
	}

	/**
	 * Define Region Of Interest (ROI)
	 * 
	 * @param id
	 * @param x
	 * @param y
	 * @param width
	 * @param height
	 */
	public void setROI(int id, int x, int y, int width, int height) {
		int color = 0;
		int interactive = 0;

		int x1, x2, y1, y2;
		x1 = x;
		x2 = x + width;
		y1 = y;
		y2 = y + height;

		msImpl.IvsTDefineROI((short) id, color, (short) x1, (short) y1, (short) x2, (short) y2, (char) interactive);
	}

	/**
	 * Return the normalised intensity in a specified ROI, which is identified by the id parameter
	 * 
	 * @param id
	 *            id of ROI
	 * @return double ROI data
	 */
	public double getROIData(int id) {

		return msImpl.ROIdata((short) id);

	}

	/**
	 * Return the normalised intensity in a specified ROI, which is identified by the id parameter
	 * 
	 * @return int ROI data
	 */
	public int setROIPlot() {
		return msImpl.IvsTAddPoint((short) 0, (short) 0, (float) 0.0, (float) 0.0);

	}

}
