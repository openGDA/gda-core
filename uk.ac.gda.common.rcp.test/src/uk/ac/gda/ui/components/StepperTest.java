/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.ui.components;

import junit.framework.Assert;

import org.eclipse.draw2d.GridData;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Image;
import org.eclipse.swt.graphics.ImageData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Shell;
import org.junit.Ignore;
import org.junit.Test;

/**
 *
 */
public class StepperTest {

	@Ignore("2016-03-04 This is not really a test so shouldn't be included in automated test runs")
	@Test
	public final void testStepper() {
		Display display = new Display();
		Shell shell = new Shell(display);

		shell.setLayout(new GridLayout());

		Stepper stepperScaler = new Stepper(shell, SWT.BORDER,
				new Image(display, new ImageData("icons/sliderIcon.gif")));
		org.eclipse.swt.layout.GridData layoutData = new org.eclipse.swt.layout.GridData(GridData.FILL_HORIZONTAL);
		layoutData.heightHint = 50;
		stepperScaler.setLayoutData(layoutData);
		int n = 100000;
		stepperScaler.setSteps(n);
		shell.open();

		// stepperScaler.setSelection(n - 1);
		//
		// Assert.assertEquals(n - 1, stepperScaler.getSelection());
		//
		// stepperScaler.setSelection(n + 500);
		//
		// Assert.assertEquals(n - 1, stepperScaler.getSelection());

		stepperScaler.setSelection(0);

		Assert.assertEquals(0, stepperScaler.getSelection());

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}

	@Ignore("2016-03-04 This is not really a test so shouldn't be included in automated test runs")
	@Test
	public final void testStepperWithLabels() {
		Display display = new Display();
		Shell shell = new Shell(display);

		shell.setLayout(new GridLayout());

		Stepper stepperScaler = new Stepper(shell, SWT.BORDER,
				new Image(display, new ImageData("icons/sliderIcon.gif")));
		org.eclipse.swt.layout.GridData layoutData = new org.eclipse.swt.layout.GridData(GridData.FILL_HORIZONTAL);
		layoutData.heightHint = 50;
		stepperScaler.setLayoutData(layoutData);
		int n = 100;

		double[] indexVals = new double[100];
		for (int i = 0; i < 100; i++) {
			indexVals[i] = (i * 3) - 89.3;
		}

		stepperScaler.setSteps(n, indexVals);
		shell.open();

		stepperScaler.setSelection(0);

		Assert.assertEquals(0, stepperScaler.getSelection());

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch())
				display.sleep();
		}
		display.dispose();
	}
}
