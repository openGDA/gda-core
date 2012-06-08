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

import java.io.FileOutputStream;
import java.io.PrintStream;

/**
 * Sets up general test environment with no GDA specific actions. It requires no program or VM arguments and is written
 * to be portable. This class implements the singleton pattern.
 */
public class GeneralSetup {
	private static final GeneralSetup generalSetupObj = new GeneralSetup();

	private GeneralSetup() {
	}

	/**
	 * method to deliver the single instance of this object to external classes.
	 * 
	 * @return the general test environment setup instance.
	 */
	public static GeneralSetup getInstance() {
		return (generalSetupObj);
	}

	/**
	 * Sets complete test environment by calling all public methods in class. It requires no program or VM arguments and
	 * is written to be portable. A tearDownAll() method is available for any necessary tidying operations.
	 * 
	 * @throws Exception
	 */
	public void setUpAll() throws Exception {
		setUpNoOutput();
	}

	/**
	 * Sets output screen to file "del.log" as some GDA code generates messages which are hard to turn off and JUnit
	 * requires no outputs.
	 * 
	 * @throws Exception
	 */
	public void setUpNoOutput() throws Exception {
		PrintStream out = new PrintStream(new FileOutputStream("del.log"));
		System.setOut(out);
	}

	/**
	 * resets complete test environment for compatibility with GDASetup class. NB if things don't need stopping, this
	 * should not generate an error.
	 */
	public void tearDownAll() {
	}

}
