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

package uk.ac.diamond.daq.persistence.bcm;

import gda.device.Scannable;
import gda.device.ScannableMotion;
import gda.jython.JythonServerFacade;

/**
 *
 */
public class Util {

	/**
	 * output a string to the JythonTerminal(s)
	 *
	 * @param toPrint
	 */
	public static void printTerm(String toPrint) {
		JythonServerFacade.getInstance().print(toPrint);
	}

	/**
	 * Returns a scannable from the Jython names space.
	 *
	 * @param name
	 *            The name of the scannable as referred to in the Jython namespace.
	 * @return ScannableMotion The scannable.
	 * @throws BcmException
	 *             If the scannable does not exist, is null, or cannot be cast to a MotionScannable
	 */
	public static Scannable getScannableFromJython(String name) throws BcmException {
		Object objectToReturn = JythonServerFacade.getInstance().getFromJythonNamespace(name);
		// Check the object was found (i.e. is not null)
		if (objectToReturn == null) {
			throw new BcmException("Could not find scannable " + name
					+ " in Jython\'s root namespace. (Or it was null when found.)");
		}

		// Try and cast the object to a scannable
		Scannable scannableToReturn;
		try {
			scannableToReturn = (Scannable) objectToReturn;
		} catch (RuntimeException e) {
			throw new BcmException("The object " + name + " from Jython\'s root namespace is not a Scannable");
		}
		return scannableToReturn;
	}

	/**
	 * Return true if the incoming scannable can be found in the JythonNamespace using its .getName() name
	 *
	 * @param foo
	 *            scannable
	 * @return validness
	 */
	public static boolean isScannableNameValid(Scannable foo) {
		Scannable bar;
		try {
			bar = getScannableFromJython(foo.getName());
		} catch (BcmException e) {
			// printTerm("Could not get your scannable from the namespace");
			return false;
		}

		if (bar == foo) {
			// printTerm("That looks good, I got the same thing!");
			return true;
		}

		return false;
	}

	/**
	 * @param scannable
	 * @return first upper limit or null
	 */
	public static Double getUpperLimit(ScannableMotion scannable) {
		Double l[] = scannable.getUpperGdaLimits();
		if (l == null)
			return null;
		return l[0];
	}

	/**
	 * @param scannable
	 * @return first lower limit or null
	 */
	public static Double getLowerLimit(ScannableMotion scannable) {
		Double l[] = scannable.getLowerGdaLimits();
		if (l == null)
			return null;
		return l[0];
	}
}
