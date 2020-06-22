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
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationListener;

import gda.mscan.element.Mutator;
import gda.rcp.views.CompositeFactory;
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
import uk.ac.gda.ui.tool.ClientSWTElements;
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
	private ScanningParametersHelper dataHelper;

	private Composite mainComposite;
	private ScrolledComposite scrolledComposite;
	private DataBindingContext dbc = new DataBindingContext();

	private static final Logger logger = LoggerFactory.getLogger(TomographyConfigurationCompositeFactory.class);

	public TomographyConfigurationCompositeFactory(AcquisitionController<ScanningAcquisition> controller, IStageController stageController) {
		super();
		this.controller = controller;
		this.stageController = stageController;
		this.dataHelper = new ScanningParametersHelper(this::getTemplateData);
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		scrolledComposite = new ScrolledComposite(parent, SWT.BORDER | SWT.H_SCROLL | SWT.V_SCROLL);
		scrolledComposite.setExpandHorizontal(true);
		scrolledComposite.setExpandVertical(true);

		GridLayoutFactory.fillDefaults().applyTo(scrolledComposite);
		GridDataFactory.fillDefaults().grab(true, true).applyTo(scrolledComposite);

		mainComposite = ClientSWTElements.createClientCompositeWithGridLayout(scrolledComposite, SWT.NONE, 3);
		scrolledComposite.setContent(mainComposite);

		createElements(mainComposite, SWT.NONE, SWT.BORDER);
		bindElements();
		initialiseElements();
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
		Group group = ClientSWTElements.createClientGroup(parent, SWT.NONE, 3, ClientMessages.START);
		// Configure the group to span all the parent 3 columns
		ClientSWTElements.createClientGridDataFactory().span(3, 1).applyTo(group);
		startAngleContent(group, labelStyle, textStyle);

		// Defines a Group with 4 columns
		group = ClientSWTElements.createClientGroup(parent, SWT.NONE, 4, ClientMessages.END);
		// Configure the group to span all the parent 3 columns
		ClientSWTElements.createClientGridDataFactory().span(3, 1).applyTo(group);
		endAngleContent(group, labelStyle, textStyle);

		// Defines a Group with 3 columns
		group = ClientSWTElements.createClientGroup(parent, SWT.NONE, 3, ClientMessages.PROJECTIONS);
		// Configure the group to span all the parent 3 columns
		ClientSWTElements.createClientGridDataFactory().span(3, 1).applyTo(group);
		projectionsContent(group, labelStyle, textStyle);

		group = ClientSWTElements.createClientGroup(parent, SWT.NONE, 6, ClientMessages.IMAGE_CALIBRATION);
		ClientSWTElements.createClientGridDataFactory().span(3, 1).applyTo(group);
		imagesCalibrationContent(group, labelStyle, textStyle);

		multipleScansContentAlternative(parent, labelStyle, textStyle);

		multipleGroup = ClientSWTElements.createClientCompositeWithGridLayout(parent, SWT.NONE, 1);
		// multipleGroup = ClientSWTElements.createClientGroup(parent, SWT.NONE, 1, ClientMessages.MULTIPLE_SCANS);
		ClientSWTElements.createClientGridDataFactory().span(3, 1).applyTo(multipleGroup);
		multipleScansContent(multipleGroup, labelStyle, textStyle);
	}

	private void nameAndScanTypeContent(Composite parent, int labelStyle, int textStyle) {
		Label labelName = ClientSWTElements.createClientLabel(parent, labelStyle, ClientMessages.NAME, Optional.empty());
		ClientSWTElements.createClientGridDataFactory().align(SWT.BEGINNING, SWT.END).applyTo(labelName);

		this.name = ClientSWTElements.createClientText(parent, textStyle, ClientMessages.NAME_TOOLTIP, Optional.empty());
		ClientSWTElements.createClientGridDataFactory().span(2, 1).applyTo(this.name);

		flyScanType = ClientSWTElements.createClientButton(parent, SWT.RADIO, ClientMessages.FLY_SCAN, ClientMessages.FLY_SCAN_TOOLTIP, Optional.empty());
		flyScanType.setData(ScanType.FLY);
		ClientSWTElements.createClientGridDataFactory().applyTo(flyScanType);

		stepScanType = ClientSWTElements.createClientButton(parent, SWT.RADIO, ClientMessages.STEP_SCAN, ClientMessages.STEP_SCAN_TOOLTIP, Optional.empty());
		stepScanType.setData(ScanType.STEP);
		ClientSWTElements.createClientGridDataFactory().applyTo(stepScanType);
	}

	/**
	 * @param parent
	 *            a three columns composite
	 * @param labelStyle
	 * @param textStyle
	 */
	private void startAngleContent(Composite parent, int labelStyle, int textStyle) {
		Label labelStart = ClientSWTElements.createClientLabel(parent, labelStyle, ClientMessages.ANGLE, Optional.empty());
		ClientSWTElements.createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(80, SWT.DEFAULT).indent(5, SWT.DEFAULT).applyTo(labelStart);

		startAngleText = ClientSWTElements.createClientText(parent, textStyle, ClientMessages.START_ANGLE_TOOLTIP, Optional.of(verifyOnlyDoubleText));
		ClientSWTElements.createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(ClientSWTElements.DEFAULT_TEXT_SIZE).indent(5, SWT.DEFAULT)
				.applyTo(startAngleText);

		currentAngleButton = ClientSWTElements.createClientButton(parent, SWT.CHECK, ClientMessages.CURRENT_ANGLE, ClientMessages.CURRENT_ANGLE_TOOLTIP,
				Optional.empty());
		ClientSWTElements.createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(150, SWT.DEFAULT).indent(5, SWT.DEFAULT)
				.applyTo(currentAngleButton);
	}

	private void endAngleContent(Composite parent, int labelStyle, int textStyle) {
		halfRotationRangeType = ClientSWTElements.createClientButton(parent, SWT.RADIO, ClientMessages.STRAIGHT_ANGLE, ClientMessages.STRAIGHT_ANGLE_TOOLTIP,
				Optional.empty());
		ClientSWTElements.createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).applyTo(halfRotationRangeType);

		fullRotationRangeType = ClientSWTElements.createClientButton(parent, SWT.RADIO, ClientMessages.FULL_ANGLE, ClientMessages.FULL_ANGLE_TOOLTIP,
				Optional.empty());
		ClientSWTElements.createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).applyTo(fullRotationRangeType);

		customRotationRangeType = ClientSWTElements.createClientButton(parent, SWT.RADIO, ClientMessages.CUSTOM, ClientMessages.CUSTOM_END_TOOLTIP,
				Optional.empty());
		ClientSWTElements.createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).applyTo(customRotationRangeType);

		customAngle = ClientSWTElements.createClientText(parent, textStyle, ClientMessages.CUSTOM_END_TOOLTIP, Optional.of(verifyOnlyDoubleText));
		ClientSWTElements.createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(ClientSWTElements.DEFAULT_TEXT_SIZE).applyTo(customAngle);
	}

	private void projectionsContent(Composite parent, int labelStyle, int textStyle) {
		Label label = ClientSWTElements.createClientLabel(parent, labelStyle, ClientMessages.TOTAL_PROJECTIONS, Optional.empty());
		ClientSWTElements.createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).applyTo(label);

		totalProjections = ClientSWTElements.createClientText(parent, textStyle, ClientMessages.TOTAL_PROJECTIONS_TOOLTIP, Optional.of(verifyOnlyIntegerText));
		ClientSWTElements.createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(ClientSWTElements.DEFAULT_TEXT_SIZE).span(2, 1)
				.applyTo(totalProjections);

		label = ClientSWTElements.createClientLabel(parent, labelStyle, ClientMessages.ANGULAR_STEP, Optional.empty());
		ClientSWTElements.createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).applyTo(label);

		angularStep = ClientSWTElements.createClientLabel(parent, labelStyle, ClientMessages.EMPTY_MESSAGE, Optional.empty());
		ClientSWTElements.createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).span(2, 1).applyTo(angularStep);
	}

	private void imagesCalibrationContent(Composite parent, int labelStyle, int textStyle) {
		Label label = ClientSWTElements.createClientLabel(parent, labelStyle, ClientMessages.NUM_DARK, Optional.empty());
		ClientSWTElements.createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).indent(5, SWT.DEFAULT).applyTo(label);

		numberDark = ClientSWTElements.createClientText(parent, textStyle, ClientMessages.NUM_DARK_TOOLTIP, Optional.of(verifyOnlyIntegerText));
		ClientSWTElements.createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(ClientSWTElements.DEFAULT_TEXT_SIZE).applyTo(numberDark);

		ClientSWTElements.createClientEmptyCell(parent, new Point(50, 10));

		label = ClientSWTElements.createClientLabel(parent, labelStyle, ClientMessages.DARK_EXPOSURE, Optional.empty());
		ClientSWTElements.createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).indent(20, SWT.DEFAULT).applyTo(label);

		darkExposure = ClientSWTElements.createClientText(parent, textStyle, ClientMessages.DARK_EXPOSURE_TP, Optional.of(verifyOnlyIntegerText));
		ClientSWTElements.createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(ClientSWTElements.DEFAULT_TEXT_SIZE).span(2, 1)
				.applyTo(darkExposure);

		label = ClientSWTElements.createClientLabel(parent, labelStyle, ClientMessages.NUM_FLAT, Optional.empty());
		ClientSWTElements.createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).indent(5, SWT.DEFAULT).applyTo(label);

		numberFlat = ClientSWTElements.createClientText(parent, textStyle, ClientMessages.NUM_FLAT_TOOLTIP, Optional.of(verifyOnlyIntegerText));
		ClientSWTElements.createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(ClientSWTElements.DEFAULT_TEXT_SIZE).span(2, 1)
				.applyTo(numberFlat);

		label = ClientSWTElements.createClientLabel(parent, labelStyle, ClientMessages.FLAT_EXPOSURE, Optional.empty());
		ClientSWTElements.createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).indent(20, SWT.DEFAULT).applyTo(label);

		flatExposure = ClientSWTElements.createClientText(parent, textStyle, ClientMessages.FLAT_EXPOSURE_TP, Optional.of(verifyOnlyIntegerText));
		ClientSWTElements.createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(ClientSWTElements.DEFAULT_TEXT_SIZE).span(2, 1)
				.applyTo(flatExposure);

		beforeAcquisition = ClientSWTElements.createClientButton(parent, SWT.CHECK, ClientMessages.AT_START, ClientMessages.AT_START_TOOLTIP, Optional.empty());
		ClientSWTElements.createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).span(2, 1).applyTo(beforeAcquisition);

		afterAcquisition = ClientSWTElements.createClientButton(parent, SWT.CHECK, ClientMessages.AT_END, ClientMessages.AT_END_TOOLTIP, Optional.empty());
		ClientSWTElements.createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).span(2, 1).applyTo(afterAcquisition);
	}

	private void multipleScansContentAlternative(Composite parent, int labelStyle, int textStyle) {
		final Button multipleScansAlternative = ClientSWTElements.createClientButton(parent, SWT.PUSH, ClientMessages.MULTIPLE_SCANS,
				ClientMessages.MULTIPLE_SCANS_TOOLTIP, Optional.empty());
		ClientSWTElements.createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).span(3, 1).applyTo(multipleScansAlternative);

		multipleScansAlternative.addSelectionListener(new SelectionListener() {

			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.widget.equals(multipleScansAlternative)) {
					new MultipleScanDialog(parent.getShell(), () -> getConfigurationData()).open();
					updateMultipleScan();
					if (getConfigurationData().getMultipleScans().getNumberRepetitions() > 1 || getConfigurationData().getMultipleScans().getWaitingTime() > 0
							|| !MultipleScansType.REPEAT_SCAN.equals(getConfigurationData().getMultipleScans().getMultipleScansType())) {
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
		Composite container = ClientSWTElements.createClientCompositeWithGridLayout(customBar, SWT.NONE, 5);
		ClientSWTElements.createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).grab(true, true).applyTo(container);
		// container.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_RED));

		Label label = ClientSWTElements.createClientLabel(container, SWT.NONE, ClientMessages.NUM_REPETITIONS, Optional.empty());
		ClientSWTElements.createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).applyTo(label);

		numberRepetitions = ClientSWTElements.createClientText(container, SWT.NONE, ClientMessages.NUM_REPETITIONS_TOOLTIP, Optional.of(verifyOnlyIntegerText));
		ClientSWTElements.createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(ClientSWTElements.DEFAULT_TEXT_SIZE).applyTo(numberRepetitions);

		ClientSWTElements.createClientEmptyCell(container, new Point(10, 10));

		label = ClientSWTElements.createClientLabel(container, SWT.NONE, ClientMessages.WAITING_TIME, Optional.empty());
		ClientSWTElements.createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).applyTo(label);

		waitingTime = ClientSWTElements.createClientText(container, SWT.NONE, ClientMessages.WAITING_TIME_TOOLTIP, Optional.of(verifyOnlyIntegerText));
		ClientSWTElements.createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(ClientSWTElements.DEFAULT_TEXT_SIZE).applyTo(waitingTime);

		repeateMultipleScansType = ClientSWTElements.createClientButton(container, SWT.RADIO, ClientMessages.REPEATE_SCAN, ClientMessages.REPEATE_SCAN_TOOLTIP,
				Optional.empty());
		repeateMultipleScansType.setData(MultipleScansType.REPEAT_SCAN);
		ClientSWTElements.createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).applyTo(repeateMultipleScansType);

		switchbackMultipleScansType = ClientSWTElements.createClientButton(container, SWT.RADIO, ClientMessages.SWITCHBACK_SCAN,
				ClientMessages.SWITCHBACK_SCAN_TOOLTIP, Optional.empty());
		switchbackMultipleScansType.setData(MultipleScansType.SWITCHBACK_SCAN);
		ClientSWTElements.createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).applyTo(switchbackMultipleScansType);

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
		if (!event.getSource().equals(customAngle))
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
				customAngle.setText(Double.toString(getScannableTrackDocument().getStop()));
			}
			// configureWhenShown();
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent event) {
			// do nothing
		}
	};

	private void updateCurrentAngularPosition() {
		double currentMotorPostion = stageController.getMotorPosition(StageDevice.MOTOR_STAGE_ROT_Y);
		startAngleText.setText(Double.toString(currentMotorPostion));
	}

	private SelectionListener scanTypeListener = new SelectionListener() {
		@Override
		public void widgetSelected(SelectionEvent event) {
			if (!Button.class.isInstance(event.getSource()))
				return;

			if (!((Button) event.getSource()).getSelection())
				return;

			if (event.getSource().equals(flyScanType)) {
				dataHelper.addMutators(Mutator.CONTINUOUS, new ArrayList<>());
			} else if (event.getSource().equals(stepScanType)) {
				dataHelper.removeMutators(Mutator.CONTINUOUS);
			}
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent event) {
			// do nothing
		}
	};

	private void bindElements() {
		bindScanType(dbc);
		bindMultipleScanType(dbc);
		ClientBindingElements.bindText(dbc, name, String.class, "name", getController().getAcquisition());

		flyScanType.addSelectionListener(scanTypeListener);
		stepScanType.addSelectionListener(scanTypeListener);

		startAngleText.addModifyListener(this::startAngleTextListener);
		customAngle.addModifyListener(this::customAngleTextListener);
		currentAngleButton.addSelectionListener(predefinedAngleListener);
		halfRotationRangeType.addSelectionListener(predefinedAngleListener);
		fullRotationRangeType.addSelectionListener(predefinedAngleListener);
		customRotationRangeType.addSelectionListener(predefinedAngleListener);
		totalProjections.addModifyListener(this::totalProjectionsListener);

		ClientBindingElements.bindCheckBox(dbc, beforeAcquisition, "imageCalibration.beforeAcquisition", getConfigurationData());
		ClientBindingElements.bindCheckBox(dbc, afterAcquisition, "imageCalibration.afterAcquisition", getConfigurationData());
		ClientBindingElements.bindText(dbc, numberDark, Integer.class, "imageCalibration.numberDark", getConfigurationData());
		ClientBindingElements.bindText(dbc, numberFlat, Integer.class, "imageCalibration.numberFlat", getConfigurationData());
		ClientBindingElements.bindText(dbc, darkExposure, Integer.class, "imageCalibration.darkExposure", getConfigurationData());
		ClientBindingElements.bindText(dbc, flatExposure, Integer.class, "imageCalibration.flatExposure", getConfigurationData());

		// ClientBindingElements.bindCheckBox(dbc, multipleScans, "multipleScans.enabled", getConfigurationData());
		// multipleScans.setSelection(multipleScans.getSelection());
		ClientBindingElements.bindText(dbc, numberRepetitions, Integer.class, "multipleScans.numberRepetitions", getConfigurationData());
		ClientBindingElements.bindText(dbc, waitingTime, Integer.class, "multipleScans.waitingTime", getConfigurationData());
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
		ClientBindingElements.bindEnumToRadio(dbc, MultipleScansType.class, "multipleScans.multipleScansType", getConfigurationData(), enumRadioMap);
	}

	private void initialiseElements() {
		stepScanType.setSelection(false);
		flyScanType.setSelection(true);

		endGroupsListeners();
		customAngleLooseFocus();
		startAngleText.setText(Double.toString(getScannableTrackDocument().getStart()));
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
		}
		// configureWhenShown();
		totalProjections.setText(Integer.toString(getScannableTrackDocument().getPoints()));
		forceFocusOnEmpty(numberDark, Integer.toString(getConfigurationData().getImageCalibration().getNumberDark()));
		forceFocusOnEmpty(numberFlat, Integer.toString(getConfigurationData().getImageCalibration().getNumberFlat()));
		updateMultipleScan();
	}

	private void updateMultipleScan() {
		Arrays.asList(switchbackMultipleScansType, repeateMultipleScansType).stream()
				.filter(i -> getConfigurationData().getMultipleScans().getMultipleScansType().equals(i.getData())).findFirst()
				.ifPresent(b -> b.setSelection(true));
		Arrays.asList(switchbackMultipleScansType, repeateMultipleScansType).stream()
				.filter(i -> !getConfigurationData().getMultipleScans().getMultipleScansType().equals(i.getData())).findFirst()
				.ifPresent(b -> b.setSelection(false));
		numberRepetitions.setText(Integer.toString(getConfigurationData().getMultipleScans().getNumberRepetitions()));
		waitingTime.setText(Integer.toString(getConfigurationData().getMultipleScans().getWaitingTime()));
	}

	private void endGroupsListeners() {
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

	private void customAngleLooseFocus() {
		FocusListener customAngleListener = FocusListener.focusLostAdapter(c -> updateCurrentAngularPosition());
		customAngle.addFocusListener(customAngleListener);
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
		ClientSWTElements.createClientGridDataFactory().grab(true, true).applyTo(bar);
		return bar;
	}

	private ScanningConfiguration getConfigurationData() {
		return getController().getAcquisition().getAcquisitionConfiguration();
	}

	private ScanningParameters getTemplateData() {
		return getController().getAcquisition().getAcquisitionConfiguration().getAcquisitionParameters();
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
			bindElements();
			initialiseElements();
			composite.layout(true, true);
		}
	}
}