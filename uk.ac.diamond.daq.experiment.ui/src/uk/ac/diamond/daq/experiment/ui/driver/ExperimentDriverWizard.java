package uk.ac.diamond.daq.experiment.ui.driver;

import static uk.ac.diamond.daq.experiment.api.Services.getExperimentService;

import java.util.Arrays;

import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.wizard.IWizardPage;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Point;

import uk.ac.diamond.daq.experiment.api.driver.DriverModel;

public class ExperimentDriverWizard extends Wizard {

	private final String experimentId;

	private ProfileManagerPage profileManagerPage;

	private String driverName;

	public ExperimentDriverWizard(String experimentId) {
		this.experimentId = experimentId;
	}

	@Override
	public void addPages() {
		addPage(new DriverSelectionPage());
		profileManagerPage = new ProfileManagerPage(experimentId);
		addPage(profileManagerPage);
		addPage(new ProfileEditorPage(this::saveProfile));
	}

	@Override
	public boolean performFinish() {
		return Arrays.asList(getPages()).stream().allMatch(IWizardPage::isPageComplete);
	}

	public Point getPreferredPageSize() {
		return new Point(1000, 550);
	}

	private boolean saveProfile(DriverModel profile) {
		final String profileName = profile.getName();
		if (getExperimentService().getDriverProfileNames(driverName, experimentId).contains(profileName)) {
			if (confirmOverwrite(profileName)) {
				getExperimentService().deleteDriverProfile(profile, driverName, experimentId);
			} else {
				return false;
			}
		}

		getExperimentService().saveDriverProfile(profile, driverName, experimentId);
		return true;
	}

	private boolean confirmOverwrite(String profileName) {
		return MessageDialog.openConfirm(getShell(), "Overwriting driver profile",
				"Are you sure you want to overwrite profile '" + profileName + "'?");
	}

	void setDriverName(String name) {
		this.driverName = name;
	}

	void shouldReloadData() {
		profileManagerPage.setDriverName(driverName);
	}

}
