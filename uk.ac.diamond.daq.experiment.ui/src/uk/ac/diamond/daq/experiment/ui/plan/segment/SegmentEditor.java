package uk.ac.diamond.daq.experiment.ui.plan.segment;

import static uk.ac.diamond.daq.experiment.ui.driver.DiadUIUtils.STRETCH;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.Set;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Combo;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.swt.widgets.Text;

import uk.ac.diamond.daq.experiment.api.ExperimentService;
import uk.ac.diamond.daq.experiment.api.plan.SegmentDescriptor;
import uk.ac.diamond.daq.experiment.api.remote.Inequality;
import uk.ac.diamond.daq.experiment.api.remote.SignalSource;
import uk.ac.diamond.daq.experiment.api.ui.EditableWithListWidget;
import uk.ac.diamond.daq.experiment.ui.plan.trigger.TriggerListEditor;
import uk.ac.diamond.daq.experiment.ui.widget.ElementEditor;

public class SegmentEditor implements ElementEditor {
	
	private SegmentDescriptor model;
	
	private Composite composite;
	private Composite limitComposite;
	
	private Text name;
	private Button sevSource;
	private Button timeSource;
	
	private SignalSource limitingSource;
	
	// sev-based segment
	private Combo sevCombo;
	private ComboViewer inequality;
	private Text predicateArgument;
		
	// time-based
	private Text duration;
	
	// Triggers
	private TriggerListEditor triggers;
	
	private Set<String> sevs;
	
	public SegmentEditor(ExperimentService experimentService, String experimentId) {
		triggers = new TriggerListEditor(experimentService, experimentId);
	}

	@Override
	public void createControl(Composite parent) {
		composite = new Composite(parent, SWT.NONE);
		STRETCH.copy().align(SWT.FILL, SWT.TOP).applyTo(composite);
		GridLayoutFactory.swtDefaults().applyTo(composite);
		
		new Label(composite, SWT.NONE).setText("Name");
		name = new Text(composite, SWT.BORDER);
		name.addListener(SWT.Modify, e -> model.setName(name.getText()));
		STRETCH.applyTo(name);
		
		new Label(composite, SWT.NONE); // space
		
		new Label(composite, SWT.NONE).setText("Limiting source");
		
		sevSource = new Button(composite, SWT.RADIO);
		sevSource.setText("Environment variable");
		sevSource.setSelection(true);
		sevSource.addListener(SWT.Selection, e -> changeLimitingSource(SignalSource.POSITION));
		
		STRETCH.applyTo(sevSource);
		
		timeSource = new Button(composite, SWT.RADIO);
		timeSource.setText("Time");
		timeSource.addListener(SWT.Selection, e -> changeLimitingSource(SignalSource.TIME));
		
		STRETCH.applyTo(timeSource);
		
		new Label(composite, SWT.NONE); // space
		
		if (limitingSource == null) limitingSource = SignalSource.POSITION;
		
		updateLimitControl();
		
		// FIXME this doesn't look right
		// it is weird that we are creating a new composite on top of parent.getParent()
		Composite triggersComposite = new Composite(parent.getParent(), SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.TOP).grab(true, false).applyTo(triggersComposite);
		GridLayoutFactory.fillDefaults().applyTo(triggersComposite);
		triggers.createEditorPart(triggersComposite);

		triggers.addListListener(e -> {
			if (model != null) {
				model.setTriggers(triggers.getTriggerList());
			}
		});
		
		name.setFocus();
	}

	@Override
	public void load(EditableWithListWidget element) {
		model = (SegmentDescriptor) element;
		name.setText(model.getName());
		
		limitingSource = model.getSignalSource();
		updateLimitControl();
		
		if (limitingSource == SignalSource.POSITION) {
			
			sevSource.setSelection(true);
			timeSource.setSelection(false);
			
			
			
			int index = Arrays.asList(sevCombo.getItems()).indexOf(model.getSampleEnvironmentVariableName());
			sevCombo.select(index);
			
			inequality.setSelection(new StructuredSelection(model.getInequality()));
			
			predicateArgument.setText(String.valueOf(model.getInequalityArgument()));
		} else {
			timeSource.setSelection(true);
			sevSource.setSelection(false);
			duration.setText(String.valueOf(model.getDuration()));
		}
		
		triggers.setTriggerList(model.getTriggers());
		name.setFocus();
	}

	@Override
	public void clear() {
		name.setText("");
		timeSource.setEnabled(true);
		limitingSource = SignalSource.TIME;
		updateLimitControl();
	}
	
	private void changeLimitingSource(SignalSource source) {
		if (model != null) model.setSignalSource(source);
		limitingSource = source;
		updateLimitControl();
		limitComposite.setFocus();
	}
	
	private void updateLimitControl() {
		if (limitComposite!=null) {
			limitComposite.dispose();
			limitComposite = null;
		}
		
		limitComposite = new Composite(composite, SWT.NONE);
		GridDataFactory.swtDefaults().span(2, 1).align(SWT.FILL, SWT.CENTER).grab(true, true).applyTo(limitComposite);
		
		switch (limitingSource) {
		case POSITION:
			GridLayoutFactory.fillDefaults().numColumns(3).applyTo(limitComposite);
			sevCombo = new Combo(limitComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
			
			sevCombo.addListener(SWT.Selection, e -> model.setSevName(sevCombo.getText()));
			STRETCH.applyTo(sevCombo);
			
			inequality = new ComboViewer(limitComposite, SWT.DROP_DOWN | SWT.READ_ONLY);
			inequality.setContentProvider(ArrayContentProvider.getInstance());
			
			Set<Inequality> pred = EnumSet.allOf(Inequality.class);
			inequality.setInput(pred.toArray());
			inequality.setSelection(new StructuredSelection(Inequality.GREATER_THAN_OR_EQUAL_TO));
			inequality.addSelectionChangedListener(e -> model.setIneq((Inequality) ((StructuredSelection) inequality.getSelection()).getFirstElement()));
			
			predicateArgument = new Text(limitComposite, SWT.BORDER);
			predicateArgument.addListener(SWT.Modify, e -> model.setIneqRef(Double.parseDouble(predicateArgument.getText())));
			STRETCH.applyTo(predicateArgument);
			break;
		case TIME:
			GridLayoutFactory.fillDefaults().numColumns(2).equalWidth(true).applyTo(limitComposite);
			new Label(limitComposite, SWT.NONE).setText("Duration (min)");
			
			duration = new Text(limitComposite, SWT.BORDER);
			duration.addListener(SWT.Modify, e -> model.setDuration(Double.parseDouble(duration.getText())));
			STRETCH.applyTo(duration);
			break;
		default:
			throw new IllegalArgumentException("Unsupported segment limiting source");
		}
		
		if (sevs != null && !sevCombo.isDisposed()) {
			sevCombo.setItems(sevs.toArray(new String[0]));
		}
		
		composite.layout();
	}
	
	public void setSevNames(Set<String> sevs) {
		this.sevs = sevs;
		triggers.setSevs(sevs);
	}

}
