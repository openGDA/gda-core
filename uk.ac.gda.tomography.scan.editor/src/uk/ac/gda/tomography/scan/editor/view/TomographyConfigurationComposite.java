/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.gda.tomography.scan.editor.view;

import java.util.EnumMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.gda.tomography.base.TomographyMode.TomographyDevices;
import uk.ac.gda.tomography.base.TomographyParameters;
import uk.ac.gda.tomography.controller.AcquisitionControllerException;
import uk.ac.gda.tomography.model.DevicePosition;
import uk.ac.gda.tomography.model.MultipleScansType;
import uk.ac.gda.tomography.model.RangeType;
import uk.ac.gda.tomography.model.ScanType;
import uk.ac.gda.tomography.ui.controller.TomographyParametersAcquisitionController;
import uk.ac.gda.tomography.ui.controller.TomographyParametersAcquisitionController.Positions;
import uk.ac.gda.ui.tool.ClientBindingElements;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientSWTElements;

/**
 * This Composite allows to edit a {@link TomographyParameters} object.
 *
 * @author Maurizio Nagni
 */
public class TomographyConfigurationComposite extends CompositeTemplate<TomographyParametersAcquisitionController> {

	/** Scan prefix **/
	private Text name;

	/** Scan type **/
	private Button flyScanType;
	private Button stepScanType;

	/** The Start Composite elements **/
	private Text startAngleText;
	private Button currentAngleButton;

	/** The End Composite elements **/
	private Button halfRotationRangeType;

	private Group fullGroup;
	private Button fullRotationRangeType;
	private Text numberRotation;

	private Group customGroup;
	private Button customRotationRangeType;
	private Text customAngle;

	/** The Projections Composite elements **/
	private Text totalProjections;
	private Label angularStep;

	/** The Calibration Composite elements **/
	private Text numberDark;
	private Text darkExposure;
	private Text numberFlat;
	private Text flatExposure;
	private Button beforeAcquisition;
	private Button afterAcquisition;

	/** The MultipleScans Composite elements **/
	private Button multipleScans;
	private Text numberRepetitions;
	private Text waitingTime;
	private Button repeateMultipleScansType;
	private Button switchbackMultipleScansType;

	private static final Logger logger = LoggerFactory.getLogger(TomographyConfigurationComposite.class);

	public TomographyConfigurationComposite(Composite parent, TomographyParametersAcquisitionController controller) {
		this(parent, SWT.NONE, controller);
	}

	public TomographyConfigurationComposite(Composite parent, int style, TomographyParametersAcquisitionController controller) {
		super(parent, style, controller);
	}

	@Override
	protected void createElements(int labelStyle, int textStyle) {
		GridLayoutFactory.swtDefaults().margins(ClientSWTElements.defaultCompositeMargin()).applyTo(this);
		nameAndScanTypeContent(ClientSWTElements.createComposite(this, SWT.NONE, 2), labelStyle, textStyle);
		startAngleContent(ClientSWTElements.createGroup(this, 2, ClientMessages.START), labelStyle, textStyle);
		endAngleContent(ClientSWTElements.createGroup(this, 3, ClientMessages.END), labelStyle, textStyle);
		projectionsContent(ClientSWTElements.createGroup(this, 2, ClientMessages.PROJECTIONS), labelStyle, textStyle);
		imagesCalibrationContent(ClientSWTElements.createGroup(this, 2, ClientMessages.IMAGE_CALIBRATION), labelStyle, textStyle);
		multipleScansContent(this, labelStyle, textStyle);
	}

