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

import org.eclipse.jface.resource.JFaceResources;
import org.eclipse.jface.wizard.Wizard;
import org.eclipse.swt.graphics.Font;

import uk.ac.diamond.daq.mapping.ui.experiment.ScanManagementController;

/**
 * Wizard to guide the user through the process of saving the scan currently configured in the mapping experiment view
 * to the clipboard and/or a file.
 * <p>
 * See the individual pages for details.
 */
public class CopyScanWizard extends Wizard {

	private final ChooseClassNameWizardPage chooseNamePage;
	private final CopyOrSaveScanWizardPage saveClassPage;

	protected static Font DEFAULT_FONT = JFaceResources.getFontRegistry().get("Cantarell");

	public CopyScanWizard(ScanManagementController controller, CopyScanConfig config) {
		chooseNamePage = new ChooseClassNameWizardPage(config);
		saveClassPage = new CopyOrSaveScanWizardPage(controller, config);
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
		return true;
	}
}
