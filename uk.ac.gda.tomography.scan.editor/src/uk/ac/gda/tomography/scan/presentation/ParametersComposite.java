package uk.ac.gda.tomography.scan.presentation;

import java.util.Arrays;
import java.util.List;

import org.eclipse.emf.common.command.Command;
import org.eclipse.emf.common.notify.Notification;
import org.eclipse.emf.ecore.EAttribute;
import org.eclipse.emf.ecore.resource.Resource;
import org.eclipse.emf.ecore.util.EContentAdapter;
import org.eclipse.emf.edit.command.SetCommand;
import org.eclipse.emf.edit.domain.AdapterFactoryEditingDomain;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.DisposeEvent;
import org.eclipse.swt.events.DisposeListener;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.layout.FormAttachment;
import org.eclipse.swt.layout.FormData;
import org.eclipse.swt.layout.FormLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.wb.swt.SWTResourceManager;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.factory.Finder;
import gda.jython.InterfaceProvider;
import uk.ac.gda.tomography.scan.Parameters;
import uk.ac.gda.tomography.scan.ScanPackage;

/**
 * A viewer's model consists of elements, represented by objects. A viewer
 * defines and implements generic infrastructure for handling model input,
 * updates, and selections in terms of elements. Input is obtained by querying
 * an <code>IContentProvider</code> which returns elements. The elements
 * themselves are not displayed directly. They are mapped to labels, containing
 * text and/or an image, using the viewer's <code>ILabelProvider</code>. </p>
 * <p>
 * Implementing a concrete content viewer typically involves the following
 * steps:
 * <ul>
 * <li>
 * create SWT controls for viewer (in constructor) (optional)</li>
 * <li>
 * initialize SWT controls from input (inputChanged)</li>
 * <li>
 * define viewer-specific update methods</li>
 * <li>
 * support selections (<code>setSelection</code>, <code>getSelection</code>)
 * </ul>
 */
public class ParametersComposite extends Composite {

	private static final Logger logger = LoggerFactory.getLogger(ParametersComposite.class);

	private Text start, stop, step;
	private AdapterFactoryEditingDomain editingDomain;
	private EContentAdapter adapter;
	private Text imagesPerDark;
	private Text imagesPerFlat;
	private Text darkFieldInterval;
	private Text flatFieldInterval;
	private Text title;
	private Text exposure;
	private Text inBeamX;
	private Text outBeamX;
	private Text minI;
	private Button flyscan;
	private Button extraFlatsAtEnd;
	private Button closeShutterAfterLastScan;
	private Text numFlyScans;
	private Text flyScanDelay;
	private Text approxCentreOfRotation;
	private Text detectorToSampleDistance;
	private Combo detectorToSampleDistanceUnits;
	private Text xPixelSize;
	private Combo xPixelSizeUnits;
	private Text yPixelSize;
	private Combo yPixelSizeUnits;
	private Combo rotationStage;
	private Combo linearStage;
	private Button sendDataToTempDirectory;
	private Text outputDirectory;
	private String outputDirectoryPath;
	private TomographyOptions tomographyOptions;

	public ParametersComposite(Composite parent) {
		super(parent, SWT.NONE);

		// Read in options for Combo boxes
		final List<TomographyOptions> options = Finder.getInstance().listLocalFindablesOfType(TomographyOptions.class);
		if (options.size() == 0) {
			logger.error("No TomographyOptions object found: drop-down lists will not be available");
		} else {
			if (options.size() > 1) {
				logger.warn("Multiple TomographyOptions objects found: using " + options.get(0).getName());
			}
			tomographyOptions = options.get(0);
		}

		// Overall layout is a 3-column grid
		GridLayoutFactory.swtDefaults().numColumns(3).equalWidth(false).extendedMargins(5, 5, 5, 5).applyTo(this);

		// Main title
		final Label lblWindowTitle = new Label(this, SWT.CENTER);
		lblWindowTitle.setText("Tomography scan");
		lblWindowTitle.setFont(SWTResourceManager.getFont("Sans", 14, SWT.BOLD));
		GridDataFactory.fillDefaults().span(3, 1).applyTo(lblWindowTitle);

		// Current output directory
		outputDirectoryPath = InterfaceProvider.getCommandRunner().evaluateCommand("PathConstructor.createFromDefaultProperty()");

		// Create the individual parts of the window
		createDevicesGrid();
		createScanParametersForm();
		createReconstructionGrid();

		addDisposeListener(new DisposeListener() {

			@Override
			public void widgetDisposed(DisposeEvent e) {
				if( editingDomain != null){
					Resource resource = editingDomain.getResourceSet().getResources().get(0);
					if( adapter != null){
						resource.eAdapters().remove(adapter);
						adapter = null;
					}
					editingDomain = null;
				}
			}
		});
	}

