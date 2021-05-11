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
import org.eclipse.scanning.api.points.models.IScanPointGeneratorModel;
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
	private String message;
	private PathInfo pathInfo;
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

		updateStatusLabel();
	}

	@Override
	protected void updateStatusLabel() {
		if (statusLabel.isDisposed()) {
			logger.warn("Attempt to update Status label when disposed");
			return;
		}

		final String firstLine;
		final String secondLine;
		if (message != null && message.length() > 0) {
			firstLine = message;
			secondLine = "";
		} else {
			if (pathInfo == null) return; // come back later
			firstLine = getNumberOfPointsString() + getExposureString();
			secondLine = getStepsString();
		}
		String text = firstLine + "\n" + secondLine;
		statusLabel.setText(text);
	}

	private String getNumberOfPointsString() {
		int innerPoints = pathInfo.getInnerPointCount();
		int totalPoints = pathInfo.getTotalPointCount();
		int actualPoints = pathInfo.getReturnedPointCount();

		String pointsMessage = String.format("Map points: %,d",
				totalPoints);
		if (actualPoints < innerPoints)
			pointsMessage += String.format(" (Only displaying the first %,d points)",
								actualPoints);
		return pointsMessage;
	}

	private String getExposureString() {
		double totalTime = getPointExposureTime();
		return String.format("    Exposure time: %02.0f:%02.0f:%02.0f",
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

	protected List<IScanModelWrapper<IScanPointGeneratorModel>> getOuterScannables() {
		return mappingBean.getScanDefinition().getOuterScannables();
	}

	void setPathInfo(PathInfo pathInfo) {
		this.pathInfo = pathInfo;
		this.message = null;
		updateStatusLabel();
	}

	void setMessage(String message) {
		this.message = message;
		updateStatusLabel();
	}

	void setMappingBean(IMappingExperimentBean mappingBean) {
		this.mappingBean = mappingBean;
	}

	private double getPointExposureTime() {
		return mappingBean.getDetectorParameters().stream()
				.filter(IScanModelWrapper<IDetectorModel>::isIncludeInScan)
				.mapToDouble(wrapper -> wrapper.getModel().getExposureTime())
				.max().orElse(0) * pathInfo.getTotalPointCount();
	}
}