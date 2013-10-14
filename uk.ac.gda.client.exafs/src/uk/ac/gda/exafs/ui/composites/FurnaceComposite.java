/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.composites;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.gda.richbeans.components.FieldBeanComposite;
import uk.ac.gda.richbeans.components.scalebox.NumberBox;
import uk.ac.gda.richbeans.components.scalebox.ScaleBox;

/**
 * @author fcp94556
 *
 */
public class FurnaceComposite extends FieldBeanComposite {

	private ScaleBox x,y,z;
	private ScaleBox temperature;
	private ScaleBox tolerance;
	private ScaleBox time;

	/**
	 * @param parent
	 * @param style
	 */
	public FurnaceComposite(Composite parent, int style) {
		super(parent, style);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		setLayout(gridLayout);

		final Label xLabel = new Label(this, SWT.NONE);
		xLabel.setText("x");

		x = new ScaleBox(this, SWT.NONE);
		x.setMinimum(-15);
		x.setMaximum(15);
		x.setUnit("mm");
		final GridData gd_x = new GridData(SWT.FILL, SWT.CENTER, true, false);
		x.setLayoutData(gd_x);

		final Label yLabel = new Label(this, SWT.NONE);
		yLabel.setText("y");

		y = new ScaleBox(this, SWT.NONE);
		y.setMinimum(-20.0);
		y.setMaximum(20.0);
		final GridData gd_y = new GridData(SWT.FILL, SWT.CENTER, true, false);
		y.setLayoutData(gd_y);
		y.setUnit("mm");

		final Label zLabel = new Label(this, SWT.NONE);
		zLabel.setText("z");

		z = new ScaleBox(this, SWT.NONE);
		z.setMinimum(-15);
		z.setMaximum(15);
		final GridData gd_z = new GridData(SWT.FILL, SWT.CENTER, true, false);
		z.setLayoutData(gd_z);
		z.setUnit("mm");
		
		final Label temperatureLabel = new Label(this, SWT.NONE);
		temperatureLabel.setText("Temperature");

		temperature = new ScaleBox(this, SWT.NONE);
		temperature.setMinimum(295);
		temperature.setMaximum(1300);
		temperature.setUnit("K");
		final GridData gd_temperature = new GridData(SWT.FILL, SWT.CENTER, true, false);
		temperature.setLayoutData(gd_temperature);

		final Label toleranceLabel = new Label(this, SWT.NONE);
		toleranceLabel.setText("Tolerance");

		tolerance = new ScaleBox(this, SWT.NONE);
		final GridData gd_tolerance = new GridData(SWT.FILL, SWT.CENTER, true, false);
		tolerance.setLayoutData(gd_tolerance);
		tolerance.setMaximum(5);

		final Label timeLabel = new Label(this, SWT.NONE);
		timeLabel.setText("Time");

		time = new ScaleBox(this, SWT.NONE);
		final GridData gd_time = new GridData(SWT.FILL, SWT.CENTER, true, false);
		time.setLayoutData(gd_time);
		time.setUnit("s");
		time.setMaximum(400.0);

	}
	/**
	 * @return ScaleBox
	 */ 
	public NumberBox getX() {
		return x;
	}
	/**
	 * @return ScaleBox
	 */ 
	public NumberBox getY() {
		return y;
	}
	/**
	 * @return ScaleBox
	 */ 
	public NumberBox getZ() {
		return z;
	}
	/**
	 * @return ScaleBox
	 */ 
	public NumberBox getTemperature() {
		return temperature;
	}
	/**
	 * @return ScaleBox
	 */ 
	public ScaleBox getTolerance() {
		return tolerance;
	}
	/**
	 * @return ScaleBox
	 */ 
	public ScaleBox getTime() {
		return time;
	}

}

	