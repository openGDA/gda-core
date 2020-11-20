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

package uk.ac.diamond.daq.client.gui.camera.settings;

import static uk.ac.gda.ui.tool.ClientSWTElements.createClientCompositeWithGridLayout;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.binning.BinningCompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.event.ChangeActiveCameraEvent;
import uk.ac.diamond.daq.client.gui.camera.exposure.ExposureDurationComposite;

/**
 * Assembles different {@link Composite} as control panel for a camera. Listen
 * to {@link ChangeActiveCameraEvent} events to update the components
 * accordingly.
 *
 * @author Maurizio Nagni
 */
public class CameraSettingsComposite implements CompositeFactory {

	public CameraSettingsComposite() {
		super();
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		Composite container = createClientCompositeWithGridLayout(parent, style, 2);

		// Exposure Component
		Composite exposureLengthComposite = new ExposureDurationComposite().createComposite(container, style);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(exposureLengthComposite);

		// Binning Component
		Composite binningCompositeArea = new BinningCompositeFactory().createComposite(container, style);
		createClientGridDataFactory().align(SWT.FILL, SWT.FILL).grab(true, true).applyTo(binningCompositeArea);

		return container;
	}
}
