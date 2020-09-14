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

import static uk.ac.gda.ui.tool.ClientSWTElements.DEFAULT_TEXT_SIZE;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientButton;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientCompositeWithGridLayout;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientEmptyCell;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGroup;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientLabel;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientText;
import static uk.ac.gda.ui.tool.ClientVerifyListener.verifyOnlyDoubleText;
import static uk.ac.gda.ui.tool.ClientVerifyListener.verifyOnlyIntegerText;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.custom.ScrolledComposite;
import org.eclipse.swt.events.ExpandEvent;
import org.eclipse.swt.events.ExpandListener;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.ExpandBar;
import org.eclipse.swt.widgets.ExpandItem;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.springframework.context.ApplicationListener;

import gda.mscan.element.Mutator;
import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.mapping.api.document.helper.MultipleScansHelper;
import uk.ac.diamond.daq.mapping.api.document.helper.ScanpathDocumentHelper;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningConfiguration;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.diamond.daq.mapping.ui.stage.IStageController;
import uk.ac.diamond.daq.mapping.ui.stage.enumeration.StageDevice;
import uk.ac.gda.api.acquisition.AcquisitionController;
import uk.ac.gda.api.acquisition.configuration.MultipleScansType;
import uk.ac.gda.api.acquisition.resource.event.AcquisitionConfigurationResourceLoadEvent;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.ui.tool.ClientBindingElements;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;
import uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy;

/**
 * This Composite allows to edit a {@link ScanningParameters} object.
 *
 * @author Maurizio Nagni
 */
public class TomographyConfigurationCompositeFactory implements CompositeFactory {

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
	private ExpandItem multiplExpandItem;
	private Composite multipleGroup;

	protected final AcquisitionController<ScanningAcquisition> controller;
	private final IStageController stageController;
	private ScanpathDocumentHelper dataHelper;
	private MultipleScansHelper configurationHelper;

	private Composite mainComposite;
	private ScrolledComposite scrolledComposite;
	private DataBindingContext dbc = new DataBindingContext();

