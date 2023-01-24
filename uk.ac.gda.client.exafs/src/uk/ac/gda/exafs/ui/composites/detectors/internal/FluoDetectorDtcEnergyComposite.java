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

package uk.ac.gda.exafs.ui.composites.detectors.internal;

import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.richbeans.api.widget.IFieldWidget;
import org.eclipse.richbeans.widgets.scalebox.ScaleBox;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;

public class FluoDetectorDtcEnergyComposite extends Composite {
	private ScaleBox dtcEnergyBox;
	private Button updateDtcEnergyButton;

	public FluoDetectorDtcEnergyComposite(Composite parent, int style) {
		super(parent, style);
		this.setLayout(new FillLayout());

		Group group = new Group(this, SWT.NONE);
		group.setText("Dead time correction energy");
		GridLayoutFactory.swtDefaults().numColumns(3).applyTo(group);

		Label energyLabel = new Label(group, SWT.NONE);
		energyLabel.setText("Dead time correction energy (keV) ");
		energyLabel.setToolTipText("This is energy that will be used for the detector 'deadtime correction factor' calculation");

		dtcEnergyBox = new ScaleBox(group, SWT.NONE);
		dtcEnergyBox.setMinimum(0);
		dtcEnergyBox.setValue(0);
		dtcEnergyBox.setMaximum(500);
		dtcEnergyBox.setDecimalPlaces(4);
		dtcEnergyBox.setUnit("keV");
		dtcEnergyBox.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));

		updateDtcEnergyButton = new Button(group, SWT.NONE);
		updateDtcEnergyButton.setText("Get value from line");
		updateDtcEnergyButton.setToolTipText("Set the energy using the value from the line currently selected in 'Element name and line selection' part of gui");
		updateDtcEnergyButton.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
	}

	/**
	 *
	 * @return ScaleBox containing 'Deadtime correction energy' value (keV)
	 */
	public IFieldWidget getDeadtimeCorrectionEnergy() {
		return dtcEnergyBox;
	}

	public Button getUpdateDtcEnergyButton() {
		return updateDtcEnergyButton;
	}

}
