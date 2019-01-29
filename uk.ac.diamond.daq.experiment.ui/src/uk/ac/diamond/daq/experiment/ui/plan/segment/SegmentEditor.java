package uk.ac.diamond.daq.experiment.ui.plan.segment;

import static uk.ac.diamond.daq.experiment.ui.driver.DiadUIUtils.STRETCH;

import java.util.Arrays;
import java.util.EnumSet;
import java.util.List;
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

import uk.ac.diamond.daq.experiment.api.remote.Inequality;
import uk.ac.diamond.daq.experiment.api.ui.EditableWithListWidget;
import uk.ac.diamond.daq.experiment.ui.plan.trigger.TriggerDescriptor.Source;
import uk.ac.diamond.daq.experiment.ui.plan.trigger.TriggerListEditor;
import uk.ac.diamond.daq.experiment.ui.widget.ElementEditor;

public class SegmentEditor implements ElementEditor {
	
	private SegmentDescriptor model;
	
	private Composite composite;
	private Composite limitComposite;
	
	private Text name;
	private Button sevSource;
	private Button timeSource;
	
	private Source limitingSource;
	
	// sev-based segment
	private Combo sevCombo;
	private ComboViewer inequality;
	private Text predicateArgument;
		
	// time-based
	private Text duration;
	
	// Triggers
	private TriggerListEditor triggers;
	
	private List<String> sevs;

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
		sevSource.addListener(SWT.Selection, e -> changeLimitingSource(Source.SEV));
		
		STRETCH.applyTo(sevSource);
		
		timeSource = new Button(composite, SWT.RADIO);
		timeSource.setText("Time");
		timeSource.addListener(SWT.Selection, e -> changeLimitingSource(Source.TIME));
		
		STRETCH.applyTo(timeSource);
		
		new Label(composite, SWT.NONE); // space
		
		if (limitingSource == null) limitingSource = Source.SEV;
		
		updateLimitControl();
		
		triggers = new TriggerListEditor();
		
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
		
		limitingSource = model.getSource();
		updateLimitControl();
		
		if (limitingSource == Source.SEV) {
			
			sevSource.setSelection(true);
			timeSource.setSelection(false);
			
			
			
			int index = Arrays.asList(sevCombo.getItems()).indexOf(model.getSevName());
			sevCombo.select(index);
			
			inequality.setSelection(new StructuredSelection(model.getIneq()));
			
			predicateArgument.setText(String.valueOf(model.getIneqRef()));
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
		limitingSource = Source.TIME;
		updateLimitControl();
	}
	
	private void changeLimitingSource(Source source) {
		if (model != null) model.setSource(source);
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
		case SEV:
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
	
	public void setSevNames(List<String> sevs) {
		// SEV Combo in this editor is not working...
		this.sevs = sevs;
		triggers.setSevs(sevs);
	}

}
