package uk.ac.diamond.daq.experiment.ui.driver;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Point;

import gda.factory.Finder;
import uk.ac.diamond.daq.experiment.api.driver.ExperimentDriverModel;

public class TR6ConfigurationWizard extends Wizard {
	
	@Inject
	private IEclipseContext injectionContext;
	
	private String calibrationScannableName;
	
	private ProfileSetupPage profileSetupPage;
	
	public void setCalibrationScannableName(String name) {
		this.calibrationScannableName = name;
	}
	
	@Override
	public void addPages() {
		setWindowTitle("Configuring TR6");
		CalibrationPage calibrationPage = ContextInjectionFactory.make(CalibrationPage.class, injectionContext);
		calibrationPage.setScannable(Finder.getInstance().find(calibrationScannableName));
		addPage(calibrationPage);
		profileSetupPage = ContextInjectionFactory.make(ProfileSetupPage.class, injectionContext);
		addPage(profileSetupPage);
		addPage(ContextInjectionFactory.make(AbortConditionsPage.class, injectionContext));
	}

	@Override
	public boolean performFinish() {
		return true;
	}
	
	public Point getPreferredPageSize() {
		return new Point(750, 700);
	}

	public ExperimentDriverModel getProfile() {
		ExperimentDriverModel model = new ExperimentDriverModel();
		model.setProfile(profileSetupPage.getProfile());
		return model;
	}

	public String getName() {
		return profileSetupPage.getProfileName();
	}

}
