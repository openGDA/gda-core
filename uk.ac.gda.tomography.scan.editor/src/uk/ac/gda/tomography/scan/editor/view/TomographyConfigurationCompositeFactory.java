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
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.events.FocusListener;
import org.eclipse.swt.events.ModifyEvent;
import org.eclipse.swt.events.SelectionAdapter;
import org.eclipse.swt.events.SelectionEvent;
import org.eclipse.swt.events.SelectionListener;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.layout.GridData;
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
import uk.ac.diamond.daq.mapping.api.document.base.configuration.MultipleScansType;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.diamond.daq.mapping.api.document.tomography.TomographyConfiguration;
import uk.ac.diamond.daq.mapping.api.document.tomography.TomographyParameterAcquisition;
import uk.ac.gda.api.acquisition.AcquisitionController;
import uk.ac.gda.api.acquisition.resource.event.AcquisitionConfigurationResourceLoadEvent;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.tomography.stage.IStageController;
import uk.ac.gda.tomography.stage.enumeration.StageDevice;
import uk.ac.gda.ui.tool.ClientBindingElements;
import uk.ac.gda.ui.tool.ClientMessages;
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

	protected final AcquisitionController<TomographyParameterAcquisition> controller;
	private final IStageController stageController;
	private TomographyTemplateDataHelper dataHelper;

	private static final Logger logger = LoggerFactory.getLogger(TomographyConfigurationCompositeFactory.class);

	public TomographyConfigurationCompositeFactory(AcquisitionController<TomographyParameterAcquisition> controller, IStageController stageController) {
		super();
		this.controller = controller;
		this.stageController = stageController;
		this.dataHelper = new TomographyTemplateDataHelper(this::getTemplateData);
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		Composite composite = ClientSWTElements.createComposite(parent, SWT.NONE);
		createElements(parent, SWT.NONE, SWT.BORDER);
		bindElements();
		initialiseElements();
		try {
			SpringApplicationContextProxy.addDisposableApplicationListener(composite, new LoadListener(composite));
		} catch (GDAClientException e) {
			UIHelper.showWarning("Loading a file will not refresh the gui", "Spring application listener not registered");
		}
		return composite;
	}

	private AcquisitionController<TomographyParameterAcquisition> getController() {
		return controller;
	}

	private void createElements(Composite parent, int labelStyle, int textStyle) {
		parent.setBackground(parent.getDisplay().getSystemColor(SWT.COLOR_WIDGET_LIGHT_SHADOW));
		GridLayoutFactory.swtDefaults().margins(ClientSWTElements.defaultCompositeMargin()).applyTo(parent);
		nameAndScanTypeContent(ClientSWTElements.createComposite(parent, SWT.NONE, 2), labelStyle, textStyle);
		startAngleContent(ClientSWTElements.createGroup(parent, 2, ClientMessages.START), labelStyle, textStyle);
		endAngleContent(ClientSWTElements.createGroup(parent, 3, ClientMessages.END), labelStyle, textStyle);
		projectionsContent(ClientSWTElements.createGroup(parent, 2, ClientMessages.PROJECTIONS), labelStyle, textStyle);
		imagesCalibrationContent(ClientSWTElements.createGroup(parent, 2, ClientMessages.IMAGE_CALIBRATION), labelStyle, textStyle);
		multipleScansContent(parent, labelStyle, textStyle);
	}

	private void nameAndScanTypeContent(Composite parent, int labelStyle, int textStyle) {
		ClientSWTElements.createLabel(parent, labelStyle, ClientMessages.NAME, new Point(2, 1));
		this.name = ClientSWTElements.createText(parent, textStyle, null, new Point(2, 1), ClientMessages.NAME_TOOLTIP, new Point(500, SWT.DEFAULT), null);
		flyScanType = ClientSWTElements.createButton(parent, SWT.RADIO, ClientMessages.FLY_SCAN, ClientMessages.FLY_SCAN_TOOLTIP);
		stepScanType = ClientSWTElements.createButton(parent, SWT.RADIO, ClientMessages.STEP_SCAN, ClientMessages.STEP_SCAN_TOOLTIP);

		flyScanType.addSelectionListener(mutatorsListener);
		stepScanType.addSelectionListener(mutatorsListener);
	}

	private SelectionListener mutatorsListener = new SelectionListener() {
		@Override
		public void widgetSelected(SelectionEvent event) {
			if (!Button.class.isInstance(event.getSource()))
				return;
			if (event.getSource().equals(flyScanType)) {
				dataHelper.addMutators(Mutator.CONTINUOUS, new ArrayList<>());
			} else {
				dataHelper.removeMutators(Mutator.CONTINUOUS);
			}
		}

		@Override
		public void widgetDefaultSelected(SelectionEvent event) {
			// do nothing
		}
	};

	private void startAngleContent(Composite parent, int labelStyle, int textStyle) {
		ClientSWTElements.createLabel(parent, labelStyle, ClientMessages.ANGLE, new Point(2, 1));
		startAngleText = ClientSWTElements.createText(parent, textStyle, verifyOnlyDoubleText, null, ClientMessages.START_ANGLE_TOOLTIP, null);
		currentAngleButton = ClientSWTElements.createButton(parent, SWT.CHECK, ClientMessages.CURRENT_ANGLE, ClientMessages.CURRENT_ANGLE_TOOLTIP);
	}

	private void endAngleContent(Composite parent, int labelStyle, int textStyle) {
		halfRotationRangeType = ClientSWTElements.createButton(parent, SWT.RADIO, ClientMessages.STRAIGHT_ANGLE, ClientMessages.STRAIGHT_ANGLE_TOOLTIP);

		fullRotationRangeType = ClientSWTElements.createButton(parent, SWT.RADIO, ClientMessages.FULL_ANGLE, ClientMessages.FULL_ANGLE_TOOLTIP);

		customRotationRangeType = ClientSWTElements.createButton(parent, SWT.RADIO, ClientMessages.CUSTOM, ClientMessages.CUSTOM_END_TOOLTIP);
		ExpandBar customBar = createExpandBar(parent);
		customGroup = ClientSWTElements.createGroup(customBar, 1, null);
		ClientSWTElements.createLabel(customGroup, labelStyle, ClientMessages.ANGLE);
		customAngle = ClientSWTElements.createText(customGroup, textStyle, verifyOnlyDoubleText, null, ClientMessages.CUSTOM_END_TOOLTIP, null);
		expandBar(customBar, customGroup, customRotationRangeType);
	}

	private void projectionsContent(Composite parent, int labelStyle, int textStyle) {
		ClientSWTElements.createLabel(parent, labelStyle, ClientMessages.TOTAL_PROJECTIONS);
		ClientSWTElements.createLabel(parent, labelStyle, ClientMessages.ANGULAR_STEP);
		totalProjections = ClientSWTElements.createText(parent, textStyle, verifyOnlyIntegerText, null, ClientMessages.TOTAL_PROJECTIONS_TOOLTIP, null);
		angularStep = ClientSWTElements.createLabel(parent, labelStyle, ClientMessages.EMPTY_MESSAGE);
	}

	private void imagesCalibrationContent(Composite parent, int labelStyle, int textStyle) {
		ClientSWTElements.createLabel(parent, labelStyle, ClientMessages.NUM_DARK);
		ClientSWTElements.createLabel(parent, labelStyle, ClientMessages.DARK_EXPOSURE);
		numberDark = ClientSWTElements.createText(parent, textStyle, verifyOnlyIntegerText, new Point(1, 1), ClientMessages.NUM_DARK_TOOLTIP, null);
		darkExposure = ClientSWTElements.createText(parent, textStyle, verifyOnlyIntegerText, new Point(1, 1), ClientMessages.DARK_EXPOSURE_TP, null);

		ClientSWTElements.createLabel(parent, labelStyle, ClientMessages.NUM_FLAT);
		ClientSWTElements.createLabel(parent, labelStyle, ClientMessages.FLAT_EXPOSURE);
		numberFlat = ClientSWTElements.createText(parent, textStyle, verifyOnlyIntegerText, new Point(1, 1), ClientMessages.NUM_FLAT_TOOLTIP, null);
		flatExposure = ClientSWTElements.createText(parent, textStyle, verifyOnlyIntegerText, new Point(1, 1), ClientMessages.FLAT_EXPOSURE_TP, null);

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
		numberRepetitions = ClientSWTElements.createText(multipleScan, textStyle, verifyOnlyIntegerText, new Point(1, 2),
				ClientMessages.NUM_REPETITIONS_TOOLTIP, null);
		waitingTime = ClientSWTElements.createText(multipleScan, textStyle, verifyOnlyIntegerText, new Point(1, 2), ClientMessages.WAITING_TIME_TOOLTIP, null);
		repeateMultipleScansType = ClientSWTElements.createButton(multipleScan, SWT.RADIO, ClientMessages.REPEATE_SCAN, ClientMessages.REPEATE_SCAN_TOOLTIP);
		switchbackMultipleScansType = ClientSWTElements.createButton(multipleScan, SWT.RADIO, ClientMessages.SWITCHBACK_SCAN,
				ClientMessages.SWITCHBACK_SCAN_TOOLTIP);

		// --- this is a workaround to expand the bar ---//
		multipleScans.setSelection(getConfigurationData().getMultipleScans().isEnabled());

		expandBar(customBar, multipleScan, multipleScans);
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
			configureWhenShown();
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

	private void bindElements() {
		DataBindingContext dbc = new DataBindingContext();

		bindScanType(dbc);
		bindMultipleScanType(dbc);
		ClientBindingElements.bindText(dbc, name, String.class, "name", getController().getAcquisition());

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

		ClientBindingElements.bindCheckBox(dbc, multipleScans, "multipleScans.enabled", getConfigurationData());
		multipleScans.setSelection(multipleScans.getSelection());
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
		configureWhenShown();
		totalProjections.setText(Integer.toString(getScannableTrackDocument().getPoints()));

		forceFocusOnEmpty(numberDark, Integer.toString(getConfigurationData().getImageCalibration().getNumberDark()));
		forceFocusOnEmpty(numberFlat, Integer.toString(getConfigurationData().getImageCalibration().getNumberFlat()));
		forceFocusOnEmpty(numberRepetitions, Integer.toString(getConfigurationData().getMultipleScans().getNumberRepetitions()));
		forceFocusOnEmpty(waitingTime, Integer.toString(getConfigurationData().getMultipleScans().getWaitingTime()));
	}

	private void endGroupsListeners() {
		SelectionListener activateGroupListener = new SelectionAdapter() {
			@Override
			public void widgetSelected(SelectionEvent e) {
				Button source = Button.class.cast(e.getSource());

				if (source.getSelection()) {
					if (source.equals(halfRotationRangeType)) {
						customAngle.setText(Double.toString(180));
					} else if (source.equals(fullRotationRangeType)) {
						customAngle.setText(Double.toString(360));
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
		FocusListener customAngleListener = FocusListener.focusLostAdapter(c -> {
			updateCurrentAngularPosition();
		});
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

	private void configureWhenShown() {
		if (fullRotationRangeType.getSelection() || halfRotationRangeType.getSelection()) {
			customGroup.setEnabled(false);
		}
		if (customRotationRangeType.getSelection()) {
			fullRotationRangeType.setSelection(false);
			halfRotationRangeType.setSelection(false);
			customGroup.setEnabled(true);
		}
		updateAngularStep();
	}

	private double totalAngle() {
		double start = getScannableTrackDocument().getStart();
		double end = getScannableTrackDocument().getStop();
		return end - start;
	}

	private double calculateAngularStep() {
		return totalAngle() / getScannableTrackDocument().getPoints();
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

	private TomographyConfiguration getConfigurationData() {
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