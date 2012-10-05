/*-
 * Copyright © 2011 Diamond Light Source Ltd.
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

package uk.ac.gda.client.tomo.composites;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.resource.FontRegistry;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.StackLayout;
import org.eclipse.swt.events.FocusAdapter;
import org.eclipse.swt.events.FocusEvent;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.KeyAdapter;
import org.eclipse.swt.events.KeyEvent;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.ImageConstants;
import uk.ac.gda.client.tomo.TomoClientActivator;
import uk.ac.gda.client.tomo.composites.MotionControlComposite.MotionControlCentring;
import uk.ac.gda.client.tomo.composites.MotionControlComposite.SAMPLE_WEIGHT;
import uk.ac.gda.client.tomo.composites.ZoomButtonComposite.ZOOM_LEVEL;

/**
 * Camera controls composite - composite include
 * <p>
 * <li>exposure time</li>
 * <li>enable streaming</li>
 * <li>demand raw images</li>
 * <li>instance of zoom button composite</li>
 * <li>Saturation</li>
 * <li>Cross hair</li>
 * <li>Flat images - take, show, correction</li>
 * <li>Dark images - take</li>
 * <li>enable Profiling</li>
 * <p>
 */
public class CameraControlComposite extends Composite {
	private static final String CAMERA_DISTANCE_lbl = "Camera distance \r  (mm)";
	private static final String ERR_PROBLEM_SETTING_SAMPLE_DESCRIPTION = "Problem setting sample Description";
	private static final String BLANK_STR = "";
	private static final String SAVE_ALIGNMENT = "Save Alignment";
	private static final String FLAT_EXP_TIME_CAPTURED_shortmsg = "Exposure time \r %.3g (s)";
	private static final String FLAT_AND_DARK_UNAVAILABLE_shortdesc = "Flat and Dark Images have not been captured. Click 'Take Flat && Dark' to capture Flat and dark images";
	private static final String FOR_FUTURE_USE = "For Future Use";
	private static final String X_RAY_ENERGY_lbl = "X-ray energy \r (keV)";
	private static final String ENERGY_DEFAULT_VALUE = "112";
	private static final String ALIGN_TILT_lbl = "Align Tilt";
	private static final String CAMERA_DIST_DEFAULT_VALUE = "341";

	public enum STREAM_STATE {
		FLAT_STREAM, SAMPLE_STREAM, NO_STREAM;
	}

	public enum RESOLUTION {
		FULL(RESOLUTION_FULL, 0), TWO_X(RESOLUTION_2x, 1), FOUR_X(RESOLUTION_4x, 2), EIGHT_X(RESOLUTION_8x, 3);

		private final String value;
		private final int index;

		@Override
		public String toString() {
			return value;
		}

		private RESOLUTION(String value, int index) {
			this.value = value;
			this.index = index;
		}

		public static RESOLUTION get(String resolution) {
			for (RESOLUTION res : values()) {
				if (resolution.equals(res.toString())) {
					return res;
				}
			}
			return null;
		}

		public int getValue() {
			return index;
		}
	}

	// logger
	private static final Logger logger = LoggerFactory.getLogger(CameraControlComposite.class);
	// Labels
	private static final String SAVE_A_FLAT_AND_DARK_IMAGE_tooltip = "Save a flat and dark image";
	private static final String TAKE_F_AND_D = "Take Flat && Dark";
	private static final String SAMPLE_OUT = "Sample OUT";
	private static final String SAMPLE_IN = "Sample IN";
	private static final String TYPE_DESCRIPTION = "type description";
	private static final String HISTOGRAM = "Histogram";
	private static final String SINGLE = "Single";
	private static final String STREAM = "Stream";
	private static final String FLAT = "Flat";
	private static final String SAMPLE = "Sample";
	private static final String SHOW_FLAT = "Show Flat";
	private static final String RESET_ROI = "Remove";
	private static final String DEFINE_ROI = "Define";
	private static final String PROFILE = "Profile";
	private static final String SATURATION = "Saturation";
	private static final String FRAMES_PER_PROJECTION = "Frames per Projection";
	private static final String RESOLUTION_8x = "8x";
	private static final String RESOLUTION_4x = "4x";
	private static final String RESOLUTION_2x = "2x";
	private static final String RESOLUTION_FULL = "Full";
	private static final String SHOW_DARK = "Show Dark";
	private static final String CORRECT_FLAT_DARK = "Correct Flat && Dark";
	private static final String FRAMES_PER_PROJECTION_DEFAULT_VAL = "1";
	// Error Descriptions
	private static final String ERR_PROFILING = "Error while profiling image";
	private static final String ERR_WHILE_SATURATION = "Error while saturation";
	private static final String ERR_CAPTURING_RAW = "Problem capturing a raw image";
	private static final String ERR_START_STREAM = "Error while starting stream";
	private static final String INVALID_EXPOSURE_TIME_shortdesc = "Exposure time entered is invalid";
	private static final String INVALID_VALUE = "Invalid value";
	private static final String ERROR_UPDATING_EXPOSURE_TIME = "Error updating exposure time";
	// Default values
	private static final String EXP_TIME_MEASURE = " s";
	private static final String DEFAULT_EXP_TIME = "1.0";
	// status messages

	private double flatCapturedExposureTime = -1;
	// Fonts
	private FontRegistry fontRegistry;
	private static final String NORMAL_TEXT_8 = "normal_text_8";
	private static final String NORMAL_TEXT_9 = "normal_text_9";
	private static final String NORMAL_TEXT_7 = "normal_text_7";
	private static final String BOLD_TEXT_11 = "bold_text_11";

	//
	private double flatExposureTime;
	private double sampleExposureTime;
	private int framesPerProjection = Integer.parseInt(FRAMES_PER_PROJECTION_DEFAULT_VAL);

	private String sampleDescription;

	private STREAM_STATE streamState = STREAM_STATE.NO_STREAM;
	private RESOLUTION resolution = RESOLUTION.FULL;

	public synchronized void setStreamState(STREAM_STATE streamState) {
		this.streamState = streamState;
	}

	/* Control Box Buttons */
	// Set for sample
	private Text txtSampleExposureTime;
	private Button btnSampleStream;
	private Button btnSampleSingle;
	private Button btnSampleToFlat;
	private Button btnSampleHistogram;
	// Set for flat
	private Text txtFlatExpTime;
	private Button btnFlatStream;
	private Button btnFlatSingle;
	private Button btnFlatHistogram;

	//
	private Button btnFlatCorrection;
	private Button btnFlatShow;
	//
	private Button btnDarkShow;
	//
	private Button btnSaturation;
	private Button btnProfile;
	//
	private Button btnTakeFlatAndDark;
	//
	private Button btnSampleIn;
	private Button btnSampleOut;

	private ZoomButtonComposite zoomComposite;
	//
	private Button btnFlatToSample;

	private Button btnDefineRoi;
	private Button btnResetRoi;
	//
	private Button btnResFull;
	private Button btnRes2x;
	private Button btnRes4x;
	private Button btnRes8x;

	private Text txtSampleDesc;
	private Text txtResFramesPerProjection;

	private Composite flatDarkComposite;
	private StackLayout flatAndDarkCompositeStackLayout;
	private Composite flatAndDarkContainerComposite;
	private Label lblFlatDarkNotAvailable;

	private Label lblFlatExpTime;
	private Button btnSaveAlignment;

	private Label lblObjectPixelSize;

	/**
	 * @param parent
	 * @param toolkit
	 * @param style
	 */
	public CameraControlComposite(Composite parent, FormToolkit toolkit, int style) {
		super(parent, style);
		initializeFontRegistry();

		GridLayout layout = new GridLayout(8, true);
		layout.horizontalSpacing = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 2;
		this.setLayout(layout);
		this.setBackground(ColorConstants.black);

		// 1
		Composite expStreamSingleHistoComposite = createExpStreamSingleHistoComposite(toolkit, this);
		GridData ld = new GridData(GridData.FILL_BOTH);
		expStreamSingleHistoComposite.setLayoutData(ld);

		//
		Composite beamlineControlComposite = createBeamlineControlComposite(this, toolkit);
		ld = new GridData(GridData.FILL_BOTH);
		beamlineControlComposite.setLayoutData(ld);

		//
		Composite instrumentControlComposite = toolkit.createComposite(this);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.horizontalSpan = 6;
		instrumentControlComposite.setLayoutData(layoutData);
		instrumentControlComposite.setLayout(new FillLayout());

		createExperimentInstrument(instrumentControlComposite, toolkit);

		// setResolution(RESOLUTION.FULL);
	}

