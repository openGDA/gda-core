package uk.ac.diamond.daq.experiment.ui.plan;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.wizard.Wizard;

import gda.factory.Finder;
import uk.ac.diamond.daq.experiment.api.driver.IExperimentDriver;

public class PlanSetupWizard extends Wizard {
	
	@Inject
	private IEclipseContext injectionContext;
	
	// pages
	private MetadataAndExperimentDriverPage metadataAndDriverPage;
	private SegmentsAndTriggersPage segmentsAndTriggersPage;
	
	@Override
	public void addPages() {
		setWindowTitle("Setup experiment plan");

		// just for the demo!
		Map<String, IExperimentDriver> drivers = Finder.getInstance().getFindablesOfType(IExperimentDriver.class);
		Map<IExperimentDriver, List<String>> driverConfigs = new HashMap<>();
		
		for (Map.Entry<String, IExperimentDriver> driver : drivers.entrySet()) {
			driverConfigs.put(driver.getValue(), Arrays.asList("config" + Math.random()));
		}
		
		metadataAndDriverPage = ContextInjectionFactory.make(MetadataAndExperimentDriverPage.class, injectionContext);
		metadataAndDriverPage.setExperimentDriverConfigurations(driverConfigs);
		addPage(metadataAndDriverPage);
		
		segmentsAndTriggersPage = ContextInjectionFactory.make(SegmentsAndTriggersPage.class, injectionContext);
		addPage(segmentsAndTriggersPage);
	}
	
	@Override
	public boolean canFinish() {
		return metadataAndDriverPage.isPageComplete() && segmentsAndTriggersPage.isPageComplete();
	}

	@Override
	public boolean performFinish() {
		return false;
	}
}
