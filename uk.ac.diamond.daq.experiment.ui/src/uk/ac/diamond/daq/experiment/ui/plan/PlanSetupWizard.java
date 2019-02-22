package uk.ac.diamond.daq.experiment.ui.plan;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import org.eclipse.jface.wizard.Wizard;

import gda.factory.Finder;
import uk.ac.diamond.daq.experiment.api.ExperimentService;
import uk.ac.diamond.daq.experiment.api.driver.IExperimentDriver;
import uk.ac.diamond.daq.experiment.api.plan.ExperimentPlanBean;

public class PlanSetupWizard extends Wizard {
	
	private ExperimentPlanBean planBean = new ExperimentPlanBean();
	
	// pages
	private MetadataAndExperimentDriverPage metadataAndDriverPage;
	private SegmentsAndTriggersPage segmentsAndTriggersPage;
	
	private final ExperimentService experimentService;
	private final String experimentId;
	
	public PlanSetupWizard(ExperimentService experimentService, String experimentId) {
		this.experimentService = experimentService;
		this.experimentId = experimentId;
	}
	
	@Override
	public void addPages() {
		setWindowTitle("Setup experiment plan");
		
		Map<String, IExperimentDriver> drivers = Finder.getInstance().getFindablesOfType(IExperimentDriver.class);
		
		Map<IExperimentDriver, Set<String>> driverConfigs = new HashMap<>();
		for (Map.Entry<String, IExperimentDriver> driver : drivers.entrySet()) {
			driverConfigs.put(driver.getValue(), experimentService.getDriverProfileNames(driver.getKey(), experimentId));
		}
		
		metadataAndDriverPage = new MetadataAndExperimentDriverPage(experimentService, experimentId);
		metadataAndDriverPage.setExperimentDriverConfigurations(driverConfigs);
		addPage(metadataAndDriverPage);
		
		segmentsAndTriggersPage = new SegmentsAndTriggersPage(experimentService, experimentId);
		addPage(segmentsAndTriggersPage);
	}
	
	@Override
	public boolean canFinish() {
		return metadataAndDriverPage.isPageComplete() && segmentsAndTriggersPage.isPageComplete();
	}

	@Override
	public boolean performFinish() {
		return true;
	}
	
	public ExperimentPlanBean getExperimentPlanBean() {
		planBean.setName(metadataAndDriverPage.getPlanName());
		planBean.setDescription(metadataAndDriverPage.getPlanDescription());
		planBean.setExperimentDriverName(metadataAndDriverPage.getExperimentDriverName());
		planBean.setExperimentDriverProfile(metadataAndDriverPage.getExperimentDriverProfile());
		
		planBean.setSegments(segmentsAndTriggersPage.getSegments());
		
		return planBean;
	}
}
