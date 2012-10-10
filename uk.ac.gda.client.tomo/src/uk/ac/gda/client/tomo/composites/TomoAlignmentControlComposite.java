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

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import org.eclipse.draw2d.ColorConstants;
import org.eclipse.draw2d.PositionConstants;
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
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Listener;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;
import org.eclipse.ui.forms.widgets.FormToolkit;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.client.tomo.ImageConstants;
import uk.ac.gda.client.tomo.TomoClientActivator;
import uk.ac.gda.client.tomo.composites.ModuleButtonComposite.CAMERA_MODULE;
import uk.ac.gda.client.tomo.composites.RotationButtonComposite.RotationButtonSelectionListener;
import uk.ac.gda.client.tomo.composites.RotationSliderComposite.SliderSelectionListener;

/**
 * Motion control composite - this contains
 * <p>
 * <li>module change buttons</li>
 * <li>rotation change sliders</li>
 * <li>rotation change buttons</li>
 * <li>centring buttons</li>
 * </p>
 * 
 * @author rsr31645
 */
public class TomoAlignmentControlComposite extends Composite {

	private static final String AUTO_FOCUS = "Auto Focus";
	private static final String RESOLUTION_PIXEL_SIZE = "Resolution : Pixel Size = ";
	private static final String SAMPLE_CENTRING_lbl = "Sample Centring";
	private static final String REGION_OF_INTEREST_lbl = "ROI";
	private static final String TOMOGRAPHY_PARAMETERS_lbl = "Tomography Parameters";

	/**
	 * Enum that caters to the motion control composite
	 */
	public enum MotionControlCentring {
		TILT, HORIZONTAL,

		FIND_AXIS_ROTATION, VERTICAL, MOVE_AXIS_OF_ROTATION
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

	public enum STREAM_STATE {
		FLAT_STREAM, SAMPLE_STREAM, NO_STREAM;
	}

	private STREAM_STATE streamState = STREAM_STATE.NO_STREAM;
	private static final int MOTION_COMPOSITE_HEIGHT = 120;
	private static final int CONTROL_COMPOSITE_HEIGHT = 100;

	private static final String ERR_PROBLEM_SETTING_SAMPLE_DESCRIPTION = "Problem setting sample Description";
	private static final String SAVE_ALIGNMENT_lbl = "Save Alignment";
	private static final String FOR_FUTURE_USE = "For Future Use";
	private static final String X_RAY_ENERGY_lbl = "X-ray energy \r (keV)";
	private static final String ENERGY_DEFAULT_VALUE = "112";
	private static final String EXP_TIME_MEASURE = " s";
	private static final String DEFAULT_EXP_TIME = "1.0";

	private static final String BLANK_STR = "";

	private static final String TYPE_DESCRIPTION = "type description";

	private static final String RESET_ROI = "Remove";

	private static final String DEFINE_ROI = "Define";

	private static final String ROTATION_AXIS_NOT_DEFINED_shortdesc = "Rotation Axis Not Defined";

	private static final String CAMERA_DIST_DEFAULT_VALUE = "341";

	private static final String CAMERA_DISTANCE_lbl = "Camera distance \r  (mm)";

	private static final String LAST_SAVED_lbl = "Last Saved: ";

	private static final String FIND_TOMO_AXIS_lbl = "Find Tomo Axis";

	private static final String ALIGN_TILT_lbl = "Align Tilt";

	private static final String TOMO_ALIGNMENT_lbl = "Tomo Alignment";

	private static final String BEAMLINE_CONTROL_lbl = "Beamline Control";

	private static final String SAMPLE_WEIGHT_TWENTY_TO_FIFTY_lbl = "20 - 50";

	private static final String SAMPLE_WEIGHT_ONE_TO_TEN_lbl = "1 - 10";

	private static final String SAMPLE_WEIGHT_LESS_THAN_1_lbl = "< 1";

	private static final String SAMPLE_WEIGHT_TEN_TO_TWENTY_lbl = "10 - 20";

	private static final String SAMPLE_WEIGHT_KG_lbl = "Sample Weight (kg)";

	private static final String SAMPLE_MOTION_CONTROL_lbl = "Sample Motion Control";

	private static final String HORIZONTAL_lbl = "Move Horizontal";

	private static final String MOVE_AXIS_OF_ROTATION_lbl = "Move Axis of Rotation";

	private static final String TOMO_ROTATION_AXIS_SPECIFIC = "Tomo Rotation Axis Specific";

	private static final String NOT_FOUND_key = "N O T     F O U N D";

	private List<ITomoAlignmentControlListener> tomoAlignmentControlListeners;

	private boolean rotationAxisFound = false;

	private double energy = Double.NaN;

	private SAMPLE_WEIGHT sampleWeight = SAMPLE_WEIGHT.TWENTY_TO_FIFTY;

	private static final String FRAMES_PER_PROJECTION = "Frames per Projection";

	private static final String RESOLUTION_8x = "8x";

	private static final String RESOLUTION_4x = "4x";

	private static final String RESOLUTION_2x = "2x";

	private static final String RESOLUTION_FULL = "Full";
	private static final String FLAT = "Flat";

	private int framesPerProjection = Integer.parseInt(FRAMES_PER_PROJECTION_DEFAULT_VAL);
	// Fonts
	private FontRegistry fontRegistry;

	private static final String BOLD_TEXT_12 = "bold-text_12";
	private static final String NORMAL_TEXT_9 = "normal-text_9";
	private static final String BOLD_TEXT_7 = "bold-text_6";
	private static final String BOLD_TEXT_9 = "bold-text_9";
	private static final String BOLD_TEXT_11 = "bold-text_11";
	private static final String NORMAL_TEXT_8 = "normal_text_8";
	private static final String NORMAL_TEXT_7 = "normal_text_7";

	//
	private static final Logger logger = LoggerFactory.getLogger(TomoAlignmentControlComposite.class);
	private static final String PLUS_90_DEG = "+90°";
	private static final String MINUS_90_DEG = "-90°";
	private static final String PLUS_DELTA_5 = "▲+5°";
	private static final String MINUS_DELTA_5 = "▲-5°";
	private static final String PLUS_ONE_EIGHTY_DEG = "+180°";
	private static final String ZERO_DEG = "0°";
	private static final String MINUS_ONE_EIGHTY_DEG = "-180°";
	private static final String FIND_AXIS_OF_ROTATION_label = "Find Rotation Axis";
	private static final String VERTICAL_lbl = "Move Vertical";
	private static final String SAMPLE = "Sample";
	private static final String MOVE_TOMO_AXIS_lbl = "Move Tomo Axis";

	private static final String FRAMES_PER_PROJECTION_DEFAULT_VAL = "1";

	private Label lblRotationAxisNotFound;

	protected ControlButton btnHorizontal;
	protected Label lblFindRotationAxis;
	protected Button btnTilt;
	protected Button btnFindAxisOfRotation;

	private ControlButton btnVertical;

	protected Button btnMoveAxisOfRotation;

	protected ModuleButtonComposite moduleButtonComposite;

	protected RotationButtonComposite btnLeftRotate;

	protected RotationButtonComposite btnRightRotate;

	protected TomoCoarseRotationComposite coarseRotation;

	protected TomoFineRotationComposite fineRotation;

	protected Text txtXrayEnergy;

	protected Button btnSampleWeightLessThan1;

	protected Button btnSampleWeight1to10;

	protected Button btnSampleWeight10to20;

	protected Button btnSampleWeight20to50;

	protected Text txtCameraDistance;

	private Text txtSampleDesc;
	private String sampleDescription;

	private StackLayout moveAxisRotationCmpLayout;
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
	private Button btnFlatToSample;

	private Button btnDefineRoi;
	private Button btnResetRoi;
	//
	private Button btnResFull;
	private Button btnRes2x;
	private Button btnRes4x;
	private Button btnRes8x;

	private Text txtResFramesPerProjection;

	private Composite flatDarkComposite;
	private StackLayout flatAndDarkCompositeStackLayout;
	private Composite flatAndDarkContainerComposite;
	private Label lblFlatDarkNotAvailable;

	private Label lblFlatExpTime;
	private Button btnSaveAlignment;
	private double flatExposureTime;

