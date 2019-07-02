package uk.ac.diamond.daq.experiment.ui.plan;

import java.util.List;
import java.util.Set;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.daq.experiment.api.ExperimentService;
import uk.ac.diamond.daq.experiment.api.plan.ExperimentPlanBean;
import uk.ac.diamond.daq.experiment.api.plan.SegmentDescriptor;
import uk.ac.diamond.daq.experiment.ui.plan.segment.SegmentListEditor;

public class SegmentsAndTriggersPage extends WizardPage {
	
	private SegmentListEditor segments;
	
	private final ExperimentService experimentService;
	private final ExperimentPlanBean planBean;

	SegmentsAndTriggersPage(ExperimentService experimentService, String experimentId, ExperimentPlanBean planBean) {
		super(SegmentsAndTriggersPage.class.getSimpleName());
		setTitle("Segments and Triggers");
		setDescription("Automate the execution of defined measurements");
		
		segments = new SegmentListEditor(experimentService, experimentId, planBean);
		
		this.experimentService = experimentService;
		this.planBean = planBean;
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(composite);
		GridLayoutFactory.fillDefaults().spacing(50, SWT.DEFAULT).numColumns(3).equalWidth(true).applyTo(composite);
		
		segments.createEditorPart(composite);
		
		PlanPreviewer profilePlot = new PlanPreviewer(experimentService, planBean, composite);
		profilePlot.update();
		
		setControl(composite);
	}
	
	public void setSevs(Set<String> sevs) {
		segments.setSevs(sevs);
	}

	public List<SegmentDescriptor> getSegments() {
		return segments.getSegments();
	}

}
