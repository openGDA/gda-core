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

package uk.ac.gda.server.ncd.samplerack;

import static java.util.stream.Collectors.toMap;

import java.util.List;
import java.util.Map;
import java.util.UUID;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import gda.device.DeviceException;
import uk.ac.diamond.daq.osgi.OsgiService;
import uk.ac.gda.api.remoting.ServiceInterface;

@OsgiService(SampleRackService.class)
@ServiceInterface(SampleRackService.class)
public class BasicSampleRackService implements SampleRackService {
	private static final Logger logger = LoggerFactory.getLogger(BasicSampleRackService.class);
	private String name;
	private Map<UUID, SampleRack> racks;

	@Override
	public void setName(String name) {
		this.name = name;
	}

	@Override
	public String getName() {
		return name;
	}

	@Override
	public List<SampleRack> getRacks() {
		return racks.values().stream().toList();
	}

	public void setRacks(List<SampleRack> racks) {
		this.racks = racks.stream().collect(toMap(SampleRack::getID, s -> s));
	}

	@Override
	public void runSamples(SampleRack rack, SampleConfiguration samples) {
			try {
				racks.get(rack.getID()).runSamples(samples);
			} catch (DeviceException e) {
				logger.error("Error running samples", e);
			}
	}

	@Override
	public void configureSampleRack(SampleRack rack, RackConfigurationInput rackConfigInput) {
		racks.get(rack.getID()).configureRack(rackConfigInput);
	}

	@Override
	public RackConfigurationInput getRackConfigurationInput(SampleRack rack) {
		return racks.get(rack.getID()).getRackConfigurationInput();
	}
}