	private Composite createSaveBtnComposite(FormToolkit toolkit, Composite parent) {
		Composite saveBtnComposite = toolkit.createComposite(parent);

		saveBtnComposite.setLayout(getGridLayoutZeroSetting());

		btnSaveAlignment = toolkit.createButton(saveBtnComposite, SAVE_ALIGNMENT, SWT.PUSH | SWT.WRAP);
		btnSaveAlignment.setLayoutData(new GridData(GridData.FILL_BOTH));
		btnSaveAlignment.addSelectionListener(buttonSelectionListener);
		//
		return saveBtnComposite;
	}

	private Composite createResolutionComposite(FormToolkit toolkit, Composite parent) {
		Composite resolutionComposite = toolkit.createComposite(parent);

		GridLayout layout1 = getGridLayoutZeroSetting();
		layout1.marginLeft = 2;
		layout1.marginRight = 2;
		layout1.numColumns = 2;
		layout1.makeColumnsEqualWidth = false;
		resolutionComposite.setLayout(layout1);

		Label lblTomoResolution = toolkit.createLabel(resolutionComposite, "Resolution : Pixel Size = ", SWT.LEFT);
		lblTomoResolution.setLayoutData(new GridData());

		lblObjectPixelSize = toolkit.createLabel(resolutionComposite, "0.000 mm", SWT.LEFT_TO_RIGHT);
		lblObjectPixelSize.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		//
		Composite upperRowComposite = toolkit.createComposite(resolutionComposite);
		GridLayout layout = new GridLayout(4, false);
		layout.horizontalSpacing = 2;
		layout.verticalSpacing = 2;
		layout.marginWidth = 2;
		layout.marginHeight = 2;

		upperRowComposite.setLayout(layout);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 2;
		upperRowComposite.setLayoutData(layoutData);

		btnResFull = toolkit.createButton(upperRowComposite, RESOLUTION_FULL, SWT.PUSH);
		btnResFull.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnResFull.addSelectionListener(buttonSelectionListener);
		btnResFull.setFont(fontRegistry.get(NORMAL_TEXT_7));

		btnRes2x = toolkit.createButton(upperRowComposite, RESOLUTION_2x, SWT.PUSH);
		btnRes2x.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnRes2x.addSelectionListener(buttonSelectionListener);
		btnRes2x.setFont(fontRegistry.get(NORMAL_TEXT_7));

		btnRes4x = toolkit.createButton(upperRowComposite, RESOLUTION_4x, SWT.PUSH);
		btnRes4x.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnRes4x.addSelectionListener(buttonSelectionListener);
		btnRes4x.setFont(fontRegistry.get(NORMAL_TEXT_7));

		btnRes8x = toolkit.createButton(upperRowComposite, RESOLUTION_8x, SWT.PUSH);
		btnRes8x.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnRes8x.addSelectionListener(buttonSelectionListener);
		btnRes8x.setFont(fontRegistry.get(NORMAL_TEXT_7));

		Composite horizontalBar = toolkit.createComposite(resolutionComposite);
		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		horizontalBar.setBackground(ColorConstants.black);
		layoutData.heightHint = 2;
		layoutData.horizontalSpan = 2;
		horizontalBar.setLayoutData(layoutData);

		//
		Composite lowerRowComposite = toolkit.createComposite(resolutionComposite);
		layout = new GridLayout(4, true);
		layout.horizontalSpacing = 2;
		layout.verticalSpacing = 2;
		layout.marginWidth = 2;
		layout.marginHeight = 2;

		lowerRowComposite.setLayout(layout);
		GridData layoutData2 = new GridData(GridData.FILL_HORIZONTAL);
		layoutData2.horizontalSpan = 2;
		lowerRowComposite.setLayoutData(layoutData2);

		Label lblNumFrames = toolkit.createLabel(lowerRowComposite, FRAMES_PER_PROJECTION, SWT.CENTER | SWT.WRAP);
		GridData ld = new GridData(GridData.FILL_HORIZONTAL);
		ld.verticalAlignment = SWT.CENTER;
		ld.horizontalSpan = 3;
		lblNumFrames.setLayoutData(ld);
		lblNumFrames.setFont(fontRegistry.get(NORMAL_TEXT_9));

		txtResFramesPerProjection = toolkit
				.createText(lowerRowComposite, FRAMES_PER_PROJECTION_DEFAULT_VAL, SWT.CENTER);
		txtResFramesPerProjection.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtResFramesPerProjection.addKeyListener(txtKeyListener);
		txtResFramesPerProjection.addFocusListener(focusListener);
		return resolutionComposite;
	}

	protected Composite createBeamlineControlComposite(Composite parent, FormToolkit toolkit) {
		Composite beamlineControlComposite = toolkit.createComposite(parent);
		GridLayout layout = new GridLayout(2, true);
		layout.marginWidth = 2;
		layout.marginHeight = 2;
		beamlineControlComposite.setLayout(layout);

		Label lblBeamlineControl = toolkit.createLabel(beamlineControlComposite, "Beamline Control");
		lblBeamlineControl.setFont(fontRegistry.get(BOLD_TEXT_11));
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		gd.horizontalAlignment = SWT.CENTER;
		lblBeamlineControl.setLayoutData(gd);

		Composite futureUseComposite = toolkit.createComposite(beamlineControlComposite);
		futureUseComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		futureUseComposite.setLayout(new FillLayout());

		Label lblFutureUse = toolkit.createLabel(futureUseComposite, FOR_FUTURE_USE);
		lblFutureUse.setFont(fontRegistry.get(NORMAL_TEXT_9));
		lblFutureUse.setBackground(ColorConstants.lightGray);

		Composite xRayEnergyComposite = toolkit.createComposite(beamlineControlComposite);
		xRayEnergyComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		layout = new GridLayout();
		xRayEnergyComposite.setLayout(layout);
		xRayEnergyComposite.setBackground(ColorConstants.lightGray);

		Label lblXrayEnergy = toolkit.createLabel(xRayEnergyComposite, X_RAY_ENERGY_lbl, SWT.WRAP | SWT.CENTER);
		lblXrayEnergy.setLayoutData(new GridData(GridData.FILL_BOTH));
		lblXrayEnergy.setFont(fontRegistry.get(NORMAL_TEXT_9));
		lblXrayEnergy.setBackground(ColorConstants.lightGray);

		Text txtXrayEnergy = toolkit.createText(xRayEnergyComposite, ENERGY_DEFAULT_VALUE, SWT.CENTER);
		txtXrayEnergy.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtXrayEnergy.addFocusListener(focusListener);
		txtXrayEnergy.addKeyListener(txtKeyListener);
		txtXrayEnergy.setEditable(false);
		return beamlineControlComposite;
	}

