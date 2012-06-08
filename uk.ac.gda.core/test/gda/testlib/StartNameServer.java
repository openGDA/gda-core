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
 * Placeholder for main method to start the Simulator GDA name service in its own process and leave running.
 */
public class StartNameServer {
	/**
	 * Main Method.
	 * 
	 * @param args
	 * @throws Exception
	 */
	public static void main(String[] args) throws Exception {
		// set paths for simulator properties and jacorb locations
		GDASetup gdaSetup = GDASetup.getInstance();
		gdaSetup.setUpSimProperties();
		// launch the name server native program
		gdaSetup.setUpNameServer();
	}
}
