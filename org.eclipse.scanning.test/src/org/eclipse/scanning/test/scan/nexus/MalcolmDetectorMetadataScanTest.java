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

package org.eclipse.scanning.test.scan.nexus;

import static org.hamcrest.MatcherAssert.assertThat;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.notNullValue;
import static org.hamcrest.Matchers.sameInstance;

import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.NexusException;
import org.eclipse.dawnsci.nexus.appender.SimpleNexusMetadataAppender;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.models.IMalcolmDetectorModel;
import org.eclipse.scanning.api.event.scan.DeviceState;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.malcolm.DummyMalcolmModel;
import org.eclipse.scanning.sequencer.ServiceHolder;
import org.junit.After;
import org.junit.Test;

public class MalcolmDetectorMetadataScanTest extends AbstractMalcolmScanTest {

	@Test
	public void testMalcolmDetectorMetadata() throws Exception {
		final List<IMalcolmDetectorModel> detModels = malcolmDevice.getModel().getDetectorModels();

		for (int i = 0; i < detModels.size(); i++) {
			final IMalcolmDetectorModel detModel = detModels.get(i);
			final Map<String, Object> detMetadata = new HashMap<>();
			detMetadata.put(NXdetector.NX_LOCAL_NAME, detModel.getName());
			detMetadata.put(NXdetector.NX_DESCRIPTION, "description of " + detModel.getName());
			detMetadata.put(NXdetector.NX_LAYOUT, "area");
			detMetadata.put(NXdetector.NX_DETECTOR_NUMBER, i + 1l);
			final SimpleNexusMetadataAppender<?> metadataAppender = new SimpleNexusMetadataAppender<>(detModel.getName());
			metadataAppender.setNexusMetadata(detMetadata);
			ServiceHolder.getNexusDeviceService().register(metadataAppender);
			assertThat(ServiceHolder.getNexusDeviceService().getDecorator(detModel.getName()), is(sameInstance(metadataAppender)));
		}

		final int[] shape = { 8, 5 };
		final IRunnableDevice<ScanModel> scanner = createMalcolmGridScan(malcolmDevice, output, false, shape);
		scanner.run(null);

		checkSize(scanner, shape);

		assertThat(scanner.getDeviceState(), is(DeviceState.ARMED));
		checkNexusFile(scanner, shape);
	}

	@After
	public void removeServices() {
		var serviceHolder = new ServiceHolder();
		serviceHolder.setNexusDeviceService(null);
	}

	@Override
	protected DummyMalcolmModel createMalcolmModel() {
		final DummyMalcolmModel model = createMalcolmModelTwoDetectors();
		model.setAxesToMove(Arrays.asList("stage_x", "stage_y" ));
		model.setPositionerNames(Arrays.asList("stage_x", "j1", "j2", "j3"));
		model.setMonitorNames(Arrays.asList("i0"));

		return model;
	}

	protected void checkNexusFile(IRunnableDevice<ScanModel> scanner, int[] shape) throws Exception {
		checkNexusFile(scanner, false, shape);

		final NXinstrument instrument = getNexusRoot(scanner).getEntry().getInstrument();
		for (IMalcolmDetectorModel detModel : malcolmDevice.getModel().getDetectorModels()) {
			checkMalcolmDetectorMetadata(instrument, detModel.getName());
		}
	}

	private void checkMalcolmDetectorMetadata(NXinstrument instrument, String name) throws NexusException {
		final NXdetector detector = instrument.getDetector(name);
		assertThat(detector, is(notNullValue()));
		SimpleNexusMetadataAppender<?> metadataAppender = (SimpleNexusMetadataAppender<?>) ServiceHolder.getNexusDeviceService().getDecorator(name);
		assertThat(metadataAppender, is(notNullValue()));
		for (Map.Entry<String, Object> metadataEntry : metadataAppender.getNexusMetadata().entrySet()) {
			// annoyingly there doesn't seem to be a way to get the scalar value of a field
			final DataNode dataNode = detector.getDataNode(metadataEntry.getKey());
			assertThat(dataNode, is(notNullValue()));
			if (dataNode.isString()) {
				assertThat(detector.getString(metadataEntry.getKey()), is(equalTo(metadataEntry.getValue())));
			} else {
				assertThat(detector.getNumber(metadataEntry.getKey()), is(equalTo(metadataEntry.getValue())));
			}
		}
	}

}