	protected Composite createExperimentInstrument(Composite parent, FormToolkit toolkit) {
		Composite tomoAlignmentComposite = toolkit.createComposite(parent);
		int numCols = 5;
		GridLayout layout = new GridLayout(numCols, true);
		layout.verticalSpacing = 1;
		layout.marginWidth = 2;
		tomoAlignmentComposite.setLayout(layout);

		Label lblTomoAlignment = toolkit.createLabel(tomoAlignmentComposite, "Experiment Instrument");
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = numCols;
		gd.horizontalAlignment = SWT.CENTER;
		lblTomoAlignment.setLayoutData(gd);
		lblTomoAlignment.setFont(fontRegistry.get(BOLD_TEXT_11));

		Button btnAutoFocus = toolkit.createButton(tomoAlignmentComposite, "Auto Focus", SWT.PUSH);
		btnAutoFocus.setFont(fontRegistry.get(NORMAL_TEXT_9));
		btnAutoFocus.setLayoutData(new GridData(GridData.FILL_BOTH));
		btnAutoFocus.addListener(SWT.MouseDown, ctrlMouseListener);

		// Buttons for align tilts
		Button btnTilt = toolkit.createButton(tomoAlignmentComposite, ALIGN_TILT_lbl, SWT.PUSH);
		btnTilt.setFont(fontRegistry.get(NORMAL_TEXT_9));
		btnTilt.setLayoutData(new GridData(GridData.FILL_BOTH));
		btnTilt.addListener(SWT.MouseDown, ctrlMouseListener);

		//
		Button btnFindAxisOfRotation = toolkit.createButton(tomoAlignmentComposite, "Find Tomo Axis", SWT.PUSH
				| SWT.WRAP);
		btnFindAxisOfRotation.setLayoutData(new GridData(GridData.FILL_BOTH));
		btnFindAxisOfRotation.setFont(fontRegistry.get(NORMAL_TEXT_9));
		btnFindAxisOfRotation.addListener(SWT.MouseDown, ctrlMouseListener);

		// Label lblTiltLastSaved = toolkit.createLabel(tomoAlignmentComposite, LAST_SAVED_lbl, SWT.WRAP | SWT.CENTER);
		// lblTiltLastSaved.setFont(fontRegistry.get(NORMAL_TEXT_9));
		// lblTiltLastSaved.setLayoutData(new GridData(GridData.FILL_BOTH));
		// lblTiltLastSaved.setBackground(ColorConstants.lightBlue);
		// lblTiltLastSaved.setForeground(ColorConstants.white);

		// moveAxisBtnComposite = toolkit.createComposite(tomoAlignmentComposite);
		// moveAxisBtnComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		//
		// moveAxisRotationCmpLayout = new StackLayout();
		// moveAxisBtnComposite.setLayout(moveAxisRotationCmpLayout);

		Button btnMoveAxisOfRotation = toolkit.createButton(tomoAlignmentComposite, "Move Tomo Axis", SWT.PUSH
				| SWT.WRAP);
		// btnMoveAxisOfRotation.setLayoutData(new GridData(GridData.FILL_BOTH));
		btnMoveAxisOfRotation.setBackground(ColorConstants.green);
		btnMoveAxisOfRotation.setFont(fontRegistry.get(NORMAL_TEXT_9));
		btnMoveAxisOfRotation.addListener(SWT.MouseDown, ctrlMouseListener);
		btnMoveAxisOfRotation.setLayoutData(new GridData(GridData.FILL_BOTH));
		// lblRotationAxisNotFound = toolkit.createLabel(moveAxisBtnComposite, ROTATION_AXIS_NOT_DEFINED_shortdesc,
		// SWT.WRAP | SWT.CENTER);
		// lblRotationAxisNotFound.setBackground(ColorConstants.lightBlue);
		// lblRotationAxisNotFound.setForeground(ColorConstants.white);

		// moveAxisRotationCmpLayout.topControl = lblRotationAxisNotFound;

		Composite cameraMotionControlComposite = createCameraMotionControlComposite(tomoAlignmentComposite, toolkit);
		cameraMotionControlComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		return tomoAlignmentComposite;
	}

	protected Composite createCameraMotionControlComposite(Composite parent, FormToolkit toolkit) {
		Composite cameraMotionComposite = toolkit.createComposite(parent);

		GridLayout layout = new GridLayout(2, true);
		layout.marginHeight = 2;
		layout.marginWidth = 2;
		cameraMotionComposite.setLayout(layout);

		ModuleButtonComposite moduleButtonComposite = new ModuleButtonComposite(cameraMotionComposite, toolkit);
		moduleButtonComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite cameraDistanceComposite = toolkit.createComposite(cameraMotionComposite);
		cameraDistanceComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		layout = new GridLayout();

		cameraDistanceComposite.setLayout(layout);
		cameraDistanceComposite.setBackground(ColorConstants.lightGray);

		Label lblCameraDistance = toolkit.createLabel(cameraDistanceComposite, CAMERA_DISTANCE_lbl, SWT.WRAP
				| SWT.CENTER);
		lblCameraDistance.setFont(fontRegistry.get(NORMAL_TEXT_9));

		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.horizontalSpan = 2;
		layoutData.horizontalAlignment = SWT.CENTER;
		lblCameraDistance.setLayoutData(layoutData);
		lblCameraDistance.setBackground(ColorConstants.lightGray);

		Text txtCameraDistance = toolkit.createText(cameraDistanceComposite, CAMERA_DIST_DEFAULT_VALUE, SWT.CENTER);
		txtCameraDistance.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtCameraDistance.addFocusListener(focusListener);
		txtCameraDistance.addKeyListener(txtKeyListener);

		return cameraMotionComposite;
	}

	/**
	 * listener for buttons and slider - but this is control masked - the "Ctrl" key needs to be pressed
	 */
	private Listener ctrlMouseListener = new Listener() {

		@Override
		public void handleEvent(Event event) {
			// to check whether the control key is pressend along with the mouse button click
			// if (event.stateMask == SWT.CTRL) {
			// Object sourceObj = event.widget;
			// try {
			// if (sourceObj == btnHorizontal) {
			// // Button - Center Current position
			// if (!isSelected(btnHorizontal)) {
			// try {
			// switchOn(MotionControlCentring.HORIZONTAL);
			// } catch (Exception ex) {
			// switchOff(MotionControlCentring.HORIZONTAL);
			// }
			// } else {
			// switchOff(MotionControlCentring.HORIZONTAL);
			// }
			// } else if (sourceObj == btnFindAxisOfRotation) {
			// // Button - Half Rotation Tool
			// if (!isSelected(btnFindAxisOfRotation)) {
			// try {
			// switchOn(MotionControlCentring.FIND_AXIS_ROTATION);
			// } catch (Exception ex) {
			// switchOff(MotionControlCentring.FIND_AXIS_ROTATION);
			// }
			// } else {
			// switchOff(MotionControlCentring.FIND_AXIS_ROTATION);
			//
			// }
			// } else if (sourceObj == btnMoveAxisOfRotation) {
			// // Button - Half Rotation Tool
			// if (!isSelected(btnMoveAxisOfRotation)) {
			// try {
			// switchOn(MotionControlCentring.MOVE_AXIS_OF_ROTATION);
			// } catch (Exception ex) {
			// switchOff(MotionControlCentring.MOVE_AXIS_OF_ROTATION);
			// }
			// } else {
			// switchOff(MotionControlCentring.MOVE_AXIS_OF_ROTATION);
			//
			// }
			// } else if (sourceObj == btnVertical) {
			// // Button - Vertical
			// if (!isSelected(btnVertical)) {
			// try {
			// switchOn(MotionControlCentring.VERTICAL);
			// } catch (Exception ex) {
			// switchOff(MotionControlCentring.VERTICAL);
			// }
			// } else {
			// switchOff(MotionControlCentring.VERTICAL);
			// }
			// } else if (sourceObj == btnTilt) {
			// // Button - Vertical
			// if (!isSelected(btnTilt)) {
			// try {
			// switchOn(MotionControlCentring.TILT);
			// } catch (Exception ex) {
			// switchOff(MotionControlCentring.TILT);
			// }
			// } else {
			// switchOff(MotionControlCentring.TILT);
			// }
			// } else if (sourceObj == btnSampleWeightLessThan1) {
			// // Button - Vertical
			// if (!isSelected(btnSampleWeightLessThan1)) {
			// selectSampleWeightControlInUi(btnSampleWeightLessThan1);
			// //
			// for (IMotionControlListener mcl : motionControlListeners) {
			// mcl.setSampleWeight(SAMPLE_WEIGHT.LESS_THAN_ONE);
			// }
			// }
			// } else if (sourceObj == btnSampleWeight1to10) {
			// // Button - Vertical
			// if (!isSelected(btnSampleWeight1to10)) {
			// selectSampleWeightControlInUi(btnSampleWeight1to10);
			// //
			// for (IMotionControlListener mcl : motionControlListeners) {
			// mcl.setSampleWeight(SAMPLE_WEIGHT.ONE_TO_TEN);
			// }
			// }
			// } else if (sourceObj == btnSampleWeight10to20) {
			// // Button - Vertical
			// if (!isSelected(btnSampleWeight10to20)) {
			// selectSampleWeightControlInUi(btnSampleWeight10to20);
			//
			// //
			// for (IMotionControlListener mcl : motionControlListeners) {
			// mcl.setSampleWeight(SAMPLE_WEIGHT.TEN_TO_TWENTY);
			// }
			// }
			// } else if (sourceObj == btnSampleWeight20to50) {
			// // Button - Vertical
			// if (!isSelected(btnSampleWeight20to50)) {
			// selectSampleWeightControlInUi(btnSampleWeight20to50);
			// //
			// for (IMotionControlListener mcl : motionControlListeners) {
			// mcl.setSampleWeight(SAMPLE_WEIGHT.TWENTY_TO_FIFTY);
			// }
			// }
			// }
			// } catch (Exception ex) {
			// logger.error(((Button) sourceObj).getText() + ": problem switching off", ex);
			// }
			// }

		}
	};

