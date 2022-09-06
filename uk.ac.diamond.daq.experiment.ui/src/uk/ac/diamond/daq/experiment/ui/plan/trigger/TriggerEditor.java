package uk.ac.diamond.daq.experiment.ui.plan.trigger;

import static uk.ac.diamond.daq.experiment.api.plan.TriggerDescriptor.EXECUTION_POLICY_PROPERTY;
import static uk.ac.diamond.daq.experiment.api.plan.TriggerDescriptor.INTERVAL_PROPERTY;
import static uk.ac.diamond.daq.experiment.api.plan.TriggerDescriptor.NAME_PROPERTY;
import static uk.ac.diamond.daq.experiment.api.plan.TriggerDescriptor.SCAN_PROPERTY;
import static uk.ac.diamond.daq.experiment.api.plan.TriggerDescriptor.SEV_PROPERTY;
import static uk.ac.diamond.daq.experiment.api.plan.TriggerDescriptor.SOURCE_PROPERTY;
import static uk.ac.diamond.daq.experiment.api.plan.TriggerDescriptor.TARGET_PROPERTY;
import static uk.ac.diamond.daq.experiment.api.plan.TriggerDescriptor.TOLERANCE_PROPERTY;
import static uk.ac.diamond.daq.experiment.ui.ExperimentUiUtils.STRETCH;
import static uk.ac.diamond.daq.experiment.ui.ExperimentUiUtils.addSpace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.stream.Collectors;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.UpdateValueStrategy;
import org.eclipse.core.databinding.beans.typed.BeanProperties;
import org.eclipse.core.databinding.conversion.IConverter;
import org.eclipse.core.databinding.observable.sideeffect.ISideEffect;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.SelectObservableValue;
import org.eclipse.jface.databinding.swt.typed.WidgetProperties;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.typed.ViewerProperties;
import org.eclipse.jface.fieldassist.AutoCompleteField;
import org.eclipse.jface.fieldassist.ComboContentAdapter;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.LabelProvider;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Event;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import uk.ac.diamond.daq.experiment.api.plan.TriggerDescriptor;
import uk.ac.diamond.daq.experiment.api.remote.ExecutionPolicy;
import uk.ac.diamond.daq.experiment.api.remote.SignalSource;
import uk.ac.diamond.daq.experiment.api.ui.EditableWithListWidget;
import uk.ac.diamond.daq.experiment.ui.plan.ScannableMotionNamesCombo;
import uk.ac.diamond.daq.experiment.ui.widget.ElementEditor;
import uk.ac.gda.client.exception.GDAClientRestException;
import uk.ac.gda.common.entity.Document;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.rest.ConfigurationsRestServiceClient;

/**
 * GUI for editing a {@link TriggerDescriptor}
 */
public class TriggerEditor implements ElementEditor {
	
	private static final Logger logger = LoggerFactory.getLogger(TriggerEditor.class);
	
	/**
	 * For a single, time-based trigger, no control for the tolerance is created.
	 * This value is quietly written in the model
	 */
	private static final double TIME_TOLERANCE = 0.1;
	
	// data
	private TriggerDescriptor model;
	
	// ui (static)
	private Composite composite;
	private Text nameText;
	private ComboViewer scanCombo;
	private Button sevSourceButton;
	private Button timeSourceButton;
	private Button oneShotButton;
	private Button periodicButton;

	// ui (dynamic)
	private Composite detailComposite;
	private ScannableMotionNamesCombo readoutsCombo;
	private Text interval;
	private Text target;
	private Text tolerance;
	
	// binding
	private final DataBindingContext dbc;
	private final List<Binding> mainBindings;
	private final List<Binding> detailBindings;
	private final List<ISideEffect> sideEffects;

	/** access via {@link #getService()} */
	private ConfigurationsRestServiceClient service;

	private Set<String> sevReadouts = Collections.emptySet();
	
	/**
	 * Instantiate with experiment service and experiment ID
	 * so that I can retrieve saved scans
	 */
	public TriggerEditor(String experimentId) {
		dbc = new DataBindingContext();
		mainBindings = new ArrayList<>();
		detailBindings = new ArrayList<>();
		sideEffects = new ArrayList<>();
	}

