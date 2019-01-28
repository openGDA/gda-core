package uk.ac.diamond.daq.experiment.ui.plan.trigger;

import java.beans.PropertyChangeListener;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.diamond.daq.experiment.api.ExperimentService;
import uk.ac.diamond.daq.experiment.api.plan.TriggerDescriptor;
import uk.ac.diamond.daq.experiment.api.ui.EditableWithListWidget;
import uk.ac.diamond.daq.experiment.ui.widget.ListWithCustomEditor;

public class TriggerListEditor {
	
	private ListWithCustomEditor triggers = new ListWithCustomEditor();
	private TriggerEditor triggerEditor;
	
	public TriggerListEditor(ExperimentService experimentService, String experimentId) {
		triggerEditor = new TriggerEditor(experimentService, experimentId);
	}

	public Composite createEditorPart(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).grab(true, true).applyTo(composite);
		GridLayoutFactory.swtDefaults().applyTo(composite);
		
		new Label(composite, SWT.NONE).setText("Triggers");
		
		triggers.setMinimumElements(0);
		triggers.setListHeight(150);
		triggers.setTemplate(new TriggerDescriptor());
		
		triggers.setElementEditor(triggerEditor);
		
		triggers.create(composite);
		
		return composite;
	}
	
	public void setSevs(List<String> sevs) {
		triggerEditor.setSevNames(sevs);
	}
	
	public void addListListener(PropertyChangeListener listener) {
		triggers.addListListener(listener);
	}
	
	public List<TriggerDescriptor> getTriggerList() {
		return triggers.getList().stream().map(TriggerDescriptor.class::cast).collect(Collectors.toList());
	}
	
	public void setTriggerList(List<TriggerDescriptor> triggerList) {
		triggers.setList(triggerList.stream().map(EditableWithListWidget.class::cast).collect(Collectors.toList()));
	}
}
