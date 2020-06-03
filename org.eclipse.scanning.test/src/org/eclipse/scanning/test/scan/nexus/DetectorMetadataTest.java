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

import static org.eclipse.scanning.test.scan.nexus.NexusAssert.assertScanNotFinished;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.HashMap;
import java.util.Map;

import org.eclipse.dawnsci.analysis.api.tree.DataNode;
import org.eclipse.dawnsci.nexus.NXdetector;
import org.eclipse.dawnsci.nexus.NXinstrument;
import org.eclipse.dawnsci.nexus.appender.NexusMetadataAppender;
import org.eclipse.scanning.api.device.IRunnableDevice;
import org.eclipse.scanning.api.device.IWritableDetector;
import org.eclipse.scanning.api.scan.models.ScanModel;
import org.eclipse.scanning.example.detector.MandelbrotModel;
import org.eclipse.scanning.sequencer.ServiceHolder;
import org.junit.BeforeClass;
import org.junit.Test;

public class DetectorMetadataTest extends NexusTest {

	private static IWritableDetector<MandelbrotModel> detector;

	@BeforeClass
	public static void before() throws Exception {
		MandelbrotModel model = createMandelbrotModel();
		detector = (IWritableDetector<MandelbrotModel>)runnableDeviceService.createRunnableDevice(model);
		assertNotNull(detector);
	}

	@Test
	public void testScanWithAddedDetectorMetadata() throws Exception {
		final Map<String, Object> metadata = new HashMap<>();
		metadata.put(NXdetector.NX_DESCRIPTION, "an example dummy detector that uses the mandelbrot set");
		metadata.put(NXdetector.NX_LOCAL_NAME, "mandelbrot");
		metadata.put(NXdetector.NX_TYPE, "dummy");
		metadata.put(NXdetector.NX_DETECTOR_READOUT_TIME, 0.015);
		final NexusMetadataAppender<?> metadataAppender = new NexusMetadataAppender<>(detector.getName());
		metadataAppender.setNexusMetadata(metadata);

		ServiceHolder.getNexusDeviceService().register(metadataAppender);

		final int[] shape = { 8, 5 };
		IRunnableDevice<ScanModel> scanner = createGridScan(detector, output, false, shape);
		assertScanNotFinished(getNexusRoot(scanner).getEntry());
		scanner.run(null);

		// Check we reached ready (it will normally throw an exception on error)
		checkNexusFile(scanner, shape, metadata); // Step model is +1 on the size
	}

	private void checkNexusFile(IRunnableDevice<ScanModel> scanner, int[] shape, Map<String, Object> metadata) throws Exception {
		checkNexusFile(scanner, false, shape);

		// check that the metadata has been added to the mandlebrot NXdetector object for the instrument
		final NXinstrument instrument = getNexusRoot(scanner).getEntry().getInstrument();
		final NXdetector detector = instrument.getDetector("mandelbrot");
		assertNotNull(detector);

		for (Map.Entry<String, Object> entry : metadata.entrySet()) {
			// annoyingly there doesn't seem to be a way to get the scalar value of a field without knowing the type
			DataNode dataNode = detector.getDataNode(entry.getKey());
			assertNotNull(dataNode);
			if (dataNode.isString()) {
				assertEquals(entry.getValue(), detector.getString(entry.getKey()));
			} else {
				assertEquals(entry.getValue(), detector.getNumber(entry.getKey()));
			}
		}
	}

}
