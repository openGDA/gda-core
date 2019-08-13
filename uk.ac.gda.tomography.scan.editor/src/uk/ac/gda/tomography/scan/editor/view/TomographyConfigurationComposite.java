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

import uk.ac.gda.tomography.base.TomographyParameters;
import uk.ac.gda.tomography.model.MultipleScansType;
import uk.ac.gda.tomography.model.RangeType;
import uk.ac.gda.tomography.model.ScanType;
import uk.ac.gda.tomography.ui.controller.TomographyParametersAcquisitionController;
import uk.ac.gda.tomography.ui.tool.TomographyBindingElements;
import uk.ac.gda.tomography.ui.tool.TomographySWTElements;

/**
 * This Composite allows to edit a {@link TomographyParameters} object.
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
	private Text numberFlat;
	private Button beforeAcquisition;
	private Button afterAcquisition;

	/** The MultipleScans Composite elements **/
	private Button multipleScans;
	private Text numberRepetitions;
	private Text waitingTime;
	private Button repeateMultipleScansType;
	private Button switchbackMultipleScansType;

	public TomographyConfigurationComposite(Composite parent, TomographyParametersAcquisitionController controller) {
		this(parent, SWT.NONE, controller);
	}

	public TomographyConfigurationComposite(Composite parent, int style, TomographyParametersAcquisitionController controller) {
		super(parent, style, controller);
	}

	@Override
	protected void createElements(int labelStyle, int textStyle) {
		GridLayoutFactory.swtDefaults().margins(TomographySWTElements.defaultCompositeMargin()).applyTo(this);
		nameAndScanTypeContent(TomographySWTElements.createComposite(this, SWT.NONE, 2), labelStyle, textStyle);
		startAngleContent(TomographySWTElements.createGroup(this, 2, TomographyMessages.START), labelStyle, textStyle);
		endAngleContent(TomographySWTElements.createGroup(this, 3, TomographyMessages.END), labelStyle, textStyle);
		projectionsContent(TomographySWTElements.createGroup(this, 2, TomographyMessages.PROJECTIONS), labelStyle, textStyle);
		imagesCalibrationContent(TomographySWTElements.createGroup(this, 3, TomographyMessages.IMAGE_CALIBRATION), labelStyle, textStyle);
		multipleScansContent(this, labelStyle, textStyle);
	}

	private void nameAndScanTypeContent(Composite parent, int labelStyle, int textStyle) {
		TomographySWTElements.createLabel(parent, labelStyle, TomographyMessages.NAME, new Point(2, 1));
		this.name = TomographySWTElements.createText(parent, textStyle, null, new Point(2, 1), TomographyMessages.NAME_TOOLTIP, new Point(500, SWT.DEFAULT));
		flyScanType = TomographySWTElements.createButton(parent, SWT.RADIO, TomographyMessages.FLY_SCAN, TomographyMessages.FLY_SCAN_TOOLTIP);
		flyScanType.setData(ScanType.FLY);
		stepScanType = TomographySWTElements.createButton(parent, SWT.RADIO, TomographyMessages.STEP_SCAN, TomographyMessages.STEP_SCAN_TOOLTIP);
		stepScanType.setData(ScanType.STEP);
	}

	private void startAngleContent(Composite parent, int labelStyle, int textStyle) {
		TomographySWTElements.createLabel(parent, labelStyle, TomographyMessages.ANGLE, new Point(2, 1));
		startAngleText = TomographySWTElements.createText(parent, textStyle, TomographyVerifyListener.verifyOnlyDoubleText, null,
				TomographyMessages.START_ANGLE_TOOLTIP);
		currentAngleButton = TomographySWTElements.createButton(parent, SWT.CHECK, TomographyMessages.CURRENT_ANGLE, TomographyMessages.CURRENT_ANGLE_TOOLTIP);
	}

	private void endAngleContent(Composite parent, int labelStyle, int textStyle) {
		halfRotationRangeType = TomographySWTElements.createButton(parent, SWT.RADIO, TomographyMessages.STRAIGHT_ANGLE,
				TomographyMessages.STRAIGHT_ANGLE_TOOLTIP);

		fullRotationRangeType = TomographySWTElements.createButton(parent, SWT.RADIO, TomographyMessages.FULL_ANGLE, TomographyMessages.FULL_ANGLE_TOOLTIP);
		// fullGroup = TomographySWTElements.createGroup(parent, 1, null);
		// TomographySWTElements.createLabel(fullGroup, labelStyle, TomographyMessages.NUM_ROTATIONS);
		// numberRotation = TomographySWTElements.createText(fullGroup, textStyle,
		// TomographyVerifyListener.verifyOnlyDigitText);

		customRotationRangeType = TomographySWTElements.createButton(parent, SWT.RADIO, TomographyMessages.CUSTOM, TomographyMessages.CUSTOM_END_TOOLTIP);
		ExpandBar customBar = createExpandBar(parent);
		customGroup = TomographySWTElements.createGroup(customBar, 1, null);
		TomographySWTElements.createLabel(customGroup, labelStyle, TomographyMessages.ANGLE);
		customAngle = TomographySWTElements.createText(customGroup, textStyle, TomographyVerifyListener.verifyOnlyDoubleText, null,
				TomographyMessages.CUSTOM_END_TOOLTIP);
		expandBar(customBar, customGroup, customRotationRangeType);
	}

	private void projectionsContent(Composite parent, int labelStyle, int textStyle) {
		TomographySWTElements.createLabel(parent, labelStyle, TomographyMessages.TOTAL_PROJECTIONS);
		TomographySWTElements.createLabel(parent, labelStyle, TomographyMessages.ANGULAR_STEP);
		totalProjections = TomographySWTElements.createText(parent, textStyle, TomographyVerifyListener.verifyOnlyIntegerText, null,
				TomographyMessages.TOTAL_PROJECTIONS_TOOLTIP);
		angularStep = TomographySWTElements.createLabel(parent, labelStyle, TomographyMessages.EMPTY_MESSAGE);
	}

	private void imagesCalibrationContent(Composite parent, int labelStyle, int textStyle) {
		TomographySWTElements.createLabel(parent, labelStyle, TomographyMessages.NUM_DARK);
		TomographySWTElements.createLabel(parent, labelStyle, TomographyMessages.NUM_FLAT);
		TomographySWTElements.createEmptyCell(parent, labelStyle);
		numberDark = TomographySWTElements.createText(parent, textStyle, TomographyVerifyListener.verifyOnlyIntegerText, new Point(1, 2),
				TomographyMessages.NUM_DARK_TOOLTIP);
		numberFlat = TomographySWTElements.createText(parent, textStyle, TomographyVerifyListener.verifyOnlyIntegerText, new Point(1, 2),
				TomographyMessages.NUM_FLAT_TOOLTIP);
		beforeAcquisition = TomographySWTElements.createButton(parent, SWT.CHECK, TomographyMessages.AT_START, TomographyMessages.AT_START_TOOLTIP);
		afterAcquisition = TomographySWTElements.createButton(parent, SWT.CHECK, TomographyMessages.AT_END, TomographyMessages.AT_END_TOOLTIP);
	}

	private void multipleScansContent(Composite parent, int labelStyle, int textStyle) {
		multipleScans = TomographySWTElements.createButton(parent, SWT.CHECK, TomographyMessages.MULTIPLE_SCANS, TomographyMessages.MULTIPLE_SCANS_TOOLTIP);
		ExpandBar customBar = createExpandBar(parent);
		Group multipleScan = TomographySWTElements.createGroup(customBar, 3, TomographyMessages.EMPTY_MESSAGE);
		TomographySWTElements.createLabel(multipleScan, labelStyle, TomographyMessages.NUM_REPETITIONS);
		TomographySWTElements.createLabel(multipleScan, labelStyle, TomographyMessages.WAITING_TIME);
		TomographySWTElements.createEmptyCell(multipleScan, labelStyle);
		numberRepetitions = TomographySWTElements.createText(multipleScan, textStyle, TomographyVerifyListener.verifyOnlyIntegerText, new Point(1, 2),
				TomographyMessages.NUM_REPETITIONS_TOOLTIP);
		waitingTime = TomographySWTElements.createText(multipleScan, textStyle, TomographyVerifyListener.verifyOnlyIntegerText, new Point(1, 2),
				TomographyMessages.WAITING_TIME_TOOLTIP);
		repeateMultipleScansType = TomographySWTElements.createButton(multipleScan, SWT.RADIO, TomographyMessages.REPEATE_SCAN,
				TomographyMessages.REPEATE_SCAN_TOOLTIP);
		switchbackMultipleScansType = TomographySWTElements.createButton(multipleScan, SWT.RADIO, TomographyMessages.SWITCHBACK_SCAN,
				TomographyMessages.SWITCHBACK_SCAN_TOOLTIP);

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

		TomographyBindingElements.bindText(dbc, name, String.class, "name", getTemplateData());

		TomographyBindingElements.bindCheckBox(dbc, currentAngleButton, "start.useCurrentAngle", getTemplateData());
		TomographyBindingElements.bindText(dbc, startAngleText, Double.class, "start.start", getTemplateData());

//		TomographyBindingElements.bindText(dbc, numberRotation, Integer.class, "end.numberRotation", getTemplateData());
		TomographyBindingElements.bindText(dbc, customAngle, Double.class, "end.customAngle", getTemplateData());

		TomographyBindingElements.bindText(dbc, totalProjections, Double.class, "projections.totalProjections", getTemplateData());

		TomographyBindingElements.bindCheckBox(dbc, beforeAcquisition, "imageCalibration.beforeAcquisition", getTemplateData());
		TomographyBindingElements.bindCheckBox(dbc, afterAcquisition, "imageCalibration.afterAcquisition", getTemplateData());
		TomographyBindingElements.bindText(dbc, numberDark, Integer.class, "imageCalibration.numberDark", getTemplateData());
		TomographyBindingElements.bindText(dbc, numberFlat, Integer.class, "imageCalibration.numberFlat", getTemplateData());

		TomographyBindingElements.bindCheckBox(dbc, multipleScans, "multipleScans.enabled", getTemplateData());
		multipleScans.setSelection(multipleScans.getSelection());
		TomographyBindingElements.bindText(dbc, numberRepetitions, Integer.class, "multipleScans.numberRepetitions", getTemplateData());
		TomographyBindingElements.bindText(dbc, waitingTime, Integer.class, "multipleScans.waitingTime", getTemplateData());
	}

	private void bindScanType(DataBindingContext dbc) {
		Map<ScanType, Object> enumRadioMap = new EnumMap<>(ScanType.class);
		enumRadioMap.put(ScanType.FLY, flyScanType);
		enumRadioMap.put(ScanType.STEP, stepScanType);
		TomographyBindingElements.bindEnumToRadio(dbc, ScanType.class, "scanType", getTemplateData(), enumRadioMap);
	}

	private void bindMultipleScanType(DataBindingContext dbc) {
		Map<MultipleScansType, Object> enumRadioMap = new EnumMap<>(MultipleScansType.class);
		enumRadioMap.put(MultipleScansType.REPEAT_SCAN, repeateMultipleScansType);
		enumRadioMap.put(MultipleScansType.SWITCHBACK_SCAN, switchbackMultipleScansType);
		TomographyBindingElements.bindEnumToRadio(dbc, MultipleScansType.class, "multipleScans.multipleScansType", getTemplateData(), enumRadioMap);
	}

	private void binRangeType(DataBindingContext dbc) {
		Map<RangeType, Object> enumRadioMap = new EnumMap<>(RangeType.class);
		enumRadioMap.put(RangeType.RANGE_180, halfRotationRangeType);
		enumRadioMap.put(RangeType.RANGE_360, fullRotationRangeType);
		enumRadioMap.put(RangeType.CUSTOM, customRotationRangeType);
		TomographyBindingElements.bindEnumToRadio(dbc, RangeType.class, "end.rangeType", getTemplateData(), enumRadioMap);
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
						// fullGroup.setEnabled(false);
						customGroup.setEnabled(false);
					} else if (source.equals(fullRotationRangeType)) {
						// fullGroup.setEnabled(true);
						customGroup.setEnabled(false);
					} else if (source.equals(customRotationRangeType)) {
						// fullGroup.setEnabled(false);
						customGroup.setEnabled(true);
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
//			if (getTemplateData().getEnd().getCustomAngle() < getTemplateData().getStart().getStart()) {
//				getTemplateData().getEnd().setCustomAngle(getTemplateData().getStart().getStart());
//				customAngle.setText(startAngleText.getText());
//			}
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
			angularStep.setText(Double.toString(calculateAngularStep()));
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
				angularStep.setText(Double.toString(calculateAngularStep()));
			}
		};
		currentAngleButton.addSelectionListener(useCurrentAngleListener);
	}

	// private VerifyListener validateCustomAngle() {
	// return e -> {
	// Text widget = Text.class.cast(e.widget);
	// String currentText = widget.getText();
	// String newText = (currentText.substring(0, e.start) + e.text + currentText.substring(e.end));
	// if (TomographyVerifyListener.stringIsDoubleNumber(newText)) {
	// if (Double.parseDouble(newText) < getTemplateData().getStart().getStart()) {
	// widget.setToolTipText(TomographyMessagesUtility.getMessage(TomographyMessages.LESS_THAN_START));
	// WidgetUtilities.addErrorDecorator(widget,
	// TomographyMessagesUtility.getMessage(TomographyMessages.LESS_THAN_START)).show();
	// e.doit = false;
	// return;
	// }
	// widget.setToolTipText("");
	// WidgetUtilities.hideDecorator(widget);
	// return;
	// }
	// widget.setToolTipText(TomographyMessagesUtility.getMessage(TomographyMessages.ONLY_NUMBERS_ALLOWED));
	// WidgetUtilities.addErrorDecorator(widget,
	// TomographyMessagesUtility.getMessage(TomographyMessages.ONLY_NUMBERS_ALLOWED)).show();
	// e.doit = false;
	// };
	// }

	private double getMotorAngularPosition() {
		// TBD
		return 33.33;
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
		angularStep.setText(Double.toString(calculateAngularStep()));
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
		return getController().getAcquisition().getAcquisitionConfiguration().getAcquisitionParameters();
	}
}
