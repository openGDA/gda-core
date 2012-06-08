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

package gda.device.gpib;

/**
 * A JNI class for native c code. To enable access to National Instruments IEEE488 interface library
 */

public class JniGpib {
	/**
	 * @param string
	 * @param device
	 * @param dataArray
	 * @return String
	 */
	public native String readwrite(String string, int device[], int dataArray[]);

	/**
	 * @param string
	 * @param device
	 * @param buffer
	 * @param nChars
	 * @return long
	 */
	public native long readwrite2(String string, int device[], byte buffer[], long nChars);

	/**
	 * @param modulename
	 *            is the name of the native interface library eg. windows DLL
	 */
	public JniGpib(String modulename) {
		System.loadLibrary(modulename);
	}

	/**
	 * @param res
	 * @param deviceDesc
	 * @param dataArray
	 * @return String
	 */
	public String callJni(String res, int deviceDesc[], int dataArray[]) {
		return readwrite(res, deviceDesc, dataArray);
	}

	/**
	 * @param res
	 * @param deviceDesc
	 * @param buffer
	 * @param no
	 * @return long
	 */
	public long callJni2(String res, int deviceDesc[], byte buffer[], long no) {
		return readwrite2(res, deviceDesc, buffer, no);
	}

}