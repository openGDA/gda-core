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

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 */
public abstract class CollisionCheckerBase implements CollisionChecker {
	private static final Logger logger = LoggerFactory.getLogger(CollisionCheckerBase.class);

	int nParam;

	Double[] currentRangeStart;
	Double[] currentRangeEnd;
	Double[] requestedConfiguration;
	Double[] sampleSize = null;
	String[] paramNames; // List of names corresponding to each parameter in the
	// above arrays
	// These will be set by the CAC controller when
	/**
	 * The name of this Scannable
	 */
	protected String name = "";

	@Override
	public String[] checkMove(final Double[] _currentRangeStart, final Double[] _currentRangeEnd,
			final Double[] _requestedConfiguration) throws CacException {
		Double[] testRangeStart;
		Double[] testRangeEnd;

		nParam = _requestedConfiguration.length;

		// Check arrays are all the same length
		if ((_currentRangeStart.length != nParam) || (_currentRangeEnd.length != nParam)) {
			// TODO should this just throw a CacException?
			throw new RuntimeException("All input arrays must be same length (except sampleSize which may null");
		}

		// Set this CollisionChecker's fields
		// TODO consider if cloning is needed (performance issue)
		currentRangeStart = _currentRangeStart.clone();
		currentRangeEnd = _currentRangeEnd.clone();
		requestedConfiguration = _requestedConfiguration.clone();

		// Produce a lowTestRange and highTestRange array describing the endpoints
		// of each dimension of the field to test over (just as the
		// currentRangeStart and currentRangeEnd arrays describe the field of
		// possible current locations.

		// (We can assume that requested position for a parameter is give (i.e.
		// not NaN), then that parameter is not currently moving and it will not
		// allready have a end of range set.

		testRangeStart = currentRangeStart; // These will not change
		testRangeEnd = currentRangeEnd; // These will expand
		for (int i = 0; i < nParam; i++)
			if (requestedConfiguration[i] != null) {
				testRangeEnd[i] = requestedConfiguration[i];
			}

		// call the relevant apply function
		if (sampleSize == null) {
			return applyTestAtEndpoints(testRangeStart, testRangeEnd, 0);
		}
		// else sampleSizeArray has been specified
		return applyTestOverRange(testRangeStart, testRangeEnd, 0);
	}

	/*
	 * @param startRange If a parameter is to be varied this contains its start value, if it is fixed this contains its
	 * value @param endRange If a parameter is to be varied this contains its end value, if it is fixed this contains
	 * null (or None from Jython). startI is needed to tell this call to the function how nested its call is (normally a
	 * recursive function might be passed a shorter list of arguments at each call, but this is not possible here).
	 */
	private String[] applyTestAtEndpoints(final Double[] rangeStart, final Double[] rangeEnd, final int startI)
			throws CacException {
		Double[] rangeStartSlice;
		Double[] rangeEndSlice;
		String[] result;

		logger.debug(name + ".applyTestAtEndpoints(" + ArrayToString.get(rangeStart) + ", "
				+ ArrayToString.get(rangeEnd) + ", " + startI + ") entered");
		// Move through range arrays from startI onwards.
		for (int i = startI; i < nParam; i++) {
			// if the current parameter (i) is to be varied, vary it and make a
			// recursive call at each point. Otherwise move on.
			if (rangeEnd[i] != null) {
				// Fix this parameter ...
				rangeEndSlice = rangeEnd.clone();
				// Double thisTestEnd = rangeEndSlice[i];
				rangeEndSlice[i] = null;

				// ... to the start of its range
				rangeStartSlice = rangeStart.clone();
				result = applyTestAtEndpoints(rangeStartSlice, rangeEndSlice, i + 1);

				if (result.length > 0)
					return result;

				// ... and to the end of it
				rangeStartSlice = rangeStart.clone();
				rangeStartSlice[i] = rangeEnd[i];
				result = applyTestAtEndpoints(rangeStartSlice, rangeEndSlice, i + 1);
				if (result.length > 0)
					return result;
				// else this tree has been searched as is okay!
				return new String[0];
			}
		}

		// else End of recursion! Test this point.
		logger.debug("CollisionCheckerBase.applyTestAtEndpoints() calling isConfigurationPermitted("
				+ ArrayToString.get(rangeStart) + ")");
		return checkConfigurationPermitted(rangeStart);

	}

