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

package uk.ac.gda.ui.components;

import java.util.ArrayList;
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

import uk.ac.gda.ui.components.ModuleButtonComposite.CAMERA_MODULE;
import uk.ac.gda.ui.components.RotationButtonComposite.RotationButtonSelectionListener;
import uk.ac.gda.ui.components.RotationSliderComposite.SliderSelectionListener;

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
public class MotionControlComposite extends Composite {

	/**
	 * Enum that caters to the motion control composite
	 */
	public enum MotionControlCentring {
		TILT, HORIZONTAL,

		FIND_AXIS_ROTATION, VERTICAL, MOVE_AXIS_OF_ROTATION
	}

	private static final String ROTATION_AXIS_NOT_DEFINED_shortdesc = "Rotation Axis Not Defined";

	private static final String CAMERA_DIST_DEFAULT_VALUE = "341";

	private static final String CAMERA_DISTANCE_lbl = "Camera distance \r  (mm)";

	private static final String CAMERA_MOTION_lbl = "Camera Motion";

	private static final String LAST_SAVED_lbl = "Last Saved: ";

	private static final String FIND_TOMO_AXIS_lbl = "Find Tomo Axis";

	private static final String ALIGN_TILT_lbl = "Align Tilt";

	private static final String TOMO_ALIGNMENT_lbl = "Tomo Alignment";

	private static final String ENERGY_DEFAULT_VALUE = "112";

	private static final String X_RAY_ENERGY_lbl = "X-ray energy \r (keV)";

	private static final String FOR_FUTURE_USE = "For Future Use";

	private static final String BEAMLINE_CONTROL_lbl = "Beamline Control";

	private static final String SAMPLE_WEIGHT_TWENTY_TO_FIFTY_lbl = "20 - 50";

	private static final String SAMPLE_WEIGHT_ONE_TO_TEN_lbl = "1 - 10";

	private static final String SAMPLE_WEIGHT_LESS_THAN_1_lbl = "< 1";

	private static final String SAMPLE_WEIGHT_TEN_TO_TWENTY_lbl = "10 - 20";

	private static final String SAMPLE_WEIGHT_KG_lbl = "Sample Weight (kg)";

	private static final String SAMPLE_MOTION_CONTROL_lbl = "Sample Motion Control";

	private static final String HORIZONTAL_lbl = "Horizontal";

	private static final String MOVE_AXIS_OF_ROTATION_lbl = "Move Axis of Rotation";

	private static final String TOMO_ROTATION_AXIS_SPECIFIC = "Tomo Rotation Axis Specific";

	private static final String NOT_FOUND_key = "N O T     F O U N D";

	private List<IMotionControlListener> motionControlListeners;

	private boolean rotationAxisFound = false;

	private double energy = Double.NaN;

	private SAMPLE_WEIGHT sampleWeight = SAMPLE_WEIGHT.TWENTY_TO_FIFTY;

	// Fonts
	private FontRegistry fontRegistry;

	private static final String BOLD_TEXT_12 = "bold-text_12";
	private static final String NORMAL_TEXT_9 = "normal-text_9";
	private static final String BOLD_TEXT_7 = "bold-text_6";
	private static final String BOLD_TEXT_9 = "bold-text_9";
	//
	private static final Logger logger = LoggerFactory.getLogger(MotionControlComposite.class);
	private static final String USE_CONTROL_KEY_ShortMsg = "The controls below should be used along with the 'Ctrl' key";
	private static final String PLUS_90_DEG = "+90°";
	private static final String MINUS_90_DEG = "-90°";
	private static final String PLUS_DELTA_5 = "▲+5°";
	private static final String MINUS_DELTA_5 = "▲-5°";
	private static final String PLUS_ONE_EIGHTY_DEG = "+180°";
	private static final String ZERO_DEG = "0°";
	private static final String MINUS_ONE_EIGHTY_DEG = "-180°";
	private static final String FIND_AXIS_OF_ROTATION_label = "Find Rotation Axis";
	private static final String lbl_VERTICAL = "Vertical";

	private static final String MOVE_TOMO_AXIS_lbl = "Move Tomo Axis";

	private Label lblRotationAxisNotFound;

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

	private StackLayout moveAxisRotationCmpLayout;

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

	/**
	 * @return {@link List} of motioncontrol listeners
	 */
	public List<IMotionControlListener> getMotionControlListeners() {
		return Collections.unmodifiableList(motionControlListeners);
	}

