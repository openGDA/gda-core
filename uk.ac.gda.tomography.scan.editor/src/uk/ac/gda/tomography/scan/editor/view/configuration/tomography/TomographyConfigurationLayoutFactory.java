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

package uk.ac.gda.tomography.scan.editor.view.configuration.tomography;

import static uk.ac.gda.core.tool.spring.SpringApplicationContextFacade.getBean;
import static uk.ac.gda.ui.tool.ClientMessages.ANGULAR_STEP;
import static uk.ac.gda.ui.tool.ClientMessages.AT_END;
import static uk.ac.gda.ui.tool.ClientMessages.AT_END_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessages.AT_START;
import static uk.ac.gda.ui.tool.ClientMessages.AT_START_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessages.CONFIGURATION_LAYOUT_ERROR;
import static uk.ac.gda.ui.tool.ClientMessages.CURRENT_ANGLE;
import static uk.ac.gda.ui.tool.ClientMessages.CURRENT_ANGLE_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessages.CUSTOM_END_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessages.DARK_EXPOSURE_TP;
import static uk.ac.gda.ui.tool.ClientMessages.EMPTY_MESSAGE;
import static uk.ac.gda.ui.tool.ClientMessages.EXPOSURE;
import static uk.ac.gda.ui.tool.ClientMessages.FINAL_ANGLE;
import static uk.ac.gda.ui.tool.ClientMessages.FLAT_EXPOSURE_TP;
import static uk.ac.gda.ui.tool.ClientMessages.FLY_SCAN;
import static uk.ac.gda.ui.tool.ClientMessages.FLY_SCAN_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessages.FULL_ANGLE;
import static uk.ac.gda.ui.tool.ClientMessages.FULL_ANGLE_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessages.IMAGE_CALIBRATION;
import static uk.ac.gda.ui.tool.ClientMessages.MULTIPLE_SCANS;
import static uk.ac.gda.ui.tool.ClientMessages.NAME;
import static uk.ac.gda.ui.tool.ClientMessages.NAME_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessages.NUM_DARK;
import static uk.ac.gda.ui.tool.ClientMessages.NUM_DARK_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessages.NUM_FLAT;
import static uk.ac.gda.ui.tool.ClientMessages.NUM_FLAT_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessages.NUM_REPETITIONS;
import static uk.ac.gda.ui.tool.ClientMessages.NUM_REPETITIONS_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessages.PROCESS_REQUESTS;
import static uk.ac.gda.ui.tool.ClientMessages.PROJECTIONS;
import static uk.ac.gda.ui.tool.ClientMessages.PROJECTIONS_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessages.RANGE;
import static uk.ac.gda.ui.tool.ClientMessages.REPEATE_SCAN;
import static uk.ac.gda.ui.tool.ClientMessages.REPEATE_SCAN_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessages.START;
import static uk.ac.gda.ui.tool.ClientMessages.START_ANGLE_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessages.STEP_SCAN;
import static uk.ac.gda.ui.tool.ClientMessages.STEP_SCAN_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessages.STRAIGHT_ANGLE;
import static uk.ac.gda.ui.tool.ClientMessages.STRAIGHT_ANGLE_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessages.SWITCHBACK_SCAN;
import static uk.ac.gda.ui.tool.ClientMessages.SWITCHBACK_SCAN_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessages.WAITING_TIME;
import static uk.ac.gda.ui.tool.ClientMessages.WAITING_TIME_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientCompositeWithGridLayout;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGroup;
import static uk.ac.gda.ui.tool.ClientSWTElements.standardMarginHeight;
import static uk.ac.gda.ui.tool.ClientSWTElements.standardMarginWidth;
import static uk.ac.gda.ui.tool.GUIComponents.checkComponent;
import static uk.ac.gda.ui.tool.GUIComponents.doubleContent;
import static uk.ac.gda.ui.tool.GUIComponents.doublePositiveContent;
import static uk.ac.gda.ui.tool.GUIComponents.integerPositiveContent;
import static uk.ac.gda.ui.tool.GUIComponents.labelComponent;
import static uk.ac.gda.ui.tool.GUIComponents.labelledLabelContent;
import static uk.ac.gda.ui.tool.GUIComponents.radioComponent;
import static uk.ac.gda.ui.tool.GUIComponents.textContent;
import static uk.ac.gda.ui.tool.WidgetUtilities.addWidgetDisposableListener;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.commons.lang3.math.NumberUtils;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.ModifyListener;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.eclipse.swt.widgets.Widget;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.mscan.element.Mutator;
import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.mapping.api.document.helper.ImageCalibrationHelper;
import uk.ac.diamond.daq.mapping.api.document.helper.MultipleScansHelper;
import uk.ac.diamond.daq.mapping.api.document.helper.ScanpathDocumentHelper;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningConfiguration;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScanpathDocument;
import uk.ac.diamond.daq.mapping.ui.controller.StageController;
import uk.ac.diamond.daq.mapping.ui.stage.enumeration.StageDevice;
import uk.ac.gda.api.acquisition.configuration.MultipleScansType;
import uk.ac.gda.api.acquisition.configuration.calibration.DarkCalibrationDocument;
import uk.ac.gda.api.acquisition.configuration.calibration.FlatCalibrationDocument;
import uk.ac.gda.api.acquisition.parameters.DetectorDocument;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.exception.AcquisitionConfigurationException;
import uk.ac.gda.client.properties.acquisition.AcquisitionConfigurationProperties;
import uk.ac.gda.client.properties.acquisition.AcquisitionPropertyType;
import uk.ac.gda.client.properties.acquisition.ProcessingRequestProperties;
import uk.ac.gda.core.tool.spring.AcquisitionFileContext;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.core.tool.spring.TomographyContextFile;
import uk.ac.gda.tomography.scan.editor.view.ScanType;
import uk.ac.gda.ui.tool.ClientBindingElements;
import uk.ac.gda.ui.tool.Reloadable;
import uk.ac.gda.ui.tool.document.ScanningAcquisitionTemporaryHelper;
import uk.ac.gda.ui.tool.processing.ProcessingRequestComposite;
import uk.ac.gda.ui.tool.processing.context.ProcessingRequestContext;
import uk.ac.gda.ui.tool.processing.keys.ProcessingRequestKeyFactory;
import uk.ac.gda.ui.tool.processing.keys.ProcessingRequestKeyFactory.ProcessKey;
import uk.ac.gda.ui.tool.spring.ClientSpringProperties;

