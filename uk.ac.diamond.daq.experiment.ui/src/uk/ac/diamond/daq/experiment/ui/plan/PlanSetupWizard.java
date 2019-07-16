package uk.ac.diamond.daq.experiment.ui.plan;

import java.util.Objects;

import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.Wizard;

import uk.ac.diamond.daq.experiment.api.plan.ExperimentPlanBean;

public class PlanSetupWizard extends Wizard {
	
	private final ExperimentPlanBean planBean;
	
	// pages
	private MetadataAndExperimentDriverPage metadataAndDriverPage;
	private SegmentsAndTriggersPage segmentsAndTriggersPage;
	private PlanSummaryPage planSummaryPage;
	
	private final String experimentId;
	

	/**
	 * This constructor should be used to create a new experiment plan
	 */
	public PlanSetupWizard(String experimentId) {
		this(experimentId, new ExperimentPlanBean());
	}
	
	/**
	 * This constructor should be used to edit a previously defined experiment plan
	 */
	public PlanSetupWizard(String experimentId, ExperimentPlanBean planBean) {
		this.experimentId = experimentId;
		Objects.requireNonNull(planBean);
		this.planBean = planBean;
	}

	@Override
	public void addPages() {
		setWindowTitle("Setup experiment plan");
		
		metadataAndDriverPage = new MetadataAndExperimentDriverPage(experimentId, planBean);
		
		addPage(metadataAndDriverPage);
		
		segmentsAndTriggersPage = new SegmentsAndTriggersPage(experimentId, planBean);
		addPage(segmentsAndTriggersPage);
		
		planSummaryPage = new PlanSummaryPage(experimentId, planBean);
		addPage(planSummaryPage);
	}
	
	@Override
	public boolean canFinish() {
		if (metadataAndDriverPage.isPageComplete() && segmentsAndTriggersPage.isPageComplete()) {
			IWizardContainer wizardContainer = getContainer();
			if (wizardContainer.getCurrentPage() == planSummaryPage) {
				return true;
			}
		}
		return false;
	}

	@Override
	public boolean performFinish() {
		return true;
	}
	
	public ExperimentPlanBean getExperimentPlanBean() {		
		return planBean;
	}
}
