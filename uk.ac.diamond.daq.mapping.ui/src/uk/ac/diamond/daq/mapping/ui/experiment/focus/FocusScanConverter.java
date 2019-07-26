/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.roi.IROI;
import org.eclipse.dawnsci.analysis.dataset.roi.LinearROI;
import org.eclipse.scanning.api.device.models.IDetectorModel;
import org.eclipse.scanning.api.event.scan.ScanRequest;
import org.eclipse.scanning.api.points.models.BoundingLine;
import org.eclipse.scanning.api.points.models.CompoundModel;
import org.eclipse.scanning.api.points.models.IBoundingLineModel;
import org.eclipse.scanning.api.points.models.IMapPathModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.OneDEqualSpacingModel;
import org.eclipse.scanning.api.points.models.ScanRegion;
import org.eclipse.scanning.api.points.models.StepModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.FocusScanBean;
import uk.ac.diamond.daq.mapping.api.ILineMappingRegion;
import uk.ac.diamond.daq.mapping.impl.MappingStageInfo;

public class FocusScanConverter {

	private static final Logger logger = LoggerFactory.getLogger(FocusScanConverter.class);

	private MappingStageInfo mappingStageInfo;

	public void setMappingStageInfo(MappingStageInfo mappingStageInfo) {
		this.mappingStageInfo = mappingStageInfo;
	}

	public ScanRequest<IROI> convertToScanRequest(FocusScanBean focusScanBean) {
		logger.debug("Converting focusScanBean to scan request");
		final ScanRequest<IROI> scanRequest = new ScanRequest<>();

		final IMapPathModel lineModel = createLineModel(focusScanBean);
		final ILineMappingRegion lineRegion = focusScanBean.getLineRegion();
		final ScanRegion<IROI> scanRegion = new ScanRegion<>(lineRegion.toROI(),
				lineModel.getYAxisName(), lineModel.getXAxisName());
		final IScanPathModel focusModel = createFocusPathModel(focusScanBean);

		final CompoundModel<IROI> compoundModel = new CompoundModel<>(Arrays.asList(focusModel, lineModel));
		compoundModel.setRegions(Arrays.asList(scanRegion));
		scanRequest.setCompoundModel(compoundModel);

		// add detectors
		final IDetectorModel detectorModel = focusScanBean.getDetector();
		final Map<String, Object> detectorsMap = new HashMap<>();
		detectorsMap.put(detectorModel.getName(), detectorModel);
		scanRequest.setDetectors(detectorsMap);

		return scanRequest;
	}

	private IScanPathModel createFocusPathModel(FocusScanBean focusScanBean) {
		final String zonePlateScannableName = focusScanBean.getFocusScannableName();
		final double centre = focusScanBean.getFocusCentre();
		final double range = focusScanBean.getFocusRange();

		final double step = (range * 2) / focusScanBean.getNumberOfFocusSteps();
		final double start = (centre - range) + step / 2;
		final double stop = centre + range - (step / 2) + (step / 100); // add step/100 to prevent rounding errors

		return new StepModel(zonePlateScannableName, start, stop, step);
	}

	private IBoundingLineModel createLineModel(FocusScanBean focusScanBean) {
		final ILineMappingRegion lineRegion = focusScanBean.getLineRegion();
		final int numPoints = focusScanBean.getNumberOfLinePoints();

		final OneDEqualSpacingModel lineModel = new OneDEqualSpacingModel();
		lineModel.setPoints(numPoints);
		lineModel.setContinuous(true); // continuous if possible (i.e. malcolm)
		if (mappingStageInfo != null) {
			lineModel.setXAxisName(mappingStageInfo.getPlotXAxisName());
			lineModel.setYAxisName(mappingStageInfo.getPlotYAxisName());
		}

		// DO we need to do this, as we set the region in the scan request?
		final LinearROI lineRoi = (LinearROI) lineRegion.toROI();
		BoundingLine line = lineModel.getBoundingLine();
		if (line == null) {
			line = new BoundingLine();
			lineModel.setBoundingLine(line);
		}
		line.setxStart(lineRoi.getPointX());
		line.setyStart(lineRoi.getPointY());
		line.setAngle(lineRoi.getAngle());
		line.setLength(lineRoi.getLength());
		lineModel.setBoundingLine(line);

		return lineModel;
	}

}
