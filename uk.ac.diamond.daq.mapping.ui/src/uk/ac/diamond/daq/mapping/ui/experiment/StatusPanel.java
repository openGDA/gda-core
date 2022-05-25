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

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.points.models.IAxialModel;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;
import uk.ac.diamond.daq.mapping.api.IScanModelWrapper;
import uk.ac.diamond.daq.mapping.api.document.scanpath.PathInfo;

public class StatusPanel extends AbstractMappingSection {

	private Label statusLabel;
	private PathInfo pathInfo;
	private ScanPointsCalculator scanPointsCalculator;
	private IMappingExperimentBean mappingBean;

	private static final Logger logger = LoggerFactory.getLogger(StatusPanel.class);

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		final Composite sectionComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sectionComposite);
		GridLayoutFactory.fillDefaults().applyTo(sectionComposite);

		statusLabel = new Label(sectionComposite, SWT.NONE);
		statusLabel.setText(" \n "); // to make sure height is allocated correctly
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(statusLabel);
	}

	@Override
	protected void updateStatusLabel() {
		if (statusLabel.isDisposed()) {
			logger.warn("Attempt to update Status label when disposed");
			return;
		}

		String firstLine = "";
		String secondLine = "";

		if (displayScanPointsCalculatorResults()) {
			firstLine = getScanPointsCalculatorString();
			secondLine = "Full scan path calculation in progress...";
		} else if (pathInfo != null){
			firstLine = getPathInfoString();
			secondLine = getStepsString();
		}

		statusLabel.setText(firstLine + "\n" + secondLine);
	}

	private String getNumberOfPointsString(int scanPoints) {
		return String.format("Map points: % ,d", scanPoints);
	}

	private String getExposureString(int scanPoints) {
		double totalTime = getExposureTime() * scanPoints;
		return String.format("    Total exposure time: %02.0f:%02.0f:%02.0f",
					Math.floor(totalTime / 3600.0),
					Math.floor((totalTime % 3600.0) / 60.0),
					totalTime % 60.0);
	}

	private String getStepsString() {
		return String.format("Smallest steps: X = %s;  Y = %s;  Absolute = %s",
				pathInfo.getFormattedSmallestXStep(),
				pathInfo.getFormattedSmallestYStep(),
				pathInfo.getFormattedSmallestAbsStep());
	}

	private double getExposureTime() {
		return mappingBean.getDetectorParameters().stream()
				.filter(IScanModelWrapper<IDetectorModel>::isIncludeInScan)
				.mapToDouble(wrapper -> wrapper.getModel().getExposureTime())
				.max().orElse(0);
	}

	private String getScanPointsCalculatorString() {
		int totalPoints = scanPointsCalculator.calculateScanPoints();
		return getNumberOfPointsString(totalPoints) + getExposureString(totalPoints);
	}

	private String getPathInfoString() {
		int innerPoints = pathInfo.getInnerPointCount();
		int totalPoints = pathInfo.getTotalPointCount();
		int actualPoints = pathInfo.getReturnedPointCount();

		String pointsMessage = getNumberOfPointsString(totalPoints);
		if (actualPoints < innerPoints)
			pointsMessage += String.format(" (Only displaying the first %,d points)",
								actualPoints);

		return pointsMessage + getExposureString(totalPoints);
	}

	void setScanPointsCalculator(ScanPointsCalculator scanPointsCalculator) {
		this.scanPointsCalculator = scanPointsCalculator;
		if (scanPointsCalculator.canCalculateScanPoints()) {
			updateStatusLabel();
		} else {
			statusLabel.setText("Full scan path calculation in progress...");
		}
	}

	protected List<IScanModelWrapper<IAxialModel>> getOuterScannables() {
		return mappingBean.getScanDefinition().getOuterScannables();
	}

	void setPathInfo(PathInfo pathInfo) {
		this.pathInfo = pathInfo;
		updateStatusLabel();
	}

	void setMessage(String message) {
		statusLabel.setText(message);
	}

	void setMappingBean(IMappingExperimentBean mappingBean) {
		this.mappingBean = mappingBean;
	}

	private boolean displayScanPointsCalculatorResults() {
		return scanPointsCalculator != null && (pathInfo == null || !pathInfo.getEventId().equals(scanPointsCalculator.getEventId()));
	}


}
