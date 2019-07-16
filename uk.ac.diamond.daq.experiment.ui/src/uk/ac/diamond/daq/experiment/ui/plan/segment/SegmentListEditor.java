package uk.ac.diamond.daq.experiment.ui.plan.segment;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;

import uk.ac.diamond.daq.experiment.api.plan.ExperimentPlanBean;
import uk.ac.diamond.daq.experiment.api.plan.SegmentDescriptor;
import uk.ac.diamond.daq.experiment.api.ui.EditableWithListWidget;
import uk.ac.diamond.daq.experiment.ui.widget.ListWithCustomEditor;

public class SegmentListEditor {
	
	private ListWithCustomEditor listEditor;
	private SegmentEditor segmentEditor;
	
	private final ExperimentPlanBean planBean;
	
	public SegmentListEditor(String experimentId, ExperimentPlanBean planBean) {
		segmentEditor = new SegmentEditor(experimentId);
		this.planBean = planBean;
	}
	
	public Composite createEditorPart(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridDataFactory.swtDefaults().align(SWT.FILL, SWT.TOP).grab(true, true).applyTo(composite);
		GridLayoutFactory.swtDefaults().applyTo(composite);
		
		new Label(composite, SWT.NONE).setText("Segments");
		
		listEditor = new ListWithCustomEditor();
		listEditor.setMinimumElements(1);
		listEditor.setListHeight(150);
		listEditor.setTemplate(new SegmentDescriptor());
		
		listEditor.setElementEditor(segmentEditor);
		
		if (planBean.getSegments() != null) {
			listEditor.setList(planBean.getSegments().stream().map(EditableWithListWidget.class::cast).collect(Collectors.toList()));
		}
		
		listEditor.addListListener(e -> planBean.setSegments(getSegments()));
		
		listEditor.create(composite);
		
		return composite;
	}
	
	public void setSevs(Set<String> sevs) {
		segmentEditor.setSevNames(sevs);
	}
	
	public List<SegmentDescriptor> getSegments() {
		return listEditor.getList().stream().map(SegmentDescriptor.class::cast).collect(Collectors.toList());
	}
	
	
}