/**
 * This Composite allows to edit a {@link ScanningParameters} object.
 *
 * @author Maurizio Nagni
 */
public class TomographyConfigurationLayoutFactory implements CompositeFactory, Reloadable {

	private static final Logger logger = LoggerFactory.getLogger(TomographyConfigurationLayoutFactory.class);

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
	private Button fullRotationRangeType;
	private Button customRotationRangeType;
	private Text customAngle;
	private Label finalAngle;

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
	private Text numberRepetitions;
	private Text waitingTime;
	private Button repeateMultipleScansType;
	private Button switchbackMultipleScansType;

	private ProcessingRequestComposite processingRequest;

	private ScanpathDocumentHelper dataHelper;
	private MultipleScansHelper configurationHelper;
	private ImageCalibrationHelper imageCalibrationHelper;

	private DataBindingContext dbc = new DataBindingContext();

	private Composite mainComposite;

	public TomographyConfigurationLayoutFactory() {
		try {
			this.dataHelper = new ScanpathDocumentHelper(this::getScanningParameters);
			this.configurationHelper = new MultipleScansHelper(this::getScanningConfiguration);
			this.imageCalibrationHelper = new ImageCalibrationHelper(this::getScanningConfiguration);
		} catch (NoSuchElementException e) {
			UIHelper.showWarning(CONFIGURATION_LAYOUT_ERROR, e);
		}
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		logger.debug("Creating {}", this);
		mainComposite = createClientCompositeWithGridLayout(parent, SWT.NONE, 1);
		createClientGridDataFactory().align(SWT.FILL, SWT.BEGINNING).grab(true, true).applyTo(mainComposite);
		standardMarginHeight(mainComposite.getLayout());
		standardMarginWidth(mainComposite.getLayout());

		try {
			createElements(mainComposite, SWT.NONE, SWT.BORDER);
			bindElements();
			initialiseElements();
			addWidgetsListener();
			logger.debug("Created {}", this);
		} catch (NoSuchElementException e) {
			UIHelper.showWarning(CONFIGURATION_LAYOUT_ERROR, e);
		}
		return mainComposite;
	}

	@Override
	public void reload() {
		try {
			bindElements();
			initialiseElements();
			mainComposite.getShell().layout(true, true);
		} catch (NoSuchElementException e) {
			UIHelper.showWarning(CONFIGURATION_LAYOUT_ERROR, e);
		}
	}

