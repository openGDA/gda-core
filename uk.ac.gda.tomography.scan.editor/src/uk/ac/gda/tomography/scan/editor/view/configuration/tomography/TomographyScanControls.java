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
import static org.eclipse.swt.events.SelectionListener.widgetSelectedAdapter;
import static uk.ac.gda.core.tool.spring.SpringApplicationContextFacade.getBean;
import static uk.ac.gda.ui.tool.ClientMessages.ANGULAR_STEP;
import static uk.ac.gda.ui.tool.ClientMessages.CONFIGURATION_LAYOUT_ERROR;
import static uk.ac.gda.ui.tool.ClientMessages.CUSTOM_END_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessages.EMPTY_MESSAGE;
import static uk.ac.gda.ui.tool.ClientMessages.FINAL_ANGLE;
import static uk.ac.gda.ui.tool.ClientMessages.FLY_SCAN;
import static uk.ac.gda.ui.tool.ClientMessages.FLY_SCAN_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessages.FULL_ANGLE;
import static uk.ac.gda.ui.tool.ClientMessages.FULL_ANGLE_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessages.MULTIPLE_SCANS;
import static uk.ac.gda.ui.tool.ClientMessages.NAME;
import static uk.ac.gda.ui.tool.ClientMessages.NAME_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessages.NUM_REPETITIONS;
import static uk.ac.gda.ui.tool.ClientMessages.NUM_REPETITIONS_TOOLTIP;
import static uk.ac.gda.ui.tool.ClientMessages.PROCESS_REQUESTS;
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
import static uk.ac.gda.ui.tool.GUIComponents.doubleContent;
import static uk.ac.gda.ui.tool.GUIComponents.integerPositiveContent;
import static uk.ac.gda.ui.tool.GUIComponents.labelComponent;
import static uk.ac.gda.ui.tool.GUIComponents.labelledLabelContent;
import static uk.ac.gda.ui.tool.GUIComponents.radioComponent;
import static uk.ac.gda.ui.tool.GUIComponents.textContent;
import static uk.ac.gda.ui.tool.WidgetUtilities.selectAndNotify;

import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Optional;

import org.apache.commons.math3.util.Precision;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.typed.PojoProperties;
import org.eclipse.core.databinding.observable.IChangeListener;
import org.eclipse.core.databinding.observable.value.SelectObservableValue;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.springframework.context.ApplicationListener;

import gda.mscan.element.Mutator;
import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.mapping.api.document.event.ScanningAcquisitionChangeEvent;
import uk.ac.diamond.daq.mapping.api.document.helper.ScanpathDocumentHelper;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningConfiguration;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.diamond.daq.mapping.ui.controller.StageController;
import uk.ac.diamond.daq.mapping.ui.stage.enumeration.StageDevice;
import uk.ac.gda.api.acquisition.AcquisitionPropertyType;
import uk.ac.gda.api.acquisition.configuration.MultipleScansType;
import uk.ac.gda.client.UIHelper;
import uk.ac.gda.client.properties.acquisition.AcquisitionConfigurationProperties;
import uk.ac.gda.client.properties.acquisition.processing.ProcessingRequestProperties;
import uk.ac.gda.core.tool.spring.AcquisitionFileContext;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.core.tool.spring.TomographyContextFile;
import uk.ac.gda.tomography.scan.editor.view.ScanType;
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
public class TomographyScanControls implements CompositeFactory, Reloadable {

	/** Scan prefix **/
	private Text name;

	/** Scan type **/
	private Button flyScanType;
	private Button stepScanType;

	private Text startAngleText;
	private Text customAngleText;

	private Label endAngleLabel;
	private Label angularStepLabel;

	private Button halfRotationRangeButton;
	private Button fullRotationRangeButton;
	private Button customRotationRangeButton;
	private Button currentAngleButton;

	/** The MultipleScans Composite elements **/
	private Text numberRepetitions;
	private Text waitingTime;
	private Button repeatMultipleScansType;
	private Button switchbackMultipleScansType;

	private ScanpathDocumentHelper dataHelper;

	private DataBindingContext bindingContext = new DataBindingContext();

	private List<Reloadable> reloadables = new ArrayList<>();
	private ProjectionsCompositeFactory projections;

	private static final double HALF_ROTATION_RANGE = 180.0;
	private static final double FULL_ROTATION_RANGE = 360.0;
	private static final double ANGULAR_TOLERANCE = 1e-6;