	/**
	 * @param toolkit
	 * @param parent
	 * @return {@link Composite} container for the "Define ROI" and "Hide ROI" buttons
	 */
	private Composite createDefineRoiComposite(FormToolkit toolkit, Composite parent) {
		Composite defineRoiComposite = toolkit.createComposite(parent);
		defineRoiComposite.setLayout(getGridLayoutZeroSetting());

		Label lblROI = toolkit.createLabel(defineRoiComposite, "ROI", SWT.CENTER);
		lblROI.setLayoutData(new GridData(GridData.FILL_BOTH));

		btnDefineRoi = toolkit.createButton(defineRoiComposite, DEFINE_ROI, SWT.PUSH);
		btnDefineRoi.setLayoutData(new GridData(GridData.FILL_BOTH));
		btnDefineRoi.addSelectionListener(buttonSelectionListener);
		//
		btnResetRoi = toolkit.createButton(defineRoiComposite, RESET_ROI, SWT.PUSH);
		btnResetRoi.setLayoutData(new GridData(GridData.FILL_BOTH));
		btnResetRoi.addSelectionListener(buttonSelectionListener);
		return defineRoiComposite;
	}

	private GridLayout getGridLayoutZeroSetting() {
		GridLayout gl = new GridLayout();
		gl.horizontalSpacing = 0;
		gl.verticalSpacing = 0;
		gl.marginWidth = 0;
		gl.marginHeight = 0;
		return gl;
	}

	/**
	 * Method to create the two panels for Exposure time, Steam, Single and histogram
	 * 
	 * @param toolkit
	 * @return composite
	 */
	private Composite createExpStreamSingleHistoComposite(FormToolkit toolkit, Composite parent) {
		Composite expStreamSingleHistoComposite = toolkit.createComposite(parent);
		GridLayout layout = new GridLayout(4, false);
		layout.horizontalSpacing = 2;
		layout.verticalSpacing = 5;
		layout.marginWidth = 3;
		layout.marginHeight = 2;

		expStreamSingleHistoComposite.setLayout(layout);

		Composite lblComposite = toolkit.createComposite(expStreamSingleHistoComposite);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		// layoutData.widthHint = 60;
		lblComposite.setLayoutData(layoutData);
		layout = new GridLayout();
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		lblComposite.setLayout(layout);
		// 1. Sample Label
		Button lblSample = toolkit.createButton(lblComposite, SAMPLE, SWT.PUSH);
		GridData ld = new GridData(GridData.FILL_BOTH);
		ld.verticalAlignment = SWT.CENTER;
		lblSample.setLayoutData(ld);

		Composite borderComposite = toolkit.createComposite(lblComposite);
		borderComposite.setBackground(ColorConstants.black);
		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.heightHint = 2;
		borderComposite.setLayoutData(layoutData);
		// 1. Flat Label
		Button lblFlatSample = toolkit.createButton(lblComposite, FLAT, SWT.PUSH);
		ld = new GridData(GridData.FILL_BOTH);
		ld.verticalAlignment = SWT.CENTER;
		lblFlatSample.setLayoutData(ld);
		//
		btnFlatToSample = toolkit.createButton(expStreamSingleHistoComposite, null, SWT.PUSH);
		layoutData = new GridData(GridData.FILL_VERTICAL);
		btnFlatToSample.setLayoutData(layoutData);
		btnFlatToSample.setImage(TomoClientActivator.getDefault().getImageRegistry()
				.get(ImageConstants.ICON_DOWN_TO_UP));
		btnFlatToSample.addSelectionListener(buttonSelectionListener);
		// 2

		Composite txtBoxComposite = toolkit.createComposite(expStreamSingleHistoComposite);
		ld = new GridData(GridData.FILL_VERTICAL);
		// ld.widthHint = 80;
		txtBoxComposite.setLayoutData(ld);

		layout = new GridLayout(2, false);
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 3;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
		txtBoxComposite.setLayout(layout);

		txtSampleExposureTime = toolkit.createText(txtBoxComposite, DEFAULT_EXP_TIME, SWT.CENTER);
		ld = new GridData(GridData.FILL_BOTH);
		ld.verticalAlignment = SWT.CENTER;
		// ld.widthHint = 45;
		txtSampleExposureTime.setLayoutData(ld);

		txtSampleExposureTime.addKeyListener(txtKeyListener);
		txtSampleExposureTime.addFocusListener(focusListener);

		Label lblSampleExpTimeUnits = toolkit.createLabel(txtBoxComposite, EXP_TIME_MEASURE);
		ld = new GridData(GridData.FILL_BOTH);
		ld.verticalAlignment = SWT.CENTER;
		lblSampleExpTimeUnits.setLayoutData(ld);
		//
		borderComposite = toolkit.createComposite(txtBoxComposite);
		borderComposite.setBackground(ColorConstants.black);
		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.heightHint = 2;
		layoutData.horizontalSpan = 2;
		borderComposite.setLayoutData(layoutData);
		//
		txtFlatExpTime = toolkit.createText(txtBoxComposite, DEFAULT_EXP_TIME, SWT.CENTER);
		ld = new GridData(GridData.FILL_BOTH);
		// ld.widthHint = 45;
		ld.verticalAlignment = SWT.CENTER;
		txtFlatExpTime.setLayoutData(ld);
		txtFlatExpTime.addKeyListener(txtKeyListener);
		txtFlatExpTime.addFocusListener(focusListener);
		//
		Label lblFlatExpTimeUnits = toolkit.createLabel(txtBoxComposite, EXP_TIME_MEASURE);
		ld = new GridData(GridData.FILL_BOTH);
		ld.verticalAlignment = SWT.CENTER;
		lblFlatExpTimeUnits.setLayoutData(ld);

		btnSampleToFlat = toolkit.createButton(expStreamSingleHistoComposite, null, SWT.PUSH);
		layoutData = new GridData(GridData.FILL_VERTICAL);
		btnSampleToFlat.setLayoutData(layoutData);
		btnSampleToFlat.addSelectionListener(buttonSelectionListener);
		btnSampleToFlat.setImage(TomoClientActivator.getDefault().getImageRegistry()
				.get(ImageConstants.ICON_UP_TO_DOWN));
		return expStreamSingleHistoComposite;
	}

	private ModifyListener descModifyListener = new ModifyListener() {

		@Override
		public void modifyText(ModifyEvent e) {
			if (!TYPE_DESCRIPTION.equals(txtSampleDesc.getText())) {
				txtSampleDesc.setForeground(ColorConstants.black);
			} else {
				txtSampleDesc.setForeground(ColorConstants.lightGray);
			}
		}
	};

	private void initializeFontRegistry() {
		if (Display.getCurrent() != null) {
			fontRegistry = new FontRegistry(Display.getCurrent());
			String fontName = Display.getCurrent().getSystemFont().getFontData()[0].getName();
			fontRegistry.put(NORMAL_TEXT_7, new FontData[] { new FontData(fontName, 7, SWT.NORMAL) });
			fontRegistry.put(NORMAL_TEXT_8, new FontData[] { new FontData(fontName, 8, SWT.NORMAL) });
			fontRegistry.put(NORMAL_TEXT_9, new FontData[] { new FontData(fontName, 9, SWT.NORMAL) });
			fontRegistry.put(BOLD_TEXT_11, new FontData[] { new FontData(fontName, 11, SWT.BOLD) });
		}
	}

