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

import java.util.Vector;

import gda.device.Scannable;

/**
 * Wrapper for the ConcurrentScan, except it takes start,step,number of points as arguments.
 */
public class PointsScan extends PassthroughScanAdapter {


	/**
	 * Expect arguments in the format:
	 * <p>
	 * scannbleObject1 start step [number points] scannbleObject2 start [step] scannbleObject3 start [step]
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

			// create the internal ConcurrentScan object using the converted
			// list
			// of arguments
			super(new ConcurrentScan(changeArgsToConcurrentScanArgs(args).toArray()));
	}

	static Vector<Object> changeArgsToConcurrentScanArgs(Object[] args) {
		Vector<Object> newArgs = new Vector<Object>();
		double firstNumberSteps = -1;
		try {
			for (int i = 0; i < args.length - 1;) {
				if (args[i] instanceof Scannable) {
					// use the first scannable to define the number of
					// points
					// (and therefor the stop position)
					if (i == 0) {
						double start = Double.valueOf(args[i + 1].toString()).doubleValue();
						double step = Double.valueOf(args[i + 2].toString()).doubleValue();
						double numberSteps = Double.valueOf(args[i + 3].toString()).doubleValue();

						double stop = start + ((step * numberSteps) - 1);

						newArgs.add(args[i]);
						newArgs.add(Double.toString(start));
						newArgs.add(Double.toString(stop));
						newArgs.add(Double.toString(step));
						i = 4;
					}
					// check to see if there are two more args
					else if (args.length > i + 2) {
						// is the arg two ahead a scannable?
						// therefore only the start defined
						if ((args[i + 2] instanceof Scannable)) {
							// this must not be the first scannable
							// else the number of steps cannot be
							// established.
							if (firstNumberSteps == -1) {
								throw new IllegalArgumentException("Wrong type or number of arguments");
							}
							newArgs.add(args[i]);
							newArgs.add(args[i + 1]);
							i += 2;
						}
						// else next two agruments must be related to this
						// scannable
						else {
							newArgs.add(args[i]);
							newArgs.add(args[i + 1]);
							newArgs.add(args[i + 2]);
							i += 3;
						}
					}
					// if there is at least one more arg then it must be a
					// start
					// value
					else if (args.length > i + 1) {
						newArgs.add(args[i]);
						newArgs.add(args[i + 1]);
						i += 2;
					} else {
						i++;
					}
				} else {
					i++;
				}
			}
		} catch (Exception ex) {
			throw new IllegalArgumentException(
					"pscan usage: scannableName start step no_points [scannablename2] start [step]");
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
