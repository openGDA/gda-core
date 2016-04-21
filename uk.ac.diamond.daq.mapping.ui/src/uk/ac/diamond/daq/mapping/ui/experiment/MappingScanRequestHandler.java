/*-
 * Copyright © 2015 Diamond Light Source Ltd.
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

import gda.jython.InterfaceProvider;

import java.util.List;

import org.eclipse.scanning.api.event.EventException;
import org.eclipse.scanning.api.event.core.IConsumerProcess;
import org.eclipse.scanning.api.event.core.IPublisher;
import org.eclipse.scanning.api.points.GeneratorException;
import org.eclipse.scanning.api.points.IPointGeneratorService;
import org.eclipse.scanning.api.points.Point;
import org.eclipse.scanning.api.points.models.GridModel;
import org.eclipse.scanning.api.points.models.IScanPathModel;
import org.eclipse.scanning.api.points.models.RasterModel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.mapping.api.IDetectorModelWrapper;
import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;
import uk.ac.diamond.daq.mapping.api.IMappingScanRegion;
import uk.ac.diamond.daq.mapping.api.MappingExperimentStatusBean;
import uk.ac.diamond.daq.mapping.region.RectangularMappingRegion;

/**
 * Runs old scan, will be useful when running old scan from queue.
 */
@Deprecated
public class MappingScanRequestHandler implements IConsumerProcess<MappingExperimentStatusBean> {

	private static final Logger logger = LoggerFactory.getLogger(MappingScanRequestHandler.class);

	private static IPointGeneratorService pointGeneratorFactory;

	public static void setPointGeneratorService(IPointGeneratorService service) {
		pointGeneratorFactory = service;
	}

	public static final String X_AXIS_NAME = "stage_x";
	public static final String Y_AXIS_NAME = "stage_y";
	public static final String XY_GROUP_NAME = "mapping_stage_xy";

	private final MappingExperimentStatusBean experimentBean;
	private final IPublisher<MappingExperimentStatusBean> publisher;

	/**
	 * Only for OSGi!
	 */
	public MappingScanRequestHandler() {
		this.experimentBean = null;
		this.publisher = null;
	}

	public MappingScanRequestHandler(MappingExperimentStatusBean experimentBean, IPublisher<MappingExperimentStatusBean> publisher) {
		this.experimentBean = experimentBean;
		this.publisher = publisher;
	}

	@Override
	public MappingExperimentStatusBean getBean() {
		return experimentBean;
	}

	@Override
	public IPublisher<MappingExperimentStatusBean> getPublisher() {
		return publisher;
	}

	@Override
	public void execute() throws EventException {
		runMapScan(experimentBean.getMappingExperimentBean());
	}

	@Override
	public void terminate() throws EventException {
		InterfaceProvider.getCommandAborter().abortCommands();
	}

	private void runMapScan(IMappingExperimentBean experimentBean) {
		String scanCommand = createScanCommand(experimentBean);
		InterfaceProvider.getCommandRunner().runCommand(scanCommand);
	}

	private String createScanCommand(IMappingExperimentBean experimentBean) {

		outputToLogAndConsole("Setting up scan for sample " + experimentBean.getSampleMetadata().getSampleName());

		IMappingScanRegion mapRegion = experimentBean.getScanDefinition().getMappingScanRegion();

		String scanCommand = "scan ";

		scanCommand += getScanMotorParameters(mapRegion);

		scanCommand += getScanDetectorParameters(experimentBean.getDetectorParameters());

		outputToLogAndConsole("Scan command: " + scanCommand);
		return scanCommand;
	}

	private String getScanMotorParameters(IMappingScanRegion mapRegion) {
		// Handle rectangles as a special case so the data is written to the file in a nice 2D block
		// TODO currently this will use the wrong area if an angled rectangle is given
		IScanPathModel scanPath = mapRegion.getScanPath();
		if (mapRegion.getRegion() instanceof RectangularMappingRegion
				&& ((scanPath instanceof GridModel && !((GridModel) scanPath).isSnake())
						|| (scanPath instanceof RasterModel && !((RasterModel) scanPath).isSnake()))) {
			RectangularMappingRegion rectangle = (RectangularMappingRegion) mapRegion.getRegion();
			double xStart, xStop, xStep;
			double yStart, yStop, yStep;
			if (scanPath instanceof GridModel) {
				yStep = (rectangle.getyStop() - rectangle.getyStart()) / ((GridModel) scanPath).getSlowAxisPoints();
				yStart = rectangle.getyStart() + yStep / 2;
				yStop = rectangle.getyStop();
				xStep = (rectangle.getxStop() - rectangle.getxStart()) / ((GridModel) scanPath).getFastAxisPoints();
				xStart = rectangle.getxStart() + xStep / 2;
				xStop = rectangle.getxStop();
			} else {
				yStep = ((RasterModel) scanPath).getSlowAxisStep();
				yStart = rectangle.getyStart();
				yStop = rectangle.getyStop();
				xStep = ((RasterModel) scanPath).getFastAxisStep();
				xStart = rectangle.getxStart();
				xStop = rectangle.getxStop();
			}
			// Y first because X is expected to be the fast axis and therefore the inner scan should be X
			String scanCommandFormatString = "%s %s %s %s %s %s %s %s ";
			return String.format(scanCommandFormatString, Y_AXIS_NAME, yStart, yStop, yStep, X_AXIS_NAME, xStart, xStop, xStep);
		} else {
			// Not a rectangle - allow the scan path to generate a list of points from the ROI
			String scanMotorCommand = XY_GROUP_NAME + " tuple(["; // use tuple keyword to allow one-element lists to work
			try {
				Iterable<Point> pointIterable = pointGeneratorFactory.createGenerator(scanPath, mapRegion.getRegion().toROI());
				for (Point point : pointIterable) {
					scanMotorCommand += "(" + point.getX() + ", " + point.getY() + "), ";
				}
			} catch (GeneratorException e) {
				logger.error("Error creating point generator", e);
				throw new RuntimeException(e);
			}
			scanMotorCommand += "]) ";
			return scanMotorCommand;
		}
	}

	// TODO handle detectors without exposure time?
	private String getScanDetectorParameters(List<IDetectorModelWrapper> detectorParametersList) {
		String result = "";
		for (IDetectorModelWrapper detectorWrapper : detectorParametersList) {
			if (detectorWrapper.isIncludeInScan()) {
				result += String.format("%s %s ", detectorWrapper.getName(), detectorWrapper.getModel().getExposureTime());
			}
		}
		return result;
	}

	private void outputToLogAndConsole(String message) {

		logger.info(message);

		// TODO This console output is only for testing and development - should be removed
		System.out.println(message);

		// TODO Also print to Jython console - this requires uk.ac.gda.api dependency should be removed in future
		InterfaceProvider.getTerminalPrinter().print(message);
	}
}
