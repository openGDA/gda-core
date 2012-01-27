/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package gda.data.nexus;

import java.io.UnsupportedEncodingException;

/**
 * FileNameBufToStrings is used to convert a buf read from Nexus containing a collection of filenames to a String[]
 */
public class FileNameBufToStrings {
	public static final int MAX_DATAFILENAME = 255;

	private int offset = 0;
	private int filenamesOffset = 0;
	private byte[] buffer = null;
	private String[] filenames = null;

	public String[] getFilenames() {
		return filenames;
	}

	public FileNameBufToStrings(int[] dimensions, byte[] buffer) throws UnsupportedEncodingException {
		super();
		this.offset = 0;
		this.filenamesOffset = 0;
		this.buffer = buffer;
		if (dimensions == null || dimensions.length < 1)
			throw new IllegalArgumentException("dimensions.length must be >=1");

		int totalNumFileNames = 1;
		for (int i = 0; i < dimensions.length - 1; i++) {
			totalNumFileNames *= dimensions[i];
		}
		if (totalNumFileNames != buffer.length / MAX_DATAFILENAME)
			throw new IllegalArgumentException("buffer length does not match dimensions");

		filenames = new String[totalNumFileNames];
		fillStringArray(dimensions);

	}

	private void fillStringArray(int[] dimensions) throws UnsupportedEncodingException {
		if (dimensions.length == 1) {
			if (MAX_DATAFILENAME > buffer.length - offset)
				throw new IllegalArgumentException("buffer is not large enough for given dimensions");

			// look for null
			int len = MAX_DATAFILENAME;
			for (int i = 0; i < MAX_DATAFILENAME; i++) {
				if (buffer[i + offset] == 0) {
					len = i;
					break;
				}
			}
			filenames[filenamesOffset] = new String(buffer, offset, len, "UTF-8");
			offset += MAX_DATAFILENAME;
			filenamesOffset++;
		} else {
			for (int iFilename = 0; iFilename < dimensions[0]; iFilename++) {
				int[] newDims = java.util.Arrays.copyOfRange(dimensions, 1, dimensions.length);
				fillStringArray(newDims);
			}

		}
	}

}
