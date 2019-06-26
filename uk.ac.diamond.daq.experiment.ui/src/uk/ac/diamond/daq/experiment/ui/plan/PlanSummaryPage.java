package uk.ac.diamond.daq.experiment.ui.plan;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import uk.ac.diamond.daq.experiment.api.ExperimentService;
import uk.ac.diamond.daq.experiment.api.plan.ExperimentPlanBean;

public class PlanSummaryPage  extends WizardPage {
	
	private ExperimentService experimentService;
	private ExperimentPlanBean bean;
	
	protected PlanSummaryPage(ExperimentService experimentService, String experimentId, ExperimentPlanBean bean) {
		super(PlanSummaryPage.class.getSimpleName());
		
		setTitle("Plan Summary");
		setDescription("Defined Segments and Triggers");
		
		this.experimentService = experimentService;
		this.bean = bean;
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridLayoutFactory.fillDefaults().applyTo(composite);
		GridDataFactory.fillDefaults().applyTo(composite);
		
		new PlanPreviewer(experimentService, bean, composite);
		
		
		setControl(composite);
	}
	
	@Override
	public boolean isPageComplete() {
		return true;
	}
}