	private FocusListener focusListener = new FocusAdapter() {
		@Override
		public void focusLost(FocusEvent focusEvent) {
			if (focusEvent.getSource().equals(txtCameraDistance)) {
				logger.debug("Camera distance text box focus lost");
				try {
					for (IMotionControlListener ml : motionControlListeners) {
						ml.resetCameraDistance();
					}
				} catch (Exception e) {
					showErrorDialog(new IllegalArgumentException("Error reseting camera distance", e));
				}
			} else if (focusEvent.getSource().equals(txtXrayEnergy)) {
				logger.debug("X-ray energy text box focus lost");
				try {
					for (IMotionControlListener ml : motionControlListeners) {
						ml.resetXrayEnergy();
					}
				} catch (Exception e) {
					showErrorDialog(new IllegalArgumentException("Error reseting X-ray energy", e));
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
							for (IMotionControlListener ml : motionControlListeners) {
								ml.cameraDistanceChanged(Double.parseDouble(txtCameraDistance.getText()));
							}
						} catch (Exception e1) {
							logger.debug("Error setting exposure time", e1);
							showErrorDialog(new IllegalArgumentException("Invalid value to be set as camera distance",
									e1));
						}
						MotionControlComposite.this.setFocus();
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
							for (IMotionControlListener ml : motionControlListeners) {
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
					for (IMotionControlListener mcl : motionControlListeners) {
						mcl.rotateLeft90();
					}
				} catch (Exception ex) {
					showErrorDialog(ex);
				}
			} else if (button.equals(btnRightRotate)) {
				try {
					for (IMotionControlListener mcl : motionControlListeners) {
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
					for (IMotionControlListener mcl : motionControlListeners) {
						double currentSliderDegree = coarseRotation.getCurrentSliderDegree();
						mcl.degreeMovedBy(currentSliderDegree - initialDegree);
					}
				} catch (Exception ex) {
					showErrorDialog(ex);
				}
			} else if (sliderComposite.equals(fineRotation)) {
				try {
					for (IMotionControlListener mcl : motionControlListeners) {
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
				for (IMotionControlListener mcl : motionControlListeners) {
					mcl.degreeMovedTo(deg);
				}
			} catch (Exception ex) {
				showErrorDialog(ex);
			}
		}
	};

	public void addMotionControlListener(IMotionControlListener motionControlListener) {
		logger.debug("Registering motion control listener");
		if (motionControlListener != null) {
			motionControlListeners.add(motionControlListener);
			moduleButtonComposite.addModuleChangeListener(motionControlListener);
		}
	}

	protected void showErrorDialog(Exception ex) {
		MessageDialog.openError(getShell(), "Error during alignment", ex.getMessage());
	}

	public void removeMotionControlListener(IMotionControlListener motionControlListener) {
		logger.debug("Registering motion control listener");
		if (motionControlListener != null) {
			motionControlListeners.remove(motionControlListener);
			moduleButtonComposite.removeModuleChangeListener(motionControlListener);
		}
	}

	public MotionControlComposite(Composite parent, FormToolkit toolkit, int style) {
		super(parent, style);
		logger.debug("Constructing motion control composite");
		/* Initialise Font Registry */
		initializeFontRegistry();
		/* Initalize the list of composites. */

		/**/
		motionControlListeners = new ArrayList<IMotionControlListener>();
		/**/
		GridLayout layout = new GridLayout();
		setDefaultLayoutSettings(layout);
		this.setLayout(layout);
		/**/
		/* Label to say that use control key */
		Composite useCtrlLabelComposite = createUseControlLabelComosite(toolkit);
		useCtrlLabelComposite.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));

		/**/
		Composite controlsComposite = createMotionControlsBarComponents(toolkit);
		controlsComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		/**/
	}

	/**
	 * @param toolkit
	 * @return {@link Composite} that contains the motion control bar
	 */
	protected Composite createMotionControlsBarComponents(FormToolkit toolkit) {

		Composite controlsComposite = toolkit.createComposite(this);
		GridLayout layout = new GridLayout(5, true);
		setDefaultLayoutSettings(layout);
		controlsComposite.setLayout(layout);

		// Container containing the Beamline Control, Tomo Alignment, and the Camera Motion
		Composite firstThreeComposites = createBeamlineControlTomoAlignmentCameraMotionComposite(controlsComposite,
				toolkit);
		GridData gridData = new GridData(GridData.FILL_BOTH);
		gridData.horizontalSpan = 2;
		firstThreeComposites.setLayoutData(gridData);

		//
		Composite sampleMotionControlComposite = createSampleMotionControlComposite(controlsComposite, toolkit);
		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.horizontalSpan = 3;
		sampleMotionControlComposite.setLayoutData(layoutData);

		return controlsComposite;
	}

	protected Composite createBeamlineControlTomoAlignmentCameraMotionComposite(Composite parent, FormToolkit toolkit) {
		Composite firstThreeComposites = toolkit.createComposite(parent);

		GridLayout layout = new GridLayout(3, true);
		setDefaultLayoutSettings(layout);
		layout.marginWidth = 1;
		layout.marginHeight = 1;
		layout.horizontalSpacing = 1;
		firstThreeComposites.setLayout(layout);
		firstThreeComposites.setBackground(ColorConstants.black);
		// 1
		Composite beamlineControlsComposite = createBeamlineControlComposite(firstThreeComposites, toolkit);
		beamlineControlsComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		// 2
		Composite tomoAlignmentControlsComposite = createTomoAlignmentControlComposite(firstThreeComposites, toolkit);
		tomoAlignmentControlsComposite.setLayoutData(new GridData(GridData.FILL_BOTH));
		// 3
		Composite cameraMotionControlsComposite = createCameraMotionControlComposite(firstThreeComposites, toolkit);
		cameraMotionControlsComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		return firstThreeComposites;

	}

	protected Composite createSampleMotionControlComposite(Composite parent, FormToolkit toolkit) {
		Composite sampleMotionControlComposite = toolkit.createComposite(parent);
		GridLayout layout = new GridLayout(3, false);
		setDefaultLayoutSettings(layout);
		sampleMotionControlComposite.setLayout(layout);

		Composite lineComposite = toolkit.createComposite(sampleMotionControlComposite);
		GridData layoutData = new GridData(GridData.FILL_HORIZONTAL);
		layoutData.horizontalSpan = 3;
		layoutData.heightHint = 1;
		lineComposite.setBackground(ColorConstants.black);
		lineComposite.setLayoutData(layoutData);
		//
		Label lblSampleMotionControl = toolkit.createLabel(sampleMotionControlComposite, SAMPLE_MOTION_CONTROL_lbl);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 3;
		gd.horizontalAlignment = SWT.CENTER;
		lblSampleMotionControl.setFont(fontRegistry.get(BOLD_TEXT_12));
		lblSampleMotionControl.setLayoutData(gd);

		Composite tomoRotationComposite = createAxisRotationComposite(sampleMotionControlComposite, toolkit);
		gd = new GridData(GridData.FILL_VERTICAL);
		gd.widthHint = 808;// multiple of 12
		gd.verticalAlignment = SWT.BOTTOM;
		tomoRotationComposite.setLayoutData(gd);

		Composite rotationStageComposite = createRotationStageComposite(sampleMotionControlComposite, toolkit);
		gd = new GridData(GridData.FILL_VERTICAL);
		gd.widthHint = 105;
		rotationStageComposite.setLayoutData(gd);

		Composite horizontalVerticalWeightControlContainer = toolkit.createComposite(sampleMotionControlComposite);
		layout = new GridLayout(2, true);

		horizontalVerticalWeightControlContainer.setLayout(layout);
		horizontalVerticalWeightControlContainer.setLayoutData(new GridData(GridData.FILL_BOTH));

		btnHorizontal = toolkit.createButton(horizontalVerticalWeightControlContainer, HORIZONTAL_lbl, SWT.PUSH);
		btnHorizontal.setFont(fontRegistry.get(NORMAL_TEXT_9));
		btnHorizontal.setLayoutData(new GridData(GridData.FILL_BOTH));
		btnHorizontal.addListener(SWT.MouseDown, ctrlMouseListener);

		btnVertical = toolkit.createButton(horizontalVerticalWeightControlContainer, lbl_VERTICAL, SWT.PUSH);
		btnVertical.setFont(fontRegistry.get(NORMAL_TEXT_9));
		btnVertical.setLayoutData(new GridData(GridData.FILL_BOTH));
		btnVertical.addListener(SWT.MouseDown, ctrlMouseListener);

		Composite sampleWeightComposite = toolkit.createComposite(horizontalVerticalWeightControlContainer);
		GridData ld = new GridData(GridData.FILL_HORIZONTAL);
		ld.horizontalSpan = 2;
		sampleWeightComposite.setLayoutData(ld);

		layout = new GridLayout(4, true);
		sampleWeightComposite.setLayout(layout);

		Label lblSampleWeight = toolkit.createLabel(sampleWeightComposite, SAMPLE_WEIGHT_KG_lbl);
		gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 4;
		gd.horizontalAlignment = SWT.CENTER;
		lblSampleWeight.setLayoutData(gd);

		sampleWeightComposite.setBackground(ColorConstants.lightGreen);
		lblSampleWeight.setBackground(ColorConstants.lightGreen);
		//
		btnSampleWeightLessThan1 = toolkit.createButton(sampleWeightComposite, SAMPLE_WEIGHT_LESS_THAN_1_lbl, SWT.PUSH);
		btnSampleWeightLessThan1.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnSampleWeightLessThan1.addListener(SWT.MouseDown, ctrlMouseListener);
		btnSampleWeightLessThan1.setFont(fontRegistry.get(BOLD_TEXT_7));
		//
		btnSampleWeight1to10 = toolkit.createButton(sampleWeightComposite, SAMPLE_WEIGHT_ONE_TO_TEN_lbl, SWT.PUSH);
		btnSampleWeight1to10.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnSampleWeight1to10.addListener(SWT.MouseDown, ctrlMouseListener);
		btnSampleWeight1to10.setFont(fontRegistry.get(BOLD_TEXT_7));
		//
		btnSampleWeight10to20 = toolkit.createButton(sampleWeightComposite, SAMPLE_WEIGHT_TEN_TO_TWENTY_lbl, SWT.PUSH);
		btnSampleWeight10to20.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnSampleWeight10to20.addListener(SWT.MouseDown, ctrlMouseListener);
		btnSampleWeight10to20.setFont(fontRegistry.get(BOLD_TEXT_7));
		//
		btnSampleWeight20to50 = toolkit
				.createButton(sampleWeightComposite, SAMPLE_WEIGHT_TWENTY_TO_FIFTY_lbl, SWT.PUSH);
		btnSampleWeight20to50.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		btnSampleWeight20to50.addListener(SWT.MouseDown, ctrlMouseListener);
		btnSampleWeight20to50.setFont(fontRegistry.get(BOLD_TEXT_7));

		btnSampleWeight20to50.setSelection(true);
		return sampleMotionControlComposite;
	}

	private Composite createBeamlineControlComposite(Composite parent, FormToolkit toolkit) {
		Composite beamlineControlComposite = toolkit.createComposite(parent);
		GridLayout layout = new GridLayout(2, true);
		beamlineControlComposite.setLayout(layout);

		Label lblBeamlineControl = toolkit.createLabel(beamlineControlComposite, BEAMLINE_CONTROL_lbl);
		lblBeamlineControl.setFont(fontRegistry.get(BOLD_TEXT_12));
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

		txtXrayEnergy = toolkit.createText(xRayEnergyComposite, ENERGY_DEFAULT_VALUE, SWT.CENTER);
		txtXrayEnergy.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtXrayEnergy.addFocusListener(focusListener);
		txtXrayEnergy.addKeyListener(txtKeyListener);
		txtXrayEnergy.setEditable(false);
		return beamlineControlComposite;
	}

	private Composite createTomoAlignmentControlComposite(Composite parent, FormToolkit toolkit) {
		Composite tomoAlignmentComposite = toolkit.createComposite(parent);
		GridLayout layout = new GridLayout(2, true);
		layout.verticalSpacing = 1;
		layout.marginWidth = 2;
		tomoAlignmentComposite.setLayout(layout);

		Label lblTomoAlignment = toolkit.createLabel(tomoAlignmentComposite, TOMO_ALIGNMENT_lbl);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalSpan = 2;
		gd.horizontalAlignment = SWT.CENTER;
		lblTomoAlignment.setLayoutData(gd);
		lblTomoAlignment.setFont(fontRegistry.get(BOLD_TEXT_12));

		// Buttons for align tilts

		btnTilt = toolkit.createButton(tomoAlignmentComposite, ALIGN_TILT_lbl, SWT.PUSH);
		btnTilt.setFont(fontRegistry.get(NORMAL_TEXT_9));
		btnTilt.setLayoutData(new GridData(GridData.FILL_BOTH));
		btnTilt.addListener(SWT.MouseDown, ctrlMouseListener);

		//
		btnFindAxisOfRotation = toolkit.createButton(tomoAlignmentComposite, FIND_TOMO_AXIS_lbl, SWT.PUSH | SWT.WRAP);
		btnFindAxisOfRotation.setLayoutData(new GridData(GridData.FILL_BOTH));
		btnFindAxisOfRotation.setFont(fontRegistry.get(NORMAL_TEXT_9));
		btnFindAxisOfRotation.addListener(SWT.MouseDown, ctrlMouseListener);

		Label lblTiltLastSaved = toolkit.createLabel(tomoAlignmentComposite, LAST_SAVED_lbl, SWT.WRAP | SWT.CENTER);
		lblTiltLastSaved.setFont(fontRegistry.get(NORMAL_TEXT_9));
		lblTiltLastSaved.setLayoutData(new GridData(GridData.FILL_BOTH));
		lblTiltLastSaved.setBackground(ColorConstants.lightBlue);
		lblTiltLastSaved.setForeground(ColorConstants.white);

		moveAxisBtnComposite = toolkit.createComposite(tomoAlignmentComposite);
		moveAxisBtnComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		moveAxisRotationCmpLayout = new StackLayout();
		moveAxisBtnComposite.setLayout(moveAxisRotationCmpLayout);

		btnMoveAxisOfRotation = toolkit.createButton(moveAxisBtnComposite, MOVE_TOMO_AXIS_lbl, SWT.PUSH | SWT.WRAP);
		// btnMoveAxisOfRotation.setLayoutData(new GridData(GridData.FILL_BOTH));
		btnMoveAxisOfRotation.setBackground(ColorConstants.green);
		btnMoveAxisOfRotation.setFont(fontRegistry.get(NORMAL_TEXT_9));
		btnMoveAxisOfRotation.addListener(SWT.MouseDown, ctrlMouseListener);

		lblRotationAxisNotFound = toolkit.createLabel(moveAxisBtnComposite, ROTATION_AXIS_NOT_DEFINED_shortdesc,
				SWT.WRAP | SWT.CENTER);
		lblRotationAxisNotFound.setBackground(ColorConstants.lightBlue);
		lblRotationAxisNotFound.setForeground(ColorConstants.white);

		moveAxisRotationCmpLayout.topControl = lblRotationAxisNotFound;

		return tomoAlignmentComposite;
	}

	private Composite createCameraMotionControlComposite(Composite parent, FormToolkit toolkit) {
		Composite cameraMotionComposite = toolkit.createComposite(parent);

		GridLayout layout = new GridLayout(2, true);

		cameraMotionComposite.setLayout(layout);

		Label lblCameraMotion = toolkit.createLabel(cameraMotionComposite, CAMERA_MOTION_lbl);
		GridData gd = new GridData(GridData.FILL_HORIZONTAL);
		gd.horizontalAlignment = SWT.CENTER;
		gd.horizontalSpan = 2;
		lblCameraMotion.setLayoutData(gd);
		lblCameraMotion.setFont(fontRegistry.get(BOLD_TEXT_12));

		moduleButtonComposite = new ModuleButtonComposite(cameraMotionComposite, toolkit);
		moduleButtonComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		Composite cameraDistanceComposite = toolkit.createComposite(cameraMotionComposite);
		cameraDistanceComposite.setLayoutData(new GridData(GridData.FILL_BOTH));

		layout = new GridLayout();

		cameraDistanceComposite.setLayout(layout);
		cameraDistanceComposite.setBackground(ColorConstants.lightGray);

		lblCameraDistance = toolkit.createLabel(cameraDistanceComposite, CAMERA_DISTANCE_lbl, SWT.WRAP | SWT.CENTER);
		lblCameraDistance.setFont(fontRegistry.get(NORMAL_TEXT_9));

		GridData layoutData = new GridData(GridData.FILL_BOTH);
		layoutData.horizontalSpan = 2;
		layoutData.horizontalAlignment = SWT.CENTER;
		lblCameraDistance.setLayoutData(layoutData);
		lblCameraDistance.setBackground(ColorConstants.lightGray);

		txtCameraDistance = toolkit.createText(cameraDistanceComposite, CAMERA_DIST_DEFAULT_VALUE, SWT.CENTER);
		txtCameraDistance.setLayoutData(new GridData(GridData.FILL_HORIZONTAL));
		txtCameraDistance.addFocusListener(focusListener);
		txtCameraDistance.addKeyListener(txtKeyListener);

		return cameraMotionComposite;
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
		GridData layoutData = new GridData(GridData.FILL_VERTICAL);
		layoutData.widthHint = 55;
		btnLeftRotate.setLayoutData(layoutData);
		btnLeftRotate.addSelectionListener(rotationButtonListener);

		btnRightRotate = new RotationButtonComposite(rotationStageComposite, PositionConstants.EAST, PLUS_90_DEG, true);
		layoutData = new GridData(GridData.FILL_VERTICAL);
		layoutData.widthHint = 55;
		btnRightRotate.setLayoutData(layoutData);
		btnRightRotate.addSelectionListener(rotationButtonListener);

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
	 * @param toolkit
	 * @return {@link Composite} that contains the label to state that the controls need to be used with the "Ctrl" key
	 */
	protected Composite createUseControlLabelComosite(FormToolkit toolkit) {
		Composite useCtrlLabelComposite = toolkit.createComposite(this);
		useCtrlLabelComposite.setLayout(new FillLayout());
		Label lblUseCtrl = toolkit.createLabel(useCtrlLabelComposite, USE_CONTROL_KEY_ShortMsg, SWT.CENTER);
		lblUseCtrl.setBackground(ColorConstants.darkBlue);
		lblUseCtrl.setForeground(ColorConstants.white);
		lblUseCtrl.setFont(fontRegistry.get(BOLD_TEXT_9));
		return useCtrlLabelComposite;
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
	protected Composite createCentringButtonsComposite(Composite motionControlComposite, FormToolkit toolkit) {
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

		btnHorizontal = toolkit.createButton(sampleMotionControlGroup, HORIZONTAL_lbl, SWT.PUSH);
		ld = new GridData(GridData.FILL_BOTH);
		ld.verticalSpan = 2;
		btnHorizontal.setLayoutData(ld);
		btnHorizontal.addListener(SWT.MouseDown, ctrlMouseListener);
		/**/
		btnVertical = toolkit.createButton(sampleMotionControlGroup, lbl_VERTICAL, SWT.PUSH);
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
		}
	}

	/**
	 * @param layout
	 */
	private void setDefaultLayoutSettings(GridLayout layout) {
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
							for (IMotionControlListener mcl : motionControlListeners) {
								mcl.setSampleWeight(SAMPLE_WEIGHT.LESS_THAN_ONE);
							}
						}
					} else if (sourceObj == btnSampleWeight1to10) {
						// Button - Vertical
						if (!isSelected(btnSampleWeight1to10)) {
							selectSampleWeightControlInUi(btnSampleWeight1to10);
							//
							for (IMotionControlListener mcl : motionControlListeners) {
								mcl.setSampleWeight(SAMPLE_WEIGHT.ONE_TO_TEN);
							}
						}
					} else if (sourceObj == btnSampleWeight10to20) {
						// Button - Vertical
						if (!isSelected(btnSampleWeight10to20)) {
							selectSampleWeightControlInUi(btnSampleWeight10to20);

							//
							for (IMotionControlListener mcl : motionControlListeners) {
								mcl.setSampleWeight(SAMPLE_WEIGHT.TEN_TO_TWENTY);
							}
						}
					} else if (sourceObj == btnSampleWeight20to50) {
						// Button - Vertical
						if (!isSelected(btnSampleWeight20to50)) {
							selectSampleWeightControlInUi(btnSampleWeight20to50);
							//
							for (IMotionControlListener mcl : motionControlListeners) {
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
		MotionControlComposite motionControlComposite = new MotionControlComposite(shell, new FormToolkit(display),
				SWT.None);

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

			motionControlListeners.clear();
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
		return moduleButtonComposite.getModuleSelected();
	}

	public void setRotationMotorBusy(boolean isBusy) {
		coarseRotation.setMotorBusy(isBusy);
	}

	public void deselectModule() {
		moduleButtonComposite.deselectAll();
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
		moduleButtonComposite.setModuleButtonText(units, btn1Text, btn2Text, btn3Text, btn4Text);
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
		Button[] selectableBtns = new Button[] { btnTilt, btnVertical, btnHorizontal, btnMoveAxisOfRotation,
				btnFindAxisOfRotation };

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
			for (IMotionControlListener mcl : getMotionControlListeners()) {
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
			for (IMotionControlListener mcl : getMotionControlListeners()) {
				mcl.vertical(switchOn);
			}
		} catch (Exception ex) {
			showErrorDialog(ex);
			throw ex;
		}

	}

	private void doHorizontal(boolean switchOn) throws Exception {
		logger.debug("'Tilt' - is selected");
		switchMotionCentring(switchOn, btnHorizontal);
		try {
			for (IMotionControlListener mcl : getMotionControlListeners()) {
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
			for (IMotionControlListener mcl : getMotionControlListeners()) {
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
			for (IMotionControlListener mcl : getMotionControlListeners()) {
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

}