	public TomographyConfigurationCompositeFactory(AcquisitionController<ScanningAcquisition> controller, IStageController stageController) {
		super();
		this.controller = controller;
		this.stageController = stageController;
		this.dataHelper = new ScanpathDocumentHelper(this::getAcquisitionParameters);
		this.configurationHelper = new MultipleScansHelper(this::getAcquisitionConfiguration);
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		scrolledComposite = new ScrolledComposite(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		GridLayoutFactory.fillDefaults().applyTo(scrolledComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(scrolledComposite);

		mainComposite = createClientCompositeWithGridLayout(scrolledComposite, SWT.NONE, 3);
		scrolledComposite.setContent(mainComposite);

		createElements(mainComposite, SWT.NONE, SWT.BORDER);
		bindElements();
		initialiseElements();
		addWidgetsListener();
		try {
			SpringApplicationContextProxy.addDisposableApplicationListener(mainComposite, new LoadListener(mainComposite));
		} catch (GDAClientException e) {
			UIHelper.showWarning("Loading a file will not refresh the gui", "Spring application listener not registered");
		}
		scrolledComposite.setMinSize(mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
		return mainComposite;
	}

	private AcquisitionController<ScanningAcquisition> getController() {
		return controller;
	}

	/**
	 * @param parent
	 *            a three column composite
	 * @param labelStyle
	 * @param textStyle
	 */
	private void createElements(Composite parent, int labelStyle, int textStyle) {
		nameAndScanTypeContent(parent, labelStyle, textStyle);

		// Defines a Group with 3 columns
		Group group = createClientGroup(parent, SWT.NONE, 3, ClientMessages.START);
		// Configure the group to span all the parent 3 columns
		createClientGridDataFactory().span(3, 1).applyTo(group);
		startAngleContent(group, labelStyle, textStyle);

		// Defines a Group with 4 columns
		group = createClientGroup(parent, SWT.NONE, 4, ClientMessages.END);
		// Configure the group to span all the parent 3 columns
		createClientGridDataFactory().span(3, 1).applyTo(group);
		endAngleContent(group, labelStyle, textStyle);

		// Defines a Group with 3 columns
		group = createClientGroup(parent, SWT.NONE, 3, ClientMessages.PROJECTIONS);
		// Configure the group to span all the parent 3 columns
		createClientGridDataFactory().span(3, 1).applyTo(group);
		projectionsContent(group, labelStyle, textStyle);

		group = createClientGroup(parent, SWT.NONE, 6, ClientMessages.IMAGE_CALIBRATION);
		createClientGridDataFactory().span(3, 1).applyTo(group);
		imagesCalibrationContent(group, labelStyle, textStyle);

		multipleScansContentAlternative(parent, labelStyle, textStyle);

		multipleGroup = createClientCompositeWithGridLayout(parent, SWT.NONE, 1);
		createClientGridDataFactory().span(3, 1).applyTo(multipleGroup);
		multipleScansContent(multipleGroup, labelStyle, textStyle);
	}

	private void nameAndScanTypeContent(Composite parent, int labelStyle, int textStyle) {
		Label labelName = createClientLabel(parent, labelStyle, ClientMessages.NAME);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.END).applyTo(labelName);

		this.name = createClientText(parent, textStyle, ClientMessages.NAME_TOOLTIP, Optional.empty());
		createClientGridDataFactory().span(2, 1).applyTo(this.name);

		flyScanType = createClientButton(parent, SWT.RADIO, ClientMessages.FLY_SCAN, ClientMessages.FLY_SCAN_TOOLTIP);
		flyScanType.setData(ScanType.FLY);
		createClientGridDataFactory().applyTo(flyScanType);

		stepScanType = createClientButton(parent, SWT.RADIO, ClientMessages.STEP_SCAN, ClientMessages.STEP_SCAN_TOOLTIP);
		stepScanType.setData(ScanType.STEP);
		createClientGridDataFactory().applyTo(stepScanType);
	}

	/**
	 * @param parent
	 *            a three columns composite
	 * @param labelStyle
	 * @param textStyle
	 */
	private void startAngleContent(Composite parent, int labelStyle, int textStyle) {
		Label labelStart = createClientLabel(parent, labelStyle, ClientMessages.ANGLE);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(80, SWT.DEFAULT).indent(5, SWT.DEFAULT).applyTo(labelStart);

		startAngleText = createClientText(parent, textStyle, ClientMessages.START_ANGLE_TOOLTIP, verifyOnlyDoubleText);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(DEFAULT_TEXT_SIZE).indent(5, SWT.DEFAULT)
				.applyTo(startAngleText);

		currentAngleButton = createClientButton(parent, SWT.CHECK, ClientMessages.CURRENT_ANGLE, ClientMessages.CURRENT_ANGLE_TOOLTIP);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(150, SWT.DEFAULT).indent(5, SWT.DEFAULT)
				.applyTo(currentAngleButton);
	}

	private void endAngleContent(Composite parent, int labelStyle, int textStyle) {
		halfRotationRangeType = createClientButton(parent, SWT.RADIO, ClientMessages.STRAIGHT_ANGLE, ClientMessages.STRAIGHT_ANGLE_TOOLTIP);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).applyTo(halfRotationRangeType);

		fullRotationRangeType = createClientButton(parent, SWT.RADIO, ClientMessages.FULL_ANGLE, ClientMessages.FULL_ANGLE_TOOLTIP);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).applyTo(fullRotationRangeType);

		customRotationRangeType = createClientButton(parent, SWT.RADIO, ClientMessages.CUSTOM, ClientMessages.CUSTOM_END_TOOLTIP);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).applyTo(customRotationRangeType);

