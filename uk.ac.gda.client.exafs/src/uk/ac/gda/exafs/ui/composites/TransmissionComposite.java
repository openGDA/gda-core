/*-
 * Copyright © 2009 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.composites;

import org.eclipse.richbeans.api.binding.IBeanController;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

import uk.ac.gda.beans.exafs.DetectorParameters;
import uk.ac.gda.exafs.ExafsActivator;
import uk.ac.gda.exafs.ui.preferences.ExafsPreferenceConstants;

public class TransmissionComposite extends WorkingEnergyWithIonChambersComposite {

	public TransmissionComposite(Composite parent, int style, DetectorParameters abean, IBeanController control) {
		super(parent, style, abean);
		setLayout(new GridLayout());

		final Composite top = new Composite(this, SWT.NONE);
		top.setLayoutData(new GridData(SWT.FILL, SWT.CENTER, true, false));
		final GridLayout gridLayout = new GridLayout();
		gridLayout.numColumns = 2;
		top.setLayout(gridLayout);

		createDiffractionSection(top);

		if (!ExafsActivator.getDefault().getPreferenceStore().getDefaultBoolean(ExafsPreferenceConstants.HIDE_WORKING_ENERGY)) {
			createEdgeEnergy(top, control);
		}
		createIonChamberSection(abean, control);
	}
}