	private void createDevicesGrid() {
		final Group devices = new Group(this, SWT.BORDER);
		devices.setText("Device setup");
		devices.setFont(SWTResourceManager.getFont("Sans", 12, SWT.NORMAL));
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.FILL).grab(true, false).applyTo(devices);
		GridLayoutFactory.fillDefaults().numColumns(1).extendedMargins(5, 5, 5, 5).applyTo(devices);

		// Drop-down boxes to allow user to choose stages
		final Group devicesMain = new Group(devices, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.FILL).grab(true, false).applyTo(devicesMain);
		GridLayoutFactory.fillDefaults().numColumns(2).extendedMargins(5, 5, 5, 5).applyTo(devicesMain);

		final Label lblRotationStage = new Label(devicesMain, SWT.NONE);
		lblRotationStage.setText("Rotation stage");
		rotationStage = new Combo(devicesMain, SWT.READ_ONLY);
		rotationStage.setItems(tomographyOptions.getRotationStages());

		final Label lblLinearStage = new Label(devicesMain, SWT.NONE);
		lblLinearStage.setText("Linear stage");
		linearStage = new Combo(devicesMain, SWT.READ_ONLY);
		linearStage.setItems(tomographyOptions.getLinearStages());

		// Check box to allow user to send data to temporary directory
		final Group grpOutputDirectory = new Group(devices, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.FILL).grab(true, false).applyTo(grpOutputDirectory);
		GridLayoutFactory.fillDefaults().numColumns(1).extendedMargins(5, 5, 5, 5).applyTo(grpOutputDirectory);

		sendDataToTempDirectory = new Button(grpOutputDirectory, SWT.CHECK);
		GridDataFactory.fillDefaults().align(SWT.LEFT, SWT.CENTER).grab(true, false).span(1, 1).applyTo(sendDataToTempDirectory);
		sendDataToTempDirectory.setText("Send data to temporary directory");
		sendDataToTempDirectory.setToolTipText("Data sent to the temporary directory will not be archived.\nUse this option for test scans to avoid filling up archive storage.");

		final Label lblOutputDirectory = new Label(grpOutputDirectory, SWT.NONE);
		lblOutputDirectory.setText("Current output directory");

		outputDirectory = new Text(grpOutputDirectory, SWT.WRAP | SWT.READ_ONLY);
		GridDataFactory.fillDefaults().hint(250, SWT.DEFAULT).grab(true, false).applyTo(outputDirectory);

		// -----------------------------------------------------------------------------------

