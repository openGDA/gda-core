/*-
 * Copyright © 2017 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.experiment.focus;

import javax.inject.Inject;

import org.eclipse.e4.core.contexts.ContextInjectionFactory;
import org.eclipse.e4.core.contexts.IEclipseContext;
import org.eclipse.jface.wizard.Wizard;

/**
 * A wizard to run a focus scan (a.k.a. 'butterfly scan') and set the focus (zone plate) position.
 */
public class FocusScanWizard extends Wizard {

	@Inject
	private IEclipseContext injectionContext;

	private FocusScanResultPage resultPage;

	@Override
	public void addPages() {
		setWindowTitle("Configure Focus");

		addPage(ContextInjectionFactory.make(FocusScanSetupPage.class, injectionContext));
		resultPage = ContextInjectionFactory.make(FocusScanResultPage.class, injectionContext);
		addPage(resultPage);
	}

	@Override
	public boolean performCancel() {
		return resultPage.closeWizard();
	}

	@Override
	public boolean performFinish() {
		if (!resultPage.closeWizard()) {
			return false;
		}

		// move the zone plate to the selected value
		resultPage.setFocusPosition();

		return true;
	}

}