	private Label lblObjectPixelSize;

	private RESOLUTION resolution = RESOLUTION.FULL;
	private double sampleExposureTime;

	public enum SAMPLE_WEIGHT {
		LESS_THAN_ONE(SAMPLE_WEIGHT_LESS_THAN_1_lbl), ONE_TO_TEN(SAMPLE_WEIGHT_ONE_TO_TEN_lbl), TEN_TO_TWENTY(
				SAMPLE_WEIGHT_TEN_TO_TWENTY_lbl), TWENTY_TO_FIFTY(SAMPLE_WEIGHT_TWENTY_TO_FIFTY_lbl);

		private String value;

		@Override
		public String toString() {
			return value;
		}

		private SAMPLE_WEIGHT(String value) {
			this.value = value;
		}
	}

	private FocusListener focusListener = new FocusAdapter() {
		@Override
		public void focusLost(FocusEvent focusEvent) {
			if (focusEvent.getSource().equals(txtCameraDistance)) {
				logger.debug("Camera distance text box focus lost");
				try {
					for (ITomoAlignmentControlListener ml : tomoAlignmentControlListeners) {
						ml.resetCameraDistance();
					}
				} catch (Exception e) {
					showErrorDialog(new IllegalArgumentException("Error reseting camera distance", e));
				}
			} else if (focusEvent.getSource().equals(txtXrayEnergy)) {
				logger.debug("X-ray energy text box focus lost");
				try {
					for (ITomoAlignmentControlListener ml : tomoAlignmentControlListeners) {
						ml.resetXrayEnergy();
					}
				} catch (Exception e) {
					showErrorDialog(new IllegalArgumentException("Error reseting X-ray energy", e));
				}
			} else if (focusEvent.getSource().equals(txtSampleDesc)) {
				if (BLANK_STR.equals(txtSampleDesc.getText())) {
					txtSampleDesc.setText(TYPE_DESCRIPTION);
				}
			}
		}
	};

