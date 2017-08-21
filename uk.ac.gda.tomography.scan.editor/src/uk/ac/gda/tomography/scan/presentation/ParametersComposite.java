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

package uk.ac.gda.tomography.scan.presentation;

import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.swtdesigner.SWTResourceManager;

import gda.factory.Finder;

public class ParametersComposite extends Composite {

	private static final Logger logger = LoggerFactory.getLogger(ParametersComposite.class);

	private TomographyOptions tomographyOptions;

	// SWT widgets in order of appearance
	private Combo rotationStage;
	private Combo linearStage;

	private Button sendDataToTempDirectory;
	private Text outputDirectory;

	private Text title;
	private Text exposure;
	private Text minI;
	private Text inBeamPosition;
	private Text outBeamPosition;

	private Text start;
	private Text stop;
	private Text step;

	private Text imagesPerDark;
	private Text darkFieldInterval;
	private Text imagesPerFlat;
	private Text flatFieldInterval;

	private Button flyScan;
	private Button extraFlatsAtEnd;

	private Text numFlyScans;
	private Text flyScanDelay;
	private Button closeShutterAfterLastScan;

	private Text detectorToSampleDistance;
	private Combo detectorToSampleDistanceUnits;
	private Text xPixelSize;
	private Combo xPixelSizeUnits;
	private Text yPixelSize;
	private Combo yPixelSizeUnits;
	private Text approxCentreOfRotation;

	public ParametersComposite(Composite parent, int style) {
		super(parent, style);

		loadTomographyOptions();

		// Overall layout is a 3-column grid
		GridLayoutFactory.swtDefaults().numColumns(3).margins(5,0).spacing(5, 0).applyTo(this);

		// Main title
		final Label lblWindowTitle = new Label(this, SWT.CENTER);
		lblWindowTitle.setText("Tomography scan");
		lblWindowTitle.setFont(SWTResourceManager.getFont("Sans", 14, SWT.BOLD));
		GridDataFactory.fillDefaults().span(3, 1).align(SWT.CENTER, SWT.TOP).applyTo(lblWindowTitle);

		createDevicesGrid();
		createScanParametersForm();
		createReconstructionGrid();
	}

	private void loadTomographyOptions() {
		// Read in options for Comboboxes
		final List<TomographyOptions> options = Finder.getInstance().listLocalFindablesOfType(TomographyOptions.class);
		if (options.isEmpty()) {
			logger.error("No TomographyOptions object found: drop-down lists will not be available");
		} else {
			if (options.size() > 1) {
				logger.warn("Multiple TomographyOptions objects found: using " + options.get(0).getName());
			}
			tomographyOptions = options.get(0);
		}

	}

	private void createDevicesGrid() {
		final Group devices = new Group(this, SWT.BORDER);
		devices.setText("Device setup");
		devices.setFont(SWTResourceManager.getFont("Sans", 12, SWT.NORMAL));
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(devices);
		GridLayoutFactory.fillDefaults().numColumns(1).extendedMargins(5, 5, 5, 5).applyTo(devices);

		// Drop-down boxes to allow user to choose stages
		final Group devicesMain = new Group(devices, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(devicesMain);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(true).extendedMargins(5, 5, 5, 5).applyTo(devicesMain);

		// GridData for labels
		GridDataFactory gdLbl = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false);

		// GridData for widgets
		GridDataFactory gdWgt = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).grab(true, false);

		final Label lblRotationStage = new Label(devicesMain, SWT.NONE);
		lblRotationStage.setText("Rotation stage");
		gdLbl.applyTo(lblRotationStage);
		rotationStage = new Combo(devicesMain, SWT.READ_ONLY);
		gdWgt.applyTo(rotationStage);
		rotationStage.setItems(tomographyOptions.getRotationStages());

		final Label lblLinearStage = new Label(devicesMain, SWT.NONE);
		lblLinearStage.setText("Linear stage");
		gdLbl.applyTo(lblLinearStage);
		linearStage = new Combo(devicesMain, SWT.READ_ONLY);
		gdWgt.applyTo(linearStage);
		linearStage.setItems(tomographyOptions.getLinearStages());

