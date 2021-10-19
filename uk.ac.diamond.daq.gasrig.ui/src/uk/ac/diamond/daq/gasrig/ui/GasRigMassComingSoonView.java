/*-
 * Copyright © 2021 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.gasrig.ui;

import javax.annotation.PostConstruct;

import org.eclipse.jface.layout.GridDataFactory;
import org.eclipse.jface.layout.GridLayoutFactory;
import org.eclipse.jface.resource.FontDescriptor;
import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Display;
import org.eclipse.swt.widgets.Label;

import gda.factory.Finder;
import uk.ac.diamond.daq.gasrig.api.IGasRig;

public class GasRigMassComingSoonView {

	private IGasRig gasRig;

	@PostConstruct
	public void postConstruct(Composite parent) {

		gasRig = Finder.findOptionalSingleton(IGasRig.class).orElseThrow(() -> new RuntimeException("No gas rig found in configuration"));

		GridLayoutFactory.fillDefaults().numColumns(1).applyTo(parent);

		Label label = new Label(parent, SWT.NONE);
		GridDataFactory.fillDefaults()
			.grab(true, true)
			.align(SWT.FILL, SWT.CENTER)
			.applyTo(label);

		label.setAlignment(SWT.CENTER);
		label.setForeground(Display.getCurrent().getSystemColor(SWT.COLOR_RED));
		var fontDescriptor = FontDescriptor.createFrom(label.getFont()).setHeight(32).setStyle(SWT.BOLD);
		label.setFont(fontDescriptor.createFont(label.getDisplay()));

		label.setText("Coming Soon!");
	}
}