	public TomographyScanControls() {
		try {
			this.dataHelper = new ScanpathDocumentHelper(this::getScanningParameters);
		} catch (NoSuchElementException e) {
			UIHelper.showWarning(CONFIGURATION_LAYOUT_ERROR, e);
		}
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		Composite mainComposite = createClientCompositeWithGridLayout(parent, SWT.NONE, 1);
		createClientGridDataFactory().align(SWT.FILL, SWT.BEGINNING).grab(true, true).applyTo(mainComposite);
		standardMarginHeight(mainComposite.getLayout());
		standardMarginWidth(mainComposite.getLayout());

		createElements(mainComposite, SWT.NONE, SWT.BORDER);
		bindElements();
		var updateListener = new UpdateListener();
		SpringApplicationContextFacade.addApplicationListener(updateListener);
		mainComposite.addDisposeListener(dispose -> SpringApplicationContextFacade.removeApplicationListener(updateListener));
		return mainComposite;
	}

	private void createElements(Composite parent, int labelStyle, int textStyle) {
		this.name = textContent(parent, labelStyle, textStyle,
				NAME, NAME_TOOLTIP);
		// guarantee that fills the horizontal space
		createClientGridDataFactory().grab(true, true).applyTo(name);

		scanTypeContent(parent);
		createRangeGroup(parent, labelStyle, textStyle);

		projections = new ProjectionsCompositeFactory();
		reloadables.add(projections);
		projections.createComposite(parent, textStyle);

		var exposure = new ExposureCompositeFactory();
		reloadables.add(exposure);
		exposure.createComposite(parent, textStyle);

		var darkFlat = new DarkFlatCompositeFactory();
		reloadables.add(darkFlat);
		darkFlat.createComposite(parent, textStyle);

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

		this.endAngleLabel = labelledLabelContent(rightCompositeContent, labelStyle,
				FINAL_ANGLE, EMPTY_MESSAGE);
		this.angularStepLabel = labelledLabelContent(rightCompositeContent, labelStyle,
				ANGULAR_STEP, EMPTY_MESSAGE);
	}

	private void startAngleContent(Composite parent, int labelStyle, int textStyle) {
		var mainCompositeContent = createClientCompositeWithGridLayout(parent, SWT.NONE, 2);
		createClientGridDataFactory().grab(true, true).applyTo(mainCompositeContent);

		this.startAngleText = doubleContent(mainCompositeContent, labelStyle, textStyle,
				START, START_ANGLE_TOOLTIP);
		this.currentAngleButton = new Button(mainCompositeContent, SWT.PUSH);
		currentAngleButton.setText("Get current angle");
	}

	private void endAngleContent(Composite parent, int labelStyle, int textStyle) {
		var mainCompositeContent = createClientCompositeWithGridLayout(parent, SWT.NONE, 5);
		createClientGridDataFactory().grab(true, true).applyTo(mainCompositeContent);

		labelComponent(mainCompositeContent, labelStyle, RANGE);
		this.halfRotationRangeButton = radioComponent(mainCompositeContent,
				STRAIGHT_ANGLE, STRAIGHT_ANGLE_TOOLTIP);
		this.fullRotationRangeButton = radioComponent(mainCompositeContent,
				FULL_ANGLE, FULL_ANGLE_TOOLTIP);
		this.customRotationRangeButton = radioComponent(mainCompositeContent,
				EMPTY_MESSAGE, CUSTOM_END_TOOLTIP);
		this.customAngleText = doubleContent(mainCompositeContent, labelStyle, textStyle,
				EMPTY_MESSAGE, CUSTOM_END_TOOLTIP);
	}

	private void multipleScansContent(Composite parent, int labelStyle, int textStyle) {
		var group = createClientGroup(parent, SWT.NONE, 2, MULTIPLE_SCANS);
		createClientGridDataFactory().applyTo(group);

		this.numberRepetitions = integerPositiveContent(group, labelStyle, textStyle,
				NUM_REPETITIONS, NUM_REPETITIONS_TOOLTIP);
		this.waitingTime = integerPositiveContent(group, labelStyle, textStyle,
				WAITING_TIME, WAITING_TIME_TOOLTIP);

		this.repeatMultipleScansType = radioComponent(group,
				REPEATE_SCAN, REPEATE_SCAN_TOOLTIP);
		this.repeatMultipleScansType.setData(MultipleScansType.REPEAT_SCAN);

		this.switchbackMultipleScansType = radioComponent(group,
				SWITCHBACK_SCAN, SWITCHBACK_SCAN_TOOLTIP);
		this.switchbackMultipleScansType.setData(MultipleScansType.SWITCHBACK_SCAN);
	}

