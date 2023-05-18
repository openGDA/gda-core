/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.tomo;

import static uk.ac.diamond.daq.mapping.ui.tomo.TensorTomoScanSetupView.ANGLE_1_LABEL;
import static uk.ac.diamond.daq.mapping.ui.tomo.TensorTomoScanSetupView.ANGLE_2_LABEL;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

class StatusPanelSection extends AbstractTomoViewSection {

	private static final Logger logger = LoggerFactory.getLogger(StatusPanelSection.class);

	private static final String EMPTY_LABEL = " \n";

	private Label overallScanLabel;
	private Label tomoScanLabel;
	private String statusMessage = null;

	@Override
	public void createControls(Composite parent) {
		super.createSeparator(parent);

		final Composite composite = createComposite(parent, 2, true);
		((GridLayout) composite.getLayout()).makeColumnsEqualWidth = true;
		overallScanLabel = new Label(composite, SWT.NONE);
		overallScanLabel.setText(EMPTY_LABEL); // to ensure two lines are allocated
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(overallScanLabel);

		tomoScanLabel = new Label(composite, SWT.NONE);
		tomoScanLabel.setText(EMPTY_LABEL);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(tomoScanLabel);

		asyncExec(this::updateStatusLabel);
	}

	@Override
	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
		asyncExec(this::updateStatusLabel);
	}

	@Override
	protected void updateStatusLabel() {
		if (overallScanLabel.isDisposed()) {
			logger.warn("Attempt to update Status label when disposed");
		}

		final TensorTomoPathInfo pathInfo = getView().getPathInfo();
		if (statusMessage != null && !statusMessage.isBlank()) {
			overallScanLabel.setText(statusMessage + '\n'); // add '\n' to ensure label is two lines, in case of relayout event
			tomoScanLabel.setText(EMPTY_LABEL);
			return;
		} else if (pathInfo == null) {
			return; // come back later
		}

		final int numberOfPoints = pathInfo.getTotalPointCount();
		final String numPointsString = String.format("Total number of points: %,d", numberOfPoints);
		final double totalTime = getBean().getMalcolmModel().getExposureTime() * numberOfPoints;
		final String scanTimeString = String.format("Estimated scan time: %02.0f:%02.0f:%02.0f",
				Math.floor(totalTime / 3600.0),
				Math.floor((totalTime % 3600.0) / 60.0),
				totalTime % 60.0);
		overallScanLabel.setText(numPointsString + "\n" + scanTimeString);

		final String numAngle1PositionsString = String.format("Number of %s positions: %d", ANGLE_1_LABEL, pathInfo.getAngle1Positions().length);
		final String scanString = String.format("Total number of (%s, %s) positions: %,d", ANGLE_1_LABEL, ANGLE_2_LABEL, pathInfo.getOuterPointCount());
		tomoScanLabel.setText(numAngle1PositionsString + "\n" + scanString);
	}

}