	@Override
	public void createControl(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(composite);
		GridLayoutFactory.swtDefaults().applyTo(composite);
		
		//////// NAME ////////
		
		new Label(composite, SWT.NONE).setText("Name");
		nameText = new Text(composite, SWT.BORDER);
		STRETCH.applyTo(nameText);
		
		addSpace(composite);
		
		//////// SCAN ////////
		
		new Label(composite, SWT.NONE).setText("Measurement");
		
		scanCombo = new ComboViewer(composite, SWT.DROP_DOWN);
		scanCombo.setContentProvider(ArrayContentProvider.getInstance());
		scanCombo.setLabelProvider(new LabelProvider() {
			@Override
			public String getText(Object element) {
				if (element instanceof Document) {
					return ((Document) element).getName();
				}
				return super.getText(element);
			}
		});
		
		var input = getScansList();
		scanCombo.setInput(input);
		
		var scanNames = input.stream().map(Document::getName).collect(Collectors.toList()).toArray(new String[0]);
		new AutoCompleteField(scanCombo.getControl(), new ComboContentAdapter(), scanNames);

		STRETCH.applyTo(scanCombo.getControl());
		
		addSpace(composite);
		
		Composite sourceAndMode = new Composite(composite, SWT.NONE);
		STRETCH.applyTo(sourceAndMode);
		GridLayoutFactory.swtDefaults().numColumns(2).equalWidth(true).spacing(20, SWT.DEFAULT).applyTo(sourceAndMode);
		
		//////// TRIGGER SOURCE ////////
		
		Group sourceGroup = new Group(sourceAndMode, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(sourceGroup);
		GridLayoutFactory.swtDefaults().applyTo(sourceGroup);
		sourceGroup.setText("Trigger source");
		
		sevSourceButton = new Button(sourceGroup, SWT.RADIO);
		sevSourceButton.setText("Scannable");
		
		timeSourceButton = new Button(sourceGroup, SWT.RADIO);
		timeSourceButton.setText("Time");
		
		toggleSourceSelection();
		
		//////// TRIGGER MODE ////////
		
		Group modeGroup = new Group(sourceAndMode, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(modeGroup);
		GridLayoutFactory.swtDefaults().applyTo(modeGroup);
		modeGroup.setText("Trigger mode");
		
		oneShotButton = new Button(modeGroup, SWT.RADIO);
		oneShotButton.setText("Single");
		
		periodicButton = new Button(modeGroup, SWT.RADIO);
		periodicButton.setText("Periodic");
		
		createDetailComposite();
		
		// slight hack... 
		STRETCH.copy().hint(SWT.DEFAULT, 100).applyTo(detailComposite);
		
		setEnabled(false);
	}
	
	private List<Document> getScansList() {
		try {
			return getService().getDocuments();
		} catch (GDAClientRestException e) {
			logger.error("Could not retrieve saved scans!", e);
			return Collections.emptyList();
		}
	}
	
	private ConfigurationsRestServiceClient getService() {
		if (service == null) {
			service = SpringApplicationContextFacade.getBean(ConfigurationsRestServiceClient.class);
		}
		return service;
	}
	
	@Override
	public void load(EditableWithListWidget element) {
		model = (TriggerDescriptor) element;
		createDetailComposite();
		updateBindings();
		setEnabled(true);
	}
	
	@Override
	public void clear() {
		removeOldBindings();
		setEnabled(false);
	}
	
	private void createDetailComposite() {
		if (detailComposite != null) {
			detailComposite.dispose();
			detailComposite = null;
		}
		detailComposite = new Composite(composite, SWT.NONE);
		STRETCH.applyTo(detailComposite);
		GridLayoutFactory.swtDefaults().applyTo(detailComposite);
		
		updateDetailControl();
	}

	private void updateDetailControl() {
		Composite controlComposite = new Composite(detailComposite, SWT.NONE);
		STRETCH.applyTo(controlComposite);
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(true).applyTo(controlComposite);
		
		if (model == null) return;
		
		removeOldDetailBindings();
		
		switch (model.getSignalSource()) {
		case POSITION:
			createPositionBasedTriggerControl(controlComposite);
			break;
			
		case TIME:
			createTimeBasedTriggerControl(controlComposite);
			break;
			
		default:
			throw new IllegalArgumentException("Unsupported signal source: '" + model.getSignalSource() + "'");
		}
		
		composite.getParent().layout(true, true);
	}

	private void createTimeBasedTriggerControl(Composite controlComposite) {
		switch (model.getExecutionPolicy()) {
		case REPEATING:
			new Label(controlComposite, SWT.NONE).setText("Period (s)");
			interval = new Text(controlComposite, SWT.BORDER);
			STRETCH.applyTo(interval);
			
			bindPeriod();
			break;
		
		case SINGLE:
			new Label(controlComposite, SWT.NONE).setText("Target (s)");
			target = new Text(controlComposite, SWT.BORDER);
			STRETCH.applyTo(target);
			
			bindTarget();
			
			model.setTolerance(TIME_TOLERANCE);
			break;
			
		default:
			throw new IllegalArgumentException("Unsupported execution policy: '" + model.getExecutionPolicy() + "'");
		}
	}

	private void createPositionBasedTriggerControl(Composite controlComposite) {
		
		new Label(controlComposite, SWT.NONE).setText("Scannable");
		readoutsCombo = new ScannableMotionNamesCombo(controlComposite);
		readoutsCombo.setPriorityItems(sevReadouts);
		bindSev();
		
		STRETCH.applyTo(readoutsCombo.getControl());
		
		switch (model.getExecutionPolicy()) {
		case REPEATING:
			new Label(controlComposite, SWT.NONE).setText("Interval");
			interval = new Text(controlComposite, SWT.BORDER);
			STRETCH.applyTo(interval);
			
			bindPeriod();
			break;
			
		case SINGLE:
			new Label(controlComposite, SWT.NONE).setText("Target");
			target = new Text(controlComposite, SWT.BORDER);
			STRETCH.applyTo(target);
			
			bindTarget();
			
			new Label(controlComposite, SWT.NONE).setText("Tolerance");
			tolerance = new Text(controlComposite, SWT.BORDER);
			STRETCH.applyTo(tolerance);
			
			bindTolerance();
			break;
			
		default:
			throw new IllegalArgumentException("Unsupported execution policy: '" + model.getExecutionPolicy() + "'");			
		}
	}
	
	private void bindTarget() {
		IObservableValue<String> targetInText = WidgetProperties.text(SWT.Modify).observe(target);
		IObservableValue<Double> targetInModel = BeanProperties.value(TARGET_PROPERTY, double.class).observe(model);
		
		Binding targetBinding = dbc.bindValue(targetInText, targetInModel);
		detailBindings.add(targetBinding);
	}
	
	private void bindPeriod() {
		IObservableValue<String> intervalText = WidgetProperties.text(SWT.Modify).observe(interval);
		IObservableValue<Double> intervalInModel = BeanProperties.value(INTERVAL_PROPERTY, double.class).observe(model);
		
		Binding intervalBinding = dbc.bindValue(intervalText, intervalInModel);
		detailBindings.add(intervalBinding);
	}
	
	private void bindTolerance() {
		IObservableValue<String> inWidget = WidgetProperties.text(SWT.Modify).observe(tolerance);
		IObservableValue<Double> inModel = BeanProperties.value(TOLERANCE_PROPERTY, double.class).observe(model);
		
		Binding toleranceBinding = dbc.bindValue(inWidget, inModel);
		detailBindings.add(toleranceBinding);
	}
	
	private void bindSev() {
		IViewerObservableValue<String> inWidget = ViewerProperties.singleSelection(String.class).observe(readoutsCombo);
		IObservableValue<String> inModel = BeanProperties.value(SEV_PROPERTY, String.class).observe(model);
		
		Binding sevBinding = dbc.bindValue(inWidget, inModel);
		detailBindings.add(sevBinding);
		
		// if there is no sev in model, let's select first option
		if (model.getSampleEnvironmentVariableName() == null) {
			readoutsCombo.setSelection(new StructuredSelection(readoutsCombo.getElementAt(0)), true);
		}
	}
	
	private UUID documentToId(Document doc) {
		if (doc == null) return null;
		return doc.getUuid();
	}
	
	private Document idToDocument(UUID id) {
		if (id == null) return null;
		try {
			return getService().getDocument(id.toString());
		} catch (GDAClientRestException e) {
			logger.error("Error retrieving scan with id '{}'", id);
			return null;
		}
	}

	private void updateBindings() {
		removeOldBindings();
		
		// name
		IObservableValue<String> nameTextObservable = WidgetProperties.text(SWT.Modify).observe(nameText);
		IObservableValue<String> nameInModelObservable = BeanProperties.value(NAME_PROPERTY, String.class).observe(model);
		
		Binding nameBinding = dbc.bindValue(nameTextObservable, nameInModelObservable);
		mainBindings.add(nameBinding);
		
		// scan
		var scanInWidget = ViewerProperties.singleSelection(Document.class).observe(scanCombo);
		var scanInModel = BeanProperties.value(SCAN_PROPERTY, UUID.class).observe(model);
		
		var docToIdConverter = IConverter.create(Document.class, UUID.class, document -> documentToId((Document) document));
		var idToDocConverter = IConverter.create(UUID.class, Document.class, id -> idToDocument((UUID) id));
		
		Binding scanBinding = dbc.bindValue(scanInWidget, scanInModel, UpdateValueStrategy.create(docToIdConverter), UpdateValueStrategy.create(idToDocConverter));
		
		mainBindings.add(scanBinding);
		
		// trigger source
		IObservableValue<SignalSource> sourceInModelObservable = BeanProperties.value(SOURCE_PROPERTY, SignalSource.class).observe(model);
		
		SelectObservableValue<SignalSource> sourceSelection = new SelectObservableValue<>();
		sourceSelection.addOption(SignalSource.POSITION, WidgetProperties.buttonSelection().observe(sevSourceButton));
		sourceSelection.addOption(SignalSource.TIME, WidgetProperties.buttonSelection().observe(timeSourceButton));
		
		Binding sourceBinding = dbc.bindValue(sourceSelection, sourceInModelObservable);
		mainBindings.add(sourceBinding);
		
		// trigger mode
		IObservableValue<ExecutionPolicy> modeInModelObservable = BeanProperties.value(EXECUTION_POLICY_PROPERTY, ExecutionPolicy.class).observe(model);
		
		SelectObservableValue<ExecutionPolicy> modeSelection = new SelectObservableValue<>();
		modeSelection.addOption(ExecutionPolicy.SINGLE, WidgetProperties.buttonSelection().observe(oneShotButton));
		modeSelection.addOption(ExecutionPolicy.REPEATING, WidgetProperties.buttonSelection().observe(periodicButton));
		
		Binding modeBinding = dbc.bindValue(modeSelection, modeInModelObservable);
		mainBindings.add(modeBinding);
		
		sideEffects.add(ISideEffect.create(sourceSelection::getValue, source -> createDetailComposite()));
		sideEffects.add(ISideEffect.create(modeSelection::getValue, mode -> createDetailComposite()));
	}

	private void removeOldBindings() {
		mainBindings.forEach(binding -> {
			dbc.removeBinding(binding);
			binding.dispose();
		});
		
		mainBindings.clear();
		
		sideEffects.forEach(ISideEffect::dispose);
		
		sideEffects.clear();
	}

	private void removeOldDetailBindings() {
		detailBindings.forEach(binding -> {
			dbc.removeBinding(binding);
			binding.dispose();
		});
		
		detailBindings.clear();
	}
	
	private void toggleSourceSelection() {
		if (sevSourceButton.getSelection()) {
			sevSourceButton.setSelection(false);
			timeSourceButton.setSelection(true);
			sevSourceButton.notifyListeners(SWT.Selection, new Event());
			timeSourceButton.notifyListeners(SWT.Selection, new Event());
		}
	}
	
	private void setEnabled(boolean enabled) {
		nameText.setEnabled(enabled);
		scanCombo.getControl().setEnabled(enabled);
		sevSourceButton.setEnabled(enabled);
		timeSourceButton.setEnabled(enabled);
		oneShotButton.setEnabled(enabled);
		periodicButton.setEnabled(enabled);
		
		List<Control> controls = new ArrayList<>(Arrays.asList(target, tolerance, interval));
		if (readoutsCombo != null) controls.add(readoutsCombo.getControl());
		controls.stream()
			.filter(Objects::nonNull).filter(control -> !control.isDisposed())
			.forEach(control -> control.setEnabled(enabled));
		
		if (enabled) {
			toggleSourceSelection();
		}
	}

	void setExperimentDriverReadouts(Set<String> readouts) {
		if (readoutsCombo != null) {
			readoutsCombo.setPriorityItems(readouts);
		} else {
			sevReadouts = readouts;
		}
	}

}
