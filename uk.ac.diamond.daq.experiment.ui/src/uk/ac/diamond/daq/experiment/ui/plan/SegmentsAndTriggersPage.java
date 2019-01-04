package uk.ac.diamond.daq.experiment.ui.plan;

import java.util.Map;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.daq.experiment.api.plan.SEVSignal;
import uk.ac.diamond.daq.experiment.ui.plan.segment.SegmentListEditor;

public class SegmentsAndTriggersPage extends WizardPage {
	
	private SegmentListEditor segments;

	SegmentsAndTriggersPage() {
		super(SegmentsAndTriggersPage.class.getSimpleName());
		setTitle("Segments and Triggers");
		setDescription("Automate the execution of defined measurements");
		
		segments = new SegmentListEditor();
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(composite);
		GridLayoutFactory.fillDefaults().spacing(50, SWT.DEFAULT).numColumns(2).equalWidth(true).applyTo(composite);
		
		segments.createEditorPart(composite);
		
		setControl(composite);
	}
	
	public void setSevs(Map<String, SEVSignal> sevs) {
		segments.setSevs(sevs);
	}

}
