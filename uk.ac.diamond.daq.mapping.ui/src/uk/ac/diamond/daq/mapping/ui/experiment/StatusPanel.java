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

import java.text.DecimalFormat;
import java.util.Arrays;
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

import gda.configuration.properties.LocalProperties;
import uk.ac.diamond.daq.mapping.api.IMappingExperimentBean;
import uk.ac.diamond.daq.mapping.api.IScanModelWrapper;
import uk.ac.diamond.daq.mapping.api.document.scanpath.PathInfo;
import uk.ac.diamond.daq.mapping.region.CentredRectangleMappingRegion;
import uk.ac.diamond.daq.mapping.region.LineMappingRegion;
import uk.ac.diamond.daq.mapping.region.RectangularMappingRegion;

public class StatusPanel extends AbstractMappingSection {

	private Label statusLabel;
	private PathInfo pathInfo;
	private ScanPointsCalculator scanPointsCalculator;
	private IMappingExperimentBean mappingBean;
	private static final String DISPLAY_SPEED = "gda.client.displayMotorSpeed";

	private static final Logger logger = LoggerFactory.getLogger(StatusPanel.class);

	@Override
	public void createControls(Composite parent) {
		super.createControls(parent);
		final Composite sectionComposite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().grab(true, false).applyTo(sectionComposite);
		GridLayoutFactory.fillDefaults().applyTo(sectionComposite);

		statusLabel = new Label(sectionComposite, SWT.NONE);
		// to make sure height is allocated correctly set the number of rows in the statusLabel
		statusLabel.setText(" \n ");
		if(LocalProperties.check(DISPLAY_SPEED))
			statusLabel.setText(" \n \n ");
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
			if(LocalProperties.check(DISPLAY_SPEED) && scanPointsCalculator!=null) {
				secondLine += "\n"+getApproxMotorSpeed();
			}
		}
		statusLabel.setText(firstLine + "\n" + secondLine);
	}

	private String getApproxMotorSpeed() {
		if(scanPointsCalculator.getScanRegionShape() instanceof RectangularMappingRegion
				|| scanPointsCalculator.getScanRegionShape() instanceof CentredRectangleMappingRegion) {
			return getMotorSpeedReportForRectangle();
		}
		else if(scanPointsCalculator.getScanRegionShape() instanceof LineMappingRegion) {
			return getMotorSpeedReportForLine();
		}
		else {
			return "Approx. Motor Speed: N/A";
		}
	}

	/**
	 * This method will return a report on the motor speed for a line, to be displayed in the status panel.
	 * Because of where this method is called we can assume that scanPointsCalculator and path info
	 * will not be null, as the null check should have happened earlier.
	 * */
	private String getMotorSpeedReportForLine() {
		double[] xpoints = pathInfo.getXCoordinates();
		double[] ypoints = pathInfo.getYCoordinates();
		int numOfPoints = xpoints.length;
		double xdistance = xpoints[0] - xpoints[numOfPoints-1];
		double ydistance = ypoints[0] - ypoints[numOfPoints-1];
		double distance = Math.sqrt(Math.pow(ydistance,2)+Math.pow(xdistance, 2));
		double time = getExposureTime()*numOfPoints;
		double speed = distance/time;
		String units = (scanPointsCalculator.getUnits().isEmpty())? "": scanPointsCalculator.getUnits();
		DecimalFormat df = new DecimalFormat("0.00");
		return String.format("Approx. Max Motor Speed: %s %s/s",df.format(speed), units);
	}

	/**
	 * This method will return a report on the motor speed for a rectangle, to be displayed in the status panel.
	 * Because of where this method is called we can assume that scanPointsCalculator and path info
	 * will not be null, as the null check should have happened earlier.
	 * */
	private String getMotorSpeedReportForRectangle() {
		double[] xpoints = pathInfo.getXCoordinates();
		// Sort the x values
		Arrays.sort(xpoints);
		// Get the difference between the last and first x values to get the distance travelled in one row
		double distance = xpoints[xpoints.length-1] - xpoints[0];
		// Count the number of distinct x values to get the number of points in a row
		long numxPoints = Arrays.stream(xpoints).distinct().count();
		// The motor is moving continuously but needs to be moving at
		// such a speed that it is able to spend the set exposure time at
		// each point - so the total time will equal the exposure time
		// multiplied by the number of points.
		double time = getExposureTime() * numxPoints;
		double speed = distance/time;
		String units = (scanPointsCalculator.getUnits().isEmpty())? "": scanPointsCalculator.getUnits();
		DecimalFormat df = new DecimalFormat("0.00");
		return String.format("Approx. Max Motor Speed: %s %s/s",df.format(speed), units);
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
