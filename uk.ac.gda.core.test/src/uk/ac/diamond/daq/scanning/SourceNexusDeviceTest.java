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

package uk.ac.diamond.daq.scanning;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;

import org.eclipse.dawnsci.nexus.INexusDevice;
import org.eclipse.dawnsci.nexus.NXsource;

public class SourceNexusDeviceTest extends AbstractNexusMetadataDeviceTest<NXsource> {

	private static final String SOURCE_DEVICE_NAME = "source"; // the name of the NXsource group within its parent group. not used here
	private static final String EXPECTED_SOURCE_NAME = "Diamond Light Source"; // the value to set the name field to
	private static final String EXPECTED_TYPE= "Pulsed Muon Source";
	private static final String EXPECTED_PROBE = "muon";
	private static final String CURRENT_SCANNABLE_NAME = "current";

	@Override
	protected void setupMockScannables() throws Exception {
		createMockScannable(CURRENT_SCANNABLE_NAME, 12.34);
	}

	@Override
	protected INexusDevice<NXsource> setupNexusDevice() throws Exception {
		final SourceNexusDevice sourceDevice = new SourceNexusDevice();
		sourceDevice.setName(SOURCE_DEVICE_NAME);
		sourceDevice.setSourceName(EXPECTED_SOURCE_NAME);
		sourceDevice.setType(EXPECTED_TYPE);
		sourceDevice.setProbe(EXPECTED_PROBE);
		sourceDevice.setCurrentScannableName(CURRENT_SCANNABLE_NAME);
		return sourceDevice;
	}

	@Override
	protected void checkNexusObject(NXsource source) throws Exception {
		assertThat(source.getNumberOfDataNodes(), is(4));
		assertThat(source.getName(), is(equalTo(EXPECTED_SOURCE_NAME)));
		assertThat(source.getTypeScalar(), is(equalTo(EXPECTED_TYPE)));
		assertThat(source.getProbeScalar(), is(equalTo(EXPECTED_PROBE)));
		assertThat(source.getCurrentScalar(), is(equalTo(getScannableValue(CURRENT_SCANNABLE_NAME))));
	}

}
