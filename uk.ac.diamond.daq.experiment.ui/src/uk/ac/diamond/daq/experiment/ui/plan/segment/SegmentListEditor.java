package uk.ac.diamond.daq.experiment.ui.plan.segment;

import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.diamond.daq.experiment.api.plan.SEVSignal;
import uk.ac.diamond.daq.experiment.api.plan.SegmentDescriptor;
import uk.ac.diamond.daq.experiment.ui.widget.ListWithCustomEditor;

public class SegmentListEditor {
	private SegmentEditor segmentEditor;
	
	public SegmentListEditor() {
		segmentEditor = new SegmentEditor();
	}
	
	public Composite createEditorPart(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).grab(true, true).applyTo(composite);
		GridLayoutFactory.swtDefaults().applyTo(composite);
		
		new Label(composite, SWT.NONE).setText("Segments");
		
		ListWithCustomEditor listEditor = new ListWithCustomEditor();
		listEditor.setMinimumElements(1);
		listEditor.setListHeight(150);
		listEditor.setTemplate(new SegmentDescriptor());
		
		
		listEditor.setElementEditor(segmentEditor);
		
		listEditor.create(composite);
		
		return composite;
	}
	
	public void setSevs(Map<String, SEVSignal> sevs) {
		segmentEditor.setSevNames(sevs.keySet().stream().collect(Collectors.toList()));
	}
	
	
}