	/**
	 * @param rangeStart
	 * @param rangeEnd
	 * @param startI
	 * @return gh
	 * @throws CacException
	 */
	private String[] applyTestOverRange(final Double[] rangeStart, final Double[] rangeEnd, final int startI)
			throws CacException {
		Double[] rangeStartSlice;
		Double[] rangeEndSlice;
		String[] result;

		logger.debug(name + ".applyTestOverRange(" + ArrayToString.get(rangeStart) + ", " + ArrayToString.get(rangeEnd)
				+ ", " + startI + ") entered");
		// Move through range arrays
		for (int i = startI; i < nParam; i++) {
			// if the current parameter is to be varied, vary it and make a
			// recursive
			// call at each point. Otherwise move on.
			if (rangeEnd[i] != null) {
				// Fix this parameter ...
				rangeEndSlice = rangeEnd.clone();
				// Double thisTestEnd = rangeEndSlice[i];
				rangeEndSlice[i] = null; // This will not be varied (as the ith parameter is fixed)

				// 
				double startVal; // used later
				for (startVal = rangeStart[i]; startVal <= rangeEnd[i]; startVal += sampleSize[i]) {
					rangeStartSlice = rangeStart.clone();
					rangeStartSlice[i] = startVal;
					result = applyTestAtEndpoints(rangeStartSlice, rangeEndSlice, i + 1);

					if (result.length > 0)
						return result;
				}

				// ... and to the end of the range if the last point of the grid scan didn't
				// quite hit the end of the range.
				if (startVal < rangeEnd[i]) {
					rangeStartSlice = rangeStart.clone();
					rangeStartSlice[i] = rangeEnd[i];
					result = applyTestAtEndpoints(rangeStartSlice, rangeEndSlice, i + 1);
					if (result.length > 0)
						return result;
				}

				// else this tree has been searched as is okay!
				return new String[0];
			}
		}

		// else End of recursion! Test this point.
		logger.debug("CollisionCheckerBase.applyTestAtEndpoints() calling isConfigurationPermitted("
				+ ArrayToString.get(rangeStart) + ")");
		return checkConfigurationPermitted(rangeStart);

	}

	@Override
	public String getName() {
		return name;
	}

	/**
	 * @return the sample size
	 */
	public Double[] getSampleSize() {
		return sampleSize;
	}

	/**
	 * @param sampleSize
	 * @throws CacException
	 */
	public void setSampleSize(Double[] sampleSize) throws CacException {
		// check sampleSize array given is the right length (nParam may not be set yet)
		if (sampleSize.length != paramNames.length) {

			throw new CacException("Sample size not set. Requires " + nParam + "parameters");
		}
		this.sampleSize = sampleSize;
	}

	/**
	 * @return list of parameter names
	 */
	public String[] getParamNames() {
		return paramNames;
	}

	/**
	 * @param paramNames
	 *            the partameter names
	 */
	public void setParamNames(final String[] paramNames) {
		this.paramNames = paramNames;
		this.nParam = paramNames.length;
	}

	@Override
	public void setName(String newName) {
		this.name = newName;
	}

	// public void display()
	// {
	// // Display name
	// String underline = "";
	// for (int i = 0; i < name.length(); i++)
	// underline = underline + "~";
	// printTerm("~~~~~~~~~" + underline);
	// printTerm("Checker: " + name);
	// printTerm("~~~~~~~~~" + underline);
	//
	// // Display parameters
	// printTerm(" Current range start: "
	// + ArrayToString.get(currentRangeStart));
	// printTerm(" Current range end: " + ArrayToString.get(currentRangeEnd));
	// printTerm(" Desired position: "
	// + ArrayToString.get(requestedConfiguration));
	//
	// if (sampleSize != null)
	// {
	// printTerm("Sample size: " + ArrayToString.get(sampleSize));
	// }
	// }

	/**
	 * @param position
	 * @return null if okay, else string with reason.
	 */
	public abstract String[] checkConfigurationPermitted(Double[] position);

	@Override
	public abstract String toString();

}
