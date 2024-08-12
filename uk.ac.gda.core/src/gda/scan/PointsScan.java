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

package gda.scan;

import java.util.ArrayList;
import java.util.List;

import com.google.common.primitives.Doubles;

import gda.device.Scannable;

/**
 * Wrapper for the ConcurrentScan, except it takes start, stop ,number of points as arguments.
 */
public class PointsScan extends PassthroughScanAdapter {


	/**
	 * Expect arguments in the format:
	 * <p>
	 * scannbleObject1 start stop [number points] scannbleObject2 start [stop] scannbleObject3 start [stop]
	 * <P>
	 * All scannables are assumed to use the same number of steps.
	 *
	 * @param args
	 *            Object[]
	 * @throws IllegalArgumentException
	 */
	public PointsScan(Object[] args) throws IllegalArgumentException {
		// work through the commands and translate into start stop step
		// then go through each in turn to work out what are args and what are
		// Scannables

			// create the internal ConcurrentScan object using the converted list of arguments
			super(new ConcurrentScan(changeArgsToConcurrentScanArgs(args).toArray()));
	}

	static List<Object> changeArgsToConcurrentScanArgs(Object[] args) {
		final List<Object> newArgs = new ArrayList<>();
		double firstNumberSteps = -1;
		try {
			for (int i = 0; i < args.length;) {
				if (args[i] instanceof Scannable
						&& (i == args.length - 1 || Doubles.tryParse(args[i+1].toString()) != null )) {
					// use the first scannable to define the number of
					// points (and therefore the step size)
					if (i == 0) {
						double start = Double.parseDouble(args[i + 1].toString());
						double stop = Double.parseDouble(args[i + 2].toString());
						firstNumberSteps = Double.parseDouble(args[i + 3].toString());

						double step = Math.abs(start - stop) / (firstNumberSteps - 1);

						newArgs.add(args[i]);
						newArgs.add(Double.toString(start));
						newArgs.add(Double.toString(stop));
						newArgs.add(Double.toString(step));
						i = 4;
					}
					// check to see if there are two more args
					else if (args.length > i + 2) {
						// is the arg two ahead a scannable? therefore only the start defined
						if ((args[i + 2] instanceof Scannable)) {
							// this must not be the first scannable else the number of steps cannot be established.
							if (firstNumberSteps == -1) {
								throw new IllegalArgumentException("Wrong type or number of arguments");
							}
							newArgs.add(args[i]);
							newArgs.add(args[i + 1]);
							i += 2;
						}
						// else next two agruments must be related to this scannable
						else {
							newArgs.add(args[i]);		//Scannable
							double start = Double.parseDouble(args[i + 1].toString());
							double stop = Double.parseDouble(args[i + 2].toString());
							double step = Math.abs(start - stop) / (firstNumberSteps - 1);
							newArgs.add(start);
							newArgs.add(step);
							i += 3;
						}
					}
					// if there is at least one more arg then it must be a
					// start value (i.e. constant over the scan)
					else if (args.length > i + 1) {
						newArgs.add(args[i]);
						newArgs.add(args[i + 1]);
						i += 2;
					} else {
						newArgs.add(args[i]);
						i++;
					}
				} else {
					newArgs.add(args[i]);
					i++;
				}
			}
		} catch (Exception ex) {
			throw new IllegalArgumentException(
					"pscan usage: scannableName start stop no_points [scannablename2] start [stop]");
		}
		return newArgs;
	}
	/**
	 * Creates and runs a scan.
	 *
	 * @param args
	 *            String[]
	 * @throws InterruptedException
	 * @throws Exception
	 */
	public static void runScan(Object[] args) throws InterruptedException, Exception {
		PointsScan thisScan = new PointsScan(args);
		thisScan.runScan();
	}

}
