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
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;

public class StanfordAmplifiersComposite extends Composite {

	public StanfordAmplifiersComposite(Composite parent, int style) {
		super(parent, style);
		
		//Composite comp = new Composite(parent, SWT.NONE);
		//comp.setLayout(new GridLayout(1, false));
		
		new StanfordAmplifierComposite(parent, style, "I0");
		new StanfordAmplifierComposite(parent, style, "It");
		new StanfordAmplifierComposite(parent, style, "Iref");
	}
}
