/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.tomography;

import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;
import static uk.ac.gda.ui.tool.ClientMessages.CONFIGURE;
import static uk.ac.gda.ui.tool.ClientMessagesUtility.getMessage;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.ui.SubmitScanToScriptSection;
import uk.ac.diamond.daq.mapping.ui.tomography.TomographyConfigurationDialog.Motor;

public class TomographySubmitScanSection extends SubmitScanToScriptSection {
	private static final Logger logger = LoggerFactory.getLogger(TomographySubmitScanSection.class);

	private static final int NUM_COLUMNS = 4;

	// Names of the motors whose values are to be used in configuration
	private final String fileDirectory;

	public TomographySubmitScanSection(String fileDirectory) {
		this.fileDirectory = fileDirectory;
	}

	@Override
	protected void createSubmitSection() {
		final Composite submitComposite = new Composite(getComposite(), SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(submitComposite);
		GridLayoutFactory.swtDefaults().numColumns(NUM_COLUMNS).applyTo(submitComposite);

		createSubmitButton(submitComposite);

		// Button to show configuration dialogue
		final Button configButton = new Button(submitComposite, SWT.PUSH);
		GridDataFactory.swtDefaults().applyTo(configButton);
		configButton.setText(getMessage(CONFIGURE));
		configButton.addSelectionListener(widgetSelectedAdapter(e -> showConfigurationDialog()));

		createStopButton(submitComposite);
	}

	@Override
	protected void onShow() {
		selectOuterScannable(Motor.R.getScannableName(), true);
	}

	@Override
	protected void onHide() {
		selectOuterScannable(Motor.R.getScannableName(), false);
	}

	private void showConfigurationDialog() {
		final TomographyConfigurationDialog dialog = new TomographyConfigurationDialog(getShell(), fileDirectory);
		dialog.open();
	}
}