	@SuppressWarnings({ "unchecked", "rawtypes" })
	private List<ProcessingRequestContext<?>> getProcessingRequestContext() {
		List<ProcessingRequestContext<?>> availableProcessingOptions = new ArrayList<>();

		// nexus template
		getDefaultNexusTemplatesProcessingFile().ifPresent(defaults ->
			availableProcessingOptions.add(new ProcessingRequestContext(getProcessingRequestKeyFactory().getProcessingKey(ProcessKey.NEXUS_TEMPLATE),
					getNexusTemplatesProcessingDirectory(), defaults, true)));

		// savu
		availableProcessingOptions.add(new ProcessingRequestContext(getProcessingRequestKeyFactory().getProcessingKey(ProcessKey.SAVU),
				getSavuProcessingFileDirectory(), getSavuDefaultProcessingFile(), false));

		return availableProcessingOptions;
	}

	private void createProcessRequestGroup(Composite parent, int labelStyle) {
		var group = createClientGroup(parent, SWT.NONE, 3, PROCESS_REQUESTS);
		createClientGridDataFactory().applyTo(group);
		var processingComposite = new ProcessingRequestComposite(getProcessingRequestContext());
		processingComposite.createComposite(group, labelStyle);
		reloadables.add(processingComposite);
	}

	private void disposeBindings() {
		new ArrayList<>(bindingContext.getBindings()).forEach(binding -> {
			bindingContext.removeBinding(binding);
			binding.dispose();
		});
	}

	private void bindElements() {
		bindName();
		bindScanType();
		bindMultipleScanType();
		bindAngularRange();
	}

	private void bindName() {
		var nameUi = WidgetProperties.text(SWT.Modify).observe(name);
		var nameModel = PojoProperties.value("name", String.class).observe(getScanningAcquisition());
		bindingContext.bindValue(nameUi, nameModel);
	}

	/**
	 * scan type being either continuous (a.k.a. fly scan) or step scan
	 */
	private void bindScanType() {
		// simple selection listeners to add/remove Mutator.CONTINUOUS from model
		flyScanType.addSelectionListener(widgetSelectedAdapter(selection -> dataHelper.addMutators(Mutator.CONTINUOUS, new ArrayList<>())));
		stepScanType.addSelectionListener(widgetSelectedAdapter(selection -> dataHelper.removeMutators(Mutator.CONTINUOUS)));

		// manually initialise state from model
		var model = getScanningParameters().getScanpathDocument();
		var flyScan = model.getMutators().containsKey(Mutator.CONTINUOUS);
		selectAndNotify(stepScanType, !flyScan);
		selectAndNotify(flyScanType, flyScan);
	}

	private void bindMultipleScanType() {
		var model = getAcquisitionConfiguration().getMultipleScans();

		var repsUi = WidgetProperties.text(SWT.Modify).observe(numberRepetitions);
		var repsModel = PojoProperties.value("numberRepetitions", Integer.class).observe(model);
		bindingContext.bindValue(repsUi, repsModel);

		var delayUi = WidgetProperties.text(SWT.Modify).observe(waitingTime);
		var delayModel = PojoProperties.value("waitingTime", Integer.class).observe(model);
		bindingContext.bindValue(delayUi, delayModel);

		var repetitionType = new SelectObservableValue<>(MultipleScansType.class);
		repetitionType.addOption(MultipleScansType.REPEAT_SCAN, WidgetProperties.buttonSelection().observe(repeatMultipleScansType));
		repetitionType.addOption(MultipleScansType.SWITCHBACK_SCAN, WidgetProperties.buttonSelection().observe(switchbackMultipleScansType));
		var typeInModel = PojoProperties.value("multipleScansType", MultipleScansType.class).observe(model);
		bindingContext.bindValue(repetitionType, typeInModel);
	}

