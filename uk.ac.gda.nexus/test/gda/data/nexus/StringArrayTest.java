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

package gda.data.nexus;

import org.junit.Ignore;
import org.junit.Test;

import org.nexusformat.NexusException;
import org.nexusformat.NexusFile;

/**
 * Small test class for writing String Arrays.
 */
public class StringArrayTest {

	/**
	 * This is a dummy test, since no real JUnit tests exist in this class.
	 */
	@Ignore("Incomplete test class - it does contain not any Junit-runnable methods.")
	@Test
	public void dummyTest() {
	// TODO: Remove dummyTest and add some Junit-runnable methods (add @Test where appropriate).
	}

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		// TODO Auto-generated method stub

		int[] startPos = new int[2];
		int[] dimArray = new int[2];
		String value = null;

		int nDims = 2;
		int nPoints = 10;
		int defaultLength = 255;

		try {
			// Lets create a new file.
			NexusFile file = new NexusFile("/tmp/file.nxs", NexusFile.NXACC_CREATE5);

			dimArray[0] = NexusFile.NX_UNLIMITED;
			dimArray[1] = defaultLength;

			file.makegroup("test", "NXnote");
			file.opengroup("test", "NXnote");

			file.makedata("stringarray", NexusFile.NX_CHAR, nDims, dimArray);

			file.opendata("stringarray");

			for (int i = 0; i < nPoints; i++) {

				value = "file" + i;

				byte valueBytes[] = new byte[defaultLength];

				for (int k = 0; k < value.length(); k++) {
					valueBytes[k] = (byte) value.charAt(k);
				}

				dimArray[0] = 1;
				dimArray[1] = defaultLength;

				startPos[0] = i;
				startPos[1] = 0;

				file.putslab(valueBytes, startPos, dimArray);

			}

			file.closedata();

			file.closegroup();

			file.close();
		} catch (NexusException e) {
			e.printStackTrace();
		}

	}

}
