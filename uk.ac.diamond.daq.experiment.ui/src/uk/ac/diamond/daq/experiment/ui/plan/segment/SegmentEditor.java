package uk.ac.diamond.daq.experiment.ui.plan.segment;

import static uk.ac.diamond.daq.experiment.ui.ExperimentUiUtils.STRETCH;
import static uk.ac.diamond.daq.experiment.ui.ExperimentUiUtils.addSpace;

import java.util.ArrayList;
import java.util.EnumSet;
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
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import uk.ac.diamond.daq.experiment.api.plan.SegmentDescriptor;
import uk.ac.diamond.daq.experiment.api.remote.Inequality;
import uk.ac.diamond.daq.experiment.api.remote.SignalSource;
import uk.ac.diamond.daq.experiment.api.ui.EditableWithListWidget;
import uk.ac.diamond.daq.experiment.ui.plan.trigger.TriggerListEditor;
import uk.ac.diamond.daq.experiment.ui.widget.ElementEditor;

public class SegmentEditor implements ElementEditor {
	
	private Composite composite;
	private Composite limitComposite;
	
	private Text name;
	private Button sevSource;
	private Button timeSource;
	
	// sev-based segment
	private ComboViewer readoutsCombo;
	private ComboViewer inequality;
	private Text predicateArgument;
		
	// time-based
	private Text duration;
	
	private TriggerListEditor triggers;
	
	// for binding
	private SegmentDescriptor segment;
	private final DataBindingContext dbc;
	private final List<Binding> segmentDescriptorBindings;
	private final List<Binding> limitControlBindings;
	private ISideEffect limitControlSwitch;
	private Set<String> readouts;
	
	public SegmentEditor(String experimentId) {
		triggers = new TriggerListEditor(experimentId);
		dbc = new DataBindingContext();
		segmentDescriptorBindings = new ArrayList<>();
		limitControlBindings = new ArrayList<>();
	}

	@Override
	public void createControl(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		STRETCH.copy().align(SWT.FILL, SWT.TOP).applyTo(composite);
		GridLayoutFactory.swtDefaults().applyTo(composite);
		
		new Label(composite, SWT.NONE).setText("Name");
		name = new Text(composite, SWT.BORDER);
		STRETCH.applyTo(name);
		
		addSpace(composite);
		
		new Label(composite, SWT.NONE).setText("Limiting source");
		
		sevSource = new Button(composite, SWT.RADIO);
		sevSource.setText("Environment variable");
		sevSource.setSelection(true);
		STRETCH.applyTo(sevSource);
		
		timeSource = new Button(composite, SWT.RADIO);
		timeSource.setText("Time");
		STRETCH.applyTo(timeSource);
		
		addSpace(composite);
		
		createLimitComposite();
		
		Composite triggersComposite = new Composite(parent.getParent(), SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).applyTo(triggersComposite);
		GridLayoutFactory.fillDefaults().applyTo(triggersComposite);
		triggers.createEditorPart(triggersComposite);
		triggers.addListListener(e -> {
			if (segment != null) {
				segment.setTriggers(triggers.getTriggerList());
			}
		});
	}
	
	@Override
	public void load(EditableWithListWidget element) {
		segment = (SegmentDescriptor) element;
		triggers.setTriggerList(segment.getTriggers());
		updateBindings();
	}
	
	@Override
	public void clear() {
		removeOldBindings();
	}
	
	private void createLimitComposite() {
		if (limitComposite != null) {
			limitComposite.dispose();
			limitComposite = null;
		}
		
		limitComposite = new Composite(composite, SWT.NONE);
		GridDataFactory.swtDefaults().span(2, 1).align(SWT.FILL, SWT.CENTER).grab(true, true).applyTo(limitComposite);
	}
	
	private void updateLimitControls(SignalSource signalSource) {
		createLimitComposite();
		
		switch (signalSource) {
		case POSITION:
			createPositionControls();
			break;
		case TIME:
			createTimeControls();
			break;
		default: 
			throw new IllegalArgumentException("Unsupported  segment limiting source");
		}
		
		updateLimitControlBindings(signalSource);
		
		composite.layout();
	}
	
