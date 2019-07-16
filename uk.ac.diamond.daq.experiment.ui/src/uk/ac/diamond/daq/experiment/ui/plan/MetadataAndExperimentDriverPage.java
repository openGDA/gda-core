package uk.ac.diamond.daq.experiment.ui.plan;

import java.beans.PropertyChangeListener;
import java.util.Collections;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.WizardPage;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import gda.factory.Finder;
import uk.ac.diamond.daq.experiment.api.driver.IExperimentDriver;
import uk.ac.diamond.daq.experiment.api.plan.ExperimentPlanBean;


public class MetadataAndExperimentDriverPage extends WizardPage {
	
	private String experimentId;
	private ExperimentPlanBean planBean;
	
	private final PropertyChangeListener planBeanListener;
	
	private DriverAndProfileSelectionSection driverAndProfileSection;
	private NameAndDescriptionSection nameAndDescriptionSection;
	
	MetadataAndExperimentDriverPage(String experimentId, ExperimentPlanBean planBean) {
		super(MetadataAndExperimentDriverPage.class.getSimpleName());
		setTitle("Metadata and experiment driver");
		setDescription("Add a title and description to your plan, and select experiment driver configuration if required");
		
		this.experimentId = experimentId;
		this.planBean = planBean;
		
		this.planBeanListener = event -> setPageComplete(isPageComplete());
		this.planBean.addPropertyChangeListener(planBeanListener);
		
	}

	@Override
	public void createControl(Composite parent) {
		Composite composite = new Composite(parent, SWT.NONE);
		GridDataFactory.swtDefaults().applyTo(composite);
		GridLayoutFactory.swtDefaults().applyTo(composite);
		
		nameAndDescriptionSection = new NameAndDescriptionSection(planBean);
		nameAndDescriptionSection.createSection(composite);
		
		driverAndProfileSection = new DriverAndProfileSelectionSection(planBean, experimentId);
		driverAndProfileSection.createSection(composite);
		
		setPageComplete(isPageComplete());
		
		composite.addDisposeListener(e -> planBean.removePropertyChangeListener(planBeanListener));
		
		setControl(composite);
	}
	
	@Override
	public boolean isPageComplete() {
		return nameAndDescriptionSection.validSelection() && driverAndProfileSection.validSelection();
	}
	
	@Override
	public IWizardPage getNextPage() {
		SegmentsAndTriggersPage nextPage = (SegmentsAndTriggersPage) super.getNextPage();
		if (planBean.isDriverUsed()) {
			IExperimentDriver<?> driver = Finder.getInstance().find(planBean.getExperimentDriverName());
			nextPage.setSevs(driver.getReadoutNames());
		} else {
			nextPage.setSevs(Collections.emptySet());
		}
		return nextPage;
	}

	public String getPlanName() {
		return planBean.getPlanName();
	}
	
	public String getPlanDescription() {
		return planBean.getPlanDescription();
	}
	
	public String getExperimentDriverName() {
		return planBean.getExperimentDriverName();
	}

	public String getExperimentDriverProfile() {
		return planBean.getExperimentDriverProfile();
	}

}
