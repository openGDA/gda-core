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
import static uk.ac.gda.ui.tool.ClientSWTElements.STRETCH;
import static uk.ac.gda.ui.tool.ClientSWTElements.composite;
import static uk.ac.gda.ui.tool.ClientSWTElements.innerComposite;
import static uk.ac.gda.ui.tool.ClientSWTElements.label;
import static uk.ac.gda.ui.tool.ClientSWTElements.numericTextBox;
import static uk.ac.gda.ui.tool.ClientSWTElements.spinner;
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
import org.eclipse.swt.widgets.Spinner;
import org.eclipse.swt.widgets.Text;

import gda.factory.Finder;
import gda.mscan.element.Mutator;
import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.mapping.api.document.helper.ScanpathDocumentHelper;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningAcquisition;
import uk.ac.diamond.daq.mapping.api.document.scanning.ScanningParameters;
import uk.ac.diamond.daq.mapping.api.document.scanpath.ScannableTrackDocument;
import uk.ac.diamond.daq.mapping.ui.stage.IStageController;
import uk.ac.diamond.daq.mapping.ui.stage.enumeration.StageDevice;
import uk.ac.gda.api.acquisition.AcquisitionPropertyType;
import uk.ac.gda.client.properties.acquisition.AcquisitionConfigurationProperties;
import uk.ac.gda.client.properties.acquisition.processing.ProcessingRequestProperties;
import uk.ac.gda.core.tool.spring.AcquisitionFileContext;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.core.tool.spring.TomographyContextFile;
import uk.ac.gda.tomography.scan.editor.view.configuration.TomographyConfiguration;
import uk.ac.gda.ui.tool.ClientSWTElements;
import uk.ac.gda.ui.tool.Reloadable;
import uk.ac.gda.ui.tool.document.ScanningAcquisitionTemporaryHelper;
import uk.ac.gda.ui.tool.images.ClientImages;
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

	private Text name;

	private Button flyScanType;
	private Button stepScanType;

	private Text startAngle;
	private Text endAngle;

	private Button halfRotation;
	private Button fullRotation;
	private Button customRotation;

	private final ScanpathDocumentHelper dataHelper;
	private final TomographyConfiguration config;

	private DataBindingContext bindingContext = new DataBindingContext();

	private List<Reloadable> reloadables = new ArrayList<>();
	private Text stepSize;

	private Spinner projections;

	private static final double HALF_ROTATION_RANGE = 180.0;
	private static final double FULL_ROTATION_RANGE = 360.0;
	private static final double ANGULAR_TOLERANCE = 1e-6;

	public TomographyScanControls() {
		config = Finder.findLocalSingleton(TomographyConfiguration.class);
		dataHelper = new ScanpathDocumentHelper(this::getScanningParameters);
	}

	@Override
	public Composite createComposite(Composite parent, int style) {

		var composite = composite(parent, 1);
		STRETCH.copy().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(composite);

		createNameControl(composite);
		scanTypeContent(composite);
		createAngularControls(composite);
		createDetectorControls(composite);
		createDarksAndFlatsControls(composite);
		createPositionControls(composite);
		createProcessingControls(composite);

		bindControls();

		return composite;
	}

	private void createNameControl(Composite parent) {
		var composite = composite(parent, 2);
		label(composite, "Acquisition name");
		name = new Text(composite, SWT.BORDER);
		STRETCH.applyTo(name);
	}

	private void createAngularControls(Composite parent) {
		var startStop = composite(parent, 2);

		label(startStop, "Start angle");
		label(startStop, "End angle");

		var startControls = innerComposite(startStop, 2, false);

		startAngle = numericTextBox(startControls);
		var currentAngle = new Button(startControls, SWT.PUSH);
		var img = ClientSWTElements.getImage(ClientImages.POSITION_PIN);
		currentAngle.setToolTipText("Grab current position");
		currentAngle.setImage(img);
		currentAngle.addDisposeListener(dispose -> img.dispose());
		currentAngle.addListener(SWT.Selection,event -> copyCurrentThetaPosition());

		endAngle = numericTextBox(startStop);

		var range = composite(parent, 4);

		label(range, "Angular range");

		halfRotation = new Button(range, SWT.RADIO);
		halfRotation.setText("180°");
		STRETCH.applyTo(halfRotation);

		fullRotation = new Button(range, SWT.RADIO);
		fullRotation.setText("360°");
		STRETCH.applyTo(fullRotation);

		customRotation = new Button(range, SWT.RADIO);
		customRotation.setText("Custom range");
		STRETCH.applyTo(customRotation);

		var points = composite(parent, 2);

		label(points, "Projections");
		label(points, "Step size");

		projections = spinner(points);

		stepSize = new Text(points, SWT.BORDER);
		stepSize.setEnabled(false);
		STRETCH.applyTo(stepSize);
	}

	private void copyCurrentThetaPosition() {
		double currentMotorPosition = getStageController().getMotorPosition(StageDevice.MOTOR_STAGE_ROT_Y);
		startAngle.setText(Double.toString(currentMotorPosition));
	}

	private IStageController getStageController() {
		return SpringApplicationContextFacade.getBean(IStageController.class);
	}

	private void createDetectorControls(Composite parent) {
		var exposure = composite(parent, 2);

		label(exposure, "Detector exposure (s)");
		var exposureControl = new ExposureCompositeFactory();
		exposureControl.createComposite(exposure, SWT.NONE);
		reloadables.add(exposureControl);
	}

	private void createDarksAndFlatsControls(Composite parent) {
		var darkAndFlats = new DarkFlatCompositeFactory();
		darkAndFlats.createComposite(parent, SWT.NONE);
		reloadables.add(darkAndFlats);
	}

	private void bindControls() {
		bindName();
		bindScanType();
		bindAngleControls();
	}

	private void scanTypeContent(Composite parent) {
		var composite = composite(parent, 2);
		label(composite, ""); // just a space in the grid
		var buttons = composite(composite, 2);
		flyScanType = new Button(buttons, SWT.RADIO);
		flyScanType.setText("Continuous");
		STRETCH.applyTo(flyScanType);

		stepScanType = new Button(buttons, SWT.RADIO);
		stepScanType.setText("Step");
		STRETCH.applyTo(stepScanType);
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

	private void createPositionControls(Composite parent) {

		var position = new InAndOutOfBeamPositionControls(config);
		position.createControls(parent);
		reloadables.add(position);
	}

	private void createProcessingControls(Composite parent) {
		var composite = composite(parent, 1);
		label(composite, "Processing");
		var processingComposite = new ProcessingRequestComposite(getProcessingRequestContext());
		processingComposite.createComposite(composite, SWT.NONE);
		reloadables.add(processingComposite);
	}

	private void disposeBindings() {
		new ArrayList<>(bindingContext.getBindings()).forEach(binding -> {
			bindingContext.removeBinding(binding);
			binding.dispose();
		});
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

	private SelectObservableValue<AngularRange> rangeSelection;

	private enum AngularRange {

		HALF_ROTATION(180.0),
		FULL_ROTATION(360.0),
		CUSTOM_ROTATION(Double.NaN);

		private Double range;

		AngularRange(Double range) {
			this.range = range;
		}

		public Double getRange() {
			return range;
		}

	}

	private void bindAngleControls() {

		// manual initialisation of UI from model (easier to set these values before adding listeners)
		var model = getScannableTrackDocument();

		startAngle.setText(String.valueOf(model.getStart()));
		endAngle.setText(String.valueOf(model.getStop()));
		projections.setSelection(model.getPoints());

		// create widget observables
		var startObservable = WidgetProperties.text(SWT.Modify).observe(startAngle);
		var endObservable = WidgetProperties.text(SWT.Modify).observe(endAngle);
		var projectionsObservable = WidgetProperties.spinnerSelection().observe(projections);

		rangeSelection = new SelectObservableValue<>();
		rangeSelection.addOption(AngularRange.HALF_ROTATION, WidgetProperties.buttonSelection().observe(halfRotation));
		rangeSelection.addOption(AngularRange.FULL_ROTATION, WidgetProperties.buttonSelection().observe(fullRotation));
		rangeSelection.addOption(AngularRange.CUSTOM_ROTATION, WidgetProperties.buttonSelection().observe(customRotation));

		// add change listeners
		IChangeListener updateAnglesInModelThenStepSize = event -> {
			updateAnglesInModel();
			updateAngularStepLabel();
		};

		startObservable.addChangeListener(event -> {
			if (rangeSelection.getValue() == AngularRange.CUSTOM_ROTATION) {
				updateAnglesInModel();
				updateAngularStepLabel();
			} else {
				updateStop();
			}
		});

		endObservable.addChangeListener(updateAnglesInModelThenStepSize);

		projectionsObservable.addChangeListener(updateAnglesInModelThenStepSize);

		rangeSelection.addChangeListener(event -> updateStop());


		// continue manual initialisation of UI from model
		var range = getRange();
		if (Precision.equals(range, HALF_ROTATION_RANGE, ANGULAR_TOLERANCE)) {
			selectAndNotify(halfRotation, true);
			selectAndNotify(fullRotation, false);
			selectAndNotify(customRotation, false);
		} else if (Precision.equals(range, FULL_ROTATION_RANGE, ANGULAR_TOLERANCE)) {
			selectAndNotify(halfRotation, false);
			selectAndNotify(fullRotation, true);
			selectAndNotify(customRotation, false);
		} else {
			selectAndNotify(halfRotation, false);
			selectAndNotify(fullRotation, false);
			selectAndNotify(customRotation, true);
		}

		updateAngularStepLabel();
	}

	/**
	 * Populate the end angle box based on start angle and angular range selection
	 * <p>
	 * Change listeners attached to end angle box are triggered
	 * if value changed in this method!
	 */
	private void updateStop() {
		var start = Double.parseDouble(startAngle.getText());
		var selection = rangeSelection.getValue();
		if (selection == null) return;
		switch (selection) {
		case CUSTOM_ROTATION:
			endAngle.setEnabled(true);
			break;
		case FULL_ROTATION:
		case HALF_ROTATION:
			endAngle.setEnabled(false);
			endAngle.setText(String.valueOf(start + selection.getRange()));
			break;
		}
	}

	private void updateAnglesInModel() {
		var model = getScannableTrackDocument();
		model.setStart(Double.parseDouble(startAngle.getText()));
		model.setStop(Double.parseDouble(endAngle.getText()));
		model.setPoints(projections.getSelection());
	}

	private void updateAngularStepLabel() {
		double angularStep = getAngularStep();
		stepSize.setText(Double.toString(angularStep));
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
		bindControls();
		reloadables.forEach(Reloadable::reload);
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