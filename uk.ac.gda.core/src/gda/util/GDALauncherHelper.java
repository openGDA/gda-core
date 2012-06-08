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

package gda.util;

import java.io.PrintWriter;

/**
 * Class to allow testing of starting the gda software by the new gdascripts/launcher/gda_launcher.py
 */
public class GDALauncherHelper {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		PrintWriter writer;
		writer = new PrintWriter(System.out);//args[0]);
		writer.write("TestStartupScript_result= {\n");
		writer.write("\"Release\":\""+ Version.getRelease()+ "\"\n");
		writer.write(",\"CLASSPATH\":\""+ System.getenv("CLASSPATH").replace("\\","\\\\") +"\"\n");
		writer.write("}\n");
		writer.close();
		System.exit(0);
	}

}
