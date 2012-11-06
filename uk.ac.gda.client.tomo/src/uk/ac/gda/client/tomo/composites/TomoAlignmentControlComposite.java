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
import org.eclipse.swt.graphics.Color;
import org.eclipse.swt.graphics.FontData;
import org.eclipse.swt.layout.FillLayout;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
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

	private static final Color NORMAL_COLOR = ColorConstants.black;
	private static final Color ERROR_COLOR = ColorConstants.red;
	private static final String INSTRUMENT = "Instrument";
	private static final String BEAMLINE_CONTROL = "Beamline Control";
	private static final String AUTO_FOCUS_lbl = "Auto Focus";
	private static final String RESOLUTION_PIXEL_SIZE = "Resolution : Pixel Size = ";
	private static final String SAMPLE_CENTRING_lbl = "Sample Alignment";
	private static final String REGION_OF_INTEREST_lbl = "ROI";
	private static final String TOMOGRAPHY_PARAMETERS_lbl = "Tomography Parameters";
	private Button btnAutoFocus;

	/**
	 * Enum that caters to the motion control composite
	 */
	public enum MotionControlCentring {
		TILT, HORIZONTAL,

		FIND_AXIS_ROTATION, VERTICAL, MOVE_AXIS_OF_ROTATION, AUTO_FOCUS
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

	private static final int MOTION_COMPOSITE_HEIGHT = 120;
	private static final int CONTROL_COMPOSITE_HEIGHT = 100;

	private static final String SAVE_ALIGNMENT_lbl = "Save Alignment";
	private static final String FOR_FUTURE_USE = "For Future Use";
	private static final String X_RAY_ENERGY_lbl = "X-ray energy \r (keV)";
	private static final String ENERGY_DEFAULT_VALUE = "112";

	private static final String BLANK_STR = "";

	private static final String TYPE_DESCRIPTION = "type description";

	private static final String RESET_ROI = "Remove";

	private static final String DEFINE_ROI = "Define";

	private static final String CAMERA_DIST_DEFAULT_VALUE = "341";

	private static final String CAMERA_DISTANCE_lbl = "Camera distance \r  (mm)";

	private static final String LAST_SAVED_lbl = "Last Saved: ";

	private static final String FIND_TOMO_AXIS_lbl = "Find Rotation Axis";

	private static final String ALIGN_TILT_lbl = "Align Tilt";

	private static final String SAMPLE_WEIGHT_TWENTY_TO_FIFTY_lbl = "20 - 50";

	private static final String SAMPLE_WEIGHT_ONE_TO_TEN_lbl = "1 - 10";

	private static final String SAMPLE_WEIGHT_LESS_THAN_1_lbl = "< 1";

	private static final String SAMPLE_WEIGHT_TEN_TO_TWENTY_lbl = "10 - 20";

	private static final String SAMPLE_WEIGHT_KG_lbl = "Sample Weight (kg)";

	private static final String HORIZONTAL_lbl = "Move Horizontal";

	private List<ITomoAlignmentControlListener> tomoAlignmentControlListeners;

	private boolean rotationAxisFound = false;

	private double energy = Double.NaN;

	private SAMPLE_WEIGHT sampleWeight = SAMPLE_WEIGHT.TWENTY_TO_FIFTY;

	private static final String FRAMES_PER_PROJECTION = "Frames per Projection";

	private static final String RESOLUTION_8x = "8x";

	private static final String RESOLUTION_4x = "4x";

	private static final String RESOLUTION_2x = "2x";

	private static final String RESOLUTION_FULL = "Full";

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
	private static final String VERTICAL_lbl = "Move Vertical";
	private static final String MOVE_TOMO_AXIS_lbl = "Move Rotation Axis";

	private static final String FRAMES_PER_PROJECTION_DEFAULT_VAL = "1";

	protected Button btnHorizontal;
	protected Label lblFindRotationAxis;
	protected Button btnTilt;
	protected Button btnFindAxisOfRotation;

	private Button btnVertical;

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

	private Button btnDefineRoi;
	private Button btnResetRoi;
	//
	private Button btnResFull;
	private Button btnRes2x;
	private Button btnRes4x;
	private Button btnRes8x;

	private Text txtResFramesPerProjection;
	private Button btnSaveAlignment;

	private Label lblObjectPixelSize;

	private Button chkTomoParameters;
	private Button chkInstrument;
	private Button chkSampleAlignment;
	private Button chkSampleWeight;

	private RESOLUTION resolution = RESOLUTION.FULL;

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
		public void focusGained(FocusEvent focusEvent) {
			if (focusEvent.getSource().equals(txtSampleDesc)) {
				if (TYPE_DESCRIPTION.equals(txtSampleDesc.getText())) {
					txtSampleDesc.setText(BLANK_STR);
				}
			}
		}

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
				} else {
					sampleDescription = txtSampleDesc.getText();
				}
			}
		}
	};

	private KeyAdapter txtKeyListener = new KeyAdapter() {
		@Override
		public void keyPressed(KeyEvent e) {
			if (e.getSource().equals(txtCameraDistance)) {
				// Camera distance
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
			} else if (e.getSource().equals(txtSampleDesc)) {
				// Sample description
				if (e.keyCode == SWT.CR || e.keyCode == SWT.KEYPAD_CR) {
					if (!TYPE_DESCRIPTION.equals(txtSampleDesc.getText())) {
						sampleDescription = txtSampleDesc.getText();
						chkTomoParameters.setFocus();
					} else {
						showErrorDialog(new IllegalArgumentException("Problem setting description"));
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
			moduleButtonComposite.addModuleChangeListener(motionControlListener);
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
		upperPanel.setBackground(NORMAL_COLOR);

		// 1
		// Composite expStreamSingleHistoComposite = createSampleFlatButtons(toolkit, upperPanel);
		// GridData ld = new GridData(GridData.FILL_BOTH);
		// expStreamSingleHistoComposite.setLayoutData(ld);

		//
		Composite beamlineControlComposite = createBeamlineControlComposite(upperPanel, toolkit);
		GridData ld = new GridData(GridData.FILL_BOTH);
		beamlineControlComposite.setLayoutData(ld);

		//
		Composite instrumentControlComposite = createExperimentInstrument(upperPanel, toolkit);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.horizontalSpan = 7;
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

		final Button lblBeamlineControl = toolkit.createButton(beamlineControlComposite, BEAMLINE_CONTROL, SWT.CHECK);
		lblBeamlineControl.setFont(fontRegistry.get(BOLD_TEXT_11));
		lblBeamlineControl.setSelection(true);
		lblBeamlineControl.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				super.widgetSelected(e);
				if (!lblBeamlineControl.getSelection()) {
					lblBeamlineControl.setSelection(true);
				}
			}
		});

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
		setDefaultLayoutSettings(layout);
		xRayEnergyComposite.setLayout(layout);
		xRayEnergyComposite.setBackground(ColorConstants.lightGray);

		Label lblXrayEnergy = toolkit.createLabel(xRayEnergyComposite, X_RAY_ENERGY_lbl, SWT.WRAP | SWT.CENTER);
		lblXrayEnergy.setLayoutData(new GridData(GridData.FILL_BOTH));
		lblXrayEnergy.setFont(fontRegistry.get(NORMAL_TEXT_9));
		lblXrayEnergy.setBackground(ColorConstants.lightGray);

		txtXrayEnergy = toolkit.createText(xRayEnergyComposite, ENERGY_DEFAULT_VALUE, SWT.CENTER);
		txtXrayEnergy.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtXrayEnergy.addFocusListener(focusListener);
		txtXrayEnergy.addKeyListener(txtKeyListener);
		txtXrayEnergy.setEnabled(false);

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

		chkInstrument = toolkit.createButton(tomoAlignmentComposite, INSTRUMENT, SWT.CHECK);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = numCols;
		gd.horizontalAlignment = SWT.CENTER;
		chkInstrument.setLayoutData(gd);
		chkInstrument.setFont(fontRegistry.get(BOLD_TEXT_11));
		chkInstrument.addSelectionListener(chkboxSelection);

		moduleButtonComposite = new ModuleButtonComposite(tomoAlignmentComposite, toolkit);
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

		btnAutoFocus = toolkit.createButton(cmpAutoFocus, AUTO_FOCUS_lbl, SWT.PUSH);
		btnAutoFocus.setFont(fontRegistry.get(NORMAL_TEXT_9));
		btnAutoFocus.setLayoutData(new GridData(GridData.FILL_BOTH));
		btnAutoFocus.addListener(SWT.MouseDown, ctrlMouseListener);
		ButtonSelectionUtil.decorateControlButton(btnAutoFocus);

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
		btnTilt = toolkit.createButton(cmpTilt, ALIGN_TILT_lbl, SWT.PUSH);
		btnTilt.setFont(fontRegistry.get(NORMAL_TEXT_9));
		btnTilt.setLayoutData(new GridData(GridData.FILL_BOTH));
		btnTilt.addListener(SWT.MouseDown, ctrlMouseListener);
		ButtonSelectionUtil.decorateControlButton(btnTilt);

		Label lblTiltLastDone = toolkit.createLabel(cmpTilt, "Last Saved:");
		lblTiltLastDone.setLayoutData(new GridData(GridData.FILL_BOTH));
		//
		btnFindAxisOfRotation = toolkit.createButton(tomoAlignmentComposite, FIND_TOMO_AXIS_lbl, SWT.PUSH);
		GridData layoutData3 = new GridData(GridData.FILL_BOTH);
		layoutData3.horizontalSpan = 2;
		btnFindAxisOfRotation.setLayoutData(layoutData3);
		btnFindAxisOfRotation.setFont(fontRegistry.get(NORMAL_TEXT_9));
		btnFindAxisOfRotation.addListener(SWT.MouseDown, ctrlMouseListener);
		ButtonSelectionUtil.decorateControlButton(btnFindAxisOfRotation);

		btnMoveAxisOfRotation = toolkit.createButton(tomoAlignmentComposite, MOVE_TOMO_AXIS_lbl, SWT.PUSH);
		// btnMoveAxisOfRotation.setBackground(ColorConstants.green);
		btnMoveAxisOfRotation.setFont(fontRegistry.get(NORMAL_TEXT_9));
		btnMoveAxisOfRotation.addListener(SWT.MouseDown, ctrlMouseListener);
		GridData layoutData4 = new GridData(GridData.FILL_BOTH);
		layoutData4.horizontalSpan = 2;
		btnMoveAxisOfRotation.setLayoutData(layoutData4);
		ButtonSelectionUtil.decorateControlButton(btnMoveAxisOfRotation);

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

		txtCameraDistance = toolkit.createText(cameraDistanceComposite, CAMERA_DIST_DEFAULT_VALUE, SWT.CENTER);
		txtCameraDistance.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtCameraDistance.addFocusListener(focusListener);
		txtCameraDistance.addKeyListener(txtKeyListener);

		return tomoAlignmentComposite;
	}

	private Composite createLowerPanel(FormToolkit toolkit) {
		GridLayout layout;
		Composite motionControlCmp = toolkit.createComposite(this);
		layout = new GridLayout(14, true);
		layout.horizontalSpacing = 2;
		layout.marginWidth = 2;
		layout.verticalSpacing = 2;
		layout.marginHeight = 2;
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

		int numCols = 5;
		GridLayout gl = new GridLayout(numCols, true);
		gl.horizontalSpacing = 2;
		gl.marginHeight = 2;
		gl.marginWidth = 2;
		gl.verticalSpacing = 2;
		tomoParametersCmp.setLayout(gl);

		chkTomoParameters = toolkit.createButton(tomoParametersCmp, TOMOGRAPHY_PARAMETERS_lbl, SWT.CHECK);
		chkTomoParameters.setFont(fontRegistry.get(BOLD_TEXT_11));
		GridData layoutData3 = new GridData(GridData.FILL_HORIZONTAL);
		layoutData3.horizontalSpan = numCols;
		layoutData3.horizontalAlignment = SWT.CENTER;
		chkTomoParameters.setLayoutData(layoutData3);
		chkTomoParameters.addSelectionListener(chkboxSelection);

		Composite cmpHorizontalLine = toolkit.createComposite(tomoParametersCmp);
		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = numCols;
		layoutData.heightHint = 1;
		cmpHorizontalLine.setBackground(NORMAL_COLOR);
		cmpHorizontalLine.setLayoutData(layoutData);

		// ROI
		Composite roiComposite = createDefineRoiComposite(toolkit, tomoParametersCmp);
		GridData gd = new GridData(GridData.FILL_BOTH);
		roiComposite.setLayoutData(gd);
		// Resolution
		Composite resolutionComposite = createResolutionComposite(toolkit, tomoParametersCmp);
		layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.horizontalSpan = 2;
		resolutionComposite.setLayoutData(layoutData);

		// Tomo Scan Estimate
		Composite tomoScanEstimate = createTomoScanComposite(toolkit, tomoParametersCmp);
		layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.horizontalSpan = 2;
		tomoScanEstimate.setLayoutData(layoutData);
		tomoScanEstimate.setBackground(ColorConstants.buttonDarkest);

		// Save
		btnSaveAlignment = toolkit.createButton(motionControlCmp, SAVE_ALIGNMENT_lbl, SWT.PUSH | SWT.WRAP);
		if (TomoClientActivator.getDefault() != null) {
			btnSaveAlignment.setImage(TomoClientActivator.getDefault().getImageRegistry()
					.get(ImageConstants.ICON_SHOPPING_CART));
		}
		GridData layoutData4 = new GridData(GridData.FILL_BOTH);
		btnSaveAlignment.setLayoutData(layoutData4);
		btnSaveAlignment.addSelectionListener(buttonSelectionListener);

		return motionControlCmp;
	}

	private Composite createTomoScanComposite(FormToolkit toolkit, Composite parent) {
		Composite tomoScanComposite = toolkit.createComposite(parent);
		GridLayout layout = new GridLayout();
		layout.marginHeight = 0;
		layout.marginWidth = 0;
		layout.horizontalSpacing = 2;
		layout.verticalSpacing = 1;
		tomoScanComposite.setLayout(layout);

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
				txtSampleDesc.setForeground(NORMAL_COLOR);
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
		layout.marginHeight = 2;
		layout.marginWidth = 2;
		layout.horizontalSpacing = 2;
		layout.verticalSpacing = 2;
		defineRoiComposite.setLayout(layout);

		Label lblROI = toolkit.createLabel(defineRoiComposite, REGION_OF_INTEREST_lbl, SWT.CENTER | SWT.WRAP);
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

	private Composite createCentringComposite(Composite motionControlCmp, FormToolkit toolkit) {
		Composite centringCmp = toolkit.createComposite(motionControlCmp);
		GridLayout layout = new GridLayout(3, false);
		layout.marginHeight = 1;
		layout.marginWidth = 1;
		layout.horizontalSpacing = 0;
		layout.verticalSpacing = 0;
		centringCmp.setLayout(layout);
		centringCmp.setBackground(NORMAL_COLOR);

		Composite lblCmp = toolkit.createComposite(centringCmp);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 3;
		layout = new GridLayout();
		setDefaultLayoutSettings(layout);
		lblCmp.setLayout(layout);
		lblCmp.setLayoutData(layoutData);

		chkSampleAlignment = toolkit.createButton(lblCmp, SAMPLE_CENTRING_lbl, SWT.CHECK);
		chkSampleAlignment.setFont(fontRegistry.get(BOLD_TEXT_11));
		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalAlignment = SWT.CENTER;
		chkSampleAlignment.setLayoutData(layoutData);
		chkSampleAlignment.addSelectionListener(chkboxSelection);

		btnHorizontal = toolkit.createButton(centringCmp, HORIZONTAL_lbl, SWT.PUSH);
		btnHorizontal.setFont(fontRegistry.get(NORMAL_TEXT_9));
		btnHorizontal.setLayoutData(new GridData(GridData.FILL_BOTH));
		btnHorizontal.addListener(SWT.MouseDown, ctrlMouseListener);
		ButtonSelectionUtil.decorateControlButton(btnHorizontal);

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

		btnVertical = toolkit.createButton(centringCmp, VERTICAL_lbl, SWT.PUSH);
		btnVertical.setFont(fontRegistry.get(NORMAL_TEXT_9));
		btnVertical.setLayoutData(new GridData(GridData.FILL_BOTH));
		btnVertical.addListener(SWT.MouseDown, ctrlMouseListener);
		ButtonSelectionUtil.decorateControlButton(btnVertical);

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

		lblObjectPixelSize = toolkit.createLabel(resolutionComposite, "0.000 mm", SWT.LEFT_TO_RIGHT);
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

		btnResFull = toolkit.createButton(upperRowComposite, RESOLUTION_FULL, SWT.PUSH);
		btnResFull.setLayoutData(new GridData(GridData.FILL_BOTH));
		btnResFull.addSelectionListener(buttonSelectionListener);
		btnResFull.setFont(fontRegistry.get(NORMAL_TEXT_7));
		ButtonSelectionUtil.setButtonSelected(btnResFull);

		btnRes2x = toolkit.createButton(upperRowComposite, RESOLUTION_2x, SWT.PUSH);
		btnRes2x.setLayoutData(new GridData(GridData.FILL_BOTH));
		btnRes2x.addSelectionListener(buttonSelectionListener);
		btnRes2x.setFont(fontRegistry.get(NORMAL_TEXT_7));

		btnRes4x = toolkit.createButton(upperRowComposite, RESOLUTION_4x, SWT.PUSH);
		btnRes4x.setLayoutData(new GridData(GridData.FILL_BOTH));
		btnRes4x.addSelectionListener(buttonSelectionListener);
		btnRes4x.setFont(fontRegistry.get(NORMAL_TEXT_7));

		btnRes8x = toolkit.createButton(upperRowComposite, RESOLUTION_8x, SWT.PUSH);
		btnRes8x.setLayoutData(new GridData(GridData.FILL_BOTH));
		btnRes8x.addSelectionListener(buttonSelectionListener);
		btnRes8x.setFont(fontRegistry.get(NORMAL_TEXT_7));

		Composite horizontalBar = toolkit.createComposite(resolutionComposite);
		layoutData = new GridData(GridData.FILL_HORIZONTAL);
		horizontalBar.setBackground(NORMAL_COLOR);
		layoutData.heightHint = 1;
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

		txtResFramesPerProjection = toolkit
				.createText(lowerRowComposite, FRAMES_PER_PROJECTION_DEFAULT_VAL, SWT.CENTER);
		txtResFramesPerProjection.setLayoutData(new GridData(GridData.FILL_BOTH));
		txtResFramesPerProjection.addKeyListener(txtKeyListener);
		txtResFramesPerProjection.addFocusListener(focusListener);
		return resolutionComposite;
	}

	private Composite createSampleWeightComposite(Composite motionControlCmp, FormToolkit toolkit) {
		Composite sampleWeightComposite = toolkit.createComposite(motionControlCmp);

		GridLayout layout = new GridLayout(2, true);
		setDefaultLayoutSettings(layout);
		sampleWeightComposite.setLayout(layout);

		chkSampleWeight = toolkit.createButton(sampleWeightComposite, SAMPLE_WEIGHT_KG_lbl, SWT.CHECK);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		gd.horizontalAlignment = SWT.CENTER;
		chkSampleWeight.setFont(fontRegistry.get(BOLD_TEXT_9));
		chkSampleWeight.setLayoutData(gd);
		chkSampleWeight.addSelectionListener(chkboxSelection);

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
					if (btnHorizontal.equals(sourceObj)) {
						// Button - Center Current position
						if (!ButtonSelectionUtil.isButtonSelected(btnHorizontal)) {
							try {
								switchOn(MotionControlCentring.HORIZONTAL);
							} catch (Exception ex) {
								switchOff(MotionControlCentring.HORIZONTAL);
							}
						} else {
							switchOff(MotionControlCentring.HORIZONTAL);
						}
					} else if (btnFindAxisOfRotation.equals(sourceObj)) {
						// Button - Half Rotation Tool
						if (!ButtonSelectionUtil.isButtonSelected(btnFindAxisOfRotation)) {
							try {
								switchOn(MotionControlCentring.FIND_AXIS_ROTATION);
							} catch (Exception ex) {
								switchOff(MotionControlCentring.FIND_AXIS_ROTATION);
							}
						} else {
							switchOff(MotionControlCentring.FIND_AXIS_ROTATION);

						}
					} else if (btnMoveAxisOfRotation.equals(sourceObj)) {
						// Button - Half Rotation Tool
						if (!ButtonSelectionUtil.isButtonSelected(btnMoveAxisOfRotation)) {
							try {
								switchOn(MotionControlCentring.MOVE_AXIS_OF_ROTATION);
							} catch (Exception ex) {
								switchOff(MotionControlCentring.MOVE_AXIS_OF_ROTATION);
							}
						} else {
							switchOff(MotionControlCentring.MOVE_AXIS_OF_ROTATION);

						}
					} else if (btnVertical.equals(sourceObj)) {
						// Button - Vertical
						if (!ButtonSelectionUtil.isButtonSelected(btnVertical)) {
							try {
								switchOn(MotionControlCentring.VERTICAL);
							} catch (Exception ex) {
								switchOff(MotionControlCentring.VERTICAL);
							}
						} else {
							switchOff(MotionControlCentring.VERTICAL);
						}
					} else if (btnTilt.equals(sourceObj)) {
						// Button - Tilt
						if (!ButtonSelectionUtil.isCtrlButtonSelected(btnTilt)) {
							try {
								switchOn(MotionControlCentring.TILT);
							} catch (Exception ex) {
								switchOff(MotionControlCentring.TILT);
							}
						} else {
							switchOff(MotionControlCentring.TILT);
						}
					} else if (btnAutoFocus.equals(sourceObj)) {
						// Button - Auto-focus
						if (!ButtonSelectionUtil.isCtrlButtonSelected(btnAutoFocus)) {
							try {
								switchOn(MotionControlCentring.AUTO_FOCUS);
							} catch (Exception ex) {
								switchOff(MotionControlCentring.AUTO_FOCUS);
							}
						} else {
							switchOff(MotionControlCentring.AUTO_FOCUS);
						}
					} else if (sourceObj == btnSampleWeightLessThan1) {
						// Button - Vertical
						if (!ButtonSelectionUtil.isButtonSelected(btnSampleWeightLessThan1)) {
							selectSampleWeightControlInUi(btnSampleWeightLessThan1);
							//
							for (ITomoAlignmentControlListener mcl : tomoAlignmentControlListeners) {
								mcl.setSampleWeight(SAMPLE_WEIGHT.LESS_THAN_ONE);
							}
						}
					} else if (sourceObj == btnSampleWeight1to10) {
						// Button - Vertical
						if (!ButtonSelectionUtil.isButtonSelected(btnSampleWeight1to10)) {
							selectSampleWeightControlInUi(btnSampleWeight1to10);
							//
							for (ITomoAlignmentControlListener mcl : tomoAlignmentControlListeners) {
								mcl.setSampleWeight(SAMPLE_WEIGHT.ONE_TO_TEN);
							}
						}
					} else if (sourceObj == btnSampleWeight10to20) {
						// Button - Vertical
						if (!ButtonSelectionUtil.isButtonSelected(btnSampleWeight10to20)) {
							selectSampleWeightControlInUi(btnSampleWeight10to20);

							//
							for (ITomoAlignmentControlListener mcl : tomoAlignmentControlListeners) {
								mcl.setSampleWeight(SAMPLE_WEIGHT.TEN_TO_TWENTY);
							}
						}
					} else if (sourceObj == btnSampleWeight20to50) {
						// Button - Vertical
						if (!ButtonSelectionUtil.isButtonSelected(btnSampleWeight20to50)) {
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

	/**
	 * @return the selected {@link MotionControlCentring} button
	 */
	public MotionControlCentring getSelectedCentring() {
		if (ButtonSelectionUtil.isCtrlButtonSelected(btnHorizontal)) {
			return MotionControlCentring.HORIZONTAL;
		} else if (ButtonSelectionUtil.isCtrlButtonSelected(btnFindAxisOfRotation)) {
			return MotionControlCentring.FIND_AXIS_ROTATION;
		} else if (ButtonSelectionUtil.isCtrlButtonSelected(btnMoveAxisOfRotation)) {
			return MotionControlCentring.MOVE_AXIS_OF_ROTATION;
		} else if (ButtonSelectionUtil.isCtrlButtonSelected(btnVertical)) {
			return MotionControlCentring.VERTICAL;
		} else if (ButtonSelectionUtil.isCtrlButtonSelected(btnTilt)) {
			return MotionControlCentring.TILT;
		} else if (ButtonSelectionUtil.isCtrlButtonSelected(btnAutoFocus)) {
			return MotionControlCentring.AUTO_FOCUS;
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
		case AUTO_FOCUS:
			doAutoFocus(true);
			break;
		}

	}

	private void doAutoFocus(boolean switchOn) throws Exception {
		switchMotionCentring(switchOn, btnAutoFocus);
		try {
			for (ITomoAlignmentControlListener mcl : getTomoControlListeners()) {
				mcl.autoFocus(switchOn);
			}
		} catch (Exception ex) {
			showErrorDialog(ex);
			throw ex;
		}
	}

	/**
	 * Method to switch OFF the specific Centring button
	 * 
	 * @param centringButton
	 */
	public void switchOff(final MotionControlCentring centringButton) {
		if (getDisplay() != null && !getDisplay().isDisposed()) {
			getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					try {
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
						case AUTO_FOCUS:
							doAutoFocus(false);
							break;
						}
					} catch (Exception e) {
						showErrorDialog(e);
						logger.error("Problem switching of centring", e);
					}
				}
			});

		}
	}

	public static void main(String[] args) {
		final Display display = new Display();
		final Shell shell = new Shell(display, SWT.SHELL_TRIM);
		shell.setBounds(new org.eclipse.swt.graphics.Rectangle(0, 0, 100, 400));
		shell.setLayout(new GridLayout());
		shell.setBackground(NORMAL_COLOR);
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
				ButtonSelectionUtil.setButtonSelected(button);
			} else {
				ButtonSelectionUtil.setButtonDeselected(button);
			}
		}
	}

	public SAMPLE_WEIGHT getSampleWeight() {
		return sampleWeight;
	}

	private void switchMotionCentring(boolean switchOn, Button btnSelected) {
		// TODO - Ravi fix
		Button[] selectableBtns = new Button[] { btnTilt, btnVertical, btnHorizontal, btnMoveAxisOfRotation,
				btnFindAxisOfRotation, btnAutoFocus };

		if (switchOn) {
			for (Button button : selectableBtns) {
				if (button.equals(btnSelected)) {
					ButtonSelectionUtil.setControlButtonSelected(button);
				} else {
					ButtonSelectionUtil.setControlButtonDeselected(button);
					disable(button);
				}
			}

			disableCommonControls();
		} else {
			for (Button button : selectableBtns) {
				if (button.equals(btnSelected)) {
					ButtonSelectionUtil.setControlButtonDeselected(button);
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
		switchMotionCentring(switchOn, btnVertical);
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
		logger.debug("'Horizontal' - is selected");
		switchMotionCentring(switchOn, btnHorizontal);
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
		logger.debug("'Find Axis of Rotation' - is selected");
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
		logger.debug("'Move Axis of Rotation' - is selected");
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

	/**
	 * selection listener for the buttons
	 */
	private SelectionListener buttonSelectionListener = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent event) {
			Object sourceObj = event.getSource();
			if (sourceObj == btnDefineRoi) {
				if (!ButtonSelectionUtil.isButtonSelected(btnDefineRoi)) {
					logger.debug("'btnDefineRoi' is selected");
					ButtonSelectionUtil.setButtonSelected(btnDefineRoi);
					try {
						for (ITomoAlignmentControlListener tcl : getTomoControlListeners()) {
							tcl.defineRoi(true);
						}
					} catch (IllegalStateException s) {
						// showError("Cannot Define ROI", s);
						showErrorDialog(s);
						ButtonSelectionUtil.setButtonDeselected(btnDefineRoi);
					} catch (Exception e1) {
						logger.debug("Error setting exposure time", e1);
						// showError("Cannot Define ROI", e1);
						showErrorDialog(e1);
					}
				} else {
					logger.debug("'btnDefineRoi' is de-selected");
					ButtonSelectionUtil.setButtonDeselected(btnDefineRoi);
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
				if (!ButtonSelectionUtil.isButtonSelected(btnResFull)) {
					logger.debug("'btnResFull' is selected");
					resolution = RESOLUTION.FULL;
					selectRes(btnResFull);
				}
			} else if (sourceObj == btnRes2x) {
				if (!ButtonSelectionUtil.isButtonSelected(btnRes2x)) {
					logger.debug("'btnRes2x' is selected");
					resolution = RESOLUTION.TWO_X;
					selectRes(btnRes2x);
				}
			} else if (sourceObj == btnRes4x) {
				if (!ButtonSelectionUtil.isButtonSelected(btnRes4x)) {
					logger.debug("'btnRes4x' is selected");
					resolution = RESOLUTION.FOUR_X;
					selectRes(btnRes4x);
				}
			} else if (sourceObj == btnRes8x) {
				if (!ButtonSelectionUtil.isButtonSelected(btnRes8x)) {
					logger.debug("'btnRes8x' is selected");
					resolution = RESOLUTION.EIGHT_X;
					selectRes(btnRes8x);
				}
			} else if (sourceObj == btnSaveAlignment) {
				boolean canSave = true;
				if (!chkInstrument.getSelection()) {
					canSave = false;
					chkInstrument.setFocus();
					chkInstrument.setForeground(ERROR_COLOR);
					showSaveProblemDialog("The checkbox next to 'Instrument' needs to be checked before you can save the tomography alignment.");
				} else if (!chkSampleWeight.getSelection()) {
					canSave = false;
					chkSampleWeight.setFocus();
					chkSampleWeight.setForeground(ERROR_COLOR);
					showSaveProblemDialog("The checkbox next to 'Sample Weight' needs to be checked before you can save the tomography alignment.");
				} else if (!chkSampleAlignment.getSelection()) {
					chkSampleAlignment.setFocus();
					chkSampleAlignment.setForeground(ERROR_COLOR);
					showSaveProblemDialog("The checkbox next to 'Sample Alignment' needs to be checked before you can save the tomography alignment.");
					canSave = false;
				} else if (!chkTomoParameters.getSelection()
						|| (chkTomoParameters.getSelection() && txtSampleDesc.getText().equals(TYPE_DESCRIPTION))) {
					if (txtSampleDesc.getText().equals(TYPE_DESCRIPTION)) {
						showSaveProblemDialog("'Description' is required to save tomography alignment");
					} else if (!chkTomoParameters.getSelection()) {
						chkTomoParameters.setFocus();
						chkTomoParameters.setForeground(ERROR_COLOR);
						showSaveProblemDialog("The checkbox next to 'Tomography Parameters' needs to be checked before you can save the tomography alignment.");
					}
					canSave = false;
				}
				if (canSave) {
					logger.debug("Save alignment clicked");
					try {
						for (ITomoAlignmentControlListener tcl : getTomoControlListeners()) {
							tcl.saveAlignmentConfiguration();
						}
					} catch (Exception e) {
						logger.error("Error Saving:{}", e.getMessage());
					}

					chkInstrument.setSelection(false);
					chkSampleWeight.setSelection(false);
					chkSampleAlignment.setSelection(false);
					chkTomoParameters.setSelection(false);

					MessageDialog.openInformation(getShell(), "Tomography Alignment Saved",
							"The tomography alignment details are saved as part of the experiment configuration");
				} else {

				}

			}
		}

	};

	private SelectionAdapter chkboxSelection = new SelectionAdapter() {
		@Override
		public void widgetSelected(SelectionEvent e) {
			if (e.getSource().equals(chkTomoParameters)) {
				if (txtSampleDesc.getText().equals(TYPE_DESCRIPTION)) {
					chkTomoParameters.setSelection(false);
					MessageDialog.openError(getShell(), "Cannot enable Tomography parameters for saving",
							"Please enter a 'Sample description' to enable the tomography parameters.");
				}
			}
			if (e.getSource().equals(chkInstrument) && chkInstrument.getForeground().equals(ERROR_COLOR)) {
				if (chkInstrument.getSelection()) {
					chkInstrument.setForeground(NORMAL_COLOR);
				}
			}
			if (e.getSource().equals(chkSampleAlignment) && chkSampleAlignment.getForeground().equals(ERROR_COLOR)) {
				if (chkSampleAlignment.getSelection()) {
					chkSampleAlignment.setForeground(NORMAL_COLOR);
				}
			}
			if (e.getSource().equals(chkSampleWeight) && chkSampleWeight.getForeground().equals(ERROR_COLOR)) {
				if (chkSampleWeight.getSelection()) {
					chkSampleWeight.setForeground(NORMAL_COLOR);
				}
			}
			if (e.getSource().equals(chkTomoParameters) && chkTomoParameters.getForeground().equals(ERROR_COLOR)) {
				if (chkTomoParameters.getSelection()) {
					chkTomoParameters.setForeground(NORMAL_COLOR);
				}
			}
		}
	};

	private void showSaveProblemDialog(String message) {
		MessageDialog.openError(getShell(), "Saving Tomography Alignment", message);
	}

	protected void selectRes(Button btnRes) {
		Button[] buttons = new Button[] { btnResFull, btnRes2x, btnRes4x, btnRes8x };
		for (Button button : buttons) {
			if (button.equals(btnRes)) {
				ButtonSelectionUtil.setButtonSelected(button);
			} else {
				ButtonSelectionUtil.setButtonDeselected(button);
			}
		}

	}

	public void setResolutionPixelSize(final String resolutionPixelSize) {
		if (resolutionPixelSize != null && lblObjectPixelSize != null && !lblObjectPixelSize.isDisposed()) {
			lblObjectPixelSize.getDisplay().syncExec(new Runnable() {

				@Override
				public void run() {
					lblObjectPixelSize.setText(resolutionPixelSize);
				}
			});
		}
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
