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

package gda.device.detector.odccd;


import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.Serializable;
import java.nio.ByteBuffer;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * <p>
 * <b>Title: </b>Class to hold the Oxford Diffraction CCD image files
 * </p>
 * <p>
 * <b>Description: </b>This class can be used to hold the Oxford diffraction CCD image files. It is designed to be used
 * with the ISBinaryHeader class. There is a method to write the image to a file.
 * </p>
 */

public class ODCCDImage implements Serializable {
	private static final Logger logger = LoggerFactory.getLogger(ODCCDImage.class);

	private ByteBuffer mImage = ByteBuffer.allocate(20000000); // 20MB - i know this is inefficient

	/**
	 * Add a byte buffer to the image buffer. This method will append data to the image buffer sequentially.
	 * 
	 * @param dataBlock
	 *            The byte buffer to use.
	 * @param headerInfo
	 *            The header information object.
	 */
	public void addImageData(byte[] dataBlock, @SuppressWarnings("unused") ISBinaryHeader headerInfo) {
		mImage.put(dataBlock, 0, dataBlock.length);
	}

	/**
	 * Write the image buffer to a file.
	 * 
	 * @param filename
	 *            The filename to use.
	 */
	public void writeToFile(String filename) {
		File imageFile = new File(filename);
		try {
			if (mImage.hasArray()) {
				OutputStream out = new FileOutputStream(imageFile);
				out.write(mImage.array());
				out.close();
			} else {
				logger.error("ERROR: Could not write ODCCD image to file.");
			}
		} catch (FileNotFoundException e1) {
			logger.error("ERROR: Could not find " + filename);
		} catch (IOException e1) {
			logger.error("ERROR: IOException caught when trying to write to " + filename);
		}
	}

}
