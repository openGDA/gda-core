package uk.ac.diamond.daq.experiment.ui.plan;

import static uk.ac.diamond.daq.experiment.ui.ExperimentUiUtils.STRETCH;
import static uk.ac.diamond.daq.experiment.ui.ExperimentUiUtils.addSpace;

import java.util.List;
import java.util.Set;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.daq.experiment.api.ExperimentService;
import uk.ac.diamond.daq.experiment.api.plan.ExperimentPlanBean;
import uk.ac.diamond.daq.experiment.api.plan.SegmentDescriptor;
import uk.ac.diamond.daq.experiment.ui.plan.preview.PlanPreviewer;
import uk.ac.diamond.daq.experiment.ui.plan.preview.PlotController;
import uk.ac.diamond.daq.experiment.ui.plan.preview.PlotControllerImpl;
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
		
		PlotController plotController = new PlotControllerImpl(composite);
		PlanPreviewer preview = new PlanPreviewer(planBean, experimentService, plotController);
		preview.update();
		
		// TODO remove this temporary button once PlanPreviewer can listen for changes in the bean
		addSpace(composite);
		addSpace(composite);
		Button updateButton = new Button(composite, SWT.NONE);
		STRETCH.applyTo(updateButton);
		updateButton.setText("Recalculate...");
		updateButton.addListener(SWT.Selection, event -> preview.update());
		
		setControl(composite);
	}
	
	public void setSevs(Set<String> sevs) {
		segments.setSevs(sevs);
	}

	public List<SegmentDescriptor> getSegments() {
		return segments.getSegments();
	}

}
