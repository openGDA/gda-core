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

package uk.ac.diamond.daq.mapping.ui.experiment.focus;

import static uk.ac.diamond.daq.mapping.ui.experiment.focus.FocusScanUtils.saveConfig;

import java.util.Objects;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.function.ILinearFunction;
import uk.ac.diamond.daq.mapping.api.EnergyFocusBean;

/**
 * Allows the user to edit a a LinearFunction coupling beamline energy and focus position. It was written for I08 but
 * may be applicable to other beamlines.
 * <p>
 * The function to be edited and the file in which it is to be serialised are passed in a
 * {@link EnergyFocusBean}
 * <p>
 * The class is intended to be used in a {@link FocusScanSetupPage} and as such writes directly onto the parent
 * Composite, which must have a grid layout with at least 2 columns.
 */
public class EnergyFocusEditor {
	private static final Logger logger = LoggerFactory.getLogger(EnergyFocusEditor.class);

	private final ILinearFunction energyFocusFunction;
	private final String energyFocusConfigPath;

	private final EnergyFocusFunctionDisplay energyFocusDisplay;

	public EnergyFocusEditor(Composite parent, EnergyFocusBean energyFocusBean) {
		energyFocusFunction = energyFocusBean.getEnergyFocusFunction();
		Objects.requireNonNull(energyFocusFunction, "No energy focus function defined for this beamline");
		energyFocusConfigPath = energyFocusBean.getEnergyFocusConfigPath();
		Objects.requireNonNull(energyFocusConfigPath, "No file defined to save energy focus function settings");

		final Label title = new Label(parent, SWT.NONE);
		GridDataFactory.swtDefaults().span(2, 1).applyTo(title);
		title.setText("Energy focus mapping:");

		energyFocusDisplay = new EnergyFocusFunctionDisplay(parent, energyFocusFunction);

		final Button applyButton = new Button(parent, SWT.PUSH);
		GridDataFactory.swtDefaults().span(2, 1).align(SWT.END, SWT.CENTER).applyTo(applyButton);
		applyButton.setText("Apply");
		applyButton.setToolTipText("Apply new energy focus values");
		applyButton.addSelectionListener(SelectionListener.widgetSelectedAdapter(e -> save()));
	}

	/**
	 * Update displayed values from the energy focus function
	 * <p>
	 * This handles the case where the wizard page is redisplayed when function values have been updated externally
	 * (e.g. on the command line)
	 */
	public void refresh() {
		energyFocusDisplay.refresh();
	}

	/**
	 * Update the energy focus function object from the GUI values, and save to file
	 */
	public void save() {
		try {
			energyFocusDisplay.updateEnergyFocusFunction();
			saveConfig(energyFocusFunction, energyFocusConfigPath, logger);
		} catch (Exception e) {
			FocusScanUtils.displayError("Error saving function", "Error saving energy focus function", logger);
		}
	}
}