	/**
	 * @param parent
	 *            a three column composite
	 * @param labelStyle
	 * @param textStyle
	 */
	private void createElements(Composite parent, int labelStyle, int textStyle) {
		this.name = textContent(parent, labelStyle, textStyle,
				NAME, NAME_TOOLTIP);
		// guarantee that fills the horizontal space
		createClientGridDataFactory().grab(true, true).applyTo(name);

		scanTypeContent(parent);
		createRangeGroup(parent, labelStyle, textStyle);
		createProjectionGroup(parent, labelStyle, textStyle);
		createImagesCalibrationGroup(parent, labelStyle, textStyle);
		createProcessRequestGroup(parent, SWT.NONE);
		multipleScansContent(parent, labelStyle, textStyle);
	}

	private void scanTypeContent(Composite parent) {
		var mainCompositeContent = createClientCompositeWithGridLayout(parent, SWT.NONE, 2);
		createClientGridDataFactory().grab(true, true).applyTo(mainCompositeContent);

		this.flyScanType = radioComponent(mainCompositeContent,
				FLY_SCAN, FLY_SCAN_TOOLTIP);
		this.flyScanType.setData(ScanType.FLY);

		this.stepScanType = radioComponent(mainCompositeContent,
				STEP_SCAN, STEP_SCAN_TOOLTIP);
		this.stepScanType.setData(ScanType.STEP);
	}

	private void createRangeGroup(Composite parent, int labelStyle, int textStyle) {
		var group = createClientGroup(parent, SWT.NONE, 2, RANGE);
		createClientGridDataFactory().applyTo(group);

		var leftCompositeContent = createClientCompositeWithGridLayout(group, SWT.NONE, 1);
		createClientGridDataFactory().grab(true, true).applyTo(leftCompositeContent);

		var rightCompositeContent = createClientCompositeWithGridLayout(group, SWT.NONE, 1);
		createClientGridDataFactory().grab(true, true).applyTo(rightCompositeContent);

		startAngleContent(leftCompositeContent, labelStyle, textStyle);
		endAngleContent(leftCompositeContent, labelStyle, textStyle);

		this.finalAngle = labelledLabelContent(rightCompositeContent, labelStyle,
				FINAL_ANGLE, EMPTY_MESSAGE);
		this.angularStep = labelledLabelContent(rightCompositeContent, labelStyle,
				ANGULAR_STEP, EMPTY_MESSAGE);
	}

	private void createProjectionGroup(Composite parent, int labelStyle, int textStyle) {
		var group = createClientGroup(parent, SWT.NONE, 1, PROJECTIONS);
		createClientGridDataFactory().applyTo(group);
		this.totalProjections = integerPositiveContent(group, labelStyle, textStyle,
				PROJECTIONS, PROJECTIONS_TOOLTIP);
	}

	/**
	 * @param parent
	 *            a three columns composite
	 * @param labelStyle
	 * @param textStyle
	 */
	private void startAngleContent(Composite parent, int labelStyle, int textStyle) {
		var mainCompositeContent = createClientCompositeWithGridLayout(parent, SWT.NONE, 2);
		createClientGridDataFactory().grab(true, true).applyTo(mainCompositeContent);

		this.startAngleText = doubleContent(mainCompositeContent, labelStyle, textStyle,
				START, START_ANGLE_TOOLTIP);
		this.currentAngleButton = checkComponent(mainCompositeContent,
				CURRENT_ANGLE, CURRENT_ANGLE_TOOLTIP);
	}

	private void endAngleContent(Composite parent, int labelStyle, int textStyle) {
		var mainCompositeContent = createClientCompositeWithGridLayout(parent, SWT.NONE, 5);
		createClientGridDataFactory().grab(true, true).applyTo(mainCompositeContent);

		labelComponent(mainCompositeContent, labelStyle, RANGE);
		this.halfRotationRangeType = radioComponent(mainCompositeContent,
				STRAIGHT_ANGLE, STRAIGHT_ANGLE_TOOLTIP);
		this.fullRotationRangeType = radioComponent(mainCompositeContent,
				FULL_ANGLE, FULL_ANGLE_TOOLTIP);
		this.customRotationRangeType = radioComponent(mainCompositeContent,
				EMPTY_MESSAGE, CUSTOM_END_TOOLTIP);
		this.customAngle = doubleContent(mainCompositeContent, labelStyle, textStyle,
				EMPTY_MESSAGE, CUSTOM_END_TOOLTIP);
	}