	private void nameAndScanTypeContent(Composite parent, int labelStyle, int textStyle) {
		ClientSWTElements.createLabel(parent, labelStyle, ClientMessages.NAME, new Point(2, 1));
		this.name = ClientSWTElements.createText(parent, textStyle, null, new Point(2, 1), ClientMessages.NAME_TOOLTIP, new Point(500, SWT.DEFAULT));
		flyScanType = ClientSWTElements.createButton(parent, SWT.RADIO, ClientMessages.FLY_SCAN, ClientMessages.FLY_SCAN_TOOLTIP);
		flyScanType.setData(ScanType.FLY);
		stepScanType = ClientSWTElements.createButton(parent, SWT.RADIO, ClientMessages.STEP_SCAN, ClientMessages.STEP_SCAN_TOOLTIP);
		stepScanType.setData(ScanType.STEP);
	}

	private void startAngleContent(Composite parent, int labelStyle, int textStyle) {
		ClientSWTElements.createLabel(parent, labelStyle, ClientMessages.ANGLE, new Point(2, 1));
		startAngleText = ClientSWTElements.createText(parent, textStyle, TomographyVerifyListener.verifyOnlyDoubleText, null,
				ClientMessages.START_ANGLE_TOOLTIP);
		currentAngleButton = ClientSWTElements.createButton(parent, SWT.CHECK, ClientMessages.CURRENT_ANGLE, ClientMessages.CURRENT_ANGLE_TOOLTIP);
	}

	private void endAngleContent(Composite parent, int labelStyle, int textStyle) {
		halfRotationRangeType = ClientSWTElements.createButton(parent, SWT.RADIO, ClientMessages.STRAIGHT_ANGLE, ClientMessages.STRAIGHT_ANGLE_TOOLTIP);

		fullRotationRangeType = ClientSWTElements.createButton(parent, SWT.RADIO, ClientMessages.FULL_ANGLE, ClientMessages.FULL_ANGLE_TOOLTIP);
		// fullGroup = ClientSWTElements.createGroup(parent, 1, null);
		// ClientSWTElements.createLabel(fullGroup, labelStyle, ClientMessages.NUM_ROTATIONS);
		// numberRotation = ClientSWTElements.createText(fullGroup, textStyle,
		// TomographyVerifyListener.verifyOnlyDigitText);

		customRotationRangeType = ClientSWTElements.createButton(parent, SWT.RADIO, ClientMessages.CUSTOM, ClientMessages.CUSTOM_END_TOOLTIP);
		ExpandBar customBar = createExpandBar(parent);
		customGroup = ClientSWTElements.createGroup(customBar, 1, null);
		ClientSWTElements.createLabel(customGroup, labelStyle, ClientMessages.ANGLE);
		customAngle = ClientSWTElements.createText(customGroup, textStyle, TomographyVerifyListener.verifyOnlyDoubleText, null,
				ClientMessages.CUSTOM_END_TOOLTIP);
		expandBar(customBar, customGroup, customRotationRangeType);
	}

	private void projectionsContent(Composite parent, int labelStyle, int textStyle) {
		ClientSWTElements.createLabel(parent, labelStyle, ClientMessages.TOTAL_PROJECTIONS);
		ClientSWTElements.createLabel(parent, labelStyle, ClientMessages.ANGULAR_STEP);
		totalProjections = ClientSWTElements.createText(parent, textStyle, TomographyVerifyListener.verifyOnlyIntegerText, null,
				ClientMessages.TOTAL_PROJECTIONS_TOOLTIP);
		angularStep = ClientSWTElements.createLabel(parent, labelStyle, ClientMessages.EMPTY_MESSAGE);
	}

