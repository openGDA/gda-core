package uk.ac.diamond.daq.experiment.ui.plan;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import org.eclipse.jface.wizard.IWizardContainer;
import org.eclipse.jface.wizard.Wizard;

import gda.factory.Finder;
import uk.ac.diamond.daq.experiment.api.ExperimentService;
import uk.ac.diamond.daq.experiment.api.driver.DriverModel;
import uk.ac.diamond.daq.experiment.api.driver.IExperimentDriver;
import uk.ac.diamond.daq.experiment.api.plan.ExperimentPlanBean;

public class PlanSetupWizard extends Wizard {
	
	private final ExperimentPlanBean planBean;
	
	// pages
	private MetadataAndExperimentDriverPage metadataAndDriverPage;
	private SegmentsAndTriggersPage segmentsAndTriggersPage;
	private PlanSummaryPage planSummaryPage;
	
	private final ExperimentService experimentService;
	private final String experimentId;
	

	/**
	 * This constructor should be used to create a new experiment plan
	 */
	public PlanSetupWizard(ExperimentService experimentService, String experimentId) {
		this(experimentService, experimentId, new ExperimentPlanBean());
	}
	
	/**
	 * This constructor should be used to edit a previously defined experiment plan
	 */
	public PlanSetupWizard(ExperimentService experimentService, String experimentId, ExperimentPlanBean planBean) {
		this.experimentService = experimentService;
		this.experimentId = experimentId;
		Objects.requireNonNull(planBean);
		this.planBean = planBean;
	}

	@SuppressWarnings("unchecked")
	@Override
	public void addPages() {
		setWindowTitle("Setup experiment plan");
		
		@SuppressWarnings("rawtypes")
		Map<String, IExperimentDriver> drivers = Finder.getInstance().getFindablesOfType(IExperimentDriver.class);
		
		Map<IExperimentDriver<? extends DriverModel>, Set<String>> driverConfigs = new HashMap<>();
		for (@SuppressWarnings("rawtypes") Map.Entry<String, IExperimentDriver> driver : drivers.entrySet()) {
			driverConfigs.put(driver.getValue(), experimentService.getDriverProfileNames(driver.getKey(), experimentId));
		}
		
		metadataAndDriverPage = new MetadataAndExperimentDriverPage(experimentService, experimentId, planBean);
		metadataAndDriverPage.setExperimentDriverConfigurations(driverConfigs);
		
		addPage(metadataAndDriverPage);
		
		segmentsAndTriggersPage = new SegmentsAndTriggersPage(experimentService, experimentId, planBean);
		addPage(segmentsAndTriggersPage);
		
		planSummaryPage = new PlanSummaryPage();
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