	private void createImagesCalibrationGroup(Composite parent, int labelStyle, int textStyle) {
		var group = createClientGroup(parent, SWT.NONE, 2, IMAGE_CALIBRATION);
		createClientGridDataFactory().applyTo(group);

		this.numberDark = integerPositiveContent(group, labelStyle, textStyle,
				NUM_DARK, NUM_DARK_TOOLTIP);
		this.darkExposure = doublePositiveContent(group, labelStyle, textStyle,
				EXPOSURE, DARK_EXPOSURE_TP);

		this.numberFlat = integerPositiveContent(group, labelStyle, textStyle,
				NUM_FLAT, NUM_FLAT_TOOLTIP);
		this.flatExposure = doublePositiveContent(group, labelStyle, textStyle,
				EXPOSURE, FLAT_EXPOSURE_TP);

		this.beforeAcquisition = checkComponent(group,
				AT_START, AT_START_TOOLTIP);
		this.afterAcquisition = checkComponent(group,
				AT_END, AT_END_TOOLTIP);
	}

	private void multipleScansContent(Composite parent, int labelStyle, int textStyle) {
		var group = createClientGroup(parent, SWT.NONE, 2, MULTIPLE_SCANS);
		createClientGridDataFactory().applyTo(group);

		this.numberRepetitions = integerPositiveContent(group, labelStyle, textStyle,
				NUM_REPETITIONS, NUM_REPETITIONS_TOOLTIP);
		this.waitingTime = doublePositiveContent(group, labelStyle, textStyle,
				WAITING_TIME, WAITING_TIME_TOOLTIP);

		this.repeateMultipleScansType = radioComponent(group,
				REPEATE_SCAN, REPEATE_SCAN_TOOLTIP);
		this.repeateMultipleScansType.setData(MultipleScansType.REPEAT_SCAN);

		this.switchbackMultipleScansType = radioComponent(group,
				SWITCHBACK_SCAN, SWITCHBACK_SCAN_TOOLTIP);
		this.switchbackMultipleScansType.setData(MultipleScansType.SWITCHBACK_SCAN);
	}

	private List<ProcessingRequestContext<?>> getProcessingRequestContext() {
		// The selectable process elements
		List<ProcessingRequestContext<?>> processingRequestContexts = new ArrayList<>();
		try {
			// makes available for selection a ApplyNexusTemplatesRequest element			;
			processingRequestContexts.add(new ProcessingRequestContext(getProcessingRequestKeyFactory().getProcessingKey(ProcessKey.NEXUS_TEMPLATE),
					getNexusTemplatesProcessingDirectory(), getDefaultNexusTemplatesProcessingFile(), true));
		} catch (AcquisitionConfigurationException e) {
			logger.error("TODO put description of error here", e);
		}

		// makes available for selection a SavuProcessingRequest element
		processingRequestContexts.add(new ProcessingRequestContext(getProcessingRequestKeyFactory().getProcessingKey(ProcessKey.SAVU),
				getSavuProcessingFileDirectory(), getSavuDefaultProcessingFile(), false));
		return processingRequestContexts;
	}

	private void createProcessRequestGroup(Composite parent, int labelStyle) {
		var group = createClientGroup(parent, SWT.NONE, 3, PROCESS_REQUESTS);
		createClientGridDataFactory().applyTo(group);
		processingRequest = new ProcessingRequestComposite(getProcessingRequestContext());
		processingRequest.createComposite(group, labelStyle);
	}

	private void startAngleTextListener(ModifyEvent event) {
		if (!event.getSource().equals(startAngleText))
			return;
		updateStartStop();
	}

	private void customAngleTextListener(ModifyEvent event) {
		if (!event.getSource().equals(customAngle) || !customRotationRangeType.getSelection())
			return;
		updateStartStop();
	}

	private void updateStartStop() {
		double startAngle = parseAngle(startAngleText.getText());
		dataHelper.updateStartAngle(startAngle);
		double stopAngle = parseAngle(customAngle.getText());
		dataHelper.updateStopAngle(startAngle + stopAngle);
		finalAngle.setText(Double.toString(startAngle + stopAngle));
		finalAngle.getParent().layout(true,  true);
		updateAngularStep();
	}