		// Check box to allow user to send data to temporary directory
		Group grpOutputDirectory = new Group(devices, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(grpOutputDirectory);
		GridLayoutFactory.fillDefaults().numColumns(1).extendedMargins(5, 5, 5, 5).applyTo(grpOutputDirectory);

		sendDataToTempDirectory = new Button(grpOutputDirectory, SWT.CHECK);
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).grab(true, false).span(1, 1).applyTo(sendDataToTempDirectory);
		sendDataToTempDirectory.setText("Send data to temporary directory");
		sendDataToTempDirectory.setToolTipText("Data sent to the temporary directory will not be archived.\nUse this option for test scans to avoid filling up archive storage.");

		final Label lblOutputDirectory = new Label(grpOutputDirectory, SWT.NONE);
		lblOutputDirectory.setText("Current output directory");

		outputDirectory = new Text(grpOutputDirectory, SWT.WRAP | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().hint(270, SWT.DEFAULT).grab(true, false).applyTo(outputDirectory);
	}

	private void createScanParametersForm() {
		final Group scanParameters = new Group(this, SWT.BORDER);
		scanParameters.setText("Scan parameters");
		scanParameters.setFont(SWTResourceManager.getFont("Sans", 12, SWT.NORMAL));

		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(scanParameters);
		GridLayoutFactory.fillDefaults().numColumns(1).extendedMargins(5, 5, 5, 5).applyTo(scanParameters);

		final Group miscGroup = new Group(scanParameters, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(miscGroup);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(true).extendedMargins(5, 5, 5, 5).applyTo(miscGroup);

		// GridData for labels
		GridDataFactory gdLbl = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.CENTER).grab(true, false);

		// GridData for text widgets
		GridDataFactory gdTxt = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).grab(true, false);


		final Label lblTitle = new Label(miscGroup, SWT.NONE);
		lblTitle.setText("Title");
		gdLbl.applyTo(lblTitle);

		title = new Text(miscGroup, SWT.BORDER);
		gdTxt.applyTo(title);

		final Label lblExposures = new Label(miscGroup, SWT.NONE);
		lblExposures.setText("Exposure/s");
		gdLbl.applyTo(lblExposures);

		exposure = new Text(miscGroup, SWT.BORDER);
		gdTxt.applyTo(exposure);

		final Label lblMinI = new Label(miscGroup, SWT.NONE);
		lblMinI.setText("Min. i");
		gdLbl.applyTo(lblMinI);

		minI = new Text(miscGroup, SWT.BORDER);
		gdTxt.applyTo(minI);

		final Group motorGroup = new Group(scanParameters, SWT.NONE);
		motorGroup.setText("Sample Positions");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(motorGroup);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(true).extendedMargins(5, 5, 5, 5).applyTo(motorGroup);

		final Label lblInBeamX = new Label(motorGroup, SWT.NONE);
		lblInBeamX.setText("In Beam X");
		gdLbl.applyTo(lblInBeamX);

		inBeamPosition = new Text(motorGroup, SWT.BORDER);
		gdTxt.applyTo(inBeamPosition);

		final Label lblOutOfBeam = new Label(motorGroup, SWT.NONE);
		lblOutOfBeam.setText("Out of Beam X");
		gdLbl.applyTo(lblOutOfBeam);

		outBeamPosition = new Text(motorGroup, SWT.BORDER);
		gdTxt.applyTo(outBeamPosition);

		final Group rotationAngleGroup = new Group(scanParameters, SWT.NONE);
		rotationAngleGroup.setText("Rotation Angle");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(rotationAngleGroup);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(true).extendedMargins(5, 5, 5, 5).applyTo(rotationAngleGroup);

		final Label lblStart = new Label(rotationAngleGroup, SWT.NONE);
		lblStart.setText("Start");
		gdLbl.applyTo(lblStart);

		start = new Text(rotationAngleGroup, SWT.BORDER);
		gdTxt.applyTo(start);

		final Label lblStop = new Label(rotationAngleGroup, SWT.NONE);
		lblStop.setText("Stop");
		gdLbl.applyTo(lblStop);

		stop = new Text(rotationAngleGroup, SWT.BORDER);
		gdTxt.applyTo(stop);

		final Label lblStep = new Label(rotationAngleGroup, SWT.NONE);
		lblStep.setText("Step");
		gdLbl.applyTo(lblStep);

		step = new Text(rotationAngleGroup, SWT.BORDER);
		gdTxt.applyTo(step);

		final Group grpDarksFlats = new Group(scanParameters, SWT.NONE);
		grpDarksFlats.setText("Darks && Flats");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(grpDarksFlats);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(true).extendedMargins(5, 5, 5, 5).applyTo(grpDarksFlats);