	private FocusListener focusListener = new FocusAdapter() {
		@Override
		public void focusLost(FocusEvent focusEvent) {
			if (focusEvent.getSource().equals(txtSampleExposureTime)) {
				logger.debug("focus los on sample exposure time");
				try {
					for (ICameraControlListener cl : cameraControlListeners) {
						cl.updatePreferredSampleExposureTime();
					}
				} catch (InvocationTargetException e) {
					showError(ERROR_UPDATING_EXPOSURE_TIME, e);
				} catch (Exception e) {
					showError(ERROR_UPDATING_EXPOSURE_TIME, e);
				}
			} else if (focusEvent.getSource().equals(txtFlatExpTime)) {
				logger.debug("focus los on flat exposure time");
				try {
					for (ICameraControlListener cl : cameraControlListeners) {
						cl.updatePreferredFlatExposureTime();
					}
				} catch (InvocationTargetException e) {
					showError(ERROR_UPDATING_EXPOSURE_TIME, e);
				} catch (Exception e) {
					showError(ERROR_UPDATING_EXPOSURE_TIME, e);
				}
			} else if (focusEvent.getSource().equals(txtSampleDesc)) {
				if (BLANK_STR.equals(txtSampleDesc.getText())) {
					txtSampleDesc.setText(TYPE_DESCRIPTION);
				}
			}
		}

		@Override
		public void focusGained(FocusEvent focusEvent) {
			if (focusEvent.getSource().equals(txtSampleDesc)) {
				if (TYPE_DESCRIPTION.equals(txtSampleDesc.getText())) {
					txtSampleDesc.setText(BLANK_STR);
				}
			}
		}
	};