	private void imagesCalibrationContent(Composite parent, int labelStyle, int textStyle) {
		ClientSWTElements.createLabel(parent, labelStyle, ClientMessages.NUM_DARK);
		ClientSWTElements.createLabel(parent, labelStyle, ClientMessages.DARK_EXPOSURE);
		numberDark = ClientSWTElements.createText(parent, textStyle, TomographyVerifyListener.verifyOnlyIntegerText, new Point(1, 1),
				ClientMessages.NUM_DARK_TOOLTIP);
		darkExposure = ClientSWTElements.createText(parent, textStyle, TomographyVerifyListener.verifyOnlyIntegerText, new Point(1, 1),
				ClientMessages.DARK_EXPOSURE_TP);

		ClientSWTElements.createLabel(parent, labelStyle, ClientMessages.NUM_FLAT);
		ClientSWTElements.createLabel(parent, labelStyle, ClientMessages.FLAT_EXPOSURE);
		numberFlat = ClientSWTElements.createText(parent, textStyle, TomographyVerifyListener.verifyOnlyIntegerText, new Point(1, 1),
				ClientMessages.NUM_FLAT_TOOLTIP);
		flatExposure = ClientSWTElements.createText(parent, textStyle, TomographyVerifyListener.verifyOnlyIntegerText, new Point(1, 1),
				ClientMessages.FLAT_EXPOSURE_TP);

		ClientSWTElements.createEmptyCell(parent, labelStyle);
		ClientSWTElements.createEmptyCell(parent, labelStyle);

		beforeAcquisition = ClientSWTElements.createButton(parent, SWT.CHECK, ClientMessages.AT_START, ClientMessages.AT_START_TOOLTIP);
		afterAcquisition = ClientSWTElements.createButton(parent, SWT.CHECK, ClientMessages.AT_END, ClientMessages.AT_END_TOOLTIP);
	}

	private void multipleScansContent(Composite parent, int labelStyle, int textStyle) {
		multipleScans = ClientSWTElements.createButton(parent, SWT.CHECK, ClientMessages.MULTIPLE_SCANS, ClientMessages.MULTIPLE_SCANS_TOOLTIP);
		ExpandBar customBar = createExpandBar(parent);
		Group multipleScan = ClientSWTElements.createGroup(customBar, 3, ClientMessages.EMPTY_MESSAGE);
		ClientSWTElements.createLabel(multipleScan, labelStyle, ClientMessages.NUM_REPETITIONS);
		ClientSWTElements.createLabel(multipleScan, labelStyle, ClientMessages.WAITING_TIME);
		ClientSWTElements.createEmptyCell(multipleScan, labelStyle);
		numberRepetitions = ClientSWTElements.createText(multipleScan, textStyle, TomographyVerifyListener.verifyOnlyIntegerText, new Point(1, 2),
				ClientMessages.NUM_REPETITIONS_TOOLTIP);
		waitingTime = ClientSWTElements.createText(multipleScan, textStyle, TomographyVerifyListener.verifyOnlyIntegerText, new Point(1, 2),
				ClientMessages.WAITING_TIME_TOOLTIP);
		repeateMultipleScansType = ClientSWTElements.createButton(multipleScan, SWT.RADIO, ClientMessages.REPEATE_SCAN, ClientMessages.REPEATE_SCAN_TOOLTIP);
		switchbackMultipleScansType = ClientSWTElements.createButton(multipleScan, SWT.RADIO, ClientMessages.SWITCHBACK_SCAN,
				ClientMessages.SWITCHBACK_SCAN_TOOLTIP);

		// --- this is a workaround to expand the bar ---//
		multipleScans.setSelection(getTemplateData().getMultipleScans().isEnabled());

		expandBar(customBar, multipleScan, multipleScans);
	}

