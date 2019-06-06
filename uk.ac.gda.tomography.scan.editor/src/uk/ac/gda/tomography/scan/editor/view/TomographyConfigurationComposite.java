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
import org.eclipse.swt.events.VerifyListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import uk.ac.gda.tomography.model.MultipleScansType;
import uk.ac.gda.tomography.model.RangeType;
import uk.ac.gda.tomography.model.ScanType;
import uk.ac.gda.tomography.model.TomographyScanParameters;
import uk.ac.gda.tomography.scan.editor.TomographyBindingElements;
import uk.ac.gda.tomography.scan.editor.TomographySWTElements;

/**
 * @author Maurizio Nagni
 */
public class TomographyConfigurationComposite extends TomographyCompositeTemplate<TomographyScanParameters> {

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
	private Text numberRepetitions;
	private Text waitingTime;
	private Button repeateMultipleScansType;
	private Button switchbackMultipleScansType;

	public TomographyConfigurationComposite(Composite parent, TomographyScanParameters tomographyData) {
		this(parent, SWT.NONE, tomographyData);
	}

	public TomographyConfigurationComposite(Composite parent, int style, TomographyScanParameters tomographyData) {
		super(parent, style, tomographyData);
	}

	@Override
	protected void createElements(int labelStyle, int textStyle) {
		GridLayoutFactory.swtDefaults().margins(TomographySWTElements.defaultCompositeMargin()).applyTo(this);
		nameAndScanTypeContent(TomographySWTElements.createComposite(this, SWT.NONE, 2), labelStyle, textStyle);
		startAngleContent(TomographySWTElements.createGroup(this, 2, TomographyMessages.START), labelStyle, textStyle);
		endAngleContent(TomographySWTElements.createGroup(this, 1, TomographyMessages.END), labelStyle, textStyle);
		projectionsContent(TomographySWTElements.createGroup(this, 2, TomographyMessages.PROJECTIONS), labelStyle,
				textStyle);
		imagesCalibrationContent(TomographySWTElements.createGroup(this, 3, TomographyMessages.IMAGE_CALIBRATION),
				labelStyle, textStyle);
		multipleScansContent(TomographySWTElements.createGroup(this, 2, TomographyMessages.MULTIPLE_SCANS), labelStyle,
				textStyle);
	}

	private void nameAndScanTypeContent(Composite parent, int labelStyle, int textStyle) {
		TomographySWTElements.createLabel(parent, labelStyle, TomographyMessages.NAME, new Point(2, 1));
		this.name = TomographySWTElements.createText(parent, textStyle, null, new Point(500, SWT.DEFAULT),
				new Point(2, 1));
		flyScanType = TomographySWTElements.createButton(parent, TomographyMessages.FLY_SCAN, SWT.RADIO);
		flyScanType.setData(ScanType.FLY);
		stepScanType = TomographySWTElements.createButton(parent, TomographyMessages.STEP_SCAN, SWT.RADIO);
		stepScanType.setData(ScanType.STEP);
	}

	private void startAngleContent(Composite parent, int labelStyle, int textStyle) {
		TomographySWTElements.createLabel(parent, labelStyle, TomographyMessages.ANGLE, new Point(2, 1));
		startAngleText = TomographySWTElements.createText(parent, textStyle,
				TomographyVerifyListener.verifyOnlyDigitText);
		currentAngleButton = TomographySWTElements.createButton(parent, TomographyMessages.USE_CURRENT_ANGLE,
				SWT.CHECK);
	}

	private void endAngleContent(Composite parent, int labelStyle, int textStyle) {
		halfRotationRangeType = TomographySWTElements.createButton(parent, "180°", SWT.RADIO);

		fullRotationRangeType = TomographySWTElements.createButton(parent, "360°", SWT.RADIO);
		fullGroup = TomographySWTElements.createGroup(parent, 1, null);
		TomographySWTElements.createLabel(fullGroup, labelStyle, TomographyMessages.NUM_ROTATIONS);
		numberRotation = TomographySWTElements.createText(fullGroup, textStyle,
				TomographyVerifyListener.verifyOnlyDigitText);

		customRotationRangeType = TomographySWTElements.createButton(parent, TomographyMessages.CUSTOM, SWT.RADIO);
		customGroup = TomographySWTElements.createGroup(parent, 1, null);
		TomographySWTElements.createLabel(customGroup, labelStyle, TomographyMessages.ANGLE);

		customAngle = TomographySWTElements.createText(customGroup, textStyle,
				TomographyVerifyListener.verifyOnlyDigitText);
	}

