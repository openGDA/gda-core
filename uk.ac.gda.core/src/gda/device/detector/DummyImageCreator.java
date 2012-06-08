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

package gda.device.detector;

import gda.data.NumTracker;
import gda.data.PathConstructor;
import gda.data.metadata.GDAMetadataProvider;
import gda.data.metadata.Metadata;
import gda.device.Detector;
import gda.device.DeviceException;
import gda.factory.FactoryException;

import java.awt.Color;
import java.awt.Graphics2D;
import java.awt.image.BufferedImage;
import java.io.File;
import java.io.IOException;

import javax.imageio.ImageIO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * A Dummy class that will create an image file of a specified format.
 */
public class DummyImageCreator extends DetectorBase implements Detector {
	
	private static final Logger logger = LoggerFactory.getLogger(DummyImageCreator.class);
	
	/**
	 * Static string to define PNG format.
	 */
	static public final String PNG = "png";

	/**
	 * Static string to define JPEG format.
	 */
	static public final String JPEG = "jpg";

	private int width = 100;

	private int height = 100;

	private String format = "";

	private int status;

	private NumTracker runNumber;

	@Override
	public void configure() throws FactoryException {
		initialiseRunNumber();
	}

	@Override
	public boolean createsOwnFiles() throws DeviceException {
		return true;
	}

	@Override
	public int[] getDataDimensions() throws DeviceException {
		int[] dims = { width, height };
		return dims;
	}

	@Override
	public void collectData() throws DeviceException {
		// Intentionally left blank.
	}

	@Override
	public int getStatus() throws DeviceException {
		return status;
	}

	@Override
	public Object readout() throws DeviceException {
		status = Detector.BUSY;
		File dir = null;

		String filename = "notcreated";
		File f;

		// Create a buffered image in which to draw
		BufferedImage bufferedImage = new BufferedImage(width, height, BufferedImage.TYPE_INT_RGB);

		// Create a graphics contents on the buffered image
		Graphics2D g2d = bufferedImage.createGraphics();

		// Draw graphics
		g2d.setColor(Color.white);
		g2d.fillRect(0, 0, width, height);
		g2d.setColor(Color.black);
		g2d.fillOval(0, 0, width, height);

		// Graphics context no longer needed so dispose it
		g2d.dispose();

		try {
			dir = new File(getDummyDataDir());
			if (!dir.exists()) {
				dir.mkdir();
			}
			f = File.createTempFile("dummy-image", ".png", new File(getDummyDataDir()));
			ImageIO.write(bufferedImage, format, f);
			filename = f.getCanonicalPath();
			status = Detector.IDLE;
		} catch (IOException e) {
			// Failed to write file (without errors!)
			status = Detector.FAULT;
		}

		return filename;
	}

	private void initialiseRunNumber() {
		String beamline = null;

		Metadata metadata = GDAMetadataProvider.getInstance();

		try {
			beamline = metadata.getMetadataValue("instrument", "gda.instrument", "base");
		} catch (DeviceException e1) {
		}

		try {
			runNumber = new NumTracker(beamline);
		} catch (IOException e) {
			logger.debug("ERROR: Could not instantiate NumTracker in DummyImageCreator().");
		}
	}

	private String getDummyDataDir() {
		String dir = null;

		dir = PathConstructor.createFromDefaultProperty();

		if (dir == null) {
			dir = "/tmp/";
		} else {
			if (runNumber != null) {
				dir += "/" + (runNumber.getCurrentFileNumber()) + "/";
			}
		}

		return dir;
	}

	/**
	 * @return The width of the image
	 */
	public int getWidth() {
		return width;
	}

	/**
	 * @param width
	 */
	public void setWidth(int width) {
		this.width = width;
	}

	/**
	 * @return the height of the image
	 */
	public int getHeight() {
		return height;
	}

	/**
	 * @param height
	 */
	public void setHeight(int height) {
		this.height = height;
	}

	/**
	 * @return image file format
	 */
	public String getFormat() {
		return format;
	}

	/**
	 * @param format
	 *            The image format
	 */
	public void setFormat(String format) {
		this.format = format;
	}

	@Override
	public String getDescription() throws DeviceException {
		return "Dummy Image Creating Detector";
	}

	@Override
	public String getDetectorID() throws DeviceException {
		return "dumdum-1";
	}

	@Override
	public String getDetectorType() throws DeviceException {
		return "Dummy";
	}

}