	@Override
	protected void bindElements() {
		DataBindingContext dbc = new DataBindingContext();

		bindScanType(dbc);
		bindMultipleScanType(dbc);
		binRangeType(dbc);

		ClientBindingElements.bindText(dbc, name, String.class, "name", getTemplateData());

		ClientBindingElements.bindCheckBox(dbc, currentAngleButton, "start.useCurrentAngle", getTemplateData());
		ClientBindingElements.bindText(dbc, startAngleText, Double.class, "start.start", getTemplateData());

		// ClientBindingElements.bindText(dbc, numberRotation, Integer.class, "end.numberRotation", getTemplateData());
		ClientBindingElements.bindText(dbc, customAngle, Double.class, "end.customAngle", getTemplateData());

		ClientBindingElements.bindText(dbc, totalProjections, Double.class, "projections.totalProjections", getTemplateData());
		// ClientBindingElements.bindL(dbc, angularStep, Double.class, "projections.angularStep", getTemplateData());

		ClientBindingElements.bindCheckBox(dbc, beforeAcquisition, "imageCalibration.beforeAcquisition", getTemplateData());
		ClientBindingElements.bindCheckBox(dbc, afterAcquisition, "imageCalibration.afterAcquisition", getTemplateData());
		ClientBindingElements.bindText(dbc, numberDark, Integer.class, "imageCalibration.numberDark", getTemplateData());
		ClientBindingElements.bindText(dbc, numberFlat, Integer.class, "imageCalibration.numberFlat", getTemplateData());
		ClientBindingElements.bindText(dbc, darkExposure, Integer.class, "imageCalibration.darkExposure", getTemplateData());
		ClientBindingElements.bindText(dbc, flatExposure, Integer.class, "imageCalibration.flatExposure", getTemplateData());

		ClientBindingElements.bindCheckBox(dbc, multipleScans, "multipleScans.enabled", getTemplateData());
		multipleScans.setSelection(multipleScans.getSelection());
		ClientBindingElements.bindText(dbc, numberRepetitions, Integer.class, "multipleScans.numberRepetitions", getTemplateData());
		ClientBindingElements.bindText(dbc, waitingTime, Integer.class, "multipleScans.waitingTime", getTemplateData());
	}

	private void bindScanType(DataBindingContext dbc) {
		Map<ScanType, Object> enumRadioMap = new EnumMap<>(ScanType.class);
		enumRadioMap.put(ScanType.FLY, flyScanType);
		enumRadioMap.put(ScanType.STEP, stepScanType);
		ClientBindingElements.bindEnumToRadio(dbc, ScanType.class, "scanType", getTemplateData(), enumRadioMap);
	}

	private void bindMultipleScanType(DataBindingContext dbc) {
		Map<MultipleScansType, Object> enumRadioMap = new EnumMap<>(MultipleScansType.class);
		enumRadioMap.put(MultipleScansType.REPEAT_SCAN, repeateMultipleScansType);
		enumRadioMap.put(MultipleScansType.SWITCHBACK_SCAN, switchbackMultipleScansType);
		ClientBindingElements.bindEnumToRadio(dbc, MultipleScansType.class, "multipleScans.multipleScansType", getTemplateData(), enumRadioMap);
	}

	private void binRangeType(DataBindingContext dbc) {
		Map<RangeType, Object> enumRadioMap = new EnumMap<>(RangeType.class);
		enumRadioMap.put(RangeType.RANGE_180, halfRotationRangeType);
		enumRadioMap.put(RangeType.RANGE_360, fullRotationRangeType);
		enumRadioMap.put(RangeType.CUSTOM, customRotationRangeType);
		ClientBindingElements.bindEnumToRadio(dbc, RangeType.class, "end.rangeType", getTemplateData(), enumRadioMap);
	}

	@Override
	protected void initialiseElements() {
		endGroupsListeners();
		customAngleLooseFocus();
		angularStepListener();
		useCurrentAngleListener();
		configureWhenShow();

		forceFocusOnEmpty(startAngleText, Double.toString(getTemplateData().getStart().getStart()));
		forceFocusOnEmpty(customAngle, Double.toString(getTemplateData().getEnd().getCustomAngle()));
		forceFocusOnEmpty(totalProjections, Integer.toString(getTemplateData().getProjections().getTotalProjections()));
		forceFocusOnEmpty(numberDark, Integer.toString(getTemplateData().getImageCalibration().getNumberDark()));
		forceFocusOnEmpty(numberFlat, Integer.toString(getTemplateData().getImageCalibration().getNumberFlat()));
		forceFocusOnEmpty(numberRepetitions, Integer.toString(getTemplateData().getMultipleScans().getNumberRepetitions()));
		forceFocusOnEmpty(waitingTime, Integer.toString(getTemplateData().getMultipleScans().getWaitingTime()));
	}