		customAngle = createClientText(parent, textStyle, ClientMessages.CUSTOM_END_TOOLTIP, verifyOnlyDoubleText);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(DEFAULT_TEXT_SIZE).applyTo(customAngle);
	}

	private void projectionsContent(Composite parent, int labelStyle, int textStyle) {
		Label label = createClientLabel(parent, labelStyle, ClientMessages.TOTAL_PROJECTIONS);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).applyTo(label);

		totalProjections = createClientText(parent, textStyle, ClientMessages.TOTAL_PROJECTIONS_TOOLTIP, verifyOnlyIntegerText);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(DEFAULT_TEXT_SIZE).span(2, 1)
				.applyTo(totalProjections);

		label = createClientLabel(parent, labelStyle, ClientMessages.ANGULAR_STEP);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).applyTo(label);

		angularStep = createClientLabel(parent, labelStyle, ClientMessages.EMPTY_MESSAGE);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).span(2, 1).applyTo(angularStep);
	}

	private void imagesCalibrationContent(Composite parent, int labelStyle, int textStyle) {
		Label label = createClientLabel(parent, labelStyle, ClientMessages.NUM_DARK);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).indent(5, SWT.DEFAULT).applyTo(label);

		numberDark = createClientText(parent, textStyle, ClientMessages.NUM_DARK_TOOLTIP, verifyOnlyIntegerText);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(DEFAULT_TEXT_SIZE).applyTo(numberDark);

		createClientEmptyCell(parent, new Point(50, 10));

		label = createClientLabel(parent, labelStyle, ClientMessages.DARK_EXPOSURE);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).indent(20, SWT.DEFAULT).applyTo(label);

		darkExposure = createClientText(parent, textStyle, ClientMessages.DARK_EXPOSURE_TP, verifyOnlyIntegerText);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(DEFAULT_TEXT_SIZE).span(2, 1)
				.applyTo(darkExposure);

		label = createClientLabel(parent, labelStyle, ClientMessages.NUM_FLAT);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).indent(5, SWT.DEFAULT).applyTo(label);

		numberFlat = createClientText(parent, textStyle, ClientMessages.NUM_FLAT_TOOLTIP, verifyOnlyIntegerText);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(DEFAULT_TEXT_SIZE).span(2, 1)
				.applyTo(numberFlat);

		label = createClientLabel(parent, labelStyle, ClientMessages.FLAT_EXPOSURE);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).indent(20, SWT.DEFAULT).applyTo(label);

		flatExposure = createClientText(parent, textStyle, ClientMessages.FLAT_EXPOSURE_TP, verifyOnlyIntegerText);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(DEFAULT_TEXT_SIZE).span(2, 1)
				.applyTo(flatExposure);

		beforeAcquisition = createClientButton(parent, SWT.CHECK, ClientMessages.AT_START, ClientMessages.AT_START_TOOLTIP);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).span(2, 1).applyTo(beforeAcquisition);

		afterAcquisition = createClientButton(parent, SWT.CHECK, ClientMessages.AT_END, ClientMessages.AT_END_TOOLTIP);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).span(2, 1).applyTo(afterAcquisition);
	}

	private void multipleScansContentAlternative(Composite parent, int labelStyle, int textStyle) {
		final Button multipleScansAlternative = createClientButton(parent, SWT.PUSH, ClientMessages.MULTIPLE_SCANS,
				ClientMessages.MULTIPLE_SCANS_TOOLTIP);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).span(3, 1).applyTo(multipleScansAlternative);

		multipleScansAlternative.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.widget.equals(multipleScansAlternative)) {
					new MultipleScanDialog(parent.getShell(), () -> getAcquisitionConfiguration()).open();
					updateMultipleScan();
					if (getAcquisitionConfiguration().getMultipleScans().getNumberRepetitions() > 1
							|| getAcquisitionConfiguration().getMultipleScans().getWaitingTime() > 0
							|| !MultipleScansType.REPEAT_SCAN.equals(getAcquisitionConfiguration().getMultipleScans().getMultipleScansType())) {
						multipleScansAlternative.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_YELLOW));
					} else {
						multipleScansAlternative.setBackground(null);
					}
				}
			}

			@Override
			public void widgetDefaultSelected(SelectionEvent e) {
				// Has no default selection
			}
		});
	}

	private void multipleScansContent(Composite parent, int labelStyle, int textStyle) {
		ExpandBar customBar = createExpandBar(parent);
		Composite container = createClientCompositeWithGridLayout(customBar, SWT.NONE, 5);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).grab(true, true).applyTo(container);

		Label label = createClientLabel(container, SWT.NONE, ClientMessages.NUM_REPETITIONS);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).applyTo(label);

		numberRepetitions = createClientText(container, SWT.NONE, ClientMessages.NUM_REPETITIONS_TOOLTIP, verifyOnlyIntegerText);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(DEFAULT_TEXT_SIZE).applyTo(numberRepetitions);

		createClientEmptyCell(container, new Point(10, 10));

		label = createClientLabel(container, SWT.NONE, ClientMessages.WAITING_TIME);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).applyTo(label);
		label.setText(label.getText() + " (s)");

		waitingTime = createClientText(container, SWT.NONE, ClientMessages.WAITING_TIME_TOOLTIP, verifyOnlyIntegerText);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(DEFAULT_TEXT_SIZE).applyTo(waitingTime);

		repeateMultipleScansType = createClientButton(container, SWT.RADIO, ClientMessages.REPEATE_SCAN,
				ClientMessages.REPEATE_SCAN_TOOLTIP);
		repeateMultipleScansType.setData(MultipleScansType.REPEAT_SCAN);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).applyTo(repeateMultipleScansType);

		switchbackMultipleScansType = createClientButton(container, SWT.RADIO, ClientMessages.SWITCHBACK_SCAN,
				ClientMessages.SWITCHBACK_SCAN_TOOLTIP);
		switchbackMultipleScansType.setData(MultipleScansType.SWITCHBACK_SCAN);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).applyTo(switchbackMultipleScansType);

		multiplExpandItem = expandBar(customBar, container, multipleScans);
		multiplExpandItem.setExpanded(true);
		customBar.addExpandListener(new ExpandListener() {

			@Override
			public void itemExpanded(ExpandEvent e) {
				customBar.layout(true, true);
				// scrolledComposite.setMinSize(mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				// scrolledComposite.layout(true, true);
			}

			@Override
			public void itemCollapsed(ExpandEvent e) {
				customBar.layout(true, true);
				// scrolledComposite.setMinSize(mainComposite.computeSize(SWT.DEFAULT, SWT.DEFAULT));
				// scrolledComposite.layout(true, true);
			}
		});
	}

	private void startAngleTextListener(ModifyEvent event) {
		if (!event.getSource().equals(startAngleText))
			return;
		double angle = Optional.ofNullable(startAngleText.getText()).filter(s -> !s.isEmpty()).map(Double::parseDouble).orElse(0.0);
		dataHelper.updateStartAngle(angle);
		updateAngularStep();
	}

	private void customAngleTextListener(ModifyEvent event) {
		if (!event.getSource().equals(customAngle) || !customRotationRangeType.getSelection())
			return;
		double angle = Optional.ofNullable(customAngle.getText()).filter(s -> !s.isEmpty()).map(Double::parseDouble).orElse(0.0);
		dataHelper.updateStopAngle(angle);
		updateAngularStep();
	}

	private void totalProjectionsListener(ModifyEvent event) {
		if (!event.getSource().equals(totalProjections))
			return;
		int points = Optional.ofNullable(totalProjections.getText()).filter(s -> !s.isEmpty()).map(Integer::parseInt).orElse(1);
		dataHelper.updatePoints(points);
		updateAngularStep();
	}

	private SelectionListener predefinedAngleListener = new SelectionListener() {
		@Override
		public void widgetSelected(SelectionEvent event) {
			if (!Button.class.isInstance(event.getSource()))
				return;

			if (!((Button) event.getSource()).getSelection())
				return;
			if (event.getSource().equals(currentAngleButton)) {
				updateCurrentAngularPosition();
			} else if (event.getSource().equals(fullRotationRangeType)) {
				dataHelper.updateStopAngle(360.0);
			} else if (event.getSource().equals(halfRotationRangeType)) {
				dataHelper.updateStopAngle(180.0);
			} else if (event.getSource().equals(customRotationRangeType)) {
				customAngle.setEnabled(true);
				customAngle.setText(Double.toString(getScannableTrackDocument().getStop()));
			}
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent event) {
			// do nothing
		}

		private void updateCurrentAngularPosition() {
			double currentMotorPostion = stageController.getMotorPosition(StageDevice.MOTOR_STAGE_ROT_Y);
			startAngleText.setText(Double.toString(currentMotorPostion));
		}
	};

	private SelectionListener scanTypeListener = new SelectionListener() {
		@Override
		public void widgetSelected(SelectionEvent event) {
			if (!Button.class.isInstance(event.getSource()))
				return;

			if (!((Button) event.getSource()).getSelection())
				return;


			if (event.getSource().equals(flyScanType)) {
				addContinuous();
			} else if (event.getSource().equals(stepScanType)) {
				removeContinuous();
			}
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent event) {
			// do nothing
		}

		private void removeContinuous() {
			dataHelper.removeMutators(Mutator.CONTINUOUS);
		}
	};

	private void addContinuous() {
		dataHelper.addMutators(Mutator.CONTINUOUS, new ArrayList<>());
	}

	private void numberRepetitionsListener(ModifyEvent event) {
		if (!event.getSource().equals(numberRepetitions))
			return;
		int repetitions = Optional.ofNullable(numberRepetitions.getText()).filter(s -> !s.isEmpty()).map(Integer::parseInt).orElse(1);
		configurationHelper.updateMultipleScanRepetitions(repetitions);
	}

	private void waitingTimeListener(ModifyEvent event) {
		if (!event.getSource().equals(waitingTime))
			return;
		int wTime = Optional.ofNullable(waitingTime.getText()).filter(s -> !s.isEmpty()).map(Integer::parseInt).orElse(1);
		configurationHelper.updateMultipleScanWaitingTime(wTime);
	}

	private void switchbackScanTypeListener(SelectionEvent event) {
		if (!event.getSource().equals(switchbackMultipleScansType))
			return;
		if (switchbackMultipleScansType.getSelection())
			configurationHelper.updateMultipleScanType(MultipleScansType.SWITCHBACK_SCAN);
	}

	private void repeateMultipleScansType(SelectionEvent event) {
		if (!event.getSource().equals(repeateMultipleScansType))
			return;
		if (repeateMultipleScansType.getSelection())
			configurationHelper.updateMultipleScanType(MultipleScansType.REPEAT_SCAN);
	}

	private  void addWidgetsListener() {
		flyScanType.addSelectionListener(scanTypeListener);
		stepScanType.addSelectionListener(scanTypeListener);

		startAngleText.addModifyListener(this::startAngleTextListener);
		customAngle.addModifyListener(this::customAngleTextListener);
		customAngle.addFocusListener(FocusListener.focusLostAdapter(c -> updateAngularStep()));
		currentAngleButton.addSelectionListener(predefinedAngleListener);

		endGroupsListeners();

		totalProjections.addModifyListener(this::totalProjectionsListener);

		numberRepetitions.addModifyListener(this::numberRepetitionsListener);
		waitingTime.addModifyListener(this::waitingTimeListener);
		switchbackMultipleScansType.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::switchbackScanTypeListener));
		repeateMultipleScansType.addSelectionListener(SelectionListener.widgetSelectedAdapter(this::repeateMultipleScansType));
	}

	private void bindElements() {
		bindScanType(dbc);
		bindMultipleScanType(dbc);

		name.addModifyListener(modifyNameListener);

		ClientBindingElements.bindCheckBox(dbc, beforeAcquisition, "imageCalibration.beforeAcquisition", getAcquisitionConfiguration());
		ClientBindingElements.bindCheckBox(dbc, afterAcquisition, "imageCalibration.afterAcquisition", getAcquisitionConfiguration());
		ClientBindingElements.bindText(dbc, numberDark, Integer.class, "imageCalibration.numberDark", getAcquisitionConfiguration());
		ClientBindingElements.bindText(dbc, numberFlat, Integer.class, "imageCalibration.numberFlat", getAcquisitionConfiguration());
		ClientBindingElements.bindText(dbc, darkExposure, Integer.class, "imageCalibration.darkExposure", getAcquisitionConfiguration());
		ClientBindingElements.bindText(dbc, flatExposure, Integer.class, "imageCalibration.flatExposure", getAcquisitionConfiguration());
	}

	private final ModifyListener modifyNameListener = event -> updateAcquisitionName();

	private void updateAcquisitionName() {
		getController().getAcquisition().setName(name.getText());
	}

	private void bindScanType(DataBindingContext dbc) {
		Map<ScanType, Object> enumRadioMap = new EnumMap<>(ScanType.class);
		enumRadioMap.put(ScanType.FLY, flyScanType);
		enumRadioMap.put(ScanType.STEP, stepScanType);
		ClientBindingElements.bindEnumToRadio(dbc, ScanType.class, "scanType", getAcquisitionParameters(), enumRadioMap);
	}

	private void bindMultipleScanType(DataBindingContext dbc) {
		Map<MultipleScansType, Object> enumRadioMap = new EnumMap<>(MultipleScansType.class);
		enumRadioMap.put(MultipleScansType.REPEAT_SCAN, repeateMultipleScansType);
		enumRadioMap.put(MultipleScansType.SWITCHBACK_SCAN, switchbackMultipleScansType);
		ClientBindingElements.bindEnumToRadio(dbc, MultipleScansType.class, "multipleScans.multipleScansType", getAcquisitionConfiguration(), enumRadioMap);
	}

	private void initialiseElements() {
		name.setText(getController().getAcquisition().getName());
		initializeScanType();
		initializeStartAngle();
		initializeEndAngle();
		updateAngularStep();

		totalProjections.setText(Integer.toString(getScannableTrackDocument().getPoints()));
		forceFocusOnEmpty(numberDark, Integer.toString(getAcquisitionConfiguration().getImageCalibration().getNumberDark()));
		forceFocusOnEmpty(numberFlat, Integer.toString(getAcquisitionConfiguration().getImageCalibration().getNumberFlat()));
		updateMultipleScan();
	}

	private void initializeScanType() {
		stepScanType.setSelection(false);
		flyScanType.setSelection(true);
		addContinuous();
	}

	private void initializeStartAngle() {
		currentAngleButton.setSelection(false);
		startAngleText.setText(Double.toString(getScannableTrackDocument().getStart()));
	}

	private void initializeEndAngle() {
		halfRotationRangeType.setSelection(false);
		fullRotationRangeType.setSelection(false);
		customAngle.setEnabled(false);
		customRotationRangeType.setSelection(false);
		customAngle.setText("");

		if (getScannableTrackDocument().getStop() == 180.0) {
			halfRotationRangeType.setSelection(true);
		} else if (getScannableTrackDocument().getStop() == 360.0) {
			fullRotationRangeType.setSelection(true);
		} else if (!Double.isNaN(getScannableTrackDocument().getStop())) {
			customRotationRangeType.setSelection(true);
			Event event = new Event();
			event.widget = customRotationRangeType;
			Arrays.stream(customRotationRangeType.getListeners(SWT.SELECTED)).forEach(l -> l.handleEvent(event));
			customAngle.setText(Double.toString(getScannableTrackDocument().getStop()));
			customAngle.setEnabled(true);
		}
	}

	private void updateMultipleScan() {
		Arrays.asList(switchbackMultipleScansType, repeateMultipleScansType).stream()
				.filter(i -> getAcquisitionConfiguration().getMultipleScans().getMultipleScansType().equals(i.getData())).findFirst()
				.ifPresent(b -> b.setSelection(true));
		Arrays.asList(switchbackMultipleScansType, repeateMultipleScansType).stream()
				.filter(i -> !getAcquisitionConfiguration().getMultipleScans().getMultipleScansType().equals(i.getData())).findFirst()
				.ifPresent(b -> b.setSelection(false));
		numberRepetitions.setText(Integer.toString(getAcquisitionConfiguration().getMultipleScans().getNumberRepetitions()));
		waitingTime.setText(Integer.toString(getAcquisitionConfiguration().getMultipleScans().getWaitingTime()));
	}

	private void endGroupsListeners() {
		halfRotationRangeType.addSelectionListener(predefinedAngleListener);
		fullRotationRangeType.addSelectionListener(predefinedAngleListener);
		customRotationRangeType.addSelectionListener(predefinedAngleListener);
		SelectionListener activateGroupListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button source = Button.class.cast(e.getSource());

				if (source.getSelection()) {
					if (source.equals(halfRotationRangeType)) {
						customAngle.setText(Double.toString(180));
						customAngle.setEnabled(false);
					} else if (source.equals(fullRotationRangeType)) {
						customAngle.setText(Double.toString(360));
						customAngle.setEnabled(false);
					} else if (source.equals(customRotationRangeType)) {
						customAngle.setText(Double.toString(getScannableTrackDocument().getStop()));
					}
				}
				angularStep.setText(Double.toString(calculateAngularStep()));
			}
		};
		halfRotationRangeType.addSelectionListener(activateGroupListener);
		fullRotationRangeType.addSelectionListener(activateGroupListener);
		customRotationRangeType.addSelectionListener(activateGroupListener);
	}

	private void forceFocusOnEmpty(Text text, String defaultValue) {
		text.addFocusListener(FocusListener.focusLostAdapter(c -> {
			if (text.getText().trim().isEmpty()) {
				text.setText(defaultValue);
			}
		}));
	}

	private void updateAngularStep() {
		double newAngularStep = calculateAngularStep();
		angularStep.setText(Double.toString(newAngularStep));
	}

	private double totalAngle() {
		double start = getScannableTrackDocument().getStart();
		double end = getScannableTrackDocument().getStop();
		return end - start;
	}

	private double calculateAngularStep() {
		return totalAngle() / getScannableTrackDocument().getPoints();
	}

	private ExpandItem expandBar(ExpandBar parent, Composite group, Button radio) {
		ExpandItem item0 = new ExpandItem(parent, SWT.NONE, 0);
		item0.setText(ClientMessagesUtility.getMessage(ClientMessages.MULTIPLE_SCANS));
		item0.setHeight(parent.computeSize(SWT.DEFAULT, 300).y);
		item0.setControl(group);
		// item0.setImage(group.getDisplay().getSystemImage(SWT.ICON_QUESTION));
		return item0;
		// createExpandBarListener(item0, group, radio);
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
		ExpandBar bar = new ExpandBar(parent, SWT.V_SCROLL);
		createClientGridDataFactory().grab(true, true).applyTo(bar);
		return bar;
	}

//	private ScanningConfiguration getConfigurationData() {
//		return getController().getAcquisition().getAcquisitionConfiguration();
//	}

	private ScanningParameters getAcquisitionParameters() {
		return getAcquisitionConfiguration().getAcquisitionParameters();
	}

	private ScanningConfiguration getAcquisitionConfiguration() {
		return getController().getAcquisition().getAcquisitionConfiguration();
	}

	private ScannableTrackDocument getScannableTrackDocument() {
		return getController().getAcquisition().getAcquisitionConfiguration().getAcquisitionParameters().getScanpathDocument().getScannableTrackDocuments()
				.get(0);
	}

	private class LoadListener implements ApplicationListener<AcquisitionConfigurationResourceLoadEvent> {

		private final Composite composite;

		public LoadListener(Composite composite) {
			super();
			this.composite = composite;
		}

		@Override
		public void onApplicationEvent(AcquisitionConfigurationResourceLoadEvent event) {
			if (getController().equals(event.getSource())) {
				bindElements();
				initialiseElements();
				composite.layout(true, true);
			}
		}
	}
}