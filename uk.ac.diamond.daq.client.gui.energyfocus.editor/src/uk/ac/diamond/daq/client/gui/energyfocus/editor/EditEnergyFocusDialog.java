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

package uk.ac.diamond.daq.client.gui.energyfocus.editor;

import static uk.ac.diamond.daq.mapping.ui.experiment.focus.FocusScanUtils.saveConfig;

import org.eclipse.jface.dialogs.TitleAreaDialog;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.function.ILinearFunction;
import uk.ac.diamond.daq.mapping.ui.experiment.focus.EnergyFocusFunctionDisplay;

public class EditEnergyFocusDialog extends TitleAreaDialog {
	private static final Logger logger = LoggerFactory.getLogger(EditEnergyFocusDialog.class);

	private static final String TITLE = "Energy focus function editor";
	private static final String MESSAGE = "Edit the parameters for the energy focus mapping";

	private static final int INITIAL_WIDTH = 450;
	private static final int INITIAL_HEIGHT = 300;

	private final ILinearFunction energyFocusFunction;
	private final String energyFocusConfigPath;
	private EnergyFocusFunctionDisplay energyFocusDisplay;

	public EditEnergyFocusDialog(Shell parentShell, ILinearFunction energyFocusFunction, String energyFocusConfigPath) {
		super(parentShell);
		this.energyFocusFunction = energyFocusFunction;
		this.energyFocusConfigPath = energyFocusConfigPath;
	}

	@Override
	public void create() {
		super.create();
		setTitle(TITLE);
		setMessage(MESSAGE);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		final Composite dialogArea = (Composite) super.createDialogArea(parent);
		energyFocusDisplay = new EnergyFocusFunctionDisplay(dialogArea, energyFocusFunction);
		return dialogArea;
	}

	@Override
	protected Point getInitialSize() {
		return new Point(INITIAL_WIDTH, INITIAL_HEIGHT);
	}

	@Override
	protected void okPressed() {
		energyFocusDisplay.updateEnergyFocusFunction();
		saveConfig(energyFocusFunction, energyFocusConfigPath, logger);
		super.okPressed();
	}

}
