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

import gda.data.nexus.FileNameBufToStrings;

import java.io.UnsupportedEncodingException;

import org.junit.Assert;
import org.junit.Test;

public class FileNameBufToStringsTest {

	@Test
	public void testDim1() throws UnsupportedEncodingException {

		String dataFileName = "This is a test";

		byte[] buf = StringToByteArray(dataFileName);
		int[] dimensions = new int[] { FileNameBufToStrings.MAX_DATAFILENAME };

		String[] stringList = new FileNameBufToStrings(dimensions, buf).getFilenames();
		Assert.assertEquals(dataFileName, stringList[0]);
	}

	private byte[] StringToByteArray(String dataFileName) {
		return StringToByteArray(new String[] { dataFileName }, new int[] { 1 });
	}

	private byte[] StringToByteArray(String[] dataFileNames, int[] dimensions) {
		int len = dataFileNames.length * FileNameBufToStrings.MAX_DATAFILENAME;
		byte filenameBytes[] = new byte[len];
		java.util.Arrays.fill(filenameBytes, (byte) 0); // zero terminate

		if (dimensions.length == 1) {
			int offset = 0;
			for (String dataFileName : dataFileNames) {
				for (int k = 0; k < dataFileName.length(); k++) {
					filenameBytes[k + offset] = (byte) dataFileName.charAt(k);
				}
				offset += FileNameBufToStrings.MAX_DATAFILENAME;
			}
			return filenameBytes;
		} else if (dimensions.length == 2) {
			int offset = 0;
			for (int i = 0; i < dimensions[0]; i++) {
				for (int j = 0; j < dimensions[1]; j++) {
					String dataFileName = dataFileNames[i * dimensions[1] + j];
					for (int k = 0; k < dataFileName.length(); k++) {
						filenameBytes[k + offset] = (byte) dataFileName.charAt(k);
					}
					offset += FileNameBufToStrings.MAX_DATAFILENAME;
				}
			}
			return filenameBytes;
		}
		return null;
	}

	@Test
	public void testDim2() throws UnsupportedEncodingException {

		String dataFileName = "This is a test";
		String dataFileName1 = "This is a test1";

		String[] dataFileNames = new String[] { dataFileName, dataFileName1 };
		int[] dataFileNamesDimensions = new int[] { dataFileNames.length };
		byte[] buf = StringToByteArray(dataFileNames, dataFileNamesDimensions);

		int[] dimensions = new int[] { dataFileNames.length, FileNameBufToStrings.MAX_DATAFILENAME };
		String[] stringList = new FileNameBufToStrings(dimensions, buf).getFilenames();
		Assert.assertEquals(dataFileName, stringList[0]);
		Assert.assertEquals(dataFileName1, stringList[1]);
	}

	@Test
	public void testDim3() throws UnsupportedEncodingException {

		String dataFileName1A = "This is a test1A";
		String dataFileName2A = "This is a test2A";

		String dataFileName1B = "This is a test1B";
		String dataFileName2B = "This is a test2B";

		String dataFileName1C = "This is a test1C";
		String dataFileName2C = "This is a test2C";

		String[] dataFileNames = new String[] { dataFileName1A, dataFileName1B, dataFileName1C, 
				dataFileName2A, dataFileName2B, dataFileName2C };
		int[] dataFileNamesDimensions = new int[] { 2, 3 };

		byte[] buf = StringToByteArray(dataFileNames, dataFileNamesDimensions);
		int[] dimensions = new int[] { 2, 3, FileNameBufToStrings.MAX_DATAFILENAME };

		String[] stringList = new FileNameBufToStrings(dimensions, buf).getFilenames();
		Assert.assertEquals(dataFileName1A, stringList[0]);
		Assert.assertEquals(dataFileName1B, stringList[1]);
		Assert.assertEquals(dataFileName1C, stringList[2]);
		Assert.assertEquals(dataFileName2A, stringList[3]);
		Assert.assertEquals(dataFileName2B, stringList[4]);
		Assert.assertEquals(dataFileName2C, stringList[5]);
	}

}
