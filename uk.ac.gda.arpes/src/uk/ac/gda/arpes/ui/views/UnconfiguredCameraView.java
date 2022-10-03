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

package uk.ac.gda.arpes.ui.views;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.swt.SWT;
import org.eclipse.swt.layout.GridLayout;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.eclipse.ui.part.ViewPart;

public class UnconfiguredCameraView extends ViewPart {

	@Override
	public void createPartControl(Composite parent) {
		parent.setLayout(new GridLayout(1, true));
		Label label = new Label(parent, SWT.NONE);
		label.setAlignment(SWT.CENTER);
		label.setText("This camera has not been configured yet");
		GridDataFactory.fillDefaults().grab(true,  true).align(SWT.CENTER, SWT.CENTER).applyTo(label);
	}

	@Override
	public void setFocus() {
		// TODO Auto-generated method stub
	}
}