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

package gda.device.detector.odccd;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;

public class ReadCrysalisHeader {

	int ASCII_HEADER_SIZE = 256;
	int BIN_HEADER_SIZE = 4864;
	int COMBINED_HEADER_SIZE = ASCII_HEADER_SIZE + BIN_HEADER_SIZE;
	byte[] header;

	public ReadCrysalisHeader(String inputFileName) {

		File inputFile = new File(inputFileName);

		header = new byte[COMBINED_HEADER_SIZE];

		InputStream is = null;
		try {
			is = new FileInputStream(inputFile);
			is.read(header, 0, COMBINED_HEADER_SIZE);
		} catch (IOException e) {
			e.printStackTrace();
		}
	}

	public String readHeader(String field) {

		int specialOffset = 768;
		String result = null;
		if (field.equals("imon1")) {
			byte[] b = new byte[4];
			b[3] = header[specialOffset + 528];
			b[2] = header[specialOffset + 529];
			b[1] = header[specialOffset + 530];
			b[0] = header[specialOffset + 531];
			result = Integer.toString(byteArrayToInt(b));
		}
		
		if (field.equals("imon2")) {
			byte[] b = new byte[4];
			b[3] = header[specialOffset + 532];
			b[2] = header[specialOffset + 533];
			b[1] = header[specialOffset + 534];
			b[0] = header[specialOffset + 535];
			result = Integer.toString(byteArrayToInt(b));
		}

		return result;
	}

	public static int byteArrayToInt(byte[] b) {
        int value = 0;
        for (int i = 0; i < 4; i++) {
            int shift = (4 - 1 - i) * 8;
            value += (b[i] & 0x000000FF) << shift;
        }
        return value;
    }

}
