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
import static uk.ac.gda.ui.tool.ClientMessages.PROJECTIONS;
import static uk.ac.gda.ui.tool.ClientMessages.RANGE;
import static uk.ac.gda.ui.tool.ClientMessages.REMOVE_SELECTION_TP;
import static uk.ac.gda.ui.tool.ClientMessages.REPEATE_SCAN;
import static uk.ac.gda.ui.tool.ClientMessages.REPEATE_SCAN_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessages.SAVU;
import static uk.ac.gda.ui.tool.ClientMessages.SELECT_SAVU_PROCESSING_FILE;
import static uk.ac.gda.ui.tool.ClientMessages.SELECT_SAVU_PROCESSING_FILE_TP;
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
import static uk.ac.gda.ui.tool.ClientVerifyListener.verifyOnlyIntegerText;
import static uk.ac.gda.ui.tool.WidgetUtilities.addWidgetDisposableListener;
import static uk.ac.gda.ui.tool.spring.SpringApplicationContextProxy.addDisposableApplicationListener;

import java.net.URL;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.EnumMap;
import java.util.Map;
import java.util.Optional;

import org.apache.commons.io.FilenameUtils;
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
import org.springframework.context.ApplicationListener;

import gda.mscan.element.Mutator;
import uk.ac.diamond.daq.mapping.api.document.helper.ImageCalibrationHelper;
import uk.ac.diamond.daq.mapping.api.document.helper.MultipleScansHelper;
import uk.ac.diamond.daq.mapping.api.document.helper.ScanpathDocumentHelper;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningConfiguration;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.diamond.daq.mapping.ui.stage.IStageController;
import uk.ac.diamond.daq.mapping.ui.stage.enumeration.StageDevice;
import uk.ac.gda.api.acquisition.AcquisitionController;
import uk.ac.gda.api.acquisition.configuration.ImageCalibration;
import uk.ac.gda.api.acquisition.configuration.MultipleScansType;
import uk.ac.gda.api.acquisition.parameters.DetectorDocument;
import uk.ac.gda.api.acquisition.resource.event.AcquisitionConfigurationResourceLoadEvent;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.client.widgets.SelectFileComposite;
import uk.ac.gda.core.tool.spring.AcquisitionFileContext;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.core.tool.spring.TomographyContextFile;
import uk.ac.gda.ui.tool.ClientBindingElements;
import uk.ac.gda.ui.tool.ClientMessages;
import uk.ac.gda.ui.tool.ClientMessagesUtility;
import uk.ac.gda.ui.tool.images.ClientImages;
import uk.ac.gda.ui.tool.selectable.NamedComposite;

/**
 * This Composite allows to edit a {@link ScanningParameters} object.
 *
 * @author Maurizio Nagni
 */
public class TomographyConfigurationCompositeFactory implements NamedComposite {

	private static final Logger logger = LoggerFactory.getLogger(TomographyConfigurationCompositeFactory.class);
	private static final String NO_FILE_SELECTED = "No File selected";

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

	private Composite selectProcessingFile;
	private Label savuFileName;
	private Button removeSavuFile;

	protected final AcquisitionController<ScanningAcquisition> controller;
	private final IStageController stageController;
	private ScanpathDocumentHelper dataHelper;
	private MultipleScansHelper configurationHelper;
	private ImageCalibrationHelper imageCalibrationHelper;

	private DataBindingContext dbc = new DataBindingContext();

	public TomographyConfigurationCompositeFactory(AcquisitionController<ScanningAcquisition> controller, IStageController stageController) {
		super();
		this.controller = controller;
		this.stageController = stageController;
		this.dataHelper = new ScanpathDocumentHelper(this::getAcquisitionParameters);
		this.configurationHelper = new MultipleScansHelper(this::getAcquisitionConfiguration);
		this.imageCalibrationHelper = new ImageCalibrationHelper(this::getAcquisitionConfiguration);
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		logger.debug("Creating {}", this);
		Composite mainComposite = createClientCompositeWithGridLayout(parent, SWT.NONE, 3);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(mainComposite);
		standardMarginHeight(mainComposite.getLayout());
		standardMarginWidth(mainComposite.getLayout());

		createElements(mainComposite, SWT.NONE, SWT.BORDER);
		bindElements();
		initialiseElements();
		addWidgetsListener();
		try {
			addDisposableApplicationListener(mainComposite, new LoadListener(mainComposite));
		} catch (GDAClientException e) {
			UIHelper.showWarning("Loading a file will not refresh the gui", "Spring application listener not registered");
		}
		logger.debug("Created {}", this);
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

		group = createClientGroup(parent, SWT.NONE, 3, SAVU);
		createClientGridDataFactory().span(3, 1).applyTo(group);
		savuSelection(group, SWT.NONE);

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

		totalProjections = createClientText(parent, textStyle, TOTAL_PROJECTIONS_TOOLTIP, verifyOnlyIntegerText);
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

		numberDark = createClientText(parent, textStyle, NUM_DARK_TOOLTIP, verifyOnlyIntegerText);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(DEFAULT_TEXT_SIZE).applyTo(numberDark);

		createClientEmptyCell(parent, new Point(50, 10));

		label = createClientLabel(parent, labelStyle, DARK_EXPOSURE);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).indent(20, SWT.DEFAULT).applyTo(label);

