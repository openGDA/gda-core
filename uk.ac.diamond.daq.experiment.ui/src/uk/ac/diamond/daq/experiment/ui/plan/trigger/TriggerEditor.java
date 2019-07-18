package uk.ac.diamond.daq.experiment.ui.plan.trigger;

import static uk.ac.diamond.daq.experiment.api.Services.getExperimentService;
import static uk.ac.diamond.daq.experiment.ui.ExperimentUiUtils.STRETCH;
import static uk.ac.diamond.daq.experiment.ui.ExperimentUiUtils.addSpace;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Set;

import org.eclipse.core.databinding.Binding;
import org.eclipse.core.databinding.DataBindingContext;
import org.eclipse.core.databinding.beans.BeanProperties;
import org.eclipse.core.databinding.observable.sideeffect.ISideEffect;
import org.eclipse.core.databinding.observable.value.IObservableValue;
import org.eclipse.core.databinding.observable.value.SelectObservableValue;
import org.eclipse.jface.databinding.swt.WidgetProperties;
import org.eclipse.jface.databinding.viewers.IViewerObservableValue;
import org.eclipse.jface.databinding.viewers.ViewerProperties;
import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Group;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import uk.ac.diamond.daq.experiment.api.plan.TriggerDescriptor;
import uk.ac.diamond.daq.experiment.api.remote.ExecutionPolicy;
import uk.ac.diamond.daq.experiment.api.remote.SignalSource;
import uk.ac.diamond.daq.experiment.api.ui.EditableWithListWidget;
import uk.ac.diamond.daq.experiment.ui.widget.ElementEditor;

/**
 * GUI for editing a {@link TriggerDescriptor}
 */
public class TriggerEditor implements ElementEditor {
	
	/**
	 * For a single, time-based trigger, no control for the tolerance is created.
	 * This value is quietly written in the model
	 */
	private static final double TIME_TOLERANCE = 0.1;
	
	// data
	private TriggerDescriptor model;
	private Set<String> sevs;
	private final String experimentId;
	
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
	private Combo sevCombo;
	private Text interval;
	private Text target;
	private Text tolerance;
	
	// binding
	private final DataBindingContext dbc;
	private final List<Binding> mainBindings;
	private final List<Binding> detailBindings;
	private final List<ISideEffect> sideEffects;

