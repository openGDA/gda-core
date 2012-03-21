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

import uk.ac.gda.richbeans.components.scalebox.ScaleBox;
import uk.ac.gda.richbeans.components.wrappers.TextWrapper;

/**
 * @author fcp94556
 *
 */
public class CustomXYZParameterComposite extends Composite {

	private TextWrapper deviceName;
	private ScaleBox value;
	private ScaleBox z;
	private ScaleBox y;
	private ScaleBox x;
	/**
	 * @param parent
	 * @param style
	 */
	public CustomXYZParameterComposite(Composite parent, int style) {
		super(parent, style);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		setLayout(gridLayout);
		setLayoutData(new GridData(SWT.FILL, SWT.CENTER, false, false));

		final Label deviceNameLabel = new Label(this, SWT.NONE);
		deviceNameLabel.setText("Device Name");

		deviceName = new TextWrapper(this, SWT.BORDER);
		final GridData gd_deviceName = new GridData(SWT.FILL, SWT.CENTER, true, false);
		deviceName.setLayoutData(gd_deviceName);

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
		z.setMinimum(-15.0);
		z.setMaximum(15);
		final GridData gd_z = new GridData(SWT.FILL, SWT.CENTER, true, false);
		z.setLayoutData(gd_z);
		z.setUnit("mm");

		final Label valueLabel = new Label(this, SWT.NONE);
		valueLabel.setText("Value");

		value = new ScaleBox(this, SWT.NONE);
		final GridData gd_value = new GridData(SWT.FILL, SWT.CENTER, true, false);
		value.setLayoutData(gd_value);
	}
	/**
	 * @return ScaleBox
	 */
	public ScaleBox getValue() {
		return value;
	}
	/**
	 * @return TextWrapper
	 */
	public TextWrapper getDeviceName() {
		return deviceName;
	}
	/**
	 * @return ScaleBox
	 */
	public ScaleBox getX() {
		return x;
	}
	/**
	 * @return ScaleBox
	 */
	public ScaleBox getY() {
		return y;
	}
	/**
	 * @return ScaleBox
	 */
	public ScaleBox getZ() {
		return z;
	}

}

	