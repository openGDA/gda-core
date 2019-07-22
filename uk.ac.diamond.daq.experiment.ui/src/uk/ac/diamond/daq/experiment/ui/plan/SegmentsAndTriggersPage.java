package uk.ac.diamond.daq.experiment.ui.plan;

import java.beans.PropertyChangeListener;
import java.util.List;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.daq.experiment.api.plan.ExperimentPlanBean;
import uk.ac.diamond.daq.experiment.api.plan.SegmentDescriptor;
import uk.ac.diamond.daq.experiment.ui.plan.preview.PlanPreviewer;
import uk.ac.diamond.daq.experiment.ui.plan.preview.PlotController;
import uk.ac.diamond.daq.experiment.ui.plan.preview.PlotControllerImpl;
import uk.ac.diamond.daq.experiment.ui.plan.segment.SegmentListEditor;

public class SegmentsAndTriggersPage extends WizardPage {
	
	private SegmentListEditor segments;
	
	private final ExperimentPlanBean planBean;

	SegmentsAndTriggersPage(String experimentId, ExperimentPlanBean planBean) {
		super(SegmentsAndTriggersPage.class.getSimpleName());
		setTitle("Segments and Triggers");
		setDescription("Automate the execution of defined measurements");
		
		segments = new SegmentListEditor(experimentId, planBean);
		
		this.planBean = planBean;
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridDataFactory.fillDefaults().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(composite);
		GridLayoutFactory.fillDefaults().spacing(50, SWT.DEFAULT).numColumns(3).equalWidth(true).applyTo(composite);
		
		segments.createEditorPart(composite);
		
		PlotController plotController = new PlotControllerImpl(composite);
		PlanPreviewer preview = new PlanPreviewer(planBean, plotController);
		preview.update();
		
		final PropertyChangeListener beanListener = change -> preview.update();
		
		planBean.addPropertyChangeListener(beanListener);
		
		composite.addDisposeListener(dispose -> planBean.removePropertyChangeListener(beanListener));
		
		setControl(composite);
	}
	
	public List<SegmentDescriptor> getSegments() {
		return segments.getSegments();
	}

}