	private void bindAngularRange() {
		/* a change listener attached to 'start' and 'custom range' text boxes
		 * will trigger method to recalculate start and stop, update model, update labels */
		var startUi = WidgetProperties.text(SWT.Modify).observe(startAngleText);
		var customUi = WidgetProperties.text(SWT.Modify).observe(customAngleText);
		IChangeListener changeListener = event -> {
			updateAnglesInModel();
			updateAngularStepLabel();
		};
		startUi.addChangeListener(changeListener);
		customUi.addChangeListener(changeListener);

		/* simple selection listener to copy current rotation stage position as start angle */
		currentAngleButton.addSelectionListener(widgetSelectedAdapter(e-> copyCurrentRotationToStartAngle()));

		/*
		 * selection listeners on range radio buttons populate 'custom range' box,
		 * and enable/disable the widget */
		halfRotationRangeButton.addSelectionListener(widgetSelectedAdapter(selection -> {
			customAngleText.setText(String.valueOf(HALF_ROTATION_RANGE));
			customAngleText.setEnabled(false);
		}));

		fullRotationRangeButton.addSelectionListener(widgetSelectedAdapter(selection -> {
			customAngleText.setText(String.valueOf(FULL_ROTATION_RANGE));
			customAngleText.setEnabled(false);
		}));

		customRotationRangeButton.addSelectionListener(widgetSelectedAdapter(selection ->
			customAngleText.setEnabled(true)));

		/* manual initialisation of state from model */
		var document = getScannableTrackDocument();
		var start = document.getStart();
		var stop = document.getStop();
		var range = stop - start;
		startAngleText.setText(String.valueOf(start));
		if (Precision.equals(range, HALF_ROTATION_RANGE, ANGULAR_TOLERANCE)) {
			selectAndNotify(halfRotationRangeButton, true);
			selectAndNotify(fullRotationRangeButton, false);
			selectAndNotify(customRotationRangeButton, false);
		} else if (Precision.equals(range, FULL_ROTATION_RANGE, ANGULAR_TOLERANCE)) {
			selectAndNotify(halfRotationRangeButton, false);
			selectAndNotify(fullRotationRangeButton, true);
			selectAndNotify(customRotationRangeButton, false);
		} else {
			selectAndNotify(halfRotationRangeButton, false);
			selectAndNotify(fullRotationRangeButton, false);
			selectAndNotify(customRotationRangeButton, true);
		}
		customAngleText.setText(String.valueOf(range));
		customAngleText.setEnabled(customRotationRangeButton.getSelection());
	}

	private void updateAnglesInModel() {
		if (startAngleText.getText().isEmpty() || customAngleText.getText().isEmpty()) {
			// we'll be back soon enough
			return;
		}
		var start = Double.parseDouble(startAngleText.getText());
		var stop = start + Double.parseDouble(customAngleText.getText());
		var document = getScannableTrackDocument();
		document.setStart(start);
		document.setStop(stop);
		endAngleLabel.setText(String.valueOf(stop));
	}

	private void copyCurrentRotationToStartAngle() {
		double currentMotorPosition = getBean(StageController.class).getMotorPosition(StageDevice.MOTOR_STAGE_ROT_Y);
		startAngleText.setText(Double.toString(currentMotorPosition));
	}

	private void updateAngularStepLabel() {
		double angularStep = getAngularStep();
		angularStepLabel.setText(Double.toString(angularStep));
	}

	private double getAngularStep() {
		return Math.abs(getRange() / getScannableTrackDocument().getPoints());
	}

	private double getRange() {
		return getScannableTrackDocument().getStop() - getScannableTrackDocument().getStart();
	}

	@Override
	public void reload() {
		disposeBindings();
		bindElements();
		reloadables.forEach(Reloadable::reload);
	}

	private class UpdateListener implements ApplicationListener<ScanningAcquisitionChangeEvent> {
		@Override
		public void onApplicationEvent(ScanningAcquisitionChangeEvent event) {
			if ((event.getSource() == projections) /* && !mainComposite.isDisposed() */) {
				updateAngularStepLabel();
			}
		}
	}

	private ScanningConfiguration getAcquisitionConfiguration() {
		return getScanningAcquisitionTemporaryHelper()
			.getAcquisitionConfiguration()
			.orElseThrow();
	}

	private ScannableTrackDocument getScannableTrackDocument() {
		var tracks = getScanningAcquisitionTemporaryHelper().getScannableTrackDocuments();

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

	private Optional<List<URL>> getDefaultNexusTemplatesProcessingFile() {
		return SpringApplicationContextFacade.getBean(ClientSpringProperties.class).getAcquisitions().stream()
				.filter(acquistition -> acquistition.getType().equals(AcquisitionPropertyType.TOMOGRAPHY))
				.findFirst()
				.map(AcquisitionConfigurationProperties::getProcessingRequest)
				.map(ProcessingRequestProperties::getNexusTemplates);
	}

	private ProcessingRequestKeyFactory getProcessingRequestKeyFactory() {
		return SpringApplicationContextFacade.getBean(ProcessingRequestKeyFactory.class);
	}

	private ScanningParameters getScanningParameters() {
		return getScanningAcquisitionTemporaryHelper()
				.getScanningParameters()
				.orElseThrow();
	}

	private ScanningAcquisition getScanningAcquisition() {
		return getScanningAcquisitionTemporaryHelper()
				.getScanningAcquisition().orElseThrow();
	}

	private ScanningAcquisitionTemporaryHelper getScanningAcquisitionTemporaryHelper() {
		return SpringApplicationContextFacade.getBean(ScanningAcquisitionTemporaryHelper.class);
	}
}