	private void projectionsContent(Composite parent, int labelStyle, int textStyle) {
		TomographySWTElements.createLabel(parent, labelStyle, TomographyMessages.TOTAL_PROJECTIONS);
		TomographySWTElements.createLabel(parent, labelStyle, TomographyMessages.ANGULAR_STEP);
		totalProjections = TomographySWTElements.createText(parent, textStyle,
				TomographyVerifyListener.verifyOnlyDigitText);
		angularStep = TomographySWTElements.createLabel(parent, labelStyle, "");
	}

	private void imagesCalibrationContent(Composite parent, int labelStyle, int textStyle) {
		TomographySWTElements.createLabel(parent, labelStyle, TomographyMessages.NUM_DARK);
		TomographySWTElements.createLabel(parent, labelStyle, TomographyMessages.NUM_FLAT);
		TomographySWTElements.createEmptyCell(parent, labelStyle);
		numberDark = TomographySWTElements.createText(parent, textStyle, TomographyVerifyListener.verifyOnlyDigitText,
				new Point(1, 2));
		numberFlat = TomographySWTElements.createText(parent, textStyle, TomographyVerifyListener.verifyOnlyDigitText,
				new Point(1, 2));
		beforeAcquisition = TomographySWTElements.createButton(parent, TomographyMessages.AT_START, SWT.CHECK);
		afterAcquisition = TomographySWTElements.createButton(parent, TomographyMessages.AT_END, SWT.CHECK);
	}

	private void multipleScansContent(Composite parent, int labelStyle, int textStyle) {
		TomographySWTElements.createLabel(parent, labelStyle, TomographyMessages.NUM_REPETITIONS);
		TomographySWTElements.createLabel(parent, labelStyle, TomographyMessages.WAITING_TIME);
		numberRepetitions = TomographySWTElements.createText(parent, textStyle,
				TomographyVerifyListener.verifyOnlyDigitText);
		waitingTime = TomographySWTElements.createText(parent, textStyle, TomographyVerifyListener.verifyOnlyDigitText);
		repeateMultipleScansType = TomographySWTElements.createButton(parent, TomographyMessages.REPEATE_SCAN,
				SWT.RADIO);
		switchbackMultipleScansType = TomographySWTElements.createButton(parent, TomographyMessages.SWITCHBACK_SCAN,
				SWT.RADIO);
	}

	@Override
	protected void bindElements() {
		DataBindingContext dbc = new DataBindingContext();

		bindScanType(dbc);
		bindMultipleScanType(dbc);
		binRangeType(dbc);

		TomographyBindingElements.bindText(dbc, name, String.class, "name", getTomographyData());
		TomographyBindingElements.bindText(dbc, startAngleText, Double.class, "start.start", getTomographyData());
		TomographyBindingElements.bindText(dbc, numberRotation, Integer.class, "end.numberRotation",
				getTomographyData());
		TomographyBindingElements.bindText(dbc, customAngle, Double.class, "end.customAngle", getTomographyData());
		TomographyBindingElements.bindText(dbc, totalProjections, Double.class, "projections.totalProjections",
				getTomographyData());
		TomographyBindingElements.bindText(dbc, numberDark, Integer.class, "imageCalibration.numberDark",
				getTomographyData());
		TomographyBindingElements.bindText(dbc, numberFlat, Integer.class, "imageCalibration.numberFlat",
				getTomographyData());
		TomographyBindingElements.bindText(dbc, numberRepetitions, Integer.class, "multipleScans.numberRepetitions",
				getTomographyData());
		TomographyBindingElements.bindText(dbc, waitingTime, Integer.class, "multipleScans.waitingTime",
				getTomographyData());

		TomographyBindingElements.bindCheckBox(dbc, currentAngleButton, "start.useCurrentAngle", getTomographyData());
		TomographyBindingElements.bindCheckBox(dbc, beforeAcquisition, "imageCalibration.beforeAcquisition",
				getTomographyData());
		TomographyBindingElements.bindCheckBox(dbc, afterAcquisition, "imageCalibration.afterAcquisition",
				getTomographyData());
	}

	private void bindScanType(DataBindingContext dbc) {
		Map<ScanType, Object> enumRadioMap = new EnumMap<>(ScanType.class);
		enumRadioMap.put(ScanType.FLY, flyScanType);
		enumRadioMap.put(ScanType.STEP, stepScanType);
		TomographyBindingElements.bindEnumToRadio(dbc, ScanType.class, "scanType", getTomographyData(), enumRadioMap);
	}

	private void bindMultipleScanType(DataBindingContext dbc) {
		Map<MultipleScansType, Object> enumRadioMap = new EnumMap<>(MultipleScansType.class);
		enumRadioMap.put(MultipleScansType.REPEAT_SCAN, repeateMultipleScansType);
		enumRadioMap.put(MultipleScansType.SWITCHBACK_SCAN, switchbackMultipleScansType);
		TomographyBindingElements.bindEnumToRadio(dbc, MultipleScansType.class, "multipleScans.multipleScansType",
				getTomographyData(), enumRadioMap);
	}

