/*-
 * Copyright © 2019 Diamond Light Source Ltd.
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

package uk.ac.gda.client.composites;

import org.eclipse.swt.SWT;
import org.eclipse.swt.widgets.Composite;
import org.eclipse.swt.widgets.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.Scannable;
import gda.rcp.views.AbstractPositionerComposite;
import gda.rcp.views.CompositeFactory;
import gda.rcp.views.EnumPositionerComposite;
import gda.rcp.views.SlimPositionerComposite;
import uk.ac.gda.client.exception.GDAClientException;
import uk.ac.gda.client.properties.MotorProperties;
import uk.ac.gda.core.tool.spring.SpringApplicationContextFacade;
import uk.ac.gda.ui.tool.spring.FinderService;

/**
 * A Composite to control a motor. This class automatically recognises Epics and ENUM motors displaying the proper layout.
 *
 * @author Maurizio Nagni
 */
public class MotorCompositeFactory implements CompositeFactory {

	private final MotorProperties motorProperties;

	private static final Logger logger = LoggerFactory.getLogger(MotorCompositeFactory.class);

	/**
	 * Creates a component based on text properties delegating to this component to find the motor associated
	 * {@link Scannable}
	 *
	 * @param motorProperties
	 */
	public MotorCompositeFactory(MotorProperties motorProperties) {
		this.motorProperties = motorProperties;
	}

	@Override
	public Composite createComposite(Composite parent, int style) {
		Scannable scannable;
		try {
			scannable = getScannable();
			if (scannable.getPosition() instanceof String) {
				return createEnumPositionComposite(parent, style, scannable);
			} else if (scannable.getPosition() instanceof Number) {
				return createSlimPositionerComposite(parent, style, scannable);
			}
		} catch (Exception e) {
			String message = "Could not get motor's position";
			new Label(parent, SWT.NONE).setText(message);
			logger.error(message, e);
		}
		return null;
	}

	private Composite createEnumPositionComposite(Composite parent, int style, Scannable scannable) {
		AbstractPositionerComposite abstractComposite = new EnumPositionerComposite(parent, style);
		abstractComposite.setScannable(scannable);
		return abstractComposite;
	}

	private Composite createSlimPositionerComposite(Composite parent, int style, Scannable scannable) {
		AbstractPositionerComposite abstractComposite = new SlimPositionerComposite(parent, style);
		abstractComposite.setScannable(scannable);
		return abstractComposite;
	}

	private Scannable getScannable() throws GDAClientException {
		return SpringApplicationContextFacade.getBean(FinderService.class)
				.getFindableObject(motorProperties.getController(), Scannable.class)
				.orElseThrow(GDAClientException::new);
	}
}
