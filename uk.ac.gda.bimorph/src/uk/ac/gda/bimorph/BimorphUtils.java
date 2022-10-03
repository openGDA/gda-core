/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.gda.bimorph;

import static java.util.stream.IntStream.range;
import static java.util.stream.IntStream.rangeClosed;

import gda.device.DeviceException;
import gda.scan.ScanPositionProvider;

public class BimorphUtils {
	public static ScanPositionProvider positions(double[] startingPositions, double increment) {
		return new BimorphPositionProvider(startingPositions, increment);
	}

	public static ScanPositionProvider positions(BimorphMirrorScannable bimorph, double inc) throws DeviceException {
		return positions((double[])bimorph.getPosition(), inc);
	}

	public static class BimorphPositionProvider implements ScanPositionProvider {

		private final double[] startingPosition;
		private final double[/*step*/][/*voltages*/] voltages;

		public BimorphPositionProvider(double[] initial, double inc) {
			startingPosition = initial;
			voltages = rangeClosed(0,initial.length)
					.mapToObj(index -> {
						double[] newVoltages = initial.clone();
						range(0, index).forEach(i -> newVoltages[i] += inc);
						return newVoltages;
					})
					.toArray(double[][]::new);
		}

		@Override
		public double[] get(int index) {
			return voltages[index];
		}

		@Override
		public int size() {
			return startingPosition.length + 1;
		}
	}
}