	private void endGroupsListeners() {
		SelectionListener activateGroupListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button source = Button.class.cast(e.getSource());

				if (source.getSelection()) {
					if (source.equals(halfRotationRangeType)) {
						customGroup.setEnabled(false);
						customAngle.setText(Double.toString(180));
					} else if (source.equals(fullRotationRangeType)) {
						customAngle.setText(Double.toString(360));
						customGroup.setEnabled(false);
					} else if (source.equals(customRotationRangeType)) {
						customGroup.setEnabled(true);
						customAngle.setText(Double.toString(getTemplateData().getStart().getStart()));
					}
				}
				angularStep.setText(Double.toString(calculateAngularStep()));
			}
		};
		halfRotationRangeType.addSelectionListener(activateGroupListener);
		fullRotationRangeType.addSelectionListener(activateGroupListener);
		customRotationRangeType.addSelectionListener(activateGroupListener);
	}

	private void customAngleLooseFocus() {
		FocusListener customAngleListener = FocusListener.focusLostAdapter(c -> {
			// if (getTemplateData().getEnd().getCustomAngle() < getTemplateData().getStart().getStart()) {
			// getTemplateData().getEnd().setCustomAngle(getTemplateData().getStart().getStart());
			// customAngle.setText(startAngleText.getText());
			// }
			// customAngle.pack();
			pack();
			updateCurrentAngularPosition();
		});
		customAngle.addFocusListener(customAngleListener);
	}

	private void forceFocusOnEmpty(Text text, String defaultValue) {
		// FocusListener focusListener = FocusListener.focusLostAdapter(c -> {
		// if (totalProjections.getText().trim().isEmpty()) {
		// totalProjections.setText(Integer.toString(getTemplateData().getProjections().getTotalProjections()));
		// }
		// });
		text.addFocusListener(FocusListener.focusLostAdapter(c -> {
			if (text.getText().trim().isEmpty()) {
				text.setText(defaultValue);
			}
		}));
	}

	private void angularStepListener() {
		ModifyListener angularStepListener = event -> {
			updateCurrentAngularPosition();
			setAngularStep();
			angularStep.pack();
		};
		totalProjections.addModifyListener(angularStepListener);
		startAngleText.addModifyListener(angularStepListener);
		customAngle.addModifyListener(angularStepListener);
		totalProjections.addModifyListener(angularStepListener);
	}

	private void useCurrentAngleListener() {
		SelectionListener useCurrentAngleListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button source = Button.class.cast(e.getSource());
				if (source.getSelection()) {
					updateCurrentAngularPosition();
					getTemplateData().getStart().setStart(getTemplateData().getStart().getCurrentAngle());
					startAngleText.setText(Double.toString(getTemplateData().getStart().getCurrentAngle()));
					startAngleText.setEnabled(false);
				} else {
					startAngleText.setEnabled(true);
				}
				setAngularStep();
			}
		};
		currentAngleButton.addSelectionListener(useCurrentAngleListener);
	}

	private void setAngularStep() {
		double newAngularStep = calculateAngularStep();
		angularStep.setText(Double.toString(newAngularStep));
		getTemplateData().getProjections().setAngularStep(newAngularStep);
	}

	// private VerifyListener validateCustomAngle() {
	// return e -> {
	// Text widget = Text.class.cast(e.widget);
	// String currentText = widget.getText();
	// String newText = (currentText.substring(0, e.start) + e.text + currentText.substring(e.end));
	// if (TomographyVerifyListener.stringIsDoubleNumber(newText)) {
	// if (Double.parseDouble(newText) < getTemplateData().getStart().getStart()) {
	// widget.setToolTipText(ClientMessagesUtility.getMessage(ClientMessages.LESS_THAN_START));
	// WidgetUtilities.addErrorDecorator(widget,
	// ClientMessagesUtility.getMessage(ClientMessages.LESS_THAN_START)).show();
	// e.doit = false;
	// return;
	// }
	// widget.setToolTipText("");
	// WidgetUtilities.hideDecorator(widget);
	// return;
	// }
	// widget.setToolTipText(ClientMessagesUtility.getMessage(ClientMessages.ONLY_NUMBERS_ALLOWED));
	// WidgetUtilities.addErrorDecorator(widget,
	// ClientMessagesUtility.getMessage(ClientMessages.ONLY_NUMBERS_ALLOWED)).show();
	// e.doit = false;
	// };
	// }

	private double getMotorAngularPosition() {
		Set<DevicePosition<Double>> start = controller.savePosition(Positions.START);
		return start.stream().filter(dp -> dp.getName().equals(TomographyDevices.MOTOR_STAGE_ROT_Y.name())).findFirst()
				.orElse(new DevicePosition<>(TomographyDevices.MOTOR_STAGE_ROT_Y.name(), 0.0)).getPosition();
	}

	private void updateCurrentAngularPosition() {
		double currentMotorPostion = getMotorAngularPosition();
		getTemplateData().getStart().setCurrentAngle(currentMotorPostion);
	}

	private void configureWhenShow() {
		// if (halfRotationRangeType.getSelection()) {
		// fullGroup.setEnabled(false);
		// customGroup.setEnabled(false);
		// }
		// if (fullRotationRangeType.getSelection()) {
		// customGroup.setEnabled(false);
		// }
		// if (customRotationRangeType.getSelection()) {
		// fullGroup.setEnabled(false);
		// }
		setAngularStep();
	}

	private double totalAngle() {
		double start = getTemplateData().getStart().getStart();
		double end = 180f;

		if (fullRotationRangeType.getSelection()) {
			end = 360f * getTemplateData().getEnd().getNumberRotation();
		}
		if (customRotationRangeType.getSelection()) {
			end = getTemplateData().getEnd().getCustomAngle();
		}
		return end - start;
	}

	private double calculateAngularStep() {
		return totalAngle() / getTemplateData().getProjections().getTotalProjections();
	}

	private void expandBar(ExpandBar parent, Composite group, Button radio) {
		ExpandItem item0 = new ExpandItem(parent, SWT.NONE, 0);
		item0.setControl(group);
		createExpandBarListener(item0, group, radio);
	}

	private void createExpandBarListener(ExpandItem item0, Composite group, Button radio) {
		expandBarItem(item0, group, radio);
		radio.addSelectionListener(new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				expandBarItem(item0, group, Button.class.cast(e.getSource()));
				getShell().pack();
			}
		});
	}

	private void expandBarItem(ExpandItem item0, Composite group, Button radio) {
		item0.setExpanded(radio.getSelection());
		if (radio.getSelection()) {
			item0.getParent().setVisible(true);
			item0.setHeight(group.computeSize(SWT.DEFAULT, SWT.DEFAULT).y);
		} else {
			item0.getParent().setVisible(false);
		}
	}

	private ExpandBar createExpandBar(Composite parent) {
		ExpandBar bar = new ExpandBar(parent, SWT.NONE);
		bar.setLayoutData(new GridData(SWT.FILL, SWT.FILL, true, true));
		bar.setVisible(false);
		return bar;
	}

	private TomographyParameters getTemplateData() {
		if (Objects.isNull(getController().getAcquisition())) {
			try {
				getController().loadData(TomographyParametersAcquisitionController.createNewAcquisition());
			} catch (AcquisitionControllerException e) {
				logger.error("Cannot create the acquisition controller", e);
			}
		}
		return getController().getAcquisition().getAcquisitionConfiguration().getAcquisitionParameters();
	}
}
