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
import org.eclipse.swt.widgets.Link;

import uk.ac.gda.exafs.util.ScannableValueListener;
import uk.ac.gda.richbeans.components.FieldBeanComposite;
import uk.ac.gda.richbeans.components.scalebox.RangeBox;

/**
 * @author fcp94556
 *
 */
public class RoomTemperatureComposite extends FieldBeanComposite {

	private RangeBox yaw;
	private RangeBox roll;
	private RangeBox rotation;
	private RangeBox z;
	private RangeBox y;
	private RangeBox x;
	/**
	 * @param parent
	 * @param style
	 */
	public RoomTemperatureComposite(Composite parent, int style) {
		super(parent, style);
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		setLayout(gridLayout);

		final Link xLabel = new Link(this, SWT.NONE);
		xLabel.setText("<a>x</a>");

		x = new RangeBox(this, SWT.NONE);
		x.setMinimum(-15.3);
		x.setMaximum(14.1);
		x.setDecimalPlaces(4);
		x.setUnit("mm");
		final GridData gd_x = new GridData(SWT.FILL, SWT.CENTER, true, false);
		x.setLayoutData(gd_x);

		final Link yLabel = new Link(this, SWT.NONE);
		yLabel.setText("<a>y</a>");

		y = new RangeBox(this, SWT.NONE);
		y.setMinimum(-0.1);
		y.setMaximum(40.1);
		y.setDecimalPlaces(4);
		final GridData gd_y = new GridData(SWT.FILL, SWT.CENTER, true, false);
		y.setLayoutData(gd_y);
		y.setUnit("mm");

		final Link zLabel = new Link(this, SWT.NONE);
		zLabel.setText("<a>z</a>");

		z = new RangeBox(this, SWT.NONE);
		z.setMinimum(-15.3);
		z.setMaximum(14.1);
		z.setDecimalPlaces(4);
		final GridData gd_z = new GridData(SWT.FILL, SWT.CENTER, true, false);
		z.setLayoutData(gd_z);
		z.setUnit("mm");

		final Link rotationLabel = new Link(this, SWT.NONE);
		rotationLabel.setText("<a>Rotation*</a>");

		final GridData gd_rotation = new GridData(SWT.FILL, SWT.CENTER, true, false);
		rotation = new RangeBox(this, SWT.NONE);
		rotation.setMaximum(51);
		rotation.setMinimum(-228.5);
		rotation.setDecimalPlaces(4);
		rotation.setLayoutData(gd_rotation);
		rotation.setUnit("°");

		final Link rollLabel = new Link(this, SWT.NONE);
		rollLabel.setText("<a>Roll*</a>");

		roll = new RangeBox(this, SWT.NONE);
		roll.setMinimum(-12.2);
		final GridData gd_roll = new GridData(SWT.FILL, SWT.CENTER, true, false);
		roll.setLayoutData(gd_roll);
		roll.setUnit("°");
		roll.setMaximum(12.2);
		roll.setDecimalPlaces(4);

		final Link yawLabel = new Link(this, SWT.NONE);
		yawLabel.setText("<a>Yaw*</a>");

		yaw = new RangeBox(this, SWT.NONE);
		yaw.setMinimum(-10.6);
		final GridData gd_yaw = new GridData(SWT.FILL, SWT.CENTER, true, false);
		yaw.setLayoutData(gd_yaw);
		yaw.setUnit("°");
		yaw.setMaximum(10.6);
		yaw.setDecimalPlaces(4);
		
		ScannableValueListener.createLinkedLabel(xLabel,        "sample_x",     x);
		ScannableValueListener.createLinkedLabel(yLabel,        "sample_y",     y);
		ScannableValueListener.createLinkedLabel(zLabel,        "sample_z",     z);
		ScannableValueListener.createLinkedLabel(rotationLabel, "sample_rot",   rotation);
		ScannableValueListener.createLinkedLabel(rollLabel,     "sample_roll", roll);
		ScannableValueListener.createLinkedLabel(yawLabel,      "sample_pitch",   yaw);
	}
	/**
	 * @return ScaleBox
	 */
	public RangeBox getX() {
		return x;
	}
	/**
	 * @return ScaleBox
	 */
	public RangeBox getY() {
		return y;
	}
	/**
	 * @return ScaleBox
	 */
	public RangeBox getZ() {
		return z;
	}
	/**
	 * @return ScaleBox
	 */
	public RangeBox getRotation() {
		return rotation;
	}
	/**
	 * @return ScaleBox
	 */
	public RangeBox getRoll() {
		return roll;
	}
	/**
	 * @return ScaleBox
	 */
	public RangeBox getYaw() {
		return yaw;
	}

}

	