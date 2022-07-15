/*-
 * Copyright © 2020 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.experiment.copyscan;

import org.eclipse.jface.dialogs.DialogSettings;
import org.eclipse.jface.dialogs.IDialogSettings;
import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.scanning.api.event.scan.ScanBean;
import org.eclipse.swt.graphics.Font;
import org.eclipse.ui.PlatformUI;
import org.osgi.framework.FrameworkUtil;

/**
 * Wizard to guide the user through the process of saving the scan currently configured in the mapping experiment view
 * to the clipboard and/or a file.
 * <p>
 * See the individual pages for details.
 */
public class CopyScanWizard extends Wizard {

	protected static final String PROPERTY_NAME_CLASS_NAME = "className";

	protected static final String KEY_NAME_LAST_SAVE_LOCATION = "lastSaveLocation";
	protected static final String KEY_NAME_CLASS_NAME = "className";

	protected static final Font DEFAULT_FONT = JFaceResources.getFontRegistry().get("Cantarell");

	private final ChooseClassNameWizardPage chooseNamePage;
	private final CopyOrSaveScanWizardPage saveClassPage;

	private String className;

	public CopyScanWizard(ScanBean scanBean) {
		chooseNamePage = new ChooseClassNameWizardPage();
		saveClassPage = new CopyOrSaveScanWizardPage(scanBean);

		initializeDialogSettings();
		className = getDialogSettings().get(KEY_NAME_CLASS_NAME);
	}

	private void initializeDialogSettings() {
		final IDialogSettings bundleDialogSettings = PlatformUI.getDialogSettingsProvider(
				FrameworkUtil.getBundle(getClass())).getDialogSettings();
		final IDialogSettings dialogSettings = DialogSettings.getOrCreateSection(
				bundleDialogSettings, CopyScanWizard.class.getSimpleName());
		setDialogSettings(dialogSettings);
	}

	public String getClassName() {
		return className;
	}

	public void setClassName(String className) {
		this.className = className;
	}

	@Override
	public void addPages() {
		setWindowTitle("Save scan command");
		addPage(chooseNamePage);
		addPage(saveClassPage);
	}

	@Override
	public boolean canFinish() {
		return getContainer().getCurrentPage() == saveClassPage;
	}

	@Override
	public boolean performFinish() {
		getDialogSettings().put(KEY_NAME_CLASS_NAME, className);

		return true;
	}
}