		darkExposure = createClientText(parent, textStyle, DARK_EXPOSURE_TP, verifyOnlyIntegerText);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(DEFAULT_TEXT_SIZE).span(2, 1)
				.applyTo(darkExposure);

		label = createClientLabel(parent, labelStyle, NUM_FLAT);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).indent(5, SWT.DEFAULT).applyTo(label);

		numberFlat = createClientText(parent, textStyle, NUM_FLAT_TOOLTIP, verifyOnlyIntegerText);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(DEFAULT_TEXT_SIZE).span(2, 1)
				.applyTo(numberFlat);

		label = createClientLabel(parent, labelStyle, ClientMessagesUtility.getMessage(FLAT_EXPOSURE) + ":");
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).indent(20, SWT.DEFAULT).applyTo(label);

		flatExposure = createClientText(parent, textStyle, FLAT_EXPOSURE_TP, verifyOnlyIntegerText);
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
							TomographyConfigurationCompositeFactory.this::getAcquisitionConfiguration).open();
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

		numberRepetitions = createClientText(container, SWT.NONE, NUM_REPETITIONS_TOOLTIP, verifyOnlyIntegerText);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).hint(DEFAULT_TEXT_SIZE).applyTo(numberRepetitions);

		createClientEmptyCell(container, new Point(10, 10));

		label = createClientLabel(container, SWT.NONE, WAITING_TIME);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.CENTER).applyTo(label);
		label.setText(label.getText() + " (s)");

		waitingTime = createClientText(container, SWT.NONE, WAITING_TIME_TOOLTIP, verifyOnlyIntegerText);
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

	private void savuSelection(Composite parent, int labelStyle) {
		Composite container = createClientCompositeWithGridLayout(parent, SWT.NONE, 3);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(container);

		selectProcessingFile = new SelectFileComposite.SelectFileCompositeBuilder()
				.setLayout(SELECT_SAVU_PROCESSING_FILE, SELECT_SAVU_PROCESSING_FILE_TP)
				.setLogic(getProcessingFileDirectory(), this::setDefaultProcessingFile)
				.build()
				.createComposite(container, SWT.NONE);
		createClientGridDataFactory().align(SWT.BEGINNING, SWT.END).applyTo(selectProcessingFile);

		savuFileName = createClientLabel(container, labelStyle, EMPTY_MESSAGE);
		createClientGridDataFactory().align(SWT.FILL, SWT.END).grab(true, false).applyTo(savuFileName);

		removeSavuFile = createClientButton(container, SWT.PUSH, EMPTY_MESSAGE, REMOVE_SELECTION_TP, ClientImages.DELETE);
		createClientGridDataFactory().align(SWT.END, SWT.END).applyTo(removeSavuFile);
		addWidgetDisposableListener(removeSavuFile, SWT.Selection, this::removeDefaultProcessingFile);
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
		return Optional.ofNullable(angle)
				.filter(s -> !s.isEmpty())
				.map(Double::parseDouble)
				.orElseGet(() -> 0.0);
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
		ClientBindingElements.bindEnumToRadio(dbc, MultipleScansType.class, "multipleScans.multipleScansType",
				getAcquisitionConfiguration(), enumRadioMap);
	}

	private void initialiseElements() {
		name.setText(getController().getAcquisition().getName());
		initializeScanType();
		initializeAngleRange();
		initializeEndAngle();
		updateAngularStep();

		initializeImageCalibration();

		totalProjections.setText(Integer.toString(getScannableTrackDocument().getPoints()));
		forceFocusOnEmpty(numberDark, Integer.toString(getAcquisitionConfiguration().getImageCalibration()
				.getDarkCalibration().getNumberExposures()));
		forceFocusOnEmpty(numberFlat, Integer.toString(getAcquisitionConfiguration().getImageCalibration()
				.getFlatCalibration().getNumberExposures()));
		updateMultipleScan();
		updateSavuComponent();
	}

	private void initializeScanType() {
		stepScanType.setSelection(false);
		flyScanType.setSelection(true);
		addContinuous();
	}

	private void initializeAngleRange() {
		currentAngleButton.setSelection(false);
		startAngleText.setText(Double.toString(getScannableTrackDocument().getStart()));
		initializeEndAngle();
		updateStartStop();
	}

	private void initializeEndAngle() {
		halfRotationRangeType.setSelection(false);
		fullRotationRangeType.setSelection(false);
		customAngle.setEnabled(false);
		customRotationRangeType.setSelection(false);
		customAngle.setText("");

		String customAngleString = "";
		if (getScannableTrackDocument().getStop() == 180.0) {
			halfRotationRangeType.setSelection(true);
			customAngleString = "180.0";
		} else if (getScannableTrackDocument().getStop() == 360.0) {
			fullRotationRangeType.setSelection(true);
			customAngleString = "360.0";
		} else if (!Double.isNaN(getScannableTrackDocument().getStop())) {
			customRotationRangeType.setSelection(true);
			Event event = new Event();
			event.widget = customRotationRangeType;
			customAngleString = Double.toString(getScannableTrackDocument().getStop());
			Arrays.stream(customRotationRangeType.getListeners(SWT.SELECTED))
				.forEach(l -> l.handleEvent(event));
			customAngle.setEnabled(true);
		}
		customAngle.setText(customAngleString);
	}

	private void initializeImageCalibration() {
		ImageCalibration ic = getAcquisitionConfiguration().getImageCalibration();
		Optional.ofNullable(ic.getDarkCalibration().getNumberExposures())
			.ifPresent(exposure -> numberDark.setText(Integer.toString(exposure)));

		Optional.ofNullable(ic.getDarkCalibration().getDetectorDocument())
			.map(DetectorDocument::getExposure)
			.ifPresent(exposure -> darkExposure.setText(Double.toString(exposure)));

		// For the moment dark and flat have the same boolean values
		Optional.ofNullable(ic.getDarkCalibration().isBeforeAcquisition())
			.ifPresent(selected -> beforeAcquisition.setSelection(selected));

		// For the moment dark and flat have the same boolean values
		Optional.ofNullable(ic.getDarkCalibration().isAfterAcquisition())
		.ifPresent(selected -> afterAcquisition.setSelection(selected));

		Optional.ofNullable(ic.getFlatCalibration().getNumberExposures())
			.ifPresent(exposure ->	numberFlat.setText(Integer.toString(exposure)));

		Optional.ofNullable(ic.getFlatCalibration().getDetectorDocument())
			.map(DetectorDocument::getExposure)
			.ifPresent(exposure -> flatExposure.setText(Double.toString(exposure)));
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

	private AcquisitionFileContext getClientContext() {
		return SpringApplicationContextFacade.getBean(AcquisitionFileContext.class);
	}

	private URL getProcessingFileDirectory() {
		return getClientContext().getTomographyContext().getContextFile(TomographyContextFile.TOMOGRAPHY_SAVU_DIRECTORY);
	}

	private void setDefaultProcessingFile(URL defaultProcessingFile) {
		getClientContext().getTomographyContext()
			.putFileInContext(TomographyContextFile.TOMOGRAPHY_DEFAULT_PROCESSING_FILE, defaultProcessingFile);
		updateSavuComponent();
	}

	private void removeDefaultProcessingFile(Event event) {
		getClientContext().getTomographyContext()
			.removeFileFromContext(TomographyContextFile.TOMOGRAPHY_DEFAULT_PROCESSING_FILE);
		updateSavuComponent();
	}

	private void updateSavuComponent() {
		savuFileName.setText(NO_FILE_SELECTED);
		URL url = getClientContext().getTomographyContext().getContextFile(TomographyContextFile.TOMOGRAPHY_DEFAULT_PROCESSING_FILE);
		if (url == null) {
			savuFileName.setText(NO_FILE_SELECTED);
			removeSavuFile.setEnabled(false);
		} else {
			savuFileName.setText(FilenameUtils.getName(url.getFile()));
			removeSavuFile.setEnabled(true);
		}
		savuFileName.getParent().layout(true, true);
	}

	@Override
	public ClientMessages getName() {
		return ClientMessages.TOMOGRAPHY;
	}

	@Override
	public ClientMessages getTooltip() {
		return ClientMessages.TOMOGRAPHY_TP;
	}
}