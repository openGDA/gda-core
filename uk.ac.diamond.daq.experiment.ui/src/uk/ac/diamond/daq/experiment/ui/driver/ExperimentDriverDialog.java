package uk.ac.diamond.daq.experiment.ui.driver;

import static uk.ac.diamond.daq.experiment.ui.ExperimentUiUtils.MINUS_ICON;
import static uk.ac.diamond.daq.experiment.ui.ExperimentUiUtils.PLUS_ICON;
import static uk.ac.gda.ui.tool.ClientSWTElements.STRETCH;
import static uk.ac.gda.ui.tool.ClientSWTElements.composite;
import static uk.ac.gda.ui.tool.ClientSWTElements.innerComposite;
import static uk.ac.gda.ui.tool.ClientSWTElements.label;

import java.util.Set;

import org.eclipse.jface.dialogs.Dialog;
import org.eclipse.jface.dialogs.IDialogConstants;
import org.eclipse.jface.dialogs.MessageDialog;
import org.eclipse.jface.viewers.ArrayContentProvider;
import org.eclipse.jface.viewers.ComboViewer;
import org.eclipse.jface.viewers.IStructuredSelection;
import org.eclipse.jface.viewers.StructuredSelection;
import org.eclipse.jface.window.Window;
import org.eclipse.swt.SWT;
import org.eclipse.swt.graphics.Point;
import org.eclipse.swt.widgets.Button;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Control;
import org.eclipse.swt.widgets.Shell;
import org.eclipse.swt.widgets.Text;

import gda.factory.Finder;
import uk.ac.diamond.daq.experiment.api.Services;
import uk.ac.diamond.daq.experiment.api.driver.DriverModel;
import uk.ac.diamond.daq.experiment.api.driver.IExperimentDriver;
import uk.ac.diamond.daq.experiment.api.driver.SingleAxisLinearSeries;
import uk.ac.diamond.daq.experiment.ui.ExperimentUiUtils;

/*-
 * Copyright © 2022 Diamond Light Source Ltd.
 *
 * This file is part of GDA.
 *
 * GDA is free software: you can redistribute it and/or modify it under the
 * terms of the GNU General Public License version 3 as published by the Free
 * Software Foundation.
 *
 * GDA is distributed in the hope that it will be useful, but WITHOUT ANY
 * WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along
 * with GDA. If not, see <http://www.gnu.org/licenses/>.
 */

public class ExperimentDriverDialog extends Dialog {

	private final String experimentId;

	/** list of available drivers */
	private ComboViewer drivers;

	/** existing profiles for the selected driver */
	private ComboViewer profiles;

	/** creates new profile for selected driver */
	private Button addProfile;

	/** deletes selected profile */
	private Button deleteProfile;

	/** edits the selected profile */
	private ProfileEditor editor;

	public ExperimentDriverDialog(Shell parent, String experimentId) {
		super(parent);

		this.experimentId = experimentId;
	}

	@Override
	protected void configureShell(Shell newShell) {
		super.configureShell(newShell);
		newShell.setText("Configure experiment driver profiles");
	}

	@Override
	protected Point getInitialSize() {
		return new Point(1000, 600);
	}

	@Override
	protected Control createDialogArea(Composite parent) {
		var base = composite(parent, 1);

		createSelectionControls(base);
		createProfileEditor(base);

		attachListeners();

		return base;
	}

	private void createSelectionControls(Composite parent) {
		var selections = composite(parent, 2, false);
		createDriverSelection(selections);
		createProfileSelection(selections);
	}

	private void createDriverSelection(Composite composite) {
		label(composite, "Experiment driver");
		drivers = new ComboViewer(composite);
		drivers.setContentProvider(ArrayContentProvider.getInstance());
		drivers.setInput(Finder.getFindablesOfType(IExperimentDriver.class).keySet());
		STRETCH.applyTo(drivers.getCombo());
	}

