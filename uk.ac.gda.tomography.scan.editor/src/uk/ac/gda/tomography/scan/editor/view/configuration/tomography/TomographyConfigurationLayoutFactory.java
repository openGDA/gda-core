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
import static uk.ac.gda.ui.tool.ClientMessages.CURRENT_ANGLE;
import static uk.ac.gda.ui.tool.ClientMessages.CURRENT_ANGLE_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessages.CUSTOM_END_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessages.DARK_EXPOSURE;
import static uk.ac.gda.ui.tool.ClientMessages.DARK_EXPOSURE_TP;
import static uk.ac.gda.ui.tool.ClientMessages.EMPTY_MESSAGE;
import static uk.ac.gda.ui.tool.ClientMessages.FINAL_ANGLE;
import static uk.ac.gda.ui.tool.ClientMessages.FLAT_EXPOSURE;
import static uk.ac.gda.ui.tool.ClientMessages.FLAT_EXPOSURE_TP;
import static uk.ac.gda.ui.tool.ClientMessages.FLY_SCAN;
import static uk.ac.gda.ui.tool.ClientMessages.FLY_SCAN_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessages.FULL_ANGLE;
import static uk.ac.gda.ui.tool.ClientMessages.FULL_ANGLE_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessages.IMAGE_CALIBRATION;
import static uk.ac.gda.ui.tool.ClientMessages.MULTIPLE_SCANS;
import static uk.ac.gda.ui.tool.ClientMessages.MULTIPLE_SCANS_TOOLTIP;
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
import static uk.ac.gda.ui.tool.ClientMessages.TOTAL_PROJECTIONS;
import static uk.ac.gda.ui.tool.ClientMessages.TOTAL_PROJECTIONS_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessages.WAITING_TIME;
import static uk.ac.gda.ui.tool.ClientMessages.WAITING_TIME_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientSWTElements.DEFAULT_TEXT_SIZE;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientButton;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientCompositeWithGridLayout;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientEmptyCell;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGroup;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientLabel;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientText;
import static uk.ac.gda.ui.tool.ClientSWTElements.standardMarginHeight;
import static uk.ac.gda.ui.tool.ClientSWTElements.standardMarginWidth;
import static uk.ac.gda.ui.tool.ClientVerifyListener.verifyOnlyDoubleText;
import static uk.ac.gda.ui.tool.ClientVerifyListener.verifyOnlyPositiveDoubleText;
import static uk.ac.gda.ui.tool.ClientVerifyListener.verifyOnlyPositiveIntegerText;
import static uk.ac.gda.ui.tool.WidgetUtilities.addWidgetDisposableListener;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.lang3.math.NumberUtils;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.swt.SWT;
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
import uk.ac.diamond.daq.mapping.ui.controller.AcquisitionConfigurationException;
import uk.ac.diamond.daq.mapping.ui.controller.StageController;
import uk.ac.diamond.daq.mapping.ui.stage.enumeration.StageDevice;
import uk.ac.gda.api.acquisition.AcquisitionController;
import uk.ac.gda.api.acquisition.configuration.MultipleScansType;
import uk.ac.gda.api.acquisition.configuration.calibration.DarkCalibrationDocument;
import uk.ac.gda.api.acquisition.configuration.calibration.FlatCalibrationDocument;
import uk.ac.gda.api.acquisition.parameters.DetectorDocument;
import uk.ac.gda.client.properties.acquisition.AcquisitionConfigurationProperties;
import uk.ac.gda.client.properties.acquisition.AcquisitionPropertyType;
import uk.ac.gda.core.tool.spring.AcquisitionFileContext;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.core.tool.spring.TomographyContextFile;
import uk.ac.gda.tomography.scan.editor.view.MultipleScanDialog;
import uk.ac.gda.tomography.scan.editor.view.ScanType;
import uk.ac.gda.ui.tool.ClientBindingElements;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;
import uk.ac.gda.ui.tool.Reloadable;
import uk.ac.gda.ui.tool.processing.ProcessingRequestComposite;
import uk.ac.gda.ui.tool.processing.ProcessingRequestContext;
import uk.ac.gda.ui.tool.processing.ProcessingRequestKey;
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
	private Button multipleScans;
	private Text numberRepetitions;
	private Text waitingTime;
	private Button repeateMultipleScansType;
	private Button switchbackMultipleScansType;
	private ExpandItem multiplExpandItem;
	private Composite multipleGroup;

	private ProcessingRequestComposite processingRequest;

	private final AcquisitionController<ScanningAcquisition> acquisitionController;

	private ScanpathDocumentHelper dataHelper;
	private MultipleScansHelper configurationHelper;
	private ImageCalibrationHelper imageCalibrationHelper;

	private DataBindingContext dbc = new DataBindingContext();

	private Composite mainComposite;

	public TomographyConfigurationLayoutFactory(AcquisitionController<ScanningAcquisition> acquisitionController) {
		this.acquisitionController = acquisitionController;
		this.dataHelper = new ScanpathDocumentHelper(this::getAcquisitionParameters);
		this.configurationHelper = new MultipleScansHelper(this::getAcquisitionConfiguration);
		this.imageCalibrationHelper = new ImageCalibrationHelper(this::getAcquisitionConfiguration);
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		logger.debug("Creating {}", this);
		mainComposite = createClientCompositeWithGridLayout(parent, SWT.NONE, 3);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(mainComposite);
		standardMarginHeight(mainComposite.getLayout());
		standardMarginWidth(mainComposite.getLayout());

		createElements(mainComposite, SWT.NONE, SWT.BORDER);
		bindElements();
		initialiseElements();
		addWidgetsListener();

		logger.debug("Created {}", this);
		return mainComposite;
	}

	@Override
	public void reload() {
		bindElements();
		initialiseElements();
		mainComposite.getShell().layout(true, true);
	}

	private AcquisitionController<ScanningAcquisition> getAcquisitionController() {
		return acquisitionController;
	}

	/**
	 * @param parent
	 *            a three column composite
	 * @param labelStyle
	 * @param textStyle
	 */
	private void createElements(Composite parent, int labelStyle, int textStyle) {
		nameAndScanTypeContent(parent, labelStyle, textStyle);

		Group group = createClientGroup(parent, SWT.NONE, 10, RANGE);
		// Configure the group to span all the parent 3 columns
		createClientGridDataFactory().span(3, 1).applyTo(group);
		startAngleContent(group, labelStyle, textStyle);
		endAngleContent(group, labelStyle, textStyle);

		// Defines a Group with 3 columns
		group = createClientGroup(parent, SWT.NONE, 3, PROJECTIONS);
		// Configure the group to span all the parent 3 columns
		createClientGridDataFactory().span(3, 1).applyTo(group);
		projectionsContent(group, labelStyle, textStyle);

		group = createClientGroup(parent, SWT.NONE, 6, IMAGE_CALIBRATION);
		createClientGridDataFactory().span(3, 1).applyTo(group);
		imagesCalibrationContent(group, labelStyle, textStyle);

		group = createClientGroup(parent, SWT.NONE, 3, PROCESS_REQUESTS);
		createClientGridDataFactory().span(3, 1).applyTo(group);
		createProcessRequestComposite(group, SWT.NONE);

		//multipleScansContentAlternative(parent, labelStyle, textStyle);

		multipleGroup = createClientCompositeWithGridLayout(parent, SWT.NONE, 1);
		createClientGridDataFactory().span(3, 1).applyTo(multipleGroup);
		multipleScansContent(multipleGroup, labelStyle, textStyle);
	}

	private void nameAndScanTypeContent(Composite parent, int labelStyle, int textStyle) {
		Label labelName = createClientLabel(parent, labelStyle, NAME);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.END).applyTo(labelName);

		this.name = createClientText(parent, textStyle, NAME_TOOLTIP);
		createClientGridDataFactory().span(2, 1).applyTo(this.name);

		flyScanType = createClientButton(parent, SWT.RADIO, FLY_SCAN, FLY_SCAN_TOOLTIP);
		flyScanType.setData(ScanType.FLY);
		createClientGridDataFactory().applyTo(flyScanType);

		stepScanType = createClientButton(parent, SWT.RADIO, STEP_SCAN, STEP_SCAN_TOOLTIP);
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
		Label labelStart = createClientLabel(parent, labelStyle, START);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER)
			.indent(5, SWT.DEFAULT).applyTo(labelStart);

		startAngleText = createClientText(parent, textStyle, START_ANGLE_TOOLTIP, verifyOnlyDoubleText);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(DEFAULT_TEXT_SIZE)
			.span(2, 1).applyTo(startAngleText);

		currentAngleButton = createClientButton(parent, SWT.CHECK, CURRENT_ANGLE, CURRENT_ANGLE_TOOLTIP);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(150, SWT.DEFAULT).indent(5, SWT.DEFAULT)
			.span(7, 1).applyTo(currentAngleButton);
	}

	private void endAngleContent(Composite parent, int labelStyle, int textStyle) {
		Label labelRange = createClientLabel(parent, labelStyle, RANGE);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER)
			.indent(5, SWT.DEFAULT).applyTo(labelRange);

		halfRotationRangeType = createClientButton(parent, SWT.RADIO, STRAIGHT_ANGLE, STRAIGHT_ANGLE_TOOLTIP);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER)
			.applyTo(halfRotationRangeType);

		fullRotationRangeType = createClientButton(parent, SWT.RADIO, FULL_ANGLE, FULL_ANGLE_TOOLTIP);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER)
			.applyTo(fullRotationRangeType);

		customRotationRangeType = createClientButton(parent, SWT.RADIO, EMPTY_MESSAGE, CUSTOM_END_TOOLTIP);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER)
			.applyTo(customRotationRangeType);

		customAngle = createClientText(parent, textStyle, CUSTOM_END_TOOLTIP, verifyOnlyDoubleText);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(DEFAULT_TEXT_SIZE)
			.applyTo(customAngle);

		Label finalLabel = createClientLabel(parent, labelStyle, FINAL_ANGLE);
		createClientGridDataFactory().align(SWT.END, SWT.CENTER).span(3, 1).applyTo(finalLabel);
		finalAngle = createClientLabel(parent, labelStyle, EMPTY_MESSAGE);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).applyTo(finalAngle);
	}

	private void projectionsContent(Composite parent, int labelStyle, int textStyle) {
		Label label = createClientLabel(parent, labelStyle, TOTAL_PROJECTIONS);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).applyTo(label);

		totalProjections = createClientText(parent, textStyle, TOTAL_PROJECTIONS_TOOLTIP, verifyOnlyPositiveIntegerText);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(DEFAULT_TEXT_SIZE).span(2, 1)
				.applyTo(totalProjections);

		label = createClientLabel(parent, labelStyle, ANGULAR_STEP);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).applyTo(label);

		angularStep = createClientLabel(parent, labelStyle, EMPTY_MESSAGE);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).span(2, 1).applyTo(angularStep);
	}

	private void imagesCalibrationContent(Composite parent, int labelStyle, int textStyle) {
		Label label = createClientLabel(parent, labelStyle, NUM_DARK);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).indent(5, SWT.DEFAULT).applyTo(label);

		numberDark = createClientText(parent, textStyle, NUM_DARK_TOOLTIP, verifyOnlyPositiveIntegerText);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(DEFAULT_TEXT_SIZE).applyTo(numberDark);

		createClientEmptyCell(parent, new Point(50, 10));

		label = createClientLabel(parent, labelStyle, DARK_EXPOSURE);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).indent(20, SWT.DEFAULT).applyTo(label);

		darkExposure = createClientText(parent, textStyle, DARK_EXPOSURE_TP, verifyOnlyPositiveDoubleText);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(DEFAULT_TEXT_SIZE).span(2, 1)
				.applyTo(darkExposure);

		label = createClientLabel(parent, labelStyle, NUM_FLAT);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).indent(5, SWT.DEFAULT).applyTo(label);

		numberFlat = createClientText(parent, textStyle, NUM_FLAT_TOOLTIP, verifyOnlyPositiveIntegerText);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(DEFAULT_TEXT_SIZE).span(2, 1)
				.applyTo(numberFlat);

		label = createClientLabel(parent, labelStyle, ClientMessagesUtility.getMessage(FLAT_EXPOSURE) + ":");
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).indent(20, SWT.DEFAULT).applyTo(label);

		flatExposure = createClientText(parent, textStyle, FLAT_EXPOSURE_TP, verifyOnlyPositiveDoubleText);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(DEFAULT_TEXT_SIZE).span(2, 1)
				.applyTo(flatExposure);

		beforeAcquisition = createClientButton(parent, SWT.CHECK, AT_START, AT_START_TOOLTIP);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).span(2, 1).applyTo(beforeAcquisition);

		afterAcquisition = createClientButton(parent, SWT.CHECK, AT_END, AT_END_TOOLTIP);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).span(2, 1).applyTo(afterAcquisition);
	}

	private void multipleScansContentAlternative(Composite parent, int labelStyle, int textStyle) {
		final Button multipleScansAlternative = createClientButton(parent, SWT.PUSH, MULTIPLE_SCANS,
				MULTIPLE_SCANS_TOOLTIP);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).span(3, 1).applyTo(multipleScansAlternative);

		multipleScansAlternative.addSelectionListener(new SelectionListener() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				if (e.widget.equals(multipleScansAlternative)) {
					new MultipleScanDialog(parent.getShell(),
							TomographyConfigurationLayoutFactory.this::getAcquisitionConfiguration).open();
					updateMultipleScan();
					if (getAcquisitionConfiguration().getMultipleScans().getNumberRepetitions() > 1
							|| getAcquisitionConfiguration().getMultipleScans().getWaitingTime() > 0
							|| !MultipleScansType.REPEAT_SCAN
								.equals(getAcquisitionConfiguration().getMultipleScans().getMultipleScansType())) {
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
		Composite container = createClientCompositeWithGridLayout(customBar, SWT.NONE | SWT.BORDER, 5);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.BEGINNING).grab(true, true).applyTo(container);

		Label label = createClientLabel(container, SWT.NONE, NUM_REPETITIONS);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).applyTo(label);

		numberRepetitions = createClientText(container, SWT.NONE, NUM_REPETITIONS_TOOLTIP, verifyOnlyPositiveIntegerText);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(DEFAULT_TEXT_SIZE).applyTo(numberRepetitions);

		createClientEmptyCell(container, new Point(10, 10));

		label = createClientLabel(container, SWT.NONE, WAITING_TIME);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).applyTo(label);
		label.setText(label.getText() + " (s)");

		waitingTime = createClientText(container, SWT.NONE, WAITING_TIME_TOOLTIP, verifyOnlyPositiveDoubleText);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(DEFAULT_TEXT_SIZE).applyTo(waitingTime);

		repeateMultipleScansType = createClientButton(container, SWT.RADIO, REPEATE_SCAN,
				REPEATE_SCAN_TOOLTIP);
		repeateMultipleScansType.setData(MultipleScansType.REPEAT_SCAN);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).applyTo(repeateMultipleScansType);

		switchbackMultipleScansType = createClientButton(container, SWT.RADIO, SWITCHBACK_SCAN,
				SWITCHBACK_SCAN_TOOLTIP);
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

	private List<ProcessingRequestContext> getProcessingRequestContext() {
		// The selectable process elements
		List<ProcessingRequestContext> processingRequestContexts = new ArrayList<>();
		try {
			// makes available for selection a ApplyNexusTemplatesRequest element
			processingRequestContexts.add(new ProcessingRequestContext(ProcessingRequestKey.APPLY_NEXUS_TEMPLATE,
					getNexusTemplatesProcessingDirectory(), getDefaultNexusTemplatesProcessingFile(), true));
		} catch (AcquisitionConfigurationException e) {
			logger.error("TODO put description of error here", e);
		}

		// makes available for selection a SavuProcessingRequest element
		processingRequestContexts.add(new ProcessingRequestContext(ProcessingRequestKey.SAVU,
				getSavuProcessingFileDirectory(), getSavuDefaultProcessingFile(), false));
		return processingRequestContexts;
	}

	private void createProcessRequestComposite(Composite parent, int labelStyle) {
		processingRequest = new ProcessingRequestComposite(getProcessingRequestContext());
		processingRequest.createComposite(parent, labelStyle);
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
			if (!Button.class.isInstance(event.getSource()))
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
		getAcquisitionController().getAcquisition().setName(name.getText());
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
		name.setText(getAcquisitionController().getAcquisition().getName());
		initializeScanType();
		initializeStartAngle();
		initializeEndAngle();
		initializeProjections();
		updateStartStop();

		initializeImageCalibration();
		updateMultipleScan();
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
		var ic = getAcquisitionConfiguration().getImageCalibration();
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


	private void updateMultipleScan() {
		Arrays.asList(switchbackMultipleScansType, repeateMultipleScansType).stream()
				.filter(i -> getAcquisitionConfiguration().getMultipleScans().getMultipleScansType()
						.equals(i.getData()))
				.findFirst()
				.ifPresent(b -> b.setSelection(true));
		Arrays.asList(switchbackMultipleScansType, repeateMultipleScansType).stream()
				.filter(i -> !getAcquisitionConfiguration().getMultipleScans().getMultipleScansType()
						.equals(i.getData()))
				.findFirst()
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

	private ExpandItem expandBar(ExpandBar parent, Composite group, Button radio) {
		ExpandItem item0 = new ExpandItem(parent, SWT.NONE, 0);
		item0.setText(ClientMessagesUtility.getMessage(ClientMessages.MULTIPLE_SCANS));
		item0.setHeight(parent.computeSize(SWT.DEFAULT, 100).y);
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
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(bar);
		return bar;
	}

	private ScanningParameters getAcquisitionParameters() {
		return getAcquisitionConfiguration().getAcquisitionParameters();
	}

	private ScanningConfiguration getAcquisitionConfiguration() {
		return getAcquisitionController().getAcquisition().getAcquisitionConfiguration();
	}

	private ScannableTrackDocument getScannableTrackDocument() {
		return getAcquisitionController().getAcquisition().getAcquisitionConfiguration().getAcquisitionParameters().getScanpathDocument().getScannableTrackDocuments()
				.get(0);
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
				.map(AcquisitionConfigurationProperties::getNexusTemplates)
				.orElseThrow(() -> new AcquisitionConfigurationException("There are no properties associated with the acqual acquisition"));
	}

	private StageController getStageController() {
		return getBean(StageController.class);
	}
}