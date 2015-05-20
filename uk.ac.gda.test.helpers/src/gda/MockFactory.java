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

package gda;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import gda.device.DeviceException;
import gda.device.Scannable;
import gda.device.ScannableMotion;
import gda.device.ScannableMotionUnits;
import gda.device.scannable.ScannableBase;

/**
 * Returns mock classes built using the Mockito testing framework. Can be used with Mockito or not. None of these Mock
 * objects hold internal state. See gda.scan.ConcurrentScanTest for an example of how to use the Mockito framework
 */
public class MockFactory {

	// Scannable

	public static Scannable createMockScannable(String name) throws DeviceException {
		return createMockScannable(name, 5);
	}

	public static Scannable createMockScannable(String name, int level) throws DeviceException {
		// if only one inputname, then set it to the Scannable name
		return createMockScannable(name, new String[] { name }, new String[] {}, new String[] { "%1.0f" }, level, 0.);
	}

	public static Scannable createMockScannable(String name, String[] inputNames, String[] extraNames,
			String[] outputFormat, int level, Object position) throws DeviceException {
		return createMockScannable(Scannable.class, name, inputNames, extraNames, outputFormat, level, position);
	}

	// ScannableMotion

	public static ScannableMotion createMockScannableMotion(String name) throws DeviceException {
		return createMockScannableMotion(name, 5);
	}

	public static ScannableMotion createMockScannableMotion(String name, int level) throws DeviceException {
		// if only one inputname, then set it to the Scannable name
		return createMockScannableMotion(name, new String[] { name }, new String[] {}, new String[] { "%1.0f" }, level,
				0.);
	}

	public static ScannableMotion createMockScannableMotion(String name, String[] inputNames, String[] extraNames,
			String[] outputFormat, int level, Object position) throws DeviceException {
		ScannableMotion scn = (ScannableMotion) createMockScannable(ScannableMotion.class, name, inputNames,
				extraNames, outputFormat, level, position);
		return scn;
	}

	// ScannableMotionUnits

	public static ScannableMotionUnits createMockScannableMotionUnits(String name) throws DeviceException {
		return createMockScannableMotionUnits(name, 5);
	}

	public static ScannableMotionUnits createMockScannableMotionUnits(String name, int level) throws DeviceException {
		// if only one inputname, then set it to the Scannable name
		return createMockScannableMotionUnits(name, new String[] { name }, new String[] {}, new String[] { "%1.0f" },
				level, 0.);
	}

	public static ScannableMotionUnits createMockScannableMotionUnits(String name, String[] inputNames,
			String[] extraNames, String[] outputFormat, int level, Object position) throws DeviceException {
		ScannableMotionUnits scn = (ScannableMotionUnits) createMockScannable(ScannableMotionUnits.class, name,
				inputNames, extraNames, outputFormat, level, position);
		return scn;
	}

	// Generic

	@SuppressWarnings("unchecked")
	public static <S extends Scannable> S createMockScannable(Class<? extends Scannable> clazz, String name,
			String[] inputNames, String[] extraNames, String[] outputFormat, int level, Object position)
			throws DeviceException {

		Scannable scn = mock(clazz, name);
		when(scn.getName()).thenReturn(name);
		when(scn.getInputNames()).thenReturn(inputNames);
		when(scn.getExtraNames()).thenReturn(extraNames);
		when(scn.getOutputFormat()).thenReturn(outputFormat);
		when(scn.getLevel()).thenReturn(level);
		when(scn.getPosition()).thenReturn(position);
		when(scn.isBusy()).thenReturn(true);
		// when(scn.checkPositionValid(anyObject()) == null).thenReturn(true);
		when(scn.toFormattedString()).thenReturn(name + " : " + position);
		return (S) scn;
	}

	public static Scannable createMockZieScannable(String name, int level) throws DeviceException {
		Scannable zie = mock(ScannableBase.class, name);
		when(zie.getName()).thenReturn(name);
		when(zie.getInputNames()).thenReturn(new String[] {});
		when(zie.getExtraNames()).thenReturn(new String[] {});
		when(zie.getOutputFormat()).thenReturn(new String[] {});
		when(zie.getLevel()).thenReturn(level);
		when(zie.getPosition()).thenReturn(null);
		when(zie.isBusy()).thenReturn(false);
		// when(zie.checkPositionValid(anyObject()) == null).thenReturn(true);
		when(zie.toFormattedString()).thenReturn(name);
		return zie;
	}

}