		final Label lblImagesperDark = new Label(grpDarksFlats, SWT.NONE);
		lblImagesperDark.setText("Images per Dark");
		gdLbl.applyTo(lblImagesperDark);

		imagesPerDark = new Text(grpDarksFlats, SWT.BORDER);
		gdTxt.applyTo(imagesPerDark);

		final Label lblStepsPerDark = new Label(grpDarksFlats, SWT.NONE);
		lblStepsPerDark.setText("Steps per Dark");
		gdLbl.applyTo(lblStepsPerDark);

		darkFieldInterval = new Text(grpDarksFlats, SWT.BORDER);
		gdTxt.applyTo(darkFieldInterval);

		final Label lblImagesPerFlat = new Label(grpDarksFlats, SWT.NONE);
		lblImagesPerFlat.setText("Images per Flat");
		gdLbl.applyTo(lblImagesPerFlat);

		imagesPerFlat = new Text(grpDarksFlats, SWT.BORDER);
		gdTxt.applyTo(imagesPerFlat);

		final Label lblStepsPerFlat = new Label(grpDarksFlats, SWT.NONE);
		lblStepsPerFlat.setText("Steps per Flat");
		gdLbl.applyTo(lblStepsPerFlat);

		flatFieldInterval = new Text(grpDarksFlats, SWT.BORDER);
		gdTxt.applyTo(flatFieldInterval);

		final Group grpFlyScan = new Group(scanParameters, SWT.NONE);
		grpFlyScan.setText("Fly Scan");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(grpFlyScan);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(true).extendedMargins(5, 5, 5, 5).applyTo(grpFlyScan);

		flyScan = new Button(grpFlyScan, SWT.CHECK);
		flyScan.setText("Fly Scan");
		flyScan.setToolTipText("The scan can be performed as a step scan or fly scan");
		gdLbl.applyTo(flyScan);

		extraFlatsAtEnd = new Button(grpFlyScan, SWT.CHECK);
		extraFlatsAtEnd.setText("Extra Flats at End");
		extraFlatsAtEnd.setToolTipText("Collect additional flat images at end of scan");
		gdLbl.applyTo(extraFlatsAtEnd);

		final Group grpMultipleFlyScans = new Group(scanParameters, SWT.NONE);
		grpMultipleFlyScans.setText("Multiple Fly Scans");
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(grpMultipleFlyScans);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(true).extendedMargins(5, 5, 5, 5).applyTo(grpMultipleFlyScans);

		final Label lblNumFlyScans = new Label(grpMultipleFlyScans, SWT.NONE);
		lblNumFlyScans.setText("Number of Fly Scans");
		gdLbl.applyTo(lblNumFlyScans);

		numFlyScans = new Text(grpMultipleFlyScans, SWT.BORDER);
		gdTxt.applyTo(numFlyScans);

		final Label lblFlyScanDelay = new Label(grpMultipleFlyScans, SWT.NONE);
		lblFlyScanDelay.setText("Delay Between Scans/s");
		gdLbl.applyTo(lblFlyScanDelay);

		flyScanDelay = new Text(grpMultipleFlyScans, SWT.BORDER);
		gdTxt.applyTo(flyScanDelay);
		flyScanDelay.setToolTipText("Delay in seconds between multiple fly scans");

