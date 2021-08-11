/*-
 * Copyright © 2016 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.experiment;

import java.util.Map;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;

import uk.ac.gda.client.widgets.BeamlineConfigurationControls;

/**
 * A dialog for editing beamline configuration.
 */
public class EditBeamlineConfigurationDialog extends Dialog {

	private final BeamlineConfigurationControls controls;

	private Map<String, Object> newBeamlineConfiguration;

	protected EditBeamlineConfigurationDialog(Shell parentShell,
			IScannableDeviceService scannableDeviceService, Map<String, Object> initialConfiguration) {
		super(parentShell);
		controls = new BeamlineConfigurationControls(scannableDeviceService, initialConfiguration);
	}

	@Override
	protected boolean isResizable() {
		return true;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Edit Beamline Configuration");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(600, 500);
	}

	@Override
	public Control createDialogArea(Composite parent) {
		var composite = new Composite(parent, SWT.NONE);
		GridDataFactory.swtDefaults().grab(true, true).align(SWT.FILL, SWT.FILL).applyTo(composite);
		GridLayoutFactory.swtDefaults().applyTo(composite);
		controls.draw(composite);
		return composite;
	}

	@Override
	public void okPressed() {
		newBeamlineConfiguration = controls.getBeamlineConfiguration();
		super.okPressed();
	}

	/**
	 * Returns the new, modified beamline configuration.
	 * @return new beamline configuration
	 */
	public Map<String, Object> getModifiedBeamlineConfiguration() {
		return newBeamlineConfiguration;
	}
}