	private void binRangeType(DataBindingContext dbc) {
		Map<RangeType, Object> enumRadioMap = new EnumMap<>(RangeType.class);
		enumRadioMap.put(RangeType.RANGE_180, halfRotationRangeType);
		enumRadioMap.put(RangeType.RANGE_360, fullRotationRangeType);
		enumRadioMap.put(RangeType.CUSTOM, customRotationRangeType);
		TomographyBindingElements.bindEnumToRadio(dbc, RangeType.class, "end.rangeType", getTomographyData(),
				enumRadioMap);
	}

	@Override
	protected void initialiseElements() {
		endGroupsListeners();
		customAngleLooseFocus();
		angularStepListener();
		useCurrentAngleListener();
		configureWhenShow();
	}

	private void endGroupsListeners() {
		SelectionListener activateGroupListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button source = Button.class.cast(e.getSource());

				if (source.getSelection()) {
					if (source.equals(halfRotationRangeType)) {
						fullGroup.setEnabled(false);
						customGroup.setEnabled(false);
					} else if (source.equals(fullRotationRangeType)) {
						fullGroup.setEnabled(true);
						customGroup.setEnabled(false);
					} else if (source.equals(customRotationRangeType)) {
						fullGroup.setEnabled(false);
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
		FocusListener focusListener = FocusListener.focusLostAdapter(c -> {
			if (getTomographyData().getEnd().getCustomAngle() < getTomographyData().getStart().getStart()) {
				getTomographyData().getEnd().setCustomAngle(getTomographyData().getStart().getStart());
				customAngle.setText(startAngleText.getText());
			}
			updateCurrentAngularPosition();
		});
		customAngle.addFocusListener(focusListener);
	}


	private void angularStepListener() {
		ModifyListener angularStepListener = event -> {
			updateCurrentAngularPosition();
			angularStep.setText(Double.toString(calculateAngularStep()));
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
					getTomographyData().getStart().setStart(getTomographyData().getStart().getCurrentAngle());
					startAngleText.setText(Double.toString(getTomographyData().getStart().getCurrentAngle()));
					startAngleText.setEnabled(false);
				} else {
					startAngleText.setEnabled(true);
				}
				angularStep.setText(Double.toString(calculateAngularStep()));
			}
		};
		currentAngleButton.addSelectionListener(useCurrentAngleListener);
	}

	private VerifyListener validateCustomAngle() {
		return e -> {
			Text widget = Text.class.cast(e.widget);
			String currentText = widget.getText();
			String newText = (currentText.substring(0, e.start) + e.text + currentText.substring(e.end));
			if (TomographyVerifyListener.stringIsNumber(newText)) {
				if (Double.parseDouble(newText) < getTomographyData().getStart().getStart()) {
					widget.setToolTipText(TomographyMessagesUtility.getMessage(TomographyMessages.LESS_THAN_START));
					WidgetUtilities.addErrorDecorator(widget,
							TomographyMessagesUtility.getMessage(TomographyMessages.LESS_THAN_START)).show();
					e.doit = false;
					return;
				}
				widget.setToolTipText("");
				WidgetUtilities.hideDecorator(widget);
				return;
			}
			widget.setToolTipText(TomographyMessagesUtility.getMessage(TomographyMessages.ONLY_NUMBERS_ALLOWED));
			WidgetUtilities.addErrorDecorator(widget,
					TomographyMessagesUtility.getMessage(TomographyMessages.ONLY_NUMBERS_ALLOWED)).show();
			e.doit = false;
		};
	}

	private double getMotorAngularPosition() {
		// TBD
		return 33.33;
	}

	private void updateCurrentAngularPosition() {
		double currentMotorPostion = getMotorAngularPosition();
		getTomographyData().getStart().setCurrentAngle(currentMotorPostion);
	}

	private void configureWhenShow() {
		if (halfRotationRangeType.getSelection()) {
			fullGroup.setEnabled(false);
			customGroup.setEnabled(false);
		}
		if (fullRotationRangeType.getSelection()) {
			customGroup.setEnabled(false);
		}
		if (customRotationRangeType.getSelection()) {
			fullGroup.setEnabled(false);
		}
		angularStep.setText(Double.toString(calculateAngularStep()));
	}

	private double totalAngle() {
		double start = getTomographyData().getStart().getStart();
		double end = start + 180;

		if (fullRotationRangeType.getSelection()) {
			end = 360f * getTomographyData().getEnd().getNumberRotation();
		}
		if (customRotationRangeType.getSelection()) {
			end = start + getTomographyData().getEnd().getCustomAngle();
		}
		return end - start;
	}

	private double calculateAngularStep() {
		return totalAngle() / getTomographyData().getProjections().getTotalProjections();
	}
}