	/**
	 * Key adapter for the text boxes to validate and persist values
	 */
	private KeyAdapter txtKeyListener = new KeyAdapter() {

		@Override
		public void keyPressed(KeyEvent e) {
			// Sample exposure time
			if (e.getSource().equals(txtSampleExposureTime)) {
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
					if (isValid(Double.class, txtSampleExposureTime.getText())) {
						sampleExposureTime = Double.parseDouble(txtSampleExposureTime.getText());
						try {
							for (ICameraControlListener cl : cameraControlListeners) {
								cl.sampleExposureTimeChanged(Double.parseDouble(txtSampleExposureTime.getText()));
							}
						} catch (Exception e1) {
							logger.debug("Error setting exposure time", e1);
						}
						btnSampleStream.setFocus();
					} else {
						MessageDialog.openError(txtSampleExposureTime.getShell(), INVALID_VALUE,
								INVALID_EXPOSURE_TIME_shortdesc);
					}
				}
			} else if (e.getSource().equals(txtFlatExpTime)) {
				// Flat exposure time
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
					if (isValid(Double.class, txtFlatExpTime.getText())) {
						flatExposureTime = Double.parseDouble(txtFlatExpTime.getText());
						try {
							for (ICameraControlListener cl : cameraControlListeners) {
								cl.flatExposureTimeChanged(Double.parseDouble(txtFlatExpTime.getText()));
							}
						} catch (Exception e1) {
							logger.error("Error setting exposure time", e1);
						}
						btnFlatStream.setFocus();
					} else {
						MessageDialog.openError(txtFlatExpTime.getShell(), INVALID_VALUE,
								INVALID_EXPOSURE_TIME_shortdesc);
					}
				}
			} else if (e.getSource().equals(txtSampleDesc)) {
				// Sample description
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
					if (!TYPE_DESCRIPTION.equals(txtSampleDesc.getText())) {
						sampleDescription = txtSampleDesc.getText();
						try {
							for (ICameraControlListener cl : cameraControlListeners) {
								cl.sampleDescriptionChanged(txtSampleDesc.getText());
							}
						} catch (Exception e1) {
							logger.error(ERR_PROBLEM_SETTING_SAMPLE_DESCRIPTION, e1);
						}
						btnSaveAlignment.setFocus();
					} else {
						MessageDialog.openError(txtSampleDesc.getShell(), ERR_PROBLEM_SETTING_SAMPLE_DESCRIPTION,
								ERR_PROBLEM_SETTING_SAMPLE_DESCRIPTION);
					}
				}
			} else if (e.getSource().equals(txtResFramesPerProjection)) {
				// Flat exposure time
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
					if (isValid(Integer.class, txtResFramesPerProjection.getText())) {
						framesPerProjection = Integer.parseInt(txtResFramesPerProjection.getText());

						btnSaveAlignment.setFocus();
					} else {
						MessageDialog.openError(txtResFramesPerProjection.getShell(), INVALID_VALUE,
								"Invalid Frames Per Projection value");
					}
				}
			}
		}
	};

	@SuppressWarnings("rawtypes")
	private boolean isValid(Class cls, String expTime) {
		String expr = null;
		if (Double.class.equals(cls)) {
			expr = "(\\d)*.?(\\d)*";
		} else if (Integer.class.equals(cls)) {
			expr = "(\\d)*";
		}
		if (expr != null) {
			if (expTime == null || expTime.length() < 1) {
				return false;
			}

			if (!expTime.matches(expr)) {
				return false;
			}
			return true;
		}
		return false;
	}

	/**
	 * Returns <code>true</code> if the colors as set when selected.
	 * 
	 * @param button
	 * @return true when background color is lightgray and foreground color is red - this is what was set when the
	 *         widget was selected.
	 */
	private boolean isSelected(final Button button) {
		final boolean[] isSelected = new boolean[1];
		if (button != null && !button.isDisposed()) {
			getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					if (ColorConstants.red.equals(button.getForeground())
							&& ColorConstants.lightGray.equals(button.getBackground())) {
						isSelected[0] = true;
					} else {
						isSelected[0] = false;
					}
				}
			});
		}
		return isSelected[0];
	}

	/**
	 * Method to run in the UI thread to set the colors to show the button selected
	 * 
	 * @param btnCntrl
	 */
	private static void selectControl(final Button btnCntrl) {
		btnCntrl.getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				btnCntrl.setForeground(ColorConstants.red);
				btnCntrl.setBackground(ColorConstants.lightGray);

			}
		});
	}

	/**
	 * Method to run in the UI thread to set the colors to show the button de-selected
	 * 
	 * @param btnCntrl
	 */
	private static void deSelectControl(final Button btnCntrl) {
		if (btnCntrl != null && !btnCntrl.isDisposed()) {
			btnCntrl.getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					if (!btnCntrl.isDisposed()) {
						logger.debug("Deselecting control:{}", btnCntrl);
						btnCntrl.setForeground(btnCntrl.getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND));
						btnCntrl.setBackground(btnCntrl.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
					}
				}
			});
		}
	}

	private List<ICameraControlListener> cameraControlListeners = new ArrayList<ICameraControlListener>();

	public void addCamerControlListener(ICameraControlListener cameraControlListener) {
		cameraControlListeners.add(cameraControlListener);

		// zoomComposite.addZoomButtonActionListener(cameraControlListener);
	}

	public void removeCamerControlListener(ICameraControlListener cameraControlListener) {
		cameraControlListeners.remove(cameraControlListener);
		// /zoomComposite.removeZoomButtonActionListener(cameraControlListener);
	}

	private void showError(final String dialogTitle, final Exception ex) {
		if (!this.isDisposed()) {
			getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					MessageDialog.openError(getShell(), dialogTitle, ex.getMessage());
				}
			});
		}
	}

	/**
	 * selection listener for the buttons
	 */
	private SelectionListener buttonSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent event) {
			Object sourceObj = event.getSource();
			if (sourceObj == btnSampleStream) {
				if (!isSelected(btnSampleStream)) {
					logger.debug("'Stream' is selected");
					/**/
					try {
						startSampleStreaming();
					} catch (Exception e) {
						showError(ERR_START_STREAM, e);
						setStreamState(STREAM_STATE.NO_STREAM);
						logger.error("exception selecting stream", e);
					}
					selectControl(btnSampleStream);
					setStreamState(STREAM_STATE.SAMPLE_STREAM);

				} else {
					logger.debug("'Stream' is de-selected");
					stopSampleStream();
					setStreamState(STREAM_STATE.NO_STREAM);
				}
			} else if (sourceObj == btnFlatToSample) {
				// txtSampleExposureTime.setText(txtFlatExpTime.getText());
				setPreferredSampleExposureTime(Double.parseDouble(txtFlatExpTime.getText()));
				try {
					for (ICameraControlListener cl : cameraControlListeners) {
						cl.sampleExposureTimeChanged(Double.parseDouble(txtSampleExposureTime.getText()));
					}
				} catch (Exception e1) {
					logger.debug("Error setting exposure time", e1);
				}
			} else if (sourceObj == btnSampleToFlat) {
				// txtFlatExpTime.setText(txtSampleExposureTime.getText());
				setPreferredFlatExposureTime(Double.parseDouble(txtSampleExposureTime.getText()));
				try {
					for (ICameraControlListener cl : cameraControlListeners) {
						cl.flatExposureTimeChanged(Double.parseDouble(txtFlatExpTime.getText()));
					}
				} catch (Exception e1) {
					logger.debug("Error setting exposure time", e1);
				}
			} else if (sourceObj == btnSampleHistogram) {
				if (!isSelected(btnSampleHistogram)) {
					startSampleHistogram();
				} else {
					logger.debug("'btnSampleHistogram' is de-selected");
					stopSampleHistogram();
				}
			} else if (sourceObj == btnFlatStream) {
				if (!isSelected(btnFlatStream)) {
					logger.debug("'btnFlatStream' is selected");
					selectControl(btnFlatStream);
					try {
						startFlatStreaming();
					} catch (Exception e) {
						showError(ERR_START_STREAM, e);
						setStreamState(STREAM_STATE.NO_STREAM);
						logger.error("exception selecting stream", e);
					}
					setStreamState(STREAM_STATE.FLAT_STREAM);
				} else {
					setStreamState(STREAM_STATE.NO_STREAM);
					logger.debug("'btnFlatStream' is de-selected");
					deSelectControl(btnFlatStream);
					try {
						for (ICameraControlListener cl : cameraControlListeners) {
							cl.flatStream(false);
						}
					} catch (Exception e) {
						showError(ERR_CAPTURING_RAW, e);
						logger.error("Flat single capturing has problems", e);
					}
				}
			} else if (sourceObj == btnFlatSingle) {
				if (!isSelected(btnFlatSingle)) {
					logger.debug("'Flat single' is selected");
					try {
						for (ICameraControlListener cl : cameraControlListeners) {
							cl.flatSingle(isFlatCorrectionSelected());
						}
					} catch (Exception e) {
						showError(ERR_CAPTURING_RAW, e);
						logger.error("Flat single capturing has problems", e);
					}
				} else {
					logger.debug("'Flat Single' is de-selected");
					deSelectControl(btnFlatSingle);
				}
			} else if (sourceObj == btnFlatHistogram) {
				if (!isSelected(btnFlatHistogram)) {
					startFlatHistogram();
				} else {
					logger.debug("'btnSampleHistogram' is de-selected");
					stopFlatHistogram();
				}
			} else if (sourceObj == btnSampleIn) {
				if (!isSelected(btnSampleIn)) {
					logger.debug("'btnSampleIn' is selected");
					selectSampleIn();
					try {
						for (ICameraControlListener cl : cameraControlListeners) {
							cl.moveSampleIn();
						}
					} catch (IllegalStateException s) {
						showError("Cannot Move Sample In", s);
						selectSampleOut();
					} catch (Exception e1) {
						showError("Cannot Move Sample In", e1);
						selectSampleOut();
					}
				}
			} else if (sourceObj == btnSampleOut) {
				if (!isSelected(btnSampleOut)) {
					logger.debug("'btnSampleOut' is selected");
					logger.debug("'btnSampleIn' is selected");
					selectSampleOut();
					try {
						for (ICameraControlListener cl : cameraControlListeners) {
							cl.moveSampleOut();
						}
					} catch (IllegalStateException s) {
						showError("Cannot Move Sample In", s);
						selectSampleIn();
					} catch (Exception e1) {
						showError("Cannot Move Sample In", e1);
						selectSampleIn();
					}
				}
			} else if (sourceObj == btnDefineRoi) {
				if (!isSelected(btnDefineRoi)) {
					logger.debug("'btnDefineRoi' is selected");
					selectControl(btnDefineRoi);
					try {
						for (ICameraControlListener cl : cameraControlListeners) {
							cl.defineRoi(true);
						}
					} catch (IllegalStateException s) {
						showError("Cannot Define ROI", s);
						deSelectControl(btnDefineRoi);
					} catch (Exception e1) {
						logger.debug("Error setting exposure time", e1);
						showError("Cannot Define ROI", e1);
					}
				} else {
					logger.debug("'btnDefineRoi' is de-selected");
					deSelectControl(btnDefineRoi);
					try {
						for (ICameraControlListener cl : cameraControlListeners) {
							cl.defineRoi(false);
						}
					} catch (Exception e1) {
						logger.debug("Error setting exposure time", e1);
					}
				}
			} else if (sourceObj == btnResetRoi) {
				logger.debug("'btnResetRoi' is selected");
				try {
					for (ICameraControlListener cl : cameraControlListeners) {
						cl.resetRoi();
					}
				} catch (IllegalStateException s) {
					showError("Cannot Reset ROI", s);
				} catch (Exception e1) {
					logger.debug("Error setting exposure time", e1);
					showError("Cannot Define ROI", e1);
				}
			} else if (sourceObj == btnResFull) {
				if (!isSelected(btnResFull)) {
					logger.debug("'btnResFull' is selected");
					resolution = RESOLUTION.FULL;
					selectRes(btnResFull);
				}
			} else if (sourceObj == btnRes2x) {
				if (!isSelected(btnRes2x)) {
					logger.debug("'btnRes2x' is selected");
					resolution = RESOLUTION.TWO_X;
					selectRes(btnRes2x);
				}
			} else if (sourceObj == btnRes4x) {
				if (!isSelected(btnRes4x)) {
					logger.debug("'btnRes4x' is selected");
					resolution = RESOLUTION.FOUR_X;
					selectRes(btnRes4x);
				}
			} else if (sourceObj == btnRes8x) {
				if (!isSelected(btnRes8x)) {
					logger.debug("'btnRes8x' is selected");
					resolution = RESOLUTION.EIGHT_X;
					selectRes(btnRes8x);
				}
			} else if (sourceObj == btnSampleSingle) {
				if (!isSelected(btnSampleSingle)) {
					logger.debug("'Demand Raw' is selected");
					try {
						startSampleSingle();
					} catch (Exception e) {
						showError(ERR_CAPTURING_RAW, e);
						logger.error("Problem with starting demand raw", e);
					}
				} else {
					logger.debug("'Demand Raw' is de-selected");
					deSelectControl(btnSampleSingle);
				}
			} else if (sourceObj == btnSaturation) {
				if (!isSelected(btnSaturation)) {
					logger.debug("'Saturation' is selected");
					selectControl(btnSaturation);
					try {
						for (ICameraControlListener cl : cameraControlListeners) {
							cl.saturation(true);
						}
					} catch (Exception e) {
						deSelectControl(btnSaturation);
						showError("Errpr while saturation", e);
					}
				} else {
					logger.debug("'Saturation' is de-selected");
					deSelectControl(btnSaturation);
					try {
						for (ICameraControlListener cl : cameraControlListeners) {
							cl.saturation(false);
						}
					} catch (Exception e) {
						deSelectControl(btnSaturation);
						showError(ERR_WHILE_SATURATION, e);
					}
				}
			} else if (sourceObj == btnTakeFlatAndDark) {
				logger.debug("'btnTakeFlatAndDark' is selected");
				try {
					for (ICameraControlListener cl : cameraControlListeners) {
						cl.takeFlatAndDark();
					}
				} catch (InvocationTargetException ex) {
					showError("Error while taking flat images", ex);
				} catch (InterruptedException e) {
					showError("Error while taking flat images", e);
				}
			} else if (sourceObj == btnFlatCorrection) {
				if (!isSelected(btnFlatCorrection)) {
					selectControl(btnFlatCorrection);
					logger.debug("'Flat Correction' is selected");
					try {
						for (ICameraControlListener cl : cameraControlListeners) {
							cl.correctFlatAndDark(true);
						}
					} catch (Exception ex) {
						deSelectControl(btnFlatCorrection);
					}
				} else {
					deSelectControl(btnFlatCorrection);
					logger.debug("'Flat Correction' is de-selected");
					try {
						for (ICameraControlListener cl : cameraControlListeners) {
							cl.correctFlatAndDark(false);
						}
					} catch (Exception ex) {
						deSelectControl(btnFlatCorrection);
					}
				}
			} else if (sourceObj == btnFlatShow) {
				logger.debug("'Show Flat' is selected");
				for (ICameraControlListener cl : cameraControlListeners) {
					try {
						cl.showFlat();
					} catch (Exception e) {
						logger.error("Problem showing flat image", e);
					}
				}
			} else if (sourceObj == btnDarkShow) {
				logger.debug("'Show Dark' is selected");
				for (ICameraControlListener cl : cameraControlListeners) {
					try {
						cl.showDark();
					} catch (Exception e) {
						logger.error("Problem showing flat image", e);
					}
				}
			} else if (sourceObj == btnProfile) {
				if (!isSelected(btnProfile)) {
					logger.debug("'Profile' is selected");
					stopSampleStream();
					try {
						for (ICameraControlListener cl : cameraControlListeners) {
							cl.profile(true);
						}
					} catch (Exception ex) {
						deSelectControl(btnProfile);
						showError(ERR_PROFILING, ex);
					}
					selectControl(btnProfile);
				} else {
					try {
						for (ICameraControlListener cl : cameraControlListeners) {
							cl.profile(false);
						}
					} catch (Exception e) {
						logger.error("Error Profiling ;{}", e.getMessage());
					}
					deSelectControl(btnProfile);
				}
			} else if (sourceObj == btnSaveAlignment) {
				logger.debug("Save alignment clicked");
				try {
					for (ICameraControlListener cl : cameraControlListeners) {
						cl.saveAlignmentConfiguration();
					}
				} catch (Exception e) {
					logger.error("Error Saving:{}", e.getMessage());
				}
			}
		}

	};

	public void startSampleHistogram() {
		logger.debug("'btnSampleHistogram' is selected");
		selectControl(btnSampleHistogram);
		try {
			for (ICameraControlListener cl : cameraControlListeners) {
				cl.sampleHistogram(true);
			}
		} catch (Exception e1) {
			logger.debug("Error setting exposure time", e1);
		}
	}

	/**
	 * @param toolkit
	 * @param parent
	 * @return zoomButtonComposite
	 */
	private Composite createZoomBtnComposite(FormToolkit toolkit, Composite parent) {
		zoomComposite = new ZoomButtonComposite(parent, toolkit);
		return zoomComposite;
	}

	protected void selectRes(Button btnRes) {
		Button[] buttons = new Button[] { btnResFull, btnRes2x, btnRes4x, btnRes8x };
		for (Button button : buttons) {
			if (button.equals(btnRes)) {
				selectControl(button);
			} else {
				deSelectControl(button);
			}
		}

	}

	/**
	 * Starts the sample single
	 * 
	 * @throws Exception
	 */
	public void startSampleSingle() throws Exception {
		try {
			for (ICameraControlListener cl : cameraControlListeners) {
				cl.sampleSingle(isFlatCorrectionSelected());
			}
		} catch (Exception ex) {
			logger.error("Start sample single" + ex);
			throw ex;
		}
	}

	/**
	 * Starts the demand raw
	 * 
	 * @throws Exception
	 */
	public void startFlatSingle() throws Exception {
		try {
			for (ICameraControlListener cl : cameraControlListeners) {
				cl.flatSingle(isFlatCorrectionSelected());
			}
		} catch (Exception ex) {
			logger.error("start flat single:" + ex);
			throw ex;
		}
	}

	/**
	 * Force the GUI control to toggle the right button
	 * 
	 * @param zoomLevel
	 */
	public void setZoom(ZOOM_LEVEL zoomLevel) {
		zoomComposite.setZoomLevel(zoomLevel);
	}

	@Override
	public void setEnabled(boolean enabled) {
		super.setEnabled(enabled);
	}

	/**
	 * @return {@link ZOOM_LEVEL} selected
	 * @see ZoomButtonComposite#getSelectedZoom()
	 */
	public ZOOM_LEVEL getSelectedZoomLevel() {
		return zoomComposite.getSelectedZoom();
	}

	@Override
	public void dispose() {
		cameraControlListeners.clear();
		zoomComposite.dispose();
		buttonSelectionListener = null;
		//
		btnSampleSingle.dispose();
		btnFlatCorrection.dispose();
		btnFlatShow.dispose();
		btnTakeFlatAndDark.dispose();
		btnProfile.dispose();
		btnSaturation.dispose();
		btnSampleStream.dispose();
		//
		super.dispose();
	}

	/**
	 * 
	 */
	public void disableAll() {
		txtSampleExposureTime.setEditable(false);
		txtFlatExpTime.setEditable(false);
		txtSampleDesc.setEditable(false);

		btnSampleStream.setEnabled(false);
		btnSampleSingle.setEnabled(false);
		btnSampleHistogram.setEnabled(false);

		btnFlatStream.setEnabled(false);
		btnFlatSingle.setEnabled(false);
		btnFlatHistogram.setEnabled(false);

		btnSampleIn.setEnabled(false);
		btnSampleOut.setEnabled(false);

		zoomComposite.disableZoomButtons();
		btnSaturation.setEnabled(false);
		//
		btnTakeFlatAndDark.setEnabled(false);
		btnFlatCorrection.setEnabled(false);
		btnFlatShow.setEnabled(false);
		//
		btnProfile.setEnabled(false);

		btnDarkShow.setEnabled(false);

		btnDefineRoi.setEnabled(false);
		btnResetRoi.setEnabled(false);

		btnResFull.setEnabled(false);
		btnRes2x.setEnabled(false);
		btnRes4x.setEnabled(false);
		txtResFramesPerProjection.setEditable(false);

		btnSampleToFlat.setEnabled(false);
		btnFlatToSample.setEnabled(false);

		btnSaveAlignment.setEnabled(false);
	}

	public void enableAll() {
		txtSampleExposureTime.setEditable(true);
		txtFlatExpTime.setEditable(true);
		txtSampleDesc.setEditable(true);

		btnSampleStream.setEnabled(true);
		btnSampleSingle.setEnabled(true);
		btnSampleHistogram.setEnabled(true);

		btnFlatStream.setEnabled(true);
		btnFlatSingle.setEnabled(true);
		btnFlatHistogram.setEnabled(true);

		btnSampleIn.setEnabled(true);
		btnSampleOut.setEnabled(true);

		zoomComposite.enableZoomButtons();
		btnSaturation.setEnabled(true);
		//
		btnTakeFlatAndDark.setEnabled(true);
		btnFlatCorrection.setEnabled(true);
		btnFlatShow.setEnabled(true);
		//
		btnProfile.setEnabled(true);

		btnDarkShow.setEnabled(true);

		btnDefineRoi.setEnabled(true);
		btnResetRoi.setEnabled(true);

		btnResFull.setEnabled(true);
		btnRes2x.setEnabled(true);
		btnRes4x.setEnabled(true);
		txtResFramesPerProjection.setEditable(true);

		btnSampleToFlat.setEnabled(true);
		btnFlatToSample.setEnabled(true);

		btnSaveAlignment.setEnabled(true);
	}

	public void profileStopped() {
		deSelectControl(btnProfile);
	}

	public void setFlatFieldCorrection(final boolean enabled) {
		if (this != null && !this.isDisposed()) {
			this.getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					if (enabled) {
						selectControl(btnFlatCorrection);
					} else {
						deSelectControl(btnFlatCorrection);
					}
				}
			});
		}
	}

	public void setPreferredSampleExposureTime(final double preferredExposureTime) {
		sampleExposureTime = preferredExposureTime;
		if (txtSampleExposureTime != null && !txtSampleExposureTime.isDisposed()) {
			txtSampleExposureTime.getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					txtSampleExposureTime.setText(String.format("%.3g", preferredExposureTime));
				}
			});
		}
	}

	public double getSampleExposureTime() {
		return sampleExposureTime;
	}

	/**
	 * To be called only if the saturation button needs to be show as de-selected
	 */
	public void deSelectSaturation() {
		deSelectControl(btnSaturation);
	}

	/**
	 * To be called if the saturation button has to be shown de-selected and further processing needs to be done on the
	 * image in the image displayed window.
	 */
	public void saturationOff() {
		deSelectSaturation();
		try {
			for (ICameraControlListener cl : cameraControlListeners) {
				cl.saturation(false);
			}
		} catch (Exception ex) {
			logger.error("Exception switching off saturation");
		}
	}

	/**
	 * @return true if saturation button is selected else returns false.
	 */
	public boolean isSaturationSelected() {
		return isSelected(btnSaturation);
	}

	/**
	 * @return true if saturation button is selected else returns false.
	 */
	public boolean isProfileSelected() {
		return isSelected(btnProfile);
	}

	// Stream options
	public void selectStreamButton() {
		selectControl(btnSampleStream);
	}

	/**
	 * Deselects both the streams
	 */
	public void deselectSampleAndFlatStream() {
		deSelectControl(btnSampleStream);
		deSelectControl(btnFlatStream);
		setStreamState(STREAM_STATE.NO_STREAM);
	}

	/**
	 * @throws Exception
	 */
	public void startSampleStreaming() throws Exception {
		if (getStreamState() != STREAM_STATE.SAMPLE_STREAM) {
			selectStreamButton();
			if (isSelected(btnFlatStream)) {
				deSelectControl(btnFlatStream);
			}
			try {
				for (ICameraControlListener cl : cameraControlListeners) {
					cl.sampleStream(true);
				}
				streamState = STREAM_STATE.SAMPLE_STREAM;
			} catch (Exception ex) {
				logger.error("startStreaming:" + ex);
				deselectSampleAndFlatStream();
				throw new Exception("Unable to start streaming:", ex);
			}
		}
	}

	public void startFlatStreaming() throws Exception {
		if (getStreamState() != STREAM_STATE.FLAT_STREAM) {
			selectControl(btnFlatStream);

			try {
				for (ICameraControlListener cl : cameraControlListeners) {
					cl.flatStream(true);
				}
				streamState = STREAM_STATE.FLAT_STREAM;
			} catch (Exception ex) {
				logger.error("startStreaming:" + ex);
				deSelectControl(btnFlatStream);
				throw new Exception("Unable to start streaming:", ex);
			}
		}
	}

	/**
	 * This is called when the "Stream" button is de-selected from the GDA GUI.
	 */
	public void stopSampleStream() {
		if (getStreamState() == STREAM_STATE.SAMPLE_STREAM) {
			if (this != null && !this.isDisposed()) {
				this.getDisplay().syncExec(new Runnable() {

					@Override
					public void run() {
						try {
							deselectSampleAndFlatStream();
							for (ICameraControlListener cl : cameraControlListeners) {
								cl.sampleStream(false);
							}
							if (!ZOOM_LEVEL.NO_ZOOM.equals(getSelectedZoomLevel())) {
								setZoom(ZOOM_LEVEL.NO_ZOOM);
							}
						} catch (Exception ex) {
							logger.error("stopStreaming:", ex);
						}
					}
				});
				// Deselects both sample and flat stream

			}
		}
	}

	/**
	 * This is called when the "Stream" button is de-selected from the GDA GUI.
	 */
	public void stopFlatStream() {
		if (getStreamState() == STREAM_STATE.FLAT_STREAM) {
			if (this != null && !this.isDisposed()) {
				this.getDisplay().syncExec(new Runnable() {

					@Override
					public void run() {
						try {
							deselectSampleAndFlatStream();
							for (ICameraControlListener cl : cameraControlListeners) {
								cl.flatStream(false);
							}
							if (!ZOOM_LEVEL.NO_ZOOM.equals(getSelectedZoomLevel())) {
								setZoom(ZOOM_LEVEL.NO_ZOOM);
							}
						} catch (Exception ex) {
							logger.error("stopStreaming:", ex);
						}
					}
				});
				// Deselects both sample and flat stream

			}
		}

	}

	//
	public boolean isFlatCorrectionSelected() {
		return isSelected(btnFlatCorrection);
	}

	public double getFlatExposureTime() {
		return flatExposureTime;
	}

	public STREAM_STATE getStreamState() {
		return streamState;
	}

	public void enableFlatStreamButton() {
		selectControl(btnFlatStream);
	}

	public void setPreferredFlatExposureTime(final double preferredFlatExposureTime) {
		flatExposureTime = preferredFlatExposureTime;
		if (txtFlatExpTime != null && !txtFlatExpTime.isDisposed()) {
			txtFlatExpTime.getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					txtFlatExpTime.setText(String.format("%.3g", preferredFlatExposureTime));
				}
			});
		}

	}

	public void setFlatCaptured(boolean flatCaptured, double flatCapturedExposureTime) {
		if (flatCaptured) {
			// relayout the flat dark composite to show the 'show/correct flat/dark button'
			flatAndDarkCompositeStackLayout.topControl = flatDarkComposite;
			lblFlatExpTime.setText(String.format(FLAT_EXP_TIME_CAPTURED_shortmsg, flatCapturedExposureTime));
			this.flatCapturedExposureTime = flatCapturedExposureTime;
		} else {
			flatAndDarkCompositeStackLayout.topControl = lblFlatDarkNotAvailable;
			flatCapturedExposureTime = -1;
		}
		flatAndDarkContainerComposite.layout();
	}

	public static void main(String[] args) {
		final Display display = new Display();
		final Shell shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setBounds(new org.eclipse.swt.graphics.Rectangle(0, 0, 100, 400));
		shell.setLayout(new GridLayout());
		shell.setBackground(ColorConstants.black);
		CameraControlComposite cameraControlComposite = new CameraControlComposite(shell, new FormToolkit(display),
				SWT.None);

		cameraControlComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		shell.pack();
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

	public void stopSampleHistogram() {
		if (isSelected(btnSampleHistogram)) {
			deSelectControl(btnSampleHistogram);
			for (ICameraControlListener c : cameraControlListeners) {
				try {
					c.sampleHistogram(false);
				} catch (Exception e) {
					logger.error("TODO put description of error here", e);
					showError("Problem stopping Histogram", e);
				}
			}
		}
	}

	public void stopFlatHistogram() {
		if (isSelected(btnFlatHistogram)) {
			deSelectControl(btnFlatHistogram);
			for (ICameraControlListener c : cameraControlListeners) {
				try {
					c.flatHistogram(false);
				} catch (Exception e) {
					logger.error("TODO put description of error here", e);
					showError("Problem stopping Histogram", e);
				}
			}
		}
	}

	public void deselectSampleStream() {
		deSelectControl(btnSampleStream);
	}

	public void deselectFlatStream() {
		deSelectControl(btnFlatStream);
	}

	public void selectSampleIn() {
		selectControl(btnSampleIn);
		deSelectControl(btnSampleOut);
	}

	public void selectSampleOut() {
		selectControl(btnSampleOut);
		deSelectControl(btnSampleIn);
	}

	public String getSampleDescription() {
		return sampleDescription;
	}

	public void clearSampleDescription() {
		sampleDescription = null;
		getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				txtSampleDesc.setText(TYPE_DESCRIPTION);
			}
		});

	}

	public void setResolutionPixelSize(final String pixelSize) {
		if (pixelSize != null && lblObjectPixelSize != null && !lblObjectPixelSize.isDisposed()) {
			lblObjectPixelSize.getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					lblObjectPixelSize.setText(pixelSize);
				}
			});
		}
	}

	public void setResolution(final RESOLUTION res) {
		this.resolution = res;
		switch (res) {
		case FULL:
			selectRes(btnResFull);
			break;
		case TWO_X:
			selectRes(btnRes2x);
			break;
		case FOUR_X:
			selectRes(btnRes4x);
			break;
		case EIGHT_X:
			selectRes(btnRes8x);
			break;
		}
	}

	public RESOLUTION getResolution() {
		return resolution;
	}

	public void deSelectSampleHistogram() {
		deSelectControl(btnSampleHistogram);
	}

	public void startFlatHistogram() {
		logger.debug("'btnFlatHistogram' is selected");
		selectControl(btnFlatHistogram);
		try {
			for (ICameraControlListener cl : cameraControlListeners) {
				cl.flatHistogram(true);
			}
		} catch (Exception e1) {
			logger.debug("Error setting exposure time", e1);
		}
	}

	public int getFramesPerProjection() {
		return framesPerProjection;
	}

}