	private double parseAngle(String angle) {
		if (NumberUtils.isNumber(angle)) {
			return NumberUtils.toDouble(angle);
		}
		return 0.0;
	}

	private void totalProjectionsListener(ModifyEvent event) {
		if (!event.getSource().equals(totalProjections))
			return;
		int points = Optional.ofNullable(totalProjections.getText())
				.filter(s -> !s.isEmpty())
				.map(Integer::parseInt)
				.orElseGet(() -> 1);
		dataHelper.updatePoints(points);
		updateAngularStep();
	}

	private SelectionListener predefinedAngleListener = new SelectionListener() {
		@Override
		public void widgetSelected(SelectionEvent event) {
			if (!(event.getSource() instanceof Button))
				return;
			if (!((Button) event.getSource()).getSelection())
				return;
			if (event.getSource().equals(currentAngleButton)) {
				updateCurrentAngularPosition();
			} else if (event.getSource().equals(fullRotationRangeType)) {
				customAngle.setText(Double.toString(360.0));
			} else if (event.getSource().equals(halfRotationRangeType)) {
				customAngle.setText(Double.toString(180.0));
			} else if (event.getSource().equals(customRotationRangeType)) {
				customAngle.setEnabled(true);
			}
			updateStartStop();
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent event) {
			// do nothing
		}

		private void updateCurrentAngularPosition() {
			double currentMotorPostion = getStageController().getMotorPosition(StageDevice.MOTOR_STAGE_ROT_Y);
			startAngleText.setText(Double.toString(currentMotorPostion));
		}

		private StageController getStageController() {
			return getBean(StageController.class);
		}
	};