		addComboToTextListeners(rotationStage, ScanPackage.eINSTANCE.getParameters_RotationStage());
		addComboToTextListeners(linearStage, ScanPackage.eINSTANCE.getParameters_LinearStage());
		addButtonSelectionListeners(sendDataToTempDirectory, ScanPackage.eINSTANCE.getParameters_SendDataToTemporaryDirectory());
	}

	private void createReconstructionGrid() {
		final Group reconstruction = new Group(this, SWT.BORDER);
		reconstruction.setText("Reconstruction");
		reconstruction.setFont(SWTResourceManager.getFont("Sans", 12, SWT.NORMAL));
		GridDataFactory.fillDefaults().align(SWT.CENTER, SWT.FILL).grab(true, false).applyTo(reconstruction);
		GridLayoutFactory.fillDefaults().numColumns(1).extendedMargins(5, 5, 5, 5).applyTo(reconstruction);

		final Label reconstructionComment = new Label(reconstruction, SWT.BORDER | SWT.WRAP);
		reconstructionComment.setText("These parameters are recorded in the Nexus scan file\nbut are not mandatory for running the scan.");
		reconstructionComment.setFont(JFaceResources.getFontRegistry().getItalic(JFaceResources.DEFAULT_FONT));

		final Group reconstructionMain = new Group(reconstruction, SWT.BORDER);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, false).applyTo(reconstructionMain);
		GridLayoutFactory.fillDefaults().numColumns(3).extendedMargins(5, 5, 5, 5).applyTo(reconstructionMain);

		final Label lblDetectorToSampleDist = new Label(reconstructionMain, SWT.NONE);
		lblDetectorToSampleDist.setText("Detector to sample distance");
		detectorToSampleDistance = new Text(reconstructionMain, SWT.BORDER);
		detectorToSampleDistance.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		detectorToSampleDistanceUnits = new Combo(reconstructionMain, SWT.READ_ONLY);
		detectorToSampleDistanceUnits.setItems(tomographyOptions.getDetectorToSampleDistanceUnits());

		final Label lblXPixelSize = new Label(reconstructionMain, SWT.NONE);
		lblXPixelSize.setText("x pixel size");
		xPixelSize = new Text(reconstructionMain, SWT.BORDER);
		xPixelSize.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		xPixelSizeUnits = new Combo(reconstructionMain, SWT.READ_ONLY);
		xPixelSizeUnits.setItems(tomographyOptions.getxPixelSizeUnits());

		final Label lblYPixelSize = new Label(reconstructionMain, SWT.NONE);
		lblYPixelSize.setText("y pixel size");
		yPixelSize = new Text(reconstructionMain, SWT.BORDER);
		yPixelSize.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		yPixelSizeUnits = new Combo(reconstructionMain, SWT.READ_ONLY);
		yPixelSizeUnits.setItems(tomographyOptions.getyPixelSizeUnits());

		final Label lblApproxCentreOfRotation = new Label(reconstructionMain, SWT.NONE);
		lblApproxCentreOfRotation.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblApproxCentreOfRotation.setText("Approx. centre of rotation/px");
		approxCentreOfRotation = new Text(reconstructionMain, SWT.BORDER);
		approxCentreOfRotation.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		approxCentreOfRotation.setToolTipText("Approximate centre of rotation (pixel)");

		addTextToTextListeners(approxCentreOfRotation, ScanPackage.eINSTANCE.getParameters_ApproxCentreOfRotation());
		addTextToTextListeners(detectorToSampleDistance, ScanPackage.eINSTANCE.getParameters_DetectorToSampleDistance());
		addComboToTextListeners(detectorToSampleDistanceUnits, ScanPackage.eINSTANCE.getParameters_DetectorToSampleDistanceUnits());
		addTextToTextListeners(xPixelSize, ScanPackage.eINSTANCE.getParameters_XPixelSize());
		addComboToTextListeners(xPixelSizeUnits, ScanPackage.eINSTANCE.getParameters_XPixelSizeUnits());
		addTextToTextListeners(yPixelSize, ScanPackage.eINSTANCE.getParameters_YPixelSize());
		addComboToTextListeners(yPixelSizeUnits, ScanPackage.eINSTANCE.getParameters_YPixelSizeUnits());
	}

	private void createScanParametersForm() {
		// Scan parameters
		final Group scanParameters = new Group(this, SWT.BORDER);
		scanParameters.setLayoutData(new GridData(SWT.CENTER, SWT.BEGINNING, true, false));
		scanParameters.setText("Scan parameters");
		scanParameters.setFont(SWTResourceManager.getFont("Sans", 12, SWT.NORMAL));

		final FormLayout scanParametersLayout = new FormLayout();
		scanParametersLayout.spacing = 5;
		scanParametersLayout.marginWidth = 5;
		scanParametersLayout.marginTop = 5;
		scanParametersLayout.marginRight = 5;
		scanParametersLayout.marginLeft = 5;
		scanParametersLayout.marginHeight = 5;
		scanParametersLayout.marginBottom = 5;
		scanParameters.setLayout(scanParametersLayout);

		final Group miscGroup = new Group(scanParameters, SWT.NONE);
		final FormData fd_miscGroup = new FormData();
		fd_miscGroup.right = new FormAttachment(100);
		fd_miscGroup.left = new FormAttachment(0);
		miscGroup.setLayoutData(fd_miscGroup);
		miscGroup.setLayout(new GridLayout(2, false));

		final Label lblTitle = new Label(miscGroup, SWT.NONE);
		lblTitle.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblTitle.setText("Title");

		title = new Text(miscGroup, SWT.BORDER);
		title.setLayoutData(new GridData(SWT.FILL, SWT.TOP, true, false, 1, 1));

		final Label lblExposures = new Label(miscGroup, SWT.NONE);
		lblExposures.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblExposures.setText("Exposure/s");

		exposure = new Text(miscGroup, SWT.BORDER);
		exposure.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		final Label lblMinI = new Label(miscGroup, SWT.NONE);
		lblMinI.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblMinI.setText("Min. i");

		minI = new Text(miscGroup, SWT.BORDER);
		minI.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		final Group motorGroup = new Group(scanParameters, SWT.NONE);
		motorGroup.setText("Sample Positions");
		final FormData fd_motorGroup = new FormData();
		fd_motorGroup.top = new FormAttachment(miscGroup);
		fd_motorGroup.right = new FormAttachment(100);
		fd_motorGroup.left = new FormAttachment(0);
		motorGroup.setLayoutData(fd_motorGroup);
		motorGroup.setLayout(new GridLayout(2, false));

		final Label lblInBeamX = new Label(motorGroup, SWT.NONE);
		lblInBeamX.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblInBeamX.setText("In Beam X");

		inBeamX = new Text(motorGroup, SWT.BORDER);
		inBeamX.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		final Label lblOutOfBeam = new Label(motorGroup, SWT.NONE);
		lblOutOfBeam.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblOutOfBeam.setText("Out of Beam X");

		outBeamX = new Text(motorGroup, SWT.BORDER);
		outBeamX.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		final Group rotationAngleGroup = new Group(scanParameters, SWT.NONE);
		final FormData fd_rotationAngleGroup = new FormData();
		fd_rotationAngleGroup.top = new FormAttachment(motorGroup);
		fd_rotationAngleGroup.right = new FormAttachment(100);
		fd_rotationAngleGroup.left = new FormAttachment(0);
		rotationAngleGroup.setLayoutData(fd_rotationAngleGroup);
		rotationAngleGroup.setText("Rotation Angle");
		rotationAngleGroup.setLayout(new GridLayout(2, false));

		final Label lblStart = new Label(rotationAngleGroup, SWT.NONE);
		lblStart.setText("Start");
		lblStart.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		start = new Text(rotationAngleGroup, SWT.BORDER);
		start.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		final Label lblStop = new Label(rotationAngleGroup, SWT.NONE);
		lblStop.setText("Stop");
		lblStop.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		stop = new Text(rotationAngleGroup, SWT.BORDER);
		stop.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		final Label lblStep = new Label(rotationAngleGroup, SWT.NONE);
		lblStep.setText("Step");
		lblStep.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		step = new Text(rotationAngleGroup, SWT.BORDER);
		step.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		final Group grpDarksFlats = new Group(scanParameters, SWT.NONE);
		grpDarksFlats.setLayout(new GridLayout(2, false));
		final FormData fd_grpDarksFlats = new FormData();
		fd_grpDarksFlats.top = new FormAttachment(rotationAngleGroup);
		fd_grpDarksFlats.right = new FormAttachment(100);
		fd_grpDarksFlats.left = new FormAttachment(0);
		grpDarksFlats.setLayoutData(fd_grpDarksFlats);
		grpDarksFlats.setText("Darks && Flats");

		final Label lblImagesperDark = new Label(grpDarksFlats, SWT.NONE);
		lblImagesperDark.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblImagesperDark.setText("Images per Dark");

		imagesPerDark = new Text(grpDarksFlats, SWT.BORDER);
		imagesPerDark.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		final Label lblStepsPerDark = new Label(grpDarksFlats, SWT.NONE);
		lblStepsPerDark.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblStepsPerDark.setText("Steps per Dark");

		darkFieldInterval = new Text(grpDarksFlats, SWT.BORDER);
		darkFieldInterval.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		final Label lblImagesPerFlat = new Label(grpDarksFlats, SWT.NONE);
		lblImagesPerFlat.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblImagesPerFlat.setText("Images per Flat");

		imagesPerFlat = new Text(grpDarksFlats, SWT.BORDER);
		imagesPerFlat.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		final Label lblStepsPerFlat = new Label(grpDarksFlats, SWT.NONE);
		lblStepsPerFlat.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblStepsPerFlat.setText("Steps per Flat");

		flatFieldInterval = new Text(grpDarksFlats, SWT.BORDER);
		flatFieldInterval.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		final Group grpFlyScan = new Group(scanParameters, SWT.NONE);
		grpFlyScan.setLayout(new GridLayout(2, false));
		final FormData fd_grpFlyScan = new FormData();
		fd_grpFlyScan.top = new FormAttachment(grpDarksFlats);
		fd_grpFlyScan.right = new FormAttachment(100);
		fd_grpFlyScan.left = new FormAttachment(0);
		grpFlyScan.setLayoutData(fd_grpFlyScan);
		grpFlyScan.setText("Fly Scan");

		flyscan = new Button(grpFlyScan, SWT.CHECK);
		flyscan.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, true, false, 1, 1));
		flyscan.setText("Fly Scan");
		flyscan.setToolTipText("The scan can be performed as a step scan or fly scan");

		extraFlatsAtEnd = new Button(grpFlyScan, SWT.CHECK);
		extraFlatsAtEnd.setLayoutData(new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1));
		extraFlatsAtEnd.setText("Extra Flats at End");
		extraFlatsAtEnd.setToolTipText("Collect additional flat images at end of scan");

		final Group grpMultipleFlyScans = new Group(scanParameters, SWT.NONE);
		grpMultipleFlyScans.setLayout(new GridLayout(2, false));
		final FormData fd_grpMultipleFlyScans = new FormData();
		fd_grpMultipleFlyScans.top = new FormAttachment(grpFlyScan);
		fd_grpMultipleFlyScans.right = new FormAttachment(100);
		fd_grpMultipleFlyScans.left = new FormAttachment(0);
		grpMultipleFlyScans.setLayoutData(fd_grpMultipleFlyScans);
		grpMultipleFlyScans.setText("Multiple Fly Scans");

		final Label lblNumFlyScans = new Label(grpMultipleFlyScans, SWT.NONE);
		lblNumFlyScans.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblNumFlyScans.setText("Number of Fly Scans");

		numFlyScans = new Text(grpMultipleFlyScans, SWT.BORDER);
		numFlyScans.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));

		final Label lblFlyScanDelay = new Label(grpMultipleFlyScans, SWT.NONE);
		lblFlyScanDelay.setLayoutData(new GridData(SWT.RIGHT, SWT.CENTER, false, false, 1, 1));
		lblFlyScanDelay.setText("Delay Between Scans/s");

		flyScanDelay = new Text(grpMultipleFlyScans, SWT.BORDER);
		flyScanDelay.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false, 1, 1));
		flyScanDelay.setToolTipText("Delay in seconds between multiple fly scans");

		closeShutterAfterLastScan = new Button(grpMultipleFlyScans, SWT.CHECK);
		GridDataFactory.fillDefaults().align(SWT.RIGHT, SWT.CENTER).grab(true, false).span(2, 1).applyTo(closeShutterAfterLastScan);
		closeShutterAfterLastScan.setText("Close shutter after last scan");

		// -----------------------------------------------------------------------------------

		addTextToTextListeners(title, ScanPackage.eINSTANCE.getParameters_Title());
		addTextToDoubleListeners(exposure, ScanPackage.eINSTANCE.getParameters_ExposureTime());
		addTextToDoubleListeners(minI, ScanPackage.eINSTANCE.getParameters_MinI());

		addTextToDoubleListeners(inBeamX, ScanPackage.eINSTANCE.getParameters_InBeamPosition());
		addTextToDoubleListeners(outBeamX, ScanPackage.eINSTANCE.getParameters_OutOfBeamPosition());

		addTextToIntegerListeners(imagesPerDark, ScanPackage.eINSTANCE.getParameters_ImagesPerDark());
		addTextToIntegerListeners(darkFieldInterval, ScanPackage.eINSTANCE.getParameters_DarkFieldInterval());
		addTextToIntegerListeners(imagesPerFlat, ScanPackage.eINSTANCE.getParameters_ImagesPerFlat());
		addTextToIntegerListeners(flatFieldInterval, ScanPackage.eINSTANCE.getParameters_FlatFieldInterval());

		addTextToDoubleListeners(start, ScanPackage.eINSTANCE.getParameters_Start());
		addTextToDoubleListeners(stop, ScanPackage.eINSTANCE.getParameters_Stop());
		addTextToDoubleListeners(step, ScanPackage.eINSTANCE.getParameters_Step());

		addButtonSelectionListeners(flyscan, ScanPackage.eINSTANCE.getParameters_FlyScan());
		addButtonSelectionListeners(extraFlatsAtEnd, ScanPackage.eINSTANCE.getParameters_ExtraFlatsAtEnd());

		addTextToIntegerListeners(numFlyScans, ScanPackage.eINSTANCE.getParameters_NumFlyScans());
		addTextToDoubleListeners(flyScanDelay, ScanPackage.eINSTANCE.getParameters_FlyScanDelay());
		addButtonSelectionListeners(extraFlatsAtEnd, ScanPackage.eINSTANCE.getParameters_ExtraFlatsAtEnd());
		addButtonSelectionListeners(closeShutterAfterLastScan, ScanPackage.eINSTANCE.getParameters_CloseShutterAfterLastScan());
	}

	private void addButtonSelectionListeners(final Button btn, final EAttribute eAttribute) {
		btn.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				addModelUpdateCommand(eAttribute,btn.getSelection());
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				addModelUpdateCommand(eAttribute,btn.getSelection());
			}
		});

		btn.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				addModelUpdateCommand(eAttribute,btn.getSelection());
			}
		});
	}

	private void addTextToDoubleListeners(final Text text, final EAttribute eAttribute) {
		text.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				addModelUpdateCommand(eAttribute,Double.valueOf(text.getText()));
			}
		});

		text.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				addModelUpdateCommand(eAttribute,Double.valueOf(text.getText()));
			}
		});
	}

	private void addTextToTextListeners(final Text text, final EAttribute eAttribute) {
		text.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				addModelUpdateCommand(eAttribute,text.getText());
			}
		});

		text.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				addModelUpdateCommand(eAttribute,text.getText());
			}
		});
	}

	private void addTextToIntegerListeners(final Text text, final EAttribute eAttribute) {
		text.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				addModelUpdateCommand(eAttribute,Integer.valueOf(text.getText()));
			}
		});

		text.addFocusListener(new FocusAdapter() {

			@Override
			public void focusLost(FocusEvent e) {
				addModelUpdateCommand(eAttribute,Integer.valueOf(text.getText()));
			}
		});
	}

	private void addComboToTextListeners(final Combo combo, final EAttribute eAttribute) {
		combo.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				addModelUpdateCommand(eAttribute, combo.getText());
			}
		});

		combo.addFocusListener(new FocusAdapter() {
			@Override
			public void focusLost(FocusEvent e) {
				addModelUpdateCommand(eAttribute, combo.getText());
			}
		});
	}

	private void addModelUpdateCommand(Object itemToBeUpdated, Object newValue) {
		Command setCommand = SetCommand.create(editingDomain,
				editingDomain.getResourceSet().getResources().get(0)
						.getContents().get(0),
				itemToBeUpdated, newValue
				);
		editingDomain.getCommandStack().execute(setCommand);
	}

	public void setInput(AdapterFactoryEditingDomain editingDomain) {
		if (this.editingDomain != null)
			throw new IllegalStateException(
					"setInput should only be called once");
		this.editingDomain = editingDomain;
		final Resource resource = editingDomain.getResourceSet().getResources().get(0);
		final Parameters params = (Parameters) (resource.getContents().get(0));

		// Device setup
		setComboText(rotationStage, params.getRotationStage());
		setComboText(linearStage, params.getLinearStage());

		sendDataToTempDirectory.setSelection(params.getSendDataToTemporaryDirectory());
		updateOutputDirectory();

		// Scan parameters
		title.setText(params.getTitle());
		exposure.setText(Double.toString(params.getExposureTime()));
		minI.setText(Double.toString(params.getMinI()));

		start.setText(Double.toString(params.getStart()));
		stop.setText(Double.toString(params.getStop()));
		step.setText(Double.toString(params.getStep()));

		outBeamX.setText(Double.toString(params.getOutOfBeamPosition()));
		inBeamX.setText(Double.toString(params.getInBeamPosition()));

		imagesPerDark.setText(Integer.toString(params.getImagesPerDark()));
		darkFieldInterval.setText(Integer.toString(params.getDarkFieldInterval()));
		imagesPerFlat.setText(Integer.toString(params.getImagesPerFlat()));
		flatFieldInterval.setText(Integer.toString(params.getFlatFieldInterval()));

		flyscan.setSelection(params.isFlyScan());
		extraFlatsAtEnd.setSelection(params.getExtraFlatsAtEnd());

		numFlyScans.setText(Integer.toString(params.getNumFlyScans()));
		flyScanDelay.setText(Double.toString(params.getFlyScanDelay()));
		closeShutterAfterLastScan.setSelection(params.getCloseShutterAfterLastScan());

		// Reconstruction
		approxCentreOfRotation.setText(params.getApproxCentreOfRotation());
		detectorToSampleDistance.setText(params.getDetectorToSampleDistance());
		setComboText(detectorToSampleDistanceUnits, params.getDetectorToSampleDistanceUnits());
		xPixelSize.setText(params.getXPixelSize());
		setComboText(xPixelSizeUnits, params.getXPixelSizeUnits());
		yPixelSize.setText(params.getYPixelSize());
		setComboText(yPixelSizeUnits, params.getYPixelSizeUnits());

		adapter = new EContentAdapter() {

			@Override
			public void notifyChanged(Notification notification) {
				super.notifyChanged(notification);
				Object notifier = notification.getNotifier();
				if (notifier instanceof Parameters) {
					Object feature = notification.getFeature();
					if( feature != null){
						Parameters parameters = (Parameters) notifier;
						if( feature.equals(ScanPackage.eINSTANCE.getParameters_Start())){
							start.setText(Double.toString(parameters.getStart()));
						} else if( feature.equals(ScanPackage.eINSTANCE.getParameters_Stop())){
							stop.setText(Double.toString(parameters.getStop()));
						} else if( feature.equals(ScanPackage.eINSTANCE.getParameters_Step())){
							step.setText(Double.toString(parameters.getStep()));
						} else if( feature.equals(ScanPackage.eINSTANCE.getParameters_Title())){
							title.setText(parameters.getTitle());
						} else if( feature.equals(ScanPackage.eINSTANCE.getParameters_ExposureTime())){
							exposure.setText(Double.toString(parameters.getExposureTime()));
						} else if( feature.equals(ScanPackage.eINSTANCE.getParameters_MinI())){
							minI.setText(Double.toString(parameters.getMinI()));
						} else if( feature.equals(ScanPackage.eINSTANCE.getParameters_OutOfBeamPosition())){
							outBeamX.setText(Double.toString(parameters.getOutOfBeamPosition()));
						} else if( feature.equals(ScanPackage.eINSTANCE.getParameters_InBeamPosition())){
							inBeamX.setText(Double.toString(parameters.getInBeamPosition()));
						} else if( feature.equals(ScanPackage.eINSTANCE.getParameters_ImagesPerDark())){
							imagesPerDark.setText(Integer.toString(parameters.getImagesPerDark()));
						} else if( feature.equals(ScanPackage.eINSTANCE.getParameters_DarkFieldInterval())){
							darkFieldInterval.setText(Integer.toString(parameters.getDarkFieldInterval()));
						} else if( feature.equals(ScanPackage.eINSTANCE.getParameters_ImagesPerFlat())){
							imagesPerFlat.setText(Integer.toString(parameters.getImagesPerFlat()));
						} else if( feature.equals(ScanPackage.eINSTANCE.getParameters_FlatFieldInterval())){
							flatFieldInterval.setText(Integer.toString(parameters.getFlatFieldInterval()));
						} else if( feature.equals(ScanPackage.eINSTANCE.getParameters_FlyScan())){
							flyscan.setSelection(parameters.isFlyScan());
						} else if (feature.equals(ScanPackage.eINSTANCE.getParameters_ExtraFlatsAtEnd())) {
							extraFlatsAtEnd.setSelection(parameters.getExtraFlatsAtEnd());
						} else if (feature.equals(ScanPackage.eINSTANCE.getParameters_NumFlyScans())) {
							numFlyScans.setText(Integer.toString(parameters.getNumFlyScans()));
						} else if (feature.equals(ScanPackage.eINSTANCE.getParameters_FlyScanDelay())) {
							flyScanDelay.setText(Double.toString(parameters.getFlyScanDelay()));
						} else if (feature.equals(ScanPackage.eINSTANCE.getParameters_ApproxCentreOfRotation())) {
							approxCentreOfRotation.setText(parameters.getApproxCentreOfRotation());
						} else if (feature.equals(ScanPackage.eINSTANCE.getParameters_DetectorToSampleDistance())) {
							detectorToSampleDistance.setText(parameters.getDetectorToSampleDistance());
						} else if (feature.equals(ScanPackage.eINSTANCE.getParameters_DetectorToSampleDistanceUnits())) {
							detectorToSampleDistanceUnits.setText(parameters.getDetectorToSampleDistanceUnits());
						} else if (feature.equals(ScanPackage.eINSTANCE.getParameters_XPixelSize())) {
							xPixelSize.setText(parameters.getXPixelSize());
						} else if (feature.equals(ScanPackage.eINSTANCE.getParameters_XPixelSizeUnits())) {
							xPixelSizeUnits.setText(parameters.getXPixelSizeUnits());
						} else if (feature.equals(ScanPackage.eINSTANCE.getParameters_YPixelSize())) {
							yPixelSize.setText(parameters.getYPixelSize());
						} else if (feature.equals(ScanPackage.eINSTANCE.getParameters_YPixelSizeUnits())) {
							yPixelSizeUnits.setText(parameters.getYPixelSizeUnits());
						} else if (feature.equals(ScanPackage.eINSTANCE.getParameters_RotationStage())) {
							rotationStage.setText(parameters.getRotationStage());
						} else if (feature.equals(ScanPackage.eINSTANCE.getParameters_LinearStage())) {
							linearStage.setText(parameters.getLinearStage());
						} else if (feature.equals(ScanPackage.eINSTANCE.getParameters_CloseShutterAfterLastScan())) {
							closeShutterAfterLastScan.setSelection(parameters.getCloseShutterAfterLastScan());
						} else if (feature.equals(ScanPackage.eINSTANCE.getParameters_SendDataToTemporaryDirectory())) {
							sendDataToTempDirectory.setSelection(parameters.getSendDataToTemporaryDirectory());
							updateOutputDirectory();
						}
					}
				}
			}
		};
		resource.eAdapters().add(adapter);
	}

	// Update the display of the the output directory.
	// This is for the user's information only: the actual output directory is determined
	// by the tomographyScan.ProcessScanParameters() script.
	private void updateOutputDirectory() {
		final String subdir = sendDataToTempDirectory.getSelection() ? "tmp" : "raw";
		outputDirectory.setText(outputDirectoryPath + "/" + subdir);
	}

	// Set value of combo box if it is one of the allowed values.
	// Otherwise, warn and set blank.
	private void setComboText(final Combo combo, final String value) {
		if (!value.isEmpty()) {
			if (Arrays.asList(combo.getItems()).contains(value)) {
				combo.setText(value);
			} else {
				logger.warn(String.format("Cannot set value %s: not one of the allowed values. Setting to blank instead", value));
				combo.setText("");
			}
		}
	}
}
