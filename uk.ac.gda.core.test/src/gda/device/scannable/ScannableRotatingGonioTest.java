/*-
 * Copyright © 2012 Diamond Light Source Ltd.
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

package gda.device.scannable;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.anyObject;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.when;
import gda.device.DeviceException;
import gda.device.ScannableMotionUnits;
import gda.factory.FactoryException;

import org.junit.Test;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;

public class ScannableRotatingGonioTest {

	private Double xVal = 0.0;
	private Double yVal = 0.0;
	private Double rotVal = 0.;

	@SuppressWarnings("rawtypes")
	@Test
	public void testGoni0() throws DeviceException, FactoryException {

		ScannableMotionUnits xScannableMotor = mock(ScannableMotionUnits.class);
		when(xScannableMotor.getExtraNames()).thenReturn(new String[0]);
		when(xScannableMotor.getInputNames()).thenReturn(new String[] { "x" });
		when(xScannableMotor.getUserUnits()).thenReturn("mm");

		doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				xVal = (Double) args[0];
				return null;
			}
		}).when(xScannableMotor).asynchronousMoveTo(anyObject());

		doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) {
				return xVal;
			}
		}).when(xScannableMotor).getPosition();

		ScannableMotionUnits yScannableMotor = mock(ScannableMotionUnits.class);
		when(yScannableMotor.getExtraNames()).thenReturn(new String[0]);
		when(yScannableMotor.getInputNames()).thenReturn(new String[] { "y" });
		when(yScannableMotor.getUserUnits()).thenReturn("mm");

		doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				yVal = (Double) args[0];
				return null;
			}
		}).when(yScannableMotor).asynchronousMoveTo(anyObject());
		doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) {
				return yVal;
			}
		}).when(yScannableMotor).getPosition();

		ScannableMotionUnits rotScannableMotor = mock(ScannableMotionUnits.class);
		when(rotScannableMotor.getExtraNames()).thenReturn(new String[0]);
		when(rotScannableMotor.getInputNames()).thenReturn(new String[] { "rot" });

		doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) {
				Object[] args = invocation.getArguments();
				rotVal = (Double) args[0];
				return null;
			}
		}).when(rotScannableMotor).asynchronousMoveTo(anyObject());
		doAnswer(new Answer() {
			@Override
			public Object answer(InvocationOnMock invocation) {
				return rotVal;
			}
		}).when(rotScannableMotor).getPosition();

		ScannableRotatingGonio x_gonio = new ScannableRotatingGonio();
		x_gonio = spy(x_gonio);
		x_gonio.setxScannableMotor(xScannableMotor);
		x_gonio.setyScannableMotor(yScannableMotor);
		x_gonio.setRotScannableMotor(rotScannableMotor);
		x_gonio.setInputNames(new String[] { "x_gonio" });
		x_gonio.configure();

		ScannableRotatingGonio y_gonio = new ScannableRotatingGonio();
		y_gonio = spy(y_gonio);
		y_gonio.setxScannableMotor(xScannableMotor);
		y_gonio.setyScannableMotor(yScannableMotor);
		y_gonio.setRotScannableMotor(rotScannableMotor);
		y_gonio.setInputNames(new String[] { "y_gonio" });
		y_gonio.setReportX(false);
		y_gonio.configure();

		/*
		 * move x to 1 , y=0, rot=0.
		 */
		xScannableMotor.asynchronousMoveTo(1.0);
		yScannableMotor.asynchronousMoveTo(0.0);
		rotScannableMotor.asynchronousMoveTo(0.0);

		assertEquals(1.0, (Double) x_gonio.getPosition(), .1);
		assertEquals(0.0, (Double) y_gonio.getPosition(), .1);

		/*
		 * rotate to 90
		 */
		rotScannableMotor.asynchronousMoveTo(90.0);
		assertEquals(0., (Double) x_gonio.getPosition(), .1);
		assertEquals(1.0, (Double) y_gonio.getPosition(), .1);

		/*
		 * move y to 1.0
		 */
		yScannableMotor.asynchronousMoveTo(1.0);
		assertEquals(-1., (Double) x_gonio.getPosition(), .1);
		assertEquals(1.0, (Double) y_gonio.getPosition(), .1);

		/*
		 * rotate to 30
		 */
		rotScannableMotor.asynchronousMoveTo(30.0);
		assertEquals(.366, (Double) x_gonio.getPosition(), .1);
		assertEquals(1.366, (Double) y_gonio.getPosition(), .1);

		/*
		 * now use gonio to move to 1.0
		 */
		x_gonio.moveTo(1.0);
		assertEquals(1.549, (Double) xScannableMotor.getPosition(), .1);
		assertEquals(0.683, (Double) yScannableMotor.getPosition(), .1);
		assertEquals(1.0, (Double) x_gonio.getPosition(), .1);
		assertEquals(1.366, (Double) y_gonio.getPosition(), .1);

		y_gonio.moveTo(1.0);
		assertEquals(1.366, (Double) xScannableMotor.getPosition(), .1);
		assertEquals(0.366, (Double) yScannableMotor.getPosition(), .1);
		assertEquals(1.0, (Double) y_gonio.getPosition(), .1);
		assertEquals(1.0, (Double) x_gonio.getPosition(), .1);
	}
}