	private KeyAdapter txtKeyListener = new KeyAdapter() {
		@Override
		public void keyPressed(KeyEvent e) {
			// Camear distance
			if (e.getSource().equals(txtCameraDistance)) {
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
					if (isValid(Double.class, txtCameraDistance.getText())) {
						try {
							for (ITomoAlignmentControlListener ml : tomoAlignmentControlListeners) {
								ml.cameraDistanceChanged(Double.parseDouble(txtCameraDistance.getText()));
							}
						} catch (Exception e1) {
							logger.debug("Error setting exposure time", e1);
							showErrorDialog(new IllegalArgumentException("Invalid value to be set as camera distance",
									e1));
						}
						TomoAlignmentControlComposite.this.setFocus();
					} else {
						showErrorDialog(new IllegalArgumentException("Invalid value to be set as camera distance"));
					}
				}
			} else if (e.getSource().equals(txtXrayEnergy)) {
				// X-ray energy
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
					if (isValid(Double.class, txtXrayEnergy.getText())) {
						energy = Double.parseDouble(txtXrayEnergy.getText());
						try {
							for (ITomoAlignmentControlListener ml : tomoAlignmentControlListeners) {
								ml.xRayEnergyChanged(Double.parseDouble(txtXrayEnergy.getText()));
							}
						} catch (Exception e1) {
							logger.error("Error setting exposure time", e1);
						}
					} else {
						showErrorDialog(new IllegalArgumentException("Invalid value to be set as X-ray energy"));
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
	 * 
	 */
	private RotationButtonSelectionListener rotationButtonListener = new RotationButtonSelectionListener() {
		@Override
		public void handleButtonClicked(RotationButtonComposite button) {
			if (button.equals(btnLeftRotate)) {
				try {
					for (ITomoAlignmentControlListener mcl : tomoAlignmentControlListeners) {
						mcl.rotateLeft90();
					}
				} catch (Exception ex) {
					showErrorDialog(ex);
				}
			} else if (button.equals(btnRightRotate)) {
				try {
					for (ITomoAlignmentControlListener mcl : tomoAlignmentControlListeners) {
						mcl.rotateRight90();
					}
				} catch (Exception ex) {
					showErrorDialog(ex);
				}
			}
		}
	};

	private SliderSelectionListener sliderSelectionListener = new SliderSelectionListener() {
		@Override
		public void sliderMoved(RotationSliderComposite sliderComposite, double initialDegree, int totalWidth) {
			if (sliderComposite.equals(coarseRotation)) {
				try {
					for (ITomoAlignmentControlListener mcl : tomoAlignmentControlListeners) {
						double currentSliderDegree = coarseRotation.getCurrentSliderDegree();
						mcl.degreeMovedBy(currentSliderDegree - initialDegree);
					}
				} catch (Exception ex) {
					showErrorDialog(ex);
				}
			} else if (sliderComposite.equals(fineRotation)) {
				try {
					for (ITomoAlignmentControlListener mcl : tomoAlignmentControlListeners) {
						double currentSliderDegree = fineRotation.getCurrentSliderDegree();
						mcl.degreeMovedBy(currentSliderDegree - initialDegree);
					}
				} catch (Exception ex) {
					showErrorDialog(ex);
				}
			}
		}

		@Override
		public void sliderMovedTo(double deg) {
			try {
				for (ITomoAlignmentControlListener mcl : tomoAlignmentControlListeners) {
					mcl.degreeMovedTo(deg);
				}
			} catch (Exception ex) {
				showErrorDialog(ex);
			}
		}
	};

	public void addMotionControlListener(ITomoAlignmentControlListener motionControlListener) {
		logger.debug("Registering motion control listener");
		if (motionControlListener != null) {
			tomoAlignmentControlListeners.add(motionControlListener);
			// moduleButtonComposite.addModuleChangeListener(motionControlListener);
		}
	}

	private void showErrorDialog(Exception ex) {
		MessageDialog.openError(getShell(), "Error during alignment", ex.getMessage());
	}

	public void removeMotionControlListener(ITomoAlignmentControlListener motionControlListener) {
		logger.debug("Registering motion control listener");
		if (motionControlListener != null) {
			tomoAlignmentControlListeners.remove(motionControlListener);
			moduleButtonComposite.removeModuleChangeListener(motionControlListener);
		}
	}

	public TomoAlignmentControlComposite(Composite parent, FormToolkit toolkit, int style) {
		super(parent, style);
		logger.debug("Constructing motion control composite");
		/* Initialise Font Registry */
		initializeFontRegistry();
		/* Initalize the list of composites. */

		/**/
		tomoAlignmentControlListeners = new ArrayList<ITomoAlignmentControlListener>();
		/**/
		GridLayout layout = new GridLayout();
		setDefaultLayoutSettings(layout);
		this.setLayout(layout);

		/**/
		Composite upperPanel = createUpperPanel(toolkit);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.heightHint = CONTROL_COMPOSITE_HEIGHT;
		upperPanel.setLayoutData(layoutData);
		/**/
		Composite lowerPanel = createLowerPanel(toolkit);
		layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.heightHint = MOTION_COMPOSITE_HEIGHT;
		lowerPanel.setLayoutData(layoutData);
		/**/
	}

	/**
	 * @param toolkit
	 * @return {@link Composite}
	 */
	private Composite createUpperPanel(FormToolkit toolkit) {
		Composite upperPanel = toolkit.createComposite(this);

		GridLayout layout = new GridLayout(8, true);
		layout.horizontalSpacing = 2;
		layout.marginWidth = 0;
		layout.marginHeight = 2;
		upperPanel.setLayout(layout);
		upperPanel.setBackground(ColorConstants.black);

		// 1
		Composite expStreamSingleHistoComposite = createExpStreamSingleHistoComposite(toolkit, upperPanel);
		GridData ld = new GridData(GridData.FILL_BOTH);
		expStreamSingleHistoComposite.setLayoutData(ld);

		//
		Composite beamlineControlComposite = createBeamlineControlComposite(upperPanel, toolkit);
		ld = new GridData(GridData.FILL_BOTH);
		beamlineControlComposite.setLayoutData(ld);

		//
		Composite instrumentControlComposite = createExperimentInstrument(upperPanel, toolkit);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.horizontalSpan = 6;
		instrumentControlComposite.setLayoutData(layoutData);

		return upperPanel;
	}

	/**
	 * @param parent
	 * @param toolkit
	 * @return {@link Composite}
	 */
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

	/**
	 * @param parent
	 * @param toolkit
	 * @return {@link Composite}
	 */
	protected Composite createExperimentInstrument(Composite parent, FormToolkit toolkit) {
		Composite tomoAlignmentComposite = toolkit.createComposite(parent);
		int numCols = 10;
		GridLayout layout = new GridLayout(numCols, true);
		layout.verticalSpacing = 1;
		layout.horizontalSpacing = 2;
		layout.marginWidth = 2;
		layout.marginHeight = 0;
		tomoAlignmentComposite.setLayout(layout);

		Label lblInstrument = toolkit.createLabel(tomoAlignmentComposite, "Instrument");
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = numCols;
		gd.horizontalAlignment = SWT.CENTER;
		lblInstrument.setLayoutData(gd);
		lblInstrument.setFont(fontRegistry.get(BOLD_TEXT_11));

		ModuleButtonComposite moduleButtonComposite = new ModuleButtonComposite(tomoAlignmentComposite, toolkit);
		moduleButtonComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite cmpAutoFocus = toolkit.createComposite(tomoAlignmentComposite);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.horizontalSpan = 2;
		cmpAutoFocus.setLayoutData(layoutData);
		GridLayout gl = new GridLayout();
		gl.horizontalSpacing = 0;
		gl.verticalSpacing = 0;
		gl.marginWidth = 0;
		gl.marginHeight = 0;
		cmpAutoFocus.setLayout(gl);

		ControlButton btnAutoFocus = new ControlButton(toolkit, cmpAutoFocus, AUTO_FOCUS);
		btnAutoFocus.setFont(fontRegistry.get(NORMAL_TEXT_9));
		btnAutoFocus.setLayoutData(new GridData(GridData.FILL_BOTH));
		btnAutoFocus.addListener(SWT.MouseDown, ctrlMouseListener);

		Label lblAutoFocusLastDone = toolkit.createLabel(cmpAutoFocus, LAST_SAVED_lbl);
		lblAutoFocusLastDone.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite cmpTilt = toolkit.createComposite(tomoAlignmentComposite);
		GridData layoutData2 = new GridData(GridData.FILL_BOTH);
		layoutData2.horizontalSpan = 2;
		cmpTilt.setLayoutData(layoutData2);
		gl = new GridLayout();
		gl.horizontalSpacing = 0;
		gl.verticalSpacing = 0;
		gl.marginWidth = 0;
		gl.marginHeight = 0;
		cmpTilt.setLayout(gl);
		// Buttons for align tilts
		ControlButton btnTilt = new ControlButton(toolkit, cmpTilt, ALIGN_TILT_lbl);
		btnTilt.setFont(fontRegistry.get(NORMAL_TEXT_9));
		btnTilt.setLayoutData(new GridData(GridData.FILL_BOTH));
		btnTilt.addListener(SWT.MouseDown, ctrlMouseListener);

		Label lblTiltLastDone = toolkit.createLabel(cmpTilt, "Last Saved:");
		lblTiltLastDone.setLayoutData(new GridData(GridData.FILL_BOTH));
		//
		ControlButton btnFindAxisOfRotation = new ControlButton(toolkit, tomoAlignmentComposite, FIND_TOMO_AXIS_lbl,
				SWT.PUSH);
		GridData layoutData3 = new GridData(GridData.FILL_BOTH);
		layoutData3.horizontalSpan = 2;
		btnFindAxisOfRotation.setLayoutData(layoutData3);
		btnFindAxisOfRotation.setFont(fontRegistry.get(NORMAL_TEXT_9));
		btnFindAxisOfRotation.addListener(SWT.MouseDown, ctrlMouseListener);

		ControlButton btnMoveAxisOfRotation = new ControlButton(toolkit, tomoAlignmentComposite, MOVE_TOMO_AXIS_lbl,
				SWT.PUSH | SWT.WRAP);
		btnMoveAxisOfRotation.setBackground(ColorConstants.green);
		btnMoveAxisOfRotation.setFont(fontRegistry.get(NORMAL_TEXT_9));
		btnMoveAxisOfRotation.addListener(SWT.MouseDown, ctrlMouseListener);
		GridData layoutData4 = new GridData(GridData.FILL_BOTH);
		layoutData4.horizontalSpan = 2;
		btnMoveAxisOfRotation.setLayoutData(layoutData4);

		Composite cameraDistanceComposite = toolkit.createComposite(tomoAlignmentComposite);
		cameraDistanceComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		layout = new GridLayout();

		cameraDistanceComposite.setLayout(layout);
		cameraDistanceComposite.setBackground(ColorConstants.lightGray);

		Label lblCameraDistance = toolkit.createLabel(cameraDistanceComposite, CAMERA_DISTANCE_lbl, SWT.WRAP
				| SWT.CENTER);
		lblCameraDistance.setFont(fontRegistry.get(NORMAL_TEXT_9));

		layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.horizontalSpan = 2;
		layoutData.horizontalAlignment = SWT.CENTER;
		lblCameraDistance.setLayoutData(layoutData);
		lblCameraDistance.setBackground(ColorConstants.lightGray);

		Text txtCameraDistance = toolkit.createText(cameraDistanceComposite, CAMERA_DIST_DEFAULT_VALUE, SWT.CENTER);
		txtCameraDistance.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtCameraDistance.addFocusListener(focusListener);
		txtCameraDistance.addKeyListener(txtKeyListener);

		return tomoAlignmentComposite;
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

		TomoClientActivator tomoClientActivator = TomoClientActivator.getDefault();
		if (tomoClientActivator != null) {
			btnFlatToSample.setImage(tomoClientActivator.getImageRegistry().get(ImageConstants.ICON_DOWN_TO_UP));
		}
		btnFlatToSample.addSelectionListener(buttonSelectionListener);
		// 2

		Composite txtBoxComposite = toolkit.createComposite(expStreamSingleHistoComposite);
		ld = new GridData(GridData.FILL_VERTICAL);
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
		if (tomoClientActivator != null) {
			btnSampleToFlat.setImage(TomoClientActivator.getDefault().getImageRegistry()
					.get(ImageConstants.ICON_UP_TO_DOWN));
		}
		return expStreamSingleHistoComposite;
	}

	private Composite createLowerPanel(FormToolkit toolkit) {
		GridLayout layout;
		Composite motionControlCmp = toolkit.createComposite(this);
		layout = new GridLayout(14, true);
		setDefaultLayoutSettings(layout);
		motionControlCmp.setLayout(layout);

		// Sample Weight
		Composite sampleWeightComposite = createSampleWeightComposite(motionControlCmp, toolkit);
		sampleWeightComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		// Centring
		Composite centringComposite = createCentringComposite(motionControlCmp, toolkit);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.horizontalSpan = 7;
		centringComposite.setLayoutData(layoutData);

		Composite tomoParametersCmp = toolkit.createComposite(motionControlCmp);
		GridData layoutData2 = new GridData(GridData.FILL_BOTH);
		layoutData2.horizontalSpan = 5;
		tomoParametersCmp.setLayoutData(layoutData2);

		GridLayout gl = new GridLayout(10, false);
		setDefaultLayoutSettings(gl);

		tomoParametersCmp.setLayout(gl);

		Label lblTomoParameters = toolkit.createLabel(tomoParametersCmp, TOMOGRAPHY_PARAMETERS_lbl, SWT.CENTER);
		lblTomoParameters.setFont(fontRegistry.get(BOLD_TEXT_11));
		GridData layoutData3 = new GridData(GridData.FILL_HORIZONTAL);
		layoutData3.horizontalSpan = 10;
		lblTomoParameters.setLayoutData(layoutData3);

		Composite cmpHorizontalLine = toolkit.createComposite(tomoParametersCmp);
		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 10;
		layoutData.heightHint = 1;
		cmpHorizontalLine.setBackground(ColorConstants.black);
		cmpHorizontalLine.setLayoutData(layoutData);

		// ROI
		Composite roiComposite = createDefineRoiComposite(toolkit, tomoParametersCmp);
		GridData gd = new GridData(GridData.FILL_BOTH);
		gd.grabExcessHorizontalSpace = false;
		gd.horizontalSpan = 2;
		roiComposite.setLayoutData(gd);
		// Resolution
		Composite resolutionComposite = createResolutionComposite(toolkit, tomoParametersCmp);
		layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.grabExcessHorizontalSpace = false;
		layoutData.horizontalSpan = 3;
		resolutionComposite.setLayoutData(layoutData);

		// Tomo Scan Estimate
		Composite tomoScanEstimate = createTomoScanComposite(toolkit, tomoParametersCmp);
		layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.horizontalSpan = 5;
		layoutData.grabExcessHorizontalSpace = false;
		tomoScanEstimate.setLayoutData(layoutData);
		tomoScanEstimate.setBackground(ColorConstants.buttonDarkest);

		// Save
		Button btnSave = toolkit.createButton(motionControlCmp, SAVE_ALIGNMENT_lbl, SWT.PUSH | SWT.WRAP);
		if (TomoClientActivator.getDefault() != null) {
			btnSave.setImage(TomoClientActivator.getDefault().getImageRegistry().get(ImageConstants.ICON_SHOPPING_CART));
		}
		btnSave.setLayoutData(new GridData(GridData.FILL_BOTH));

		return motionControlCmp;
	}

	private Composite createTomoScanComposite(FormToolkit toolkit, Composite parent) {
		Composite tomoScanComposite = toolkit.createComposite(parent);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 2;
		layout.horizontalSpacing = 2;
		layout.verticalSpacing = 2;
		tomoScanComposite.setLayout(layout);
		tomoScanComposite.setBackground(ColorConstants.black);

		txtSampleDesc = toolkit.createText(tomoScanComposite, TYPE_DESCRIPTION);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.verticalAlignment = SWT.TOP;
		txtSampleDesc.setLayoutData(layoutData);
		txtSampleDesc.setForeground(ColorConstants.lightGray);
		txtSampleDesc.addModifyListener(descModifyListener);
		txtSampleDesc.addFocusListener(focusListener);
		txtSampleDesc.addKeyListener(txtKeyListener);

		Composite infoCmp = toolkit.createComposite(tomoScanComposite);
		GridData layoutData2 = new GridData(GridData.FILL_BOTH);
		infoCmp.setLayoutData(layoutData2);
		infoCmp.setLayout(new FillLayout());
		toolkit.createLabel(infoCmp, "Information space");

		return tomoScanComposite;
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

	/**
	 * @param toolkit
	 * @param parent
	 * @return {@link Composite} container for the "Define ROI" and "Hide ROI" buttons
	 */
	private Composite createDefineRoiComposite(FormToolkit toolkit, Composite parent) {
		Composite defineRoiComposite = toolkit.createComposite(parent);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 2;
		layout.horizontalSpacing = 2;
		layout.verticalSpacing = 0;
		defineRoiComposite.setLayout(layout);

		Label lblROI = toolkit.createLabel(defineRoiComposite, REGION_OF_INTEREST_lbl, SWT.CENTER | SWT.WRAP);
		lblROI.setLayoutData(new GridData(GridData.FILL_BOTH));

		Button btnDefineRoi = toolkit.createButton(defineRoiComposite, DEFINE_ROI, SWT.PUSH);
		btnDefineRoi.setLayoutData(new GridData(GridData.FILL_BOTH));
		// TODO - Ravi fix
		// btnDefineRoi.addSelectionListener(buttonSelectionListener);
		//
		Button btnResetRoi = toolkit.createButton(defineRoiComposite, RESET_ROI, SWT.PUSH);
		btnResetRoi.setLayoutData(new GridData(GridData.FILL_BOTH));
		// btnResetRoi.addSelectionListener(buttonSelectionListener);
		return defineRoiComposite;
	}

	private Composite createCentringComposite(Composite motionControlCmp, FormToolkit toolkit) {
		Composite centringCmp = toolkit.createComposite(motionControlCmp);
		GridLayout layout = new GridLayout(3, false);
		layout.marginHeight = 1;
		layout.marginWidth = 1;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		centringCmp.setLayout(layout);
		centringCmp.setBackground(ColorConstants.black);

		Label lblCentring = toolkit.createLabel(centringCmp, SAMPLE_CENTRING_lbl, SWT.CENTER);
		lblCentring.setFont(fontRegistry.get(BOLD_TEXT_11));
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 3;
		lblCentring.setLayoutData(layoutData);

		btnHorizontal = new ControlButton(toolkit, centringCmp, HORIZONTAL_lbl, SWT.PUSH);
		btnHorizontal.setFont(fontRegistry.get(NORMAL_TEXT_9));
		btnHorizontal.setLayoutData(new GridData(GridData.FILL_BOTH));
		btnHorizontal.addListener(SWT.MouseDown, ctrlMouseListener);

		Composite tomoRotationComposite = createAxisRotationComposite(centringCmp, toolkit);
		GridData gd = new GridData(GridData.FILL_VERTICAL);
		gd.verticalSpan = 2;
		gd.widthHint = 720;// multiple of 12
		gd.verticalAlignment = SWT.BOTTOM;
		tomoRotationComposite.setLayoutData(gd);

		Composite rotationStageComposite = createRotationStageComposite(centringCmp, toolkit);
		GridData gd1 = new GridData(GridData.FILL_BOTH);
		gd1.verticalSpan = 2;
		// gd1.widthHint = 105;
		rotationStageComposite.setLayoutData(gd1);

		btnVertical = new ControlButton(toolkit, centringCmp, VERTICAL_lbl, SWT.PUSH);
		btnVertical.setFont(fontRegistry.get(NORMAL_TEXT_9));
		btnVertical.setLayoutData(new GridData(GridData.FILL_BOTH));
		btnVertical.addListener(SWT.MouseDown, ctrlMouseListener);

		return centringCmp;

	}

	private Composite createResolutionComposite(FormToolkit toolkit, Composite parent) {
		Composite resolutionComposite = toolkit.createComposite(parent);

		GridLayout layout1 = new GridLayout();
		layout1.verticalSpacing = 0;
		layout1.horizontalSpacing = 0;
		layout1.marginWidth = 2;
		layout1.marginHeight = 2;
		layout1.numColumns = 2;
		layout1.makeColumnsEqualWidth = false;
		resolutionComposite.setLayout(layout1);

		Label lblTomoResolution = toolkit.createLabel(resolutionComposite, RESOLUTION_PIXEL_SIZE, SWT.LEFT);
		lblTomoResolution.setLayoutData(new GridData(GridData.FILL_BOTH));

		Label lblObjectPixelSize = toolkit.createLabel(resolutionComposite, "0.000 mm", SWT.LEFT_TO_RIGHT);
		lblObjectPixelSize.setLayoutData(new GridData(GridData.FILL_BOTH));

		//
		Composite upperRowComposite = toolkit.createComposite(resolutionComposite);
		GridLayout layout = new GridLayout(4, true);
		layout.horizontalSpacing = 2;
		layout.verticalSpacing = 2;
		layout.marginWidth = 2;
		layout.marginHeight = 2;

		upperRowComposite.setLayout(layout);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.horizontalSpan = 2;
		upperRowComposite.setLayoutData(layoutData);

		Button btnResFull = toolkit.createButton(upperRowComposite, RESOLUTION_FULL, SWT.PUSH);
		btnResFull.setLayoutData(new GridData(GridData.FILL_BOTH));
		// btnResFull.addSelectionListener(buttonSelectionListener);
		btnResFull.setFont(fontRegistry.get(NORMAL_TEXT_7));

		Button btnRes2x = toolkit.createButton(upperRowComposite, RESOLUTION_2x, SWT.PUSH);
		btnRes2x.setLayoutData(new GridData(GridData.FILL_BOTH));
		// btnRes2x.addSelectionListener(buttonSelectionListener);
		btnRes2x.setFont(fontRegistry.get(NORMAL_TEXT_7));

		Button btnRes4x = toolkit.createButton(upperRowComposite, RESOLUTION_4x, SWT.PUSH);
		btnRes4x.setLayoutData(new GridData(GridData.FILL_BOTH));
		// btnRes4x.addSelectionListener(buttonSelectionListener);
		btnRes4x.setFont(fontRegistry.get(NORMAL_TEXT_7));

		Button btnRes8x = toolkit.createButton(upperRowComposite, RESOLUTION_8x, SWT.PUSH);
		btnRes8x.setLayoutData(new GridData(GridData.FILL_BOTH));
		// btnRes8x.addSelectionListener(buttonSelectionListener);
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
		GridData layoutData2 = new GridData(GridData.FILL_BOTH);
		layoutData2.horizontalSpan = 2;
		lowerRowComposite.setLayoutData(layoutData2);

		Label lblNumFrames = toolkit.createLabel(lowerRowComposite, FRAMES_PER_PROJECTION, SWT.CENTER | SWT.WRAP);
		GridData ld = new GridData(GridData.FILL_BOTH);
		ld.verticalAlignment = SWT.CENTER;
		ld.horizontalSpan = 3;
		lblNumFrames.setLayoutData(ld);
		lblNumFrames.setFont(fontRegistry.get(NORMAL_TEXT_9));

		Text txtResFramesPerProjection = toolkit.createText(lowerRowComposite, FRAMES_PER_PROJECTION_DEFAULT_VAL,
				SWT.CENTER);
		txtResFramesPerProjection.setLayoutData(new GridData(GridData.FILL_BOTH));
		txtResFramesPerProjection.addKeyListener(txtKeyListener);
		txtResFramesPerProjection.addFocusListener(focusListener);
		return resolutionComposite;
	}

	private Composite createSampleWeightComposite(Composite motionControlCmp, FormToolkit toolkit) {
		Composite sampleWeightComposite = toolkit.createComposite(motionControlCmp);

		GridLayout layout = new GridLayout(2, true);
		sampleWeightComposite.setLayout(layout);

		Label lblSampleWeight = toolkit.createLabel(sampleWeightComposite, SAMPLE_WEIGHT_KG_lbl);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		gd.horizontalAlignment = SWT.CENTER;
		lblSampleWeight.setFont(fontRegistry.get(BOLD_TEXT_11));
		lblSampleWeight.setLayoutData(gd);

		sampleWeightComposite.setBackground(ColorConstants.lightGreen);
		lblSampleWeight.setBackground(ColorConstants.lightGreen);
		//
		btnSampleWeightLessThan1 = toolkit.createButton(sampleWeightComposite, SAMPLE_WEIGHT_LESS_THAN_1_lbl, SWT.PUSH);
		btnSampleWeightLessThan1.setLayoutData(new GridData(GridData.FILL_BOTH));
		btnSampleWeightLessThan1.addListener(SWT.MouseDown, ctrlMouseListener);
		//
		btnSampleWeight1to10 = toolkit.createButton(sampleWeightComposite, SAMPLE_WEIGHT_ONE_TO_TEN_lbl, SWT.PUSH);
		btnSampleWeight1to10.setLayoutData(new GridData(GridData.FILL_BOTH));
		btnSampleWeight1to10.addListener(SWT.MouseDown, ctrlMouseListener);
		//
		btnSampleWeight10to20 = toolkit.createButton(sampleWeightComposite, SAMPLE_WEIGHT_TEN_TO_TWENTY_lbl, SWT.PUSH);
		btnSampleWeight10to20.setLayoutData(new GridData(GridData.FILL_BOTH));
		btnSampleWeight10to20.addListener(SWT.MouseDown, ctrlMouseListener);
		//
		btnSampleWeight20to50 = toolkit
				.createButton(sampleWeightComposite, SAMPLE_WEIGHT_TWENTY_TO_FIFTY_lbl, SWT.PUSH);
		btnSampleWeight20to50.setLayoutData(new GridData(GridData.FILL_BOTH));
		btnSampleWeight20to50.addListener(SWT.MouseDown, ctrlMouseListener);

		btnSampleWeight20to50.setSelection(true);
		return sampleWeightComposite;
	}

	/**
	 * @param parent
	 * @param toolkit
	 * @return {@link Composite} that contains the rotation stage.
	 */
	protected Composite createRotationStageComposite(Composite parent, FormToolkit toolkit) {
		Composite rotationStageComposite = toolkit.createComposite(parent);
		GridLayout layout = new GridLayout(2, true);
		setDefaultLayoutSettings(layout);
		rotationStageComposite.setLayout(layout);

		btnLeftRotate = new RotationButtonComposite(rotationStageComposite, PositionConstants.WEST, MINUS_90_DEG, true);
		btnLeftRotate.addSelectionListener(rotationButtonListener);

		GridData layoutData = new GridData(GridData.FILL_BOTH);
		btnLeftRotate.setLayoutData(layoutData);

		btnRightRotate = new RotationButtonComposite(rotationStageComposite, PositionConstants.EAST, PLUS_90_DEG, true);
		btnRightRotate.addSelectionListener(rotationButtonListener);

		layoutData = new GridData(GridData.FILL_BOTH);
		btnRightRotate.setLayoutData(layoutData);

		return rotationStageComposite;
	}

	/**
	 * @param motionControlComposite
	 * @param toolkit
	 * @return {@link Composite} axis rotation composite
	 */
	protected Composite createAxisRotationComposite(Composite motionControlComposite, FormToolkit toolkit) {
		Composite axisRotationCmp = toolkit.createComposite(motionControlComposite);
		GridLayout layout = new GridLayout();
		setDefaultLayoutSettings(layout);
		layout.marginWidth = 2;
		layout.marginHeight = 2;
		axisRotationCmp.setLayout(layout);
		coarseRotation = new TomoCoarseRotationComposite(axisRotationCmp, SWT.DOWN, new String[] {
				MINUS_ONE_EIGHTY_DEG, MINUS_90_DEG, ZERO_DEG, PLUS_90_DEG, PLUS_ONE_EIGHTY_DEG }, true);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 55;
		coarseRotation.setLayoutData(gd);
		coarseRotation.addSliderEventListener(sliderSelectionListener);

		fineRotation = new TomoFineRotationComposite(axisRotationCmp, SWT.UP, new String[] { MINUS_DELTA_5,
				PLUS_DELTA_5 }, true);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.heightHint = 40;
		fineRotation.setLayoutData(gd);
		fineRotation.addSliderEventListener(sliderSelectionListener);

		return axisRotationCmp;
	}

	/**
	 * @param motionControlComposite
	 * @return {@link Composite} which contains the camera module buttons.
	 */
	protected Composite createCameraModulesComposite(Composite motionControlComposite, FormToolkit toolkit) {
		moduleButtonComposite = new ModuleButtonComposite(motionControlComposite, toolkit);
		return moduleButtonComposite;
	}

	/**
	 * @param motionControlComposite
	 * @param toolkit
	 * @return {@link Composite} which contains the centring buttons
	 */
	private Composite createCentringButtonsComposite(Composite motionControlComposite, FormToolkit toolkit) {
		Composite centringButtonsComposite = toolkit.createComposite(motionControlComposite);
		GridLayout layout = new GridLayout(2, false);
		layout.verticalSpacing = 2;
		layout.horizontalSpacing = 2;
		layout.marginWidth = 2;
		layout.marginHeight = 2;

		centringButtonsComposite.setLayout(layout);

		Group tomoAxisRotationGroup = new Group(centringButtonsComposite, SWT.BORDER);
		tomoAxisRotationGroup.setBackground(ColorConstants.white);
		tomoAxisRotationGroup.setText(TOMO_ROTATION_AXIS_SPECIFIC);
		tomoAxisRotationGroup.setLayoutData(new GridData(GridData.FILL_BOTH));
		layout = new GridLayout();
		layout.verticalSpacing = 2;
		layout.horizontalSpacing = 2;
		layout.marginWidth = 2;
		layout.marginHeight = 2;
		tomoAxisRotationGroup.setLayout(layout);

		/**/
		btnFindAxisOfRotation = toolkit.createButton(tomoAxisRotationGroup, FIND_AXIS_OF_ROTATION_label, SWT.PUSH);
		GridData ld = new GridData(GridData.FILL_BOTH);
		btnFindAxisOfRotation.setLayoutData(ld);
		btnFindAxisOfRotation.addListener(SWT.MouseDown, ctrlMouseListener);

		/**/
		Composite statusComposite = toolkit.createComposite(tomoAxisRotationGroup);
		GridData statusCLd = new GridData(GridData.FILL_BOTH);
		statusComposite.setLayoutData(statusCLd);
		layout = new GridLayout();
		setDefaultLayoutSettings(layout);
		layout.marginWidth = 2;
		layout.marginHeight = 2;
		statusComposite.setLayout(layout);
		statusComposite.setBackground(ColorConstants.black);

		lblFindRotationAxis = toolkit.createLabel(statusComposite, NOT_FOUND_key, SWT.CENTER | SWT.BORDER_DOT);
		lblFindRotationAxis.setFont(fontRegistry.get(NORMAL_TEXT_9));
		GridData layoutData1 = new GridData(GridData.FILL_BOTH);
		layoutData1.verticalAlignment = SWT.CENTER;
		lblFindRotationAxis.setBackground(ColorConstants.red);
		lblFindRotationAxis.setLayoutData(layoutData1);

		/**/
		btnMoveAxisOfRotation = toolkit.createButton(tomoAxisRotationGroup, MOVE_AXIS_OF_ROTATION_lbl, SWT.PUSH);
		GridData ld1 = new GridData(GridData.FILL_BOTH);
		btnMoveAxisOfRotation.setLayoutData(ld1);
		btnMoveAxisOfRotation.addListener(SWT.MouseDown, ctrlMouseListener);
		btnMoveAxisOfRotation.setEnabled(false);

		/**/
		Group sampleMotionControlGroup = new Group(centringButtonsComposite, SWT.BORDER);
		sampleMotionControlGroup.setText(SAMPLE_MOTION_CONTROL_lbl);
		sampleMotionControlGroup.setBackground(ColorConstants.white);
		layout = new GridLayout(2, true);
		layout.verticalSpacing = 2;
		layout.horizontalSpacing = 2;
		layout.marginWidth = 2;
		layout.marginHeight = 2;
		sampleMotionControlGroup.setLayout(layout);
		sampleMotionControlGroup.setLayoutData(new GridData(GridData.FILL_BOTH));

		// btnHorizontal = toolkit.createButton(sampleMotionControlGroup, HORIZONTAL_lbl, SWT.PUSH);
		ld = new GridData(GridData.FILL_BOTH);
		ld.verticalSpan = 2;
		btnHorizontal.setLayoutData(ld);
		btnHorizontal.addListener(SWT.MouseDown, ctrlMouseListener);
		/**/
		// btnVertical = toolkit.createButton(sampleMotionControlGroup, lbl_VERTICAL, SWT.PUSH);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.verticalSpan = 2;
		btnVertical.setLayoutData(layoutData);
		btnVertical.addListener(SWT.MouseDown, ctrlMouseListener);

		/**/
		return centringButtonsComposite;
	}

	private void initializeFontRegistry() {
		if (Display.getCurrent() != null) {
			fontRegistry = new FontRegistry(Display.getCurrent());
			String fontName = Display.getCurrent().getSystemFont().getFontData()[0].getName();
			fontRegistry.put(BOLD_TEXT_12, new FontData[] { new FontData(fontName, 12, SWT.BOLD) });
			fontRegistry.put(BOLD_TEXT_9, new FontData[] { new FontData(fontName, 9, SWT.BOLD) });
			fontRegistry.put(NORMAL_TEXT_9, new FontData[] { new FontData(fontName, 9, SWT.NORMAL) });
			fontRegistry.put(BOLD_TEXT_7, new FontData[] { new FontData(fontName, 7, SWT.BOLD) });
			fontRegistry.put(BOLD_TEXT_11, new FontData[] { new FontData(fontName, 11, SWT.BOLD) });
			fontRegistry.put(NORMAL_TEXT_7, new FontData[] { new FontData(fontName, 7, SWT.NORMAL) });
			fontRegistry.put(NORMAL_TEXT_8, new FontData[] { new FontData(fontName, 8, SWT.NORMAL) });

		}
	}

	/**
	 * @param layout
	 */
	protected void setDefaultLayoutSettings(GridLayout layout) {
		layout.verticalSpacing = 0;
		layout.horizontalSpacing = 0;
		layout.marginWidth = 0;
		layout.marginHeight = 0;
	}

	/**
	 * Delegate to move the rotation slider.
	 * 
	 * @param degree
	 */
	public void moveRotationSlider(double degree) {
		coarseRotation.moveSlider(degree);
	}

	/**
	 * Delegate to move the rotation slider.
	 * 
	 * @param degree
	 */
	public void moveRotationSliderTo(double degree) {
		coarseRotation.moveSliderTo(degree);
	}

	/**
	 * listener for buttons and slider - but this is control masked - the "Ctrl" key needs to be pressed
	 */
	private Listener ctrlMouseListener = new Listener() {

		@Override
		public void handleEvent(Event event) {
			// to check whether the control key is pressend along with the mouse button click
			if (event.stateMask == SWT.CTRL) {
				Object sourceObj = event.widget;
				try {
					if (sourceObj == btnHorizontal) {
						// Button - Center Current position
						if (!isSelected(btnHorizontal)) {
							try {
								switchOn(MotionControlCentring.HORIZONTAL);
							} catch (Exception ex) {
								switchOff(MotionControlCentring.HORIZONTAL);
							}
						} else {
							switchOff(MotionControlCentring.HORIZONTAL);
						}
					} else if (sourceObj == btnFindAxisOfRotation) {
						// Button - Half Rotation Tool
						if (!isSelected(btnFindAxisOfRotation)) {
							try {
								switchOn(MotionControlCentring.FIND_AXIS_ROTATION);
							} catch (Exception ex) {
								switchOff(MotionControlCentring.FIND_AXIS_ROTATION);
							}
						} else {
							switchOff(MotionControlCentring.FIND_AXIS_ROTATION);

						}
					} else if (sourceObj == btnMoveAxisOfRotation) {
						// Button - Half Rotation Tool
						if (!isSelected(btnMoveAxisOfRotation)) {
							try {
								switchOn(MotionControlCentring.MOVE_AXIS_OF_ROTATION);
							} catch (Exception ex) {
								switchOff(MotionControlCentring.MOVE_AXIS_OF_ROTATION);
							}
						} else {
							switchOff(MotionControlCentring.MOVE_AXIS_OF_ROTATION);

						}
					} else if (sourceObj == btnVertical) {
						// Button - Vertical
						if (!isSelected(btnVertical)) {
							try {
								switchOn(MotionControlCentring.VERTICAL);
							} catch (Exception ex) {
								switchOff(MotionControlCentring.VERTICAL);
							}
						} else {
							switchOff(MotionControlCentring.VERTICAL);
						}
					} else if (sourceObj == btnTilt) {
						// Button - Vertical
						if (!isSelected(btnTilt)) {
							try {
								switchOn(MotionControlCentring.TILT);
							} catch (Exception ex) {
								switchOff(MotionControlCentring.TILT);
							}
						} else {
							switchOff(MotionControlCentring.TILT);
						}
					} else if (sourceObj == btnSampleWeightLessThan1) {
						// Button - Vertical
						if (!isSelected(btnSampleWeightLessThan1)) {
							selectSampleWeightControlInUi(btnSampleWeightLessThan1);
							//
							for (ITomoAlignmentControlListener mcl : tomoAlignmentControlListeners) {
								mcl.setSampleWeight(SAMPLE_WEIGHT.LESS_THAN_ONE);
							}
						}
					} else if (sourceObj == btnSampleWeight1to10) {
						// Button - Vertical
						if (!isSelected(btnSampleWeight1to10)) {
							selectSampleWeightControlInUi(btnSampleWeight1to10);
							//
							for (ITomoAlignmentControlListener mcl : tomoAlignmentControlListeners) {
								mcl.setSampleWeight(SAMPLE_WEIGHT.ONE_TO_TEN);
							}
						}
					} else if (sourceObj == btnSampleWeight10to20) {
						// Button - Vertical
						if (!isSelected(btnSampleWeight10to20)) {
							selectSampleWeightControlInUi(btnSampleWeight10to20);

							//
							for (ITomoAlignmentControlListener mcl : tomoAlignmentControlListeners) {
								mcl.setSampleWeight(SAMPLE_WEIGHT.TEN_TO_TWENTY);
							}
						}
					} else if (sourceObj == btnSampleWeight20to50) {
						// Button - Vertical
						if (!isSelected(btnSampleWeight20to50)) {
							selectSampleWeightControlInUi(btnSampleWeight20to50);
							//
							for (ITomoAlignmentControlListener mcl : tomoAlignmentControlListeners) {
								mcl.setSampleWeight(SAMPLE_WEIGHT.TWENTY_TO_FIFTY);
							}
						}
					}
				} catch (Exception ex) {
					logger.error(((Button) sourceObj).getText() + ": problem switching off", ex);
				}
			}

		}
	};

	private Composite moveAxisBtnComposite;

	private Label lblCameraDistance;

	/**
	 * Returns <code>true</code> if the colors as set when selected.
	 * 
	 * @param button
	 * @return true when background color is lightgray and foreground color is red - this is what was set when the
	 *         widget was selected.
	 */
	public static boolean isSelected(Button button) {
		if (ColorConstants.red.equals(button.getForeground())
				&& ColorConstants.lightGray.equals(button.getBackground())) {
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
	public static boolean isSelected(ControlButton button) {
		if (ColorConstants.red.equals(button.getForeground())
				&& ColorConstants.lightGray.equals(button.getBackground())) {
			return true;
		}
		return false;
	}

	/**
	 * Method to run in the UI thread to set the colors to show the button selected
	 * 
	 * @param btnCntrl
	 */
	private static void selectControl(final Button btnCntrl) {
		btnCntrl.getDisplay().asyncExec(new Runnable() {

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
			btnCntrl.getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					btnCntrl.setForeground(btnCntrl.getDisplay().getSystemColor(SWT.COLOR_LIST_FOREGROUND));
					btnCntrl.setBackground(btnCntrl.getDisplay().getSystemColor(SWT.COLOR_LIST_BACKGROUND));
				}
			});
		}
	}

	/**
	 * @return the selected {@link MotionControlCentring} button
	 */
	public MotionControlCentring getSelectedCentring() {
		if (isSelected(btnHorizontal)) {
			return MotionControlCentring.HORIZONTAL;
		} else if (isSelected(btnFindAxisOfRotation)) {
			return MotionControlCentring.FIND_AXIS_ROTATION;
		} else if (isSelected(btnMoveAxisOfRotation)) {
			return MotionControlCentring.MOVE_AXIS_OF_ROTATION;
		} else if (isSelected(btnVertical)) {
			return MotionControlCentring.VERTICAL;
		} else if (isSelected(btnTilt)) {
			return MotionControlCentring.TILT;
		}
		return null;
	}

	/**
	 * Method to switch ON the specific Centring button
	 * 
	 * @param centringButton
	 * @throws Exception
	 */
	public void switchOn(MotionControlCentring centringButton) throws Exception {
		switch (centringButton) {
		case TILT:
			doTilt(true);
			break;
		case FIND_AXIS_ROTATION:
			doFindAxisOfRotation(true);
			break;
		case HORIZONTAL:
			doHorizontal(true);
			break;
		case MOVE_AXIS_OF_ROTATION:
			doMoveAxisOfRotation(true);
			break;
		case VERTICAL:
			doVertical(true);
			break;
		}

	}

	/**
	 * Method to switch OFF the specific Centring button
	 * 
	 * @param centringButton
	 * @throws Exception
	 */
	public void switchOff(MotionControlCentring centringButton) throws Exception {
		switch (centringButton) {
		case TILT:
			doTilt(false);
			break;
		case FIND_AXIS_ROTATION:
			doFindAxisOfRotation(false);
			break;
		case HORIZONTAL:
			doHorizontal(false);
			break;
		case MOVE_AXIS_OF_ROTATION:
			doMoveAxisOfRotation(false);
			break;
		case VERTICAL:
			doVertical(false);
			break;
		}
	}

	public static void main(String[] args) {
		final Display display = new Display();
		final Shell shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setBounds(new org.eclipse.swt.graphics.Rectangle(0, 0, 100, 400));
		shell.setLayout(new GridLayout());
		shell.setBackground(ColorConstants.black);
		TomoAlignmentControlComposite motionControlComposite = new TomoAlignmentControlComposite(shell,
				new FormToolkit(display), SWT.None);

		GridData layoutData = new GridData(GridData.FILL_BOTH);
		motionControlComposite.setLayoutData(layoutData);
		shell.pack();
		shell.open();

		while (!shell.isDisposed()) {
			if (!display.readAndDispatch()) {
				display.sleep();
			}
		}
		display.dispose();
	}

	public void showRotationButtonsDeselected() {
		btnLeftRotate.showButtonDeSelected();
		btnRightRotate.showButtonDeSelected();
		coarseRotation.showButtonDeSelected();
		fineRotation.moveSliderTo(0);
	}

	@Override
	public void dispose() {
		try {
			logger.info("Disposing composite");
			coarseRotation.dispose();
			fineRotation.dispose();
			//
			fontRegistry = null;

			tomoAlignmentControlListeners.clear();
		} catch (Exception ex) {
			logger.error("While disposing");
		}
		//
		super.dispose();
	}

	/**
	 * Set the relevant button enabled given the camera module that is selected.
	 * 
	 * @param cameraModule
	 */
	public void setCameraModule(CAMERA_MODULE cameraModule) {
		if (moduleButtonComposite != null) {
			moduleButtonComposite.switchOn(cameraModule);
		}
	}

	/**
	 * the camera module that is currently selected on the GUI
	 * 
	 * @return {@link CAMERA_MODULE} that is currently enabled on the GUI
	 */
	public CAMERA_MODULE getSelectedCameraModule() {
		if (moduleButtonComposite != null) {
			return moduleButtonComposite.getModuleSelected();
		}
		return CAMERA_MODULE.NO_MODULE;
	}

	public void setRotationMotorBusy(boolean isBusy) {
		coarseRotation.setMotorBusy(isBusy);
	}

	public void deselectModule() {
		if (moduleButtonComposite != null) {
			moduleButtonComposite.deselectAll();
		}
	}

	public void setTomoAxisFound(boolean found) {
		if (found) {
			rotationAxisFound = true;
			moveAxisRotationCmpLayout.topControl = btnMoveAxisOfRotation;
			moveAxisBtnComposite.layout();
		}
	}

	public boolean isRotationAxisFound() {
		return rotationAxisFound;
	}

	public void setModuleButtonText(String units, String btn1Text, String btn2Text, String btn3Text, String btn4Text) {
		if (moduleButtonComposite != null) {
			moduleButtonComposite.setModuleButtonText(units, btn1Text, btn2Text, btn3Text, btn4Text);
		}
	}

	public void setCameraMotionPosition(final double cameraMotionPosition) {
		if (txtCameraDistance != null && !txtCameraDistance.isDisposed()) {
			txtCameraDistance.getDisplay().asyncExec(new Runnable() {

				@Override
				public void run() {
					txtCameraDistance.setText(String.format("%.5g", cameraMotionPosition));
				}
			});
		}
	}

	public void setEnergyValue(final double energy) {
		this.energy = energy;
		getDisplay().asyncExec(new Runnable() {

			@Override
			public void run() {
				txtXrayEnergy.setText(Double.toString(energy));
			}
		});
	}

	public double getEnergy() {
		return energy;
	}

	// Sample Weight controls

	public void setSampleWeightUI(final SAMPLE_WEIGHT sampleWeight) {
		this.sampleWeight = sampleWeight;

		getDisplay().syncExec(new Runnable() {

			@Override
			public void run() {
				switch (sampleWeight) {
				case ONE_TO_TEN:
					selectSampleWeightControlInUi(btnSampleWeight1to10);
					break;
				case TEN_TO_TWENTY:
					selectSampleWeightControlInUi(btnSampleWeight10to20);
					break;
				case TWENTY_TO_FIFTY:
					selectSampleWeightControlInUi(btnSampleWeight20to50);
					break;
				case LESS_THAN_ONE:
					selectSampleWeightControlInUi(btnSampleWeightLessThan1);
					break;
				}
			}
		});
	}

	protected void selectSampleWeightControlInUi(Button btnSampleWt) {
		final Button[] btns = new Button[] { btnSampleWeightLessThan1, btnSampleWeight1to10, btnSampleWeight10to20,
				btnSampleWeight20to50 };
		for (Button button : btns) {
			if (btnSampleWt.equals(button)) {
				selectControl(button);
			} else {
				deSelectControl(button);
			}
		}
	}

	public SAMPLE_WEIGHT getSampleWeight() {
		return sampleWeight;
	}

	private void switchMotionCentring(boolean switchOn, Button btnSelected) {
		// TODO - Ravi fix
		Button[] selectableBtns = new Button[] { btnTilt,
				// btnVertical, btnHorizontal,
				btnMoveAxisOfRotation, btnFindAxisOfRotation };

		if (switchOn) {
			for (Button button : selectableBtns) {
				if (button.equals(btnSelected)) {
					selectControl(button);
				} else {
					deSelectControl(button);
					disable(button);
				}
			}

			disableCommonControls();
		} else {
			for (Button button : selectableBtns) {
				if (button.equals(btnSelected)) {
					deSelectControl(button);
				} else {
					enable(button);
				}
			}

			enableCommonControls();

		}
	}

	private void doTilt(boolean switchOn) throws Exception {
		logger.debug("'Tilt' - is selected");
		switchMotionCentring(switchOn, btnTilt);
		try {
			for (ITomoAlignmentControlListener mcl : getTomoControlListeners()) {
				mcl.tilt(switchOn);
			}
		} catch (Exception ex) {
			showErrorDialog(ex);
			throw ex;
		}
	}

	private void doVertical(boolean switchOn) throws Exception {
		logger.debug("'Tilt' - is selected");
		// switchMotionCentring(switchOn, btnVertical);
		try {
			for (ITomoAlignmentControlListener mcl : getTomoControlListeners()) {
				mcl.vertical(switchOn);
			}
		} catch (Exception ex) {
			showErrorDialog(ex);
			throw ex;
		}

	}

	private Collection<ITomoAlignmentControlListener> getTomoControlListeners() {
		return Collections.unmodifiableCollection(tomoAlignmentControlListeners);
	}

	private void doHorizontal(boolean switchOn) throws Exception {
		logger.debug("'Tilt' - is selected");
		// switchMotionCentring(switchOn, btnHorizontal);
		try {
			for (ITomoAlignmentControlListener mcl : getTomoControlListeners()) {
				mcl.horizontal(switchOn);
			}
		} catch (Exception ex) {
			showErrorDialog(ex);
			throw ex;
		}

	}

	private void doFindAxisOfRotation(boolean switchOn) throws Exception {
		logger.debug("'Tilt' - is selected");
		switchMotionCentring(switchOn, btnFindAxisOfRotation);
		try {
			for (ITomoAlignmentControlListener mcl : getTomoControlListeners()) {
				mcl.findRotationAxis(switchOn);
			}
		} catch (Exception ex) {
			showErrorDialog(ex);
			throw ex;
		}

	}

	private void doMoveAxisOfRotation(boolean switchOn) throws Exception {
		logger.debug("'Tilt' - is selected");
		switchMotionCentring(switchOn, btnMoveAxisOfRotation);
		try {
			for (ITomoAlignmentControlListener mcl : getTomoControlListeners()) {
				mcl.moveAxisOfRotation(switchOn);
			}
		} catch (Exception ex) {
			showErrorDialog(ex);
			throw ex;
		}
	}

	private Control[] getCommonControlList() {
		Control[] disableList = new Control[] { txtXrayEnergy, btnSampleWeight20to50, btnSampleWeight1to10,
				btnSampleWeight10to20, btnSampleWeightLessThan1, txtCameraDistance };
		return disableList;
	}

	private void enableCommonControls() {
		Control[] enableControlList = getCommonControlList();

		for (Control control : enableControlList) {
			enable(control);
		}
	}

	private void disableCommonControls() {
		Control[] disableControlList = getCommonControlList();

		for (Control control : disableControlList) {
			disable(control);
		}
	}

	private static void enable(Control control) {
		control.setEnabled(true);
	}

	private static void disable(Control control) {
		control.setEnabled(false);
	}

	public STREAM_STATE getStreamState() {
		return streamState;
	}

	public synchronized void setStreamState(STREAM_STATE streamState) {
		this.streamState = streamState;
	}

	/**
	 * selection listener for the buttons
	 */
	private SelectionListener buttonSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent event) {
			Object sourceObj = event.getSource();
			if (sourceObj == btnFlatToSample) {
				setPreferredSampleExposureTime(Double.parseDouble(txtFlatExpTime.getText()));
				try {
					for (ITomoAlignmentControlListener lis : getTomoControlListeners()) {
						lis.sampleExposureTimeChanged(Double.parseDouble(txtSampleExposureTime.getText()));
					}
				} catch (Exception e1) {
					logger.debug("Error setting exposure time", e1);
				}
			} else if (sourceObj == btnSampleToFlat) {
				setPreferredFlatExposureTime(Double.parseDouble(txtSampleExposureTime.getText()));
				try {
					for (ITomoAlignmentControlListener lis : getTomoControlListeners()) {
						lis.flatExposureTimeChanged(Double.parseDouble(txtFlatExpTime.getText()));
					}
				} catch (Exception e1) {
					logger.debug("Error setting exposure time", e1);
				}
			} else if (sourceObj == btnDefineRoi) {
				if (!isSelected(btnDefineRoi)) {
					logger.debug("'btnDefineRoi' is selected");
					selectControl(btnDefineRoi);
					try {
						for (ITomoAlignmentControlListener tcl : getTomoControlListeners()) {
							tcl.defineRoi(true);
						}
					} catch (IllegalStateException s) {
						// showError("Cannot Define ROI", s);
						showErrorDialog(s);
						deSelectControl(btnDefineRoi);
					} catch (Exception e1) {
						logger.debug("Error setting exposure time", e1);
						// showError("Cannot Define ROI", e1);
						showErrorDialog(e1);
					}
				} else {
					logger.debug("'btnDefineRoi' is de-selected");
					deSelectControl(btnDefineRoi);
					try {
						for (ITomoAlignmentControlListener tcl : getTomoControlListeners()) {
							tcl.defineRoi(false);
						}
					} catch (Exception e1) {
						logger.debug("Error setting exposure time", e1);
					}
				}
			} else if (sourceObj == btnResetRoi) {
				logger.debug("'btnResetRoi' is selected");
				try {
					for (ITomoAlignmentControlListener tcl : getTomoControlListeners()) {
						tcl.resetRoi();
					}
				} catch (IllegalStateException s) {
					// showError("Cannot Reset ROI", s);
					showErrorDialog(s);
				} catch (Exception e1) {
					logger.debug("Error setting exposure time", e1);
					// showError("Cannot Define ROI", e1);
					showErrorDialog(e1);
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
			} else if (sourceObj == btnSaveAlignment) {
				logger.debug("Save alignment clicked");
				try {
					for (ITomoAlignmentControlListener tcl : getTomoControlListeners()) {
						tcl.saveAlignmentConfiguration();
					}
				} catch (Exception e) {
					logger.error("Error Saving:{}", e.getMessage());
				}
			}
		}

	};

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

	public void setResolutionPixelSize(String resolutionPixelSize) {
		// TODO Auto-generated method stub

	}

	public void clearSampleDescription() {

	}

	public double getSampleExposureTime() {
		return sampleExposureTime;
	}

	public double getFlatExposureTime() {
		return flatExposureTime;
	}

	public String getSampleDescription() {
		return sampleDescription;
	}

	public RESOLUTION getResolution() {
		return resolution;
	}

	public int getFramesPerProjection() {
		return framesPerProjection;
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

}