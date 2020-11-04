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

package uk.ac.diamond.daq.mapping.ui.services.position;

import java.util.List;
import java.util.Optional;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Service;

import gda.device.Scannable;
import uk.ac.gda.api.acquisition.parameters.DevicePositionDocument;
import uk.ac.gda.ui.tool.spring.FinderService;

/**
 * Provide {@link DevicePositionDocument}s on the available beamline findable devices.
 *
 * <p>
 * {@link #devicePositionAsDocument(String)} ability to identify a device type and produce a {@link DevicePositionDocument}
 * relies on the underlying <i>Chain of Responsibility</i> pattern composed by a list of {@link DeviceHandler} instances
 * each dedicated to handle a specific device type.
 * </p>
 *
 *  @author Maurizio Nagni
 *
 *  @see DeviceHandler
 */
@Service
public class DevicePositionDocumentService {

	@Autowired
	private FinderService finder;

	/**
	 * All the classes extending {@link DeviceHandler} are annotated with Spring {@link Component}
	 * @Autowired does the rest of the magic...
	 */
	@Autowired
	private List<DeviceHandler> handlers;

	/**
	 * The chain first element. If {@code null} means either no handlers or the class
	 * still has not created the responsibility chain.
	 */
	private DeviceHandler deviceHandler;

	/**
	 * Returns a {@link DevicePositionDocument} for the required findable device.
	 * @param device the device to interrogate
	 * @return the document otherwise {@code null} if the device is not available
	 */
	public final DevicePositionDocument devicePositionAsDocument(String device) {
		Optional<Scannable> scannable = Optional.empty();
		try {
			scannable = finder.getFindableObject(device);
		} catch (ClassCastException e) {
			// If the device is of an un-castable class, does nothing
		}
		return scannable
				.map(deviceHandler::handleDevice)
				.orElse(null);
	}

	/**
	 * Creates the responsibility chain with the handlers
	 */
	@PostConstruct
	private void initaliseService() {
		if (deviceHandler == null) {
			deviceHandler = handlers.get(0);
			handlers.stream()
				.reduce((a, b) -> {
					a.setNextHandler(b);
					return b;
			});
		}
	}
}
