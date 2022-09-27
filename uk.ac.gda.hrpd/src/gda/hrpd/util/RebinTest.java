/*-
 * Copyright © 2009 Diamond Light Source Ltd., Science and Technology
 * Facilities Council
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

package gda.hrpd.util;

import java.io.BufferedReader;
import java.io.InputStreamReader;

/**
 * try to test Java communication with external processing - streaming message live, in blocks, or at end
 *
 */
public class RebinTest {
	/**
	 * @param argv
	 */
	public static void main(String argv[]) {
		try {
			String line;
			String[] cmdArray = new String[4];
			cmdArray[0] = "/dls_sw/tools/bin/python2.4";
			cmdArray[1] = "/dls/i11/software/gda/config/scripts/rebin.py";
			cmdArray[2] = "/dls/i11/data/2008/ee0/5612.dat";
			cmdArray[3] = "0.001";
			ProcessBuilder pb = new ProcessBuilder(cmdArray);
			pb.redirectErrorStream(true);
			
			Process p = pb.start(); //Runtime.getRuntime().exec(cmdArray);
			// get normal output of the external process
			BufferedReader input = new BufferedReader(new InputStreamReader(p.getInputStream()));
			// get error output of the external process
			//BufferedReader error = new BufferedReader(new InputStreamReader(p.getErrorStream()));
			// write to the input of the external process
			//BufferedWriter output = new BufferedWriter(new OutputStreamWriter(p.getOutputStream()));

			while ((line = input.readLine()) != null) {
				System.out.println(line);
			}
			
//			while ((line = error.readLine()) != null) {
//				System.out.println(line);
//			}

			input.close();
//			error.close();
//			output.close();
		} catch (Exception err) {
			err.printStackTrace();
		}
	}
}
