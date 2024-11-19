/*-
 * Copyright © 2024 Diamond Light Source Ltd.
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

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import gda.configuration.properties.LocalProperties;
import gda.epics.LazyPVFactory;
import gda.epics.PV;

@Component
public class PVSampleNameProvider implements SampleMetadataService {

	private static final String SAMPLE_NAME_PV = "sample.name.pv";

	private static final Logger logger = LoggerFactory.getLogger(PVSampleNameProvider.class);
	private final PV<String> channel;

	public PVSampleNameProvider() {
		channel = LazyPVFactory.newStringPV(LocalProperties.get(SAMPLE_NAME_PV));
	}

	@Override
	public String getSampleName() {
		try {
			return channel.get();
		} catch (IOException e) {
			logger.error("Error reading sample name PV", e);
			return "";
		}
	}

	@Override
	public void setSampleName(String name) {
		try {
			channel.putWait(name);
		} catch (IOException e) {
			logger.error("Error writing sample name PV", e);
		}
	}

}