	private SelectionListener scanTypeListener = new SelectionListener() {
		@Override
		public void widgetSelected(SelectionEvent event) {
			if (!(event.getSource() instanceof Button))
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
		int repetitions = Optional.ofNullable(numberRepetitions.getText())
				.filter(s -> !s.isEmpty())
				.map(Integer::parseInt)
				.orElseGet(() -> 1);
		configurationHelper.updateMultipleScanRepetitions(repetitions);
	}

	private void waitingTimeListener(ModifyEvent event) {
		if (!event.getSource().equals(waitingTime))
			return;
		int wTime = Optional.ofNullable(waitingTime.getText())
				.filter(s -> !s.isEmpty())
				.map(Integer::parseInt)
				.orElseGet(() -> 1);
		configurationHelper.updateMultipleScanWaitingTime(wTime);
	}

	private void beforeAcquisitionListener(SelectionEvent event) {
		if (!event.getSource().equals(beforeAcquisition))
			return;
		imageCalibrationHelper.updateDarkBeforeAcquisitionExposures(beforeAcquisition.getSelection());
		imageCalibrationHelper.updateFlatBeforeAcquisitionExposures(beforeAcquisition.getSelection());
	}

	private void afterAcquisitionListener(SelectionEvent event) {
		if (!event.getSource().equals(afterAcquisition))
			return;
		imageCalibrationHelper.updateDarkAfterAcquisitionExposures(afterAcquisition.getSelection());
		imageCalibrationHelper.updateFlatAfterAcquisitionExposures(afterAcquisition.getSelection());
	}

	private void setNumberFlat(Widget widget) {
		Optional.ofNullable(widget)
			.map(Text.class::cast)
			.map(Text::getText)
			.map(Integer::parseInt)
			.ifPresent(imageCalibrationHelper::updateFlatNumberExposures);
	}

	private void setNumberDark(Widget widget) {
		Optional.ofNullable(widget)
		.map(Text.class::cast)
		.map(Text::getText)
		.map(Integer::parseInt)
		.ifPresent(imageCalibrationHelper::updateDarkNumberExposures);
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

		// Range fields
		startAngleText.addModifyListener(this::startAngleTextListener);
		customAngle.addModifyListener(this::customAngleTextListener);
		customAngle.addFocusListener(FocusListener.focusLostAdapter(c -> updateAngularStep()));
		currentAngleButton.addSelectionListener(predefinedAngleListener);
		endGroupsListeners();

		// Calibration fields
		addWidgetDisposableListener(beforeAcquisition, SelectionListener.widgetSelectedAdapter(this::beforeAcquisitionListener));
		addWidgetDisposableListener(afterAcquisition, SelectionListener.widgetSelectedAdapter(this::afterAcquisitionListener));
		addWidgetDisposableListener(numberDark, SWT.Modify, event -> setNumberDark(event.widget));
		addWidgetDisposableListener(numberFlat, SWT.Modify, event -> setNumberFlat(event.widget));

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
	}

	private final ModifyListener modifyNameListener = event -> updateAcquisitionName();

	private void updateAcquisitionName() {
		getScanningAcquisitionTemporaryHelper()
			.getScanningAcquisition()
			.ifPresent(a -> a.setName(name.getText()));
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
		ClientBindingElements.bindEnumToRadio(dbc, MultipleScansType.class, "multipleScans.multipleScansType",
				getAcquisitionConfiguration(), enumRadioMap);
	}

	private void initialiseElements() {
		getScanningAcquisitionTemporaryHelper()
			.getScanningAcquisition()
			.map(ScanningAcquisition::getName)
			.ifPresent(name::setText);
		initializeScanType();
		initializeStartAngle();
		initializeEndAngle();
		initializeProjections();
		updateStartStop();

		initializeImageCalibration();
		updateMultipleScan(getAcquisitionConfiguration());
		processingRequest.reload();
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

	private void initializeProjections() {
		totalProjections.setText(Integer.toString(getScannableTrackDocument().getPoints()));
	}

	private void initializeEndAngle() {
		halfRotationRangeType.setSelection(false);
		fullRotationRangeType.setSelection(false);
		customAngle.setEnabled(false);
		customRotationRangeType.setSelection(false);
		customAngle.setText("");

		var customAngleString = "";
		if (getScannableTrackDocument().getStop() == 180.0) {
			halfRotationRangeType.setSelection(true);
			customAngleString = "180.0";
		} else if (getScannableTrackDocument().getStop() == 360.0) {
			fullRotationRangeType.setSelection(true);
			customAngleString = "360.0";
		} else if (!Double.isNaN(getScannableTrackDocument().getStop())) {
			customRotationRangeType.setSelection(true);
			var event = new Event();
			event.widget = customRotationRangeType;
			customAngleString = Double.toString(getScannableTrackDocument().getStop());
			Arrays.stream(customRotationRangeType.getListeners(SWT.SELECTED))
				.forEach(l -> l.handleEvent(event));
			customAngle.setEnabled(true);
		}
		customAngle.setText(customAngleString);
	}

	private void initializeImageCalibration() {
		ScanningConfiguration configuration = getAcquisitionConfiguration();

		var ic = configuration.getImageCalibration();
		Optional.ofNullable(ic.getDarkCalibration())
			.ifPresent(this::initializeDarkCalibration);

		Optional.ofNullable(ic.getFlatCalibration())
			.ifPresent(this::initializeFlatCalibration);
	}

	private void initializeDarkCalibration(DarkCalibrationDocument darkCalibrationDocument) {
		int exposures = Optional.ofNullable(darkCalibrationDocument.getNumberExposures())
				.orElse(0);
		numberDark.setText(Integer.toString(exposures));

		double exposure = Optional.ofNullable(darkCalibrationDocument.getDetectorDocument())
				.map(DetectorDocument::getExposure)
				.orElse(0d);
		darkExposure.setText(Double.toString(exposure));


		// For the moment dark and flat have the same boolean values
		boolean selected = Optional.ofNullable(darkCalibrationDocument.isBeforeAcquisition())
				.orElse(false);
		beforeAcquisition.setSelection(selected);

		// For the moment dark and flat have the same boolean values
		selected = Optional.ofNullable(darkCalibrationDocument.isAfterAcquisition())
				.orElse(false);
		afterAcquisition.setSelection(selected);

		forceFocusOnEmpty(numberDark, Integer.toString(exposures));
	}




	private void initializeFlatCalibration(FlatCalibrationDocument flatCalibrationDocument) {
		int exposures = Optional.ofNullable(flatCalibrationDocument.getNumberExposures())
				.orElse(0);
		numberFlat.setText(Integer.toString(exposures));

		double exposure = Optional.ofNullable(flatCalibrationDocument.getDetectorDocument())
			.map(DetectorDocument::getExposure)
			.orElse(0d);
		flatExposure.setText(Double.toString(exposure));

		forceFocusOnEmpty(numberFlat, Integer.toString(exposures));
	}


	private void updateMultipleScan(ScanningConfiguration configuration) {
		Arrays.asList(switchbackMultipleScansType, repeateMultipleScansType).stream()
				.filter(i -> configuration.getMultipleScans().getMultipleScansType()
						.equals(i.getData()))
				.findFirst()
				.ifPresent(b -> b.setSelection(true));
		Arrays.asList(switchbackMultipleScansType, repeateMultipleScansType).stream()
				.filter(i -> !configuration.getMultipleScans().getMultipleScansType()
						.equals(i.getData()))
				.findFirst()
				.ifPresent(b -> b.setSelection(false));
		numberRepetitions.setText(Integer.toString(configuration.getMultipleScans().getNumberRepetitions()));
		waitingTime.setText(Integer.toString(configuration.getMultipleScans().getWaitingTime()));
	}

	private void endGroupsListeners() {
		halfRotationRangeType.addSelectionListener(predefinedAngleListener);
		fullRotationRangeType.addSelectionListener(predefinedAngleListener);
		customRotationRangeType.addSelectionListener(predefinedAngleListener);
		SelectionListener activateGroupListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button source = Button.class.cast(e.getSource());
				customAngle.setText(Double.toString(getScannableTrackDocument().getStop()));
				if (!source.getSelection())
					return;
				double tempCustomAngle = getScannableTrackDocument().length() - getScannableTrackDocument().getStart();
				if (source.equals(halfRotationRangeType)) {
					tempCustomAngle = 180.0;
					customAngle.setEnabled(false);
				} else if (source.equals(fullRotationRangeType)) {
					tempCustomAngle = 360.0;
					customAngle.setEnabled(false);
				}
				customAngle.setText(Double.toString(tempCustomAngle));
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
		dataHelper.updateStep(newAngularStep);
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

	private Optional<ScanningParameters> getAcquisitionParameters() {
		return getScanningAcquisitionTemporaryHelper().getScanningParameters();
	}

	private ScanningConfiguration getAcquisitionConfiguration() {
		return getScanningAcquisitionTemporaryHelper()
			.getAcquisitionConfiguration()
			.orElseThrow();
	}

	private ScannableTrackDocument getScannableTrackDocument() {
		List<ScannableTrackDocument> tracks = getScanningAcquisitionTemporaryHelper()
				.getScanpathDocument()
				.map(ScanpathDocument::getScannableTrackDocuments)
				.orElseGet(ArrayList::new);

		if (!tracks.isEmpty()) {
			return tracks.get(0);
		}
		throw new NoSuchElementException("No track document available");
	}

	private AcquisitionFileContext getClientContext() {
		return SpringApplicationContextFacade.getBean(AcquisitionFileContext.class);
	}

	private URL getSavuProcessingFileDirectory() {
		return getClientContext().getTomographyContext().getContextFile(TomographyContextFile.TOMOGRAPHY_SAVU_DIRECTORY);
	}

	private List<URL> getSavuDefaultProcessingFile() {
		List<URL> urls = new ArrayList<>();
		urls.add(getClientContext().getTomographyContext().getContextFile(TomographyContextFile.TOMOGRAPHY_DEFAULT_PROCESSING_FILE));
		return urls;
	}

	private URL getNexusTemplatesProcessingDirectory() {
		return null;
	}

	private List<URL> getDefaultNexusTemplatesProcessingFile() throws AcquisitionConfigurationException {
		return SpringApplicationContextFacade.getBean(ClientSpringProperties.class).getAcquisitions().stream()
				.filter(a -> a.getType().equals(AcquisitionPropertyType.TOMOGRAPHY))
				.findFirst()
				.map(AcquisitionConfigurationProperties::getProcessingRequest)
				.map(ProcessingRequestProperties::getNexusTemplates)
				.orElseThrow(() -> new AcquisitionConfigurationException("There are no properties associated with the acqual acquisition"));
	}

	private ProcessingRequestKeyFactory getProcessingRequestKeyFactory() {
		return SpringApplicationContextFacade.getBean(ProcessingRequestKeyFactory.class);
	}

	private ScanningParameters getScanningParameters() {
		return getScanningAcquisitionTemporaryHelper()
				.getScanningParameters()
				.orElseThrow();
	}

	private ScanningConfiguration getScanningConfiguration() {
		return getScanningAcquisitionTemporaryHelper()
				.getAcquisitionConfiguration()
				.orElseThrow();
	}

	private ScanningAcquisitionTemporaryHelper getScanningAcquisitionTemporaryHelper() {
		return SpringApplicationContextFacade.getBean(ScanningAcquisitionTemporaryHelper.class);
	}
}