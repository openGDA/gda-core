package uk.ac.diamond.daq.experiment.ui.driver;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Point;

import gda.factory.Finder;

public class TR6ConfigurationWizard extends Wizard {
	
	@Inject
	private IEclipseContext injectionContext;
	
	private String calibrationScannableName;
	
	public void setCalibrationScannableName(String name) {
		this.calibrationScannableName = name;
	}
	
	@Override
	public void addPages() {
		setWindowTitle("Configuring TR6");
		CalibrationPage calibrationPage = ContextInjectionFactory.make(CalibrationPage.class, injectionContext);
		calibrationPage.setScannable(Finder.getInstance().find(calibrationScannableName));
		addPage(calibrationPage);
		addPage(ContextInjectionFactory.make(ProfileSetupPage.class, injectionContext));
		addPage(ContextInjectionFactory.make(AbortConditionsPage.class, injectionContext));
	}

	@Override
	public boolean performFinish() {
		return true;
	}
	
	public Point getPreferredPageSize() {
		return new Point(750, 500);
	}

}
