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

import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.window.Window;
import org.eclipse.scanning.api.device.IScannableDeviceService;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

/**
 * A section to edit the beamline configuration, i.e. the positions that certain
 * scannables should be set to before a scan in run.
 */
public class BeamlineConfigurationSection extends AbstractMappingSection {

	BeamlineConfigurationSection(MappingExperimentView mappingView, IEclipseContext context) {
		super(mappingView, context);
	}

	@Override
	public void createControls(Composite parent) {
		Composite beamlineConfigComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(beamlineConfigComposite);
		final int configColumns = 2;
		GridLayoutFactory.swtDefaults().numColumns(configColumns).applyTo(beamlineConfigComposite);
		Label beamlineConfigLabel = new Label(beamlineConfigComposite, SWT.NONE);
		beamlineConfigLabel.setText("Beamline Configuration");
		GridDataFactory.fillDefaults().span(configColumns, 1).applyTo(beamlineConfigLabel);
		Button editBeamlineConfigButton = new Button(beamlineConfigComposite, SWT.PUSH);
		editBeamlineConfigButton.setText("Configure Beamline...");
		editBeamlineConfigButton.addListener(SWT.Selection, event -> editBeamlineConfiguration());
	}

	private void editBeamlineConfiguration() {
		final IScannableDeviceService scannableDeviceService = context.get(IScannableDeviceService.class);
		EditBeamlineConfigurationDialog dialog =
				new EditBeamlineConfigurationDialog(getShell(), scannableDeviceService);
		dialog.setInitialBeamlineConfiguration(mappingBean.getBeamlineConfiguration());
		dialog.create();
		if (dialog.open() == Window.OK) {
			mappingBean.setBeamlineConfiguration(dialog.getModifiedBeamlineConfiguration());
		}
	}

}
