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

package gda.server.collisionAvoidance;

import gda.jython.JythonServerFacade;

import java.lang.reflect.Array;

import org.python.core.PySequence;

/*
 * Returns an array of integers extracted from a position Object. Position may be a java integer, or array, or a
 * PySequence. Rumour says that in other parts of the gda, position may be a string with units: this code is untested
 * with this type of position!
 */

/**
 *
 */
public final class HelperFunctions {

	/**
	 * @param position
	 * @param desiredNumInputArgs
	 * @return an array
	 * @throws CacException
	 */
	public static Double[] positionToArray(Object position, int desiredNumInputArgs) throws CacException

	{
		// if objects position defined by a single number
		Double[] posArray = new Double[desiredNumInputArgs];
		if (desiredNumInputArgs == 1) {

			// work out what position is and turn it into an array of integers

			if (position.getClass().isArray()) {
				posArray = (Double[]) position;
			} else if (position instanceof PySequence) {
				posArray[0] = Double.parseDouble(((PySequence) position).__finditem__(0).toString());
			} else {
				posArray[0] = Double.parseDouble(position.toString());
			}

			if (posArray[0] == null) {
				throw new CacException("Could not parse input array to get single argument");
			}
		}
		// if objects position defined by an array of numbers
		else
		// (desiredNumInputArgs ~= 1)
		{
			if (position.getClass().isArray()) {
				for (int i = 0; i < desiredNumInputArgs; i++) {
					posArray[i] = Array.getDouble(position, i);
					if (posArray[i] == null) {
						throw new CacException("Could not parse input array to get " + desiredNumInputArgs
								+ " arguments");
					}
				}

			}

			else if (position instanceof PySequence) {
				for (int i = 0; i < desiredNumInputArgs; i++) {
					posArray[i] = Double.parseDouble(((PySequence) position).__finditem__(i).toString());
					if (posArray[i] == null) {
						throw new CacException("Could not parse input array to get " + desiredNumInputArgs
								+ " arguments");
					}
				}
			}

		}
		return posArray;
	}

	static void printTerm(String toPrint) {
		JythonServerFacade.getInstance().print(toPrint);

		// Delay so they come out in right order!!!
		try {
			Thread.sleep(100);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
	}
}
