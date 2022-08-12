/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package gda.function;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;
import static tec.units.indriya.unit.MetricPrefix.MICRO;
import static tec.units.indriya.unit.MetricPrefix.MILLI;
import static tec.units.indriya.unit.Units.METRE;
import static tec.units.indriya.unit.Units.RADIAN;

import java.util.function.Function;

import javax.measure.Quantity;
import javax.measure.quantity.Angle;
import javax.measure.quantity.Length;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import gda.device.DeviceException;
import gda.device.ScannableMotionUnits;
import gda.factory.FactoryException;
import tec.units.indriya.quantity.Quantities;

public class RelativeMotionFunctionTest {
	// Allow for inaccuracy in floating point values
	private static final double FP_TOLERANCE = 1e-12;

	private static final double INITIAL_MM_POSITION = 10.0;
	private static final double INITIAL_UM_POSITION = 1200.0;
	private static final double INITIAL_RAD_POSITION = 15.8;

	private RelativeMotionFunction<Length, Length> lengthToLengthFunction;
	private RelativeMotionFunction<Length, Angle> lengthToAngleFunction;

	private ScannableMotionUnits mmScannable;
	private ScannableMotionUnits umScannable;
	private ScannableMotionUnits radScannable;

	private ILinearFunction<Length, Length> lengthToLengthCoupling;
	private ILinearFunction<Length, Angle> lengthToAngleCoupling;

	@BeforeEach
	public void setUp() throws DeviceException {
		lengthToLengthFunction = new RelativeMotionFunction<>();
		lengthToAngleFunction = new RelativeMotionFunction<>();

		mmScannable = mock(ScannableMotionUnits.class);
		when(mmScannable.getPosition()).thenReturn(INITIAL_MM_POSITION);
		when(mmScannable.getHardwareUnitString()).thenReturn("mm");

		umScannable = mock(ScannableMotionUnits.class);
		when(umScannable.getPosition()).thenReturn(INITIAL_UM_POSITION);
		when(umScannable.getHardwareUnitString()).thenReturn("um");

		radScannable = mock(ScannableMotionUnits.class);
		when(radScannable.getPosition()).thenReturn(INITIAL_RAD_POSITION);
		when(radScannable.getHardwareUnitString()).thenReturn("rad");

		lengthToLengthCoupling = new LinearFunction<>();
		lengthToAngleCoupling = new LinearFunction<>();
	}

	@SuppressWarnings("unchecked")
	@Test(expected = FactoryException.class)
	public void testMissingPrimaryScannable() throws FactoryException {
		lengthToLengthFunction.setSecondaryScannable(umScannable);
		lengthToLengthFunction.setCouplingFunction(mock(Function.class));
		lengthToLengthFunction.configure();
	}

	@SuppressWarnings("unchecked")
	@Test(expected = FactoryException.class)
	public void testMissingSecondaryScannable() throws FactoryException {
		lengthToLengthFunction.setPrimaryScannable(mmScannable);
		lengthToLengthFunction.setCouplingFunction(mock(Function.class));
		lengthToLengthFunction.configure();
	}

	@Test(expected = FactoryException.class)
	public void testMissingCouplingFunction() throws FactoryException {
		lengthToLengthFunction.setPrimaryScannable(mmScannable);
		lengthToLengthFunction.setSecondaryScannable(umScannable);
		lengthToLengthFunction.configure();
	}

	@Test
	public void testContraryMotion() throws FactoryException {
		// Secondary scannable should move in the equal & opposite direction to the primary
		final double scalingFactor = -1;
		lengthToLengthCoupling.setSlopeDividend(Quantities.getQuantity(scalingFactor,  MILLI(METRE)));
		lengthToLengthCoupling.setSlopeDivisor(Quantities.getQuantity(1,  MILLI(METRE)));
		lengthToLengthCoupling.setInterception(Quantities.getQuantity(0, MILLI(METRE)));

		lengthToLengthFunction.setPrimaryScannable(mmScannable);
		lengthToLengthFunction.setSecondaryScannable(umScannable);
		lengthToLengthFunction.setCouplingFunction(lengthToLengthCoupling);
		lengthToLengthFunction.configure();

		final double primaryMove = 0.4;
		final double primaryTargetPosition = INITIAL_MM_POSITION + primaryMove;
		final double expectedSecondaryPosition = INITIAL_UM_POSITION + (primaryMove * scalingFactor * 1000);

		final Quantity<Length> targetPosition = lengthToLengthFunction.apply(Quantities.getQuantity(primaryTargetPosition, MILLI(METRE)));
		assertEquals(expectedSecondaryPosition, targetPosition.getValue().doubleValue(), FP_TOLERANCE);
		assertEquals(MICRO(METRE), targetPosition.getUnit());
	}

	@Test
	public void testLengthToAngleCoupling() throws FactoryException {
		final double scalingFactor = 5;
		lengthToAngleCoupling.setSlopeDividend(Quantities.getQuantity(scalingFactor, RADIAN));
		lengthToAngleCoupling.setSlopeDivisor(Quantities.getQuantity(1,  MILLI(METRE)));
		lengthToAngleCoupling.setInterception(Quantities.getQuantity(0, RADIAN));

		lengthToAngleFunction.setPrimaryScannable(mmScannable);
		lengthToAngleFunction.setSecondaryScannable(radScannable);
		lengthToAngleFunction.setCouplingFunction(lengthToAngleCoupling);
		lengthToAngleFunction.configure();

		final double primaryMove = 0.4;
		final double primaryTargetPosition = INITIAL_MM_POSITION + primaryMove;
		final double expectedSecondaryPosition = INITIAL_RAD_POSITION + (primaryMove * scalingFactor);

		final Quantity<Angle> targetPosition = lengthToAngleFunction.apply(Quantities.getQuantity(primaryTargetPosition, MILLI(METRE)));
		assertEquals(expectedSecondaryPosition, targetPosition.getValue().doubleValue(), FP_TOLERANCE);
		assertEquals(RADIAN, targetPosition.getUnit());
	}
}