	/**
	 * Instantiate with experiment service and experiment ID
	 * so that I can retrieve saved scans
	 */
	public TriggerEditor(String experimentId) {
		this.experimentId = experimentId;
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
		
		scanCombo = new ComboViewer(composite, SWT.DROP_DOWN | SWT.READ_ONLY);
		scanCombo.setContentProvider(ArrayContentProvider.getInstance());
		scanCombo.setInput(getExperimentService().getScanNames(experimentId));
		STRETCH.copy().applyTo(scanCombo.getControl());
		
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
		sevSourceButton.setText("Environment variable");
		
		timeSourceButton = new Button(sourceGroup, SWT.RADIO);
		timeSourceButton.setText("Time");
		
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
		
		composite.layout(true);
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
		
		new Label(controlComposite, SWT.NONE).setText("Environment variable");
		sevCombo = new Combo(controlComposite, SWT.READ_ONLY);
		if (sevs != null) {
			populateSevCombo();
			bindSev();
		}
		
		STRETCH.applyTo(sevCombo);
		
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

	private void populateSevCombo() {
		sevCombo.setItems(sevs.toArray(new String[0]));
	}
	
	@SuppressWarnings("unchecked")
	private void bindTarget() {
		IObservableValue<Double> targetInText = WidgetProperties.text(SWT.Modify).observe(target);
		IObservableValue<Double> targetInModel = BeanProperties.value("target").observe(model);
		
		Binding targetBinding = dbc.bindValue(targetInText, targetInModel);
		detailBindings.add(targetBinding);
	}
	
	@SuppressWarnings("unchecked")
	private void bindPeriod() {
		IObservableValue<Double> intervalText = WidgetProperties.text(SWT.Modify).observe(interval);
		IObservableValue<Double> intervalInModel = BeanProperties.value("interval").observe(model);
		
		Binding intervalBinding = dbc.bindValue(intervalText, intervalInModel);
		detailBindings.add(intervalBinding);
	}
	
	@SuppressWarnings("unchecked")
	private void bindTolerance() {
		IObservableValue<Double> inWidget = WidgetProperties.text(SWT.Modify).observe(tolerance);
		IObservableValue<Double> inModel = BeanProperties.value("tolerance").observe(model);
		
		Binding toleranceBinding = dbc.bindValue(inWidget, inModel);
		detailBindings.add(toleranceBinding);
	}
	
	@SuppressWarnings("unchecked")
	private void bindSev() {
		IObservableValue<String> inWidget = WidgetProperties.selection().observe(sevCombo);
		IObservableValue<String> inModel = BeanProperties.value("sampleEnvironmentVariableName").observe(model);
		
		Binding sevBinding = dbc.bindValue(inWidget, inModel);
		detailBindings.add(sevBinding);
		
		// if there is no sev in model, let's select first option
		if (model.getSampleEnvironmentVariableName() == null) {
			sevCombo.select(0);
		}
	}

	@SuppressWarnings("unchecked")
	private void updateBindings() {
		removeOldBindings();
		
		// name
		IObservableValue<String> nameTextObservable = WidgetProperties.text(SWT.Modify).observe(nameText);
		IObservableValue<String> nameInModelObservable = BeanProperties.value("name").observe(model);
		
		Binding nameBinding = dbc.bindValue(nameTextObservable, nameInModelObservable);
		mainBindings.add(nameBinding);
		
		// scan
		IViewerObservableValue scanInWidget = ViewerProperties.singleSelection().observe(scanCombo);
		IObservableValue<String> scanInModel = BeanProperties.value("scanName").observe(model);
		
		Binding scanBinding = dbc.bindValue(scanInWidget, scanInModel);
		mainBindings.add(scanBinding);
		
		// trigger source
		IObservableValue<SignalSource> sourceInModelObservable = BeanProperties.value("signalSource").observe(model);
		
		SelectObservableValue<SignalSource> sourceSelection = new SelectObservableValue<>();
		sourceSelection.addOption(SignalSource.POSITION, WidgetProperties.selection().observe(sevSourceButton));
		sourceSelection.addOption(SignalSource.TIME, WidgetProperties.selection().observe(timeSourceButton));
		
		Binding sourceBinding = dbc.bindValue(sourceSelection, sourceInModelObservable);
		mainBindings.add(sourceBinding);
		
		// trigger mode
		IObservableValue<ExecutionPolicy> modeInModelObservable = BeanProperties.value("executionPolicy").observe(model);
		
		SelectObservableValue<ExecutionPolicy> modeSelection = new SelectObservableValue<>();
		modeSelection.addOption(ExecutionPolicy.SINGLE, WidgetProperties.selection().observe(oneShotButton));
		modeSelection.addOption(ExecutionPolicy.REPEATING, WidgetProperties.selection().observe(periodicButton));
		
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
	
	private void setEnabled(boolean enabled) {
		nameText.setEnabled(enabled);
		scanCombo.getControl().setEnabled(enabled);
		sevSourceButton.setEnabled(enabled);
		timeSourceButton.setEnabled(enabled);
		oneShotButton.setEnabled(enabled);
		periodicButton.setEnabled(enabled);
		
		for (Control control : Arrays.asList(sevCombo, target, tolerance, interval)) {
			if (control != null && !control.isDisposed()) {
				control.setEnabled(enabled);
			}
		}
	}

	void setSevNames(Set<String> sevs) {
		this.sevs = sevs;
		if (sevCombo != null && !sevCombo.isDisposed()) {
			populateSevCombo();
		}
	}

}