	private void createPositionControls() {
		GridLayoutFactory.fillDefaults().numColumns(3).applyTo(limitComposite);
		readoutsCombo = new ComboViewer(limitComposite);
		readoutsCombo.setContentProvider(ArrayContentProvider.getInstance());
		STRETCH.applyTo(readoutsCombo.getControl());
		populateReadoutsCombo();
		
		inequality = new ComboViewer(limitComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
		inequality.setContentProvider(ArrayContentProvider.getInstance());
		
		Set<Inequality> pred = EnumSet.allOf(Inequality.class);
		inequality.setInput(pred.toArray());
		
		predicateArgument = new Text(limitComposite, SWT.BORDER);
		STRETCH.applyTo(predicateArgument);
	}
	
	private void createTimeControls() {
		GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(true).applyTo(limitComposite);
		new Label(limitComposite, SWT.NONE).setText("Duration (s)");
		
		duration = new Text(limitComposite, SWT.BORDER);
		STRETCH.applyTo(duration);
	}
	
	@SuppressWarnings("unchecked")
	private void updateBindings() {
		removeOldBindings();
		
		IObservableValue<String> nameTextObservable = WidgetProperties.text(SWT.Modify).observe(name);
		IObservableValue<String> nameInModelObservable = BeanProperties.value("name").observe(segment);
		
		Binding nameBinding = dbc.bindValue(nameTextObservable, nameInModelObservable);
		segmentDescriptorBindings.add(nameBinding);
		
		IObservableValue<SignalSource> sourceInModelObservable = BeanProperties.value("signalSource").observe(segment);
		
		SelectObservableValue<SignalSource> limitingSourceSelection = new SelectObservableValue<>();
		limitingSourceSelection.addOption(SignalSource.POSITION, WidgetProperties.selection().observe(sevSource));
		limitingSourceSelection.addOption(SignalSource.TIME, WidgetProperties.selection().observe(timeSource));

		Binding sourceBinding = dbc.bindValue(limitingSourceSelection, sourceInModelObservable);
		segmentDescriptorBindings.add(sourceBinding);
		limitControlSwitch = ISideEffect.create(limitingSourceSelection::getValue, this::updateLimitControls);
	}
	
	@SuppressWarnings("unchecked")
	private void updateLimitControlBindings(SignalSource signalSource) {
		
		limitControlBindings.forEach(binding -> {
			dbc.removeBinding(binding);
			binding.dispose();
		});
		
		limitControlBindings.clear();
		
		switch (signalSource) {
		case POSITION:
			IViewerObservableValue sevControlObservable = ViewerProperties.singleSelection().observe(readoutsCombo);
			IObservableValue<String> sevInModelObservable = BeanProperties.value("sampleEnvironmentVariableName").observe(segment);
			Binding sevBinding = dbc.bindValue(sevControlObservable, sevInModelObservable);
			limitControlBindings.add(sevBinding);
			
			// if sevName is not set, choose first option
			if (segment.getSampleEnvironmentVariableName() == null) {
				readoutsCombo.setSelection(new StructuredSelection(readoutsCombo.getElementAt(0)), true);
			}
			
			IViewerObservableValue selectedInequalityObservable = ViewerProperties.singleSelection().observe(inequality);
			IObservableValue<SignalSource> inequalityInModelObservable = BeanProperties.value("inequality").observe(segment);
			Binding inequalityBinding = dbc.bindValue(selectedInequalityObservable, inequalityInModelObservable);
			limitControlBindings.add(inequalityBinding);
			
			IObservableValue<Double> ineqArgControlObservable = WidgetProperties.text(SWT.Modify).observe(predicateArgument);
			IObservableValue<Double> ineqArgInModelObservable = BeanProperties.value("inequalityArgument").observe(segment);
			Binding ineqArgBinding = dbc.bindValue(ineqArgControlObservable, ineqArgInModelObservable);
			limitControlBindings.add(ineqArgBinding);
			break;
			
		case TIME:
			IObservableValue<Double> durationControlObservable = WidgetProperties.text(SWT.Modify).observe(duration);
			IObservableValue<Double> durationInModelObservable = BeanProperties.value("duration").observe(segment);
			Binding durationBinding = dbc.bindValue(durationControlObservable, durationInModelObservable);
			limitControlBindings.add(durationBinding);
			break;
			
		default:
			throw new IllegalArgumentException("Unsupported  segment limiting source");
		}
	}

	private void removeOldBindings() {
		segmentDescriptorBindings.forEach(binding -> {
			dbc.removeBinding(binding);
			binding.dispose();
		});
		
		if (limitControlSwitch != null) {
			limitControlSwitch.dispose();
		}
		
		segmentDescriptorBindings.clear();
	}
	
	private void populateReadoutsCombo() {
		if (readoutsCombo != null && readouts != null) { 
			readoutsCombo.setInput(readouts);
			if (segment != null && readouts.contains(segment.getSampleEnvironmentVariableName())) {
				readoutsCombo.setSelection(new StructuredSelection(segment.getSampleEnvironmentVariableName()));
			}
		}
	}

	public void setReadouts(Set<String> readouts) {
		this.readouts = readouts;
		populateReadoutsCombo();
		triggers.setReadouts(readouts);
	}
}
