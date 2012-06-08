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

package gda.testlib;

/**
 * Placeholder for main method to start the Simulator GDA event service in this VM and leave running.
 */
public class StartVMTree {
	private static Process vmTreeProcess;

	/**
	 * Main Method.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		String propFile = ".", jacorbConfDir = ".";

		// test of startNameServer using simple GUI/text app.
		String cmd = "java -Ddl.propertiesFile=" + propFile + " " + "-Djacorb.config.dir=" + jacorbConfDir + " "
				+ "VMTree" + " " + "stnSimulator.eventChannel";

		vmTreeProcess = Runtime.getRuntime().exec(cmd);

		if (vmTreeProcess == null) {
			throw new Exception("VMTree has not started (null)");
		}
	}
}