		closeShutterAfterLastScan = new Button(grpMultipleFlyScans, SWT.CHECK);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).span(2, 1).applyTo(closeShutterAfterLastScan);
		closeShutterAfterLastScan.setText("Close shutter after last scan");
	}

	private void createReconstructionGrid() {
		final Group reconstruction = new Group(this, SWT.BORDER);
		reconstruction.setText("Reconstruction");
		reconstruction.setFont(SWTResourceManager.getFont("Sans", 12, SWT.NORMAL));
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(reconstruction);
		GridLayoutFactory.fillDefaults().numColumns(1).extendedMargins(5, 5, 5, 5).applyTo(reconstruction);

		final Label reconstructionComment = new Label(reconstruction, SWT.WRAP);
		reconstructionComment.setText("These parameters are recorded in the Nexus scan file\nbut are not mandatory for running the scan.");
		reconstructionComment.setFont(JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT));

		final Group reconstructionMain = new Group(reconstruction, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(reconstructionMain);
		GridLayoutFactory.fillDefaults().numColumns(3).extendedMargins(5, 5, 5, 5).applyTo(reconstructionMain);

		//Prepare a common layout
		GridDataFactory horizGrab = GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).grab(true, false);

		final Label lblDetectorToSampleDist = new Label(reconstructionMain, SWT.NONE);
		lblDetectorToSampleDist.setText("Detector to sample distance");
		detectorToSampleDistance = new Text(reconstructionMain, SWT.BORDER);
		horizGrab.applyTo(detectorToSampleDistance);
		detectorToSampleDistanceUnits = new Combo(reconstructionMain, SWT.READ_ONLY);
		detectorToSampleDistanceUnits.setItems(tomographyOptions.getDetectorToSampleDistanceUnits());
		horizGrab.applyTo(detectorToSampleDistanceUnits);

		final Label lblXPixelSize = new Label(reconstructionMain, SWT.NONE);
		lblXPixelSize.setText("x pixel size");
		xPixelSize = new Text(reconstructionMain, SWT.BORDER);
		horizGrab.applyTo(xPixelSize);
		xPixelSizeUnits = new Combo(reconstructionMain, SWT.READ_ONLY);
		xPixelSizeUnits.setItems(tomographyOptions.getxPixelSizeUnits());
		horizGrab.applyTo(getxPixelSizeUnits());

		final Label lblYPixelSize = new Label(reconstructionMain, SWT.NONE);
		lblYPixelSize.setText("y pixel size");
		yPixelSize = new Text(reconstructionMain, SWT.BORDER);
		horizGrab.applyTo(yPixelSize);
		yPixelSizeUnits = new Combo(reconstructionMain, SWT.READ_ONLY);
		yPixelSizeUnits.setItems(tomographyOptions.getyPixelSizeUnits());
		horizGrab.applyTo(yPixelSizeUnits);

		final Label lblApproxCentreOfRotation = new Label(reconstructionMain, SWT.NONE);
		lblApproxCentreOfRotation.setText("Approx. centre of rotation/px");
		approxCentreOfRotation = new Text(reconstructionMain, SWT.BORDER);
		approxCentreOfRotation.setToolTipText("Approximate centre of rotation (pixel)");
		horizGrab.applyTo(approxCentreOfRotation);
	}

	// Widget getters

	public Button getSendDataToTempDirectory() {
		return sendDataToTempDirectory;
	}

	public Text getOutputDirectory() {
		return outputDirectory;
	}

	public Text getTitle() {
		return title;
	}

	public Text getExposure() {
		return exposure;
	}

	public Text getMinI() {
		return minI;
	}

	public Text getInBeamPosition() {
		return inBeamPosition;
	}

	public Text getOutBeamPosition() {
		return outBeamPosition;
	}

	public Text getStart() {
		return start;
	}

	public Text getStop() {
		return stop;
	}

	public Text getStep() {
		return step;
	}

	public Text getImagesPerDark() {
		return imagesPerDark;
	}

	public Text getDarkFieldInterval() {
		return darkFieldInterval;
	}

	public Text getImagesPerFlat() {
		return imagesPerFlat;
	}

	public Text getFlatFieldInterval() {
		return flatFieldInterval;
	}

	public Button getFlyScan() {
		return flyScan;
	}

	public Button getExtraFlatsAtEnd() {
		return extraFlatsAtEnd;
	}

	public Text getNumFlyScans() {
		return numFlyScans;
	}

	public Text getFlyScanDelay() {
		return flyScanDelay;
	}

	public Button getCloseShutterAfterLastScan() {
		return closeShutterAfterLastScan;
	}

	public Text getDetectorToSampleDistance() {
		return detectorToSampleDistance;
	}

	public Text getxPixelSize() {
		return xPixelSize;
	}

	public Text getyPixelSize() {
		return yPixelSize;
	}

	public Text getApproxCentreOfRotation() {
		return approxCentreOfRotation;
	}

	public Combo getRotationStage() {
		return rotationStage;
	}

	public Combo getLinearStage() {
		return linearStage;
	}

	public Combo getDetectorToSampleDistanceUnits() {
		return detectorToSampleDistanceUnits;
	}

	public Combo getxPixelSizeUnits() {
		return xPixelSizeUnits;
	}

	public Combo getyPixelSizeUnits() {
		return yPixelSizeUnits;
	}
}
