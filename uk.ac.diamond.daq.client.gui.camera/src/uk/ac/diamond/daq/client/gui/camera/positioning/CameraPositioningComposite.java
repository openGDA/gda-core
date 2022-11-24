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

package uk.ac.diamond.daq.client.gui.camera.positioning;

import static uk.ac.gda.ui.tool.ClientSWTElements.createClientCompositeWithGridLayout;
import static uk.ac.gda.ui.tool.ClientSWTElements.createClientGridDataFactory;

import java.util.Arrays;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Widget;

import gda.rcp.views.CompositeFactory;
import uk.ac.diamond.daq.client.gui.camera.event.ChangeActiveCameraEvent;
import uk.ac.gda.client.composites.MotorCompositeFactory;
import uk.ac.gda.client.properties.camera.CameraConfigurationProperties;

/**
 * Assembles different {@link Composite} as control panel for a camera. Listen
 * to {@link ChangeActiveCameraEvent} events to update the components
 * accordingly.
 *
 * @author Maurizio Nagni
 */
public class CameraPositioningComposite implements CompositeFactory {

	private CameraConfigurationProperties camera;

	public CameraPositioningComposite(CameraConfigurationProperties camera){
		this.camera = camera;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {

		// Motors Components
		Composite motorCompositeArea = createClientCompositeWithGridLayout(parent, style, 1);
		createClientGridDataFactory().grab(true, true).applyTo(motorCompositeArea);

		Arrays.stream(motorCompositeArea.getChildren()).forEach(Widget::dispose);

		camera.getMotors().stream()
			.forEach(motor -> {
			MotorCompositeFactory mc = new MotorCompositeFactory(motor);
				mc.createComposite(motorCompositeArea,  SWT.HORIZONTAL);
			});

		motorCompositeArea.layout(true, true);

		return motorCompositeArea;
	}

}