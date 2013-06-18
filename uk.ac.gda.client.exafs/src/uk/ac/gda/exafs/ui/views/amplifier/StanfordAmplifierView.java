/*-
 * Copyright © 2013 Diamond Light Source Ltd.
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

package uk.ac.gda.exafs.ui.views.amplifier;

import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridData;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.ui.part.ViewPart;

public class StanfordAmplifierView extends ViewPart {
	public StanfordAmplifierView() {
	}

	@Override
	public void createPartControl(Composite parent) {
		
		Composite composite = new Composite(parent, SWT.NONE);
		composite.setLayout(new GridLayout(1, false));
		StanfordAmplifiersComposite stanfordAmplifiersComposite = new StanfordAmplifiersComposite(composite, SWT.None);
		GridData gd_stanfordAmplifiersComposite = new GridData(SWT.LEFT, SWT.CENTER, false, false, 1, 1);
		gd_stanfordAmplifiersComposite.exclude = true;
		stanfordAmplifiersComposite.setLayoutData(gd_stanfordAmplifiersComposite);
	}

	@Override
	public void setFocus() {
	}

}
