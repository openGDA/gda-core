/*-
 * Copyright © 2022 Diamond Light Source Ltd.
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

package uk.ac.diamond.daq.mapping.ui.controller;

import org.eclipse.swt.widgets.Display;
import org.springframework.context.ApplicationListener;

import uk.ac.gda.api.acquisition.AcquisitionKeys;
import uk.ac.gda.api.acquisition.resource.event.AcquisitionConfigurationResourceLoadEvent;
import uk.ac.gda.ui.tool.Reloadable;

/**
 * Listens to AcquisitionConfigurationResourceLoadEvent relating to the given
 * {@link AcquisitionKeys} and reloads the given {@link Reloadable} UI part.
 */
public class AcquisitionUiReloader implements ApplicationListener<AcquisitionConfigurationResourceLoadEvent> {

	private final AcquisitionKeys keys;
	private final Reloadable reloadable;

	public AcquisitionUiReloader(AcquisitionKeys keys, Reloadable reloadable) {
		this.keys = keys;
		this.reloadable = reloadable;
	}

	@Override
	public void onApplicationEvent(AcquisitionConfigurationResourceLoadEvent event) {
		if (!(event.getSource() instanceof ScanningAcquisitionController)) return;
		var controller = (ScanningAcquisitionController) event.getSource();

		if (controller.getAcquisitionKeys().equals(keys)) {
			Display.getDefault().asyncExec(reloadable::reload);
		}
	}

}
