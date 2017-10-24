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

import java.util.Objects;
import java.util.OptionalDouble;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGenerator;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.scan.ScanningException;
import org.eclipse.scanning.device.ui.ServiceHolder;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.IDetectorModelWrapper;
import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;

class StatusPanel extends Composite {

	private Label statusLabel;
	private String message;
	private PathInfo pathInfo;
	private final IMappingExperimentBean mappingBean;
	private final ScanRequestConverter scanRequestConverter;
	private final IPointGeneratorService pointGeneratorService;

	private static final Logger logger = LoggerFactory.getLogger(StatusPanel.class);

	StatusPanel(Composite parent, int style, IMappingExperimentBean mappingBean, ScanRequestConverter scanRequestConverter) {
		super(parent, style);
		this.mappingBean = mappingBean;
		this.scanRequestConverter = scanRequestConverter;
		pointGeneratorService = ServiceHolder.getGeneratorService();

		GridLayoutFactory.fillDefaults().applyTo(this);

		statusLabel = new Label(parent, SWT.NONE);
		statusLabel.setText(" \n "); // to make sure height is allocated correctly
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(statusLabel);

		Label separator = new Label(parent, SWT.SEPARATOR | SWT.HORIZONTAL);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false).applyTo(separator);

		updateStatusLabel();
	}

	void updateStatusLabel() {
		final String firstLine;
		final String secondLine;
		if (message != null && message.length() > 0) {
			firstLine = message;
			secondLine = "";
		} else {
			firstLine = getNumberOfPointsString() + getExposureString();
			secondLine = getStepsString();
		}
		String text = firstLine + "\n" + secondLine;
		statusLabel.setText(text);
	}

	private String getNumberOfPointsString() {
		int totalPoints = getScanPoints();
		if (totalPoints > PathInfoCalculatorJob.MAX_POINTS_IN_ROI) {
			return String.format(" (Only displaying the first %,d points)",
								PathInfoCalculatorJob.MAX_POINTS_IN_ROI);
		}
		return "Map points: " + totalPoints;
	}

	private String getExposureString() {
		double totalTime = getPointExposureTime();
		return String.format("    Exposure time: %02.0f:%02.0f:%02.0f",
					Math.floor(totalTime / 3600.0),
					Math.floor((totalTime % 3600.0) / 60.0),
					totalTime % 60.0);
	}

	private String getStepsString() {
		if (Objects.isNull(pathInfo)) return "";
		return String.format("Smallest steps: X = %s;  Y = %s;  Absolute = %s",
				pathInfo.getFormattedSmallestXStep(),
				pathInfo.getFormattedSmallestYStep(),
				pathInfo.getFormattedSmallestAbsStep());
	}

	private int getScanPoints() {
		if (Objects.isNull(mappingBean.getScanDefinition().getMappingScanRegion().getScanPath())) {
			// When starting the client from a fresh workspace,
			// the mapping view won't have been fully initialised at this point.
			// We'll return zero now; this method will be called again when the scan path is set.
			return 0;
		}
		ScanRequest<IROI> req = getScanRequest();
		if (Objects.isNull(req)) {
			return get2DPoints();
		}
		CompoundModel<IROI> cm = req.getCompoundModel();
		try {
			final IPointGenerator<?> gen = pointGeneratorService.createCompoundGenerator(cm);
			return gen.size();
		} catch(GeneratorException ge) {
			logger.warn("Can only provide 2D map information", ge);
			return get2DPoints();
		}
	}

	private int get2DPoints() {
		if (Objects.isNull(pathInfo)) {
			logger.error("Path not set!");
			return 0;
		} else {
			return pathInfo.pointCount;
		}
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

	private double getPointExposureTime() {
		OptionalDouble exposure = mappingBean.getDetectorParameters().stream()
				.filter(IDetectorModelWrapper::isIncludeInScan)
				.mapToDouble(wrapper -> wrapper.getModel().getExposureTime())
				.max();
		return exposure.orElse(0) * getScanPoints();
	}

	private ScanRequest<IROI> getScanRequest() {
		try {
			return scanRequestConverter.convertToScanRequest(mappingBean);
		} catch (ScanningException e) {
			logger.error("Cannot get the ScanRequest!", e);
			return null;
		}
	}
}
