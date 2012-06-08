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

package gda.util;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * JSeam adapter class providing Java bindings to PINCER SEAM1 and SEAM2 C code. Currently only a Windows version is
 * available built on the native side using National Instruments Labwindows/CVI v7.1 but in principle a similar version
 * for Linux generic shared libraries could be built. The unmodified PINCER SEAM DLL is accessed via a generic JNI
 * native interface DLL called "seam.dll" using the method "callSeam (...)" for both SEAM1 and SEAM2 types. The full
 * path name of the PINCER SEAM '.lib' type library is passed as an argument to the JSeam JAVA methods "callJSeam1(...)"
 * and "callJSeam2(...)". These methods are called by JCLAM to allow SEAM1 and SEAM2 function calls. Windows DLLs must
 * be included in the system path (recommended), system directory or current directory. See SR Newsround issue 10 for a
 * description of the XRD PINCER SEAM. The JSeam class implements a singleton pattern and initially loads the
 * "seam.dll".
 */

public class JSeam {
	private static final Logger logger = LoggerFactory.getLogger(JSeam.class);

	/**
	 * JNI C method provided by "seam.dll"
	 * 
	 * @param moduleStr
	 *            full path name of PINCER SEAM '.lib' type library
	 * @param readWrite
	 *            int flag true for SEAM2 "readwrite" call needed
	 * @param cmdStr
	 *            SEAM command string
	 * @param replyMsgStr
	 *            1st element reply from SEAM call with status/ messages stripped and 2nd element warning or error
	 *            message depending on whether status=0 or status!=1 respectively.
	 * @param nVals
	 *            no. of numeric values to write out or read back
	 * @param dataArray
	 *            array of numeric values for read/write
	 * @return SEAM warning (status=0) or error message (status >1)
	 */
	private native int callSeam(String moduleStr, int readWrite, String cmdStr, String replyMsgStr[], int nVals,
			double dataArray[]);

	// class reference, null until instatiated
	private static JSeam jseamInstance = new JSeam();

	/**
	 * singleton pattern constructor load seam DLL and allow default exception if it fails
	 */
	private JSeam() {
		logger.debug("loading seam.dll ...");
		System.loadLibrary("seam");
	}

	/**
	 * singleton pattern constructor called from here make and store a single JSeam object only This is the only way to
	 * access a JSeam object
	 * 
	 * @return singleton object reference of JSeam object
	 */
	public static JSeam getInstance() {
		return jseamInstance;
	}

	/**
	 * Java SEAM1 format call. Dummy arguments are set for the JNI call
	 * 
	 * @param moduleStr
	 *            full path name of PINCER SEAM1 '.lib' type library
	 * @param cmdStr
	 *            SEAM command string
	 * @param replyMsgStr
	 *            string array[2], reply and warning/error messages
	 * @throws Exception
	 *             if call fails either due to lack of SEAM DLL or because the hardware involved is not found or is
	 *             faulty.
	 */
	public void callJSeam1(String moduleStr, String cmdStr, String replyMsgStr[]) throws Exception {
		int readWrite = 0, // 0 for no readWrite SEAM2 function
		nVals = 2, // dummy arguments
		status = -1; // default failed status
		double dataArray[] = { 0., 0 };

		// set reply and message to default
		replyMsgStr[0] = "";
		replyMsgStr[1] = "!!! error executing callSeam native method";

		// invoke the SEAM1 call with some dummy arguments
		status = callSeam(moduleStr, readWrite, cmdStr, replyMsgStr, nVals, dataArray);

		// throw exception if an error status is returned
		if (status != 0) {
			throw new Exception(moduleStr + " command : '" + cmdStr + "' has failed : " + replyMsgStr[1]);
		}

		return;
	}

	/**
	 * Java SEAM2 format call.
	 * 
	 * @param moduleStr
	 *            full path name of PINCER SEAM1 '.lib' type library
	 * @param cmdStr
	 *            SEAM command string
	 * @param replyMsgStr
	 *            string array[2], reply and warning/error messages
	 * @param nVals
	 *            no. of numeric values to write out or read back
	 * @param dataArray
	 *            array of numeric values for read/write
	 * @throws Exception
	 *             Exception if call fails either due to lack of SEAM DLL or because the hardware involved is not found
	 *             or is faulty.
	 */
	public void callJSeam2(String moduleStr, String cmdStr, String replyMsgStr[], int nVals, double dataArray[])
			throws Exception {
		int readWrite = 1, // 1 for readWrite SEAM2 function
		status = -1; // default failed status

		// set reply and message to default
		replyMsgStr[0] = "";
		replyMsgStr[1] = "!!! error executing callSeam native method";

		// invoke the SEAM1 call with specified arguments
		status = callSeam(moduleStr, readWrite, cmdStr, replyMsgStr, nVals, dataArray);

		// throw exception if an error status is returned
		if (status != 0) {
			throw new Exception(moduleStr + " command : '" + cmdStr + "' has failed : " + replyMsgStr[1]);
		}
		return;
	}

}
