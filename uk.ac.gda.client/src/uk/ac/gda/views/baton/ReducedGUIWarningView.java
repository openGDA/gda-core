/*-
 * Copyright © 2018 Diamond Light Source Ltd.
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

package uk.ac.gda.views.baton;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

public class ReducedGUIWarningView extends ViewPart {

	public static final String ID = "gda.rcp.views.baton.ReducedGUIWarningView";

	private final String explanation = "The baton is currently held by another user on a different visit, so you are restricted to the reduced GUI.";

	@Override
	public void createPartControl(Composite parent) {
		Label t = new Label(parent, SWT.WRAP);
		t.setText(explanation);
		t.setBackground(Display.getCurrent().getSystemColor(SWT.COLOR_YELLOW));
		parent.setLayout(new GridLayout(1, false));
		GridDataFactory.swtDefaults().align(SWT.CENTER, SWT.CENTER).grab(true, true).applyTo(t);
	}

	@Override
	public void setFocus() {
		// Do nothing
	}
}
