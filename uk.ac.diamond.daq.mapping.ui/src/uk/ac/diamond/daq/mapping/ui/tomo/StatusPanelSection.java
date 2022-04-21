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

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.document.scanpath.PathInfo;

class StatusPanelSection extends AbstractTomoViewSection {

	private static final Logger logger = LoggerFactory.getLogger(StatusPanelSection.class);

	private Label statusLabel;
	private String statusMessage = null;
	private PathInfo pathInfo = null;

	@Override
	public void createControls(Composite parent) {
		super.createSeparator(parent);

		final Composite composite = createComposite(parent, 1, true);
		statusLabel = new Label(composite, SWT.NONE);
		statusLabel.setText(" \n "); // to ensure two lines are allocated
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(statusLabel);

		asyncExec(this::updateStatusLabel);
	}

	@Override
	public void setStatusMessage(String statusMessage) {
		this.statusMessage = statusMessage;
		asyncExec(this::updateStatusLabel);
	}

	@Override
	protected void updateStatusLabel() {
		if (statusLabel.isDisposed()) {
			logger.warn("Attempt to update Status label when disposed");
		}

		if (statusMessage != null && !statusMessage.isBlank()) {
			statusLabel.setText(statusMessage + '\n'); // add '\n' to ensure label is two lines, in case of relayout event
			return;
		} else if (pathInfo == null) {
			return; // come back later
		}

		final int numberOfPoints = pathInfo.getTotalPointCount();
		final String numPointsString = String.format("Number of points: %,d", numberOfPoints);
		final double totalTime = getBean().getMalcolmModel().getExposureTime() * numberOfPoints;
		final String scanTimeString = String.format("Estimated scan time: %02.0f:%02.0f:%02.0f",
				Math.floor(totalTime / 3600.0),
				Math.floor((totalTime % 3600.0) / 60.0),
				totalTime % 60.0);
		statusLabel.setText(numPointsString + "\n" + scanTimeString);
	}

	public void setPathInfo(PathInfo pathInfo) {
		this.pathInfo = pathInfo;
		this.statusMessage = null;
		asyncExec(this::updateStatusLabel);
	}

}