	private void createProfileSelection(Composite parent) {
		label(parent, "Profile");

		var composite = innerComposite(parent, 3, false);
		profiles = new ComboViewer(composite);
		profiles.setContentProvider(ArrayContentProvider.getInstance());
		STRETCH.applyTo(profiles.getControl());

		addProfile = new Button(composite, SWT.PUSH);
		addProfile.setText("Add");
		var addIcon = ExperimentUiUtils.getImage(PLUS_ICON);
		addProfile.setImage(addIcon);
		addProfile.setEnabled(false);

		deleteProfile = new Button(composite, SWT.PUSH);
		deleteProfile.setText("Delete");
		var deleteIcon = ExperimentUiUtils.getImage(MINUS_ICON);
		deleteProfile.setImage(deleteIcon);
		deleteProfile.setEnabled(false);

		composite.addDisposeListener(dispose -> {
			addIcon.dispose();
			deleteIcon.dispose();
		});
	}

	private void createProfileEditor(Composite parent) {
		var composite = composite(parent, 1);
		editor = new ProfileEditor();
		editor.createControl(composite);
	}

	private void attachListeners() {

		// when changing driver, populate list of profiles relating to this selection
		drivers.addSelectionChangedListener(event -> {
			var driverName = (String) ((IStructuredSelection) event.getSelection()).getFirstElement();
			populateProfiles(driverName);
			toggleButtons();
			editor.clear();
		});

		// when profile selected, load into editor
		profiles.addSelectionChangedListener(event -> {
			var profileName = (String) ((IStructuredSelection) event.getSelection()).getFirstElement();
			DriverModel profile = Services.getExperimentService().getDriverProfile(drivers.getCombo().getText(), profileName, experimentId);
			editor.setModel((SingleAxisLinearSeries) profile);
			toggleButtons();
		});

		addProfile.addListener(SWT.Selection, event -> newProfile());

		deleteProfile.addListener(SWT.Selection, event -> deleteProfile());
	}

	private void populateProfiles(String driverName) {
		Set<String> profileNames = Services.getExperimentService().getDriverProfileNames(driverName, experimentId);
		profiles.setInput(profileNames);
	}

	private void newProfile() {
		var newProfileWizard = new NewProfileName(getShell());
		if (newProfileWizard.open() != Window.OK) return;

		var driverName = drivers.getCombo().getText();
		IExperimentDriver<SingleAxisLinearSeries> driver = Finder.find(driverName);

		var name = newProfileWizard.getName();
		if (name.isBlank()) return;
		var model = new SingleAxisLinearSeries(driver.getQuantityName());
		model.setName(name);

		Services.getExperimentService().saveDriverProfile(model, driverName, experimentId);

		populateProfiles(driverName);

		profiles.setSelection(new StructuredSelection(name));
		toggleButtons();
	}

	private void toggleButtons() {
		boolean driverSelected = !drivers.getSelection().isEmpty();
		boolean profileSelected = !profiles.getSelection().isEmpty();

		addProfile.setEnabled(driverSelected);
		deleteProfile.setEnabled(profileSelected);
	}

	private class NewProfileName extends MessageDialog {

		private String name;

		public NewProfileName(Shell shell) {
			super(shell, "New profile", null, "Enter profile name", INFORMATION, 0, IDialogConstants.OK_LABEL, IDialogConstants.CANCEL_LABEL);
		}

		@Override
		protected void configureShell(Shell newShell) {
			super.configureShell(newShell);
			newShell.setText("New profile");
		}

		@Override
		protected Control createCustomArea(Composite parent) {
			var composite = composite(parent, 1);

			var nameBox = new Text(composite, SWT.BORDER);
			STRETCH.applyTo(nameBox);
			nameBox.addListener(SWT.Modify, event -> name = nameBox.getText());

			return composite;
		}

		public String getName() {
			return name;
		}
	}

	private void deleteProfile() {
		var driverName = drivers.getCombo().getText();
		var profileName = profiles.getCombo().getText();
		var profile = Services.getExperimentService().getDriverProfile(driverName, profileName, experimentId);

		if (MessageDialog.openConfirm(getShell(), "Delete profile", "Are you sure you wish to delete profile '" + profileName + "'?")) {
			Services.getExperimentService().deleteDriverProfile(profile, driverName, experimentId);

			populateProfiles(driverName);
			toggleButtons();
			editor.clear();
		}

	}

	@Override
	protected void okPressed() {
		if (!drivers.getStructuredSelection().isEmpty() && !profiles.getStructuredSelection().isEmpty()) {
			String driver = drivers.getStructuredSelection().getFirstElement().toString();
			editor.saveProfile(driver, experimentId);
		}
		super.okPressed();
	}

}
